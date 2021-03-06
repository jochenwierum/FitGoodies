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

import de.cologneintelligence.fitgoodies.Fixture;
import de.cologneintelligence.fitgoodies.util.DependencyManager;

import java.io.File;


/**
 * Fixture which allows a user to select a file from its arguments.
 * <p>
 * A fixture which extends the {@code AbstractFileReaderFixture} can
 * easily access a selected file via {@link #getFile()}. The selected encoding
 * can be retrieved via {@link #getEncoding()}.
 * <p>
 * There are two ways of selecting a file. Either by providing a full file path,
 * or by using a pattern. In the latter case, the first matching file is selected:
 * <table border="1" summary=""><tr>
 * <td>MyFixture</td><td>file=/path/to/file</td><td>encoding=latin-1</td>
 * </tr><tr><td>...</td></tr></table>
 * <table border="1" summary=""><tr>
 * <td>MyFixture</td><td>dir=/path/to/</td><td>pattern=.*\.txt</td>
 * </tr><tr><td>...</td></tr></table>
 * <p>
 * Either {@code file} or {@code pattern} must be provided.
 * {@code dir} and {@code encoding} are retrieved from the
 * {@link FileFixtureHelper} if they are missing.
 */
public abstract class AbstractFileReaderFixture extends Fixture {
	private FileInformationWrapper wrapper;
	private FileInformation file;
	private String encoding;

	public AbstractFileReaderFixture() {
		this(new SimpleFileInformationWrapper());
	}

	AbstractFileReaderFixture(FileInformationWrapper wrapper) {
		this.wrapper = wrapper;
	}

	/**
	 * Reads the given parameters and initializes the values of
	 * {@link #getEncoding()} and {@link #getFile()}.
	 *
	 * @throws java.lang.Exception General Exception, e.g. the file cannot be opened.
	 */
	@Override
	public void setUp() throws Exception {
		super.setUp();

        FileFixtureHelper helper = DependencyManager.getOrCreate(FileFixtureHelper.class);
        if (encoding == null) {
            encoding = helper.getEncoding();
        }

		String fileName = getArg("file");
		if (fileName == null) {
			File provider = helper.getDirectory();

			String dir = getArg("dir");
			if (dir != null) {
				provider = new File(dir);
			}

			if (provider == null) {
				throw new RuntimeException("No directory selected");
			}

			String pattern = helper.getPattern();
			pattern = getArg("pattern", pattern);

			FileSelector fs = new FileSelector(provider, pattern);
			file = wrapper.wrap(fs.getFirstFile());
		} else {
			file = wrapper.wrap(new File(fileName).getAbsoluteFile());
		}
	}

	/**
	 * Gets the selected file.
	 *
	 * @return the matching file
	 */
	public FileInformation getFile() {
		return file;
	}

	/**
	 * Gets the selected encoding.
	 *
	 * @return the encoding name
	 */
	public String getEncoding() {
		return encoding;
	}
}
