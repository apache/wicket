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
package org.apache.wicket.serialize.java;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * A {@link JavaSerializer} that deflates the outputstream on the fly, reducing page store size by
 * up to a factor 8. Be advised that deflating serialized objects comes at a price of about 2-20ms
 * per page request, depending on the size of the page and the cpu power of the machine.
 * 
 * <p>
 * To use this serializer, put the following code in your application's init:
 * 
 * <pre>
 * getFrameworkSettings().setSerializer(new DeflatedJavaSerializer(getApplicationKey()));
 * </pre>
 * 
 * @author papegaaij
 */
public class DeflatedJavaSerializer extends JavaSerializer
{
	private static final int COMPRESS_BUF_SIZE = 4 * 1024;

	/**
	 * Construct.
	 * 
	 * @param applicationKey
	 */
	public DeflatedJavaSerializer(String applicationKey)
	{
		super(applicationKey);
	}

	@Override
	protected ObjectOutputStream newObjectOutputStream(OutputStream out) throws IOException
	{
		return super.newObjectOutputStream(new DeflaterOutputStream(out, createDeflater(),
			COMPRESS_BUF_SIZE));
	}

	/**
	 * Creates the {@code Deflater}. Override this method to customize the deflater, for example to
	 * change the compression level and/or strategy.
	 * 
	 * @return the {@code Deflater}
	 */
	protected Deflater createDeflater()
	{
		return new Deflater(Deflater.BEST_SPEED);
	}

	@Override
	protected ObjectInputStream newObjectInputStream(InputStream in) throws IOException
	{
		return super.newObjectInputStream(new InflaterInputStream(in, new Inflater(),
			COMPRESS_BUF_SIZE));
	}
}
