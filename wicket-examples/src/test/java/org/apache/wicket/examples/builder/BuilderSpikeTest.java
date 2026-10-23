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
package org.apache.wicket.examples.builder;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.core.util.objects.checker.AbstractObjectChecker;
import org.apache.wicket.core.util.objects.checker.CheckingObjectOutputStream;
import org.apache.wicket.core.util.objects.checker.NotDetachedModelChecker;
import org.apache.wicket.core.util.objects.checker.ObjectSerializationChecker;
import org.apache.wicket.examples.builder.spike.ComponentBuilder;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * SPIKE - throwaway. Drives the built pages and reports how they serialize next to the originals.
 */
class BuilderSpikeTest extends WicketTestCase
{
	@Test
	void guestBook() throws IOException
	{
		tester.startPage(GuestBook.class);
		FormTester form = tester.newFormTester("commentForm");
		form.setValue("text", "first");
		form.submit();
		tester.assertModelValue("comments:0:text", "first");

		form = tester.newFormTester("commentForm");
		form.setValue("text", "spam");
		form.setValue("comment", "filled");
		form.submit();
		tester.assertErrorMessages("Caught a spammer!!!");
		tester.assertModelValue("comments:0:text", "first");

		check(tester.getLastRenderedPage());
		tester.startPage(org.apache.wicket.examples.guestbook.GuestBook.class);
		report("GuestBook", tester.getLastRenderedPage(), GuestBook.class);
	}

	@Test
	void todoList() throws IOException
	{
		tester.startPage(TodoList.class);
		tester.assertInvisible("addItems:form");

		tester.clickLink("addItems:link", true);
		tester.assertVisible("addItems:form");
		tester.assertInvisible("addItems:link");

		FormTester form = tester.newFormTester("addItems:form");
		form.setValue("text", "write the spike");
		tester.executeAjaxEvent("addItems:form:add", "click");
		tester.assertModelValue("showItems:item:0:text", "write the spike");
		tester.assertInvisible("addItems:form");

		AjaxCheckBox check = (AjaxCheckBox)tester.getComponentFromLastRenderedPage(
			"showItems:item:0:check");
		check.setModelObject(true);
		tester.clickLink("addItems:remove", true);
		tester.assertModelValue("showItems:item", List.of());

		check(tester.getLastRenderedPage());
		tester.startPage(org.apache.wicket.examples.ajax.builtin.TodoList.class);
		report("TodoList", tester.getLastRenderedPage(), TodoList.class);
	}

	@Test
	void editableLabels() throws IOException
	{
		tester.startPage(EditableLabelPage.class);
		tester.assertContains("0000000042");

		tester.clickLink("form:refresh-link");
		tester.assertLabel("form:refresh-counter", "1");

		check(tester.getLastRenderedPage());
		tester.startPage(org.apache.wicket.examples.ajax.builtin.EditableLabelPage.class);
		report("EditableLabelPage", tester.getLastRenderedPage(), EditableLabelPage.class);
	}

	private void check(Page page) throws IOException
	{
		page.detach();
		try (CheckingObjectOutputStream out = new CheckingObjectOutputStream(
			new ByteArrayOutputStream(), new ObjectSerializationChecker(),
			new NotDetachedModelChecker(), new NoBuilderChecker()))
		{
			out.writeObject(page);
		}
	}

	private void report(String name, Page original, Class<? extends Page> built)
	{
		Page builtPage = tester.startPage(built);
		byte[] originalBytes = serialize(original);
		byte[] builtBytes = serialize(builtPage);
		System.out.printf("SPIKE %-18s original %6d bytes, built %6d bytes%n", name,
			originalBytes.length, builtBytes.length);
		assertTrue(builtBytes.length > 0);
	}

	private static final class NoBuilderChecker extends AbstractObjectChecker
	{
		@Override
		protected Result doCheck(Object object)
		{
			return object instanceof ComponentBuilder
				? new Result(Result.Status.FAILURE, "a builder ended up in the page")
				: Result.SUCCESS;
		}
	}

	private byte[] serialize(Page page)
	{
		page.detach();
		return tester.getApplication().getFrameworkSettings().getSerializer().serialize(page);
	}
}
