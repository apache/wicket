/*
 * $Id$
 * $Revision$ $Date$
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.markup.AutolinkComponentResolver;
import wicket.markup.ComponentTag;
import wicket.markup.ComponentWicketTag;
import wicket.markup.MarkupParser;
import wicket.markup.WicketTagComponentResolver;
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
import wicket.util.watch.ModificationWatcher;

/**
 * Contains application settings as property values. All settings exposed are
 * generic to any kind of protocol or markup.
 * <p>
 * Application settings properties:
 * <p>
 * <ul>
 * <i>componentNameAttribute </i> (defaults to "componentName") - The markup
 * attribute which denotes the names of components to be attached
 * <p>
 * <i>componentUseCheck </i> (defaults to true) - Causes the framework to do a
 * check after rendering each page to ensure that each component was used in
 * rendering the markup. If components are found that are not referenced in the
 * markup, an appropriate error will be displayed
 * <p>
 * <i>compressWhitespace </i> (defaults to false) - Causes pages to render with
 * redundant whitespace removed. Whitespace stripping is not HTML or JavaScript
 * savvy and can conceivably break pages, but should provide significant
 * performance improvements.
 * <p>
 * <i>unexpectedExceptionDisplay </i> (defaults to SHOW_EXCEPTION_PAGE) -
 * Determines how exceptions are displayed to the developer or user
 * <p>
 * <i>homePage </i> (no default) - You must set this property to the
 * bookmarkable page that you want the framework to respond with when no path
 * information is specified.
 * <p>
 * <i>internalErrorPage </i>- You can override this with your own page class to
 * display internal errors in a different way.
 * <p>
 * <i>maxSessionPages </i>- The maximum number of pages in the user's session
 * before old pages are expired.
 * <p>
 * <i>pageExpiredErrorPage </i>- You can override this with your own
 * bookmarkable page class to display expired page errors in a different way.
 * <p>
 * <i>resourcePollFrequency </i> (defaults to no polling frequency) - Frequency
 * at which resources should be polled for changes.
 * <p>
 * <i>sourcePath </i> (no default) - Set this to enable polling of resources on
 * your source path
 * <p>
 * <i>staleDataErrorPage </i>- You can override this with your own bookmarkable
 * page class to display stale data errors in a different way.
 * <p>
 * <i>stripComments </i> (defaults to false) - Set to true to strip HTML
 * comments during markup loading
 * <p>
 * <i>stripComponentNames </i> (defaults to false) - Set to true to strip
 * component name attributes during rendering
 * <p>
 * <i>exceptionOnMissingResource </i> (defaults to true) - Set to true to throw
 * a runtime exception if a required string resource is not found. Set to false
 * to return the requested resource key surrounded by pairs of question mark
 * characters (e.g. "??missingKey??")
 * <p>
 * <i>useDefaultOnMissingResource </i> (defaults to true) - Set to true to
 * return a default value if available when a required string resource is not
 * found. If set to false then the exceptionOnMissingResource flag is used to
 * determine how to behave. If no default is available then this is the same as
 * if this flag were false
 * <p>
 * <i>stringResourceLoaders </i>- A chain of <code>IStringResourceLoader</code>
 * instances that are searched in order to obtain string resources used during
 * localization. By default the chain is set up to first search for resources
 * against a particular component (e.g. page etc.) and then against the
 * application.
 * <p>
 * <i>localizer </i> (read-only) - An application wide object encapsulating all
 * of the functionality required to access localized resources.
 * <p>
 * <i>converterRegistry </i> (read-only) - The registry with converters that
 * should be used for type conversion e.g. by {@link wicket.model.PropertyModel}.
 * Use the reference of converterRegistry to register/ deregister type
 * converters if needed. Also, there are convenience method in converterRegistry
 * to swith to a localized/ non-localized set of type converters.
 * <p>
 * <i>defaultPageFactory </i>- the factory class that is used for constructing
 * page instances.
 * <p>
 * <i>propertyModelDefaultApplyFormatting </i>- Whether the PropertyModel
 * instances apply formatting by default; default == false.
 * </ul>
 * <p>
 * More documentation is available about each setting in the setter method for
 * the property.
 * 
 * @author Jonathan Locke
 * @author Chris Turner
 * @author Eelco Hillenius
 */
public class ApplicationSettings
{
    // TODO finalize javadoc
    /** Log */
    private static final Log log = LogFactory.getLog(ApplicationSettings.class);

    /** Component attribute name */
    private String componentNameAttribute = ComponentTag.DEFAULT_COMPONENT_NAME_ATTRIBUTE;

    /** List of (static) ComponentResolvers */
    private List componentResolvers;

    /** True to check that each component on a page is used */
    private boolean componentUseCheck = true;

    /** True if multiple tabs/spaces should be compressed to a single space */
    private boolean compressWhitespace = false;

    // Registry with converters
    private ConverterRegistry converterRegistry = new ConverterRegistry();

    // Class of type ICrypt to implement encryption
    private Class cryptClass = Crypt.class;

    /** Default markup for after a disabled link */
    private String defaultAfterDisabledLink = "</i>";

    /** Default markup for before a disabled link */
    private String defaultBeforeDisabledLink = "<i>";

    /** Default class resolver to find classes */
    private IClassResolver defaultClassResolver = new DefaultClassResolver();

    /** Default factory to create new Page objects */
    private IPageFactory defaultPageFactory;

    /** Encryption key used to encode/decode passwords e.g. */
    private String encryptionKey = "WiCkEt-FRAMEwork";

    /** Flags used to determine how to behave if resources are not found */
    private boolean exceptionOnMissingResource = true;

    /** Default values for persistence of form data (by means of cookies) */
    private FormComponentPersistenceDefaults formComponentPersistenceDefaults = new FormComponentPersistenceDefaults();

    /** The single application-wide localization class */
    private Localizer localizer;

    /** Pluggable markup parser */
    private String markupParserClassName = MarkupParser.class.getName();

    /** The maximum number of pages in a session */
    private int maxSessionPages = 10;

    /** True if string resource loaders have been overridden */
    private boolean overriddenStringResourceLoaders = false;

    /**
     * Whether the {@link wicket.model.PropertyModel}instances apply formatting
     * by default; default == false.
     */
    private boolean propertyModelDefaultApplyFormatting = false;

    /** Frequency at which files should be polled */
    private Duration resourcePollFrequency = null;

    /** ModificationWatcher to watch for changes in markup files */
    private ModificationWatcher resourceWatcher;

    /** Source path */
    private Path sourcePath = new Path();

    /** Chain of string resource loaders to use */
    private List stringResourceLoaders = new ArrayList(2);

    /** Should HTML comments be stripped during rendering? */
    private boolean stripComments = false;

    /** Should component names be stripped during rendering? */
    private boolean stripComponentNames = false;

    /** If true, wicket tags ( <wicket ..>) shall be removed from output */
    private boolean stripWicketTags = false;

    /** Type of handling for unexpected exceptions */
    private UnexpectedExceptionDisplay unexpectedExceptionDisplay = SHOW_EXCEPTION_PAGE;

    /** Determines behavior of string resource loading if string is missing */
    private boolean useDefaultOnMissingResource = true;

    /** Default xhtml wicket tag name: e.g. <wicket> */
    private String wicketTagName = ComponentWicketTag.WICKET_TAG_NAME;

    /**
     * Indicates that an exception page appropriate to development should be
     * shown when an unexpected exception is thrown.
     */
    public static final UnexpectedExceptionDisplay SHOW_EXCEPTION_PAGE = new UnexpectedExceptionDisplay(
            "SHOW_EXCEPTION_PAGE");

    /**
     * Indicates a generic internal error page should be shown when an
     * unexpected exception is thrown.
     */
    public static final UnexpectedExceptionDisplay SHOW_INTERNAL_ERROR_PAGE = new UnexpectedExceptionDisplay(
            "SHOW_INTERNAL_ERROR_PAGE");

    /**
     * Indicates that no exception page should be shown when an unexpected
     * exception is thrown.
     */
    public static final UnexpectedExceptionDisplay SHOW_NO_EXCEPTION_PAGE = new UnexpectedExceptionDisplay(
            "SHOW_NO_EXCEPTION_PAGE");
    
    /**
     * Enumerated type for different ways of displaying unexpected exceptions.
     */
    public static final class UnexpectedExceptionDisplay extends EnumeratedType
    {
        UnexpectedExceptionDisplay(final String name)
        {
            super(name);
        }
    }

    /**
     * Create the application settings, carrying out any necessary
     * initialisations.
     * 
     * @param application
     *            The application that these settings are for
     */
    public ApplicationSettings(final IApplication application)
    {
        localizer = new Localizer(this);
        stringResourceLoaders.add(new ComponentStringResourceLoader());
        stringResourceLoaders.add(new ApplicationStringResourceLoader(
                application));
        defaultPageFactory = new DefaultPageFactory(application);

        componentResolvers = new ArrayList();
        componentResolvers.add(new AutolinkComponentResolver());
        componentResolvers.add(new WicketTagComponentResolver());
    }

    /**
     * Add a string resource loader to the chain of loaders. If this is the
     * first call to this method since the creation of the application settings
     * then the existing chain is cleared before the new loader is added.
     * 
     * @param loader
     *            The loader to be added
     * @return This
     */
    public ApplicationSettings addStringResourceLoader(
            final IStringResourceLoader loader)
    {
        if (!overriddenStringResourceLoaders)
        {
            stringResourceLoaders.clear();
            overriddenStringResourceLoaders = true;
        }
        stringResourceLoaders.add(loader);
        return this;
    }

    /**
     * Gets component name attribute in use in this application. Normally, this
     * is "componentName", but it can be changed in the unlikely event that tag
     * attribute naming conflicts arise.
     * 
     * @return The current component name attribute
     * @see ApplicationSettings#setComponentNameAttribute(String)
     */
    public final String getComponentNameAttribute()
    {
        return componentNameAttribute;
    }

    /**
     * Get the (modifiable) List of ComponentResolvers.
     * 
     * @see wicket.markup.AutolinkComponentResolver for an example
     * @return List of ComponentResolvers
     */
    public final List getComponentResolvers()
    {
        return componentResolvers;
    }

    /**
     * Get whether component use should be checked or not.
     * 
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
     * Get converterRegistry.
     * 
     * @return converterRegistry.
     */
    public final ConverterRegistry getConverterRegistry()
    {
        return converterRegistry;
    }

    /**
     * Get instance of de-/encryption class.
     * 
     * @return instance of de-/encryption class
     */
    public ICrypt getCryptInstance()
    {
        try
        {
            final ICrypt crypt = (ICrypt) this.cryptClass.newInstance();
            crypt.setKey(getEncryptionKey());
            return crypt;
        }
        catch (InstantiationException e)
        {
            throw new RuntimeException(
                    "Encryption/decryption object can not be instantiated", e);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(
                    "Encryption/decryption object can not be instantiated", e);
        }
    }

    /**
     * @return Returns the defaultAfterDisabledLink.
     */
    public String getDefaultAfterDisabledLink()
    {
        return defaultAfterDisabledLink;
    }

    /**
     * @return Returns the defaultBeforeDisabledLink.
     */
    public String getDefaultBeforeDisabledLink()
    {
        return defaultBeforeDisabledLink;
    }

    /**
     * Gets the default resolver to use when finding classes
     * 
     * @return Default class resolver
     */
    public IClassResolver getDefaultClassResolver()
    {
        return defaultClassResolver;
    }

    /**
     * Gets the default factory to be used when creating pages
     * 
     * @return The default page factory
     */
    public IPageFactory getDefaultPageFactory()
    {
        return defaultPageFactory;
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
     * @return Whether to throw an exception when a missing resource is
     *         requested
     */
    public final boolean getExceptionOnMissingResource()
    {
        return exceptionOnMissingResource;
    }

    /**
     * Get the defaults to be used by persistence manager
     * 
     * @return FormComponentPersistenceDefaults
     */
    public FormComponentPersistenceDefaults getFormComponentPersistenceDefaults()
    {
        return formComponentPersistenceDefaults;
    }

    /**
     * @return The application wide localizer instance
     */
    public Localizer getLocalizer()
    {
        return localizer;
    }

    /**
     * @return The class to use for parsing markup
     */
    public final Class getMarkupParserClass()
    {
        return getDefaultClassResolver().resolveClass(markupParserClassName);
    }

    /**
     * Gets the maximum number of pages held in a session.
     * 
     * @return Returns the maxSessionPages.
     * @see ApplicationSettings#setMaxSessionPages(int)
     */
    public final int getMaxSessionPages()
    {
        return maxSessionPages;
    }

    /**
     * Gets whether the {@link wicket.model.PropertyModel}instances apply
     * formatting by default.
     * 
     * @return whether the {@link wicket.model.PropertyModel}instances apply
     *         formatting by default
     */
    public boolean getPropertyModelDefaultApplyFormatting()
    {
        return propertyModelDefaultApplyFormatting;
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
     * @return Resource watcher with polling frequency determined by setting, or
     *         null if no polling frequency has been set.
     */
    public ModificationWatcher getResourceWatcher()
    {
        if (resourceWatcher == null)
        {
            final Duration pollFrequency = getResourcePollFrequency();
            if (pollFrequency != null)
            {
                resourceWatcher = new ModificationWatcher(pollFrequency);
            }
        }
        return resourceWatcher;
    }

    /**
     * Gets any source code path to use when searching for resources.
     * 
     * @return Returns the sourcePath.
     * @see ApplicationSettings#setSourcePath(Path)
     */
    public final Path getSourcePath()
    {
        return sourcePath;
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
     * Returns true if componentName attributes should be stripped from tags
     * when rendering.
     * 
     * @return Returns the stripComponentNames.
     * @see ApplicationSettings#setStripComponentNames(boolean)
     */
    public final boolean getStripComponentNames()
    {
        return stripComponentNames;
    }

    /**
     * Gets whether to remove wicket tags from the output.
     * 
     * @return whether to remove wicket tags from the output
     */
    public final boolean getStripWicketParamTag()
    {
        return this.stripWicketTags;
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
     * @return Whether to use a default value (if available) when a missing
     *         resource is requested
     */
    public final boolean getUseDefaultOnMissingResource()
    {
        return useDefaultOnMissingResource;
    }

    /**
     * Get the current tag name
     * 
     * @return wicket tag name
     */
    public final String getWicketTagName()
    {
        return this.wicketTagName;
    }

    /**
     * Sets component name attribute in use in this application. Normally, this
     * is "componentName", but it can be changed in the unlikely event that tag
     * attribute naming conflicts arise.
     * 
     * @param componentNameAttribute
     *            The componentNameAttribute to set.
     * @return This
     */
    public final ApplicationSettings setComponentNameAttribute(
            final String componentNameAttribute)
    {
        this.componentNameAttribute = componentNameAttribute;
        return this;
    }

    /**
     * Enables or disables checking for unused components. Component checking is
     * on by defaults and only minor efficiency can be gained from turning it
     * off in a production environment.
     * 
     * @param componentUseCheck
     * @return This
     */
    public final ApplicationSettings setComponentUseCheck(
            boolean componentUseCheck)
    {
        this.componentUseCheck = componentUseCheck;
        return this;
    }

    /**
     * Turns on whitespace compression. Multiple occurrences of space/tab
     * characters will be compressed to a single space. Multiple line breaks
     * newline/carriage-return will also be compressed to a single newline.
     * <p>
     * Compression is currently not HTML aware and so it may be possible for
     * whitespace compression to break pages. For this reason, whitespace
     * compression is off by default and you should test your application
     * throroughly after turning whitespace compression on.
     * <p>
     * Spaces are removed from markup at markup load time and there should be no
     * effect on page rendering speed. In fact, your pages should render faster
     * with whitespace compression enabled.
     * 
     * @param compressWhitespace
     *            The compressWhitespace to set.
     * @return This
     */
    public ApplicationSettings setCompressWhitespace(boolean compressWhitespace)
    {
        this.compressWhitespace = compressWhitespace;
        return this;
    }

    /**
     * Set new Class to be used for de-/encryption
     * 
     * @param crypt
     * @return This
     */
    public ApplicationSettings setCrypt(Class crypt)
    {
        this.cryptClass = crypt;
        return this;
    }

    /**
     * @param defaultAfterDisabledLink
     *            The defaultAfterDisabledLink to set.
     * @return This
     */
    public ApplicationSettings setDefaultAfterDisabledLink(
            String defaultAfterDisabledLink)
    {
        this.defaultAfterDisabledLink = defaultAfterDisabledLink;
        return this;
    }

    /**
     * @param defaultBeforeDisabledLink
     *            The defaultBeforeDisabledLink to set.
     * @return This
     */
    public ApplicationSettings setDefaultBeforeDisabledLink(
            String defaultBeforeDisabledLink)
    {
        this.defaultBeforeDisabledLink = defaultBeforeDisabledLink;
        return this;
    }

    /**
     * Sets the default class resolver to use when finding classes.
     * 
     * @param defaultClassResolver
     *            The default class resolver
     * @return This
     */
    public ApplicationSettings setDefaultClassResolver(
            final IClassResolver defaultClassResolver)
    {
        this.defaultClassResolver = defaultClassResolver;
        return this;
    }

    /**
     * Sets the default factory to be used when creating pages.
     * 
     * @param defaultPageFactory
     *            The default factory
     * @return This
     */
    public ApplicationSettings setDefaultPageFactory(
            final IPageFactory defaultPageFactory)
    {
        this.defaultPageFactory = defaultPageFactory;
        return this;
    }

    /**
     * Set encryption key used to encode/decode PasswordTextFields e.g.
     * 
     * @param encryptionKey
     * @return This
     */
    public ApplicationSettings setEncryptionKey(String encryptionKey)
    {
        this.encryptionKey = encryptionKey;
        return this;
    }

    /**
     * @param exceptionOnMissingResource
     *            Whether to throw an exception when a missing resource is
     *            requested
     * @return This
     */
    public final ApplicationSettings setExceptionOnMissingResource(
            final boolean exceptionOnMissingResource)
    {
        this.exceptionOnMissingResource = exceptionOnMissingResource;
        return this;
    }

    /**
     * @param markupParserClassName
     *            The markupParserClass to set.
     * @return This
     */
    public final ApplicationSettings setMarkupParserClassName(
            final String markupParserClassName)
    {
        this.markupParserClassName = markupParserClassName;
        return this;
    }

    /**
     * Sets the maximum number of pages held in a session. If a page is added to
     * a user's session when the session is full, the oldest page in the session
     * will be expired. The primary purpose of setting a maximum number of pages
     * in a session is to limit the resources consumed by server-side state.
     * 
     * @param maxSessionPages
     *            The maxSessionPages to set.
     * @return This
     */
    public final ApplicationSettings setMaxSessionPages(
            final int maxSessionPages)
    {
        this.maxSessionPages = maxSessionPages;
        return this;
    }

    /**
     * Sets whether the {@link wicket.model.PropertyModel}instances apply
     * formatting by default.
     * 
     * @param propertyModelDefaultApplyFormatting
     *            whether the {@link wicket.model.PropertyModel}instances apply
     *            formatting by default
     * @return This
     */
    public ApplicationSettings setPropertyModelDefaultApplyFormatting(
            boolean propertyModelDefaultApplyFormatting)
    {
        this.propertyModelDefaultApplyFormatting = propertyModelDefaultApplyFormatting;
        return this;
    }

    /**
     * Sets the resource polling frequency. This is the duration of time between
     * checks of resource modification times. If a resource, such as an HTML
     * file, has changed, it will be reloaded. Default is for no resource
     * polling to occur.
     * 
     * @param resourcePollFrequency
     *            Frequency at which to poll resources
     * @return This
     * @see ApplicationSettings#setSourcePath(Path)
     */
    public final ApplicationSettings setResourcePollFrequency(
            final Duration resourcePollFrequency)
    {
        this.resourcePollFrequency = resourcePollFrequency;
        return this;
    }

    /**
     * Sets a source code path to use when searching for resources. Setting a
     * source path can allow developers to "hot update" pages by simply changing
     * markup on the fly and hitting refresh in their browser.
     * 
     * @param sourcePath
     *            The sourcePath to set
     * @return This
     */
    public final ApplicationSettings setSourcePath(final Path sourcePath)
    {
        this.sourcePath = sourcePath;
        return this;
    }


    /**
     * Enables stripping of markup comments denoted in markup by HTML comment
     * tagging.
     * 
     * @param stripComments
     *            True to strip markup comments from rendered pages
     * @return This
     */
    public ApplicationSettings setStripComments(boolean stripComments)
    {
        this.stripComments = stripComments;
        return this;
    }

    /**
     * Determines if componentName attributes should be stripped from tags when
     * rendering. Component name attributes in rendered pages can be a helpful
     * debugging tool, but they are not helpful to end-users and do not increase
     * efficiency in delivering pages over any protocol.
     * 
     * @param stripComponentNames
     *            The stripComponentNames to set.
     * @return This
     */
    public final ApplicationSettings setStripComponentNames(
            final boolean stripComponentNames)
    {
        this.stripComponentNames = stripComponentNames;
        return this;
    }

    /**
     * Sets whether to remove wicket tags from the output.
     * 
     * @param stripWicketTags
     *            whether to remove wicket tags from the output
     * @return This
     */
    public final ApplicationSettings setStripWicketTags(boolean stripWicketTags)
    {
        this.stripWicketTags = stripWicketTags;
        return this;
    }

    /**
     * The exception display type determines how the framework displays
     * exceptions to you as a developer or user.
     * <p>
     * The default value for exception display type is SHOW_EXCEPTION_PAGE. When
     * this value is set and an unhandled runtime exception is thrown by a page,
     * a redirect to a helpful exception display page will occur.
     * <p>
     * This is a developer feature, however, and you may want to instead show an
     * internal error page without developer details that allows a user to start
     * over at the application's home page. This can be accomplished by setting
     * the exception display type to SHOW_INTERNAL_ERROR_PAGE.
     * <p>
     * Finally, if you are having trouble with the exception display pages
     * themselves, you can disable exception displaying entirely with the value
     * SHOW_NO_EXCEPTION_PAGE. This will cause the framework to re-throw any
     * unhandled runtime exceptions after wrapping them in a ServletException
     * wrapper.
     * 
     * @param unexpectedExceptionDisplay
     *            The unexpectedExceptionDisplay to set.
     * @return This
     */
    public final ApplicationSettings setUnexpectedExceptionDisplay(
            final UnexpectedExceptionDisplay unexpectedExceptionDisplay)
    {
        this.unexpectedExceptionDisplay = unexpectedExceptionDisplay;
        return this;
    }

    /**
     * @param useDefaultOnMissingResource
     *            Whether to use a default value (if available) when a missing
     *            resource is requested
     * @return This
     */
    public final ApplicationSettings setUseDefaultOnMissingResource(
            final boolean useDefaultOnMissingResource)
    {
        this.useDefaultOnMissingResource = useDefaultOnMissingResource;
        return this;
    }

    /**
     * Define a new wicket tag name to use instead of "wicket"
     * 
     * @param wicketTagName
     *            the tag name
     * @return This
     */
    public final ApplicationSettings setWicketTagName(final String wicketTagName)
    {
        this.wicketTagName = wicketTagName;
        return this;
    }

    /**
     * Internal method to expose the string resource loaders configured within
     * the settings to the localization helpers that need to work with them.
     * 
     * @return The string resource loaders
     */
    List getStringResourceLoaders()
    {
        return Collections.unmodifiableList(stringResourceLoaders);
    }
}


