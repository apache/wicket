package org.apache.wicket.request.mapper;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.SharedResourceReference;
import org.apache.wicket.util.string.StringValue;
import org.junit.Test;

public class ResourceMapperTest extends WicketTestCase
{
	private static final Charset CHARSET = Charset.forName("UTF-8");
	private static final String SHARED_NAME = "test-resource";

	private IRequestMapper mapper;
	private TestResource resource;

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();

		resource = new TestResource();
		tester.getApplication().getSharedResources().add(SHARED_NAME, resource);
		ResourceReference resourceReference = new SharedResourceReference(SHARED_NAME);
		mapper = new ResourceMapper("/test/resource", resourceReference);
		tester.getApplication().getRootRequestMapperAsCompound().add(mapper);
	}

	private Request createRequest(final String url)
	{
		return new Request()
		{
			@Override
			public Url getUrl()
			{
				return Url.parse(url, CHARSET);
			}

			@Override
			public Locale getLocale()
			{
				return null;
			}

			@Override
			public Charset getCharset()
			{
				return CHARSET;
			}

			@Override
			public Url getBaseUrl()
			{
				return getUrl();
			}
		};
	}

	@Test
	public void testInvalidPathIsEmpty()
	{
		IRequestHandler requestHandler = mapper.mapRequest(createRequest(""));
		assertNull(requestHandler);
	}

	@Test
	public void testInvalidPathIsMismatch()
	{
		IRequestHandler requestHandler = mapper.mapRequest(createRequest("test/resourcex"));
		assertNull(requestHandler);
	}

	@Test
	public void testInvalidPathIsTooShort()
	{
		IRequestHandler requestHandler = mapper.mapRequest(createRequest("test"));
		assertNull(requestHandler);
	}

	@Test
	public void testValidPathWithParams()
	{
		Request request = createRequest("test/resource/1/fred");
		IRequestHandler requestHandler = mapper.mapRequest(request);
		assertNotNull(requestHandler);
		assertEquals(ResourceReferenceRequestHandler.class, requestHandler.getClass());
		assertEquals(request.getUrl(), mapper.mapHandler(requestHandler));

		tester.processRequest(requestHandler);
		PageParameters params = resource.pageParameters;
		assertNotNull(params);
		assertEquals(0, params.getAllNamed().size());
		assertEquals(2, params.getIndexedCount());

		StringValue paramId = params.get(0);
		assertNotNull(paramId);
		assertEquals(1, paramId.toInt());

		StringValue paramName = params.get(1);
		assertNotNull(paramName);
		assertEquals("fred", paramName.toString());
	}

	@Test
	public void testValidPathWithParamsAndQueryPath()
	{
		Request request = createRequest("test/resource/1/fred?foo=bar&foo=baz&value=12");
		IRequestHandler requestHandler = mapper.mapRequest(request);
		assertNotNull(requestHandler);
		assertEquals(ResourceReferenceRequestHandler.class, requestHandler.getClass());
		assertEquals(request.getUrl(), mapper.mapHandler(requestHandler));

		tester.processRequest(requestHandler);
		PageParameters params = resource.pageParameters;
		assertNotNull(params);
		assertEquals(3, params.getAllNamed().size());
		assertEquals(2, params.getIndexedCount());

		StringValue paramId = params.get(0);
		assertNotNull(paramId);
		assertEquals(1, paramId.toInt());

		StringValue paramName = params.get(1);
		assertNotNull(paramName);
		assertEquals("fred", paramName.toString());

		List<StringValue> foo = params.getValues("foo");
		assertNotNull(foo.size() == 2);
		assertEquals("bar", foo.get(0).toString(""));
		assertEquals("baz", foo.get(1).toString(""));

		StringValue paramValue = params.get("value");
		assertEquals(12, paramValue.toInt());
	}

	private static class TestResource implements IResource
	{
		private static final long serialVersionUID = -3130204487473856574L;

		public PageParameters pageParameters;

		public void respond(Attributes attributes)
		{
			pageParameters = attributes.getParameters();
		}
	}
}
