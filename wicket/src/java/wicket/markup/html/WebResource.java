/*
 * $Id$ $Revision$
 * $Date$
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
package wicket.markup.html;

import wicket.Resource;
import wicket.util.resource.IResourceStream;

/**
 * Base class for web resources. See the base class {@link wicket.Resource}for
 * details on resources in general, including how they can be shared in an
 * application.
 * 
 * @author Jonathan Locke
 */
public abstract class WebResource extends Resource
{
	/**
	 * @see Resource#getResourceStream()
	 */
	protected abstract IResourceStream getResourceStream();
}
