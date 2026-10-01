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
package org.apache.wicket.markup.html.form;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;
import org.apache.wicket.settings.ExceptionSettings.NotRenderableErrorStrategy;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

class AutoLabelCheckGroupTest extends WicketTestCase
{
	@Test
	void autoLabelSupportsBodyOnlyCheckGroup()
	{
		tester.getApplication().getExceptionSettings()
			.setNotRenderableErrorStrategy(NotRenderableErrorStrategy.THROW_EXCEPTION);
		CheckGroupPage page = new CheckGroupPage();
		CheckGroup<?> group = (CheckGroup<?>)page.get("form:categories");
		assertTrue(group.getRenderBodyOnly());
		assertFalse(group.getOutputMarkupId());
		group.setRequired(true);

		tester.startPage(page);

		assertFalse(group.getOutputMarkupId());
		assertNull(group.getMarkupId(false));
		TagTester label = TagTester.createTagByAttribute(tester.getLastResponseAsString(), "wicket:for", "categories");
		assertNotNull(label);
		assertFalse(label.hasAttribute("for"));
		assertEquals("Categories", label.getValue());
		assertTrue(label.getAttribute("class").contains("required"));
		String labelId = label.getAttribute("id");
		assertEquals(AutoLabelResolver.getLabelIdFor(group), labelId);

		group.setEnabled(false);
		group.error("Invalid selection");
		tester.startPage(page);

		assertFalse(group.getOutputMarkupId());
		label = TagTester.createTagByAttribute(tester.getLastResponseAsString(), "wicket:for", "categories");
		assertFalse(label.hasAttribute("for"));
		assertTrue(label.getAttribute("class").contains("disabled"));
		assertTrue(label.getAttribute("class").contains("error"));
		assertEquals(labelId, label.getAttribute("id"));
		assertNull(group.getMarkupId(false));
	}

	@Test
	void ajaxUpdatesUseTheBodyOnlyTargetsLabelId()
	{
		CheckGroupPage page = new CheckGroupPage();
		tester.startPage(page);
		CheckGroup<?> group = (CheckGroup<?>)page.get("form:categories");
		String labelId = AutoLabelResolver.getLabelIdFor(group);
		IPartialPageRequestHandler target = mock(IPartialPageRequestHandler.class);

		group.setRequired(true);
		group.setEnabled(false);
		group.error("Invalid selection");
		group.getMetaData(AutoLabelResolver.MARKER_KEY).updateFrom(group, target);

		verify(target).appendJavaScript("Wicket.DOM.toggleClass('" + labelId + "', 'required', true);");
		verify(target).appendJavaScript("Wicket.DOM.toggleClass('" + labelId + "', 'disabled', true);");
		verify(target).appendJavaScript("Wicket.DOM.toggleClass('" + labelId + "', 'error', true);");
		assertFalse(group.getOutputMarkupId());
		assertNull(group.getMarkupId(false));
	}

	public static class CheckGroupPage extends WebPage implements IMarkupResourceStreamProvider
	{
		public CheckGroupPage()
		{
			Form<Void> form = new Form<>("form");
			CheckGroup<String> group = new CheckGroup<>("categories", Model.ofList(new ArrayList<>()));
			group.setLabel(Model.of("Categories"));
			form.add(group);
			add(form);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass)
		{
			return new StringResourceStream("""
				<html xmlns:wicket="http://wicket.apache.org">
					<body>
						<form wicket:id="form">
							<label wicket:for="categories"><wicket:label/></label>
							<wicket:container wicket:id="categories"></wicket:container>
						</form>
					</body>
				</html>
				""");
		}
	}
}
