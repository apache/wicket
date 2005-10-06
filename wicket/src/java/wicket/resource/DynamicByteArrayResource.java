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
import wicket.util.time.Duration;
import wicket.util.time.Time;

/**
 * @author jcompagner
 *
 */
public abstract class DynamicByteArrayResource extends WebResource
{
	/** The time this image resource was last modified */
	private Time lastModifiedTime;

	/** The maximum duration a resource can be idle before its cache is flushed */
	private Duration cacheTimeout = Duration.NONE;

	private Locale locale;
	
	/**
	 * defaut constructor
	 */
	public DynamicByteArrayResource()
	{
	}

	/**
	 * @param locale  The Locale for which this resource is meant for.
	 */
	public DynamicByteArrayResource(Locale locale)
	{
		this.locale = locale;
	}
	
	/**
	 * @return Gets the image resource to attach to the component.
	 */
	public IResourceStream getResourceStream()
	{
		return new IResourceStream()
		{
			private static final long serialVersionUID = 1L;

			/** Transient input stream to resource */
			private transient InputStream inputStream = null;
			
			/** Transient byte array of the resources, will always be deleted in the close*/
			private transient byte[] data = null; 

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
				data = null;
			}

			/**
			 * @see wicket.util.resource.IResourceStream#getContentType()
			 */
			public String getContentType()
			{
				checkLoadData();
				return DynamicByteArrayResource.this.getContentType();
			}

			/**
			 * @see wicket.util.resource.IResourceStream#getInputStream()
			 */
			public InputStream getInputStream() throws ResourceStreamNotFoundException
			{
				checkLoadData();
				if (inputStream == null)
				{
					inputStream = new ByteArrayInputStream(data);
				}
				return inputStream;
			}

			/**
			 * @see wicket.util.watch.IModifiable#lastModifiedTime()
			 */
			public Time lastModifiedTime()
			{
				checkLoadData();
				return DynamicByteArrayResource.this.lastModifiedTime();
			}

			/**
			 * @see wicket.util.resource.IResourceStream#length()
			 */
			public long length()
			{
				checkLoadData();
				return (data != null) ? data.length : 0;
			}

			/**
			 * @see wicket.util.resource.IResourceStream#getLocale()
			 */
			public Locale getLocale()
			{
				return locale;
			}

			/**
			 * @see wicket.util.resource.IResourceStream#setLocale(java.util.Locale)
			 */
			public void setLocale(Locale loc)
			{
				DynamicByteArrayResource.this.locale = loc;
			}

			/**
			 * Check whether the data was loaded yet. If not, load it now.
			 */
			private void checkLoadData()
			{
				if (data == null)
				{
					data = getData();
				}
			}
		};
	}

	/**
	 * @return The last time this image resource was modified
	 */
	public final Time lastModifiedTime()
	{
		return lastModifiedTime;
	}

	/**
	 * @param lastModifiedTime 
	 */
	public final void setLastModifiedTime(Time lastModifiedTime)
	{
		this.lastModifiedTime = lastModifiedTime;
	}


	/**
	 * @return The content type of the byte array
	 */
	public abstract String getContentType();

	/**
	 * Set the maximum duration the resource can be idle before its cache is flushed.
	 * The cache might get flushed sooner if the JVM is low on memory.
	 * 
	 * @param value The cache timout 
	 */
	public final void setCacheTimeout(Duration value)
	{
		cacheTimeout = value;
	}

	/**
	 * Returns the maximum duration the resource can be idle before its cache is flushed.
	 * 
	 * @return The cache timeout 
	 */
	public final Duration getCacheTimeout()
	{
		return cacheTimeout;
	}

	/**
	 * Get byte array for our dynamic resource. If the subclass
	 * regenerates the data, it should set the lastModifiedTime when it does so.
	 * This ensures that image caching works correctly.
	 *
	 * @return The byte array for this dynamic resource.
	 */
	protected abstract byte[] getData();
}
