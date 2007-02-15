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

import javax.servlet.http.HttpServletResponse;

import wicket.AccessStackPageMap;
import wicket.Application;
import wicket.Component;
import wicket.IPageFactory;
import wicket.IPageMap;
import wicket.IRedirectListener;
import wicket.IRequestTarget;
import wicket.Page;
import wicket.PageParameters;
import wicket.RequestCycle;
import wicket.RequestListenerInterface;
import wicket.RestartResponseAtInterceptPageException;
import wicket.Session;
import wicket.WicketRuntimeException;
import wicket.AccessStackPageMap.Access;
import wicket.authorization.AuthorizationException;
import wicket.authorization.UnauthorizedActionException;
import wicket.markup.MarkupException;
import wicket.protocol.http.portlet.pages.ExceptionErrorPortletPage;
import wicket.protocol.http.request.WebErrorCodeResponseTarget;
import wicket.request.AbstractRequestCycleProcessor;
import wicket.request.IRequestCodingStrategy;
import wicket.request.RequestParameters;
import wicket.request.target.basic.EmptyRequestTarget;
import wicket.request.target.component.BookmarkablePageRequestTarget;
import wicket.request.target.component.ExpiredPageClassRequestTarget;
import wicket.request.target.component.IPageRequestTarget;
import wicket.request.target.component.PageRequestTarget;
import wicket.request.target.component.listener.RedirectPageRequestTarget;
import wicket.request.target.resource.SharedResourceRequestTarget;
import wicket.settings.IExceptionSettings;
import wicket.util.string.Strings;

/**
 * A RequestCycleProcessor for portlet action requests. The page is not rendered
 * in the action phase. Render state is stored as portlet render parameters in
 * the PortletActionRequestResponseStrategy.
 * 
 * @see PortletActionRequestResponseStrategy
 * @see PortletRequestCycle
 * 
 * @author Janne Hietam&auml;ki
 */
public class PortletActionRequestCycleProcessor extends AbstractPortletRequestCycleProcessor
{
	/**
	 * Construct.
	 */
	public PortletActionRequestCycleProcessor()
	{
	}

	/**
	 * @see wicket.request.AbstractRequestCycleProcessor#respond(wicket.RequestCycle)
	 */
	public void respond(RequestCycle requestCycle)
	{
		PortletRequestCodingStrategy strategy = (PortletRequestCodingStrategy)requestCycle
				.getProcessor().getRequestCodingStrategy();
		strategy.setRenderParameters((PortletRequestCycle)requestCycle, requestCycle
				.getRequestTarget());
	}
}
