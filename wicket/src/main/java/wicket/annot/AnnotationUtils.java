/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar 2006) eelco12 $
 * $Revision: 5004 $
 * $Date: 2006-03-17 20:47:08 -0800 (Fri, 17 Mar 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.annot;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import wicket.WicketRuntimeException;
import wicket.util.lang.reflect.ClassOrder;
import wicket.util.lang.reflect.ReflectionUtils;

/**
 * Annotation related utilities.
 * 
 * @author ivaynberg
 */
public class AnnotationUtils
{
	private AnnotationUtils()
	{
	}

	/**
	 * Invokes after render listeners on the specified object
	 * 
	 * @see OnAfterRender
	 * 
	 * @param object
	 */
	public static void invokeOnAfterRenderListeners(Object object)
	{
		invokeListeners(object, OnAfterRender.class, InvocationOrder.CLEANUP);
	}


	/**
	 * Invokes before render listeners on the specified object
	 * 
	 * @see OnBeforeRender
	 * 
	 * @param object
	 */
	public static void invokeOnBeforeRenderListeners(Object object)
	{
		invokeListeners(object, OnBeforeRender.class, InvocationOrder.SETUP);
	}


	/**
	 * Invokes detach listeners on the specified object
	 * 
	 * @see OnDetach
	 * 
	 * @param object
	 */
	public static void invokeOnDetachListeners(Object object)
	{
		invokeListeners(object, OnDetach.class, InvocationOrder.CLEANUP);
	}


	/**
	 * Invokes attach listeners on the specified object
	 * 
	 * @see OnAttach
	 * 
	 * @param object
	 */
	public static void invokeOnAttachListeners(Object object)
	{
		invokeListeners(object, OnAttach.class, InvocationOrder.SETUP);
	}

	/**
	 * Invokes annotated listener methods on the object
	 * 
	 * @param object
	 * @param annot
	 * @param order
	 */
	private static void invokeListeners(Object object, Class<? extends Annotation> annot,
			InvocationOrder order)
	{
		List<Method> listeners = ReflectionUtils.invocationChainForAnnotation(object.getClass(),
				annot, order.toClassOrder());
		for (Method method : listeners)
		{
			invokeAnnotatedListenerMethod(object, method, annot);
		}
	}

	/** empty object[] array used for invoking listener methods */
	private static final Object[] LISTENER_ARGS = new Object[] {};

	/**
	 * Invokes a listener method
	 * 
	 * @param object
	 *            object whose listener will be invoked
	 * @param method
	 *            listener method
	 * @param annot
	 *            annotation responsible for invocation
	 */
	private static void invokeAnnotatedListenerMethod(Object object, Method method,
			Class<? extends Annotation> annot)
	{
		if (!method.getReturnType().equals(void.class) || method.getParameterTypes().length != 0)
		{
			throw new IllegalStateException("Method [[" + method.getName()
					+ "]] cannot be annotated with [[" + OnAttach.class.getSimpleName()
					+ "]] because it doesnt match signature [[void method()]]");
		}
		try
		{
			if (!method.isAccessible())
			{
				method.setAccessible(true);
			}
			method.invoke(object, LISTENER_ARGS);
		}
		catch (Exception e)
		{
			throw new WicketRuntimeException("Error while invoking listener method [["
					+ method.getName() + "]] for [[" + annot.getClass().getSimpleName()
					+ "]] event", e);
		}
	}

	/**
	 * Convenience wrapper around {@link ClassOrder} that makes it easier to
	 * figure out which class order should be used
	 * 
	 * @author ivaynberg
	 */
	private static enum InvocationOrder {
		/**
		 * represents order of initializing methods such as onAttach which are
		 * called with superclass to subclass order
		 */
		SETUP(ClassOrder.SUPER_TO_SUB),

		/**
		 * represents order of cleanup methods such as onDetach which are called
		 * with subclass to superclass order
		 */
		CLEANUP(ClassOrder.SUB_TO_SUPER);

		private InvocationOrder(ClassOrder order)
		{
			this.order = order;
		}

		private final ClassOrder order;

		/**
		 * @return class order equivalent
		 */
		public ClassOrder toClassOrder()
		{
			return order;
		}
	}
}