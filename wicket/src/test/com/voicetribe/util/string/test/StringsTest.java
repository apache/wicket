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

package com.voicetribe.util.string.test;

import com.voicetribe.util.string.Strings;

import junit.framework.Assert;
import junit.framework.TestCase;


/**
 * Test cases for this object
 * @author Jonathan Locke
 */
public final class StringsTest extends TestCase
{
    public void test()
    {
        Assert.assertEquals("foo", Strings.lastPathComponent("bar.garply.foo", '.'));
        Assert.assertEquals("foo", Strings.lastPathComponent("foo", '.'));
        Assert.assertEquals("bar", Strings.firstPathComponent("bar.garply.foo", '.'));
        Assert.assertEquals("foo", Strings.lastPathComponent("foo", '.'));
        Assert.assertEquals("garply.foo", Strings.afterFirstPathComponent("bar.garply.foo", '.'));
        Assert.assertEquals("", Strings.afterFirstPathComponent("foo", '.'));
        Assert.assertEquals("bar.baz", Strings.beforeLast("bar.baz.foo", '.'));
        Assert.assertEquals("", Strings.beforeLast("bar", '.'));
        Assert.assertEquals("bar", Strings.beforeFirst("bar.baz.foo", '.'));
        Assert.assertEquals("", Strings.beforeFirst("bar", '.'));
        Assert.assertEquals("baz.foo", Strings.afterFirst("bar.baz.foo", '.'));
        Assert.assertEquals("", Strings.afterFirst("bar", '.'));
        Assert.assertEquals("foo", Strings.afterLast("bar.baz.foo", '.'));
        Assert.assertEquals("", Strings.afterLast("bar", '.'));
        Assert.assertEquals("foo", Strings.replaceAll("afaooaaa", "a", ""));
        Assert.assertEquals("fuzzyffuzzyoofuzzyfuzzyfuzzy", Strings.replaceAll("afaooaaa", "a", "fuzzy"));
    }
}
///////////////////////////////// End of File /////////////////////////////////
