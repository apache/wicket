package org.apache.wicket.util.visit;
public  class Visit<R> implements IVisit<R>
	{
		private static enum Action {
			CONTINUE, CONTINUE_BUT_DONT_GO_DEEPER, STOP;
		}

		private R result;
		private Action action = Action.CONTINUE;

		public void stop()
		{
			stop(null);
		}

		public void stop(R result)
		{
			action = Action.STOP;
			this.result = result;
		}

		public void dontGoDeeper()
		{
			action = Action.CONTINUE_BUT_DONT_GO_DEEPER;
		}

		public boolean isStopped()
		{
			return action == Action.STOP;
		}

		public boolean isContinue()
		{
			return action == Action.CONTINUE;
		}

		public boolean isDontGoDeeper()
		{
			return action == Action.CONTINUE_BUT_DONT_GO_DEEPER;
		}

		public R getResult()
		{
			return result;
		}


	}

	