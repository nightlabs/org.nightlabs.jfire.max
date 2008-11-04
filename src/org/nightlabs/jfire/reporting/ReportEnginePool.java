/**
 * 
 */
package org.nightlabs.jfire.reporting;

import java.io.Serializable;

import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.log4j.Logger;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.HTMLCompleteImageHandler;
import org.eclipse.birt.report.engine.api.HTMLEmitterConfig;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.ReportEngine;
import org.nightlabs.jfire.reporting.platform.ServerPlatformContext;
import org.nightlabs.jfire.reporting.platform.ServerResourceLocator;

/**
 * A pool of {@link ReportEngine}s. This pool is put into JNDI and is accessible via {@link #getInstance(InitialContext)}. 
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de --> 
 *
 */
public class ReportEnginePool implements Serializable {
	
	private static final long serialVersionUID = 20081104L;
	
	private static Logger logger = Logger.getLogger(ReportEnginePool.class);
	public static final String JNDI_NAME = "java:/jfire/reportEnginePool";

	/**
	 * Factory that creates {@link ReportEngine}s.
	 */
	class ReportEngineFactory extends BasePoolableObjectFactory {
		@Override
		public Object makeObject() throws Exception {
			try {
				EngineConfig config = new EngineConfig( );
				config.setPlatformContext(new ServerPlatformContext());
				// TODO: Add configuration for other formats/emitters as well -> the appropriate ReportLayoutRenderer configure it

//				Create the emitter configuration.
				HTMLEmitterConfig hc = new HTMLEmitterConfig( );
//				Use the "HTML complete" image handler to write the files to disk.
				HTMLCompleteImageHandler imageHandler = new HTMLCompleteImageHandler( );
				hc.setImageHandler( imageHandler );
//				Associate the configuration with the HTML output format.
				config.setEmitterConfiguration( IRenderOption.OUTPUT_FORMAT_HTML, hc );
//				File organisationLogDir = new File(JFireReportingEAR.getEARDir(), "log" + File.separator + organisationID);
//				if (!organisationLogDir.exists()) {
//					organisationLogDir.mkdirs();
//				}
				ReportEngine reportEngine = new ReportEngine(config);
				reportEngine.getConfig().setResourceLocator(new ServerResourceLocator());
//				reportEngine.setLogger();
				return reportEngine;
			} catch (Exception e) {
				logger.error("Could not create ReportEngine", e);
				throw e;
			}
		}
		
		@Override
		public void destroyObject(Object obj) throws Exception {
			logger.debug("Destroying ReportEngine " + obj);
			super.destroyObject(obj);
		}
		
	}
	
	private GenericObjectPool enginePool;
	
	/**
	 * Lazily get/create the object pool. 
	 */
	protected GenericObjectPool getEnginePool() {
		if (enginePool == null) {
			synchronized (this) {
				if (enginePool == null) {
					enginePool = createGenericObjectPool(); 
				}
			}
		}
		return enginePool;
	}
	
	/**
	 * Create the object pool.
	 * TODO: Make its parameters (maxIdle etc.) configurable.
	 * 
	 * @return A new {@link GenericObjectPool}.
	 */
	protected GenericObjectPool createGenericObjectPool() {
		logger.debug("Creating GenericObjectPool for ReportEngines");
		GenericObjectPool pool = new GenericObjectPool();
		pool.setFactory(new ReportEngineFactory());
		pool.setMaxIdle(5);
		pool.setMaxActive(20);
		pool.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_BLOCK);
		pool.setMaxWait(2 * 60 * 1000); // max wait 2 min
		pool.setTimeBetweenEvictionRunsMillis(50 * 60 *1000); // Evict runs every 5 min.
		logger.debug("GenericObjectPool for ReportEngines created:");
		logger.debug(pool.toString());
		return pool;
	}
	
	/**
	 * Protected, no foreign instantiation.
	 */
	protected ReportEnginePool() {
	}
	
	/**
	 * Borrow a {@link ReportEngine} from the pool for exclusive usage.
	 * Make sure you return the engine after you finished using it ({@link #returnReportEngine(ReportEngine)}).
	 * 
	 * @return A {@link ReportEngine} for exclusive usage. 
	 * @throws Exception If an error occurs getting/creating the engine.
	 */
	public ReportEngine borrowReportEngine() throws Exception {
		GenericObjectPool pool = getEnginePool();
		ReportEngine engine = (ReportEngine) pool.borrowObject();
		logger.debug("lend object: " + engine);
		return engine;
	}
	
	/**
	 * Return a {@link ReportEngine} to the pool after usage.
	 * @param engine The engine to return.
	 * @throws Exception If an error occurs.
	 */
	public void returnReportEngine(ReportEngine engine) throws Exception {
		logger.debug("Received object back: " + engine);
		getEnginePool().returnObject(engine);
	}

	/**
	 * Get the instance of {@link ReportEnginePool} from JNDI.
	 * 
	 * @param ctx The initial context to use.
	 * @return The instance of {@link ReportEnginePool} from JNDI.
	 * @throws NamingException If an error occurs.
	 */
	public static ReportEnginePool getInstance(InitialContext ctx) throws NamingException {
		ReportEnginePool pool = null;
		try {
			pool = (ReportEnginePool) ctx.lookup(JNDI_NAME);
		} catch (NameNotFoundException e) {
			pool = null;
		}
		if (pool == null) {
			pool = new ReportEnginePool();
			
			try {
				ctx.createSubcontext("java:/jfire");
			} catch (NameAlreadyBoundException e) {
				// ignore
			}

			ctx.bind(JNDI_NAME, pool);
			pool = (ReportEnginePool) ctx.lookup(JNDI_NAME); 
		}
		return pool;
	}
}
