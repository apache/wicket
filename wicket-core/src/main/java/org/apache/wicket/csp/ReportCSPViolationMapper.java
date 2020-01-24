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

	private ContentSecurityPolicyEnforcer csp;

	public ReportCSPViolationMapper(ContentSecurityPolicyEnforcer csp)
	{
		this.csp = csp;
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
						log.error(reportToString(httpRequest));
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
						int n = 0;
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
			for (CSPHeaderConfiguration curConfig : csp.getConfiguration().values())
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
