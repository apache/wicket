package org.apache.wicket.core.util.lang;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.core.util.reflection.ArrayGetAndSet;
import org.apache.wicket.core.util.reflection.ArrayLengthGetAndSet;
import org.apache.wicket.core.util.reflection.FieldGetAndSet;
import org.apache.wicket.core.util.reflection.IGetAndSet;
import org.apache.wicket.core.util.reflection.IndexedPropertyGetAndSet;
import org.apache.wicket.core.util.reflection.ListGetAndSet;
import org.apache.wicket.core.util.reflection.MapGetAndSet;
import org.apache.wicket.core.util.reflection.MethodGetAndSet;
import org.apache.wicket.core.util.reflection.ReflectionUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default locator supporting <em>Java Beans</em> properties, maps, lists and method invocations.
 */
public class DefaultPropertyLocator implements IPropertyResolver
{
	public static final Logger log = LoggerFactory.getLogger(DefaultPropertyLocator.class);
	
	public IGetAndSet get(Class<?> clz, String exp)
	{
		IGetAndSet getAndSet = null;

		Method method = null;
		Field field;
		if (exp.startsWith("["))
		{
			// if expression begins with [ skip method finding and use it as
			// a key/index lookup on a map.
			exp = exp.substring(1, exp.length() - 1);
		}
		else if (exp.endsWith("()"))
		{
			// if expression ends with (), don't test for setters just skip
			// directly to method finding.
			method = ReflectionUtility.findMethod(clz, exp);
		}
		else
		{
			method = ReflectionUtility.findGetter(clz, exp);
		}
		if (method == null)
		{
			if (List.class.isAssignableFrom(clz))
			{
				try
				{
					int index = Integer.parseInt(exp);
					getAndSet = new ListGetAndSet(index);
				}
				catch (NumberFormatException ex)
				{
					// can't parse the exp as an index, maybe the exp was a
					// method.
					method = ReflectionUtility.findMethod(clz, exp);
					if (method != null)
					{
						getAndSet = new MethodGetAndSet(method,
							MethodGetAndSet.findSetter(method, clz), null);
					}
					else
					{
						field = ReflectionUtility.findField(clz, exp);
						if (field != null)
						{
							getAndSet = new FieldGetAndSet(field);
						}
						else
						{
							throw new WicketRuntimeException("The expression '" + exp
								+ "' is neither an index nor is it a method or field for the list "
								+ clz);
						}
					}
				}
			}
			else if (Map.class.isAssignableFrom(clz))
			{
				getAndSet = new MapGetAndSet(exp);
			}
			else if (clz.isArray())
			{
				try
				{
					int index = Integer.parseInt(exp);
					getAndSet = new ArrayGetAndSet(clz.getComponentType(), index);
				}
				catch (NumberFormatException ex)
				{
					if (exp.equals("length") || exp.equals("size"))
					{
						getAndSet = new ArrayLengthGetAndSet();
					}
					else
					{
						throw new WicketRuntimeException("Can't parse the expression '" + exp
							+ "' as an index for an array lookup");
					}
				}
			}
			else
			{
				field = ReflectionUtility.findField(clz, exp);
				if (field == null)
				{
					method = ReflectionUtility.findMethod(clz, exp);
					if (method == null)
					{
						int index = exp.indexOf('.');
						if (index != -1)
						{
							String propertyName = exp.substring(0, index);
							String propertyIndex = exp.substring(index + 1);
							try
							{
								int parsedIndex = Integer.parseInt(propertyIndex);
								// if so then it could be a getPropertyIndex(int)
								// and setPropertyIndex(int, object)
								String name = Character.toUpperCase(propertyName.charAt(0))
									+ propertyName.substring(1);
								method = clz.getMethod(MethodGetAndSet.GET + name,
									new Class[] { int.class });
								getAndSet = new IndexedPropertyGetAndSet(method, parsedIndex);
							}
							catch (Exception e)
							{
								throw new WicketRuntimeException("No get method defined for class: "
									+ clz + " expression: " + propertyName);
							}
						}
					}
					else
					{
						getAndSet = new MethodGetAndSet(method,
							MethodGetAndSet.findSetter(method, clz), null);
					}
				}
				else
				{
					getAndSet = new FieldGetAndSet(field);
				}
			}
		}
		else
		{
			field = ReflectionUtility.findField(clz, exp);
			getAndSet = new MethodGetAndSet(method, MethodGetAndSet.findSetter(method, clz), field);
		}

		return getAndSet;
	}
}