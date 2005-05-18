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
package wicket;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.markup.ComponentTag;
import wicket.markup.html.form.persistence.CookieValuePersisterSettings;
import wicket.resource.ApplicationStringResourceLoader;
import wicket.resource.ComponentStringResourceLoader;
import wicket.resource.IStringResourceLoader;
import wicket.util.crypt.SunJceCrypt;
import wicket.util.file.Folder;
import wicket.util.file.Path;
import wicket.util.lang.EnumeratedType;
import wicket.util.parse.metapattern.MetaPattern;
import wicket.util.time.Duration;

/**
 * Contains application settings as property values. All settings exposed are
 * generic to any kind of protocol or markup.
 * <p>
 * Application settings properties:
 * <p>
 * <ul>
 * <i>bufferResponse </i> (defaults to true) - True if the application should
 * buffer responses. This does require some additional memory, but helps keep
 * exception displays accurate because the whole rendering process completes
 * before the page is sent to the user, thus avoiding the possibility of a
 * partially rendered page.
 * <p>
 * <ul>
 * <i>componentIdAttribute </i> (defaults to "wicket") - The markup attribute
 * which denotes the ids of components to be attached
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
 * <i>maxPages </i>- The maximum number of pages in the user's session
 * before old pages are expired.
 * <p>
 * <i>resourcePollFrequency </i> (defaults to no polling frequency) - Frequency
 * at which resources should be polled for changes.
 * <p>
 * <i>resourcePath </i> (no default) - Set this to enable polling of resources on
 * your source path
 * <p>
 * <i>stripComments </i> (defaults to false) - Set to true to strip HTML
 * comments during markup loading
 * <p>
 * <i>stripComponentIds </i> (defaults to false) - Set to true to strip
 * component id attributes during rendering
 * <p>
 * <i>throwExceptionOnMissingResource </i> (defaults to true) - Set to true to
 * throw a runtime exception if a required string resource is not found. Set to
 * false to return the requested resource key surrounded by pairs of question
 * mark characters (e.g. "??missingKey??")
 * <p>
 * <i>useDefaultOnMissingResource </i> (defaults to true) - Set to true to
 * return a default value if available when a required string resource is not
 * found. If set to false then the throwExceptionOnMissingResource flag is used
 * to determine how to behave. If no default is available then this is the same
 * as if this flag were false
 * <p>
 * <i>stringResourceLoaders </i>- A chain of <code>IStringResourceLoader</code>
 * instances that are searched in order to obtain string resources used during
 * localization. By default the chain is set up to first search for resources
 * against a particular component (e.g. page etc.) and then against the
 * application.
 * <p>
 * <i>defaultPageFactory </i>- the factory class that is used for constructing
 * page instances.
 * <p>
 * More documentation is available about each setting in the setter method for
 * the property.
 * 
 * @author Jonathan Locke
 * @author Chris Turner
 * @author Eelco Hillenius
 * @author Juergen Donnerstag
 * @author Johan Compagner
 */
public final class ApplicationSettings
{
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
	 * The render part of a request (opposed to the 'action part' which is either the
	 * construction of a bookmarkable page or the execution of a IRequestListener handler)
	 * is handled by a seperate request by issueing a redirect request to the browser.
	 * This is commonly known as the 'redirect after submit' pattern, though in our case,
	 * we use it for GET and POST requests instead of just the POST requests.
	 * To cancel the client side redirect for a request, users can set the
	 * 'redirect' property of {@link RequestCycle} to false (getRequestCycle.setRedirect(false)).
	 * <p>
	 * This pattern solves the 'refresh' problem. While it is a common feature of browsers
	 * to refresh/ reload a web page, this results in problems in many dynamic web applications.
	 * For example, when you have a link with an event handler that e.g. deletes a row from a list,
	 * you usually want to ignore refresh requests after that link is clicked on. By using
	 * this strategy, the refresh request only results in the re-rendering of the page without
	 * executing the event handler again.
	 * </p>
	 * <p>
	 * Though it solves the refresh problem, it introduces potential problems, as the request
	 * that is logically one, are actually two seperate request. Not only is this less efficient,
	 * but this also can mean that within the same request attachement/ detachement of
	 * models is done twice (in case you use models in the bookmarkable page constructors
	 * and IRequestListener handlers). If you use this strategy, you should be aware of
	 * this possibily, and should also be aware that for one logical request, actually two
	 * instances of RequestCycle are created and processed.
	 * </p>
	 */
	public static final RenderStrategy REDIRECT_TO_RENDER = new RenderStrategy("CLIENT_SIDE_REDIRECT");

	/**
	 * All logical parts of a request (the action and render part) are handled within the
	 * same request. To enable a the client side redirect for a request, users can set the
	 * 'redirect' property of {@link RequestCycle} to true (getRequestCycle.setRedirect(true)),
	 * after which the behaviour will be like RenderStragegy 'REDIRECT_TO_RENDER'.
	 * <p>
	 * Though this strategy is more efficient than the 'REDIRECT_TO_RENDER' strategy,
	 * and doesn't have some of the potential problems of it, it also does not solve the
	 * refresh problem (see javadoc of 'REDIRECT_TO_RENDER' strategy).
	 * </p>
	 */
	public static final RenderStrategy ONE_PASS_RENDER = new RenderStrategy("ONE_PASS_RENDER");

	/**
	 * All logical parts of a request (the action and render part) are handled within the
	 * same request, but instead of streaming the render result to the browser directly,
	 * the result is cached on the server. A client side redirect command is issued to the
	 * browser specifically to render this request.
	 * </p>
	 *  TODO: explain the advantages/ disadvantages.
	 * </p>
	 */
	public static final RenderStrategy REDIRECT_TO_BUFFER = new RenderStrategy("REDIRECT_BUFFER");
	
	/** Log */
	private static final Log log = LogFactory.getLog(ApplicationSettings.class);

	/** The application */
	private Application application;

	/**
	 * The render strategy, defaults to 'REDIRECT_TO_BUFFER'.
	 * This property influences the default way in how a logical request that consists
	 * of an 'action' and a 'render' part is handled, and is mainly used to have a means
	 * to circumvent the 'refresh' problem.
	 */
	private RenderStrategy renderStrategy = REDIRECT_TO_BUFFER;

	/** Application default for automatically resolving hrefs */
	private boolean automaticLinking = false;

	/** True if the response should be buffered */
	private boolean bufferResponse = true;

	/** Wicket namespace to identify wicket components */
	private String wicketNamespace = ComponentTag.DEFAULT_WICKET_NAMESPACE;

	/** True to check that each component on a page is used */
	private boolean componentUseCheck = true;

	/** True if multiple tabs/spaces should be compressed to a single space */
	private boolean compressWhitespace = false;

	/** Default values for persistence of form data (by means of cookies) */
	private CookieValuePersisterSettings cookieValuePersisterSettings = new CookieValuePersisterSettings();

	/** Class of type ICrypt to implement encryption */
	private Class cryptClass = SunJceCrypt.class;

	/** Default markup for after a disabled link */
	private String defaultAfterDisabledLink = "</i>";

	/** Default markup for before a disabled link */
	private String defaultBeforeDisabledLink = "<i>";

	/** Default class resolver to find classes */
	private IClassResolver defaultClassResolver = new DefaultClassResolver();

	/** The default locale to use */
	private Locale defaultLocale = Locale.getDefault();

	/** Default factory to create new Page objects */
	private IPageFactory defaultPageFactory = new DefaultPageFactory();

	/** Encryption key used to encode/decode passwords e.g. */
	private String encryptionKey = "WiCkEt-FRAMEwork";

	/** The maximum number of versions of a page to track */
	private int maxPageVersions = 10;

	/** The maximum number of pages in a session */
	private int maxPages = 10;

	/** True if string resource loaders have been overridden */
	private boolean overriddenStringResourceLoaders = false;

	/** Frequency at which files should be polled */
	private Duration resourcePollFrequency = null;

	/** Filesystem Path to search for resources */
	private Path resourcePath = new Path();

	/** Chain of string resource loaders to use */
	private List stringResourceLoaders = new ArrayList(2);

	/** Should HTML comments be stripped during rendering? */
	private boolean stripComments = false;

	/** If true, wicket tags (<wicket: ..>) and wicket:id attributes 
	 * we be removed from output */
	private boolean stripWicketTags = false;

	/** Flags used to determine how to behave if resources are not found */
	private boolean throwExceptionOnMissingResource = true;

	/** Type of handling for unexpected exceptions */
	private UnexpectedExceptionDisplay unexpectedExceptionDisplay = SHOW_EXCEPTION_PAGE;

	/** Determines behavior of string resource loading if string is missing */
	private boolean useDefaultOnMissingResource = true;

	/** Determines if pages should be managed by a version manager by default */
	private boolean versionPagesByDefault = true;

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
	 * Enumerated type for different ways of handling the render part of requests.
	 */
	public static final class RenderStrategy extends EnumeratedType
	{
		RenderStrategy(final String name)
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
	public ApplicationSettings(final Application application)
	{
		this.application = application;
		stringResourceLoaders.add(new ComponentStringResourceLoader());
		stringResourceLoaders.add(new ApplicationStringResourceLoader(application));
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
	public final ApplicationSettings addStringResourceLoader(final IStringResourceLoader loader)
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
	 * Configures application settings for a given configuration type.
	 * 
	 * @param configurationType
	 *            The configuration type. Must currently be either "development"
	 *            or "deployment".
	 */
	public final void configure(final String configurationType)
	{
		configure(configurationType, "src/java");
	}

	/**
	 * Configures application settings for a given configuration type.
	 * 
	 * @param configurationType
	 *            The configuration type. Must currently be either "development"
	 *            or "deployment".
	 * @param sourceFolder
	 *            If configurationType is "development", then this folder will
	 *            be polled for resource changes, use the system path separator to separate folders
	 * @see File#pathSeparator
	 */
	public final void configure(final String configurationType, final String sourceFolder)
	{
		if ("development".equalsIgnoreCase(configurationType))
		{
			if (sourceFolder != null)
			{
				String[] paths = sourceFolder.split(File.pathSeparator);
				Path path = new Path();
				for (int i = 0; i < paths.length; i++)
				{
					Folder folder = new Folder(paths[i]);
					if (!folder.exists())
					{
						log.warn("Source folder " + folder.getAbsolutePath() + " does not exist.");
					}
					else
						path.add(folder);
				}
				setResourcePath(path);
				setResourcePollFrequency(Duration.ONE_SECOND);
			}
			setComponentUseCheck(true);
			setStripWicketTags(false);
		}
		else if ("deployment".equalsIgnoreCase(configurationType))
		{
			setComponentUseCheck(false);
			setStripWicketTags(true);
		}
		else
		{
			throw new IllegalArgumentException(
					"Invalid configuration type.  Must be \"development\" or \"deployment\".");
		}
	}

	/**
	 * Gets the renderStrategy.
	 * This property influences the default way in how a logical request that consists
	 * of an 'action' and a 'render' part is handled, and is mainly used to have a means
	 * to circumvent the 'refresh' problem.
	 * @return the render strategy
	 */
	public final RenderStrategy getRenderStrategy()
	{
		return renderStrategy;
	}

	/**
	 * Sets the renderStrategy.
	 * This property influences the default way in how a logical request that consists
	 * of an 'action' and a 'render' part is handled, and is mainly used to have a means
	 * to circumvent the 'refresh' problem.
	 * @param renderStrategy the render strategy
	 */
	public final void setRenderStrategy(RenderStrategy renderStrategy)
	{
		this.renderStrategy = renderStrategy;
	}

	/**
	 * If true, automatic link resolution is enabled.
	 * Please @see wicket.AutoLinkResolver and
	 * @see wicket.markup.parser.filter.WicketLinkTagHandler
	 * for more details.
	 * 
	 * @return Returns the automaticLinking.
	 */
	public final boolean getAutomaticLinking()
	{
		return automaticLinking;
	}

	/**
	 * @return True if this application buffers its responses
	 */
	public final boolean getBufferResponse()
	{
		return bufferResponse;
	}

	/**
	 * Gets wicket namespace in use in this application. Normally, this is
	 * "wicket", but it can be changed in the unlikely event of nameing 
	 * conflicts or for use by of pre-processors e.g. 
	 * 
	 * @return The current wicket namespace
	 * @see ApplicationSettings#setWicketNamespace(String)
	 */
	public final String getWicketNamespace()
	{
		return wicketNamespace;
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
	public final boolean getCompressWhitespace()
	{
		return compressWhitespace;
	}

	/**
	 * Get the defaults to be used by persistence manager
	 * 
	 * @return CookieValuePersisterSettings
	 */
	public final CookieValuePersisterSettings getCookieValuePersisterSettings()
	{
		return cookieValuePersisterSettings;
	}

	/**
	 * @return Returns the cryptClass.
	 */
	public final Class getCryptClass()
	{
		return cryptClass;
	}

	/**
	 * @return Returns the defaultAfterDisabledLink.
	 */
	public final String getDefaultAfterDisabledLink()
	{
		return defaultAfterDisabledLink;
	}

	/**
	 * @return Returns the defaultBeforeDisabledLink.
	 */
	public final String getDefaultBeforeDisabledLink()
	{
		return defaultBeforeDisabledLink;
	}

	/**
	 * Gets the default resolver to use when finding classes
	 * 
	 * @return Default class resolver
	 */
	public final IClassResolver getDefaultClassResolver()
	{
		return defaultClassResolver;
	}

	/**
	 * @return Returns the defaultLocale.
	 */
	public final Locale getDefaultLocale()
	{
		return defaultLocale;
	}

	/**
	 * Gets the default factory to be used when creating pages
	 * 
	 * @return The default page factory
	 */
	public final IPageFactory getDefaultPageFactory()
	{
		return defaultPageFactory;
	}

	/**
	 * Get encryption key used to encode/decode passwords e.g.
	 * 
	 * @return encryption key
	 */
	public final String getEncryptionKey()
	{
		return encryptionKey;
	}

	/**
	 * @return Returns the maxPageVersions.
	 */
	public final int getMaxPageVersions()
	{
		return maxPageVersions;
	}

	/**
	 * Gets the maximum number of pages held in a session.
	 * 
	 * @return Returns the maxPages.
	 * @see ApplicationSettings#setMaxPages(int)
	 */
	public final int getMaxPages()
	{
		return maxPages;
	}

	/**
	 * @return Returns the resourcePollFrequency.
	 * @see ApplicationSettings#setResourcePollFrequency(Duration)
	 */
	public final Duration getResourcePollFrequency()
	{
		return resourcePollFrequency;
	}

	/**
	 * Gets any filesystem path to use when searching for resources.
	 * 
	 * @return Returns the resourcePath.
	 * @see ApplicationSettings#setResourcePath(Path)
	 */
	public final Path getResourcePath()
	{
		return resourcePath;
	}

	/**
	 * @return Returns the stripComments.
	 * @see ApplicationSettings#setStripComments(boolean)
	 */
	public final boolean getStripComments()
	{
		return stripComments;
	}

	/**
	 * Gets whether to remove wicket tags from the output.
	 * 
	 * @return whether to remove wicket tags from the output
	 */
	public final boolean getStripWicketTags()
	{
		return this.stripWicketTags;
	}

	/**
	 * @return Whether to throw an exception when a missing resource is
	 *         requested
	 */
	public final boolean getThrowExceptionOnMissingResource()
	{
		return throwExceptionOnMissingResource;
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
	 * @return Returns the pagesVersionedByDefault.
	 */
	public final boolean getVersionPagesByDefault()
	{
		return versionPagesByDefault;
	}

	/**
	 * Application default for automatic link resolution.
	 * Please @see wicket.AutoLinkResolver and
	 * @see wicket.markup.parser.filter.WicketLinkTagHandler
	 * for more details.
	 * 
	 * @param automaticLinking
	 *            The automaticLinking to set.
	 */
	public final void setAutomaticLinking(boolean automaticLinking)
	{
		this.automaticLinking = automaticLinking;
	}

	/**
	 * @param bufferResponse
	 *            True if this application should buffer responses.
	 */
	public final void setBufferResponse(boolean bufferResponse)
	{
		this.bufferResponse = bufferResponse;
	}

	/**
	 * Sets wicket namespace in use in this application. Normally, this is
	 * "wicket", but it can be changed in the unlikely event of
	 * nameing conflicts.
	 * 
	 * @param namespace
	 *            The wicket namespace to set.
	 * @return This
	 */
	public final ApplicationSettings setWicketNamespace(final String namespace)
	{
		if (!MetaPattern.VARIABLE_NAME.matcher(namespace).matches())
		{
			throw new IllegalArgumentException(
					"Wicket namespace must be a valid variable name ([a-z][a-z0-9_]*)");
		}

		this.wicketNamespace = namespace;
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
	public final ApplicationSettings setComponentUseCheck(final boolean componentUseCheck)
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
	public final ApplicationSettings setCompressWhitespace(final boolean compressWhitespace)
	{
		this.compressWhitespace = compressWhitespace;
		return this;
	}

	/**
	 * @param cookieValuePersisterSettings
	 *            The cookieValuePersisterSettings to set.
	 */
	public final void setCookieValuePersisterSettings(
			CookieValuePersisterSettings cookieValuePersisterSettings)
	{
		this.cookieValuePersisterSettings = cookieValuePersisterSettings;
	}

	/**
	 * Set new Class to be used for de-/encryption
	 * 
	 * @param crypt
	 * @return This
	 */
	public final ApplicationSettings setCryptClass(Class crypt)
	{
		this.cryptClass = crypt;
		return this;
	}

	/**
	 * @param defaultAfterDisabledLink
	 *            The defaultAfterDisabledLink to set.
	 * @return This
	 */
	public final ApplicationSettings setDefaultAfterDisabledLink(
			final String defaultAfterDisabledLink)
	{
		this.defaultAfterDisabledLink = defaultAfterDisabledLink;
		return this;
	}

	/**
	 * @param defaultBeforeDisabledLink
	 *            The defaultBeforeDisabledLink to set.
	 * @return This
	 */
	public final ApplicationSettings setDefaultBeforeDisabledLink(String defaultBeforeDisabledLink)
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
	public final ApplicationSettings setDefaultClassResolver(
			final IClassResolver defaultClassResolver)
	{
		this.defaultClassResolver = defaultClassResolver;
		return this;
	}

	/**
	 * @param defaultLocale
	 *            The defaultLocale to set.
	 */
	public final void setDefaultLocale(Locale defaultLocale)
	{
		this.defaultLocale = defaultLocale;
	}

	/**
	 * Sets the default factory to be used when creating pages.
	 * 
	 * @param defaultPageFactory
	 *            The default factory
	 * @return This
	 */
	public final ApplicationSettings setDefaultPageFactory(final IPageFactory defaultPageFactory)
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
	public final ApplicationSettings setEncryptionKey(String encryptionKey)
	{
		this.encryptionKey = encryptionKey;
		return this;
	}

	/**
	 * @param maxPageVersions
	 *            The maxPageVersion to set.
	 */
	public final void setMaxPageVersions(int maxPageVersions)
	{
		if (maxPageVersions < 0)
		{
			throw new IllegalArgumentException("Value for maxPageVersions must be >= 0");
		}
		this.maxPageVersions = maxPageVersions;
	}

	/**
	 * Sets the maximum number of pages held in a session. If a page is added to
	 * a user's session when the session is full, the oldest page in the session
	 * will be expired. The primary purpose of setting a maximum number of pages
	 * in a session is to limit the resources consumed by server-side state.
	 * 
	 * @param maxPages
	 *            The maxPages to set.
	 * @return This
	 */
	public final ApplicationSettings setMaxPages(final int maxPages)
	{
		if (maxPages < 1)
		{
			throw new IllegalArgumentException("Value for maxPages must be >= 1");
		}
		this.maxPages = maxPages;
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
	 * @see ApplicationSettings#setResourcePath(Path)
	 */
	public final ApplicationSettings setResourcePollFrequency(final Duration resourcePollFrequency)
	{
		this.resourcePollFrequency = resourcePollFrequency;
		return this;
	}

	/**
	 * Sets a source code path to use when searching for resources. Setting a
	 * source path can allow developers to "hot update" pages by simply changing
	 * markup on the fly and hitting refresh in their browser.
	 * 
	 * @param resourcePath
	 *            The resourcePath to set
	 * @return This
	 */
	public final ApplicationSettings setResourcePath(final Path resourcePath)
	{
		this.resourcePath = resourcePath;

		// Cause resource locator to get recreated
		application.resourcePathChanged();

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
	public final ApplicationSettings setStripComments(boolean stripComments)
	{
		this.stripComments = stripComments;
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
	 * @param throwExceptionOnMissingResource
	 *            Whether to throw an exception when a missing resource is
	 *            requested
	 * @return This
	 */
	public final ApplicationSettings setThrowExceptionOnMissingResource(
			final boolean throwExceptionOnMissingResource)
	{
		this.throwExceptionOnMissingResource = throwExceptionOnMissingResource;
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
	 * @param pagesVersionedByDefault
	 *            The pagesVersionedByDefault to set.
	 */
	public final void setVersionPagesByDefault(boolean pagesVersionedByDefault)
	{
		this.versionPagesByDefault = pagesVersionedByDefault;
	}

	/**
	 * Internal method to expose the string resource loaders configured within
	 * the settings to the localization helpers that need to work with them.
	 * 
	 * @return The string resource loaders
	 */
	final List getStringResourceLoaders()
	{
		return Collections.unmodifiableList(stringResourceLoaders);
	}
}