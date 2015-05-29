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
package org.apache.wicket.model;

import org.apache.wicket.request.cycle.RequestCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Model that makes working with detachable models a breeze. LoadableDetachableModel holds a
 * temporary, transient model object, that is set when {@link #getObject()} is called by calling
 * abstract method 'load', and that will be reset/ set to null on {@link #detach()}.
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
 * Though you can override methods {@link #onAttach()} and {@link #onDetach()} for additional
 * attach/ detach behavior, the point of this class is to hide as much of the attaching/ detaching
 * as possible. So you should rarely need to override those methods, if ever.
 * </p>
 * 
 * @author Eelco Hillenius
 * @author Igor Vaynberg
 * 
 * @param <T>
 *            The Model Object type
 */
public abstract class LoadableDetachableModel<T> implements IModel<T>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** Logger. */
	private static final Logger log = LoggerFactory.getLogger(LoadableDetachableModel.class);

	private enum AttachingState 
	{
		DETACHED(false, false),
		ATTACHING(true, false), 
		ATTACHED(true, true);

		private boolean attaching;
		private boolean attached;

		private AttachingState(boolean attaching, boolean attached)
		{
			this.attached = attached;
			this.attaching = attaching;
		}
		
		public boolean isAttached() 
		{
			return attached;
		}

		public boolean isAttaching() 
		{
			return attaching;
		}
		
		@Override
		public String toString()
		{
			return name().toLowerCase();
		}
	}

	/** keeps track of whether this model is attached or detached */
	private transient AttachingState attached = AttachingState.DETACHED;

	/** temporary, transient object. */
	private transient T transientModelObject;

	/**
	 * Construct.
	 */
	public LoadableDetachableModel()
	{
	}

	/**
	 * This constructor is used if you already have the object retrieved and want to wrap it with a
	 * detachable model.
	 * 
	 * @param object
	 *            retrieved instance of the detachable object
	 */
	public LoadableDetachableModel(T object)
	{
		this.transientModelObject = object;
		attached = AttachingState.ATTACHED;
	}

	/**
	 * @see org.apache.wicket.model.IDetachable#detach()
	 */
	@Override
	public void detach()
	{
		if (attached == AttachingState.ATTACHED)
		{
			try
			{
				onDetach();
			}
			finally
			{
				attached = AttachingState.DETACHED;
				transientModelObject = null;

				log.debug("removed transient object for {}, requestCycle {}", this,
					RequestCycle.get());
			}
		}
	}

	/**
	 * @see org.apache.wicket.model.IModel#getObject()
	 */
	@Override
	public final T getObject()
	{
		if (attached == AttachingState.DETACHED)
		{
			// prevent infinite attachment loops
			attached = AttachingState.ATTACHING;

			transientModelObject = load();

			if (log.isDebugEnabled())
			{
				log.debug("loaded transient object " + transientModelObject + " for " + this +
					", requestCycle " + RequestCycle.get());
			}

			attached = AttachingState.ATTACHED;
			onAttach();
		}
		return transientModelObject;
	}

	/**
	 * Gets the attached status of this model instance
	 * 
	 * @return true if the model is attached, false otherwise
	 */
	public final boolean isAttached()
	{
		return attached.isAttached();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(super.toString());
		sb.append(":attached=")
			.append(isAttached())
			.append(":tempModelObject=[")
			.append(this.transientModelObject)
			.append("]");
		return sb.toString();
	}

	/**
	 * Loads and returns the (temporary) model object.
	 * 
	 * @return the (temporary) model object
	 */
	protected abstract T load();

	/**
	 * Attaches to the current request. Implement this method with custom behavior, such as loading
	 * the model object.
	 */
	protected void onAttach()
	{
	}

	/**
	 * Detaches from the current request. Implement this method with custom behavior, such as
	 * setting the model object to null.
	 */
	protected void onDetach()
	{
	}


	/**
	 * Manually loads the model with the specified object. Subsequent calls to {@link #getObject()}
	 * will return {@code object} until {@link #detach()} is called.
	 * 
	 * @param object
	 *            The object to set into the model
	 */
	@Override
	public void setObject(final T object)
	{
		attached = AttachingState.ATTACHED;
		transientModelObject = object;
	}
}
