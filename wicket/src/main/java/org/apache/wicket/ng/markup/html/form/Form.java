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
package org.apache.wicket.ng.markup.html.form;

import org.apache.wicket.Component;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.ng.request.RequestHandler;
import org.apache.wicket.ng.request.cycle.RequestCycle;
import org.apache.wicket.ng.request.handler.PageAndComponentProvider;
import org.apache.wicket.ng.request.handler.impl.BookmarkableListenerInterfaceRequestHandler;
import org.apache.wicket.ng.request.handler.impl.ListenerInterfaceRequestHandler;

public class Form extends Component implements IFormSubmitListener
{
	private static final long serialVersionUID = 1L;

	public Form(String id)
	{
		super(id);
	}

	public void onFormSubmitted()
	{
		Request r = RequestCycle.get().getRequest();
		System.out.println(r.getRequestParameters().getParameterValue("key1"));
	}


	public void renderComponent2()
	{
		Response response = RequestCycle.get().getResponse();

		response.write("<form action=\"" + getURL() + "\" method=\"post\">\n");
		response.write("<input type=\"hidden\" name=\"key1\" value=\"value1\">\n");
		response.write("<input type=\"hidden\" name=\"key2\" value=\"value2\">\n");
		response.write("<input type=\"submit\" value=\"Submit\">");
		response.write("</form>");
	}

	private boolean bookmarkable;

	public void setBookmarkable(boolean bookmarkable)
	{
		this.bookmarkable = bookmarkable;
	}

	public boolean isBookmarkable()
	{
		return bookmarkable;
	}

	private String getURL()
	{
		RequestHandler handler;
		PageAndComponentProvider provider = new PageAndComponentProvider(getPage(), this);
		if (isBookmarkable())
		{
			handler = new BookmarkableListenerInterfaceRequestHandler(provider,
				IFormSubmitListener.INTERFACE);
		}
		else
		{
			handler = new ListenerInterfaceRequestHandler(provider, IFormSubmitListener.INTERFACE);
		}
		return RequestCycle.get().renderUrlFor(handler);
	}

	@Override
	protected void onRender()
	{
	}
}
