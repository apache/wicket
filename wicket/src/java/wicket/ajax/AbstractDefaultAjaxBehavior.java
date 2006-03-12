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

import wicket.Application;
import wicket.RequestCycle;
import wicket.Response;
import wicket.behavior.AbstractAjaxBehavior;
import wicket.markup.html.PackageResourceReference;
import wicket.settings.IAjaxSettings;

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

	/** reference to the default ajax debug support javascript file. */
	private static final PackageResourceReference JAVASCRIPT_DEBUG_DRAG = new PackageResourceReference(
			AbstractDefaultAjaxBehavior.class, "wicket-ajax-debug-drag.js");

	/** reference to the default ajax debug support javascript file. */
	private static final PackageResourceReference JAVASCRIPT_DEBUG = new PackageResourceReference(
			AbstractDefaultAjaxBehavior.class, "wicket-ajax-debug.js");

	
	
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
		final IAjaxSettings settings=Application.get().getAjaxSettings();
		
		writeJsReference(response, JAVASCRIPT);
		
		if (settings.isAjaxDebugModeEnabled()) {
			response.write("<script>wicketAjaxDebugEnable=true;</script>");
			writeJsReference(response, JAVASCRIPT_DEBUG_DRAG);
			writeJsReference(response, JAVASCRIPT_DEBUG);
		}
		
		
		
	}

	/**
	 * @see wicket.behavior.IBehaviorListener#onRequest()
	 */
	public final void onRequest()
	{
		boolean isPageVersioned = true;
		try
		{
			isPageVersioned = getComponent().getPage().isVersioned();
			getComponent().getPage().setVersioned(false);

			AjaxRequestTarget target = new AjaxRequestTarget();
			RequestCycle.get().setRequestTarget(target);
			respond(target);
		} 
		finally 
		{
			getComponent().getPage().setVersioned(isPageVersioned);
		}
	}
	
	/**
	 * @param target
	 *            The AJAX target
	 */
	protected abstract void respond(AjaxRequestTarget target);
}
