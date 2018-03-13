package org.eclipse.epsilon.zest.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.epsilon.zest.EpsilonZestPlugin;
import org.eclipse.gef.layout.ILayoutAlgorithm;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Launch configuration tab for customizing the way the visualization looks and is laid out.
 */
public class EpsilonZestVisualizationTab extends AbstractLaunchConfigurationTab {

	private ComboViewer algorithmCombo;

	@Override
	public void createControl(Composite parent) {
		FillLayout parentLayout = new FillLayout();
		parent.setLayout(parentLayout);

		Composite control = new Composite(parent, SWT.NONE);
		setControl(control);
		
		GridLayout controlLayout = new GridLayout(2, false);
		control.setLayout(controlLayout);

		Label algorithmLabel = new Label(control, SWT.NONE);
		algorithmLabel.setText("Layout algorithm: ");
		algorithmCombo = new ComboViewer(control);
		algorithmCombo.setContentProvider(ArrayContentProvider.getInstance());
		algorithmCombo.setLabelProvider(new LabelProvider(){
			@Override
			public String getText(Object element) {
				return element.getClass().getSimpleName().replace("LayoutAlgorithm", "").replace("Algorithm", "");
			}
		});
		algorithmCombo.setInput(EpsilonZestLaunchConfigurationDelegate.ALGORITHMS);
		algorithmCombo.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				updateLaunchConfigurationDialog();
			}
		});

		control.setBounds(0, 0, 300, 300);
		control.layout();
		control.pack();
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		algorithmCombo.setSelection(new StructuredSelection(EpsilonZestLaunchConfigurationDelegate.DEFAULT_ALGORITHM));
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			ILayoutAlgorithm algo = EpsilonZestLaunchConfigurationDelegate.getLayoutAlgorithm(configuration);
			algorithmCombo.setSelection(new StructuredSelection(algo));
			updateLaunchConfigurationDialog();
		} catch (CoreException e) {
			EpsilonZestPlugin.getDefault().logException(e);
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if (algorithmCombo.getSelection() instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) algorithmCombo.getStructuredSelection();
			ILayoutAlgorithm layoutAlgo = (ILayoutAlgorithm) ssel.getFirstElement();
			EpsilonZestLaunchConfigurationDelegate.setLayoutAlgorithm(configuration, layoutAlgo);
		}
	}

	@Override
	public String getName() {
		return "Visualization";
	}

}
