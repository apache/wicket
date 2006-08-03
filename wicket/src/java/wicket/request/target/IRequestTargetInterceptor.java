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
package wicket.request.target;

import wicket.IRequestTarget;
import wicket.RequestCycle;
import wicket.ajax.AjaxRequestTarget;

/**
 * Intercepts calls to {@link RequestCycle#setRequestTarget(IRequestTarget)}.
 * <p>
 * Request targets that implement this interface will be able to veto any next
 * request target that is requested to be set at the
 * {@link RequestTarget request target}. A typical use for this are
 * {@link AjaxRequestTarget} where instead of following the normal request
 * processing with a full page render only a partial request is rendered. In
 * that case, it is up to {@link AjaxRequestTarget} to decide what to do with
 * the new request target; should it be handled by {@link AjaxRequestTarget} or
 * may the new request target be added on top of it.
 * </p>
 * <p>
 * This interface is only useful when it is implemented by a
 * {@link IRequestTarget request target}.
 * </p>
 * 
 * @author eelcohillenius
 */
public interface IRequestTargetInterceptor
{
	/**
	 * Called when this request target is the current on and a request at
	 * {@link RequestCycle#setRequestTarget(IRequestTarget) request target}
	 * arrives to set another one as the current. Typically, implementations of
	 * this method either 'eat up' the request target and either discard the
	 * request or do some custom processing with the provided request target. In
	 * that case, this method should return null. This method may also return -
	 * a possibly different/ altered - request target which is then used to be
	 * added on top of this one in the {@link RequestCycle}.
	 * 
	 * @param requestTarget
	 * @return Null if this request target wants to 'eat up' to request to set
	 *         the request target, or otherwise the request target that should
	 *         be set as the current on the request cycle (which may or may not
	 *         be the same request target as was passed in)
	 */
	IRequestTarget onSetRequestTarget(IRequestTarget requestTarget);
}
