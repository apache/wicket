package org.apache.wicket.settings;

import org.apache.wicket.ajax.strategies.IAjaxStrategy;

/**
 *
 */
public interface IAjaxSettings
{
	IAjaxStrategy getAjaxStrategy();

	void setAjaxStrategy(IAjaxStrategy ajaxStrategy);
}
