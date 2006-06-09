package wicket.extensions.injection;

/**
 * Abstract injector that allows subclasses to provide IFieldValueFactory
 * pragmatically by implementing getFieldValueFactory(). Allows for injectors
 * that can be used with inject(Object obj) call instead of inject(Object obj,
 * IFieldValueFactory factory), thereby allowing for default factories.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public abstract class ConfigurableInjector extends Injector
{
	/**
	 * Injects proxies using IFieldValueFactory obtained by calling
	 * getFieldValueFactory() method
	 * 
	 * @param object
	 *            object to be injected
	 * @return Object that was injected - used for chainig
	 */
	public Object inject(Object object)
	{
		return inject(object, getFieldValueFactory());
	}

	/**
	 * Return the field value factory that will be used to inject objects
	 * 
	 * @return field value locator factory that will be used to inject objects
	 */
	abstract protected IFieldValueFactory getFieldValueFactory();
}
