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
package wicket.examples.springframework2;

import org.springframework.context.ApplicationContext;

import wicket.model.IModel;

/**
 * Serves as convinient base class for {@link IModel}s that should access and
 * wrap transparent Spring managed beans.
 * 
 * @author Martin Fey
 */
public abstract class SpringAwareModel implements IModel
{
    protected SpringBeanModel springBeanModel = null;

    /** 
     * Creates a new instance of SpringAwareModel 
     * @param model
     */
    public SpringAwareModel(SpringBeanModel model)
    {
        this.springBeanModel = model;
    }

    /**
     * @return Spring application context
     */
    public final ApplicationContext getApplicationContext()
    {
        return this.springBeanModel.getApplicationContext();
    }

    /**
     * @see wicket.model.IModel#setObject(java.lang.Object)
     */
    public abstract void setObject(Object object);

    /**
     * @see wicket.model.IModel#getObject()
     */
    public abstract Object getObject();
}
