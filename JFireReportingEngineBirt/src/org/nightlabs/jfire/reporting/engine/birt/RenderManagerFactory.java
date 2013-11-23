/**
 * 
 */
package org.nightlabs.jfire.reporting.engine.birt;

import javax.jdo.PersistenceManager;
import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class RenderManagerFactory {

	private static Logger logger = Logger.getLogger(RenderManagerFactory.class);
	
	public static final String JNDI_PREFIX = "java:/jfire/JFireReportingEngineBirt/renderManagerFactory";
	
	protected RenderManagerFactory() {
	}

	
	public static void registerRenderManagerFactory(
			InitialContext ctx, String organisationID, IRenderManagerFactory renderManagerFactory, PersistenceManager pm) 
	throws NamingException {
		
		IRenderManagerFactory foundFactory = null;
		try {
			foundFactory = (IRenderManagerFactory) ctx.lookup(getJNDIName(organisationID));
		} catch (NameNotFoundException e) {
			foundFactory = null;
		}
		if (foundFactory == null) {
			try {
				ctx.createSubcontext("java:/jfire");
			} catch (NameAlreadyBoundException e) {
				// ignore
			}
			try {
				ctx.createSubcontext("java:/jfire/JFireReportingEngineBirt");
			} catch (NameAlreadyBoundException e) {
				// ignore
			}
			try {
				ctx.createSubcontext("java:/jfire/JFireReportingEngineBirt/renderManagerFactory");
			} catch (NameAlreadyBoundException e) {
				// ignore
			}
		}
		renderManagerFactory.initialize(pm);
		ctx.bind(getJNDIName(organisationID), renderManagerFactory);
	}
	
	public static IRenderManagerFactory getRenderManagerFactory(InitialContext ctx, String organisationID) throws NamingException {
		return (IRenderManagerFactory) ctx.lookup(getJNDIName(organisationID));
	}

	public static IRenderManagerFactory getRenderManagerFactory(String organisationID) {
		InitialContext ctx = null;
		try {
			ctx = new InitialContext();
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
		try {
			return getRenderManagerFactory(ctx, organisationID);
		} catch (NamingException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				ctx.close();
			} catch (NamingException e) {
				logger.error("Could not close IntialContext for IRenderManagerFactory lookup");
			}
		}
	}
	
	public static boolean isRenderManagerFactoryRegistered(InitialContext ctx, String organisationID) {
		try {
			return (IRenderManagerFactory) ctx.lookup(getJNDIName(organisationID)) != null;
		} catch (NameNotFoundException e) {
			return false;
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static String getJNDIName(String organisationID) {
		return JNDI_PREFIX + "/" + organisationID;
	}
}
