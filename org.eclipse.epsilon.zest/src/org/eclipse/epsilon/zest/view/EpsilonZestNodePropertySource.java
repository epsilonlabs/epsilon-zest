package org.eclipse.epsilon.zest.view;

import java.util.Map;

import org.eclipse.epsilon.zest.eol.EpsilonZestModuleWrapper;
import org.eclipse.epsilon.zest.graph.EpsilonZestNode;
import org.eclipse.epsilon.zest.utils.CallableRunnable;

/**
 * Uses an {@link EpsilonZestModuleWrapper} to feed the contents of the
 * Properties view for a node selected in Epsilon Zest (mapped from an
 * object, usually a model element).
 */
public class EpsilonZestNodePropertySource extends AbstractEpsilonZestPropertySource {

	public EpsilonZestNodePropertySource(EpsilonZestNode node) throws Exception {
		final Object nodeObject = EpsilonZestProperties.getModelElement(node);
		final EpsilonZestModuleWrapper moduleWrapper = EpsilonZestProperties.getView(node.getGraph()).getModuleWrapper();

		properties = CallableRunnable.syncExec(new CallableRunnable<Map<String, Object>>() {
			@Override
			public Map<String, Object> call() throws Exception {
				return moduleWrapper.getNodeProperties(nodeObject);
			}
		});

		computeDescriptors();
	}

}
