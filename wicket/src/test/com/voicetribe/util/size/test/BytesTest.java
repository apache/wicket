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

package com.voicetribe.util.size.test;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.voicetribe.util.size.Bytes;
import com.voicetribe.util.string.StringValueConversionException;


/**
 * Test cases for this object
 * @author Jonathan Locke
 */
public final class BytesTest extends TestCase
{
    public void test() throws StringValueConversionException
    {
        Assert.assertTrue(Bytes.bytes(1024).equals(Bytes.kilobytes(1)));
        Assert.assertTrue(Bytes.bytes(1024 * 1024).equals(Bytes.megabytes(1)));
        Assert.assertTrue("1G".equals(Bytes.gigabytes(1).toString()));
        Assert.assertTrue(Bytes.valueOf("15.5K").bytes() == (15 * 1024 + 512));
        final Bytes b = Bytes.kilobytes(7.3);
        Assert.assertTrue(b.equals(Bytes.kilobytes(7.3)));
        Assert.assertTrue(b.greaterThan(Bytes.kilobytes(7.25)));
        Assert.assertTrue(b.lessThan(Bytes.kilobytes(7.9)));
        Assert.assertTrue(Bytes.valueOf(b.toString()).equals(b));
    }
}
///////////////////////////////// End of File /////////////////////////////////
