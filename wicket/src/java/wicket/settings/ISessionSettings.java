package wicket.settings;

import wicket.IPageFactory;
import wicket.session.pagemap.IPageMapEvictionStrategy;
import wicket.util.convert.IConverterFactory;

/**
 * Interface for session related settings *
 * <p>
 * <i>pageFactory </i>- The factory class that is used for constructing
 * page instances.
 * <p>
 * <p>
 * <b>A Converter Factory </b>- By overriding getConverterFactory(), you can
 * provide your own factory which creates locale sensitive Converter instances.
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
	 * Gets pageMapEvictionStrategy.
	 * 
	 * @return pageMapEvictionStrategy
	 */
	IPageMapEvictionStrategy getPageMapEvictionStrategy();

	/**
	 * Sets the factory to be used when creating pages.
	 * 
	 * @param pageFactory
	 *            The default factory
	 * @return This
	 */
	IPageSettings setPageFactory(final IPageFactory pageFactory);

	/**
	 * Sets pageMapEvictionStrategy.
	 * 
	 * @param pageMapEvictionStrategy
	 *            pageMapEvictionStrategy
	 */
	void setPageMapEvictionStrategy(IPageMapEvictionStrategy pageMapEvictionStrategy);

	/**
	 * Gets the converter factory.
	 * 
	 * @return the converter factory
	 */
	IConverterFactory getConverterFactory();

	/**
	 * Sets converter factory
	 * 
	 * @param factory
	 *            new factory
	 */
	void setConverterFactory(IConverterFactory factory);
}