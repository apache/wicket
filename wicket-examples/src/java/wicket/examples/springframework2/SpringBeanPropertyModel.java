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

import wicket.model.PropertyModel;

/**
 * This class adds the power of Wicket's PropertyModel to the SpringBeanModel.
 * 
 * @author Martin Fey
 */
public class SpringBeanPropertyModel extends PropertyModel
{
    /**
     * Creates a new instance of a SpringBeanPropertyModel by simply creating a
     * SpringBeanModel with the given beanName parameter and adding that model
     * together with the given OGNL expression to the super constructor.
     * 
     * @param beanName The name of the Spring managed bean as defined in the
     *                 Spring's ApplicationContext.
     * @param expression The OGNL expression that should work on the Spring
     *                   bean
     */
    public SpringBeanPropertyModel(final String beanName, String expression)
    {
        super(new SpringBeanModel(beanName), expression);
    }

    /**
     * Creates a new instance of SpringBeanPropertyModel by simply creating a
     * SpringBeanModel with the given beanClass parameter and adding that model
     * object with the given OGNL expression to the super constructor.
     * 
     * @param beanClass The class of a subclass of SpringAwareModel.
     * @param expression The OGNL expression that should work on the Spring
     *        managed bean defined in the beanClass.
     */
    public SpringBeanPropertyModel(Class beanClass, String expression)
    {
        super(new SpringBeanModel(beanClass), expression);
    }

    /**
    * Construct.
    * @param model
    * @param expression
    */
   public SpringBeanPropertyModel(SpringBeanModel model, String expression)
    {
        super(model, expression);
    }
}
