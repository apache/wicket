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
package org.apache.wicket.ng.markup.html.link;


import org.apache.wicket.Component;
import org.apache.wicket.Response;
import org.apache.wicket.ng.request.IRequestHandler;
import org.apache.wicket.ng.request.cycle.RequestCycle;
import org.apache.wicket.ng.request.handler.PageAndComponentProvider;
import org.apache.wicket.ng.request.handler.impl.BookmarkableListenerInterfaceRequestHandler;
import org.apache.wicket.ng.request.handler.impl.ListenerInterfaceRequestHandler;
import org.apache.wicket.util.string.Strings;

@SuppressWarnings("serial")
// Very simple and naive link component
public abstract class Link extends Component implements ILinkListener
{
	public Link(String id)
	{
		super(id);
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	public String getLabel()
	{
		return label;
	}

	private String label;

	private boolean bookmarkable;

	public void setBookmarkable(boolean bookmarkable)
	{
		this.bookmarkable = bookmarkable;
	}

	public boolean isBookmarkable()
	{
		return bookmarkable;
	}

	public void renderComponent2()
	{
		Response response = RequestCycle.get().getResponse();
		response.write("<p><a href=\"" + Strings.escapeMarkup(getURL()) + "\">" +
			Strings.escapeMarkup(getLabel()) + "</a></p>");
	}

	private String getURL()
	{
		IRequestHandler handler;
		PageAndComponentProvider provider = new PageAndComponentProvider(getPage(), this);
		if (isBookmarkable())
		{
			handler = new BookmarkableListenerInterfaceRequestHandler(provider,
				ILinkListener.INTERFACE);
		}
		else
		{
			handler = new ListenerInterfaceRequestHandler(provider, ILinkListener.INTERFACE);
		}
		return RequestCycle.get().renderUrlFor(handler);
	}
}
