/*
 * $Id: ExplainPanel.java 5860 2006-05-25 20:29:28 +0000 (Thu, 25 May 2006)
 * eelco12 $ $Revision$ $Date: 2006-05-25 20:29:28 +0000 (Thu, 25 May
 * 2006) $
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
package wicket.examples.compref;

import wicket.MarkupContainer;
import wicket.markup.html.basic.MultiLineLabel;
import wicket.markup.html.panel.Panel;

/**
 * A explanation panel component.
 * 
 * @author Gwyn Evans
 */
class ExplainPanel extends Panel
{
	/**
	 * Construct.
	 * 
	 * @param parent
	 *            The parent of this component The parent of this component.
	 * @param html
	 * @param code
	 */
	public ExplainPanel(MarkupContainer parent, String html, String code)
	{
		super(parent, "explainPanel");
		new MultiLineLabel(this, "html", html);
		new MultiLineLabel(this, "code", code).setEscapeModelStrings(false);
	}
}
