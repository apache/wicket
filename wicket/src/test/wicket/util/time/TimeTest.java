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
package wicket.util.time;


import junit.framework.Assert;
import junit.framework.TestCase;

import java.text.ParseException;

import java.util.GregorianCalendar;

import wicket.util.time.Time;

/**
 * Test cases for this object
 * @author Jonathan Locke
 */
public final class TimeTest extends TestCase
{
    /**
     * 
     * @throws ParseException
     */
    public void test() throws ParseException
    {
        final Time start = Time.now();

        Assert.assertTrue(Time.now().after(start) || Time.now().equals(start));

        final Time birthday = Time.parseDate("1966.06.01");

        Assert.assertEquals(1966, birthday.getYear());
        Assert.assertEquals(GregorianCalendar.JUNE, birthday.getMonth());
        Assert.assertEquals(1, birthday.getDayOfMonth());

        final String y2k = "2000.01.01-12.00am";

        Assert.assertEquals(y2k, Time.valueOf(y2k).toString());
    }
}

///////////////////////////////// End of File /////////////////////////////////
