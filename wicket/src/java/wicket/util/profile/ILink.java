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
package wicket.util.profile;

// ----------------------------------------------------------------------------

/**
 * Interface used internally for memory-efficient representations of names of profile tree
 * links between profile tree nodes.
 * @author (C) <a href="http://www.javaworld.com/columns/jw-qna-index.shtml">Vlad Roubtsov
 *         </a>, 2003
 */
interface ILink
{ // TODO finalize javadoc
    // public: ................................................................

    /**
     * Returns the string that will be used for a {@link IObjectProfileNode#name()}
     * implementation. It is expected that the implementation will generate the return on
     * every call to this method and not keep in memory.
     * @return the name of the link
     */
    String name();
} // end of interface
// ----------------------------------------------------------------------------
