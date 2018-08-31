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
package org.apache.wicket.resource.aggregator;

import static org.apache.wicket.markup.head.JavaScriptHeaderItem.forReference;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.PriorityHeaderItem;
import org.apache.wicket.markup.head.ResourceAggregator;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.resource.CircularDependencyException;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link org.apache.wicket.markup.head.ResourceAggregator} class.
 * 
 * @author Hielke Hoeve
 */
class ResourceAggregatorTest extends WicketTestCase
{
	private void assertItems(ResourceReference... references)
	{
		List<HeaderItem> items = new ArrayList<HeaderItem>();
		for (ResourceReference curReference : references)
			items.add(forReference(curReference));
		assertItems(items);
	}

	private void assertItems(HeaderItem... items)
	{
		assertItems(Arrays.asList(items));
	}

	private void assertItems(List<HeaderItem> items)
	{
		aggregator.close();
		assertEquals(items, responseStub.getItems());
	}

	private TestHeaderResponse responseStub;
	private ResourceAggregator aggregator;

	/**
	 * Setup the testcase, creating a new header response stub and wrapping it in a resource
	 * aggregator
	 */
	@BeforeEach
	void setup()
	{
		responseStub = new TestHeaderResponse();
		aggregator = new ResourceAggregator(responseStub);
	}

	/**
	 * render [b->a], should render [a,b]
	 */
	@Test
	void testDependency()
	{
		aggregator.render(forReference(new ResourceReferenceB()));
		assertItems(new ResourceReferenceA(), new ResourceReferenceB());
	}

	/**
	 * render [b->a, c->a], should render [a,b,c]
	 */
	@Test
	void test2RefsWithDependency()
	{
		aggregator.render(forReference(new ResourceReferenceB()));
		aggregator.render(forReference(new ResourceReferenceC()));
		assertItems(new ResourceReferenceA(), new ResourceReferenceB(), new ResourceReferenceC());
	}

	/**
	 * render [d->c->a], should render [a, c, d]
	 */
	@Test
	void testTransitiveDependencies()
	{
		aggregator.render(forReference(new ResourceReferenceD()));
		assertItems(new ResourceReferenceA(), new ResourceReferenceC(), new ResourceReferenceD());
	}

	/**
	 * bundle {a, b->a}, render [a], should render [ab]
	 */
	@Test
	void testBundle()
	{
		HeaderItem bundleAB = Application.get()
			.getResourceBundles()
			.addJavaScriptBundle(Application.class, "ab.js", new ResourceReferenceA(),
				new ResourceReferenceB());
		aggregator.render(forReference(new ResourceReferenceA()));
		assertItems(bundleAB);
	}

	/**
	 * bundle {a, b->a}, render [b], should render [ab]
	 */
	@Test
	void testBundleRenderingOther()
	{
		HeaderItem bundleAB = Application.get()
			.getResourceBundles()
			.addJavaScriptBundle(Application.class, "ab.js", new ResourceReferenceA(),
				new ResourceReferenceB());
		aggregator.render(forReference(new ResourceReferenceB()));
		assertItems(bundleAB);
	}

	/**
	 * bundle {a, b->a}, render [a, b], should render [ab]
	 */
	@Test
	void testBundleRenderingBoth()
	{
		HeaderItem bundleAB = Application.get()
			.getResourceBundles()
			.addJavaScriptBundle(Application.class, "ab.js", new ResourceReferenceA(),
				new ResourceReferenceB());
		aggregator.render(forReference(new ResourceReferenceA()));
		aggregator.render(forReference(new ResourceReferenceB()));
		assertItems(bundleAB);
	}

	/**
	 * bundle {a, b->a}, render [d->c->a], should render [ab, c, d]
	 */
	@Test
	void testBundleRenderedAsDependency()
	{
		HeaderItem bundleAB = Application.get()
			.getResourceBundles()
			.addJavaScriptBundle(Application.class, "ab.js", new ResourceReferenceA(),
				new ResourceReferenceB());
		aggregator.render(forReference(new ResourceReferenceD()));
		assertItems(bundleAB, forReference(new ResourceReferenceC()),
			forReference(new ResourceReferenceD()));
	}

	/**
	 * bundle {c->a, d->c->a}, render [d], should render [a, cd]
	 */
	@Test
	void testBundleWithDependencies()
	{
		HeaderItem bundleCD = Application.get()
			.getResourceBundles()
			.addJavaScriptBundle(Application.class, "cd.js", new ResourceReferenceC(),
				new ResourceReferenceD());
		aggregator.render(forReference(new ResourceReferenceD()));
		assertItems(forReference(new ResourceReferenceA()), bundleCD);
	}


	/**
	 * bundle {a, b->a} and {c->a, d->c->a}, render [d], should render [ab, cd]
	 */
	@Test
	void testTwoBundlesWithDependencies()
	{
		HeaderItem bundleAB = Application.get()
			.getResourceBundles()
			.addJavaScriptBundle(Application.class, "ab.js", new ResourceReferenceA(),
				new ResourceReferenceB());
		HeaderItem bundleCD = Application.get()
			.getResourceBundles()
			.addJavaScriptBundle(Application.class, "cd.js", new ResourceReferenceC(),
				new ResourceReferenceD());
		aggregator.render(forReference(new ResourceReferenceD()));
		assertItems(bundleAB, bundleCD);
	}

	/**
	 * bundle {a, b->a} and {c->a, d->c->a}, render [priority(b), d], should render [priority(ab),
	 * cd]
	 */
	@Test
	void testTwoBundlesWithDependenciesAndPriority()
	{
		HeaderItem bundleAB = Application.get()
			.getResourceBundles()
			.addJavaScriptBundle(Application.class, "ab.js", new ResourceReferenceA(),
				new ResourceReferenceB());
		HeaderItem bundleCD = Application.get()
			.getResourceBundles()
			.addJavaScriptBundle(Application.class, "cd.js", new ResourceReferenceC(),
				new ResourceReferenceD());
		aggregator.render(new PriorityHeaderItem(forReference(new ResourceReferenceB())));
		aggregator.render(forReference(new ResourceReferenceD()));
		assertItems(new PriorityHeaderItem(bundleAB), bundleCD);
	}

	/**
	 * bundle {a, b->a} and {a, c->a}, should give exception
	 */
	@Test
	void testTwoBundlesProvidingSameResource()
	{
		assertThrows(IllegalArgumentException.class, () -> {
			Application.get()
					.getResourceBundles()
					.addJavaScriptBundle(Application.class, "ab.js", new ResourceReferenceA(),
										 new ResourceReferenceB());
			Application.get()
					.getResourceBundles()
					.addJavaScriptBundle(Application.class, "ac.js", new ResourceReferenceA(),
										 new ResourceReferenceC());
		});
	}

	/**
	 * render [circ1->circ2->circ1->...], should give exception
	 */
	@Test
	void testCircularDependency()
	{
		assertThrows(CircularDependencyException.class, () -> {
			aggregator.render(forReference(new ResourceReferenceCirc1()));
		});
	}

	/**
	 * bundle {bun1 -> x, bun2 -> y}, render [bun1], should render [x, y, bun12]
	 */
	@Test
	void testTwoResourcesWithBundleAsDependency()
	{
		HeaderItem bundle12 = Application.get()
			.getResourceBundles()
			.addJavaScriptBundle(Application.class, "bun12.js", new ResourceReferenceBun1(),
				new ResourceReferenceBun2());
		aggregator.render(forReference(new ResourceReferenceBun1()));
		assertItems(forReference(new ResourceReferenceX()), forReference(new ResourceReferenceY()),
			bundle12);
	}

	/**
	 * bundle {a, b -> a}, render [x, priority(a)], should render [priority(ab), x]
	 */
	@Test
	void testBundleWithPriority()
	{
		HeaderItem bundleAB = Application.get()
			.getResourceBundles()
			.addJavaScriptBundle(Application.class, "ab.js", new ResourceReferenceA(),
				new ResourceReferenceB());
		aggregator.render(forReference(new ResourceReferenceX()));
		aggregator.render(new PriorityHeaderItem(forReference(new ResourceReferenceA())));
		assertItems(new PriorityHeaderItem(bundleAB), forReference(new ResourceReferenceX()));
	}
}
