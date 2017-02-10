package org.apache.wicket.core.util.reflection;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.wicket.core.util.lang.IPropertyResolver;
import org.apache.wicket.core.util.lang.PropertyResolverConverter;
import org.apache.wicket.util.lang.Generics;

/**
 * A wrapper for another {@link IPropertyLocator} that caches results of
 * {@link #get(Class, String)}.
 */
public class CachingPropertyLocator  implements IPropertyResolver
{
	private final ConcurrentHashMap<String, IGetAndSet> map = Generics.newConcurrentHashMap(16);

	/**
	 * Special token to put into the cache representing no located {@link IGetAndSet}.
	 */
	private IGetAndSet NONE = new AbstractGetAndSet()
	{

		@Override
		public Object getValue(Object object)
		{
			return null;
		}

		@Override
		public Object newValue(Object object)
		{
			return null;
		}

		@Override
		public void setValue(Object object, Object value, PropertyResolverConverter converter)
		{
		}
	};

	private IPropertyResolver resolver;

	public CachingPropertyLocator(IPropertyResolver locator)
	{
		this.resolver = locator;
	}

	public IGetAndSet get(Class<?> clz, String exp)
	{
		String key = clz.getName() + "#" + exp;

		IGetAndSet located = map.get(key);
		if (located == null)
		{
			located = resolver.get(clz, exp);
			if (located == null)
			{
				located = NONE;
			}
			map.put(key, located);
		}

		if (located == NONE)
		{
			located = null;
		}

		return located;
	}
}