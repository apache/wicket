package org.apache.wicket.ng.request.response;

public class StringResponse extends Response
{

	private final StringBuilder builder = new StringBuilder();
	
	public StringResponse()
	{
	}

	@Override
	public String encodeURL(String url)
	{
		return url;
	}

	@Override
	public void write(CharSequence sequence)
	{
		builder.append(sequence);
	}

	@Override
	public void write(byte[] array)
	{
		throw new UnsupportedOperationException();
	}
		
	@Override
	public String toString()
	{
		return builder.toString();
	}
}
