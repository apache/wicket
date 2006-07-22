package app;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class RequestTimeFilter implements Filter {

	public void destroy() {
	}

	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException,
			ServletException {
		long start = System.currentTimeMillis();
		chain.doFilter(req, res);
		System.out.println("Request took: " + (System.currentTimeMillis() - start));
	}

	public void init(FilterConfig arg0) throws ServletException {
	}

}
