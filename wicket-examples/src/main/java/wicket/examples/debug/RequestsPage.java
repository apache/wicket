/*
 * $Id$
 * $Revision$
 * $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.examples.debug;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import wicket.Application;
import wicket.Component;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.WebPage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.image.Image;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.PageableListView;
import wicket.markup.html.navigation.paging.PagingNavigator;
import wicket.model.IModel;
import wicket.model.Model;
import wicket.protocol.http.IRequestLogger;
import wicket.protocol.http.RequestLogger;
import wicket.protocol.http.WebApplication;
import wicket.protocol.http.RequestLogger.RequestData;
import wicket.protocol.http.RequestLogger.SessionData;
import wicket.util.lang.Bytes;

/**
 * @author jcompagner
 */
public class RequestsPage extends WebPage
{
	private static final long serialVersionUID = 1L;
	private final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM hh:mm:ss.SSS");
	/**
	 * Construct.
	 * @param sessionData
	 */
	public RequestsPage(final SessionData sessionData)
	{
		add(new Image("bug"));
		if(sessionData == null)
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
			add(new Label("id", new Model(sessionData.getSessionId())));
			add(new Label("sessionInfo",new Model((Serializable)sessionData.getSessionInfo())));
			add(new Label("startDate",new Model(sdf.format(sessionData.getStartDate()))));
			add(new Label("lastRequestTime",new Model(sdf.format(sessionData.getLastActive()))));
			add(new Label("numberOfRequests",new Model(new Long(sessionData.getNumberOfRequests()))));
			add(new Label("totalTimeTaken",new Model(new Long(sessionData.getTotalTimeTaken()))));
			add(new Label("size",  new Model(Bytes.bytes(sessionData.getSessionSize()))));		
			add(new WebMarkupContainer("sessionid").setVisible(false));
		}		
		
		IModel requestsModel = new Model()
		{
			private static final long serialVersionUID = 1L;

			public Object getObject(Component component)
			{
				List requests = getRequestLogger().getRequests();
				if(sessionData != null)
				{
					List  returnValues = new ArrayList (); 
					for (int i=0;i<requests.size();i++)
					{
						RequestData data = (RequestData)requests.get(i);
						if(sessionData.getSessionId().equals(data.getSessionId()))
						{
							returnValues.add(data);
						}
					}
					return returnValues;
				}
				return requests;
			}
		};
		PageableListView listView = new PageableListView("requests",requestsModel,50)
		{
			private static final long serialVersionUID = 1L;
			
			protected void populateItem(ListItem item) 
			{
				RequestData rd = (RequestData)item.getModelObject();
				item.add(new Label("id", new Model(rd.getSessionId())).setVisible(sessionData == null));
				item.add(new Label("startDate", new Model(sdf.format(rd.getStartDate()))));
				item.add(new Label("timeTaken", new Model(rd.getTimeTaken())));
				item.add(new Label("eventTarget", new Model(rd.getEventTarget())));
				item.add(new Label("responseTarget", new Model(rd.getResponseTarget())));
				item.add(new Label("alteredObjects", new Model(rd.getAlteredObjects())))
						.setEscapeModelStrings(false);
				item.add(new Label("sessionSize", new Model(Bytes.bytes(rd.getSessionSize().longValue()))));
			}
		};
		add(listView);
		
		PagingNavigator navigator = new PagingNavigator("navigator",listView);
		add(navigator);
	}
	
	IRequestLogger getRequestLogger()
	{
		WebApplication webApplication = (WebApplication)Application.get();
		final IRequestLogger requestLogger;
		if (webApplication.getRequestLogger() == null)
		{
			// make default one.
			requestLogger = new RequestLogger();
		}
		else
		{
			requestLogger = webApplication.getRequestLogger();
		}
		return requestLogger;
	}
}
