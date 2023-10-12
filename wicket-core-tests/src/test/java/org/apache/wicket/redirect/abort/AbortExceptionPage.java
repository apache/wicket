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
package org.apache.wicket.redirect.abort;

import java.util.Collections;
import java.util.List;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.http.flow.AbortWithHttpErrorCodeException;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * Page that optionally throws an abortexception during render phase.
 * 
 * @author Peter Ertl
 */
public class AbortExceptionPage extends WebPage
{
	private static final long serialVersionUID = -5906071716129043859L;

	/**
	 * Construct.
	 * 
	 * @param parameters
	 */
	public AbortExceptionPage(PageParameters parameters)
	{
		final boolean triggerError = parameters.get("trigger").toBoolean();

		if (!triggerError)
		{
			throw new AbortWithHttpErrorCodeException(1234, "this error will be rendered");
		}

		IModel<List<Object>> model = new LoadableDetachableModel<List<Object>>()
		{
			private static final long serialVersionUID = -1285116295157071919L;

			@Override
			protected List<Object> load()
			{
				if (triggerError)
				{
					throw new AbortWithHttpErrorCodeException(1234,
						"this error will NOT be rendered");
				}
				else
				{
					return Collections.emptyList();
				}
			}
		};

		add(new ListView<Object>("test", model)
		{
			private static final long serialVersionUID = -4176346513350288174L;

			@Override
			protected void populateItem(final ListItem<Object> item)
			{
			}
		});
	}
}
