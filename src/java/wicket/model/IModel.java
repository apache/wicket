/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.model;

import java.io.Serializable;

/**
 * A IModel wraps the actual model objects of components. IModel implementations are used
 * as a facade for the real model so that users have control over the actual persistence
 * strategy. Note that instances of implementations of this class will be stored in the
 * session. Hence, you should use (non-transient) instance variables sparcelingly.
 * @see wicket.model.IDetachableModel
 */
public interface IModel extends Serializable
{
    /**
     * Gets the model object.
     * @return the model object
     */
    public Object getObject();

    /**
     * Sets the model object.
     * @param object the model object
     */
    public void setObject(Object object);
}
