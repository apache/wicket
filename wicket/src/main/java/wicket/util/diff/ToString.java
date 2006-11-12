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

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;
import java.util.LinkedList;

/**
 * This class delegates handling of the to a StringBuffer based version.
 * 
 * @version $Revision: 1.1 $ $Date: 2006/03/12 00:24:21 $
 * @author <a href="mailto:juanco@suigeneris.org">Juanco Anez</a>
 */
public class ToString
{
    /**
     * Construct.
     */
    public ToString()
    {
    }

    /**
     * Default implementation of the
     * {@link java.lang.Object#toString toString() } method that delegates work
     * to a {@link java.lang.StringBuffer StringBuffer} base version.
     */
    @Override
	public String toString()
    {
        StringBuffer s = new StringBuffer();
        toString(s);
        return s.toString();
    }

    /**
     * Place a string image of the object in a StringBuffer.
     * 
     * @param s
     *            the string buffer.
     */
    public void toString(StringBuffer s)
    {
        s.append(super.toString());
    }

    /**
     * Breaks a string into an array of strings. Use the value of the
     * <code>line.separator</code> system property as the linebreak character.
     * 
     * @param value
     *            the string to convert.
     * @return String[]
     */
    public static String[] stringToArray(String value)
    {
        BufferedReader reader = new BufferedReader(new StringReader(value));
        List<String> l = new LinkedList<String>();
        String s;
        try
        {
            while ((s = reader.readLine()) != null)
            {
                l.add(s);
            }
        }
        catch (java.io.IOException e)
        {
        }
        return l.toArray(new String[l.size()]);
    }

    /**
     * Converts an array of {@link Object Object} to a string Use the value of
     * the <code>line.separator</code> system property the line separator.
     * 
     * @param o
     *            the array of objects.
     * @return String
     */
    public static String arrayToString(Object[] o)
    {
        return arrayToString(o, System.getProperty("line.separator"));
    }

    /**
     * Converts an array of {@link Object Object} to a string using the given
     * line separator.
     * 
     * @param o
     *            the array of objects.
     * @param EOL
     *            the string to use as line separator.
     * @return String
     */
    public static String arrayToString(Object[] o, String EOL)
    {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < o.length - 1; i++)
        {
            buf.append(o[i]);
            buf.append(EOL);
        }
        buf.append(o[o.length - 1]);
        return buf.toString();
    }
}
