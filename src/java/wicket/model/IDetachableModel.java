/*
 * $Id$ $Revision:
 * 1.3 $ $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.model;


/**
 * A IDetachableModel wraps the actual model objects of components and provides
 * a call back mechanism for reacting on the starting/ending of a request.
 * Please use the abstract class {@link wicket.model.DetachableModel}for
 * implementations instead of implementing this interface directely.
 * 
 * @author Chris Turner
 * @author Eelco Hillenius
 */
public interface IDetachableModel extends IModel
{
    /**
     * Detaches from a session
     */
    public void detach();

    /**
     * Attaches to a session
     */
    public void attach();
}