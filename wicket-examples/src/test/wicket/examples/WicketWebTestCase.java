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

import java.io.StringReader;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

import com.meterware.httpunit.HttpUnitUtils;

import net.sourceforge.jwebunit.WebTestCase;

/**
 * Add XPATH based validation
 * 
 * @author Juergen Donnerstag
 */
public class WicketWebTestCase extends WebTestCase
{
    private String responseText;
    private Document doc;
    
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
        if ((this.doc == null) || (this.responseText != this.getDialog().getResponseText()))
        {
	        this.responseText = this.getDialog().getResponseText();
	        org.w3c.dom.Document xdoc = HttpUnitUtils.newParser().parse(new InputSource(new StringReader(this.responseText)));
	        this.doc = new org.dom4j.io.DOMReader().read(xdoc);
/*
	        final SAXReader reader = new SAXReader();
	    	reader.setFeature("http://xml.org/sax/features/namespace-prefixes", true); 
	        // Convert the DOMDocument to a DOM4J-Document
	        this.doc = reader.read(new InputSource(new StringReader(responseText)));
*/
        }
        
        final Node node = this.doc.selectSingleNode(xpath);
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
        assertNotNull(node);
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
