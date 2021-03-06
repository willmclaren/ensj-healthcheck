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
import java.sql.Statement;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * An EnsEMBL Healthcheck test case that checks that all the genome_dbs for a
 * method_link_species_set are present in the genomic_aligns
 */

public class CheckGenomicAlignGenomeDBs extends SingleDatabaseTestCase {

	/**
	 * Create an CheckGenomicAlignGenomeDBs that applies to a specific set of
	 * databases.
	 */
	public CheckGenomicAlignGenomeDBs() {

		addToGroup("compara_genomic");
		setDescription("Check the genome_dbs for a method_link_species_set are present in the genomic_aligns");
		setTeamResponsible(Team.COMPARA);

	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *            The database to use.
	 * @return true if the test passed.
	 * 
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		/**
		 * Check have entries in the genomic_align table
		 */
		if (!tableHasRows(con, "genomic_align")) {
			ReportManager.problem(this, con,
					"No entries in the genomic_align table");
			return result;
		}
		if (!tableHasRows(con, "genomic_align_block")) {
			ReportManager.problem(this, con,
					"No entries in the genomic_align_block table");
			return result;
		}
		if (!tableHasRows(con, "method_link_species_set")) {
			ReportManager.problem(this, con,
					"No entries in the method_link_species_set table");
			return result;
		}
		/**
		 * Get all method_link_species_set_ids for genomic_align_blocks
		 */
		String[] method_link_species_set_ids = DBUtils
				.getColumnValues(con,
						"SELECT distinct(method_link_species_set_id) FROM genomic_align_block");

		if (method_link_species_set_ids.length > 0) {

			for (int i = 0; i < method_link_species_set_ids.length; i++) {
				/**
				 * Expected number of genome_db_ids
				 */

				String gdb_sql = "SELECT COUNT(*) FROM species_set LEFT JOIN method_link_species_set USING (species_set_id) WHERE method_link_species_set_id = "
						+ method_link_species_set_ids[i];
				String[] num_genome_db_ids = DBUtils.getColumnValues(con,
						gdb_sql);

				/**
				 * Find genome_db_ids in genomic_aligns. For speed, only look at
				 * the first 100 genomic_align_blocks. If the test fails, it
				 * could be by chance that not all the genome_db_ids are found.
				 * Expect the number of distinct genome_db_ids to be the same as
				 * the number of genome_db_ids in the species set except when I
				 * have an ancestor when the number from the genomic_aligns will
				 * be one larger. Don't specifically test for this, just check
				 * if it's equal to or larger - more worried if it's smaller ie
				 * missed some expected genome_db_ids.
				 */
				String useful_sql;
				useful_sql = "SELECT COUNT(DISTINCT genome_db_id) FROM (SELECT * FROM genomic_align_block WHERE method_link_species_set_id = "
						+ method_link_species_set_ids[i]
						+ " limit 100) t1 LEFT JOIN genomic_align USING (genomic_align_block_id) LEFT JOIN dnafrag USING (dnafrag_id) HAVING COUNT(DISTINCT genome_db_id) >= (SELECT COUNT(*) FROM species_set LEFT JOIN method_link_species_set USING (species_set_id) WHERE method_link_species_set_id = "
						+ method_link_species_set_ids[i] + " )";
				String[] success = DBUtils.getColumnValues(con, useful_sql);

				if (success.length > 0) {
					/**
					 * System.out.println("MLSS " +
					 * method_link_species_set_ids[i] + " real " + success[0] +
					 * " expected " + num_genome_db_ids[0]);
					 */
					ReportManager
							.correct(
									this,
									con,
									"All genome_dbs are present in the genomic_aligns for method_link_species_set_id "
											+ method_link_species_set_ids[i]);
				} else {
					ReportManager
							.problem(
									this,
									con,
									"WARNING not all the genome_dbs are present in the first 100 genomic_align_block_ids. Could indicate a problem with alignment with method_link_species_set_id "
											+ method_link_species_set_ids[i]);
					ReportManager.problem(this, con, "USEFUL SQL: "
							+ useful_sql);
					result = false;
				}
			}
		}

		result &= checkAlignmentsOnMT(con);

		return result;

	}

	public boolean checkAlignmentsOnMT(Connection comparaCon) {

		boolean result = true;

		String sql1 = "SELECT method_link_species_set.name, genome_db.name, method_link_species_set_id, dnafrag_id"
				+ " FROM method_link_species_set LEFT JOIN method_link USING (method_link_id)"
				+ " LEFT JOIN species_set USING (species_set_id)"
				+ " LEFT JOIN genome_db USING (genome_db_id)"
				+ " LEFT JOIN dnafrag ON (genome_db.genome_db_id = dnafrag.genome_db_id AND dnafrag.name = 'MT')"
				+ " WHERE (class LIKE 'GenomicAlignTree%' OR class LIKE 'GenomicAlign%multiple%') AND dnafrag.name = 'MT'";
		try {
			Statement stmt1 = comparaCon.createStatement();
			ResultSet rs1 = stmt1.executeQuery(sql1);
			while (rs1.next()) {
				String sql2 = "SELECT count(*) FROM genomic_align WHERE method_link_species_set_id = "
						+ rs1.getInt(3) + " AND dnafrag_id = " + rs1.getInt(4);
				Statement stmt2 = comparaCon.createStatement();
				ResultSet rs2 = stmt2.executeQuery(sql2);
				while (rs2.next()) {
					if (rs2.getInt(1) == 0) {
						result = false;
						ReportManager.problem(
								this,
								comparaCon,
								"The MT chromosome from " + rs1.getString(2)
										+ " is not present in the "
										+ rs1.getString(1) + " alignments");
					}
				}
			}
			rs1.close();
			stmt1.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;

	}

} // CheckGenomicAlignGenomeDBs
