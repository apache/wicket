package org.apache.wicket.core.util.resource.locator.caching;

import java.nio.file.Paths;

import org.apache.wicket.util.resource.PathResourceStream;

/**
 * A reference which can be used to recreate {@link PathResourceStream}
 * 
 * @author Tobias Soloschenko
 */
public class FileSystemResourceStreamReference extends AbstractResourceStreamReference
{
	private final String absolutePath;

	FileSystemResourceStreamReference(final PathResourceStream pathResourceStream)
	{
		absolutePath = pathResourceStream.getPath().toAbsolutePath().toString();
		saveResourceStream(pathResourceStream);
	}

	@Override
	public PathResourceStream getReference()
	{
		PathResourceStream pathResourceStream = new PathResourceStream(Paths.get(absolutePath));
		restoreResourceStream(pathResourceStream);
		return pathResourceStream;
	}
}
