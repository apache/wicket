/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar
 * 2006) eelco12 $ $Revision: 5004 $ $Date: 2006-03-17 20:47:08 -0800 (Fri, 17
 * Mar 2006) $
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
package wicket.ajax.calldecorator;

import wicket.ajax.IAjaxCallDecorator;
import wicket.ajax.markup.html.AjaxFallbackLink;

/**
 * Decorator that can be used to cancel the regular action if ajax call was
 * performed. This allows us to, for example, cancel the default anchor behavior
 * (requesting href url) if an ajax call was made in the onclick event handler.
 * Ajax call cannot be performed if javascript has been turned off or no
 * compatible XmlHttpRequest object can be found. This decorator will make
 * javascript return true if the ajax call was made, and false otherwise.
 * 
 * @see AjaxFallbackLink
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public final class CancelEventIfNoAjaxDecorator extends AjaxPostprocessingCallDecorator
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 */
	public CancelEventIfNoAjaxDecorator()
	{
		this((IAjaxCallDecorator)null);
	}

	/**
	 * Constructors that allows chaining of another decorator
	 * 
	 * @param delegate
	 */
	public CancelEventIfNoAjaxDecorator(IAjaxCallDecorator delegate)
	{
		super(delegate);
	}

	/**
	 * @see wicket.ajax.calldecorator.AjaxPostprocessingCallDecorator#postDecorateScript(CharSequence)
	 */
	@Override
	public final CharSequence postDecorateScript(CharSequence script)
	{
		return script + "return !" + IAjaxCallDecorator.WICKET_CALL_RESULT_VAR + ";";
	}
}
