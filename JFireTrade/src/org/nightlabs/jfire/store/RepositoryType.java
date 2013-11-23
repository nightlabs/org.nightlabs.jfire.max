package org.nightlabs.jfire.store;

import java.io.Serializable;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.store.id.RepositoryTypeID;
import org.nightlabs.util.Util;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.store.id.RepositoryTypeID"
 *		detachable="true"
 *		table="JFireTrade_RepositoryType"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, repositoryTypeID"
 *
 * @jdo.fetch-group name="RepositoryType.name" fields="name"
 */
@PersistenceCapable(
	objectIdClass=RepositoryTypeID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_RepositoryType")
@FetchGroups(
	@FetchGroup(
		name=RepositoryType.FETCH_GROUP_NAME,
		members=@Persistent(name="name"))
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class RepositoryType
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_NAME = "RepositoryType.name";

//	/**
//	 * anchorTypeID for revenue repositorys of the local organisation.
//	 */
//	public static final RepositoryTypeID ACCOUNT_TYPE_ID_LOCAL_REVENUE = RepositoryTypeID.create(Organisation.DEV_ORGANISATION_ID, "Repository.Local.Revenue");
//
//	/**
//	 * anchorTypeID for expense repositorys of the Local organisation
//	 */
//	public static final RepositoryTypeID ACCOUNT_TYPE_ID_LOCAL_EXPENSE = RepositoryTypeID.create(Organisation.DEV_ORGANISATION_ID, "Repository.Local.Expense");
//
//	/**
//	 * anchorTypeID for repositorys of trading partners when they acting as vendor
//	 */
//	public static final RepositoryTypeID ACCOUNT_TYPE_ID_PARTNER_VENDOR = RepositoryTypeID.create(Organisation.DEV_ORGANISATION_ID, "Repository.Partner.Vendor");
//
//	/**
//	 * anchorTypeID for repositorys of trading partners when they acting as customer
//	 */
//	public static final RepositoryTypeID ACCOUNT_TYPE_ID_PARTNER_CUSTOMER = RepositoryTypeID.create(Organisation.DEV_ORGANISATION_ID, "Repository.Partner.Customer");
//
//	/**
//	 * anchorTypeID for repositorys of trading partners when they overpay multiple invoices and
//	 * it cannot be determined whether the partner is a customer or a vendor.
//	 */
//	public static final RepositoryTypeID ACCOUNT_TYPE_ID_PARTNER_NEUTRAL = RepositoryTypeID.create(Organisation.DEV_ORGANISATION_ID, "Repository.Partner.Neutral");
//
//	/**
//	 * anchorTypeID for repositorys that are used during payment. They represent money that's outside
//	 * the organisation (means paid to a partner), hence their {@link #isOutside()} property is <code>true</code>.
//	 */
//	public static final RepositoryTypeID ACCOUNT_TYPE_ID_OUTSIDE = RepositoryTypeID.create(Organisation.DEV_ORGANISATION_ID, "Repository.Outside");
//
//	public static final RepositoryTypeID ACCOUNT_TYPE_ID_SUMMARY = RepositoryTypeID.create(Organisation.DEV_ORGANISATION_ID, "Repository.Summary");

	/**
	 * Local products will be created in a repository with this type. Foreign products, however, are created in
	 * a repository with {@link #ANCHOR_TYPE_ID_OUTSIDE}. Foreign products are transferred here (i.e. to their home)
	 * during the booking of their DeliveryNote / ReceptionNote.
	 * <p>
	 * Both local and foreign products are transferred from
	 * this repository to the container product's repository
	 * when the container product is assembled.
	 * </p>
	 * <p>
	 * Both local and foreign products are transferred into
	 * this repository (from the container's repository) when
	 * the container product is DISassembled.
	 * </p>
	 */
	public static final RepositoryTypeID REPOSITORY_TYPE_ID_HOME = RepositoryTypeID.create(Organisation.DEV_ORGANISATION_ID, "Home");

	public static final RepositoryTypeID REPOSITORY_TYPE_ID_PARTNER = RepositoryTypeID.create(Organisation.DEV_ORGANISATION_ID, "Partner");

	/**
	 * A repository with this type is used for virtually outside repositories. If a <tt>Product</tt>
	 * is there, it means that the product is not here anymore (or not yet) and has already been delivered
	 * to a partner (or not yet been delivered from a supplier). It should have {@link #outside} == true!
	 */
	public static final RepositoryTypeID REPOSITORY_TYPE_ID_OUTSIDE = RepositoryTypeID.create(Organisation.DEV_ORGANISATION_ID, "Outside");

	public static final String ANCHOR_TYPE_ID_PREFIX_HOME = "home#";

	public static final String ANCHOR_TYPE_ID_PREFIX_PARTNER = "partner#";

	public static final String ANCHOR_TYPE_ID_PREFIX_OUTSIDE = "outside#";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String repositoryTypeID;

	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="repositoryType"
	 */
	@Persistent(
		mappedBy="repositoryType",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private RepositoryTypeName name;

	/**
	 * Whether or not this Repository represents sth. outside.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean outside;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected RepositoryType() { }

	public RepositoryType(RepositoryTypeID repositoryTypeID, boolean outside)
	{
		this(repositoryTypeID.organisationID, repositoryTypeID.repositoryTypeID, outside);
	}

	public RepositoryType(String organisationID, String repositoryTypeID, boolean outside)
	{
		Organisation.assertValidOrganisationID(organisationID);
		ObjectIDUtil.assertValidIDString(repositoryTypeID, "repositoryTypeID");

		this.organisationID = organisationID;
		this.repositoryTypeID = repositoryTypeID;
		this.outside = outside;
		this.name = new RepositoryTypeName(this);
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public String getRepositoryTypeID()
	{
		return repositoryTypeID;
	}

	public RepositoryTypeName getName()
	{
		return name;
	}

	public boolean isOutside()
	{
		return outside;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((organisationID == null) ? 0 : organisationID.hashCode());
		result = prime * result
				+ ((repositoryTypeID == null) ? 0 : repositoryTypeID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!(obj instanceof RepositoryType))
			return false;
		final RepositoryType other = (RepositoryType) obj;
		return Util.equals(this.organisationID, other.organisationID) && Util.equals(this.repositoryTypeID, other.repositoryTypeID);
	}
}
