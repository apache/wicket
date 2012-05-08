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
				+ "var attrs = {\"u\":\"./wicket/page?0-1.IBehaviorListener.0-\"};\n" //
				+ "var params = {};\n" //
				+ "attrs.ep = params;\n" //
				+ "Wicket.Ajax.ajax(attrs);\n" //
				+ "}\n", //
			page.getBehavior().getCallbackFunction().toString());
	}

	@Test
	public void testCallbackFunctionWithContext()
	{
		AjaxCallbackPage page = tester.startPage(AjaxCallbackPage.class);
		assertEquals(//
			"function (context) {\n" //
				+ "var attrs = {\"u\":\"./wicket/page?0-1.IBehaviorListener.0-\"};\n" //
				+ "var params = {};\n" //
				+ "attrs.ep = params;\n" //
				+ "Wicket.Ajax.ajax(attrs);\n" //
				+ "}\n", //
			page.getBehavior().getCallbackFunction(context("context")).toString());
	}

	@Test
	public void testCallbackFunctionWithExplicit()
	{
		AjaxCallbackPage page = tester.startPage(AjaxCallbackPage.class);
		assertEquals(//
			"function (explicit) {\n" //
				+ "var attrs = {\"u\":\"./wicket/page?0-1.IBehaviorListener.0-\"};\n" //
				+ "var params = {'explicit': explicit};\n" //
				+ "attrs.ep = params;\n" //
				+ "Wicket.Ajax.ajax(attrs);\n" //
				+ "}\n", //
			page.getBehavior().getCallbackFunction(explicit("explicit")).toString());
	}

	@Test
	public void testCallbackFunctionWithResolved()
	{
		AjaxCallbackPage page = tester.startPage(AjaxCallbackPage.class);
		assertEquals(//
			"function () {\n" //
				+ "var attrs = {\"u\":\"./wicket/page?0-1.IBehaviorListener.0-\"};\n" //
				+ "var params = {'resolved': window.location.href};\n" //
				+ "attrs.ep = params;\n" //
				+ "Wicket.Ajax.ajax(attrs);\n" //
				+ "}\n", //
			page.getBehavior()
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
				+ "var attrs = {\"u\":\"./wicket/page?0-1.IBehaviorListener.0-\"};\n" //
				+ "var params = {'converted': converted.substring(0, 3)};\n" //
				+ "attrs.ep = params;\n" //
				+ "Wicket.Ajax.ajax(attrs);\n" //
				+ "}\n", //
			page.getBehavior()
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
				+ "var attrs = {\"u\":\"./wicket/page?0-1.IBehaviorListener.0-\"};\n" //
				+ "var params = {'explicit': explicit,'resolved': window.location.href,'converted': converted.substring(0, 3)};\n" //
				+ "attrs.ep = params;\n" //
				+ "Wicket.Ajax.ajax(attrs);\n" //
				+ "}\n", //
			page.getBehavior()
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
				+ "var attrs = {\"u\":\"./wicket/page?0-1.IBehaviorListener.0-\"};\n" //
				+ "var params = {'sortIndex': $(this).find(':data(sortable-item)').index(ui.item),'sortItemId': $(ui.item).attr('id'),'sortSenderId': $(ui.sender).attr('id')};\n" //
				+ "attrs.ep = params;\n" //
				+ "Wicket.Ajax.ajax(attrs);\n" //
				+ "}\n", //
			page.getBehavior()
				.getCallbackFunction(context("event"), context("ui"),
					resolved("sortIndex", "$(this).find(':data(sortable-item)').index(ui.item)"),
					resolved("sortItemId", "$(ui.item).attr('id')"),
					resolved("sortSenderId", "$(ui.sender).attr('id')"))
				.toString());
	}
}
