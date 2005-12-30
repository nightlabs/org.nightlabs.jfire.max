/*
 * Created 	on Apr 8, 2005
 * 					by alex
 *
 */
package org.nightlabs.ipanema.trade;

import org.nightlabs.ipanema.person.util.PersonSearchFilter;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class LegalEntitySearchFilter extends PersonSearchFilter {

	/**
	 * 
	 */
	public LegalEntitySearchFilter() {
		super();
	}

	/**
	 * @param _conjunction
	 */
	public LegalEntitySearchFilter(int _conjunction) {
		super(_conjunction);
	}

	/**
	 * @param _conjunction
	 * @param personType
	 */
	public LegalEntitySearchFilter(int _conjunction, String personType) {
		super(_conjunction, personType);
	}

	public void setPersonVariableCondition(StringBuffer filter) {
		filter.append(PERSON_VARNAME+" == this.person");
	}
	
	protected Class getExtendClass() {
		return LegalEntity.class;
	}
}
