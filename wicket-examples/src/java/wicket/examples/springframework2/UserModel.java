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

import wicket.Component;

/**
 * Simple example class to access the Spring managed User bean.
 * 
 * @author Martin Fey
 */
public class UserModel extends SpringAwareModel
{
    private SimpleUser user = null;

    /**
     * Constructor
     * @param model The model
     */
    public UserModel(SpringBeanModel model)
    {
        super(model);
    }

    /**
     * @see wicket.examples.springframework2.SpringAwareModel#setObject(Component, Object)
     */
    public void setObject(final Component component, final Object object)
    {
        this.user = (SimpleUser) object;
    }

    /**
     * @see wicket.examples.springframework2.SpringAwareModel#getObject(Component)
     */
    public Object getObject(final Component component)
    {
        if (user == null)
        {
            user = (SimpleUser) getApplicationContext().getBean("user", SimpleUser.class);
        }
        return user;
    }

	/**
	 * @see wicket.model.IModel#getNestedModel()
	 */
	public Object getNestedModel()
	{
		return user;
	}
}
