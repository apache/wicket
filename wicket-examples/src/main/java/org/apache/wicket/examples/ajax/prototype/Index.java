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
package org.apache.wicket.examples.ajax.prototype;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ILinkListener;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.request.target.component.ComponentRequestTarget;
import org.apache.wicket.util.string.AppendingStringBuffer;


/**
 * Example displaying partial page rendering using the counting link example and prototype.js.
 * Prototype.js is a javascript library that provides several handy JavaScript functions, amongst
 * others an Ajax.Updater function, which updates the HTML document with the response of the Ajax
 * call.
 * 
 * @author ivaynberg
 */
public class Index extends WicketExamplePage
{
	/** Click count. */
	private int count = 0;

	/** Label showing count */
	private final Label counter;

	/**
	 * Constructor.
	 */
	public Index()
	{
		// Add the Ajaxian link to the page...
		add(new Link<Void>("link")
		{
			/**
			 * Handles a click on the link. This method is accessed normally using a standard http
			 * request, but in this example, we use Ajax to perform the call.
			 */
			@Override
			public void onClick()
			{
				// Increment count
				count++;

				// The response should refresh the label displaying the counter.
				getRequestCycle().setRequestTarget(new ComponentRequestTarget(counter)
				{
					@Override
					public void respond(RequestCycle requestCycle)
					{
						super.respond(requestCycle);
						WebResponse response = (WebResponse)requestCycle.getResponse();
						response.setHeader("Pragma", "no-cache");
						response.setHeader("Cache-Control",
							"no-cache, no-store, max-age=0, must-revalidate");
					}
				});
			}

			/**
			 * Alter the javascript 'onclick' event to emit the Ajax call and update the counter
			 * label.
			 */
			@Override
			protected String getOnClickScript(String url)
			{
				return new AppendingStringBuffer("new Ajax.Updater('counter', '").append(
					urlFor(ILinkListener.INTERFACE))
					.append("', {method:'get'}); return false;")
					.toString();
			}
		});

		// Add the label
		add(counter = new Label("counter", new PropertyModel<Integer>(this, "count")));
	}

	/**
	 * @return Returns the count.
	 */
	public int getCount()
	{
		return count;
	}
}