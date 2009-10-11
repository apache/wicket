package org.apache.wicket.ng.request.mapper.mount;

import org.apache.wicket.ng.request.Request;
import org.apache.wicket.ng.request.RequestHandler;
import org.apache.wicket.ng.request.RequestMapper;
import org.apache.wicket.ng.request.Url;
import org.apache.wicket.ng.request.mapper.AbstractMapper;
import org.apache.wicket.ng.util.lang.Check;
import org.apache.wicket.ng.util.string.StringValue;

/**
 * {@link RequestMapper} that can mount requests onto urls. TODO docs and unit test
 * 
 * @author igor.vaynberg
 */
public class MountMapper extends AbstractMapper
{
    private final String[] mountSegments;
    private final MountedRequestMapper mapper;

    public MountMapper(String mountPath, MountedRequestMapper mapper)
    {
        Check.argumentNotEmpty(mountPath, "mountPath");
        Check.argumentNotNull(mapper, "mapper");

        this.mountSegments = getMountSegments(mountPath);
        this.mapper = mapper;
    }

    public MountMapper(String mountPath, RequestMapper mapper)
    {
        Check.argumentNotEmpty(mountPath, "mountPath");
        Check.argumentNotNull(mapper, "mapper");

        this.mountSegments = getMountSegments(mountPath);
        this.mapper = new UnmountedMapperAdapter(mapper);
    }

    public MountMapper(String mountPath, RequestHandler handler)
    {
        Check.argumentNotEmpty(mountPath, "mountPath");
        Check.argumentNotNull(handler, "handler");

        this.mountSegments = getMountSegments(mountPath);
        this.mapper = new UnmountedRequestHandlerAdapter(handler);
    }

    public int getCompatibilityScore(Request request)
    {
        if (urlStartsWith(request.getUrl(), mountSegments))
        {
            return mountSegments.length + mapper.getCompatibilityScore(dismountRequest(request));
        }
        else
        {
            return 0;
        }
    }

    private Request dismountRequest(Request request)
    {
        Url dismountedUrl = new Url(request.getUrl());
        dismountedUrl.removeLeadingSegments(mountSegments.length);
        return request.requestWithUrl(dismountedUrl);
    }


    public final RequestHandler mapRequest(Request request)
    {
        final Url url = request.getUrl();

        if (url.getSegments().size() >= mountSegments.length && urlStartsWith(url, mountSegments))
        {
            MountParameters params = new MountParameters();
            for (int i = 0; i < mountSegments.length; i++)
            {
                String placeholder = getPlaceholder(mountSegments[i]);
                if (placeholder != null)
                {
                    params.setValue(placeholder, StringValue.valueOf(url.getSegments().get(i)));
                }
            }

            return mapper.mapRequest(dismountRequest(request), params);
        }

        return null;
    }

    public Url mapHandler(RequestHandler handler)
    {
        Mount mount = mapper.mapHandler(handler);

        if (mount == null)
        {
            return null;
        }

        // TODO
        // Check.notNull(mount.getUrl());
        // Check.notNull(mount.getMountParameters());

        for (int i = mountSegments.length; i > 0; i--)
        {
            String segment = mountSegments[i - 1];
            String placeholder = getPlaceholder(segment);
            String replacement = segment;

            if (placeholder != null)
            {
                replacement = mount.getMountParameters().getValue(placeholder).toString();
                if (replacement == null)
                {
                    throw new IllegalStateException();// TODO message
                }
            }

            mount.getUrl().getSegments().add(0, replacement);
        }

        return mount.getUrl();
    }


}
