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


package de.cologneintelligence.fitgoodies;

import de.cologneintelligence.fitgoodies.test.FitGoodiesTestCase;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;


public final class FixtureTest2 extends FitGoodiesTestCase {
	public static class DummyFixture extends ColumnFixture {
		public String value = "xyz";

		public final int getValue() {
			return 42;
		}
	}

	private DummyFixture dummy;
	private ValueReceiver taField;

	@Before
	public void setUp() throws Exception {
		dummy = new DummyFixture();
		taField = ValueReceiver.on(dummy, DummyFixture.class.getField("value"));
	}

	@Test
	public void testProcessWithPositiveShortcuts() {
		Parse cell = parseTd("${nonEmpty()}");
		ValueReceiver ta = new Fixture().processCell(cell, taField);

		assertThat(ta, is(nullValue()));
		assertThat(cell.text(), containsString("empty"));

		cell = parseTd("${nonEmpty()}</td>");
		dummy.value = null;
		ta = new Fixture().processCell(cell, taField);

		assertThat(ta, is(nullValue()));
		assertThat(cell.text(), containsString("(null)"));
		assertThat(cell.text(), containsString("value must not be empty"));
	}

	@Test
	public void testProcessWithNegativeShortcuts()  {
		Parse cell = parseTd("${empty()}");
		ValueReceiver ta = new Fixture().processCell(cell, taField);

		assertThat(ta, is(nullValue()));
		assertThat(cell.text(), containsString("value must be empty"));

		cell = parseTd("${empty()}");
		dummy.value = null;
		ta = new Fixture().processCell(cell, taField);
		assertThat(ta, is(nullValue()));
		assertThat(cell.text().startsWith("(null)"), is(true));
		assertThat(cell.text(), containsString("value is empty"));


		cell = parseTd("${empty()}");
		dummy.value = "";
		ta = new Fixture().processCell(cell, taField);
		assertThat(ta, is(nullValue()));
		assertThat(cell.text(), containsString("value is empty"));
	}
}
