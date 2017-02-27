package org.apache.wicket.core.util.lang;

import org.apache.wicket.core.util.reflection.ObjectWithGetAndSet;

/**
 * Resolves property expression to workable {@link ObjectWithGetAndSet}
 * 
 * @author pedro
 */
public interface IPropertyExpressionResolver
{

	int RETURN_NULL = 0;
	int CREATE_NEW_VALUE = 1;
	int RESOLVE_CLASS = 2;

	/**
	 * @param expression
	 * @param object
	 *            Optional, but will enable the resolver to find subclasses in polymorphic types
	 * @param clz
	 * @return {@link ObjectWithGetAndSet}
	 */
	ObjectWithGetAndSet resolve(String expression, Object object, Class<? extends Object> clz,
		int tryToCreateNull);

}
