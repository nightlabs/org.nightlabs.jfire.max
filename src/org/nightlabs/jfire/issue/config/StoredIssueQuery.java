package org.nightlabs.jfire.issue.config;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import org.nightlabs.io.DataBuffer;
import org.nightlabs.jdo.query.JDOQuery;
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

	public StoredIssueQuery(String organisationID, long storedIssueQueryID) 
	{
		this.organisationID = organisationID;
		this.storedIssueQueryID = storedIssueQueryID;
	}
	
	public void setIssueQueries(Collection<JDOQuery> jdoQuery) {
		try {
			DataBuffer db = new DataBuffer();
			OutputStream out = new DeflaterOutputStream(db.createOutputStream());
			try {
				XStream xStream = new XStream(new XppDriver());
				xStream.toXML(jdoQuery, out);
			} finally {
				out.close();
			}
			serializedIssueQuery = db.createByteArray();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public Collection<JDOQuery> getIssueQueries() {
		InputStream in = null;
		try {
			DataBuffer db = new DataBuffer(new InflaterInputStream(new ByteArrayInputStream(serializedIssueQuery)));
			in = db.createInputStream();
			XStream xStream = new XStream(new XppDriver());
			Collection<JDOQuery> q = (Collection<JDOQuery>) xStream.fromXML(in);
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

	public void setName(String name) {
		this.name = name;
	}
}
