package wicket.markup.html.tree.table;

import java.util.Locale;

import javax.swing.tree.TreeNode;

import wicket.Session;
import wicket.util.convert.IConverter;
import wicket.util.lang.PropertyResolver;

/**
 * TreeColumn class that uses a property expression to get the value from the
 * node.
 * 
 * @author Matej Knopp
 */
public class PropertyTreeColumn extends AbstractTreeColumn
{
	private static final long serialVersionUID = 1L;

	private IConverter converter;

	private Locale locale;

	private String propertyExpression;

	/**
	 * Creates the columns.
	 * 
	 * @param location
	 *            Specifies how the column should be aligned and what his size
	 *            should be
	 * 
	 * @param header
	 *            Header caption
	 * 
	 * @param propertyExpression
	 *            Expression for property access
	 */
	public PropertyTreeColumn(ColumnLocation location, String header, String propertyExpression)
	{
		super(location, header);
		this.propertyExpression = propertyExpression;
	}

	/**
	 * Returns the converter or null if no converter is specified.
	 * 
	 * @return Any converter
	 */
	public IConverter getConverter()
	{
		return converter;
	}

	/**
	 * Returns the locale or null if no locale is specified.
	 * 
	 * @return Any locale
	 */
	public Locale getLocale()
	{
		return locale;
	}

	/**
	 * @see AbstractTreeColumn#renderNode(TreeNode)
	 */
	@Override
	public String renderNode(TreeNode node)
	{
		Object result = PropertyResolver.getValue(propertyExpression, node);
		if (converter != null)
		{
			Locale locale = this.locale;
			if (locale == null)
			{
				locale = Session.get().getLocale();
			}
			return converter.convertToString(result, locale);
		}
		else
		{
			return result != null ? result.toString() : null;
		}
	}

	/**
	 * By default the property is converted to string using
	 * <code>toString</code> method. If you want to alter this behavior, you
	 * can specify a custom converter.
	 * 
	 * @param converter
	 *            Any converter
	 */
	public void setConverter(IConverter converter)
	{
		this.converter = converter;
	}

	/**
	 * Sets the locale to be used as parameter for custom converter (if one is
	 * specified). If no locale is set, session locale is used.
	 * 
	 * @param locale
	 *            The locale
	 */
	public void setLocale(Locale locale)
	{
		this.locale = locale;
	}
}
