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
 * Interface that gets a page. This is used to defer the creation of pages which are
 * linked to.
 * @author Jonathan Locke
 */
public interface IPageLink extends Serializable
{
    /**
     * @return The page linked to
     */
    public Page getPage();

    /**
     * @return The class of page linked to
     */
    public Class getPageClass();
}

///////////////////////////////// End of File /////////////////////////////////
