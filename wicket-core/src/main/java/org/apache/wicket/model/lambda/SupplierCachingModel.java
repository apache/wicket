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

import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.util.lang.Args;

/**
 * A caching model that gets its value from a {@link WicketSupplier}.
 *
 * @param <T>
 *            - type of the model object
 */
public class SupplierCachingModel<T> extends LoadableDetachableModel<T>
{
	/**
	 * Supplies the model object.
	 */
	private WicketSupplier<T> getter;

	/**
	 * Constructor.
	 *
	 * @param getter
	 *              The getter of the model object
	 */
	public SupplierCachingModel(WicketSupplier<T> getter)
	{
		this.getter = Args.notNull(getter, "getter");
	}

	@Override
	protected final T load()
	{
		return getter.get();
	}


	@Override
	public int hashCode()
	{
		return org.apache.wicket.util.lang.Objects.hashCode(getter);
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
		final SupplierCachingModel<?> other = (SupplierCachingModel<?>) obj;
		if (!Objects.equals(this.getter, other.getter))
		{
			return false;
		}
		return true;
	}
}
