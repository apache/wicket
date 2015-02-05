/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.markup.html.media;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

import org.apache.wicket.Application;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.core.util.resource.PackageResourceStream;
import org.apache.wicket.protocol.http.servlet.ResponseIOException;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * The media streaming resource reference is used to provided streamed data based on bytes requested
 * by the client for video and audio files
 * 
 * @author Tobias Soloschenko
 */
public class MediaStreamingResourceReference extends ResourceReference
{

	private static final long serialVersionUID = 1L;

	private Integer buffer;

	public MediaStreamingResourceReference(Class<?> scope, String name, Locale locale,
		String style, String variation)
	{
		super(scope, name, locale, style, variation);
	}

	public MediaStreamingResourceReference(Class<?> scope, String name)
	{
		super(scope, name, RequestCycle.get().getRequest().getLocale(), null, null);
	}

	public MediaStreamingResourceReference(Key key)
	{
		super(key);
	}

	public MediaStreamingResourceReference(String name)
	{
		super(name);
	}

	@Override
	public IResource getResource()
	{
		AbstractResource mediaStreamingResource = new AbstractResource()
		{
			private static final long serialVersionUID = 1L;
			private Long startbyte;
			private Long endbyte;
			private PackageResourceStream packageResourceStream;

			@Override
			protected ResourceResponse newResourceResponse(Attributes attributes)
			{
				try
				{
					Request request = attributes.getRequest();
					Response response = attributes.getResponse();
					if (request instanceof WebRequest && response instanceof WebResponse)
					{
						WebRequest webRequest = (WebRequest)request;
						WebResponse webResponse = (WebResponse)response;

						packageResourceStream = new PackageResourceStream(
							MediaStreamingResourceReference.this.getScope(),
							MediaStreamingResourceReference.this.getName(),
							MediaStreamingResourceReference.this.getLocale(),
							MediaStreamingResourceReference.this.getStyle(),
							MediaStreamingResourceReference.this.getVariation());
						long length = packageResourceStream.length().bytes();

						ResourceResponse resourceResponse = new ResourceResponse();
						resourceResponse.setContentType(packageResourceStream.getContentType());
						resourceResponse.setFileName(MediaStreamingResourceReference.this.getName());
						resourceResponse.setContentDisposition(ContentDisposition.ATTACHMENT);
						resourceResponse.setLastModified(packageResourceStream.lastModifiedTime());

						// We accept ranges, so that the player can
						// load and play content from a specific byte position
						webResponse.setHeader("Accept-Range", "bytes");

						// Calculating the response code and the byte range to be played
						String rangeHeader = webRequest.getHeader("range");
						if (rangeHeader == null || "".equals(rangeHeader))
						{
							resourceResponse.setStatusCode(200);
							resourceResponse.setContentLength(length);
						}
						else
						{
							rangeHeader = rangeHeader.replaceAll(" ", "");
							// If the range header is filled 206 for
							// partial content has to be returned
							resourceResponse.setStatusCode(206);

							// And now the calculation of the range to be read
							// and to be given as response within the Content-Range header
							String range = rangeHeader.substring(rangeHeader.indexOf('=') + 1,
								rangeHeader.length());
							String[] rangeParts = range.split("-");
							if (rangeParts[0].equals("0"))
							{
								webResponse.setHeader("Content-Range", "bytes 0-" + (length - 1) +
									"/" + length);
								resourceResponse.setContentLength(length);
							}
							else
							{
								startbyte = Long.parseLong(rangeParts[0]);
								if (rangeParts.length == 2)
								{
									endbyte = Long.parseLong(rangeParts[1]);
								}
								else
								{
									endbyte = length - 1;
								}
								webResponse.setHeader("Content-Range", "bytes " + startbyte + "-" +
									endbyte + "/" + length);
								resourceResponse.setContentLength((endbyte - startbyte) + 1);
							}
						}

						resourceResponse.setWriteCallback(new WriteCallback()
						{
							@Override
							public void writeData(Attributes attributes) throws IOException
							{
								try
								{
									InputStream inputStream = packageResourceStream.getInputStream();
									OutputStream outputStream = attributes.getResponse()
										.getOutputStream();
									byte[] buffer = new byte[MediaStreamingResourceReference.this.getBuffer()];

									if (startbyte != null || endbyte != null)
									{
										// skipping the first bytes which are
										// not requested by the client
										inputStream.skip(startbyte);

										long totalBytes = 0;
										int actualReadBytes = 0;

										while ((actualReadBytes = inputStream.read(buffer)) != -1)
										{
											totalBytes = totalBytes + buffer.length;
											long lowerBuffer = endbyte - totalBytes;
											if (lowerBuffer <= 0)
											{
												buffer = (byte[])resizeArray(buffer,
													actualReadBytes);
												outputStream.write(buffer);
												break;
											}
											else
											{
												outputStream.write(buffer);
											}
										}
									}
									else
									{
										while (inputStream.read(buffer) != -1)
										{
											outputStream.write(buffer);
										}
									}
								}
								catch (ResponseIOException e)
								{
									// the client has closed the connection and
									// doesn't read the stream further on
									// (in tomcats
									// org.apache.catalina.connector.ClientAbortException)
									// we ignore this case
								}
								catch (Exception e)
								{
									throw new WicketRuntimeException(
										"A problem occurred while writing the buffer to the output stream.",
										e);
								}
							}
						});

						return resourceResponse;
					}
					else
					{
						throw new IllegalStateException(
							"Either the request is no web request or the response is no web response");
					}
				}
				catch (Exception e)
				{
					throw new WicketRuntimeException(
						"A problem occurred while creating the video response.", e);
				}
				finally
				{
					if (packageResourceStream != null)
					{
						try
						{
							packageResourceStream.close();
						}
						catch (IOException e)
						{
							throw new WicketRuntimeException(
								"A problem occurred while closing the video response stream.", e);
						}
					}
				}
			}
		};
		return mediaStreamingResource;

	}

	/**
	 * Sets the buffer size used to send the data to the client
	 * 
	 * @return the buffer size used to send the data to the client
	 */
	public Integer getBuffer()
	{
		return buffer != null ? buffer : 4048;
	}

	/**
	 * Sets the buffer size used to send the data to the client
	 * 
	 * @param buffer
	 *            the buffer size used to send the data to the client
	 */
	public void setBuffer(Integer buffer)
	{
		this.buffer = buffer;
	}

	/**
	 * Reallocates an array with a new size, and copies the contents of the old array to the new
	 * array.
	 * 
	 * @param oldArray
	 *            the old array, to be reallocated.
	 * @param newSize
	 *            the new array size.
	 * @return A new array with the same contents.
	 */
	@SuppressWarnings("rawtypes")
	private static Object resizeArray(Object oldArray, int newSize)
	{
		int oldSize = java.lang.reflect.Array.getLength(oldArray);
		Class elementType = oldArray.getClass().getComponentType();
		Object newArray = java.lang.reflect.Array.newInstance(elementType, newSize);
		int preserveLength = Math.min(oldSize, newSize);
		if (preserveLength > 0)
		{
			System.arraycopy(oldArray, 0, newArray, 0, preserveLength);
		}
		return newArray;
	}

	/**
	 * Gets the type of the media this resource reference belongs to
	 * 
	 * @return the type of this media
	 */
	public String getType()
	{
		final String resourceName = MediaStreamingResourceReference.this.getName();
		return Application.get().getMimeType(resourceName);
	}
}
