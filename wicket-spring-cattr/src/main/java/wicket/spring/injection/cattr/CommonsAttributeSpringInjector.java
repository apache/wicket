package wicket.spring.injection.cattr;

import wicket.injection.ConfigurableInjector;
import wicket.injection.IFieldValueFactory;
import wicket.spring.ISpringContextLocator;

/**
 * Injector that injects classes based on {@link SpringBean} annotation
 *
 * @author Karthik Gurumurthy
 *
 */
public class CommonsAttributeSpringInjector extends ConfigurableInjector
{

	IFieldValueFactory factory;

	/**
	 * Constructor
	 *
	 * @param locator
	 *            spring context locator
	 */
	public CommonsAttributeSpringInjector(ISpringContextLocator locator)
	{
		initFactory(locator);
	}

	private void initFactory(ISpringContextLocator locator)
	{
		factory = new CommonsAttributeProxyFieldValueFactory(locator);
	}

	protected IFieldValueFactory getFieldValueFactory()
	{
		return factory;
	}

}
