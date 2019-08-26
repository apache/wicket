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
package org.apache.wicket.examples.csp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * Page which disallows execution of inline scripts without nonce
 */
public class NonceDemoPage extends WicketExamplePage
{
	private final IModel<Integer> clickMeCountModel = Model.of(0);

	public NonceDemoPage()
	{
		super();
		add(new Label("testNonceScript", getString("testNonceScript")));
		add(new Label("testNoNonceScript", getString("testNoNonceScript")));
		final Label clickMeCount = new Label("clickMeCount", clickMeCountModel);
		clickMeCount.setOutputMarkupId(true);
		add(clickMeCount);
		add(new AjaxLink<String>("clickMe")
		{
			@Override
			public void onClick(AjaxRequestTarget target)
			{
				clickMeCountModel.setObject(clickMeCountModel.getObject() + 1);

				// target.add (works even without unsafe-eval)
				target.add(clickMeCount);
				// append javascript (won't work without unsafe-eval)
				target.appendJavaScript("document.querySelector(\".click-me-text\").innerHTML = \"replaced\";");
			}
		}.setOutputMarkupId(true));
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);
		// Add inline script with nonce
		response.render(JavaScriptHeaderItem.forScript(
				"$(function(){$(\".test-nonce-script\").html(\"Text injected by script with nonce: success\");});",
				"test-nonce-script"
		));
		// Add inline css with nonce
		response.render(CssHeaderItem.forCSS(
				".injected-style--with-nonce{color: green; font-weight: bold;}",
				"injected-style-with-nonce")
		);
	}

	@Override
	public void detachModels()
	{
		super.detachModels();
		clickMeCountModel.detach();
	}
}
