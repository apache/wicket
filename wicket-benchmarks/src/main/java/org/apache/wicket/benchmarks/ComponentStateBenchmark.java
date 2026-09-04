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
package org.apache.wicket.benchmarks;

import java.util.concurrent.TimeUnit;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.Model;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Benchmarks the per-request accessors on {@link Component}'s flexible state (model, behaviors and
 * meta data) plus the mutate-and-detach cycle.
 * <p>
 * Deliberately written against public Wicket API only, so that the exact same source can be run
 * against different implementations of the state storage and compared.
 * <p>
 * Three things are measured separately, because they answer different questions:
 * <ul>
 * <li>{@code read*} - the cost of reading state, per state shape. Reads do not mutate, so a
 * trial-scoped component is correct here and no per-invocation harness overhead is paid.
 * <li>{@code read*MixedShapes} - the same reads, but over a component array holding every shape at
 * once. This is the interesting one: with a single shape the call sites inside the state lookup are
 * monomorphic and inline, which flatters any implementation that dispatches on the shape. Real
 * pages interleave shapes. A large gap between the per-shape and mixed numbers is the signature of
 * dispatch that stopped inlining.
 * <li>{@code buildAndDetach} - construct a component, populate its state and detach it, as one
 * operation. Detaching mutates state (temporary behaviors are removed, arrays are compacted), so it
 * cannot be measured repeatedly against the same instance; folding construction into the operation
 * keeps every invocation doing the real work without resorting to {@code Level.Invocation}.
 * </ul>
 * Single threaded on purpose: component state is per component and never contended, so extra
 * threads measure nothing new while making the Wicket thread-local setup harder to get right.
 * <p>
 * Always run with {@code -prof gc}: {@code gc.alloc.rate.norm} (bytes per operation) is the number
 * that matters for a framework that has to keep many pages in memory, and it is far more stable
 * than throughput.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(3)
@Threads(1)
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
public class ComponentStateBenchmark
{
	static final MetaDataKey<String> KEY = new MetaDataKey<>()
	{
		private static final long serialVersionUID = 1L;
	};

	/** The eight shapes the flexible state of a component can take. */
	public enum Shape
	{
		NONE(false, false, false),
		MODEL(true, false, false),
		BEHAVIOR(false, true, false),
		METADATA(false, false, true),
		MODEL_BEHAVIOR(true, true, false),
		MODEL_METADATA(true, false, true),
		BEHAVIOR_METADATA(false, true, true),
		MODEL_BEHAVIOR_METADATA(true, true, true),
		/**
		 * A behavior with a stable id, as every link and ajax-enabled component has. Master keeps
		 * those ids in a {@code BehaviorIdList} held in the component's meta data; storing the id
		 * as the behavior's own array index removes that list, which WICKET-6774 claimed as its
		 * biggest saving. None of the other shapes exercise it.
		 */
		STABLE_ID_BEHAVIOR(false, false, false, true),
		MODEL_STABLE_ID_BEHAVIOR(true, false, false, true);

		private final boolean model;
		private final boolean behavior;
		private final boolean metaData;
		private final boolean stableId;

		Shape(boolean model, boolean behavior, boolean metaData)
		{
			this(model, behavior, metaData, false);
		}

		Shape(boolean model, boolean behavior, boolean metaData, boolean stableId)
		{
			this.model = model;
			this.behavior = behavior;
			this.metaData = metaData;
			this.stableId = stableId;
		}

		Component newComponent(String id)
		{
			Component c = new WebMarkupContainer(id);
			populate(c);
			return c;
		}

		void populate(Component c)
		{
			if (model)
			{
				c.setDefaultModel(Model.of(c.getId()));
			}
			if (behavior)
			{
				c.add(AttributeModifier.replace("class", "a"));
			}
			if (metaData)
			{
				c.setMetaData(KEY, "v");
			}
			if (stableId)
			{
				Behavior stable = new StableIdBehavior();
				c.add(stable);
				// rendering a callback url does this; it is what materialises the id storage
				c.getBehaviorId(stable);
			}
		}
	}

	/** Requires a stable behavior id, the way an ajax behavior or link does. */
	private static class StableIdBehavior extends Behavior
	{
		private static final long serialVersionUID = 1L;

		@Override
		public boolean getStatelessHint(Component component)
		{
			return false;
		}
	}

	/** One component of the shape under test: the state lookup sees a single shape. */
	@State(Scope.Benchmark)
	public static class OneShape
	{
		@Param
		public Shape shape;

		Component component;

		@Setup(Level.Trial)
		public void setUp()
		{
			WicketContext.attach();
			component = shape.newComponent("c");
		}

		@TearDown(Level.Trial)
		public void tearDown()
		{
			WicketContext.detach();
		}
	}

	/** Every shape at once: the state lookup sees all of them, as it does on a real page. */
	@State(Scope.Benchmark)
	public static class AllShapes
	{
		Component[] components;

		@Setup(Level.Trial)
		public void setUp()
		{
			WicketContext.attach();
			Shape[] shapes = Shape.values();
			components = new Component[shapes.length];
			for (int i = 0; i < shapes.length; i++)
			{
				components[i] = shapes[i].newComponent("c" + i);
			}
		}

		@TearDown(Level.Trial)
		public void tearDown()
		{
			WicketContext.detach();
		}
	}

	/** A component that definitely carries behaviors, for the Ajax id lookup path. */
	@State(Scope.Benchmark)
	public static class WithBehaviors
	{
		Component component;

		@Setup(Level.Trial)
		public void setUp()
		{
			WicketContext.attach();
			component = new WebMarkupContainer("c");
			component.setDefaultModel(Model.of("m"));
			component.setMetaData(KEY, "v");
			component.add(AttributeModifier.replace("class", "a"),
				AttributeModifier.replace("style", "b"), AttributeModifier.replace("title", "c"));
		}

		@TearDown(Level.Trial)
		public void tearDown()
		{
			WicketContext.detach();
		}
	}

	// ---------------------------------------------------------------- reads, one shape at a time

	@Benchmark
	public Object readMetaData(OneShape ctx)
	{
		return ctx.component.getMetaData(KEY);
	}

	@Benchmark
	public Object readModel(OneShape ctx)
	{
		return ctx.component.getDefaultModel();
	}

	@Benchmark
	public Object readBehaviors(OneShape ctx)
	{
		return ctx.component.getBehaviors(Behavior.class);
	}

	// ------------------------------------------------------------------- reads, shapes interleaved

	@Benchmark
	public void readMetaDataMixedShapes(AllShapes ctx, Blackhole bh)
	{
		for (Component c : ctx.components)
		{
			bh.consume(c.getMetaData(KEY));
		}
	}

	@Benchmark
	public void readModelMixedShapes(AllShapes ctx, Blackhole bh)
	{
		for (Component c : ctx.components)
		{
			bh.consume(c.getDefaultModel());
		}
	}

	@Benchmark
	public void readBehaviorsMixedShapes(AllShapes ctx, Blackhole bh)
	{
		for (Component c : ctx.components)
		{
			bh.consume(c.getBehaviors(Behavior.class));
		}
	}

	// ------------------------------------------------------------------------- the Ajax id lookup

	@Benchmark
	public Object readBehaviorById(WithBehaviors ctx)
	{
		return ctx.component.getBehaviorById(1);
	}

	// -------------------------------------------------------------------- mutate, then detach

	@Benchmark
	public Object buildAndDetach(OneShape ctx)
	{
		Component c = ctx.shape.newComponent("c");
		c.detach();
		return c;
	}

	@Benchmark
	public Object buildOnly(OneShape ctx)
	{
		return ctx.shape.newComponent("c");
	}
}
