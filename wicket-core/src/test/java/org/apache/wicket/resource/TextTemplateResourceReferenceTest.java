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
package org.apache.wicket.resource;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.junit.Test;

/**
 * https://issues.apache.org/jira/browse/WICKET-3971
 */
public class TextTemplateResourceReferenceTest extends WicketTestCase
{

	private static final String TEMPLATE_NAME = "textTemplateResRef.tmpl";

	private static final String EXPECTED_VALUE = "value";
	private static final String SECOND_EXPECTED_VALUE = "second-value";
	private static final String VARIABLE_NAME = "variable";

	/**
	 * https://issues.apache.org/jira/browse/WICKET-3971
	 */
	@Test
	public void renderInterpolatedTemplate()
	{
		// the page will render just <script> element with url to the template
		// this will register it in the application's ResourceReferenceRegistry
		TemplateResourceReferencePage page = new TemplateResourceReferencePage();
		tester.startPage(page);

		// make a separate request to the template resource
		CharSequence urlForTemplate = page.urlFor(new PackageResourceReference(
			TextTemplateResourceReferenceTest.class, TEMPLATE_NAME), null);
		tester.executeUrl(urlForTemplate.toString());
		tester.assertContains("TMPL_START\\|" + EXPECTED_VALUE + "\\|TMPL_END");

		// update the model and re-render (WICKET-4487)
		page.variables.put(VARIABLE_NAME, SECOND_EXPECTED_VALUE);
		tester.executeUrl(urlForTemplate.toString());
		tester.assertContains("TMPL_START\\|"+SECOND_EXPECTED_VALUE+"\\|TMPL_END");

	}

	private static class TemplateResourceReferencePage extends WebPage
		implements
			IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		private final Map<String, Object> variables = new HashMap<String, Object>();

		@Override
		public void renderHead(IHeaderResponse response)
		{
			super.renderHead(response);

			variables.put(VARIABLE_NAME, EXPECTED_VALUE);

			final TextTemplateResourceReference reference = new TextTemplateResourceReference(
				TextTemplateResourceReferenceTest.class, TEMPLATE_NAME, Model.ofMap(variables));
			response.renderJavaScriptReference(reference);
		}

		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><body></body></html>");
		}

	}
}
