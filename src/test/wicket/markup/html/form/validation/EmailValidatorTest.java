package wicket.markup.html.form.validation;

import junit.framework.TestCase;

/**
 * Tests a couple of valid and invalid email patterns.
 * @author Maurice Marrink
 */
public class EmailValidatorTest extends TestCase
{
	/**
	 * Constructor.
	 */
	public EmailValidatorTest()
	{
		super();
	}

	/**
	 * Constructor.
	 * @param name
	 */
	public EmailValidatorTest(String name)
	{
		super(name);
	}

	/**
	 * Tests a couple of emails that should be valid.
	 */
	public void testValidEmails()
	{
		EmailAddressPatternValidator test = new EmailAddressPatternValidator();
		String[] emails = new String[] { "b.blaat@topicus.nl", "blaat@hotmail.com",
				"1.2.3.4@5.6.7.nl", "m@m.nl" };
		for (int i = 0; i < emails.length; i++)
			assertTrue(emails[i] + " should be valid", test.getPattern().matcher(emails[i])
					.matches());
	}

	/**
	 * Tests a couple of emails that should not be valid.
	 */
	public void testInvalidEmails()
	{
		EmailAddressPatternValidator test = new EmailAddressPatternValidator();
		String[] emails = new String[] { ".blaat@topicus.nl", "blaat.@hotmail.com", "blaat@nl",
				"blaat@.nl" };
		for (int i = 0; i < emails.length; i++)
			assertFalse(emails[i] + " should not be valid", test.getPattern().matcher(emails[i])
					.matches());
	}
}