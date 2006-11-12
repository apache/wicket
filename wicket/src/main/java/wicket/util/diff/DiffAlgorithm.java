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

/**
 * A simple interface for implementations of differencing algorithms.
 * 
 * @version $Revision: 1.1 $ $Date: 2006/03/12 00:24:21 $
 * 
 * @author <a href="mailto:bwm@hplb.hpl.hp.com">Brian McBride</a>
 */
public interface DiffAlgorithm
{
    /**
     * Computes the difference between the original sequence and the revised
     * sequence and returns it as a
     * {@link org.suigeneris.jrcs.diff.Revision Revision} object.
     * <p>
     * The revision can be used to construct the revised sequence from the
     * original sequence.
     * @param orig 
     * 
     * @param rev
     *            the revised text
     * @return the revision script.
     * @throws DifferentiationFailedException
     *             if the diff could not be computed.
     */
    public abstract Revision diff(Object[] orig, Object[] rev)
            throws DifferentiationFailedException;
}