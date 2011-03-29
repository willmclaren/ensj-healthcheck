/*
 * Copyright (C) 2003 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.ensembl.healthcheck.testcase.variation;

import java.util.HashMap;
import java.util.Map;
import org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionBase;
import org.ensembl.healthcheck.DatabaseRegistryEntry;

/**
 * Compare the number of variations between the current database and the
 * database on the secondary server.
 */

public class ComparePreviousVersionVariations extends ComparePreviousVersionBase {

	/**
	 * Create a new testcase.
	 */
	public ComparePreviousVersionVariations() {

		addToGroup("variation");
		addToGroup("variation-release");
		setDescription("Compare the number of variations in the current database with those from the equivalent database on the secondary server");
	}

	protected Map getCounts(DatabaseRegistryEntry dbre) {
            
            Map<String, Integer> counts = new HashMap<String, Integer>();
            
            // Count variations by source
            counts.putAll(getCountsBySQL(dbre, "SELECT s.name, COUNT(*) FROM variation v JOIN source s ON (s.source_id = v.source_id) GROUP BY s.name"));
            // Count total number of variations
            counts.putAll(getCountsBySQL(dbre, "SELECT 'all sources', COUNT(*) FROM variation"));
            
            return counts;
	}

	// ------------------------------------------------------------------------

	protected String entityDescription() {

		return "number of variations from";

	}

	// ------------------------------------------------------------------------

	protected double threshold() {

		return 1;

	}

	// ------------------------------------------------------------------------

} // ComparePreviousVersionVariations