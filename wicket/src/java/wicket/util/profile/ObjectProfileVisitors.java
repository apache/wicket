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

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import java.text.DecimalFormat;
import java.text.NumberFormat;

// ----------------------------------------------------------------------------

/**
 * A Factory for a few stock node visitors. See the implementation for details.
 * @author (C) <a href="http://www.javaworld.com/columns/jw-qna-index.shtml">Vlad Roubtsov
 *         </a>, 2003
 */
public abstract class ObjectProfileVisitors
{ // TODO finalize javadoc
    // protected: .............................................................
    // package: ...............................................................
    // private: ...............................................................
    private ObjectProfileVisitors()
    {
    } // this class is not extendible

    // public: ................................................................

    /**
     * Factory method for creating the default plain text node node print visitor. It is
     * up to the caller to buffer 'out'.
     * @param out writer to dump the nodes into [may not be null]
     * @param indent indent increment string [null is equivalent to " "]
     * @param format percentage formatter to use [null is equivalent to
     *            NumberFormat.getPercentInstance (), with a single fraction digit]
     * @param shortClassNames 'true' causes all class names to be dumped in compact [no
     *            package prefix] form
     * @return node visitor
     */
    public static ObjectProfileNode.INodeVisitor newDefaultNodePrinter(final PrintWriter out,
            final String indent, final DecimalFormat format, final boolean shortClassNames)
    {
        return new DefaultNodePrinter(out, indent, format, shortClassNames);
    }

    /**
     * Factory method for creating the XML output visitor. To create a valid XML document,
     * start the traversal on the profile root node. It is up to the caller to buffer
     * 'out'.
     * @param out stream to dump the nodes into [may not be null]
     * @param indent indent increment string [null is equivalent to " "]
     * @param format percentage formatter to use [null is equivalent to
     *            NumberFormat.getPercentInstance (), with a single fraction digit]
     * @param shortClassNames 'true' causes all class names to be dumped in compact [no
     *            package prefix] form
     * @return node visitor
     */
    public static ObjectProfileNode.INodeVisitor newXMLNodePrinter(final OutputStream out,
            final String indent, final DecimalFormat format, final boolean shortClassNames)
    {
        return new XMLNodePrinter(out, indent, format, shortClassNames);
    }

    private static abstract class AbstractProfileNodeVisitor implements
            IObjectProfileNode.INodeVisitor
    {
        /**
         * @see wicket.util.profile.IObjectProfileNode.INodeVisitor#previsit(wicket.util.profile.IObjectProfileNode)
         */
        public void previsit(final IObjectProfileNode node)
        {
        }

        /**
         * @see wicket.util.profile.IObjectProfileNode.INodeVisitor#postvisit(wicket.util.profile.IObjectProfileNode)
         */
        public void postvisit(final IObjectProfileNode node)
        {
        }
    } // end of nested class

    /**
     * This visitor prints out a node in plain text format. The output is indented
     * according to the length of the node's path within its profile tree.
     */
    private static final class DefaultNodePrinter extends AbstractProfileNodeVisitor
    {
        private final PrintWriter m_out;

        private final String m_indent;

        private final DecimalFormat m_format;

        private final boolean m_shortClassNames;

        DefaultNodePrinter(final PrintWriter out, final String indent, final DecimalFormat format,
                final boolean shortClassNames)
        {
            if (out == null)
            {
                throw new RuntimeException("null input: out");
            }

            m_out = out;
            m_indent = (indent != null) ? indent : "  ";

            if (format != null)
            {
                m_format = format;
            }
            else
            {
                m_format = (DecimalFormat) NumberFormat.getPercentInstance();
                m_format.setMaximumFractionDigits(1);
            }

            m_shortClassNames = shortClassNames;
        }

        /**
         * @see wicket.util.profile.ObjectProfileVisitors.AbstractProfileNodeVisitor#previsit(wicket.util.profile.IObjectProfileNode)
         */
        public void previsit(final IObjectProfileNode node)
        {
            final StringBuffer sb = new StringBuffer();

            for (int p = 0, pLimit = node.pathlength(); p < pLimit; ++p)
            {
                sb.append(m_indent);
            }

            final IObjectProfileNode root = node.root();

            sb.append(node.size());

            if (node != root) // root node is always 100% of the overall size
            {
                sb.append(" (");
                sb.append(m_format.format((double) node.size() / root.size()));
                sb.append(")");
            }

            sb.append(" -> ");
            sb.append(node.name());

            if (node.object() != null) // skip shell pseudo-nodes
            {
                sb.append(" : ");
                sb.append(ObjectProfiler.typeName(node.object().getClass(), m_shortClassNames));

                if (node.refcount() > 1) // show refcount only when it's > 1
                {
                    sb.append(", refcount=");
                    sb.append(node.refcount());
                }
            }

            m_out.println(sb);
            m_out.flush();
        }
    } // end of nested class

    /*
     * This visitor can dump a profile tree in an XML file, which can be handy for
     * examination of very large object graphs.
     */
    private static final class XMLNodePrinter extends AbstractProfileNodeVisitor
    {
        private static final String ENCODING = "UTF-8";

        private final PrintWriter m_out;

        private final String m_indent;

        private final DecimalFormat m_format;

        private final boolean m_shortClassNames;

        XMLNodePrinter(final OutputStream out, final String indent, final DecimalFormat format,
                final boolean shortClassNames)
        {
            if (out == null)
            {
                throw new RuntimeException("null input: out");
            }

            try
            {
                m_out = new PrintWriter(new OutputStreamWriter(out, ENCODING));
            }
            catch (UnsupportedEncodingException uee)
            {
                throw new Error(uee);
            }

            m_indent = (indent != null) ? indent : "  ";

            if (format != null)
            {
                m_format = format;
            }
            else
            {
                m_format = (DecimalFormat) NumberFormat.getPercentInstance();
                m_format.setMaximumFractionDigits(2);
            }

            m_shortClassNames = shortClassNames;
        }

        /**
         * @see wicket.util.profile.ObjectProfileVisitors.AbstractProfileNodeVisitor#previsit(wicket.util.profile.IObjectProfileNode)
         */
        public void previsit(final IObjectProfileNode node)
        {
            final IObjectProfileNode root = node.root();
            final boolean isRoot = root == node;

            if (isRoot)
            {
                m_out.println("<?xml version=\"1.0\" encoding=\"" + ENCODING + "\"?>");
                m_out.println("<input>");
            }

            final StringBuffer indent = new StringBuffer();

            for (int p = 0, pLimit = node.pathlength(); p < pLimit; ++p)
            {
                indent.append(m_indent);
            }

            final StringBuffer sb = new StringBuffer();

            sb.append("<object");

            sb.append(" size=\"");
            sb.append(node.size());
            sb.append('\"');

            if (!isRoot)
            {
                sb.append(" part=\"");
                sb.append(m_format.format((double) node.size() / root.size()));
                sb.append('\"');
            }

            sb.append(" name=\"");
            XMLEscape(node.name(), sb);
            sb.append('\"');

            if (node.object() != null) // skip shell pseudo-nodes
            {
                sb.append(" objclass=\"");

                XMLEscape(ObjectProfiler.typeName(node.object().getClass(), m_shortClassNames), sb);
                sb.append('\"');

                if (node.refcount() > 1)
                {
                    sb.append(" refcount=\"");
                    sb.append(node.refcount());
                    sb.append('\"');
                }
            }

            sb.append('>');
            m_out.print(indent);
            m_out.println(sb);
        }

        /**
         * @see wicket.util.profile.ObjectProfileVisitors.AbstractProfileNodeVisitor#postvisit(wicket.util.profile.IObjectProfileNode)
         */
        public void postvisit(final IObjectProfileNode node)
        {
            final StringBuffer indent = new StringBuffer();

            for (int p = 0, pLimit = node.pathlength(); p < pLimit; ++p)
            {
                indent.append(m_indent);
            }

            m_out.print(indent);
            m_out.println("</object>");

            if (node.root() == node)
            {
                m_out.println("</input>");
                m_out.flush();
            }
        }

        private static void XMLEscape(final String s, final StringBuffer append)
        {
            final char[] chars = s.toCharArray();

            for (int i = 0, iLimit = s.length(); i < iLimit; ++i)
            {
                final char c = chars[i];

                switch (c)
                {
                    case '<':
                        append.append("&lt;");

                        break;

                    case '>':
                        append.append("&gt;");

                        break;

                    case '"':
                        append.append("&#34;");

                        break;

                    case '&':
                        append.append("&amp;");

                        break;

                    default:
                        append.append(c);
                } // end of switch
            }
        }
    } // end of nested class
} // end of class
// ----------------------------------------------------------------------------
