package wicket.markup;

/**
 * Factory used to generate MarkupParser objects
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public interface IMarkupParserFactory
{
	/**
	 * @param resource The markup resource (file)
	 * @return new instance of {@link MarkupParser}
	 */
	MarkupParser newMarkupParser(final MarkupResourceStream resource);
}
