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
package org.apache.wicket.util.tester;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.junit.Test;

/**
 * https://issues.apache.org/jira/browse/WICKET-5426
 */
public abstract class WicketTesterLazyIsPageStatelessBase extends WicketTestCase
{
	/**
	 * The page must be stateless because the stateful component
	 * is hidden in #onConfigure
	 *
	 * @throws Exception
	 */
	@Test
	public void isStateless() throws Exception
	{
		tester.startPage(MyPage.class);

		tester.assertLabel("isPageStateless", "true");
		assertTrue(tester.getLastRenderedPage().isPageStateless());
	}

	public static class MyPage extends WebPage implements IMarkupResourceStreamProvider
	{
		public MyPage()
		{
			add(new AjaxLink<Void>("link")
			{
				@Override
				public void onClick(AjaxRequestTarget target)
				{
				}
			}.add(new Behavior()
			{
				@Override
				public void onConfigure(Component c)
				{
					c.setVisible(false);
				}
			}));
			add(new Label("isPageStateless", new AbstractReadOnlyModel<Boolean>()
			{
				@Override
				public Boolean getObject()
				{
					return MyPage.this.isPageStateless();
				}
			}));
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass)
		{
			return new StringResourceStream("<html>\n" +
						"<body>\n" +
							"\t<a wicket:id=\"link\" />\n" +
							"\t<div wicket:id=\"isPageStateless\" />\n" +
						"</body>\n" +
					"</html>");
		}
	}

}
