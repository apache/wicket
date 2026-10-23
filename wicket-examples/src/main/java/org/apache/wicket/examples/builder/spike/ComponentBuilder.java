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
package org.apache.wicket.examples.builder.spike;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.lang.Args;

/**
 * SPIKE - throwaway. Base of every builder: creates the component when it is added and then
 * applies the configured steps in order.
 * <p>
 * A builder is used once, while the page is being constructed, and is never stored in the
 * component tree. The steps are therefore plain, non-serializable consumers and may capture
 * anything. Lambdas a builder hands to the component it creates, such as event handlers, are
 * serializable and become part of the page.
 *
 * @param <C>
 *            the type of the component built
 * @param <B>
 *            the type of the builder itself
 */
public abstract class ComponentBuilder<C extends Component, B extends ComponentBuilder<C, B>>
{
	private final String id;

	private final List<Consumer<? super C>> steps = new ArrayList<>();

	protected ComponentBuilder(String id)
	{
		this.id = Args.notNull(id, "id");
	}

	protected abstract C create(String id);

	public B with(Behavior... behaviors)
	{
		return apply(component -> component.add(behaviors));
	}

	public B apply(Consumer<? super C> customizer)
	{
		steps.add(customizer);
		return self();
	}

	public B outputMarkupId()
	{
		return apply(component -> component.setOutputMarkupId(true));
	}

	public B visibleWhen(IModel<Boolean> condition)
	{
		return with(new VisibleWhen(condition));
	}

	public C build()
	{
		C component = create(id);
		steps.forEach(step -> step.accept(component));
		return component;
	}

	@SuppressWarnings("unchecked")
	protected final B self()
	{
		return (B)this;
	}

	private static final class VisibleWhen extends Behavior
	{
		private static final long serialVersionUID = 1L;

		private final IModel<Boolean> condition;

		private VisibleWhen(IModel<Boolean> condition)
		{
			this.condition = Args.notNull(condition, "condition");
		}

		@Override
		public void onConfigure(Component component)
		{
			component.setVisible(Boolean.TRUE.equals(condition.getObject()));
		}

		@Override
		public void detach(Component component)
		{
			condition.detach();
		}
	}
}
