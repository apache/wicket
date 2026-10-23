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
package org.apache.wicket.examples.builder.spike;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.util.io.IClusterable;
import org.danekja.java.util.function.serializable.SerializableBiConsumer;
import org.danekja.java.util.function.serializable.SerializableFunction;

/**
 * SPIKE - throwaway. A container being filled, together with its model.
 * <p>
 * Children bind to the container's model with {@link #bind}: the getter and setter receive the
 * model object as a parameter, so a method reference such as {@code Person::getName} captures
 * nothing and the object itself stays in the model, where it is detached.
 *
 * @param <T>
 *            the type of the container's model object
 */
public final class Scope<T> implements IClusterable
{
	private static final long serialVersionUID = 1L;

	private final MarkupContainer container;

	private final IModel<T> model;

	Scope(MarkupContainer container, IModel<T> model)
	{
		this.container = container;
		this.model = model;
	}

	public MarkupContainer self()
	{
		return container;
	}

	public IModel<T> model()
	{
		return model;
	}

	public <R> IModel<R> bind(SerializableFunction<T, R> getter)
	{
		return model.map(getter);
	}

	public <R> IModel<R> bind(SerializableFunction<T, R> getter, SerializableBiConsumer<T, R> setter)
	{
		return LambdaModel.of(model, getter, setter);
	}

	public Scope<T> add(ComponentBuilder<?, ?>... builders)
	{
		for (ComponentBuilder<?, ?> builder : builders)
		{
			container.add(builder.build());
		}
		return this;
	}

	public Scope<T> add(Component... components)
	{
		container.add(components);
		return this;
	}
}
