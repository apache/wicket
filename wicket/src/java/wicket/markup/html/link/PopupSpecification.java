/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.markup.html.link;

import java.io.Serializable;

/**
 * A popup specification can be used as a property of the {@link Link}classes to specify
 * that the link should be rendered with an onClick javascript event handler that opens a
 * new window with the links' URL.
 *
 * @author Jonathan Locke
 * @author Eelco Hillenius
 */
public class PopupSpecification implements Serializable
{
    /** Width of popup window. */
    private int width = -1;

    /** Height of popup window. */
    private int height = -1;

    /** left position of popup window. */
    private int left = -1;

    /** top position of popup window. */
    private int top = -1;

    /**
     * The target to put in JavaScript.
     * This implementation simply refers to the href element, but clients
     * may want to override this (e.g. when the HTML element is not an anchor).
     */
    private String target = "href";

    /**
     * The logical name of the window. This can be anything you want, although you
     * should use alphanumeric characters only (no spaces or punctuation).
     * If you have a window already open and call window.open a second time using
     * the same windowName, the first window will be reused rather than opening
     * a second window.
     */
    private String windowName = null;

    /** Whether the popup window should have scrollbars. */
    private boolean scrollBars = false;

    /** Whether the browser should display the browser location toolbar. */
    private boolean locationbar = false;

    /** Whether the browser should display the menu bar. */
    private boolean menubar = false;

    /** Whether the popup window is resizable. */
    private boolean resizable = false;

    /**
     * Whether the popup window should have a status bar (the area at the bottom
     * of the browser).
     */ 
    private boolean statusBar = false;

    /** 
     * Whether the browser should display the toolbar that contains
     * the back/forward/etc buttons.
     */
    private boolean toolbar = false;

    /**
     * Construct.
     */
    public PopupSpecification()
    {
    }

    /**
     * Construct.
     * @param hasScrollBars whether the popup window has scroll bars
     * @param hasLocationbar whether the popup window has a location bar
     * @param isResizable whether the popup window is resizable
     * @param hasStatusBar whether the popup window has a status bar
     * @param hasToolbar whether the popup window has a tool bar
     */
    public PopupSpecification(boolean hasScrollBars, boolean hasLocationbar, boolean isResizable,
            boolean hasStatusBar, boolean hasToolbar)
    {
        setScrollBars(hasScrollBars);
        setLocationbar(hasLocationbar);
        setResizable(isResizable);
        setStatusBar(hasStatusBar);
        setToolbar(hasToolbar);
    }

    /**
     * Get the onClick javascript event handler.
     * @return the onClick javascript event handler
     */
    public String getPopupJavaScript()
    {
        String windowTitle = getWindowName();

        if (windowTitle == null)
        {
            windowTitle = "";
        }
        else
        {
            windowTitle = windowTitle.replace('.', '_'); // Fix for IE bug.
        }

        String target = getTarget();
        StringBuffer script = new StringBuffer(
                "if (!window.focus) return true; window.open(" + target + ", '")
                .append(windowTitle).append("', '");

        script.append("scrollbars=").append(isScrollBars() ? "yes" : "no");
        script.append(", location=").append(isLocationbar() ? "yes" : "no");
        script.append(", menubar=").append(isMenubar() ? "yes" : "no");
        script.append(", resizable=").append(isResizable() ? "yes" : "no");
        script.append(", scrollbars=").append(isScrollBars() ? "yes" : "no");
        script.append(", status=").append(isStatusBar() ? "yes" : "no");
        script.append(", toolbar=").append(isToolbar() ? "yes" : "no");

        if (getWidth() != -1)
        {
            script.append(", width=").append(getWidth());
        }

        if (getHeight() != -1)
        {
            script.append(", height=").append(getHeight());
        }

        if (getLeft() != -1)
        {
            script.append(", left=").append(getLeft());
        }

        if (getTop() != -1)
        {
            script.append(", top=").append(getTop());
        }

        script.append("'); ").append(" return false;");

        return script.toString();
    }

	/**
	 * Gets the target of the link.
	 * The default implementation simply refers to the href element, but clients
     * may want to override this (e.g. when the HTML element is not an anchor)
     * by setting the target explicitly.
	 * @return the target of the link
	 */
	protected String getTarget()
	{
		return target;
	}

	/**
	 * Sets the target of the link.
	 * The default implementation simply refers to the href element, but clients
     * may want to override this (e.g. when the HTML element is not an anchor)
     * by setting the target explicitly.
	 * @param target the target of the link
	 */
	public void setTarget(String target)
	{
		this.target = target;
	}

    /**
     * Gets the name of the window. This can be anything you want, although you
     * should use alphanumeric characters only (no spaces or punctuation).
     * If you have a window already open and call window.open a second time using
     * the same windowName, the first window will be reused rather than opening
     * a second window..
     * @return window name.
     */
    public String getWindowName()
    {
        return windowName;
    }

    /**
     * Sets the window name. The logical name of the window. This can be anything you want,
     * although you should use alphanumeric characters only (no spaces or punctuation). If
     * you have a window already open and call window.open a second time using the same
     * windowName, the first window will be reused rather than opening a second window
     * @param popupWindowName window name.
     * @return This
     */
    public PopupSpecification setWindowName(String popupWindowName)
    {
        this.windowName = popupWindowName;
        return this;
    }

    /**
     * Sets the with and height of the popup window.
     * @param popupWidth The popup width.
     * @param popupHeight The popup height.
     * @return This
     */
    public PopupSpecification setPopupDimensions(int popupWidth, int popupHeight)
    {
        setWidth(popupWidth);
        setHeight(popupHeight);
        return this;
    }

    /**
     * Sets the location of the popup window.
     * @param popupLeft The left position of the popup.
     * @param popupTop The top position of the popup.
     * @return This
     */
    public PopupSpecification setLocation(int popupLeft, int popupTop)
    {
        setLeft(popupLeft);
        setTop(popupTop);
        return this;
    }

    /**
     * Gets the popup window height.
     * @return the popup window height.
     */
    public int getHeight()
    {
        return height;
    }

    /**
     * Gets the popup window width.
     * @return the popup window width.
     */
    public int getWidth()
    {
        return width;
    }

    /**
     * Sets the popup window height.
     * @param popupHeight the popup window height.
     * @return This
     */
    public PopupSpecification setHeight(int popupHeight)
    {
        this.height = popupHeight;
        return this;
    }

    /**
     * Sets the popup window width.
     * @param popupWidth the popup window width.
     * @return This
     */
    public PopupSpecification setWidth(int popupWidth)
    {
        this.width = popupWidth;
        return this;
    }

    /**
     * Gets whether the popup window should have scrollbars.
     * @return whether the popup window should have scrollbars.
     */
    public boolean isScrollBars()
    {
        return scrollBars;
    }

    /**
     * Sets whether the popup window should have scrollbars.
     * @param popupScrollBars Whether the popup window should have scrollbars.
     * @return This
     */
    public PopupSpecification setScrollBars(boolean popupScrollBars)
    {
        this.scrollBars = popupScrollBars;
        return this;
    }

    /**
     * Gets whether the browser should display the browser location toolbar.
     * @return Whether the browser should display the browser location toolbar.
     */
    public boolean isLocationbar()
    {
        return locationbar;
    }

    /**
     * Sets whether the browser should display the browser location toolbar.
     * @param popupLocation Whether the browser should display the browser location
     *            toolbar.
     * @return This
     */
    public PopupSpecification setLocationbar(boolean popupLocation)
    {
        this.locationbar = popupLocation;
        return this;
    }

    /**
     * Gets whether the browser should display the menu bar.
     * @return Whether the browser should display the menu bar.
     */
    public boolean isMenubar()
    {
        return menubar;
    }

    /**
     * Sets whether the browser should display the menu bar.
     * @param popupMenubar Whether the browser should display the menu bar.
     * @return This
     */
    public PopupSpecification setMenubar(boolean popupMenubar)
    {
        this.menubar = popupMenubar;
        return this;
    }

    /**
     * Gets the left position of the popup window.
     * @return the left position of the popup window.
     */
    public int getLeft()
    {
        return left;
    }

    /**
     * Sets the left position of the popup window.
     * @param popupPositionLeft the left position of the popup window.
     * @return This
     */
    public PopupSpecification setLeft(int popupPositionLeft)
    {
        this.left = popupPositionLeft;
        return this;
    }

    /**
     * Gets the top position of the popup window.
     * @return the top position of the popup window.
     */
    public int getTop()
    {
        return top;
    }

    /**
     * Sets the top position of the popup window.
     * @param popupPositionTop the top position of the popup window.
     * @return This
     */
    public PopupSpecification setTop(int popupPositionTop)
    {
        this.top = popupPositionTop;
        return this;
    }

    /**
     * Gets whether the popup window is resizable.
     * @return Whether the popup window is resizable.
     */
    public boolean isResizable()
    {
        return resizable;
    }

    /**
     * Sets whether the popup window is resizable.
     * @param popupResizable Whether the popup window is resizable.
     * @return This
     */
    public PopupSpecification setResizable(boolean popupResizable)
    {
        this.resizable = popupResizable;
        return this;
    }

    /**
     * Gets whether the popup window should have a status bar (the area at the bottom of
     * the browser).
     * @return Whether the popup window should have a status bar (the area at the bottom
     *         of the browser).
     */
    public boolean isStatusBar()
    {
        return statusBar;
    }

    /**
     * Sets whether the popup window should have a status bar (the area at the bottom of
     * the browser).
     * @param popupStatus Whether the popup window should have a status bar (the area at
     *            the bottom of the browser).
     * @return This
     */
    public PopupSpecification setStatusBar(boolean popupStatus)
    {
        this.statusBar = popupStatus;
        return this;
    }

    /**
     * Gets whether the browser should display the toolbar that contains the
     * back/forward/etc buttons.
     * @return Whether the browser should display the toolbar that contains the
     *         back/forward/etc buttons.
     */
    public boolean isToolbar()
    {
        return toolbar;
    }

    /**
     * Sets whether the browser should display the toolbar that contains the
     * back/forward/etc buttons.
     * @param toolbar Whether the browser should display the toolbar that contains the
     *            back/forward/etc buttons.
     * @return This
     */
    public PopupSpecification setToolbar(boolean toolbar)
    {
        this.toolbar = toolbar;
        return this;
    }
}
