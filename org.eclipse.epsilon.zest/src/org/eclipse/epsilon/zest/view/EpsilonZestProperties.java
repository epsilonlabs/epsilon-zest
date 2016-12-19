package org.eclipse.epsilon.zest.view;

import org.eclipse.gef4.graph.Graph;
import org.eclipse.gef4.graph.Node;
import org.eclipse.gef4.zest.fx.ZestProperties;

/**
 * Getter and setter methods for extra node/edge properties we may need.
 * Equivalent to the Zest {@link ZestProperties} class.
 */
public class EpsilonZestProperties {

	private static final String ZEST_VIEW = "eps.view";
	private static final String ZEST_MODELELEMENT = "eps.elem";

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
}
