package org.apache.wicket.bean.validation;

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IPropertyReflectionAwareModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.tester.WicketTesterScope;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author alexander.v.morozov
 */
public class ValidationModelResolverTest
{

    @Rule
    public WicketTesterScope scope = new WicketTesterScope();

    @Test
    public void noModelBoundToComponent()
    {
        TextField<String> textField = new TextField<String>("field");
        Assert.assertNull(ValidationModelResolver.resolvePropertyModelFrom(textField));
    }

    @Test
    public void simpleModelBoundToComponent()
    {
        TextField<String> textField = new TextField<String>("text", new Model<String>());
        Assert.assertNull(ValidationModelResolver.resolvePropertyModelFrom(textField));
    }

    @Test
    public void propertyModelBoundToComponent()
    {
        TextField<String> textField = new TextField<String>("text", new PropertyModel<String>(new TestValidatableBean(), "text"));
        IPropertyReflectionAwareModel<?> model = ValidationModelResolver.resolvePropertyModelFrom(textField);
        Assert.assertNotNull(model);
        Assert.assertEquals("text", model.getPropertyField().getName());
    }

}