/*
 * $Id: IInterface.java 3480 2005-12-23 23:00:29 +0000 (Fri, 23 Dec 2005) ivaynberg $
 * $Revision: 3480 $
 * $Date: 2005-12-23 23:00:29 +0000 (Fri, 23 Dec 2005) $
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
package wicket.extensions.proxy.util;

/**
 * Interface for mock dependency
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public interface IInterface
{
	/**
	 * @return message
	 */
	String getMessage();
}
