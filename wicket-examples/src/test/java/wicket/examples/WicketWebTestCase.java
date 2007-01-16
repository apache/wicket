/*
 * $Id$
 * $Revision$ $Date$
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
package wicket.examples;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.sourceforge.jwebunit.WebTestCase;
import nl.openedge.util.jetty.JettyDecorator;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;

import com.meterware.httpunit.HttpUnitOptions;

/**
 * Add XPATH based validation
 * 
 * @author Juergen Donnerstag
 */
public abstract class WicketWebTestCase extends WebTestCase
{
	/**
	 * Suite method.
	 * 
	 * @param clazz
	 * @return Test suite
	 */
	public static Test suite(Class clazz)
	{
		// The javascript 'history' variable is not supported by
		// httpunit and we don't want httpunit to throw an
		// exception just because they can not handle it.
		HttpUnitOptions.setExceptionsThrownOnScriptError(false);

		TestSuite suite = new TestSuite();
		suite.addTestSuite(clazz);
		JettyDecorator deco = new JettyDecorator(suite);
		deco.setPort(8098);
		deco.setWebappContextRoot("src/main/webapp");
		deco.setContextPath("/wicket-examples");

		return deco;
	}

	/**
	 * Constructor
	 * 
	 * @param name
	 */
	public WicketWebTestCase(String name)
	{
		super(name);
	}

	/**
	 * Constructor
	 */
	public WicketWebTestCase()
	{
		super();
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	public void setUp() throws Exception
	{
		getTestContext().setBaseUrl("http://localhost:8098/wicket-examples");
		getTestContext().setEncodingScheme("UTF-8");
	}

	/**
	 * Select a single node based on the xpath expression
	 * 
	 * @param xpath
	 * @return Node
	 * @throws Exception
	 */
	public Node selectSingleNode(final String xpath) throws Exception
	{
		final String resp = this.getDialog().getResponse().getText();
		final Document doc = DocumentHelper.parseText(resp);
		// String xml = doc.asXML();
		// System.out.print(xml);
		final Node node = doc.selectSingleNode(xpath);
		return node;
	}

	/**
	 * Assert the value returned by the xpath matches 'assertValue'
	 * 
	 * @param xpath
	 * @param assertValue
	 * @throws Exception
	 */
	public void assertXPath(final String xpath, final String assertValue) throws Exception
	{
		final Node node = selectSingleNode(xpath);
		assertNotNull("Node not found: " + xpath, node);
		final String value;
		if (node instanceof Attribute)
		{
			value = ((Attribute)node).getValue();
		}
		else
		{
			value = node.getText();
		}

		assertTrue("xpath found, but values don't match: '" + value + "' != '" + assertValue + "'",
				value.matches(".*" + assertValue + ".*"));
	}

	/**
	 * Assert the tag body of the tag identified by wicket:id="wicketId" matches
	 * 'assertValue'
	 * 
	 * @param wicketId
	 * @param assertValue
	 * @throws Exception
	 */
	public void assertWicketIdTagText(final String wicketId, final String assertValue)
			throws Exception
	{
		assertXPath("//*[@wicket:id='" + wicketId + "']", assertValue);
	}

	/**
	 * Assert no node matching the xpath exists
	 * 
	 * @param xpath
	 * @throws Exception
	 */
	public void assertXpathNodeNotPresent(final String xpath) throws Exception
	{
		final Node node = selectSingleNode(xpath);
		assertNull(node);
	}

	/**
	 * Assert the node matching the xpath exists
	 * 
	 * @param xpath
	 * @return Node
	 * @throws Exception
	 */
	public Node assertXpathNodePresent(final String xpath) throws Exception
	{
		final Node node = selectSingleNode(xpath);
		assertNotNull(node);
		return node;
	}
}
