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
package org.apache.wicket.ajax;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.junit.Test;

/**
 *
 */
public class GlobalUpdateAjaxAttributesTest extends WicketTestCase
{
	@Override
	protected WebApplication newApplication()
	{
		WebApplication application = super.newApplication();
		application.getAjaxRequestTargetListeners().add(new AjaxRequestTarget.AbstractListener()
		{
			@Override
			public void updateAjaxAttributes(Behavior behavior, AjaxRequestAttributes attributes)
			{
				super.updateAjaxAttributes(behavior, attributes);
				attributes
					.setChannel(new AjaxChannel("globalAjaxChannel", AjaxChannel.Type.ACTIVE));
			}
		});
		return application;
	}

	/**
	 * Tests that AjaxRequestTarget listeners can update the ajax attributes
	 * https://issues.apache.org/jira/browse/WICKET-4958
	 */
	@Test
	public void globalUpdateAjaxAttributes()
	{
		tester.startPage(new GlobalUpdateAjaxAttributesPage());
//		System.err.println(tester.getLastResponseAsString());
		tester.getLastResponseAsString().contains("\"ch\":\"globalAjaxChannel|a\"");
	}

	private static class GlobalUpdateAjaxAttributesPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private GlobalUpdateAjaxAttributesPage()
		{
			add(new AjaxLink<Void>("ajaxLink") {

				@Override
				public void onClick(AjaxRequestTarget target)
				{
				}
			});
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass)
		{
			return new StringResourceStream("<html><body><a wicket:id='ajaxLink'>Link</a></body></html>");
		}
	}
}
