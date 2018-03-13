package org.eclipse.epsilon.zest.view;

import org.eclipse.epsilon.zest.graph.EpsilonZestNode;
import org.eclipse.gef.common.adapt.AdapterKey;
import org.eclipse.gef.common.adapt.inject.AdapterInjectionSupport;
import org.eclipse.gef.common.adapt.inject.AdapterInjectionSupport.LoggingMode;
import org.eclipse.gef.mvc.fx.handlers.AbstractHandler;
import org.eclipse.gef.mvc.fx.handlers.IOnClickHandler;
import org.eclipse.gef.mvc.fx.parts.IVisualPart;
import org.eclipse.gef.zest.fx.ZestFxModule;
import org.eclipse.gef.zest.fx.parts.NodePart;
import org.eclipse.swt.widgets.Display;

import com.google.inject.multibindings.MapBinder;

import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

/**
 * Extends the Zest FX view with the ability to expand nodes on double-click. 
 */
public class EpsilonZestGraphModule extends ZestFxModule {

	public static class EpsilonExpandOnClickPolicy extends AbstractHandler implements IOnClickHandler {
		@Override
		public void click(MouseEvent e) {
			if (isRegistered(e.getTarget()) && !isRegisteredForHost(e.getTarget())) {
			 		return;
			}

			IVisualPart<? extends Node> host = getHost();

			if (host instanceof NodePart) {
				NodePart nodePart = (NodePart) host;

				if (nodePart.getContent() instanceof EpsilonZestNode) {
					final EpsilonZestNode node = (EpsilonZestNode) nodePart.getContent();

					if (e.getClickCount() == 2) {
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								node.expandOutgoing();
							}
						});
						return;
					}
				}
			}
		}
	}

	@Override
	protected void enableAdapterMapInjection() {
		install(new AdapterInjectionSupport(LoggingMode.PRODUCTION));
	}

	@Override
	protected void bindNodePartAdapters(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
		super.bindNodePartAdapters(adapterMapBinder);
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(EpsilonExpandOnClickPolicy.class);
	}

}
