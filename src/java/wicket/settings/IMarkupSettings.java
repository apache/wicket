package wicket.settings;

import wicket.Localizer;
import wicket.markup.MarkupParserFactory;
import wicket.markup.IMarkupParserFactory;
import wicket.model.IModel;

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
 * <p>
 * <i>A Localizer </i> The getLocalizer() method returns an object encapsulating
 * all of the functionality required to access localized resources. For many
 * localization problems, even this will not be required, as there are
 * convenience methods available to all components:
 * {@link wicket.Component#getString(String key)} and
 * {@link wicket.Component#getString(String key, IModel model)}.
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
	 * @return Returns the compressWhitespace.
	 * @see Settings#setCompressWhitespace(boolean)
	 */
	boolean getCompressWhitespace();

	/**
	 * Set default encoding for markup files. If null, the encoding provided by
	 * the operating system will be used.
	 * 
	 * @since 1.1
	 * @param encoding
	 */
	void setDefaultMarkupEncoding(final String encoding);

	/**
	 * @since 1.1
	 * @return Returns default encoding of markup files. If null, the operating
	 *         system provided encoding will be used.
	 */
	String getDefaultMarkupEncoding();

	/**
	 * @return Returns the defaultAfterDisabledLink.
	 */
	String getDefaultAfterDisabledLink();

	/**
	 * @param defaultAfterDisabledLink
	 *            The defaultAfterDisabledLink to set.
	 */
	void setDefaultAfterDisabledLink(String defaultAfterDisabledLink);

	/**
	 * @return Returns the defaultBeforeDisabledLink.
	 */
	String getDefaultBeforeDisabledLink();

	/**
	 * @param defaultBeforeDisabledLink
	 *            The defaultBeforeDisabledLink to set.
	 */
	void setDefaultBeforeDisabledLink(String defaultBeforeDisabledLink);

	/**
	 * @return Returns the stripComments.
	 * @see Settings#setStripComments(boolean)
	 */
	boolean getStripComments();

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
	 * 
	 * @since 1.1
	 * @param strip
	 *            if true, xml declaration will be stripped from output
	 */
	void setStripXmlDeclarationFromOutput(final boolean strip);

	/**
	 * Get the application's localizer.
	 * 
	 * @see wicket.settings.Settings#addStringResourceLoader(wicket.resource.loader.IStringResourceLoader)
	 *      for means of extending the way Wicket resolves keys to localized
	 *      messages.
	 * 
	 * @return The application wide localizer instance
	 */
	Localizer getLocalizer();

	/**
	 * Sets the markup parser factory that will be used to generate parsers for
	 * markup. By default {@link MarkupParserFactory} will be used.
	 * 
	 * @param factory
	 *            new factory
	 */
	void setMarkupParserFactory(IMarkupParserFactory factory);

	/**
	 * @return markup parser factory
	 */
	IMarkupParserFactory getMarkupParserFactory();
}
