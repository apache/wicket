/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket;


/**
 * Listens for requests to behaviours. When {@link wicket.IBehaviour}s are
 * 'enriched' with this interface, they can receive requests themselves. You can
 * use this for example to implement AJAX behaviour, though you'll probably want
 * to extend from {@link wicket.AjaxHandler} directly instead in that case.
 * 
 * @author Eelco Hillenius
 */
public interface IBehaviourListener extends IRequestListener
{
	/**
	 * Called when a request to a behaviour is received.
	 */
	void onRequest();
}
