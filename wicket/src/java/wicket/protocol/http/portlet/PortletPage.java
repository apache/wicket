/*
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
package wicket.protocol.http.portlet;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Page;
import wicket.PageMap;
import wicket.PageParameters;
import wicket.model.IModel;

/**
 * Base class for portlet pages.
 * 
 * @param <T>
 *            The type
 * @see WebPage
 * @see Page
 * @author Janne Hietam&auml;ki
 */
public class PortletPage<T> extends Page<T>
{
	private static final long serialVersionUID = 1L;

	/** log. */
	private static final Log log = LogFactory.getLog(PortletPage.class);

	/*
	 * Current portlet mode. PortletMode is not serializable, so this is stored
	 * as a String.
	 */
	private String portletMode = PortletMode.VIEW.toString();

	/*
	 * Current window state. WindowState is not serializable, so this is stored
	 * as a String.
	 */
	private String windowState = WindowState.NORMAL.toString();

	/**
	 * @see Page#Page()
	 */
	protected PortletPage()
	{
		super();
	}

	/**
	 * @see Page#Page(IModel)
	 */
	protected PortletPage(final IModel<T> model)
	{
		super(model);
	}

	/**
	 * @see Page#Page(PageMap)
	 */
	protected PortletPage(final PageMap pageMap)
	{
		super(pageMap);
	}

	/**
	 * @see Page#Page(PageMap, IModel)
	 */
	protected PortletPage(final PageMap pageMap, final IModel<T> model)
	{
		super(pageMap, model);
	}

	/**
	 * Constructor which receives wrapped query string parameters for a request.
	 * Having this constructor public means that your page is 'bookmarkable' and
	 * hence can be called/ created from anywhere. For bookmarkable pages (as
	 * opposed to when you construct page instances yourself, this constructor
	 * will be used in preference to a no-arg constructor, if both exist. Note
	 * that nothing is done with the page parameters argument. This constructor
	 * is provided so that tools such as IDEs will include it their list of
	 * suggested constructors for derived classes.
	 * 
	 * @param parameters
	 *            Wrapped query string parameters.
	 */
	protected PortletPage(final PageParameters parameters)
	{
		this((IModel<T>)null);
	}

	/**
	 * Markup type for portlets is always html
	 * 
	 * @return Markup type
	 */
	public final String getMarkupType()
	{
		return "html";
	}

	/**
	 * @return The PortletRequestCycle for this PortletPage.
	 */
	protected final PortletRequestCycle getPortletRequestCycle()
	{
		return (PortletRequestCycle)getRequestCycle();
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * <p>
	 * 
	 * @param portletMode
	 */
	public final void setPortletMode(PortletMode portletMode)
	{
		if (!portletMode.equals(this.portletMode))
		{
			this.portletMode = portletMode.toString();
			onSetPortletMode(portletMode);
		}
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * <p>
	 * 
	 * @param windowState
	 */
	public final void setWindowState(WindowState windowState)
	{
		if (!windowState.equals(this.windowState))
		{
			this.windowState = windowState.toString();
			onSetWindowState(windowState);
		}
	}

	/**
	 * Get current portlet mode
	 * 
	 * @see javax.portlet.PortletMode
	 * @return portlet mode
	 */
	public PortletMode getPortletMode()
	{
		return new PortletMode(portletMode);
	}

	/**
	 * Get current window state
	 * 
	 * @see javax.portlet.WindowState
	 * 
	 * @return portlet window state
	 */
	public WindowState getWindowState()
	{
		return new WindowState(windowState);
	}

	/**
	 * Called when the portlet mode is changed.
	 * 
	 * @param portletMode
	 */
	protected void onSetPortletMode(PortletMode portletMode)
	{
	}

	/*
	 * Called when the window state is changed.
	 * 
	 * @param portletMode
	 */
	protected void onSetWindowState(WindowState windowState)
	{
	}
}
