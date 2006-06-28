/*
 * $Id$ $Revision$ $Date:
 * 2006-05-26 00:46:21 +0200 (vr, 26 mei 2006) $
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Component;
import wicket.util.lang.Classes;

/**
 * An add change operation.
 * 
 * @author Jonathan Locke
 */
class Add extends Change
{
	private static final long serialVersionUID = 1L;

	/** log. */
	private static final Log log = LogFactory.getLog(Add.class);

	/** subject. */
	private final Component component;

	/**
	 * Construct.
	 * 
	 * @param component
	 *            subject
	 */
	Add(final Component component)
	{
		if (component == null)
		{
			throw new IllegalArgumentException("argument component must be not null");
		}

		if (component.getParent() == null)
		{
			throw new IllegalStateException("component " + component + " doesn't have a parent");
		}

		if (log.isDebugEnabled())
		{
			log.debug("RECORD ADD: added " + component.getPath() + " ("
					+ Classes.simpleName(component.getClass()) + "@" + component.hashCode()
					+ ") to parent");
		}

		this.component = component;
	}

	/**
	 * @see wicket.version.undo.Change#undo()
	 */
	@Override
	public void undo()
	{
		if (log.isDebugEnabled())
		{
			log.debug("UNDO ADD: removing " + component.getPath() + " ("
					+ Classes.simpleName(component.getClass()) + "@" + component.hashCode()
					+ ") from parent");
		}

		component.remove();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "Add[component: " + component.getPath() + "]";
	}
}
