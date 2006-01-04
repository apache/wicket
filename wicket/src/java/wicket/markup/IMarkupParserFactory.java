package wicket.markup;

/**
 * Factory used to generate MarkupParser objects
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public interface IMarkupParserFactory
{
	/**
	 * @return new instance of {@link MarkupParser}
	 */
	MarkupParser newMarkupParser();
}
