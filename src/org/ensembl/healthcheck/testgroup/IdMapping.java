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

package org.ensembl.healthcheck.testgroup;

import org.ensembl.healthcheck.GroupOfTests;

/**
 * These are the tests that register themselves as id_mapping. The tests are:
 * 
 * <ul>
 *   <li> org.ensembl.healthcheck.testcase.generic.CoreForeignKeys </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.MappingSession </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.Archive </li> 
 *   <li> org.ensembl.healthcheck.testcase.funcgen.FuncgenForeignKeys </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.BlanksInsteadOfNulls </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.SeqRegionCoordSystem </li> 
 *   <li> org.ensembl.healthcheck.testcase.funcgen.FuncgenStableID </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.NullStrings </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.StableID </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.ESTStableID </li> 
 * </ul>
 *
 * @author Autogenerated
 *
 */
public class IdMapping extends GroupOfTests {
	
	public IdMapping() {

		addTest(
			org.ensembl.healthcheck.testcase.generic.CoreForeignKeys.class,
			org.ensembl.healthcheck.testcase.generic.MappingSession.class,
			org.ensembl.healthcheck.testcase.generic.Archive.class,
			org.ensembl.healthcheck.testcase.funcgen.FuncgenForeignKeys.class,
			org.ensembl.healthcheck.testcase.generic.BlanksInsteadOfNulls.class,
			org.ensembl.healthcheck.testcase.generic.SeqRegionCoordSystem.class,
			org.ensembl.healthcheck.testcase.funcgen.FuncgenStableID.class,
			org.ensembl.healthcheck.testcase.generic.NullStrings.class,
			org.ensembl.healthcheck.testcase.generic.StableID.class,
                        org.ensembl.healthcheck.testcase.generic.ESTStableID.class
		);
	}
}
