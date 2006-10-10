/*
 * $Id: RedirectPageRequestTarget.java 4387 2006-02-13 05:23:54 +0000 (Mon, 13
 * Feb 2006) jonathanlocke $ $Revision$ $Date: 2006-02-13 05:23:54 +0000
 * (Mon, 13 Feb 2006) $
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
package wicket.request.target.component.listener;

import wicket.IRedirectListener;
import wicket.Page;
import wicket.RequestCycle;

/**
 * Specialization of page request that denotes that we are actually handling a
 * redirect request of a page.
 * 
 * @author Eelco Hillenius
 */
public class RedirectPageRequestTarget extends AbstractListenerInterfaceRequestTarget
{
	/**
	 * Construct.
	 * 
	 * @param page
	 *            the target of the redirect handling
	 */
	public RedirectPageRequestTarget(final Page page)
	{
		super(page, page, IRedirectListener.INTERFACE);
	}

	/**
	 * @see wicket.request.target.IEventProcessor#processEvents(wicket.RequestCycle)
	 */
	public final void processEvents(final RequestCycle requestCycle)
	{
		getRequestListenerInterface().invoke(getPage(), getTarget());
	}
}
