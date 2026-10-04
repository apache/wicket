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
package org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.core.util.resource.locator.IResourceNameIterator;
import org.apache.wicket.core.util.resource.locator.IResourceStreamLocator;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.MarkupNotFoundException;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ResourceTemplateTest extends WicketTestCase
{
	private RuntimeConfigurationType configurationType = RuntimeConfigurationType.DEVELOPMENT;

	private final List<String> located = new ArrayList<>();

	@Override
	protected WebApplication newApplication()
	{
		return new MockApplication()
		{
			@Override
			public RuntimeConfigurationType getConfigurationType()
			{
				return configurationType;
			}
		};
	}

	@BeforeEach
	void countLocatedTemplates()
	{
		IResourceStreamLocator locator = tester.getApplication()
			.getResourceSettings()
			.getResourceStreamLocator();
		tester.getApplication()
			.getResourceSettings()
			.setResourceStreamLocator(new CountingLocator(locator, located));
	}

	@Test
	void namedTemplateIsLookedUpForTheLocaleOfTheTable()
	{
		ResourceDynamicColumn<String> column = greetingColumn();
		DynamicDataTable<String, String> table = startTable(column);

		assertEquals("Hello {{name}}", column.getTemplate(table));
		tester.assertContains("\"template\":\"Hello \\{\\{name\\}\\}\"");

		tester.getSession().setLocale(Locale.GERMAN);
		tester.startPage(tester.getLastRenderedPage());

		assertEquals("Hallo {{name}}", column.getTemplate(table));
		tester.assertContains("\"template\":\"Hallo \\{\\{name\\}\\}\"");
	}

	@Test
	void subclassReadsTheTemplateNamedAfterItsClass()
	{
		NamedAfterClassColumn column = new NamedAfterClassColumn();
		DynamicDataTable<String, String> table = startTable(column);

		assertEquals("<b>{{name}}</b>", column.getTemplate(table));
	}

	@Test
	void contributorReadsItsTemplateThroughTheCompoundColumn()
	{
		CompoundDynamicColumn<String> column = new CompoundDynamicColumn<>(Model.of("Greeting"),
			List.<IDynamicColumnContributor<String>> of(
				new ResourceDynamicColumnContributor<>(ResourceTemplateTest.class, "greeting"),
				() -> "!"));
		DynamicDataTable<String, String> table = startTable(column);

		assertEquals("Hello {{name}}!", column.getTemplate(table));
	}

	@Test
	void templateIsTheContentOfThePanelWithoutTheLicenseHeader()
	{
		ResourceDynamicColumn<String> column = greetingColumn();
		startTable(column);

		String page = tester.getLastResponseAsString();

		assertTrue(page.contains("\"template\":\"Hello {{name}}\""), page);
		assertFalse(page.contains("Licensed to the Apache"), page);
	}

	@Test
	void templateWithoutPanelIsTheWholeFile()
	{
		ResourceDynamicColumn<String> column = new ResourceDynamicColumn<>(Model.of("Plain"),
			ResourceTemplateTest.class, "plain");
		DynamicDataTable<String, String> table = startTable(column);

		String template = column.getTemplate(table);

		assertTrue(template.startsWith("<!--"), template);
		assertTrue(template.contains("Licensed to the Apache Software Foundation"), template);
		assertTrue(template.endsWith("-->\n<i>{{name}}</i>"), template);
	}

	@Test
	void tableCachesTheTemplateOutsideOfDevelopmentMode()
	{
		configurationType = RuntimeConfigurationType.DEPLOYMENT;
		ResourceDynamicColumn<String> column = greetingColumn();
		DynamicDataTable<String, String> table = startTable(column);

		column.getTemplate(table);
		column.getTemplate(table);

		assertEquals(1, located.size(), located.toString());
	}

	@Test
	void tableCachesTheTemplatePerLocale()
	{
		configurationType = RuntimeConfigurationType.DEPLOYMENT;
		ResourceDynamicColumn<String> column = greetingColumn();
		DynamicDataTable<String, String> table = startTable(column);

		assertEquals("Hello {{name}}", column.getTemplate(table));
		tester.getSession().setLocale(Locale.GERMAN);
		assertEquals("Hallo {{name}}", column.getTemplate(table));
		assertEquals("Hallo {{name}}", column.getTemplate(table));

		assertEquals(2, located.size(), located.toString());
	}

	@Test
	void tableReadsTheTemplateEveryTimeInDevelopmentMode()
	{
		ResourceDynamicColumn<String> column = greetingColumn();
		DynamicDataTable<String, String> table = startTable(column);

		column.getTemplate(table);
		column.getTemplate(table);

		assertEquals(3, located.size(), located.toString());
	}

	@Test
	void missingNamedTemplateIsReported()
	{
		ResourceDynamicColumn<String> column = new ResourceDynamicColumn<>(Model.of("Missing"),
			ResourceTemplateTest.class, "missing");
		DynamicDataTable<String, String> table = startTable(greetingColumn());

		MarkupNotFoundException e = assertThrows(MarkupNotFoundException.class,
			() -> column.getTemplate(table));

		assertTrue(e.getMessage().contains("'missing.html'"), e.getMessage());
		assertTrue(e.getMessage().contains(ResourceTemplateTest.class.getName()),
			e.getMessage());
	}

	@Test
	void missingClassTemplateIsReported()
	{
		WithoutTemplateColumn column = new WithoutTemplateColumn();
		DynamicDataTable<String, String> table = startTable(greetingColumn());

		MarkupNotFoundException e = assertThrows(MarkupNotFoundException.class,
			() -> column.getTemplate(table));

		assertTrue(e.getMessage().contains(WithoutTemplateColumn.class.getName()),
			e.getMessage());
	}

	private static ResourceDynamicColumn<String> greetingColumn()
	{
		return new ResourceDynamicColumn<>(Model.of("Greeting"), ResourceTemplateTest.class,
			"greeting");
	}

	private DynamicDataTable<String, String> startTable(IDynamicColumn<String> column)
	{
		TemplatePage page = new TemplatePage(column);
		tester.startPage(page);
		return page.table;
	}

	static class NamedAfterClassColumn extends ResourceDynamicColumn<String>
	{
		private static final long serialVersionUID = 1L;

		NamedAfterClassColumn()
		{
			super(Model.of("Name"));
		}
	}

	static class WithoutTemplateColumn extends ResourceDynamicColumn<String>
	{
		private static final long serialVersionUID = 1L;

		WithoutTemplateColumn()
		{
			super(Model.of("Nothing"));
		}
	}

	private static class CountingLocator implements IResourceStreamLocator
	{
		private final IResourceStreamLocator delegate;

		private final List<String> located;

		CountingLocator(IResourceStreamLocator delegate, List<String> located)
		{
			this.delegate = delegate;
			this.located = located;
		}

		@Override
		public IResourceStream locate(Class<?> clazz, String path)
		{
			return delegate.locate(clazz, path);
		}

		@Override
		public IResourceStream locate(Class<?> clazz, String path, String style,
			String variation, Locale locale, String extension, boolean strict)
		{
			if (path.endsWith("/greeting"))
			{
				located.add(path + "_" + locale);
			}
			return delegate.locate(clazz, path, style, variation, locale, extension, strict);
		}

		@Override
		public IResourceNameIterator newResourceNameIterator(String path, Locale locale,
			String style, String variation, String extension, boolean strict)
		{
			return delegate.newResourceNameIterator(path, locale, style, variation, extension,
				strict);
		}
	}

	private static class StringsProvider implements IDynamicDataProvider<String, String>
	{
		private static final long serialVersionUID = 1L;

		@Override
		public List<String> rows(long first, long count)
		{
			return List.of("Ada");
		}

		@Override
		public long size()
		{
			return 1;
		}

		@Override
		public String keyOf(String row)
		{
			return row;
		}

		@Override
		public String findByKey(String key)
		{
			return key;
		}

		@Override
		public Class<String> getKeyType()
		{
			return String.class;
		}
	}

	public static class TemplatePage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		final DynamicDataTable<String, String> table;

		TemplatePage(IDynamicColumn<String> column)
		{
			table = new DynamicDataTable<>("table", List.of(column), new StringsProvider(), 10);
			add(table);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<html><head></head><body><table wicket:id=\"table\"></table></body></html>");
		}
	}
}
