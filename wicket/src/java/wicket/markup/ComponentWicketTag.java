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
package wicket.markup;



/**
 * ComponentWicketTag extends ComponentTag and will be createtd by a
 * MarkupParser whenever it parses a tag with namespace 'wicket'.<p>
 * 
 * Note 1: you need to add a XHTML doctype to your markup and use
 * &lt;html xmlns:wicket&gt; to create a XHTML conformant namespace
 * for such tags.<p>
 * 
 * Note 2: The tag's name is configurable through ApplicationSettings.
 * 
 * @author Juergen Donnerstag
 */
public final class ComponentWicketTag extends ComponentTag
{ // TODO finalize javadoc
    /**
     * Standard wicket tag name always available for components regardless of
     * user ApplicationSettings; value == 'wicket'.
     */
    public static final String WICKET_TAG_NAME = "wicket";

    /** Used to create unique anonymous component names */
    public static int autoIndex = 0;
    
    /**
     * Constructor
     */
    public ComponentWicketTag()
    {
        super();
    }

    /**
     * 
     * @return true, if tag name equals wicket:param
     */
    public final boolean isParamTag()
    {
        return "param".equalsIgnoreCase(getName());
    }

    /**
     * 
     * @return true, if &lt;wicket:remove&gt;
     */
    public final boolean isRemoveTag()
    {
        return "remove".equalsIgnoreCase(getName());
    }

    /**
     * 
     * @return true, if tag name equals wicket:component
     */
    public final boolean isComponentTag()
    {
        return "component".equalsIgnoreCase(getName());
    }

    /**
     * Get wicket's component name attribute: e.g. 
     * &lt;wicket:component name="myComponent"&gt;
     * 
     * @return name attribute
     */
    public final String getNameAttribute()
    {
        return this.getAttributes().getString("name");
    }
}

///////////////////////////////// End of File /////////////////////////////////
