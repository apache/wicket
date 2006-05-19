/*
 * $Id$ $Revision$ $Date$
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
package wicket.examples.template;

import wicket.markup.html.panel.Panel;
import wicket.model.IModel;

/**
 * Base class for banners.
 * 
 * @author Eelco Hillenius
 */
public abstract class Banner extends Panel
{
	/**
	 * Construct.
	 * 
	 * @param id
	 */
	public Banner(String id)
	{
		super(id);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 * @param model
	 */
	public Banner(String id, IModel model)
	{
		super(id, model);
	}

}
