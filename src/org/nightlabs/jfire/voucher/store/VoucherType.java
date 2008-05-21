package org.nightlabs.jfire.voucher.store;

import java.util.Collection;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.accounting.priceconfig.IPriceConfig;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.ProductTypeLocal;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.voucher.scripting.VoucherLayout;

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
 * @jdo.fetch-group name="VoucherType.voucherLayout" fields="voucherLayout"
 */
public class VoucherType
extends ProductType
{
	public static final String FETCH_GROUP_VOUCHER_LAYOUT = "VoucherType.voucherLayout";
	
	private static final long serialVersionUID = 1L;

	public static final class FieldName
	{
		public static final String voucherLayout = "voucherLayout";
	};
	
	@SuppressWarnings("unchecked")
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
	@Implement
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
}
