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
package org.apache.wicket.islands;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;

/**
 * A page with an island and a classic component its calls update.
 */
public class IslandTestPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Props containing every character that needs escaping inside a script element.
	 */
	public static final String TRICKY_NAME = "</script><!-- &     >";

	public record TestProps(String name, int count)
	{
	}

	public record Add(int amount)
	{
	}

	public record Sum(int total)
	{
	}

	private int count;

	final Label label;

	final Island<TestProps> island;

	public IslandTestPage()
	{
		label = new Label("label", () -> "count " + count);
		label.setOutputMarkupId(true);
		add(label);

		island = new Island<>("island", "test-island", () -> new TestProps(TRICKY_NAME, count));
		island.onCall("add", Add.class, Sum.class, (request, call) -> {
			call.getTarget().add(label);
			if (request.amount() < 0)
			{
				throw call.reject("negative");
			}
			count += request.amount();
			call.invalidate("totals");
			return new Sum(count);
		});
		island.onCall("reset", Void.class, (request, call) -> count = 0);
		island.onCall("boom", Void.class, (request, call) -> {
			throw new IllegalStateException("secret detail");
		});
		island.usePageless("echo");
		island.usePageless("sum", "echo");
		add(island);

		add(new AjaxLink<Void>("invalidate")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				IslandTargets.invalidate(target, "totals", "scores");
			}
		});
	}
}
