package org.apache.wicket.bean.validation;

import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IPropertyReflectionAwareModel;

/**
 * Resolves the property to be validated for the given form component. Implementations, incuding the
 * default one, usually check the form component's model for some subclass that can provide the
 * necessary meta information to resolve the property.
 * 
 * @see DefaultPropertyResolver
 * @see IPropertyReflectionAwareModel
 * 
 * @author igor
 * 
 */
public interface IPropertyResolver
{
	Property resolveProperty(FormComponent<?> component);
}
