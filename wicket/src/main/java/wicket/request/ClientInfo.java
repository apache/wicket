/*
 * $Id: ClientInfo.java 3317 2005-12-02 05:27:14 +0000 (Fri, 02 Dec 2005)
 * eelco12 $ $Revision$ $Date: 2005-12-02 05:27:14 +0000 (Fri, 02 Dec
 * 2005) $
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
package wicket.request;

import java.io.Serializable;

/**
 * Encapsulates information about the request cycle agents' capabilities.
 * 
 * @author Eelco Hillenius
 */
public abstract class ClientInfo implements Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 */
	public ClientInfo()
	{
	}
}
