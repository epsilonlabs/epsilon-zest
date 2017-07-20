package org.eclipse.epsilon.zest.graph;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.epsilon.zest.view.EpsilonZestGraphView;
import org.eclipse.epsilon.zest.view.EpsilonZestProperties;
import org.eclipse.epsilon.zest.view.EpsilonZestGraphView.MissingNodeHandling;
import org.eclipse.gef4.graph.Edge;

public class EpsilonZestEllipsisNode extends EpsilonZestNode {

	public EpsilonZestEllipsisNode() {
		super();
	}

	public EpsilonZestEllipsisNode(Map<String, Object> attributes) {
		super(attributes);
	}

	@Override
	public void expandOutgoing() {
		final EpsilonZestGraphView view = EpsilonZestProperties.getView(getGraph());
		view.expandEllipsis(this);
	}
	
}
