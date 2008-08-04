package org.nightlabs.jfire.trade.config;

import org.nightlabs.jfire.config.ConfigModule;

/**
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.config.ConfigModule"
 *		detachable="true"
 *		table="JFireTrade_TradePrintingConfigModule"
 *
 * @jdo.inheritance strategy = "new-table"
 *
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
public class TradePrintingConfigModule extends ConfigModule {
	private static final long serialVersionUID = 1L;
	
	
	/** @jdo.field persistence-modifier="persistent" */
	private boolean printDeliveryNoteByDefault = false;
	/** @jdo.field persistence-modifier="persistent" */
	private int deliveryNoteCopyCount = 0;
	
	/** @jdo.field persistence-modifier="persistent" */
	private boolean printInvoiceByDefault = false;
	/** @jdo.field persistence-modifier="persistent" */
	private int invoiceCopyCount = 0;
	
	@Override
	public void init() {
		printDeliveryNoteByDefault = false;
		deliveryNoteCopyCount = 0;
		
		printInvoiceByDefault = false;
		invoiceCopyCount = 0;
	}

	public boolean isPrintDeliveryNoteByDefault() {
		return printDeliveryNoteByDefault;
	}

	public void setPrintDeliveryNoteByDefault(boolean printDeliveryNoteByDefault) {
		this.printDeliveryNoteByDefault = printDeliveryNoteByDefault;
	}

	public int getDeliveryNoteCopyCount() {
		return deliveryNoteCopyCount;
	}

	public void setDeliveryNoteCopyCount(int deliveryNotePrintoutNumber) {
		this.deliveryNoteCopyCount = deliveryNotePrintoutNumber;
	}

	public boolean isPrintInvoiceByDefault() {
		return printInvoiceByDefault;
	}

	public void setPrintInvoiceByDefault(boolean printInvoiceByDefault) {
		this.printInvoiceByDefault = printInvoiceByDefault;
	}

	public int getInvoiceCopyCount() {
		return invoiceCopyCount;
	}

	public void setInvoiceCopyCount(int invoicePrintoutNumber) {
		this.invoiceCopyCount = invoicePrintoutNumber;
	}
}
