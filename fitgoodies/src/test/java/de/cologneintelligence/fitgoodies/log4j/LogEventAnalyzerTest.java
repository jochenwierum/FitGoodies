/*
 * Copyright (c) 2009-2012  Cologne Intelligence GmbH
 * This file is part of FitGoodies.
 *
 * Foobar is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Foobar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.cologneintelligence.fitgoodies.log4j;

import de.cologneintelligence.fitgoodies.test.FitGoodiesTestCase;
import fit.Fixture;
import fit.Parse;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public final class LogEventAnalyzerTest extends FitGoodiesTestCase {
	private LoggingEvent[] list;

	@Before
	public void setUp() throws Exception {
		list = prepareCheckForGreenTest();
	}

	private LoggingEvent[] prepareCheckForGreenTest() {
		List<LoggingEvent> list = new LinkedList<LoggingEvent>();

		list.add(new LoggingEvent("com.fqdn.class1", null,
				100, Level.ERROR, "a message", "thread1",
				new ThrowableInformation(new RuntimeException("xxx")),
				"ndc", null, null));
		list.add(new LoggingEvent("com.fqdn.class1", null, 120,
				Level.INFO, "no error", "thread2", null, "ndc", null, null));
		list.add(new LoggingEvent("rootLogger", null, 140, Level.DEBUG,
				"a root message", "main",
				new ThrowableInformation(new RuntimeException("yyy")),
				null, null, null));

		return list.toArray(new LoggingEvent[list.size()]);
	}

	private Parse makeCell(final String string) throws ParseException {
		return new Parse("<td>" + string + "</td>", new String[]{"td"});
	}

	@Test
	public void testParseContains() throws ParseException {
		final Fixture fixture = mock(Fixture.class);
		final Parse cell1 = makeCell("a message");
		final Parse cell2 = makeCell("rOOt");
		final Parse cell3 = makeCell("non existing message");

		LogEventAnalyzer analyzer = new LogEventAnalyzerImpl(
				fixture, cell1, list);
		analyzer.processContains(new HashMap<String, String>());

		analyzer = new LogEventAnalyzerImpl(fixture, cell2, list);
		analyzer.processContains(new HashMap<String, String>());

		analyzer = new LogEventAnalyzerImpl(fixture, cell3, list);
		analyzer.processContains(new HashMap<String, String>());

		assertThat(cell1.text(), is(equalTo("a messagea message")));
		assertThat(cell2.text(), is(equalTo("rOOta root message")));
		assertThat(cell3.text(), is(equalTo("non existing message")));

		verify(fixture).right(cell1);
		verify(fixture).right(cell2);
		verify(fixture).wrong(cell3);
		verify(fixture).info(cell1, "(expected)");
		verify(fixture).info(cell1, "(actual)");
		verify(fixture).info(cell2, "(expected)");
		verify(fixture).info(cell2, "(actual)");
	}

	@Test
	public void testParseWithParameters() throws ParseException {
		final Fixture fixture = mock(Fixture.class);
		final Parse cell1 = makeCell("no error");
		final Parse cell2 = makeCell("root");
		final Parse cell3 = makeCell("no error");
		final Parse cell4 = makeCell("no error");

		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("minlevel", "Info");
		parameters.put("thread", "thread2");
		LogEventAnalyzer analyzer = new LogEventAnalyzerImpl(fixture, cell1, list);
		analyzer.processContains(parameters);

		parameters.clear();
		parameters.put("minlevel", "error");
		analyzer = new LogEventAnalyzerImpl(fixture, cell2, list);
		analyzer.processNotContains(parameters);

		parameters.clear();
		parameters.put("thread", "main");
		analyzer = new LogEventAnalyzerImpl(fixture, cell3, list);
		analyzer.processNotContains(parameters);

		parameters.clear();
		parameters.put("thread", "thread5");
		analyzer = new LogEventAnalyzerImpl(fixture, cell4, list);
		analyzer.processContains(parameters);

		assertThat(cell1.text(), is(equalTo("no errorno error")));
		assertThat(cell2.text(), is(equalTo("root")));
		assertThat(cell3.text(), is(equalTo("no error")));
		assertThat(cell4.text(), is(equalTo("no error")));

		verify(fixture).right(cell1);
		verify(fixture).right(cell2);
		verify(fixture).right(cell3);
		verify(fixture).wrong(cell4);
		verify(fixture).info(cell1, "(expected)");
		verify(fixture).info(cell1, "(actual)");
	}

	@Test
	public void testNotContains() throws ParseException {
		final Fixture fixture = mock(Fixture.class);
		final Parse cell1 = makeCell("an error");
		final Parse cell2 = makeCell("toor");
		final Parse cell3 = makeCell("root");

		LogEventAnalyzer analyzer = new LogEventAnalyzerImpl(
				fixture, cell1, list);
		analyzer.processNotContains(new HashMap<String, String>());

		analyzer = new LogEventAnalyzerImpl(fixture, cell2, list);
		analyzer.processNotContains(new HashMap<String, String>());

		analyzer = new LogEventAnalyzerImpl(fixture, cell3, list);
		analyzer.processNotContains(new HashMap<String, String>());

		assertThat(cell1.text(), is(equalTo("an error")));
		assertThat(cell2.text(), is(equalTo("toor")));
		assertThat(cell3.text(), is(equalTo("roota root message")));

		verify(fixture).right(cell1);
		verify(fixture).right(cell2);
		verify(fixture).wrong(cell3);
		verify(fixture).info(cell3, "(expected)");
		verify(fixture).info(cell3, "(actual)");
	}

	@Test
	public void testContainsException() throws ParseException {
		final Fixture fixture = mock(Fixture.class);
		final Parse cell1 = makeCell("xXx");
		final Parse cell2 = makeCell("RuntiMEException");
		final Parse cell3 = makeCell("IllegalStateException");

		LogEventAnalyzer analyzer = new LogEventAnalyzerImpl(
				fixture, cell1, list);
		analyzer.processContainsException(new HashMap<String, String>());

		analyzer = new LogEventAnalyzerImpl(fixture, cell2, list);
		analyzer.processContainsException(new HashMap<String, String>());

		analyzer = new LogEventAnalyzerImpl(fixture, cell3, list);
		analyzer.processContainsException(new HashMap<String, String>());

		assertThat(cell1.text(), is(equalTo("xXxjava.lang.RuntimeException: xxx")));
		assertThat(cell2.text(), is(equalTo("RuntiMEExceptionjava.lang.RuntimeException: xxx")));
		assertThat(cell3.text(), is(equalTo("IllegalStateException")));

		verify(fixture).right(cell1);
		verify(fixture).right(cell2);
		verify(fixture).wrong(cell3);
		verify(fixture).info(cell1, "(expected)");
		verify(fixture).info(cell1, "(actual)");
		verify(fixture).info(cell2, "(expected)");
		verify(fixture).info(cell2, "(actual)");
	}

	@Test
	public void testNotContainsException() throws ParseException {
		final Fixture fixture = mock(Fixture.class);
		final Parse cell1 = makeCell("Error message");
		final Parse cell2 = makeCell("IllegalStateException");
		final Parse cell3 = makeCell("Exception");

		LogEventAnalyzer analyzer = new LogEventAnalyzerImpl(fixture, cell1, list);
		analyzer.processNotContainsException(new HashMap<String, String>());

		analyzer = new LogEventAnalyzerImpl(fixture, cell2, list);
		analyzer.processNotContainsException(new HashMap<String, String>());

		analyzer = new LogEventAnalyzerImpl(fixture, cell3, list);
		analyzer.processNotContainsException(new HashMap<String, String>());

		assertThat(cell1.text(), is(equalTo("Error message")));
		assertThat(cell2.text(), is(equalTo("IllegalStateException")));
		assertThat(cell3.text(), is(equalTo("Exceptionjava.lang.RuntimeException: xxx")));

		verify(fixture).right(cell1);
		verify(fixture).right(cell2);
		verify(fixture).wrong(cell3);
		verify(fixture).info(cell3, "(expected)");
		verify(fixture).info(cell3, "(actual)");
	}
}
