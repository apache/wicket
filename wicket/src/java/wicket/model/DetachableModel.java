/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.model;

/**
 * This provide a base class to work with {@link wicket.model.IDetachableModel}.
 * It wraps the actual model objects of components and provides a call back
 * mechanism for reacting to the start/end of a request. doAttach will be called
 * at the first access to this model within a request and - if the model was
 * attached earlier, doDetach will be called at the end of the request. In
 * effect, attachment and detachment is only done when it is actually needed. As
 * the wrapped model object is transient, either attachment should be used to
 * ensure the model object is again available (after a possible serialization),
 * or extending classes should hold their own instance data representing the
 * model.
 * 
 * @author Chris Turner
 * @author Eelco Hillenius
 */
public abstract class DetachableModel implements IDetachableModel
{
    /**
     * Transient flag to prevent multiple detach/attach scenario. We need to
     * maintain this flag as we allow 'null' model values.
     */
    private transient boolean attached = false;

    /**
     * The wrapped model object. Note that this object is transient to ensure we
     * never serialize it even if the user forgets to set the object to null in
     * their detach() method.
     */
    private transient Object object;

    /**
     * Construct.
     */
    public DetachableModel()
    {
    }

    /**
     * Constructs the detachable model with the given model object.
     * 
     * @param object
     *            the model object
     */
    public DetachableModel(final Object object)
    {
        this.object = object;
    }

    /**
     * Attaches to the current session
     * 
     * @see wicket.model.IDetachableModel#attach()
     */
    public final void attach()
    {
        if (!attached)
        {
            doAttach();
            attached = true;
        }
    }

    /**
     * Detaches from the current session.
     * 
     * @see wicket.model.IDetachableModel#detach()
     */
    public final void detach()
    {
        if (attached)
        {
            doDetach();
            attached = false;
        }
    }

    /**
     * Gets the model object.
     * 
     * @return the model object
     * @see wicket.model.IModel#getObject()
     */
    public Object getObject()
    {
        return object;
    }

    /**
     * Gets whether this model has been attached to the current session.
     * 
     * @return whether this model has been attached to the current session
     */
    public boolean isAttached()
    {
        return attached;
    }

    /**
     * Sets the model object.
     * 
     * @param object
     *            the model object
     * @see wicket.model.IModel#setObject(java.lang.Object)
     */
    public void setObject(final Object object)
    {
        this.object = object;
    }

    /**
     * Attaches to the given session. Implement this method with custom
     * behaviour, such as loading the model object.
     */
    protected abstract void doAttach();

    /**
     * Detaches from the given session. Implement this method with custom
     * behaviour, such as setting the model object to null.
     */
    protected abstract void doDetach();
}