package wicket.extensions.ajax.markup.html.form.upload;

import java.io.Serializable;

import wicket.util.lang.Bytes;
import wicket.util.time.Duration;


/**
 * Holds information about an upload, also has useful querying methods.
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

	/**
	 * @param totalBytes
	 */
	public UploadInfo(int totalBytes)
	{
		timeStarted = System.currentTimeMillis();
		this.totalBytes = totalBytes;
	}

	/**
	 * @return bytes uploaded so far
	 */
	public long getBytesUploaded()
	{
		return bytesUploaded;
	}

	/**
	 * Sets bytes uploaded so far
	 * 
	 * @param bytesUploaded
	 */
	public void setBytesUploaded(long bytesUploaded)
	{
		this.bytesUploaded = bytesUploaded;
	}

	/**
	 * @return human readable string of bytes uploaded so far
	 */
	public String getBytesUploadedString()
	{
		return Bytes.bytes(bytesUploaded).toString();
	}

	/**
	 * @return human readable string of total number of bytes
	 */
	public String getTotalBytesString()
	{
		return Bytes.bytes(totalBytes).toString();
	}

	/**
	 * @return total bytes in the upload
	 */
	public long getTotalBytes()
	{
		return totalBytes;
	}

	/**
	 * @return milliseconds elapsed since upload started
	 */
	public long getElapsedMilliseconds()
	{
		return System.currentTimeMillis() - timeStarted;
	}

	/**
	 * @return seconds elapsed since upload started
	 */
	public long getElapsedSeconds()
	{
		return getElapsedMilliseconds() / 1000L;
	}


	/**
	 * @return transfer rate in bits per second
	 */
	public long getTransferRateBPS()
	{
		return bytesUploaded / Math.max(getElapsedSeconds(), 1);
	}

	/**
	 * @return transfer rate in a human readable string
	 */
	public String getTransferRateString()
	{
		return Bytes.bytes(getTransferRateBPS()).toString() + "/s";
	}

	/**
	 * @return percent of the upload completed
	 */
	public int getPercentageComplete()
	{
		return (int)(((double)bytesUploaded / (double)totalBytes) * 100);

	}

	/**
	 * @return estimate of the remaining number of milliseconds
	 */
	public long getRemainingMilliseconds()
	{
		int percentageComplete = getPercentageComplete();


		long totalTime = ((getElapsedSeconds() * 100) / Math.max(percentageComplete, 1));
		long remainingTime = (totalTime - getElapsedSeconds());
		long remainingTimeInMillis = remainingTime * 1000;

		return remainingTimeInMillis;
	}

	/**
	 * @return estimate of the remaning time in a human readable string
	 */
	public String getRemainingTimeString()
	{
		return Duration.milliseconds(getRemainingMilliseconds()).toString();
	}


}
