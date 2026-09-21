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
import org.apache.wicket.benchmarks.AdditionalModelsShapes.Variant;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.openjdk.jol.info.GraphLayout;

/**
 * What it costs to let {@code Component} detach a component's extra models instead of detaching
 * them by hand, in bytes.
 * <p>
 * Three ways of holding the same extra models are measured against the same component carrying
 * only a default model, so the difference is the bookkeeping and not the models:
 * <ul>
 * <li>{@link Variant#FIELDS} - the way it was written before Wicket 11: a field per model and an
 * {@code onDetach()} detaching each one.
 * <li>{@link Variant#REGISTERED} - a field per model, each registered with
 * {@code addAdditionalModel}, no {@code onDetach()}.
 * <li>{@link Variant#REGISTERED_NO_FIELDS} - registered through the constructor and never
 * referenced again, which is what a component passing its models straight to children can do.
 * </ul>
 * Run for one, two and three extra models: the interesting question is not only how much the meta
 * data costs but whether it grows with the number of models, which is what decides how the two
 * approaches compare for a component holding several.
 * <p>
 * Reports retained heap via JOL, what a live page costs in the page cache, and serialized bytes,
 * what it costs in the page store. Run it twice, with and without
 * {@code -XX:+UseCompactObjectHeaders}.
 * <p>
 * Not a JMH benchmark - it measures size, not time, so it is a plain main. For the time side see
 * {@link AdditionalModelsBenchmark}.
 */
public final class AdditionalModelsFootprint
{
	private static final int CHILDREN = 1_000;

	private static final int[] EXTRA_MODELS = { 1, 2, 3 };

	private AdditionalModelsFootprint()
	{
	}

	public static void main(String[] args) throws Exception
	{
		WicketContext.attach();
		try
		{
			System.out.printf("Additional models footprint, %d children per tree%n", CHILDREN);
			System.out.printf("compact object headers: %s%n%n", compactHeaders());

			long baseHeap = retained(tree(Variant.FIELDS, 0));
			long baseWire = serialized(tree(Variant.FIELDS, 0));
			System.out.printf("baseline, default model only: heap %d, wire %d%n%n", baseHeap,
				baseWire);

			System.out.printf("%-22s %6s %12s %12s %10s %12s %12s %10s%n", "variant", "extra",
				"heap", "heap-Δ", "Δ/comp", "wire", "wire-Δ", "Δ/comp");
			System.out.println("-".repeat(104));

			for (int extra : EXTRA_MODELS)
			{
				for (Variant variant : Variant.values())
				{
					long heap = retained(tree(variant, extra));
					long wire = serialized(tree(variant, extra));
					System.out.printf("%-22s %6d %12d %12d %10.1f %12d %12d %10.1f%n", variant,
						extra, heap, heap - baseHeap, (heap - baseHeap) / (double)CHILDREN, wire,
						wire - baseWire, (wire - baseWire) / (double)CHILDREN);
				}
				System.out.println();
			}

			System.out.printf("%nWhere the bytes are, REGISTERED with %d extra models:%n%n",
				EXTRA_MODELS[EXTRA_MODELS.length - 1]);
			System.out.println(GraphLayout
				.parseInstance(tree(Variant.REGISTERED, EXTRA_MODELS[EXTRA_MODELS.length - 1]))
				.toFootprint());
		}
		finally
		{
			WicketContext.detach();
		}
	}

	/** A parent with {@link #CHILDREN} children, each holding its extra models the given way. */
	private static WebMarkupContainer tree(Variant variant, int extraModels)
	{
		WebMarkupContainer parent = new WebMarkupContainer("parent");
		for (int i = 0; i < CHILDREN; i++)
		{
			parent.add(variant.newComponent("c" + i, extraModels));
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
		return GraphLayout.parseInstance(new Object()).totalSize() <= 8 ? "on" : "off";
	}
}
