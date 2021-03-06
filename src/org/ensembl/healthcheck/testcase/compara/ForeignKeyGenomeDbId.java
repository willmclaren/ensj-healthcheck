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

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * An EnsEMBL Healthcheck test case that looks for broken foreign-key
 * relationships.
 */

public class ForeignKeyGenomeDbId extends SingleDatabaseTestCase {

    /**
     * Create an ForeignKeyGenomeDbId that applies to a specific set of databases.
     */
    public ForeignKeyGenomeDbId() {

        addToGroup("compara_genomic");
        addToGroup("compara_homology");
        setDescription("Check for broken foreign-key relationships in ensembl_compara databases.");
        setTeamResponsible(Team.COMPARA);

    }

    /**
     * Run the test.
     * 
     * @param dbre
     *          The database to use.
     * @return true if the test pased.
     *  
     */
    public boolean run(DatabaseRegistryEntry dbre) {

        boolean result = true;

        Connection con = dbre.getConnection();

        if (tableHasRows(con, "genome_db")) {

            result &= checkForOrphans(con, "dnafrag", "genome_db_id", "genome_db", "genome_db_id");
            result &= checkForOrphansWithConstraint(con, "member", "genome_db_id", "genome_db", "genome_db_id", "genome_db_id != 0");
            result &= checkForOrphans(con, "species_set", "genome_db_id", "genome_db", "genome_db_id");
            result &= checkForOrphansWithConstraint(con, "genome_db", "genome_db_id", "species_set", "genome_db_id", "taxon_id != 0");

        } else {
            ReportManager.correct(this, con, "NO ENTRIES in genome_db table, so nothing to test IGNORED");
        }

        return result;

    }

} // ForeignKeyGenomeDbId
