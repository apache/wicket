package org.apache.wicket.core.util.lang;

import org.apache.wicket.core.util.reflection.IGetAndSet;

/**
 * Resolves a property string to an {@link IGetAndSet}.
 * 
 * @see {@link IPropertyExpressionResolver} Property expression are resolved by
 *      {@link IPropertyExpressionResolver} implementations instead
 *
 * @author pedro
 */
public interface IPropertyResolver
{

	IGetAndSet get(Class<?> clz, String exp);

}
