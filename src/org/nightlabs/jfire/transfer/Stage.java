package org.nightlabs.jfire.transfer;

/**
 * The different stages of the transfer process.
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
public enum Stage {
	Initial, ClientBegin, ServerBegin, ClientDoWork, ServerDoWork, ClientEnd, ServerEnd
}