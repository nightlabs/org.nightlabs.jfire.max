package org.nightlabs.jfire.dynamictrade.store;

import java.util.Collection;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Queries;

import org.nightlabs.inheritance.FieldInheriter;
import org.nightlabs.inheritance.NullCapableInheritableFieldInheriter.ChildFieldCreator;
import org.nightlabs.jdo.inheritance.JDONullCapableInheritableFieldInheriter;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.id.ProductTypeID;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.store.ProductType"
 *		detachable="true"
 *		table="JFireDynamicTrade_DynamicProductType"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.query
 * 		global="false"
 *		name="getChildProductTypes_topLevel"
 *		language="javax.jdo.query.JDOQL"
 *		query="SELECT
 *		  WHERE this.extendedProductType == null"
 *
 * @jdo.query
 *		name="getChildProductTypes_hasParent"
 *		language="javax.jdo.query.JDOQL"
 *		query="SELECT
 *		  WHERE
 *		    this.extendedProductType.organisationID == parentProductTypeOrganisationID &&
 *		    this.extendedProductType.productTypeID == parentProductTypeProductTypeID
 *		  PARAMETERS String parentProductTypeOrganisationID, String parentProductTypeProductTypeID
 *		  import java.lang.String"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireDynamicTrade_DynamicProductType")
@FetchGroups({
	@FetchGroup(
		fetchGroups={"default"},
		name=DynamicProductType.FETCH_GROUP_PROPERTY_SET,
		members=@Persistent(name="propertySet"))
})
@Queries({
	@javax.jdo.annotations.Query(
		name="getChildProductTypes_topLevel",
		value="SELECT WHERE this.extendedProductType == null",
		language="javax.jdo.query.JDOQL"),
	@javax.jdo.annotations.Query(
		name="getChildProductTypes_hasParent",
		value="SELECT WHERE this.extendedProductType.organisationID == parentProductTypeOrganisationID && this.extendedProductType.productTypeID == parentProductTypeProductTypeID PARAMETERS String parentProductTypeOrganisationID, String parentProductTypeProductTypeID import java.lang.String",
		language="javax.jdo.query.JDOQL")
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class DynamicProductType
extends ProductType
{
	private static final long serialVersionUID = 20111115L;
	
	public static final class FieldName
	{
		public static final String propertySet = "propertySet";
	};
	
	public static final String FETCH_GROUP_PROPERTY_SET = "DynamicProductType.propertySet";

	/**
	 * Note, that this method does only return instances of {@link DynamicProductType} while
	 * the same-named method {@link ProductType#getChildProductTypes(PersistenceManager, ProductTypeID)}
	 * returns all types inherited from {@link ProductType}.
	 *
	 * @param pm The <tt>PersistenceManager</tt> that should be used to access the datastore.
	 * @param parentProductTypeID The <tt>ProductType</tt> of which to find all children or <tt>null</tt> to find all top-level-<tt>DynamicProductType</tt>s.
	 * @return Returns instances of <tt>DynamicProductType</tt>.
	 */
	@SuppressWarnings("unchecked")
	public static Collection<DynamicProductType> getChildProductTypes(PersistenceManager pm, ProductTypeID parentProductTypeID)
	{
		if (parentProductTypeID == null) {
			Query q = pm.newNamedQuery(DynamicProductType.class, "getChildProductTypes_topLevel");
			return (Collection<DynamicProductType>)q.execute();
		}

		Query q = pm.newNamedQuery(DynamicProductType.class, "getChildProductTypes_hasParent");
		return (Collection<DynamicProductType>) q.execute(
			parentProductTypeID.organisationID, parentProductTypeID.productTypeID);
	}

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected DynamicProductType() { }

	public DynamicProductType(
			String organisationID, String productTypeID,
			ProductType extendedProductType,
			byte inheritanceNature,
			byte packageNature)
	{
		super(
				organisationID, productTypeID,
				extendedProductType,
				inheritanceNature,
				packageNature);
	}

	@Override
	protected void calculatePrices()
	{
		// Nothing to do, because DynamicProducts have dynamic prices that are entered
		// on-the-fly when an Article is created.
	}
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private PropertySet propertySet;

	/**
	 * Returns the property set of this {@link SimpleProductType}.
	 * Note that this is optional and might be <code>null</code>.
	 *
	 * @return The property set of this {@link SimpleProductType}.
	 */
	public PropertySet getPropertySet() {
		return propertySet;
	}

	private static FieldInheriter propSetInheriter = new JDONullCapableInheritableFieldInheriter<DynamicProductType, PropertySet>(new ChildFieldCreator<DynamicProductType, PropertySet>() {
		@Override
		public PropertySet createAndAssignChildField(DynamicProductType mother, PropertySet motherFieldValueObj, DynamicProductType child) {
			PropertySet newPropertySet = new PropertySet(
					motherFieldValueObj.getOrganisationID(), IDGenerator.nextID(PropertySet.class), 
					motherFieldValueObj.getStructOrganisationID(), motherFieldValueObj.getStructLinkClass(), motherFieldValueObj.getStructScope(), motherFieldValueObj.getStructLocalScope());
			
			PersistenceManager pm = JDOHelper.getPersistenceManager(child);
			if (pm != null) {
				newPropertySet = pm.makePersistent(newPropertySet);
			}
			
			child.setPropertySet(newPropertySet);
			return child.getPropertySet();
		}
	});
	
	@Override
	public FieldInheriter getFieldInheriter(String fieldName) {
		if (FieldName.propertySet.equals(fieldName))
			return propSetInheriter;

		return super.getFieldInheriter(fieldName);
	}
	
	public void setPropertySet(PropertySet propertySet) {
		this.propertySet = propertySet;
	}
}
