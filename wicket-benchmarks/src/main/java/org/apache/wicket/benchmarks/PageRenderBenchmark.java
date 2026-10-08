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
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.benchmarks.ComponentStateBenchmark.Shape;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTester;
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

/**
 * End-to-end render of a panel with 50 stateful children, one shape at a time.
 * <p>
 * This is a regression guard, not a measurement of state access: markup parsing, hierarchy
 * traversal and response writing dominate a render, so a change worth a few nanoseconds per field
 * read disappears into the noise here. Its job is to catch a change that made rendering as a whole
 * worse. Use {@link ComponentStateBenchmark} to attribute a difference to state handling, and
 * {@link ComponentFootprint} for anything about memory.
 * <p>
 * Replaces the seven near-identical methods of the original benchmark with one parameterised over
 * {@link Shape}, which also makes the missing combinations measurable.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Fork(3)
@Threads(1)
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@State(Scope.Thread)
public class PageRenderBenchmark
{
	private static final int CHILDREN = 50;

	@Param
	public Shape shape;

	private WicketTester tester;

	@Setup(Level.Trial)
	public void setUp()
	{
		tester = new WicketTester(new MockApplication());
	}

	@TearDown(Level.Trial)
	public void tearDown()
	{
		tester.destroy();
	}

	@Benchmark
	public Object renderPanel()
	{
		return tester.startComponentInPage(new StatefulPanel("panel", shape));
	}

	private static class StatefulPanel extends Panel implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		StatefulPanel(String id, Shape shape)
		{
			super(id);
			RepeatingView view = new RepeatingView("rv");
			for (int i = 0; i < CHILDREN; i++)
			{
				Component child = new WebMarkupContainer(view.newChildId());
				shape.populate(child);
				view.add(child);
			}
			add(view);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class< ? > containerClass)
		{
			return new StringResourceStream(
				"<wicket:panel><div wicket:id=\"rv\"></div></wicket:panel>");
		}
	}
}
