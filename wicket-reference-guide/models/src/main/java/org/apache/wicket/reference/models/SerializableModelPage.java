package org.apache.wicket.reference.models;

import java.util.Date;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public class SerializableModelPage extends WebPage
{
	public SerializableModelPage()
	{
		//#docu
		IModel<String> message = Model.of("any message");
		message.setObject("current time: " + new Date());
		
		add(new Label("message", message));
		//#docu
	}
}
