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
package org.apache.wicket.examples.guice;

import com.google.inject.Inject;
import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.examples.guice.service.IMyService;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.AbstractReadOnlyModel;

/**
 * Everybody's favorite example (Hello World), modified to use Guice.
 * 
 * @author Alastair Maw
 */
public class HomePage extends WicketExamplePage
{
	@Inject
	private IMyService service;

	private String labelValue = "<not yet initialized>";

	/**
	 * Constructor
	 */
	public HomePage()
	{
		add(new Link<Void>("link")
		{
			/**
			 * @see org.apache.wicket.markup.html.link.Link#onClick()
			 */
			@Override
			public void onClick()
			{
				labelValue = service.getHelloWorldText();
			}
		});
		add(new Label("message", new AbstractReadOnlyModel<String>()
		{

			@Override
			public String getObject()
			{
				return labelValue;
			}

		}));
	}
}