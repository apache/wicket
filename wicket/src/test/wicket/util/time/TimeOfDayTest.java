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

import wicket.util.time.TimeOfDay;

/**
 * Test cases for this object
 * @author Jonathan Locke
 */
public final class TimeOfDayTest extends TestCase
{
    /**
     * 
     * @throws ParseException
     */
    public void test() throws ParseException
    {
        Assert.assertTrue(TimeOfDay.MIDNIGHT.hour() == 0);
        Assert.assertTrue(TimeOfDay.valueOf(TimeOfDay.MIDNIGHT.next()).equals(TimeOfDay.MIDNIGHT));

        final TimeOfDay three = TimeOfDay.time(3, 0, TimeOfDay.PM);
        final TimeOfDay five = TimeOfDay.time(5, 0, TimeOfDay.PM);

        Assert.assertTrue(five.after(three));
    }
}


