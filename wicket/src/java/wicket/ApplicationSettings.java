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
package wicket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.markup.ComponentTag;
import wicket.markup.html.form.Crypt;
import wicket.markup.html.form.FormComponentPersistenceDefaults;
import wicket.markup.html.form.ICrypt;
import wicket.resource.ApplicationStringResourceLoader;
import wicket.resource.ComponentStringResourceLoader;
import wicket.resource.IStringResourceLoader;
import wicket.util.convert.ConverterRegistry;
import wicket.util.file.Path;
import wicket.util.lang.EnumeratedType;
import wicket.util.time.Duration;
import wicket.util.watch.Watcher;

/**
 * Contains application settings as property values.  All settings exposed are generic to any
 * kind of protocol or markup.
 * <p>
 * Application settings properties:
 * <p>
 * <ul>
 * <i>componentNameAttribute</i> (defaults to "componentName") - The markup attribute which
 *    denotes the names of components to be attached
 * <p>
 * <i>componentUseCheck</i> (defaults to true) - Causes the framework to do a check after
 *    rendering each page to ensure that each component was used in rendering the markup.
 *    If components are found that are not referenced in the markup, an appropriate error
 *    will be displayed
 * <p>
 * <i>compressWhitespace</i> (defaults to false) - Causes pages to render with redundant
 *    whitespace removed.  Whitespace stripping is not HTML or JavaScript savvy and can
 *    conceivably break pages, but should provide significant performance improvements.
 * <p>
 * <i>unexpectedExceptionDisplay</i> (defaults to SHOW_EXCEPTION_PAGE) - Determines how
 *    exceptions are displayed to the developer or user
 * <p>
 * <i>homePage</i> (no default) - You must set this property to the bookmarkable page that
 *    you want the framework to respond with when no path information is specified.
 * <p>
 * <i>internalErrorPage</i> - You can override this with your own page class to display
 *    internal errors in a different way.
 * <p>
 * <i>maxSessionPages</i> - The maximum number of pages in the user's session before old pages
 *    are expired.
 * <p>
 * <i>pageExpiredErrorPage</i> - You can override this with your own bookmarkable page class
 *    to display expired page errors in a different way.
 * <p>
 * <i>resourcePollFrequency</i> (defaults to no polling frequency) - Frequency at which
 *    resources should be polled for changes.
 * <p>
 * <i>sourcePath</i> (no default) - Set this to enable polling of resources on your source path
 * <p>
 * <i>staleDataErrorPage</i> - You can override this with your own bookmarkable page class
 *    to display stale data errors in a different way.
 * <p>
 * <i>stripComments</i> (defaults to false) - Set to true to strip HTML comments during
 *    markup loading
 * <p>
 * <i>stripComponentNames</i> (defaults to false) - Set to true to strip component name attributes
 *    during rendering
 * <p>
 * <i>exceptionOnMissingResource</i> (defaults to true) - Set to true to throw a runtime exception
 *    if a required string resource is not found. Set to false to return the requested resource
 *    key surrounded by pairs of question mark characters (e.g. "??missingKey??")
 * <p>
 * <i>useDefaultOnMissingResource</i> (defaults to true) - Set to true to return a default value
 *    if available when a required string resource is not found. If set to false then the
 *    exceptionOnMissingResource flag is used to determine how to behave. If not default is
 *    available then this is the same as if this flag were false
 * <p>
 * <i>stringResourceLoaders</li> - A chain of <code>IStringResourceLoader</code> instances
 *    that are searched in order to obtain string resources used during localization.
 *    By default the chain is set up to first search for resources against a particular
 *    component (e.g. page etc.) and then against the application.
 * <p>
 * <i>localizer</i> (read-only) - An application wide object encapsulating all of the
 *    functionality required to access localized resources.
 * <p>
 * <i>converterRegistry</i> (read-only) - The registry with converters that should be used
 *    for type conversion e.g. by {@link com.voicetribe.wicket.PropertyModel}. Use the reference
 *    of converterRegistry to register/ deregister type converters if needed. Also, there are
 *    convenience method in converterRegistry to swith to a localized/ non-localized set
 *    of type converters.
 * </ul>
 * <p>
 * More documentation is available about each setting in the setter method for the property.
 *
 * @author Jonathan Locke
 * @author Chris Turner
 */
public class ApplicationSettings
{
    // registry with converters
    private ConverterRegistry converterRegistry = new ConverterRegistry();

    // Component attribute name
    private String componentNameAttribute = ComponentTag.wicketComponentNameAttribute;

    // True to check that each component on a page is used
    private boolean componentUseCheck = true;

    // True if multiple tabs/spaces should be compressed to a single space
    private boolean compressWhitespace = false;

    // True if runtime exceptions should not be shown
    private UnexpectedExceptionDisplay unexpectedExceptionDisplay = SHOW_EXCEPTION_PAGE;

    // Home page class name
    private Class homePage;
    private String homePageClassName;
    private Class internalErrorPage;
    private Class staleDataErrorPage;
    private Class pageExpiredErrorPage;

    // The maximum number of pages in a session
    private int maxSessionPages = 10;

    // Frequency at which files should be polled
    private Duration resourcePollFrequency = null;

    // Source path
    private Path sourcePath = new Path();

    // Should HTML comments be stripped during rendering?
    private boolean stripComments = false;

    // Should component names be stripped during rendering?
    private boolean stripComponentNames = false;

    // Default before/after disabled link markup
    private String defaultBeforeDisabledLink = "<i>";
    private String defaultAfterDisabledLink = "</i>";

    // Watcher to watch for changes in markup files
    private Watcher resourceWatcher;
    private boolean triedToCreateResouceWatcher = false;

    // Flags used to determine how to behave if resources are not found
    private boolean exceptionOnMissingResource = true;
    private boolean useDefaultOnMissingResource = true;

    // Chain of string resource loaders to use and internal flag
    // that keeps track of whether the user has overridden the defaults
    private List stringResourceLoaders = new ArrayList(2);
    private boolean overriddenStringResourceLoaders = false;

    // The single application wide localization class
    private Localizer localizer;
    
    // Responsible to persist and retrieve FormComponent data 
    // (e.g. by means of Cookies)
    private FormComponentPersistenceDefaults formComponentPersister = new FormComponentPersistenceDefaults();
    
    // Encryption key used to encode/decode passwords e.g.
    private String encryptionKey = "WiCkEt-FRAMEwork";
    
    // Class of type ICrypt to implement encryption
    private Class cryptClass = Crypt.class;
   
    /** Factory to create new Page objects */
    private IPageFactory pageFactory;
    
    // Code broadcaster for reporting
    private static final Log log = LogFactory.getLog(ApplicationSettings.class);

    /**
     * Enumerated type for different ways of displaying unexpected exceptions.
     */
    public static final class UnexpectedExceptionDisplay extends EnumeratedType { UnexpectedExceptionDisplay(final String name) { super(name); } }

    /**
     * Indicates a generic internal error page should be shown when an unexpected exception is thrown.
     */
    public static final UnexpectedExceptionDisplay SHOW_INTERNAL_ERROR_PAGE = new UnexpectedExceptionDisplay("SHOW_INTERNAL_ERROR_PAGE");

    /**
     * Indicates that no exception page should be shown when an unexpected exception is thrown.
     */
    public static final UnexpectedExceptionDisplay SHOW_NO_EXCEPTION_PAGE = new UnexpectedExceptionDisplay("SHOW_NO_EXCEPTION_PAGE");

    /**
     * Indicates that an exception page appropriate to development should be shown when an unexpected exception is thrown.
     */
    public static final UnexpectedExceptionDisplay SHOW_EXCEPTION_PAGE = new UnexpectedExceptionDisplay("SHOW_EXCEPTION_PAGE");

    /**
     * Create the application settings, carrying out any necessary initialisations.
     *
     * @param application The application that these settings are for
     */
    public ApplicationSettings(final IApplication application) {
        localizer = new Localizer(this);
        stringResourceLoaders.add(new ComponentStringResourceLoader());
        stringResourceLoaders.add(new ApplicationStringResourceLoader(application));
        pageFactory = new PageFactory(application);    
    }

    /**
     * Gets component name attribute in use in this application.  Normally, this is "componentName",
     * but it can be changed in the unlikely event that tag attribute naming conflicts arise.
     * @return The current component name attribute
     * @see ApplicationSettings#setComponentNameAttribute(String)
     */
    public final String getComponentNameAttribute()
    {
        return componentNameAttribute;
    }

    /**
     * Get whether component use should be checked or not.
     * @return True if component use should be checked
     * @see ApplicationSettings#setComponentUseCheck(boolean)
     */
    public final boolean getComponentUseCheck()
    {
        return this.componentUseCheck;
    }

    /**
     * @return Returns the compressWhitespace.
     * @see ApplicationSettings#setCompressWhitespace(boolean)
     */
    public boolean getCompressWhitespace()
    {
        return compressWhitespace;
    }

    /**
     * @return Returns the unexpectedExceptionDisplay.
     * @see ApplicationSettings#setUnexpectedExceptionDisplay(ApplicationSettings.UnexpectedExceptionDisplay)
     */
    public final UnexpectedExceptionDisplay getUnexpectedExceptionDisplay()
    {
        return unexpectedExceptionDisplay;
    }

    /**
     * Gets home page class.
     * @return Returns the homePage.
     * @see ApplicationSettings#setHomePage(Class)
     */
    public final Class getHomePage()
    {
        if (homePage != null)
        {
            return homePage;
        }
        
        if ((homePageClassName != null) && (homePageClassName.trim().length() > 0))
        {
            final Class homePage = getPageFactory().getClassInstance(homePageClassName);
            if (homePage != null)
            {
                return homePage;
            }
        }
        
        throw new IllegalStateException("No home page specified");
    }

    /**
     * Gets internal error page class.
     * @return Returns the internalErrorPage.
     * @see ApplicationSettings#setInternalErrorPage(Class)
     */
    public final Class getInternalErrorPage()
    {
        return internalErrorPage;
    }

    /**
     * Gets the maximum number of pages held in a session.
     * @return Returns the maxSessionPages.
     * @see ApplicationSettings#setMaxSessionPages(int)
     */
    public final int getMaxSessionPages()
    {
        return maxSessionPages;
    }

    /**
     * Gets the page expired page class.
     * @return Returns the pageExpiredErrorPage.
     * @see ApplicationSettings#setPageExpiredErrorPage(Class)
     */
    public final Class getPageExpiredErrorPage()
    {
        return pageExpiredErrorPage;
    }

    /**
     * @return Returns the resourcePollFrequency.
     * @see ApplicationSettings#setResourcePollFrequency(Duration)
     */
    public Duration getResourcePollFrequency()
    {
        return resourcePollFrequency;
    }

    /**
     * Gets any source code path to use when searching for resources.
     * @return Returns the sourcePath.
     * @see ApplicationSettings#setSourcePath(Path)
     */
    public final Path getSourcePath()
    {
        return sourcePath;
    }

    /**
     * Gets the stale data error page class.
     * @return Returns the staleDataErrorPage.
     * @see ApplicationSettings#setStaleDataErrorPage(Class)
     */
    public final Class getStaleDataErrorPage()
    {
        return staleDataErrorPage;
    }

    /**
     * @return Returns the stripComments.
     * @see ApplicationSettings#setStripComments(boolean)
     */
    public boolean getStripComments()
    {
        return stripComments;
    }

    /**
     * Returns true if componentName attributes should be stripped from tags when rendering.
     * @return Returns the stripComponentNames.
     * @see ApplicationSettings#setStripComponentNames(boolean)
     */
    public final boolean getStripComponentNames()
    {
        return stripComponentNames;
    }

    /**
     * Sets component name attribute in use in this application.  Normally, this is "componentName",
     * but it can be changed in the unlikely event that tag attribute naming conflicts arise.
     * @param componentNameAttribute The componentNameAttribute to set.
     * @return This
     */
    public final ApplicationSettings setComponentNameAttribute(final String componentNameAttribute)
    {
        this.componentNameAttribute = componentNameAttribute;
        return this;
    }

    /**
     * Enables or disables checking for unused components.  Component checking is on by defaults
     * and only minor efficiency can be gained from turning it off in a production environment.
     * @param componentUseCheck
     * @return This
     */
    public final ApplicationSettings setComponentUseCheck(boolean componentUseCheck)
    {
        this.componentUseCheck = componentUseCheck;
        return this;
    }

    /**
     * Turns on whitespace compression.  Multiple occurrences of space/tab characters will
     * be compressed to a single space.  Multiple line breaks newline/carriage-return will
     * also be compressed to a single newline.
     * <p>
     * Compression is currently not HTML aware and so it may be possible for whitespace
     * compression to break pages.  For this reason, whitespace compression is off by
     * default and you should test your application throroughly after turning whitespace
     * compression on.
     * <p>
     * Spaces are removed from markup at markup load time and there should be no effect on
     * page rendering speed.  In fact, your pages should render faster with whitespace
     * compression enabled.
     * @param compressWhitespace The compressWhitespace to set.
     * @return This
     */
    public ApplicationSettings setCompressWhitespace(boolean compressWhitespace)
    {
        this.compressWhitespace = compressWhitespace;
        return this;
    }

    /**
     * The exception display type determines how the framework displays exceptions
     * to you as a developer or user.
     * <p>
     * The default value for exception display type is SHOW_EXCEPTION_PAGE.
     * When this value is set and an unhandled runtime exception is thrown by a page,
     * a redirect to a helpful exception display page will occur.
     * <p>
     * This is a developer feature, however, and you may want to instead show an
     * internal error page without developer details that allows a user to start
     * over at the application's home page.  This can be accomplished by setting
     * the exception display type to SHOW_INTERNAL_ERROR_PAGE.
     * <p>
     * Finally, if you are having trouble with the exception display pages themselves,
     * you can disable exception displaying entirely with the value SHOW_NO_EXCEPTION_PAGE.
     * This will cause the framework to re-throw any unhandled runtime exceptions after
     * wrapping them in a ServletException wrapper.
     * @param unexpectedExceptionDisplay The unexpectedExceptionDisplay to set.
     * @return This
     */
    public final ApplicationSettings setUnexpectedExceptionDisplay(final UnexpectedExceptionDisplay unexpectedExceptionDisplay)
    {
        this.unexpectedExceptionDisplay = unexpectedExceptionDisplay;
        return this;
    }

    /**
     * Gets home page class.  The class must be external / bookmarkable and therefore
     * must extend Page and must be able to construct from PageParameters.
     * @param homePage The home page class
     * @return This
     */
    public final ApplicationSettings setHomePage(final Class homePage)
    {
        this.homePage = homePage;
        return this;
    }

    public final ApplicationSettings setHomePage(final String homePage)
    {
        this.homePageClassName = homePage;
        return this;
    }

    /**
     * Sets internal error page class.  The class must be external / bookmarkable and
     * therefore must extend Page and must be able to construct from PageParameters.
     * @param internalErrorPage The internalErrorPage to set.
     * @return This
     */
    public final ApplicationSettings setInternalErrorPage(final Class internalErrorPage)
    {
        this.internalErrorPage = internalErrorPage;
        return this;
    }

    /**
     * Sets the maximum number of pages held in a session.  If a page is added to a
     * user's session when the session is full, the oldest page in the session will
     * be expired.  The primary purpose of setting a maximum number of pages in a
     * session is to limit the resources consumed by server-side state.
     * @param maxSessionPages The maxSessionPages to set.
     * @return This
     */
    public final ApplicationSettings setMaxSessionPages(final int maxSessionPages)
    {
        this.maxSessionPages = maxSessionPages;
        return this;
    }

    /**
     * Sets the page expired page class.  The class must be external / bookmarkable and
     * therefore must extend Page and must be able to construct from PageParameters.
     * @param pageExpiredErrorPage The pageExpiredErrorPage to set.
     * @return This
     */
    public final ApplicationSettings setPageExpiredErrorPage(final Class pageExpiredErrorPage)
    {
        this.pageExpiredErrorPage = pageExpiredErrorPage;
        return this;
    }

    /**
     * Sets the resource polling frequency.  This is the duration of time between checks of
     * resource modification times.  If a resource, such as an HTML file, has changed, it will
     * be reloaded.  Default is for no resource polling to occur.
     * @param resourcePollFrequency Frequency at which to poll resources
     * @return This
     * @see ApplicationSettings#setSourcePath(Path)
     */
    public final ApplicationSettings setResourcePollFrequency(final Duration resourcePollFrequency)
    {
        this.resourcePollFrequency = resourcePollFrequency;
        return this;
    }

    /**
     * Sets a source code path to use when searching for resources.  Setting a source path
     * can allow developers to "hot update" pages by simply changing markup on the fly and
     * hitting refresh in their browser.
     * @param sourcePath The sourcePath to set
     * @return This
     */
    public final ApplicationSettings setSourcePath(final Path sourcePath)
    {
        this.sourcePath = sourcePath;
        return this;
    }

    /**
     * Sets the stale data error page class.  The class must be external / bookmarkable and
     * therefore must extend Page and must be able to construct from PageParameters.
     * @param staleDataErrorPage The staleDataErrorPage to set
     * @return This
     */
    public final ApplicationSettings setStaleDataErrorPage(final Class staleDataErrorPage)
    {
        this.staleDataErrorPage = staleDataErrorPage;
        return this;
    }

    /**
     * Enables stripping of markup comments denoted in markup by HTML comment tagging.
     * @param stripComments True to strip markup comments from rendered pages
     * @return This
     */
    public ApplicationSettings setStripComments(boolean stripComments)
    {
        this.stripComments = stripComments;
        return this;
    }

    /**
     * Determines if componentName attributes should be stripped from tags when rendering.
     * Component name attributes in rendered pages can be a helpful debugging tool, but
     * they are not helpful to end-users and do not increase efficiency in delivering pages
     * over any protocol.
     * @param stripComponentNames The stripComponentNames to set.
     * @return This
     */
    public final ApplicationSettings setStripComponentNames(final boolean stripComponentNames)
    {
        this.stripComponentNames = stripComponentNames;
        return this;
    }

    /**
     * @return Returns the defaultAfterDisabledLink.
     */
    public String getDefaultAfterDisabledLink()
    {
        return defaultAfterDisabledLink;
    }

    /**
     * @param defaultAfterDisabledLink The defaultAfterDisabledLink to set.
     */
    public void setDefaultAfterDisabledLink(String defaultAfterDisabledLink)
    {
        this.defaultAfterDisabledLink = defaultAfterDisabledLink;
    }

    /**
     * @return Returns the defaultBeforeDisabledLink.
     */
    public String getDefaultBeforeDisabledLink()
    {
        return defaultBeforeDisabledLink;
    }

    /**
     * @param defaultBeforeDisabledLink The defaultBeforeDisabledLink to set.
     */
    public void setDefaultBeforeDisabledLink(String defaultBeforeDisabledLink)
    {
        this.defaultBeforeDisabledLink = defaultBeforeDisabledLink;
    }

    /**
     * @return Whether to throw an exception when a missing resource is requested
     */
    public final boolean isExceptionOnMissingResource() {
        return exceptionOnMissingResource;
    }

    /**
     * @param exceptionOnMissingResource Whether to throw an exception when a
     *                                   missing resource is requested
     * @return This
     */
    public final ApplicationSettings setExceptionOnMissingResource(final boolean exceptionOnMissingResource) {
        this.exceptionOnMissingResource = exceptionOnMissingResource;
        return this;
    }

    /**
     * @return Whether to use a default value (if available) when a missing resource is requested
     */
    public final boolean isUseDefaultOnMissingResource() {
        return useDefaultOnMissingResource;
    }

    /**
     * @param useDefaultOnMissingResource Whether to use a default value (if available)
     *                                    when a missing resource is requested
     * @return This
     */
    public final ApplicationSettings setUseDefaultOnMissingResource(final boolean useDefaultOnMissingResource) {
        this.useDefaultOnMissingResource = useDefaultOnMissingResource;
        return this;
    }

    /**
     * Add a string resource loader to the chain of loaders. If this is the first call to this
     * method since the creation of the application settings then the existing chain is cleared
     * before the new loader is added.
     *
     * @param loader The loader to be added
     * @return This
     */
    public ApplicationSettings addStringResourceLoader(final IStringResourceLoader loader) {
        if ( !overriddenStringResourceLoaders ) {
            stringResourceLoaders.clear();
            overriddenStringResourceLoaders = true;
        }
        stringResourceLoaders.add(loader);
        return this;
    }

    /**
     * Internal method to expose the string resource loaders configured within
     * the settings to the localization helpers that need to work with them.
     *
     * @return The string resource loaders
     */
    List getStringResourceLoaders() {
        return Collections.unmodifiableList(stringResourceLoaders);
    }

    /**
     * @return The application wide localizer instance
     */
    public Localizer getLocalizer() {
        return localizer;
    }

    /**
     * @return Resource watcher with polling frequency determined by setting,
     * or null if no polling frequency has been set.
     */
    public Watcher getResourceWatcher()
    {
        if (!triedToCreateResouceWatcher)
        {
            final Duration frequency = getResourcePollFrequency();
            if (frequency != null)
            {
                resourceWatcher = new Watcher(frequency);
            }
            triedToCreateResouceWatcher = true;
        }
        return resourceWatcher;
    }

    /**
     * Get converterRegistry.
     * @return converterRegistry.
     */
    public final ConverterRegistry getConverterRegistry()
    {
        return converterRegistry;
    }
    
    /**
     * Get the defaults to be used by persistence manager
     * @return FormComponentPersistenceDefaults 
     */
    public FormComponentPersistenceDefaults getFormComponentPersistenceDefaults() 
    {
    	return formComponentPersister;
    }
    
    /**
     * Get encryption key used to encode/decode passwords e.g.
     * 
     * @return encryption key
     */
    public String getEncryptionKey()
    {
        return encryptionKey;
    }
    
    /**
     * Set encryption key used to encode/decode PasswordTextFields e.g.
     * 
     * @param encryptionKey
     */
    public void setEncryptionKey(String encryptionKey)
    {
        this.encryptionKey = encryptionKey;
    }

    /**
     * Get instance of de-/encryption class.
     * @return instance of de-/encryption class
     */
    public ICrypt getCryptInstance()
    {
        final ICrypt crypt;
        try
        {
            crypt = (ICrypt) this.cryptClass.newInstance();
            crypt.setKey(getEncryptionKey());
            return crypt;
        } 
        catch (InstantiationException e)
        {
            throw new RuntimeException("De-/Encryption object can not be instantiated", e);
        } 
        catch (IllegalAccessException e)
        {
            throw new RuntimeException("De-/Encryption object can not be instantiated", e);
        }
    }

    /**
     * Set new Class to be used for de-/encryption
     * 
     * @param crypt
     */
    public void setCrypt(Class crypt)
    {
        this.cryptClass = crypt;
    }
    
    /**
     * @return Returns the pageFactory.
     */
    public IPageFactory getPageFactory()
    {
        return pageFactory;
    }
    
    /**
     * @param pageFactory The pageFactory to set.
     */
    public void setPageFactory(final IPageFactory pageFactory)
    {
        this.pageFactory = pageFactory;
    }
}

///////////////////////////////// End of File /////////////////////////////////
