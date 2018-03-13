package org.eclipse.epsilon.zest.graph;

import java.util.Map;

import org.eclipse.gef.graph.Edge;
import org.eclipse.gef.graph.Node;

/**
 * Specialized GEF4 edges. Useful to separate our edges from other GEF4 edges
 * for the sake of the Properties view, and also useful for adding any extra
 * functionality we may need.
 */
public class EpsilonZestEdge extends Edge {

	public EpsilonZestEdge(Node source, Node target) {
		super(source, target);
	}

	public EpsilonZestEdge(Map<String, Object> attributes, Node source, Node target) {
		super(attributes, source, target);
	}

}
