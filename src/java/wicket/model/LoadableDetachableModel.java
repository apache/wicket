/*
 * $Id$
 * $Revision$ $Date$
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
package wicket.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Component;
import wicket.RequestCycle;

/**
 * Model that makes working with detachable models a breeze.
 * LoadableDetachableModel holds a temporary, transient model object, that is
 * set when {@link #getObject(Component)} is called by calling abstract method
 * 'load', and that will be reset/ set to null on {@link #detach()}.
 * 
 * A usage example:
 * 
 * <pre>
 * LoadableDetachableModel venueListModel = new LoadableDetachableModel()
 * {
 * 	protected Object load()
 * 	{
 * 		return getVenueDao().findVenues();
 * 	}
 * };
 * </pre>
 * 
 * @author Eelco Hillenius
 * @author Igor Vaynberg
 */
public abstract class LoadableDetachableModel extends AbstractReadOnlyModel
{
	/** Logger. */
	private static final Log log = LogFactory.getLog(LoadableDetachableModel.class);

	/** temporary, transient object. */
	private transient Object tempModelObject;

	/** keeps track of whether this model is attached or detached */
	private transient boolean attached = false;

	/**
	 * Construct.
	 */
	public LoadableDetachableModel()
	{
	}

	/**
	 * This constructor is used if you already have the object retrieved and
	 * want to wrap it with a detachable model.
	 * 
	 * @param object
	 *            retrieved instance of the detachable object
	 */
	public LoadableDetachableModel(Object object)
	{
		this.tempModelObject = object;
		attached = true;
	}

	/**
	 * @see wicket.model.IModel#getObject(wicket.Component)
	 */
	public Object getObject(Component component)
	{
		if (!attached)
		{
			tempModelObject = load();
			attached = true;

			if (log.isDebugEnabled())
			{
				log.debug("loaded transient object " + tempModelObject + " for " + this
						+ ", requestCycle " + RequestCycle.get());
			}

		}
		return tempModelObject;
	}

	/**
	 * @see wicket.model.IDetachable#detach()
	 */
	public void detach()
	{
		boolean wasAttached = attached;
		tempModelObject = null;
		attached = false;

		if (wasAttached && log.isDebugEnabled())
		{
			log.debug("removed transient object for " + this + ", requestCycle "
					+ RequestCycle.get());
		}
	}

	/**
	 * Gets the attached status of this model instance
	 * 
	 * @return true if the model is attached, false otherwise
	 */
	public final boolean isAttached()
	{
		return attached;
	}

	/**
	 * Loads and returns the (temporary) model object.
	 * 
	 * @return the (temporary) model object
	 */
	protected abstract Object load();

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer(super.toString());
		sb.append(":attached=").append(attached).append(":tempModelObject=[").append(this.tempModelObject).append("]");
		return sb.toString();
	}
}
