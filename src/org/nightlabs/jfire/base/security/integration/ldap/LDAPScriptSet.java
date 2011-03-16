package org.nightlabs.jfire.base.security.integration.ldap;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.nightlabs.jfire.base.security.integration.ldap.id.LDAPScriptSetID;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.security.User;
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
	
	private static final Logger logger = LoggerFactory.getLogger(LDAPScriptSet.class);

	private static final Charset SCRIPT_CHARSET_UTF_8 = Charset.forName("UTF-8");

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
	 * Bind variables (i.e. $userID$ or $personEmail$) to JFire objects data
	 */
	@Persistent(defaultFetchGroup="true")
	private byte[] bindVariablesScript;
	
	/**
	 * Generates and returns an LDAP DN using {@code bindVariablesScript)
	 */
	@Persistent(defaultFetchGroup="true")
	private byte[] ldapDNScript;
	
	/**
	 * Performs synchronization from LDAP directory to JFire(creating or modifying User/Person objects)
	 */
	@Persistent(defaultFetchGroup="true")
	private byte[] syncLdapToJFireScript;
	
	/**
	 * Generates and returns a map of attributes to be stored in LDAP directory using {@code bindVariablesScript)
	 */
	@Persistent(defaultFetchGroup="true")
	private byte[] syncJFireToLdapScript;
	
	/**
	 * @deprecated For JDO only!
	 */
	protected LDAPScriptSet(){
	}
	
	/**
	 * Default constructor for creating LDAPSCriptSet objects
	 * @param id
	 */
	public LDAPScriptSet(LDAPScriptSetID id){
		if (id == null){
			this.ldapScriptSetID = IDGenerator.nextID(LDAPScriptSet.class);
			this.organisationID = IDGenerator.getOrganisationID();
		}else{
			this.ldapScriptSetID = id.ldapScriptSetID;
			this.organisationID = id.organisationID;
		}
	}
	
	/**
	 * Set ldapDNScript contents
	 * @param ldapDN
	 */
	public void setLdapDNScript(String ldapDN) {
		if (ldapDN != null){
			this.ldapDNScript = ldapDN.getBytes(SCRIPT_CHARSET_UTF_8);
		}else{
			this.ldapDNScript = null;
		}
	}
	
	/**
	 * Set syncJFireToLdapScript content
	 * @param syncJFireToLdapScript
	 */
	public void setSyncJFireToLdapScript(String syncJFireToLdapScript) {
		if (syncJFireToLdapScript != null){
			this.syncJFireToLdapScript = syncJFireToLdapScript.getBytes(SCRIPT_CHARSET_UTF_8);
		}else{
			this.syncJFireToLdapScript = null;
		}
	}
	
	/**
	 * Set syncLdapToJFireScript content
	 * @param syncLdapToJFireScript
	 */
	public void setSyncLdapToJFireScript(String syncLdapToJFireScript) {
		if (syncLdapToJFireScript != null){
			this.syncLdapToJFireScript = syncLdapToJFireScript.getBytes(SCRIPT_CHARSET_UTF_8);
		}else{
			this.syncLdapToJFireScript = null;
		}
	}
	
	/**
	 * Set bindVariablesScript content
	 * @param bindVariablesScript
	 */
	public void setBindVariablesScript(String bindVariablesScript) {
		if (bindVariablesScript != null){
			this.bindVariablesScript = bindVariablesScript.getBytes(SCRIPT_CHARSET_UTF_8);
		}else{
			this.bindVariablesScript = null;
		}
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
		Object result = execute(
				new String(bindVariablesScript, SCRIPT_CHARSET_UTF_8)+new String(ldapDNScript, SCRIPT_CHARSET_UTF_8), 
				getDefaultUserPersonScriptContext(jfireObject)
				);
		if (result != null){
			return result.toString();
		}
		return null;
	}
	
	/**
	 * Executes script for generating map of attributes to be stored in LDAP entry 
	 * using {@code jfireObject} data and returns the result.
	 * 
	 * @param jfireObject
	 * @return map of attributes or <code>null</code> if script didn't return anything
	 * @throws ScriptException
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object[]> getAttributesMapForLDAP(Object jfireObject) throws ScriptException{
		Object result = execute(
				new String(bindVariablesScript, SCRIPT_CHARSET_UTF_8)+new String(syncJFireToLdapScript, SCRIPT_CHARSET_UTF_8), 
				getDefaultUserPersonScriptContext(jfireObject)
				);
		if (result instanceof Map){
			return (Map<String, Object[]>) result;
		}
		return null;
	}
	
	/**
	 * Executes script for syncronizing data from LDAP entry to JFire object. All objects are created
	 * and stored inside the script, so there's no need to store them outside of this method.
	 * 
	 * @param allAttributes attributes of an entry which data is fetched
	 * @param organisationID
	 * @return synchronized JFire object
	 * @throws ScriptException
	 */
	public Object syncLDAPDataToJFireObjects(Map<String, Object[]> allAttributes, String organisationID) throws ScriptException{
		
		ScriptContext ctx = new SimpleScriptContext();
		Bindings b = ctx.getBindings(ScriptContext.ENGINE_SCOPE);
		b.put("allAttributes", allAttributes);
		b.put("pm", JDOHelper.getPersistenceManager(this));
		b.put("organisationID", organisationID);
		// generate new ID for Person object here because there are difficulties with getting 
		// Class object in script (via Class.forName() and via .class field) 
		b.put("newPersonID", IDGenerator.nextID(PropertySet.class));
		b.put("logger", logger);
	
		return execute(new String(syncLdapToJFireScript, SCRIPT_CHARSET_UTF_8), ctx);
	}
	
	private Object execute(String script, ScriptContext scriptContext) throws ScriptException{
		
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");

        return engine.eval(script, scriptContext);

	}
	
	private static ScriptContext getDefaultUserPersonScriptContext(Object jfireObject) throws ScriptException{
		ScriptContext ctx = new SimpleScriptContext();
		Bindings b = ctx.getBindings(ScriptContext.ENGINE_SCOPE);
		if (jfireObject instanceof User){
			User user = (User) jfireObject;
			b.put("user", user);
			b.put("person", user.getPerson());
		}else if (jfireObject instanceof Person){
			b.put("person", (Person) jfireObject);
		}
		return ctx;
	}

}
