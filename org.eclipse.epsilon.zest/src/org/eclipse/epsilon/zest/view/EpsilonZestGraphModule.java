package org.eclipse.epsilon.zest.view;

import org.eclipse.epsilon.zest.view.EpsilonZestGraphView.MissingNodeHandling;
import org.eclipse.gef4.common.adapt.AdapterKey;
import org.eclipse.gef4.common.adapt.inject.AdapterInjectionSupport;
import org.eclipse.gef4.common.adapt.inject.AdapterInjectionSupport.LoggingMode;
import org.eclipse.gef4.mvc.fx.policies.AbstractFXInteractionPolicy;
import org.eclipse.gef4.mvc.fx.policies.IFXOnClickPolicy;
import org.eclipse.gef4.mvc.parts.IVisualPart;
import org.eclipse.gef4.zest.fx.ZestFxModule;
import org.eclipse.gef4.zest.fx.parts.NodePart;
import org.eclipse.swt.widgets.Display;

import com.google.inject.multibindings.MapBinder;

import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

/**
 * Extends the Zest FX view with the ability to expand nodes on double-click. 
 */
public class EpsilonZestGraphModule extends ZestFxModule {

	public static class EpsilonExpandOnClickPolicy extends AbstractFXInteractionPolicy implements IFXOnClickPolicy {
		@Override
		public void click(MouseEvent e) {
			if (isRegistered(e.getTarget()) && !isRegisteredForHost(e.getTarget())) {
			 		return;
			}

			IVisualPart<Node, ? extends Node> host = getHost();
			if (e.getClickCount() != 2) {
				return;
			}

			if (host instanceof NodePart) {
				NodePart nodePart = (NodePart) host;

				final org.eclipse.gef4.graph.Node node = nodePart.getContent();
				final EpsilonZestGraphView view = EpsilonZestProperties.getView(node.getGraph());
				final Object modelElement = EpsilonZestProperties.getModelElement(node);
				Display.getDefault().asyncExec(new Runnable(){
					@Override
					public void run() {
						view.expandOutgoing(modelElement, MissingNodeHandling.ADD_MISSING);
					}
				});
			}
		}
	}

	@Override
	protected void enableAdapterMapInjection() {
		install(new AdapterInjectionSupport(LoggingMode.PRODUCTION));
	}

	@Override
	protected void bindNodePartAdapters(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
		// TODO Auto-generated method stub
		super.bindNodePartAdapters(adapterMapBinder);
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(EpsilonExpandOnClickPolicy.class);
	}

}
