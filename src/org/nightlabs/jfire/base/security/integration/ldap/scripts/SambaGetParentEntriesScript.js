/**
 * This is initial script which is used when new LDAPServer and corresponding LDAPScriptSet are created. 
 * All changes in this particular file WILL NOT be reflected in existing LDAPServers but only in new ones.
 * 
 * This script is used for generating a List with names of entries which are parents to all LDAP user entries which should be synchronized.
 * Used for synchronization when LDAPServer is a leading system. 
 * 
 * Returns an ArrayList<String> with entries names.
 *  
 */
var parentEntries = new java.util.ArrayList();
parentEntries.add("ou=Users,dc=nightlabs,dc=de");
parentEntries;