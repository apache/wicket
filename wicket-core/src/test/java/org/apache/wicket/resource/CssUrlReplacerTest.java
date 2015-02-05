package org.apache.wicket.resource;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import org.apache.wicket.WicketTestCase;
import org.junit.Test;

public class CssUrlReplacerTest extends WicketTestCase
{
	@Test
	public void sameFolderSingleQuotes()
	{
		String input = ".class {background-image: url('some.img');}";
		Class<?> scope = CssUrlReplacerTest.class;
		String cssRelativePath = "res/css/some.css";
		CssUrlReplacer replacer = new CssUrlReplacer();

		String processed = replacer.process(input, scope, cssRelativePath);
		assertThat(processed, is(".class {background-image: url('./wicket/resource/org.apache.wicket.resource.CssUrlReplacerTest/res/css/some.img');}"));
	}

	@Test
	public void sameFolderDoubleQuotes()
	{
		String input = ".class {background-image: url(\"some.img\");}";
		Class<?> scope = CssUrlReplacerTest.class;
		String cssRelativePath = "res/css/some.css";
		CssUrlReplacer replacer = new CssUrlReplacer();

		String processed = replacer.process(input, scope, cssRelativePath);
		assertThat(processed, is(".class {background-image: url('./wicket/resource/org.apache.wicket.resource.CssUrlReplacerTest/res/css/some.img');}"));
	}

	@Test
	public void parentFolderAppendFolder()
	{
		String input = ".class {background-image: url('../images/some.img');}";
		Class<?> scope = CssUrlReplacerTest.class;
		String cssRelativePath = "res/css/some.css";
		CssUrlReplacer replacer = new CssUrlReplacer();

		String processed = replacer.process(input, scope, cssRelativePath);
		assertThat(processed, is(".class {background-image: url('./wicket/resource/org.apache.wicket.resource.CssUrlReplacerTest/res/images/some.img');}"));
	}

	@Test
	public void sameFolderAppendFolder()
	{
		String input = ".class {background-image: url('./images/some.img');}";
		Class<?> scope = CssUrlReplacerTest.class;
		String cssRelativePath = "res/css/some.css";
		CssUrlReplacer replacer = new CssUrlReplacer();

		String processed = replacer.process(input, scope, cssRelativePath);
		assertThat(processed, is(".class {background-image: url('./wicket/resource/org.apache.wicket.resource.CssUrlReplacerTest/res/css/images/some.img');}"));
	}

	@Test
	public void severalUrls()
	{
		String input =
				".class {\n" +
					"a: url('../images/a.img');\n" +
					"b: url('./b.img');\n" +
				"}";
		Class<?> scope = CssUrlReplacerTest.class;
		String cssRelativePath = "res/css/some.css";
		CssUrlReplacer replacer = new CssUrlReplacer();

		String processed = replacer.process(input, scope, cssRelativePath);
		assertThat(processed, containsString("CssUrlReplacerTest/res/images/a.img');"));
		assertThat(processed, containsString("CssUrlReplacerTest/res/css/b.img');"));
	}
}
