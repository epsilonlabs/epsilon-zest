package org.eclipse.epsilon.zest.view;

import java.util.List;

import org.eclipse.epsilon.zest.graph.EpsilonZestEllipsisNode;
import org.eclipse.gef.graph.Graph;
import org.eclipse.gef.graph.Node;
import org.eclipse.gef.zest.fx.ZestProperties;

/**
 * Getter and setter methods for extra node/edge properties we may need.
 * Equivalent to the Zest {@link ZestProperties} class.
 */
public class EpsilonZestProperties {

	private static final String ZEST_VIEW = "eps.view";
	private static final String ZEST_MODELELEMENT = "eps.elem";
	private static final String ZEST_ELLIPSIS_OBJECTS = "eps.ellip.obj";
	private static final String ZEST_ELLIPSIS_FROM = "eps.ellip.from";
	private static final String ZEST_ELLIPSIS_TO = "eps.ellip.to";
	private static final String ZEST_ELLIPSIS_LABEL = "eps.ellip.label";

	private EpsilonZestProperties() {}

	public static EpsilonZestGraphView getView(Graph g) {
		return (EpsilonZestGraphView) g.getAttributes().get(ZEST_VIEW);
	}

	public static void setView(Graph g, EpsilonZestGraphView view) {
		g.getAttributes().put(ZEST_VIEW, view);
	}

	public static Object getModelElement(Node n) {
		return n.getAttributes().get(ZEST_MODELELEMENT);
	}

	public static void setModelElement(Node n, Object modelElement) {
		n.getAttributes().put(ZEST_MODELELEMENT, modelElement);
	}

	@SuppressWarnings("unchecked")
	public static List<Object> getEllipsisObjects(EpsilonZestEllipsisNode n) {
		return (List<Object>) n.getAttributes().get(ZEST_ELLIPSIS_OBJECTS);
	}

	public static void setEllipsisObjects(EpsilonZestEllipsisNode n, List<Object> ellipsisObjects) {
		n.getAttributes().put(ZEST_ELLIPSIS_OBJECTS, ellipsisObjects);
	}

	public static String getEllipsisLabel(EpsilonZestEllipsisNode n) {
		return (String) n.getAttributes().get(ZEST_ELLIPSIS_LABEL);
	}

	public static void setEllipsisLabel(EpsilonZestEllipsisNode n, String label) {
		n.getAttributes().put(ZEST_ELLIPSIS_LABEL, label);
	}

	public static Integer getEllipsisFrom(EpsilonZestEllipsisNode n) {
		return (Integer) n.getAttributes().get(ZEST_ELLIPSIS_FROM);
	}

	public static void setEllipsisFrom(EpsilonZestEllipsisNode n, int startIdx) {
		n.getAttributes().put(ZEST_ELLIPSIS_FROM, startIdx);
	}

	public static Integer getEllipsisTo(EpsilonZestEllipsisNode n) {
		return (Integer) n.getAttributes().get(ZEST_ELLIPSIS_TO);
	}

	public static void setEllipsisTo(EpsilonZestEllipsisNode n, int endIdx) {
		n.getAttributes().put(ZEST_ELLIPSIS_TO, endIdx);
	}
}
