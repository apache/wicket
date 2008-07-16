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

import java.io.Serializable;

import org.apache.wicket.util.lang.Objects;

/**
 * @author Timo Rantalaiho
 */
public abstract class CollectionModelBase<T> implements IModel<T>
{
    private static final long serialVersionUID = 1L;
    private T object;

    /**
     * @see org.apache.wicket.model.IModel#getObject()
     */
    public T getObject()
    {
        return object;
    }

    /**
     * Set the model object. The contents must be
     * serializable, as they are stored in the session
     *
     * @param object the model object
     * @see org.apache.wicket.model.IModel#setObject(Object)
     */
    public void setObject(T object)
    {
        if (!(object instanceof Serializable))
        {
            object = createSerializableVersionOf(object);
        }
        this.object = object;
    }

    protected abstract T createSerializableVersionOf(T object);

    /**
     * @see org.apache.wicket.model.IDetachable#detach()
     */
    public void detach()
    {
        if (object instanceof IDetachable)
        {
            ((IDetachable) object).detach();
        }
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer("Model:classname=[");
        sb.append(getClass().getName()).append("]");
        sb.append(":object=[").append(object).append("]");
        return sb.toString();
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(object);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;

        }
        if (!(obj instanceof CollectionModelBase))
        {
            return false;
        }
        CollectionModelBase<?> that = (CollectionModelBase<?>) obj;
        return Objects.equal(object, that.object);
    }
}
