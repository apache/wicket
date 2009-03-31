package org.apache.wicket.devutils.debugbar;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.devutils.inspector.InspectorPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * A panel that adds a link to the inspector to the debug bar.
 * 
 * @author Jeremy Thomerson <jthomerson@apache.org>
 */
public class InspectorDebugPanel extends StandardDebugPanel {
	private static final long serialVersionUID = 1L;

	public static final IDebugBarContributor DEBUG_BAR_CONTRIB = new IDebugBarContributor() {
		private static final long serialVersionUID = 1L;

		public Component createComponent(String id, WicketDebugBar debugBar) {
			return new InspectorDebugPanel(id);
		}

	};

	public InspectorDebugPanel(String id) {
		super(id);
	}

	@Override
	protected Class<? extends Page> getLinkPageClass() {
		return InspectorPage.class;
	}

	@Override
	protected ResourceReference getImageResourceReference() {
		return new ResourceReference(InspectorPage.class, "bug.png");
	}

	@Override
	protected IModel<String> getDataModel() {
		return new Model<String>("Inspector");
	}

}
