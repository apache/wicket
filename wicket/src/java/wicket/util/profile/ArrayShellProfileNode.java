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
 * A shell pseudo-node implementation for an array class.
 * @author (C) <a href="http://www.javaworld.com/columns/jw-qna-index.shtml">Vlad Roubtsov
 *         </a>, 2003
 */
final class ArrayShellProfileNode extends AbstractShellProfileNode
{ // TODO finalize javadoc
    // private: ...............................................................
    private final Class m_type;

    private final int m_length;

    // protected: .............................................................
    // package: ...............................................................
    ArrayShellProfileNode(final IObjectProfileNode parent, final Class type, final int length)
    {
        super(parent);

        m_type = type;
        m_length = length;
    }

    /**
     * @see wicket.util.profile.IObjectProfileNode#name()
     */
    public String name()
    {
        return "<shell: "
                + ObjectProfiler.typeName(m_type, ObjectProfiler.SHORT_TYPE_NAMES) + ", length="
                + m_length + ">";
    }
} // end of class
// ----------------------------------------------------------------------------
