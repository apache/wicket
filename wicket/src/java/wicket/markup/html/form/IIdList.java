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
package wicket.markup.html.form;

import java.io.Serializable;
import java.util.List;

/**
 * A List interface that is used by the DropDown or ListBox implementation to get a
 * ID/Value list that can also be detach/attached.
 *
 * @author Johan Compagner
 * @author Eelco Hillenius
 */
public interface IIdList extends List, Serializable
{
    /**
     * Detach from a request.
     * @param cycle the request cycle
     */
    public void detach();

    /**
     * Attach to a request.
     * @param cycle the request cycle
     */
    public void attach();

    /**
     * Gets the display value.
     * @param row the row number
     * @return the display value
     */
    public String getDisplayValue(int row);

    /**
     * Gets the id value.
     * @param row the row number
     * @return the id value
     */
    public String getIdValue(int row);

    /**
     * Gets an object using the given id.
     * @param id the object's id
     * @return the object
     */
    public Object getObjectById(String id);
}
