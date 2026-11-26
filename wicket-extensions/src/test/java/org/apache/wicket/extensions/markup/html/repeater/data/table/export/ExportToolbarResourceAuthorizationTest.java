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
package org.apache.wicket.extensions.markup.html.repeater.data.table.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.extensions.markup.html.repeater.data.table.Contact;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceStreamResource;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.junit.jupiter.api.Test;

/**
 * {@link ExportToolbar} builds each of its export links from an {@link IResource} of its own, so
 * the exported contents of a data table are subject to the {@link IAuthorizationStrategy} like any
 * other resource a component holds.
 */
class ExportToolbarResourceAuthorizationTest extends WicketTestCase
{
	/** Every resource the strategy was asked about, in the order it was asked. */
	private final List<IResource> asked = new ArrayList<>();

	private void allowEverything(final boolean allowed)
	{
		tester.getApplication()
			.getSecuritySettings()
			.setAuthorizationStrategy(new IAuthorizationStrategy.AllowAllAuthorizationStrategy()
			{
				@Override
				public boolean isResourceAuthorized(IResource resource, PageParameters parameters)
				{
					asked.add(resource);
					return allowed;
				}
			});
	}

	/**
	 * The toolbar creates its resource and its link itself, so neither can be named from here.
	 */
	private ResourceLink<?> exportLink()
	{
		ResourceLink<?> link = tester.getLastRenderedPage()
			.visitChildren(ResourceLink.class, new IVisitor<ResourceLink<?>, ResourceLink<?>>()
			{
				@Override
				public void component(ResourceLink<?> link, IVisit<ResourceLink<?>> visit)
				{
					visit.stop(link);
				}
			});

		assertNotNull(link, "the export toolbar did not render an export link");
		return link;
	}

	@Test
	void exportIsRefusedWhenTheStrategyRefusesTheResource()
	{
		tester.startPage(new ExportPage());
		allowEverything(false);

		tester.clickLink(exportLink().getPageRelativePath(), false);

		assertEquals(HttpServletResponse.SC_FORBIDDEN, tester.getLastResponse().getStatus(),
			"the export should have been refused");
		assertFalse(tester.getLastResponseAsString().contains("clandestine"),
			"the exported data reached the response although the strategy refused it");
		assertEquals(1, asked.size(), "the strategy should have been asked once");
		assertTrue(asked.get(0) instanceof ResourceStreamResource,
			"expected the resource the toolbar builds over the exported data, was " + asked.get(0));
	}

	/**
	 * Control: the fix must not refuse an export the strategy allows.
	 */
	@Test
	void exportIsStillServedWhenTheStrategyAllowsTheResource()
	{
		tester.startPage(new ExportPage());
		allowEverything(true);

		tester.clickLink(exportLink().getPageRelativePath(), false);

		assertEquals(HttpServletResponse.SC_OK, tester.getLastResponse().getStatus());
		assertTrue(tester.getLastResponseAsString().contains("clandestine"),
			"the export did not contain the data of the table");
	}

	/** A table of one row with one exportable column, and a toolbar that exports it. */
	public static class ExportPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		ExportPage()
		{
			setStatelessHint(false);

			List<IColumn<Contact, String>> columns = new ArrayList<>();
			columns.add(new PropertyColumn<Contact, String>(Model.of("firstName"), "firstName"));

			DataTable<Contact, String> table = new DataTable<>("table", columns,
				new ListDataProvider<>(Arrays.asList(new Contact("clandestine", "row"))), 10);
			table.addBottomToolbar(new ExportToolbar(table, Model.of("Export to"),
				Model.of("export")).addDataExporter(new CSVDataExporter()));
			add(table);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<html><body><table wicket:id=\"table\"></table></body></html>");
		}
	}
}
