package org.eclipse.epsilon.zest.view;

import org.eclipse.epsilon.zest.graph.EpsilonZestNode;
import org.eclipse.gef4.common.adapt.AdapterKey;
import org.eclipse.gef4.common.adapt.inject.AdapterInjectionSupport;
import org.eclipse.gef4.common.adapt.inject.AdapterInjectionSupport.LoggingMode;
import org.eclipse.gef4.graph.Graph;
import org.eclipse.gef4.mvc.fx.policies.AbstractFXInteractionPolicy;
import org.eclipse.gef4.mvc.fx.policies.IFXOnClickPolicy;
import org.eclipse.gef4.mvc.parts.IVisualPart;
import org.eclipse.gef4.zest.fx.ZestFxModule;
import org.eclipse.gef4.zest.fx.parts.NodePart;
import org.eclipse.jface.viewers.StructuredSelection;
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

			if (host instanceof NodePart) {
				NodePart nodePart = (NodePart) host;

				if (nodePart.getContent() instanceof EpsilonZestNode) {
					final EpsilonZestNode node = (EpsilonZestNode) nodePart.getContent();
					final Graph graph = node.getGraph();
					final EpsilonZestGraphView view = EpsilonZestProperties.getView(graph);
					view.getSelectionProvider().setSelection(new StructuredSelection(node));

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
