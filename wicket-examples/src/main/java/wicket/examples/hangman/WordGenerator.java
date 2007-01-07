/*
 * $Id: WordGenerator.java 5394 2006-04-16 13:36:52 +0000 (Sun, 16 Apr 2006)
 * jdonnerstag $ $Revision$ $Date: 2006-04-16 13:36:52 +0000 (Sun, 16 Apr
 * 2006) $
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.examples.hangman;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import wicket.Application;
import wicket.util.io.Streams;
import wicket.util.resource.IResourceStream;
import wicket.util.resource.ResourceStreamNotFoundException;

/**
 * The word generator is responsible for reading in a list of words from a data
 * file and serving them up in a random order. The generator keeps a state
 * record of which words it has served and randomises them again when the last
 * word has been served.
 * 
 * @author Chris Turner
 * @version 1.0
 */
public class WordGenerator implements Serializable
{
	/** List of words */
	private final List words;

	/** Index into words */
	private int index;

	/**
	 * Create the word generator, loading the words and preparing them for
	 * serving.
	 */
	public WordGenerator()
	{
		try
		{
			final IResourceStream resource = Application.get().getResourceSettings().getResourceStreamFactory().locate(this.getClass(), 
					"wicket/examples/hangman/WordList", null, Locale.getDefault(), ".txt");
			final String wordlist = Streams.readString(resource.getInputStream());
			this.words = Arrays.asList(wordlist.split("\\s+"));
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
	 * Create the word generator using the supplied array of words as the word
	 * source to use.
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
		return new Word((String)words.get(index++));
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
	 * Randomises the list of loaded words and sets the index back to the
	 * beginning of the word list.
	 */
	private void shuffle()
	{
		Collections.shuffle(words);
		index = 0;
	}
}
