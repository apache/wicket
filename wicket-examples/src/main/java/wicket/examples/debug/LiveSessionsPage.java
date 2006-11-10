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
import java.util.Arrays;

import wicket.Application;
import wicket.Component;
import wicket.markup.html.WebPage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.image.Image;
import wicket.markup.html.link.Link;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.PageableListView;
import wicket.markup.html.navigation.paging.PagingNavigator;
import wicket.model.Model;
import wicket.protocol.http.IRequestLogger;
import wicket.protocol.http.RequestLogger;
import wicket.protocol.http.WebApplication;
import wicket.protocol.http.RequestLogger.SessionData;
import wicket.util.lang.Bytes;

/**
 * @author jcompagner
 */
public class LiveSessionsPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 */
	public LiveSessionsPage()
	{
		add(new Image("bug"));
		
		add(new ApplicationView("application", Application.get()));
		
		Link link = new Link("togglelink")
		{
			private static final long serialVersionUID = 1L;

			public void onClick() 
			{
				WebApplication webApplication = (WebApplication)Application.get();
				boolean enabled = webApplication.getRequestLoggerSettings().isRequestLoggerEnabled();
				webApplication.getRequestLoggerSettings().setRequestLoggerEnabled(!enabled);
			}
		};
		link.add( new Label("toggletext", new Model()
		{
			private static final long serialVersionUID = 1L;
			
			public Object getObject(Component component) 
			{
				WebApplication webApplication = (WebApplication)Application.get();
				IRequestLogger requestLogger = webApplication.getRequestLogger();
				if(requestLogger == null)
				{
					return "Enable request recording";
				}
				else
				{
					return "Disable request recording";
				}
			};
			
		}));
		add(link);
		add(new Label("totalSessions",new Model()
		{
			private static final long serialVersionUID = 1L;

			public Object getObject(Component component)
			{
				return new Integer(getRequestLogger().getTotalCreatedSessions());
			}
		}));
		add(new Label("peakSessions",new Model()
		{
			private static final long serialVersionUID = 1L;
		
			public Object getObject(Component component)
			{
				return new Integer(getRequestLogger().getPeakSessions());
			}
		}));
		add(new Label("liveSessions",new Model()
		{
			private static final long serialVersionUID = 1L;
		
			public Object getObject(Component component)
			{
				return new Integer(getRequestLogger().getPeakSessions());
			}
		}));
		
		Model sessionModel = new Model()
		{
			private static final long serialVersionUID = 1L;
			
			public Object getObject(Component component)
			{
				return Arrays.asList(getRequestLogger().getLiveSessions());
			}
		};
		PageableListView listView = new PageableListView("sessions",sessionModel,50)
		{
			private static final long serialVersionUID = 1L;
			
			private final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM hh:mm:ss.SSS");
			
			protected void populateItem(ListItem item) 
			{
				final SessionData sd = (SessionData)item.getModelObject();
				Link link = new Link("id")
				{
					private static final long serialVersionUID = 1L;
					/**
					 * @see wicket.markup.html.link.Link#onClick()
					 */
					public void onClick()
					{
						setResponsePage(new RequestsPage(sd));
					}
				};
				link.add( new Label("id",new Model(sd.getSessionId())));
				item.add( link);
				item.add( new Label("lastRequestTime",new Model(sdf.format(sd.getLastActive()))) );
				item.add( new Label("requestCount",new Model(new Long(sd.getNumberOfRequests()))) );
				item.add( new Label("requestsTime",new Model(new Long(sd.getTotalTimeTaken()))) );
				item.add( new Label("sessionSize",new Model(Bytes.bytes(sd.getSessionSize()))) );
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
		if(webApplication.getRequestLogger() == null)
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
