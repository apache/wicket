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
 * An {@link ILink}implementation for tree links created by array fields.
 * @author (C) <a href="http://www.javaworld.com/columns/jw-qna-index.shtml">Vlad Roubtsov
 *         </a>, 2003
 */
final class ArrayIndexLink implements ILink
{ // TODO finalize javadoc
    // private: ...............................................................
    private final ILink m_container;

    private final int m_index;

    // protected: .............................................................
    // package: ...............................................................
    ArrayIndexLink(final ILink container, final int index)
    {
        m_container = container;
        m_index = index;
    }

    /**
     * @see wicket.util.profile.ILink#name()
     */
    public String name()
    {
        final StringBuffer s = new StringBuffer();

        ILink l = this;

        while (l instanceof ArrayIndexLink)
        {
            final ArrayIndexLink asl = (ArrayIndexLink) l;

            s.insert(0, ']');
            s.insert(0, asl.m_index);
            s.insert(0, '[');

            l = asl.m_container;
        }

        s.insert(0, (l != null) ? l.name() : ObjectProfiler.INPUT_OBJECT_NAME);

        return s.toString();
    }
} // end of class
// ----------------------------------------------------------------------------
