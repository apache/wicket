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
package wicket.examples.wizard.framework.beanedit;

import wicket.Component;
import wicket.model.IModel;

/**
 * Model that does not allow writing.
 *
 * @author Eelco Hillenius
 */
public abstract class ReadOnlyModel extends BeanModel
{

	/**
	 * Construct.
	 * @param nestedModel model that provides the java bean
	 */
	public ReadOnlyModel(IModel nestedModel)
	{
		super(nestedModel);
	}

	/**
	 * @see wicket.model.IModel#setObject(wicket.Component, java.lang.Object)
	 */
	public void setObject(Component component, Object object)
	{
		throw new UnsupportedOperationException("this (" + this + ") model is read-only");
	}
}
