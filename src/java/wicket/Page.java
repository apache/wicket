/*
 * $Id$ $Revision$
 * $Date$
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
package wicket;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.markup.MarkupStream;
import wicket.markup.html.form.Form;
import wicket.model.IModel;

/**
 * Abstract base class for pages. As a MarkupContainer subclass, a Page can
 * contain a component hierarchy and markup in some markup language such as
 * HTML. Users of the framework should not attempt to subclass Page directly.
 * Instead they should subclass a subclass of Page that is appropriate to the
 * markup type they are using, such as WebPage.
 * <p>
 * When a page is constructed, it is automatically added to the user's session
 * and assigned the next page id available from the session. The session that a
 * page is contained in can be retrieved by calling getPageSession(). Page
 * identifiers start at 0 for each session and increment as new pages are added
 * to the session. The session-unique identifier assigned to a page can be
 * retrieved by calling getId(). This id serves as the Page's component name. So
 * the first page added to a new user session will always be named "0".
 * <p>
 * Pages can be constructed with any constructor when they are being used in a
 * Wicket session, but if you wish to link to a Page using a URL that is
 * bookmarkable (doesn't have session information encoded into it), you need to
 * implement your Page with a constructor that accepts a single PageParameters
 * argument.
 * 
 * @see wicket.markup.html.WebPage
 * @see MarkupContainer
 * @author Jonathan Locke
 * @author Chris Turner
 */
public abstract class Page extends MarkupContainer implements IRedirectListener
{
	/** static for access allowed flag (value == true). */
	protected static final boolean ACCESS_ALLOWED = true;

	/** static for access denied flag (value == false). */
	protected static final boolean ACCESS_DENIED = false;

	/** Log. */
	private static final Log log = LogFactory.getLog(Page.class);

	/** Used to create page-unique numbers */
	private int autoIndex;

	/** Feedback messages for this page */
	private FeedbackMessages feedbackMessages;

	/** This page's identifier. */
	private int id = -1;

	/** The session that this page is in. */
	private final Session session = Session.get();

	/** True if this page is stale. */
	private boolean stale = false;

	/** The rendering before which all pages are stale. */
	private int staleRendering = 0;

	/**
	 * Constructor.
	 */
	protected Page()
	{
		// A page's componentName is its id, which is not determined until
		// setId is called when the page is added to the session
		super(null);
		addToSession();
	}

	/**
	 * @param model
	 *            See Component
	 * @see Component#Component(String, IModel)
	 */
	protected Page(final IModel model)
	{
		super(null, model);
		addToSession();
	}

	/**
	 * @param object
	 *            See Component
	 * @see Component#Component(String, Serializable)
	 */
	protected Page(final Serializable object)
	{
		super(null, object);
		addToSession();
	}

	/**
	 * @param object
	 *            See Component
	 * @param expression
	 *            See Component
	 * @see Component#Component(String, Serializable, String)
	 */
	protected Page(final Serializable object, final String expression)
	{
		super(null, object, expression);
		addToSession();
	}

	/**
	 * Get a page unique number, which will be increased with each call.
	 * 
	 * @return A page unique number
	 */
	public int getAutoIndex()
	{
		return this.autoIndex++;
	}

	/**
	 * @return Returns the feedbackMessages.
	 */
	public FeedbackMessages getFeedbackMessages()
	{
		if (feedbackMessages == null)
		{
			feedbackMessages = new FeedbackMessages();
		}
		return feedbackMessages;
	}

	/**
	 * Get the identifier for this page.
	 * 
	 * @return The identifier for this page
	 */
	public final int getId()
	{
		return id;
	}

	/**
	 * Get the name of this page instance is its unique id.
	 * 
	 * @return The name of this page instance is its unique id
	 * @see wicket.Component#getName()
	 */
	public final String getName()
	{
		return Integer.toString(id);
	}

	/**
	 * Get the session for this page.
	 * 
	 * @return Returns the session for this page.
	 */
	public final Session getSession()
	{
		return session;
	}

	/**
	 * Checks a rendering number against the stale rendering threshold for this
	 * page. If the rendering occurred before the stale-rendering number, then
	 * the rendering is considered stale.
	 * 
	 * @param rendering
	 *            The rendering number to check against this page
	 * @return Returns true if the given rendering of the page is stale.
	 */
	public final boolean isRenderingStale(final int rendering)
	{
		return rendering < staleRendering;
	}

	/**
	 * Whether this page has been marked as stale.
	 * 
	 * @return True if this page has been marked as stale
	 */
	public final boolean isStale()
	{
		return stale;
	}

	/**
	 * Redirect to this page.
	 * 
	 * @see wicket.IRedirectListener#onRedirect()
	 */
	public final void onRedirect()
	{
		// This method is used when redirecting to a page
		getRequestCycle().setPage(this);
	}

	/**
	 * Convenience method. Search for children of type fromClass and invoke
	 * their respective removePersistedFormData() methods.
	 * 
	 * @see Form#removePersistentFormComponentValues(boolean)
	 * 
	 * @param formClass
	 *            Form to be selected. Pages may have more than one Form.
	 * @param disablePersistence
	 *            if true, disable persistence for all FormComponents on that
	 *            page. If false, it will remain unchanged.
	 */
	public final void removePersistedFormData(final Class formClass,
			final boolean disablePersistence)
	{
		// Check that formClass is an instanceof Form
		if (!Form.class.isAssignableFrom(formClass))
		{
			throw new WicketRuntimeException("Form class " + formClass.getName()
					+ " is not a subclass of Form");
		}

		// Visit all children which are an instance of formClass
		visitChildren(formClass, new IVisitor()
		{
			public Object component(final Component component)
			{
				// They must be of type Form as well
				if (component instanceof Form)
				{
					// Delete persistet FormComponent data and disable
					// persistence
					((Form)component).removePersistentFormComponentValues(disablePersistence);
				}
				return CONTINUE_TRAVERSAL;
			}
		});
	}

	/**
	 * Performs a render of this component.
	 */
	public void render()
	{
		// Render markup in page
		super.render();

		// If the application wants component uses checked and
		// the response is not a redirect
		if (getApplicationSettings().getComponentUseCheck() && !getResponse().isRedirect())
		{
			// Visit components on page
			checkRendering(this);
		}
		
		// Reset page for future use
		reset();
	}

	/**
	 * Get the string representation of this container.
	 * 
	 * @return String representation of this container
	 */
	public String toString()
	{
		return "[Page class = " + getClass().getName() + ", id = " + id + "]";
	}

	/**
	 * Whether access is allowed to this page.
	 * 
	 * @return true if access is allowed, false otherwise
	 */
	protected boolean checkAccess()
	{
		return ACCESS_ALLOWED;
	}

	/**
	 * Set-up response with appropriate content type and locale.
	 */
	protected void configureResponse()
	{
		// Get response
		final Response response = getResponse();

		// Set content type based on markup type for page
		response.setContentType("text/" + getMarkupType());

		// Set response locale from session locale
		response.setLocale(getSession().getLocale());
	}

	/**
	 * @see wicket.Component#initModel()
	 */
	protected IModel initModel()
	{
		return null;
	}

	/**
	 * Renders this container to the given response object.
	 */
	protected void onRender()
	{
		// Configure response object with locale and content type
		configureResponse();

		// Check access to page
		if (checkAccess())
		{
			// Set page's associated markup stream
			final MarkupStream markupStream = getAssociatedMarkupStream();
			setMarkupStream(markupStream);

			// Render all the page's markup
			renderAll(markupStream);
		}
	}

	/**
	 * Reset at end of request by resetting each component on the page
	 */
	protected void onReset()
	{
		// Reset the page container
		super.onReset();

		// Clear all feedback messages
		getFeedbackMessages().clear();
	}

	/**
	 * Reset this page. Called if rendering is interrupted by an exception to
	 * put the page back into a state where it can function again.
	 */
	final void resetMarkupStreams()
	{
		// When an exception is thrown while rendering a page, there may
		// be invalid markup streams set on various containers. We need
		// to reset these to null to ensure they get recreated correctly.
		visitChildren(MarkupContainer.class, new IVisitor()
		{
			public Object component(final Component component)
			{
				final MarkupContainer container = (MarkupContainer)component;
				container.setMarkupStream(null);
				return CONTINUE_TRAVERSAL;
			}
		});
	}

	/**
	 * Set the id.
	 * 
	 * @param id
	 *            The id to set.
	 */
	final void setId(final int id)
	{
		this.id = id;
	}

	/**
	 * Set whether this page is stale.
	 * 
	 * @param stale
	 *            whether this page is stale
	 */
	final void setStale(final boolean stale)
	{
		this.stale = stale;
	}

	/**
	 * Set rendering before which all renderings are stale for this page.
	 * 
	 * @param staleRendering
	 *            Rendering before which all renderings are stale for this page
	 */
	final void setStaleRendering(final int staleRendering)
	{
		this.staleRendering = staleRendering;
	}

	/**
	 * Adds this page to the current session
	 */
	private void addToSession()
	{
		// Add page to session. This ensures that all the nice attributes
		// of a page, such as its session and application are accessible in
		// the page constructor.
		this.session.addPage(this);
	}

	static
	{
		// Allow calls through the IRedirectListener interface
		RequestCycle.registerRequestListenerInterface(IRedirectListener.class);
	}
}
