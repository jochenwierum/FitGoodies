/*
 * Copyright (c) 2009-2012  Cologne Intelligence GmbH
 * This file is part of FitGoodies.
 *
 * FitGoodies is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FitGoodies is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FitGoodies.  If not, see <http://www.gnu.org/licenses/>.
 */


package de.cologneintelligence.fitgoodies.parsers;

import de.cologneintelligence.fitgoodies.ScientificDouble;
import de.cologneintelligence.fitgoodies.test.FitGoodiesTestCase;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


public final class ScientificDoubleParserTest extends FitGoodiesTestCase {
	@Test
	public void testParser() throws Exception {
		ScientificDoubleParser parser = new ScientificDoubleParser();

		assertThat(parser.parse("1.3", null).equals(new ScientificDouble(1.326)), is(true));
		assertThat(parser.parse("1.3", null).equals(new ScientificDouble(1.396)), is(false));

		assertThat(parser.parse("1.5e1", null).equals(new ScientificDouble(14.5)), is(true));
		assertThat(parser.parse("1.5e1", null).equals(new ScientificDouble(15.8)), is(false));

		assertThat(parser.parse("2", null).equals(new ScientificDouble(2.3)), is(true));
		assertThat(parser.parse("2", null).equals(new ScientificDouble(1.3)), is(false));

		assertThat(parser.parse("1.5e-1", null).equals(new ScientificDouble(0.148)), is(true));
		assertThat(parser.parse("1.5e-1", null).equals(new ScientificDouble(0.158)), is(false));

	}
}
