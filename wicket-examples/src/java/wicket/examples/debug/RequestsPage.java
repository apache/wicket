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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
import wicket.util.convert.IConverter;
import wicket.util.convert.converters.DateConverter;
import wicket.util.lang.Bytes;

/**
 * @author jcompagner
 */
public class RequestsPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param requestData
	 */
	public RequestsPage(final RequestData requestData)
	{
		new Image(this, "bug");

		if(requestData == null)
		{
			new Label(this,"id").setVisible(false);
			new Label(this,"size").setVisible(false);		
			new WebMarkupContainer(this,"sessionid");
		}
		else
		{
			new Label(this,"id", requestData.getSessionId());
			new Label(this,"size",  new Model<Bytes>(Bytes.bytes(requestData.getSessionSize())));		
			new WebMarkupContainer(this,"sessionid").setVisible(false);
		}
		IModel<List<RequestData>> requestsModel = new Model<List<RequestData>>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public List<RequestData> getObject()
			{
				List<RequestData> requests = getRequestLogger().getRequests();
				if(requestData != null)
				{
					List<RequestData>  returnValues = new ArrayList<RequestData> (); 
					for (RequestData data : requests)
					{
						if(requestData.getSessionId().equals(data.getSessionId()))
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

			private final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM hh:mm:ss.SSS");

			@Override
			protected void populateItem(ListItem item)
			{
				RequestData rd = (RequestData)item.getModelObject();
				new Label(item, "id", new Model<String>(rd.getSessionId())).setVisible(requestData == null);
				new Label(item, "startDate", new Model<Date>(rd.getStartDate()))
				{
					private static final long serialVersionUID = 1L;

					/**
					 * @see wicket.Component#getConverter(Class)
					 */
					@Override
					public IConverter getConverter(Class type)
					{

						return new DateConverter()
						{
							private static final long serialVersionUID = 1L;

							/**
							 * @see wicket.util.convert.converters.DateConverter#getDateFormat(java.util.Locale)
							 */
							@Override
							public DateFormat getDateFormat(Locale locale)
							{
								return sdf;
							}
						};
					}
				};
				new Label(item, "timeTaken", new Model<Long>(rd.getTimeTaken()));
				new Label(item, "eventTarget", new Model<String>(rd.getEventTargert()));
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
