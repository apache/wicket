/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.markup.html.applet.sprockets.slider;

import java.io.Serializable;

/**
 * THIS CLASS IS NOT PART OF THE WICKET PUBLIC API. DO NOT ATTEMPT TO USE IT.
 * 
 * Model for Slider Sprocket's applet.
 * 
 * @author Jonathan Locke
 */
class SliderAppletModel implements Serializable
{
	int min;
	int max;
	int value;
}
