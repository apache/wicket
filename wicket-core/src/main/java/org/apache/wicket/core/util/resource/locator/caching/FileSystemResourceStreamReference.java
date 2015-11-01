package org.apache.wicket.core.util.resource.locator.caching;

import java.nio.file.Paths;

import org.apache.wicket.util.resource.FileSystemResourceStream;

/**
 * A reference which can be used to recreate {@link FileSystemResourceStream}
 * 
 * @author Tobias Soloschenko
 */
public class FileSystemResourceStreamReference extends AbstractResourceStreamReference
{
	private final String absolutePath;

	FileSystemResourceStreamReference(final FileSystemResourceStream fileSystemResourceStream)
	{
		absolutePath = fileSystemResourceStream.getPath().toAbsolutePath().toString();
		saveResourceStream(fileSystemResourceStream);
	}

	@Override
	public FileSystemResourceStream getReference()
	{
		FileSystemResourceStream fileSystemResourceStream = new FileSystemResourceStream(Paths.get(absolutePath));
		restoreResourceStream(fileSystemResourceStream);
		return fileSystemResourceStream;
	}
}
