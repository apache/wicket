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

import wicket.IRequestListener;

/**
 * Listener method for OnChange events of dropdown lists.
 * Users can optionally implement this interface with drop down components
 * to have an onchange javascript event handler rendered that will call
 * the implementor directly when an onchange event is issued.
 * 
 * @author Eelco Hillenius
 */
public interface IOnChangeListener extends IRequestListener
{
    /**
     * Called when a option is selected of a dropdown list that wants 
     * to be notified of this event. 
     */
    void selectionChanged();
}
