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
package wicket.markup.html.link;

import java.io.Serializable;

/**
 * A popup specification can be used as a property of the {@link Link}classes
 * to specify that the link should be rendered with an onClick javascript event
 * handler that opens a new window with the links' URL.
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 */
public class PopupSettings implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	/** Flag to include location bar */
	public static final int LOCATION_BAR = 1;

	/** Flag to include menu bar */
	public static final int MENU_BAR = 2;

	/** Flag to make popup resizable */
	public static final int RESIZABLE = 4;

	/** Flag to include scrollbars */
	public static final int SCROLLBARS = 8;

	/** Flag to include status bar */
	public static final int STATUS_BAR = 16;

	/** Flag to include location bar */
	public static final int TOOL_BAR = 32;

	/** Display flags */
	private int displayFlags;

	/** Height of popup window. */
	private int height = -1;

	/** Left position of popup window. */
	private int left = -1;

	/**
	 * The target to put in JavaScript. This implementation simply refers to the
	 * href element, but clients may want to override this (e.g. when the HTML
	 * element is not an anchor).
	 */
	private String target = "href";

	/** Top position of popup window. */
	private int top = -1;

	/** Width of popup window. */
	private int width = -1;

	/**
	 * The logical name of the window. This can be anything you want, although
	 * you should use alphanumeric characters only (no spaces or punctuation).
	 * If you have a window already open and call window.open a second time
	 * using the same windowName, the first window will be reused rather than
	 * opening a second window.
	 */
	private String windowName = null;

	/**
	 * Construct.
	 */
	public PopupSettings()
	{
	}

	/**
	 * Construct.
	 * 
	 * @param displayFlags
	 *            Display flags
	 */
	public PopupSettings(final int displayFlags)
	{
		this.displayFlags = displayFlags;
	}

	/**
	 * Get the onClick javascript event handler.
	 * 
	 * @return the onClick javascript event handler
	 */
	public String getPopupJavaScript()
	{
		String windowTitle = windowName;

		if (windowTitle == null)
		{
			windowTitle = "";
		}
		else
		{
			// Fix for IE bug.
			windowTitle = windowTitle.replace(':', '_'); 
		}

		StringBuffer script = new StringBuffer("if (!window.focus) return true; window.open("
				+ target + ", '").append(windowTitle).append("', '");

		script.append("scrollbars=").append(flagToString(SCROLLBARS));
		script.append(", location=").append(flagToString(LOCATION_BAR));
		script.append(", menuBar=").append(flagToString(MENU_BAR));
		script.append(", resizable=").append(flagToString(RESIZABLE));
		script.append(", scrollbars=").append(flagToString(SCROLLBARS));
		script.append(", status=").append(flagToString(STATUS_BAR));
		script.append(", toolbar=").append(flagToString(TOOL_BAR));

		if (width != -1)
		{
			script.append(", width=").append(width);
		}

		if (height != -1)
		{
			script.append(", height=").append(height);
		}

		if (left != -1)
		{
			script.append(", left=").append(left);
		}

		if (top != -1)
		{
			script.append(", top=").append(top);
		}

		script.append("'); ").append(" return false;");

		return script.toString();
	}

	/**
	 * Sets the popup window height.
	 * 
	 * @param popupHeight
	 *            the popup window height.
	 * @return This
	 */
	public PopupSettings setHeight(int popupHeight)
	{
		this.height = popupHeight;
		return this;
	}

	/**
	 * Sets the left position of the popup window.
	 * 
	 * @param popupPositionLeft
	 *            the left position of the popup window.
	 * @return This
	 */
	public PopupSettings setLeft(int popupPositionLeft)
	{
		this.left = popupPositionLeft;
		return this;
	}

	/**
	 * Sets the target of the link. The default implementation simply refers to
	 * the href element, but clients may want to override this (e.g. when the
	 * HTML element is not an anchor) by setting the target explicitly.
	 * 
	 * @param target
	 *            the target of the link
	 */
	public void setTarget(String target)
	{
		this.target = target;
	}

	/**
	 * Sets the top position of the popup window.
	 * 
	 * @param popupPositionTop
	 *            the top position of the popup window.
	 * @return This
	 */
	public PopupSettings setTop(int popupPositionTop)
	{
		this.top = popupPositionTop;
		return this;
	}

	/**
	 * Sets the popup window width.
	 * 
	 * @param popupWidth
	 *            the popup window width.
	 * @return This
	 */
	public PopupSettings setWidth(int popupWidth)
	{
		this.width = popupWidth;
		return this;
	}

	/**
	 * Sets the window name. The logical name of the window. This can be
	 * anything you want, although you should use alphanumeric characters only
	 * (no spaces or punctuation). If you have a window already open and call
	 * window.open a second time using the same windowName, the first window
	 * will be reused rather than opening a second window
	 * 
	 * @param popupWindowName
	 *            window name.
	 * @return This
	 */
	public PopupSettings setWindowName(String popupWindowName)
	{
		this.windowName = popupWindowName;
		return this;
	}

	/**
	 * @param flag
	 *            The flag to test
	 * @return Yes or no depending on whether the flag is set
	 */
	private String flagToString(final int flag)
	{
		return (this.displayFlags & flag) != 0 ? "yes" : "no";
	}
}
