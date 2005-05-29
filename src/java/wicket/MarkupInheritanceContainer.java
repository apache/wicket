/*
 * $Id$ $Revision$
 * $Date$
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
package wicket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.markup.ComponentTag;
import wicket.markup.DualMarkupStream;
import wicket.markup.MarkupException;
import wicket.markup.MarkupStream;
import wicket.markup.WicketTag;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.parser.XmlTag;

/**
 * THIS IS PART OF MARKUP INHERITANCE AND CURRENTLY EXPERIMENTAL ONLY.
 * <p>
 * This is a special Component (containter) automatically instanstiated by 
 * MarkupInheritanceResolver if markup contains &lt;wicket:extend&gt; and
 * automatically added to the component hierarchy. <p>
 * It is actually very similiar to the Border component with the base 
 * class' markup being the border and the subclass' markup the content
 * within the border.<p>
 * 
 * TODO It ssems to be so complicated. Does it have to be like that?
 *  
 * @author Juergen Donnerstag
 */
public class MarkupInheritanceContainer extends WebMarkupContainer implements IComponentResolver
{
    /** Logger */
    private final static Log log = LogFactory.getLog(MarkupInheritanceContainer.class);
    
	/** The open tag for this container. */
	private transient ComponentTag openTag;
	
	/** Special markup stream able to switch between the base class' markup
	 * and the class' markup.
	 */
	transient DualMarkupStream dual;

	/** true, if <wicket:child/> has been processed */
	private transient boolean resolved;
	
	/**
     * Construct.
	 */
	public MarkupInheritanceContainer()
	{
	    // Default name for the container. The component is represented by
	    // the <wicket:extend>...</wicket:extend> tag.
		super("extend");
	}
	
	/**
	 * Get the base class' markup stream
	 * 
	 * @param parent Not the inheritance container but its parent component
	 * @return Returns the markup stream set on inherited component
	 */
	private MarkupStream getInheritedMarkupStream(MarkupContainer parent)
	{
	    while (parent instanceof MarkupInheritanceContainer)
	    {
	        parent = parent.getParent();
	    }
	    
	    return getApplication().getMarkupCache().getMarkupStream(
	            parent, 
	            parent.getMarkupStream().getContainerClass().getSuperclass());
	}
	
	/**
	 * Render the body of &lt;wicket:extend&gt; First get both markups involved
	 * and switch between both if &lt;wicket:child&gt; is found in the 
	 * base class' markup.
	 * 
	 * @see wicket.Component#onComponentTagBody(wicket.markup.MarkupStream,
	 *      wicket.markup.ComponentTag)
	 */
	protected final void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
	    this.openTag = openTag;
	    this.resolved = false;

	    // Get the real component
	    final MarkupContainer parent = this.getParent();
	    
		// Get the inherited markup stream
	    final MarkupStream parentMarkupStream = getInheritedMarkupStream(parent);
		if (parentMarkupStream == null)
		{
		    throw new MarkupException("Did not find parent markup (inheritance) for Class: " + super.getClass().getName());
		}

	    if (!(parent instanceof MarkupInheritanceContainer))
	    {
	        this.dual = new DualMarkupStream(markupStream);
			
			// Important: set the parent component's markup as well
			parent.setMarkupStream(this.dual);
	    }
	    else
	    {
	        this.dual = (DualMarkupStream) parent.getMarkupStream();
	    }

		this.dual.push(parentMarkupStream);
	    this.setMarkupStream(this.dual);
		
		// go on rendering the component
	    log.debug("inherit: onComponentTagBody: " + dual.getCurrentMarkupStream().toString());
		super.onComponentTagBody(this.dual, openTag);
		this.dual.pop();
	    log.debug("inherit: back from onComponentTagBody: " + dual.getCurrentMarkupStream().toString());
	}
	
	/**
	 * Try to resolve the component name, then create a component, add it to the
	 * container and render the component.
	 * 
	 * @param container
	 *            The container parsing its markup
	 * @param markupStream
	 *            The current markupStream
	 * @param tag
	 *            The current component tag while parsing the markup
	 * @return True if componentId was handled by the resolver, false otherwise.
	 */
	public boolean resolve(final MarkupContainer container, final MarkupStream markupStream,
			final ComponentTag tag)
	{
	    // Not sure this can really happen, but onComponentTagBody() must be called first 
	    if (openTag == null)
	    {
	        return false;
	    }
	    
	    log.debug("inherit: resolve: " + markupStream.toString());
	    
	    // I'm currently only interested in <wicket:...> tags
		if (!(tag instanceof WicketTag))
        {
		    MarkupContainer parent = this.getParent();
		    while (parent instanceof MarkupInheritanceContainer)
		    {
		        parent = parent.getParent();
		    }
		    
		    // resolve inherited components
		    final String name = tag.getId();
		    final Component component = parent.get(name);
		    if (component != null)
		    {
			    log.debug("inherit: render component: " + component.getId());
		        component.render();
		        return true;
		    }
		    
            return false;
        }
		

	    // It is the wrong inheritance container (nested inheritance)
	    if (this.resolved == true)
	    {
	        return false;
	    }
	    this.resolved = true;
	    
		final WicketTag wtag = (WicketTag) tag;

        // The current markup stream must be the parent's one.
        // Thus, watch out for <wicket:child>
        if (!wtag.isChildTag())
        {
            return false;
        }

        ComponentTag bodyTag = tag;
        if (tag.isOpen())
        {
            // It is open-preview-close already.
            // Only RawMarkup is allowed within the preview region, which
            // gets stripped from output
            markupStream.next();
            markupStream.skipRawMarkup();
        }
        else if (tag.isOpenClose())
        {
            // Automatically expand <wicket:body/> to <wicket:body>...</wicket:body>
            // in order for the html to look right: insert the body in between the
            // wicket tags instead of behind the open-close tag.
            bodyTag = tag.mutable();
            bodyTag.setType(XmlTag.OPEN);
        }
        else
        {
			markupStream.throwMarkupException("A <wicket:child> tag must be an open or open-close tag.");
        }

        // render <wicket:child> tag
	    log.debug("inherit: render child tag: " + markupStream.toString());
		renderComponentTag(bodyTag);
		markupStream.next();
		
		// swap temporarily back to original markup and render the body 
		// of <wicket:extend> and switch back the to parents markup.
		this.dual.incrementMarkupStreamIndex();
	    log.debug("inherit: render body (<wicket:child>): " + dual.getCurrentMarkupStream().toString());
		super.onComponentTagBody(this.dual, openTag);
		this.dual.decrementMarkupStreamIndex();
	    log.debug("inherit: back from render </wicket:child>: " + dual.getCurrentMarkupStream().toString());
		
		// Render </wicket:child>
		if (tag.isOpenClose())
		{
		    bodyTag.setType(XmlTag.CLOSE);
			renderComponentTag(bodyTag);
		}
	    
		// Continue rendering the parent markup until its end
		return true;
	}
}
