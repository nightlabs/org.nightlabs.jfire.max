package org.nightlabs.jfire.jbpm;

import java.io.File;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.jbpm.JbpmConfiguration;
import org.nightlabs.jdo.moduleregistry.ModuleMetaData;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.config.JFireServerConfigModule;
import org.nightlabs.jfire.servermanager.deploy.DeployOverwriteBehaviour;
import org.nightlabs.jfire.servermanager.deploy.DeploymentJarItem;

/**
 * @ejb.bean name="jfire/ejb/JFireJbpm/JbpmManager"
 *           jndi-name="jfire/ejb/JFireJbpm/JbpmManager"
 *           type="Stateless"
 *           transaction-type="Container"
 *
 * @ejb.util generate = "physical"
 */
public abstract class JbpmManagerBean
extends BaseSessionBeanImpl
implements SessionBean
{
	private static final Logger logger = Logger.getLogger(JbpmManagerBean.class);

	public void setSessionContext(SessionContext sessionContext)
			throws EJBException, RemoteException
	{
		logger.debug(this.getClass().getName() + ".setSessionContext("+sessionContext+")");
		super.setSessionContext(sessionContext);
	}
	/**
	 * @ejb.create-method  
	 * @ejb.permission role-name="_Guest_"
	 */
	public void ejbCreate()
	throws CreateException
	{
		logger.debug(this.getClass().getName() + ".ejbCreate()");
	}
	/**
	 * @see javax.ejb.SessionBean#ejbRemove()
	 * 
	 * @ejb.permission unchecked="true"
	 */
	public void ejbRemove() throws EJBException, RemoteException
	{
		logger.debug(this.getClass().getName() + ".ejbRemove()");
	}
	
	/**
	 * @see javax.ejb.SessionBean#ejbActivate()
	 */
	public void ejbActivate() throws EJBException, RemoteException
	{
		logger.debug(this.getClass().getName() + ".ejbActivate()");
	}
	/**
	 * @see javax.ejb.SessionBean#ejbPassivate()
	 */
	public void ejbPassivate() throws EJBException, RemoteException
	{
		logger.debug(this.getClass().getName() + ".ejbPassivate()");
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public void initialize()
	throws Exception
	{
		if (!User.USERID_SYSTEM.equals(getPrincipal().getUserID()))
			throw new IllegalStateException("This method can only be called internally!");

		PersistenceManager pm = getPersistenceManager();
		try {
			// Unfortunately, the JbpmService didn't accept the attribute "JbpmCfgResource".
			// Though this property of the class exists. That's why we use a not-so-clean way
			// and register the JbpmConfiguration manually in JNDI here.

			// First, we check whether this is the first time for this organisation.
			ModuleMetaData moduleMetaData = ModuleMetaData.getModuleMetaData(pm, JFireJbpmEAR.MODULE_NAME);
			if (moduleMetaData == null) {
				// this is the first time for the current organisation => deploy configuration

				// version is {major}.{minor}.{release}-{patchlevel}-{suffix}
				moduleMetaData = new ModuleMetaData(
						JFireJbpmEAR.MODULE_NAME, "1.0.0-0-beta", "1.0.0-0-beta");
				pm.makePersistent(moduleMetaData);


				// perform deployment
				JFireServerManager jfireServerManager = getJFireServerManager();
				try {
					JFireServerConfigModule cfmod = jfireServerManager.getJFireServerConfigModule();
					File jfireJbpmEarDirectory = new File(cfmod.getJ2ee().getJ2eeDeployBaseDirectory() + "JFireJbpm.ear");

					// the ehcache.xml seems to be global in all cases :-( as I didn't find how to specify its name somewhere
					List<DeploymentJarItem> deploymentJarItems = new LinkedList<DeploymentJarItem>();
					deploymentJarItems.add(
							new DeploymentJarItem(
									new File("ehcache.xml"),
									new File(jfireJbpmEarDirectory, "ehcache.template.xml"),
									null));

					jfireServerManager.createDeploymentJar(
							new File("JFire_ehcache_global.last/ehcache-cfg.jar"),
							deploymentJarItems,
							DeployOverwriteBehaviour.KEEP);

					deploymentJarItems.clear();
					deploymentJarItems.add(
							new DeploymentJarItem(
									new File(JbpmLookup.getJbpmConfigFileName(getOrganisationID())),
									new File(jfireJbpmEarDirectory, "jbpm.cfg.template.xml"),
									null));

					deploymentJarItems.add(
							new DeploymentJarItem(
									new File(JbpmLookup.getHibernateConfigFileName(getOrganisationID())),
									new File(jfireJbpmEarDirectory, "hibernate-"+cfmod.getDatabase().getDatabaseDriverName()+"-cfg.template.xml"),
									null));

					jfireServerManager.createDeploymentJar(
							new File("JFire_JBPM_"+getOrganisationID()+".last/jbpm-"+getOrganisationID()+"-cfg.jar"),
							deploymentJarItems,
							DeployOverwriteBehaviour.EXCEPTION);
				} finally {
					jfireServerManager.close();
				}

				ClassLoader cl = this.getClass().getClassLoader();

				// wait until the stuff is deployed
				long startDT = System.currentTimeMillis();
				boolean deploymentComplete;
				do {
					deploymentComplete = true;
					if (cl.getResource("ehcache.xml") == null)
						deploymentComplete = false;

					if (cl.getResource(JbpmLookup.getJbpmConfigFileName(getOrganisationID())) == null)
						deploymentComplete = false;

					if (cl.getResource(JbpmLookup.getJbpmConfigFileName(getOrganisationID())) == null)
						deploymentComplete = false;

					if (!deploymentComplete) {
						if (System.currentTimeMillis() - startDT > 60000)
							throw new IllegalStateException("The deployed files did not pop up within the timeout!");

						logger.info("The deployed files didn't pop up yet => Will wait a few seconds...");
						try { Thread.sleep(1000); } catch (InterruptedException x) { /* ignored */ }
					}
				} while (!deploymentComplete);

				logger.info("Deployment complete!");

			} // if (moduleMetaData == null) {

			JbpmConfiguration jbpmConfiguration = JbpmConfiguration.getInstance(JbpmLookup.getJbpmConfigFileName(getOrganisationID()));
			JbpmLookup.bindJbpmConfiguration(getOrganisationID(), jbpmConfiguration);
		} finally {
			pm.close();
		}
	}
}
