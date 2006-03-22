package wicket.extensions.ajax.markup.html.form.upload;

import java.io.Serializable;

import wicket.util.lang.Bytes;
import wicket.util.time.Duration;


/**
 * Upload information class
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class UploadInfo implements Serializable
{
	private static final long serialVersionUID = 1L;

	private transient long timeStarted;
	private transient long totalBytes;
	private transient long bytesUploaded;

	public UploadInfo(int totalBytes)
	{
		timeStarted = System.currentTimeMillis();
		this.totalBytes = totalBytes;
	}

	public long getBytesUploaded()
	{
		return bytesUploaded;
	}

	public void setBytesUploaded(long bytesUploaded)
	{
		this.bytesUploaded = bytesUploaded;
	}

	public String getBytesUploadedString() {
		return Bytes.bytes(bytesUploaded).toString();
	}
	
	public String getTotalBytesString() {
		return Bytes.bytes(totalBytes).toString();
	}
	
	public long getTotalBytes()
	{
		return totalBytes;
	}

	public long getElapsedMilliseconds()
	{
		return System.currentTimeMillis() - timeStarted;
	}

	public long getElapsedSeconds()
	{
		return getElapsedMilliseconds() / 1000L;
	}


	public long getTransferRateBPS()
	{
		return bytesUploaded / getElapsedSeconds();
	}
	
	public String getTransferRateString() {
		return Bytes.bytes(getTransferRateBPS()).toString()+"/s";
	}

	public int getPercentageComplete() {
		return (int)(((double)bytesUploaded / (double)totalBytes) * 100);

	}
	
	public long getRemainingMilliseconds()
	{
		int percentageComplete = getPercentageComplete();


		long totalTime = ((getElapsedSeconds() * 100) / percentageComplete);
		long remainingTime = (totalTime - getElapsedSeconds());
		long remainingTimeInMillis = remainingTime * 1000;

		return remainingTimeInMillis;
	}

	public String getRemainingTimeString()
	{
		return Duration.milliseconds(getRemainingMilliseconds()).toString();
	}



}
