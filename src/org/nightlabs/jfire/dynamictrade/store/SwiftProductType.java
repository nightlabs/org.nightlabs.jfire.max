package org.nightlabs.jfire.dynamictrade.store;

import java.util.Collection;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.trade.LegalEntity;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 * 
 * @jdo.persistence-capable 
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.store.ProductType"
 *		detachable="true"
 *		table="JFireSwiftTrade_SwiftProductType"
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
public class SwiftProductType
extends ProductType
{
	private static final long serialVersionUID = 1L;

	/**
	 * Note, that this method does only return instances of {@link SwiftProductType} while
	 * the same-named method {@link ProductType#getChildProductTypes(PersistenceManager, ProductTypeID)}
	 * returns all types inherited from {@link ProductType}.
	 *
	 * @param pm The <tt>PersistenceManager</tt> that should be used to access the datastore.
	 * @param parentProductTypeID The <tt>ProductType</tt> of which to find all children or <tt>null</tt> to find all top-level-<tt>SwiftProductType</tt>s.
	 * @return Returns instances of <tt>SwiftProductType</tt>.
	 */
	public static Collection getChildProductTypes(PersistenceManager pm, ProductTypeID parentProductTypeID)
	{
		if (parentProductTypeID == null) {
			Query q = pm.newNamedQuery(SwiftProductType.class, "getChildProductTypes_topLevel");
			return (Collection)q.execute();
		}

		Query q = pm.newNamedQuery(SwiftProductType.class, "getChildProductTypes_hasParent");
		return (Collection) q.execute(
			parentProductTypeID.organisationID, parentProductTypeID.productTypeID);
	}

	/**
	 * @deprecated Only for JDO!
	 */
	protected SwiftProductType() { }

	public SwiftProductType(
			String organisationID, String productTypeID,
			ProductType extendedProductType, LegalEntity owner,
			byte inheritanceNature,
			byte packageNature)
	{
		super(
				organisationID, productTypeID,
				extendedProductType, owner,
				inheritanceNature,
				packageNature);
	}

	@Implement
	protected void calculatePrices()
	{
		// Nothing to do, because SwiftProducts have dynamic prices that are entered
		// on-the-fly when an Article is created.
	}

}
