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

import java.util.Optional;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.WebPage;

/**
 * @author jcompagner
 */
public class AjaxHeaderContributionPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 */
	public AjaxHeaderContributionPage()
	{
		final Component test1 = new AjaxHeaderContribution("test1").setOutputMarkupId(true);
		add(test1);
		final Component test2 = new AjaxHeaderContribution("test2").setOutputMarkupId(true);
		add(test2);
		final Component test3 = new AjaxHeaderContribution("test3").setOutputMarkupId(true);
		add(test3);
		add(new AjaxFallbackLink<Void>("link")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(Optional<AjaxRequestTarget> targetOptional)
			{
				targetOptional.ifPresent(target -> {
					target.prependJavaScript("prepend();");
					target.add(test1);
					target.add(test2);
					target.add(test3);
					target.appendJavaScript("append();");
				});
			}
		});
	}
}
