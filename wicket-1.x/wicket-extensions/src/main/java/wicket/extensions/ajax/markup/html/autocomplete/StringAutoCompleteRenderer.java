/*
 * $Id$ $Revision$ $Date$
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
package wicket.extensions.ajax.markup.html.autocomplete;

/**
 * An renderer that assumes that assist objects are {@link String}s. Great for
 * quickly generating a list of assists.
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public final class StringAutoCompleteRenderer extends AbstractAutoCompleteTextRenderer
{
	private static final long serialVersionUID = 1L;

	/**
	 * A singleton instance
	 */
	public static final IAutoCompleteRenderer INSTANCE = new StringAutoCompleteRenderer();

	/**
	 * @see AbstractAutoCompleteTextRenderer#getTextValue(Object)
	 */
	protected String getTextValue(Object object)
	{
		return object.toString();
	}

}
