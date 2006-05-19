package wicket.settings;

import wicket.IPageFactory;
import wicket.session.pagemap.IPageMapEvictionStrategy;

/**
 * Interface for session related settings
 * <p>
 * <i>pageFactory </i>- The factory class that is used for constructing page
 * instances.
 * <p>
 * <i>pageMapEvictionStrategy </i>- The strategy for evicting pages from page
 * maps when they are too full
 * <p>
 * <i>maxPageMaps </i>- The maximum number of page maps allowed in a session (to
 * prevent denial of service attacks)
 * <p>
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public interface ISessionSettings
{
	/**
	 * Gets maximum number of page maps allowed in this session
	 * 
	 * @return Maximum number of page maps
	 */
	int getMaxPageMaps();

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
	 * Sets maximum number of page maps allowed in this session
	 * 
	 * @param maxPageMaps
	 *            Maximum number of page maps
	 */
	void setMaxPageMaps(int maxPageMaps);

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
}