/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.basic;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.RegexBranch;

public class RegexBranchSystemTest extends SystemTestBase {
	
	public static final double defaultDynamicPool = Properties.DYNAMIC_POOL;

	@After
	public void resetProperties() {
		Properties.DYNAMIC_POOL = defaultDynamicPool;
	}

	
	@Test
	public void testRegexBranch() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = RegexBranch.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.DYNAMIC_POOL = 1d / 3d;

		String[] command = new String[] { "-generateSuite", "-class", targetClass }; // , "-Dprint_to_system=true"

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
		TestSuiteChromosome best = ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
}
