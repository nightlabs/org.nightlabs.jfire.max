package org.nightlabs.jfire.jbpm;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.FetchPlan;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.jbpm.JbpmConfiguration;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jdo.moduleregistry.ModuleMetaData;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.jbpm.graph.def.ProcessDefinition;
import org.nightlabs.jfire.jbpm.graph.def.Statable;
import org.nightlabs.jfire.jbpm.graph.def.State;
import org.nightlabs.jfire.jbpm.graph.def.StateDefinition;
import org.nightlabs.jfire.jbpm.graph.def.Transition;
import org.nightlabs.jfire.jbpm.graph.def.id.ProcessDefinitionID;
import org.nightlabs.jfire.jbpm.graph.def.id.TransitionID;
import org.nightlabs.jfire.jbpm.query.StatableQuery;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.config.JFireServerConfigModule;
import org.nightlabs.jfire.servermanager.deploy.DeployOverwriteBehaviour;
import org.nightlabs.jfire.servermanager.deploy.DeploymentJarItem;
import org.nightlabs.jfire.trade.state.id.StateDefinitionID;
import org.nightlabs.jfire.trade.state.id.StateID;
import org.nightlabs.math.Base36Coder;
import org.nightlabs.util.Utils;

/**
 * @ejb.bean name="jfire/ejb/JFireJbpm/JbpmManager"
 *           jndi-name="jfire/ejb/JFireJbpm/JbpmManager"
 *           type="Stateless"
 *           transaction-type="Container"
 *
 * @ejb.util generate = "physical"
 * 
 * @author Marco Schulze
 * @author Marc Klinger - marc[at]nightlabs[dot]de
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
	 * @param hibernateConfigTemplateOutputFile This file is created by this method. If it exists, it will be overwritten.
	 * @param hibernateMainConfigTemplateFile This file is the main config template containing $INCLUDE comments.
	 * @param hibernateConfigTemplateIncludeFiles This <code>Map</code> defines which $INCLUDE will be replaced by what file. The key is the
	 *		CASE-SENSITIVE identifier behind the $INCLUDE directive. The value is the <code>File</code> that will be merged into, instead of the
	 *		complete comment containing the $INCLUDE. Note, that "$INCLUDE" must be at the beginning of the comment! That means, between
	 *		the "&lt;!--" and the "$INCLUDE", only whitespaces and linefeeds are allowed!
	 * @throws IOException 
	 */
	private static void createHibernateConfigTemplate(
			File hibernateConfigTemplateOutputFile,
			File hibernateMainConfigTemplateFile,
			Map<String, File> hibernateConfigTemplateIncludeFiles)
	throws IOException
	{
		String mainConfigText = Utils.readTextFile(hibernateMainConfigTemplateFile);
		for (Map.Entry<String, File> me : hibernateConfigTemplateIncludeFiles.entrySet()) {
			String includeText = Utils.readTextFile(me.getValue()).replace("\\", "\\\\").replace("$", "\\$"); // '\' and '$' have a specific meaning in the replacement text of a regular expression (reference groups), hence we need to escape
			if (me.getKey().matches("[^A-Za-z0-9_.]"))
				throw new IllegalArgumentException("Invalid characters in key (for $INCLUDE directive): " + me.getKey());

			Pattern p = Pattern.compile("<!--(\\s*?)\\$INCLUDE(\\s*?)" + me.getKey() + "(.*?)-->", Pattern.DOTALL);
			mainConfigText = p.matcher(mainConfigText).replaceAll(includeText);
		}
		Utils.writeTextFile(hibernateConfigTemplateOutputFile, mainConfigText);
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_System_"
	 */
	public void initialise()
	throws Exception
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			// Unfortunately, the JbpmService didn't accept the attribute "JbpmCfgResource".
			// Though this property of the class exists. That's why we use a not-so-clean way
			// and register the JbpmConfiguration manually in JNDI here.

			boolean firstRun = false;

			// First, we check whether this is the first time for this organisation.
			ModuleMetaData moduleMetaData = ModuleMetaData.getModuleMetaData(pm, JFireJbpmEAR.MODULE_NAME);
			if (moduleMetaData == null) {
				firstRun = true;
				// this is the first time for the current organisation => deploy configuration

				// version is {major}.{minor}.{release}-{patchlevel}-{suffix}
				moduleMetaData = new ModuleMetaData(
						JFireJbpmEAR.MODULE_NAME, "0.9.0-0-beta", "0.9.0-0-beta");
				pm.makePersistent(moduleMetaData);


				// perform deployment
				File jfireJbpmEarDirectory;
				JFireServerManager jfireServerManager = getJFireServerManager();
				try {
					JFireServerConfigModule cfmod = jfireServerManager.getJFireServerConfigModule();
					jfireJbpmEarDirectory = new File(cfmod.getJ2ee().getJ2eeDeployBaseDirectory(), "JFireJbpm.ear");

					// the ehcache.xml seems to be global in all cases :-( as I didn't find how to specify its name somewhere - this is solved, right? ehcache-config is created per organisation now?!
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


					// For hibernate, we create 2 deployment descriptors: One for the SchemaExport (done below) and
					// one for the Runtime. The SchemaExport uses a direct connection to the database, because it
					// must push DDL through it, which is not allowed by a managed datasource. For the Runtime, we
					// use a managed datasource (and enable hibernate's transaction-manager-bridge) in order to
					// ensure that transaction handling is done correctly.
					File tmpFolder = Utils.createUniqueIncrementalFolder(
							Utils.getTempDir(), "jBPM-hibernate-" + Base36Coder.sharedInstance(false).encode(System.currentTimeMillis(), 1) + '-');

					File hibernateMainConfigTemplateFile = new File(jfireJbpmEarDirectory, "hibernate-cfg.template.xml");
					Map<String, File> hibernateConfigTemplateIncludeFiles = new HashMap<String, File>();


					// Create the 1st descriptor TEMPLATE (which will be processed again) for SchemaExport in the tmp dir.
					hibernateConfigTemplateIncludeFiles.clear();
					hibernateConfigTemplateIncludeFiles.put("DATASOURCE", new File(jfireJbpmEarDirectory, "hibernate-" + HibernateEnvironmentMode.SCHEMA_EXPORT + '-' + cfmod.getDatabase().getDatabaseDriverName_noTx()+"-cfg.template.xml.inc"));
					File hibernateConfigTemplateSchemaExport = new File(tmpFolder, "hibernate-"+ HibernateEnvironmentMode.SCHEMA_EXPORT +"-cfg.template.xml");
					createHibernateConfigTemplate(hibernateConfigTemplateSchemaExport, hibernateMainConfigTemplateFile, hibernateConfigTemplateIncludeFiles);

					deploymentJarItems.add(
							new DeploymentJarItem(
									new File(JbpmLookup.getHibernateConfigFileName(getOrganisationID(), HibernateEnvironmentMode.SCHEMA_EXPORT)),
									hibernateConfigTemplateSchemaExport,
									null));


					// Create the 2nd descriptor TEMPLATE for Runtime.
					hibernateConfigTemplateIncludeFiles.clear();
					hibernateConfigTemplateIncludeFiles.put("DATASOURCE", new File(jfireJbpmEarDirectory, "hibernate-" + HibernateEnvironmentMode.RUNTIME + '-' + cfmod.getDatabase().getDatabaseDriverName_xa()+"-cfg.template.xml.inc"));
					File hibernateConfigTemplateRuntime = new File(tmpFolder, "hibernate-"+ HibernateEnvironmentMode.RUNTIME +"-cfg.template.xml");
					createHibernateConfigTemplate(hibernateConfigTemplateRuntime, hibernateMainConfigTemplateFile, hibernateConfigTemplateIncludeFiles);

					deploymentJarItems.add(
							new DeploymentJarItem(
									new File(JbpmLookup.getHibernateConfigFileName(getOrganisationID(), HibernateEnvironmentMode.RUNTIME)),
									hibernateConfigTemplateRuntime,
									null));


//					deploymentJarItems.add(
//							new DeploymentJarItem(
//									new File(JbpmLookup.getHibernateConfigFileName(getOrganisationID())),
//									new File(jfireJbpmEarDirectory, "hibernate-"+cfmod.getDatabase().getDatabaseDriverName()+"-cfg.template.xml"),
//									null));

					jfireServerManager.createDeploymentJar(
							new File("JFire_JBPM_"+getOrganisationID()+".last", "jbpm-"+getOrganisationID()+"-cfg.jar"),
							deploymentJarItems,
							DeployOverwriteBehaviour.EXCEPTION);
				} finally {
					jfireServerManager.close();
				}

				ClassLoader cl = this.getClass().getClassLoader();

				// wait until the stuff is deployed
				URL hibernateConfigFileResourceSchemaExport = null;
				long startDT = System.currentTimeMillis();
				boolean deploymentComplete;
				do {
					deploymentComplete = true;
					if (cl.getResource(JbpmLookup.getEhCacheConfigFileName(getOrganisationID())) == null)
						deploymentComplete = false;

					if (cl.getResource(JbpmLookup.getJbpmConfigFileName(getOrganisationID())) == null)
						deploymentComplete = false;

					hibernateConfigFileResourceSchemaExport = cl.getResource(
							JbpmLookup.getHibernateConfigFileName(getOrganisationID(), HibernateEnvironmentMode.SCHEMA_EXPORT));

					if (hibernateConfigFileResourceSchemaExport == null)
						deploymentComplete = false;

					if (!deploymentComplete) {
						if (System.currentTimeMillis() - startDT > 60000)
							throw new IllegalStateException("The deployed files did not pop up within the timeout!");

						logger.info("The deployed files didn't pop up yet => Will wait a few seconds...");
						try { Thread.sleep(1000); } catch (InterruptedException x) { /* ignored */ }
					}
				} while (!deploymentComplete);

				logger.info("Deployment complete!");

				if (firstRun) {
					logger.info("Starting Schema Creation...");

					Configuration configuration = new Configuration();
					configuration.configure(hibernateConfigFileResourceSchemaExport);
					SchemaExport schemaExport = new SchemaExport(configuration);
					schemaExport.create(true, true);

					logger.info("Schema Creation complete!");
				}
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
	@SuppressWarnings("unchecked")
	public List<State> getStates(Set<StateID> stateIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, stateIDs, State.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Set<StateID> getStateIDs(ObjectID statableID)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return State.getStateIDsForStatableID(pm, statableID);
		} finally {
			pm.close();
		}
	}

	/**
	 * @param userExecutable If <code>null</code>, it is ignored. If not <code>null</code>, the query filters only transitions where userExecutable has this value.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Set<TransitionID> getTransitionIDs(StateID stateID, Boolean userExecutable)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(Transition.class);
			q.setResult("JDOHelper.getObjectId(this)");
			StringBuffer filter = new StringBuffer("this.fromStateDefinition == :stateDefinition");
			if (userExecutable != null)
				filter.append(" && this.userExecutable == :userExecutable");

			q.setFilter(filter.toString());

			pm.getExtent(State.class);
			State state = (State) pm.getObjectById(stateID);

			Map<String, Object> params = new HashMap<String, Object>();
			params.put("stateDefinition", state.getStateDefinition());
			params.put("userExecutable", userExecutable);
			return new HashSet<TransitionID>((Collection<? extends TransitionID>) q.executeWithMap(params));
		} finally {
			pm.close();
		}
	}

	/**
	 * @param stateDefinitionID The StateDefinition from which the transitions leave.
	 * @param userExecutable If <code>null</code>, it is ignored. If not <code>null</code>, the query filters only transitions where userExecutable has this value.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Set<TransitionID> getTransitionIDs(StateDefinitionID stateDefinitionID, Boolean userExecutable)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(Transition.class);
			q.setResult("JDOHelper.getObjectId(this)");
			StringBuffer filter = new StringBuffer("this.fromStateDefinition == :stateDefinition");
			if (userExecutable != null)
				filter.append(" && this.userExecutable == :userExecutable");

			q.setFilter(filter.toString());

			pm.getExtent(StateDefinition.class);
			StateDefinition stateDefinition = (StateDefinition) pm.getObjectById(stateDefinitionID);

			Map<String, Object> params = new HashMap<String, Object>();
			params.put("stateDefinition", stateDefinition);
			params.put("userExecutable", userExecutable);
			return new HashSet<TransitionID>((Collection<? extends TransitionID>) q.executeWithMap(params));
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
		
	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */		
	@SuppressWarnings("unchecked")
	public Set<StateDefinitionID> getStateDefinitionIDs(ProcessDefinition processDefinition) 
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getObjectIDSet(StateDefinition.getStateDefinitions(pm, processDefinition));
		} finally {
			pm.close();
		}				
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */		
	@SuppressWarnings("unchecked")
	public Collection<StateDefinition> getStateDefinitions(Set<StateDefinitionID> objectIDs, 
			String[] fetchGroups, int maxFetchDepth) 
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, objectIDs, StateDefinition.class, 
					fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}				
	}
	
	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */		
	@SuppressWarnings("unchecked")
	public Collection<ProcessDefinition> getProcessDefinitions(Set<ProcessDefinitionID> objectIDs, 
			String[] fetchGroups, int maxFetchDepth) 
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, objectIDs, ProcessDefinition.class, 
					fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}				
	}	
	
	/**
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports"
	 */
	public Set<Statable> getStatables(Collection<StatableQuery> statableQueries)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(1);
			pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);

			Set<Statable> statables = null;
			for (StatableQuery query : statableQueries) {
				query.setPersistenceManager(pm);
				query.setCandidates(statables);
				statables = new HashSet<Statable>(query.getResult());
			}

			return (Set<Statable>) NLJDOHelper.getDetachedQueryResult(pm, statables);
		} finally {
			pm.close();
		}
	}			
}
