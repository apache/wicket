/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wicket.RequestCycle;

/**
 * Model that makes working with detachable models a breeze.
 * LoadableDetachableModel holds a temporary, transient model object, that is
 * set when {@link #getObject()} is called by calling abstract method
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
 * <p>
 * Though you can override methods {@link #onAttach()} and {@link #detach()} for
 * additional attach/ detach behavior, the point of this class is to hide as
 * much of the attaching/ detaching as possible. So you should rarely need to
 * override those methods, if ever.
 * </p>
 * 
 * @param <T>
 *            The Type
 * 
 * @author Eelco Hillenius
 * @author Igor Vaynberg
 */
public abstract class LoadableDetachableModel<T> extends AbstractReadOnlyModel<T>
{
	/** Logger. */
	private static final Logger log = LoggerFactory.getLogger(LoadableDetachableModel.class);

	/** keeps track of whether this model is attached or detached */
	private transient boolean attached = false;

	/** temporary, transient object. */
	private transient T tempModelObject;

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
	public LoadableDetachableModel(T object)
	{
		this.tempModelObject = object;
		attached = true;
	}

	/**
	 * @see wicket.model.IDetachable#detach()
	 */
	@Override
	public final void detach()
	{
		if (attached)
		{
			attached = false;
			tempModelObject = null;

			if (log.isDebugEnabled())
			{
				log.debug("removed transient object for " + this + ", requestCycle "
						+ RequestCycle.get());
			}
			onDetach();
		}
	}

	/**
	 * @see wicket.model.IModel#getObject()
	 */
	@Override
	public T getObject()
	{
		if (!attached)
		{
			attached = true;
			tempModelObject = load();

			if (log.isDebugEnabled())
			{
				log.debug("loaded transient object " + tempModelObject + " for " + this
						+ ", requestCycle " + RequestCycle.get());
			}

			onAttach();
		}
		return tempModelObject;
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
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer(super.toString());
		sb.append(":attached=").append(attached).append(":tempModelObject=[").append(
				this.tempModelObject).append("]");
		return sb.toString();
	}

	/**
	 * Loads and returns the (temporary) model object.
	 * 
	 * @return the (temporary) model object
	 */
	protected abstract T load();

	/**
	 * Attaches to the current request. Implement this method with custom
	 * behavior, such as loading the model object.
	 */
	protected void onAttach()
	{
	}

	/**
	 * Detaches from the current request. Implement this method with custom
	 * behavior, such as setting the model object to null.
	 */
	protected void onDetach()
	{
	}
}
