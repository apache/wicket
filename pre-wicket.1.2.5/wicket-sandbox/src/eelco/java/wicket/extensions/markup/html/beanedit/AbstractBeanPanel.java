/*
 * $Id$ $Revision$ $Date$
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
package wicket.extensions.markup.html.beanedit;

import java.io.Serializable;

import wicket.markup.html.panel.Panel;

/**
 * Abstract Panel for generic bean displaying/ editing. It's here to provide the
 * constructors, but does nothing else.
 * 
 * @author Eelco Hillenius
 */
public abstract class AbstractBeanPanel extends Panel
{
	/**
	 * Construct.
	 * 
	 * @param id
	 *            component id
	 * @param bean
	 *            JavaBean to be edited or displayed
	 */
	public AbstractBeanPanel(String id, Serializable bean)
	{
		this(id, new BeanModel(bean));
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *            component id
	 * @param beanModel
	 *            model with the JavaBean to be edited or displayed
	 */
	public AbstractBeanPanel(String id, BeanModel beanModel)
	{
		super(id, beanModel);
		if (beanModel == null)
		{
			throw new IllegalArgumentException("Argument beanModel must not be null");
		}
	}

	/**
	 * Gets the model casted to {@link BeanModel}.
	 * 
	 * @return the model casted to {@link BeanModel}
	 */
	protected final BeanModel getBeanModel()
	{
		return (BeanModel)getModel();
	}
}
