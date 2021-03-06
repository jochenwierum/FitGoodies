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

package de.cologneintelligence.fitgoodies.mail;

import de.cologneintelligence.fitgoodies.Fixture;
import de.cologneintelligence.fitgoodies.htmlparser.FitCell;
import de.cologneintelligence.fitgoodies.mail.providers.JavaMailMessageProvider;
import de.cologneintelligence.fitgoodies.mail.providers.MessageProvider;
import de.cologneintelligence.fitgoodies.types.TestableString;
import de.cologneintelligence.fitgoodies.util.DependencyManager;
import de.cologneintelligence.fitgoodies.util.FitUtils;
import de.cologneintelligence.fitgoodies.valuereceivers.ConstantReceiver;

import javax.mail.MessagingException;
import java.util.List;

/**
 * Fixture which checks the content of a mail. Before calling, a connection must
 * be set up using either the {@link SetupFixture} or the {@link SetupHelper}.
 * The fixture opens a connection, fetches the most recent mail, checks it,
 * deletes it by default and closes the connection again.
 * <p>
 * The table must have three columns. The first one contains the header name or
 * &quot;body&quot; to check the plain text and the HTML body or
 * &quot;htmlbody&quot;/&quot;plainbody&quot; to only check one of them.
 * <p>
 * The second column must contain the keywords &quot;contains&quot; or
 * &quot;regex&quot;, which decides how the third column is interpreted. The
 * third column contains the String which is compared with the selected content.
 * Cross References are supported in the third column only.
 * <p>
 * If a text matches, only the matching line is shown. If a regular expression
 * was used, the whole match is shown.
 * <p>
 * To not delete a mail after processing set the fixture parameter
 * &quot;delete&quot; to false.
 * <p>
 * Example:
 * <table border="1" summary="">
 * <tr><td>fitgoodies.mail.MailFixture</td><td>delete=false</td></tr>
 * <tr><td>body</td><td>contains</td><td>dear user</td></tr>
 * <tr><td>subject</td><td>regex</td><td>sp.m</td></tr>
 * </table>
 */
public class MailFixture extends Fixture {
	private static final int PREVIEW_LENGTH = 128;

	private final MessageProvider provider;
	private Mail mail;

	private boolean delete = true;

	/**
	 * Generates a new fixture using the given provider.
	 *
	 * @param provider message provider to use
	 */
	public MailFixture(final MessageProvider provider) {
		this.provider = provider;
	}

	/**
	 * Generates a new fixture using the standard provider (which is JavaMail).
	 */
	public MailFixture() {
		this(new JavaMailMessageProvider(
				DependencyManager.getOrCreate(SetupHelper.class).generateProperties()));
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		provider.connect();
		mail = provider.getLatestMessage();

		if (mail == null) {
			provider.disconnect();
			throw new RuntimeException("No mail found");
		}
	}

	@Override
	public void tearDown() throws Exception {
		if (delete) {
			mail.delete();
		}
		provider.disconnect();
		super.tearDown();
	}

	@Override
	protected void doCells(List<FitCell> cells) {
		String object = cells.get(0).getFitValue().toLowerCase();
		String command = cells.get(1).getFitValue();
        FitCell content = cells.get(2);
		String originalContent = content.getFitValue();

		String[] objects;
		objects = getMailContent(object);

		if (objects != null) {
			TestableString string = new TestableString(objects);
			ConstantReceiver receiver = new ConstantReceiver(string, TestableString.class);
			validator.process(content, receiver, command, typeHandlerFactory);
		}

		content.setDisplayValue(originalContent);
		patchCellResult(content, objects);
	}

	private void patchCellResult(final FitCell cell, final String[] objects) {
		cell.addDisplayValue(FitUtils.label("expected") + "<hr />");

		if (objects == null) {
			makeMoreString(cell, "(unset)", 0);
		} else {
			boolean found = false;
			for (final String o : objects) {
				if (o != null) {
					found = true;
					makeMoreString(cell, o, objects.length);
					break;
				}
			}
			if (!found) {
				makeMoreString(cell, "(unset)", objects.length);
			}
		}
		cell.addDisplayValue(FitUtils.label("actual"));
	}

	private void makeMoreString(final FitCell cell, final String message, final int count) {
		cell.addDisplayValue(preview(message));
		if (count > 1) {
			cell.info(" (+ " + (count - 1) + " more)");
		}
	}

	private String preview(final String text) {
		String result = text;

		if (text.length() > PREVIEW_LENGTH) {
			result = text.substring(0, PREVIEW_LENGTH) + "...";
		}

		return result;
	}

	private String[] getMailContent(final String object) {
		try {
			if ("body".equals(object)) {
				return new String[]{mail.getPlainContent(), mail.getHTMLContent()};
			} else if ("plainbody".equals(object)) {
				return new String[]{mail.getPlainContent()};
			} else if ("htmlbody".equals(object)) {
				return new String[]{mail.getHTMLContent()};
			} else {
				return mail.getHeader(object);
			}
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}
}
