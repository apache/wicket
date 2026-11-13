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
package org.apache.wicket.extensions.markup.html.repeater.data.table.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

class ChoiceFilterTest extends WicketTestCase
{
	@Test
	void autoSubmitIsRegisteredFromTheHeaderInsteadOfAnInlineHandler()
	{
		ChoiceFilterTestPage page = tester.startPage(new ChoiceFilterTestPage(true));

		DropDownChoice<?> choice = page.filter.getChoice();
		TagTester select = tester.getTagByWicketId("filter");
		assertThat(select.getAttribute("onchange")).isNull();
		assertThat(tester.getLastResponseAsString()).contains("Wicket.Event.add('" +
			choice.getMarkupId() + "', 'change', function(event) { this.form.submit();;});");
	}

	@Test
	void noAutoSubmitRegistersNoChangeHandler()
	{
		tester.startPage(new ChoiceFilterTestPage(false));

		assertThat(tester.getTagByWicketId("filter").getAttribute("onchange")).isNull();
		assertThat(tester.getLastResponseAsString()).doesNotContain("'change'");
	}

	public static class ChoiceFilterTestPage extends WebPage
	{
		private static final long serialVersionUID = 1L;

		final ChoiceFilter<String> filter;

		ChoiceFilterTestPage(boolean autoSubmit)
		{
			FilterForm<String> form = new FilterForm<>("form", new IFilterStateLocator<>()
			{
				private static final long serialVersionUID = 1L;

				private String state;

				@Override
				public String getFilterState()
				{
					return state;
				}

				@Override
				public void setFilterState(String state)
				{
					this.state = state;
				}
			});
			add(form);
			filter = new ChoiceFilter<>("choice", Model.of(), form, List.of("a", "b"), autoSubmit);
			form.add(filter);
		}
	}
}
