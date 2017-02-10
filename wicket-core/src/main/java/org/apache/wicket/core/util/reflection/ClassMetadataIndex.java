package org.apache.wicket.core.util.reflection;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.wicket.core.util.lang.IPropertyExpressionResolver;
import org.apache.wicket.util.lang.Generics;

public class ClassMetadataIndex
{

	private final static ConcurrentHashMap<Object, IPropertyExpressionResolver> applicationToClassesToGetAndSetters = Generics
		.newConcurrentHashMap(2);
	

	private final ConcurrentHashMap<Class<?>, Map<String, IGetAndSet>> map = Generics.newConcurrentHashMap(16);
	
//	void put(Class<?> clz, Map<String, IGetAndSet> values)
//	IGetAndSet get(Class<?> clz, String exp);

	
}
