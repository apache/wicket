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
import java.util.Arrays;
import java.util.List;

import wicket.Application;
import wicket.markup.html.WebPage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.image.Image;
import wicket.markup.html.link.Link;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.PageableListView;
import wicket.markup.html.navigation.paging.PagingNavigator;
import wicket.model.IModel;
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
		new Image(this, "bug");

		new ApplicationView(this, "application", Application.get());

		Link link = new Link(this, "togglelink")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick()
			{
				WebApplication webApplication = (WebApplication)Application.get();
				boolean enabled = webApplication.getRequestLoggerSettings().isRequestLoggerEnabled();
				webApplication.getRequestLoggerSettings().setRequestLoggerEnabled(!enabled);
			}
		};
		new Label(link, "toggletext", new Model()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public Object getObject()
			{
				WebApplication webApplication = (WebApplication)Application.get();
				IRequestLogger requestLogger = webApplication.getRequestLogger();
				if (requestLogger == null)
				{
					return "Enable request recording";
				}
				else
				{
					return "Disable request recording";
				}
			};

		});
		new Label(this, "totalSessions", new Model()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public Object getObject()
			{
				return new Integer(getRequestLogger().getTotalCreatedSessions());
			}
		});
		new Label(this, "peakSessions", new Model()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public Object getObject()
			{
				return new Integer(getRequestLogger().getPeakSessions());
			}
		});
		new Label(this, "liveSessions", new Model()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public Object getObject()
			{
				return new Integer(getRequestLogger().getPeakSessions());
			}
		});

		IModel<List<SessionData>> sessionModel = new Model<List<SessionData>>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public List<SessionData> getObject()
			{
				return Arrays.asList(getRequestLogger().getLiveSessions());
			}
		};
		new Link(this, "requests")
		{
			private static final long serialVersionUID = 1L;

			/**
			 * @see wicket.markup.html.link.Link#onClick()
			 */
			@Override
			public void onClick()
			{
				setResponsePage(new RequestsPage(null));
			}
		};
		PageableListView<SessionData> listView = new PageableListView<SessionData>(this, "sessions", sessionModel, 50)
		{
			private static final long serialVersionUID = 1L;

			private final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM hh:mm:ss.SSS");
			
			@Override
			protected void populateItem(ListItem<SessionData> item)
			{
				final SessionData sd = item.getModelObject();
				Link link = new Link(item, "id")
				{
					private static final long serialVersionUID = 1L;

					/**
					 * @see wicket.markup.html.link.Link#onClick()
					 */
					@Override
					public void onClick()
					{
						setResponsePage(new RequestsPage(sd));
					}
				};
				new Label(link, "id", new Model<String>(sd.getSessionId()));
				new Label(item, "sessionInfo",new Model<Object>(sd.getSessionInfo()));
				new Label(item, "startDate",new Model<String>(sdf.format(sd.getStartDate())));
				new Label(item, "lastRequestTime",new Model<String>(sdf.format(sd.getLastActive())));
				new Label(item, "numberOfRequests",new Model<Long>(sd.getNumberOfRequests()));
				new Label(item, "totalTimeTaken",new Model<Long>(sd.getTotalTimeTaken()));
				new Label(item, "sessionSize", new Model<Bytes>(Bytes.bytes(sd.getSessionSize())));
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
