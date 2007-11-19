
	public static IssueID create(String issuePK)
	{
		IssueID n = new IssueID();

		String[] sa = issuePK.split("/");
		if (sa.length != 2)
			throw new IllegalArgumentException("issuePK does not contain exactly 2 parts: " + issuePK);

		n.organisationID = sa[0];
		n.issueID = Long.parseLong(sa[1]);
		return n;
	}

	public String getPrimaryKey()
	{
		return org.nightlabs.jfire.issue.Issue.getPrimaryKey(organisationID, issueID);
	}