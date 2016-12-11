package org.eclipse.epsilon.zest.utils;

import org.eclipse.epsilon.zest.EpsilonZestGraphView;
import org.eclipse.gef4.graph.Graph;

/**
 * Getter and setter methods for extra node/edge properties we may need.
 */
public class EpsilonZestProperties {

	private static final String ZEST_VIEW = "zest.view";
	private EpsilonZestProperties() {}

	public static EpsilonZestGraphView getView(Graph g) {
		return (EpsilonZestGraphView) g.getAttributes().get(ZEST_VIEW);
	}

	public static void setView(Graph g, EpsilonZestGraphView view) {
		g.getAttributes().put(ZEST_VIEW, view);
	}
}
