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
package org.apache.wicket.extensions.ajax.markup.html;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * The value an editable label shows when its model is empty comes from an overridable method, so it
 * is escaped where it is written, the same way the label escapes the model value it normally shows.
 * <p>
 * The escaping follows escapeModelStrings, which the label takes from the panel in onConfigure, so
 * setting it on the panel decides whether the null label is escaped.
 */
public class AjaxEditableLabelNullLabelEscapeTest extends WicketTestCase
{
	private static final String MARKUP = "<script>x=1</script>";

	private static final String ESCAPED = "&lt;script&gt;x=1&lt;/script&gt;";

	/** Renders one editable label over an empty model, so the null label is what is shown. */
	private static class NullLabelPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private NullLabelPage(AjaxEditableLabel<String> label)
		{
			add(label);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<html><body><div wicket:id=\"label\"></div></body></html>");
		}
	}

	private static AjaxEditableLabel<String> plainLabel()
	{
		return new AjaxEditableLabel<String>("label", Model.of(""))
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected String defaultNullLabel()
			{
				return MARKUP;
			}
		};
	}

	private static AjaxEditableLabel<String> multiLineLabel()
	{
		return new AjaxEditableMultiLineLabel<String>("label", Model.of(""))
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected String defaultNullLabel()
			{
				return MARKUP;
			}
		};
	}

	private static AjaxEditableLabel<String> choiceLabel()
	{
		IModel<java.util.List<String>> choices = Model.ofList(Arrays.asList("A", "B"));
		return new AjaxEditableChoiceLabel<String>("label", Model.of(""), choices)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected String defaultNullLabel()
			{
				return MARKUP;
			}
		};
	}

	private void assertNullLabelEscaped(AjaxEditableLabel<String> label)
	{
		tester.startPage(new NullLabelPage(label));

		String response = tester.getLastResponseAsString();
		assertTrue("the null label should be escaped",
			response.contains(ESCAPED));
		assertFalse("the null label should not reach the markup as markup",
			response.contains(MARKUP));
	}

	@Test
	public void nullLabelOfAnEditableLabelIsEscaped()
	{
		assertNullLabelEscaped(plainLabel());
	}

	@Test
	public void nullLabelOfAnEditableMultiLineLabelIsEscaped()
	{
		assertNullLabelEscaped(multiLineLabel());
	}

	/**
	 * The choice label writes the null label in one branch and the value the renderer produced in
	 * the other, so both branches of the same method have to escape.
	 */
	@Test
	public void nullLabelOfAnEditableChoiceLabelIsEscaped()
	{
		assertNullLabelEscaped(choiceLabel());
	}

	/**
	 * Escaping follows the setting the panel passes to its label, so markup can still be shown on
	 * purpose.
	 */
	@Test
	public void nullLabelIsNotEscapedWhenEscapeModelStringsIsFalse()
	{
		AjaxEditableLabel<String> label = plainLabel();
		label.setEscapeModelStrings(false);
		tester.startPage(new NullLabelPage(label));

		assertTrue("escaping is off, so the null label should be written as is",
			tester.getLastResponseAsString().contains(MARKUP));
	}
}
