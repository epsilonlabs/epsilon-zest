package org.eclipse.epsilon.zest.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

/**
 * Base class for Epsilon Zest property sources.
 */
public abstract class AbstractEpsilonZestPropertySource implements IPropertySource {

	private IPropertyDescriptor[] descriptors;
	protected Map<String, Object> properties;

	protected void computeDescriptors() {
		final List<IPropertyDescriptor> lDescriptors = new ArrayList<>(properties.size());
		for (String name : properties.keySet()) {
			final PropertyDescriptor descriptor = new PropertyDescriptor(name, name);
			lDescriptors.add(descriptor);
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
		// not supported - properties are read only
	}
}