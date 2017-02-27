package org.apache.wicket.core.util.parser;

import static java.lang.Integer.parseInt;
import static org.apache.wicket.core.util.reflection.ReflectionUtility.findField;
import static org.apache.wicket.core.util.reflection.ReflectionUtility.findGetter;
import static org.apache.wicket.core.util.reflection.ReflectionUtility.findMethod;
import static org.apache.wicket.core.util.reflection.ReflectionUtility.findPositionGetter;
import static org.apache.wicket.core.util.reflection.ReflectionUtility.findSetter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.core.util.lang.IPropertyExpressionResolver;
import org.apache.wicket.core.util.reflection.ArrayGetAndSet;
import org.apache.wicket.core.util.reflection.ArrayLengthGetAndSet;
import org.apache.wicket.core.util.reflection.FieldGetAndSet;
import org.apache.wicket.core.util.reflection.IGetAndSet;
import org.apache.wicket.core.util.reflection.IndexedPropertyGetAndSet;
import org.apache.wicket.core.util.reflection.ListGetAndSet;
import org.apache.wicket.core.util.reflection.MapGetAndSet;
import org.apache.wicket.core.util.reflection.MethodGetAndSet;
import org.apache.wicket.core.util.reflection.ObjectWithGetAndSet;

public class ParsedPropertyExpressionResolver implements IPropertyExpressionResolver
{
	@Override
	public ObjectWithGetAndSet resolve(String expression, Object object,
		Class<? extends Object> clz, int tryToCreateNull)
	{
		PropertyExpression ast = new PropertyExpressionParser().parse(expression);
		return resolveExpression(ast, object, clz, tryToCreateNull);
	}

	public ObjectWithGetAndSet resolveExpression(PropertyExpression ast, Object object,
		Class<?> clz, int tryToCreateNull)
	{

		IGetAndSet getAndSet = resolveProperty(clz, ast);

		if (getAndSet == null
			&& (ast.index != null || ast.next != null && ast.next.canDefaultToIndex()))
		{
			getAndSet = resolvePropertyAtPosition(clz, ast.getPropertyToken(),
				ast.index != null ? ast.index : ast.next.toIndex());

			if (getAndSet != null && ast.index == null)
				ast = ast.next;
		}

		if (getAndSet == null && ast.canDefaultToIndex())
			getAndSet = resolveIndex(clz, ast.toIndex());

		if (getAndSet == null)// ok, finally give up
			throw new WicketRuntimeException("Can't parse the expression '" + ast);

		ObjectWithGetAndSet resolved = new ObjectWithGetAndSet(getAndSet, object);

		if (ast.javaProperty != null && ast.javaProperty.index != null)
		{
			getAndSet = resolveIndex(getAndSet.getTargetClass(), ast.javaProperty.index);
			resolved = new ObjectWithGetAndSet(getAndSet,
				resolved.getValue(tryToCreateNull == CREATE_NEW_VALUE));
		}
		if (ast.beanProperty != null && ast.beanProperty.index != null)
		{
			getAndSet = resolveIndex(getAndSet.getTargetClass(), ast.beanProperty.index);
			resolved = new ObjectWithGetAndSet(getAndSet,
				resolved.getValue(tryToCreateNull == CREATE_NEW_VALUE));
		}

		if (ast.next == null)
			return resolved;

		Object nextValue = resolved.getValue(tryToCreateNull == CREATE_NEW_VALUE);
		Class<?> nextClass = nextValue != null ? nextValue.getClass() : resolved.getTargetClass();

		if (nextValue == null && tryToCreateNull == RETURN_NULL)
			return null;

		return resolveExpression(ast.next, nextValue, nextClass, tryToCreateNull);

	}

	private IGetAndSet resolveProperty(Class<?> clz, PropertyExpression ast)
	{
		if (ast.javaProperty != null)
			return resolveJavaProperty(clz, ast);
		else if (ast.beanProperty != null)
			return resolveBeanProperty(clz, ast);
		else if (ast.index != null)
			return resolveIndex(clz, ast.index);
		else
			throw new WicketRuntimeException("Resolver failed to find a property to resolve");
	}

	private IGetAndSet resolveJavaProperty(Class<?> clz, PropertyExpression ast)
	{
		if (ast.javaProperty.hasMethodSign)
		{
			Method method = findMethod(clz, ast.javaProperty.javaIdentifier);
			return new MethodGetAndSet(method, findSetter(method, clz), null);
		}
		else
		{
			Method method = findGetter(clz, ast.javaProperty.javaIdentifier);
			Field field = findField(clz, ast.javaProperty.javaIdentifier);

			if (method == null && field == null)
				return null;
			else if (method == null)
				return new FieldGetAndSet(field);
			else
				return new MethodGetAndSet(method, findSetter(method, clz), field);
		}
	}

	private IGetAndSet resolveBeanProperty(Class<?> clz, PropertyExpression ast)
	{
		Method method = findGetter(clz, ast.beanProperty.propertyName);

		return method == null ? null : new MethodGetAndSet(method, findSetter(method, clz), null);
	}

	private IGetAndSet resolveIndex(Class<?> clz, String index)
	{
		if (List.class.isAssignableFrom(clz))
		{
			int position = Integer.parseInt(index);
			return new ListGetAndSet(position);
		}
		else if (Map.class.isAssignableFrom(clz))
		{
			return new MapGetAndSet(index);
		}
		else if (clz.isArray())
		{
			try
			{
				int position = Integer.parseInt(index);
				return new ArrayGetAndSet(clz.getComponentType(), position);
			}
			catch (NumberFormatException ex)
			{
				if (index.equals("length") || index.equals("size"))
				{
					return new ArrayLengthGetAndSet();
				}
				throw new WicketRuntimeException(
					"Can't parse the expression '" + index + "' as an index for an array lookup");
			}
		}
		return null;
	}

	private IGetAndSet resolvePropertyAtPosition(Class<?> clz, String property, String position)
	{
		Method method = findPositionGetter(clz, property);
		if (method == null)
			return null;
		try
		{
			return new IndexedPropertyGetAndSet(method, parseInt(position));
		}
		catch (NumberFormatException e)
		{
			return null;
		}
	}

}
