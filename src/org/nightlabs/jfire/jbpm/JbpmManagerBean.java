package org.nightlabs.jfire.jbpm;

import java.io.File;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.moduleregistry.ModuleMetaData;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.jbpm.graph.def.State;
import org.nightlabs.jfire.jbpm.graph.def.StateDefinition;
import org.nightlabs.jfire.jbpm.graph.def.Transition;
import org.nightlabs.jfire.jbpm.graph.def.id.TransitionID;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.config.JFireServerConfigModule;
import org.nightlabs.jfire.servermanager.deploy.DeployOverwriteBehaviour;
import org.nightlabs.jfire.servermanager.deploy.DeploymentJarItem;
import org.nightlabs.jfire.trade.state.id.StateDefinitionID;
import org.nightlabs.jfire.trade.state.id.StateID;

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
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_System_"
	 */
	public void initialize()
	throws Exception
	{
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
//					deploymentJarItems.add(
//							new DeploymentJarItem(
//									new File("ehcache.xml"),
//									new File(jfireJbpmEarDirectory, "ehcache.template.xml"),
//									null));
//
//					jfireServerManager.createDeploymentJar(
//							new File("JFire_ehcache_global.last/ehcache-cfg.jar"),
//							deploymentJarItems,
//							DeployOverwriteBehaviour.KEEP);

					deploymentJarItems.clear();

					deploymentJarItems.add(
						new DeploymentJarItem(
								new File(JbpmLookup.getEhCacheConfigFileName(getOrganisationID())),
								new File(jfireJbpmEarDirectory, "ehcache.template.xml"),
								null));

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
				URL hibernateConfigFileResource = null;
				long startDT = System.currentTimeMillis();
				boolean deploymentComplete;
				do {
					deploymentComplete = true;
					if (cl.getResource(JbpmLookup.getEhCacheConfigFileName(getOrganisationID())) == null)
						deploymentComplete = false;

					if (cl.getResource(JbpmLookup.getJbpmConfigFileName(getOrganisationID())) == null)
						deploymentComplete = false;

					hibernateConfigFileResource = cl.getResource(JbpmLookup.getHibernateConfigFileName(getOrganisationID()));
					if (hibernateConfigFileResource == null)
						deploymentComplete = false;

					if (!deploymentComplete) {
						if (System.currentTimeMillis() - startDT > 60000)
							throw new IllegalStateException("The deployed files did not pop up within the timeout!");

						logger.info("The deployed files didn't pop up yet => Will wait a few seconds...");
						try { Thread.sleep(1000); } catch (InterruptedException x) { /* ignored */ }
					}
				} while (!deploymentComplete);

				logger.info("Deployment complete!");

//				Configuration configuration = new Configuration();
//				String xml;
//				InputStream in = hibernateConfigFileResource.openStream();
//				try {
//					xml = Utils.readTextFile(in);
//				} finally {
//					in.close();
//				}
//				configuration.addXML(xml);
//				SchemaExport schemaExport = new SchemaExport(configuration);
//				schemaExport.create(false, true);
			} // if (moduleMetaData == null) {

			JbpmConfiguration jbpmConfiguration = JbpmConfiguration.getInstance(JbpmLookup.getJbpmConfigFileName(getOrganisationID()));
			JbpmLookup.bindJbpmConfiguration(getOrganisationID(), jbpmConfiguration);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Set<TransitionID> getTransitionIDs(StateID stateID)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(Transition.class);
			q.setResult("JDOHelper.getObjectId(this)");
			q.setFilter("this.fromStateDefinition == :stateDefinition");

			pm.getExtent(State.class);
			State state = (State) pm.getObjectById(stateID);
			return new HashSet<TransitionID>((Collection<? extends TransitionID>) q.execute(state.getStateDefinition()));
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Set<TransitionID> getTransitionIDs(StateDefinitionID stateDefinitionID)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(Transition.class);
			q.setResult("JDOHelper.getObjectId(this)");
			q.setFilter("this.fromStateDefinition == :stateDefinition");

			pm.getExtent(StateDefinition.class);
			StateDefinition stateDefinition = (StateDefinition) pm.getObjectById(stateDefinitionID);
			return new HashSet<TransitionID>((Collection<? extends TransitionID>) q.execute(stateDefinition));
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public List<Transition> getTransitions(Set<TransitionID> transitionIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, transitionIDs, Transition.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

//	public void signal(StateID stateID, String jbpmTransitionName)
//	{
//		PersistenceManager pm = getPersistenceManager();
//		try {
//			pm.getExtent(State.class);
//			State state = (State) pm.getObjectById(stateID);
//			JbpmContext jbpmContext = JbpmLookup.getJbpmConfiguration().createJbpmContext();
//			try {
//				state.getStateDefinition().getProcessDefinition().getJbpmProcessDefinitionName();
//				
//			} finally {
//				jbpmContext.close();
//			}
//		} finally {
//			pm.close();
//		}
//	}
}
