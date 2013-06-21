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
package org.apache.wicket.examples.pub;

import java.util.Locale;

import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.util.value.ValueMap;


/**
 * Demonstrates localization.
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 */
public final class Home extends WicketExamplePage
{
	/**
	 * Constructor
	 * 
	 * @param parameters
	 *            Page parameters (ignored since this is the home page)
	 */
	public Home(final PageParameters parameters)
	{
		add(new Image("beer", new PackageResourceReference(Home.class, "Beer.gif")));

		// create a dummy object to serve as our substitution model
		ValueMap map = new ValueMap();
		map.put("user", "Jonathan");

		// Here, we create a model that knows how to get localized strings.
		// It uses the page's resource (Home_cc_LC.properties) and gets the
		// text with resource key 'salution'. For the US, this is:
		// salutation=${user}, dude!
		// variable ${user} will be regconized as a property variable, and will
		// be substituted with the given model (the wrapped map). Hence,
		// ${user} will be replaced by map.get('user'), which is 'Jonathan'.
		StringResourceModel labelModel = new StringResourceModel("salutation", this,
			new Model<>(map));

		// Add the label with the dynamic model
		add(new Label("salutation", labelModel));

		// Add a couple of links to be able to play around with the session
		// locale
		add(new Link("goCanadian")
		{
			@Override
			public void onClick()
			{
				getSession().setLocale(Locale.CANADA);
			}
		});
		add(new Link("goUS")
		{
			@Override
			public void onClick()
			{
				getSession().setLocale(Locale.US);
			}
		});
		add(new Link("goDutch")
		{
			@Override
			public void onClick()
			{
				getSession().setLocale(new Locale("nl", "NL"));
			}
		});
		add(new Link("goGerman")
		{
			@Override
			public void onClick()
			{
				getSession().setLocale(new Locale("de", "DE"));
			}
		});
		add(new Link("goChinese")
		{
			@Override
			public void onClick()
			{
				getSession().setLocale(new Locale("zh", "CN"));
			}
		});
		add(new Link("goDanish")
		{
			@Override
			public void onClick()
			{
				getSession().setLocale(new Locale("da", "DK"));
			}
		});
		add(new Link("goKorean")
		{
			@Override
			public void onClick()
			{
				getSession().setLocale(new Locale("ko", "KR"));
			}
		});
		add(new Link("goHungarian")
		{
			@Override
			public void onClick()
			{
				getSession().setLocale(new Locale("hu"));
			}
		});
	}
}
