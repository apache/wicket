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
package wicket.protocol.http;

import java.io.IOException;

import junit.framework.TestCase;

/**
 * @author Juergen Donnerstag
 */
public class WebResponseCrawlerSaveTest extends TestCase
{
    /**
     * 
     * @throws IOException
     */
    public void testEncode() throws IOException
    {
        WebResponseCrawlerSave resp = new WebResponseCrawlerSave(null); 
        
        String url = resp.encodeURL("http://localhost:8080/library?bookmarkablePage=wicket.examples.library.BookDetails");
        assertEquals(url, "http://localhost:8080/library/wicket/examples/library/BookDetails.wic");
        
        url = resp.encodeURL("http://localhost:8080/myApp?bookmarkablePage=wicket.markup.html.Page&id=5");
        assertEquals(url, "http://localhost:8080/myApp/wicket/markup/html/Page.wic?id=5");
        
        /** Note that param order has changed due to HashMap key order */
        url = resp.encodeURL("http://localhost:8080/myApp?bookmarkablePage=wicket.markup.html.Page&id=5&test=abcd");
        assertEquals(url, "http://localhost:8080/myApp/wicket/markup/html/Page.wic?test=abcd&id=5");
    }
}
