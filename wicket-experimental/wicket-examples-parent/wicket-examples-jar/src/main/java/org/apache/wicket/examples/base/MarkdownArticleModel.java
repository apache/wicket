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
package org.apache.wicket.examples.base;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.model.LoadableDetachableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Resources;

public class MarkdownArticleModel extends LoadableDetachableModel<String>
{
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(MarkdownArticleModel.class);

	private final Class<?> parent;
	private final String path;

	public MarkdownArticleModel(Class<?> parent, String path)
	{
		this.parent = parent;
		this.path = path;
	}

	@Override
	protected String load()
	{
		String text;
		try
		{
			text = Resources.toString(Resources.getResource(parent, path), Charset.forName("utf-8"));
		}
		catch (IOException e)
		{
			throw new WicketRuntimeException("Unable to load " + path + " relative to " +
				parent.getName(), e);
		}
		return text;
	}
}
