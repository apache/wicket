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
package org.apache.wicket.markup.html;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.Serial;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.parser.XmlPullParser;
import org.apache.wicket.markup.parser.XmlTag;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michael Pritt
 */
class JsCssReferenceHeaderItemTest extends WicketTestCase
{
	private static final Logger log = LoggerFactory.getLogger(JsCssReferenceHeaderItemTest.class);
	
	private static final String JS_RESOURCE_HASH = "sha384-jsResourceHash2856816aw771";
	private static final String JS_REFERENCE_HASH = "sha384-jssReferenceHash28546qt8725171";
	private static final String CSS_RESOURCE_HASH = "sha384-cssResourceHash2512ab6wts23";
	private static final String CSS_REFERENCE_HASH = "sha384-cssReferenceHash2awet512asd623";
	
	/**
	 * Basic ResourceReference integrity hash check
	 */
	@Test
	void resourceReferenceTest_basic() throws Exception
	{
		tester.startPage(TestPage.class);
		XmlPullParser parser = new XmlPullParser();
		parser.parse(tester.getLastResponseAsString());
		XmlTag tag = parser.nextTag();
		CharSequence integrity = null;
		CharSequence crossOrigin = null;
		do
		{
			if (tag != null && tag.isOpen() && "script".equals(tag.getName()) && "jsref".contentEquals(tag.getAttribute("id")))
			{
				log.info(" SCRIPT TAG: " + tag.toDebugString() + "\nExpect js resource hash: " + JS_RESOURCE_HASH
						+ "\nExpect crossOrigin: " + CrossOrigin.USE_CREDENTIALS.getRealName());
				integrity = tag.getAttribute("integrity");
				crossOrigin = tag.getAttribute("crossOrigin");
				break;
			}
		}
		while ((tag = parser.nextTag()) != null);
		assertEquals(JS_RESOURCE_HASH, integrity);
		assertEquals(CrossOrigin.USE_CREDENTIALS.getRealName(), crossOrigin);
	}
	

	/**
	 * Basic JavaScriptReferenceHeaderItem integrity check - should override any integrity at resource level
	 */
	@Test
	void javaScriptReferenceIntegrityTest_override() throws Exception
	{
		tester.startPage(TestPage.class);
		XmlPullParser parser = new XmlPullParser();
		parser.parse(tester.getLastResponseAsString());
		XmlTag tag = parser.nextTag();
		CharSequence integrity = null;
		CharSequence crossOrigin = null;
		do
		{
			if (tag != null && tag.isOpen() && "script".equals(tag.getName()) && "jsref2".contentEquals(tag.getAttribute("id")))
			{
				log.info(" SCRIPT TAG: " + tag.toDebugString() + "\nExpect js reference hash: " + JS_REFERENCE_HASH
						+ "\nExpect crossOrigin: " + CrossOrigin.ANONYMOUS.getRealName());
				integrity = tag.getAttribute("integrity");
				crossOrigin = tag.getAttribute("crossOrigin");
				break;
			}
		}
		while ((tag = parser.nextTag()) != null);
		assertEquals(JS_REFERENCE_HASH, integrity);
		assertEquals(CrossOrigin.ANONYMOUS.getRealName(), crossOrigin);
	}
		
	@Test
	void cssReferenceIntegrityTest_basic() throws Exception
	{
		tester.startPage(TestPage.class);
		XmlPullParser parser = new XmlPullParser();
		parser.parse(tester.getLastResponseAsString());
		XmlTag tag = parser.nextTag();
		CharSequence integrity = null;
		CharSequence crossOrigin = null;
		do
		{
			if (tag != null && "link".equals(tag.getName()) && tag.getAttribute("id").equals("cssref"))
			{
				log.debug(" LINK TAG: " + tag.toDebugString() + "\nExpect resource hash: " + CSS_RESOURCE_HASH
					+ "\nExpect crossOrigin: " + CrossOrigin.ANONYMOUS.getRealName());
				integrity = tag.getAttribute("integrity");
				crossOrigin = tag.getAttribute("crossOrigin");
				break;
			}
		}
		while ((tag = parser.nextTag()) != null);
		assertEquals(CSS_RESOURCE_HASH, integrity);
		assertEquals(CrossOrigin.ANONYMOUS.getRealName(), crossOrigin);
	}

	
	@Test
	void cssReferenceIntegrityTest_override() throws Exception
	{
		tester.startPage(TestPage.class);
		XmlPullParser parser = new XmlPullParser();
		parser.parse(tester.getLastResponseAsString());
		XmlTag tag = parser.nextTag();
		CharSequence integrity = null;
		CharSequence crossOrigin = null;
		do
		{
			if (tag != null && "link".equals(tag.getName()) && tag.getAttribute("id").equals("cssref2"))
			{
				log.debug(" LINK TAG: " + tag.toDebugString() + "\nExpect reference hash: " + CSS_REFERENCE_HASH
					+ "\nExpect crossOrigin: " + CrossOrigin.USE_CREDENTIALS.getRealName());
				integrity = tag.getAttribute("integrity");
				crossOrigin = tag.getAttribute("crossOrigin");
				break;
			}
		}
		while ((tag = parser.nextTag()) != null);
		assertEquals(CSS_REFERENCE_HASH, integrity);
		assertEquals(CrossOrigin.USE_CREDENTIALS.getRealName(), crossOrigin);
	}	

	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		@Serial
		private static final long serialVersionUID = 1L;

		@Override
		public void renderHead(IHeaderResponse response)
		{
			super.renderHead(response);

			// JS Reference - Basic
			PackageResourceReference jsRef = new PackageResourceReference("jsres");
			jsRef.setIntegrity(JS_RESOURCE_HASH);
			jsRef.setCrossOrigin(CrossOrigin.USE_CREDENTIALS);
			JavaScriptReferenceHeaderItem jsHeaderItem = JavaScriptHeaderItem.forReference(jsRef, "jsref");
			response.render(jsHeaderItem);
			
			// JS Reference - Override
			jsRef = new PackageResourceReference("jsres2");
			jsRef.setIntegrity(JS_RESOURCE_HASH);
			jsRef.setCrossOrigin(CrossOrigin.USE_CREDENTIALS);
			jsHeaderItem = JavaScriptHeaderItem.forReference(jsRef, "jsref2");
			jsHeaderItem.setIntegrity(JS_REFERENCE_HASH);
			jsHeaderItem.setCrossOrigin(CrossOrigin.ANONYMOUS);
			response.render(jsHeaderItem);

			// CSS Reference - Basic
			PackageResourceReference cssRef = new PackageResourceReference("cssres");
			cssRef.setIntegrity(CSS_RESOURCE_HASH);
			cssRef.setCrossOrigin(CrossOrigin.ANONYMOUS);
			CssReferenceHeaderItem cssHeaderItem = CssHeaderItem.forReference(cssRef);
			cssHeaderItem.setId("cssref");
			response.render(cssHeaderItem);

			// CSS References - Override
			cssRef = new PackageResourceReference("cssres2");
			cssRef.setIntegrity(CSS_RESOURCE_HASH);
			cssRef.setCrossOrigin(CrossOrigin.ANONYMOUS);
			cssHeaderItem = CssHeaderItem.forReference(cssRef);
			cssHeaderItem.setId("cssref2");
			cssHeaderItem.setIntegrity(CSS_REFERENCE_HASH);
			cssHeaderItem.setCrossOrigin(CrossOrigin.USE_CREDENTIALS);
			response.render(cssHeaderItem);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><body></body></html>");
		}
	}
}
