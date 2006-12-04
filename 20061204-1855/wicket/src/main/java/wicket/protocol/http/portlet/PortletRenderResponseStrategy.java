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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Application;
import wicket.Component;
import wicket.IRequestTarget;
import wicket.Page;
import wicket.RequestCycle;
import wicket.Response;
import wicket.markup.html.internal.HtmlHeaderContainer;
import wicket.markup.parser.filter.HtmlHeaderSectionHandler;
import wicket.request.compound.IResponseStrategy;
import wicket.request.target.component.PageRequestTarget;

/**
 * Portlet render ResponseStrategy handles outputting of the header contributions 
 * into the response. 
 * 
 * Portlets do not support real header contribution. 
 * 
 * @author Janne Hietam&auml;ki
 */
public class PortletRenderResponseStrategy implements IResponseStrategy
{
	/** Log */
	private static final Log log = LogFactory.getLog(PortletRenderResponseStrategy.class);


	public void respond(RequestCycle requestCycle)
	{
		IRequestTarget requestTarget = requestCycle.getRequestTarget();
		if (requestTarget != null)
		{
			Application.get().logResponseTarget(requestTarget);
			respondHeaderContribution(requestCycle,requestTarget);
			requestTarget.respond(requestCycle);
		}
	}

	private void respondHeaderContribution(final RequestCycle requestCycle,final IRequestTarget requestTarget)
	{
		if(requestTarget instanceof PageRequestTarget)
		{
			final PageRequestTarget target=(PageRequestTarget)requestTarget;
			final Response response = RequestCycle.get().getResponse();
			final Page page=target.getPage();

			final HtmlHeaderContainer header = new HtmlHeaderContainer(
					HtmlHeaderSectionHandler.HEADER_ID);

			if(page.get(HtmlHeaderSectionHandler.HEADER_ID) != null)
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
			page.remove(header);
		}
	}
}