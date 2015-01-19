package org.apache.wicket.bean.validation;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author alexander.v.morozov
 */
public class TestValidatableBean implements Serializable
{

    @NotNull
    private String text;

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }

}
