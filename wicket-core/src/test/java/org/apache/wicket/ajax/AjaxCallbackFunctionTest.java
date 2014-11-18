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
package org.apache.wicket.ajax;

import static org.apache.wicket.ajax.attributes.CallbackParameter.context;
import static org.apache.wicket.ajax.attributes.CallbackParameter.converted;
import static org.apache.wicket.ajax.attributes.CallbackParameter.explicit;
import static org.apache.wicket.ajax.attributes.CallbackParameter.resolved;

import org.apache.wicket.WicketTestCase;
import org.junit.Test;


public class AjaxCallbackFunctionTest extends WicketTestCase
{
	@Test
	public void testDefaultCallbackFunction()
	{
		AjaxCallbackPage page = tester.startPage(AjaxCallbackPage.class);
		assertEquals(//
			"function () {\n" //
				+ "var attrs = {\"u\":\"./wicket/bookmarkable/org.apache.wicket.ajax.AjaxCallbackPage?0-1.IBehaviorListener.0-\"};\n" //
				+ "var params = [];\n" //
				+ "attrs.ep = params.concat(attrs.ep);\n" //
				+ "Wicket.Ajax.ajax(attrs);\n" //
				+ "}\n", //
			page.getBehavior1().getCallbackFunction().toString());

		assertEquals(//
			"function () {\n" //
				+ "var attrs = {\"u\":\"./wicket/bookmarkable/org.apache.wicket.ajax.AjaxCallbackPage?0-1.IBehaviorListener.1-\"," //
				+ "\"ep\":[{\"name\":\"param1\",\"value\":123},{\"name\":\"param2\",\"value\":\"zh_CN\"}]};\n" //
				+ "var params = [];\n" //
				+ "attrs.ep = params.concat(attrs.ep);\n" //
				+ "Wicket.Ajax.ajax(attrs);\n" //
				+ "}\n", //
			page.getBehavior2().getCallbackFunction().toString());
	}

	@Test
	public void testCallbackFunctionWithContext()
	{
		AjaxCallbackPage page = tester.startPage(AjaxCallbackPage.class);
		assertEquals(//
			"function (context) {\n" //
				+ "var attrs = {\"u\":\"./wicket/bookmarkable/org.apache.wicket.ajax.AjaxCallbackPage?0-1.IBehaviorListener.0-\"};\n" //
				+ "var params = [];\n" //
				+ "attrs.ep = params.concat(attrs.ep);\n" //
				+ "Wicket.Ajax.ajax(attrs);\n" //
				+ "}\n", //
			page.getBehavior1().getCallbackFunction(context("context")).toString());

		assertEquals(//
			"function (context) {\n" //
				+ "var attrs = {\"u\":\"./wicket/bookmarkable/org.apache.wicket.ajax.AjaxCallbackPage?0-1.IBehaviorListener.1-\"," //
				+ "\"ep\":[{\"name\":\"param1\",\"value\":123},{\"name\":\"param2\",\"value\":\"zh_CN\"}]};\n" //
				+ "var params = [];\n" //
				+ "attrs.ep = params.concat(attrs.ep);\n" //
				+ "Wicket.Ajax.ajax(attrs);\n" //
				+ "}\n", //
			page.getBehavior2().getCallbackFunction(context("context")).toString());
	}

	@Test
	public void testCallbackFunctionWithExplicit()
	{
		AjaxCallbackPage page = tester.startPage(AjaxCallbackPage.class);
		assertEquals(//
			"function (explicit) {\n" //
				+ "var attrs = {\"u\":\"./wicket/bookmarkable/org.apache.wicket.ajax.AjaxCallbackPage?0-1.IBehaviorListener.0-\"};\n" //
				+ "var params = [{\"name\":\"explicit\",\"value\":explicit}];\n" //
				+ "attrs.ep = params.concat(attrs.ep);\n" //
				+ "Wicket.Ajax.ajax(attrs);\n" //
				+ "}\n", //
			page.getBehavior1().getCallbackFunction(explicit("explicit")).toString());

		assertEquals(//
			"function (explicit) {\n" //
				+ "var attrs = {\"u\":\"./wicket/bookmarkable/org.apache.wicket.ajax.AjaxCallbackPage?0-1.IBehaviorListener.1-\",\"ep\":[{\"name\":\"param1\",\"value\":123},{\"name\":\"param2\",\"value\":\"zh_CN\"}]};\n" //
				+ "var params = [{\"name\":\"explicit\",\"value\":explicit}];\n" //
				+ "attrs.ep = params.concat(attrs.ep);\n" //
				+ "Wicket.Ajax.ajax(attrs);\n" //
				+ "}\n", //
			page.getBehavior2().getCallbackFunction(explicit("explicit")).toString());
	}

	@Test
	public void testCallbackFunctionWithResolved()
	{
		AjaxCallbackPage page = tester.startPage(AjaxCallbackPage.class);
		assertEquals(//
			"function () {\n" //
				+ "var attrs = {\"u\":\"./wicket/bookmarkable/org.apache.wicket.ajax.AjaxCallbackPage?0-1.IBehaviorListener.0-\"};\n" //
				+ "var params = [{\"name\":\"resolved\",\"value\":window.location.href}];\n" //
				+ "attrs.ep = params.concat(attrs.ep);\n" //
				+ "Wicket.Ajax.ajax(attrs);\n" //
				+ "}\n", //
			page.getBehavior1()
				.getCallbackFunction(resolved("resolved", "window.location.href"))
				.toString());

		assertEquals(//
			"function () {\n" //
				+ "var attrs = {\"u\":\"./wicket/bookmarkable/org.apache.wicket.ajax.AjaxCallbackPage?0-1.IBehaviorListener.1-\"," //
				+ "\"ep\":[{\"name\":\"param1\",\"value\":123},{\"name\":\"param2\",\"value\":\"zh_CN\"}]};\n" //
				+ "var params = [{\"name\":\"resolved\",\"value\":window.location.href}];\n" //
				+ "attrs.ep = params.concat(attrs.ep);\n" //
				+ "Wicket.Ajax.ajax(attrs);\n" //
				+ "}\n", //
			page.getBehavior2()
				.getCallbackFunction(resolved("resolved", "window.location.href"))
				.toString());
	}

	@Test
	public void testCallbackFunctionWithConverted()
	{
		AjaxCallbackPage page = tester.startPage(AjaxCallbackPage.class);
		assertEquals(
			//
			"function (converted) {\n" //
				+ "var attrs = {\"u\":\"./wicket/bookmarkable/org.apache.wicket.ajax.AjaxCallbackPage?0-1.IBehaviorListener.0-\"};\n" //
				+ "var params = [{\"name\":\"converted\",\"value\":converted.substring(0, 3)}];\n" //
				+ "attrs.ep = params.concat(attrs.ep);\n" //
				+ "Wicket.Ajax.ajax(attrs);\n" //
				+ "}\n", //
			page.getBehavior1()
				.getCallbackFunction(converted("converted", "converted.substring(0, 3)"))
				.toString());

		assertEquals(
			//
			"function (converted) {\n" //
				+ "var attrs = {\"u\":\"./wicket/bookmarkable/org.apache.wicket.ajax.AjaxCallbackPage?0-1.IBehaviorListener.1-\"," //
				+ "\"ep\":[{\"name\":\"param1\",\"value\":123},{\"name\":\"param2\",\"value\":\"zh_CN\"}]};\n" //
				+ "var params = [{\"name\":\"converted\",\"value\":converted.substring(0, 3)}];\n" //
				+ "attrs.ep = params.concat(attrs.ep);\n" //
				+ "Wicket.Ajax.ajax(attrs);\n" //
				+ "}\n", //
			page.getBehavior2()
				.getCallbackFunction(converted("converted", "converted.substring(0, 3)"))
				.toString());
	}

	@Test
	public void testCallbackFunctionWithAll()
	{
		AjaxCallbackPage page = tester.startPage(AjaxCallbackPage.class);
		assertEquals(
			//
			"function (context,explicit,converted) {\n" //
				+ "var attrs = {\"u\":\"./wicket/bookmarkable/org.apache.wicket.ajax.AjaxCallbackPage?0-1.IBehaviorListener.0-\"};\n" //
				+ "var params = [{\"name\":\"explicit\",\"value\":explicit},"
				+ "{\"name\":\"resolved\",\"value\":window.location.href},"
				+ "{\"name\":\"converted\",\"value\":converted.substring(0, 3)}];\n" //
				+ "attrs.ep = params.concat(attrs.ep);\n" //
				+ "Wicket.Ajax.ajax(attrs);\n" //
				+ "}\n", //
			page.getBehavior1()
				.getCallbackFunction(context("context"), explicit("explicit"),
					resolved("resolved", "window.location.href"),
					converted("converted", "converted.substring(0, 3)"))
				.toString());

		assertEquals(
			//
			"function (context,explicit,converted) {\n" //
				+ "var attrs = {\"u\":\"./wicket/bookmarkable/org.apache.wicket.ajax.AjaxCallbackPage?0-1.IBehaviorListener.1-\"," //
				+ "\"ep\":[{\"name\":\"param1\",\"value\":123},{\"name\":\"param2\",\"value\":\"zh_CN\"}]};\n" //
				+ "var params = [{\"name\":\"explicit\",\"value\":explicit},"
				+ "{\"name\":\"resolved\",\"value\":window.location.href},"
				+ "{\"name\":\"converted\",\"value\":converted.substring(0, 3)}];\n" //
				+ "attrs.ep = params.concat(attrs.ep);\n" //
				+ "Wicket.Ajax.ajax(attrs);\n" //
				+ "}\n", //
			page.getBehavior2()
				.getCallbackFunction(context("context"), explicit("explicit"),
					resolved("resolved", "window.location.href"),
					converted("converted", "converted.substring(0, 3)"))
				.toString());
	}

	@Test
	public void testJQueryUIEvent()
	{
		AjaxCallbackPage page = tester.startPage(AjaxCallbackPage.class);
		assertEquals(
			//
			"function (event,ui) {\n" //
				+ "var attrs = {\"u\":\"./wicket/bookmarkable/org.apache.wicket.ajax.AjaxCallbackPage?0-1.IBehaviorListener.0-\"};\n" //
				+ "var params = [{\"name\":\"sortIndex\",\"value\":$(this).find(':data(sortable-item)').index(ui.item)}," //
				+ "{\"name\":\"sortItemId\",\"value\":$(ui.item).attr('id')}," //
				+ "{\"name\":\"sortSenderId\",\"value\":$(ui.sender).attr('id')}];\n" //
				+ "attrs.ep = params.concat(attrs.ep);\n" //
				+ "Wicket.Ajax.ajax(attrs);\n" //
				+ "}\n", //
			page.getBehavior1()
				.getCallbackFunction(context("event"), context("ui"),
					resolved("sortIndex", "$(this).find(':data(sortable-item)').index(ui.item)"),
					resolved("sortItemId", "$(ui.item).attr('id')"),
					resolved("sortSenderId", "$(ui.sender).attr('id')"))
				.toString());

		assertEquals(
			//
			"function (event,ui) {\n" //
				+ "var attrs = {\"u\":\"./wicket/bookmarkable/org.apache.wicket.ajax.AjaxCallbackPage?0-1.IBehaviorListener.1-\"," //
					+ "\"ep\":[{\"name\":\"param1\",\"value\":123},{\"name\":\"param2\",\"value\":\"zh_CN\"}]};\n" //
				+ "var params = [{\"name\":\"sortIndex\",\"value\":$(this).find(':data(sortable-item)').index(ui.item)}," //
				+ "{\"name\":\"sortItemId\",\"value\":$(ui.item).attr('id')}," //
				+ "{\"name\":\"sortSenderId\",\"value\":$(ui.sender).attr('id')}];\n" //
				+ "attrs.ep = params.concat(attrs.ep);\n" //
				+ "Wicket.Ajax.ajax(attrs);\n" //
				+ "}\n", //
			page.getBehavior2()
				.getCallbackFunction(context("event"), context("ui"),
					resolved("sortIndex", "$(this).find(':data(sortable-item)').index(ui.item)"),
					resolved("sortItemId", "$(ui.item).attr('id')"),
					resolved("sortSenderId", "$(ui.sender).attr('id')"))
				.toString());
	}
}
