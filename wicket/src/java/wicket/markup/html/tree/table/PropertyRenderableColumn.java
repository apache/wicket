package wicket.markup.html.tree.table;

import java.util.Locale;

import javax.swing.tree.TreeNode;

import wicket.Session;
import wicket.util.convert.IConverter;
import wicket.util.lang.PropertyResolver;

/**
 * Lightweight column that uses a property expression to get the value
 * from the node.
 * 
 * @author Matej Knopp
 */
public class PropertyRenderableColumn extends AbstractRenderableColumn 
{
	private String propertyExpression;
	private IConverter converter;
	private Locale locale;

	/**
	 * Creates the columns.
	 * 
	 * @param location
	 *			Specifies how the column should be aligned and what his size should be 			
	 * 
	 * @param header
	 * 			Header caption
	 * 
	 * @param propertyExpression
	 * 			Expression for property access
	 */
	public PropertyRenderableColumn(ColumnLocation location, String header, String propertyExpression) 
	{
		super(location, header);
		this.propertyExpression = propertyExpression;
	}

	/**
	 * @see AbstractRenderableColumn#getNodeValue(TreeNode)
	 */
	@Override
	public String getNodeValue(TreeNode node) 
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
			return result != null ? result.toString() : "";
		}
	}
		
	/**
	 * Returns the converter or null if no converter is specified.
	 */
	public IConverter getConverter() 
	{
		return converter;
	} 

	/**
	 * By default the property is converted to string using <code>toString</code> method.
	 * If you want to alter this behavior, you can specify a custom converter. 
	 */
	public void setConverter(IConverter converter) 
	{
		this.converter = converter;
	}
	
	/**
	 * Returns the locale or null if no locale is specified.
	 */
	public Locale getLocale() 
	{
		return locale;
	}

	/**
	 * Sets the locale to be used as parameter for custom converter (if one is specified).
	 * If no locale is set, session locale is used.
	 */
	public void setLocale(Locale locale) 
	{
		this.locale = locale;
	}
	
	/**
	 * Returns the property epression.
	 */
	protected String getPropertyExpression() 
	{
		return propertyExpression;
	}
}
