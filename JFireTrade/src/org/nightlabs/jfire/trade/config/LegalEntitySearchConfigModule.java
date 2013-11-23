package org.nightlabs.jfire.trade.config;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

import org.nightlabs.jfire.person.PersonSearchConfigModule;
import org.nightlabs.jfire.prop.search.PropSearchFilter;
import org.nightlabs.jfire.trade.query.LegalEntitySearchFilter;

/**
 * A PersonSearchComposite that constrains the filter-class to {@link LegalEntitySearchFilter} but
 * besides that provides the same functionality to configure search-fields and result-viewer.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireTrade_LegalEntitySearchConfigModule")
	@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class LegalEntitySearchConfigModule
extends PersonSearchConfigModule
{
	private static final long serialVersionUID = 20100405L;

	/**
	 * {@inheritDoc}
	 * <p>
	 * Returns {@link LegalEntitySearchFilter}.class
	 * </p>
	 */
	@Override
	public Class<? extends PropSearchFilter> getFilterClass() {
		return LegalEntitySearchFilter.class;
	}
	
	protected String getDefaultResultViewerIdentifier() {
		// FIXME: Well this string is very special and only known in the rcp-client, this should come from somewhere else
		return "org.nightlabs.jfire.trade.ui.legalentity.search.LegalEntityTableViewer";
	}
	
}
