package org.nightlabs.jfire.issue.config;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import org.nightlabs.io.DataBuffer;
import org.nightlabs.jfire.issue.query.IssueQuery;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.XppDriver;

/**
 * @author Chairat Kongarayawetchakun <!-- chairat at nightlabs dot de -->
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.issue.id.StoredIssueQueryID"
 *		detachable="true"
 *		table="JFireIssueTracking_StoredIssueQuery"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, storedIssueQueryID"
 *
 * @jdo.fetch-group name="StoredIssueQuery.this" fetch-groups="default" fields="name, serializedIssueQuery"
 **/
public class StoredIssueQuery
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_STOREDISSUEQUERY = "StoredIssueQuery.this";
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	
	/**
	 * @jdo.field primary-key="true"
	 */
	private long storedIssueQueryID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String name;
	
	/**
	 * @jdo.field persistence-modifier="persistent" collection-type="array" serialized-element="true"
	 */
	private byte[] serializedIssueQuery;
	
	/**
	 * @deprecated Constructor exists only for JDO! 
	 */
	protected StoredIssueQuery() { }

	public StoredIssueQuery(String organisationID, long storedIssueQueryID, String name, IssueQuery issueQuery) 
	{
		this.organisationID = organisationID;
		this.storedIssueQueryID = storedIssueQueryID;
		this.name = name;
		
		setIssueQuery(issueQuery);
	}
	
	public void setIssueQuery(IssueQuery issueQuery) {
		DataBuffer db = null;
		OutputStream out = null;
		try {
			db = new DataBuffer(new InflaterInputStream(new ByteArrayInputStream(serializedIssueQuery)));
			out = new DeflaterOutputStream(db.createOutputStream());
			XStream xStream = new XStream(new XppDriver());
			xStream.toXML(issueQuery, out);
			serializedIssueQuery = db.createByteArray();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally {
			try {
				out.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			
		}
	}
	
	public IssueQuery getIssueQuery() {
		InputStream in = null;
		try {
			DataBuffer db = new DataBuffer(new InflaterInputStream(new ByteArrayInputStream(serializedIssueQuery)));
			in = db.createInputStream();
			XStream xStream = new XStream(new XppDriver());
			IssueQuery q = (IssueQuery) xStream.fromXML(in);
			return q;
		} 
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally {
			try {
				in.close();	
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public String getName() {
		return name;
	}
}
