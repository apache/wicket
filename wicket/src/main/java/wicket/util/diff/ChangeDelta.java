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

import java.util.List;

/**
 * Holds an change-delta between to revisions of a text.
 * 
 * @version $Id: ChangeDelta.java,v 1.1 2006/03/12 00:24:21 juanca Exp $
 * @author <a href="mailto:juanco@suigeneris.org">Juanco Anez</a>
 * @see Delta
 * @see Diff
 * @see Chunk
 */
public class ChangeDelta extends Delta
{
	/**
	 * Construct.
	 */
    ChangeDelta()
    {
    }

    /**
     * Construct.
     * @param orig
     * @param rev
     */
    public ChangeDelta(Chunk orig, Chunk rev)
    {
        init(orig, rev);
    }

    /**

     * @see wicket.util.diff.Delta#verify(java.util.List)
     */
    @Override
	public void verify(List<Object> target) throws PatchFailedException
    {
        if (!original.verify(target))
        {
            throw new PatchFailedException();
        }
        if (original.first() > target.size())
        {
            throw new PatchFailedException("original.first() > target.size()");
        }
    }

    /**
     * @see wicket.util.diff.Delta#applyTo(java.util.List)
     */
    @Override
	public void applyTo(List<Object> target)
    {
        original.applyDelete(target);
        revised.applyAdd(original.first(), target);
    }

    /**
     * @see wicket.util.diff.Delta#toString(java.lang.StringBuffer)
     */
    @Override
	public void toString(StringBuffer s)
    {
        original.rangeString(s);
        s.append("c");
        revised.rangeString(s);
        s.append(Diff.NL);
        original.toString(s, "< ", "\n");
        s.append("---");
        s.append(Diff.NL);
        revised.toString(s, "> ", "\n");
    }

    /**
     * @see wicket.util.diff.Delta#toRCSString(java.lang.StringBuffer, java.lang.String)
     */
    @Override
	public void toRCSString(StringBuffer s, String EOL)
    {
        s.append("d");
        s.append(original.rcsfrom());
        s.append(" ");
        s.append(original.size());
        s.append(EOL);
        s.append("a");
        s.append(original.rcsto());
        s.append(" ");
        s.append(revised.size());
        s.append(EOL);
        revised.toString(s, "", EOL);
    }

    /**
     * @see wicket.util.diff.Delta#accept(wicket.util.diff.RevisionVisitor)
     */
    @Override
	public void accept(RevisionVisitor visitor)
    {
        visitor.visit(this);
    }
}
