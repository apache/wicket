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
package wicket.protocol.http;

import wicket.PageParameters;
import wicket.markup.html.WebPage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.link.Link;


/**
 * Mock page for testing.
 *
 * @author Chris Turner
 */
public class MockPage extends WebPage {

	/** Serial Version ID */
	private static final long serialVersionUID = 2957175986254155110L;

	/**
	 * Construct.
	 * @param parameters
	 */
	public MockPage(final PageParameters parameters) {
        // Action link counts link clicks
        final Link actionLink = new Link("actionLink") {
			/** Serial Version ID */
			private static final long serialVersionUID = 6609669501359176769L;

			public void linkClicked() {
                linkClickCount++;

                // Redirect back to result to avoid refresh updating the link count
                getRequestCycle().setRedirect(true);
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
