package org.apache.wicket.core.util.lang;

public interface IPropertyExpressionResolver
{

	<T> T getValue(String expression, T object);

	<T> Class<T> getPropertyClass(String expression, Object object, Class<?> targetClass);

	void setValue(String expression, Object object, Object value, PropertyResolverConverter prc);

}
