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
package com.voicetribe.wicket.protocol.http;

import com.voicetribe.wicket.markup.html.HtmlPage;
import com.voicetribe.wicket.markup.html.basic.Label;
import com.voicetribe.wicket.markup.html.link.Link;
import com.voicetribe.wicket.PageParameters;
import com.voicetribe.wicket.RequestCycle;

/**
 * Mock page for testing.
 *
 * @author Chris Turner
 */
public class MockPage extends HtmlPage {

    public MockPage(final PageParameters parameters) {
        // Action link counts link clicks
        final Link actionLink = new Link("actionLink") {
            public void linkClicked(final RequestCycle cycle) {
                linkClickCount++;

                // Redirect back to result to avoid refresh updating the link count
                cycle.setRedirect(true);
            }
        };
        actionLink.add(new Label("linkClickCount", this, "linkClickCount"));
        add(actionLink);
    }

    /**
     * @return Returns the linkClickCount.
     */
    public int getLinkClickCount() {
        return linkClickCount;
    }

    /**
     * @param linkClickCount The linkClickCount to set.
     */
    public void setLinkClickCount(final int linkClickCount) {
        this.linkClickCount = linkClickCount;
    }

    private int linkClickCount = 0;
}
