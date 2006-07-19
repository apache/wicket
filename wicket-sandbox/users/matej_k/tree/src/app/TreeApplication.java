package app;

import wicket.Page;
import wicket.protocol.http.WebApplication;

public class TreeApplication extends WebApplication {

	@Override
	protected void init() {
		getAjaxSettings().setAjaxDebugModeEnabled(false);
		getPageSettings().setAutomaticMultiWindowSupport(false);
	}
		
	@Override
	public Class<? extends Page> getHomePage() {
		return HomePage.class;
	}

}
