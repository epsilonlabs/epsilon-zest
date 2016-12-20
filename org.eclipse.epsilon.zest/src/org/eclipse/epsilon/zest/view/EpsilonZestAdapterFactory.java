package org.eclipse.epsilon.zest.view;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.epsilon.zest.EpsilonZestPlugin;
import org.eclipse.epsilon.zest.graph.EpsilonZestEdge;
import org.eclipse.epsilon.zest.graph.EpsilonZestNode;
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
			try {
				if (adaptableObject instanceof EpsilonZestNode) {
					final EpsilonZestNode node = (EpsilonZestNode) adaptableObject;
					return new EpsilonZestNodePropertySource(node);
				} else if (adaptableObject instanceof EpsilonZestEdge) {
					final EpsilonZestEdge edge = (EpsilonZestEdge) adaptableObject;
					return new EpsilonZestEdgePropertySource(edge);
				}
			} catch (Exception e) {
				EpsilonZestPlugin.getDefault().logException(e);
			}
		}
		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return new Class[]{ IPropertySource.class };
	}

}
