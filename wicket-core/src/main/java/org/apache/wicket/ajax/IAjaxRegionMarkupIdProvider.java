/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.ajax;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.TextField;

/**
 * A mixin that allows behaviors and components to override the id of the markup region that will be
 * updated via ajax. If this mixin is not used then {@link Component#getMarkupId()} is used.
 * <p>
 * This mixin is useful when behaviors write directly to the response. Lets examine a simple
 * behavior that wraps the component in a paragraph tag:
 * 
 * <pre>
 * class PB extends Behavior
 * {
 * 	public void beforeRender(Component c)
 * 	{
 * 		c.getResponse().write(&quot;&lt;p&gt;&quot;);
 * 	}
 * 
 * 	public void afterRender(Component c)
 * 	{
 * 		c.getResponse().write(&quot;&lt;/p&gt;&quot;);
 * 	}
 * }
 * </pre>
 * 
 * If we add this behavior to a {@link TextField} the generated markup will be:
 * 
 * <pre>
 * &lt;p&gt;&lt;input wicket:id="name" type="text"&gt;&lt;/p&gt;
 * </pre>
 * 
 * If we then update this {@link TextField} via ajax the generated markup will erroneously be:
 * 
 * <pre>
 * &lt;p&gt;&lt;p&gt;&lt;input wicket:id="name" type="text"&gt;&lt;/p&gt;&lt;/p&gt;
 * </pre>
 * 
 * Notice the doubling of the {@code <p>} tags. This is happening because every ajax render the
 * input field is replaced with the markup that contains the input field surrounded by paragraph
 * tags.
 * 
 * To fix this we can modify our behavior as follows:
 * 
 * <pre>
 * class PB extends Behavior implements IAjaxRegionMarkupIdProvider
 * {
 * 	public void beforeRender(Component c)
 * 	{
 * 		c.getResponse().write(&quot;&lt;p id='&quot; + c.getMarkupId() + &quot;_p'&gt;&quot;);
 * 	}
 * 
 * 	public void afterRender(Component c)
 * 	{
 * 		c.getResponse().write(&quot;&lt;/p&gt;&quot;);
 * 	}
 * 
 * 	public String getAjaxRegionMarkupId(Component component)
 * 	{
 * 		return component.getMarkupId() + &quot;_p&quot;;
 * 	}
 * }
 * </pre>
 * 
 * Now, the ajax update will properly replace the markup region that includes the paragraph tags
 * with the generated markup.
 * 
 * </p>
 * <p>
 * In the rare case that {@link Component} needs to implement this interface the {@code component}
 * argument of the {@link #getAjaxRegionMarkupId(Component)} method can be safely ignored because it
 * will be the component itself.
 * </p>
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public interface IAjaxRegionMarkupIdProvider
{
	/**
	 * @param component
	 * @return the id of the markup region that will be updated via ajax.
	 */
	String getAjaxRegionMarkupId(Component component);
}
