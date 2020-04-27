package org.apache.wicket;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.mock.MockWebRequest;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.protocol.http.mock.MockServletContext;
import org.apache.wicket.request.Url;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.stream.IntStream;

public class ComponentBenchmarks {

	@State(Scope.Benchmark)
	public static class ExecutionPlan {

		private MarkupContainer component1;
		private MarkupContainer component2;

		@Setup(Level.Trial)
		public void setUp() {
			final MockApplication app = new MockApplication();
			app.setServletContext(new MockServletContext(app, null));
			ThreadContext.setApplication(app);
			app.setName(getClass().getName());
			app.initApplication();
			final Session session = new WebSession(new MockWebRequest(Url.parse("/")));
			ThreadContext.setSession(session);
			ThreadContext.setApplication(app);

			component1 = new WebMarkupContainer("anyId");
			component1.setMarkupId("anyId");
			component1.setOutputMarkupId(true);
			component1.setUseFlagsForDetach(false);
			IntStream.range(0, 50).forEach(i -> component1.add(newChild(i, 50, false)));

			component2 = new WebMarkupContainer("anyId");
			component2.setMarkupId("anyId");
			component2.setOutputMarkupId(true);
			component2.setUseFlagsForDetach(true);
			IntStream.range(0, 50).forEach(i -> component2.add(newChild(i, 50, true)));
		}

		private Component newChild(int index, int maxChildren, boolean useFlagsForDetach) {
			final WebMarkupContainer c = createChild(index, useFlagsForDetach);
			IntStream.range(0, maxChildren).forEach(i -> c.add(createChild(i, useFlagsForDetach)));
			return c;
		}

		private WebMarkupContainer createChild(int index, boolean useFlagsForDetach) {
			final WebMarkupContainer c = new WebMarkupContainer("anyChildId" + index);
			c.setUseFlagsForDetach(useFlagsForDetach);
			c.setMarkupId("anyChild" + index);
			return c;
		}
	}

	@Fork(value = 1, warmups = 1)
	@Benchmark
	@BenchmarkMode(Mode.Throughput)
	@Warmup(iterations = 20)
	@Measurement(iterations = 20)
	public void detachComponentWithMetaData(ExecutionPlan plan) {
		plan.component1.detach();
	}

	@Fork(value = 1, warmups = 1)
	@Benchmark
	@BenchmarkMode(Mode.Throughput)
	@Warmup(iterations = 20)
	@Measurement(iterations = 20)
	public void detachComponentWithFlags(ExecutionPlan plan) {
		plan.component2.detach();
	}

	public static void main(String[] args) throws Exception {
		org.openjdk.jmh.Main.main(args);
	}

}
