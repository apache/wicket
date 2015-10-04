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
package org.apache.wicket.model.lambda;

import java.util.Objects;

import org.apache.wicket.model.IModel;
import org.apache.wicket.util.lang.Args;

/**
 * <code>LambdaModel</code> is a basic implementation of an <code>IModel</code>
 * that uses a serializable {@link java.util.function.Supplier} to get the object
 * and {@link java.util.function.Consumer} to set it.
 *
 * The detach method defaults does nothing.
 *
 * @param <T> The type of the Model Object
 */
public class LambdaModel<T> implements IModel<T>
{
	private static final long serialVersionUID = 1L;

	private final WicketSupplier<T> getter;
	private final WicketConsumer<T> setter;

	/**
	 * Construct the model, using the given supplier and consumer as
	 * implementation for getObject and setObject.
	 *
	 * @param getter Used for the getObject() method.
	 * @param setter Used for the setObject(T object) method.
	 */
	public LambdaModel(WicketSupplier<T> getter, WicketConsumer<T> setter)
	{
		this.getter = Args.notNull(getter, "getter");
		this.setter = Args.notNull(setter, "setter");
	}

	public static <T> LambdaModel<T> of(WicketSupplier<T> getter, WicketConsumer<T> setter)
	{
		return new LambdaModel<>(getter, setter);
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
	public void detach()
	{
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
}
