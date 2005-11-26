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
package wicket.request.compound;

import wicket.Request;

/**
 * Implementations of this interface are responsible for digesting the incomming
 * request and create a suitable
 * {@link wicket.request.compound.RequestParameters} object for it.
 * 
 * @author hillenius
 */
public interface IRequestParametersFactory
{
	/**
	 * Analyze the request and create a corresponding request paramters object
	 * for it.
	 * 
	 * @param request
	 *            the incomming request
	 * @return a request parameters object that corresponds to the request
	 */
	RequestParameters newParameters(Request request);
}
