/**
 * File: GeneDescriptionSourceTest.java
 * Created by: dstaines
 * Created on: May 26, 2009
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import org.ensembl.healthcheck.testcase.AbstractRowCountTestCase;

/**
 * Test to see if newline found in the description
 * 
 * @author dstaines
 * 
 */
public class GeneDescriptionNewline extends AbstractRowCountTestCase {

	public GeneDescriptionNewline() {
		super();
		addToGroup(AbstractEgCoreTestCase.EG_GROUP);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.healthcheck.testcase.AbstractRowCountTestCase#getExpectedCount
	 * ()
	 */
	@Override
	protected int getExpectedCount() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ensembl.healthcheck.testcase.AbstractIntegerTestCase#getSql()
	 */
	@Override
	protected String getSql() {
		return "select count(*) from gene where description like '%\\n%'";
	}

}