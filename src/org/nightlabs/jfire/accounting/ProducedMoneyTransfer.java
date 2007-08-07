package org.nightlabs.jfire.accounting;



/**
* @author Chairat Kongarayawetchakun <chairatk[AT]nightlabs[DOT]de>
*
* @jdo.persistence-capable
*		identity-type="application"
*		objectid-class="org.nightlabs.jfire.accounting.id.ProducedMoneyTransferID
*		persistence-capable-superclass="org.nightlabs.jfire.accounting.MoneyTransfer"
*		detachable="true"
*		table="JFireTrade_ProducedMoneyTransfer"
*
* @jdo.create-objectid-class field-order="producedMoneyTransferID"
* 
* @jdo.inheritance strategy="new-table"
 */	
public class ProducedMoneyTransfer extends MoneyTransfer{
	/////// begin primary key ///////
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String producedMoneyTransferID;
	/////// end primary key ///////
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String primaryKey;
	
	protected ProducedMoneyTransfer(){
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	protected MoneyTransfer containerMoneyTransfer;
	/** 
	 * Used to create a MoneyTransfer accosiated to
	 * the (first) invoice of containerMoneyTransfer. 
	 * 
	 * @param containerMoneyTransfer
	 */
	public ProducedMoneyTransfer(MoneyTransfer containerMoneyTransfer)
	{
//		if (productedMoneyTransferID == null)
//			throw new NullPointerException("productedMoneyTransferID");
		
		this.containerMoneyTransfer = containerMoneyTransfer;
		this.subject = new ProducedMoneyTransferSubject(this);
	}
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ProducedMoneyTransferSubject subject;
	
	public MoneyTransfer getContainerMoneyTransfer() {
		return containerMoneyTransfer;
	}

	public ProducedMoneyTransferSubject getSubject() {
		return subject;
	}
	
	/**
	 * @return Returns the regionID.
	 */
	public String getProducedMoneyTransferID()
	{
		return producedMoneyTransferID;
	}
	/**
	 * @param regionID The regionID to set.
	 */
	protected void setProducedMoneyTransferID(String producedMoneyTransferID)
	{
		this.producedMoneyTransferID = producedMoneyTransferID;
		this.primaryKey = getPrimaryKey(producedMoneyTransferID);
	}
	public static String getPrimaryKey(String producedMoneyTransferID)
	{
		return producedMoneyTransferID;
	}
}
