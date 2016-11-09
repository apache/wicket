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
package org.apache.wicket.model;

import java.util.Objects;

import org.apache.wicket.util.lang.Args;
import org.danekja.java.util.function.serializable.SerializableBiConsumer;
import org.danekja.java.util.function.serializable.SerializableConsumer;
import org.danekja.java.util.function.serializable.SerializableFunction;
import org.danekja.java.util.function.serializable.SerializableSupplier;

/**
 * <code>LambdaModel</code> is a basic implementation of an <code>IModel</code>
 * that uses a serializable {@link SerializableSupplier} to get the object
 * and {@link SerializableConsumer} to set it.
 *
 * The {@link #detach()} method by default does nothing.
 *
 * @param <T> The type of the Model Object
 */
public class LambdaModel<T> implements IModel<T>
{
	private static final long serialVersionUID = 1L;

	private final SerializableSupplier<T> getter;
	private final SerializableConsumer<T> setter;

	/**
	 * Construct the model, using the given supplier and consumer as
	 * implementation for getObject and setObject.
	 *
	 * @param getter Used for the getObject() method.
	 * @param setter Used for the setObject(T object) method.
	 */
	public LambdaModel(SerializableSupplier<T> getter, SerializableConsumer<T> setter)
	{
		this.getter = Args.notNull(getter, "getter");
		this.setter = Args.notNull(setter, "setter");
	}

	@Override
	public T getObject()
	{
		return getter.get();
	}

	@Override
	public void setObject(T t)
	{
		setter.accept(t);
	}

	@Override
	public int hashCode()
	{
		return org.apache.wicket.util.lang.Objects.hashCode(getter, setter);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		final LambdaModel<?> other = (LambdaModel<?>) obj;
		if (!Objects.equals(this.getter, other.getter))
		{
			return false;
		}
		if (!Objects.equals(this.setter, other.setter))
		{
			return false;
		}
		return true;
	}

	/**
	 * Create a {@link LambdaModel}. Usage:
	 * <pre>
	 * {@code
	 * 	LambdaModel.of(person::getName, person::setName)
	 * }
	 * </pre>
	 *
	 * @param getter used to get value
	 * @param setter used to set value
	 * @return model
	 *
	 * @param <T> model object type
	 */
	public static <T> IModel<T> of(SerializableSupplier<T> getter, SerializableConsumer<T> setter)
	{
		return new LambdaModel<>(getter, setter);
	}

	/**
	 * Create a {@link LambdaModel} for a given target. Usage:
	 * <pre>
	 * {@code
	 * 	LambdaModel.of(personModel, Person::getName)
	 * }
	 * </pre>
	 * The target model will be detached automatically.
	 *
	 * @param target target for getter and setter
	 * @param getter used to get a value
	 * @param <X> target model object type
	 * @param <T> model object type
	 * 
	 * @return model
	 */
	public static <X, T> IModel<T> of(IModel<X> target, SerializableFunction<X, T> getter)
	{
		Args.notNull(target, "target");
		Args.notNull(getter, "getter");

		return new LambdaModel<T>(
			() ->
			{
				X x = target.getObject();
				if (x == null) {
					return null;
				}
				return getter.apply(x);
			},

			(t) ->
			{
				throw new UnsupportedOperationException("setObject(Object) not supported");
			}
		) {
			private static final long serialVersionUID = 1L;

			@Override
			public void detach() {
				target.detach();
			}
		};
	}

	/**
	 * Create a {@link LambdaModel} for a given target. Usage:
	 * <pre>
	 * {@code
	 * 	LambdaModel.of(personModel, Person::getName, Person::setName)
	 * }
	 * </pre>
	 * The target model will be detached automatically.
	 *
	 * @param target target for getter and setter
	 * @param getter used to get a value
	 * @param setter used to set a value
	 *
	 * @param <X> target model object type
	 * @param <T> model object type

	 * @return model
	 */
	public static <X, T> IModel<T> of(IModel<X> target, SerializableFunction<X, T> getter,
		SerializableBiConsumer<X, T> setter)
	{
		Args.notNull(target, "target");
		Args.notNull(getter, "getter");
		Args.notNull(setter, "setter");

		return new LambdaModel<T>(
			() ->
			{
				X x = target.getObject();
				if (x == null) {
					return null;
				}
				return getter.apply(x);
			},

			(t) ->
			{
				X x = target.getObject();
				if (x != null) {
					setter.accept(x, t);
				}
			}
		) {
			private static final long serialVersionUID = 1L;

			@Override
			public void detach() {
				target.detach();
			}
		};
	}
}
