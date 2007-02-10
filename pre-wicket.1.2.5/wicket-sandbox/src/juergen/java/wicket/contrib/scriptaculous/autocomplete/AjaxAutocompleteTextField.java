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

import wicket.RequestCycle;
import wicket.markup.html.internal.HtmlHeaderContainer;
import wicket.request.target.InterfaceRequestTarget;

/**
 * 
 * @author <a href="mailto:wireframe6464@users.sourceforge.net">Ryan Sonnek</a>
 */
public abstract class AjaxAutocompleteTextField extends AutocompleteTextFieldSupport
		implements
			IAjaxListener, IAjaxResponder
{
	protected abstract String[] getResults(String input);
	
	public AjaxAutocompleteTextField(String id)
	{
		super(id);
	}

	public void renderHead(HtmlHeaderContainer container)
	{
		super.renderHead(container);

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
		write(container, urlFor(IAjaxListener.class));
		write(container, "', {});\n");
		write(container, "\t} \n");
		write(container, "} \n");
		write(container, "Behaviour.register(myrules);\n");
		write(container, "</script>\n");
	}

	public void onAjaxRequest()
	{
		validate();
		if (isValid())
		{
			updateModel();
		}
		
		getRequestCycle().setRequestTarget(new InterfaceRequestTarget(this, IAjaxResponder.class));
	}
	
	public void onAjaxRender()
	{
		String value = getValue();
		getResponse().write("<ul>\n");
		String[] results = getResults(value);
		for (int x = 0; x < results.length; x++)
		{
			String result = results[x];
			getResponse().write("<li>");
			getResponse().write(result);
			getResponse().write("</li>\n");
		}
		getResponse().write("</ul>\n");
	}
	
	static
	{
		// 
		RequestCycle.registerRequestListenerInterface(IAjaxListener.class);
	}
}
