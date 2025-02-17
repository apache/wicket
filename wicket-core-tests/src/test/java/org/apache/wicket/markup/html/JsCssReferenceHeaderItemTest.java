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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.text.ParseException;

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
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
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
	
	private static String jsResourceHash = "sha384-jsResourceHash2856816aw771";
	private static String jsReferenceHash = "sha384-jssReferenceHash28546qt8725171";
	private static String cssResourceHash = "sha384-cssResourceHash2512ab6wts23";
	private static String cssReferenceHash = "sha384-cssReferenceHash2awet512asd623";
	
	/**
	 * Basic ResourceReference integrity hash check
	 * 
	 * @throws IOException
	 * @throws ResourceStreamNotFoundException
	 * @throws ParseException
	 */
	@Test
	void resourceReferenceTest_basic() throws IOException, ResourceStreamNotFoundException,
		ParseException
	{
		tester.startPage(TestPage.class);
		XmlPullParser parser = new XmlPullParser();
		parser.parse(tester.getLastResponseAsString());
		XmlTag tag = parser.nextTag();
		boolean hasIntegrity = false;
		boolean hasCrossOrigin = false;
		do
		{
			if (tag.isOpen() && "script".equals(tag.getName()) && tag.getAttribute("id").equals("jsref"))
			{
				System.out.println(" SCRIPT TAG: " + tag.toDebugString() + "\nExpect js resource hash: " + jsResourceHash
						+ "\nExpect crossOrigin: " + CrossOrigin.USE_CREDENTIALS.getRealName());
				CharSequence seq = tag.getAttribute("integrity");
				if (seq != null)
				{
					hasIntegrity = seq.toString().equals(jsResourceHash);
				}
				seq = tag.getAttribute("crossOrigin");
				if (seq != null)
				{
					hasCrossOrigin = seq.toString().equals(CrossOrigin.USE_CREDENTIALS.getRealName());
				}
				break;
			}
		}
		while ((tag = parser.nextTag()) != null);
		assertTrue(hasIntegrity);
		assertTrue(hasCrossOrigin);
	}
	

	/**
	 * Basic JavaScriptReferenceHeaderItem integrity check - should override any integrity at resource level
	 * 
	 * @throws IOException
	 * @throws ResourceStreamNotFoundException
	 * @throws ParseException
	 */
	@Test
	void javaScriptReferenceIntegrityTest_override() throws IOException, ResourceStreamNotFoundException,
		ParseException
	{
		tester.startPage(TestPage.class);
		XmlPullParser parser = new XmlPullParser();
		parser.parse(tester.getLastResponseAsString());
		XmlTag tag = parser.nextTag();
		boolean hasIntegrity = false;
		boolean hasCrossOrigin = false;
		do
		{
			if (tag.isOpen() && "script".equals(tag.getName()) && tag.getAttribute("id").equals("jsref2"))
			{
				System.out.println(" SCRIPT TAG: " + tag.toDebugString() + "\nExpect js reference hash: " + jsReferenceHash
						+ "\nExpect crossOrigin: " + CrossOrigin.ANONYMOUS.getRealName());
				CharSequence seq = tag.getAttribute("integrity");
				if (seq != null)
				{
					hasIntegrity = seq.toString().equals(jsReferenceHash);
				}
				seq = tag.getAttribute("crossOrigin");
				if (seq != null)
				{
					hasCrossOrigin = seq.toString().equals(CrossOrigin.ANONYMOUS.getRealName());
				}
				break;
			}
		}
		while ((tag = parser.nextTag()) != null);
		assertTrue(hasIntegrity);
		assertTrue(hasCrossOrigin);
	}
	
	
	
		
	@Test
	void cssReferenceIntegrityTest_basic() throws IOException, ResourceStreamNotFoundException, ParseException
	{
		tester.startPage(TestPage.class);
		XmlPullParser parser = new XmlPullParser();
		parser.parse(tester.getLastResponseAsString());
		XmlTag tag = parser.nextTag();
		boolean hasIntegrity = false;
		boolean hasCrossOrigin = false;
		do
		{
			if ("link".equals(tag.getName()) && tag.getAttribute("id").equals("cssref"))
			{
				System.out.println(" LINK TAG: " + tag.toDebugString() + "\nExpect resource hash: " + cssResourceHash
					+ "\nExpect crossOrigin: " + CrossOrigin.ANONYMOUS.getRealName());
				CharSequence seq = tag.getAttribute("integrity");
				if (seq != null)
				{
					hasIntegrity = seq.toString().equals(cssResourceHash);
				}
				seq = tag.getAttribute("crossOrigin");
				if (seq != null)
				{
					hasCrossOrigin = seq.toString().equals(CrossOrigin.ANONYMOUS.getRealName());
				}
				break;
			}
		}
		while ((tag = parser.nextTag()) != null);
		assertTrue(hasIntegrity);
		assertTrue(hasCrossOrigin);
	}

	
	@Test
	void cssReferenceIntegrityTest_override() throws IOException, ResourceStreamNotFoundException, ParseException
	{
		tester.startPage(TestPage.class);
		XmlPullParser parser = new XmlPullParser();
		parser.parse(tester.getLastResponseAsString());
		XmlTag tag = parser.nextTag();
		boolean hasIntegrity = false;
		boolean hasCrossOrigin = false;
		do
		{
			if ("link".equals(tag.getName()) && tag.getAttribute("id").equals("cssref2"))
			{
				System.out.println(" LINK TAG: " + tag.toDebugString() + "\nExpect reference hash: " + cssReferenceHash
					+ "\nExpect crossOrigin: " + CrossOrigin.USE_CREDENTIALS.getRealName());
				CharSequence seq = tag.getAttribute("integrity");
				if (seq != null)
				{
					hasIntegrity = seq.toString().equals(cssReferenceHash);
				}
				seq = tag.getAttribute("crossOrigin");
				if (seq != null)
				{
					hasCrossOrigin = seq.toString().equals(CrossOrigin.USE_CREDENTIALS.getRealName());
				}
				break;
			}
		}
		while ((tag = parser.nextTag()) != null);
		assertTrue(hasIntegrity);
		assertTrue(hasCrossOrigin);
	}	

	/**
	 * 
	 */
	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void renderHead(IHeaderResponse response)
		{
			super.renderHead(response);

			// JS Reference - Basic
			PackageResourceReference jsRef = new PackageResourceReference("jsres");
			jsRef.setIntegrity(jsResourceHash);
			jsRef.setCrossOrigin(CrossOrigin.USE_CREDENTIALS);
			JavaScriptReferenceHeaderItem jsHeaderItem = JavaScriptHeaderItem.forReference(jsRef, "jsref");
			response.render(jsHeaderItem);
			
			// JS Reference - Override
			jsRef = new PackageResourceReference("jsres2");
			jsRef.setIntegrity(jsResourceHash);
			jsRef.setCrossOrigin(CrossOrigin.USE_CREDENTIALS);
			jsHeaderItem = JavaScriptHeaderItem.forReference(jsRef, "jsref2");
			jsHeaderItem.setIntegrity(jsReferenceHash);
			jsHeaderItem.setCrossOrigin(CrossOrigin.ANONYMOUS);
			response.render(jsHeaderItem);

			// CSS Reference - Basic
			PackageResourceReference cssRef = new PackageResourceReference("cssres");
			cssRef.setIntegrity(cssResourceHash);
			cssRef.setCrossOrigin(CrossOrigin.ANONYMOUS);
			CssReferenceHeaderItem cssHeaderItem = CssHeaderItem.forReference(cssRef);
			cssHeaderItem.setId("cssref");
			response.render(cssHeaderItem);

			// CSS References - Override
			cssRef = new PackageResourceReference("cssres2");
			cssRef.setIntegrity(cssResourceHash);
			cssRef.setCrossOrigin(CrossOrigin.ANONYMOUS);
			cssHeaderItem = CssHeaderItem.forReference(cssRef);
			cssHeaderItem.setId("cssref2");
			cssHeaderItem.setIntegrity(cssReferenceHash);
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
