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

/**
 * Interface used to decorate a wicket generated javascript that initiates an
 * ajax callback
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public interface IAjaxCallDecorator
{
	/**
	 * Name of javascript variable that will be true if ajax call was made,
	 * false otherwise. This variable is available in the after script only.
	 */
	public static final String WICKET_CALL_MADE_VAR = "wicketAjaxCallMade";

	/**
	 * @return javascript evaluated before initiation of ajax callback
	 */
	String getBeforeScript();

	/**
	 * @return javascript evaluated after initiation of ajax callback
	 */
	String getAfterScript();

	/**
	 * @return javascript evaluated when ajax request completes successfully
	 */
	String getOnSuccessScript();

	/**
	 * @return javascript evaluated when ajax request fails
	 */
	String getOnFailureScript();
}
