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
package wicket.protocol.http.portlet;

import wicket.Application;
import wicket.Component;
import wicket.IRequestTarget;
import wicket.Page;
import wicket.RequestCycle;
import wicket.Response;
import wicket.Component.IVisitor;
import wicket.feedback.IFeedback;
import wicket.markup.html.internal.HtmlHeaderContainer;
import wicket.markup.parser.filter.HtmlHeaderSectionHandler;
import wicket.request.target.component.PageRequestTarget;

/**
 * A RequestCycleProcessor for portlet render requests. The events are not
 * processed in the render phase.
 * 
 * @see PortletRequestCycle
 * 
 * @author Janne Hietam&auml;ki
 * 
 */
public class PortletRenderRequestCycleProcessor extends AbstractPortletRequestCycleProcessor
{
	/**
	 * Construct.
	 */
	public PortletRenderRequestCycleProcessor()
	{
	}

	/**
	 * Process only PortletMode and WindowState changes in the RenderRequests.
	 * 
	 * @see wicket.request.AbstractRequestCycleProcessor#processEvents(wicket.RequestCycle)
	 */
	public void processEvents(final RequestCycle requestCycle)
	{
		PortletPage page = (PortletPage)requestCycle.getRequest().getPage();
		if (page != null)
		{
			PortletRequestCycle cycle = (PortletRequestCycle)requestCycle;
			page.setPortletMode(cycle.getPortletRequest().getPortletRequest().getPortletMode());
			page.setWindowState(cycle.getPortletRequest().getPortletRequest().getWindowState());
		}
	}

	/**
	 * @see wicket.request.AbstractRequestCycleProcessor#respond(wicket.RequestCycle)
	 */
	public void respond(RequestCycle requestCycle)
	{
		IRequestTarget requestTarget = requestCycle.getRequestTarget();
		if (requestTarget != null)
		{
			Application.get().logResponseTarget(requestTarget);
			respondHeaderContribution(requestCycle, requestTarget);
			requestTarget.respond(requestCycle);
		}
	}

	/**
	 * Handle header contribution.
	 * 
	 * @param requestCycle
	 *            The request cycle
	 * @param requestTarget
	 *            The request target
	 */
	private void respondHeaderContribution(final RequestCycle requestCycle,
			final IRequestTarget requestTarget)
	{

		// TODO Does this work with portlets - I thought header contributions
		// were not supported in JSR 170? And if it works, it should be
		// forwarded to 2.0 as there is no such facility for it now (Eelco)

		if (requestTarget instanceof PageRequestTarget)
		{
			final PageRequestTarget target = (PageRequestTarget)requestTarget;
			final Response response = RequestCycle.get().getResponse();
			final Page page = target.getPage();

			final HtmlHeaderContainer header = new HtmlHeaderContainer(
					HtmlHeaderSectionHandler.HEADER_ID);

			if (page.get(HtmlHeaderSectionHandler.HEADER_ID) != null)
			{
				page.replace(header);
			}
			else
			{
				page.add(header);
			}

			page.visitChildren(new Component.IVisitor()
			{
				public Object component(Component component)
				{
					if (component.isVisible())
					{
						component.renderHead(header);
						return CONTINUE_TRAVERSAL;
					}
					else
					{
						return CONTINUE_TRAVERSAL_BUT_DONT_GO_DEEPER;
					}
				}
			});


			// collect feedback, is this really necessary for a header
			// container?
			header.visitChildren(IFeedback.class, new IVisitor()
			{
				public Object component(Component component)
				{
					((IFeedback)component).updateFeedback();
					return IVisitor.CONTINUE_TRAVERSAL;
				}
			});

			if (header instanceof IFeedback)
			{
				((IFeedback)header).updateFeedback();
			}

			header.internalAttach();

			try
			{
				header.visitChildren(new Component.IVisitor()
				{
					public Object component(Component component)
					{
						page.startComponentRender(component);
						component.renderComponent();
						page.endComponentRender(component);
						return CONTINUE_TRAVERSAL;
					}
				});
			}
			finally
			{
				header.internalDetach();
			}

			page.remove(header);
		}
	}
}