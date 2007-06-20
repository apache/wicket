// $Id: $
package org.apache.wicket.guice;

import org.apache.wicket.Component;
import org.apache.wicket.markup.MarkupStream;

import com.google.inject.Inject;

public class TestComponent extends Component
{
	private static final long serialVersionUID = 1L;

	@Inject
	private ITestService testService;
	
	public TestComponent(String id)
	{
		super(id);
	}

	@Override
	protected void onRender(MarkupStream markupStream)
	{
		// Do nothing.
	}

	public ITestService getTestService()
	{
		return testService;
	}
}
