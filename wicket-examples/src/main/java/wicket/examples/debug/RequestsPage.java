/*
 * $Id$ $Revision$ $Date$
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
 * $Id$ $Revision$ $Date$
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import wicket.Application;
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
	 * 
	 * @param sessionData
	 */
	public RequestsPage(final SessionData sessionData)
	{
		new Image(this, "bug");

		if(sessionData == null)
		{
			new Label(this,"id").setVisible(false);
			new Label(this,"sessionInfo").setVisible(false);		
			new Label(this,"startDate").setVisible(false);
			new Label(this,"lastRequestTime").setVisible(false);		
			new Label(this,"numberOfRequests").setVisible(false);
			new Label(this,"totalTimeTaken").setVisible(false);
			new Label(this,"size").setVisible(false);		
			new WebMarkupContainer(this,"sessionid");
		}
		else
		{
			new Label(this,"id", new Model<String>(sessionData.getSessionId()));
			new Label(this,"sessionInfo",new Model<Object>(sessionData.getSessionInfo()));
			new Label(this,"startDate",new Model<String>(sdf.format(sessionData.getStartDate())));
			new Label(this,"lastRequestTime",new Model<String>(sdf.format(sessionData.getLastActive())));
			new Label(this,"numberOfRequests",new Model<Long>(sessionData.getNumberOfRequests()));
			new Label(this,"totalTimeTaken",new Model<Long>(sessionData.getTotalTimeTaken()));
			new Label(this,"size",  new Model<Bytes>(Bytes.bytes(sessionData.getSessionSize())));		
			new WebMarkupContainer(this,"sessionid").setVisible(false);
		}
		IModel<List<RequestData>> requestsModel = new Model<List<RequestData>>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public List<RequestData> getObject()
			{
				List<RequestData> requests = getRequestLogger().getRequests();
				if(sessionData != null)
				{
					List<RequestData>  returnValues = new ArrayList<RequestData> (); 
					for (RequestData data : requests)
					{
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
		PageableListView<RequestData> listView = new PageableListView<RequestData>(this, "requests", requestsModel, 50)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem item)
			{
				RequestData rd = (RequestData)item.getModelObject();
				new Label(item, "id", new Model<String>(rd.getSessionId())).setVisible(sessionData == null);
				new Label(item, "startDate", new Model<String>(sdf.format(rd.getStartDate())));
				new Label(item, "timeTaken", new Model<Long>(rd.getTimeTaken()));
				new Label(item, "eventTarget", new Model<String>(rd.getEventTarget()));
				new Label(item, "responseTarget", new Model<String>(rd.getResponseTarget()));
				new Label(item, "alteredObjects", new Model<String>(rd.getAlteredObjects()))
						.setEscapeModelStrings(false);
				new Label(item, "sessionSize", new Model<Bytes>(Bytes.bytes(rd.getSessionSize())));
			}
		};

		new PagingNavigator(this, "navigator", listView);
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
