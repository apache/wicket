package org.apache.wicket.resource;

/**
 *
 */
public interface IScopeAwareTextResourceProcessor extends ITextResourceCompressor
{
	/**
	 * Processes/manipulates a text resource.
	 *
	 *
	 * @param input
	 * @param scope
	 * @return The processed input
	 */
	public String process(String input, Class<?> scope, String name);
}
