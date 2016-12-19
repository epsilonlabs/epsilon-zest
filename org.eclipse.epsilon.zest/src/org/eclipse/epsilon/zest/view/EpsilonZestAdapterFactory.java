package org.eclipse.epsilon.zest.view;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.epsilon.zest.EpsilonZestPlugin;
import org.eclipse.epsilon.zest.graph.EpsilonZestNode;
import org.eclipse.epsilon.zest.utils.CallableRunnable;
import org.eclipse.ui.views.properties.IPropertySource;

/**
 * Creates adapters for Epsilon Zest nodes and edges that provide content for
 * the Properties view.
 */
public class EpsilonZestAdapterFactory implements IAdapterFactory {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (IPropertySource.class.equals(adapterType)) {
			if (adaptableObject instanceof EpsilonZestNode) {
				try {
					final EpsilonZestNode node = (EpsilonZestNode) adaptableObject;
					final EpsilonZestGraphView view = EpsilonZestProperties.getView(node.getGraph());
					final Object modelElement = EpsilonZestProperties.getModelElement(node);

					return CallableRunnable.syncExec(new CallableRunnable<IPropertySource>() {
						@Override
						public IPropertySource call() throws Exception {
							return view.getNodePropertySource(modelElement);
						}
					});
				} catch (Exception e) {
					EpsilonZestPlugin.getDefault().logException(e);
				}
			}
		}
		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return new Class[]{ IPropertySource.class };
	}

}
