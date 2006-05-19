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

/**
 * Ajax call decorator that decorates script after allowing the wrapped delegate
 * decorator to decorate it first.
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public abstract class AjaxPostprocessingCallDecorator implements IAjaxCallDecorator
{
	private final IAjaxCallDecorator delegate;

	/**
	 * Construct.
	 * 
	 * @param delegate
	 *            wrapped delegate decorator
	 */
	public AjaxPostprocessingCallDecorator(IAjaxCallDecorator delegate)
	{
		this.delegate = delegate;
	}


	/**
	 * @see wicket.ajax.IAjaxCallDecorator#decorateScript(CharSequence)
	 */
	public final CharSequence decorateScript(CharSequence script)
	{
		CharSequence s = (delegate == null) ? script : delegate.decorateScript(script);
		return postDecorateScript(s);
	}

	/**
	 * @see wicket.ajax.IAjaxCallDecorator#decorateOnSuccessScript(java.lang.String)
	 */
	public final CharSequence decorateOnSuccessScript(CharSequence script)
	{
		CharSequence s = (delegate == null) ? script : delegate.decorateOnSuccessScript(script);
		return postDecorateOnSuccessScript(s);
	}

	/**
	 * @see wicket.ajax.IAjaxCallDecorator#decorateOnFailureScript(java.lang.String)
	 */
	public final CharSequence decorateOnFailureScript(CharSequence script)
	{
		CharSequence s = (delegate == null) ? script : delegate.decorateOnFailureScript(script);
		return postDecorateOnFailureScript(s);
	}


	/**
	 * Decorates ajax call script
	 * 
	 * @param script
	 * @return decorated script
	 */
	public CharSequence postDecorateScript(CharSequence script)
	{
		return script;
	}

	/**
	 * Decorates the success handling script
	 * 
	 * @param script
	 * @return decorated script
	 */
	public CharSequence postDecorateOnSuccessScript(CharSequence script)
	{
		return script;
	}

	/**
	 * Decorates the failure handling script
	 * 
	 * @param script
	 * @return decorated script
	 */
	public CharSequence postDecorateOnFailureScript(CharSequence script)
	{
		return script;
	}


}
