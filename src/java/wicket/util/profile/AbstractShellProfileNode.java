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
 * Abstract base class for all shell pseudo-node implementations in this package. It is
 * used primarily to lower memory consumption by shell nodes.
 * @author (C) <a href="http://www.javaworld.com/columns/jw-qna-index.shtml">Vlad Roubtsov
 *         </a>, 2003
 */
abstract class AbstractShellProfileNode extends AbstractProfileNode
{
    // protected: .............................................................
    // package: ...............................................................
    AbstractShellProfileNode(final IObjectProfileNode parent)
    {
        super(parent);
    }

    /**
     * @see wicket.util.profile.IObjectProfileNode#object()
     */
    public final Object object()
    {
        return null;
    }

    /**
     * @see wicket.util.profile.IObjectProfileNode#shell()
     */
    public final IObjectProfileNode shell()
    {
        return null;
    }

    /**
     * @see wicket.util.profile.IObjectProfileNode#children()
     */
    public final IObjectProfileNode[] children()
    {
        return EMPTY_OBJECTPROFILENODE_ARRAY;
    }

    /**
     * @see wicket.util.profile.IObjectProfileNode#refcount()
     */
    public final int refcount()
    {
        return 0;
    }

    /**
     * @see wicket.util.profile.IObjectProfileNode#traverse(wicket.util.profile.IObjectProfileNode.INodeFilter, wicket.util.profile.IObjectProfileNode.INodeVisitor)
     */
    public final boolean traverse(final INodeFilter filter, final INodeVisitor visitor)
    {
        if ((visitor != null) && ((filter == null) || filter.accept(this)))
        {
            visitor.previsit(this);
            visitor.postvisit(this);

            return true;
        }

        return false;
    }

    // private: ...............................................................
} // end of class
// ----------------------------------------------------------------------------
