package org.apache.wicket.proxy;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.core.NamingPolicy;
import org.apache.wicket.proxy.LazyInitProxyFactory.IWriteReplace;
import org.apache.wicket.proxy.LazyInitProxyFactory.ObjenesisCGLibInterceptor;
import org.objenesis.ObjenesisStd;

import java.io.Serializable;

public class ObjenesisProxyFactory
{

	private static final ObjenesisStd OBJENESIS = new ObjenesisStd(false);

	public static Object createProxy(final Class<?> type, final IProxyTargetLocator locator, NamingPolicy namingPolicy)
	{
		ObjenesisCGLibInterceptor handler = new ObjenesisCGLibInterceptor(type, locator);

		Enhancer e = new Enhancer();
		e.setInterfaces(new Class[]{Serializable.class, ILazyInitProxy.class, IWriteReplace.class});
		e.setSuperclass(type);
		e.setCallbackType(handler.getClass());
		e.setNamingPolicy(namingPolicy);
		e.setUseCache(false);

		Class<?> proxyClass = e.createClass();
		Enhancer.registerCallbacks(proxyClass, new Callback[]{handler});
		return OBJENESIS.newInstance(proxyClass);
	}
}
