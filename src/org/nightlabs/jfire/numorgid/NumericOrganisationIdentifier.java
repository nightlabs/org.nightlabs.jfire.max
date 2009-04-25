package org.nightlabs.jfire.numorgid;

import java.io.Serializable;
import java.util.Hashtable;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.Lookup;
import org.nightlabs.jfire.numorgid.id.NumericOrganisationIdentifierID;
import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.math.Base62Coder;

/**
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 * @author marco schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		detachable="true"
 *		table="JFireNumericOrganisationID_NumericOrganisationIdentifier"
 *		objectid-class="org.nightlabs.jfire.numorgid.id.NumericOrganisationIdentifierID"
 *
 * @jdo.create-objectid-class
 *
 * @jdo.query
 *		name="getNumericOrganisationIdentifierByNumericID"
 *		query="SELECT UNIQUE WHERE numericOrganisationID == :numericOrganisationID"
 */
@PersistenceCapable(
	objectIdClass=NumericOrganisationIdentifierID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireNumericOrganisationID_NumericOrganisationIdentifier")
@Queries(
	@javax.jdo.annotations.Query(
		name="getNumericOrganisationIdentifierByNumericID",
		value="SELECT UNIQUE WHERE numericOrganisationID == :numericOrganisationID")
)
public class NumericOrganisationIdentifier
implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** @jdo.field primary-key="true" */
	@PrimaryKey
	private String organisationID;

	/** @jdo.field persistence-modifier="persistent" */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private int numericOrganisationID;

	/**
	 * This ({@value #MAX_NUMERIC_ORGANISATION_ID }) is the highest 4 digit number in the base-62-system,
	 * i.e. "zzzz" => {@link Base62Coder}) and defines the highest possible value for a numeric organisation ID.
	 * The reason for this is the use of the numeric organisationID in CODE128-barcodes, where the base62-encoding is used.
	 */
	public static final int MAX_NUMERIC_ORGANISATION_ID = 14776335;

	/**
	 * The root organisation has by definition the numeric id 0 and this constant can be used to access it - hard-coding
	 * values is bad practice!
	 */
	public static final int ROOT_ORGANISATION_NUMERIC_ORGANISATION_ID = 0;

	static NumericOrganisationIdentifier getNumericOrganisationIdentifierByNumericID(PersistenceManager pm, int numericOrganisationID)
	{
		Query q = pm.newNamedQuery(NumericOrganisationIdentifier.class, "getNumericOrganisationIdentifierByNumericID");
		return (NumericOrganisationIdentifier) q.execute(new Integer(numericOrganisationID));
	}

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected NumericOrganisationIdentifier() { }

	/**
	 * Constructor that creates a new {@link NumericOrganisationIdentifier} using the given parameters.
	 * @param organisationID The organisation ID to be assigned.
	 * @param numericOrganisationID The numeric organisation ID to be assigned.
	 */
	public NumericOrganisationIdentifier(String organisationID, int numericOrganisationID)
	{
		if (numericOrganisationID > MAX_NUMERIC_ORGANISATION_ID)
			throw new IllegalArgumentException("numericOrganisationID > MAX_NUMERIC_ORGANISATION_ID");

		this.organisationID = organisationID;
		this.numericOrganisationID = numericOrganisationID;
	}

	/**
	 * Returns the (string based) organisation ID.
	 * @return the (string based) organisation ID.
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 * Returns the numeric organisation ID.
	 * @return the numeric organisation ID.
	 */
	public int getNumericOrganisationID() {
		return numericOrganisationID;
	}

	/**
	 * This method returns the numeric organisation ID for the current organisation. In order to do so, it asks the root organisation for
	 * the numeric organisation ID of the current organisation which returns it if one is already existing or creates a new unique one
	 * if it doesn't exist yet.<br />
	 * If the JFire server has been configured in stand-alone mode (i.e. no root-organisation available), the value
	 * {@link NumericOrganisationIdentifier#MAX_NUMERIC_ORGANISATION_ID} is returned instead as ID.
	 *
	 * @param pm A persistence manager to be used to lookup the {@link NumericOrganisationIdentifier} and the initial context properties for the root
	 * 		organisation.
	 * @return The numeric organisation ID for the current organisation.
	 */
	public static int getLocalNumericOrganisationID(PersistenceManager pm)
	{
		String localOrganisationID = LocalOrganisation.getLocalOrganisation(pm).getOrganisationID();
		return getNumericOrganisationIdentifier(pm, localOrganisationID).getNumericOrganisationID();
	}

	/**
	 * @deprecated Use {@link #getLocalNumericOrganisationID(PersistenceManager)} or {@link #getNumericOrganisationIdentifier(PersistenceManager, String)} instead!
	 */
	@Deprecated
	public static int getNumericOrganisationID(PersistenceManager pm)
	{
		return getLocalNumericOrganisationID(pm);
	}

	public static NumericOrganisationIdentifier getNumericOrganisationIdentifier(PersistenceManager pm, String organisationID)
	{
		NumericOrganisationIdentifier numericOrganisationIdentifier = null;
		try {
			NumericOrganisationIdentifierID id = NumericOrganisationIdentifierID.create(organisationID);
			numericOrganisationIdentifier = (NumericOrganisationIdentifier) pm.getObjectById(id);
		} catch (JDOObjectNotFoundException e) {
			String rootOrganisationID = null;
			Hashtable<?,?> rootOrganisationInitialContextProperties;
			try {
				InitialContext initialContext = new InitialContext();
				try {
					if (! Organisation.hasRootOrganisation(initialContext))
						throw new UnknownOrganisationException(organisationID); // the initialiser should have created a value for the local organisation - and another org is unsupported in a standalone org.

					rootOrganisationID = Organisation.getRootOrganisationID(initialContext);
				} finally {
					initialContext.close();
				}

				if (organisationID.equals(rootOrganisationID))
					throw new IllegalStateException("Fucking shit! The root-organisation has no numeric id! The datastore-initialiser did not do his work!");

				rootOrganisationInitialContextProperties = Lookup.getInitialContextProperties(pm, rootOrganisationID);
			} catch (NamingException x) {
				throw new RuntimeException("Could not acquire initial context properties for root organisation " + rootOrganisationID, x);
			}

			try {
				NumericOrganisationIdentifierManagerRemote noim = JFireEjb3Factory.getRemoteBean(NumericOrganisationIdentifierManagerRemote.class, rootOrganisationInitialContextProperties);
				numericOrganisationIdentifier = noim.getNumericOrganisationIdentifier(organisationID, null);
			} catch (Exception x) {
				throw new RuntimeException("Communication with root-organisation failed!", x);
			}

			numericOrganisationIdentifier = pm.makePersistent(numericOrganisationIdentifier);
		}

		return numericOrganisationIdentifier;
	}

	public static NumericOrganisationIdentifier getNumericOrganisationIdentifier(PersistenceManager pm, int numericOrganisationID)
	{
		NumericOrganisationIdentifier numericOrganisationIdentifier = getNumericOrganisationIdentifierByNumericID(pm, numericOrganisationID);
		if (numericOrganisationIdentifier == null) {
			if (numericOrganisationID == ROOT_ORGANISATION_NUMERIC_ORGANISATION_ID)
				throw new IllegalStateException("Fucking shit! The root-organisation has no numeric id! The datastore-initialiser did not do his work!");

			String rootOrganisationID = null;
			Hashtable<?,?> rootOrganisationInitialContextProperties;
			try {
				InitialContext initialContext = new InitialContext();
				try {
					if (! Organisation.hasRootOrganisation(initialContext))
						throw new UnknownNumericOrganisationIdentifierException(numericOrganisationID); // the initialiser should have created a value for the local organisation - and another org is unsupported in a standalone org.

					rootOrganisationID = Organisation.getRootOrganisationID(initialContext);
				} finally {
					initialContext.close();
				}

				rootOrganisationInitialContextProperties = Lookup.getInitialContextProperties(pm, rootOrganisationID);
			} catch (NamingException x) {
				throw new RuntimeException("Could not acquire initial context properties for root organisation " + rootOrganisationID, x);
			}

			try {
				NumericOrganisationIdentifierManagerRemote noim = JFireEjb3Factory.getRemoteBean(NumericOrganisationIdentifierManagerRemote.class, rootOrganisationInitialContextProperties);
				numericOrganisationIdentifier = noim.getNumericOrganisationIdentifier(null, numericOrganisationID);
			} catch (Exception x) {
				throw new RuntimeException("Communication with root-organisation failed!", x);
			}

			numericOrganisationIdentifier = pm.makePersistent(numericOrganisationIdentifier);
		}

		return numericOrganisationIdentifier;
	}
}
