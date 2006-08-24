/*
 * $Id$ $Revision$ $Date:
 * 2006-05-26 00:57:30 +0200 (vr, 26 mei 2006) $
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
package wicket.examples.pub;

import java.util.Locale;

import wicket.PageParameters;
import wicket.examples.WicketExamplePage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.image.Image;
import wicket.markup.html.link.Link;
import wicket.model.Model;
import wicket.model.StringResourceModel;
import wicket.util.value.ValueMap;

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
		new Image(this, "beer");

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
		StringResourceModel labelModel = new StringResourceModel("salutation", this, new Model(map));

		// Add the label with the dynamic model
		new Label(this, "salutation", labelModel);

		// Add a couple of links to be able to play around with the session
		// locale
		new Link(this, "goCanadian")
		{
			@Override
			public void onClick()
			{
				getSession().setLocale(Locale.CANADA);
			}
		};
		new Link(this, "goUS")
		{
			@Override
			public void onClick()
			{
				getSession().setLocale(Locale.US);
			}
		};
		new Link(this, "goDutch")
		{
			@Override
			public void onClick()
			{
				getSession().setLocale(new Locale("nl", "NL"));
			}
		};
		new Link(this, "goGerman")
		{
			@Override
			public void onClick()
			{
				getSession().setLocale(new Locale("de", "DE"));
			}
		};
		new Link(this, "goChinese")
		{
			@Override
			public void onClick()
			{
				getSession().setLocale(new Locale("zh", "CN"));
			}
		};
		new Link(this, "goDanish")
		{
			@Override
			public void onClick()
			{
				getSession().setLocale(new Locale("da", "DK"));
			}
		};
	}
}
