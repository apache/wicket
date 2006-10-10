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

import java.util.ArrayList;
import java.util.List;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.markup.html.basic.Label;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.markup.html.panel.Panel;
import wicket.model.LoadableDetachableModel;

/**
 * A component that renders bread crumbs. By default, it renders a horizontal
 * list from left to right (oldest left) with bread crumb links and a ' / ' as a
 * seperator, e.g.
 * 
 * <pre>
 * first / second / third
 * </pre>
 * 
 * This component also functions as a implementation of
 * {@link IBreadCrumbModel bread crumb model}. This component holds the state
 * as well as doing the rendering. Override and provide your own markup file if
 * you want to work with other elements, e.g. uls instead of spans.
 * 
 * @author Eelco Hillenius
 */
public class BreadCrumbBar extends Panel implements IBreadCrumbModel
{
	/**
	 * List view for rendering the bread crumbs.
	 */
	protected class BreadCrumbsListView extends ListView<IBreadCrumbParticipant>
			implements
				IBreadCrumbModelListener
	{
		private static final long serialVersionUID = 1L;

		private transient boolean attachedButNotRendered = false;

		private transient boolean dirty = false;

		private transient int size;

		/**
		 * Construct.
		 * 
		 * @param parent
		 *            The parent component
		 * 
		 * @param id
		 *            Component id
		 */
		public BreadCrumbsListView(MarkupContainer parent, final String id)
		{
			super(parent, id);
			setReuseItems(false);
			setModel(new LoadableDetachableModel<List<IBreadCrumbParticipant>>()
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected List<IBreadCrumbParticipant> load()
				{
					// save a copy
					List<IBreadCrumbParticipant> l = new ArrayList<IBreadCrumbParticipant>(
							allBreadCrumbParticipants());
					size = l.size();
					return l;
				}
			});
		}

		/**
		 * @see wicket.extensions.breadcrumb.IBreadCrumbModelListener#breadCrumbActivated(wicket.extensions.breadcrumb.IBreadCrumbParticipant,
		 *      wicket.extensions.breadcrumb.IBreadCrumbParticipant)
		 */
		public void breadCrumbActivated(IBreadCrumbParticipant previousParticipant,
				IBreadCrumbParticipant breadCrumbParticipant)
		{
			signalModelChange();
		}

		/**
		 * @see wicket.extensions.breadcrumb.IBreadCrumbModelListener#breadCrumbAdded(wicket.extensions.breadcrumb.IBreadCrumbParticipant)
		 */
		public void breadCrumbAdded(IBreadCrumbParticipant breadCrumbParticipant)
		{
		}

		/**
		 * @see wicket.extensions.breadcrumb.IBreadCrumbModelListener#breadCrumbRemoved(wicket.extensions.breadcrumb.IBreadCrumbParticipant)
		 */
		public void breadCrumbRemoved(IBreadCrumbParticipant breadCrumbParticipant)
		{
		}

		/**
		 * @see wicket.markup.html.list.ListView#internalOnAttach()
		 */
		@Override
		protected void internalOnAttach()
		{
			if (dirty)
			{
				super.internalOnAttach();
				this.dirty = false;
			}
			attachedButNotRendered = true;
		}

		/**
		 * @see wicket.Component#onBeforeRender()
		 */
		@Override
		protected void onBeforeRender()
		{
			// it this point, we can't change the hierarchy anymore
			attachedButNotRendered = false;
		}

		/**
		 * @see wicket.markup.html.list.ListView#populateItem(wicket.markup.html.list.ListItem)
		 */
		@Override
		protected void populateItem(ListItem item)
		{
			int index = item.getIndex();
			IBreadCrumbParticipant breadCrumbParticipant = (IBreadCrumbParticipant)item
					.getModelObject();
			newBreadCrumbComponent(item, "crumb", index, size, breadCrumbParticipant);
		}

		/**
		 * Signal model change.
		 */
		private void signalModelChange()
		{
			if (!attachedButNotRendered)
			{
				// if the list view was not yet attached, or
				// it has already been rendered, setting it dirty will suffice
				// it will have the effect that next time the list view
				// is processes, it will recalculate it's children
				this.dirty = true;
			}
			else
			{
				// else let the listview recalculate it's childs immediately;
				// it was attached, but it needs to go trhough that again now
				// as the signalling component attached after this
				getModel().detach();
				super.internalOnAttach();
			}
		}
	}

	/** Default crumb component. */
	private static final class BreadCrumbComponent extends Panel
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param parent
		 *            The parent component
		 * 
		 * @param id
		 *            Component id
		 * @param index
		 *            The index of the bread crumb
		 * @param breadCrumbModel
		 *            The bread crumb model
		 * @param breadCrumbParticipant
		 *            The bread crumb
		 * @param enableLink
		 *            Whether the link should be enabled
		 */
		public BreadCrumbComponent(MarkupContainer parent, final String id, int index,
				IBreadCrumbModel breadCrumbModel,
				final IBreadCrumbParticipant breadCrumbParticipant, boolean enableLink)
		{
			super(parent, id);
			new Label(this, "sep", (index > 0) ? "/" : "").setEscapeModelStrings(false)
					.setRenderBodyOnly(true);
			BreadCrumbLink link = new BreadCrumbLink(this, "link", breadCrumbModel)
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected IBreadCrumbParticipant getParticipant(MarkupContainer parent,
						String componentId)
				{
					return breadCrumbParticipant;
				}
			};
			link.setEnabled(enableLink);
			new Label(link, "label", breadCrumbParticipant.getTitle()).setRenderBodyOnly(true);
		}
	}

	private static final long serialVersionUID = 1L;

	/** The currently active participant, if any (possibly null). */
	private IBreadCrumbParticipant activeParticipant = null;

	/** Holds the current list of crumbs. */
	private List<IBreadCrumbParticipant> crumbs = new ArrayList<IBreadCrumbParticipant>();

	/** listeners utility. */
	private final BreadCrumbModelListenerSupport listenerSupport = new BreadCrumbModelListenerSupport();

	/**
	 * Construct. Adds the default style.
	 * 
	 * @param parent
	 *            Parent component
	 * 
	 * @param id
	 *            Component id
	 */
	public BreadCrumbBar(MarkupContainer parent, final String id)
	{
		super(parent, id);

		BreadCrumbsListView breadCrumbsListView = new BreadCrumbsListView(this, "crumbs");
		addListener(breadCrumbsListView);
	}

	/**
	 * @see wicket.extensions.breadcrumb.IBreadCrumbModel#addListener(wicket.extensions.breadcrumb.IBreadCrumbModelListener)
	 */
	public final void addListener(IBreadCrumbModelListener listener)
	{
		this.listenerSupport.addListener(listener);
	}

	/**
	 * @see wicket.extensions.breadcrumb.IBreadCrumbModel#allBreadCrumbParticipants()
	 */
	public final List<IBreadCrumbParticipant> allBreadCrumbParticipants()
	{
		return crumbs;
	}

	/**
	 * @see wicket.extensions.breadcrumb.IBreadCrumbModel#getActive()
	 */
	public IBreadCrumbParticipant getActive()
	{
		return activeParticipant;
	}

	/**
	 * @see wicket.extensions.breadcrumb.IBreadCrumbModel#removeListener(wicket.extensions.breadcrumb.IBreadCrumbModelListener)
	 */
	public final void removeListener(IBreadCrumbModelListener listener)
	{
		this.listenerSupport.removeListener(listener);
	}

	/**
	 * @see wicket.extensions.breadcrumb.IBreadCrumbModel#setActive(wicket.extensions.breadcrumb.IBreadCrumbParticipant)
	 */
	public final void setActive(final IBreadCrumbParticipant breadCrumbParticipant)
	{
		// see if the bread crumb was already added, and if so,
		// clean up the stack after (on top of) this bred crumb
		// and notify listeners of the removal
		int len = crumbs.size() - 1;
		int i = len;
		while (i > -1)
		{
			IBreadCrumbParticipant temp = crumbs.get(i);

			// if we found the bread crumb
			if (breadCrumbParticipant.equals(temp))
			{
				// remove the bread crumbs after this one
				int j = len;
				while (j > i)
				{
					// remove and fire event
					IBreadCrumbParticipant removed = crumbs.remove(j--);
					listenerSupport.fireBreadCrumbRemoved(removed);
				}

				// activate the bread crumb participant
				activate(breadCrumbParticipant);

				// we're done; the provided bread crumb is on top
				// and the content is replaced, so just return this function
				return;
			}

			i--;
		}

		// arriving here means we weren't able to find the bread crumb
		// add the new crumb
		crumbs.add(breadCrumbParticipant);

		// and notify listeners
		listenerSupport.fireBreadCrumbAdded(breadCrumbParticipant);

		// activate the bread crumb participant
		activate(breadCrumbParticipant);
	}

	/**
	 * Activates the bread crumb participant.
	 * 
	 * @param breadCrumbParticipant
	 *            The participant to activate
	 */
	protected final void activate(final IBreadCrumbParticipant breadCrumbParticipant)
	{
		// get old value
		IBreadCrumbParticipant previousParticipant = this.activeParticipant;

		// and set the provided participant as the active one
		this.activeParticipant = breadCrumbParticipant;

		// fire bread crumb activated event
		listenerSupport.fireBreadCrumbActivated(previousParticipant, breadCrumbParticipant);

		// signal the bread crumb participant that it is selected as the
		// currently active one
		breadCrumbParticipant.onActivate(previousParticipant);
	}

	/**
	 * Gets whether the current bread crumb should be displayed as a link (e.g.
	 * for refreshing) or as a disabled link (effictively just a label). The
	 * latter is the default. Override if you want different behavior.
	 * 
	 * @return Whether the current bread crumb should be displayed as a link;
	 *         this method returns false
	 */
	protected boolean getEnableLinkToCurrent()
	{
		return false;
	}

	/**
	 * Creates a new bread crumb component. That component will be rendered as
	 * part of the bread crumbs list (which is a &lt;ul&gt; &lt;li&gt;
	 * structure).
	 * 
	 * @param parent
	 *            The parent component
	 * 
	 * @param id
	 *            The component id
	 * @param index
	 *            The index of the bread crumb
	 * @param total
	 *            The total number of bread crumbs in the current model
	 * @param breadCrumbParticipant
	 *            the bread crumb
	 * @return A new bread crumb component
	 */
	protected Component newBreadCrumbComponent(MarkupContainer parent, final String id, int index,
			int total, IBreadCrumbParticipant breadCrumbParticipant)
	{
		boolean enableLink = getEnableLinkToCurrent() || (index < (total - 1));
		return new BreadCrumbComponent(parent, id, index, this, breadCrumbParticipant, enableLink);
	}
}
