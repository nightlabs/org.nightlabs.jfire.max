package org.nightlabs.jfire.pbx;

import java.io.Serializable;

import org.nightlabs.jfire.prop.id.DataFieldID;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class Call
implements Serializable
{
	private static final long serialVersionUID = 1L;

	private DataFieldID phoneNumberDataFieldID;

	public Call(DataFieldID phoneNumberDataFieldID) {
		this.phoneNumberDataFieldID = phoneNumberDataFieldID;
	}

	public DataFieldID getPhoneNumberDataFieldID() {
		return phoneNumberDataFieldID;
	}
}
