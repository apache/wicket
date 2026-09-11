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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.apache.wicket.Component;
import org.apache.wicket.benchmarks.ComponentStateBenchmark.Shape;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.openjdk.jol.info.GraphLayout;

/**
 * Reports what component state actually costs, which is what the single-field packing in
 * {@code Component.data} exists to minimise and what no throughput benchmark can tell you.
 * <p>
 * Two numbers per state shape, both measured against an identical tree whose components carry no
 * state at all, so the difference isolates the state itself:
 * <ul>
 * <li><b>retained heap</b>, via JOL's graph walk - what a live page costs in the page cache.
 * <li><b>serialized bytes</b>, via Java serialization - what it costs in the page store, and the
 * constraint that any extra wrapper class also pays for its class descriptor.
 * </ul>
 * Run it twice, with and without {@code -XX:+UseCompactObjectHeaders}: that flag shifts every
 * object by 4 bytes and can change which layout wins.
 * <p>
 * Not a JMH benchmark - it measures size, not time, so it is a plain main.
 */
public final class ComponentFootprint
{
	private static final int CHILDREN = 1_000;

	private ComponentFootprint()
	{
	}

	public static void main(String[] args) throws Exception
	{
		WicketContext.attach();
		try
		{
			System.out.printf("Component state footprint, %d children per tree%n", CHILDREN);
			System.out.printf("compact object headers: %s%n%n", compactHeaders());

			long baseHeap = retained(tree(Shape.NONE));
			long baseWire = serialized(tree(Shape.NONE));

			System.out.printf("%-24s %12s %12s %10s %12s %12s %10s%n", "shape", "heap", "heap-Δ",
				"Δ/comp", "wire", "wire-Δ", "Δ/comp");
			System.out.println("-".repeat(98));

			for (Shape shape : Shape.values())
			{
				long heap = retained(tree(shape));
				long wire = serialized(tree(shape));
				System.out.printf("%-24s %12d %12d %10.1f %12d %12d %10.1f%n", shape, heap,
					heap - baseHeap, (heap - baseHeap) / (double)CHILDREN, wire, wire - baseWire,
					(wire - baseWire) / (double)CHILDREN);
			}

			Shape detail = args.length > 0 ? Shape.valueOf(args[0])
				: Shape.MODEL_BEHAVIOR_METADATA;
			System.out.printf("%n%nWhere the bytes are, for %s:%n%n", detail);
			System.out.println(GraphLayout.parseInstance(tree(detail)).toFootprint());
		}
		finally
		{
			WicketContext.detach();
		}
	}

	/** A parent with {@link #CHILDREN} children, each carrying the given state shape. */
	private static WebMarkupContainer tree(Shape shape)
	{
		WebMarkupContainer parent = new WebMarkupContainer("parent");
		for (int i = 0; i < CHILDREN; i++)
		{
			Component child = new WebMarkupContainer("c" + i);
			shape.populate(child);
			parent.add(child);
		}
		return parent;
	}

	private static long retained(Object root)
	{
		return GraphLayout.parseInstance(root).totalSize();
	}

	private static long serialized(Component root) throws IOException
	{
		root.detach();
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (ObjectOutputStream out = new ObjectOutputStream(bytes))
		{
			out.writeObject(root);
		}
		return bytes.size();
	}

	private static String compactHeaders()
	{
		// a plain Object is 16 bytes with 12-byte headers, 8 with compact ones
		long size = GraphLayout.parseInstance(new Object()).totalSize();
		return size <= 8 ? "on" : "off";
	}
}
