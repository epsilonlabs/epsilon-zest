package org.eclipse.epsilon.zest.view.dialogs;

import java.io.File;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog for exporting the visualization to a file.
 */
public class ExportDialog extends Dialog {
	private final Shell shell;

	private ComboViewer zoomCombo;
	private Text pathText;

	private int scaleFactor;
	private String filePath;

	protected ExportDialog(Shell shell) {
		super(shell);
		this.shell = shell;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);

		Composite contents = new Composite(container, SWT.NONE);
		contents.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		contents.setLayout(new GridLayout(3, false));
		Label zoomLabel = new Label(contents, SWT.NONE);
		zoomLabel.setText("Scale: ");

		zoomCombo = new ComboViewer(contents);
		zoomCombo.setContentProvider(ArrayContentProvider.getInstance());
		zoomCombo.setLabelProvider(new LabelProvider(){
			@Override
			public String getText(Object element) {
				return element + "x";
			}
		});
		final String[] options = new String[]{ "1", "2", "4", "8" };
		zoomCombo.setInput(options);
		zoomCombo.setSelection(new StructuredSelection(options[0]));
		GridData gd = new GridData(SWT.LEFT, SWT.FILL, false, false);
		gd.horizontalSpan = 2;
		zoomCombo.getCombo().setLayoutData(gd);
		

		Label fileLabel = new Label(contents, SWT.NONE);
		fileLabel.setText("File: ");

		pathText = new Text(contents, SWT.BORDER);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.minimumWidth = 400;
		pathText.setLayoutData(gd);
		pathText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				final boolean canSave = new File(pathText.getText()).getParentFile().canWrite();
				getButton(IDialogConstants.OK_ID).setEnabled(canSave);
			}
		});

		Button browseButton = new Button(contents, SWT.NONE);
		browseButton.setText("Browse...");
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dlgFile = new FileDialog(shell);
				dlgFile.setFilterExtensions(new String[]{"*.png"});
				String path = dlgFile.open();
				if (!path.endsWith(".png")) {
					path = path + ".png";
				}
				if (path != null) {
					pathText.setText(path);
				}
			}
		});

		return container;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Save visualization as image");
	}

	@Override
	protected void okPressed() {
		final IStructuredSelection ssel = (IStructuredSelection)zoomCombo.getSelection();
		scaleFactor = Integer.parseInt(ssel.getFirstElement().toString());
		filePath = pathText.getText();
		super.okPressed();
	}

	public int getScaleFactor() {
		return scaleFactor;
	}

	public String getFilePath() {
		return filePath;
	}
}