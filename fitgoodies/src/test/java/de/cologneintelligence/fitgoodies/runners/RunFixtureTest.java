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


package de.cologneintelligence.fitgoodies.runners;

import de.cologneintelligence.fitgoodies.file.FileSystemDirectoryHelper;
import de.cologneintelligence.fitgoodies.test.FitGoodiesTestCase;
import de.cologneintelligence.fitgoodies.util.DependencyManager;
import fit.Parse;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.text.ParseException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public final class RunFixtureTest extends FitGoodiesTestCase {

    private Parse table;

    @Before
    public void setup() throws ParseException {
        RunnerHelper helper = DependencyManager.getOrCreate(RunnerHelper.class);
        final Runner runner = mock(Runner.class);

        File f1 = mock(File.class);
        File f2 = mock(File.class);
        File f3 = mock(File.class);
        File f4 = mock(File.class);

        when(runner.run(f1, f2)).thenReturn(mkCounts(1, 2, 3, 0));
        when(runner.run(f3, f4)).thenReturn(mkCounts(5, 0, 7, 0));

        FileSystemDirectoryHelper dirHelper = mock(FileSystemDirectoryHelper.class);
        helper.setFile(mock(File.class));
        helper.setResultFile(mock(File.class));
        helper.setHelper(dirHelper);
        helper.setRunner(runner);

        File inputDir = mock(File.class);
        File outputDir = mock(File.class);

        when(helper.getResultFile().getParentFile()).thenReturn(outputDir);
        when(helper.getFile().getParentFile()).thenReturn(inputDir);

        when(inputDir.getAbsoluteFile()).thenReturn(inputDir);
        when(outputDir.getAbsoluteFile()).thenReturn(outputDir);

        when(dirHelper.subdir(inputDir, "file1.html")).thenReturn(f1);
        when(dirHelper.subdir(outputDir, "file1.html")).thenReturn(f2);
        when(dirHelper.subdir(inputDir, "../tests2/test2.html")).thenReturn(f3);
        when(dirHelper.subdir(outputDir, "../tests2/test2.html")).thenReturn(f4);

        table = new Parse("<table><tr><td>ignored</td></tr>"
                + "<tr><td>file</td><td>file1.html</td></tr>"
                + "<tr><td>file</td><td>../tests2/test2.html</td></tr></table>");
    }

    @Test
    public void testFile() throws ParseException {
        RunFixture fixture = new RunFixture();
        fixture.doTable(table);

        assertCounts(fixture.counts, table, 6, 2, 10, 0);

        assertThat(table.parts.more.parts.more.tag.contains("ffcfcf"), is(true));
        assertThat(table.parts.more.more.parts.more.tag.contains("cfffcf"), is(true));
    }

    @Test
    public void testFileTableProcessing() throws ParseException {
        RunFixture fixture = new RunFixture();
        fixture.doTable(table);

        assertThat(table.parts.more.parts.body, is(equalTo("<a href=\"file1.html\">file1.html</a>")));
        assertThat(table.parts.more.parts.more.body, is(equalTo("1 right, 2 wrong, 3 ignored, 0 exceptions")));
        assertThat(table.parts.more.more.parts.body, is(equalTo("<a href=\"../tests2/test2.html\">../tests2/test2.html</a>")));
        assertThat(table.parts.more.more.parts.more.body, is(equalTo("5 right, 0 wrong, 7 ignored, 0 exceptions")));

        assertThat(table.parts.more.parts.more.tag.contains("ffcfcf"), is(true));
        assertThat(table.parts.more.more.parts.more.tag.contains("cfffcf"), is(true));
    }
}
