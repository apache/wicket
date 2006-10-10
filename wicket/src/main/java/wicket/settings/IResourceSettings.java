package wicket.settings;

import java.util.List;
import java.util.Locale;

import wicket.IResourceFactory;
import wicket.Localizer;
import wicket.markup.html.IPackageResourceGuard;
import wicket.markup.html.PackageResourceGuard;
import wicket.model.IModel;
import wicket.resource.IPropertiesFactory;
import wicket.resource.loader.IStringResourceLoader;
import wicket.util.file.IResourceFinder;
import wicket.util.resource.locator.IResourceStreamLocator;
import wicket.util.time.Duration;
import wicket.util.watch.ModificationWatcher;

/**
 * Interface for resource related settings
 * <p>
 * <i>resourcePollFrequency </i> (defaults to no polling frequency) - Frequency
 * at which resources should be polled for changes.
 * <p>
 * <i>resourceFinder </i> (classpath) - Set this to alter the search path for
 * resources.
 * <p>
 * <i>useDefaultOnMissingResource </i> (defaults to true) - Set to true to
 * return a default value if available when a required string resource is not
 * found. If set to false then the throwExceptionOnMissingResource flag is used
 * to determine how to behave. If no default is available then this is the same
 * as if this flag were false
 * <p>
 * <i>A ResourceStreamLocator </i>- An Application's ResourceStreamLocator is
 * used to find resources such as images or markup files. You can supply your
 * own ResourceStreamLocator if your prefer to store your application's
 * resources in a non-standard location (such as a different filesystem
 * location, a particular JAR file or even a database) by overriding the
 * getResourceLocator() method.
 * <p>
 * <i>Resource Factories </i>- Resource factories can be used to create
 * resources dynamically from specially formatted HTML tag attribute values. For
 * more details, see {@link IResourceFactory},
 * {@link wicket.markup.html.image.resource.DefaultButtonImageResourceFactory}
 * and especially
 * {@link wicket.markup.html.image.resource.LocalizedImageResource}.
 * <p>
 * <i>A Localizer </i> The getLocalizer() method returns an object encapsulating
 * all of the functionality required to access localized resources. For many
 * localization problems, even this will not be required, as there are
 * convenience methods available to all components:
 * {@link wicket.Component#getString(String key)} and
 * {@link wicket.Component#getString(String key, IModel model)}.
 * <p>
 * <i>stringResourceLoaders </i>- A chain of <code>IStringResourceLoader</code>
 * instances that are searched in order to obtain string resources used during
 * localization. By default the chain is set up to first search for resources
 * against a particular component (e.g. page etc.) and then against the
 * application.
 * </p>
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public interface IResourceSettings
{
	/**
	 * Adds a resource factory to the list of factories to consult when
	 * generating resources automatically
	 * 
	 * @param name
	 *            The name to give to the factory
	 * @param resourceFactory
	 *            The resource factory to add
	 */
	void addResourceFactory(final String name, final IResourceFactory resourceFactory);

	/**
	 * Convenience method that sets the resource search path to a single folder.
	 * use when searching for resources. By default, the resources are located
	 * on the classpath. If you want to configure other, additional, search
	 * paths, you can use this method
	 * 
	 * @param resourceFolder
	 *            The resourceFolder to set
	 */
	void addResourceFolder(final String resourceFolder);

	/**
	 * Add a string resource loader to the chain of loaders. If this is the
	 * first call to this method since the creation of the application settings
	 * then the existing chain is cleared before the new loader is added.
	 * 
	 * @param loader
	 *            The loader to be added
	 */
	void addStringResourceLoader(final IStringResourceLoader loader);

	/**
	 * @return Returns the defaultLocale.
	 */
	Locale getDefaultLocale();

	/**
	 * Get the application's localizer.
	 * 
	 * @see IResourceSettings#addStringResourceLoader(wicket.resource.loader.IStringResourceLoader)
	 *      for means of extending the way Wicket resolves keys to localized
	 *      messages.
	 * 
	 * @return The application wide localizer instance
	 */
	Localizer getLocalizer();

	/**
	 * Gets the {@link PackageResourceGuard package resource guard}.
	 * 
	 * @return The package resource guard
	 */
	IPackageResourceGuard getPackageResourceGuard();

	/**
	 * Get the property factory which will be used to load property files
	 * 
	 * @return PropertiesFactory
	 */
	IPropertiesFactory getPropertiesFactory();

	/**
	 * @param name
	 *            Name of the factory to get
	 * @return The IResourceFactory with the given name.
	 */
	IResourceFactory getResourceFactory(final String name);

	/**
	 * Gets the resource finder to use when searching for resources.
	 * 
	 * @return Returns the resourceFinder.
	 * @see IResourceSettings#setResourceFinder(IResourceFinder)
	 */
	IResourceFinder getResourceFinder();

	/**
	 * @return Returns the resourcePollFrequency.
	 * @see IResourceSettings#setResourcePollFrequency(Duration)
	 */
	Duration getResourcePollFrequency();

	/**
	 * @return Resource locator for this application
	 */
	IResourceStreamLocator getResourceStreamLocator();

	/**
	 * @param start
	 *            boolean if the resource watcher should be started if not
	 *            already started.
	 * 
	 * @return Resource watcher with polling frequency determined by setting, or
	 *         null if no polling frequency has been set.
	 */
	ModificationWatcher getResourceWatcher(boolean start);

	/**
	 * @return an unmodifiable list of all available string resource loaders
	 */
	List<IStringResourceLoader> getStringResourceLoaders();

	/**
	 * @see wicket.settings.IExceptionSettings#getThrowExceptionOnMissingResource()
	 * 
	 * @return boolean
	 */
	boolean getThrowExceptionOnMissingResource();

	/**
	 * @return Whether to use a default value (if available) when a missing
	 *         resource is requested
	 */
	boolean getUseDefaultOnMissingResource();

	/**
	 * @param defaultLocale
	 *            The defaultLocale to set.
	 */
	void setDefaultLocale(Locale defaultLocale);

	/**
	 * Sets the {@link PackageResourceGuard package resource guard}.
	 * 
	 * @param packageResourceGuard
	 *            The package resource guard
	 */
	void setPackageResourceGuard(IPackageResourceGuard packageResourceGuard);

	/**
	 * Set the property factory which will be used to load property files
	 * 
	 * @param factory
	 */
	void setPropertiesFactory(IPropertiesFactory factory);

	/**
	 * Sets the finder to use when searching for resources. By default, the
	 * resources are located on the classpath. If you want to configure other,
	 * additional, search paths, you can use this method.
	 * 
	 * @param resourceFinder
	 *            The resourceFinder to set
	 */
	void setResourceFinder(final IResourceFinder resourceFinder);

	/**
	 * Sets the resource polling frequency. This is the duration of time between
	 * checks of resource modification times. If a resource, such as an HTML
	 * file, has changed, it will be reloaded. Default is for no resource
	 * polling to occur.
	 * 
	 * @param resourcePollFrequency
	 *            Frequency at which to poll resources
	 * @see IResourceSettings#setResourceFinder(IResourceFinder)
	 */
	void setResourcePollFrequency(final Duration resourcePollFrequency);

	/**
	 * Sets the resource stream locator for this application
	 * 
	 * @param resourceStreamLocator
	 *            new resource stream locator
	 */
	void setResourceStreamLocator(IResourceStreamLocator resourceStreamLocator);

	/**
	 * @see wicket.settings.IExceptionSettings#setThrowExceptionOnMissingResource(boolean)
	 * 
	 * @param throwExceptionOnMissingResource
	 */
	void setThrowExceptionOnMissingResource(final boolean throwExceptionOnMissingResource);

	/**
	 * @param useDefaultOnMissingResource
	 *            Whether to use a default value (if available) when a missing
	 *            resource is requested
	 */
	void setUseDefaultOnMissingResource(final boolean useDefaultOnMissingResource);
}