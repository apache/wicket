package wicket.threadtest.apps.app1;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import wicket.WicketRuntimeException;
import wicket.markup.html.WebPage;
import wicket.markup.html.WebResource;
import wicket.markup.html.image.Image;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.util.resource.IResourceStream;
import wicket.util.resource.ResourceStreamNotFoundException;
import wicket.util.time.Time;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * Web page with 50 dynamically-created image resources.
 * 
 * @author almaw
 */
public class ResourceTestPage extends WebPage {
	
	public static final int IMAGES_PER_PAGE = 20;
	
	public ResourceTestPage() {
		List list = Arrays.asList(new Object[IMAGES_PER_PAGE]);
		add(new ListView("listView", list) {
		
			@Override
			protected void populateItem(ListItem item) {
				final Random random = new Random();
				BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
				Graphics gfx = image.getGraphics();
				gfx.setColor(new Color(random.nextFloat(), random.nextFloat(), random.nextFloat()));
				gfx.fillRect(0, 0, 32, 32);
				gfx.dispose();
				
				// Write it into a byte array as a JPEG.
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
                JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(baos);
                try {
                    encoder.encode(image);
                }
				catch (IOException e) {
					throw new WicketRuntimeException(e);
				}
                final byte[] imageData = baos.toByteArray();
                
				item.add(new Image("image", new WebResource() {
				
					@Override
					public IResourceStream getResourceStream() {
						return new IResourceStream() {
						
							public Time lastModifiedTime() {
								return Time.now();
							}
						
							public void setLocale(Locale locale) {
							}
						
							public long length() {
								return imageData.length;
							}
						
							public Locale getLocale() {
								return null;
							}
						
							// Make a 16x16 randomly background-coloured JPEG.
							public InputStream getInputStream() throws ResourceStreamNotFoundException {
								return new ByteArrayInputStream(imageData);
							}
						
							public String getContentType() {
								return "image/jpeg";
							}
						
							public void close() throws IOException {
							}
						
						};
					}
				
				}));
			}
		
		});
	}
}
