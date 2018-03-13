package org.eclipse.epsilon.zest.graph;

import java.util.Map;

import org.eclipse.gef.graph.Node;

/**
 * Base for specialised GEF4 nodes. Useful to separate our nodes from other GEF4
 * nodes for the sake of the Properties view, and also useful for adding any
 * extra functionality we may need.
 */
public abstract class EpsilonZestNode extends Node {

	public EpsilonZestNode() {
		// nothing to do
	}

	public EpsilonZestNode(Map<String, Object> attributes) {
		super(attributes);
	}

	public abstract void expandOutgoing();

	
}
