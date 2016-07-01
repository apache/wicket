package org.apache.wicket.resource.loader;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.wicket.Component;
import org.apache.wicket.resource.loader.ClassStringResourceLoaderTest.MyValidator;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * Tests the nested string resource loader
 */
public class NestedStringResourceLoaderTest extends WicketTestCase
{

	/**
	 * Tests the nested string resource loader
	 */
	@Test
	public void testNestedStrings(){
		List<IStringResourceLoader> loaders = tester.getApplication().getResourceSettings().getStringResourceLoaders();
		ClassStringResourceLoader classStringResourceLoader = new ClassStringResourceLoader(MyValidator.class);
		loaders.add(classStringResourceLoader);
		NestedStringResourceLoader nestedStringResourceLoader = new NestedStringResourceLoader(loaders,Pattern.compile("#\\(([^ ]*?)\\)"));
		loaders.clear();
		loaders.add(nestedStringResourceLoader);
		
		assertEquals("This is an assembled nested key.",
			nestedStringResourceLoader.loadStringResource((Component)null, "nested", null, null, null));
	}

}
