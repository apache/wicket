// (c)2004 Templemore Technologies Limited. All Rights Reserved
// http://www.templemore.co.uk/copyright.html
package wicket.examples.hangman;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

/**
 * The word generator is responsible for reading in a list of words from
 * a data file and serving them up in a random order. The generator keeps
 * a state record of which words it has served and randomises them again when
 * the last word has been served.
 *
 * @author Chris Turner
 * @version 1.0
 */
public class WordGenerator implements Serializable {

    /**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;

	private static final String WORD_LIST_RESOURCE = "wicket/examples/hangman/WordList.txt";

    private List words;
    private int index;

    /**
     * Create the word generator, loading the words and preparing them
     * for serving.
     *
     * @throws WordListException If the word list cannot be loaded
     */
    public WordGenerator() throws WordListException {
        loadWords();
        randomiseWords();
    }

    /**
     * Create the word generator using the supplied array of words
     * as the word source to use.
     *
     * @param words The words to use
     */
    public WordGenerator(final String[] words) {
        this.words = new ArrayList(words.length);
        for ( int i = 0; i < words.length; i++ ) {
            this.words.add(words[i].toLowerCase());
        }
        randomiseWords();
    }

    /**
     * Returns the next word from the word generator.
     *
     * @return The next word
     */
    public String nextWord() {
        String word = (String)words.get(index);
        index++;
        if ( index == words.size() ) randomiseWords();
        return word;
    }

    /**
     * Get the number of words that were discovered.
     *
     * @return The number of words
     */
    public int getWordCount() {
        return words.size();
    }

    /**
     * Load the words from the data file into the internal word list.
     *
     * @throws WordListException If the word list cannot be read
     */
    private void loadWords() throws WordListException {
        InputStream iStream = getClass().getClassLoader().
                getResourceAsStream(WORD_LIST_RESOURCE);
        if ( iStream == null ) {
            throw new WordListException("Unable to find word list resource: " + WORD_LIST_RESOURCE);
        }
        LineNumberReader reader = new LineNumberReader(new InputStreamReader(iStream));
        String line;
        words = new ArrayList();
        try {
            while ( (line = reader.readLine()) != null ) {
                line = line.trim().toLowerCase();
                if ( line.length() > 0 ) words.add(line);
            }
            reader.close();
        } catch (IOException e) {
            throw new WordListException("Error reading word list resource: " + WORD_LIST_RESOURCE, e);
        }
    }

    /**
     * Randomises the list of loaded words and sets the index back to the beginning
     * of the word list.
     */
    private void randomiseWords() {
        Collections.shuffle(words);
        index = 0;
    }

}
