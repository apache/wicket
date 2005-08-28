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
package wicket.util.string;

import wicket.util.string.Strings;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Test cases for this object
 * 
 * @author Jonathan Locke
 */
public final class StringsTest extends TestCase
{
	/**
	 * 
	 */
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
		Assert.assertEquals("fuzzyffuzzyoofuzzyfuzzyfuzzy", Strings.replaceAll("afaooaaa", "a",
				"fuzzy"));
	}

	/**
	 * Tests the <code>beforeFirst</code> method.
	 */
	public void testBeforeFirst()
	{
		assertNull(Strings.beforeFirst(null, '.'));
		assertEquals("", Strings.beforeFirst("", '.'));
		assertEquals("", Strings.beforeFirst("", ' '));
		assertEquals("", Strings.beforeFirst(".", '.'));
		assertEquals("", Strings.beforeFirst("..", '.'));
		assertEquals("com", Strings.beforeFirst("com.foo.bar", '.'));
		assertEquals("com", Strings.beforeFirst("com foo bar", ' '));
		assertEquals("com foo", Strings.beforeFirst("com foo.bar", '.'));
	}

	/**
	 * Tests the <code>afterFirst</code> method.
	 */
	public void testAfterFirst()
	{
		assertNull(Strings.afterFirst(null, '.'));
		assertEquals("", Strings.afterFirst("", '.'));
		assertEquals("", Strings.afterFirst("", ' '));
		assertEquals("", Strings.afterFirst(".", '.'));
		assertEquals(".", Strings.afterFirst("..", '.'));
		assertEquals("foo.bar", Strings.afterFirst("com.foo.bar", '.'));
		assertEquals("foo bar", Strings.afterFirst("com foo bar", ' '));
		assertEquals("bar", Strings.afterFirst("com.foo bar", ' '));
	}

	/**
	 * Tests the <code>afterLast</code> method.
	 */
	public void testAfterLast()
	{
		assertNull(Strings.afterLast(null, '.'));
		assertEquals("", Strings.afterLast("", '.'));
		assertEquals("", Strings.afterLast("", ' '));
		assertEquals("", Strings.afterLast(".", '.'));
		assertEquals("", Strings.afterLast("..", '.'));
		assertEquals("bar", Strings.afterLast("com.foo.bar", '.'));
		assertEquals("bar", Strings.afterLast("com foo bar", ' '));
		assertEquals("bar", Strings.afterLast("com foo.bar", '.'));
	}

	/**
	 * Tests the beforeLastPathComponent method
	 */
	public void testBeforeLastPathComponent()
	{
		assertNull(Strings.beforeLastPathComponent(null, '.'));
		assertEquals("", Strings.beforeLastPathComponent("", '.'));
		assertEquals("", Strings.beforeLastPathComponent("", ' '));
		assertEquals("", Strings.beforeLastPathComponent(".", '.'));
		assertEquals(".", Strings.beforeLastPathComponent("..", '.'));
		assertEquals("foo", Strings.beforeLastPathComponent("foo.bar", '.'));
		assertEquals("", Strings.beforeLastPathComponent("foo.bar", ' '));
		assertEquals("foo.ba", Strings.beforeLastPathComponent("foo.bar", 'r'));
		assertEquals("com.foo", Strings.beforeLastPathComponent("com.foo.bar", '.'));
	}

	/**
	 * Tests the capitalize method.
	 */
	public void testCapitalize()
	{
		assertEquals("Lorem ipsum dolor sit amet", Strings.capitalize("lorem ipsum dolor sit amet"));
		assertEquals("Lorem ipsum dolor sit amet", Strings.capitalize("Lorem ipsum dolor sit amet"));
		assertEquals(" abcdefghijklm", Strings.capitalize(" abcdefghijklm"));
		assertEquals("", Strings.capitalize(""));
		assertNull(Strings.capitalize(null));
	}

	/**
	 * Tests the escapeMarkup method.
	 */
	public void testEscapeMarkup()
	{
		assertNull(Strings.escapeMarkup(null));
		assertEquals("", Strings.escapeMarkup(""));

		assertEquals("&amp;", Strings.escapeMarkup("&"));
		assertEquals("&#", Strings.escapeMarkup("&#"));
		assertEquals("&#0000;", Strings.escapeMarkup("&#0000;"));
		
		assertEquals("&amp;amp;", Strings.escapeMarkup("&amp;"));
		assertEquals("&lt; &gt;&amp;&quot;&#039;?:;{}[]-_+=()*^%$#@!~`", Strings.escapeMarkup("< >&\"'?:;{}[]-_+=()*^%$#@!~`"));
		assertEquals("&lt;&nbsp;&gt;&amp;&quot;&#039;?:;{}[]-_+=()*^%$#@!~`", Strings.escapeMarkup("< >&\"'?:;{}[]-_+=()*^%$#@!~`", true));
	}
	
	/**
	 * Tests the escapeMarkup method with whitespace.
	 */
	public void testEscapeMarkupWhiteSpace()
	{
		assertNull(Strings.escapeMarkup(null, true));
		assertEquals("", Strings.escapeMarkup("", true));
		
		assertEquals("\n \t", Strings.escapeMarkup("\n \t", false));
		assertEquals("\n&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;", Strings.escapeMarkup("\n \t", true));
		assertEquals("  ", Strings.escapeMarkup("  ", false));
		assertEquals("&nbsp;&nbsp;", Strings.escapeMarkup("  ", true));
	}

	/**
	 * Tests the escapeMarkup method with unicode escapes.
	 */
	public void testEscapeMarkupUnicode()
	{
		assertNull(Strings.escapeMarkup(null, true, true));
		assertEquals("", Strings.escapeMarkup("", true, true));

		assertEquals("&#199;&#252;&#233;&#226;&#228;&#224;&#229;&#231;&#234;&#235;", 
				Strings.escapeMarkup("Çüéâäàåçêë", false, true));

		assertEquals("\n \t&#233;", Strings.escapeMarkup("\n \té", false, true));
		assertEquals("\n \té", Strings.escapeMarkup("\n \té", false, false));
	}
}
