package org.nightlabs.jfire.voucher.store;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.accounting.book.LocalAccountantDelegate;
import org.nightlabs.jfire.accounting.priceconfig.IPriceConfig;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.ProductTypeLocal;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.voucher.scripting.VoucherLayout;
import org.nightlabs.jfire.voucher.scripting.id.VoucherLayoutID;
import org.nightlabs.util.CollectionUtil;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.store.ProductType"
 *		detachable="true"
 *		table="JFireVoucher_VoucherType"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.query
 * 		global="false"
 *		name="getChildVoucherTypes_topLevel"
 *		language="javax.jdo.query.JDOQL"
 *		query="SELECT
 *		  WHERE this.extendedProductType == null"
 *
 * @jdo.query
 *		name="getChildVoucherTypes_hasParent"
 *		language="javax.jdo.query.JDOQL"
 *		query="SELECT
 *		  WHERE
 *		    this.extendedProductType.organisationID == parentProductTypeOrganisationID &&
 *		    this.extendedProductType.productTypeID == parentProductTypeProductTypeID
 *		  PARAMETERS String parentProductTypeOrganisationID, String parentProductTypeProductTypeID
 *		  import java.lang.String"
 *
 * @jdo.query
 * 		name="getVoucherTypeIdsByVoucherLayoutId"
 * 		query="SELECT JDOHelper.getObjectId(this) WHERE :voucherLayoutId == JDOHelper.getObjectId(this.voucherLayout)"
 *
 * @jdo.query
 * 		name="getVoucherTypeIdsByLocalAccountantDelegateId"
 * 		query="SELECT JDOHelper.getObjectId(this) WHERE :localAccountantDelegateID == JDOHelper.getObjectId(this.productTypeLocal.localAccountantDelegate)"
 *
 * @jdo.fetch-group name="VoucherType.voucherLayout" fields="voucherLayout"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireVoucher_VoucherType")
@FetchGroups(
	@FetchGroup(
		name=VoucherType.FETCH_GROUP_VOUCHER_LAYOUT,
		members=@Persistent(name="voucherLayout"))
)
@Queries({
	@javax.jdo.annotations.Query(
		name="getChildVoucherTypes_topLevel",
		value="SELECT WHERE this.extendedProductType == null",
		language="javax.jdo.query.JDOQL"),
	@javax.jdo.annotations.Query(
		name="getChildVoucherTypes_hasParent",
		value="SELECT WHERE this.extendedProductType.organisationID == parentProductTypeOrganisationID && this.extendedProductType.productTypeID == parentProductTypeProductTypeID PARAMETERS String parentProductTypeOrganisationID, String parentProductTypeProductTypeID import java.lang.String",
		language="javax.jdo.query.JDOQL"),
	@javax.jdo.annotations.Query(
		name="getVoucherTypeIdsByVoucherLayoutId",
		value="SELECT JDOHelper.getObjectId(this) WHERE JDOHelper.getObjectId(this.voucherLayout) == :voucherLayoutId",
		language="javax.jdo.query.JDOQL"),
	@javax.jdo.annotations.Query(
		name="getVoucherTypeIdsByLocalAccountantDelegateId",
		value="SELECT JDOHelper.getObjectId(this) WHERE JDOHelper.getObjectId(this.productTypeLocal.localAccountantDelegate) == :localAccountantDelegateID",
		language="javax.jdo.query.JDOQL")
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class VoucherType
extends ProductType
{
	public static final String FETCH_GROUP_VOUCHER_LAYOUT = "VoucherType.voucherLayout";

	private static final long serialVersionUID = 1L;

	public static final class FieldName
	{
		public static final String voucherLayout = "voucherLayout";
	};

	public static Collection<VoucherType> getChildVoucherTypes(PersistenceManager pm, ProductTypeID parentVoucherTypeID)
	{
		if (parentVoucherTypeID == null) {
			Query q = pm.newNamedQuery(VoucherType.class, "getChildVoucherTypes_topLevel");
			return (Collection<VoucherType>)q.execute();
		}

		Query q = pm.newNamedQuery(VoucherType.class, "getChildVoucherTypes_hasParent");
		return (Collection<VoucherType>) q.execute(
			parentVoucherTypeID.organisationID, parentVoucherTypeID.productTypeID);
	}

//	/**
//	 * @jdo.field persistence-modifier="persistent" mapped-by="voucherType"
//	 */
//	private VoucherTypeName name;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected VoucherType() { }

	public VoucherType(String organisationID, String productTypeID,
			ProductType extendedProductType,
			byte inheritanceNature, byte packageNature)
	{
		super(organisationID, productTypeID, extendedProductType,
				inheritanceNature, packageNature);

//		this.name = new VoucherTypeName(this);
	}

//	@Implement
//	public I18nText getName()
//	{
//		return name;
//	}

	@Override
	protected ProductTypeLocal createProductTypeLocal(User user)
	{
		return new VoucherTypeLocal(user, this);
	}

	@Override
	protected void calculatePrices()
	{
		// TODO Auto-generated method stub

	}

	// ******************************
	// /// *** begin inheritance *** ///
// Marco: important change: the basic ProductType does NOT filter packagePriceConfig anymore, because there are about the same number of implementations
// needing inheritance as there are which must not have inheritance for this field!
//	@Override
//	public FieldMetaData getFieldMetaData(String fieldName, boolean createMissingMetaData)
//	{
//		if ("packagePriceConfig".equals(fieldName)) { // this is normally not inheritable as it usually stores per-ProductType-data, but for the VoucherType it is
//			ProductTypeFieldMetaData fmd = fieldMetaDataMap.get(fieldName);
//			if (fmd == null) {
//				fmd = new ProductTypeFieldMetaData(this, fieldName);
//				fieldMetaDataMap.put(fieldName, fmd);
//			} // if (fmd == null) {
//			return fmd;
//		}
//
//		return super.getFieldMetaData(fieldName, createMissingMetaData);
//	}

//	@Override
//	public FieldInheriter getFieldInheriter(String fieldName)
//	{
//		if ("name".equals(fieldName))
//			return new InheritableFieldInheriter();
//
//		return super.getFieldInheriter(fieldName);
//	}
//
//	@Override
//	public void preInherit(Inheritable mother, Inheritable child)
//	{
//		super.preInherit(mother, child);
//		name.getI18nMap();
//	}
	// /// *** end inheritance *** ///
	// ******************************

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private VoucherLayout voucherLayout;

	public VoucherLayout getVoucherLayout()
	{
		return voucherLayout;
	}

	public void setVoucherLayout(VoucherLayout voucherLayout)
	{
		this.voucherLayout = voucherLayout;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation always returns the {@link #getPackagePriceConfig()} as
	 * vouchers have a special PriceConfig that does not virtual self packaging.
	 * </p>
	 * @see org.nightlabs.jfire.store.ProductType#getPriceConfigInPackage(java.lang.String)
	 */
	@Override
	public IPriceConfig getPriceConfigInPackage(String packageProductTypePK) {
		return getPackagePriceConfig();
	}

	/**
	 * Returns a set of the IDs of all {@link VoucherType}s that share the {@link VoucherLayout} with the given ID.
	 * @param pm The persistence manager to use to execute the query.
	 * @param voucherLayoutId The ID of the {@link VoucherLayout} that all returned {@link VoucherType}s should share.
	 * @return a set of the IDs of all {@link VoucherType}s that share the {@link VoucherLayout} with the given ID.
	 */
	public static Set<ProductTypeID> getVoucherTypeIdsByVoucherLayoutId(PersistenceManager pm, VoucherLayoutID voucherLayoutId) {
		Query query = pm.newNamedQuery(VoucherType.class, "getVoucherTypeIdsByVoucherLayoutId");
		Collection<ProductTypeID> result = CollectionUtil.castCollection((Collection<ProductTypeID>) query.execute(voucherLayoutId));
		return new HashSet<ProductTypeID>(result);
	}

	/**
	 * Returns a set of the IDs of all {@link VoucherType}s that share the {@link org.nightlabs.jfire.accounting.book.LocalAccountantDelegate} with the given ID.
	 * @param pm The persistence manager to use to execute the query.
	 * @param localAccountantDelegateId The ID of the {@link LocalAccountantDelegate} that all returned {@link VoucherType}s should share.
	 * @return a set of the IDs of all {@link VoucherType}s that share the {@link  org.nightlabs.jfire.accounting.book.LocalAccountantDelegate} with the given ID.
	 */
	public static Set<ProductTypeID> getVoucherTypeIdsByLocalAccountantDelegateId(PersistenceManager pm, ObjectID localAccountantDelegateId) {
		Query query = pm.newNamedQuery(VoucherType.class, "getVoucherTypeIdsByLocalAccountantDelegateId");
		Collection<ProductTypeID> result = CollectionUtil.castCollection((Collection<ProductTypeID>) query.execute(localAccountantDelegateId));
		return new HashSet<ProductTypeID>(result);
	
	}







}
