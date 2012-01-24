package org.nightlabs.jfire.base.security.integration.ldap.sync;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.security.auth.login.LoginException;

import org.nightlabs.jfire.asyncinvoke.AsyncInvoke;
import org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnqueueException;
import org.nightlabs.jfire.asyncinvoke.Invocation;
import org.nightlabs.jfire.base.security.integration.ldap.LDAPServer;
import org.nightlabs.jfire.base.security.integration.ldap.connection.ILDAPConnectionParamsProvider;
import org.nightlabs.jfire.base.security.integration.ldap.connection.ILDAPConnectionParamsProvider.AuthenticationMethod;
import org.nightlabs.jfire.base.security.integration.ldap.connection.LDAPConnection;
import org.nightlabs.jfire.base.security.integration.ldap.connection.LDAPConnectionManager;
import org.nightlabs.jfire.base.security.integration.ldap.connection.LDAPConnectionManager.IPreservedConnectionListener;
import org.nightlabs.jfire.security.GlobalSecurityReflector;
import org.nightlabs.jfire.security.NoUserException;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserDescriptor;
import org.nightlabs.jfire.security.integration.UserManagementSystemCommunicationException;
import org.nightlabs.jfire.security.integration.id.UserManagementSystemID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This invocation is used for synchronizing between JFire and {@link LDAPServer}. Synchronization is configured by given {@link LDAPSyncEvent}.
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
public class LDAPSyncInvocation extends Invocation{

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(LDAPSyncInvocation.class);

	private UserManagementSystemID ldapServerID;
	private LDAPSyncEvent ldapSyncEvent;
	
	/**
	 * Construct a {@link LDAPSyncInvocation} which will run synchronization on {@link LDAPServer} with given {@link #ldapServerID} 
	 * configured with {@link #ldapSyncEvent}.
	 * 
	 * @param ldapServerID
	 * @param ldapSyncEvent
	 */
	public LDAPSyncInvocation(UserManagementSystemID ldapServerID, LDAPSyncEvent ldapSyncEvent){
		this.ldapServerID = ldapServerID;
		this.ldapSyncEvent = ldapSyncEvent;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Serializable invoke() throws Exception {
		
		if (ldapServerID == null || ldapSyncEvent == null){
			return null;
		}
		
		PersistenceManager pm = createPersistenceManager();
		try{
			
			LDAPServer ldapServer = null;
			try{
				ldapServer = (LDAPServer) pm.getObjectById(ldapServerID);
			}catch(JDOObjectNotFoundException e){
				logger.error(
						String.format("LDAPServer with ID %s is not found! Cannot proceed with invocation!", ldapServerID.toString()));
				return null;
			}
			if (logger.isDebugEnabled()){
				logger.debug(
						String.format("Running %s synchronization for LDAPServer at %s:%s", 
								ldapSyncEvent.getEventType().stringValue(), ldapServer.getHost(), ldapServer.getPort()));
			}
			ldapServer.synchronize(ldapSyncEvent);
			
			return null;
			
		}finally{
			pm.close();
		}
	}
	
	private static Collection<LDAPSyncInvocationDescriptor> invocationsInProgress = Collections.synchronizedSet(new HashSet<LDAPSyncInvocationDescriptor>());
	
	/**
	 * Preserves {@link LDAPConnection} and executes new {@link LDAPSyncInvocation} with given {@link LDAPSyncEvent}.
	 * As this method makes use of preserved (private) {@link LDAPConnection}s which could be used only once per {@link User}
	 * and {@link LDAPServer}, it also controls that no invocation will be executed for the same {@link LDAPServer}, the same {@link User}
	 * and the same object being synchronized if such an invocation was started already earlier, preserved {@link LDAPConnection} for itself
	 * and still did NOT make use of this preserved {@link LDAPConnection} which means that sync has not been started. 
	 * As soon as preserved connection was used by invocation started earlier it is allowed to execute any invocation with 
	 * private connections again. 
	 * 
	 * @param pm The {@link PersistenceManager} to use
	 * @param syncEvent {@link LDAPSyncEvent} with all synchronization related data
	 * @param ldapServer {@link LDAPServer} to connect to
	 * @throws AsyncInvokeEnqueueException 
	 */
	public static void executeWithPreservedLDAPConnection(
			PersistenceManager pm, LDAPSyncEvent syncEvent, LDAPServer ldapServer) throws AsyncInvokeEnqueueException{

		final LDAPSyncInvocationDescriptor invocationDescriptor = new LDAPSyncInvocationDescriptor(syncEvent, ldapServer);
		if (invocationsInProgress.contains(invocationDescriptor)){
			
			logger.info(
					String.format(
							"No LDAPInvocation will be started for sync event %s, LDAPServer %s and User %s cause similiar one is running already and all changes to objects should be processed by it.", 
							syncEvent.toString(), ldapServer.getUserManagementSystemObjectID().toString(), GlobalSecurityReflector.sharedInstance().getUserDescriptor().getCompleteUserID()));
			return;
		}
		
		LDAPConnection connection = null;
		if (!AuthenticationMethod.NONE.equals(ldapServer.getAuthenticationMethod())){

			// We preserve an authenticated instance of LDAPConnection before executing an Invocation
			// because it will be impossible to authenticate against LDAP inside the invocation 
			// in case of simple clean-password authentication.
			// See https://www.jfire.org/modules/bugs/view.php?id=1974 for details.
			connection = preserveConnection(pm, ldapServer);
			
			if (connection != null){
				invocationsInProgress.add(invocationDescriptor);
				boolean listenerAdded = false;
				try{
					listenerAdded = LDAPConnectionManager.sharedInstance().addPreservedConnectionListener(connection, new IPreservedConnectionListener() {
						@Override
						public void connectionRecieved() {
							invocationsInProgress.remove(invocationDescriptor);
						}
					});
				}finally{
					if (!listenerAdded){
						// We should remove invocationDescriptor from the invocationsInProgress if listener was not added successfully,
						// because otherwise it will cause the case when no similiar invocations could be ever executed
						logger.warn("Preserved connection listener was not added so instantly removing previously added invocation descriptor!");
						invocationsInProgress.remove(invocationDescriptor);
					}
				}
			}
		}
		
		try{
			AsyncInvoke.exec(new LDAPSyncInvocation(ldapServer.getUserManagementSystemObjectID(), syncEvent), true);
		}catch(Exception e){
			try{
				if (connection != null){
					connection.unbind();
				}
			} catch (UserManagementSystemCommunicationException ex) {
				// do nothing
			}finally{
				LDAPConnectionManager.sharedInstance().releaseConnection(connection);
			}
			
			AsyncInvokeEnqueueException asyncInvokeEnqueueException = null;
			if (e instanceof AsyncInvokeEnqueueException){
				asyncInvokeEnqueueException = (AsyncInvokeEnqueueException) e;
			}else{
				asyncInvokeEnqueueException = new AsyncInvokeEnqueueException(e);
			}
			throw asyncInvokeEnqueueException;
		}
	}
	
	/**
	 * Obtains {@link LDAPConnection} instance from {@link LDAPConnectionManager} pool, binds it with current {@link User}'s
	 * credentials and preserves it back to {@link LDAPConnectionManager} for later usage. 
	 * See {@link LDAPConnectionManager#preservePrivateLDAPConnection(ILDAPConnectionParamsProvider, LDAPConnection)} for details.
	 * 
	 * @param pm The {@link PersistenceManager} to use
	 * @param ldapServer {@link LDAPServer} to connect to
	 */
	public static LDAPConnection preserveConnection(PersistenceManager pm, LDAPServer ldapServer){
		UserDescriptor userDescriptor = null;
		LDAPConnection connection = null;
		
		try{
			userDescriptor = GlobalSecurityReflector.sharedInstance().getUserDescriptor();
		}catch(NoUserException e){
			throw new IllegalStateException("No User is logged in! Makes no sense as no Invocation could be executed with no User.");
		}
		
		boolean exceptionOccured = false;
		try {
			connection = LDAPConnectionManager.sharedInstance().getConnection(ldapServer);
			
			User user = null;
			if (pm != null){
				user = userDescriptor.getUser(pm);
			}else{	// create fake, not persisted new User just to get LDAP entry DN
				user = new User(userDescriptor.getOrganisationID(), userDescriptor.getUserID());
			}

			ldapServer.bindConnection(connection, user, LDAPServer.getLDAPPasswordForCurrentUser());
			
			LDAPConnectionManager.sharedInstance().preservePrivateLDAPConnection(connection);
			
			return connection;
			
		} catch (UserManagementSystemCommunicationException e) {
			exceptionOccured = true;
			logger.error("Exception while trying to preserve a connection! No connection will be available inside sync invocation!", e);
		} catch (LoginException e) {
			exceptionOccured = true;
			logger.error("LDAPConnection could not be authenticated! No connection will be available inside sync invocation!", e);
		} catch (Exception e){
			exceptionOccured = true;
			throw new RuntimeException(e);
		} finally {
			if (exceptionOccured){
				try {
					connection.unbind();
				} catch (UserManagementSystemCommunicationException e) {
					// do nothing, this exception does not matter for us
				}
				LDAPConnectionManager.sharedInstance().releaseConnection(connection);
			}
		}
		return null;
	}
	
	private static class LDAPSyncInvocationDescriptor{
		private LDAPSyncEvent syncEvent;
		private LDAPServer ldapServer;
		private UserDescriptor userDescriptor;
		public LDAPSyncInvocationDescriptor(LDAPSyncEvent syncEvent, LDAPServer ldapServer) {
			this.syncEvent = syncEvent;
			this.ldapServer = ldapServer;
			try{
				this.userDescriptor = GlobalSecurityReflector.sharedInstance().getUserDescriptor();
			}catch(NoUserException e){
				throw new IllegalStateException("No User is logged in! Makes no sense as no Invocation could be executed with no User.");
			}
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((ldapServer == null) ? 0 : ldapServer.hashCode());
			result = prime * result
					+ ((syncEvent == null) ? 0 : syncEvent.hashCode());
			result = prime
					* result
					+ ((userDescriptor == null) ? 0 : userDescriptor.hashCode());
			return result;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			LDAPSyncInvocationDescriptor other = (LDAPSyncInvocationDescriptor) obj;
			if (ldapServer == null) {
				if (other.ldapServer != null)
					return false;
			} else if (!ldapServer.equals(other.ldapServer))
				return false;
			if (syncEvent == null) {
				if (other.syncEvent != null)
					return false;
			} else if (!syncEvent.equals(other.syncEvent))
				return false;
			if (userDescriptor == null) {
				if (other.userDescriptor != null)
					return false;
			} else if (!userDescriptor.equals(other.userDescriptor))
				return false;
			return true;
		}
	}
}

