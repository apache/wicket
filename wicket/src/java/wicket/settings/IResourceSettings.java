package wicket.settings;

import java.util.Locale;

import wicket.IResourceFactory;
import wicket.resource.PropertiesFactory;
import wicket.resource.loader.IStringResourceLoader;
import wicket.util.file.IResourceFinder;
import wicket.util.resource.locator.ResourceStreamLocator;
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
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public interface IResourceSettings
{
	/**
	 * @return Resource locator for this application
	 */
	ResourceStreamLocator getResourceStreamLocator();

	/**
	 * Sets the resource stream locator for this application
	 * 
	 * @param resourceStreamLocator
	 *            new resource stream locator
	 */
	void setResourceStreamLocator(ResourceStreamLocator resourceStreamLocator);


	/**
	 * @return Whether to use a default value (if available) when a missing
	 *         resource is requested
	 */
	boolean getUseDefaultOnMissingResource();

	/**
	 * @param useDefaultOnMissingResource
	 *            Whether to use a default value (if available) when a missing
	 *            resource is requested
	 */
	void setUseDefaultOnMissingResource(final boolean useDefaultOnMissingResource);

	/**
	 * Convenience method that sets the resource search path to a single folder.
	 * use when searching for resources. By default, the resources are located
	 * on the classpath. If you want to configure other, additional, search
	 * paths, you can use this method
	 * 
	 * @param resourceFolder
	 *            The resourceFolder to set
	 * @return This
	 */
	IPageSettings addResourceFolder(final String resourceFolder);

	/**
	 * Add a string resource loader to the chain of loaders. If this is the
	 * first call to this method since the creation of the application settings
	 * then the existing chain is cleared before the new loader is added.
	 * 
	 * @param loader
	 *            The loader to be added
	 * @return This
	 */
	IPageSettings addStringResourceLoader(final IStringResourceLoader loader);

	/**
	 * @return Returns the defaultLocale.
	 */
	Locale getDefaultLocale();

	/**
	 * Gets the resource finder to use when searching for resources. If no
	 * resource finder has been set explicitly via setResourceFinder(), the
	 * factory method newResourceFinder() will be called to create a resource
	 * finder.
	 * 
	 * @return Returns the resourceFinder.
	 * @see Settings#setResourceFinder(IResourceFinder)
	 */
	IResourceFinder getResourceFinder();

	/**
	 * @return Returns the resourcePollFrequency.
	 * @see Settings#setResourcePollFrequency(Duration)
	 */
	Duration getResourcePollFrequency();

	/**
	 * @see wicket.settings.IExceptionSettings#getThrowExceptionOnMissingResource()
	 * 
	 * @return boolean
	 */
	boolean getThrowExceptionOnMissingResource();

	/**
	 * @param defaultLocale
	 *            The defaultLocale to set.
	 */
	void setDefaultLocale(Locale defaultLocale);

	/**
	 * Sets the finder to use when searching for resources. By default, the
	 * resources are located on the classpath. If you want to configure other,
	 * additional, search paths, you can use this method.
	 * 
	 * @param resourceFinder
	 *            The resourceFinder to set
	 * @return This
	 */
	IPageSettings setResourceFinder(final IResourceFinder resourceFinder);

	/**
	 * Sets the resource polling frequency. This is the duration of time between
	 * checks of resource modification times. If a resource, such as an HTML
	 * file, has changed, it will be reloaded. Default is for no resource
	 * polling to occur.
	 * 
	 * @param resourcePollFrequency
	 *            Frequency at which to poll resources
	 * @return This
	 * @see Settings#setResourceFinder(IResourceFinder)
	 */
	IPageSettings setResourcePollFrequency(final Duration resourcePollFrequency);

	/**
	 * @see wicket.settings.IExceptionSettings#setThrowExceptionOnMissingResource(boolean)
	 * 
	 * @param throwExceptionOnMissingResource
	 */
	void setThrowExceptionOnMissingResource(final boolean throwExceptionOnMissingResource);

	/**
	 * @return Resource watcher with polling frequency determined by setting, or
	 *         null if no polling frequency has been set.
	 */
	ModificationWatcher getResourceWatcher();

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
	 * @param name
	 *            Name of the factory to get
	 * @return The IResourceFactory with the given name.
	 */
	IResourceFactory getResourceFactory(final String name);

	/**
	 * Get the property factory which will be used to load property files
	 * 
	 * @return PropertiesFactory
	 */
	PropertiesFactory getPropertiesFactory();

	/**
	 * Set the property factory which will be used to load property files
	 * 
	 * @param factory
	 */
	void setPropertiesFactory(PropertiesFactory factory);
}