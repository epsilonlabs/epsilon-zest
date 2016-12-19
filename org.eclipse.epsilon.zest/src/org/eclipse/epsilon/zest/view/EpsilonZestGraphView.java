package org.eclipse.epsilon.zest.view;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.epsilon.eol.IEolExecutableModule;
import org.eclipse.epsilon.eol.dom.Operation;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.execute.context.IEolContext;
import org.eclipse.epsilon.zest.EpsilonZestPlugin;
import org.eclipse.epsilon.zest.graph.EpsilonZestEdge;
import org.eclipse.epsilon.zest.graph.EpsilonZestNode;
import org.eclipse.epsilon.zest.utils.ArrowTypes;
import org.eclipse.gef4.graph.Edge;
import org.eclipse.gef4.graph.Graph;
import org.eclipse.gef4.graph.Node;
import org.eclipse.gef4.layout.algorithms.SpringLayoutAlgorithm;
import org.eclipse.gef4.zest.fx.ZestProperties;
import org.eclipse.gef4.zest.fx.ui.ZestFxUiModule;
import org.eclipse.gef4.zest.fx.ui.parts.ZestFxUiView;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.views.properties.IPropertySource;

import com.google.common.collect.HashBiMap;
import com.google.inject.Guice;
import com.google.inject.util.Modules;

/**
 * View that populates a Zest graph-based visualization from an EOL script with
 * a set of context operations. Node identity is done through
 * {@link Object#hashCode()}.
 *
 * TODO: extend {@link #getEdgeLabel(Object, Object)} so it can return a
 * collection of edges and not just one - otherwise we won't be able to have
 * more than one edge connecting a pair of nodes.
 */
public class EpsilonZestGraphView extends ZestFxUiView {

	public static enum MissingNodeHandling {
		ADD_MISSING, SKIP_MISSING;
	}

	public static final String ID = "org.eclipse.epsilon.zest.view";

	private static final String OP_NODE_LABEL = "nodeLabel";
	private static final String OP_OUTGOING = "outgoing";

	private Graph graph = new Graph();
	private Map<Object, EpsilonZestNode> object2Node = new HashMap<>();
	private IEolExecutableModule module;

	public EpsilonZestGraphView() {
		super(Guice.createInjector(Modules.override(new EpsilonZestGraphModule()).with(new ZestFxUiModule())));
		setGraph(graph);
	}

	@Override
	public void dispose() {
		super.dispose();
		disposeModule();
	}

	public void load(IEolExecutableModule newModule) {
		graph = new Graph();
		ZestProperties.setLayoutAlgorithm(graph, new SpringLayoutAlgorithm());
		EpsilonZestProperties.setView(graph, this);
		setGraph(graph);

		disposeModule();
		module = newModule;
		object2Node = HashBiMap.create();

		Display.getDefault().asyncExec(new Runnable(){
			@Override
			public void run() {
				Iterable<Object> nodeObjects = getInitialNodes();

				// First loop: create nodes, set labels
				for (Object nodeObject : nodeObjects) {
					mapToNode(nodeObject);
				}

				// Second loop: create edges (ignore edges to non-initial nodes for now)
				for (final Object sourceObject : object2Node.keySet()) {
					expandOutgoing(sourceObject, MissingNodeHandling.SKIP_MISSING);
				}
			}
		});
	}

	public void expandOutgoing(final Object sourceObject, MissingNodeHandling mode) {
		for (Entry<String, Object> entry : getOutgoing(sourceObject).entrySet()) {
			String label = entry.getKey();
			for (Object targetObject : adaptToIterable(entry.getValue())) {
				mapToEdge(sourceObject, targetObject, label, mode);
			}
		}
	}

	public Edge mapToEdge(final Object source, final Object target, final String label, final MissingNodeHandling mode) {
		final Node sourceNode = object2Node.get(source);
		assert sourceNode != null : "The source node should already exist";

		// Create target node if it does not exist
		Node targetNode = object2Node.get(target);
		if (targetNode == null) {
			switch (mode) {
			case ADD_MISSING:
				targetNode = mapToNode(target);
				break;
			case SKIP_MISSING:
				return null;
			}
		}

		// Avoid having duplicate edges
		for (Edge e : sourceNode.getAllOutgoingEdges()) {
			if (e.getTarget() == targetNode) {
				return e;
			}
		}

		// Create new edge
		Edge e = new EpsilonZestEdge(sourceNode, targetNode);
		ZestProperties.setLabel(e, label);
		ZestProperties.setTargetDecoration(e, ArrowTypes.filledTriangle());
		e.setGraph(graph);
		graph.getEdges().add(e);

		return e;
	}

	public EpsilonZestNode mapToNode(Object nodeObject) {
		EpsilonZestNode n = object2Node.get(nodeObject);

		if (n == null) {
			n = new EpsilonZestNode();
			ZestProperties.setLabel(n, getNodeLabel(nodeObject));
			n.setGraph(graph);
			graph.getNodes().add(n);
			object2Node.put(nodeObject, n);
		}

		return n;
	}

	protected void disposeModule() {
		if (module != null) {
			IEolContext context = module.getContext();

			// Models are always handled from the UI thread
			Display.getDefault().syncExec(new Runnable(){
				@Override
				public void run() {
					context.getModelRepository().dispose();
				}
			});

			context.getExecutorFactory().getExecutionController().dispose();
			module = null;
		}
	}

	public String getNodeLabel(Object nodeObject) {
		try {
			final IEolContext ctx = module.getContext();
			final Operation opLabel = module.getOperations().getOperation(nodeObject, OP_NODE_LABEL,
					Collections.emptyList(), ctx);

			if (opLabel == null) {
				EpsilonZestPlugin.getDefault()
						.logWarning("Object " + nodeObject + " has no " + OP_NODE_LABEL + " context operation");
				return "<NONE>";
			} else {
				return opLabel.execute(nodeObject, Collections.emptyList(), ctx) + "";
			}
		} catch (EolRuntimeException e) {
			EpsilonZestPlugin.getDefault().logException(e);
			return "<ERROR>";
		}
	}

	public Map<String, Object> getOutgoing(Object nodeObject) {
		try {
			final IEolContext ctx = module.getContext();
			final Operation opLabel = module.getOperations().getOperation(nodeObject, OP_OUTGOING,
					Collections.emptyList(), ctx);

			if (opLabel == null) {
				EpsilonZestPlugin.getDefault()
						.logWarning("Object " + nodeObject + " has no " + OP_OUTGOING + " context operation");
				return Collections.emptyMap();
			} else {
				Object result = opLabel.execute(nodeObject, Collections.emptyList(), ctx);
				return adaptToStringMap(result);
			}
		} catch (EolRuntimeException e) {
			EpsilonZestPlugin.getDefault().logException(e);
			return Collections.emptyMap();
		}
	}

	public IPropertySource getNodePropertySource(Object modelElement) {
		return new EpsilonZestElementPropertySource(modelElement);
	}

	public Iterable<Object> getInitialNodes() {
		for (Operation op : module.getOperations()) {
			if (op.hasAnnotation("initial")) {
				try {
					Object result = op.execute(null, Collections.emptyList(), module.getContext());
					return adaptToIterable(result);
				} catch (EolRuntimeException e) {
					EpsilonZestPlugin.getDefault().logException(e);
				}
			}
		}
		return Collections.emptyList();
	}

	@SuppressWarnings("unchecked")
	protected Map<String, Object> adaptToStringMap(Object result) {
		if (result instanceof Map) {
			return (Map<String, Object>)result;
		} else if (result instanceof Iterable) {
			return Collections.singletonMap("out", result);
		} else {
			return Collections.singletonMap("out", Collections.singletonList(result));
		}
	}

	@SuppressWarnings("unchecked")
	protected Iterable<Object> adaptToIterable(Object result) {
		if (result instanceof Iterable) {
			return (Iterable<Object>) result;
		} else {
			return Collections.singleton(result);
		}
	}

}
