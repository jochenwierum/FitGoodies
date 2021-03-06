/*
 * Copyright (c) 2002 Cunningham & Cunningham, Inc.
 * Copyright (c) 2009-2015 by Jochen Wierum & Cologne Intelligence
 *
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


package de.cologneintelligence.fitgoodies.file;

import de.cologneintelligence.fitgoodies.file.readers.FixedLengthRecordReader;
import de.cologneintelligence.fitgoodies.htmlparser.FitCell;
import de.cologneintelligence.fitgoodies.htmlparser.FitRow;
import de.cologneintelligence.fitgoodies.typehandler.BooleanTypeHandler;
import de.cologneintelligence.fitgoodies.typehandler.TypeHandler;

import java.util.List;

/**
 * {@link AbstractFileRecordReaderFixture} implementation which processes
 * files with fixed record length using a
 * {@link de.cologneintelligence.fitgoodies.file.readers.FixedLengthRecordReader}.
 * There is one optional parameter which correspondents to the newLineAtEOR
 * parameter of
 * {@link FixedLengthRecordReader#FixedLengthRecordReader(java.io.BufferedReader, int[], boolean)}.
 * It is called &quot;skipEOL&quot;.
 * <p>
 * The first row must contain the field width of each field:
 * <table border="1" summary="">
 * <tr><td>fitgoodies.file.FixedLengthFileRecordFixture</td>
 * <td>file=/myfile.txt</td></tr>
 * <tr><td>3</td><td>10</td><td>25</td></tr>
 * <tr><td>1</td><td>record 1</td><td>more content</td></tr>
 * <tr><td>2</td><td>record 2</td><td>some text</td></tr>
 * </table>
 */
public class FixedLengthFileRecordFixture extends AbstractFileRecordReaderFixture {
	private boolean noEol;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		noEol = BooleanTypeHandler.parseBool(getArg("skipEOL", "0"));
	}

	@Override
	protected void doRows(List<FitRow> rows) throws Exception {
		int[] width = extractWidth(rows.get(0));

		if (width == null) {
			return;
		}

        setRecordReader(new FixedLengthRecordReader(
                getFile().openBufferedReader(getEncoding()),
                width, noEol));
        super.doRows(rows.subList(1, rows.size()));
    }

	/**
	 * Parses the first table row and generates an {@code int} array
	 * which contains the length of each record field.
	 *
	 * @param row row to process
	 * @return length of each field
	 */
	public final int[] extractWidth(FitRow row) {
		int[] width = new int[row.size()];

        int i = 0;
        for (FitCell cell : row.cells()) {
			try {
                String value = validator.preProcess(cell.getFitValue());
                TypeHandler handler = typeHandlerFactory.getHandler(Integer.class, null);
                width[i++] = (Integer) handler.parse(value);
			} catch (Exception e) {
				cell.exception(e);
				return null;
			}
		}
		return width;
	}
}
