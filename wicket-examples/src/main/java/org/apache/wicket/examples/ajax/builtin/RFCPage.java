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
package org.apache.wicket.examples.ajax.builtin;

import com.github.openjson.JSONObject;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.request.http.WebResponse;

import java.util.Optional;

/**
 * Demonstrates remote function call
 */
public class RFCPage extends BasePage
{
	private int counter1 = 0;
	private int counter2 = 0;

	/**
	 * @return Value of counter1
	 */
	public int getCounter1()
	{
		return counter1;
	}

	/**
	 * @param counter1
	 * 		New value for counter1
	 */
	public void setCounter1(int counter1)
	{
		this.counter1 = counter1;
	}

	/**
	 * @return Value for counter2
	 */
	public int getCounter2()
	{
		return counter2;
	}

	/**
	 * @param counter2
	 * 		New value for counter2
	 */
	public void setCounter2(int counter2)
	{
		this.counter2 = counter2;
	}

	/**
	 * Constructor
	 */
	public RFCPage()
	{
		final Label c1 = new Label("c1", LambdaModel.of(this::getCounter1));
		c1.setOutputMarkupId(true);
		add(c1);
		final Label c2 = new Label("c2", LambdaModel.of(this::getCounter2));
		c2.setOutputMarkupId(true);
		add(c2);
		add(new AjaxLink<Void>("c1-link")
		{
			@Override
			public void onClick(AjaxRequestTarget target)
			{
				counter1++;
				target.add(c1);
				target.prependRemoteFunctionCall("testFunc1Pre", "something1", 1.5);
				target.appendRemoteFunctionCall("testFunc1", "something2", 1);
				JSONObject obj = new JSONObject();
				obj.put("horse", "big");
				obj.put("cat", "small");
				target.appendRemoteFunctionCall("testFuncFunc", "myJson", obj);
			}
		});
		add(new AjaxFallbackLink<Void>("c2-link")
		{
			@Override
			public void onClick(Optional<AjaxRequestTarget> targetOptional)
			{
				counter2++;
				targetOptional.ifPresent(target -> {
					target.add(c2);
					target.prependRemoteFunctionCall("testFunc2Pre", "1", "ABC");
					target.appendRemoteFunctionCall("testFunc2", "1", Math.random());
				});
			}
		});
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);
		response.render(OnDomReadyHeaderItem.forScript(
				"otherGlobalFunction = function(fn, args) {" +
						"console.log(fn, args);" +
						"var cnt = document.getElementById('resultsContainer');" +
						"cnt.innerHTML = fn + ':' + JSON.stringify(args) + \"\\n\" + cnt.innerHTML;" +
				"};" +
				"Wicket.Ajax.RMF.testFunc1 = function(){otherGlobalFunction('testFunc1', arguments)};" +
				"Wicket.Ajax.RMF.testFuncFunc = function(){otherGlobalFunction('testFuncFunc', arguments)};" +
				"Wicket.Ajax.RMF.testFunc1Pre = function(){otherGlobalFunction('testFunc1Pre', arguments)};" +
				"Wicket.Ajax.RMF.testFunc2 = function(){otherGlobalFunction('testFunc2', arguments)};" +
				"Wicket.Ajax.RMF.testFunc2Pre = function(){otherGlobalFunction('testFunc2Pre', arguments)};" +
				"eval('alert(\"this popup should not be displayed\")');"
		));
	}

	@Override
	protected void setHeaders(WebResponse response)
	{
		super.setHeaders(response);
		// There is a variety of CSP configurations, this is a very simple one
		response.setHeader(
				"Content-Security-Policy",
				// No unsafe eval in this policy
				"script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline';"
		);
	}
}
