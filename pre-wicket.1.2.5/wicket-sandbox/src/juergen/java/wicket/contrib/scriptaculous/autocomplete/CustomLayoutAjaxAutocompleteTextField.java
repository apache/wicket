/*
 * $Id: CustomLayoutAjaxAutocompleteTextField.java,v 1.4 2006/01/08 12:14:24
 * jdonnerstag Exp $ $Revision$ $Date$
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
package wicket.contrib.scriptaculous.autocomplete;

import wicket.PageParameters;
import wicket.markup.html.internal.HtmlHeaderContainer;

/**
 * Autocomplete text field that allows for customized layout of autocomplete
 * entries. A user defined <code>PageContribution</code> is used to display
 * the autocomplete information.
 * 
 * @author <a href="mailto:wireframe6464@users.sourceforge.net">Ryan Sonnek</a>
 */
public class CustomLayoutAjaxAutocompleteTextField extends AutocompleteTextFieldSupport
{
	private final Class page;

	public CustomLayoutAjaxAutocompleteTextField(String id, Class page)
	{
		super(id);
		this.page = page;
	}

	public void renderHead(HtmlHeaderContainer container)
	{
		super.renderHead(container);

		PageParameters parameters = new PageParameters();
		parameters.put("fieldName", this.getId());
		String url = this.urlFor(null, page, parameters);

		write(container, "<script type='text/javascript'>\n");
		write(container, "var myrules = { \n");
		write(container, "\t'#");
		write(container, getId());
		write(container, "' : function(el){ \n");
		write(container, "\t\tnew Ajax.Autocompleter('");
		write(container, getId());
		write(container, "', '");
		write(container, getAutocompleteId());
		write(container, "', '");
		write(container, url);
		write(container, "', {});\n");
		write(container, "\t} \n");
		write(container, "} \nBehaviour.register(myrules);\n");
		write(container, "</script>\n");
	}
}
