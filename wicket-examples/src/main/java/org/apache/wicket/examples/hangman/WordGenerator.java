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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.util.io.Streams;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.core.util.resource.locator.ResourceStreamLocator;


/**
 * The word generator is responsible for reading in a list of words from a data file and serving
 * them up in a random order. The generator keeps a state record of which words it has served and
 * randomises them again when the last word has been served.
 * 
 * @author Chris Turner
 * @version 1.0
 */
public class WordGenerator implements IClusterable
{
	/** List of words */
	private final List<String> words;

	/** Index into words */
	private int index;

	/**
	 * Create the word generator, loading the words and preparing them for serving.
	 */
	public WordGenerator()
	{
		try
		{
			final IResourceStream resource = new ResourceStreamLocator().locate(WordGenerator.class,
				"org/apache/wicket/examples/hangman/WordList", null, null, Locale.getDefault(),
				".txt", false);
			final String wordlist = Streams.readString(resource.getInputStream());
			words = Arrays.asList(wordlist.split("\\s+"));
			shuffle();
		}
		catch (IOException e)
		{
			throw new RuntimeException("Couldn't read word list");
		}
		catch (ResourceStreamNotFoundException e)
		{
			throw new RuntimeException("Couldn't read word list");
		}
	}

	/**
	 * Create the word generator using the supplied array of words as the word source to use.
	 * 
	 * @param words
	 *            The words to use
	 */
	public WordGenerator(final String[] words)
	{
		this.words = Arrays.asList(words);
		shuffle();
	}

	/**
	 * Returns the next word from the word generator.
	 * 
	 * @return The next word
	 */
	public Word next()
	{
		if (index == words.size())
		{
			shuffle();
		}
		return new Word(words.get(index++));
	}

	/**
	 * Get the number of words that were discovered.
	 * 
	 * @return The number of words
	 */
	public int size()
	{
		return words.size();
	}

	/**
	 * Randomizes the list of loaded words and sets the index back to the beginning of the word
	 * list.
	 */
	private void shuffle()
	{
		Collections.shuffle(words);
		index = 0;
	}
}
