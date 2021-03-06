/*
 * Copyright [1999-2013] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.ensembl.healthcheck.testcase.compara;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.commons.lang.StringUtils;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Repair;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.testcase.compara.MethodLinkSpeciesSetTag;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * An EnsEMBL Healthcheck test case that looks for broken foreign-key relationships.
 */

public class MLSSTagMaxAlign extends MethodLinkSpeciesSetTag {

	/**
	 * Create an ForeignKeyMethodLinkId that applies to a specific set of databases.
	 */
	public MLSSTagMaxAlign() {

		addToGroup("compara_genomic");
		setDescription("Tests that proper max_alignment_length have been defined in the method_link_species_set_tag table.");
		setTeamResponsible(Team.COMPARA);
		tagToCheck = "max_align";
	}

	/**
	 * Check the max_align in the method_link_species_set_tag table
	 */
	boolean doCheck(Connection con) {

		boolean result = true;

		int globalMaxAlignmentLength = 0;

		// Check whether tables are empty or not
		if (!tableHasRows(con, "genomic_align")) {
			ReportManager.correct(this, con, "NO ENTRIES in genomic_align table");
			return true;
		}

		// Calculate current max_alignment_length by method_link_species_set
		String sql = "SELECT method_link_species_set_id, MAX(dnafrag_end - dnafrag_start) FROM genomic_align GROUP BY method_link_species_set_id";
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			// Add 2 to the dnafrag_end - dnafrag_start in order to get length + 1.
			// Adding this at this point is probably faster than asking MySQL to add 2
			// to every single row...
			while (rs.next()) {
				MetaEntriesToAdd.put(rs.getString(1), new Integer(rs.getInt(2) + 2).toString());
				if (rs.getInt(2) > globalMaxAlignmentLength) {
					globalMaxAlignmentLength = rs.getInt(2) + 2;
				}
			}
			rs.close();
			stmt.close();
		} catch (SQLException se) {
			se.printStackTrace();
			result = false;
		}

		// Calculate current max_align by method_link_species_set for constrained elements
		String sql_ce = "SELECT method_link_species_set_id, MAX(dnafrag_end - dnafrag_start) FROM constrained_element GROUP BY method_link_species_set_id";
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql_ce);
			// Add 2 to the dnafrag_end - dnafrag_start in order to get length + 1.
			// Adding this at this point is probably faster than asking MySQL to add 2
			// to every single row...
			while (rs.next()) {
				MetaEntriesToAdd.put(rs.getString(1), new Integer(rs.getInt(2) + 2).toString());
				if (rs.getInt(2) > globalMaxAlignmentLength) {
					globalMaxAlignmentLength = rs.getInt(2) + 2;
				}
			}
			rs.close();
			stmt.close();
		} catch (SQLException se) {
			se.printStackTrace();
			result = false;
		}

		// Get values currently stored in the method_link_species_set_tag table
		sql = "SELECT method_link_species_set_id, value, COUNT(*) FROM method_link_species_set_tag WHERE tag = \"max_align\" GROUP BY method_link_species_set_id";
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				if (rs.getInt(3) != 1) {
					MetaEntriesToRemove.put(rs.getString(1), rs.getString(2));
				} else if (MetaEntriesToAdd.containsKey(rs.getString(1))) {
					if (!MetaEntriesToAdd.get(rs.getString(1)).equals(rs.getString(2))) {
						MetaEntriesToUpdate.put(rs.getString(1), MetaEntriesToAdd.get(rs.getString(1)));
					}
					MetaEntriesToAdd.remove(rs.getString(1));
				} else {
					MetaEntriesToRemove.put(rs.getString(1), rs.getString(2));
				}
			}
			rs.close();
			stmt.close();
		} catch (SQLException se) {
			se.printStackTrace();
			result = false;
		}

		return result;

	}


}
