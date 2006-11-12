/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.util.diff;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * A Revision holds the series of deltas that describe the differences between
 * two sequences.
 * 
 * @version $Revision: 1.1 $ $Date: 2006/03/12 00:24:21 $
 * 
 * @author <a href="mailto:juanco@suigeneris.org">Juanco Anez</a>
 * @author <a href="mailto:bwm@hplb.hpl.hp.com">Brian McBride</a>
 * 
 * @see Delta
 * @see Diff
 * @see Chunk
 * @see Revision
 * 
 * modifications 27 Apr 2003 bwm
 * 
 * Added visitor pattern Visitor interface and accept() method.
 */

public class Revision extends ToString
{

    List<Delta> deltas_ = new LinkedList<Delta>();

    /**
     * Creates an empty Revision.
     */
    public Revision()
    {
    }

    /**
     * Adds a delta to this revision.
     * 
     * @param delta
     *            the {@link Delta Delta} to add.
     */
    public synchronized void addDelta(Delta delta)
    {
        if (delta == null)
        {
            throw new IllegalArgumentException("new delta is null");
        }
        deltas_.add(delta);
    }

    /**
     * Adds a delta to the start of this revision.
     * 
     * @param delta
     *            the {@link Delta Delta} to add.
     */
    public synchronized void insertDelta(Delta delta)
    {
        if (delta == null)
        {
            throw new IllegalArgumentException("new delta is null");
        }
        deltas_.add(0, delta);
    }

    /**
     * Retrieves a delta from this revision by position.
     * 
     * @param i
     *            the position of the delta to retrieve.
     * @return the specified delta
     */
    public Delta getDelta(int i)
    {
        return deltas_.get(i);
    }

    /**
     * Returns the number of deltas in this revision.
     * 
     * @return the number of deltas.
     */
    public int size()
    {
        return deltas_.size();
    }

    /**
     * Applies the series of deltas in this revision as patches to the given
     * text.
     * 
     * @param src
     *            the text to patch, which the method doesn't change.
     * @return the resulting text after the patches have been applied.
     * @throws PatchFailedException
     *             if any of the patches cannot be applied.
     */
	public Object[] patch(Object[] src) throws PatchFailedException
    {
        List<Object> target = new ArrayList<Object>(Arrays.asList(src));
        applyTo(target);
        return target.toArray();
    }

    /**
     * Applies the series of deltas in this revision as patches to the given
     * text.
     * 
     * @param target
     *            the text to patch.
     * @throws PatchFailedException
     *             if any of the patches cannot be applied.
     */
    public synchronized void applyTo(List<Object> target) throws PatchFailedException
    {
        ListIterator i = deltas_.listIterator(deltas_.size());
        while (i.hasPrevious())
        {
            Delta delta = (Delta) i.previous();
            delta.patch(target);
        }
    }

    /**
     * Converts this revision into its Unix diff style string representation.
     * 
     * @param s
     *            a {@link StringBuffer StringBuffer} to which the string
     *            representation will be appended.
     */
    @Override
	public synchronized void toString(StringBuffer s)
    {
        Iterator i = deltas_.iterator();
        while (i.hasNext())
        {
            ((Delta) i.next()).toString(s);
        }
    }

    /**
     * Converts this revision into its RCS style string representation.
     * 
     * @param s
     *            a {@link StringBuffer StringBuffer} to which the string
     *            representation will be appended.
     * @param EOL
     *            the string to use as line separator.
     */
    public synchronized void toRCSString(StringBuffer s, String EOL)
    {
        Iterator i = deltas_.iterator();
        while (i.hasNext())
        {
            ((Delta) i.next()).toRCSString(s, EOL);
        }
    }

    /**
     * Converts this revision into its RCS style string representation.
     * 
     * @param s
     *            a {@link StringBuffer StringBuffer} to which the string
     *            representation will be appended.
     */
    public void toRCSString(StringBuffer s)
    {
        toRCSString(s, Diff.NL);
    }

    /**
     * Converts this delta into its RCS style string representation.
     * 
     * @param EOL
     *            the string to use as line separator.
     * @return String
     */
    public String toRCSString(String EOL)
    {
        StringBuffer s = new StringBuffer();
        toRCSString(s, EOL);
        return s.toString();
    }

    /**
     * Converts this delta into its RCS style string representation using the
     * default line separator.
     * @return String
     */
    public String toRCSString()
    {
        return toRCSString(Diff.NL);
    }

    /**
     * Accepts a visitor.
     * 
     * @param visitor
     *            the {@link Visitor} visiting this instance
     */
    public void accept(RevisionVisitor visitor)
    {
        visitor.visit(this);
        Iterator iter = deltas_.iterator();
        while (iter.hasNext())
        {
            ((Delta) iter.next()).accept(visitor);
        }
    }

}
