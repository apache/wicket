/*
 * $Id: org.eclipse.jdt.ui.prefs,v 1.6 2006/02/06 08:27:03 ivaynberg Exp $
 * $Revision: 1.6 $ $Date: 2006/02/06 08:27:03 $
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
 * An adapter for implementations of {@link IAjaxCallDecorator}.
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public abstract class AjaxCallDecorator implements IAjaxCallDecorator
{

	/**
	 * @see wicket.ajax.IAjaxCallDecorator#decorateScript(CharSequence)
	 */
	public CharSequence decorateScript(CharSequence script)
	{
		return script;
	}

	/**
	 * @see wicket.ajax.IAjaxCallDecorator#decorateOnSuccessScript(java.lang.String)
	 */
	public CharSequence decorateOnSuccessScript(CharSequence script)
	{
		return script;
	}

	/**
	 * @see wicket.ajax.IAjaxCallDecorator#decorateOnFailureScript(java.lang.String)
	 */
	public CharSequence decorateOnFailureScript(CharSequence script)
	{
		return script;
	}


}
