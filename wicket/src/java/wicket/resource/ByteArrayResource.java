/**
 * 
 */
package wicket.resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import wicket.markup.html.WebResource;
import wicket.util.resource.IResourceStream;
import wicket.util.resource.ResourceStreamNotFoundException;
import wicket.util.time.Time;

/**
 * @author jcompagner
 *
 */
public class ByteArrayResource extends WebResource
{
	private final String contentType;
	private final byte[] array;
	private final Locale locale;
	private final Time lastModified = Time.now();

	/**
	 * @param contentType
	 * @param array
	 */
	public ByteArrayResource(String contentType, byte[] array)
	{
		this.contentType = contentType;
		this.array = array;
		this.locale = null;
	}

	/**
	 * @param contentType
	 * @param array
	 * @param locale 
	 */
	public ByteArrayResource(String contentType, byte[] array, Locale locale)
	{
		this.contentType = contentType;
		this.array = array;
		this.locale = locale;
	}

	/**
	 * @see wicket.Resource#getResourceStream()
	 */
	public IResourceStream getResourceStream()
	{
		return new IResourceStream()
		{
			/** Transient input stream to resource */
			private transient InputStream inputStream = null;
			
			/**
			 * @see wicket.util.resource.IResourceStream#close()
			 */
			public void close() throws IOException
			{
				if (inputStream != null)
				{
					inputStream.close();
					inputStream = null;
				}
			}
	
			/**
			 * @see wicket.util.resource.IResourceStream#getContentType()
			 */
			public String getContentType()
			{
				return contentType;
			}
	
			/**
			 * @see wicket.util.resource.IResourceStream#getInputStream()
			 */
			public InputStream getInputStream() throws ResourceStreamNotFoundException
			{
				if (inputStream == null)
					inputStream = new ByteArrayInputStream(array);

				return inputStream;
			}
	
			/**
			 * @see wicket.util.watch.IModifiable#lastModifiedTime()
			 */
			public Time lastModifiedTime()
			{
				return lastModified;
			}
	
			public long length()
			{
				return array.length;
			}
			
			public Locale getLocale()
			{
				return locale;
			}
			
			public void setLocale(Locale locale)
			{
				// ignore  
			}
		};
	}
}
