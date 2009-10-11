package org.apache.wicket.ng.markup.html.link;

import org.apache.wicket.ng.Component;
import org.apache.wicket.ng.request.RequestHandler;
import org.apache.wicket.ng.request.cycle.RequestCycle;
import org.apache.wicket.ng.request.handler.PageAndComponentProvider;
import org.apache.wicket.ng.request.handler.impl.BookmarkableListenerInterfaceRequestHandler;
import org.apache.wicket.ng.request.handler.impl.ListenerInterfaceRequestHandler;
import org.apache.wicket.ng.request.response.Response;
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

	@Override
	public void renderComponent()
	{
		Response response = RequestCycle.get().getResponse();
		response.write("<p><a href=\"" + Strings.escapeMarkup(getURL()) + "\">" + Strings.escapeMarkup(getLabel())
				+ "</a></p>");
	}

	private String getURL()
	{
		RequestHandler handler;
		PageAndComponentProvider provider = new PageAndComponentProvider(getPage(), this);
		if (isBookmarkable())
		{
			handler = new BookmarkableListenerInterfaceRequestHandler(provider, ILinkListener.INTERFACE);
		}
		else
		{
			handler = new ListenerInterfaceRequestHandler(provider, ILinkListener.INTERFACE);
		}
		return RequestCycle.get().renderUrlFor(handler);
	}
}
