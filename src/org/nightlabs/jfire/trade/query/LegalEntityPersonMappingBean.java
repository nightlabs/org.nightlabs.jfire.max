package org.nightlabs.jfire.trade.query;

import java.io.Serializable;

import javax.jdo.JDOHelper;

import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.prop.id.PropertySetID;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.transfer.id.AnchorID;

/**
 * A bean describing the link of a {@link LegalEntity} to a {@link Person} (by id).
 */
public class LegalEntityPersonMappingBean implements Serializable {
	
	private static final long serialVersionUID = 20100406L;
	
	private AnchorID legalEntityID;
	private PropertySetID personID;
	
	/**
	 * Create a new {@link LegalEntityPersonMappingBean}
	 */
	public LegalEntityPersonMappingBean() {
	}

	/**
	 * Create a new {@link LegalEntityPersonMappingBean}.
	 * 
	 * @param legalEntityID The Id of the {@link LegalEntity} to describe.
	 * @param personID The Id of the {@link Person} linked to the {@link LegalEntity}.
	 */
	public LegalEntityPersonMappingBean(AnchorID legalEntityID, PropertySetID personID) {
		super();
		this.legalEntityID = legalEntityID;
		this.personID = personID;
	}
	
	/**
	 * Create a new {@link LegalEntityPersonMappingBean}.
	 * 
	 * @param legalEntity The LegalEntity to initialize the bean from (id of legalEntity and assigned Person will be used)
	 */
	public LegalEntityPersonMappingBean(LegalEntity legalEntity) {
		setLegalEntity(legalEntity);
	}

	/**
	 * Set the properties of this bean using the given {@link LegalEntity}. The ids of the
	 * {@link LegalEntity} and its assigned {@link Person} will be used.
	 * 
	 * @param legalEntity The {@link LegalEntity} to initialise this bean from.
	 */
	public void setLegalEntity(LegalEntity legalEntity) {
		this.legalEntityID = (AnchorID) JDOHelper.getObjectId(legalEntity);
		this.personID = (PropertySetID) JDOHelper.getObjectId(legalEntity.getPerson());
	}

	/**
	 * @return The Id of the {@link LegalEntity} described by this bean.
	 */
	public AnchorID getLegalEntityID() {
		return legalEntityID;
	}
	/**
	 * @param legalEntityID The Id of the {@link LegalEntity} described by this bean.
	 */
	public void setLegalEntityID(AnchorID legalEntityID) {
		this.legalEntityID = legalEntityID;
	}
	
	/**
	 * @return The Id of the {@link Person} linked to the {@link LegalEntity} described by this bean.
	 */
	public PropertySetID getPersonID() {
		return personID;
	}
	/**
	 * @param personID The Id of the {@link Person} linked to the {@link LegalEntity} described by this bean.
	 */
	public void setPersonID(PropertySetID personID) {
		this.personID = personID;
	}
}