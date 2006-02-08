/*
 * $Id$ $Revision:
 * 1.5 $ $Date$
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

import wicket.RequestCycle;
import wicket.Response;
import wicket.behavior.AbstractAjaxBehavior;
import wicket.markup.html.PackageResourceReference;

/**
 * The base class for Wicket's default AJAX implementation.
 * 
 * @author Igor Vaynberg
 */
public abstract class AbstractDefaultAjaxBehavior extends AbstractAjaxBehavior
{
	private static final long serialVersionUID = 1L;

	/** reference to the default ajax support javascript file. */
	private static final PackageResourceReference JAVASCRIPT = new PackageResourceReference(
			AbstractDefaultAjaxBehavior.class, "wicket-ajax.js");

	/**
	 * 
	 * @see wicket.behavior.AbstractAjaxBehavior#getImplementationId()
	 */
	protected final String getImplementationId()
	{
		return "wicket-default";
	}

	/**
	 * 
	 * @see wicket.behavior.AbstractAjaxBehavior#onRenderHeadInitContribution(wicket.Response)
	 */
	protected void onRenderHeadInitContribution(final Response response)
	{
		writeJsReference(response, JAVASCRIPT);
	}

	/**
	 * @see wicket.behavior.IBehaviorListener#onRequest()
	 */
	public final void onRequest()
	{
		AjaxRequestTarget target = new AjaxRequestTarget();
		RequestCycle.get().setRequestTarget(target);
		respond(target);
	}

	/**
	 * @param target
	 *            The AJAX target
	 */
	protected abstract void respond(AjaxRequestTarget target);
}
