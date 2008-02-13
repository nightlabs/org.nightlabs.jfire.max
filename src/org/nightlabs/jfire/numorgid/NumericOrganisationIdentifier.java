package org.nightlabs.jfire.numorgid;

import java.util.Hashtable;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.nightlabs.jfire.base.Lookup;
import org.nightlabs.jfire.numorgid.id.NumericOrganisationIdentifierID;
import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.jfire.organisation.Organisation;

/**
 * @jdo.persistence-capable
 *		identity-type="application"
 *		detachable="true"
 *		table="JFireNumericOrganisationID_NumericOrganisationIdentifier"
 *		objectid-class="org.nightlabs.jfire.numorgid.id.NumericOrganisationIdentifierID"
 *
 * @jdo.create-objectid-class
 *
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
public class NumericOrganisationIdentifier {
	
	/** @jdo.field primary-key="true" */
	private String organisationID;
	
	/** @jdo.field persistence-modifier="persistent" */
	private int numericOrganisationID;

	/**
	 * This ({@value #MAX_NUMERIC_ORGANISATION_ID }) is the highest 4 digit number in the base-62-system,
	 * i.e. "zzzz" => {@link Base62Coder}) and defines the highest possible value for a numeric organisation ID.
	 */
	public static final int MAX_NUMERIC_ORGANISATION_ID = 14776335;
	
	public static final int ROOT_ORGANISATION_NUMERIC_ORGANISATION_ID = 0;

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
	public static int getNumericOrganisationID(PersistenceManager pm)
	{
		String localOrganisationID = LocalOrganisation.getLocalOrganisation(pm).getOrganisationID();
		NumericOrganisationIdentifier numericOrganisationID = null;
		try {
			NumericOrganisationIdentifierID id = NumericOrganisationIdentifierID.create(localOrganisationID);
			numericOrganisationID = (NumericOrganisationIdentifier) pm.getObjectById(id);
		} catch (JDOObjectNotFoundException e) {
			String rootOrganisationID = null;
			Hashtable<?,?> rootOrganisationInitialContextProperties;
			try {
				InitialContext initialContext = new InitialContext();
				try {
					if (! Organisation.hasRootOrganisation(initialContext))
						return NumericOrganisationIdentifier.MAX_NUMERIC_ORGANISATION_ID; // For test systems it's good to have a valid value
						
					rootOrganisationID = Organisation.getRootOrganisationID(initialContext);
				} finally {
					initialContext.close();
				}

				if (localOrganisationID.equals(rootOrganisationID))
					throw new IllegalStateException("Fucking shit! We are the root-organisation and have no numeric id! The datastore-initialiser did not do his work!");

				rootOrganisationInitialContextProperties = Lookup.getInitialContextProperties(pm, rootOrganisationID);
			} catch (NamingException x) {
				throw new RuntimeException("Could not acquire initial context properties for root organisation " + String.valueOf(rootOrganisationID), x);
			}

			try {
				NumericOrganisationIdentifierManager noim = NumericOrganisationIdentifierManagerUtil.getHome(rootOrganisationInitialContextProperties).create();
				return noim.getNumericOrganisationID();

			} catch (Exception x) {
				throw new RuntimeException("Communication with root-organisation failed!", x);
			}
		}

		return numericOrganisationID.getNumericOrganisationID();
	}
}
