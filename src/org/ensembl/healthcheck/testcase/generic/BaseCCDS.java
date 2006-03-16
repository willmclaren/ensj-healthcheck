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

package org.ensembl.healthcheck.testcase.generic;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.Utils;

/**
 * Check that all the transcripts in the a particular CCDS set are present.
 * Looks for a file called ccds_{species_name}_{assembly_version}.txt
 * e.g. ccds_homo_sapiens_35.txt
 */
public abstract class BaseCCDS extends SingleDatabaseTestCase {

	/**
   * This only really applies to core databases
   */
  public void types() {

      removeAppliesToType(DatabaseType.OTHERFEATURES);
      removeAppliesToType(DatabaseType.ESTGENE);
      removeAppliesToType(DatabaseType.VEGA);
      removeAppliesToType(DatabaseType.CDNA);
      
  }
  
	public boolean doRun(DatabaseRegistryEntry dbre) {

		boolean result = true;

		String ccdsFile = "ccds_" + dbre.getSpecies() + "_" + dbre.getNumericGeneBuildVersion() + ".txt";
		
		if (!(new File(ccdsFile)).exists()) {
			
			logger.warning("CCDS healthcheck can't find corresponding file for " + dbre.getSpecies() + " assembly version " + dbre.getNumericGeneBuildVersion() + "; returning true");
			return true;

		}
		
		int notPresent = 0;

		Connection con = dbre.getConnection();

		// cache all stable IDs
		ArrayList transcriptArray = new ArrayList(Arrays.asList(getColumnValues(con, "SELECT stable_id FROM transcript_stable_id")));
		HashMap transcriptStableIDHash = new HashMap();
		Iterator it = transcriptArray.iterator();
		while (it.hasNext()) {
			String stable_id = (String)it.next();
			transcriptStableIDHash.put(stable_id, stable_id);
		}
		
		String[] lines = Utils.readTextFile(ccdsFile);
		
		for (int i = 0; i < lines.length; i++) {
		
			String[] bits = lines[i].split("\\s");
			String stable_id = bits[0];
			String ccds = bits[1];
			
			if (!transcriptStableIDHash.containsKey(stable_id)) {
				
				logger.warning("Missing transcript " + stable_id + " corresponding to " + ccds);
				notPresent++;
				result = false;
				
			} 
			
		}
		
		if (!result) {
			
			ReportManager.problem(this, con, notPresent + " transcripts in the CCDS set are not present");
			
		} else {
			
			ReportManager.correct(this, con, "All transcripts in the CCDS set are not present");
			
		}
		
		return result;

	} // run
	
} // BaseCCDS
