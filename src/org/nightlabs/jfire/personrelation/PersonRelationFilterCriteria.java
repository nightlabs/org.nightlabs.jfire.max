package org.nightlabs.jfire.personrelation;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.nightlabs.jfire.personrelation.id.PersonRelationTypeID;
import org.nightlabs.jfire.prop.id.PropertySetID;

/**
 * Parameter-Object for methods in {@link PersonRelationManagerRemote}.
 * 
 * @author abieber
 */
public class PersonRelationFilterCriteria implements Serializable {
	
	private static final long serialVersionUID = 20110827L;
	
	private Set<PersonRelationTypeID> personRelationTypeIncludeIDs;
	private Set<PersonRelationTypeID> personRelationTypeExcludeIDs;
	private PropertySetID fromPersonID;
	private PropertySetID toPersonID;
	private PersonRelationComparator personRelationComparator;
	private Set<PropertySetID> toPropertySetIDsToExclude;
	private Set<PropertySetID> fromPropertySetIDsToExclude;

	/**
	 * Convenience Constructor
	 */
	public PersonRelationFilterCriteria(
			Set<PersonRelationTypeID> personRelationTypeIncludeIDs,
			Set<PersonRelationTypeID> personRelationTypeExcludeIDs,
			PropertySetID fromPersonID, PropertySetID toPersonID,
			Set<PropertySetID> toPropertySetIDsToExclude,
			Set<PropertySetID> fromPropertySetIDsToExclude,
			PersonRelationComparator personRelationComparator) {
		super();
		this.personRelationTypeIncludeIDs = personRelationTypeIncludeIDs;
		this.personRelationTypeExcludeIDs = personRelationTypeExcludeIDs;
		this.fromPersonID = fromPersonID;
		this.toPersonID = toPersonID;
		this.toPropertySetIDsToExclude = toPropertySetIDsToExclude;
		this.fromPropertySetIDsToExclude = fromPropertySetIDsToExclude;
		this.personRelationComparator = personRelationComparator;
	}

	/**
	 * Convenience Constructor
	 */
	public PersonRelationFilterCriteria(
			PersonRelationTypeID personRelationTypeID,
			PropertySetID fromPersonID, PropertySetID toPersonID,
			PersonRelationComparator personRelationComparator) {

		this(Collections.singleton(personRelationTypeID), null, fromPersonID, toPersonID,
				null, null, personRelationComparator);
	}

	/**
	 * Convenience Constructor
	 */
	public PersonRelationFilterCriteria(
			PersonRelationTypeID personRelationTypeID,
			PropertySetID fromPersonID, PropertySetID toPersonID) {

		this(Collections.singleton(personRelationTypeID), null, fromPersonID, toPersonID, null, null, null);
	}
	
	/**
	 * Default Constructor
	 */
	public PersonRelationFilterCriteria() {
	}

	/**
	 * @return A list of PersonRelationTypes to limit the result to.
	 */
	public Collection<PersonRelationTypeID> getPersonRelationTypeIncludeIDs() {
		return personRelationTypeIncludeIDs;
	}
	
	
	/**
	 * @param personRelationTypeID A list of PersonRelationTypes to limit the result to.
	 */
	public void setPersonRelationTypeIncludeIDs(Set<PersonRelationTypeID> personRelationTypeIncludeIDs) {
		this.personRelationTypeIncludeIDs = personRelationTypeIncludeIDs;
	}

	/**
	 * @return A list of PersonRelationTypes to exclude from the result.
	 */
	public Collection<PersonRelationTypeID> getPersonRelationTypeExcludeIDs() {
		return personRelationTypeExcludeIDs;
	}

	/**
	 * @param excludedPersonRelationTypeIDs A PersonRelationType to limit the result to.
	 */
	public void setPersonRelationTypeExcludeIDs(Set<PersonRelationTypeID> personRelationTypeExcludeIDs) {
		this.personRelationTypeExcludeIDs = personRelationTypeExcludeIDs;
	}

	/**
	 * @return The id of the person that should be the from-anchor of the result.
	 */
	public PropertySetID getFromPersonID() {
		return fromPersonID;
	}

	/**
	 * @param fromPersonID The id of the person that should be the from-anchor of the result.
	 */
	public void setFromPersonID(PropertySetID fromPersonID) {
		this.fromPersonID = fromPersonID;
	}

	/**
	 * @return The id of the person that should be the to-anchor of the result.
	 */
	public PropertySetID getToPersonID() {
		return toPersonID;
	}

	/**
	 * @param toPersonID The id of the person that should be the to-anchor of the result.
	 */
	public void setToPersonID(PropertySetID toPersonID) {
		this.toPersonID = toPersonID;
	}

	/**
	 * @return The comparator to use in order to sort the result.
	 */
	public PersonRelationComparator getPersonRelationComparator() {
		return personRelationComparator;
	}

	/**
	 * @param personRelationComparator The comparator to use in order to sort the result.
	 */
	public void setPersonRelationComparator(PersonRelationComparator personRelationComparator) {
		this.personRelationComparator = personRelationComparator;
	}
	
	public Set<PropertySetID> getToPropertySetIDsToExclude() {
		return toPropertySetIDsToExclude;
	}
	
	public void setToPropertySetIDsToExclude(Set<PropertySetID> toPropertySetIDsToExclude) {
		this.toPropertySetIDsToExclude = toPropertySetIDsToExclude;
	}
	
	public Set<PropertySetID> getFromPropertySetIDsToExclude() {
		return fromPropertySetIDsToExclude;
	}
	
	public void setFromPropertySetIDsToExclude(
			Set<PropertySetID> fromPropertySetIDsToExclude) {
		this.fromPropertySetIDsToExclude = fromPropertySetIDsToExclude;
	}
}
