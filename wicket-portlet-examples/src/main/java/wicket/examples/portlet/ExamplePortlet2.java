package wicket.examples.portlet;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.util.Random;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Page;
import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.markup.html.AjaxLink;
import wicket.markup.html.basic.Label;
import wicket.markup.html.image.Image;
import wicket.markup.html.image.resource.RenderedDynamicImageResource;
import wicket.markup.html.link.Link;
import wicket.model.PropertyModel;
import wicket.protocol.http.portlet.PortletPage;

/**
 * @author Janne Hietam&auml;ki
 */
public class ExamplePortlet2 extends PortletPage
{
	private static final Log log = LogFactory.getLog(ExamplePortlet.class);

	long counter = 0;

	/**
	 * @return The counter
	 */
	public long getCounter()
	{
		return counter;
	}

	/**
	 * @param page
	 */
	public ExamplePortlet2(final Page page)
	{
		add(new Link("link")
		{
			public void onClick()
			{
				setResponsePage(page);
			}
		});
		add(new Label("windowState", new PropertyModel(this, "windowState")));
		add(new Label("portletMode", new PropertyModel(this, "portletMode")));
		add(new Image("image", new RenderedDynamicImageResource(100, 100)
		{
			protected boolean render(Graphics2D graphics)
			{
				// Compute random size for circle
				final Random random = new Random();
				int dx = Math.abs(10 + random.nextInt(80));
				int dy = Math.abs(10 + random.nextInt(80));
				int x = Math.abs(random.nextInt(100 - dx));
				int y = Math.abs(random.nextInt(100 - dy));

				// Draw circle with thick stroke width
				graphics.setStroke(new BasicStroke(5));
				graphics.drawOval(x, y, dx, dy);
				return true;
			}
		}));

		final Label counterValue = new Label("counter",
				new PropertyModel(this, "counter"));
		counterValue.setOutputMarkupId(true);
		add(counterValue);
		add(new AjaxLink("counterLink")
		{
			@Override
			public void onClick(AjaxRequestTarget target)
			{
				counter++;
				target.addComponent(counterValue);
			}
		});
	}

	protected void onSetWindowState(WindowState state)
	{
		log.info("Window state changed to " + state);
	}

	protected void onSetPortletMode(PortletMode mode)
	{
		log.info("Portlet mode changed to " + mode);
	}
}
