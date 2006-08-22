package wicket.settings;

import wicket.MarkupFragmentFinder;
import wicket.markup.IMarkupParserFactory;
import wicket.markup.MarkupParserFactory;

/**
 * Interface for markup related settings.
 * <p>
 * <i>compressWhitespace </i> (defaults to false) - Causes pages to render with
 * redundant whitespace removed. Whitespace stripping is not HTML or JavaScript
 * savvy and can conceivably break pages, but should provide significant
 * performance improvements.
 * <p>
 * <i>stripComments </i> (defaults to false) - Set to true to strip HTML
 * comments during markup loading
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public interface IMarkupSettings
{
	/**
	 * If true, automatic link resolution is enabled. Disabled by default.
	 * 
	 * @see wicket.markup.resolver.AutoLinkResolver
	 * @see wicket.markup.parser.filter.WicketLinkTagHandler
	 * @return Returns the automaticLinking.
	 */
	boolean getAutomaticLinking();

	/**
	 * @return Returns the compressWhitespace.
	 * @see IMarkupSettings#setCompressWhitespace(boolean)
	 */
	boolean getCompressWhitespace();

	/**
	 * @return Returns the defaultAfterDisabledLink.
	 */
	String getDefaultAfterDisabledLink();

	/**
	 * @return Returns the defaultBeforeDisabledLink.
	 */
	String getDefaultBeforeDisabledLink();

	/**
	 * @since 1.1
	 * @return Returns default encoding of markup files. If null, the operating
	 *         system provided encoding will be used.
	 */
	String getDefaultMarkupEncoding();

	/**
	 * @return markup parser factory
	 */
	IMarkupParserFactory getMarkupParserFactory();

	/**
	 * @return Returns the stripComments.
	 * @see IMarkupSettings#setStripComments(boolean)
	 */
	boolean getStripComments();

	/**
	 * Gets whether to remove wicket tags from the output.
	 * 
	 * @return whether to remove wicket tags from the output
	 */
	boolean getStripWicketTags();

	/**
	 * 
	 * @since 1.1
	 * @return if true, xml declaration will be removed.
	 */
	boolean getStripXmlDeclarationFromOutput();

	/**
	 * Application default for automatic link resolution. Please
	 * 
	 * @see wicket.markup.resolver.AutoLinkResolver and
	 * @see wicket.markup.parser.filter.WicketLinkTagHandler for more details.
	 * 
	 * @param automaticLinking
	 *            The automaticLinking to set.
	 */
	void setAutomaticLinking(boolean automaticLinking);

	/**
	 * Turns on whitespace compression. Multiple occurrences of space/tab
	 * characters will be compressed to a single space. Multiple line breaks
	 * newline/carriage-return will also be compressed to a single newline.
	 * <p>
	 * Compression is currently not HTML aware and so it may be possible for
	 * whitespace compression to break pages. For this reason, whitespace
	 * compression is off by default and you should test your application
	 * throroughly after turning whitespace compression on.
	 * <p>
	 * Spaces are removed from markup at markup load time and there should be no
	 * effect on page rendering speed. In fact, your pages should render faster
	 * with whitespace compression enabled.
	 * 
	 * @param compressWhitespace
	 *            The compressWhitespace to set.
	 */
	void setCompressWhitespace(final boolean compressWhitespace);

	/**
	 * @param defaultAfterDisabledLink
	 *            The defaultAfterDisabledLink to set.
	 */
	void setDefaultAfterDisabledLink(String defaultAfterDisabledLink);

	/**
	 * @param defaultBeforeDisabledLink
	 *            The defaultBeforeDisabledLink to set.
	 */
	void setDefaultBeforeDisabledLink(String defaultBeforeDisabledLink);

	/**
	 * Set default encoding for markup files. If null, the encoding provided by
	 * the operating system will be used.
	 * 
	 * @since 1.1
	 * @param encoding
	 */
	void setDefaultMarkupEncoding(final String encoding);

	/**
	 * Sets the markup parser factory that will be used to generate parsers for
	 * markup. By default {@link MarkupParserFactory} will be used.
	 * 
	 * @param factory
	 *            new factory
	 */
	void setMarkupParserFactory(IMarkupParserFactory factory);

	/**
	 * Enables stripping of markup comments denoted in markup by HTML comment
	 * tagging.
	 * 
	 * @param stripComments
	 *            True to strip markup comments from rendered pages
	 */
	void setStripComments(boolean stripComments);

	/**
	 * Sets whether to remove wicket tags from the output.
	 * 
	 * @param stripWicketTags
	 *            whether to remove wicket tags from the output
	 */
	void setStripWicketTags(boolean stripWicketTags);

	/**
	 * 
	 * @since 1.1
	 * @param strip
	 *            if true, xml declaration will be stripped from output
	 */
	void setStripXmlDeclarationFromOutput(final boolean strip);

	/**
	 * @since 2.0
	 * @return The MarkupFragmentFinder to be used by the application
	 */
	MarkupFragmentFinder getMarkupFragmentFinder();

	/**
	 * @since 2.0
	 * @param markupFragmentFinder The MarkupFragmentFinder to be used by the application
	 */
	void setMarkupFragmentFinder(MarkupFragmentFinder markupFragmentFinder);
}
