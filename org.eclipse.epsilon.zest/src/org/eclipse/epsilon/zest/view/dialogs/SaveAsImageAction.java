package org.eclipse.epsilon.zest.view.dialogs;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.eclipse.epsilon.zest.EpsilonZestPlugin;
import org.eclipse.epsilon.zest.view.EpsilonZestGraphView;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.FrameworkUtil;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.transform.Transform;

/**
 * Implements the view action 'Save as image'. 
 */
public class SaveAsImageAction extends Action {
	private final Node node;

	public SaveAsImageAction(Node node) {
		super("Save as image...");
		this.node = node;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return EpsilonZestPlugin.imageDescriptorFromPlugin(
			FrameworkUtil.getBundle(EpsilonZestGraphView.class).getSymbolicName(), "icons/screenshot.gif");
	}

	@Override
	public void run() {
		final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		final ExportDialog dlgExport = new ExportDialog(shell);

		if (dlgExport.open() == Dialog.OK) {
			// From http://stackoverflow.com/questions/32288411/
			final SnapshotParameters params = new SnapshotParameters();
			final int scaleFactor = dlgExport.getScaleFactor();
			params.setTransform(Transform.scale(scaleFactor, scaleFactor));
			Bounds bounds = node.getBoundsInParent();
			final WritableImage writableImage = new WritableImage(
				(int) Math.rint(scaleFactor * bounds.getWidth()),
				(int) Math.rint(scaleFactor * bounds.getHeight())
			);
			node.snapshot(params, writableImage);

			final RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
	        try {
				ImageIO.write(renderedImage, "png", new File(dlgExport.getFilePath()));
			} catch (IOException e) {
				EpsilonZestPlugin.getDefault().logException(e);
			}				
		}
	}
}