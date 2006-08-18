/*
 * $Id: SimplePage.java 5875 2006-05-25 22:52:19 +0000 (Thu, 25 May 2006)
 * eelco12 $ $Revision$ $Date: 2006-05-25 22:52:19 +0000 (Thu, 25 May
 * 2006) $
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.markup.html.internal;

import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.WebPage;
import wicket.markup.html.basic.Label;


/**
 * Mock page for testing.
 * 
 * @author Juergen Donnerstag
 */
public class EnclosurePage_1 extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 */
	public EnclosurePage_1()
	{
		new Label(this, "label1", "Test Label 1");
		new Label(this, "label2", "Test Label 2");
		new Label(this, "label3", "Test Label 3").setVisible(false);
		new Label(this, "label4", "Test Label 2");
		new Label(this, "label5", "Test Label 2");
		new Label(this, "label6", "Test Label 2");
		new Label(this, "label7", "Test Label 2");
		WebMarkupContainer container = new WebMarkupContainer(this, "container");
		new Label(container, "label8", "Test Label 2");
		new Label(this, "label9", "Test Label 2");
	}
}
