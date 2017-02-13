package org.apache.wicket.core.util.lang;

import org.apache.wicket.core.util.reflection.ObjectWithGetAndSet;

/**
 * Resolves property expression to workable {@link ObjectWithGetAndSet}
 * 
 * @author pedro
 */
public interface IPropertyExpressionResolver
{

	/**
	 * @param expression
	 * @param object
	 *            Optional, but will enable the resolver to find subclasses in polymorphic types
	 * @param clz
	 * @return {@link ObjectWithGetAndSet}
	 */
	ObjectWithGetAndSet resolve(String expression, Object object, Class<? extends Object> clz);

	/**
	 * Creates a new value to each null property in the way to the expressed property. So the
	 * property expression: "attr1.attr2.attr3" would have "attr1" and "attr2" tested for null, in
	 * which case they would get new constructed values to guarantee a place to set the new value at
	 * "attr3"
	 * 
	 * @param expression
	 * @param object
	 * @param value
	 * @param prc
	 */
	// TODO find a better name, multiple values are being setting here
	// TODO if possible, to move to PropertyExpression
	void setValue(String expression, Object object, Object value, PropertyResolverConverter prc);

}
