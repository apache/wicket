/*
 * $Id$ $Revision:
 * 1.11 $ $Date$
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
 * AbstractModel is an adapter base class for implementing models which have no
 * attach/detach logic.
 * 
 * @author Jonathan Locke
 */
public abstract class AbstractModel implements IModel
{
	/** Serial Version ID. */
	private static final long serialVersionUID = 3031804965001519439L;

	/**
	 * @see wicket.model.IModel#attach()
	 */
	public void attach()
	{
	}

	/**
	 * @see wicket.model.IModel#detach()
	 */
	public void detach()
	{
	}
}