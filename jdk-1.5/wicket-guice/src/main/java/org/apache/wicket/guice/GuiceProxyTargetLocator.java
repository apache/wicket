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
package org.apache.wicket.guice;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import org.apache.wicket.Application;
import org.apache.wicket.proxy.IProxyTargetLocator;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;

class GuiceProxyTargetLocator implements IProxyTargetLocator
{
	private static final long serialVersionUID = 1L;

	private final String typeName;
	private final Annotation bindingAnnotation;

	GuiceProxyTargetLocator(Type type, Annotation bindingAnnotation)
	{
		// I'm not too happy about
		typeName = type.toString();
		this.bindingAnnotation = bindingAnnotation;

		GuiceTypeStore typeStore = (GuiceTypeStore)Application.get().getMetaData(
				GuiceTypeStore.TYPESTORE_KEY);
		typeStore.setType(typeName, type);
	}

	public Object locateProxyTarget()
	{
		final GuiceInjectorHolder holder = (GuiceInjectorHolder)Application.get().getMetaData(
				GuiceInjectorHolder.INJECTOR_KEY);

		final GuiceTypeStore typeStore = (GuiceTypeStore)Application.get().getMetaData(
				GuiceTypeStore.TYPESTORE_KEY);
		final Type type = typeStore.getType(typeName);

		// using TypeLiteral to retrieve the key gives us automatic support for
		// Providers and other injectable TypeLiterals
		final Key< ? > key;

		if (bindingAnnotation == null)
		{
			key = Key.get(TypeLiteral.get(type));
		}
		else
		{
			key = Key.get(TypeLiteral.get(type), bindingAnnotation);
		}
		return holder.getInjector().getInstance(key);
	}
}
