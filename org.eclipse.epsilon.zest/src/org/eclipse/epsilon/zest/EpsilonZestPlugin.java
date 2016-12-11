package org.eclipse.epsilon.zest;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

/**
 * The activator class controls the plug-in life cycle
 */
public class EpsilonZestPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.epsilon.zest"; //$NON-NLS-1$

	// The shared instance
	private static EpsilonZestPlugin plugin;
	
	/**
	 * The constructor
	 */
	public EpsilonZestPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static EpsilonZestPlugin getDefault() {
		return plugin;
	}

	/**
	 * Logs an exception in the "Error Log" view.
	 */
	public void logException(Exception e) {
		plugin.getLog().log(new Status(IStatus.ERROR,
			FrameworkUtil.getBundle(getClass()).getSymbolicName(), e.getMessage(), e));
	}

	/**
	 * Logs a warning in the "Error Log" view.
	 */
	public void logWarning(String e) {
		plugin.getLog().log(new Status(IStatus.WARNING,
			FrameworkUtil.getBundle(getClass()).getSymbolicName(), e));
	}
}
