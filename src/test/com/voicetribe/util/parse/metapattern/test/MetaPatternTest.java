///////////////////////////////////////////////////////////////////////////////////
//
// Created Jun 2, 2004
//
// Copyright 2004, Jonathan W. Locke
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package com.voicetribe.util.parse.metapattern.test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import junit.framework.TestCase;

import com.voicetribe.util.parse.metapattern.Group;
import com.voicetribe.util.parse.metapattern.IntegerGroup;
import com.voicetribe.util.parse.metapattern.MetaPattern;
import com.voicetribe.util.parse.metapattern.parsers.CommaSeparatedVariableParser;
import com.voicetribe.util.parse.metapattern.parsers.IntegerVariableAssignmentParser;


/**
 * Test cases for this object
 * @author Jonathan Locke
 */
public final class MetaPatternTest extends TestCase
{
    public void test()
    {
        {
            // Parse "variable = <number>"
            final Group variable = new Group(MetaPattern.VARIABLE_NAME);
            final IntegerGroup value = new IntegerGroup(MetaPattern.INTEGER);
            final MetaPattern variableAssignment = new MetaPattern(new MetaPattern[]
            {
                variable,
                MetaPattern.OPTIONAL_WHITESPACE,
                MetaPattern.EQUALS,
                MetaPattern.OPTIONAL_WHITESPACE,
                value
            });

            System.out.println("variableAssignment MetaPattern = " + variableAssignment);
            final Matcher matcher = variableAssignment.matcher("foo = 9");
            final Map variables = new HashMap();
            if (matcher.matches())
            {
                variables.put(variable.get(matcher), new Integer(value.getInt(matcher)));
            }
            System.out.println("variables = " + variables);
        }
        {
            final IntegerVariableAssignmentParser parser = new IntegerVariableAssignmentParser("foo = 9");
            final Map variables = new HashMap();
            if (parser.matches())
            {
                variables.put(parser.getVariable(), new Integer(parser.getIntValue()));
            }
            System.out.println("variables = " + variables);
        }
        {
            final String csv = "a,b,c";
            final CommaSeparatedVariableParser parser = new CommaSeparatedVariableParser(csv);
            if (parser.matches())
            {
                final List list = parser.getValues();
                for (Iterator iterator = list.iterator(); iterator.hasNext(); )
                {
                    System.out.println("value = " + iterator.next());
                }
            }
        }
    }
}

///////////////////////////////// End of File /////////////////////////////////
