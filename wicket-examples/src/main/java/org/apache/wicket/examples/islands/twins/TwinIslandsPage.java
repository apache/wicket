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
package org.apache.wicket.examples.islands.twins;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.islands.IslandSettings;
import org.apache.wicket.islands.modules.ViteManifestModuleLoader;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;

/**
 * One poll, two islands: the same {@link PollIsland} rendered by a React component and by a
 * Svelte component. A call of either changes the poll and updates both, and the plain Wicket
 * summary below them.
 */
public class TwinIslandsPage extends WicketExamplePage
{
	private static final long serialVersionUID = 1L;

	private final IModel<Poll> poll = Model.of(Poll.start());

	private final PollIsland reactPoll;

	private final PollIsland sveltePoll;

	private final Label summary;

	public TwinIslandsPage()
	{
		reactPoll = newPoll("reactPoll", "poll-react");
		sveltePoll = newPoll("sveltePoll", "poll-svelte");
		summary = new Label("summary", this::getSummary);
		summary.setOutputMarkupId(true);
		add(reactPoll, sveltePoll, summary);

		add(new AjaxLink<Void>("reset")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				poll.getObject().reset();
				onPollChanged(target);
			}
		});

		add(new WebMarkupContainer("noBuild")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onConfigure()
			{
				super.onConfigure();
				setVisible(IslandSettings.get()
					.getModuleLoader() instanceof ViteManifestModuleLoader loader &&
					loader.getManifest() == null);
			}
		});
	}

	private PollIsland newPoll(String id, String type)
	{
		return new PollIsland(id, type, poll)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onPollChanged(AjaxRequestTarget target)
			{
				TwinIslandsPage.this.onPollChanged(target);
			}
		};
	}

	private void onPollChanged(AjaxRequestTarget target)
	{
		target.add(reactPoll, sveltePoll, summary);
	}

	private String getSummary()
	{
		Poll current = poll.getObject();
		int total = current.getTotal();
		String votes = total == 1 ? "1 vote" : total + " votes";
		return current.getLeader()
			.map(leader -> votes + ", " + leader.getLabel() + " leads.")
			.orElse(total == 0 ? "No votes yet." : votes + ", no option leads.");
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);

		response.render(CssHeaderItem.forReference(
			new CssResourceReference(TwinIslandsPage.class, "TwinIslandsPage.css")));
	}
}
