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
package org.apache.wicket.extensions.ajax.markup.html.modal;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.AbstractRepeater;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.resource.CoreLibrariesContributor;
import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.util.lang.EnumeratedType;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.Strings;

/**
 * Modal window component.
 * <p>
 * Modal window is a draggable window (with either div or iframe content) that prevent user from
 * interacting the rest of page (using a mask) until the window is closed.
 * <p>
 * If you want this to work under IE, don't attach this component to a &lt;span&gt; tag, make sure
 * you use a &lt;div&gt;.
 * <p>
 * The window is draggable and optionally resizable. The content can be either
 * <ul>
 * <li><b>a component</b> - you need to add the component to modal window (with id obtained using
 * <code>{@link #getContentId()}</code>, or
 * <li><b>a page</b> - you need to pass a <code>{@link PageCreator}</code> instance to a
 * <code>{@link #setPageCreator(ModalWindow.PageCreator)}</code> method.
 * </ul>
 * In case the content is a component, it is not rendered until the window is shown (method
 * <code>{@link #show(org.apache.wicket.ajax.AjaxRequestTarget)})</code>. The window can be made
 * visible from an ajax handler using
 * <code>{@link #show(org.apache.wicket.ajax.AjaxRequestTarget)}</code>.
 * <p>
 * To close the window there are multiple options. Static method
 * <code>{@link #close(org.apache.wicket.ajax.AjaxRequestTarget)}</code> can be used to close the
 * window from a handler of ajax link inside the window. By default the close button in the upper
 * right corner of the window closes it. This behavior can be altered using
 * <code>{@link #setCloseButtonCallback(ModalWindow.CloseButtonCallback)}</code>. If you want to be
 * notified when the window is closed (either using the close button or calling
 * <code>{@link #close(org.apache.wicket.ajax.AjaxRequestTarget)})</code>, you can use
 * <code>{@link #setWindowClosedCallback(ModalWindow.WindowClosedCallback)}</code>.
 * <p>
 * Title is specified using {@link #setTitle(String)}. If the content is a page (iframe), the title
 * can remain unset, in that case title from the page inside window will be shown.
 * <p>
 * There are several options to specify the visual properties of the window. In all methods where
 * size is expected, width refers to width of entire window (including frame), height refers to the
 * height of window content (without frame).
 * <p>
 * <ul>
 * <li><code>{@link #setResizable(boolean)}</code> specifies, whether the window can be resized.
 * <li><code>{@link #setInitialWidth(int)}</code> and <code>{@link #setInitialHeight(int)}</code>
 * specify the initial width and height of window. If the window is resizable, the unit of these
 * dimensions is always "px". If the window is not resizable, the unit can be specified using
 * <code>{@link #setWidthUnit(String)}</code> and <code>{@link #setHeightUnit(String)}</code>. If
 * the window is not resizable and the content is a component (not a page), the initial height value
 * can be ignored and the actual height can be determined from the height of the content. To enable
 * this behavior use <code>{@link #setUseInitialHeight(boolean)}</code>.
 * <li>The window position (and size if the window is resizable) can be stored in a cookie, so that
 * it is preserved when window is close. The name of the cookie is specified via
 * <code>{@link #setCookieName(String)}</code>. If the name is <code>null</code>, position is not
 * stored (initial width and height are always used). Default cookie name is null (position is not
 * stored).
 * <li><code>{@link #setMinimalWidth(int)}</code> and <code>{@link #setMinimalHeight(int)}</code>
 * set the minimal dimensions of resizable window.
 * <li><code>{@link #setAutoSize(boolean)}</code> sets whether window size will be automatically
 * adjusted on opening to fit content's width and height. Default is false.<span
 * style="text-decoration: underline"> Doesn't work on IE 6.</span></li>
 * <li>Modal window can chose between two colors of frame.
 * <code>{@link #setCssClassName(String)}</code> sets the dialog css class, possible values are
 * <code>{@link #CSS_CLASS_BLUE}</code> for blue frame and <code>{@link #CSS_CLASS_GRAY}</code> for
 * gray frame.
 * <li>Mask (element that prevents user from interacting the rest of the page) can be either
 * transparent or semitransparent. <code>{@link #setMaskType(ModalWindow.MaskType)}</code> alters
 * this.
 * </ul>
 * Also it is recommended to put the modal window component in markup before any component (i.e.
 * AjaxLink or AjaxButton) that shows it.
 * <p>
 * If you want to use form in modal window component make sure that you put the modal window itself
 * in another form (nesting forms is legal in Wicket) and that the form on modal window is submitted
 * before the window get closed.
 * 
 * @author Matej Knopp
 */
public class ModalWindow extends Panel
{
	private static final long serialVersionUID = 1L;

	/** CSS class for window with blue border. */
	public final static String CSS_CLASS_BLUE = "w_blue";

	/** CSS class for window with gray border. */
	public final static String CSS_CLASS_GRAY = "w_silver";

	private static final ResourceReference JAVASCRIPT = new JavaScriptResourceReference(
		ModalWindow.class, "res/modal.js");

	private static final ResourceReference CSS = new CssResourceReference(ModalWindow.class,
		"res/modal.css");

	/** the default id of the content component */
	public static final String CONTENT_ID = "content";

	/** True while the ModalWindows is showing */
	private boolean shown = false;

	/** empty container - used when no component is added */
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
	private IModel<String> title = null;
	private MaskType maskType = MaskType.SEMI_TRANSPARENT;
	private boolean autoSize = false;
	private boolean unloadConfirmation = true;

	private PageCreator pageCreator = null;
	private CloseButtonCallback closeButtonCallback = null;
	private WindowClosedCallback windowClosedCallback = null;

	/**
	 * Interface for lazy page creation. The advantage of creating page using this interface over
	 * just passing a page instance is that page created in <code>{@link #createPage()}</code> will
	 * have the pagemap automatically set to the pagemap specified for
	 * <code>{@link ModalWindow}</code>.
	 * 
	 * @author Matej Knopp
	 */
	public static interface PageCreator extends IClusterable
	{
		/**
		 * Creates a new instance of content page.
		 * 
		 * @return new page instance
		 */
		public Page createPage();
	}

	/**
	 * Callback for close button that contains a method that is invoked after the button has been
	 * clicked. If no callback instance is specified using
	 * <code>{@link ModalWindow#setCloseButtonCallback(ModalWindow.CloseButtonCallback)}</code>, no
	 * ajax request will be fired. Clicking the button will just close the window.
	 * 
	 * @author Matej Knopp
	 */
	public static interface CloseButtonCallback extends IClusterable
	{
		/**
		 * Methods invoked after the button has been clicked. The invocation is done using an ajax
		 * call, so <code>{@link org.apache.wicket.ajax.AjaxRequestTarget}</code> instance is
		 * available.
		 * 
		 * @param target
		 *            <code>{@link org.apache.wicket.ajax.AjaxRequestTarget}</code> instance bound
		 *            with the ajax request.
		 * 
		 * @return True if the window can be closed (will close the window), false otherwise
		 */
		public boolean onCloseButtonClicked(AjaxRequestTarget target);
	}

	/**
	 * Callback called after the window has been closed. If no callback instance is specified using
	 * {@link ModalWindow#setWindowClosedCallback(ModalWindow.WindowClosedCallback)}, no ajax
	 * request will be fired.
	 * 
	 * @author Matej Knopp
	 */
	public static interface WindowClosedCallback extends IClusterable
	{
		/**
		 * Called after the window has been closed.
		 * 
		 * @param target
		 *            <code>{@link org.apache.wicket.ajax.AjaxRequestTarget}</code> instance bound
		 *            with the ajax request.
		 */
		public void onClose(AjaxRequestTarget target);
	}

	/**
	 * Creates a new modal window component.
	 * 
	 * @param id
	 *            Id of component
	 */
	public ModalWindow(final String id)
	{
		super(id);
		init();
	}

	/**
	 * Creates a new modal window component.
	 * 
	 * @param id
	 *            Id of component
	 * @param model
	 *            Model
	 */
	public ModalWindow(final String id, final IModel<?> model)
	{
		super(id, model);
		init();
	}

	/**
	 * Initialize
	 */
	private void init()
	{
		setVersioned(false);
		cookieName = null;

		add(empty = new WebMarkupContainer(getContentId()));

		add(newCloseButtonBehavior());
		add(new WindowClosedBehavior());

		// install a default callback that will force
		// WindowClosedBehavior to be executed
		setWindowClosedCallback(new WindowClosedCallback()
		{
			@Override
			public void onClose(AjaxRequestTarget target)
			{
				// noop
			}
		});

	}

	@Override
	public void renderHead(final IHeaderResponse response)
	{
		super.renderHead(response);

		CoreLibrariesContributor.contributeAjax(getApplication(), response);
		response.render(JavaScriptHeaderItem.forReference(JAVASCRIPT));

		ResourceReference cssResource = newCssResource();
		if (cssResource != null)
		{
			response.render(CssHeaderItem.forReference(cssResource));
		}
	}

	/**
	 * Allows to override CSS contribution. Returning null means the CSS will be contributed via
	 * other sources, e.g. a global CSS resource.
	 * 
	 * @return The CSS resource reference or null if CSS is contributed via other means.
	 * @see #setCssClassName(String)
	 */
	protected ResourceReference newCssResource()
	{
		return CSS;
	}

	/**
	 * Is this window currently showing.
	 * 
	 * @return the shown
	 */
	public boolean isShown()
	{
		return shown;
	}


	/**
	 * Sets the <code>{@link PageCreator}</code> instance. The instance is only used when no custom
	 * component has been added to the dialog.
	 * 
	 * @param creator
	 *            <code>{@link PageCreator}</code> instance
	 * @return this
	 */
	public ModalWindow setPageCreator(final PageCreator creator)
	{
		setContent(empty);
		pageCreator = creator;
		return this;
	}

	/**
	 * Sets the <code>{@link CloseButtonCallback}</code> instance.
	 * 
	 * @param callback
	 *            Callback instance
	 * @return this
	 */
	public ModalWindow setCloseButtonCallback(final CloseButtonCallback callback)
	{
		closeButtonCallback = callback;
		return this;
	}

	/**
	 * Sets the <code>@{link {@link WindowClosedCallback}</code> instance.
	 * 
	 * @param callback
	 *            Callback instance
	 * @return this
	 */
	public ModalWindow setWindowClosedCallback(final WindowClosedCallback callback)
	{
		windowClosedCallback = callback;
		return this;
	}

	/**
	 * Shows the modal window.
	 * 
	 * @param target
	 *            Request target associated with current ajax request.
	 */
	public void show(final AjaxRequestTarget target)
	{
		if (shown == false)
		{
			getContent().setVisible(true);
			target.add(this);
			target.appendJavaScript(getWindowOpenJavaScript());
			shown = true;
		}
	}

	/**
	 * Hides the modal window. This can be called from within the modal window, however, the modal
	 * window must have configured WindowClosedCallback. Otherwise use the
	 * {@link #close(org.apache.wicket.ajax.AjaxRequestTarget)} method.
	 * 
	 * @param target
	 *            Request target associated with current ajax request.
	 */
	public static final void closeCurrent(final AjaxRequestTarget target)
	{
		target.appendJavaScript(getCloseJavacriptInternal());
	}

	/**
	 * Closes the modal window.
	 * 
	 * @param target
	 *            Request target associated with current ajax request.
	 */
	public void close(final AjaxRequestTarget target)
	{
		getContent().setVisible(false);
		if (isCustomComponent())
		{
			target.add(getContent());
		}
		target.appendJavaScript(getCloseJavacript());
		shown = false;
	}

	/**
	 * Method that allows alternate script for showing the window.
	 * 
	 * @return the script that actually shows the window.
	 */
	protected CharSequence getShowJavaScript()
	{
		return "window.setTimeout(function(){\n" + "  Wicket.Window.create(settings).show();\n"
			+ "}, 0);\n";
	}

	private static String getCloseJavacriptInternal()
	{
		return "var win;\n" //
			+ "try {\n" + "	win = window.parent.Wicket.Window;\n"
			+ "} catch (ignore) {\n"
			+ "}\n"
			+ "if (typeof(win) == \"undefined\" || typeof(win.current) == \"undefined\") {\n"
			+ "  try {\n" + "     win = window.Wicket.Window;\n"
			+ "  } catch (ignore) {\n"
			+ "  }\n"
			+ "}\n"
			+ "if (win && win.current) {\n"
			+ " var close = function(w) { w.setTimeout(function() {\n"
			+ "		win.current.close();\n"
			+ "	}, 0);  };\n"
			+ "	try { close(window.parent); } catch (ignore) { close(window); }\n" + "}";
	}

	/**
	 * Method that allows alternate script for closing the window.
	 * 
	 * @return the script that actually closes the window.
	 */
	protected String getCloseJavacript()
	{
		return getCloseJavacriptInternal();
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
		return CONTENT_ID;
	}

	/**
	 * Sets the minimal width of window. This value is only used if the window is resizable. The
	 * width is specified in pixels and it is the width of entire window (including frame).
	 * 
	 * @param minimalWidth
	 *            Minimal window width.
	 * @return this
	 */
	public ModalWindow setMinimalWidth(final int minimalWidth)
	{
		this.minimalWidth = minimalWidth;
		return this;
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
	 * Sets the minimal height of window. This value is only used if window is resizable. The height
	 * is specified in pixels and it is the height of window content (without frame).
	 * 
	 * @param minimalHeight
	 *            Minimal height
	 * @return this
	 */
	public ModalWindow setMinimalHeight(final int minimalHeight)
	{
		this.minimalHeight = minimalHeight;
		return this;
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
	 * Sets the CSS class name for this window. This class affects the look of window frame.
	 * Possible values (if you don't make your style sheet) are <code>{@link #CSS_CLASS_BLUE}</code>
	 * and <code>{@link #CSS_CLASS_GRAY}</code>.
	 * 
	 * @param cssClassName
	 * @return this
	 * @see #newCssResource()
	 */
	public ModalWindow setCssClassName(final String cssClassName)
	{
		this.cssClassName = cssClassName;
		return this;
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
	 * Sets the initial width of the window. The width refers to the width of entire window
	 * (including frame). If the window is resizable, the width unit is always "px". If the window
	 * is not resizable, the unit can be specified using {@link #setWidthUnit(String)}. If cookie
	 * name is set and window is resizable, the initial width may be ignored in favor of width
	 * stored in cookie.
	 * 
	 * @param initialWidth
	 *            Initial width of the window
	 * @return this
	 */
	public ModalWindow setInitialWidth(final int initialWidth)
	{
		this.initialWidth = initialWidth;
		return this;
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
	 * Sets the initial height of the window. The height refers to the height of window content
	 * (without frame). If the window is resizable, the height unit is always "px". If the window is
	 * not resizable, the unit can be specified using {@link #setHeightUnit(String)}. If cookie name
	 * is set and window is resizable, the initial height may be ignored in favor of height stored
	 * in cookie.
	 * 
	 * @param initialHeight
	 *            Initial height of the window
	 * @return this
	 */
	public ModalWindow setInitialHeight(final int initialHeight)
	{
		this.initialHeight = initialHeight;
		return this;
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
	 * Sets whether to use initial height or preserve the real content height. This can only be used
	 * if the content is a component (not a page) and the window is not resizable.
	 * 
	 * @param useInitialHeight
	 *            Whether to use initial height instead of preserving content height instead of
	 *            using initial height
	 * @return this
	 */
	public ModalWindow setUseInitialHeight(final boolean useInitialHeight)
	{
		this.useInitialHeight = useInitialHeight;
		return this;
	}

	/**
	 * Returns true if the initial height should be used (in favor of preserving real content
	 * height).
	 * 
	 * @return True if initial height should be used, false is real content height should be
	 *         preserved (valid only if the window is not resizable and the content is a component
	 *         (not a page)
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
	 * @return this
	 */
	public ModalWindow setResizable(final boolean resizable)
	{
		this.resizable = resizable;
		return this;
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
	 * Sets a flag whether to ask the user before leaving the page.
	 * 
	 * @param unloadConfirmation
	 *            a flag whether to ask the user before leaving the page
	 * @return {@code this} instance, for chaining
	 */
	public ModalWindow showUnloadConfirmation(final boolean unloadConfirmation)
	{
		this.unloadConfirmation = unloadConfirmation;
		return this;
	}

	/**
	 * Returns whether the user should be asked before leaving the page.
	 * 
	 * @return {@code true} if the user should be asked if the last action causes leaving the page,
	 *         {@code false} otherwise
	 */
	public boolean showUnloadConfirmation()
	{
		return unloadConfirmation;
	}

	/**
	 * Sets the CSS unit used for initial window width. This is only applicable when the window is
	 * not resizable.
	 * 
	 * @param widthUnit
	 *            CSS unit for initial window width.
	 * @return this
	 */
	public ModalWindow setWidthUnit(final String widthUnit)
	{
		this.widthUnit = widthUnit;
		return this;
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
	 * Sets the CSS unit used for initial window height. This is only applicable when the window is
	 * not resizable.
	 * 
	 * @param heightUnit
	 *            CSS unit for initial window height.
	 * @return this
	 */
	public ModalWindow setHeightUnit(final String heightUnit)
	{
		this.heightUnit = heightUnit;
		return this;
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
	 * Sets the name of the cookie that is used to remember window position (and size if the window
	 * is resizable).
	 * 
	 * @param cookieName
	 *            Name of the cookie
	 * @return this
	 */
	public ModalWindow setCookieName(final String cookieName)
	{
		if ((cookieName != null) && (cookieName.contains(",") || cookieName.contains("|")))
		{
			throw new IllegalArgumentException("Cookie name may not contain ',' or '|' characters.");
		}
		this.cookieName = cookieName;
		return this;
	}

	/**
	 * Returns the name of cookie that is used to remember window position (and size if the window
	 * is resizable).
	 * 
	 * @return Name of the cookie
	 */
	public String getCookieName()
	{
		return cookieName;
	}

	/**
	 * Sets the title of window. If the window is a page, title can be <code>null</code>. In that
	 * case it will display the title document inside the window.
	 * 
	 * @param title
	 *            Title of the window
	 * @return this
	 */
	public ModalWindow setTitle(final String title)
	{
		this.title = new Model<String>(title);
		return this;
	}

	/**
	 * Sets the title of window. If the window is a page, title can be <code>null</code>. In that
	 * case it will display the title document inside the window.
	 * 
	 * @param title
	 *            Title of the window
	 * @return this
	 */
	public ModalWindow setTitle(IModel<String> title)
	{
		title = wrap(title);
		this.title = title;
		return this;
	}

	/**
	 * Returns the title of the window.
	 * 
	 * @return Title of the window
	 */
	public IModel<String> getTitle()
	{
		return title;
	}

	/**
	 * Mask is the element behind the window, that prevents user from interacting the rest of page.
	 * Mask can be either
	 * <ul>
	 * <li><code>{@link #TRANSPARENT}</code> - the mask is invisible
	 * <li><code>{@link #SEMI_TRANSPARENT}</code> - the mask is black with small opacity (10%)
	 * </ul>
	 * 
	 * @author Matej Knopp
	 */
	public static final class MaskType extends EnumeratedType
	{
		private static final long serialVersionUID = 1L;

		/** Transparent mask (not visible). */
		public static final MaskType TRANSPARENT = new MaskType("TRANSPARENT");

		/** Visible mask (black with low opacity). */
		public static final MaskType SEMI_TRANSPARENT = new MaskType("SEMI_TRANSPARENT");

		/**
		 * Constructor.
		 * 
		 * @param name
		 */
		public MaskType(final String name)
		{
			super(name);
		}
	}

	/**
	 * Sets the mask type of the window.
	 * 
	 * @param mask
	 *            The mask type
	 * @return this
	 */
	public ModalWindow setMaskType(final MaskType mask)
	{
		maskType = mask;
		return this;
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
			return pageCreator.createPage();
		}
	}

	/**
	 * @see org.apache.wicket.Component#onBeforeRender()
	 */
	@Override
	protected void onBeforeRender()
	{
		shown = makeContentVisible();

		getContent().setOutputMarkupId(true);
		getContent().setVisible(shown);

		super.onBeforeRender();
	}

	/**
	 * You may subclass this method in case you don't want to show up the window on normal page
	 * refresh.
	 * 
	 * @return true, if the window shall be shown
	 */
	protected boolean makeContentVisible()
	{
		// if user is refreshing whole page, the window will not be shown
		if (getWebRequest().isAjax() == false)
		{
			return false;
		}
		else
		{
			return shown;
		}
	}

	/**
	 * @see org.apache.wicket.markup.html.panel.Panel#onComponentTag(org.apache.wicket.markup.ComponentTag)
	 */
	@Override
	protected void onComponentTag(final ComponentTag tag)
	{
		super.onComponentTag(tag);
		tag.put("style", "display:none");
	}

	/**
	 * Returns a content component. In case user haven't specified any content component, it returns
	 * an empty WebMarkupContainer.
	 * 
	 * @return Content component
	 */
	protected final Component getContent()
	{
		return get(getContentId());
	}

	/**
	 * Returns true if user has added own component to the window.
	 * 
	 * @return True if user has added own component to the window, false otherwise.
	 */
	protected boolean isCustomComponent()
	{
		return getContent() != empty;
	}

	/**
	 * @see org.apache.wicket.MarkupContainer#remove(org.apache.wicket.Component)
	 */
	@Override
	public ModalWindow remove(final Component component)
	{
		super.remove(component);
		if (component.getId().equals(getContentId()))
		{
			add(empty = new WebMarkupContainer(getContentId()));
		}

		return this;
	}

	/**
	 * Sets the content of the modal window.
	 * 
	 * @param component
	 * @return this;
	 */
	public ModalWindow setContent(final Component component)
	{
		if (component.getId().equals(getContentId()) == false)
		{
			throw new WicketRuntimeException("Modal window content id is wrong. Component ID:" +
				component.getId() + "; content ID: " + getContentId());
		}
		else if (component instanceof AbstractRepeater)
		{
			throw new WicketRuntimeException(
				"A repeater component cannot be used as the content of a modal window, please use repeater's parent");
		}

		component.setOutputMarkupPlaceholderTag(true);
		component.setVisible(false);
		replace(component);
		shown = false;
		pageCreator = null;
		return this;
	}

	/**
	 * @author Matej Knopp
	 */
	private class WindowClosedBehavior extends AbstractDefaultAjaxBehavior
	{
		private static final long serialVersionUID = 1L;

		@Override
		protected void respond(final AjaxRequestTarget target)
		{
			shown = false;

			if (windowClosedCallback != null)
			{
				windowClosedCallback.onClose(target);
			}
		}

		@Override
		public CharSequence getCallbackScript()
		{
			return super.getCallbackScript();
		}
	}

	/**
	 * @author Matej Knopp
	 */
	protected class CloseButtonBehavior extends AbstractDefaultAjaxBehavior
	{
		private static final long serialVersionUID = 1L;

		public CloseButtonBehavior()
		{
		}

		@Override
		protected final void respond(final AjaxRequestTarget target)
		{
			if ((closeButtonCallback == null) ||
				(closeButtonCallback.onCloseButtonClicked(target) == true))
			{
				close(target);
			}
		}

		/**
		 * @see org.apache.wicket.ajax.AbstractDefaultAjaxBehavior#getCallbackScript()
		 */
		@Override
		public final CharSequence getCallbackScript()
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
	 * Replaces all occurrences of " in string with \".
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
	 * Returns the javascript used to open the window. Subclass
	 * {@link #postProcessSettings(AppendingStringBuffer)} to modify the JavaScript if needed.
	 * 
	 * See WICKET-12
	 * 
	 * @return javascript that opens the window
	 */
	protected final String getWindowOpenJavaScript()
	{
		AppendingStringBuffer buffer = new AppendingStringBuffer(500);

		if (isCustomComponent() == true)
		{
			buffer.append("var element = document.getElementById(\"");
			buffer.append(getContentMarkupId());
			buffer.append("\");\n");
		}

		buffer.append("var settings = new Object();\n");

		appendAssignment(buffer, "settings.minWidth", getMinimalWidth());
		appendAssignment(buffer, "settings.minHeight", getMinimalHeight());
		appendAssignment(buffer, "settings.className", getCssClassName());
		appendAssignment(buffer, "settings.width", getInitialWidth());

		if ((isUseInitialHeight() == true) || (isCustomComponent() == false))
		{
			appendAssignment(buffer, "settings.height", getInitialHeight());
		}
		else
		{
			buffer.append("settings.height=null;\n");
		}

		appendAssignment(buffer, "settings.resizable", isResizable());

		if (isResizable() == false)
		{
			appendAssignment(buffer, "settings.widthUnit", getWidthUnit());
			appendAssignment(buffer, "settings.heightUnit", getHeightUnit());
		}

		if (isCustomComponent() == false)
		{
			Page page = createPage();
			if (page == null)
			{
				throw new WicketRuntimeException("Error creating page for modal dialog.");
			}
			CharSequence pageUrl;
			RequestCycle requestCycle = RequestCycle.get();

			if (page.isPageStateless())
			{
				pageUrl = requestCycle.urlFor(page.getClass(), page.getPageParameters());
			}
			else
			{
				IRequestHandler handler = new RenderPageRequestHandler(new PageProvider(page));
				pageUrl = requestCycle.urlFor(handler);
			}

			appendAssignment(buffer, "settings.src", pageUrl);
		}
		else
		{
			buffer.append("settings.element=element;\n");
		}

		if (getCookieName() != null)
		{
			appendAssignment(buffer, "settings.cookieId", getCookieName());
		}

		Object title = getTitle() != null ? getTitle().getObject() : null;
		if (title != null)
		{
			appendAssignment(buffer, "settings.title", escapeQuotes(title.toString()));
		}

		if (getMaskType() == MaskType.TRANSPARENT)
		{
			buffer.append("settings.mask=\"transparent\";\n");
		}
		else if (getMaskType() == MaskType.SEMI_TRANSPARENT)
		{
			buffer.append("settings.mask=\"semi-transparent\";\n");
		}

		appendAssignment(buffer, "settings.autoSize", autoSize);

		appendAssignment(buffer, "settings.unloadConfirmation", showUnloadConfirmation());

		// set true if we set a windowclosedcallback
		boolean haveCloseCallback = false;

		// in case user is interested in window close callback or we have a pagemap to clean attach
		// notification request
		if (windowClosedCallback != null)
		{
			WindowClosedBehavior behavior = getBehaviors(WindowClosedBehavior.class).get(0);
			buffer.append("settings.onClose = function() { ");
			buffer.append(behavior.getCallbackScript());
			buffer.append(" };\n");

			haveCloseCallback = true;
		}

		// in case we didn't set windowclosecallback, we need at least callback on close button, to
		// close window property (thus cleaning the shown flag)
		if ((closeButtonCallback != null) || (haveCloseCallback == false))
		{
			CloseButtonBehavior behavior = getBehaviors(CloseButtonBehavior.class).get(0);
			buffer.append("settings.onCloseButton = function() { ");
			buffer.append(behavior.getCallbackScript());
			buffer.append(";return false;};\n");
		}

		postProcessSettings(buffer);

		buffer.append(getShowJavaScript());
		return buffer.toString();
	}

	/**
	 * 
	 * @param buffer
	 * @param key
	 * @param value
	 */
	private void appendAssignment(final AppendingStringBuffer buffer, final CharSequence key,
		final int value)
	{
		buffer.append(key).append("=");
		buffer.append(value);
		buffer.append(";\n");
	}

	/**
	 * 
	 * @param buffer
	 * @param key
	 * @param value
	 */
	private void appendAssignment(final AppendingStringBuffer buffer, final CharSequence key,
		final boolean value)
	{
		buffer.append(key).append("=");
		buffer.append(Boolean.toString(value));
		buffer.append(";\n");
	}

	/**
	 * 
	 * @param buffer
	 * @param key
	 * @param value
	 */
	private void appendAssignment(final AppendingStringBuffer buffer, final CharSequence key,
		final CharSequence value)
	{
		buffer.append(key).append("=\"");
		buffer.append(value);
		buffer.append("\";\n");
	}

	/**
	 * Method that allows tweaking the settings
	 * 
	 * @param settings
	 * @return settings javascript
	 */
	protected AppendingStringBuffer postProcessSettings(final AppendingStringBuffer settings)
	{
		return settings;
	}

	/**
	 * Detach the 'title' model
	 * 
	 * @see org.apache.wicket.Component#onDetach()
	 */
	@Override
	protected void onDetach()
	{
		super.onDetach();

		if (title != null)
		{
			title.detach();
		}
	}

	/**
	 * Sets whether window size will be automatically adjusted on opening to fit content's width and
	 * height. <span style="text-decoration: underline">Doesn't work on IE 6.</span>
	 * 
	 * @param autoSize
	 *            Whether window size will be automatically adjusted
	 * @return this
	 */
	public ModalWindow setAutoSize(final boolean autoSize)
	{
		this.autoSize = autoSize;
		return this;
	}

	/**
	 * Returns whether window will be opened in autosize mode.
	 * 
	 * @return True if the window will be opened open in autosize mode, false otherwise
	 */
	public boolean isAutoSize()
	{
		return autoSize;
	}

	/**
	 * Gives the possibility to provide custom
	 * {@link org.apache.wicket.ajax.attributes.IAjaxCallListener}
	 * 
	 * @return the behavior that should be used for the window close button
	 */
	protected CloseButtonBehavior newCloseButtonBehavior()
	{
		return new CloseButtonBehavior();
	}
}
