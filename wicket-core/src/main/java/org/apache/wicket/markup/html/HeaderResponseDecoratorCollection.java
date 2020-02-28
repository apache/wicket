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
package org.apache.wicket.markup.html;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.ResourceAggregator;
import org.apache.wicket.util.lang.Args;

/**
 * A collection of {@link IHeaderResponseDecorator}s. The decorators will be nested oldest on the
 * inside, newest on the outside. By default {@link ResourceAggregator} is already registered.
 * 
 * @author Emond Papegaaij
 */
public class HeaderResponseDecoratorCollection implements IHeaderResponseDecorator
{
	private final List<IHeaderResponseDecorator> decorators = new CopyOnWriteArrayList<>();

	public HeaderResponseDecoratorCollection()
	{
		decorators.add(response -> new ResourceAggregator(response));
	}

	/**
	 * Adds a new {@link IHeaderResponseDecorator} that will be invoked prior to all already
	 * registered decorators. That means, the first to be added will be wrapped by a
	 * {@link ResourceAggregator} like this: {@code new ResourceAggregator(first)}. The second will
	 * be wrapped by the first and the aggregator: {@code new ResourceAggregator(first(second))}.
	 * 
	 * @param decorator
	 *            The decorator to add, cannot be null.
	 * @return {@code this} for chaining.
	 */
	public HeaderResponseDecoratorCollection add(IHeaderResponseDecorator decorator)
	{
		Args.notNull(decorator, "decorator");
		decorators.add(0, decorator);
		return this;
	}

	/**
	 * Adds a new {@link IHeaderResponseDecorator} that will be invoked after all already registered
	 * decorators.
	 * 
	 * @param decorator
	 *            The decorator to add, cannot be null.
	 * @return {@code this} for chaining.
	 */
	public HeaderResponseDecoratorCollection
			addPostProcessingDecorator(IHeaderResponseDecorator decorator)
	{
		Args.notNull(decorator, "decorator");
		decorators.add(decorator);
		return this;
	}

	/**
	 * Replaces all registered {@link IHeaderResponseDecorator}s with the given decorator. This also
	 * removes the {@link ResourceAggregator}, which is required to render resource dependencies.
	 * 
	 * @param decorator
	 *            The decorator to add, cannot be null.
	 * @return {@code this} for chaining.
	 */
	public HeaderResponseDecoratorCollection replaceAll(IHeaderResponseDecorator decorator)
	{
		Args.notNull(decorator, "decorator");
		decorators.clear();
		decorators.add(decorator);
		return this;
	}

	@Override
	public IHeaderResponse decorate(IHeaderResponse response)
	{
		IHeaderResponse ret = response;
		for (IHeaderResponseDecorator curDecorator : decorators)
			ret = curDecorator.decorate(ret);
		return ret;
	}
}
