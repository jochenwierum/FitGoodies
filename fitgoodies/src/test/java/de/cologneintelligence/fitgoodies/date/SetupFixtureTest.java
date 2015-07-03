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


package de.cologneintelligence.fitgoodies.date;

import de.cologneintelligence.fitgoodies.test.FitGoodiesTestCase;
import de.cologneintelligence.fitgoodies.util.DependencyManager;
import fit.Parse;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.util.Locale;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


public class SetupFixtureTest extends FitGoodiesTestCase {
    private SetupFixture fixture;

    @Before
    public void setUp() throws Exception {
        fixture = new SetupFixture();
    }

    @Test
    public void testSetup() throws ParseException {
        SetupHelper helper = DependencyManager.getOrCreate(SetupHelper.class);

        Parse table = new Parse("<table>"
                + "<tr><td>ignore</td></tr>"
                + "<tr><td>locale</td><td>de_DE</td></tr>"
                + "<tr><td>format</td><td>hh:mm:ss</td></tr>"
                + "</table>");

        fixture.doTable(table);

        assertThat(fixture.counts.exceptions, is(equalTo((Object) 0)));
        assertThat(helper.getFormat(), is(equalTo("hh:mm:ss")));
        assertThat(helper.getLocale(), is(equalTo(Locale.GERMANY)));

        table = new Parse("<table>"
                + "<tr><td>ignore</td></tr>"
                + "<tr><td>locale</td><td>en_US</td></tr>"
                + "<tr><td>format</td><td>MM/dd/yyyy</td></tr>"
                + "</table>");

        fixture.doTable(table);

        assertThat(fixture.counts.exceptions, is(equalTo((Object) 0)));
        assertThat(helper.getFormat(), is(equalTo("MM/dd/yyyy")));
        assertThat(helper.getLocale(), is(equalTo(Locale.US)));
    }
}
