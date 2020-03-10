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
package org.apache.wicket.csp;

import java.io.IOException;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.mapper.AbstractMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple {@link IRequestMapper} that logs the content of a CSP violation.
 * 
 * @author papegaaij
 * @see CSPHeaderConfiguration#reportBack()
 */
public class ReportCSPViolationMapper extends AbstractMapper
{
	private static final int MAX_LOG_SIZE = 4 * 1024;

	private static final Logger log = LoggerFactory.getLogger(ReportCSPViolationMapper.class);

	private final ContentSecurityPolicySettings settings;

	public ReportCSPViolationMapper(ContentSecurityPolicySettings settings)
	{
		this.settings = settings;
	}

	@Override
	public IRequestHandler mapRequest(Request request)
	{
		if (requestMatches(request))
		{
			return new IRequestHandler()
			{
				@Override
				public void respond(IRequestCycle requestCycle)
				{
					try
					{
						HttpServletRequest httpRequest =
							((ServletWebRequest) requestCycle.getRequest()).getContainerRequest();
						if (log.isErrorEnabled())
						{
							log.error(reportToString(httpRequest));
						}
					}
					catch (IOException e)
					{
						throw new WicketRuntimeException(e);
					}
				}

				private String reportToString(HttpServletRequest httpRequest) throws IOException
				{
					try (StringWriter sw = new StringWriter())
					{
						char[] buffer = new char[MAX_LOG_SIZE];
						int n;
						if (-1 != (n = httpRequest.getReader().read(buffer)))
						{
							sw.write(buffer, 0, n);
						}
						return sw.toString();
					}
				}
			};
		}
		return null;
	}

	@Override
	public int getCompatibilityScore(Request request)
	{
		return requestMatches(request) ? 1000 : 0;
	}

	private boolean requestMatches(Request request)
	{
		if (request instanceof ServletWebRequest)
		{
			if (!((ServletWebRequest) request).getContainerRequest().getMethod().equals("POST"))
			{
				return false;
			}
			for (CSPHeaderConfiguration curConfig : settings.getConfiguration().values())
			{
				String mountPath = curConfig.getReportUriMountPath();
				if (mountPath != null
					&& urlStartsWith(request.getUrl(), getMountSegments(mountPath)))
				{
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public Url mapHandler(IRequestHandler requestHandler)
	{
		return null;
	}
}
