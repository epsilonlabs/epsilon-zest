package org.eclipse.epsilon.zest.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.epsilon.common.dt.console.EpsilonConsole;
import org.eclipse.epsilon.eol.IEolModule;
import org.eclipse.epsilon.eol.dt.debug.EolDebugger;
import org.eclipse.epsilon.eol.dt.launching.EclipseContextManager;
import org.eclipse.epsilon.eol.dt.launching.EolLaunchConfigurationDelegate;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.zest.EpsilonZestGraphView;
import org.eclipse.epsilon.zest.EpsilonZestPlugin;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * Tweaked version of the EOL launch configuration delegate for the Zest
 * visualization view. It will run the script normally, but instead of tearing
 * down the module afterwards it will pass it on ot the Zest view, which will
 * invoke various operations to feed the graph.
 *
 * TODO: support EOL debug mode ({@link EolDebugger} does not allow for invoking
 * specific operations, AFAIK).
 *
 * TODO: may be useful to support both object identity-based graphs and object
 * hash-based graphs.
 */
public class EpsilonZestLaunchConfigurationDelegate extends EolLaunchConfigurationDelegate  {

	@Override
	public boolean launch(final ILaunchConfiguration configuration, final String mode, final ILaunch launch,
			final IProgressMonitor progressMonitor, final IEolModule module, EolDebugger debugger,
			final String lauchConfigurationSourceAttribute, boolean setup, boolean disposeModelRepository)
					throws CoreException {
		collectListeners();

		if (setup) EpsilonConsole.getInstance().clear();

		aboutToParse(configuration, mode, launch, progressMonitor, module);

		if (!parse(module, lauchConfigurationSourceAttribute, configuration, mode, launch, progressMonitor)) return false;

		try { 
			EclipseContextManager.setup(module.getContext(),configuration, progressMonitor, launch, setup);
			aboutToExecute(configuration, mode, launch, progressMonitor, module);
			String subtask = "Executing";
			progressMonitor.subTask(subtask);
			progressMonitor.beginTask(subtask, 100);
			
			result = module.execute();
			executed(configuration, mode, launch, progressMonitor, module, result);
			
		} catch (Exception e) {
			e = EolRuntimeException.wrap(e);
			e.printStackTrace();
			module.getContext().getErrorStream().println(e.toString());
			progressMonitor.setCanceled(true);
			return false;
		}

		progressMonitor.done();

		// Show Zest view and pass the EOL module to it
		final Display display = PlatformUI.getWorkbench().getDisplay();
		display.syncExec(new Runnable(){
			public void run() {
				IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					EpsilonZestGraphView zestView = (EpsilonZestGraphView)activePage.showView(EpsilonZestGraphView.ID);
					zestView.load(module);
				} catch (PartInitException e) {
					EpsilonZestPlugin.getDefault().logException(e);
				}
			}
		});

		return true;
	}

}
