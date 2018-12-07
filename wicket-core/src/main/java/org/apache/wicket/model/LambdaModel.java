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

import org.apache.wicket.util.lang.Args;
import org.danekja.java.util.function.serializable.SerializableBiConsumer;
import org.danekja.java.util.function.serializable.SerializableConsumer;
import org.danekja.java.util.function.serializable.SerializableFunction;
import org.danekja.java.util.function.serializable.SerializableSupplier;

/**
 * <code>LambdaModel</code> is a basic implementation of an <code>IModel</code> that uses a
 * serializable {@link java.util.function.Supplier} to get the object and
 * {@link java.util.function.Consumer} to set it.
 *
 * @param <T>
 *            The type of the Model Object
 */
public abstract class LambdaModel<T> implements IModel<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor hidden, instantiation is done using one of the factory methods
	 */
	private LambdaModel()
	{
	}

	@Override
	public void setObject(T t)
	{
		throw new UnsupportedOperationException("setObject(Object) not supported");
	}

	/**
	 * Create a read-only {@link IModel}. Usage:
	 * 
	 * <pre>
	 * {@code
	 *     LambdaModel.of(person::getName)
	 * }
	 * </pre>
	 * 
	 * Note that {@link IModel} is a {@code FunctionalInterface} and you can also use a lambda
	 * directly as a model.
	 *
	 * @param getter
	 *            used to get value
	 * @return model
	 *
	 * @param <R>
	 *            model object type
	 */
	public static <R> IModel<R> of(SerializableSupplier<R> getter)
	{
		return getter::get;
	}

	/**
	 * Create a {@link LambdaModel}. Usage:
	 * 
	 * <pre>
	 * {@code
	 * 	LambdaModel.of(person::getName, person::setName)
	 * }
	 * </pre>
	 *
	 * @param getter
	 *            used to get value
	 * @param setter
	 *            used to set value
	 * @return model
	 *
	 * @param <R>
	 *            model object type
	 */
	public static <R> IModel<R> of(SerializableSupplier<R> getter, SerializableConsumer<R> setter)
	{
		Args.notNull(getter, "getter");
		Args.notNull(setter, "setter");

		return new LambdaModel<R>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public R getObject()
			{
				return getter.get();
			}

			@Override
			public void setObject(R r)
			{
				setter.accept(r);
			}
		};
	}


	/**
	 * Create a {@link LambdaModel} for a given target. Usage:
	 * 
	 * <pre>
	 * {@code
	 * 	LambdaModel.of(personModel, Person::getName)
	 * }
	 * </pre>
	 * 
	 * The target model will be detached automatically.
	 *
	 * @param target
	 *            target for getter and setter
	 * @param getter
	 *            used to get a value
	 * @param <X>
	 *            target model object type
	 * @param <R>
	 *            model object type
	 * 
	 * @return model
	 * 
	 * @deprecated this method will be remove in Wicket 9.x, use {@link IModel#map(SerializableFunction)} instead.
	 */
	@Deprecated
	public static <X, R> IModel<R> of(IModel<X> target, SerializableFunction<X, R> getter)
	{
		Args.notNull(target, "target");
		Args.notNull(getter, "getter");

		return new LambdaModel<R>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public R getObject()
			{
				X x = target.getObject();
				if (x == null)
				{
					return null;
				}
				return getter.apply(x);
			}

			@Override
			public void setObject(R r) {
				throw new UnsupportedOperationException("setObject(Object) on " + target + " is not supported");
			}

			@Override
			public void detach()
			{
				target.detach();
			}
		};
	}

	/**
	 * Create a {@link LambdaModel} for a given target. Usage:
	 * 
	 * <pre>
	 * {@code
	 * 	LambdaModel.of(personModel, Person::getName, Person::setName)
	 * }
	 * </pre>
	 * 
	 * The target model will be detached automatically.
	 *
	 * @param target
	 *            target for getter and setter
	 * @param getter
	 *            used to get a value
	 * @param setter
	 *            used to set a value
	 *
	 * @param <X>
	 *            target model object type
	 * @param <R>
	 *            model object type
	 * 
	 * @return model
	 * @see IModel#flatMap(SerializableFunction)
	 */
	public static <X, R> IModel<R> of(IModel<X> target, SerializableFunction<X, R> getter,
		SerializableBiConsumer<X, R> setter)
	{
		Args.notNull(target, "target");
		Args.notNull(getter, "getter");
		Args.notNull(setter, "setter");

		return new LambdaModel<R>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public R getObject()
			{
				X x = target.getObject();
				if (x == null)
				{
					return null;
				}
				return getter.apply(x);
			}

			@Override
			public void setObject(R r)
			{
				X x = target.getObject();
				if (x != null)
				{
					setter.accept(x, r);
				}
			}

			@Override
			public void detach()
			{
				target.detach();
			}
		};
	}
}
