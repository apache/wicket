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
package wicket.markup.html.link;

import java.io.Serializable;

import wicket.Page;

/**
 * Interface that is used to implement delayed page linking.  The 
 * getPage() method returns an instance of Page when a link is actually
 * clicked (thus avoiding the need to create a destination Page object 
 * for every link on a given Page in advance).  The getPageClass() 
 * method returns the subclass of Page that getPage() will return if 
 * and when it is  called.  
 * <p>
 * This way of arranging things is useful in determining whether a 
 * link links to a given page, which is in turn useful for deciding 
 * how to display the link (because links in a navigation which link to 
 * a page itself are not useful and generally should instead indicate 
 * where the user is in the navigation). 
 * 
 * @author Jonathan Locke
 */
public interface IPageLink extends Serializable
{
    /**
     * @return The page to go to
     */
    public Page getPage();

    /**
     * @return The class of page linked to
     */
    public Class getPageClass();
}


