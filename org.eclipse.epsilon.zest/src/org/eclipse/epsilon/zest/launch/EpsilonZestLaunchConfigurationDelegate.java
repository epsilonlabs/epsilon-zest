package org.eclipse.epsilon.zest.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.epsilon.common.dt.console.EpsilonConsole;
import org.eclipse.epsilon.eol.IEolModule;
import org.eclipse.epsilon.eol.dt.debug.EolDebugger;
import org.eclipse.epsilon.eol.dt.launching.EclipseContextManager;
import org.eclipse.epsilon.eol.dt.launching.EolLaunchConfigurationDelegate;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.zest.EpsilonZestPlugin;
import org.eclipse.epsilon.zest.utils.CallableRunnable;
import org.eclipse.epsilon.zest.view.EpsilonZestGraphView;
import org.eclipse.gef.layout.ILayoutAlgorithm;
import org.eclipse.gef.layout.algorithms.GridLayoutAlgorithm;
import org.eclipse.gef.layout.algorithms.HorizontalShiftAlgorithm;
import org.eclipse.gef.layout.algorithms.SpaceTreeLayoutAlgorithm;
import org.eclipse.gef.layout.algorithms.SpringLayoutAlgorithm;
import org.eclipse.gef.layout.algorithms.SugiyamaLayoutAlgorithm;
import org.eclipse.gef.layout.algorithms.TreeLayoutAlgorithm;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
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

	public static final ILayoutAlgorithm DEFAULT_ALGORITHM = new SugiyamaLayoutAlgorithm();
	public static final ILayoutAlgorithm[] ALGORITHMS = new ILayoutAlgorithm[]{
		new GridLayoutAlgorithm(),      // Zest 20160906: will enter infinite loop on an empty graph if not careful
		new HorizontalShiftAlgorithm(),
		// new RadialLayoutAlgorithm(), // Zest 20160906: buggy? tries to resize node under minimal width 
		new SpaceTreeLayoutAlgorithm(),
		new SpringLayoutAlgorithm(),
		DEFAULT_ALGORITHM,
		new TreeLayoutAlgorithm()
	};

	private static final String LAYOUT_ATTRIBUTE = "eps.zestlayout";
	
	@Override
	public boolean launch(final ILaunchConfiguration configuration, final String mode, final ILaunch launch,
			final IProgressMonitor progressMonitor, final IEolModule module, EolDebugger debugger,
			final String lauchConfigurationSourceAttribute, boolean setup, boolean disposeModelRepository)
					throws CoreException {
		collectListeners();

		if (setup) EpsilonConsole.getInstance().clear();

		aboutToParse(configuration, mode, launch, progressMonitor, module);

		if (!parse(module, lauchConfigurationSourceAttribute, configuration, mode, launch, progressMonitor)) return false;

		final Display display = PlatformUI.getWorkbench().getDisplay();
		try {
			// Unload existing models (if any)
			display.syncExec(new Runnable() {
				public void run() {
					try {
						IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
						final IViewPart rawView = activePage.showView(EpsilonZestGraphView.ID);
						if (rawView instanceof EpsilonZestGraphView) {
							EpsilonZestGraphView zestView = (EpsilonZestGraphView) rawView;
							zestView.disposeModule();
						}
					} catch (PartInitException ex) {
						EpsilonZestPlugin.getDefault().logException(ex);
					}
				}
			});

			// Load models in UI thread (otherwise, we get "not in tx" exceptions with Hawk)
			CallableRunnable.syncExec(new CallableRunnable<Object>(){
				@Override
				public Object call() throws Exception {
					EclipseContextManager.setup(module.getContext(),configuration, progressMonitor, launch, setup);
					return null;
				}
			});

			aboutToExecute(configuration, mode, launch, progressMonitor, module);
			String subtask = "Executing";
			progressMonitor.subTask(subtask);
			progressMonitor.beginTask(subtask, 100);

			result = module.execute();
			executed(configuration, mode, launch, progressMonitor, module, result);
		} catch (Exception e) {
			EpsilonZestPlugin.getDefault().logException(e);

			e = EolRuntimeException.wrap(e);
			module.getContext().getErrorStream().println(e.toString());
			progressMonitor.setCanceled(true);
			return false;
		}

		progressMonitor.done();

		// Reset Zest view and pass the EOL module to it
		display.syncExec(new Runnable(){
			public void run() {
				IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					final IViewPart rawView = activePage.showView(EpsilonZestGraphView.ID);
					if (rawView instanceof EpsilonZestGraphView) {
						EpsilonZestGraphView zestView = (EpsilonZestGraphView)rawView;
						ILayoutAlgorithm layoutAlgo = getLayoutAlgorithm(configuration);
						zestView.load(module, layoutAlgo);
					}
				} catch (CoreException e) {
					EpsilonZestPlugin.getDefault().logException(e);
				}
			}
		});

		return true;
	}

	public static ILayoutAlgorithm getLayoutAlgorithm(ILaunchConfiguration configuration) throws CoreException {
		final String algorithmClassName = configuration.getAttribute(
			LAYOUT_ATTRIBUTE, DEFAULT_ALGORITHM.getClass().getName()
		);
		for (ILayoutAlgorithm algo : ALGORITHMS) {
			if (algo.getClass().getName().equals(algorithmClassName)) {
				return algo;
			}
		}
		return DEFAULT_ALGORITHM;
	}

	public static void setLayoutAlgorithm(ILaunchConfigurationWorkingCopy configuration, ILayoutAlgorithm algorithm) {
		configuration.setAttribute(LAYOUT_ATTRIBUTE, algorithm.getClass().getName());
	}
}
