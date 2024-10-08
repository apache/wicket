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
package org.apache.wicket.markup;

import java.text.ParseException;

import org.apache.wicket.markup.parser.AbstractMarkupFilter;
import org.apache.wicket.markup.parser.IMarkupFilter;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Hans Hosea Schaefer
 */
class MarkupFactoryTest extends WicketTestCase  {

	private IMarkupFilter dummyMarkupFilter = new DummyMarkupFilter() {};
	private IMarkupFilter secondMarkupFilter = new DummyMarkupFilter() {};

	@Test
	public void testAdditionalMarkupFiltersAdded() {
		final MarkupFactory markupFactory = new MarkupFactory();
		markupFactory.addAdditionalMarkupFilter(secondMarkupFilter);
		markupFactory.addAdditionalMarkupFilter(dummyMarkupFilter, secondMarkupFilter.getClass());

		final MarkupParser markupParser = markupFactory.newMarkupParser(new MarkupResourceStream(new StringResourceStream("<hello/>")));
		final MarkupParser.MarkupFilterList markupFilters = markupParser.getMarkupFilters();
		Assertions.assertTrue(markupFilters.contains(secondMarkupFilter));
		Assertions.assertTrue(markupFilters.contains(dummyMarkupFilter));
	}

	@Test
	public void testAdditionalMarkupFiltersAddedAndOneRemoved() {
		final MarkupFactory markupFactory = new MarkupFactory();
		markupFactory.addAdditionalMarkupFilter(secondMarkupFilter);
		markupFactory.addAdditionalMarkupFilter(dummyMarkupFilter, secondMarkupFilter.getClass());
		markupFactory.removeAdditionalMarkupFilter(secondMarkupFilter);

		final MarkupParser markupParser = markupFactory.newMarkupParser(new MarkupResourceStream(new StringResourceStream("<hello/>")));
		final MarkupParser.MarkupFilterList markupFilters = markupParser.getMarkupFilters();
		Assertions.assertFalse(markupFilters.contains(secondMarkupFilter));
		Assertions.assertTrue(markupFilters.contains(dummyMarkupFilter));
	}

	@Test
	public void testAdditionalMarkupFiltersAddedAndOneRemovedByClass() {
		final MarkupFactory markupFactory = new MarkupFactory();
		markupFactory.addAdditionalMarkupFilter(secondMarkupFilter);
		markupFactory.addAdditionalMarkupFilter(dummyMarkupFilter, secondMarkupFilter.getClass());
		markupFactory.removeAdditionalMarkupFilters(secondMarkupFilter.getClass());

		final MarkupParser markupParser = markupFactory.newMarkupParser(new MarkupResourceStream(new StringResourceStream("<hello/>")));
		final MarkupParser.MarkupFilterList markupFilters = markupParser.getMarkupFilters();
		Assertions.assertFalse(markupFilters.contains(secondMarkupFilter));
		Assertions.assertTrue(markupFilters.contains(dummyMarkupFilter));
	}

	@Test
	public void testAdditionalMarkupFiltersAddedAndAllRemovedByClass() {
		final MarkupFactory markupFactory = new MarkupFactory();
		markupFactory.addAdditionalMarkupFilter(secondMarkupFilter);
		markupFactory.addAdditionalMarkupFilter(dummyMarkupFilter, secondMarkupFilter.getClass());
		markupFactory.removeAdditionalMarkupFilters(IMarkupFilter.class);

		final MarkupParser markupParser = markupFactory.newMarkupParser(new MarkupResourceStream(new StringResourceStream("<hello/>")));
		final MarkupParser.MarkupFilterList markupFilters = markupParser.getMarkupFilters();
		Assertions.assertFalse(markupFilters.contains(secondMarkupFilter));
		Assertions.assertFalse(markupFilters.contains(dummyMarkupFilter));
	}


	static abstract class DummyMarkupFilter extends AbstractMarkupFilter {
		protected MarkupElement onComponentTag(final ComponentTag tag) throws ParseException {
			return null;
		}
	}
}