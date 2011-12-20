/**
 * 
 */
package org.nightlabs.jfire.trade.dashboard;

import java.util.Date;

import org.nightlabs.jfire.transfer.id.AnchorID;

/**
 * @author abieber
 *
 */
public class LastCustomerTransaction {

	private AnchorID customerID;
	private String transactionType;
	private Date transactionDate;
	
	public LastCustomerTransaction() {
	}
	
	
	public LastCustomerTransaction(AnchorID customerID, String transactionType,
			Date transactionDate) {
		super();
		this.customerID = customerID;
		this.transactionType = transactionType;
		this.transactionDate = transactionDate;
	}



	public AnchorID getCustomerID() {
		return customerID;
	}

	public void setCustomerID(AnchorID customerID) {
		this.customerID = customerID;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public Date getTransactionDate() {
		return transactionDate;
	}

	public void setTransactionDate(Date transactionDate) {
		this.transactionDate = transactionDate;
	}

	
}
