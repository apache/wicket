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
import java.util.Locale;

import wicket.Component;
import wicket.markup.html.WebPage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.image.Image;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.PageableListView;
import wicket.markup.html.navigation.paging.PagingNavigator;
import wicket.model.Model;
import wicket.protocol.http.RequestLogger.RequestData;
import wicket.protocol.http.RequestLogger.SessionData;
import wicket.util.convert.IConverter;
import wicket.util.convert.converters.DateConverter;

/**
 * @author jcompagner
 */
public class RequestsPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param sessionData
	 */
	public RequestsPage(final SessionData sessionData)
	{
		new Image(this, "bug");

		new SessionView(this, "session", sessionData.getSession());

		Model requestsModel = new Model()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public Object getObject(Component component)
			{
				return new ArrayList<RequestData>(sessionData.getRequests());
			}
		};
		PageableListView listView = new PageableListView(this, "requests", requestsModel, 50)
		{
			private static final long serialVersionUID = 1L;

			private final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM hh:mm:ss.SSS");

			@Override
			protected void populateItem(ListItem item)
			{
				RequestData rd = (RequestData)item.getModelObject();
				Label startDate = new Label(item, "startDate", new Model<Date>(rd.getStartDate()))
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
			}
		};

		PagingNavigator navigator = new PagingNavigator(this, "navigator", listView);
	}
}
