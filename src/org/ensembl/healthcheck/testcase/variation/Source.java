/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with
 * this library; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.healthcheck.testcase.variation;

import java.sql.Connection;

import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Species;


/**
 * Checks the source table to make sure it is OK. Only for mouse databse, to check external sources
 * 
 */
public class Source extends SingleDatabaseTestCase {

    /**
   * Creates a new instance of CheckSourceDataTableTestCase
   */
	public Source() {

		addToGroup("variation");
		setDescription("Check that the source table contains the right entries for mouse");
	}

    /**
     * Check various aspects of the meta table.
     * 
     * @param dbre The database to check.
     * @return True if the test passed.
     */
	public boolean run(final DatabaseRegistryEntry dbre) {
	    boolean result = true;

	    Connection con = dbre.getConnection();


	    if (dbre.getSpecies() == Species.MUS_MUSCULUS){
		String source = "Sanger";
		int mc = getRowCount(con,"SELECT COUNT(*) FROM source WHERE name = '" + source + "' AND version IS NULL");    
		if (mc == 0){
		    ReportManager.problem(this, con, "No entry for source " + source + " or has a not NULL version in Source table");
		    result = false;
		}
		else{
		    ReportManager.correct(this, con, "Source table correct in mouse");
		}
	    }
	    return result;
	} // run

    // --------------------------------------------------------------
}