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

package navomatic;

import com.voicetribe.wicket.ApplicationSettings;
import com.voicetribe.wicket.WebApplication;

/**
 * HttpApplication class for hello world example.
 * @author Jonathan Locke
 */
public class NavomaticApplication extends WebApplication
{
    /**
     * Constructor.
     */
    public NavomaticApplication()
    {
        ApplicationSettings settings = getSettings();
        settings.setHomePage(Page1.class);
        // settings.getSourcePath().add(new Folder("w:/src/java"))
        //                         .add(new Folder("w:/examples/Navomatic/src"));
        settings.setUnexpectedExceptionDisplay(ApplicationSettings.SHOW_NO_EXCEPTION_PAGE);
    }
}

///////////////////////////////// End of File /////////////////////////////////
