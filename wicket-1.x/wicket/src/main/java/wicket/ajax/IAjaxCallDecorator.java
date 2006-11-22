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
package wicket.ajax;

import java.io.Serializable;

/**
 * Interface used to decorate a wicket generated javascript that performs an
 * ajax callback
 * <p>
 * The returned scripts are rendered in the HTML as follow:
 * 
 * <pre>
 *      &lt;a href=&quot;javascript:[script]var wcall=wicketAjaxGet('[url]', function() {[onSuccessScript]},
 *      function() {[onFailureScript});&quot; ...&gt;[text of the link]&lt;/a&gt;
 * </pre>
 * 
 * As a result, using double quotes in the script will break the link syntax and
 * make it fail (or fallback in the case of an AjaxFallbackLink). So, if single
 * quotes have to be inserted in strings contained in the scripts, they must be
 * properly escaped to pass through Java and Javascript, for example:
 * 
 * <pre>
 * return &quot;alert('It\\'s ok!')&quot;;
 * </pre>
 * 
 * Also note that <tt>decorateScript(CharSequence script)</tt> should
 * generally append to the script rather than replace it:
 * 
 * <pre>
 * return &quot;alert('Before ajax call');&quot; + script;
 * </pre>
 * 
 * Both following examples will break the link:
 * 
 * <pre>
 * return &quot;alert('Before ajax call');&quot;; // missing to append the script
 * return &quot;alert('Before ajax call')&quot; + script; // missing &quot;;&quot;
 * </pre>
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public interface IAjaxCallDecorator extends Serializable
{
	/**
	 * Name of javascript variable that will be true if ajax call was made,
	 * false otherwise. This variable is available in the after script only.
	 */
	public static final String WICKET_CALL_RESULT_VAR = "wcall";

	/**
	 * Decorates the script that performs the ajax call
	 * 
	 * @param script
	 * @return decorated script
	 */
	CharSequence decorateScript(CharSequence script);

	/**
	 * Decorates the onSuccess handler script
	 * 
	 * @param script
	 * @return decorated onSuccess handler script
	 */
	CharSequence decorateOnSuccessScript(CharSequence script);

	/**
	 * Decorates the onFailure handler script
	 * 
	 * @param script
	 * @return decorated onFailure handler script
	 */
	CharSequence decorateOnFailureScript(CharSequence script);

}
