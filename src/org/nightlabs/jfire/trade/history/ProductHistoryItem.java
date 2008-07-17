package org.nightlabs.jfire.trade.history;

import java.util.Date;

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
{
	public enum ProductHistoryItemType {
		ALLOCATION,
		PAYMENT,
		DELIVERY,
		OFFER_ACCEPTED,
		INVOICE_FINALIZED,
		DELIVERY_NOTE_FINALIZED
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
	
}
