/*
 * $Id$
 * $Revision$ $Date$
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

import wicket.markup.html.internal.HtmlHeaderContainer;

/**
 * 
 * @author <a href="mailto:wireframe6464@users.sourceforge.net">Ryan Sonnek</a>
 */
public class AutocompleteTextField extends AutocompleteTextFieldSupport
{
	private final String[] results;

	public AutocompleteTextField(String id, String[] results)
	{
		super(id);

		this.results = results;
	}

	public void renderHead(HtmlHeaderContainer container)
	{
		super.renderHead(container);

		write(container, "<script type='text/javascript'>\n");
		write(container, "var myrules = { \n");
		write(container, "\t'#");
		write(container, getId());
		write(container, "' : function(el){ \n");
		write(container, "\t\tnew Autocompleter.Local('");
		write(container, getId());
		write(container, "', '");
		write(container, getAutocompleteId());
		write(container, "', ");
		write(container, buildResults());
		write(container, ", {});\n");
		write(container, "\t} \n");
		write(container, "} \n");
		write(container, "Behaviour.register(myrules);\n");
		write(container, "</script>\n");
	}

	private String buildResults()
	{
		String result = "new Array(";
		for (int i = 0; i < results.length; i++)
		{
			result += "'" + results[i] + "'";
			if (i < results.length - 1)
			{
				result += ",";
			}
		}
		result += ")";
		return result;
	}
}
