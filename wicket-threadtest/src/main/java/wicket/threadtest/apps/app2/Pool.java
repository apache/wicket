package wicket.threadtest.apps.app2;

import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Test pool.
 * 
 * @author eelcohillenius
 */
public class Pool {

	private static Pool _instance = new Pool();

	private static Log log = LogFactory.getLog(Pool.class);

	public static Connection getConnection() {
		return getInstance().doGetConnection();
	}

	public static Pool getInstance() {
		return _instance;
	}

	public static void release() {
		getInstance().doRelease();
	}

	private Connection[] allConnections;

	private Stack<Connection> available = new Stack<Connection>();

	private ThreadLocal<Connection> locks = new ThreadLocal<Connection>();

	private int size = 3;

	private Pool() {

		allConnections = new Connection[size];
		for (int i = 0; i < size; i++) {
			Connection connection = new Connection(String.valueOf(i));
			allConnections[i] = connection;
			available.push(connection);
		}
	}

	private synchronized Connection doGetConnection() {

		Connection c = locks.get();

		if (c != null) {
			return c;

		} else {

			while (c == null) {

				if (!available.isEmpty()) {
					c = available.pop();
					locks.set(c);
					log.info("returning " + c + " for " + Thread.currentThread());
				} else {
					try {
						log.info("enter wait for " + Thread.currentThread());
						wait();
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
		return c;
	}

	private synchronized void doRelease() {
		Connection c = locks.get();
		if (c != null) {
			available.push(c);
			locks.remove();
			log.info("releasing " + c + " for " + Thread.currentThread());
			notifyAll();
		}
	}
}
