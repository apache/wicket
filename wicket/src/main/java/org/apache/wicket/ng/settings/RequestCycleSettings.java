package org.apache.wicket.ng.settings;

public interface RequestCycleSettings
{
	public enum RenderStrategy
	{
		ONE_PASS_RENDER,
		REDIRECT_TO_BUFFER,
		REDIRECT_TO_RENDER
	};
	
	void setRenderStrategy(RenderStrategy renderStrategy);
	
	RenderStrategy getRenderStrategy();
	
	String getResponseRequestEncoding();
	
	void setResponseRequestEncoding(final String responseRequestEncoding);
}
