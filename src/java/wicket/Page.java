/*
 * $Id$ $Revision$
 * $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.markup.MarkupStream;
import wicket.markup.html.form.Form;


/**
 * Abstract base class for pages. As a Container subclass, a Page can contain a
 * component hierarchy and markup in some markup language such as HTML.
 * <p>
 * When a page is constructed, it is automatically added to the user's session
 * and assigned the next page id available from the session. The session that a
 * page is contained in can be retrieved by calling getPageSession(). Page
 * identifiers start at 0 for each session and increment as new pages are added
 * to the session. The session-unique identifier assigned to a page can be
 * retrieved by calling getId(). This id serves as the Page's component name. So
 * the first page added to a new user session will always be named "0".
 * 
 * @see Container
 * @author Jonathan Locke
 * @author Chris Turner
 */
public abstract class Page extends Container implements IRedirectListener
{
    // TODO finalize javadoc
    /** Log. */
    private static final Log log = LogFactory.getLog(Page.class);

    /** static for access allowed flag (value == true). */
    protected static final boolean ACCESS_ALLOWED = true;

    /** static for access denied flag (value == false). */
    protected static final boolean ACCESS_DENIED = false;

    /** Temporary reference to the messages in case we are redirecting. */
    FeedbackMessages messages;

    /** This page's identifier. */
    private int id = -1;

    /** The session that this page is in. */
    private final Session session;

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

        // Get thread-local session and add this page. This ensures that
        // all the nice attributes of a page, such as its session and
        // application
        // are accessible in the page constructor.
        this.session = Session.get();
        this.session.addPage(this);
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
     * @see wicket.IRedirectListener#redirect()
     */
    public final void redirect()
    {
        // This method is used when redirecting to a page
        getRequestCycle().setPage(this);
    }

    /**
     * Convinience method. Search for children of type fromClass and invoke
     * their respectiv removePersistedFormData() method.
     * 
     * @see Form#removePersistedFormComponentData(boolean)
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
                    ((Form)component).removePersistedFormComponentData(disablePersistence);
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
        try
        {
            initUIMessages();
            super.render();

            // If the application wants component uses checked and
            // the response is not a redirect
            if (getApplicationSettings().getComponentUseCheck() && !getResponse().isRedirect())
            {
                // Visit components on page
                checkRendering(this);
            }
        }
        finally
        // be sure to have models detached
        {
            // Visit components on page
            final Page page = (Page)this;
            detachModels(page);
        }
    }

    /**
     * Get the string representation of this container.
     * 
     * @return String representation of this container
     */
    public String toString()
    {
        return "[class = " + getClass().getName() + ", id = " + id + "]";
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
     * Renders this container to the given response object.
     */
    protected void handleRender()
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
     * Looks if any messages were set as a temporary variable on the page and,
     * if so, sets these messages as the current.
     */
    private void initUIMessages()
    {
        if (this.messages != null)
        {
            // so, we are comming from a redirect;
            // these are the saved messages from the thread that issued
            // the redirect. Set as the current threads' messages
            FeedbackMessages.set(this.messages);

            // reset the page variable
            this.messages = null;
        }
    }

    static
    {
        // Allow calls through the IRedirectListener interface
        RequestCycle.registerRequestListenerInterface(IRedirectListener.class);
    }
}


