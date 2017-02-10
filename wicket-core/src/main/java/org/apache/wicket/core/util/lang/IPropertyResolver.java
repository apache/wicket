package org.apache.wicket.core.util.lang;

import org.apache.wicket.core.util.reflection.IGetAndSet;

public interface IPropertyResolver
{

	IGetAndSet get(Class<?> clz, String exp);

}
