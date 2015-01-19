package org.apache.wicket.bean.validation;

import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.IPropertyReflectionAwareModel;
import org.apache.wicket.model.IWrapModel;

/**
 * @author alexander.v.morozov
 */
final class ValidationModelResolver
{

    /**
     * Lookup for property-aware model, attached to certain form component.
     *
     * @param component
     *              form component
     *
     * @return property-aware model, extracted from supplied component or <code>null</code>
     */
    public static IPropertyReflectionAwareModel<?> resolvePropertyModelFrom(FormComponent<?> component)
    {
        IModel<?> model = component.getModel();
        while (true)
        {
            if (model == null)
            {
                return null;
            }
            if (model instanceof IPropertyReflectionAwareModel)
            {
                return (IPropertyReflectionAwareModel<?>) model;
            }
            if (model instanceof IWrapModel<?>)
            {
                model = ((IWrapModel<?>)model).getWrappedModel();
                continue;
            }
            break; // not model found
        }
        return null;
    }

    private ValidationModelResolver()
    {
        // nop
    }

}
