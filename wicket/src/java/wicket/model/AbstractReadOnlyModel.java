/*
 * $Id: AbstractReadOnlyModel.java 5861 2006-05-25 20:55:07 +0000 (Thu, 25 May
 * 2006) eelco12 $ $Revision$ $Date: 2006-05-25 20:55:07 +0000 (Thu, 25
 * May 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
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
 * AbstractReadOnlyModel is an adapter base class for implementing models which
 * have no detach logic and are read-only.
 * 
 * @param <T>
 *            The Type
 * 
 * @author Igor Vaynberg ( ivaynberg )
 */
public abstract class AbstractReadOnlyModel<T> extends AbstractModel<T>
{

	/**
	 * @see IModel#getObject()
	 */
	public abstract T getObject();

	/**
	 * This default implementation of setObject unconditionally throws an
	 * UnsupportedOperationException. Since the method is final, any subclass is
	 * effectively a read-only model.
	 * @param object
	 *            The object to set into the model
	 * 
	 * @throws UnsupportedOperationException
	 */
	public final void setObject(final T object)
	{
		throw new UnsupportedOperationException("Model " + getClass()
				+ " does not support setObject(Object)");
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return super.toString();
	}
}