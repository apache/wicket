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
import org.danekja.java.util.function.serializable.SerializableBiFunction;
import org.danekja.java.util.function.serializable.SerializableFunction;
import org.danekja.java.util.function.serializable.SerializablePredicate;
import org.danekja.java.util.function.serializable.SerializableSupplier;

/**
 * A IModel wraps the actual model Object used by a Component. IModel implementations are used as a
 * facade for the real model so that users have control over the actual persistence strategy. Note
 * that objects implementing this interface will be stored in the Session. Hence, you should use
 * (non-transient) instance variables sparingly.
 * <ul>
 * <li><b>Basic Models </b>- To implement a basic (non-detachable) model which holds its entire
 * state in the Session, you can use the simple model wrapper {@link Model}.
 * 
 * <li><b>Detachable Models </b>- IModel inherits a hook, {@link IDetachable#detach()}, so that
 * interface implementers can detach transient information when a model is no longer being actively
 * used by the framework. This reduces memory use and reduces the expense of replicating the model
 * in a clustered server environment. To implement a detachable model, you should generally extend
 * {@link LoadableDetachableModel} instead of implementing IModel directly.
 * 
 * <li><b>Property Models </b>- The AbstractPropertyModel class provides default functionality for
 * property models. A property model provides access to a particular property of its wrapped model.
 * 
 * <li><b>Compound Property Models </b>- The IModel interface is parameterized by Component,
 * allowing a model to be shared among several Components. When the {@link IModel#getObject()}method
 * is called, the value returned will depend on the Component which is asking for the value.
 * Likewise, the {@link IModel#setObject(Object)}method sets a different property depending on which
 * Component is doing the setting. For more information on CompoundPropertyModels and model
 * inheritance, see {@link org.apache.wicket.model.CompoundPropertyModel}and
 * {@link org.apache.wicket.Page}.
 * </ul>
 * 
 * @see org.apache.wicket.Component#sameInnermostModel(org.apache.wicket.Component)
 * @see org.apache.wicket.Component#sameInnermostModel(IModel)
 * 
 * @author Chris Turner
 * @author Eelco Hillenius
 * @author Jonathan Locke
 * 
 * @param <T>
 *            Model object.
 */
@FunctionalInterface
public interface IModel<T> extends IDetachable
{
	/**
	 * Gets the model object.
	 * 
	 * @return The model object
	 */
	T getObject();

	/**
	 * Sets the model object.
	 *
	 * @param object
	 *            The model object
	 * @throws UnsupportedOperationException
	 *             unless overridden
	 */
	default void setObject(final T object)
	{
		throw new UnsupportedOperationException(
			"Override this method to support setObject(Object)");
	}

	@Override
	default void detach()
	{
	}

	/**
	 * Returns a IModel checking whether the predicate holds for the contained object, if it is not
	 * {@code null}. If the predicate doesn't evaluate to {@code true}, the contained object will be {@code null}.
	 *
	 * @param predicate
	 *            a predicate to be used for testing the contained object
	 * @return a new IModel
	 */
	default IModel<T> filter(SerializablePredicate<? super T> predicate)
	{
		Args.notNull(predicate, "predicate");
		return new IModel<T>()
		{
			@Override
			public T getObject()
			{
				T object = IModel.this.getObject();
				if (object != null && predicate.test(object))
				{
					return object;
				}
				else
				{
					return null;
				}
			}

			@Override
			public void detach()
			{
				IModel.this.detach();
			}
		};
	}

	/**
	 * Returns a IModel applying the given mapper to the contained object, if it is not {@code null}.
	 *
	 * @param <R>
	 *            the new type of the contained object
	 * @param mapper
	 *            a mapper, to be applied to the contained object
	 * @return a new IModel
	 */
	default <R> IModel<R> map(SerializableFunction<? super T, R> mapper)
	{
		Args.notNull(mapper, "mapper");
		return new IModel<R>() {
			@Override
			public R getObject()
			{
				T object = IModel.this.getObject();
				if (object == null)
				{
					return null;
				} else
				{
					return mapper.apply(object);
				}
			}

			@Override
			public void detach()
			{
				IModel.this.detach();
			}
		};
	}

	/**
	 * Returns a {@link IModel} applying the given combining function to the current model object and
	 * to the one from the other model, if they are not {@code null}.
	 *
	 * @param <R>
	 *            the resulting type
	 * @param <U>
	 *            the other models type
	 * @param other
	 *            another model to be combined with this one
	 * @param combiner
	 *            a function combining this and the others object to a result.
	 * @return a new IModel
	 */
	default <R, U> IModel<R> combineWith(IModel<U> other,
		SerializableBiFunction<? super T, ? super U, R> combiner)
	{
		Args.notNull(combiner, "combiner");
		Args.notNull(other, "other");
		return new IModel<R>() {
			@Override
			public R getObject()
			{
				T t = IModel.this.getObject();
				U u = other.getObject();
				if (t != null && u != null)
				{
					return combiner.apply(t, u);
				} else
				{
					return null;
				}
			}

			@Override
			public void detach()
			{
				other.detach();
				IModel.this.detach();
			}
		};
	}

	/**
	 * Returns a IModel applying the given IModel-bearing mapper to the contained object, if it is not {@code null}.
	 *
	 * @param <R>
	 *            the new type of the contained object
	 * @param mapper
	 *            a mapper, to be applied to the contained object
	 * @return a new IModel
	 * @see LambdaModel#of(IModel, SerializableFunction, SerializableBiConsumer)
	 */
	default <R> IModel<R> flatMap(SerializableFunction<? super T, IModel<R>> mapper)
	{
		Args.notNull(mapper, "mapper");
		return new IModel<R>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public R getObject()
			{
				T object = IModel.this.getObject();
				if (object != null)
				{
					IModel<R> model = mapper.apply(object);
					if (model != null) 
					{
						return model.getObject();
					}
					else 
					{
						return null;
					}
				}
				else
				{
					return null;
				}
			}

			@Override
			public void setObject(R object)
			{
				T modelObject = IModel.this.getObject();
				if (modelObject != null)
				{
					IModel<R> model = mapper.apply(modelObject);
					if (model != null)
					{
						model.setObject(object);
					}
				}
			}

			@Override
			public void detach()
			{
				T object = IModel.this.getObject();
				IModel.this.detach();
				if (object != null)
				{
					IModel<R> model = mapper.apply(object);
					if (model != null)
					{
						model.detach();
					}
				}
			}
		};
	}

	/**
	 * Returns a IModel, returning either the contained object or the given default value, depending
	 * on the {@code null}ness of the contained object.
	 *
	 * <p>
	 * Possible usages:
	 * <ul>
	 *     <li>{@code myComponent = new AnyComponent(&quot;someId&quot;, someModel.orElse(defaultValue));}
	 *      - This way Wicket will make use of the default value if the model object of <em>someModel</em>
	 *      is {@code null}.
	 *     </li>
	 *     <li>in the middle of the application logic: {@code ... = someModel.orElse(default).getModelObject();}</li>
	 * </ul>
	 *
	 * </p>
	 *
	 * @param other
	 *            a default value
	 * @return a new IModel
	 */
	default IModel<T> orElse(T other)
	{
		return new IModel<T>() {
			@Override
			public T getObject()
			{
				T object = IModel.this.getObject();
				if (object == null)
				{
					return other;
				} else
				{
					return object;
				}
			}

			@Override
			public void detach()
			{
				IModel.this.detach();
			}
		};
	}

	/**
	 * Returns a IModel, returning either the contained object or invoking the given supplier to get
	 * a default value.
	 *
	 * @param other
	 *            a supplier to be used as a default
	 * @return a new IModel
	 */
	default IModel<T> orElseGet(SerializableSupplier<? extends T> other)
	{
		Args.notNull(other, "other");
		return new IModel<T>() {
			@Override
			public T getObject()
			{
				T object = IModel.this.getObject();
				if (object == null)
				{
					return other.get();
				} else
				{
					return object;
				}
			}

			@Override
			public void detach()
			{
				IModel.this.detach();
			}
		};
	}
	
	/**
	 * Returns a IModel, returning whether the contained object is non-null.
	 *
	 * @return a new IModel
	 */
	default IModel<Boolean> isPresent() {
		return new IModel<Boolean>() {
			@Override
			public Boolean getObject()
			{
				return IModel.this.getObject() != null;
			}

			@Override
			public void detach()
			{
				IModel.this.detach();
			}
		};
	}

	/**
	 * Returns an IModel, returning the object typed as {@code R} if it is an instance of that type,
	 * otherwise {@code null}.
	 *
	 * @param <R> the type the object should be an instance of
	 * @param clazz the {@code Class} the current model object should be an instance of
	 * @return a new IModel
	 */
	default <R extends T> IModel<R> as(Class<R> clazz) {
		Args.notNull(clazz, "clazz");
		return filter(clazz::isInstance).map(clazz::cast);
	}

	/**
	 * Suppresses generics warning when casting model types.
	 *
	 * @param <T>
	 * @param model
	 * @return cast <code>model</code>
	 */
	@SuppressWarnings("unchecked")
	static <T> IModel<T> of(IModel<?> model)
	{
		return (IModel<T>)model;
	}
}
