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

import org.apache.wicket.Component;
import org.apache.wicket.benchmarks.AdditionalModelsShapes.Variant;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

/**
 * What it costs in time and allocation to let {@code Component} detach a component's extra models
 * instead of detaching them by hand, for the shapes of {@link AdditionalModelsShapes}.
 * <p>
 * Two operations, because they answer different questions:
 * <ul>
 * <li>{@code build} - constructing the component. This is where registering does its extra work:
 * a meta data write and a copy of the model array per model registered.
 * <li>{@code buildAndDetach} - construct and detach, the whole per request cycle. Detaching
 * mutates state, so it cannot be measured repeatedly against the same instance; folding
 * construction into the operation keeps every invocation doing real work.
 * </ul>
 * Always run with {@code -prof gc}: {@code gc.alloc.rate.norm} is the number that decides whether
 * the convenience is affordable for a component rendered in large numbers, and it is far steadier
 * than throughput. For the memory a component keeps, rather than the garbage it makes, see
 * {@link AdditionalModelsFootprint}.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(3)
@Threads(1)
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@State(Scope.Thread)
public class AdditionalModelsBenchmark
{
	@Param
	public Variant variant;

	@Param({ "1", "2", "3" })
	public int extraModels;

	@Setup
	public void setUp()
	{
		WicketContext.attach();
	}

	@TearDown
	public void tearDown()
	{
		WicketContext.detach();
	}

	@Benchmark
	public Component build()
	{
		return variant.newComponent("c", extraModels);
	}

	@Benchmark
	public void buildAndDetach(Blackhole blackhole)
	{
		Component component = variant.newComponent("c", extraModels);
		component.detach();
		blackhole.consume(component);
	}
}
