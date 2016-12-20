package org.eclipse.epsilon.zest.view;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.epsilon.eol.IEolExecutableModule;
import org.eclipse.epsilon.eol.execute.context.IEolContext;
import org.eclipse.epsilon.zest.eol.EpsilonZestModuleWrapper;
import org.eclipse.epsilon.zest.graph.EpsilonZestEdge;
import org.eclipse.epsilon.zest.graph.EpsilonZestNode;
import org.eclipse.epsilon.zest.utils.ArrowTypes;
import org.eclipse.gef4.geometry.planar.Dimension;
import org.eclipse.gef4.graph.Edge;
import org.eclipse.gef4.graph.Graph;
import org.eclipse.gef4.graph.Node;
import org.eclipse.gef4.layout.LayoutProperties;
import org.eclipse.gef4.layout.algorithms.SpringLayoutAlgorithm;
import org.eclipse.gef4.mvc.parts.IContentPart;
import org.eclipse.gef4.zest.fx.ZestProperties;
import org.eclipse.gef4.zest.fx.parts.NodePart;
import org.eclipse.gef4.zest.fx.ui.ZestFxUiModule;
import org.eclipse.gef4.zest.fx.ui.parts.ZestFxUiView;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.views.properties.IPropertySource;

import com.google.inject.Guice;
import com.google.inject.util.Modules;

import javafx.geometry.Bounds;

/**
 * View that populates a Zest graph-based visualization from an EOL script with
 * a set of context operations. Node identity is done through
 * {@link Object#hashCode()}.
 */
public class EpsilonZestGraphView extends ZestFxUiView {

	public static enum MissingNodeHandling {
		ADD_MISSING, SKIP_MISSING;
	}

	public static final String ID = "org.eclipse.epsilon.zest.view";

	private Graph graph = new Graph();
	private Map<Object, EpsilonZestNode> object2Node;
	private EpsilonZestModuleWrapper moduleWrapper;

	public EpsilonZestGraphView() {
		super(Guice.createInjector(Modules.override(new EpsilonZestGraphModule()).with(new ZestFxUiModule())));
		setGraph(graph);
	}

	@Override
	public void dispose() {
		setGraph(null);
		super.dispose();
		disposeModule();
	}

	/**
	 * Repopulates the build using an EOL module. This method can be called from
	 * any thread.
	 */
	public void load(IEolExecutableModule newModule) {
		graph = new Graph();

		final SpringLayoutAlgorithm algorithm = new SpringLayoutAlgorithm();
		ZestProperties.setLayoutAlgorithm(graph, algorithm);
		EpsilonZestProperties.setView(graph, this);
		setGraph(graph);

		disposeModule();
		moduleWrapper = new EpsilonZestModuleWrapper(newModule);
		object2Node = new HashMap<>();

		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				Iterable<Object> nodeObjects = moduleWrapper.getInitialNodes();

				// First loop: create nodes, set labels
				for (Object nodeObject : nodeObjects) {
					mapToNode(nodeObject);
				}

				// Second loop: create edges (ignore edges to non-initial nodes
				// for now)
				for (final Object sourceObject : object2Node.keySet()) {
					expandOutgoing(sourceObject, MissingNodeHandling.SKIP_MISSING);
				}
			}
		});
	}

	public EpsilonZestModuleWrapper getModuleWrapper() {
		return moduleWrapper;
	}

	/**
	 * <p>
	 * Computes and shows the outgoing edges of a certain model element, using
	 * EOL. This method assumes it will be called from the UI thread - failing
	 * to do so will result in exceptions.
	 * </p>
	 *
	 * <p>
	 * This method can be used in two ways, depending on <code>mode</code>:
	 * </p>
	 * <ul>
	 * <li>{@link MissingNodeHandling#ADD_MISSING}: create missing target nodes
	 * on demand before creating the edge.</li>
	 * <li>{@link MissingNodeHandling#SKIP_MISSING}: do not create edges for
	 * missing target nodes - skip creating the edge.</li>
	 * </ul>
	 */
	protected void expandOutgoing(final Object sourceObject, MissingNodeHandling mode) {
		for (Entry<String, Iterable<Object>> entry : moduleWrapper.getOutgoing(sourceObject).entrySet()) {
			String label = entry.getKey();
			for (Object targetObject : entry.getValue()) {
				mapToEdge(sourceObject, targetObject, label, mode);
			}
		}
	}

	/**
	 * Returns a property source for the specified Epsilon Zest node.
	 */
	protected IPropertySource getPropertySource(final EpsilonZestNode node) throws Exception {
		return new EpsilonZestNodePropertySource(node);
	}

	/**
	 * Set the visual properties and labels of the node using the provided EOL script.
	 */
	protected void styleNode(EpsilonZestNode n) {
		Object nodeObject = EpsilonZestProperties.getModelElement(n);
		ZestProperties.setLabel(n, moduleWrapper.getNodeLabel(nodeObject));

		IContentPart<javafx.scene.Node, ? extends javafx.scene.Node> part = getContentViewer().getContentPartMap().get(n);
		if (part instanceof NodePart) {
			javafx.scene.Node visual = part.getVisual();
			Bounds hostBounds = visual.getLayoutBounds();
			final double minx = hostBounds.getMinX();
			final double miny = hostBounds.getMinY();
			final double maxx = hostBounds.getMaxX();
			final double maxy = hostBounds.getMaxY();
			final Dimension preLayoutSize = new Dimension(maxx - minx, maxy - miny);

			// "layout_size" doesn't seem to work, but I can't find "size" in the
			// GEF code either - possibly a JavaFX-specific part?
			LayoutProperties.setSize(n, preLayoutSize.getCopy());
			n.getAttributes().put("size", preLayoutSize.getCopy());
		}
	}

	/**
	 * Maps a reference from the source to the target into an edge with the
	 * specified label. This 
	 */
	private Edge mapToEdge(final Object source, final Object target, final String label,
			final MissingNodeHandling mode) {
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

	private EpsilonZestNode mapToNode(Object nodeObject) {
		EpsilonZestNode n = object2Node.get(nodeObject);

		if (n == null) {
			n = new EpsilonZestNode();
			n.setGraph(graph);
			graph.getNodes().add(n);
			object2Node.put(nodeObject, n);
			EpsilonZestProperties.setModelElement(n, nodeObject);

			styleNode(n);
		}

		return n;
	}

	private void disposeModule() {
		if (moduleWrapper != null) {
			IEolContext context = moduleWrapper.getModule().getContext();

			// Models are always handled from the UI thread
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					context.getModelRepository().dispose();
				}
			});

			context.getExecutorFactory().getExecutionController().dispose();
			moduleWrapper = null;
		}
	}
}
