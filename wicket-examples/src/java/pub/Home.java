///////////////////////////////////////////////////////////////////////////////////
//
// Created Jun 13, 2004
//
// Copyright 2004, Jonathan W. Locke
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package pub;

import java.util.Locale;

import com.voicetribe.util.value.ValueMap;
import com.voicetribe.wicket.PageParameters;
import com.voicetribe.wicket.RequestCycle;
import com.voicetribe.wicket.Session;
import com.voicetribe.wicket.markup.html.HtmlPage;
import com.voicetribe.wicket.markup.html.basic.Label;
import com.voicetribe.wicket.markup.html.image.Image;

/**
 * Demonstrates localization.
 * @author Jonathan Locke
 */
public final class Home extends HtmlPage
{
    /**
     * Constructor
     * @param parameters Page parameters (ignored since this is the home page)
     */
    public Home(final PageParameters parameters)
    {
        add(new Image("beer"));
        ValueMap map = new ValueMap();
        map.put("user", "Jonathan");
        add(new Label("salutation", getLocalizedStringWithModel("salutation", map)));
    }
    
    /**
     * @see com.voicetribe.wicket.Container#handleRender(com.voicetribe.wicket.RequestCycle)
     */
    protected void handleRender(final RequestCycle cycle)
    {        
        super.handleRender(cycle);
        final Session session = cycle.getSession();
        if (session.getLocale() != Locale.CANADA)
        {
            session.setLocale(Locale.CANADA);
        }
        else
        {
            session.setLocale(Locale.US);
        }
    }
}

///////////////////////////////// End of File /////////////////////////////////
