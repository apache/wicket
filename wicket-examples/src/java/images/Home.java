///////////////////////////////////////////////////////////////////////////////////
//
// Created Jun 13, 2004
//
// Copyright 2004, Jonathan W. Locke
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package images;

import java.awt.image.BufferedImage;

import com.voicetribe.wicket.PageParameters;
import com.voicetribe.wicket.markup.html.HtmlPage;
import com.voicetribe.wicket.markup.html.image.DynamicImage;
import com.voicetribe.wicket.markup.html.image.Image;

/**
 * Demonstrates different flavors of images.
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
        // Image as package resource
        add(new Image("image2"));
        
        // Dynamically created image
        final BufferedImage circle = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        circle.getGraphics().drawOval(0, 0, 100, 100);
        add(new DynamicImage("image3").setExtension("jpeg").setImage(circle));
    }
}

///////////////////////////////// End of File /////////////////////////////////
