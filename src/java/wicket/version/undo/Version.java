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
package wicket.version.undo;

import java.util.ArrayList;
import java.util.List;

import wicket.Component;

/**
 * A version is a sequence of changes to a Page that can be undone.
 * 
 * @author Jonathan Locke
 */
class Version
{
	private List changes = new ArrayList();
	
	void componentAdded(Component component)
	{
		changes.add(new Add(component));
	}

	void componentModelChangeImpending(Component component)
	{
		
	}

	void componentRemoved(Component component)
	{
		changes.add(new Remove(component));		
	}
}
