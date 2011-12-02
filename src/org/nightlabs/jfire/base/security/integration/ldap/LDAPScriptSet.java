package org.nightlabs.jfire.base.security.integration.ldap;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

import javax.jdo.JDOHelper;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.nightlabs.jfire.base.security.integration.ldap.attributes.LDAPAttributeSet;
import org.nightlabs.jfire.base.security.integration.ldap.id.LDAPScriptSetID;
import org.nightlabs.jfire.base.security.integration.ldap.scripts.LDAPScriptUtil;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.id.UserID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * <p>One instance of LDAPScriptSet belongs to exactly one instance of {@link LDAPServer}. 
 * It is not planned to share them across multiple LDAPServer instances.</p>
 * 
 * <p>That's why an LDAPScriptSet has no name (it does not need to be shown in a UI).
 * It merely keeps all the LDAP-query/command-stuff together in a nice, object-oriented way 
 * and capsules logic to prepare the scripts before execution (e.g. replace variables).</p>
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
@PersistenceCapable(
		objectIdClass=LDAPScriptSetID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireLDAP_LDAPScriptSet")
public class LDAPScriptSet implements Serializable{
	
	public static final String BASE_USER_ENTRY_NAME_PLACEHOLDER = "BASE_USER_ENTRY_NAME_PLACEHOLDER";
	public static final String BASE_GROUP_ENTRY_NAME_PLACEHOLDER = "BASE_GROUP_ENTRY_NAME_PLACEHOLDER";
	
	private static final Logger logger = LoggerFactory.getLogger(LDAPScriptSet.class);
	
	/**
	 * The serial version UID of this class.
	 */
	private static final long serialVersionUID = 1L;

	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	private long ldapScriptSetID;

	/**
	 * Bind variables (i.e. $userID$ or $personEmail$) to JFire objects data.
	 */
	@Persistent
	@Column(sqlType="CLOB")
	private String bindVariablesScript;
	
	/**
	 * Generates and returns an LDAP DN using {@code bindVariablesScript).
	 */
	@Persistent
	@Column(sqlType="CLOB")
	private String ldapDNScript;
	
	/**
	 * Performs synchronization from LDAP directory to JFire(creating or modifying User/Person objects)
	 */
	@Persistent
	@Column(sqlType="CLOB")
	private String syncLdapToJFireScript;
	
	/**
	 * Generates and returns a {@link LDAPAttributeSet} of attributes to be stored in LDAP directory using {@code bindVariablesScript).
	 */
	@Persistent
	@Column(sqlType="CLOB")
	private String generateJFireToLdapAttributesScript;

	/**
	 * Generates and returns a collection of names for parent LDAP entries which hold all entries that
	 * should be synchronized to JFire objects.
	 */
	@Persistent
	@Column(sqlType="CLOB")
	private String generateParentLdapEntriesScript;

	@Persistent(defaultFetchGroup="true")
	private LDAPServer ldapServer;
	
	/**
	 * @deprecated For JDO only!
	 */
	@Deprecated
	public LDAPScriptSet(){}
	
	/**
	 * Default constructor for creating LDAPSCriptSet objects
	 * @param server
	 */
	public LDAPScriptSet(LDAPServer server){
		this.ldapScriptSetID = server.getUserManagementSystemID();
		this.organisationID = server.getOrganisationID();
		this.ldapServer = server;
	}
	
	/**
	 * Set {@link #ldapDNScript} contents
	 * @param ldapDN
	 */
	public void setLdapDNScript(String ldapDN) {
		this.ldapDNScript = ldapDN;
	}
	
	/**
	 * Get {@link #ldapDNScript} contents
	 * @return script contents as {@link String}
	 */
	public String getLdapDNScript() {
		return ldapDNScript;
	}
	
	/**
	 * Set {@link #generateJFireToLdapAttributesScript} content
	 * @param syncJFireToLdapScript
	 */
	public void setGenerateJFireToLdapAttributesScript(String syncJFireToLdapScript) {
		this.generateJFireToLdapAttributesScript = syncJFireToLdapScript;
	}
	
	/**
	 * Get {@link #generateJFireToLdapAttributesScript} contents
	 * @return script contents as {@link String}
	 */
	public String getGenerateJFireToLdapAttributesScript() {
		return generateJFireToLdapAttributesScript;
	}
	
	/**
	 * Set {@link #syncLdapToJFireScript} content
	 * @param syncLdapToJFireScript
	 */
	public void setSyncLdapToJFireScript(String syncLdapToJFireScript) {
		this.syncLdapToJFireScript = syncLdapToJFireScript;
	}
	
	/**
	 * Get {@link #syncLdapToJFireScript} contents
	 * @return script contents as {@link String}
	 */
	public String getSyncLdapToJFireScript() {
		return syncLdapToJFireScript;
	}
	
	/**
	 * Set {@link #bindVariablesScript} content
	 * @param bindVariablesScript
	 */
	public void setBindVariablesScript(String bindVariablesScript) {
		this.bindVariablesScript = bindVariablesScript;
	}
	
	/**
	 * Get {@link #bindVariablesScript} contents
	 * @return script contents as {@link String}
	 */
	public String getBindVariablesScript() {
		return bindVariablesScript;
	}
	
	/**
	 * Set {@link #generateParentLdapEntriesScript} content
	 * @param generateParentLdapEntriesScript
	 */
	public void setGenerateParentLdapEntriesScript(String generateParentLdapEntriesScript) {
		this.generateParentLdapEntriesScript = generateParentLdapEntriesScript;
	}
	
	/**
	 * Get {@link #generateParentLdapEntriesScript} contents
	 * @return script contents as {@link String}
	 */
	public String getGenerateParentLdapEntriesScript() {
		return generateParentLdapEntriesScript;
	}
	
	/**
	 * 
	 * @return mapped LDAPServer
	 */
	public LDAPServer getLDAPServer() {
		return ldapServer;
	}
	
	/**
	 * 
	 * @return id
	 */
	public long getLdapScriptSetID() {
		return ldapScriptSetID;
	}
	
	/**
	 * 
	 * @return organisationID
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 * Executes script for generating LDAP DN using {@code jfireObject} data and returns the result.
	 * 
	 * @param jfireObject
	 * @return generated DN or <code>null</code> if script didn't return anything
	 * @throws ScriptException
	 */
	public String getLdapDN(Object jfireObject) throws ScriptException{

		ScriptContext ctx = new SimpleScriptContext();
		Bindings b = ctx.getBindings(ScriptContext.ENGINE_SCOPE);
		if (jfireObject instanceof User){
			User user = (User) jfireObject;
			b.put("user", user);
			b.put("person", getPerson(user));
		}else if (jfireObject instanceof Person){
			b.put("person", jfireObject);
		}

		Object result = getScriptEngine().eval(bindVariablesScript + ldapDNScript, ctx);
		if (result != null){
			return result.toString();
		}
		return null;
	}
	
	/**
	 * Executes script for generating {@link LDAPAttributeSet} with attributes to be stored in LDAP entry 
	 * using {@code jfireObject} data and returns the result.
	 * 
	 * @param jfireObject
	 * @return {@link LDAPAttributeSet} with attributes or <code>null</code> if script didn't return anything
	 * @throws ScriptException
	 * @throws NoSuchMethodException 
	 */
	public LDAPAttributeSet getLDAPAttributes(Object jfireObject, boolean isNewEntry) throws ScriptException, NoSuchMethodException{

		ScriptContext ctx = new SimpleScriptContext();
		Bindings b = ctx.getBindings(ScriptContext.ENGINE_SCOPE);
		b.put("isNewEntry", isNewEntry);
		if (jfireObject instanceof User){
			User user = (User) jfireObject;
			b.put("user", user);
			b.put("person", getPerson(user));
		}else if (jfireObject instanceof Person){
			b.put("person", jfireObject);
		}
		ScriptEngine scriptEngine = getScriptEngine();
		scriptEngine.setContext(ctx);
		scriptEngine.eval(bindVariablesScript+generateJFireToLdapAttributesScript);
		
		Invocable invocable = (Invocable) scriptEngine;
		Object result = null;
		
		result = invocable.invokeFunction("getMappedAttributes");
		
		if (result instanceof LDAPAttributeSet){
			return (LDAPAttributeSet) result;
		}
		return null;
	}
	
	/**
	 * Executes script for syncronizing data from LDAP entry to JFire object. All objects are created,
	 * stored and deleted inside the script, so there's no need to store/delete them outside of this method.
	 * 
	 * @param entryName Name of LDAP entry, could be needed in script to determine which JFire object this entry maps to
	 * @param allAttributes attributes of an entry which data is fetched
	 * @param removeJFireObjects 
	 * @return synchronized JFire object
	 * @throws ScriptException
	 */
	public Object syncLDAPDataToJFireObjects(String entryName, LDAPAttributeSet allAttributes, boolean removeJFireObjects) throws ScriptException{
		
		ScriptContext ctx = new SimpleScriptContext();
		Bindings b = ctx.getBindings(ScriptContext.ENGINE_SCOPE);
		b.put("entryName", entryName);
		b.put("removeJFireObjects", removeJFireObjects);
		b.put("allAttributes", allAttributes);
		b.put("attributeSyncPolicy", ldapServer.getAttributeSyncPolicy().stringValue());
		b.put("pm", JDOHelper.getPersistenceManager(this));
		b.put("logger", logger);
	
		return getScriptEngine().eval(syncLdapToJFireScript, ctx);
	}
	
	/**
	 * Executes script for getting collection of parent LDAP entries which hold child entries to be synchronized. 
	 * 
	 * @return {@link Collection} of entries names or empty List if script did not return any
	 * @throws ScriptException
	 * @throws NoSuchMethodException 
	 */
	public Collection<String> getUserParentEntriesForSync() throws ScriptException, NoSuchMethodException{
		return getParentEntriesInternal("getUserParentEntries");
	}

	/**
	 * Executes script for getting collection of parent LDAP entries which hold child entries to be synchronized. 
	 * 
	 * @return {@link Collection} of entries names or empty List if script did not return any
	 * @throws ScriptException
	 * @throws NoSuchMethodException 
	 */
	public Collection<String> getGroupParentEntriesForSync() throws ScriptException, NoSuchMethodException{
		return getParentEntriesInternal("getGroupParentEntries");
	}

	@SuppressWarnings("unchecked")
	private Collection<String> getParentEntriesInternal(String functionName) throws ScriptException, NoSuchMethodException{
        ScriptEngine engine = getScriptEngine();
		engine.eval(generateParentLdapEntriesScript);
		Invocable invocable = (Invocable) engine;
		Object result = invocable.invokeFunction(functionName);
		if (result instanceof Collection){
			return (Collection<String>) result;
		}
		return Collections.emptyList();
	}
	
	/**
	 * Executes script for getting the name of attribute which holfd user password in LDAP. 
	 * 
	 * @return attribute name as {@link String} or <code>null</code> if script did not return any
	 * @throws ScriptException
	 * @throws NoSuchMethodException 
	 */
	public String getUserPasswordAttributeName() throws ScriptException, NoSuchMethodException{
        ScriptEngine engine = getScriptEngine();
		engine.eval(generateJFireToLdapAttributesScript);
		Invocable invocable = (Invocable) engine;
		Object result = invocable.invokeFunction("getPasswordAttributeName");
		if (result instanceof String){
			return (String) result;
		}
		return null;
	}
	
	/**
	 * Get JFire {@link UserID} by given attributes of LDAP entry.
	 * If organisationID could not be obtained from LDAP entry it will use local organisationID.
	 * 
	 * @param attributes {@link LDAPAttributeSet} of LDAP entry
	 * @return {@link UserID} (with current organisationID) or <code>null</code>
	 * @throws ScriptException
	 * @throws NoSuchMethodException
	 * @throws IllegalStateException if script function will return a non-<code>null</code> non-String value
	 */
	public UserID getUserIDFromLDAPEntry(LDAPAttributeSet attributes) throws ScriptException, NoSuchMethodException{
        ScriptEngine engine = getScriptEngine();
		engine.eval(generateJFireToLdapAttributesScript);
		Invocable invocable = (Invocable) engine;
		Object result = invocable.invokeFunction("getUserIDFromLDAPEntry", attributes);
		if (result == null){
			return null;
		}
		if (!(result instanceof String)){
			throw new IllegalStateException(
					"getUserIDFromLDAPEntry(attributes) script function should return java.lang.String! Instead it is " + result.getClass().getName());
		}
		String[] idParts = ((String) result).split(LDAPScriptUtil.ORGANISATION_SEPARATOR);
		if (idParts.length == 2){
			return UserID.create(idParts[1], idParts[0]);
		}else{
			return UserID.create(getOrganisationID(), (String) result);
		}
	}
	
	private ScriptEngine getScriptEngine(){
        ScriptEngineManager manager = new ScriptEngineManager();
        return manager.getEngineByName("JavaScript");
	}
	
	private Person getPerson(User user){
		if (user != null){
			try{
				return user.getPerson();	// to avoid possible DataNucleus exceptions (just log them) when running from unit tests
			}catch(Exception e){
				logger.error(e.getMessage(), e);
			}
		}
		return null;
	}
}
