///////////////////////////////////////////////////////////////////////////////////
//
// Created Jul 11, 2004
//
// Copyright 2004, Jonathan W. Locke
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package library;

import com.voicetribe.wicket.PageParameters;
import com.voicetribe.wicket.protocol.http.HttpApplication;
import com.voicetribe.wicket.protocol.http.HttpPageTester;
import com.voicetribe.wicket.protocol.http.HttpSession;

import junit.framework.TestCase;

/**
 * Test pages by rendering them to console.
 * @author Jonathan Locke
 */
public class LibraryTests extends TestCase
{    
    public final void testBookListPage()
    {
        tester.test(new Home(PageParameters.NULL));
    }
    
    public final void testEditBookPage()
    {
        tester.test(new EditBook(Book.get(1)));
    }
    
    public final void testSignInPage()
    {
        tester.test(new SignIn(PageParameters.NULL));
    }

    public final void testBookDetailsPage()
    {
        tester.test(new BookDetails(Book.get(1)));
    }

    static final HttpPageTester tester;
    static 
    {
        final HttpApplication application = new LibraryApplication();
        final HttpSession session = new HttpSession(application, null) { };
        Authenticator.forSession(session).authenticate("jonathan", "password");
        tester = new HttpPageTester(session);
    }
}

///////////////////////////////// End of File /////////////////////////////////
