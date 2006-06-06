/*
 * $Id: StringBufferResourceStream.java 3307 2005-11-30 15:57:34 -0800 (Wed, 30
 * Nov 2005) ivaynberg $ $Revision: 3307 $ $Date: 2005-11-30 15:57:34 -0800
 * (Wed, 30 Nov 2005) $
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
package wicket.extensions.util.resource;

import java.util.Map;

import wicket.util.string.JavascriptUtils;
import wicket.util.string.Strings;

/**
 * Decorates the template with javascript tags.
 * 
 * @author Eelco Hillenius
 */
public final class JavaScriptTemplate extends TextTemplateDecorator
{
	private static final long serialVersionUID = 1L;

	/** Start tag for JavaScript body. */
	private static final String JAVASCRIPT_START_TAG = JavascriptUtils.SCRIPT_OPEN_TAG
			+ Strings.LINE_SEPARATOR;

	/** End tag for JavaScript body. */
	private static final String JAVASCRIPT_END_TAG = Strings.LINE_SEPARATOR
			+ JavascriptUtils.SCRIPT_CLOSE_TAG;

	/**
	 * Construct.
	 * 
	 * @param textTemplate
	 *            The template to decorate
	 */
	public JavaScriptTemplate(TextTemplate textTemplate)
	{
		super(textTemplate);
	}

	/**
	 * @see wicket.extensions.util.resource.TextTemplateDecorator#getBeforeTemplateContents()
	 */
	public String getBeforeTemplateContents()
	{
		return JAVASCRIPT_START_TAG;
	}

	/**
	 * @see wicket.extensions.util.resource.TextTemplateDecorator#getAfterTemplateContents()
	 */
	public String getAfterTemplateContents()
	{
		return JAVASCRIPT_END_TAG;
	}

	/**
	 * JavaScriptTemplate class decorates another text template class and so
	 * does not allow interpolation.
	 * 
	 * @param variables
	 *            Ignored
	 * @return This for chaining
	 */
	public TextTemplate interpolate(final Map variables)
	{
		return this;
	}
}