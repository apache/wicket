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

import java.lang.reflect.Field;

// ----------------------------------------------------------------------------

/**
 * An {@link ILink}implementation for tree links created by class instance fields.
 * @author (C) <a href="http://www.javaworld.com/columns/jw-qna-index.shtml">Vlad Roubtsov
 *         </a>, 2003
 */
final class ClassFieldLink implements ILink
{
    // private: ...............................................................
    private final Field m_field;

    // protected: .............................................................
    // package: ...............................................................
    ClassFieldLink(final Field field)
    {
        m_field = field;
    }

    /**
     * @see wicket.util.profile.ILink#name()
     */
    public String name()
    {
        return ObjectProfiler.fieldName(m_field, ObjectProfiler.SHORT_TYPE_NAMES);
    }
} // end of class
// ----------------------------------------------------------------------------
