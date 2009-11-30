package org.nightlabs.jfire.asterisk.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import org.nightlabs.inheritance.Inheritable;
import org.nightlabs.jfire.asterisk.AsteriskServer;
import org.nightlabs.jfire.config.ConfigModule;

/**
 * This is a subclass of {@link org.nightlabs.jfire.config.ConfigModule} and
 * will be managed as workstation-config-module
 * (bound to the workstation, not to the user)
 *
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireAsterisk_AsteriskConfigModule")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@FetchGroups({
//	@FetchGroup(
//		name=AsteriskConfigModule.FETCH_GROUP_ASTERISK_SERVER,
//		members=@Persistent(name="asteriskServer")
//	),
	@FetchGroup(
		name=AsteriskConfigModule.FETCH_GROUP_CALL_FILE_PROPERTIES,
		members=@Persistent(name="callFileProperties")
	),
	@FetchGroup(
		name=AsteriskConfigModule.FETCH_GROUP_CALL_FILE_OVERRIDE_KEYS,
		members=@Persistent(name="overrideCallFilePropertyKeys")
	),
})
public class AsteriskConfigModule
extends ConfigModule
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_CALL_FILE_PROPERTIES = "AsteriskConfigModule.callFileProperties"; //$NON-NLS-1$
	public static final String FETCH_GROUP_CALL_FILE_OVERRIDE_KEYS = "AsteriskConfigModule.overrideCallFilePropertyKeys"; //$NON-NLS-1$

	/**
	 * The properties that shall be overridden (i.e. the value defined in the assigned asteriskServer should not be used).
	 */
	@Join
	@Persistent(table="JFireAsterisk_AsteriskConfigModule_overrideCallFilePropertyKeys")
	private Set<String> overrideCallFilePropertyKeys;

	/**
	 * Every property in this map overrides the value from the assigned asteriskServer,
	 * if it is listed in overrideCallFilePropertyKeys. If a key is part of this Map but
	 * not of the Set, it is ignored (and the global value used). If a key is only in the Set
	 * but not here, the global value is not written to the file at all.
	 */
	@Join
	@Persistent(table="JFireAsterisk_AsteriskConfigModule_callFileProperties") // persistenceModifier=PersistenceModifier.PERSISTENT is default - we don't need it (if we don't change the default of the class before).
	private Map<String, String> callFileProperties;

	@Override
	public void init() {
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("JDOHelper.getPersistenceManager(this) returned null!"); //$NON-NLS-1$

		overrideCallFilePropertyKeys = new HashSet<String>();
		overrideCallFilePropertyKeys.add(AsteriskServer.CALL_FILE_PROPERTY_CHANNEL);

		callFileProperties = new HashMap<String, String>();
		callFileProperties.put(AsteriskServer.CALL_FILE_PROPERTY_CHANNEL, "Local/2110@local"); //$NON-NLS-1$
	}

	/**
	 * Get a read-only <code>Set</code> of all property-keys that are overridden by
	 * this config-module. Keys that are not listed here, are taken from the assigned
	 * {@link #getAsteriskServer AsteriskServer} (if they occur there).
	 *
	 * @return a read-only <code>Set</code> of property keys.
	 * @see #addOverrideCallFilePropertyKey(String)
	 * @see #removeOverrideCallFilePropertyKey(String)
	 * @see #getCallFileProperties()
	 */
	public Set<String> getOverrideCallFilePropertyKeys() {
		return Collections.unmodifiableSet(overrideCallFilePropertyKeys);
	}

	/**
	 * Add a new key to the {@link #getOverrideCallFilePropertyKeys() overrideCallFilePropertyKeys}.
	 *
	 * @param key the key to be added. Must not be <code>null</code>.
	 * @return <code>true</code>, if the key was not yet in <code>Set</code> (and the <code>Set</code> was thus modified).
	 */
	public boolean addOverrideCallFilePropertyKey(String key)
	{
		if (key == null)
			throw new IllegalArgumentException("key must not be null!"); //$NON-NLS-1$

		return overrideCallFilePropertyKeys.add(key);
	}

	public boolean removeOverrideCallFilePropertyKey(String key)
	{
		return overrideCallFilePropertyKeys.remove(key);
	}

	/**
	 * Get a read-only <code>Map</code> of the call-file-properties.
	 * <p>
	 * Every property in this map overrides the value from the assigned {@link #getAsteriskServer() AsteriskServer},
	 * if it is listed in {@link #getOverrideCallFilePropertyKeys() overrideCallFilePropertyKeys}. If a key is part
	 * of this <code>Map</code> but
	 * not of the <code>Set</code>, it is ignored (and the global value from the <code>AsteriskServer</code> instance used).
	 * If a key is only in the <code>Set</code> but not here, the property is not written to the file at all.
	 * </p>
	 * <p>
	 * The values can contain variables (i.e. the values are templates and are converted to concrete
	 * values for the specific call situation). See {@link AsteriskServer#getCallFileProperties()} for all supported variables.
	 * </p>
	 *
	 * @return the call-file-properties.
	 * @see #setCallFileProperty(String, String)
	 * @see #getOverrideCallFilePropertyKeys()
	 */
	public Map<String, String> getCallFileProperties() {
		// @Yo: We always return read-only maps/sets in order to easily enforce parameter checks and (sometimes, maybe later) invalidate
		// caches and other stuff during write operations. Without returning a read-only object here, we would need to wrap it in our
		// own delegating Map which intercepts write operations. This is possible, but more work than to simply force users of our API
		// to call the setCallFileProperty(...) instead of Map.put(...).
		//
		// Of course, if you really need a full-blown writable Map, you can return it here, but if you don't really need it, this way
		// is easier.
		//
		// Marco
		//
		return Collections.unmodifiableMap(callFileProperties);
	}

	@Override
	public void preInherit(Inheritable mother, Inheritable child) {
		super.preInherit(mother, child);
		callFileProperties.size();
		overrideCallFilePropertyKeys.size();
	}

	/**
	 * Set a call-file-property or remove it.
	 *
	 * @param key the key of the property - must not be <code>null</code>.
	 * @param value the value of the property. A <code>null</code> value causes the property to be removed.
	 * @return the old value that was assigned to the property-key (or <code>null</code> if there was none).
	 * @see #getCallFileProperties()
	 */
	public String setCallFileProperty(String key, String value)
	{
		if (key == null)
			throw new IllegalArgumentException("key must not be null!"); //$NON-NLS-1$

		if (value == null)
			return callFileProperties.remove(key);
		else
			return callFileProperties.put(key, value);
	}
}