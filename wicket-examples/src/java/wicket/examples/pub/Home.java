/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.examples.pub;

import java.util.Locale;

import wicket.PageParameters;
import wicket.Session;
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
    /** current locale. */
    private Locale currentLocale = Locale.US;

    /**
     * Constructor
     * @param parameters Page parameters (ignored since this is the home page)
     */
    public Home(final PageParameters parameters)
    {
        add(new Image("beer"));

        // create a dummy object to serve as our Ognl substitution model
        ValueMap map = new ValueMap();
        map.put("user", "Jonathan");
        
        // Here, we create a model that knows how to get localized strings.
        // It uses the page's resource (Home_cc_LC.properties) and gets the
        // text with resource key 'salution'. For the US, this is:
        // salutation=${user}, dude!
        // variable ${user} will be regconized as an Ognl variable, and will
        // be substituted with the given model (the wrapped map). Hence,
        // ${user} will be replaced by map.get('user'), which is 'Jonathan'.
        StringResourceModel labelModel = new StringResourceModel(
                "salutation", this, new Model(map));

        // add the label with the dynamic model
        add(new Label("salutation", labelModel));

        // add a couple of links to be able to play around with the Locales
        add(new Link("goCanadian"){
            public void onLinkClicked()
            {
                currentLocale = Locale.CANADA;
            }
        });
        add(new Link("goUS"){
            public void onLinkClicked()
            {
                currentLocale = Locale.US;
            }
        });
        add(new Link("goDutch"){
            public void onLinkClicked()
            {
                currentLocale = new Locale("nl", "NL");
            }
        });
        add(new Link("goGerman"){
            public void onLinkClicked()
            {
                currentLocale = new Locale("de", "DE");
            }
        });
    }

    /**
     * @see wicket.MarkupContainer#onRender()
     */
    protected void onRender()
    {
        final Session session = getSession();

        // Keep the current locale
        Locale userLocale = session.getLocale();
        
        // Replace the locale for rendering
        session.setLocale(currentLocale);
        try
        {
            super.onRender();
        }
        finally
        {
            // set the user's locale back again
            session.setLocale(userLocale);
        }
    }
}

