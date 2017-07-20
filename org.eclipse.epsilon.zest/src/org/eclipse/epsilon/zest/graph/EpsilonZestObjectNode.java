package org.eclipse.epsilon.zest.graph;

import java.util.Map;

import org.eclipse.epsilon.zest.view.EpsilonZestGraphView;
import org.eclipse.epsilon.zest.view.EpsilonZestGraphView.MissingNodeHandling;
import org.eclipse.epsilon.zest.view.EpsilonZestProperties;

/**
 * Node that represents a (real or fake) model object.
 */
public class EpsilonZestObjectNode extends EpsilonZestNode {

	public EpsilonZestObjectNode(Map<String, Object> attributes) {
		super(attributes);
	}

	public EpsilonZestObjectNode() {
		super();
	}

	@Override
	public void expandOutgoing() {
		final EpsilonZestGraphView view = EpsilonZestProperties.getView(getGraph());
		final Object modelElement = EpsilonZestProperties.getModelElement(this);
		view.expandObject(modelElement, MissingNodeHandling.ADD_MISSING);
	}

}
