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

import java.util.Locale;

import org.danekja.java.util.function.serializable.SerializableSupplier;
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
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(LoadableDetachableModel.class);

	/** Internal state of the LoadableDetachableModel. */
	private enum InternalState {
		DETACHED, ATTACHING, ATTACHED;

		@Override
		public String toString()
		{
			return name().toLowerCase(Locale.ROOT);
		}
	}

	/** Keeps track of whether this model is attached or detached */
	private transient InternalState state = InternalState.DETACHED;

	/** temporary, transient object. */
	private transient T transientModelObject;

	/**
	 * Default constructor, constructs the model in detached state with no data associated with the
	 * model.
	 */
	public LoadableDetachableModel()
	{
	}

	/**
	 * This constructor is used if you already have the object retrieved and want to wrap it with a
	 * detachable model. Constructs the model in attached state. Calls to {@link #getObject()} will
	 * return {@code object} until {@link #detach()} is called.
	 * 
	 * @param object
	 *            retrieved instance of the detachable object
	 */
	public LoadableDetachableModel(T object)
	{
		this.transientModelObject = object;
		state = InternalState.ATTACHED;
	}

	@Override
	public final void detach()
	{
		// even if LDM is in partial attached state (ATTACHING) it should be detached
		if (state != null && state != InternalState.DETACHED)
		{
			try
			{
				onDetach();
			}
			finally
			{
				state = InternalState.DETACHED;
				transientModelObject = null;

				log.debug("removed transient object for '{}'", this);
			}
		}

		onDetachAlways();
	}

	@Override
	public final T getObject()
	{
		if (state == null || state == InternalState.DETACHED)
		{
			// prevent infinite attachment loops
			state = InternalState.ATTACHING;

			transientModelObject = load();

			if (log.isDebugEnabled())
			{
				log.debug("loaded transient object '{}' for '{}'", transientModelObject, this);
			}

			state = InternalState.ATTACHED;
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
		return state == InternalState.ATTACHED;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(super.toString());
		sb.append(":attached=")
			.append(isAttached())
			.append(":transientModelObject=[")
			.append(this.transientModelObject)
			.append(']');
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
	 * <p>
	 * This implementation delegates to {@link #onDetach(Object)}, passing the object this model is
	 * currently holding. An override that does not call {@code super.onDetach()} suppresses that
	 * callback.
	 */
	protected void onDetach()
	{
		onDetach(transientModelObject);
	}

	/**
	 * Detaches from the current request, handing over the object this model is holding before it is
	 * discarded. This is the place to release resources tied to that object.
	 * <p>
	 * The object is the one returned by the last {@link #load()} or passed to
	 * {@link #setObject(Object)}. It is {@code null} when that value was {@code null}, and also when
	 * the model is detached while still attaching - {@link #load()} is then either running or has
	 * thrown.
	 * 
	 * @param object
	 *            the object that was attached, may be {@code null}
	 * @since 11.0.0
	 */
	protected void onDetach(T object)
	{
	}

	/**
	 * Detaches from the current request, whether this model was attached or not. Unlike
	 * {@link #onDetach()} this is invoked on every call to {@link #detach()}, so it is the place for
	 * cleanup that is not tied to the loaded object - detaching models this one was handed, for
	 * instance, which may have been attached without this model ever loading.
	 * <p>
	 * When the model was attached, this runs after {@link #onDetach()} and after the loaded object
	 * has been discarded.
	 * 
	 * @since 11.0.0
	 */
	protected void onDetachAlways()
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
		state = InternalState.ATTACHED;
		transientModelObject = object;
	}
	
	/**
	 * Create a {@link LoadableDetachableModel} for the given supplier.
	 *
	 * @param <T>
	 * @param getter Used for the getObject() method.
	 * @return the model
	 */
	public static <T> LoadableDetachableModel<T> of(SerializableSupplier<T> getter)
	{
		return new LoadableDetachableModel<T>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected T load()
			{
				return getter.get();
			}
		};
	}
}
