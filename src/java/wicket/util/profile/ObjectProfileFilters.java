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
 * A Factory for a few stock node filters. See the implementation for details.
 * @author (C) <a href="http://www.javaworld.com/columns/jw-qna-index.shtml">Vlad Roubtsov
 *         </a>, 2003
 */
public abstract class ObjectProfileFilters
{
    // protected: .............................................................
    // package: ...............................................................
    // private: ...............................................................
    private ObjectProfileFilters()
    {
    } // this class is not extendible

    // public: ................................................................

    /**
     * Factory method for creating a visitor that only accepts profile nodes with sizes
     * larger than a given threshold value.
     * @param threshold node size in bytes
     * @return node filter
     */
    public static ObjectProfileNode.INodeFilter newSizeFilter(final int threshold)
    {
        return new SizeFilter(threshold);
    }

    /**
     * Factory method for creating a visitor that accepts a profile node only if it is at
     * least the k-th largest child of its parent for a given value of k. E.g.,
     * newRankFilter(1) will prune the profile tree so that only the largest child is
     * visited for every node.
     * @param rank acceptable size rank [must be >= 0]
     * @return node filter
     */
    public static ObjectProfileNode.INodeFilter newRankFilter(final int rank)
    {
        return new RankFilter(rank);
    }

    /**
     * Factory method for creating a visitor that accepts a profile node only if its size
     * is larger than a given threshold relative to the size of the root node (i.e., size
     * of the entire profile tree).
     * @param threshold size fraction threshold
     * @return node filter
     */
    public static ObjectProfileNode.INodeFilter newSizeFractionFilter(final double threshold)
    {
        return new SizeFractionFilter(threshold);
    }

    /**
     * Factory method for creating a visitor that accepts a profile node only if its size
     * is larger than a given threshold relative to the size of its parent node. This is
     * useful for pruning the profile tree to show the largest contributors at every tree
     * level.
     * @param threshold size fraction threshold
     * @return node filter
     */
    public static ObjectProfileNode.INodeFilter newParentSizeFractionFilter(final double threshold)
    {
        return new ParentSizeFractionFilter(threshold);
    }

    private static final class SizeFilter implements IObjectProfileNode.INodeFilter
    {
        private final int m_threshold;

        SizeFilter(final int threshold)
        {
            m_threshold = threshold;
        }

        /**
         * @see wicket.util.profile.IObjectProfileNode.INodeFilter#accept(wicket.util.profile.IObjectProfileNode)
         */
        public boolean accept(final IObjectProfileNode node)
        {
            return node.size() >= m_threshold;
        }
    } // end of nested class

    private static final class RankFilter implements IObjectProfileNode.INodeFilter
    {
        private final int m_threshold;

        RankFilter(final int threshold)
        {
            m_threshold = threshold;
        }

        /**
         * @see wicket.util.profile.IObjectProfileNode.INodeFilter#accept(wicket.util.profile.IObjectProfileNode)
         */
        public boolean accept(final IObjectProfileNode node)
        {
            final IObjectProfileNode parent = node.parent();

            if (parent == null)
            {
                return true;
            }

            final IObjectProfileNode[] siblings = parent.children();

            for (int r = 0, rLimit = Math.min(siblings.length, m_threshold); r < rLimit; ++r)
            {
                if (siblings[r] == node)
                {
                    return true;
                }
            }

            return false;
        }
    } // end of nested class

    private static final class SizeFractionFilter implements IObjectProfileNode.INodeFilter
    {
        private final double m_threshold;

        SizeFractionFilter(final double threshold)
        {
            m_threshold = threshold;
        }

        /**
         * @see wicket.util.profile.IObjectProfileNode.INodeFilter#accept(wicket.util.profile.IObjectProfileNode)
         */
        public boolean accept(final IObjectProfileNode node)
        {
            if (node.size() >= (m_threshold * node.root().size()))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
    } // end of nested class

    private static final class ParentSizeFractionFilter implements IObjectProfileNode.INodeFilter
    {
        private final double m_threshold;

        ParentSizeFractionFilter(final double threshold)
        {
            m_threshold = threshold;
        }

        /**
         * @see wicket.util.profile.IObjectProfileNode.INodeFilter#accept(wicket.util.profile.IObjectProfileNode)
         */
        public boolean accept(final IObjectProfileNode node)
        {
            final IObjectProfileNode parent = node.parent();

            if (parent == null)
            {
                return true; // always accept root node
            }
            else if (node.size() >= (m_threshold * parent.size()))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
    } // end of nested class
} // end of class
// ----------------------------------------------------------------------------
