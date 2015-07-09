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


package de.cologneintelligence.fitgoodies.test;

import de.cologneintelligence.fitgoodies.util.DependencyManager;
import fit.Counts;
import fit.Parse;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;

import static de.cologneintelligence.fitgoodies.test.FieldMatcher.hasField;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public abstract class FitGoodiesTestCase {
    protected static Counts mkCounts(final int r, final int w, final int i,
            final int e) {
        final Counts c = new Counts();
        c.right = r; c.wrong = w; c.ignores = i; c.exceptions = e;
        return c;
    }

    @Before
    public void cleanupDependencyManager() throws Exception {
        DependencyManager.clear();
    }

    @After
    public void cleanupDBDriverMock() throws Exception {
        de.cologneintelligence.fitgoodies.database.DriverMock.cleanup();
    }

    public File mockDirectory(String pattern, String... files) {
        DirectoryMockHelper helper = new DirectoryMockHelper();
        for (String file : files) {
            helper.addFile(file);
        }

        return helper.finishMock(pattern);
    }

    protected static void assertCounts(final Counts counts, final Parse table, final int right, final int wrong, final int ignores, final int exceptions) {
        assertThat("Wrong counts! First exception: " + findException(table), counts, allOf(
                hasField("right", is(equalTo(right))),
                hasField("wrong", is(equalTo(wrong))),
                hasField("ignores", is(equalTo(ignores))),
                hasField("exceptions", is(equalTo(exceptions)))
        ));
    }

    private static String findException(Parse table) {
        Parse row = table.parts;
        while (row != null) {
            Parse cell = row.parts;
            while (cell != null) {

                if (cell.tag.contains("bgcolor=\"" + fit.Fixture.yellow + "\"")) {
                    final String body = cell.body;
                    final String trace = body.replaceFirst("^.*<pre>", "").replaceFirst("</pre>.*$", "");
                    return Parse.unescape(trace);
                }

                cell = cell.more;
            }
            row = row.more;
        }
        return "none";
    }

    protected File getMockedFile(File dir, String... name) {
        File tmp = dir;
        for (String aName : name) {
            tmp = find(tmp, aName);
        }
        return tmp;
    }

    private File find(File dir, String s) {
        final File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().equals(s)) {
                    return file;
                }
            }
        }
        throw new IllegalArgumentException("not found: " + s);
    }
}