package org.eclipse.epsilon.zest.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.epsilon.zest.EpsilonZestPlugin;
import org.eclipse.epsilon.zest.eol.EpsilonZestModuleWrapper;
import org.eclipse.epsilon.zest.graph.EpsilonZestNode;
import org.eclipse.epsilon.zest.utils.CallableRunnable;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * Uses an {@link EpsilonZestModuleWrapper} to feed the contents of the
 * Properties view for a node selected in Epsilon Zest (mapped to a model
 * element).
 */
public class EpsilonZestNodePropertySource implements IPropertySource {

	private final EpsilonZestNode node;
	private final IPropertyDescriptor[] descriptors;
	private Map<String, Object> properties;

	public EpsilonZestNodePropertySource(EpsilonZestNode node) throws Exception {
		this.node = node;

		final Object nodeObject = EpsilonZestProperties.getModelElement(node);
		final EpsilonZestModuleWrapper moduleWrapper = EpsilonZestProperties.getView(node.getGraph()).getModuleWrapper();

		properties = CallableRunnable.syncExec(new CallableRunnable<Map<String, Object>>() {
			@Override
			public Map<String, Object> call() throws Exception {
				return moduleWrapper.getNodeProperties(nodeObject);
			}
		});

		final List<IPropertyDescriptor> lDescriptors = new ArrayList<>(properties.size());
		for (String name : properties.keySet()) {
			lDescriptors.add(new TextPropertyDescriptor(name, name));
		}
		descriptors = lDescriptors.toArray(new IPropertyDescriptor[properties.size()]);
	}

	@Override
	public Object getEditableValue() {
		return null;
	}

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		return descriptors;
	}

	@Override
	public Object getPropertyValue(Object id) {
		return properties.get(id.toString());
	}

	@Override
	public boolean isPropertySet(Object id) {
		return properties.containsKey(id.toString());
	}

	@Override
	public void resetPropertyValue(Object id) {
		// not supported
	}

	@Override
	public void setPropertyValue(Object id, Object rawNewValue) {
		try {
			final Object nodeObject = EpsilonZestProperties.getModelElement(node);
			final EpsilonZestGraphView view = EpsilonZestProperties.getView(node.getGraph());
			final EpsilonZestModuleWrapper moduleWrapper = view.getModuleWrapper();

			properties = CallableRunnable.syncExec(new CallableRunnable<Map<String, Object>>() {
				@Override
				public Map<String, Object> call() throws Exception {
					moduleWrapper.setProperty(nodeObject, id.toString(), rawNewValue);
					view.styleNode(node);
					return moduleWrapper.getNodeProperties(nodeObject);
				}
			});
		} catch (Exception e) {
			EpsilonZestPlugin.getDefault().logException(e);
		}
	}

}
