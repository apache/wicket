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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import wicket.Component;
import wicket.Session;
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
import wicket.util.lang.Bytes;
import wicket.util.lang.Objects;

/**
 * @author jcompagner
 */
public class RequestsPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * @param sessionData
	 */
	public RequestsPage(final SessionData sessionData)
	{
		add(new Image("bug"));
		
		final Session session = sessionData.getSession();
		add(new Label("id", session.getId()));
		add(new Label("locale", session.getLocale().toString()));
		add(new Label("style", session.getStyle() == null ? "[None]" : session.getStyle()));
		add(new Label("size", new Model()
		{
			private static final long serialVersionUID = 1L;

			public Object getObject(Component component) 
			{
				return Bytes.bytes(Objects.sizeof(session));
			}
		}));
		
		Model requestsModel = new Model()
		{
			private static final long serialVersionUID = 1L;
			
			public Object getObject(Component component)
			{
				return new ArrayList(sessionData.getRequests());
			}
		};
		PageableListView listView = new PageableListView("requests",requestsModel,50)
		{
			private static final long serialVersionUID = 1L;
			
			private final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM hh:mm:ss.SSS");
			
			protected void populateItem(ListItem item) 
			{
				RequestData rd = (RequestData)item.getModelObject();
				Label startDate = new Label("startDate",new Model(rd.getStartDate()))
				{
					private static final long serialVersionUID = 1L;

					/**
					 * @see wicket.Component#getConverter()
					 */
					public IConverter getConverter()
					{
						final IConverter converter = super.getConverter();
						return new IConverter()
						{
							private static final long serialVersionUID = 1L;

							public Locale getLocale()
							{
								return converter.getLocale();
							}
						
							public void setLocale(Locale locale)
							{
								converter.setLocale(locale);
							}
						
							public Object convert(Object value, Class c)
							{
								if(value instanceof Date && c == String.class)
								{
									return sdf.format((Date)value);
								}
								return converter.convert(value, c);
							}
						};
					}
				};
				item.add( startDate );
				item.add( new Label("timeTaken",new Model(rd.getTimeTaken())));
				item.add( new Label("eventTarget",new Model(rd.getEventTargert())) );
				item.add( new Label("responseTarget",new Model(rd.getResponseTarget())) );
				item.add( new Label("alteredObjects",new Model(rd.getAlteredObjects())).setEscapeModelStrings(false) );
			}
		};
		add(listView);
		
		PagingNavigator navigator = new PagingNavigator("navigator",listView);
		add(navigator);
	}
}
