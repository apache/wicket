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
package com.java.jdk;

import junit.framework.Assert;
import junit.framework.TestCase;


/**
 * Test for known Java Compiler Bug (incl. 1.5)
 * see http://www.theserverside.com/news/thread.tss?thread_id=29404
 * 
 * @author Juergen Donnerstag
 */
public class FinalTernaryIfCompilerBug extends TestCase 
{
    /**
     * Create the test case.
     *
     * @param s The test name
     */
    public FinalTernaryIfCompilerBug(String s) {
        super(s);
    }
    
    public void testIt()
    {
        // Should be null
        final String name = false ? "Heinz" : null;
        if (name != null)
        {
            System.err.println();
            System.err.print("****************************************************************");
            System.err.print("* Nasty Little JDK 1.5 Compiler Bug: final + ternary           *");
            System.err.print("* DON'T USE 'final String name = false ? \"Heinz\" : null;     *");
            System.err.print("*                                                              *");
            System.err.print("* http://www.theserverside.com/news/thread.tss?thread_id=29404 *");
            System.err.print("****************************************************************");
            System.err.println();
        }
        
        Assert.assertNull(name);
    }
}
