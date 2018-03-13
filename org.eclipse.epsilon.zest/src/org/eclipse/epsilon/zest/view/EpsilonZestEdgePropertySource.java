package org.eclipse.epsilon.zest.view;

import java.util.Map;

import org.eclipse.epsilon.zest.eol.EpsilonZestModuleWrapper;
import org.eclipse.epsilon.zest.graph.EpsilonZestEdge;
import org.eclipse.epsilon.zest.utils.CallableRunnable;
import org.eclipse.gef.zest.fx.ZestProperties;

/**
 * Uses an {@link EpsilonZestModuleWrapper} to feed the contents of the
 * Properties view for an edge selected in Epsilon Zest (mapped from a
 * pair of objects and a label).
 */
public class EpsilonZestEdgePropertySource extends AbstractEpsilonZestPropertySource {

	public EpsilonZestEdgePropertySource(EpsilonZestEdge edge) throws Exception {
		final Object sourceObject = EpsilonZestProperties.getModelElement(edge.getSource());
		final Object targetObject = EpsilonZestProperties.getModelElement(edge.getTarget());
		final String label = ZestProperties.getLabel(edge);
		final EpsilonZestModuleWrapper moduleWrapper = EpsilonZestProperties.getView(edge.getGraph()).getModuleWrapper();

		properties = CallableRunnable.syncExec(new CallableRunnable<Map<String, Object>>() {
			@Override
			public Map<String, Object> call() throws Exception {
				return moduleWrapper.getEdgeProperties(sourceObject, targetObject, label);
			}
		});

		computeDescriptors();
	}

}
