package wicket.spring.test;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.core.io.Resource;

/**
 * Mock application context object. This mock context allows easy creation of
 * unit tests by allowing the user to put bean instances into the context.
 * 
 * Only getBean(String), getBean(String, Class), and getBeansOfType(Class) are
 * implemented so far. Any other method throws
 * {@link UnsupportedOperationException}.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
// TODO finish implementing all needed methods
public class ApplicationContextMock implements ApplicationContext, Serializable
{
	private Map<String, Object> beans = new HashMap<String, Object>();

	/**
	 * puts bean with the given name into the context
	 * 
	 * @param name
	 * @param bean
	 */
	public void putBean(String name, Object bean)
	{
		if (beans.containsKey(name))
		{
			throw new IllegalArgumentException("a bean with name ["
					+ name + "] has alredy been added to the context");
		}
		beans.put(name, bean);
	}

	/**
	 * puts bean with into the context. bean object's class name will be used as
	 * the bean name.
	 * 
	 * @param bean
	 */
	public void putBean(Object bean)
	{
		putBean(bean.getClass().getName(), bean);
	}

	/**
	 * @see org.springframework.beans.factory.BeanFactory#getBean(java.lang.String)
	 */
	public Object getBean(String name) throws BeansException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.beans.factory.BeanFactory#getBean(java.lang.String,
	 *      java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public Object getBean(String name, Class requiredType) throws BeansException
	{
		Object bean = beans.get(name);
		if (bean == null)
		{
			throw new NoSuchBeanDefinitionException(requiredType, name);
		}
		if (!(requiredType.isAssignableFrom(bean.getClass())))
		{
			throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
		}
		return bean;
	}

	/**
	 * @see org.springframework.beans.factory.ListableBeanFactory#getBeansOfType(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getBeansOfType(Class type) throws BeansException
	{
		Map<String, Object> found = new HashMap<String, Object>();

		Iterator<Entry<String,Object>> it = beans.entrySet().iterator();
		while (it.hasNext())
		{
			final Map.Entry<String,Object> entry = it.next();
			if (type.isAssignableFrom(entry.getValue().getClass()))
			{
				found.put(entry.getKey(), entry.getValue());
			}
		}

		return found;
	}

	/**
	 * @see org.springframework.context.ApplicationContext#getParent()
	 */
	public ApplicationContext getParent()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.context.ApplicationContext#getDisplayName()
	 */
	public String getDisplayName()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.context.ApplicationContext#getStartupDate()
	 */
	public long getStartupDate()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.context.ApplicationContext#publishEvent(org.springframework.context.ApplicationEvent)
	 */
	public void publishEvent(ApplicationEvent event)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.beans.factory.ListableBeanFactory#containsBeanDefinition(java.lang.String)
	 */
	public boolean containsBeanDefinition(String beanName)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.beans.factory.ListableBeanFactory#getBeanDefinitionCount()
	 */
	public int getBeanDefinitionCount()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.beans.factory.ListableBeanFactory#getBeanDefinitionNames()
	 */
	public String[] getBeanDefinitionNames()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.beans.factory.ListableBeanFactory#getBeanDefinitionNames(java.lang.Class)
	 */
	public String[] getBeanDefinitionNames(Class type)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.beans.factory.ListableBeanFactory#getBeanNamesForType(java.lang.Class)
	 */
	public String[] getBeanNamesForType(Class type)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.beans.factory.ListableBeanFactory#getBeanNamesForType(java.lang.Class,
	 *      boolean, boolean)
	 */
	public String[] getBeanNamesForType(Class type, boolean includePrototypes,
			boolean includeFactoryBeans)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.beans.factory.ListableBeanFactory#getBeansOfType(java.lang.Class,
	 *      boolean, boolean)
	 */
	public Map getBeansOfType(Class type, boolean includePrototypes,
			boolean includeFactoryBeans) throws BeansException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.beans.factory.BeanFactory#containsBean(java.lang.String)
	 */
	public boolean containsBean(String name)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.beans.factory.BeanFactory#isSingleton(java.lang.String)
	 */
	public boolean isSingleton(String name) throws NoSuchBeanDefinitionException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.beans.factory.BeanFactory#getType(java.lang.String)
	 */
	public Class getType(String name) throws NoSuchBeanDefinitionException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.beans.factory.BeanFactory#getAliases(java.lang.String)
	 */
	public String[] getAliases(String name) throws NoSuchBeanDefinitionException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.beans.factory.HierarchicalBeanFactory#getParentBeanFactory()
	 */
	public BeanFactory getParentBeanFactory()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.context.MessageSource#getMessage(java.lang.String,
	 *      java.lang.Object[], java.lang.String, java.util.Locale)
	 */
	public String getMessage(String code, Object[] args, String defaultMessage,
			Locale locale)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.context.MessageSource#getMessage(java.lang.String,
	 *      java.lang.Object[], java.util.Locale)
	 */
	public String getMessage(String code, Object[] args, Locale locale)
			throws NoSuchMessageException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.context.MessageSource#getMessage(org.springframework.context.MessageSourceResolvable,
	 *      java.util.Locale)
	 */
	public String getMessage(MessageSourceResolvable resolvable, Locale locale)
			throws NoSuchMessageException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.core.io.support.ResourcePatternResolver#getResources(java.lang.String)
	 */
	public Resource[] getResources(String locationPattern) throws IOException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.core.io.ResourceLoader#getResource(java.lang.String)
	 */
	public Resource getResource(String location)
	{
		throw new UnsupportedOperationException();
	}

}
