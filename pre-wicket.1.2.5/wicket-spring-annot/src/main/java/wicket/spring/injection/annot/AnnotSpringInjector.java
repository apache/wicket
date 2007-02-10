package wicket.spring.injection.annot;

import wicket.injection.ConfigurableInjector;
import wicket.injection.IFieldValueFactory;
import wicket.spring.ISpringContextLocator;

/**
 * Injector that injects classes based on {@link SpringBean} annotation
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class AnnotSpringInjector extends ConfigurableInjector
{

	IFieldValueFactory factory;

	/**
	 * Constructor
	 * 
	 * @param locator
	 *            spring context locator
	 */
	public AnnotSpringInjector(ISpringContextLocator locator)
	{
		initFactory(locator);
	}

	private void initFactory(ISpringContextLocator locator)
	{
		factory = new AnnotProxyFieldValueFactory(locator);
	}

	@Override
	protected IFieldValueFactory getFieldValueFactory()
	{
		return factory;
	}

}
