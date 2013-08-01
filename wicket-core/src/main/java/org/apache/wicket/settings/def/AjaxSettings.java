package org.apache.wicket.settings.def;

import org.apache.wicket.ajax.strategies.IAjaxStrategy;
import org.apache.wicket.ajax.strategies.Wicket6AjaxStrategy;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.settings.IAjaxSettings;
import org.apache.wicket.util.lang.Args;

/**
 *
 */
public class AjaxSettings implements IAjaxSettings
{
	private IAjaxStrategy ajaxStrategy = new Wicket6AjaxStrategy();

	public AjaxSettings(WebApplication application)
	{
	}

	@Override
	public IAjaxStrategy getAjaxStrategy()
	{
		return ajaxStrategy;
	}

	@Override
	public void setAjaxStrategy(IAjaxStrategy ajaxStrategy)
	{
		this.ajaxStrategy = Args.notNull(ajaxStrategy, "ajaxStrategy");
	}
}
