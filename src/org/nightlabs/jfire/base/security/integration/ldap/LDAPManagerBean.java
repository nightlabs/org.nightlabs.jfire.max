package org.nightlabs.jfire.base.security.integration.ldap;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jdo.Extent;
import javax.jdo.JDOHelper;
import javax.jdo.JDOUserCallbackException;
import javax.jdo.PersistenceManager;
import javax.jdo.listener.InstanceLifecycleEvent;
import javax.jdo.listener.StoreLifecycleListener;

import org.jboss.annotation.security.SecurityDomain;
import org.nightlabs.jfire.asyncinvoke.AsyncInvoke;
import org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnqueueException;
import org.nightlabs.jfire.asyncinvoke.Invocation;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.base.security.integration.ldap.LDAPSyncEvent.LDAPSyncEventType;
import org.nightlabs.jfire.base.security.integration.ldap.connection.ILDAPConnectionParamsProvider.EncryptionMethod;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.security.GlobalSecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.integration.UserManagementSystemType;
import org.nightlabs.jfire.server.data.dir.JFireServerDataDirectory;
import org.nightlabs.util.CollectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
@Stateless(mappedName = "LDAPManager")
@SecurityDomain(value="jfire")
public class LDAPManagerBean extends BaseSessionBeanImpl implements LDAPManagerRemote {
	
	private static final Logger logger = LoggerFactory.getLogger(LDAPManagerBean.class);
	
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	@Override
	public void initialise() {
		
		PersistenceManager pm = createPersistenceManager();

		try{
			// creating/loading InetOrgPersonLDAPServerType single instance
			InetOrgPersonLDAPServerType.createSingleInstance(pm, "InetOrgPersonLDAPServerType");
			
			// server creation via configuration property files
			initLDAPServersFromPropertiesFile(pm);
			
			// run sync configuration only once at startup because it's unlikely 
			// that leading system scenario will be changed at server runtime
			configureSynchronization(pm);
			
		}finally{
			pm.close();
		}
		
	}
	
	private static void configureSynchronization(PersistenceManager pm){

		// determine who is the leading system, LDAP or JFire
		boolean jfireAsLeadingSystem = true;	// FIXME: read property value from configuration
												// Where could be the most appropriate place in JFire 
												// for holding this kind of configuration? Denis. 
		
		if (jfireAsLeadingSystem){
			
			pm.getPersistenceManagerFactory().addInstanceLifecycleListener(
					syncStoreLifecycleListener, new Class[]{User.class, Person.class}
					);
			
		}else{
			
			// TODO: LDAP as a leading system scenario is still to be done
			// 1. using timer
			// 2. using push notifications from LDAP server
			
		}
	}

	private static StoreLifecycleListener syncStoreLifecycleListener = new SyncStoreLifecycleListener();
	
	static class SyncStoreLifecycleListener implements StoreLifecycleListener{
		
		@Override
		public void postStore(InstanceLifecycleEvent event) {
			
			try{
				GlobalSecurityReflector.sharedInstance().getUserDescriptor();
			}catch(Exception e){	// no user logged in - return
				return;
			}
			
			try {
				AsyncInvoke.exec(
						new SyncToLDAPServersInvocation(JDOHelper.getObjectId(event.getPersistentInstance())), true
						);
			} catch (AsyncInvokeEnqueueException e) {
				throw new JDOUserCallbackException("Unable to synhronize User data to LDAP server(s)!", e);
			}
		}

		@Override
		public void preStore(InstanceLifecycleEvent event) {
			// do nothing
		}
		
	}
	
	public static class SyncToLDAPServersInvocation extends Invocation{
		
		private static final long serialVersionUID = 1L;
		
		private Object objectId;
		
		public SyncToLDAPServersInvocation(Object objectId){
			this.objectId = objectId;
		}

		@Override
		public Serializable invoke() throws Exception {
			
			PersistenceManager pm = createPersistenceManager();
			try{
				
				// call to getExtent with second parameter set to true just in case some classes
				// will appear extending an LDAPServer
				Extent<LDAPServer> ldapServersExtent = pm.getExtent(LDAPServer.class, true);
				
				boolean exceptionOccured = false;
				for (Iterator<LDAPServer> iterator = ldapServersExtent.iterator(); iterator.hasNext();) {
					
					LDAPServer ldapServer = iterator.next();
					
					LDAPSyncEvent syncEvent = new LDAPSyncEvent(LDAPSyncEventType.SEND);
					syncEvent.setOrganisationID(getOrganisationID());
					syncEvent.setJFireObjectsIds(CollectionUtil.createHashSet(objectId));
					try{
						ldapServer.synchronize(syncEvent);
					}catch(Exception e){
						// catch all exceptions here without immidiate rethrowing or other reaction
						// because we need to execute synchronization for all other servers despite  
						// to the exception at particular server synchronization
						exceptionOccured = true;
						logger.error(e.getMessage(), e);
					}
					
					
				}
				
				if(exceptionOccured){
					throw new LDAPSyncException("Exception(s) occured during synchronizing User data to LDAP server(s). Please see log for details.");
				}

				return null;
				
			}finally{
				pm.close();
			}
		}
	}

	
	private static final String PROP_LDAP_SERVER_TYPE_CLASS_NAME = "ldapServer%s.typeClassName";
	private static final String PROP_LDAP_SERVER_IS_ACTIVE = "ldapServer%s.isActive";
	private static final String PROP_LDAP_SERVER_ENCRYPTION_METHOD = "ldapServer%s.encryptionMethod";
	private static final String PROP_LDAP_SERVER_PORT = "ldapServer%s.port";
	private static final String PROP_LDAP_SERVER_HOST = "ldapServer%s.host";
	private static final String PROP_LDAP_SERVER_NAME = "ldapServer%s.name";
	private static final String PROP_LDAP_SERVER_SYNC_DN = "ldapServer%s.syncDN";
	private static final String PROP_LDAP_SERVER_SYNC_PASSWORD = "ldapServer%s.syncPassword";
	private static final String PROP_LDAP_REMOVE_SERVER_INSTANCE = "ldapServer%s.remove";

	private static final int LDAP_DEFAULT_PORT = 10389;
	private static final String LDAP_DEFAULT_HOST = "localhost";
	private static final EncryptionMethod LDAP_DEFAULT_ENCRYPTION_METHOD = EncryptionMethod.NONE;

	/**
	 * <p>Reads a property file "ldap.properties" from  JFire server data directory (i.e. "/server/default/data/jfire") 
	 * and creates, modifies or removes {@link LDAPServer) instances according to specified properties' values.</p>
	 * 
	 * <pre>Example:
	 * ldapServerN.typeClassName=org.nightlabs.jfire.base.security.integration.ldap.InetOrgPersonLDAPServerType
	 * ldapServerN.name=local test LDAP server
	 * ldapServerN.isActive=true
	 * ldapServerN.syncDN=uid=sync_user,ou=staff,ou=people,dc=nightlabs,dc=de
	 * ldapServerN.syncPassword=1111
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
					
					String host = ldapProps.getProperty(String.format(PROP_LDAP_SERVER_HOST, i));
					if (host == null || "".equals(host)){
						host = LDAP_DEFAULT_HOST;
					}
					
					int port = 0;
					try{
						port = Integer.parseInt(ldapProps.getProperty(String.format(PROP_LDAP_SERVER_PORT, i)));
					}catch(NumberFormatException e){
						port = LDAP_DEFAULT_PORT;
					}
					
					EncryptionMethod encryptionMethod = null;
					try{
						encryptionMethod = EncryptionMethod.valueOf(EncryptionMethod.class, ldapProps.getProperty(String.format(PROP_LDAP_SERVER_ENCRYPTION_METHOD, i)));
					}catch(Exception e){
						encryptionMethod = LDAP_DEFAULT_ENCRYPTION_METHOD;
					}
					
					boolean isActive = false;
					if (ldapProps.getProperty(String.format(PROP_LDAP_SERVER_IS_ACTIVE, i)) != null){
						isActive = Boolean.parseBoolean(
								ldapProps.getProperty(
										String.format(PROP_LDAP_SERVER_IS_ACTIVE, i)).toLowerCase()
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
