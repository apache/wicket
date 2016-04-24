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


import org.apache.wicket.lambda.WicketBiFunction;
import org.apache.wicket.lambda.WicketFunction;
import org.apache.wicket.lambda.WicketSupplier;

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
     * @throws UnsupportedOperationException unless overridden
	 */
	default void setObject(final T object)
	{
		throw new UnsupportedOperationException("Override this method to support setObject(Object)");
	}

	@Override
	default void detach() {}

	/**
	 * Returns a IModel checking whether the predicate holds for the
	 * contained object, if it is not null. If the predicate doesn't evaluate
	 * to true, the contained object will be null.
	 *
	 * @param predicate a predicate to be used for testing the contained object
	 * @return a new IModel
	 */
	default IModel<T> filter(WicketFunction<? super T, Boolean> predicate) 
    {
		return (IModel<T>) () -> 
        {
			T object = IModel.this.getObject();
			if (object != null && predicate.apply(object)) 
            {
				return object;
			}
			else 
            {
				return null;
			}
		};
	}

	/**
	 * Returns a IModel applying the given mapper to
	 * the contained object, if it is not NULL.
	 *
	 * @param <R> the new type of the contained object
	 * @param mapper a mapper, to be applied to the contained object
	 * @return a new IModel
	 */
	default <R> IModel<R> map(WicketFunction<? super T, R> mapper) 
    {
		return (IModel<R>) () -> 
        {
			T object = IModel.this.getObject();
			if (object == null) 
            {
				return null;
			}
			else 
            {
				return mapper.apply(object);
			}
		};
	}

	/**
	 * Returns a IModel applying the given combining function to
	 * the contained object of this and the given other model, if they are not null.
	 *
	 * @param <R> the resulting type
	 * @param <U> the other models type
	 * @param combine a function combining this and the others object to
	 * a result.
	 * @param other another model to be combined with this one
	 * @return a new IModel
	 */
	default <R, U> IModel<R> mapWith(WicketBiFunction<? super T, ? super U, R> combine, IModel<U> other) 
    {
		return (IModel<R>) () -> 
        {
			T t = IModel.this.getObject();
			U u = other.getObject();
			if (t != null && u != null) 
            {
				return combine.apply(t, u);
			}
			else 
            {
				return null;
			}
		};
	}

	/**
	 * Returns a IModel applying the given mapper to the contained
	 * object, if it is not NULL.
	 *
	 * @param <R> the new type of the contained object
	 * @param mapper a mapper, to be applied to the contained object
	 * @return a new IModel
	 */
	default <R> IModel<R> flatMap(WicketFunction<? super T, IModel<R>> mapper) 
    {
        return new IModel<R>() 
        {

            @Override
            public R getObject() 
            {
                T object = IModel.this.getObject();
                if (object != null)
                {
                    return mapper.apply(object).getObject();
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
                    mapper.apply(modelObject).setObject(object);
                }
            }

            @Override
            public void detach() 
            {
                T object = IModel.this.getObject();
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
	 * Returns a IModel applying the {@link WicketFunction} contained
	 * inside the given model to the object contained inside this model.
	 *
	 * @param <R> the type of the new contained object
	 * @param mapper an {@link IModel} containing a function to be applied
	 * to the contained model object.
	 * @return a new IModel
	 */
	default <R> IModel<R> apply(IModel<WicketFunction<? super T, R>> mapper) 
    {
		return (IModel<R>) () -> 
        {
			T object = IModel.this.getObject();
			WicketFunction<? super T, R> f = mapper.getObject();
			if (object == null || f == null) 
            {
				return null;
			}
			else 
            {
				return f.apply(object);
			}
		};
	}

	/**
	 * Returns a IModel, returning either the contained object
	 * or the given default value, depending on the nullness of the
	 * contained object.
	 *
	 * @param other a default value
	 * @return a new IModel
	 */
	default IModel<T> orElse(T other) 
    {
		return (IModel<T>) () -> 
        {
			T object = IModel.this.getObject();
			if (object == null) 
            {
				return other;
			}
			else 
            {
				return object;
			}
		};
	}

	/**
	 * Returns a IModel, returning either the contained object
	 * or invoking the given supplier to get a default value.
	 *
	 * @param other a supplier to be used as a default
	 * @return a new IModel
	 */
	default IModel<T> orElseGet(WicketSupplier<? extends T> other) {
		return (IModel<T>) () -> 
        {
			T object = IModel.this.getObject();
			if (object == null) 
            {
				return other.get();
			}
			else 
            {
				return object;
			}
		};
	}

	/**
	 * Returns a IModel lifting the given object into the
	 * Model.
	 *
	 * @param <T> the type of the given object
	 * @param object an object to be lifted into a IModel
	 * @return a new IModel
	 */
	static <T> IModel<T> of(T object) 
    {
		return of((WicketSupplier<T>) () -> object);
	}

	/**
	 * Returns a IModel applying the given supplier to
	 * get the object.
	 *
	 * @param <T> the type of the given object
	 * @param supplier a supplier, to be used to get a value
	 * @return a new IModel
	 */
	static <T> IModel<T> of(WicketSupplier<T> supplier) 
    {
		return (IModel<T>) () -> supplier.get();
	}

	/**
	 * Returns a IModel using the getObject() method
	 * of the given model.
	 *
	 * @param <T> the type of the contained object
	 * @param model a model,
	 * @return a new IModel
	 */
	static <T> IModel<T> of(IModel<T> model) 
    {
		return (IModel<T>) () -> model.getObject();
	}
}
