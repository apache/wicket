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

import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ILinkListener;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.core.request.handler.ComponentRenderingRequestHandler;
import org.apache.wicket.core.request.handler.ListenerInterfaceRequestHandler;
import org.apache.wicket.core.request.handler.PageAndComponentProvider;
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
	private static final long serialVersionUID = 1L;

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
			private static final long serialVersionUID = 1L;

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
				getRequestCycle().replaceAllRequestHandlers(
					new ComponentRenderingRequestHandler(counter));
			}

			/**
			 * Alter the javascript 'onclick' event to emit the Ajax call and update the counter
			 * label.
			 */
			@Override
			protected String getOnClickScript(CharSequence url)
			{
				IRequestHandler handler = new ListenerInterfaceRequestHandler(
					new PageAndComponentProvider(getPage(), this), ILinkListener.INTERFACE);
				Url componentUrl = RequestCycle.get().mapUrlFor(handler);
				componentUrl.addQueryParameter("anticache", Math.random());
				return new AppendingStringBuffer("new Ajax.Updater('counter', '").append(
					componentUrl)
					.append("', {method:'get'}); return false;")
					.toString();
			}
		});

		// Add the label
		add(counter = new Label("counter", new PropertyModel<>(this, "count")));
	}

	/**
	 * @return Returns the count.
	 */
	public int getCount()
	{
		return count;
	}
}
