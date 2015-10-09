/*
 * ====================================================================
 * 
 * The Apache Software License, Version 1.1
 * 
 * Copyright (c) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 * include the following acknowledgement: "This product includes software
 * developed by the Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgement may appear in the software itself, if and
 * wherever such third-party acknowledgements normally appear.
 * 
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 * Foundation" must not be used to endorse or promote products derived from this
 * software without prior written permission. For written permission, please
 * contact apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache" nor may
 * "Apache" appear in their names without prior written permission of the Apache
 * Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE APACHE
 * SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 * 
 * This software consists of voluntary contributions made by many individuals on
 * behalf of the Apache Software Foundation. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 * 
 */

package org.apache.wicket.util.diff;

import java.util.List;

/**
 * Holds a delete-delta between to revisions of a text.
 * 
 * @version $Id: DeleteDelta.java,v 1.1 2006/03/12 00:24:21 juanca Exp $
 * @author <a href="mailto:juanco@suigeneris.org">Juanco Anez</a>
 * @see Delta
 * @see Diff
 * @see Chunk
 */
public class DeleteDelta extends Delta
{
	/**
	 * Construct.
	 */
	DeleteDelta()
	{
	}

	/**
	 * Construct.
	 * 
	 * @param orig
	 */
	public DeleteDelta(final Chunk orig)
	{
		init(orig, null);
	}

	/**
	 * @see org.apache.wicket.util.diff.Delta#verify(java.util.List)
	 */
	@Override
	public void verify(final List<Object> target) throws PatchFailedException
	{
		if (!original.verify(target))
		{
			throw new PatchFailedException();
		}
	}

	/**
	 * @see org.apache.wicket.util.diff.Delta#applyTo(java.util.List)
	 */
	@Override
	public void applyTo(final List<Object> target)
	{
		original.applyDelete(target);
	}

	/**
	 * @see org.apache.wicket.util.diff.Delta#toString(java.lang.StringBuilder)
	 */
	@Override
	public void toString(final StringBuilder s)
	{
		s.append(original.rangeString());
		s.append("d");
		s.append(revised.rcsto());
		s.append(Diff.NL);
		original.toString(s, "< ", Diff.NL);
	}

	/**
	 * @see org.apache.wicket.util.diff.Delta#toRCSString(java.lang.StringBuilder, java.lang.String)
	 */
	@Override
	public void toRCSString(final StringBuilder s, final String EOL)
	{
		s.append("d");
		s.append(original.rcsfrom());
		s.append(" ");
		s.append(original.size());
		s.append(EOL);
	}

	/**
	 * @see org.apache.wicket.util.diff.Delta#accept(org.apache.wicket.util.diff.RevisionVisitor)
	 */
	@Override
	public void accept(final RevisionVisitor visitor)
	{
		visitor.visit(this);
	}
}
