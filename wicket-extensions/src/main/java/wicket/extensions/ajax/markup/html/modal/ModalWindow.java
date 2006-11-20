/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.extensions.ajax.markup.html.modal;

import java.io.Serializable;

import wicket.Application;
import wicket.Component;
import wicket.Page;
import wicket.PageMap;
import wicket.RequestCycle;
import wicket.ResourceReference;
import wicket.Session;
import wicket.WicketRuntimeException;
import wicket.ajax.AbstractDefaultAjaxBehavior;
import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.IAjaxCallDecorator;
import wicket.ajax.calldecorator.CancelEventIfNoAjaxDecorator;
import wicket.behavior.HeaderContributor;
import wicket.markup.ComponentTag;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.panel.Panel;
import wicket.markup.html.resources.CompressedResourceReference;
import wicket.request.RequestParameters;
import wicket.settings.IPageSettings;
import wicket.util.lang.EnumeratedType;
import wicket.util.string.AppendingStringBuffer;
import wicket.util.string.Strings;

/**
 * Modal window component.
 * <p>
 * Modal window is a draggable window (with either div or iframe content) that
 * prevent user from interacting the rest of page (using a mask) until the
 * window is closed.
 * <p>
 * The window is draggable and optionally resizable. The content can be either
 * <ul>
 * <li><b>a component</b> - you need to add the component to modal window
 * (with id obtained using <code>{@link #getContentId()}</code>, or
 * <li><b>a page</b> - you need to pass a <code>{@link PageCreator}</code>
 * instance to a <code>{@link #setPageCreator(ModalWindow.PageCreator)}</code>
 * method.
 * </ul>
 * In case the content is a component, it is not rendered until the window is
 * shown (method <code>{@link #show(AjaxRequestTarget)})</code>. In case the
 * content is another page, you can set the desired pagemap name using
 * <code>{@link #setPageMapName(String)}</code>. Setting pagemap is only
 * needed when wicket multiwindow support is on.
 * <p>
 * The window can be made visible from an ajax handler using
 * <code>{@link #show(AjaxRequestTarget)}</code>.
 * <p>
 * To close the window there are multiple options. Static method
 * <code>{@link #close(AjaxRequestTarget)}</code> can be used to close the
 * window from a handler of ajax link inside the window. By default the close
 * button in the upper right corner of the window closes it. This behavior can
 * be altered using
 * <code>{@link #setCloseButtonCallback(ModalWindow.CloseButtonCallback)}</code>.
 * If you want to be notified when the window is closed (either using the close
 * button or calling <code>{@link #close(AjaxRequestTarget)})</code>, you
 * can use
 * <code>{@link #setWindowClosedCallback(ModalWindow.WindowClosedCallback)}</code>.
 * <p>
 * Title is specified using {@link #setTitle(String)}. If the content is a page
 * (iframe), the title can remain unset, in that case title from the page inside
 * window will be shown.
 * <p>
 * There are several options to specify the visual properties of the window. In
 * all methods where size is expected, width refers to width of entire window
 * (including frame), height refers to the height of window content (without
 * frame).
 * <p>
 * <ul>
 * <li><code>{@link #setResizable(boolean)}</code> specifies, whether the
 * window can be resized.
 * <li><code>{@link #setInitialWidth(int)}</code> and
 * <code>{@link #setInitialHeight(int)}</code> specify the initial width and
 * height of window. If the window is resizable, the unit of these dimensions is
 * always "px". If the window is not resizable, the unit can be specified using
 * <code>{@link #setWidthUnit(String)}</code> and
 * <code>{@link #setHeightUnit(String)}</code>. If the window is not
 * resizable and the content is a component (not a page), the initial height
 * value can be ignored and the actual height can be determined from the height
 * of the content. To enable this behavior use
 * <code>{@link #setUseInitialHeight(boolean)}</code>.
 * <li>The window position (and size if the window is resizable) can be stored
 * in a cookie, so that it is preserved when window is close. The name of the
 * cookie is specified via <code>{@link #setCookieName(String)}</code>. If
 * the name is <code>null</code>, position is not stored (initial width and
 * height are always used). Default cookie name is generated using
 * <code>hashCode()</code>.
 * <li><code>{@link #setMinimalWidth(int)}</code> and
 * <code>{@link #setMinimalHeight(int)}</code> set the minimal dimensions of
 * resizable window.
 * <li>Modal window can chose between two colors of frame.
 * <code>{@link #setCssClassName(String)}</code> sets the dialog css class,
 * possible values are <code>{@link #CSS_CLASS_BLUE}</code> for blue frame and
 * <code>{@link #CSS_CLASS_GRAY}</code> for gray frame.
 * <li>Mask (element that prevents user from interacting the rest of the page)
 * can be either transparent or semitransparent.
 * <code>{@link #setMaskType(ModalWindow.MaskType)}</code> alters this.
 * </ul>
 * 
 * @see IPageSettings#setAutomaticMultiWindowSupport(boolean)
 * @author Matej Knopp
 */
public class ModalWindow extends Panel
{
	private static final long serialVersionUID = 1L;

	private static ResourceReference JAVASCRIPT = new CompressedResourceReference(
			ModalWindow.class, "res/modal.js");

	private static ResourceReference CSS = new CompressedResourceReference(
			ModalWindow.class, "res/modal.css");

	/**
	 * Creates a new modal window component.
	 * 
	 * @param id
	 *            Id of component
	 */
	public ModalWindow(String id)
	{
		super(id);
		this.cookieName = "modal-window-" + hashCode();
		add(empty = new WebMarkupContainer(getContentId()));

		add(new CloseButtonBehavior());
		add(new WindowClosedBehavior());
		add(HeaderContributor.forJavaScript(JAVASCRIPT));
		add(HeaderContributor.forCss(CSS));
	}

	/**
	 * Interface for lazy page creation. The advantage of creating page using
	 * this interface over just passing a page instance is that page created in
	 * <code>{@link #createPage()}</code> will have the pagemap automatically
	 * set to the pagemap specified for <code>{@link ModalWindow}</code>.
	 * 
	 * @author Matej Knopp
	 */
	public static interface PageCreator extends Serializable
	{
		/**
		 * Creates a new instance of content page.
		 * 
		 * @return new page instance
		 */
		public Page createPage();
	}

	/**
	 * Callback for close button that contains a method that is invoked after
	 * the button has been clicked. If no callback instance is specified using
	 * <code>{@link #setCloseButtonCallback(ModalWindow.CloseButtonCallback)}</code>,
	 * no ajax request will be fired. Clicking the button will just close the
	 * window.
	 * 
	 * @author Matej Knopp
	 */
	public static interface CloseButtonCallback extends Serializable
	{
		/**
		 * Methods invoked after the button has been clicked. The invokation is
		 * done using an ajax call, so <code>{@link AjaxRequestTarget}</code>
		 * instance is available.
		 * 
		 * @param target
		 *            <code>{@link AjaxRequestTarget}</code> instance bound
		 *            with the ajax reuqest.
		 * 
		 * @return True if the window can be closed (will close the window),
		 *         false otherwise
		 */
		public boolean onCloseButtonClicked(AjaxRequestTarget target);
	}

	/**
	 * Callback called after the window has been closed. If no callback instance
	 * is specified using
	 * {@link ModalWindow#setWindowClosedCallback(ModalWindow.WindowClosedCallback)},
	 * no ajax request will be fired.
	 * 
	 * @author Matej Knopp
	 */
	public static interface WindowClosedCallback extends Serializable
	{
		/**
		 * Called after the window has been closed.
		 * 
		 * @param target
		 *            <code>{@link AjaxRequestTarget}</code> instance bound
		 *            with the ajax reuqest.
		 */
		public void onClose(AjaxRequestTarget target);
	}

	/**
	 * Sets the name of the page ma for the content page. This makes only sense
	 * when the content is a page, not a component and if wicket multiwindow
	 * support is turned on.
	 * 
	 * @param pageMapName
	 *            Name of the page map
	 */
	public void setPageMapName(String pageMapName)
	{
		this.pageMapName = pageMapName;
	}

	/**
	 * Returns the page map name.
	 * 
	 * @return The page map name.
	 */
	public String getPageMapName()
	{
		return pageMapName;
	}

	/**
	 * Sets the <code>{@link PageCreator}</code> instance. The instance is
	 * only used when no custom component has been added to the dialog.
	 * 
	 * @param creator
	 *            <code>{@link PageCreator}</code> instance
	 */
	public void setPageCreator(PageCreator creator)
	{
		this.pageCreator = creator;
	}

	/**
	 * Sets the <code>{@link CloseButtonCallback}</code> instance.
	 * 
	 * @param callback
	 *            Callback instance
	 */
	public void setCloseButtonCallback(CloseButtonCallback callback)
	{
		this.closeButtonCallback = callback;
	}

	/**
	 * Sets the <code>@{link {@link WindowClosedCallback}</code> instance.
	 * 
	 * @param callback
	 *            Callback instance
	 */
	public void setWindowClosedCallback(WindowClosedCallback callback)
	{
		this.windowClosedCallback = callback;
	}

	/**
	 * Shows the modal window.
	 * 
	 * @param target
	 *            Request target associated with current ajax request.
	 */
	public void show(AjaxRequestTarget target)
	{
		target.addComponent(this);
		target.appendJavascript(getWindowOpenJavascript());
		shown = true;
	}

	/**
	 * Hides the modal window.
	 * 
	 * @param target
	 *            Request target associated with current ajax request.
	 */
	public static final void close(AjaxRequestTarget target)
	{
		target.appendJavascript(getCloseJavacript());
	}

	/**
	 * @return javascript that closes current modal window
	 */
	private static String getCloseJavacript()
	{
		return "var win;\n" + "try {\n" + "	win = window.parent.Wicket.Window;\n"
				+ "} catch (ignore) {\n" + "}\n"
				+ "if (typeof(win) != \"undefined\" && typeof(win.current) != \"undefined\") {\n"
				+ "	window.parent.setTimeout(function() {\n" + "		win.current.close();\n"
				+ "	}, 0);\n" + "}";
	}

	/**
	 * Returns the id of content component.
	 * 
	 * <pre>
	 * ModalWindow window = new ModalWindow(parent, &quot;window&quot;);
	 * new MyPanel(window, window.getContentId());
	 * </pre>
	 * 
	 * @return Id of content component.
	 */
	public String getContentId()
	{
		return "content";
	}

	/**
	 * Sets the minimal width of window. This value is only used if the window
	 * is resizable. The width is specified in pixels and it is the width of
	 * entire window (including frame).
	 * 
	 * @param minimalWidth
	 *            Minimal window width.
	 */
	public void setMinimalWidth(int minimalWidth)
	{
		this.minimalWidth = minimalWidth;
	}

	/**
	 * Returns the minimal width of window (in pixels).
	 * 
	 * @return Minimal width of window
	 */
	public int getMinimalWidth()
	{
		return minimalWidth;
	}

	/**
	 * Sets the minimal height of window. This value is only used if window is
	 * resizable. The height is specified in pixels and it is the height of
	 * window content (without frame).
	 * 
	 * @param minimalHeight
	 *            Minimal height
	 */
	public void setMinimalHeight(int minimalHeight)
	{
		this.minimalHeight = minimalHeight;
	}

	/**
	 * Returns the minimal height of window (in pixels).
	 * 
	 * @return Minimal height of window
	 */
	public int getMinimalHeight()
	{
		return minimalHeight;
	}

	/**
	 * CSS class for window with blue border.
	 */
	public final static String CSS_CLASS_BLUE = "w_blue";

	/**
	 * CSS class for window with gray border.
	 */
	public final static String CSS_CLASS_GRAY = "w_silver";

	/**
	 * Sets the CSS class name for this window. This class affects the look of
	 * window frame. Possible values (if you don't make your style sheet) are
	 * <code>{@link #CSS_CLASS_BLUE}</code> and
	 * <code>{@link #CSS_CLASS_GRAY}</code>.
	 * 
	 * @param cssClassName
	 */
	public void setCssClassName(String cssClassName)
	{
		this.cssClassName = cssClassName;
	}

	/**
	 * Returns the CSS class name for this window.
	 * 
	 * @return CSS class name
	 */
	public String getCssClassName()
	{
		return cssClassName;
	}

	/**
	 * Sets the initial width of the window. The width refers to the width of
	 * entire window (including frame). If the window is resizable, the width
	 * unit is always "px". If the window is not resizable, the unit can be
	 * specified using {@link #setWidthUnit(String)}. If cookie name is set and
	 * window is resizable, the initial width may be ignored in favor of width
	 * stored in cookie.
	 * 
	 * @param initialWidth
	 *            Initial width of the window
	 */
	public void setInitialWidth(int initialWidth)
	{
		this.initialWidth = initialWidth;
	}

	/**
	 * Returns the initial width of the window.
	 * 
	 * @return Initial height of the window
	 */
	public int getInitialWidth()
	{
		return initialWidth;
	}

	/**
	 * Sets the initial height of the window. The height refers to the height of
	 * window content (without frame). If the window is resizable, the height
	 * unit is always "px". If the window is not resizable, the unit can be
	 * specified using {@link #setHeightUnit(String)}. If cookie name is set
	 * and window is resizable, the initial height may be ignred in favor of
	 * height stored in cookie.
	 * 
	 * @param initialHeight
	 *            Initial height of the window
	 */
	public void setInitialHeight(int initialHeight)
	{
		this.initialHeight = initialHeight;
	}

	/**
	 * Returns the initial height of the window.
	 * 
	 * @return Initial height of the window
	 */
	public int getInitialHeight()
	{
		return initialHeight;
	}

	/**
	 * Sets whether to use initial height or preserve the real content height.
	 * This can only be used if the content is a component (not a page) and the
	 * window is not resizable.
	 * 
	 * @param useInitialHeight
	 *            Whether to use initial height instead of preserving content
	 *            height instead of using initial height
	 */
	public void setUseInitialHeight(boolean useInitialHeight)
	{
		this.useInitialHeight = useInitialHeight;
	}

	/**
	 * Returns true if the initial height should be used (in favour of
	 * preserving real content height).
	 * 
	 * @return True if initial height should be used, false is real content
	 *         height should be preserved (valid only if the window is not
	 *         resizable and the content is a component (not a page)
	 */
	public boolean isUseInitialHeight()
	{
		return useInitialHeight;
	}

	/**
	 * Sets whether the user will be able to resize the window.
	 * 
	 * @param resizable
	 *            Whether the window is resizable
	 */
	public void setResizable(boolean resizable)
	{
		this.resizable = resizable;
	}

	/**
	 * Returns whether the window is resizable.
	 * 
	 * @return True if the window is resizable, false otherwise
	 */
	public boolean isResizable()
	{
		return resizable;
	}

	/**
	 * Sets the CSS unit used for initial window width. This is only applicable
	 * when the window is not resizable.
	 * 
	 * @param widthUnit
	 *            CSS unit for initial window width.
	 */
	public void setWidthUnit(String widthUnit)
	{
		this.widthUnit = widthUnit;
	}

	/**
	 * Returns the CSS unit for initial window width.
	 * 
	 * @return CSS unit for initial window width.
	 */
	public String getWidthUnit()
	{
		return widthUnit;
	}

	/**
	 * Sets the CSS unit used for initial window height. This is only applicable
	 * when the window is not resizable.
	 * 
	 * @param heightUnit
	 *            CSS unit for initial window height.
	 */
	public void setHeightUnit(String heightUnit)
	{
		this.heightUnit = heightUnit;
	}

	/**
	 * Retrns the CSS unit for initial window height.
	 * 
	 * @return CSS unit for initial window height.
	 */
	public String getHeightUnit()
	{
		return heightUnit;
	}

	/**
	 * Sets the name of the cookie that is used to remeber window position (and
	 * size if the window is resizable).
	 * 
	 * @param cookieName
	 *            Name of the cookie
	 */
	public void setCookieName(String cookieName)
	{
		this.cookieName = cookieName;
	}

	/**
	 * Returns the name of cookie that is used to remebember window position
	 * (and size if the window is resizable).
	 * 
	 * @return Name of the cookie
	 */
	public String getCookieName()
	{
		return cookieName;
	}

	/**
	 * Sets the title of window. If the window is a page, title can be
	 * <code>null</code>. In that case it will display the title document
	 * inside the window.
	 * 
	 * @param title
	 *            Title of the window
	 */
	public void setTitle(String title)
	{
		this.title = title;
	}

	/**
	 * Returns the title of the window.
	 * 
	 * @return Title of the window
	 */
	public String getTitle()
	{
		return title;
	}

	/**
	 * Mask is the element behind the window, that prevents user from
	 * interacting the rest of page. Mask can be either
	 * <ul>
	 * <li><code>{@link #TRANSPARENT}</code> - the mask is invisible
	 * <li><code>{@link #SEMI_TRANSPARENT}</code> - the mask is black with
	 * small opacity (10%)
	 * </ul>
	 * 
	 * @author Matej Knopp
	 */
	public static final class MaskType extends EnumeratedType
	{

		private static final long serialVersionUID = 1L;

		/**
		 * Transparent mask (not visible).
		 */
		public static final MaskType TRANSPARENT = new MaskType("TRANSPARENT");

		/**
		 * Visible mask (black with low opacity).
		 */
		public static final MaskType SEMI_TRANSPARENT = new MaskType("SEMI_TRANSPARENT");

		/**
		 * Constructor.
		 * 
		 * @param name
		 */
		public MaskType(String name)
		{
			super(name);
		}
	};

	/**
	 * Sets the mask type of the window.
	 * 
	 * @param mask
	 *            The mask type
	 */
	public void setMaskType(MaskType mask)
	{
		this.maskType = mask;
	}

	/**
	 * Returns the mask type of the window
	 * 
	 * @return The mask type
	 */
	public MaskType getMaskType()
	{
		return maskType;
	}

	/**
	 * Creates the page.
	 * 
	 * @return Page instance or null if page couldn't be created.
	 */
	private Page createPage()
	{
		if (pageCreator == null)
		{
			return null;
		}
		else
		{
			RequestParameters parameters = RequestCycle.get().getRequest().getRequestParameters();
			String oldPageMapName = parameters.getPageMapName();

			// if there is a pagemap name specified and multiwindow support is
			// on
			if (getPageMapName() != null
					&& Application.get().getPageSettings().getAutomaticMultiWindowSupport() == true)
			{
				// try to find out whether the pagemap already exists
				Session session = Session.get();
				if (session.pageMapForName(getPageMapName(), false) == null)
				{
					deletePageMap = true;
				}
				parameters.setPageMapName(getPageMapName());
			}
			try
			{
				Page page = pageCreator.createPage();
				return page;
			}
			finally
			{
				parameters.setPageMapName(oldPageMapName);
			}
		}
	}

	/**
	 * @see wicket.Component#onAttach()
	 */
	protected void onAttach()
	{
		getContent().setOutputMarkupId(true);
		getContent().setVisible(shown);
	}

	/**
	 * @see wicket.markup.html.panel.Panel#onComponentTag(wicket.markup.ComponentTag)
	 */
	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);
		tag.put("style", "display:none");
	}


	/**
	 * Returns a content component. In case user haven't specified any content
	 * component, it returns an empty WebMarkupContainer.
	 * 
	 * @return Content component
	 */
	private Component getContent()
	{
		return get(getContentId());
	}

	/**
	 * Returns true if user has added own component to the window.
	 * 
	 * @return True if user has added own component to the window, false
	 *         otherwise.
	 */
	private boolean isCustomComponent()
	{
		return getContent() != empty;
	}

	/**
	 * @see wicket.MarkupContainer#remove(wicket.Component)
	 */
	public void remove(Component component)
	{
		super.remove(component);
		if (component.getId().equals(getContentId()))
		{
			add(empty = new WebMarkupContainer(getContentId()));
		}
	}

	/**
	 * Sets the content of the modal window.
	 * 
	 * @param component
	 */
	public void setContent(Component component)
	{
		if (component.getId().equals(getContentId()) == false)
		{
			throw new WicketRuntimeException("Modal window content id is wrong.");
		}
		replace(component);
	}

	/**
	 * @author Matej Knopp
	 */
	private class WindowClosedBehavior extends AbstractDefaultAjaxBehavior
	{
		private static final long serialVersionUID = 1L;

		protected void respond(AjaxRequestTarget target)
		{
			shown = false;

			// should we cleanup the pagemap?
			if (deletePageMap == true)
			{
				// get the pagemap
				Session session = Session.get();
				PageMap pageMap = session.pageMapForName(getPageMapName(), false);

				// if there is any remove it
				if (pageMap != null)
				{
					session.removePageMap(pageMap);
					deletePageMap = false;
				}
			}

			if (windowClosedCallback != null)
			{
				windowClosedCallback.onClose(target);
			}
		}

		protected CharSequence getCallbackScript()
		{
			return super.getCallbackScript();
		}
	};

	/**
	 * @author Matej Knopp
	 */
	private class CloseButtonBehavior extends AbstractDefaultAjaxBehavior
	{
		private static final long serialVersionUID = 1L;

		protected void respond(AjaxRequestTarget target)
		{
			if (closeButtonCallback == null
					|| closeButtonCallback.onCloseButtonClicked(target) == true)
			{
				target.appendJavascript("Wicket.Window.get().close();");
			}
		}

		protected IAjaxCallDecorator getAjaxCallDecorator()
		{
			return new CancelEventIfNoAjaxDecorator(super.getAjaxCallDecorator());
		}

		protected CharSequence getCallbackScript()
		{
			return super.getCallbackScript();
		}
	}

	/**
	 * Returns the markup id of the component.
	 * 
	 * @return component id
	 */
	private String getContentMarkupId()
	{
		return getContent().getMarkupId();
	}

	/**
	 * Replaces all occurences of " in string with \".
	 * 
	 * @param string
	 *            String to be escaped.
	 * 
	 * @return escaped string
	 */
	private String escapeQuotes(String string)
	{
		if (string.indexOf('"') != -1)
		{
			string = Strings.replaceAll(string, "\"", "\\\"").toString();
		}
		return string;
	}

	/**
	 * Returns the javascript used to open the window.
	 * 
	 * @return javascript that opens the window
	 */
	private String getWindowOpenJavascript()
	{
		AppendingStringBuffer buffer = new AppendingStringBuffer();

		if (isCustomComponent() == true)
		{
			buffer.append("var element = document.getElementById(\"" + getContentMarkupId()
					+ "\");\n");
		}

		buffer.append("var settings = new Object();\n");
		buffer.append("settings.minWidth=" + getMinimalWidth() + ";\n");
		buffer.append("settings.minHeight=" + getMinimalHeight() + ";\n");
		buffer.append("settings.className=\"" + getCssClassName() + "\";\n");
		buffer.append("settings.width=\"" + getInitialWidth() + "\";\n");

		if (isUseInitialHeight() == true || isCustomComponent() == false)
			buffer.append("settings.height=\"" + getInitialHeight() + "\";\n");
		else
			buffer.append("settings.height=null;\n");

		buffer.append("settings.resizable=" + Boolean.toString(isResizable()) + ";\n");

		if (isResizable() == false)
		{
			buffer.append("settings.widthUnit=\"" + getWidthUnit() + "\";\n");
			buffer.append("settings.heightUnit=\"" + getHeightUnit() + "\";\n");
		}

		if (isCustomComponent() == false)
		{
			Page page = createPage();
			if (page == null)
			{
				throw new WicketRuntimeException("Error creating page for modal dialog.");
			}
			buffer.append("settings.src=\"" + RequestCycle.get().urlFor(page) + "\";\n");

			if (getPageMapName() != null)
			{
				buffer.append("settings.iframeName=\"" + getPageMapName() + "\";\n");
			}
		}
		else
		{
			buffer.append("settings.element = element;\n");
		}

		if (getCookieName() != null)
		{
			buffer.append("settings.cookieId=\"" + getCookieName() + "\";\n");
		}

		if (getTitle() != null)
		{
			buffer.append("settings.title=\"" + escapeQuotes(getTitle()) + "\";\n");
		}

		if (getMaskType() == MaskType.TRANSPARENT)
		{
			buffer.append("settings.mask=\"transparent\";\n");
		}
		else if (getMaskType() == MaskType.SEMI_TRANSPARENT)
		{
			buffer.append("settings.mask=\"semi-transparent\";\n");
		}

		if (closeButtonCallback != null)
		{
			CloseButtonBehavior behavior = (CloseButtonBehavior)getBehaviors(
					CloseButtonBehavior.class).get(0);
			buffer.append("settings.onCloseButton = function() { " + behavior.getCallbackScript()
					+ "};\n");
		}

		WindowClosedBehavior behavior = (WindowClosedBehavior)getBehaviors(
				WindowClosedBehavior.class).get(0);
		buffer.append("settings.onClose = function() { " + behavior.getCallbackScript() + " };\n");

		buffer.append("Wicket.Window.create(settings).show();\n");

		return buffer.toString();
	}


	private boolean deletePageMap = false;
	private boolean shown = false;

	// empty container - used when no component is added
	private WebMarkupContainer empty;

	private int minimalWidth = 200;
	private int minimalHeight = 200;
	private String cssClassName = CSS_CLASS_BLUE;
	private int initialWidth = 600;
	private int initialHeight = 400;
	private boolean useInitialHeight = true;
	private boolean resizable = true;
	private String widthUnit = "px";
	private String heightUnit = "px";
	private String cookieName;
	private String title = null;
	private MaskType maskType = MaskType.SEMI_TRANSPARENT;

	private String pageMapName = "modal-dialog-pagemap";

	private PageCreator pageCreator = null;
	private CloseButtonCallback closeButtonCallback = null;
	private WindowClosedCallback windowClosedCallback = null;
}
