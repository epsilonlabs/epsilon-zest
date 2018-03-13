package org.eclipse.epsilon.zest.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.epsilon.eol.IEolModule;
import org.eclipse.epsilon.eol.execute.context.IEolContext;
import org.eclipse.epsilon.zest.eol.EpsilonZestModuleWrapper;
import org.eclipse.epsilon.zest.graph.EpsilonZestEdge;
import org.eclipse.epsilon.zest.graph.EpsilonZestEllipsisNode;
import org.eclipse.epsilon.zest.graph.EpsilonZestNode;
import org.eclipse.epsilon.zest.graph.EpsilonZestObjectNode;
import org.eclipse.epsilon.zest.utils.ArrowTypes;
import org.eclipse.epsilon.zest.view.dialogs.SaveAsImageAction;
import org.eclipse.gef.graph.Edge;
import org.eclipse.gef.graph.Graph;
import org.eclipse.gef.graph.Node;
import org.eclipse.gef.layout.ILayoutAlgorithm;
import org.eclipse.gef.mvc.fx.parts.IRootPart;
import org.eclipse.gef.zest.fx.ZestProperties;
import org.eclipse.gef.zest.fx.ui.ZestFxUiModule;
import org.eclipse.gef.zest.fx.ui.parts.ZestFxUiView;
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

	private static final int DEFAULT_ELLIPSIS_THRESHOLD = 10;

	private int ellipsisThreshold = DEFAULT_ELLIPSIS_THRESHOLD;

	public static enum MissingNodeHandling {
		ADD_MISSING, SKIP_MISSING;
	}

	public static final String ID = "org.eclipse.epsilon.zest.view";

	private Graph graph = new Graph();
	private Map<Object, EpsilonZestNode> object2Node;
	private EpsilonZestModuleWrapper moduleWrapper;

	// Batched nodes, to add everything in one go to the observable list and reduce re-layouts
	private List<Node> batchedNodes = new ArrayList<>();
	private List<Edge> batchedEdges = new ArrayList<>();

	public EpsilonZestGraphView() {
		super(Guice.createInjector(Modules.override(new EpsilonZestGraphModule()).with(new ZestFxUiModule())));
		setGraph(graph);
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		final IRootPart<? extends javafx.scene.Node> rootPart = getContentViewer().getRootPart();
		javafx.scene.Node node = rootPart.getVisual();
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
	 * Returns the number of objects after which we will stop at an "..." node,
	 * which we can double click to show the next batch of elements.
	 */
	public int getEllipsisThreshold() {
		return ellipsisThreshold;
	}

	/**
	 * Changes the number of objects after which we will stop at an "..." node,
	 * which we can double click to show the next batch of elements.
	 */
	public void setEllipsisThreshold(int ellipsisThreshold) {
		this.ellipsisThreshold = ellipsisThreshold;
	}

	/**
	 * Repopulates the build using an EOL module. This method can be called from
	 * any thread.
	 * 
	 * @param layoutAlgo
	 */
	public void load(IEolModule newModule, ILayoutAlgorithm algorithm) {
		graph = new Graph();
		EpsilonZestProperties.setView(graph, this);
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
					expandObject(sourceObject, MissingNodeHandling.SKIP_MISSING);
				}

				addBatched();
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
	public void expandObject(final Object sourceObject, MissingNodeHandling mode) {
		EpsilonZestNode node = object2Node.get(sourceObject);
		if (!node.getOutgoingEdges().isEmpty()) {
			// Already expanded - don't do anything
			return;
		}

		for (Entry<String, Iterable<Object>> entry : moduleWrapper.getOutgoing(sourceObject).entrySet()) {
			String label = entry.getKey();

			int i = 0;
			final List<Object> ellipsisObjects = new LinkedList<>();
			for (Object targetObject : entry.getValue()) {
				final EpsilonZestNode targetNode = object2Node.get(targetObject);

				/*
				 * If the target node is already there, or we are adding missing
				 * nodes and there's not too many, just add the edge. Otherwise,
				 * if we are adding missing nodes but there's too many, just
				 * collect them up.
				 */
				if (targetNode != null || mode == MissingNodeHandling.ADD_MISSING && ++i < ellipsisThreshold) {
					mapToEdge(sourceObject, targetObject, label, mode);
				} else if (mode == MissingNodeHandling.ADD_MISSING) {
					ellipsisObjects.add(targetObject);
				}
			}

			if (!ellipsisObjects.isEmpty()) {
				mapToEllipsisNode(sourceObject, ellipsisObjects, ellipsisThreshold, i, label);
			}
		}

		addBatched();
	}

	public void expandEllipsis(EpsilonZestEllipsisNode ellipsisNode) {
		final List<Object> ellipsisObjects = EpsilonZestProperties.getEllipsisObjects(ellipsisNode);
		final String ellipsisLabel = EpsilonZestProperties.getEllipsisLabel(ellipsisNode);
	
		final Edge incomingEdge = ellipsisNode.getIncomingEdges().iterator().next();
		final Node sourceNode = incomingEdge.getSource();
		final Object sourceObject = EpsilonZestProperties.getModelElement(sourceNode);
		assert sourceObject != null : "Source node should have a source object";
	
		int startIdx = EpsilonZestProperties.getEllipsisFrom(ellipsisNode);
		final int endIdx = EpsilonZestProperties.getEllipsisTo(ellipsisNode);

		int remaining = getEllipsisThreshold();
		for (Iterator<Object> itEllipsisObject = ellipsisObjects.iterator(); itEllipsisObject.hasNext() && remaining > 0;) {
			Object ellipsisObject = itEllipsisObject.next();
			mapToEdge(sourceObject, ellipsisObject, ellipsisLabel, MissingNodeHandling.ADD_MISSING);
			itEllipsisObject.remove();

			--remaining;
			++startIdx;
		}
	
		if (ellipsisObjects.isEmpty()) {
			graph.getEdges().remove(incomingEdge);
			graph.getNodes().remove(ellipsisNode);
		} else {
			EpsilonZestProperties.setEllipsisFrom(ellipsisNode, startIdx);
			ZestProperties.setLabel(incomingEdge, formatEllipsis(ellipsisLabel, startIdx, endIdx));
		}

		addBatched();
	}

	protected String formatEllipsis(final String ellipsisLabel, int startIdx, final int endIdx) {
		return String.format("%s[%d..%d]", ellipsisLabel, startIdx, endIdx);
	}

	private void mapToEllipsisNode(Object source, List<Object> ellipsisObjects, int startIdx, int endIdx, String label) {
		final Node sourceNode = object2Node.get(source);
		assert sourceNode != null : "The source node should already exist";

		final EpsilonZestEllipsisNode n = new EpsilonZestEllipsisNode();
		ZestProperties.setLabel(n, "...");
		EpsilonZestProperties.setEllipsisObjects(n, ellipsisObjects);
		EpsilonZestProperties.setEllipsisLabel(n, label);
		EpsilonZestProperties.setEllipsisFrom(n, startIdx);
		EpsilonZestProperties.setEllipsisTo(n, endIdx);
		batchedNodes.add(n);

		Edge e = new EpsilonZestEdge(sourceNode, n);
		ZestProperties.setLabel(e, formatEllipsis(label, startIdx, endIdx));
		ZestProperties.setTargetDecoration(e, ArrowTypes.filledTriangle());
		batchedEdges.add(e);
	}

	protected void addBatched() {
		graph.getNodes().addAll(batchedNodes);
		graph.getEdges().addAll(batchedEdges);
		batchedNodes.clear();
		batchedEdges.clear();
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
		batchedEdges.add(e);

		return e;
	}

	private EpsilonZestNode mapToNode(Object nodeObject) {
		EpsilonZestNode n = object2Node.get(nodeObject);

		if (n == null) {
			n = new EpsilonZestObjectNode();
			ZestProperties.setLabel(n, moduleWrapper.getNodeLabel(nodeObject));
			EpsilonZestProperties.setModelElement(n, nodeObject);

			batchedNodes.add(n);
			object2Node.put(nodeObject, n);
		}

		return n;
	}

	public void disposeModule() {
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
