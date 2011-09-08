package org.nightlabs.jfire.base.security.integration.ldap.sync;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.listener.DeleteLifecycleListener;
import javax.jdo.listener.InstanceLifecycleEvent;
import javax.jdo.listener.StoreLifecycleListener;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.NoPermissionException;
import javax.naming.event.EventContext;
import javax.naming.event.NamespaceChangeListener;
import javax.naming.event.NamingEvent;
import javax.naming.event.NamingExceptionEvent;
import javax.naming.event.NamingListener;
import javax.naming.event.ObjectChangeListener;
import javax.naming.ldap.InitialLdapContext;
import javax.script.ScriptException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.nightlabs.j2ee.LoginData;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.asyncinvoke.AsyncInvoke;
import org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnqueueException;
import org.nightlabs.jfire.asyncinvoke.ErrorCallback;
import org.nightlabs.jfire.asyncinvoke.SuccessCallback;
import org.nightlabs.jfire.base.AuthCallbackHandler;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.security.integration.ldap.LDAPManagerLocal;
import org.nightlabs.jfire.base.security.integration.ldap.LDAPServer;
import org.nightlabs.jfire.base.security.integration.ldap.connection.JNDIConnectionWrapper;
import org.nightlabs.jfire.base.security.integration.ldap.sync.LDAPSyncEvent.FetchEventTypeDataUnit;
import org.nightlabs.jfire.base.security.integration.ldap.sync.LDAPSyncEvent.LDAPSyncEventType;
import org.nightlabs.jfire.security.GlobalSecurityReflector;
import org.nightlabs.jfire.security.NoUserException;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserDescriptor;
import org.nightlabs.jfire.security.integration.UserManagementSystem;
import org.nightlabs.jfire.security.integration.UserManagementSystemCommunicationException;
import org.nightlabs.jfire.security.integration.id.UserManagementSystemID;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.JFireServerManagerFactory;
import org.nightlabs.jfire.servermanager.j2ee.J2EEAdapter;
import org.nightlabs.jfire.timer.Task;
import org.nightlabs.jfire.timer.id.TaskID;
import org.nightlabs.util.CollectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class holds all staff needed for configuration of push notifications from {@link LDAPServer}.
 * Underlying push notifications are implemented with the help of JNDI {@link NamingListener}s - 
 * see {@link #syncPushNotificationsListener} for an example.
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
public class PushNotificationsConfigurator {
	
	private static final String CONFIGURE_PUSH_NOTIFICATION_LISTENERS_TASK_ID = "configurePushNotificationListeners";

	private static final Logger logger = LoggerFactory.getLogger(PushNotificationsConfigurator.class);

	/**
	 * Shared instance of this class
	 */
	private static PushNotificationsConfigurator sharedInstance;
	
	private PushNotificationsConfigurator(){}
	
	public static PushNotificationsConfigurator sharedInstance(){
		if (sharedInstance == null) {
			synchronized (PushNotificationsConfigurator.class) {
				if (sharedInstance == null)
					sharedInstance = new PushNotificationsConfigurator();
			}
		}
		return sharedInstance;
	}
	
	
	/**
	 * {@link StoreLifecycleListener} which registers {@link #syncPushNotificationsListener} to every leading {@link LDAPServer}
	 * and removes {@link #syncPushNotificationsListener} from every non-leading {@link LDAPServer}.
	 */
	private StoreLifecycleListener ldapServerStoreListener = new StoreLifecycleListener(){
		@Override
		public void postStore(InstanceLifecycleEvent event) {
			if (event.getPersistentInstance() instanceof LDAPServer){
				LDAPServer ldapServer = (LDAPServer) event.getPersistentInstance();
				try {
					if (ldapServer.isLeading()){
						addPushNotificationsListener(ldapServer, syncPushNotificationsListener);
					}else{
						removePushNotificationsListenerInternal(ldapServer, syncPushNotificationsListener);
					}
				} catch (UserManagementSystemCommunicationException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		@Override
		public void preStore(InstanceLifecycleEvent event) {
			// do nothing
		}
	};
	
	
	/**
	 * {@link DeleteLifecycleListener} which removes {@link #syncPushNotificationsListener} from every deleted {@link LDAPServer}
	 */
	private DeleteLifecycleListener ldapServerDeleteListener = new DeleteLifecycleListener() {
		@Override
		public void preDelete(InstanceLifecycleEvent event) {
			if (event.getPersistentInstance() instanceof LDAPServer){
				LDAPServer ldapServer = (LDAPServer) event.getPersistentInstance();
				if (ldapServer.isLeading()){
					try {
						removePushNotificationsListenerInternal(ldapServer, syncPushNotificationsListener);
					} catch (UserManagementSystemCommunicationException e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
		}
		@Override
		public void postDelete(InstanceLifecycleEvent event) {
			// do nothing
		}
	};
	
	/**
	 * If this field is set than we assume that {@link PushNotificationsConfigurator} is initialized and configured correctly
	 */
	private String organisationID = null;

	/**
	 * Initialize {@link PushNotificationsConfigurator} instance. Should be called once after instance is created.
	 * If called multiple times on the same instance nothing will happen and it will return silently.
	 * 
	 * @param pm
	 * @param organisationId
	 */
	public void initialize(PersistenceManager pm, String organisationId){
		
		if (organisationID != null){	// already initialized
			logger.info("Push notifications already initialized for organisationID: " + organisationID);
			return;
		}
		
		pm.getPersistenceManagerFactory().addInstanceLifecycleListener(
				ldapServerStoreListener, new Class[]{LDAPServer.class}
				);
		pm.getPersistenceManagerFactory().addInstanceLifecycleListener(
				ldapServerDeleteListener, new Class[]{LDAPServer.class}
				);

		this.organisationID = organisationId;
		
		addPushNotificationListeners(pm);
		
		// If LDAP servers were offline while trying to add listeners - error will be logged and no listeners will be added.
		// That's why a timer task is configured for trying listers addition from time to time.
		configureTimerTask(pm);
	}
	
	/**
	 * Loads all leading {@link LDAPServer} instances and tries to add push notification listener to them.
	 * Listener is not added if was already added before. In case any exception occurs during addition it will be logged.
	 * 
	 * @param pm
	 */
	public void addPushNotificationListeners(PersistenceManager pm){
		// determine if there's any leading LDAPServers already existent
		Collection<LDAPServer> leadingSystems = UserManagementSystem.getUserManagementSystemsByLeading(
				pm, true, LDAPServer.class
				);
		for (LDAPServer ldapServer : leadingSystems) {
			try {
				addPushNotificationsListener(ldapServer, syncPushNotificationsListener);
			} catch (UserManagementSystemCommunicationException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * Add a given {@link NamingListener} to a given {@link LDAPServer}.
	 * 
	 * @param ldapServer to add listener to
	 * @param listener {@link NamingListener} to be added
	 */
	public void addPushNotificationListener(LDAPServer ldapServer, NamingListener listener){
		try {
			addPushNotificationsListener(ldapServer, listener);
		} catch (UserManagementSystemCommunicationException e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * Loads all non-leading {@link LDAPServer} instances and tries to remove push notification listener from them.
	 * In case any exception occurs during addition it will be logged.
	 * 
	 * @param pm
	 */
	public void removePushNotificationListeners(PersistenceManager pm){
		// determine if there's any leading LDAPServers already existent
		Collection<LDAPServer> leadingSystems = UserManagementSystem.getUserManagementSystemsByLeading(
				pm, false, LDAPServer.class
				);
		for (LDAPServer ldapServer : leadingSystems) {
			try {
				removePushNotificationsListenerInternal(ldapServer, syncPushNotificationsListener);
			} catch (UserManagementSystemCommunicationException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * Removes a given {@link NamingListener} from a given {@link LDAPServer} by its ID.
	 * 
	 * @param ldapServerID The ID of {@link LDAPServer} to remove listener from
	 * @param listener {@link NamingListener} to be removed
	 */
	public void removePushNotificationListener(UserManagementSystemID ldapServerID, NamingListener listener){
		try {
			removePushNotificationsListenerInternal(ldapServerID, listener);
		} catch (UserManagementSystemCommunicationException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void configureTimerTask(PersistenceManager pm){
		try {
			pm.getExtent(Task.class);
			TaskID taskID = TaskID.create(
					organisationID,
					Task.TASK_TYPE_ID_SYSTEM, CONFIGURE_PUSH_NOTIFICATION_LISTENERS_TASK_ID
					);
			Task task;
			try {
				task = (Task) pm.getObjectById(taskID);
				task.getActiveExecID();
			} catch (JDOObjectNotFoundException x) {
				task = new Task(
						taskID.organisationID, taskID.taskTypeID, taskID.taskID,
						User.getUser(pm, organisationID, User.USER_ID_SYSTEM),
						PushNotificationsManagerLocal.class,
						"configurePushNotificationListeners"
				);

				task.getName().setText(Locale.ENGLISH.getLanguage(), "configurePushNotificationListeners");
				task.getDescription().setText(
						Locale.ENGLISH.getLanguage(),
						"This Task tries to add push notification listener to existing leading LDAPServers."
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
			logger.error("Can't configure a timer task for push notification listener!", e);
		}
	}

	
	/**
	 * Listener which recieves notifications from LDAP and performs synchronization calls.
	 */
	class PushNotificationsListener implements ObjectChangeListener, NamespaceChangeListener{
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void objectChanged(NamingEvent event) {
			fetchLDAPEntry(event);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void objectAdded(NamingEvent event) {
			fetchLDAPEntry(event);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void objectRemoved(NamingEvent event) {
			removeJFireObject(event);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void objectRenamed(NamingEvent event) {
			if (event.getEventContext() == null){
				logger.warn("Can't proceed with fetching LDAP entry because source EventContext is null!");
				return;
			}
			if (event.getNewBinding() == null 
					|| event.getNewBinding().getNameInNamespace() == null 
					|| event.getNewBinding().getNameInNamespace().isEmpty()){
				logger.warn("Can't proceed with fetching LDAP entry because entry name is not specified!");
				return;
			}

			String newEntryName = event.getNewBinding().getNameInNamespace();
			EventContext eventContext = event.getEventContext();
			String ctxName = null;
			try {
				ctxName = eventContext.getNameInNamespace();
			} catch (NamingException e) {
				logger.error("Can't proceed with fetching LDAP entry!");
				logger.error(e.getMessage(), e);
				return;
			}
			
			if (ctxName != null && newEntryName.endsWith(ctxName)){	// new name is within the same parent
				
				fetchLDAPEntry(event);
				
			}else{	// we check if new name is within possible LDAP base entries (configured in LDAP scripts)
				
				logger.info("New name seems to be out of sync scope, will check LDAP parent entries...");
				
				// We need to be logged into JFire first
				JFireServerManager jsm = null;
				LoginContext loginContext = null;
				try{
					try{
						// check if there's some User logged in
						GlobalSecurityReflector.sharedInstance().getUserDescriptor();
					}catch(NoUserException e){
						// login with _System_ user
						try{
							
							if (logger.isDebugEnabled()){
								logger.debug("No user logged in, trying to log in with _System_ user...");
							}
							
							InitialContext initialContext = new InitialContext();
							J2EEAdapter j2eeAdapter = (J2EEAdapter) initialContext.lookup(J2EEAdapter.JNDI_NAME);
							JFireServerManagerFactory jsmf = (JFireServerManagerFactory) initialContext.lookup(JFireServerManagerFactory.JNDI_NAME);
							jsm = jsmf.getJFireServerManager();
							loginContext = j2eeAdapter.createLoginContext(
									LoginData.DEFAULT_SECURITY_PROTOCOL, 
									new AuthCallbackHandler(
											jsm, organisationID, User.USER_ID_SYSTEM,
											ObjectIDUtil.makeValidIDString(null, true)
											)
									);
							loginContext.login();
		
							if (logger.isDebugEnabled()){
								logger.debug("_System_ user logged in");
							}
							
						}catch(Exception ne){
							
							logger.error("Can't proceed with fetching LDAP entry!");
							logger.error(ne.getMessage(), ne);
							return;
							
						}
					}
					
					LDAPManagerLocal localBean = JFireEjb3Factory.getLocalBean(LDAPManagerLocal.class);
					Collection<String> ldapServerParentEntries = localBean.getLDAPServerParentEntries(getLDAPServerIDByEventContext(eventContext));
					
					boolean isInScope = false;
					for (String parentEntry : ldapServerParentEntries){
						if (newEntryName.endsWith(parentEntry)){
							isInScope = true;
							break;
						}
					}
					if (isInScope){
						logger.info("New name seems is in sync scope, so will proceed with fetching LDAP entry...");
						fetchLDAPEntry(event);
					}else{
						logger.info("Entry was renamed and now is out of synchronization scope! To be inside this scope it should have name ending with i.e. " + ctxName + " or another configured base parent entry.");
					}
					
				}finally{
					if (loginContext != null && loginContext.getSubject() != null){
						if (logger.isDebugEnabled()){
							logger.debug("_System_ user was logged in, loggin it out...");
						}
						try {
							loginContext.logout();
						} catch (LoginException e) {
							logger.error(e.getMessage(), e);
						}
					}
					if (jsm != null){
						jsm.close();
					}
				}
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void namingExceptionThrown(NamingExceptionEvent event) {
			Throwable t = event.getException();
			if (t != null){
				logger.error(t.getMessage(), t);
			}
			EventContext eventContext = event.getEventContext();
			if (eventContext != null){
				try {
					eventContext.addNamingListener(eventContext.getNameInNamespace(), EventContext.SUBTREE_SCOPE, this);
				} catch (NamingException e) {
					logger.error("Exception while re-adding push notification listener!", e);
				}
			}
		}
		
		
		private ErrorCallback errorCallback;
		private SuccessCallback successCallback;
		
		protected void setErrorCallback(ErrorCallback errorCallback) {
			this.errorCallback = errorCallback;
		}
		
		protected void setSuccessCallback(SuccessCallback successCallback) {
			this.successCallback = successCallback;
		}
		
		private void fetchLDAPEntry(NamingEvent event){
			if (event.getEventContext() == null){
				logger.warn("Can't proceed with fetching LDAP entry because source EventContext is null!");
				return;
			}
			
			String newName = event.getNewBinding().getNameInNamespace();
			if (event.getNewBinding() == null 
					|| newName == null 
					|| newName.isEmpty()){
				logger.warn("Can't proceed with fetching LDAP entry because entry name is not specified!");
				return;
			}
			
			String oldName = null;
			if (event.getOldBinding() != null){
				try{
					oldName = event.getOldBinding().getNameInNamespace();
				}catch(Exception e){	// it turned out that, for example, Apache DS returned no name in namespace, just a relative name
					oldName = event.getOldBinding().getName();
				}
				if (newName.equals(oldName)){
					oldName = null;
				}
			}
			if (oldName != null && !oldName.isEmpty()){
				// TODO: [Denis] If LDAP entry was renamed and corresponding JFire User already exists than we have several choices:
				// 1) Rename JFire User accordingly - but we can't change part of User primary key (userID)
				// 2) Delete existing user with an old name and let synchronization create a new one with the new name (we can tranfer Person from old User to new one)
				// 3) Do nothing, just log a warning - in this case both User with old name and User with new name will be present
				logger.warn(String.format("It seems that LDAP entry was renamed from %s to %s. Since we can't replace userID of existing JFire User new User object with a new name will be created.", oldName, newName));
			}
			
			LDAPSyncEvent syncEvent = new LDAPSyncEvent(LDAPSyncEventType.FETCH);
			syncEvent.setFetchEventTypeDataUnits(CollectionUtil.createHashSet(new FetchEventTypeDataUnit(newName)));
			try {
				AsyncInvoke.exec(
						new LDAPSyncInvocation(getLDAPServerIDByEventContext(event.getEventContext()), syncEvent),
						successCallback, errorCallback, null, false);
			} catch (AsyncInvokeEnqueueException e) {
				logger.error(e.getMessage(), e);
			}
		}

		private void removeJFireObject(NamingEvent event){
			if (event.getEventContext() == null){
				logger.warn("Can't proceed with removing JFire object because source EventContext is null!");
				return;
			}
			
			String name = event.getOldBinding().getNameInNamespace();
			if (event.getOldBinding() == null 
					|| name == null 
					|| name.isEmpty()){
				logger.warn("Can't proceed with removing JFire object because entry name is not specified!");
				return;
			}
			
			LDAPSyncEvent syncEvent = new LDAPSyncEvent(LDAPSyncEventType.FETCH_DELETE);
			syncEvent.setFetchEventTypeDataUnits(CollectionUtil.createHashSet(new FetchEventTypeDataUnit(name)));
			try {
				AsyncInvoke.exec(
						new LDAPSyncInvocation(getLDAPServerIDByEventContext(event.getEventContext()), syncEvent),
						successCallback, errorCallback, null, false);
			} catch (AsyncInvokeEnqueueException e) {
				logger.error(e.getMessage(), e);
			}
		}

	}
	
	private NamingListener syncPushNotificationsListener = new PushNotificationsListener();
	
	
	/**
	 * For unit-test purposes only!
	 * 
	 * @param successCallback
	 * @param errorCallback
	 */
	public void setCallbacks(SuccessCallback successCallback, ErrorCallback errorCallback){
		((PushNotificationsListener)syncPushNotificationsListener).setErrorCallback(errorCallback);
		((PushNotificationsListener)syncPushNotificationsListener).setSuccessCallback(successCallback);
	}
	
	
	/**
	 * Map for holding {@link EventContext} instances which have some {@link NamingListener} added to them.
	 * 
	 * FIXME: It could be not the best choice (for clustering issues) to hold a this map here in a single static instance, but I don't see another solution at the moment. Denis.
	 * Here's a quote from {@link EventContext} javadoc:
	 *  
	 * 		Furthermore, listener registration/deregistration is with the EventContext instance, and not with the corresponding object 
	 * 		in the namespace. If the program intends at some point to remove a listener, then it needs to keep a reference to the 
	 * 		EventContext instance on which it invoked addNamingListener() (just as it needs to keep a reference to the listener in order 
	 * 		to remove it later). It cannot expect to do a lookup() and get another instance of a EventContext on which to perform the 
	 * 		deregistration.
	 * 
	 */
	private Map<UserManagementSystemID, Collection<EventContext>> registeredContexts = new HashMap<UserManagementSystemID, Collection<EventContext>>();
	
	private void addPushNotificationsListener(LDAPServer ldapServer, NamingListener listener) throws UserManagementSystemCommunicationException{
		if (listener == null){
			throw new IllegalArgumentException("NamingListener to be added can't be null!");
		}
		UserManagementSystemID ldapServerID = ldapServer.getUserManagementSystemObjectID();
		try{
			Collection<String> parentEntriesForSync = ldapServer.getLdapScriptSet().getParentEntriesForSync();
			if (!parentEntriesForSync.isEmpty()){
				
				// check if listener was already added for this LDAPServer
				if (registeredContexts.containsKey(ldapServerID)){
					Collection<EventContext> contexts = registeredContexts.get(ldapServerID);
					Collection<String> contextNames = new HashSet<String>();
					for (EventContext ctx : contexts){
						contextNames.add(ctx.getNameInNamespace());
					}
					if (contextNames.containsAll(parentEntriesForSync)){
						if (logger.isDebugEnabled()){
							logger.debug("Push notifications listener already added for all parent entries of this LDAPServer, returning. LDAPServer ID: " + ldapServerID.toString());
						}
						return;
					}else{
						removePushNotificationsListenerInternal(ldapServer, listener);
					}
				}

				Hashtable<String, String> environment = new Hashtable<String, String>();
				environment.put(JNDIConnectionWrapper.JAVA_NAMING_LDAP_VERSION, "3"); //$NON-NLS-1$
				environment.put(JNDIConnectionWrapper.COM_SUN_JNDI_LDAP_CONNECT_TIMEOUT, "10000"); //$NON-NLS-1$
				environment.put(JNDIConnectionWrapper.COM_SUN_JNDI_DNS_TIMEOUT_INITIAL, "2000"); //$NON-NLS-1$
				environment.put(JNDIConnectionWrapper.COM_SUN_JNDI_DNS_TIMEOUT_RETRIES, "3"); //$NON-NLS-1$
				environment.put(Context.PROVIDER_URL, JNDIConnectionWrapper.LDAP_SCHEME + ldapServer.getHost() + ':' + ldapServer.getPort());
				environment.put(Context.INITIAL_CONTEXT_FACTORY, JNDIConnectionWrapper.getDefaultLdapContextFactory());

				if (logger.isDebugEnabled()){
					logger.debug("Connecting to LDAP server with params: " + environment.toString());
				}
				
				InitialLdapContext context = new InitialLdapContext(environment, null);
				
				for (String parentEntry : parentEntriesForSync){
					Object ctx = null;
					
					try{	// check if anonymous access to LDAP directory is enabled
						ctx = context.lookup(parentEntry);
					}catch(NoPermissionException e){	// specify bind login/password otherwise
						
						boolean successful = bindContextWithCurrentUser(context, ldapServer);
						if (!successful){
							logger.info("Unable to bind with current JFire user, will try global syncUser.");
							if (ldapServer.getSyncDN() != null
									&& ldapServer.getSyncPassword() != null){
								bindContext(context, ldapServer.getAuthenticationMethod().stringValue(), ldapServer.getSyncDN(), ldapServer.getSyncPassword());
							}else{
								
								logger.error("Unable to bind with global syncUser because it's not set. Push notifications listener will NOT be added.");
								return;
								
							}
						}

						ctx = context.lookup(parentEntry);
					}
					
					if (ctx instanceof EventContext){
						synchronized (registeredContexts) {
							EventContext eventContext = (EventContext) ctx;
							eventContext.addNamingListener(
									parentEntry, EventContext.SUBTREE_SCOPE, listener);
							
							if (registeredContexts.containsKey(ldapServerID)){
								registeredContexts.get(ldapServerID).add(eventContext);
							}else{
								registeredContexts.put(ldapServerID, CollectionUtil.createHashSet(eventContext));
							}
						}
					}
				}
				
			}
		} catch (ScriptException e) {
			throw new UserManagementSystemCommunicationException(
					"Can't get parent entries names from LDAPServer with ID: " + ldapServerID.toString(), e);
		} catch (NamingException e) {
			throw new UserManagementSystemCommunicationException(
					"Can't add push notification listener to LDAPServer with ID: " + ldapServerID.toString(), e);
		}
	}
	
	private boolean bindContextWithCurrentUser(InitialLdapContext context, LDAPServer ldapServer) throws ScriptException{
		logger.info("Trying to bind against LDAP with current user.");
		try{
			UserDescriptor userDescriptor = GlobalSecurityReflector.sharedInstance().getUserDescriptor();
			if (logger.isDebugEnabled()){
				logger.debug("Current user is: " + userDescriptor.getCompleteUserID());
			}
			
			if (User.USER_ID_SYSTEM.equals(userDescriptor.getUserID())){
				logger.info("Current user is a JFire system user, can't bind it against LDAP");
				return false;
			}
			
			Object credential = GlobalSecurityReflector.sharedInstance().getCredential();
			String pwd = null;
			if (credential instanceof String){
				pwd = (String) credential;
			}else if (credential instanceof char[]){
				pwd = new String((char[]) credential);
			}else{
				logger.warn("Cannot get string password for current user: " + userDescriptor.getCompleteUserID());
				return false;
			}
			
			if (logger.isDebugEnabled()){
				logger.debug("Got password for user, trying to bind: " + userDescriptor.getCompleteUserID());
			}
			
			String userDN = ldapServer.getLdapScriptSet().getLdapDN(
					new User(userDescriptor.getOrganisationID(), userDescriptor.getUserID()));

			if (logger.isDebugEnabled()){
				logger.debug("LDAP entry name for current user is: " + userDN);
			}
			
			try{
				bindContext(context, ldapServer.getAuthenticationMethod().stringValue(), userDN, pwd);
				return true;
			}catch(NamingException ne){
				logger.warn("Bind failed with current user: " + userDescriptor.getCompleteUserID(), ne);
				return false;
			}
			
		}catch(NoUserException ex){
			logger.info("No current user. Will try global syncUser if set.");
			return false;
		}
	}
	
	private void bindContext(InitialLdapContext context, String authMethod, String user, String pwd) throws NamingException{
		context.addToEnvironment(Context.SECURITY_AUTHENTICATION, authMethod);
		context.addToEnvironment(Context.SECURITY_PRINCIPAL, user);
		context.addToEnvironment(Context.SECURITY_CREDENTIALS, pwd);

		context.reconnect(context.getConnectControls());
	}
	
	private void removePushNotificationsListenerInternal(LDAPServer ldapServer, NamingListener listener) throws UserManagementSystemCommunicationException{
		UserManagementSystemID ldapServerID = ldapServer.getUserManagementSystemObjectID();
		removePushNotificationListener(ldapServerID, listener);
	}

	private void removePushNotificationsListenerInternal(UserManagementSystemID ldapServerID, NamingListener listener) throws UserManagementSystemCommunicationException{
		if (registeredContexts.containsKey(ldapServerID)){
			try{
				synchronized (registeredContexts) {
					Collection<EventContext> contexts = registeredContexts.get(ldapServerID);
					for (EventContext ctx : contexts){
						ctx.removeNamingListener(listener);
						ctx.close();
					}
					registeredContexts.remove(ldapServerID);
				}
			} catch (NamingException e){
				throw new UserManagementSystemCommunicationException(
						"Can't remove push notification listener from LDAPServer with ID: " + ldapServerID.toString(), e);
			}
		}
	}

	/**
	 * Get ID of {@link LDAPServer} by given {@link EventContext}.
	 * 
	 * @param ctx
	 * @return ID of {@link LDAPServer} or <code>null</code> if not found
	 */
	public UserManagementSystemID getLDAPServerIDByEventContext(EventContext ctx){
		for (UserManagementSystemID ldapServerID : registeredContexts.keySet()){
			Collection<EventContext> contexts = registeredContexts.get(ldapServerID);
			if (contexts.contains(ctx)){
				return ldapServerID;
			}
		}
		return null;
	}
	
	/**
	 * Get {@link Collection} of {@link EventContext}s which has registered listeners.
	 * 
	 * @param ldapServerID
	 * @return
	 */
	public Collection<EventContext> getEventContextsWithListenersByLDAPServerID(UserManagementSystemID ldapServerID){
		return registeredContexts.get(ldapServerID);
	}

}
