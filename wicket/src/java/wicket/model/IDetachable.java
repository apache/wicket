/*
 * $Id$
 * $Revision$ $Date$
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

import java.io.Serializable;

/**
 * Interface to detachable things
 * 
 * @author Jonathan Locke
 */
public interface IDetachable extends Serializable
{
	/**
	 * Attaches model for use. This is generally used to fill in transient
	 * fields in a model which has been serialized during session replication.
	 */
	public void attach();

	/**
	 * Detaches model after use. This is generally used to null out transient
	 * references that can be re-attached via attach().
	 */
	public void detach();
}
