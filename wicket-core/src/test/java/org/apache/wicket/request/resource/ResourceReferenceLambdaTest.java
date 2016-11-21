package org.apache.wicket.request.resource;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.WicketTestCase;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Test;

public class ResourceReferenceLambdaTest extends WicketTestCase
{

	private final String output = "lambda resource";

	@Override
	protected WicketTester newWicketTester(WebApplication app) 
	{
		WicketTester wicketTester = super.newWicketTester(app);

		IResource res = (attributes) -> 
			attributes.getResponse().write(output);
		
		ResourceReference resRef = ResourceReference.of("lambdares", () -> res);
		
		app.mountResource("/test", resRef);
				
		return wicketTester;
	}
	
	@Test
	public void lambdaBasedResurceReference() throws Exception 
	{
		tester.executeUrl("./test");
		
		assertEquals(output, tester.getLastResponseAsString());
	}
}
