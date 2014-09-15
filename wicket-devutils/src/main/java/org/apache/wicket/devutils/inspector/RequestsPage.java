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
package org.apache.wicket.devutils.inspector;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.devutils.DevUtilsPage;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.IRequestLogger;
import org.apache.wicket.protocol.http.IRequestLogger.RequestData;
import org.apache.wicket.protocol.http.IRequestLogger.SessionData;
import org.apache.wicket.protocol.http.RequestLogger;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.lang.Bytes;

/**
 * @author jcompagner
 */
public class RequestsPage extends DevUtilsPage
{
	private static final long serialVersionUID = 1L;
	private final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM hh:mm:ss.SSS");

	/**
	 * Construct.
	 * 
	 * @param sessionData
	 */
	public RequestsPage(final SessionData sessionData)
	{
		if (sessionData == null)
		{
			add(new Label("id").setVisible(false));
			add(new Label("sessionInfo").setVisible(false));
			add(new Label("startDate").setVisible(false));
			add(new Label("lastRequestTime").setVisible(false));
			add(new Label("numberOfRequests").setVisible(false));
			add(new Label("totalTimeTaken").setVisible(false));
			add(new Label("size").setVisible(false));
			add(new WebMarkupContainer("sessionid"));
		}
		else
		{
			add(new Label("id", new Model<>(sessionData.getSessionId())));
			add(new Label("sessionInfo", new Model<>(
				(Serializable)sessionData.getSessionInfo())));
			add(new Label("startDate", new Model<>(sdf.format(sessionData.getStartDate()))));
			add(new Label("lastRequestTime", new Model<>(
				sdf.format(sessionData.getLastActive()))));
			add(new Label("numberOfRequests", new Model<>(sessionData.getNumberOfRequests())));
			add(new Label("totalTimeTaken", new Model<>(sessionData.getTotalTimeTaken())));
			add(new Label("size", new Model<>(Bytes.bytes(sessionData.getSessionSize()))));
			add(new WebMarkupContainer("sessionid").setVisible(false));
		}

		IModel<List<RequestData>> requestsModel = new AbstractReadOnlyModel<List<RequestData>>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public ArrayList<RequestData> getObject()
			{
				List<RequestData> requests = getRequestLogger().getRequests();
				if (sessionData != null)
				{
					ArrayList<RequestData> returnValues = new ArrayList<>();
					for (RequestData data : requests)
					{
						if (sessionData.getSessionId().equals(data.getSessionId()))
						{
							returnValues.add(data);
						}
					}
					return returnValues;
				}
				return new ArrayList<>(requests);
			}
		};
		PageableListView<RequestData> listView = new PageableListView<RequestData>("requests",
			requestsModel, 50)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(final ListItem<RequestData> item)
			{
				RequestData rd = item.getModelObject();
				item.add(new Label("id", new Model<>(rd.getSessionId())).setVisible(sessionData == null));
				item.add(new Label("startDate", new Model<>(sdf.format(rd.getStartDate()))));
				item.add(new Label("timeTaken", new Model<>(rd.getTimeTaken())));
				String eventTarget = rd.getEventTarget() != null ? rd.getEventTarget()
					.getClass()
					.getName() : "";
				item.add(new Label("eventTarget", new Model<>(eventTarget)));
				String responseTarget = rd.getResponseTarget() != null ? rd.getResponseTarget()
					.getClass()
					.getName() : "";
				item.add(new Label("responseTarget", new Model<>(responseTarget)));
				item.add(new Label("alteredObjects", new Model<>(rd.getAlteredObjects())).setEscapeModelStrings(false));
				item.add(new Label("sessionSize", new Model<Bytes>(Bytes.bytes(rd.getSessionSize()
					.longValue()))));
			}
		};
		add(listView);

		PagingNavigator navigator = new PagingNavigator("navigator", listView);
		add(navigator);
	}

	IRequestLogger getRequestLogger()
	{
		WebApplication webApplication = (WebApplication)Application.get();

		IRequestLogger requestLogger = webApplication.getRequestLogger();

		if (webApplication.getRequestLogger() == null)
			requestLogger = new RequestLogger();
		return requestLogger;
	}
}
