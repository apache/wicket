// (c)2004 Templemore Technologies Limited. All Rights Reserved
// http://www.templemore.co.uk/copyright.html
package wicket.examples.hangman;

import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.TestCase;
import wicket.examples.hangman.WordGenerator;

/**
 * Test case for the <code>WordGenerator</code> class.
 *
 * @author Chris Turner
 * @version 1.0
 */
public class WordGeneratorTest extends TestCase {

    /**
     * Create the test case.
     *
     * @param message The test name
     */
    public WordGeneratorTest(String message) {
        super(message);
    }

    public void testWordGenerator() throws Exception {
        WordGenerator wg = new WordGenerator();
        int wordCount = wg.getWordCount();
        Set words = new HashSet();
        System.out.println("First iteration...");
        for ( int i = 0; i < wordCount; i++ ) {
            String word = wg.nextWord();
            System.out.println("Word found: " + word);
            Assert.assertFalse("Word should not be returned twice", words.contains(word));
            words.add(word);
        }
        System.out.println("Second iteration...");
        for ( int i = 0; i < wordCount; i++ ) {
            String word = wg.nextWord();
            System.out.println("Word found: " + word);
            Assert.assertTrue("Word should have been returned only once", words.remove(word));
        }
        Assert.assertTrue("All words should have been returned twice", words.isEmpty());
    }

    public void testSuppliedWordConstructor() throws Exception {
        WordGenerator wg = new WordGenerator(new String[] { "Testing" });
        Assert.assertEquals("Word should be as expected", "testing", wg.nextWord());
    }
}
