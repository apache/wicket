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
package wicket.util.resource;

import wicket.util.string.CssUtils;

/**
 * Decorates the template with CSS tags.
 * 
 * @author Eelco Hillenius
 */
public final class CssTemplate extends TextTemplateDecorator
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param textTemplate
	 *            The template to decorate
	 */
	public CssTemplate(TextTemplate textTemplate)
	{
		super(textTemplate);
	}

	/**
	 * @see wicket.util.resource.TextTemplateDecorator#getBeforeTemplateContents()
	 */
	@Override
	public String getBeforeTemplateContents()
	{
		return CssUtils.INLINE_OPEN_TAG;
	}

	/**
	 * @see wicket.util.resource.TextTemplateDecorator#getAfterTemplateContents()
	 */
	@Override
	public String getAfterTemplateContents()
	{
		return CssUtils.INLINE_CLOSE_TAG;
	}
}