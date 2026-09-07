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
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.model.Model;
import org.apache.wicket.serialize.java.JavaSerializer;
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
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

/**
 * Measures writing a page to bytes, which is what {@code SerializingPageStore} does for every page
 * at the end of a request.
 * <p>
 * Parameterised by component count because the cost tracks the number of objects in the graph
 * rather than the size of the result: {@code ObjectOutputStream} keeps a handle table per stream
 * and grows it as it walks, so a page of many small components costs more than its byte count
 * suggests.
 * <p>
 * {@code -prof gc} is the point of this one. {@code gc.alloc.rate.norm} covers both the serialized
 * bytes and everything the stream allocated to produce them, so read it against the result size:
 * 50 components serialize to about 5kB, 500 components to about 37kB.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Fork(3)
@Threads(1)
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@State(Scope.Thread)
public class PageSerializationBenchmark
{
	@Param({"50", "500"})
	public int components;

	private WicketTester tester;

	private JavaSerializer serializer;

	private WebPage page;

	@Setup(Level.Trial)
	public void setUp()
	{
		tester = new WicketTester(new MockApplication());
		serializer = new JavaSerializer("benchmarks");
		page = new BenchmarkPage(components);
		tester.startPage(page);
	}

	@TearDown(Level.Trial)
	public void tearDown()
	{
		tester.destroy();
	}

	@Benchmark
	public byte[] serializePage()
	{
		return serializer.serialize(page);
	}

	private static class BenchmarkPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		BenchmarkPage(int components)
		{
			RepeatingView view = new RepeatingView("rv");
			for (int i = 0; i < components; i++)
			{
				WebMarkupContainer child = new WebMarkupContainer(view.newChildId());
				child.add(new Label("label", Model.of("value " + i)));
				view.add(child);
			}
			add(view);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class< ? > containerClass)
		{
			return new StringResourceStream(
				"<html><body><div wicket:id=\"rv\"><span wicket:id=\"label\"></span></div></body></html>");
		}
	}
}
