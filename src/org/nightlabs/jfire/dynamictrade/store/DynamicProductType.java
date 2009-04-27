package org.nightlabs.jfire.dynamictrade.store;

import java.util.Collection;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.id.ProductTypeID;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.Queries;

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
	private static final long serialVersionUID = 1L;

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

}
