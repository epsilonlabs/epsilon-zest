package org.eclipse.epsilon.zest.launch;

import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.epsilon.common.dt.launching.tabs.EpsilonLaunchConfigurationTabGroup;
import org.eclipse.epsilon.eol.dt.launching.tabs.EolSourceConfigurationTab;

/**
 * TODO: add options 
 */
public class EpsilonZestLaunchTabGroup extends EpsilonLaunchConfigurationTabGroup {

	@Override
	public ILaunchConfigurationTab getSourceConfigurationTab() {
		return new EolSourceConfigurationTab();
	}

	@Override
	public ILaunchConfigurationTab[] getOtherConfigurationTabs() {
		return new ILaunchConfigurationTab[]{
			new EpsilonZestVisualizationTab()
		};
	}
	
}
