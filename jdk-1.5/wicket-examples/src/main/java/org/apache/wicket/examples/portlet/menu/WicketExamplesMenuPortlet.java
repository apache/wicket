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
package org.apache.wicket.examples.portlet.menu;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletSession;

import org.apache.wicket.markup.parser.XmlPullParser;
import org.apache.wicket.markup.parser.XmlTag;
import org.apache.wicket.protocol.http.portlet.WicketPortlet;

/**
 * @author Ate Douma
 */
public class WicketExamplesMenuPortlet extends WicketPortlet
{
	public static final String EXAMPLE_APPLICATION_PREF = "exampleApplication";
	public static final String EXAMPLES = WicketExamplesMenuPortlet.class.getName() + ".examples";
	public static final String EXAMPLE_APPLICATION_ATTR = WicketExamplesMenuPortlet.class.getName() + "." + EXAMPLE_APPLICATION_PREF;
	private static final String MENU_APPLICATION_URL_PORTLET_PARAMETER = "_wmu";
	private static final String PROCESS_MENU_APPLICATION = WicketExamplesMenuPortlet.class.getName() + ".processMenuApplication";
	private static final String PROCESS_HEADER_PAGE = WicketExamplesMenuPortlet.class.getName() + ".renderHeaderPage";
    /**
     * Name of portlet init Parameter for the ExampleHeader page
     */
    public static final String PARAM_HEADER_PAGE = "headerPage";
	
	private static List examples;
	
	/**
	 * @see org.apache.wicket.protocol.http.portlet.WicketPortlet#init(javax.portlet.PortletConfig)
	 */
	@Override
	public void init(PortletConfig config) throws PortletException
	{
		super.init(config);
		if (examples == null)
		{
			examples = discoverExamples(config.getPortletContext());
			if (examples == null)
			{
				examples = Collections.EMPTY_LIST;
			}
			else
			{
				examples = Collections.unmodifiableList(examples);
			}
			config.getPortletContext().setAttribute(EXAMPLES, examples);
		}
	}

	/**
	 * @see org.apache.wicket.protocol.http.portlet.WicketPortlet#getWicketConfigParameter(javax.portlet.PortletRequest, java.lang.String, java.lang.String)
	 */
	@Override
	protected String getWicketConfigParameter(PortletRequest request, String paramName, String defaultValue)
	{
		if (paramName.equals(WICKET_FILTER_PATH))
		{
			return ((ExampleApplication)request.getAttribute(EXAMPLE_APPLICATION_ATTR)).getFilterPath();
		}
		else if (paramName.equals(WICKET_FILTER_QUERY))
		{
			return ((ExampleApplication)request.getAttribute(EXAMPLE_APPLICATION_ATTR)).getFilterQuery();
		}
		return super.getWicketConfigParameter(request, paramName, defaultValue);
	}

	/**
	 * @see org.apache.wicket.protocol.http.portlet.WicketPortlet#getWicketUrlPortletParameter(javax.portlet.PortletRequest)
	 */
	@Override
	protected String getWicketUrlPortletParameter(PortletRequest request)
	{
		return request.getAttribute(PROCESS_MENU_APPLICATION) != null ? MENU_APPLICATION_URL_PORTLET_PARAMETER : super.getWicketUrlPortletParameter(request);
	}
	
	/**
	 * @see org.apache.wicket.protocol.http.portlet.WicketPortlet#getWicketURL(javax.portlet.PortletRequest, java.lang.String, java.lang.String)
	 */
	@Override
	protected String getWicketURL(PortletRequest request, String pageType, String defaultPage)
	{		
		ExampleApplication ea = (ExampleApplication)request.getAttribute(EXAMPLE_APPLICATION_ATTR);
		if (request.getAttribute(PROCESS_HEADER_PAGE) != null)
		{
			return ea.getInitParameter(PARAM_HEADER_PAGE);				
		}
		return super.getWicketURL(request, pageType, ea.getInitParameter(pageType));
	}

	/**
	 * @see org.apache.wicket.protocol.http.portlet.WicketPortlet#processRequest(javax.portlet.PortletRequest, javax.portlet.PortletResponse, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	protected void processRequest(PortletRequest request, PortletResponse response, String requestType, String pageType) throws PortletException, IOException
	{
		PortletSession session = request.getPortletSession();
		ExampleApplication ea = (ExampleApplication)session.getAttribute(EXAMPLE_APPLICATION_ATTR);
		if (ea == null)
		{
			String eaFilterPath = request.getPreferences().getValue(EXAMPLE_APPLICATION_PREF, null);
			if (eaFilterPath != null)
			{
				Iterator iter = examples.iterator();
				while (iter.hasNext())
				{
					ea = (ExampleApplication)iter.next();
					if (ea.getFilterPath().equals(eaFilterPath))
					{
						break;
					}
					ea = null;
				}
			}
			if (ea == null && examples.size() > 0)
			{
				ea = (ExampleApplication)examples.get(0);
			}
			session.setAttribute(EXAMPLE_APPLICATION_ATTR, ea);
		}
		if (ea == null || ea.getFilterPath().equals(getWicketFilterPath()) || !PortletMode.VIEW.equals(request.getPortletMode()))
		{
			request.setAttribute(PROCESS_MENU_APPLICATION, Boolean.TRUE);
			request.setAttribute(EXAMPLE_APPLICATION_ATTR, examples.get(0));
			super.processRequest(request, response, requestType, pageType);
		}
		else
		{
			if (WicketPortlet.ACTION_REQUEST.equals(requestType) || request.getParameter(PORTLET_RESOURCE_URL_PARAMETER) != null)
			{
				if (request.getParameter(MENU_APPLICATION_URL_PORTLET_PARAMETER) != null)
				{
					request.setAttribute(PROCESS_MENU_APPLICATION, Boolean.TRUE);
					request.setAttribute(EXAMPLE_APPLICATION_ATTR, examples.get(0));
					super.processRequest(request, response, requestType, pageType);
				}
				else
				{
					request.setAttribute(EXAMPLE_APPLICATION_ATTR, ea);
					super.processRequest(request, response, requestType, pageType);
				}
			}
			else
			{
				request.setAttribute(PROCESS_MENU_APPLICATION, Boolean.TRUE);
				request.setAttribute(PROCESS_HEADER_PAGE, Boolean.TRUE);
				request.setAttribute(EXAMPLE_APPLICATION_ATTR, examples.get(0));
				super.processRequest(request, response, requestType, pageType);
				request.removeAttribute(PROCESS_MENU_APPLICATION);
				request.removeAttribute(PROCESS_HEADER_PAGE);
				request.setAttribute(EXAMPLE_APPLICATION_ATTR, ea);
				super.processRequest(request, response, requestType, pageType);
			}
		}
	}
	
	protected List discoverExamples(PortletContext portletContext)
	{		
		ArrayList examples = new ArrayList();
		InputStream is = portletContext.getResourceAsStream("/WEB-INF/portlet.xml");
		if (is != null)
		{
			try
			{
				XmlPullParser parser = new XmlPullParser();
				parser.parse(is);
				while (true)
				{
					XmlTag elem;
					String name;
					int level;
					
					do
					{
						elem = (XmlTag)parser.nextTag();
					}
					while (elem != null && (!(elem.getName().equals("portlet") && elem.isOpen())));

					if (elem == null)
					{
						break;
					}

					String description = null;
					String filterPath = null;
					String filterQuery = null;
					String displayName = null;
					HashMap initParameters = new HashMap();
					
					level = 0;
					
					do
					{
						elem = (XmlTag)parser.nextTag();
						name = elem.getName();
						level = elem.isOpen() ? (level+1) : (level-1);
						
						if (level == 1)
						{
							if (name.equals("description")||name.equals("display-name"))
							{
								parser.setPositionMarker();
							}
							
							else if (name.equals("init-param"))
							{
								String initParamName = null;
								String initParamValue = null;
								do
								{
									elem = (XmlTag)parser.nextTag();
									name = elem.getName();
									level = elem.isOpen() ? (level+1) : (level-1);
									if (level == 2)
									{
										if (name.equals("name")||name.equals("value"))
										{
											parser.setPositionMarker();
										}
									}
									else if (level == 1)
									{
										if (name.equals("name"))
										{
											initParamName = parser.getInputFromPositionMarker(elem.getPos()).toString();
										}
										else if (name.equals("value"))
										{
											initParamValue = parser.getInputFromPositionMarker(elem.getPos()).toString();
										}
									}
								}
								while (level > 0);
								if (initParamName != null && initParamValue != null)
								{
									initParameters.put(initParamName, initParamValue);
								}
							}
						}
						else if (level == 0)
						{							
							if (name.equals("description"))
							{
								description = parser.getInputFromPositionMarker(elem.getPos()).toString();
							}
							else if (name.equals("display-name"))
							{
								displayName = parser.getInputFromPositionMarker(elem.getPos()).toString();
							}
						}
					}
					while (level > -1);
					filterPath = buildWicketFilterPath((String)initParameters.get(WICKET_FILTER_PATH_PARAM));					
					if (displayName != null && filterPath != null && description != null)
					{
						filterQuery = buildWicketFilterQuery(filterPath);
						validateDefaultPages(initParameters, filterPath, filterQuery);
						ExampleApplication exampleApplication = new ExampleApplication(displayName, filterPath, filterQuery, initParameters, description);
						if (exampleApplication.getFilterPath().equals(getWicketFilterPath()))
						{
							examples.add(0, exampleApplication);
						}
						else
						{
							examples.add(exampleApplication);
						}
					}
				}
			}
			catch (RuntimeException e)
			{
				throw e;
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}
		return examples;
	}
}
