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
package wicket.examples.compref;

import wicket.examples.WicketExamplePage;
import wicket.markup.html.basic.Label;

/**
 * Page with examples on {@link wicket.markup.html.basic.MultiLineLabel}.
 * @author Eelco Hillenius
 */
public class BorderPage extends WicketExamplePage
{
	/**
	 * Constructor
	 */
	public BorderPage()
	{
		Label label = new Label("label", "I am the label");
		MyBorder border = new MyBorder("border");
		border.add(label);
		add(border);
	}
}