/*
 * $Id: AjaxLinkTest.java 5167 2006-03-29 19:12:08 +0000 (Wed, 29 Mar 2006)
 * jdonnerstag $ $Revision$ $Date: 2006-03-29 19:12:08 +0000 (Wed, 29 Mar
 * 2006) $
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.ajax.markup.html.ajaxLink;

import wicket.Component;
import wicket.Page;
import wicket.WicketTestCase;
import wicket.behavior.AbstractAjaxBehavior;
import wicket.util.tester.TagTester;

/**
 * 
 */
public class AjaxLinkTest extends WicketTestCase
{
	/**
	 * Construct.
	 * 
	 * @param name
	 */
	public AjaxLinkTest(String name)
	{
		super(name);
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void testRenderHomePage_1() throws Exception
	{
		executeTest(AjaxLinkPage.class, "AjaxLinkPageExpectedResult.html");
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void testRenderHomePage_2() throws Exception
	{
		executeTest(AjaxLinkWithBorderPage.class, "AjaxLinkWithBorderPageExpectedResult.html");

		Page page = application.getLastRenderedPage();
		Component ajaxLink = page.get("border:borderBody:ajaxLink");
		AbstractAjaxBehavior behavior = (AbstractAjaxBehavior)ajaxLink.getBehaviors().get(0);

		executedBehavior(AjaxPage2.class, behavior, "AjaxLinkWithBorderPage-1ExpectedResult.html");
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void testPage_2() throws Exception
	{
		executeTest(AjaxPage2.class, "AjaxPage2_ExpectedResult.html");

		Page page = application.getLastRenderedPage();
		Component ajaxLink = page.get("pageLayout:pageLayoutBody:ajaxLink");
		AbstractAjaxBehavior behavior = (AbstractAjaxBehavior)ajaxLink.getBehaviors().get(0);

		executedBehavior(AjaxPage2.class, behavior, "AjaxPage2-1_ExpectedResult.html");
	}
	
	/**
	 * Test that the onclick on ajax link has "return !wcall;" at the end. This
	 * ensures that execution is not turned over to the href attribute, which
	 * would then append # to the url.
	 */
	public void testJavascriptEndsWithReturn()
	{
		application.startPage(AjaxLinkPage.class);

		TagTester ajaxLink = application.getTagByWicketId("ajaxLink");

		assertTrue(ajaxLink.getAttributeEndsWith("onclick", "return !wcall;"));
	}

	/**
	 * If the AjaxLink is attached to an "a" tag the href value should be
	 * replaced with "#" because we use the onclick to execute the javascript.
	 */
	public void testAnchorGetsHrefReplaced()
	{
		application.startPage(AjaxLinkPage.class);

		TagTester ajaxLink = application.getTagByWicketId("ajaxLink");

		// It was a link to google in the markup, but should be replaced to "#"
		assertTrue(ajaxLink.getAttributeIs("href", "#"));
	}
}
