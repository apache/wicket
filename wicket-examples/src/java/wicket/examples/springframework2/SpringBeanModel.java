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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.springframework.context.ApplicationContext;

import wicket.RequestCycle;
import wicket.WicketRuntimeException;
import wicket.model.DetachableModel;
import wicket.model.IModel;

/**
 * Base class for transparent access of Spring managed beans. It can be used in
 * two modes. First an instance can be constructed by passing a Spring bean name
 * to one of it's constructors. An anonymous inner class of type IModel will be
 * created that wraps the Spring bean and stored as a member of this object.
 * Doing so you will depend in your pages (and other components) on the Spring
 * bean names as defined in in your Spring XML config. Second an instance can be
 * created by passing a subclass of {@link SpringAwareModel}. A SpringAwareModel
 * object will be created using the given class passing this object to it's
 * constructor. It will be stored as a member of this object. By this way you
 * can encapsulate the Spring bean names in a specific SpringAwareModel model
 * class.
 * 
 * @author Martin Fey
 */
public class SpringBeanModel extends DetachableModel
{
    private transient ApplicationContext applicationContext = null;

    private IModel model = null;

    /**
     * @return The spring application context
     */
    public final ApplicationContext getApplicationContext()
    {
        return applicationContext;
    }

    /**
     * Creates a new instance of a SpringBeanModel. An instance of interface
     * IModel will be created and stored as an instance member that delegates
     * all calls for the getObject method to
     * applicationContext.getBean($beanName) of this SpringBeanModel's reference
     * of the ApplicationContext. The calls for getObject and setObject on the
     * SpringBeanModel object will be delegated to the created IModel member
     * instance.
     * 
     * @param beanName The name of the Spring managed bean as defined in the
     * Spring ApplicationContext.
     */
    public SpringBeanModel(final String beanName)
    {
        super(null);
        
        if (beanName == null)
        {
            throw new IllegalArgumentException("beanName must not be null");
        }
        
        this.model = new IModel()
        {
            private Object object = null;

            public void setObject(Object object)
            {
                this.object = object;
            }

            public Object getObject()
            {
                if (object == null)
                {
                    object = getApplicationContext().getBean(beanName);
                }
                return object;
            }
        };
    }

    /**
     * Creates a new instance of SpringBeanModel taking a class of type
     * SpringAwareModel An object of class beanClass will be created by passing
     * this class' object to the beanClass' constructor. The created
     * SpringAwareModel is stored as an member of this object and all calls to
     * getObject and setObject will be delegated to that member.
     * 
     * @param beanClass The class of the SpringAwareModel subclass to
     *                  instanciate
     */
    public SpringBeanModel(Class beanClass)
    {
        super(null);
        
        if (beanClass == null)
        {
            throw new IllegalArgumentException("beanClass must not be null");
        }
        
        if (!SpringAwareModel.class.isAssignableFrom(beanClass))
        {
            throw new IllegalArgumentException(
                    "beanClass must be of type SpringAwareModel");
        }
        
        Constructor constructor = null;
        try
        {
            constructor = beanClass.getConstructor(new Class[] { this
                    .getClass() });
            
            this.model = (IModel) constructor.newInstance(new Object[] { this });
            
        } 
        catch (NoSuchMethodException e)
        {
            throw new WicketRuntimeException(e);
        } 
        catch (InstantiationException e)
        {
            throw new WicketRuntimeException("Cannot instantiate model object with "
                    + constructor, e);
        } 
        catch (IllegalAccessException e)
        {
            throw new WicketRuntimeException("Cannot access " + constructor, e);
        } 
        catch (InvocationTargetException e)
        {
            throw new WicketRuntimeException("Exception thrown by " + constructor, e);
        }
    }

    /**
     * @see wicket.model.DetachableModel#setObject(java.lang.Object)
     */
    public void setObject(Object object)
    {
        this.model.setObject(object);
    }

    /**
     * @see wicket.model.DetachableModel#getObject()
     */
    public Object getObject()
    {
        return this.model.getObject();
    }

    /**
     * Sets the Spring ApplicationContext. The SpringServiceLocator will use the
     * session of the given request cycle to determine the ApplicationContext
     * that has to be configured with the Spring's ContextLoaderListener. It
     * will not access the ApplicationContext of the DispatchServlet, because we
     * want to force the separation of the overall Wicket application
     * configuration from the configuration of the middle tier beans.
     */
    protected void doAttach()
    {
        this.applicationContext = SpringContextLocator.getApplicationContext(RequestCycle.get());
    }

    protected void doDetach()
    {
        this.applicationContext = null;
    }

}
