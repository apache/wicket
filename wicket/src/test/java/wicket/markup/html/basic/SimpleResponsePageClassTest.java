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
package wicket.markup.html.basic;

import junit.framework.TestCase;
import wicket.markup.html.form.Form;
import wicket.protocol.http.MockHttpServletRequest;
import wicket.protocol.http.MockWebApplication;

/**
 * @author jcompagner
 *
 */
public class SimpleResponsePageClassTest extends TestCase
{
	/**
	 * @throws Exception
	 */
	public void testResponsePageClass() throws Exception 
	{
		MockWebApplication mockWebApp = new MockWebApplication(null);
		
        mockWebApp.setHomePage(SimpleResponsePageClass.class);
        mockWebApp.setupRequestAndResponse();
        mockWebApp.processRequestCycle();
        SimpleResponsePageClass manageBook = (SimpleResponsePageClass) mockWebApp.getLastRenderedPage();

        Form form = (Form) manageBook.get("form");
        mockWebApp.setupRequestAndResponse();

        MockHttpServletRequest mockRequest = mockWebApp.getServletRequest();
        mockRequest.setRequestToComponent(form);
        mockWebApp.processRequestCycle();      
       
        //assertion failed,  getLastRenderedPage() return null.
        assertTrue( mockWebApp.getLastRenderedPage() instanceof SimplePage);

    }
}
