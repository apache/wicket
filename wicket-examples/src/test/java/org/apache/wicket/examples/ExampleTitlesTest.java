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
package org.apache.wicket.examples;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.wicket.examples.frames.TopFrame;
import org.apache.wicket.examples.homepage.HomePage;
import org.apache.wicket.examples.wizard.NewUserWizard;
import org.apache.wicket.examples.wizard.StaticWizard;
import org.apache.wicket.examples.wizard.StaticWizardWithPanels;
import org.apache.wicket.examples.wizard.WizardPage;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Checks that every example page has a title to show.
 */
class ExampleTitlesTest extends WicketTestCase
{
	/** Pages that are not an example of their own, and the page titled after its wizard. */
	private static final Set<Class<?>> UNTITLED = Set.of(HomePage.class, TopFrame.class,
		WizardPage.class);

	@Test
	void everyExamplePageHasATitle() throws Exception
	{
		List<Class<? extends WicketExamplePage>> pages = examplePages();
		assertFalse(pages.isEmpty(), "no example pages found");
		Set<Class<?>> basePages = pages.stream()
			.map(Class::getSuperclass)
			.collect(Collectors.toSet());

		List<String> missing = pages.stream()
			.filter(page -> basePages.contains(page) == false)
			.filter(page -> hasTitle(page) == false)
			.map(Class::getName)
			.toList();

		assertEquals(List.of(), missing);
	}

	@Test
	void everyWizardHasATitle()
	{
		List<String> missing = Stream
			.of(StaticWizard.class, StaticWizardWithPanels.class, NewUserWizard.class)
			.filter(wizard -> hasTitle(wizard) == false)
			.map(Class::getName)
			.toList();

		assertEquals(List.of(), missing);
	}

	private static boolean hasTitle(Class<?> example)
	{
		return WicketExamplePage.string(example, "title", Locale.ENGLISH, null, null) != null;
	}

	private static List<Class<? extends WicketExamplePage>> examplePages()
		throws IOException, URISyntaxException
	{
		Path root = Path.of(WicketExamplePage.class.getResource("WicketExamplePage.class").toURI())
			.getParent();
		try (Stream<Path> files = Files.walk(root))
		{
			return files.map(root::relativize)
				.map(Path::toString)
				.filter(file -> file.endsWith(".class"))
				.map(file -> WicketExamplePage.class.getPackageName() + '.' +
					file.substring(0, file.length() - ".class".length()).replace('/', '.'))
				.<Class<?>> map(ExampleTitlesTest::load)
				.filter(WicketExamplePage.class::isAssignableFrom)
				.filter(type -> type != WicketExamplePage.class)
				.filter(type -> Modifier.isAbstract(type.getModifiers()) == false)
				.filter(type -> type.isAnonymousClass() == false)
				.filter(type -> UNTITLED.contains(type) == false)
				.<Class<? extends WicketExamplePage>> map(
					type -> type.asSubclass(WicketExamplePage.class))
				.toList();
		}
	}

	private static Class<?> load(String name)
	{
		try
		{
			return Class.forName(name, false, ExampleTitlesTest.class.getClassLoader());
		}
		catch (ClassNotFoundException e)
		{
			throw new IllegalStateException(e);
		}
	}
}
