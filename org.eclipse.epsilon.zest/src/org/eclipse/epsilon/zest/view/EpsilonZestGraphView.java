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
import org.eclipse.epsilon.zest.view.dialogs.SaveAsImageAction;
import org.eclipse.gef4.graph.Edge;
import org.eclipse.gef4.graph.Graph;
import org.eclipse.gef4.graph.Node;
import org.eclipse.gef4.layout.ILayoutAlgorithm;
import org.eclipse.gef4.zest.fx.ZestProperties;
import org.eclipse.gef4.zest.fx.ui.ZestFxUiModule;
import org.eclipse.gef4.zest.fx.ui.parts.ZestFxUiView;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;

import com.google.inject.Guice;
import com.google.inject.util.Modules;

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
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		javafx.scene.Node node = getContentViewer().getRootPart().getVisual();
		Action saveAsImageAction = new SaveAsImageAction(node);

		IActionBars actionBars = getViewSite().getActionBars();
		IMenuManager dropDownMenu = actionBars.getMenuManager();
		IToolBarManager toolBar = actionBars.getToolBarManager();
		dropDownMenu.add(saveAsImageAction);
		toolBar.add(saveAsImageAction);
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
	 * 
	 * @param layoutAlgo
	 */
	public void load(IEolExecutableModule newModule, ILayoutAlgorithm algorithm) {
		graph = new Graph();

		EpsilonZestProperties.setView(graph, this);

		disposeModule();
		moduleWrapper = new EpsilonZestModuleWrapper(newModule);
		object2Node = new HashMap<>();

		Display.getDefault().syncExec(new Runnable() {
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

		if (!graph.getNodes().isEmpty()) {
			// Some layout algorithms do not deal well with empty graphs
			// (e.g. Grid) - avoid specifying one in this case
			ZestProperties.setLayoutAlgorithm(graph, algorithm);
		}
		setGraph(graph);
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
			ZestProperties.setLabel(n, moduleWrapper.getNodeLabel(nodeObject));
			EpsilonZestProperties.setModelElement(n, nodeObject);

			graph.getNodes().add(n);
			object2Node.put(nodeObject, n);
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
