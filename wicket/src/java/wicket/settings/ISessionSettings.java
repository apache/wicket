package wicket.settings;

import wicket.IPageFactory;
import wicket.session.ISessionStoreFactory;
import wicket.session.pagemap.IPageMapEvictionStrategy;

/**
 * Interface for session related settings
 * <p>
 * <i>pageFactory </i>- The factory class that is used for constructing page
 * instances.
 * <p>
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public interface ISessionSettings
{
	/**
	 * Gets the factory to be used when creating pages
	 * 
	 * @return The default page factory
	 */
	IPageFactory getPageFactory();

	/**
	 * Gets the strategy for evicting pages from the page map.
	 * 
	 * @return the strategy for evicting pages from the page map
	 */
	IPageMapEvictionStrategy getPageMapEvictionStrategy();

	/**
	 * Gets the factory for session stores.
	 * 
	 * @return the factory for session stores
	 */
	ISessionStoreFactory getSessionStoreFactory();

	/**
	 * Sets the factory to be used when creating pages.
	 * 
	 * @param pageFactory
	 *            The default factory
	 */
	void setPageFactory(final IPageFactory pageFactory);

	/**
	 * Sets the strategy for evicting pages from the page map.
	 * 
	 * @param pageMapEvictionStrategy
	 *            the strategy for evicting pages from the page map
	 */
	void setPageMapEvictionStrategy(IPageMapEvictionStrategy pageMapEvictionStrategy);

	/**
	 * Sets the factory for session stores.
	 * 
	 * @param sessionStoreFactory
	 *            the factory for session stores
	 */
	void setSessionStoreFactory(ISessionStoreFactory sessionStoreFactory);
}