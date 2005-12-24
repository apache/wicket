/*
 * $Id$
 * $Revision$ $Date$
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
package wicket.request.target.mixin;

import wicket.RequestCycle;
import wicket.request.RequestParameters;

/**
 * {@link wicket.IRequestTarget}s that implement this interface, will have this
 * method checked after
 * {@link wicket.request.IRequestCycleProcessor#resolve(RequestCycle, RequestParameters)}
 * is called (and resolved to this target).
 * 
 * @author Eelco Hillenius
 */
public interface IAccessChecker
{
	/**
	 * Whether access is allowed to this target. If the page is not allowed you
	 * must redirect to another page, otherwise you will get a blank page.
	 * Redirecting to another page can be done in a two ways:
	 * <li>Use redirectToInterceptPage(Page page), You will be redirected to
	 * that page when it is done you will be returned to this one</li>
	 * <li>Use redirectTo(Page page), You will be redirected to that page when
	 * it is done you will have to specify where you will go next</li>
	 * <li>RequestCycle.setResponsePage(Page page), That page is rendered
	 * directly, no redirect wil happen</li>
	 * @param requestCycle 
	 * 
	 * @return true if access is allowed, false otherwise
	 */
	boolean checkAccess(RequestCycle requestCycle);
}
