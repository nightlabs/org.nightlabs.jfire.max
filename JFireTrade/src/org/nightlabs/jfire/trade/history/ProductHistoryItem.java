package org.nightlabs.jfire.trade.history;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.accounting.pay.ModeOfPaymentFlavour;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.store.deliver.ModeOfDeliveryFlavour;
import org.nightlabs.jfire.trade.ArticleContainer;
import org.nightlabs.jfire.trade.LegalEntity;

/**
 * This object contains trade specific information for a {@link Product}.
 * Instances of this object are grouped in an {@link ProductHistory}.
 *  
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class ProductHistoryItem
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public enum ProductHistoryItemType {
		ALLOCATION,
		PAYMENT,
		DELIVERY,
		OFFER,
		INVOICE,
		DELIVERY_NOTE,
	}
	
	private User user;
	private String name;
	private String description;
	private ArticleContainer articleContainer;
	private LegalEntity customer;
	private ModeOfDeliveryFlavour modeOfDeliveryFlavour;
	private ModeOfPaymentFlavour modeOfPaymentFlavour;
	private Date createDT;
	private ProductHistoryItemType type;
	
	/**
	 * @param user 
	 * @param name
	 * @param description
	 * @param articleContainer
	 * @param customer
	 * @param modeOfDeliveryFlavour
	 * @param modeOfPaymentFlavour
	 * @param createDT
	 * @param type
	 */
	public ProductHistoryItem(User user, String name, String description,
			ArticleContainer articleContainer, LegalEntity customer,
			ModeOfDeliveryFlavour modeOfDeliveryFlavour,
			ModeOfPaymentFlavour modeOfPaymentFlavour, Date createDT,
			ProductHistoryItemType type) 
	{
		this.user = user;
		this.name = name;
		this.description = description;
		this.articleContainer = articleContainer;
		this.customer = customer;
		this.modeOfDeliveryFlavour = modeOfDeliveryFlavour;
		this.modeOfPaymentFlavour = modeOfPaymentFlavour;
		this.createDT = createDT;
		this.type = type;
	}

	/**
	 * Returns the user.
	 * @return the user
	 */
	public User getUser() {
		return user;
	}
	
	/**
	 * Returns the description.
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Returns the articleContainer.
	 * @return the articleContainer
	 */
	public ArticleContainer getArticleContainer() {
		return articleContainer;
	}
	
	/**
	 * Returns the customer.
	 * @return the customer
	 */
	public LegalEntity getCustomer() {
		return customer;
	}
	
	/**
	 * Returns the modeOfDeliveryFlavour.
	 * @return the modeOfDeliveryFlavour
	 */
	public ModeOfDeliveryFlavour getModeOfDeliveryFlavour() {
		return modeOfDeliveryFlavour;
	}
	
	/**
	 * Returns the modeOfPaymentFlavour.
	 * @return the modeOfPaymentFlavour
	 */
	public ModeOfPaymentFlavour getModeOfPaymentFlavour() {
		return modeOfPaymentFlavour;
	}
	
	/**
	 * Returns the createDT.
	 * @return the createDT
	 */
	public Date getCreateDT() {
		return createDT;
	}

	/**
	 * Returns the name.
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the type.
	 * @return the type
	 */
	public ProductHistoryItemType getType() {
		return type;
	}
	
	/**
	 * Detaches all fields of this {@link ProductHistoryItem}.
	 * @param pm the PersistenceManager used for detaching
	 */
	public void detach(PersistenceManager pm) {
		this.articleContainer = pm.detachCopy(this.articleContainer);
		this.customer = pm.detachCopy(this.customer);
		this.modeOfDeliveryFlavour = pm.detachCopy(modeOfDeliveryFlavour);
		this.modeOfPaymentFlavour = pm.detachCopy(modeOfPaymentFlavour);
		this.user = pm.detachCopy(this.user);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((articleContainer == null) ? 0 : articleContainer.hashCode());
		result = prime * result + ((createDT == null) ? 0 : createDT.hashCode());
		result = prime * result + ((customer == null) ? 0 : customer.hashCode());
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime
				* result
				+ ((modeOfDeliveryFlavour == null) ? 0 : modeOfDeliveryFlavour
						.hashCode());
		result = prime
				* result
				+ ((modeOfPaymentFlavour == null) ? 0 : modeOfPaymentFlavour.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((user == null) ? 0 : user.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ProductHistoryItem other = (ProductHistoryItem) obj;
		if (articleContainer == null) {
			if (other.articleContainer != null)
				return false;
		} else if (!articleContainer.equals(other.articleContainer))
			return false;
		if (createDT == null) {
			if (other.createDT != null)
				return false;
		} else if (!createDT.equals(other.createDT))
			return false;
		if (customer == null) {
			if (other.customer != null)
				return false;
		} else if (!customer.equals(other.customer))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (modeOfDeliveryFlavour == null) {
			if (other.modeOfDeliveryFlavour != null)
				return false;
		} else if (!modeOfDeliveryFlavour.equals(other.modeOfDeliveryFlavour))
			return false;
		if (modeOfPaymentFlavour == null) {
			if (other.modeOfPaymentFlavour != null)
				return false;
		} else if (!modeOfPaymentFlavour.equals(other.modeOfPaymentFlavour))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		return true;
	}
	
	
}
