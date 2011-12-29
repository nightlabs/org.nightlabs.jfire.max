package org.nightlabs.jfire.base.security.integration.ldap.sync;

import java.io.Serializable;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.script.ScriptException;
import javax.security.auth.login.LoginException;

import org.nightlabs.jfire.asyncinvoke.AsyncInvoke;
import org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnqueueException;
import org.nightlabs.jfire.asyncinvoke.Invocation;
import org.nightlabs.jfire.base.security.integration.ldap.LDAPServer;
import org.nightlabs.jfire.base.security.integration.ldap.connection.ILDAPConnectionParamsProvider;
import org.nightlabs.jfire.base.security.integration.ldap.connection.ILDAPConnectionParamsProvider.AuthenticationMethod;
import org.nightlabs.jfire.base.security.integration.ldap.connection.LDAPConnection;
import org.nightlabs.jfire.base.security.integration.ldap.connection.LDAPConnectionManager;
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
	
	/**
	 * Preserves {@link LDAPConnection} and executes given {@link Invocation}.
	 * 
	 * @param pm The {@link PersistenceManager} to use
	 * @param ldapServer {@link LDAPServer} to connect to
	 * @throws AsyncInvokeEnqueueException 
	 */
	public static void executeWithPreservedLDAPConnection(
			PersistenceManager pm, Invocation invocation, LDAPServer ldapServer) throws AsyncInvokeEnqueueException{
		
		LDAPConnection connection = null;
		if (!AuthenticationMethod.NONE.equals(ldapServer.getAuthenticationMethod())){

			// We preserve an authenticated instance of LDAPConnection before executing an Invocation
			// because it will be impossible to authenticate against LDAP inside the invocation 
			// in case of simple clean-password authentication.
			// See https://www.jfire.org/modules/bugs/view.php?id=1974 for details.
			connection = preserveConnection(pm, ldapServer);
			
		}
		
		try{
			AsyncInvoke.exec(invocation, true);
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
			logger.error("No User logged in while preserving LDAPConnection!");
			return null;
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
			String bindDN = ldapServer.getLdapScriptSet().getLdapDN(user);
			String bindPwd = LDAPServer.getLDAPPasswordForCurrentUser();

			if (ldapServer.canBind(bindDN, bindPwd)){
				connection.bind(bindDN, bindPwd);
			}else{
				logger.warn(
						String.format("LDAPConnection could not be authenticated for DN %s. Either name or password is empty. No connection will be available inside sync invocation!", bindDN)
						);
				return null;
			}
			
			LDAPConnectionManager.sharedInstance().preservePrivateLDAPConnection(ldapServer, connection);
			
			return connection;
			
		} catch (UserManagementSystemCommunicationException e) {
			exceptionOccured = true;
			logger.error("Exception while trying to preserve a connection! No connection will be available inside sync invocation!", e);
		} catch (LoginException e) {
			exceptionOccured = true;
			logger.error("LDAPConnection could not be authenticated! No connection will be available inside sync invocation!", e);
		} catch (ScriptException e) {
			exceptionOccured = true;
			logger.error("Can't get userDN to bind the connection! No connection will be available inside sync invocation!", e);
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
}

