///////////////////////////////////////////////////////////////////////////////////
//
// Created Jun 17, 2004
//fo
//

package navomatic;

import com.voicetribe.wicket.markup.html.border.Border;
import com.voicetribe.wicket.markup.html.border.BoxBorder;

/**
 * Border component.
 * @author Jonathan Locke
 */
public class NavomaticBorder extends Border
{
    /**
     * Constructor
     * @param componentName The name of this component
     */
    public NavomaticBorder(final String componentName)
    {
        super(componentName);
        add(new BoxBorder("boxBorder"));
        add(new BoxBorder("boxBorder2"));
    }
}

///////////////////////////////// End of File /////////////////////////////////
