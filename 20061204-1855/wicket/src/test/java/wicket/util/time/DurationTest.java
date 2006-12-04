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
package wicket.util.time;


import java.text.NumberFormat;
import java.util.Locale;

import junit.framework.Assert;
import junit.framework.TestCase;
import wicket.util.string.StringValueConversionException;

/**
 * Test cases for this object
 * @author Jonathan Locke
 */
public final class DurationTest extends TestCase
{
    /**
     * 
     * @throws StringValueConversionException
     */
    public void testValues() throws StringValueConversionException
    {
        Assert.assertEquals(Duration.milliseconds(3000), Duration.seconds(3));
        Assert.assertEquals(Duration.seconds(120), Duration.minutes(2));
        Assert.assertEquals(Duration.minutes(1440), Duration.hours(24));
        Assert.assertEquals(Duration.hours(48), Duration.days(2));
        Assert.assertEquals(Duration.minutes(90), Duration.valueOf("90 minutes"));
        Assert.assertEquals(Duration.days(9), Duration.valueOf("9 days"));
        Assert.assertEquals(Duration.hours(1), Duration.valueOf("1 hour"));
        Assert.assertTrue(9 == Duration.days(9).days());
        Assert.assertTrue(11 == Duration.hours(11).hours());
        Assert.assertTrue(21 == Duration.minutes(21).minutes());
        Assert.assertTrue(51 == Duration.seconds(51).seconds());
    }

    /**
     * 
     *
     */
    public void testOperations()
    {
        Assert.assertTrue(Duration.milliseconds(3001).greaterThan(Duration.seconds(3)));
        Assert.assertTrue(Duration.milliseconds(2999).lessThan(Duration.seconds(3)));
        Assert.assertEquals(-1, Duration.milliseconds(2999).compareTo(Duration.seconds(3)));
        Assert.assertEquals(1, Duration.milliseconds(3001).compareTo(Duration.seconds(3)));
        Assert.assertEquals(0, Duration.milliseconds(3000).compareTo(Duration.seconds(3)));
        Assert.assertEquals(Duration.minutes(10), Duration.minutes(4).add(Duration.minutes(6)));
        Assert.assertEquals(Duration.ONE_HOUR, Duration.minutes(90).subtract(Duration.minutes(30)));

        String value = NumberFormat.getNumberInstance().format(1.5);

        Assert.assertEquals(value + " minutes", Duration.seconds(90).toString());
        Assert.assertEquals("12 hours", Duration.days(0.5).toString());
    }

    /**
     * 
     *
     */
    public void testSleep()
    {
        Assert.assertTrue(Duration.seconds(0.5).lessThan(Duration.benchmark(new Runnable()
        {
            public void run()
            {
                Duration.seconds(1.5).sleep();
            }
        })));

        Assert.assertTrue(Duration.seconds(1).greaterThan(Duration.benchmark(new Runnable()
        {
            public void run()
            {
                Duration.hours(-1).sleep();
            }
        })));
    }

    /**
     * 
     * @throws StringValueConversionException
     */
    public void testLocale() throws StringValueConversionException 
    {
        Assert.assertEquals(Duration.minutes(90), Duration.valueOf("90 minutes"));
        Assert.assertEquals(Duration.hours(1.5), Duration.valueOf("1.5 hour", Locale.US));
        Assert.assertEquals(Duration.hours(1.5), Duration.valueOf("1,5 hour", Locale.GERMAN));
        Assert.assertEquals("1.5 hours", Duration.hours(1.5).toString(Locale.US));
        Assert.assertEquals("1,5 hours", Duration.hours(1.5).toString(Locale.GERMAN));
    }
}


