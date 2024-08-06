package org.apache.wicket.core.request.handler;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.Test;

class ListenerInvocationNotAllowedExceptionTest
{
	@Test
	void smokeTest()
	{
		var tester = new WicketTester(new MockApplication());
		// Arrange
		var label = new Label("id", "Label");
		var cut = new ListenerInvocationNotAllowedException(label,
			new AttributeModifier("class", "test"), "no no no");
		// Act

		//Assert
		assertThat(cut.getMessage()).startsWith("no no noComponent: [Component id = id], Path: id, Behavior:");
	}

}