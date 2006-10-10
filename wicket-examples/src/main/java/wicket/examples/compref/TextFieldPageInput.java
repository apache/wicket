package wicket.examples.compref;

import java.io.Serializable;

/** Simple data class that acts as a model for the input fields. */
public class TextFieldPageInput implements Serializable
{
	// Normally we would have played nice and made it a proper JavaBean with
	// getters and
	// setters for its properties. But this is an example which we like to
	// keep small.

	/** some plain text. */
	public String text = "some text";

	/** an integer. */
	public Integer integer = new Integer(12);

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "text = '" + text + "', integer = '" + integer + "'";
	}
}