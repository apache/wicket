/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.examples;

import java.util.List;

import net.sourceforge.jwebunit.WebTestCase;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Node;

/**
 * Add XPATH based validation
 * 
 * @author Juergen Donnerstag
 */
public class WicketWebTestCase extends WebTestCase
{
    /**
     * 
     * @param name
     */
    public WicketWebTestCase(String name)
    {
        super(name);
    }

    /**
     * 
     *
     */
    public WicketWebTestCase()
    {
        super();
    }

    /**
     * 
     * @param xpath
     * @return Node
     * @throws Exception
     */
    public Node selectSingleNode(final String xpath) throws Exception
    {
        final org.w3c.dom.Document w3cDoc = this.getDialog().getResponse().getDOM();
        final Document doc = new org.dom4j.io.DOMReader().read(w3cDoc);
        final String xml = doc.asXML();
        final List list = doc.selectNodes(xpath);
        final Node node = doc.selectSingleNode(xpath);
        return node;
    }
    
    /**
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
        
        assertTrue(value.matches(".*" + assertValue + ".*"));
    }

    /**
     * @param wicketId
     * @param assertValue
     * @throws Exception
     */
    public void assertWicketIdTagText(final String wicketId, final String assertValue) throws Exception
    {
        assertXPath("//*[@wicket:id='" + wicketId + "']", assertValue);
    }
    
    /**
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
