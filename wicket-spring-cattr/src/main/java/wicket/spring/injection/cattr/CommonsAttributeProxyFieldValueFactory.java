/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.spring.injection.cattr;

import java.lang.reflect.Field;
import java.util.Map;

import org.apache.commons.attributes.Attributes;

import wicket.injection.IFieldValueFactory;
import wicket.proxy.LazyInitProxyFactory;
import wicket.spring.ISpringContextLocator;
import wicket.spring.SpringBeanLocator;
import wicket.util.concurrent.ConcurrentHashMap;

/**
 * {@link IFieldValueFactory} that uses {@link LazyInitProxyFactory} to create
 * proxies for Spring dependencies based on the {@link SpringBean} annotation
 * applied to a field. This class is usually used by the
 * {@link AnnotSpringInjector} to inject objects with lazy init proxies.
 * However, this class can be used on its own to create proxies for any field
 * decorated with a {@link SpringBean} annotation.
 * <p>
 * Example:
 * 
 * <pre>
 * IFieldValueFactory factory = new AnnotProxyFieldValueFactory(contextLocator);
 * field = obj.getClass().getDeclaredField(&quot;dependency&quot;);
 * IDependency dependency = (IDependency) factory.getFieldValue(field, obj);
 * </pre>
 * 
 * In the example above the <code>dependency</code> object returned is a lazy
 * init proxy that will look up the actual IDependency bean from spring context
 * upon first access to one of the methods.
 * <p>
 * This class will also cache any produced proxies so that the same proxy is
 * always returned for the same spring dependency. This helps cut down on
 * session size beacause proxies for the same dependency will not be serialized
 * twice.
 * 
 * @see LazyInitProxyFactory
 * @see SpringBean
 * @see SpringBeanLocator
 * 
 * @author Karthik Gurumurthy
 */
public class CommonsAttributeProxyFieldValueFactory implements
		IFieldValueFactory {

	/** the spring context locator. */
	private ISpringContextLocator contextLocator;

	/** cache for values. */
	private final Map cache = new ConcurrentHashMap();

	/**
	 * @param contextLocator
	 *            spring context locator
	 */
	public CommonsAttributeProxyFieldValueFactory(
			ISpringContextLocator contextLocator) {
		if (contextLocator == null) {
			throw new IllegalArgumentException(
					"[contextLocator] argument cannot be null");
		}
		this.contextLocator = contextLocator;
	}

	/**
	 * @see wicket.injection.IFieldValueFactory#getFieldValue(java.lang.reflect.Field,
	 *      java.lang.Object)
	 */
	public Object getFieldValue(Field field, Object fieldOwner) {

		if (Attributes.hasAttributeType(field, SpringBean.class)) {
			SpringBean attribute = (SpringBean) Attributes.getAttribute(field,
					SpringBean.class);
			SpringBeanLocator locator = new SpringBeanLocator(attribute
					.getName(), field.getType(), contextLocator);

			if (cache.containsKey(locator)) {
				return cache.get(locator);
			}

			Object proxy = LazyInitProxyFactory.createProxy(field.getType(),
					locator);
			cache.put(locator, proxy);
			return proxy;
		} else {
			return null;
		}
	}

	/**
	 * @see wicket.injection.IFieldValueFactory#supportsField(java.lang.reflect.Field)
	 */
	public boolean supportsField(Field field) {

		return Attributes.hasAttributeType(field, SpringBean.class);
	}

}
