package org.apache.wicket.util.visit;

public interface IVisit<R>
{
	void stop();

	void stop(R result);

	void dontGoDeeper();
}
