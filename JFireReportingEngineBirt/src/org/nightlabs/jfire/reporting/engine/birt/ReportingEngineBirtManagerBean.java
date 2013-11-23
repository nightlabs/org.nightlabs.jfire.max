/**
 * 
 */
package org.nightlabs.jfire.reporting.engine.birt;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.reporting.classloader.ReportingClassLoader;
import org.nightlabs.jfire.reporting.id.ReportingEngineID;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.util.CacheDirTag;
import org.nightlabs.util.IOUtil;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 * 
 */
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
@Stateless
public class ReportingEngineBirtManagerBean extends BaseSessionBeanImpl implements ReportingEngineBirtManagerRemote {
	
	private static final Logger logger = Logger.getLogger(ReportingEngineBirtManagerBean.class);

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.engine.birt.ReportingEngineBirtManagerRemote#initialize()
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	@Override
	public void initialise() throws IOException, NamingException {
		PersistenceManager pm;
		pm = createPersistenceManager();
		JFireServerManager jfireServerManager = getJFireServerManager();
		try {
			
			// Register the ReportingEngineBirt
			if (logger.isDebugEnabled()) {
				logger.debug("Initializing ReportEngineBirt");
			}
			
			pm.getExtent(ReportingEngineBirt.class);
			pm.flush();
			
			ReportingEngineID reportingEngineID = ReportingEngineID.create(getOrganisationID(), ReportingEngineBirt.ENGINE_TYPE_BIRT);
			ReportingEngineBirt reportingEngineBirt = null;
			try {
				reportingEngineBirt = (ReportingEngineBirt) pm.getObjectById(reportingEngineID);
			} catch (JDOObjectNotFoundException e) {
				if (logger.isDebugEnabled()) {
					logger.debug("Creating and persisting singleton instance of ReportEngineBirt");
				}
				reportingEngineBirt = new ReportingEngineBirt(getOrganisationID()); 
				pm.makePersistent(reportingEngineBirt);
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Initializing ReportEngineBirt done");
			}
			
			
			// Register the IRenderManagerFactory (load with special classloader)
			if (logger.isDebugEnabled())
				logger.debug("Initializing RenderManagerFactory for organisation " + getOrganisationID());
			
			String earName = "JFireReportingEngineBirtEAR.ear";
			File jfReportingEarFile = new File(jfireServerManager.getJFireServerConfigModule().getJ2ee().getJ2eeDeployBaseDirectory(),
					earName);

			File tmpBaseDir;
			tmpBaseDir = new File(IOUtil.createUserTempDir("jfire_server.", null), "ear");
			if (!tmpBaseDir.isDirectory())
				tmpBaseDir.mkdirs();

			if (!tmpBaseDir.isDirectory())
				throw new IOException("Could not create directory: " + tmpBaseDir.getAbsolutePath());

			InitialContext ctx = new InitialContext();
			try {
				if (!RenderManagerFactory.isRenderManagerFactoryRegistered(ctx, getOrganisationID())) {
					CacheDirTag cacheDirTag = new CacheDirTag(tmpBaseDir);
					try {
						cacheDirTag.tag("JFire - http://www.jfire.org", true, false);
					} catch (IOException e) {
						logger.warn("initialise: " + e, e);
					}

					File tmpEarDir = new File(tmpBaseDir, earName);
					
					if (logger.isDebugEnabled())
						logger.debug("Unpacking ear to temporary directory " + tmpBaseDir);
					
					IOUtil.unzipArchiveIfModified(jfReportingEarFile, tmpEarDir);

					List<URL> urls = new ArrayList<URL>();
					for (File f : tmpEarDir.listFiles()) {
						if (f.getName().startsWith("org.nightlabs.jfire.reporting.birt")) {
							if (logger.isDebugEnabled())
								logger.debug("Loading RenderManagerFactory from " + f.toURI().toURL());
							urls.add(f.toURI().toURL());
						}
					}

					try {
						IRenderManagerFactory factory = createRenderManagerFactory(urls.toArray(new URL[urls.size()]));
						RenderManagerFactory.registerRenderManagerFactory(ctx, getOrganisationID(), factory, pm);
					} catch (Exception e) {
						logger.error("Could not initially register RenderManagerFactory when initializing ReportingEngineBirtManagerBean.", e);
					}
				}
			} finally {
				ctx.close();
			}
			// init layout renderer
		} finally {
			pm.close();
			jfireServerManager.close();
		}
	}

	private static IRenderManagerFactory createRenderManagerFactory(URL[] urls) {
		Class<?> clazz;
		String className = "org.nightlabs.jfire.reporting.birt.BirtReportRenderManager";
		try {
			if (!ReportingClassLoader.isSharedInstanceCreated()) {
				ReportingClassLoader.createSharedInstance(urls);
			}
			clazz = ReportingClassLoader.sharedInstance().loadClass(className);
			IRenderManagerFactory reportEngineInitaliser = (IRenderManagerFactory) clazz.newInstance();
			return reportEngineInitaliser;
		} catch (ClassNotFoundException e) {
			logger.error("Could not load class " + className);
			return null;
		} catch (InstantiationException e) {
			logger.error("Could not instantiate class " + className);
			return null;
		} catch (IllegalAccessException e) {
			logger.error("Could not access class " + className);
			return null;
		}
	}

}
