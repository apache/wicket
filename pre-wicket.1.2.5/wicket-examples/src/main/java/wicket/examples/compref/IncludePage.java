/*
 * $Id$ $Revision$ $Date$
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
package wicket.examples.compref;

import wicket.examples.WicketExamplePage;
import wicket.markup.html.include.Include;

/**
 * Page with examples on {@link wicket.markup.html.basic.MultiLineLabel}.
 * 
 * @author Eelco Hillenius
 */
public class IncludePage extends WicketExamplePage
{
	/**
	 * Constructor
	 */
	public IncludePage()
	{
		add(new Include("include", "to_be_included.html"));
	}

	/**
	 * Override base method to provide an explanation
	 */
	protected void explain()
	{
		String html = "<span wicket:id=\"include\">\n"
				+ "Inclusion contents to be inserted here.\n" + "</span>";
		String code = "&nbsp;&nbsp;&nbsp;&nbsp;public IncludePage()\n"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;{\n"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;add(new Include(\"include\", \"to_be_included.html\"));\n"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;}";
		add(new ExplainPanel(html, code));
	}
}