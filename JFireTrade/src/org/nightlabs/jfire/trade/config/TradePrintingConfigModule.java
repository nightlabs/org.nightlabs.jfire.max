package org.nightlabs.jfire.trade.config;

import org.nightlabs.jfire.config.ConfigModule;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;

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
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_TradePrintingConfigModule")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class TradePrintingConfigModule extends ConfigModule {
	private static final long serialVersionUID = 1L;
	
	
	/** @jdo.field persistence-modifier="persistent" */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean printDeliveryNoteByDefault = false;
	/** @jdo.field persistence-modifier="persistent" */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private int deliveryNoteCopyCount = 0;
	
	/** @jdo.field persistence-modifier="persistent" */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean printInvoiceByDefault = false;
	/** @jdo.field persistence-modifier="persistent" */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
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
