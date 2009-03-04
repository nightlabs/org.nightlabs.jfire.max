package org.nightlabs.jfire.trade.endcustomer;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jdo.FetchPlanBackup;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.id.PropertySetID;
import org.nightlabs.jfire.prop.id.StructBlockID;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.util.CollectionUtil;
import org.nightlabs.util.Util;

/**
 * An instance of this class specifies how an end-customer will be transferred from
 * the reseller to the supplier. It is specified by the supplier and assigned to a
 * {@link ProductType}.
 *
 * @author marco schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.trade.endcustomer.id.EndCustomerTransferPolicyID"
 *		detachable="true"
 *		table="JFireTrade_EndCustomerTransferPolicy"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, endCustomerTransferPolicyID"
 *
 * @jdo.fetch-group name="EndCustomerTransferPolicy.name" fields="name"
 * @jdo.fetch-group name="EndCustomerTransferPolicy.description" fields="description"
 * @jdo.fetch-group name="EndCustomerTransferPolicy.structBlocks" fields="structBlocks"
 * @jdo.fetch-group name="EndCustomerTransferPolicy.structFields" fields="structFields"
 */
public class EndCustomerTransferPolicy
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_NAME = "EndCustomerTransferPolicy.name";
	public static final String FETCH_GROUP_DESCRIPTION = "EndCustomerTransferPolicy.description";
	public static final String FETCH_GROUP_STRUCT_BLOCKS = "EndCustomerTransferPolicy.structBlocks";
	public static final String FETCH_GROUP_STRUCT_FIELDS = "EndCustomerTransferPolicy.structFields";

	/**
	 * @jdo.field primary-key="true"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long endCustomerTransferPolicyID;

	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="endCustomerTransferPolicy"
	 */
	private EndCustomerTransferPolicyName name;

	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="endCustomerTransferPolicy"
	 */
	private EndCustomerTransferPolicyDescription description;

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="org.nightlabs.jfire.prop.StructBlock"
	 *		table="JFireTrade_EndCustomerTransferPolicy_structBlocks"
	 * @jdo.join
	 */
	private Set<StructBlock> structBlocks;

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="org.nightlabs.jfire.prop.StructField"
	 *		table="JFireTrade_EndCustomerTransferPolicy_structFields"
	 * @jdo.join
	 */
	private Set<StructField<? extends DataField>> structFields;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected EndCustomerTransferPolicy() { }

	public EndCustomerTransferPolicy(String organisationID, long endCustomerTransferPolicyID) {
		this.organisationID = organisationID;
		this.endCustomerTransferPolicyID = endCustomerTransferPolicyID;

		structBlocks = new HashSet<StructBlock>();
		structFields = new HashSet<StructField<? extends DataField>>();
		name = new EndCustomerTransferPolicyName(this);
		description = new EndCustomerTransferPolicyDescription(this);
	}

	public String getOrganisationID() {
		return organisationID;
	}
	public long getEndCustomerTransferPolicyID() {
		return endCustomerTransferPolicyID;
	}

	public EndCustomerTransferPolicyName getName() {
		return name;
	}

	public EndCustomerTransferPolicyDescription getDescription() {
		return description;
	}

	/**
	 * Get the {@link StructBlock}s for which the {@link DataBlock}s should be replicated completely (all fields!) from
	 * the reseller to the supplier. If only some fields of a block should be affected, specify the {@link StructField}s
	 * individually (see {@link #getStructFields()}).
	 * @return the {@link StructBlock}s whose data should be replicated.
	 * @see #addStructBlock(StructBlock)
	 * @see #removeStructBlock(StructBlock)
	 * @see #getStructFields()
	 */
	public Set<StructBlock> getStructBlocks() {
		return Collections.unmodifiableSet(structBlocks);
	}

	public boolean addStructBlock(StructBlock structBlock) {
		return structBlocks.add(structBlock);
	}

	public boolean removeStructBlock(StructBlock structBlock) {
		return structBlocks.remove(structBlock);
	}

	/**
	 * Get the {@link StructField}s for which the {@link DataField}s should be replicated from the reseller
	 * to the supplier. If complete blocks should be affected (not only individual fields), you can specify
	 * complete {@link StructBlock}s instead (see {@link #getStructBlocks()}).
	 * @return the {@link StructField}s whose data should be replicated.
	 * @see #addStructField(StructField)
	 * @see #removeStructField(StructField)
	 * @see #getStructBlocks()
	 */
	public Set<StructField<? extends DataField>> getStructFields() {
		return Collections.unmodifiableSet(structFields);
	}

	public boolean addStructField(StructField<? extends DataField> structField) {
		return structFields.add(structField);
	}

	public boolean removeStructField(StructField<? extends DataField> structField) {
		return structFields.remove(structField);
	}

	private static void removeNonTransferableFields(PersistenceManager pm, Person detachedPerson, Set<EndCustomerTransferPolicy> endCustomerTransferPolicies)
	{
		Set<StructBlock> structBlocks = new HashSet<StructBlock>();
		Set<StructField<? extends DataField>> structFields = new HashSet<StructField<? extends DataField>>();

		for (EndCustomerTransferPolicy endCustomerTransferPolicy : endCustomerTransferPolicies) {
			structBlocks.addAll(endCustomerTransferPolicy.getStructBlocks());
			structFields.addAll(endCustomerTransferPolicy.getStructFields());
		}

		Set<StructBlockID> structBlockIDs = NLJDOHelper.getObjectIDSet(structBlocks);
		Set<StructFieldID> structFieldIDs = NLJDOHelper.getObjectIDSet(structFields);
		removeNonTransferableFields(pm, detachedPerson, structBlockIDs, structFieldIDs);
	}

	private static void removeNonTransferableFields(PersistenceManager pm, Person detachedPerson, Set<StructBlockID> structBlockIDs, Set<StructFieldID> structFieldIDs)
	{
		if (JDOHelper.getPersistenceManager(detachedPerson) != null)
			throw new IllegalArgumentException("detachedPerson is not detached!");

		List<DataField> dataFieldsToFilter = new LinkedList<DataField>();

		IStruct struct = (IStruct) pm.getObjectById(detachedPerson.getStructLocalObjectID());
		detachedPerson.inflate(struct);

		for (DataField dataField : detachedPerson.getDataFields()) {
			StructBlockID structBlockID = StructBlockID.create(dataField.getStructBlockOrganisationID(), dataField.getStructBlockID());

			if (structBlockIDs.contains(structBlockID))
				continue; // keep current; continue with next

			if (structFieldIDs.contains(dataField.getStructFieldIDObj()))
				continue; // keep current; continue with next

			// if we come here, it needs to be removed => add it to our list
			dataFieldsToFilter.add(dataField);
		}

		// remove all from the list
		for (DataField dataField : dataFieldsToFilter)
			detachedPerson.internalRemoveDataFieldFromPersistentCollection(dataField);

		Set<StructFieldID> nonFilteredStructFieldIDs = new HashSet<StructFieldID>();
		detachedPerson.getNonPersistentUserObjectMap().put("nonFilteredStructFieldIDs", nonFilteredStructFieldIDs);
		for (DataField dataField : detachedPerson.getDataFields())
			nonFilteredStructFieldIDs.add(dataField.getStructFieldIDObj());

		detachedPerson.deflate();
	}

	public static LegalEntity detachLegalEntity(PersistenceManager pm, LegalEntity legalEntity, Set<EndCustomerTransferPolicy> endCustomerTransferPolicies)
	{
		FetchPlanBackup fetchPlanBackup = NLJDOHelper.backupFetchPlan(pm.getFetchPlan());
		try {
//			Collection<String> c1 = CollectionUtil.castCollection(pm.getFetchPlan().getGroups());
//			Set<String> fetchGroups = new HashSet<String>(c1);
			Set<String> fetchGroups = new HashSet<String>();
			fetchGroups.add(FetchPlan.DEFAULT);
			fetchGroups.add(LegalEntity.FETCH_GROUP_PERSON);
			fetchGroups.add(LegalEntity.FETCH_GROUP_CUSTOMER_GROUPS);
			fetchGroups.add(LegalEntity.FETCH_GROUP_DEFAULT_CUSTOMER_GROUP);
			fetchGroups.add(Person.FETCH_GROUP_FULL_DATA);
			pm.getFetchPlan().setGroups(fetchGroups);
			pm.getFetchPlan().setMaxFetchDepth(NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);

			LegalEntity detached = pm.detachCopy(legalEntity);
			removeNonTransferableFields(pm, detached.getPerson(), endCustomerTransferPolicies);
			return detached;
		} finally {
			NLJDOHelper.restoreFetchPlan(pm.getFetchPlan(), fetchPlanBackup);
		}
	}

	/**
	 * Adds all those data-fields that are existing locally but have been actively filtered out of the
	 * <code>detachedPerson</code>. This means {@link StructField}s that are known in the datastore
	 * where the {@link Person} was detached but that are not in the {@link EndCustomerTransferPolicy}s.
	 *
	 * @param pm
	 * @param detachedPerson
	 */
	private static void addExistingDataFields(PersistenceManager pm, Person detachedPerson)
	{
		if (JDOHelper.getPersistenceManager(detachedPerson) != null)
			throw new IllegalArgumentException("detachedPerson is not detached!");

		PropertySetID propertySetID = (PropertySetID) JDOHelper.getObjectId(detachedPerson);
		if (propertySetID == null)
			throw new IllegalArgumentException("detachedPerson does not have an object-id assigned!");

		Person attachedPerson;
		try {
			attachedPerson = (Person) pm.getObjectById(propertySetID);
		} catch (JDOObjectNotFoundException x) {
			// doesn't exist => return
			return;
		}

//		Collection<? extends DataField> detachedDataFields = detachedPerson.getDataFields();
		Set<StructFieldID> nonFilteredStructFieldIDs = CollectionUtil.castSet((Set<?>) detachedPerson.getNonPersistentUserObjectMap().get("nonFilteredStructFieldIDs"));
		for (DataField attachedDataField : attachedPerson.getDataFields()) {
//			if (!detachedDataFields.contains(attachedDataField))
			if (!nonFilteredStructFieldIDs.contains(attachedDataField.getStructFieldIDObj()))
				detachedPerson.internalAddDataFieldToPersistentCollection(attachedDataField);
		}
	}

	public static LegalEntity attachLegalEntity(PersistenceManager pm, LegalEntity legalEntity)
	{
		NLJDOHelper.makeDirtyAllFieldsRecursively(legalEntity);
		addExistingDataFields(pm, legalEntity.getPerson());
		return pm.makePersistent(legalEntity);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((organisationID == null) ? 0 : organisationID.hashCode());
		result = prime * result + (int) (endCustomerTransferPolicyID ^ (endCustomerTransferPolicyID >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;

		EndCustomerTransferPolicy other = (EndCustomerTransferPolicy) obj;
		return (
				Util.equals(this.endCustomerTransferPolicyID, other.endCustomerTransferPolicyID) &&
				Util.equals(this.organisationID, other.organisationID)
		);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + Integer.toHexString(System.identityHashCode(this)) + '[' + organisationID + ',' + ObjectIDUtil.longObjectIDFieldToString(endCustomerTransferPolicyID) + ']';
	}
}
