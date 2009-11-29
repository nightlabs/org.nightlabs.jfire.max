package org.nightlabs.jfire.asterisk;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.asterisk.config.AsteriskConfigModule;
import org.nightlabs.jfire.config.WorkstationConfigSetup;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.jfire.pbx.Call;
import org.nightlabs.jfire.pbx.PhoneSystem;
import org.nightlabs.jfire.pbx.PhoneSystemException;
import org.nightlabs.jfire.pbx.UnsupportedCallException;
import org.nightlabs.jfire.pbx.id.PhoneSystemID;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.prop.datafield.PhoneNumberDataField;
import org.nightlabs.jfire.prop.id.PropertySetID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.util.IOUtil;

/**
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireContactAsterisk_AsteriskServer"
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@FetchGroups({
	@FetchGroup(
			name=AsteriskServer.FETCH_GROUP_CALL_FILE_PROPERTIES,
			members=@Persistent(name="callFileProperties")
	),
})
public class AsteriskServer
extends PhoneSystem
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(AsteriskServer.class);

	public static final String FETCH_GROUP_CALL_FILE_PROPERTIES = "AsteriskServer.callFileProperties"; //$NON-NLS-1$

	/**
	 *  These properties are written to the call file when a call is initiated.
	 *  While writing this file, variables in these properties are replaced.
	 *  So far, there's only one variable planned:
	 *  ${dialPhoneNumber}: The number to be dialed.
	 *  The defaults (key: value) are:
	 *  Channel: Local/2880@local
	 *  CallerID: 00491728888888
	 *  MaxRetries: 1
	 *  RetryTime: 300
	 *  WaitTime: 45
	 *  Context: jfire-outbound
	 *  Extension: s
	 *  Priority: 1
	 *  Set: dialPhoneNumber=00491728888888
	 *
	 *  TODO: The LinkedHashMap should maintain the order of the items added to it, but some entries are not corrected. Don't know why.
	 *  For more information - http://www.javaranch.com/journal/2002/08/mapinterface.html
	 *
	 *  @Yo: If you looked into the database, you would have seen that the table
	 *  JFireContactAsterisk_AsteriskServer_callFileProperties lacks an index. That means DataNucleus seems not to support
	 *  a LinkedHashMap. But as I already stated, the order of the properties does not matter at all. Hence, the best way (user-friendly)
	 *  would be to sort them alphabetically (by key) in the UI. Additionally, I'd sort them alphabetically when calling in order
	 *  to prevent Heisenbugs (having always the same order).
	 */
	@Join
	@Persistent(table="JFireContactAsterisk_AsteriskServer_callFileProperties")
	private Map<String, String> callFileProperties;

	/**
	 * The directory in which to create the call files.
	 * The default value is: /var/spool/asterisk/outgoing
	 */
	@Persistent(nullValue=NullValue.EXCEPTION)
	private String callFileDirectory;

	@Column(defaultValue="10")
	private int callFileExpiryAgeMinutes = 10;

	/**
	 * @deprecated This constructor exists only for JDO and should never be used directly!
	 */
	@Deprecated
	protected AsteriskServer() { }

	/**
	 * Create an instance of <code>AsteriskServer</code> with an automatically generated ID. This is
	 * usually the best way in a client-UI (users normally don't want/need to assign IDs themselves).
	 *
	 * @param dummy this argument is ignored and serves only for differentiating this constructor from the default constructor.
	 * @see #AsteriskServer(String, String)
	 */
	public AsteriskServer(boolean dummy) {
		this(
				IDGenerator.getOrganisationID(),
				IDGenerator.nextIDString(PhoneSystem.class)
		);
	}

	/**
	 * Create an instance of <code>AsteriskServer</code>.
	 *
	 * @param organisationID first part of the primary key. The organisation which created this object.
	 * @param asteriskServerID second part of the primary key. A local identifier within the namespace of the organisation.
	 * @see #AsteriskServer(boolean)
	 */
	public AsteriskServer(String organisationID, String phoneSystemID) {
		super(organisationID, phoneSystemID);

		this.callFileDirectory = DEFAULT_CALL_FILE_DIRECTORY;

		// default call file values
		this.callFileProperties = new HashMap<String, String>();
		this.callFileProperties.put(CALL_FILE_PROPERTY_CALLER_ID, "${" + CALL_FILE_VARIABLE_DIAL_DISPLAY_NAME + "} <${" + CALL_FILE_VARIABLE_DIAL_PHONE_NUMBER + "}>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		this.callFileProperties.put(CALL_FILE_PROPERTY_MAX_RETRIES, "0"); //$NON-NLS-1$
		this.callFileProperties.put(CALL_FILE_PROPERTY_RETRY_TIME, "300"); //$NON-NLS-1$
		this.callFileProperties.put(CALL_FILE_PROPERTY_WAIT_TIME, "45"); //$NON-NLS-1$
		this.callFileProperties.put(CALL_FILE_PROPERTY_CONTEXT, "jfire-outbound"); //$NON-NLS-1$
		this.callFileProperties.put(CALL_FILE_PROPERTY_EXTENSION, "s"); //$NON-NLS-1$
		this.callFileProperties.put(CALL_FILE_PROPERTY_PRIORITY, "1"); //$NON-NLS-1$

		this.callFileProperties.put(CALL_FILE_PROPERTY_SET_DIAL_PHONE_NUMBER, "Set: dialPhoneNumber=${" + CALL_FILE_VARIABLE_DIAL_PHONE_NUMBER + "}"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static AsteriskServer getDefaultAsteriskServer(PersistenceManager pm)
	{
		PhoneSystemID defaultAsteriskServerID = PhoneSystemID.create(
				LocalOrganisation.getLocalOrganisation(pm).getOrganisationID(), ID_DEFAULT_ASTERISK_SERVER
		);
		AsteriskServer defaultAsteriskServer = null;
		try {
			defaultAsteriskServer = (AsteriskServer) pm.getObjectById(defaultAsteriskServerID);
			return defaultAsteriskServer; // if we come to this line, there obviously is a default instance in the datastore
		} catch (JDOObjectNotFoundException x) {
			// the default server does not exist => create it below
		}

		defaultAsteriskServer = new AsteriskServer(SecurityReflector.getUserDescriptor().getOrganisationID(), ID_DEFAULT_ASTERISK_SERVER);
		defaultAsteriskServer = pm.makePersistent(defaultAsteriskServer);
		return defaultAsteriskServer;
	}

	public String getCallFileDirectory() {
		return callFileDirectory;
	}

	public void setCallFileDirectory(String callFileDirectory) {
		if (callFileDirectory == null)
			throw new IllegalArgumentException("callFileDirectory == null"); //$NON-NLS-1$

		this.callFileDirectory = callFileDirectory;
	}

	/**
	 * Get a read-only <code>Map</code> of the call-file-properties. They are first {@link AsteriskConfigModule#getCallFileProperties() merged} with the ones in the
	 * {@link AsteriskConfigModule} and then written to a call file for Asterisk PBX.
	 * The values can contain variables (i.e. the values are templates and are converted to concrete
	 * values for the specific call situation). The following variables are currently supported:
	 * <ul>
	 * <li>{@link #CALL_FILE_VARIABLE_DIAL_DISPLAY_NAME}</li>
	 * <li>{@link #CALL_FILE_VARIABLE_DIAL_PHONE_NUMBER}</li>
	 * </ul>
	 *
	 * @return the call-file-properties.
	 * @see #setCallFileProperty(String, String)
	 */
	public Map<String, String> getCallFileProperties() {
		return Collections.unmodifiableMap(callFileProperties);
	}

	/**
	 * Set a call-file-property or remove it.
	 *
	 * @param key the key of the property - must not be <code>null</code>.
	 * @param value the value of the property. A <code>null</code> value causes the property to be removed.
	 * Since the value is a template, you can use variables in it. See {@link #getCallFileProperties()} for a
	 * list of all supported variables.
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

	public int getCallFileExpiryAgeMinutes() {
		return callFileExpiryAgeMinutes;
	}
	public void setCallFileExpiryAgeMinutes(int callFileExpiryAgeMinutes) {
		this.callFileExpiryAgeMinutes = callFileExpiryAgeMinutes;
	}

	public static boolean isInternalKey(String key) {
		return (key.startsWith("_") && key.endsWith("_")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static final String ID_DEFAULT_ASTERISK_SERVER = AsteriskServer.class.getName() + ".DEFAULT"; //$NON-NLS-1$

	public static final String DEFAULT_CALL_FILE_DIRECTORY = "/var/spool/asterisk/outgoing"; //$NON-NLS-1$

	/**
	 * The variable containing the current phone number to be dialed. You can use this variable
	 * as <i>${dialPhoneNumber}</i>
	 * in a call-file-property-value (see {@link #getCallFileProperties()}).
	 */
	public static final String CALL_FILE_VARIABLE_DIAL_PHONE_NUMBER = "dialPhoneNumber"; //$NON-NLS-1$

	/**
	 * The variable containing the name of the current dial target (i.e. the person being called). You
	 * can used this variable as <i>${dialDisplayName}</i>
	 * in a call-file-property-value (see {@link #getCallFileProperties()}).
	 */
	public static final String CALL_FILE_VARIABLE_DIAL_DISPLAY_NAME = "dialDisplayName"; //$NON-NLS-1$

	public static final String CALL_FILE_PROPERTY_SEPARATOR = ": "; //$NON-NLS-1$

	//Normal keys
	public static final String CALL_FILE_PROPERTY_CALLER_ID = "CallerID"; //$NON-NLS-1$
	public static final String CALL_FILE_PROPERTY_MAX_RETRIES = "MaxRetries"; //$NON-NLS-1$
	public static final String CALL_FILE_PROPERTY_RETRY_TIME = "RetryTime"; //$NON-NLS-1$
	public static final String CALL_FILE_PROPERTY_WAIT_TIME = "WaitTime"; //$NON-NLS-1$
	public static final String CALL_FILE_PROPERTY_CONTEXT = "Context"; //$NON-NLS-1$
	public static final String CALL_FILE_PROPERTY_EXTENSION = "Extension"; //$NON-NLS-1$
	public static final String CALL_FILE_PROPERTY_PRIORITY = "Priority"; //$NON-NLS-1$

	/**
	 * The local phone to call.
	 */
	public static final String CALL_FILE_PROPERTY_CHANNEL = "Channel"; //$NON-NLS-1$

	//Internal keys
	public static final String CALL_FILE_PROPERTY_SET_DIAL_PHONE_NUMBER = "_SetDialPhoneNumber_"; //$NON-NLS-1$

	@Override
	public void call(Call call)
	throws PhoneSystemException
	{
		if (call.getPhoneNumberDataFieldID() == null)
			throw new UnsupportedCallException(call, "The call must contain a phoneNumberDataFieldID!"); //$NON-NLS-1$

		PersistenceManager pm = getPersistenceManager();

		AsteriskConfigModule asteriskConfigModule =
			WorkstationConfigSetup.getWorkstationConfigModule(
					pm,
					AsteriskConfigModule.class
			);

		//create new combined keys - the sorting should not be necessary for Asterisk, we only sort (=> TreeSet) to prevent Heisenbugs.
		Set<String> keys = new TreeSet<String>();
		keys.addAll(asteriskConfigModule.getCallFileProperties().keySet());
		keys.addAll(this.getCallFileProperties().keySet());

		Object o = pm.getObjectById(call.getPhoneNumberDataFieldID());
		if (!(o instanceof PhoneNumberDataField))
			throw new IllegalArgumentException("call.phoneNumberDataFieldID does not reference an instance of PhoneNumberDataField! Instead, it is: " + o.getClass().getName()); //$NON-NLS-1$

		PhoneNumberDataField phoneNumberDataField = (PhoneNumberDataField) o;
		Person person = (Person) pm.getObjectById(
				PropertySetID.create(
						phoneNumberDataField.getOrganisationID(),
						phoneNumberDataField.getPropertySetID()
				)
		);

		// Pattern to clean all spaces, minuses etc. in order to make the number
		// an ordinary-phone-callable-number (i.e. purely digits from 0 to 9).
		Pattern cleanDialPhoneNumberPattern = Pattern.compile("[^0-9]"); //$NON-NLS-1$

		//create dial phone number
		StringBuilder dialPhoneNumberSB = new StringBuilder();

		// First, we clean the country-code individually to decide whether there really is one (might be only spaces).
		String countryCode = phoneNumberDataField.getCountryCode();
		if (countryCode != null && !countryCode.isEmpty())
			countryCode = cleanDialPhoneNumberPattern.matcher(countryCode).replaceAll(""); //$NON-NLS-1$

		// In order to support local numbers (within the local PBX), we only append the
		// international-call-prefix, if we have a country code. Otherwise, we omit it.
		if (countryCode != null && !countryCode.isEmpty()) {
			dialPhoneNumberSB.append(this.getInternationalCallPrefix());
			dialPhoneNumberSB.append(countryCode);
		}

		// Append the area code and the local number.
		dialPhoneNumberSB.append(phoneNumberDataField.getAreaCode());
		dialPhoneNumberSB.append(phoneNumberDataField.getLocalNumber());

		// Clean all spaces, minuses etc. to make it an ordinary-phone-callable-number
		// (i.e. purely digits from 0 to 9).
		String dialPhoneNumber = cleanDialPhoneNumberPattern.matcher(dialPhoneNumberSB.toString()).replaceAll(""); //$NON-NLS-1$

		// Create a map with all variables (and their values) which can be used in the call-file-properties' values.
		Map<String, String> variables = new HashMap<String, String>();
		variables.put(AsteriskServer.CALL_FILE_VARIABLE_DIAL_DISPLAY_NAME, person.getDisplayName());
		variables.put(AsteriskServer.CALL_FILE_VARIABLE_DIAL_PHONE_NUMBER, dialPhoneNumber);
		// make variables read-only to guarantee they don't change later
		variables = Collections.unmodifiableMap(variables);

		StringBuilder text = new StringBuilder();
		for (String key : keys) {
			String value = null;

			value = this.getCallFileProperties().get(key);

			//overrides the value from config module
			if (asteriskConfigModule.getOverrideCallFilePropertyKeys().contains(key)) {
				value = asteriskConfigModule.getCallFileProperties().get(key);
			}

			if (value == null || value.isEmpty())
				continue;

			// apply variables (transform template to concrete string)
			value = IOUtil.replaceTemplateVariables(value, variables);

			//creates text
			if (AsteriskServer.isInternalKey(key)) {
				text.append(value);
			}
			else {
				text.append(key).append(AsteriskServer.CALL_FILE_PROPERTY_SEPARATOR).append(value);
			}

			//new line
			text.append('\n');
		}

		File callFileDir = new File(this.getCallFileDirectory());
		if (!callFileDir.isDirectory())
			throw new IllegalStateException(
					"Call file directory does not exist or is no directory: " //$NON-NLS-1$
					+ callFileDir.getAbsolutePath()
			);

		File tmpDir = new File(callFileDir, CALL_FILE_TEMP_SUB_DIRECTORY);
		if (!tmpDir.exists())
			tmpDir.mkdir();

		if (!tmpDir.isDirectory())
			throw new IllegalStateException(
					"Could not create temp directory within call file directory (check permissions!): " //$NON-NLS-1$
					+ tmpDir.getAbsolutePath()
			);

		File callFile = new File(
				tmpDir,
				Long.toString(System.currentTimeMillis(), 36) + '-' +	Integer.toString((int)(Math.random() * Integer.MAX_VALUE), 36) + CALL_FILE_SUFFIX
		);

		try {
			//write to the file in the tmp directory
			IOUtil.writeTextFile(callFile, text.toString());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		//move to the parent directory
		boolean isSuccess = callFile.renameTo(new File(tmpDir.getParentFile(), callFile.getName()));
		if (!isSuccess)
			logger.warn("Can not move call file to: " + tmpDir.getParentFile().getAbsolutePath()); //$NON-NLS-1$
	}

	public static final String CALL_FILE_TEMP_SUB_DIRECTORY = "jfire.tmp"; //$NON-NLS-1$

	public static final String CALL_FILE_SUFFIX = ".jfire.call"; //$NON-NLS-1$
}
