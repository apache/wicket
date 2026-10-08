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

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.apache.wicket.core.util.resource.locator.ResourceNameIterator;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Benchmarks the candidate-filename walk that {@code ResourceStreamLocator} performs for every
 * property and markup lookup, once per registered properties loader.
 * <p>
 * The walk dominates i18n lookup cost on a miss, and misses are the common case: a key is resolved
 * by climbing the component hierarchy, so every class above the one that actually declares the key
 * contributes a full traversal that finds nothing.
 * <p>
 * The two cases are benchmarked directly rather than derived from one another, because they do
 * different amounts of work. {@link #walkAllCandidates} is the miss - every combination of style,
 * variation, locale and extension is produced. {@link #firstCandidate} is the hit, where the
 * locator stops at the first name and then reads back the locale, style and variation to stamp on
 * the resource stream; that read-back is the only caller of {@code getLocale()}, so a benchmark
 * that never performs it would miss the cost.
 * <p>
 * Written against public API only, so the same source can be run against two implementations and
 * compared. {@code -prof gc} is the point of this one: {@code gc.alloc.rate.norm} is what the
 * change being measured actually moves.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(3)
@Threads(1)
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class ResourceNameIteratorBenchmark
{
	/**
	 * The locale shapes differ in how many candidates they produce, which is the main driver of
	 * both time and allocation: a language+country locale yields three rounds, language-only two,
	 * and no locale one.
	 */
	public enum LocaleShape
	{
		LANGUAGE_COUNTRY(Locale.of("nl", "NL")),
		LANGUAGE_ONLY(Locale.of("nl")),
		WITH_VARIANT(Locale.of("nl", "NL", "vlaams")),
		NONE(null);

		private final Locale locale;

		LocaleShape(Locale locale)
		{
			this.locale = locale;
		}
	}

	// PropertiesFactory hands the locator a path with the extension already appended, and the
	// iterator splits it back off.
	private static final String PATH = "org/example/app/pages/group/GroupsPage.properties";

	private static final List<String> ONE_EXTENSION = Arrays.asList("properties");

	@Param
	public LocaleShape localeShape;

	@Param({"false", "true"})
	public boolean styled;

	@Benchmark
	public void walkAllCandidates(Blackhole blackhole)
	{
		ResourceNameIterator names = newIterator();
		while (names.hasNext())
		{
			blackhole.consume(names.next());
		}
	}

	@Benchmark
	public void firstCandidate(Blackhole blackhole)
	{
		ResourceNameIterator names = newIterator();
		if (names.hasNext())
		{
			blackhole.consume(names.next());
			blackhole.consume(names.getLocale());
			blackhole.consume(names.getStyle());
			blackhole.consume(names.getVariation());
		}
	}

	private ResourceNameIterator newIterator()
	{
		return new ResourceNameIterator(PATH, styled ? "mystyle" : null,
			styled ? "myvariation" : null, localeShape.locale, ONE_EXTENSION, false);
	}
}
