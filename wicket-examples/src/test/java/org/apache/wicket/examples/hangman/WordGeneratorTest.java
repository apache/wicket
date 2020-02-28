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
package org.apache.wicket.examples.hangman;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test case for the <code>WordGenerator</code> class.
 * 
 * @author Chris Turner
 * @version 1.0
 */
public class WordGeneratorTest extends WicketTestCase
{
	private static final Logger log = LoggerFactory.getLogger(WordGeneratorTest.class);

	/**
	 * Tests word generator
	 *
     */
	@Test
	public void testWordGenerator() {
		WordGenerator wg = new WordGenerator();
		int wordCount = wg.size();
		Set<Word> words = new HashSet<>();
		log.info("First iteration...");
		for (int i = 0; i < wordCount; i++)
		{
			Word word = wg.next();
			log.info("Word found: " + word);
			Assertions.assertFalse(words.contains(word), "Word should not be returned twice");
			words.add(word);
		}
		log.info("Second iteration...");
		for (int i = 0; i < wordCount; i++)
		{
			Word word = wg.next();
			log.info("Word found: " + word);
			assertTrue(words.remove(word), "Word " + word + " should have been returned only once");
		}
		assertTrue(words.isEmpty(), "All words should have been returned twice");
	}

	/**
	 * Tests word generator
	 *
	 */
	@Test
	public void testSuppliedWordConstructor() {
		WordGenerator wg = new WordGenerator(new String[] { "testing" });
		assertEquals("testing", wg.next().asString(), "Word should be as expected");
	}
}
