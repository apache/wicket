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
 * A shell pseudo-node implementation for a non-array class.
 * @author (C) <a href="http://www.javaworld.com/columns/jw-qna-index.shtml">Vlad Roubtsov
 *         </a>, 2003
 */
final class ObjectShellProfileNode extends AbstractShellProfileNode
{ // TODO finalize javadoc
    // private: ...............................................................
    private final int m_primitiveFieldCount;

    // private: ...............................................................
    private final int m_refFieldCount;

    // protected: .............................................................
    // package: ...............................................................
    ObjectShellProfileNode(final IObjectProfileNode parent, final int primitiveFieldCount,
            final int refFieldCount)
    {
        super(parent);

        m_primitiveFieldCount = primitiveFieldCount;
        m_refFieldCount = refFieldCount;
    }

    /**
     * @see wicket.util.profile.IObjectProfileNode#name()
     */
    public String name()
    {
        return "<shell: " + m_primitiveFieldCount + " prim/" + m_refFieldCount + " ref fields>";
    }
} // end of class
// ----------------------------------------------------------------------------
