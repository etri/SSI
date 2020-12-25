package uniresolver.driver.servlet;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uniresolver.driver.Driver;

import java.util.concurrent.atomic.AtomicReference;

public class InitServlet extends HttpServlet implements Servlet {

	private static final long serialVersionUID = 3165107149874392145L;
	private static AtomicReference<Driver> driver = new AtomicReference<>();

	public InitServlet() {

		super();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void init(ServletConfig config) throws ServletException {

		super.init(config);

		if (driver.get() == null) {
			Logger log = LoggerFactory.getLogger(InitServlet.class);

			String driverClassName = config.getInitParameter("Driver");
			Class<? extends Driver> driverClass;

			try {

				driverClass = driverClassName == null ? null : (Class<? extends Driver>) Class.forName(driverClassName);
				if (driverClass == null) {
					driver.set(null);
				} else {
					driver.set(driverClass.newInstance());
				}
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {

				throw new ServletException(ex.getMessage(), ex);
			}

			if (driver.get() == null) throw new ServletException("Unable to load driver: " + driverClassName);

			if (log.isInfoEnabled()) log.info("Loaded driver: " + driverClass);
		}
	}

	static Driver getDriver() {
		return driver.get();
	}
}
