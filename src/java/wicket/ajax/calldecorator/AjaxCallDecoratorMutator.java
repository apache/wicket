/*
 * $Id: org.eclipse.jdt.ui.prefs,v 1.6 2006/02/06 08:27:03 ivaynberg Exp $
 * $Revision: 1.6 $ $Date: 2006/02/06 08:27:03 $
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
package wicket.ajax.calldecorator;

import wicket.ajax.IAjaxCallDecorator;

/**
 * A wrapper for an instance of {@link IAjaxCallDecorator} that provides a way
 * to easily modify existing scripts by prepending or appending new scripts
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class AjaxCallDecoratorMutator implements IAjaxCallDecorator
{
	private String preBeforeScript;
	private String postBeforeScript;

	private String preAfterScript;
	private String postAfterScript;

	private String preOnSuccessScript;
	private String postOnSuccessScript;

	private String preOnFailureScript;
	private String postOnFailureScript;

	private IAjaxCallDecorator base;

	/**
	 * Construct.
	 * 
	 * @param baseDecorator
	 */
	public AjaxCallDecoratorMutator(IAjaxCallDecorator baseDecorator)
	{
		this.base = baseDecorator;
	}

	/**
	 * @see wicket.ajax.IAjaxCallDecorator#getBeforeScript()
	 */
	public String getBeforeScript()
	{
		final String before = hasBase() ? base.getBeforeScript() : null;
		return build(preBeforeScript, before, postBeforeScript);
	}

	/**
	 * @see wicket.ajax.IAjaxCallDecorator#getAfterScript()
	 */
	public String getAfterScript()
	{
		final String after = hasBase() ? base.getAfterScript() : null;
		return build(preAfterScript, after, postAfterScript);
	}

	/**
	 * @see wicket.ajax.IAjaxCallDecorator#getOnSuccessScript()
	 */
	public String getOnSuccessScript()
	{
		final String successHandler = hasBase() ? base.getOnSuccessScript() : null;
		return build(preOnSuccessScript, successHandler, postOnSuccessScript);
	}

	/**
	 * @see wicket.ajax.IAjaxCallDecorator#getOnFailureScript()
	 */
	public String getOnFailureScript()
	{
		final String failureHandler = hasBase() ? base.getOnFailureScript() : null;
		return build(preOnFailureScript, failureHandler, postOnFailureScript);
	}

	private boolean hasBase()
	{
		return base != null;
	}

	private String build(String pre, String base, String post)
	{
		String tmp;
		if (pre != null)
		{
			tmp = pre;
		}

		if (pre == null && post == null)
		{
			return base;
		}
		else if (pre == null && post != null)
		{
			return (base == null) ? post : base + ";" + post;
		}
		else if (pre != null && post == null)
		{
			return (base == null) ? pre : pre + ";" + base;
		}
		else
		{
			return (base == null) ? (pre + ";" + post) : (pre + ";" + base + ";" + post);
		}
	}


	/**
	 * @param postAfterScript
	 * @return this for chaining
	 */
	public AjaxCallDecoratorMutator setPostAfterScript(String postAfterScript)
	{
		this.postAfterScript = postAfterScript;
		return this;
	}


	/**
	 * @param postBeforeScript
	 * @return this for chaining
	 */
	public AjaxCallDecoratorMutator setPostBeforeScript(String postBeforeScript)
	{
		this.postBeforeScript = postBeforeScript;
		return this;
	}


	/**
	 * @param postFailureHandler
	 * @return this for chaining
	 */
	public AjaxCallDecoratorMutator setPostOnFailureScript(String postFailureHandler)
	{
		this.postOnFailureScript = postFailureHandler;
		return this;
	}


	/**
	 * @param postSuccessHandler
	 * @return this for chaining
	 */
	public AjaxCallDecoratorMutator setPostOnSuccessScript(String postSuccessHandler)
	{
		this.postOnSuccessScript = postSuccessHandler;
		return this;
	}


	/**
	 * @param preAfterScript
	 * @return this for chaining
	 */
	public AjaxCallDecoratorMutator setPreAfterScript(String preAfterScript)
	{
		this.preAfterScript = preAfterScript;
		return this;
	}


	/**
	 * @param preBeforeScript
	 * @return this for chaining
	 */
	public AjaxCallDecoratorMutator setPreBeforeScript(String preBeforeScript)
	{
		this.preBeforeScript = preBeforeScript;
		return this;
	}

	/**
	 * @param preFailureHandler
	 * @return this for chaining
	 */
	public AjaxCallDecoratorMutator setPreOnFailureScript(String preFailureHandler)
	{
		this.preOnFailureScript = preFailureHandler;
		return this;
	}


	/**
	 * @param preSuccessHandler
	 * @return this for chaining
	 */
	public AjaxCallDecoratorMutator setPreOnSuccessScript(String preSuccessHandler)
	{
		this.preOnSuccessScript = preSuccessHandler;
		return this;
	}


}
