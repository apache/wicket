/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.examples.images;


import java.awt.image.BufferedImage;

import wicket.PageParameters;
import wicket.examples.util.NavigationPanel;
import wicket.markup.html.HtmlPage;
import wicket.markup.html.image.DynamicImage;
import wicket.markup.html.image.Image;

/**
 * Demonstrates different flavors of wicket.examples.images.
 * @author Jonathan Locke
 */
public final class Home extends HtmlPage
{
    /**
     * Constructor
     * @param parameters Page parameters (ignored since this is the home page)
     */
    public Home(final PageParameters parameters)
    {
        add(new NavigationPanel("mainNavigation", "Images example"));

        // Image as package resource
        add(new Image("image2"));

        // Dynamically created image
        final BufferedImage circle = new BufferedImage(100, 100,
                BufferedImage.TYPE_INT_RGB);

        circle.getGraphics().drawOval(0, 0, 100, 100);
        add(new DynamicImage("image3").setExtension("jpeg").setImage(circle));

        add(new Image("image4", "Image2.gif"));
    }
}

///////////////////////////////// End of File /////////////////////////////////
