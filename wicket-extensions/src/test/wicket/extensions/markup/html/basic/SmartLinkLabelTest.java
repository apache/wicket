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
package wicket.extensions.markup.html.basic;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author Juergen Donnerstag
 */
public class SmartLinkLabelTest extends TestCase
{

    /**
     *
     */
    public void test1()
    {
        Assert.assertNull(SmartLinkLabel.smartLink(null));
        Assert.assertEquals("", SmartLinkLabel.smartLink(""));
        Assert.assertEquals("test", SmartLinkLabel.smartLink("test"));
        
        Assert.assertEquals("<a href=\"mailto:test@email.com\">test@email.com</a>", SmartLinkLabel.smartLink("test@email.com"));
        Assert.assertEquals("text (<a href=\"mailto:test@email.com\">test@email.com</a>) text", SmartLinkLabel.smartLink("text (test@email.com) text"));
        Assert.assertEquals("text <a href=\"mailto:test@email.com\">test@email.com</a> text", SmartLinkLabel.smartLink("text test@email.com text"));

        Assert.assertEquals("<a href=\"http://www.test.com\">http://www.test.com</a>", SmartLinkLabel.smartLink("http://www.test.com"));
        Assert.assertEquals("text (<a href=\"http://www.test.com\">http://www.test.com</a>) text", SmartLinkLabel.smartLink("text (http://www.test.com) text"));
        Assert.assertEquals("text <a href=\"http://www.test.com\">http://www.test.com</a> text", SmartLinkLabel.smartLink("text http://www.test.com text"));
        Assert.assertEquals("text <a href=\"http://www.test.com:8080\">http://www.test.com:8080</a> text", SmartLinkLabel.smartLink("text http://www.test.com:8080 text"));
        Assert.assertEquals("text <a href=\"http://www.test.com/test/murx.jsp\">http://www.test.com/test/murx.jsp</a> text", SmartLinkLabel.smartLink("text http://www.test.com/test/murx.jsp text"));
        Assert.assertEquals("text <a href=\"http://www.test.com/test/murx.jsp?query=test&q2=murx\">http://www.test.com/test/murx.jsp</a> text", SmartLinkLabel.smartLink("text http://www.test.com/test/murx.jsp?query=test&q2=murx text"));
        
        Assert.assertEquals(
                "line 1 <a href=\"http://www.test.com/test/murx.jsp\">http://www.test.com/test/murx.jsp</a> \nline2 <a href=\"mailto:murx@email.de\">murx@email.de</a> \r\nline3", 
                SmartLinkLabel.smartLink("line 1 http://www.test.com/test/murx.jsp \nline2 murx@email.de \r\nline3"));
    }
}
