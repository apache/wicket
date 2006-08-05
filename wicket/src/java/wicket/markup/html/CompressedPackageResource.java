/*
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.markup.html;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.zip.GZIPOutputStream;

import wicket.Application;
import wicket.SharedResources;
import wicket.protocol.http.WebRequest;
import wicket.protocol.http.WebRequestCycle;
import wicket.protocol.http.WebResponse;
import wicket.util.io.Streams;
import wicket.util.resource.IResourceStream;
import wicket.util.resource.ResourceStreamNotFoundException;
import wicket.util.time.Time;

/**
 * Identical to PackageResource, but supports gzip compression of data
 * 
 * See {@link PackageResource} and {@link CompressedPackageResourceReference}
 * 
 * @author Janne Hietam&auml;ki
 */
public class CompressedPackageResource extends PackageResource
{

	protected CompressedPackageResource(Class scope, String path, Locale locale, String style)
	{
		super(scope, path, locale, style);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Gets the resource for a given set of criteria. Only one resource will be
	 * loaded for the same criteria.
	 * 
	 * @param scope
	 *            This argument will be used to get the class loader for loading
	 *            the package resource, and to determine what package it is in.
	 *            Typically this is the class in which you call this method
	 * @param path
	 *            The path to the resource
	 * @param locale
	 *            The locale of the resource
	 * @param style
	 *            The style of the resource (see {@link wicket.Session})
	 * @return The resource
	 * @throws PackageResourceBlockedException
	 *             when the target resource is not accepted by
	 *             {@link IPackageResourceGuard the package resource guard}.
	 */
	public static PackageResource get(final Class scope, final String path, final Locale locale,
			final String style)
	{
		final SharedResources sharedResources = Application.get().getSharedResources();
		
		PackageResource resource = (PackageResource)sharedResources.get(scope, path, locale, style,
				true);
		if (resource == null)
		{
			resource = new CompressedPackageResource(scope, path, locale, style);
			sharedResources.add(scope, path, locale, style, resource);
		}
		return resource;
	}

	@Override
	public IResourceStream getResourceStream()
	{
		return new CompressingResourceStream(super.getResourceStream());
	}

	/*
	 *  IResourceStream implementation which compresses the data with gzip if the requests
	 *  header Accept-Encoding contains string gzip
	 */
	
	private class CompressingResourceStream implements IResourceStream {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		IResourceStream stream;
		byte bytes[];

		/**
		 * Construct.
		 * @param stream
		 * @throws IOException 
		 * @throws ResourceStreamNotFoundException 
		 */
		public CompressingResourceStream(IResourceStream stream){
			this.stream=stream;
		}

		boolean initialized=false;

		private boolean  init() {
			if(!initialized)
			{
				if(!supportsCompression())return false;				
				try
				{
					ByteArrayOutputStream out=new ByteArrayOutputStream();
					GZIPOutputStream zout=new GZIPOutputStream(out);
					Streams.copy(stream.getInputStream(),zout);
					zout.close();
					this.bytes=out.toByteArray();
					stream.close();
					initialized=true;
				} catch (IOException e)
				{
					throw new RuntimeException(e);
				}
				catch (ResourceStreamNotFoundException e)
				{
					throw new RuntimeException(e);
				}
			}
			return true;
		}

		public void close() throws IOException
		{
			stream.close();
			bytes=null;
		}

		public String getContentType()
		{
			return stream.getContentType();
		}

		public InputStream getInputStream() throws ResourceStreamNotFoundException
		{
			if(init()){
				return new ByteArrayInputStream(bytes);
			} else {
				return stream.getInputStream();
			}
		}

		public Locale getLocale()
		{
			return stream.getLocale();		
		}

		public long length()
		{
			if(init())
			{
				return bytes.length;
			} else {
				return stream.length();
			}
		}

		public void setLocale(Locale locale)
		{
			stream.setLocale(locale);
		}

		public Time lastModifiedTime()
		{
			return stream.lastModifiedTime();
		}
	}

	private boolean supportsCompression(){
		WebRequest request=WebRequestCycle.get().getWebRequest();
		String s = request.getHttpServletRequest().getHeader("Accept-Encoding");
		if(s == null)
		{
			return false;
		} else {
			return s.indexOf("gzip") >= 0;
		}
	}

	@Override
	protected void setHeaders(WebResponse response)
	{
		super.setHeaders(response);
		if(supportsCompression()){
			response.setHeader("Content-Encoding","gzip");
		}
	}
}
