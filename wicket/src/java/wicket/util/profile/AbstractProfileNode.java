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

import java.io.PrintWriter;
import java.io.StringWriter;

import java.util.LinkedList;

// ----------------------------------------------------------------------------

/**
 * Abstract base class for all node implementations in this package.
 * @author (C) <a href="http://www.javaworld.com/columns/jw-qna-index.shtml">Vlad Roubtsov
 *         </a>, 2003
 */
abstract class AbstractProfileNode implements IObjectProfileNode, Comparable
{
    static final IObjectProfileNode[] EMPTY_OBJECTPROFILENODE_ARRAY = new IObjectProfileNode[0];

    int m_size;

    // private: ...............................................................
    private final IObjectProfileNode m_parent;

    private transient IObjectProfileNode[] m_path;

    // protected: .............................................................
    // package: ...............................................................
    AbstractProfileNode(final IObjectProfileNode parent)
    {
        m_parent = parent;
    }

    /**
     * @see wicket.util.profile.IObjectProfileNode#size()
     */
    public final int size()
    {
        return m_size;
    }

    /**
     * @see wicket.util.profile.IObjectProfileNode#parent()
     */
    public final IObjectProfileNode parent()
    {
        return m_parent;
    }

    /**
     * @see wicket.util.profile.IObjectProfileNode#path()
     */
    public final IObjectProfileNode[] path()
    {
        IObjectProfileNode[] path = m_path;

        if (path != null)
        {
            return path;
        }
        else
        {
            final LinkedList /* IObjectProfileNode */_path = new LinkedList();

            for (IObjectProfileNode node = this; node != null; node = node.parent())
            {
                _path.addFirst(node);
            }

            path = new IObjectProfileNode[_path.size()];
            _path.toArray(path);

            m_path = path;

            return path;
        }
    }

    /**
     * @see wicket.util.profile.IObjectProfileNode#root()
     */
    public final IObjectProfileNode root()
    {
        IObjectProfileNode node = this;

        for (IObjectProfileNode parent = parent(); parent != null; node = parent, parent = parent
                .parent())
        {
            // no action required
        }

        return node;
    }

    /**
     * @see wicket.util.profile.IObjectProfileNode#pathlength()
     */
    public final int pathlength()
    {
        final IObjectProfileNode[] path = m_path;

        if (path != null)
        {
            return path.length;
        }
        else
        {
            int result = 0;

            for (IObjectProfileNode node = this; node != null; node = node.parent())
            {
                ++result;
            }

            return result;
        }
    }

    /**
     * @see wicket.util.profile.IObjectProfileNode#dump()
     */
    public final String dump()
    {
        final StringWriter sw = new StringWriter();
        final PrintWriter out = new PrintWriter(sw);

        final INodeVisitor visitor = ObjectProfileVisitors.newDefaultNodePrinter(out, null, null,
                ObjectProfiler.SHORT_TYPE_NAMES);

        traverse(null, visitor);

        out.flush();

        return sw.toString();
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public final int compareTo(final Object obj)
    {
        return ((AbstractProfileNode) obj).m_size - m_size;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return super.toString() + ": name = " + name() + ", size = " + size();
    }
} // end of class
// ----------------------------------------------------------------------------
