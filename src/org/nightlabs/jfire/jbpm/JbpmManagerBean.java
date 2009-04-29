package org.nightlabs.jfire.jbpm;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
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
import org.nightlabs.jdo.query.JDOQueryCollectionDecorator;
import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.jbpm.graph.def.ProcessDefinition;
import org.nightlabs.jfire.jbpm.graph.def.Statable;
import org.nightlabs.jfire.jbpm.graph.def.State;
import org.nightlabs.jfire.jbpm.graph.def.StateDefinition;
import org.nightlabs.jfire.jbpm.graph.def.Transition;
import org.nightlabs.jfire.jbpm.graph.def.id.ProcessDefinitionID;
import org.nightlabs.jfire.jbpm.graph.def.id.StateDefinitionID;
import org.nightlabs.jfire.jbpm.graph.def.id.StateID;
import org.nightlabs.jfire.jbpm.graph.def.id.TransitionID;
import org.nightlabs.jfire.jbpm.query.StatableQuery;
import org.nightlabs.jfire.server.data.dir.JFireServerDataDirectory;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.config.JFireServerConfigModule;
import org.nightlabs.jfire.servermanager.deploy.DeployOverwriteBehaviour;
import org.nightlabs.jfire.servermanager.deploy.DeploymentJarItem;
import org.nightlabs.math.Base36Coder;
import org.nightlabs.util.CollectionUtil;
import org.nightlabs.util.IOUtil;

/**
 * @ejb.bean name="jfire/ejb/JFireJbpm/JbpmManager"
 *           jndi-name="jfire/ejb/JFireJbpm/JbpmManager"
 *           type="Stateless"
 *           transaction-type="Container"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 *
 * @author Marco Schulze
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
@Stateless
public abstract class JbpmManagerBean
extends BaseSessionBeanImpl
implements JbpmManagerRemote
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(JbpmManagerBean.class);

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.jbpm.JbpmManagerRemote#ping(java.lang.String)
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	@RolesAllowed("_Guest_")
	@Override
	public String ping(String message) {
		return super.ping(message);
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
		String mainConfigText = IOUtil.readTextFile(hibernateMainConfigTemplateFile);
		for (Map.Entry<String, File> me : hibernateConfigTemplateIncludeFiles.entrySet()) {
			String includeText = IOUtil.readTextFile(me.getValue()).replace("\\", "\\\\").replace("$", "\\$"); // '\' and '$' have a specific meaning in the replacement text of a regular expression (reference groups), hence we need to escape
			if (me.getKey().matches("[^A-Za-z0-9_.]"))
				throw new IllegalArgumentException("Invalid characters in key (for $INCLUDE directive): " + me.getKey());

			Pattern p = Pattern.compile("<!--(\\s*?)\\$INCLUDE(\\s*?)" + me.getKey() + "(.*?)-->", Pattern.DOTALL);
			mainConfigText = p.matcher(mainConfigText).replaceAll(includeText);
		}
		IOUtil.writeTextFile(hibernateConfigTemplateOutputFile, mainConfigText);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.jbpm.JbpmManagerRemote#initialise()
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	public void initialise()
	throws Exception
	{
		String jbpmDeploymentSubDir = "JFire_JBPM_"+getOrganisationID()+".last";

		JFireServerManager jfireServerManager = getJFireServerManager();
		try {
			PersistenceManager pm = getPersistenceManager();
			boolean successful = false; boolean deploymentStarted = false;
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
							JFireJbpmEAR.MODULE_NAME, "0.9.7-0-beta", "0.9.7-0-beta");
					pm.makePersistent(moduleMetaData);


					// perform deployment
					File jfireJbpmTemplateDirectory;

					JFireServerConfigModule cfmod = jfireServerManager.getJFireServerConfigModule();
					jfireJbpmTemplateDirectory = new File(new File(JFireServerDataDirectory.getJFireServerDataDirFile(), "template"), "jbpm");

					// the ehcache.xml seems to be global in all cases :-( as I didn't find how to specify its name somewhere - this is solved, right? ehcache-config is created per organisation now?!
					List<DeploymentJarItem> deploymentJarItems = new LinkedList<DeploymentJarItem>();
//					deploymentJarItems.add(
//					new DeploymentJarItem(
//					new File("ehcache.xml"),
//					new File(jfireJbpmEarDirectory, "ehcache.template.xml"),
//					null));

//					jfireServerManager.createDeploymentJar(
//					new File("JFire_ehcache_global.last/ehcache-cfg.jar"),
//					deploymentJarItems,
//					DeployOverwriteBehaviour.KEEP);

					deploymentJarItems.clear();

					deploymentJarItems.add(
							new DeploymentJarItem(
									new File(JbpmLookup.getEhCacheConfigFileName(getOrganisationID())),
									new File(jfireJbpmTemplateDirectory, "ehcache.template.xml"),
									null));

					deploymentJarItems.add(
							new DeploymentJarItem(
									new File(JbpmLookup.getJbpmConfigFileName(getOrganisationID())),
									new File(jfireJbpmTemplateDirectory, "jbpm.cfg.template.xml"),
									null));


					// For hibernate, we create 2 deployment descriptors: One for the SchemaExport (done below) and
					// one for the Runtime. The SchemaExport uses a direct connection to the database, because it
					// must push DDL through it, which is not allowed by a managed datasource. For the Runtime, we
					// use a managed datasource (and enable hibernate's transaction-manager-bridge) in order to
					// ensure that transaction handling is done correctly.
					File tmpFolder = IOUtil.createUniqueIncrementalFolder(
							IOUtil.getTempDir(), "jBPM-hibernate-" + Base36Coder.sharedInstance(false).encode(System.currentTimeMillis(), 1) + '-');
					try {
						File hibernateMainConfigTemplateFile = new File(jfireJbpmTemplateDirectory, "hibernate-cfg.template.xml");
						Map<String, File> hibernateConfigTemplateIncludeFiles = new HashMap<String, File>();


						// Create the 1st descriptor TEMPLATE (which will be processed again) for SchemaExport in the tmp dir.
						hibernateConfigTemplateIncludeFiles.clear();
						hibernateConfigTemplateIncludeFiles.put("DATASOURCE", new File(jfireJbpmTemplateDirectory, "hibernate-" + HibernateEnvironmentMode.SCHEMA_EXPORT + '-' + cfmod.getDatabase().getDatabaseDriverName_noTx() + "-cfg.template.xml.inc"));
						File hibernateConfigTemplateSchemaExport = new File(tmpFolder, "hibernate-"+ HibernateEnvironmentMode.SCHEMA_EXPORT +"-cfg.template.xml");
						createHibernateConfigTemplate(hibernateConfigTemplateSchemaExport, hibernateMainConfigTemplateFile, hibernateConfigTemplateIncludeFiles);

						deploymentJarItems.add(
								new DeploymentJarItem(
										new File(JbpmLookup.getHibernateConfigFileName(getOrganisationID(), HibernateEnvironmentMode.SCHEMA_EXPORT)),
										hibernateConfigTemplateSchemaExport,
										null));


						// Create the 2nd descriptor TEMPLATE for Runtime.
						hibernateConfigTemplateIncludeFiles.clear();
						hibernateConfigTemplateIncludeFiles.put("DATASOURCE", new File(jfireJbpmTemplateDirectory, "hibernate-" + HibernateEnvironmentMode.RUNTIME + '-' + cfmod.getDatabase().getDatabaseDriverName_xa() + "-cfg.template.xml.inc"));
//						hibernateConfigTemplateIncludeFiles.put("DATASOURCE", new File(jfireJbpmEarDirectory, "hibernate-" + HibernateEnvironmentMode.RUNTIME + '-' + cfmod.getDatabase().getDatabaseDriverName_localTx() + "-cfg.template.xml.inc"));
						File hibernateConfigTemplateRuntime = new File(tmpFolder, "hibernate-"+ HibernateEnvironmentMode.RUNTIME +"-cfg.template.xml");
						createHibernateConfigTemplate(hibernateConfigTemplateRuntime, hibernateMainConfigTemplateFile, hibernateConfigTemplateIncludeFiles);

						deploymentJarItems.add(
								new DeploymentJarItem(
										new File(JbpmLookup.getHibernateConfigFileName(getOrganisationID(), HibernateEnvironmentMode.RUNTIME)),
										hibernateConfigTemplateRuntime,
										null));

						deploymentStarted = true;
						jfireServerManager.createDeploymentJar(
								new File(jbpmDeploymentSubDir, "jbpm-"+getOrganisationID()+"-cfg.jar"),
								deploymentJarItems,
								DeployOverwriteBehaviour.EXCEPTION);


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
					} finally {
						IOUtil.deleteDirectoryRecursively(tmpFolder);
					}
				} // if (moduleMetaData == null) {

				JbpmConfiguration jbpmConfiguration = JbpmConfiguration.getInstance(JbpmLookup.getJbpmConfigFileName(getOrganisationID()));
				JbpmLookup.bindJbpmConfiguration(getOrganisationID(), jbpmConfiguration);
				successful = true;
			} finally {
				if (deploymentStarted && !successful) {
					try {
						logger.error("An error occured after deployment was started! Will try to undeploy all deployed jbpm stuff...");
						jfireServerManager.undeploy(new File(jbpmDeploymentSubDir));
						logger.error("An error occured after deployment was started! But undeployed all deployed jbpm stuff successfully.");
					} catch (Exception x) {
						logger.error("An error occured after deployment was started! And undeploying all deployed jbpm stuff failed!!!", x);
					}
				}
				pm.close();
			}
		} finally {
			jfireServerManager.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.jbpm.JbpmManagerRemote#getStates(java.util.Set, java.lang.String[], int)
	 */
	@RolesAllowed("_Guest_")
	public List<State> getStates(Set<StateID> stateIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, stateIDs, State.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.jbpm.JbpmManagerRemote#getStateIDs(org.nightlabs.jdo.ObjectID)
	 */
	@RolesAllowed("_Guest_")
	public Set<StateID> getStateIDs(ObjectID statableID)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return State.getStateIDsForStatableID(pm, statableID);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.jbpm.JbpmManagerRemote#getTransitionIDs(org.nightlabs.jfire.jbpm.graph.def.id.StateID, java.lang.Boolean)
	 */
	@RolesAllowed("_Guest_")
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
			Collection<TransitionID> c = CollectionUtil.castCollection((Collection<?>) q.executeWithMap(params));
			return new HashSet<TransitionID>(c);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.jbpm.JbpmManagerRemote#getTransitionIDs(org.nightlabs.jfire.jbpm.graph.def.id.StateDefinitionID, java.lang.Boolean)
	 */
	@RolesAllowed("_Guest_")
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
			Collection<TransitionID> c = CollectionUtil.castCollection((Collection<?>) q.executeWithMap(params));
			return new HashSet<TransitionID>(c);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.jbpm.JbpmManagerRemote#getTransitions(java.util.Set, java.lang.String[], int)
	 */
	@RolesAllowed("_Guest_")
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.jbpm.JbpmManagerRemote#getStateDefinitionIDs(org.nightlabs.jfire.jbpm.graph.def.ProcessDefinition)
	 */
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@RolesAllowed("_Guest_")
	public Set<StateDefinitionID> getStateDefinitionIDs(ProcessDefinition processDefinition)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getObjectIDSet(StateDefinition.getStateDefinitions(pm, processDefinition));
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.jbpm.JbpmManagerRemote#getStateDefinitions(java.util.Set, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.jbpm.JbpmManagerRemote#getProcessDefinitions(java.util.Set, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.jbpm.JbpmManagerRemote#getStatables(org.nightlabs.jdo.query.QueryCollection)
	 */
	@RolesAllowed("_Guest_")
	@SuppressWarnings("unchecked")
	public Set<Statable> getStatables(QueryCollection<? extends StatableQuery> statableQueries)
	{
		if (statableQueries == null)
			return null;

		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(1);
			pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);

			JDOQueryCollectionDecorator<StatableQuery> decoratedCollection;

			// DO not apply generics to this instanceof check, otherwise the sun compiler will result in
			// an error like this:
//			 [javac] /home/marius/workspaces/crossticket/JFireJbpm/src/org/nightlabs/jfire/jbpm/JbpmManagerBean.java:510: inconvertible types
//		   [javac] found   : org.nightlabs.jdo.query.QueryCollection<org.nightlabs.jfire.jbpm.graph.def.Statable,capture#82 of ? extends org.nightlabs.jfire.jbpm.query.StatableQuery>
//		   [javac] required: org.nightlabs.jdo.query.JDOQueryCollectionDecorator<?,?>
//		   [javac] 			if (statableQueries instanceof JDOQueryCollectionDecorator<?, ?>)
			if (statableQueries instanceof JDOQueryCollectionDecorator)
			{
				decoratedCollection = (JDOQueryCollectionDecorator<StatableQuery>) statableQueries;
			}
			else
			{
				decoratedCollection = new JDOQueryCollectionDecorator<StatableQuery>(statableQueries);
			}

			decoratedCollection.setPersistenceManager(pm);
			Collection<Statable> statables = (Collection<Statable>) decoratedCollection.executeQueries();

			return NLJDOHelper.getDetachedQueryResultAsSet(pm, statables);
		} finally {
			pm.close();
		}
	}
}
