package org.nightlabs.jfire.base.security.integration.ldap;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.script.ScriptException;
import javax.security.auth.login.LoginException;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.base.security.integration.ldap.connection.ILDAPConnectionParamsProvider.EncryptionMethod;
import org.nightlabs.jfire.base.security.integration.ldap.id.LDAPScriptSetID;
import org.nightlabs.jfire.base.security.integration.ldap.scripts.ILDAPScriptProvider;
import org.nightlabs.jfire.base.security.integration.ldap.sync.LDAPSyncEvent;
import org.nightlabs.jfire.base.security.integration.ldap.sync.LDAPSyncEvent.FetchEventTypeDataUnit;
import org.nightlabs.jfire.base.security.integration.ldap.sync.LDAPUserSecurityGroupSyncConfigLifecycleListener;
import org.nightlabs.jfire.base.security.integration.ldap.sync.PushNotificationsConfigurator;
import org.nightlabs.jfire.base.security.integration.ldap.sync.SecurityChangeListenerJFirePasswordChanged;
import org.nightlabs.jfire.base.security.integration.ldap.sync.SecurityChangeListenerUserSecurityGroupMembers;
import org.nightlabs.jfire.base.security.integration.ldap.sync.SyncLifecycleListener;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserSecurityGroup;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.security.integration.UserManagementSystem;
import org.nightlabs.jfire.security.integration.UserManagementSystemCommunicationException;
import org.nightlabs.jfire.security.integration.UserManagementSystemSyncEvent.SyncEventGenericType;
import org.nightlabs.jfire.security.integration.UserManagementSystemSyncException;
import org.nightlabs.jfire.security.integration.UserManagementSystemType;
import org.nightlabs.jfire.security.integration.UserSecurityGroupSyncConfig;
import org.nightlabs.jfire.security.integration.id.UserManagementSystemID;
import org.nightlabs.jfire.server.data.dir.JFireServerDataDirectory;
import org.nightlabs.jfire.timer.Task;
import org.nightlabs.jfire.timer.id.TaskID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EJB for configuring LDAP user management system. It has an orgnisation-init which creates {@link LDAPServer} types
 * single instances if not created already, creates {@link LDAPServer} instances from ldap.properties configuration file,
 * configures synchronization of user data from JFire to LDAP directory and vice versa.
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Stateless(
		mappedName="jfire/ejb/JFireLDAP/LDAPManager", 
		name="jfire/ejb/JFireLDAP/LDAPManager"
			)
public class LDAPManagerBean extends BaseSessionBeanImpl implements LDAPManagerRemote, LDAPManagerLocal {
	
	private static final Logger logger = LoggerFactory.getLogger(LDAPManagerBean.class);
	
	private static LDAPUserSecurityGroupSyncConfigLifecycleListener syncConfigListener = new LDAPUserSecurityGroupSyncConfigLifecycleListener();
	
	/**
	 * {@inheritDoc}
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	@Override
	public void initialise() {
		
		PersistenceManager pm = createPersistenceManager();

		try{
			// creating InetOrgPersonLDAPServerType single instance (will not be created if exists already)
			UserManagementSystemType.createSingleInstance(pm, InetOrgPersonLDAPServerType.class, "InetOrgPersonLDAPServerType");
			UserManagementSystemType.createSingleInstance(pm, SambaLDAPServerType.class, "SambaLDAPServerType");
			
			pm.getExtent(LDAPServer.class);
			pm.getExtent(LDAPScriptSet.class);
			
			// server creation via configuration property files
			initLDAPServersFromPropertiesFile(pm);
			
			// run sync configuration only once at startup because it's unlikely 
			// that leading system scenario will be changed at server runtime
			configureJFireAsLeadingSystem(pm);
			configureLdapAsLeadingSystem(pm);
			
			// track password chages in JFire and propagate it to LDAP directory
			SecurityChangeListenerJFirePasswordChanged.register(pm);
			
			// listener for sync whenever LDAPUserSecurityGroupSyncConfig is created or its mapping is changed
			pm.getPersistenceManagerFactory().addInstanceLifecycleListener(
					syncConfigListener, new Class[]{LDAPUserSecurityGroupSyncConfig.class}
					);
			
			// run sync for leading LDAPServers at startup
			syncUserDataFromLDAP(null);
			
		} catch (Exception e) {
			logger.error("Exception during LDAPManagerBean.initialise!", e);
		} finally {
			pm.close();
		}
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@RolesAllowed("org.nightlabs.jfire.security.accessRightManagement")
	@Override
	public List<LDAPScriptSet> getLDAPScriptSets(Set<LDAPScriptSetID> objectIDs, String[] fetchGroups, int maxFetchDepth) {
		if (objectIDs == null){
			throw new IllegalArgumentException("Object IDs should be specified (not null) for loading LDAP script sets!");
		}
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, objectIDs, LDAPScriptSet.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@RolesAllowed("org.nightlabs.jfire.security.accessRightManagement")
	@Override
	public LDAPScriptSetID getLDAPScriptSetID(UserManagementSystemID ldapServerID) {
		PersistenceManager pm = createPersistenceManager();
		try{
			Query query = pm.newQuery(pm.getExtent(LDAPScriptSet.class, true));
			query.setResult("JDOHelper.getObjectId(this)");
			query.setFilter("JDOHelper.getObjectId(this.ldapServer) == :ldapServerID");
			HashSet<LDAPScriptSetID> hashSet = new HashSet<LDAPScriptSetID>((Collection<LDAPScriptSetID>) query.execute(ldapServerID));
			if (hashSet.size() > 0){
				return hashSet.iterator().next();
			}
			return null;
		}finally{
			pm.close();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@RolesAllowed("org.nightlabs.jfire.security.accessRightManagement")
	@Override
	public String getInitialScriptContent(LDAPScriptSetID ldapScriptSetID, String scriptID) {
		PersistenceManager pm = createPersistenceManager();
		try{
			LDAPScriptSet ldapScriptSet = (LDAPScriptSet) pm.getObjectById(ldapScriptSetID);
			if (ldapScriptSet.getLDAPServer().getType() instanceof ILDAPScriptProvider){
				return ((ILDAPScriptProvider) ldapScriptSet.getLDAPServer().getType()).getInitialScriptContentByID(scriptID);
			}
			return null;
		}finally{
			pm.close();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.security.accessRightManagement")
	@Override
	public LDAPScriptSet storeLDAPScriptSet(LDAPScriptSet ldapScriptSet, boolean get, String[] fetchGroups, int maxFetchDepth) {
		if (ldapScriptSet == null){
			logger.warn("Can't store NULL ldapScriptSet, return null.");
			return null;
		}
		
		PersistenceManager pm = createPersistenceManager();
		try{
			
			ldapScriptSet = pm.makePersistent(ldapScriptSet);
			
			if (!get){
				return null;
			}

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return pm.detachCopy(ldapScriptSet);
			
		}finally{
			pm.close();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	@Override
	public void syncUserDataFromLDAP(TaskID taskID) throws LoginException, UserManagementSystemSyncException, UserManagementSystemCommunicationException {
		PersistenceManager pm = createPersistenceManager();
		try{
			// determine if there's any leading LDAPServers
			Collection<LDAPServer> leadingSystems = UserManagementSystem.getUserManagementSystemsByLeading(
					pm, true, LDAPServer.class
					);
			
			for (LDAPServer ldapServer : leadingSystems) {
				
				// sync Users and Persons
				Collection<String> entriesForSync = ldapServer.getAllUserEntriesForSync();
				Collection<FetchEventTypeDataUnit> dataUnits = new ArrayList<FetchEventTypeDataUnit>();
				for (String ldapEntryName : entriesForSync){
					dataUnits.add(new FetchEventTypeDataUnit(ldapEntryName));
				}
				if (!dataUnits.isEmpty()){
					LDAPSyncEvent syncEvent = new LDAPSyncEvent(SyncEventGenericType.FETCH_USER);
   					syncEvent.setFetchEventTypeDataUnits(dataUnits);
					
					ldapServer.synchronize(syncEvent);
					pm.flush();
				}

				// sync UserSecurityGroups
				Collection<UserSecurityGroupSyncConfig<?, ?>> syncConfigs = UserSecurityGroupSyncConfig.getAllSyncConfigsForUserManagementSystem(
						pm, ldapServer.getUserManagementSystemObjectID());
				dataUnits = new ArrayList<FetchEventTypeDataUnit>();
				for (UserSecurityGroupSyncConfig<?, ?> syncConfig : syncConfigs){
					dataUnits.add(new FetchEventTypeDataUnit((String) syncConfig.getUserManagementSystemSecurityObject()));
				}
				if (!dataUnits.isEmpty()){
					LDAPSyncEvent syncEvent = new LDAPSyncEvent(SyncEventGenericType.FETCH_AUTHORIZATION);
   					syncEvent.setFetchEventTypeDataUnits(dataUnits);
					
					ldapServer.synchronize(syncEvent);
				}
			}
		}finally{
			pm.close();
		}
	}

	@RolesAllowed({"_System_", "org.nightlabs.jfire.security.accessRightManagement"})
	@Override
	public Collection<String> getLDAPServerParentEntries(UserManagementSystemID ldapServerID) {
		PersistenceManager pm = createPersistenceManager();
		try{
			
			LDAPServer ldapServer = (LDAPServer) pm.getObjectById(ldapServerID);
			try {
				Collection<String> result = new ArrayList<String>();
				result.addAll(ldapServer.getLdapScriptSet().getUserParentEntriesForSync());
				result.addAll(ldapServer.getLdapScriptSet().getGroupParentEntriesForSync());
				return result;
			} catch (ScriptException e) {
				logger.error(e.getMessage(), e);
				return Collections.emptyList();
			} catch (NoSuchMethodException e) {
				logger.error(e.getMessage(), e);
				return Collections.emptyList();
			}
			
		}finally{
			pm.close();
		}
	}

	@RolesAllowed("org.nightlabs.jfire.security.accessRightManagement")
	@Override
	public String getLDAPEntryName(UserManagementSystemID ldapServerID, UserID userID) {
		PersistenceManager pm = createPersistenceManager();
		try{
			User user = User.getUser(pm, userID.organisationID, userID.userID);
			LDAPServer ldapServer = (LDAPServer) pm.getObjectById(ldapServerID);
			return ldapServer.getLdapScriptSet().getLdapDN(user);
		} catch (ScriptException e) {
			logger.error(e.getMessage(), e);
			return null;
		}finally{
			pm.close();
		}
	}

	
	private static SyncLifecycleListener syncLifecycleListener = new SyncLifecycleListener();

	private void configureJFireAsLeadingSystem(PersistenceManager pm){
		pm.getPersistenceManagerFactory().addInstanceLifecycleListener(
				syncLifecycleListener, new Class[]{User.class, Person.class, UserSecurityGroup.class}
				);
		SecurityChangeListenerUserSecurityGroupMembers.register(pm);
	}
	
	private void configureLdapAsLeadingSystem(PersistenceManager pm){
		configureTimerTask(pm);
		configurePushNotifications(pm);
	}

	private void configureTimerTask(PersistenceManager pm){
		try {
			pm.getExtent(Task.class);
			TaskID taskID = TaskID.create(
					getOrganisationID(),
					Task.TASK_TYPE_ID_SYSTEM, LDAPServer.SYNC_USER_DATA_FROM_LDAP_TASK_ID
					);
			Task task;
			try {
				task = (Task) pm.getObjectById(taskID);
				task.getActiveExecID();
			} catch (JDOObjectNotFoundException x) {
				task = new Task(
						taskID.organisationID, taskID.taskTypeID, taskID.taskID,
						User.getUser(pm, getOrganisationID(), User.USER_ID_SYSTEM),
						LDAPManagerLocal.class,
						"syncUserDataFromLDAP"
				);

				task.getName().setText(Locale.ENGLISH.getLanguage(), "LDAPSynchronization");
				task.getDescription().setText(
						Locale.ENGLISH.getLanguage(),
						"This Task queries all users and user security groups from all leading LDAPServers and synchronizes changes to JFire."
						);

				task.getTimePatternSet().createTimePattern(
						"*", // year
						"*", // month
						"*", // day
						"*", // dayOfWeek
						"*", //  hour
						"*/60"); // minute

				task.setEnabled(true);
				pm.makePersistent(task);
			}
		} catch(Exception e){
			logger.error("Can't configure a timer task for LDAP as leading system sychronization!", e);
		}
	}

	private void configurePushNotifications(PersistenceManager pm){
		PushNotificationsConfigurator.sharedInstance().initialize(pm, getOrganisationID());
	}
	
		
	private static final String PROP_LDAP_SERVER_TYPE_CLASS_NAME = "ldapServer%s.typeClassName";
	private static final String PROP_LDAP_SERVER_IS_ACTIVE = "ldapServer%s.isActive";
	private static final String PROP_LDAP_SERVER_ENCRYPTION_METHOD = "ldapServer%s.encryptionMethod";
	private static final String PROP_LDAP_SERVER_PORT = "ldapServer%s.port";
	private static final String PROP_LDAP_SERVER_HOST = "ldapServer%s.host";
	private static final String PROP_LDAP_SERVER_NAME = "ldapServer%s.name";
	private static final String PROP_LDAP_SERVER_SYNC_DN = "ldapServer%s.syncDN";
	private static final String PROP_LDAP_SERVER_BASE_USER_ENTRY_DN = "ldapServer%s.baseUserEntryDN";
	private static final String PROP_LDAP_SERVER_BASE_GROUP_ENTRY_DN = "ldapServer%s.baseGroupEntryDN";
	private static final String PROP_LDAP_SERVER_SYNC_PASSWORD = "ldapServer%s.syncPassword";
	private static final String PROP_LDAP_REMOVE_SERVER_INSTANCE = "ldapServer%s.remove";
	private static final String PROP_LDAP_SERVER_IS_LEADING = "ldapServer%s.isLeading";

	/**
	 * <p>Reads a property file "ldap.properties" from  JFire server data directory (i.e. "/server/default/data/jfire") 
	 * and creates, modifies or removes {@link LDAPServer) instances according to specified properties' values.</p>
	 * 
	 * <pre>Example:
	 * ldapServerN.typeClassName=org.nightlabs.jfire.base.security.integration.ldap.InetOrgPersonLDAPServerType
	 * ldapServerN.name=local test LDAP server
	 * ldapServerN.isActive=true
	 * ldapServerN.baseUserEntryDN=ou=staff,ou=people,dc=nightlabs,dc=de
	 * ldapServerN.baseGroupEntryDN=ou=groups,dc=nightlabs,dc=de
	 * ldapServerN.syncDN=uid=sync_user,ou=staff,ou=people,dc=nightlabs,dc=de
	 * ldapServerN.syncPassword=1111
	 * ldapServerN.isLeading=false
	 * 
	 * - where N is an integer counting from 0 and ALWAYS having +1 increment. Used to create, modify 
	 * or remove more than one LDAPServer instance at once. 
	 * </pre>
	 * 
	 * <p>If property is not specifed than default value will be taken (see constants above).</p>
	 * 
	 * <p>If you want to remove LDAPServer instance, than ldapServerN.remove=true should be specified.</p>
	 * 
	 * @param pm
	 */
	private static void initLDAPServersFromPropertiesFile(PersistenceManager pm){
		
		File ldapPropsFile = new File(JFireServerDataDirectory.getJFireServerDataDirFile()+File.separator+"ldap.properties");
		if (ldapPropsFile.exists()){
			try{
				Properties ldapProps = new Properties();
				FileInputStream fis = null;
				try {
					fis = new FileInputStream(ldapPropsFile);
					ldapProps.load(fis);
				} finally {
					if (fis != null){
						fis.close();
					}
				}
				
				int i = 0;
				String typeClassName = null;
				while ( (typeClassName = ldapProps.getProperty(String.format(PROP_LDAP_SERVER_TYPE_CLASS_NAME, i))) != null){
					
					@SuppressWarnings("unchecked")
					Class<? extends UserManagementSystemType<LDAPServer>> typeClass = (Class<? extends UserManagementSystemType<LDAPServer>>) Class.forName(typeClassName);
					String serverName = ldapProps.getProperty(String.format(PROP_LDAP_SERVER_NAME, i ));
					String syncDN = ldapProps.getProperty(String.format(PROP_LDAP_SERVER_SYNC_DN, i ));					
					String syncPassword = ldapProps.getProperty(String.format(PROP_LDAP_SERVER_SYNC_PASSWORD, i ));					
					String baseUserEntryDN = ldapProps.getProperty(String.format(PROP_LDAP_SERVER_BASE_USER_ENTRY_DN, i ));					
					if (baseUserEntryDN == null || baseUserEntryDN.isEmpty()){
						logger.warn("Base entry for Users is not set for this server! Scripts generating LDAP names will not work properly!");
					}
					String baseGroupEntryDN = ldapProps.getProperty(String.format(PROP_LDAP_SERVER_BASE_GROUP_ENTRY_DN, i ));					
					if (baseGroupEntryDN == null || baseGroupEntryDN.isEmpty()){
						logger.warn("Base entry for Security Groups is not set for this server! Scripts generating LDAP names will not work properly!");
					}
					
					String host = ldapProps.getProperty(String.format(PROP_LDAP_SERVER_HOST, i));
					if (host == null || "".equals(host)){
						host = LDAPServer.LDAP_DEFAULT_HOST;
					}
					
					int port = 0;
					try{
						port = Integer.parseInt(ldapProps.getProperty(String.format(PROP_LDAP_SERVER_PORT, i)));
					}catch(NumberFormatException e){
						port = LDAPServer.LDAP_DEFAULT_PORT;
					}
					
					EncryptionMethod encryptionMethod = null;
					try{
						encryptionMethod = EncryptionMethod.valueOf(EncryptionMethod.class, ldapProps.getProperty(String.format(PROP_LDAP_SERVER_ENCRYPTION_METHOD, i)));
					}catch(Exception e){
						encryptionMethod = LDAPServer.LDAP_DEFAULT_ENCRYPTION_METHOD;
					}
					
					boolean isActive = false;
					if (ldapProps.getProperty(String.format(PROP_LDAP_SERVER_IS_ACTIVE, i)) != null){
						isActive = Boolean.parseBoolean(
								ldapProps.getProperty(
										String.format(PROP_LDAP_SERVER_IS_ACTIVE, i)).toLowerCase()
										);
					}

					boolean isLeading = false;
					if (ldapProps.getProperty(String.format(PROP_LDAP_SERVER_IS_LEADING, i)) != null){
						isLeading = Boolean.parseBoolean(
								ldapProps.getProperty(
										String.format(PROP_LDAP_SERVER_IS_LEADING, i)).toLowerCase()
										);
					}

					boolean remove = false;
					if (ldapProps.getProperty(String.format(PROP_LDAP_REMOVE_SERVER_INSTANCE, i)) != null){
						remove = Boolean.parseBoolean(
								ldapProps.getProperty(
										String.format(PROP_LDAP_REMOVE_SERVER_INSTANCE, i)).toLowerCase()
										);
					}

					LDAPServer server = null;

					// check for existing LDAPServer instances with specified host, port and encryptionMethod
					Collection<LDAPServer> servers = LDAPServer.findLDAPServers(pm, host, port, encryptionMethod);
					if (!servers.isEmpty()){
						server = servers.iterator().next();
					}
					
					if (remove){	// remove found LDAPServer instance
						
						if (server != null){
							pm.deletePersistent(server);
						}
						
					}else{
						
						if (server == null){	// create new LDAPServer instance
							UserManagementSystemType<LDAPServer> ldapServerType = UserManagementSystemType.loadSingleInstance(pm, typeClass);
							server = ldapServerType.createUserManagementSystem();
						}
						
						server.setName(serverName);
						server.setHost(host);
						server.setPort(port);
						server.setEncryptionMethod(encryptionMethod);
						server.setActive(isActive);
						server.setSyncDN(syncDN);
						server.setSyncPassword(syncPassword);
						server.setLeading(isLeading);
						server.setBaseDN(LDAPScriptSet.BASE_USER_ENTRY_NAME_PLACEHOLDER, baseUserEntryDN);
						server.setBaseDN(LDAPScriptSet.BASE_GROUP_ENTRY_NAME_PLACEHOLDER, baseGroupEntryDN);
						
						pm.makePersistent(server);
					}
					
					i++;
				}
				
			} catch (Exception e) {
				logger.error("Can't create LDAPServer instances from properties file!", e);
			}
		}
	}

}
