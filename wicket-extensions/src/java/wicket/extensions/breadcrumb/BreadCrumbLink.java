/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar
 * 2006) eelco12 $ $Revision: 5004 $ $Date: 2006-03-17 20:47:08 -0800 (Fri, 17
 * Mar 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.extensions.breadcrumb;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.extensions.breadcrumb.panel.BreadCrumbPanel;
import wicket.markup.html.link.Link;
import wicket.version.undo.Change;

/**
 * A link that when clicked will set the the active
 * {@link IBreadCrumbParticipant bread crumb participant} to the one that is
 * returned by {@link #getParticipant(String)}. It is used internally by
 * {@link BreadCrumbBar the the bread crumb bar component}, and you can use it
 * for rendering links e.g. with
 * {@link BreadCrumbPanel bread crumb panel components}.
 * 
 * <p>
 * When clicked, it registers a change for backbutton support.
 * </p>
 * 
 * @author Eelco Hillenius
 */
public abstract class BreadCrumbLink extends Link
{
	private static final long serialVersionUID = 1L;

	/** The bread crumb model. */
	private final IBreadCrumbModel breadCrumbModel;

	/**
	 * Construct.
	 * 
	 * @param parent
	 * 
	 * @param id
	 *            The link id
	 * @param breadCrumbModel
	 *            The bread crumb model
	 */
	public BreadCrumbLink(MarkupContainer parent, final String id, IBreadCrumbModel breadCrumbModel)
	{
		super(parent, id);
		this.breadCrumbModel = breadCrumbModel;
	}

	/**
	 * @see wicket.markup.html.link.Link#onClick()
	 */
	@Override
	public void onClick()
	{
		// get the currently active particpant
		final IBreadCrumbParticipant active = breadCrumbModel.getActive();
		if (active == null)
		{
			throw new IllegalStateException("The model has no active bread crumb. Before using "
					+ this + ", you have to have at least one bread crumb in the model");
		}

		Component component = active.getComponent();
		// get the participant to set as active
		final IBreadCrumbParticipant participant = getParticipant(component.getParent(),component.getId());

		// add back button support
		addStateChange(new Change()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void undo()
			{
				breadCrumbModel.setActive(active);
			}
		});

		// set the next participant as the active one
		breadCrumbModel.setActive(participant);
	}

	/**
	 * Gets the {@link IBreadCrumbParticipant bread crumb participant} to be set
	 * active when the link is clicked.
	 * @param parent 
	 * 
	 * @param componentId
	 *            When the participant creates it's own view, it typically
	 *            should use this component id for the component that is
	 *            returned by {@link IBreadCrumbParticipant#getComponent()}.
	 * @return The bread crumb participant
	 */
	protected abstract IBreadCrumbParticipant getParticipant(MarkupContainer parent, String componentId);
}