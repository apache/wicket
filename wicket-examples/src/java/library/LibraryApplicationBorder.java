///////////////////////////////////////////////////////////////////////////////////
//
// Created Jun 17, 2004
//fo
//

package library;

import com.voicetribe.wicket.markup.html.border.Border;
import com.voicetribe.wicket.markup.html.border.BoxBorder;

/**
 * Border component.
 * @author Jonathan Locke
 */
public class LibraryApplicationBorder extends Border
{
    /**
     * Constructor
     * @param componentName The name of this component
     */
    public LibraryApplicationBorder(final String componentName)
    {
        super(componentName);
        add(new BoxBorder("boxBorder"));
    }
}

///////////////////////////////// End of File /////////////////////////////////
