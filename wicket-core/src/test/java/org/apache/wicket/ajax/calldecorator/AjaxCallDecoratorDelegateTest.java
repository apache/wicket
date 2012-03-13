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
package org.apache.wicket.ajax.calldecorator;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.IComponentAwareHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.junit.Test;

/**
 * Tests for IAjaxCallDecoratorDelegate
 * 
 * @since 1.5
 */
public class AjaxCallDecoratorDelegateTest extends WicketTestCase
{
	/**
	 * Tests that the delegates of IAjaxCallDecorator also contribute to the header
	 * https://issues.apache.org/jira/browse/WICKET-4347
	 */
	@Test
	public void delegateShouldContributeToTheHeader()
	{

		tester.startPage(new HomePage());

		tester.assertContains("<script type=\"text/javascript\" src=\"./resource/org.apache.wicket.ajax.calldecorator.AjaxCallDecoratorDelegateTest\\$ContributingDecorator/myscript.js\"></script>");
	}

	private static class HomePage extends WebPage implements IMarkupResourceStreamProvider
	{
		public HomePage()
		{
			add(new AjaxLink("link")
			{

				@Override
				public void onClick(AjaxRequestTarget target)
				{
				}

				@Override
				protected IAjaxCallDecorator getAjaxCallDecorator()
				{
					return new ContributingDecorator(super.getAjaxCallDecorator());
				}
			});
		}

		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<html><body><a wicket:id='link'>link</a></body></html>");
		}
	}

	private static class ContributingDecorator extends AjaxPreprocessingCallDecorator
		implements
			IComponentAwareHeaderContributor
	{

		private ContributingDecorator(IAjaxCallDecorator delegate)
		{
			super(delegate);
		}

		public void renderHead(Component component, IHeaderResponse response)
		{
			response.renderJavaScriptReference(new PackageResourceReference(getClass(),
				"myscript.js"));
		}
	}

}
