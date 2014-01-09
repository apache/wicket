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
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.LoadableDetachableModel;


/**
 * A component that renders bread crumbs. By default, it renders a horizontal list from left to
 * right (oldest left) with bread crumb links and a ' / ' as a separator, e.g.
 * 
 * <pre>
 * first / second / third
 * </pre>
 * 
 * <p>
 * Delegates how the bread crumb model works to {@link DefaultBreadCrumbsModel}.
 * </p>
 * <p>
 * Override and provide your own markup file if you want to work with other elements, e.g. uls
 * instead of spans.
 * </p>
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
		 * @param separatorMarkup
		 *            markup used as a separator between breadcrumbs
		 * @param index
		 *            The index of the bread crumb
		 * @param breadCrumbModel
		 *            The bread crumb model
		 * @param breadCrumbParticipant
		 *            The bread crumb
		 * @param enableLink
		 *            Whether the link should be enabled
		 */
		public BreadCrumbComponent(final String id, final String separatorMarkup, final long index,
			final IBreadCrumbModel breadCrumbModel,
			final IBreadCrumbParticipant breadCrumbParticipant, final boolean enableLink)
		{
			super(id);
			add(new Label("sep", (index > 0) ? separatorMarkup : "").setEscapeModelStrings(false)
				.setRenderBodyOnly(true));
			BreadCrumbLink link = new BreadCrumbLink("link", breadCrumbModel)
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected IBreadCrumbParticipant getParticipant(final String componentId)
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
	protected class BreadCrumbsListView extends ListView<IBreadCrumbParticipant>
		implements
			IBreadCrumbModelListener
	{
		private static final long serialVersionUID = 1L;

		private transient boolean dirty = false;

		private transient int size = 0;

		/**
		 * Construct.
		 * 
		 * @param id
		 *            Component id
		 */
		public BreadCrumbsListView(final String id)
		{
			super(id);
			setReuseItems(false);
			setDefaultModel(new LoadableDetachableModel<List<IBreadCrumbParticipant>>()
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

		@Override
		public void breadCrumbActivated(final IBreadCrumbParticipant previousParticipant,
			final IBreadCrumbParticipant breadCrumbParticipant)
		{
			signalModelChange();
		}

		@Override
		public void breadCrumbAdded(final IBreadCrumbParticipant breadCrumbParticipant)
		{
		}

		@Override
		public void breadCrumbRemoved(final IBreadCrumbParticipant breadCrumbParticipant)
		{
		}

		/**
		 * Signal model change.
		 */
		private void signalModelChange()
		{
			// else let the listview recalculate it's children immediately;
			// it was attached, but it needs to go through that again now
			// as the signaling component attached after this
			getDefaultModel().detach();
		}

		/**
		 * @see org.apache.wicket.markup.html.list.ListView#onBeforeRender()
		 */
		@Override
		protected void onBeforeRender()
		{
			super.onBeforeRender();
			if (dirty)
			{
				dirty = false;
			}
		}

		@Override
		protected void populateItem(final ListItem<IBreadCrumbParticipant> item)
		{
			long index = item.getIndex();
			IBreadCrumbParticipant breadCrumbParticipant = (IBreadCrumbParticipant)item.getDefaultModelObject();
			item.add(newBreadCrumbComponent("crumb", index, size, breadCrumbParticipant));
		}
	}

	private static final long serialVersionUID = 1L;

	private final IBreadCrumbModel decorated;

	/**
	 * Construct.
	 * 
	 * @param id
	 *            Component id
	 */
	public BreadCrumbBar(final String id)
	{
		super(id);
		decorated = new DefaultBreadCrumbsModel();
		BreadCrumbsListView breadCrumbsListView = new BreadCrumbsListView("crumbs");
		addListener(breadCrumbsListView);
		add(breadCrumbsListView);
	}


	@Override
	public void addListener(final IBreadCrumbModelListener listener)
	{
		decorated.addListener(listener);
	}

	@Override
	public List<IBreadCrumbParticipant> allBreadCrumbParticipants()
	{
		return decorated.allBreadCrumbParticipants();
	}

	@Override
	public IBreadCrumbParticipant getActive()
	{
		return decorated.getActive();
	}

	@Override
	public void removeListener(final IBreadCrumbModelListener listener)
	{
		decorated.removeListener(listener);
	}

	@Override
	public void setActive(final IBreadCrumbParticipant breadCrumbParticipant)
	{
		decorated.setActive(breadCrumbParticipant);
	}

	/**
	 * Gets whether the current bread crumb should be displayed as a link (e.g. for refreshing) or
	 * as a disabled link (effectively just a label). The latter is the default. Override if you
	 * want different behavior.
	 * 
	 * @return Whether the current bread crumb should be displayed as a link; this method returns
	 *         false
	 */
	protected boolean getEnableLinkToCurrent()
	{
		return false;
	}

	/**
	 * @return markup used as a separator between breadcrumbs. By default <code>/</code> is used,
	 *         but <code>&gt;&gt;</code> is also a popular choice.
	 */
	protected String getSeparatorMarkup()
	{
		return "/";
	}

	/**
	 * Creates a new bread crumb component. That component will be rendered as part of the bread
	 * crumbs list (which is a &lt;ul&gt; &lt;li&gt; structure).
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
	protected Component newBreadCrumbComponent(final String id, final long index, final int total,
		final IBreadCrumbParticipant breadCrumbParticipant)
	{
		boolean enableLink = getEnableLinkToCurrent() || (index < (total - 1));
		return new BreadCrumbComponent(id, getSeparatorMarkup(), index, this,
			breadCrumbParticipant, enableLink);
	}

	@Override
	protected void onDetach()
	{
		super.onDetach();
		for (IBreadCrumbParticipant crumb : decorated.allBreadCrumbParticipants())
		{
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
