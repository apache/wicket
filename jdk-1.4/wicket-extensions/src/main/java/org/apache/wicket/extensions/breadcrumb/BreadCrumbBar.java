/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.extensions.breadcrumb;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.LoadableDetachableModel;


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
	/** Default crumb component. */
	private static final class BreadCrumbComponent extends Panel
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
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
		public BreadCrumbComponent(String id, int index, IBreadCrumbModel breadCrumbModel,
				final IBreadCrumbParticipant breadCrumbParticipant, boolean enableLink)
		{
			super(id);
			add(new Label("sep", (index > 0) ? "/" : "").setEscapeModelStrings(false)
					.setRenderBodyOnly(true));
			BreadCrumbLink link = new BreadCrumbLink("link", breadCrumbModel)
			{
				private static final long serialVersionUID = 1L;

				protected IBreadCrumbParticipant getParticipant(String componentId)
				{
					return breadCrumbParticipant;
				}
			};
			link.setEnabled(enableLink);
			add(link);
			link.add(new Label("label", breadCrumbParticipant.getTitle()).setRenderBodyOnly(true));
		}
	}

	/**
	 * List view for rendering the bread crumbs.
	 */
	protected class BreadCrumbsListView extends ListView implements IBreadCrumbModelListener
	{
		private static final long serialVersionUID = 1L;

		private transient boolean dirty = false;

		private transient int size;

		/**
		 * Construct.
		 * 
		 * @param id
		 *            Component id
		 */
		public BreadCrumbsListView(String id)
		{
			super(id);
			setReuseItems(false);
			setModel(new LoadableDetachableModel()
			{
				private static final long serialVersionUID = 1L;

				protected Object load()
				{
					// save a copy
					List l = new ArrayList(allBreadCrumbParticipants());
					size = l.size();
					return l;
				}
			});
		}

		/**
		 * @see org.apache.wicket.extensions.breadcrumb.IBreadCrumbModelListener#breadCrumbActivated(org.apache.wicket.extensions.breadcrumb.IBreadCrumbParticipant,
		 *      org.apache.wicket.extensions.breadcrumb.IBreadCrumbParticipant)
		 */
		public void breadCrumbActivated(IBreadCrumbParticipant previousParticipant,
				IBreadCrumbParticipant breadCrumbParticipant)
		{
			signalModelChange();
		}

		/**
		 * @see org.apache.wicket.extensions.breadcrumb.IBreadCrumbModelListener#breadCrumbAdded(org.apache.wicket.extensions.breadcrumb.IBreadCrumbParticipant)
		 */
		public void breadCrumbAdded(IBreadCrumbParticipant breadCrumbParticipant)
		{
		}

		/**
		 * @see org.apache.wicket.extensions.breadcrumb.IBreadCrumbModelListener#breadCrumbRemoved(org.apache.wicket.extensions.breadcrumb.IBreadCrumbParticipant)
		 */
		public void breadCrumbRemoved(IBreadCrumbParticipant breadCrumbParticipant)
		{
		}

		/**
		 * Signal model change.
		 */
		private void signalModelChange()
		{
			// else let the listview recalculate it's childs immediately;
			// it was attached, but it needs to go trhough that again now
			// as the signalling component attached after this
			getModel().detach();
			super.internalOnAttach();
		}

		/**
		 * @see org.apache.wicket.markup.html.list.ListView#onBeforeRender()
		 */
		protected void onBeforeRender()
		{
			super.onBeforeRender();
			if (dirty)
			{
				super.internalOnAttach();
				this.dirty = false;
			}
		}

		/**
		 * @see org.apache.wicket.markup.html.list.ListView#populateItem(org.apache.wicket.markup.html.list.ListItem)
		 */
		protected void populateItem(ListItem item)
		{
			int index = item.getIndex();
			IBreadCrumbParticipant breadCrumbParticipant = (IBreadCrumbParticipant)item
					.getModelObject();
			item.add(newBreadCrumbComponent("crumb", index, size, breadCrumbParticipant));
		}
	}

	private static final long serialVersionUID = 1L;

	/** The currently active participant, if any (possibly null). */
	private IBreadCrumbParticipant activeParticipant = null;

	/** Holds the current list of crumbs. */
	private List crumbs = new ArrayList();

	/** listeners utility. */
	private final BreadCrumbModelListenerSupport listenerSupport = new BreadCrumbModelListenerSupport();

	/**
	 * Construct.
	 * 
	 * @param id
	 *            Component id
	 */
	public BreadCrumbBar(String id)
	{
		super(id);
		BreadCrumbsListView breadCrumbsListView = new BreadCrumbsListView("crumbs");
		addListener(breadCrumbsListView);
		add(breadCrumbsListView);
	}

	/**
	 * @see org.apache.wicket.extensions.breadcrumb.IBreadCrumbModel#addListener(org.apache.wicket.extensions.breadcrumb.IBreadCrumbModelListener)
	 */
	public final void addListener(IBreadCrumbModelListener listener)
	{
		this.listenerSupport.addListener(listener);
	}

	/**
	 * @see org.apache.wicket.extensions.breadcrumb.IBreadCrumbModel#allBreadCrumbParticipants()
	 */
	public final List allBreadCrumbParticipants()
	{
		return crumbs;
	}

	/**
	 * @see org.apache.wicket.extensions.breadcrumb.IBreadCrumbModel#getActive()
	 */
	public IBreadCrumbParticipant getActive()
	{
		return activeParticipant;
	}

	/**
	 * @see org.apache.wicket.extensions.breadcrumb.IBreadCrumbModel#removeListener(org.apache.wicket.extensions.breadcrumb.IBreadCrumbModelListener)
	 */
	public final void removeListener(IBreadCrumbModelListener listener)
	{
		this.listenerSupport.removeListener(listener);
	}

	/**
	 * @see org.apache.wicket.extensions.breadcrumb.IBreadCrumbModel#setActive(org.apache.wicket.extensions.breadcrumb.IBreadCrumbParticipant)
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
			IBreadCrumbParticipant temp = (IBreadCrumbParticipant)crumbs.get(i);

			// if we found the bread crumb
			if (breadCrumbParticipant.equals(temp))
			{
				// remove the bread crumbs after this one
				int j = len;
				while (j > i)
				{
					// remove and fire event
					IBreadCrumbParticipant removed = (IBreadCrumbParticipant)crumbs.remove(j--);
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
	protected Component newBreadCrumbComponent(String id, int index, int total,
			IBreadCrumbParticipant breadCrumbParticipant)
	{
		boolean enableLink = getEnableLinkToCurrent() || (index < (total - 1));
		return new BreadCrumbComponent(id, index, this, breadCrumbParticipant, enableLink);
	}

	/**
	 * @see org.apache.wicket.Component#onDetach()
	 */
	protected void onDetach()
	{
		super.onDetach();
		for (Iterator i = crumbs.iterator(); i.hasNext();)
		{
			IBreadCrumbParticipant crumb = (IBreadCrumbParticipant)i.next();
			if (crumb instanceof Component)
			{
				((Component)crumb).detach();
			}
			else if (crumb instanceof IDetachable)
			{
				((IDetachable)crumb).detach();
			}
		}
	}
}
