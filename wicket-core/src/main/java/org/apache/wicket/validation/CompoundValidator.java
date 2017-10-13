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
package org.apache.wicket.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.util.lang.Args;

/**
 * A compound {@link IValidator}. Once an error is reported against the {@link IValidatable} being
 * checked, the rest of the validator chain is ignored.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @param <T>
 *            type of validatable
 * @since 1.2.6
 */
public class CompoundValidator<T> extends Behavior implements IValidator<T>
{
	private static final long serialVersionUID = 1L;

	private final List<IValidator<T>> validators = new ArrayList<IValidator<T>>(2);

	/**
	 * Constructor.
	 */
	public CompoundValidator()
	{
	}

	/**
	 * Adds an <code>IValidator</code> to the chain of validators.
	 * 
	 * @param validator
	 *            an <code>IValidator</code> to be added
	 * @return this <code>ValidationError</code> for chaining purposes
	 */
	public final CompoundValidator<T> add(IValidator<T> validator)
	{
		Args.notNull(validator, "validator");

		validators.add(validator);
		return this;
	}

	/**
	 * @see IValidator#validate(IValidatable)
	 */
	@Override
	public final void validate(IValidatable<T> validatable)
	{
		Iterator<IValidator<T>> it = validators.iterator();
		while (it.hasNext() && validatable.isValid())
		{
			it.next().validate(validatable);
		}
	}

	/**
	 * Gets an unmodifiable list of the registered validators.
	 * 
	 * @return unmodifiable list of delegate {@link IValidator}s inside this one
	 */
	public final List<IValidator<T>> getValidators()
	{
		return Collections.unmodifiableList(validators);
	}

	@Override
	public void beforeRender(Component component)
	{
		for (IValidator<T> validator : validators) {
			if (validator instanceof Behavior) {
				((Behavior)validator).beforeRender(component);
			}
		}
	}

	@Override
	public void afterRender(Component component)
	{
		for (IValidator<T> validator : validators) {
			if (validator instanceof Behavior) {
				((Behavior)validator).afterRender(component);
			}
		}
	}

	@Override
	public void bind(Component component)
	{
		for (IValidator<T> validator : validators) {
			if (validator instanceof Behavior) {
				((Behavior)validator).bind(component);
			}
		}
	}

	@Override
	public void unbind(Component component)
	{
		for (IValidator<T> validator : validators) {
			if (validator instanceof Behavior) {
				((Behavior)validator).unbind(component);
			}
		}
	}
	
	@Override
	public void detach(Component component)
	{
		for (IValidator<T> validator : validators) {
			if (validator instanceof Behavior) {
				((Behavior)validator).detach(component);
			}
		}
	}

	@Override
	public void onException(Component component, RuntimeException exception)
	{
		for (IValidator<T> validator : validators) {
			if (validator instanceof Behavior) {
				((Behavior)validator).onException(component, exception);
			}
		}
	}

	public boolean getStatelessHint(Component component)
	{
		for (IValidator<T> validator : validators) {
			if (validator instanceof Behavior && ((Behavior)validator).getStatelessHint(component) == false) {
				return false;
			}
		}
		return super.getStatelessHint(component);
	}

	@Override
	public void onComponentTag(Component component, ComponentTag tag)
	{
		for (IValidator<T> validator : validators) {
			if (validator instanceof Behavior) {
				((Behavior)validator).onComponentTag(component, tag);
			}
		}
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response)
	{
		for (IValidator<T> validator : validators) {
			if (validator instanceof Behavior) {
				((Behavior)validator).renderHead(component, response);
			}
		}
	}

	@Override
	public void onConfigure(Component component)
	{
		for (IValidator<T> validator : validators) {
			if (validator instanceof Behavior) {
				((Behavior)validator).onConfigure(component);
			}
		}
	}

	@Override
	public void onEvent(Component component, IEvent<?> event)
	{
		for (IValidator<T> validator : validators) {
			if (validator instanceof Behavior) {
				((Behavior)validator).onEvent(component, event);
			}
		}
	}

	@Override
	public void onRemove(Component component)
	{
		for (IValidator<T> validator : validators) {
			if (validator instanceof Behavior) {
				((Behavior)validator).onRemove(component);
			}
		}
	}
}
