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

import wicket.IListener;
import wicket.RequestCycle;

/**
 * Listener method for on change events of dropdown lists.
 * @author Eelco Hillenius
 */
public interface IOnChangeListener extends IListener
{
    /**
     * Called when a option is selected of a dropdown list that wants to be notified of
     * this event. This is the 'real' interface method that will be called by Wicket.
     * @param cycle The request cycle
     */
    void selectionChanged(final RequestCycle cycle);

    /**
     * Called when a option is selected of a dropdown list that wants to be notified of
     * this event. This method is to be implemented by clients that want to be notified of
     * selection events.
     * @param cycle The request cycle
     * @param newSelection the newly selected object of the backing model NOTE this is the
     *            same as you would get by calling getModelObject() is the new selection
     *            is current
     */
    void selectionChanged(final RequestCycle cycle, Object newSelection);
}

///////////////////////////////// End of File /////////////////////////////////
