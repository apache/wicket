/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket;

import java.io.Serializable;

/**
 * Base interface for all interfaces that listen for requests from
 * the client browser.  All sub-interfaces of this interface must have
 * a single method which takes no arguments.  New listener interfaces
 * must be registered by calling {@link RequestCycle#registerRequestListenerInterface(Class)}.
 * 
 * @author Jonathan Locke
 */
public interface IRequestListener extends Serializable
{
}


