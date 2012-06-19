package org.apache.wicket.ajax;

import java.io.IOException;
import java.util.Collection;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.core.request.handler.logger.PageLogData;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.protocol.ws.api.WebSocketConnection;
import org.apache.wicket.request.ILogData;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An extension of AjaxRequestTarget that also supports pushing data from the server to the
 * client.
 *
 * @since 6.0
 */
public class WebSocketRequestHandler implements AjaxRequestTarget, IWebSocketRequestHandler
{                                                  
	private static final Logger LOG = LoggerFactory.getLogger(WebSocketRequestHandler.class);

	private final Page page;

	private final WebSocketConnection connection;

	private final AbstractAjaxResponse ajaxResponse;

	private PageLogData logData;

	public WebSocketRequestHandler(final Component component, final WebSocketConnection connection)
	{
		this.page = Args.notNull(component, "component").getPage();
		this.connection = Args.notNull(connection, "connection");
		this.ajaxResponse = new XmlAjaxResponse(page)
		{
			@Override
			protected void fireOnAfterRespondListeners(Response response)
			{
			}

			@Override
			protected void fireOnBeforeRespondListeners()
			{
			}
		};
	}

	@Override
	public void push(String message)
	{
		if (connection.isOpen())
		{
			try
			{
				connection.sendMessage(message);
			} catch (IOException iox)
			{
				LOG.error("An error occurred while pushing text message.", iox);
			}
		}
	}

	@Override
	public void push(byte[] message, int offset, int length)
	{
		if (connection.isOpen())
		{
			try
			{
				connection.sendMessage(message, offset, length);
			} catch (IOException iox)
			{
				LOG.error("An error occurred while pushing binary message.", iox);
			}
		}
	}
	
	@Override
	public void add(Component component, String markupId)
	{
		ajaxResponse.add(component, markupId);
	}

	@Override
	public void add(Component... components)
	{
		for (final Component component : components)
		{
			Args.notNull(component, "component");

			if (component.getOutputMarkupId() == false)
			{
				throw new IllegalArgumentException(
						"cannot update component that does not have setOutputMarkupId property set to true. Component: " +
								component.toString());
			}
			add(component, component.getMarkupId());
		}
	}

	@Override
	public final void addChildren(MarkupContainer parent, Class<?> childCriteria)
	{
		Args.notNull(parent, "parent");
		Args.notNull(childCriteria, "childCriteria");

		parent.visitChildren(childCriteria, new IVisitor<Component, Void>()
		{
			@Override
			public void component(final Component component, final IVisit<Void> visit)
			{
				add(component);
				visit.dontGoDeeper();
			}
		});
	}

	@Override
	public void addListener(IListener listener)
	{
	}

	@Override
	public void appendJavaScript(CharSequence javascript)
	{
		ajaxResponse.appendJavaScript(javascript);
	}

	@Override
	public void prependJavaScript(CharSequence javascript)
	{
		ajaxResponse.prependJavaScript(javascript);
	}

	@Override
	public void registerRespondListener(ITargetRespondListener listener)
	{
	}

	@Override
	public Collection<? extends Component> getComponents()
	{
		return ajaxResponse.getComponents();
	}

	@Override
	public final void focusComponent(Component component)
	{
		if (component != null && component.getOutputMarkupId() == false)
		{
			throw new IllegalArgumentException(
					"cannot update component that does not have setOutputMarkupId property set to true. Component: " +
							component.toString());
		}
		final String id = component != null ? ("'" + component.getMarkupId() + "'") : "null";
		appendJavaScript("Wicket.Focus.setFocusOnId(" + id + ");");
	}

	@Override
	public IHeaderResponse getHeaderResponse()
	{
		return ajaxResponse.getHeaderResponse();
	}

	@Override
	public String getLastFocusedElementId()
	{
		WebRequest request = (WebRequest) page.getRequest();
		String id = request.getHeader("Wicket-FocusedElementId");
		return Strings.isEmpty(id) ? null : id;
	}

	@Override
	public Page getPage()
	{
		return page;
	}

	@Override
	public Integer getPageId()
	{
		return page.getPageId();
	}

	@Override
	public boolean isPageInstanceCreated()
	{
		return true;
	}

	@Override
	public Integer getRenderCount()
	{
		return page.getRenderCount();
	}

	@Override
	public ILogData getLogData()
	{
		return logData;
	}

	@Override
	public Class<? extends IRequestablePage> getPageClass()
	{
		return page.getPageClass();
	}

	@Override
	public PageParameters getPageParameters()
	{
		return page.getPageParameters();
	}

	@Override
	public void respond(IRequestCycle requestCycle)
	{
		ajaxResponse.writeTo(requestCycle.getResponse(), "UTF-8");
	}

	@Override
	public void detach(IRequestCycle requestCycle)
	{
		if (logData == null)
		{
			logData = new PageLogData(page);
		}
		
		ajaxResponse.detach(requestCycle);
	}
}
