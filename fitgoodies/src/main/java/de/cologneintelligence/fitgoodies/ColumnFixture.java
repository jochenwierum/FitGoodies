package de.cologneintelligence.fitgoodies;

// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.

import de.cologneintelligence.fitgoodies.valuereceivers.ValueReceiver;

import static de.cologneintelligence.fitgoodies.util.FitUtils.saveGet;

public class ColumnFixture extends Fixture {

	private String[] columnParameters;
	private ValueReceiver[] columnBindings;

	private boolean hasExecuted = false;

	// Traversal ////////////////////////////////


	/**
	 * Replacement of {@code doRows(Parse)} which resolves question marks
	 * in the first row and calls fit.ColumnFixture.doRows(Parse).
	 * <p/>
	 * Question marks represent method calls,
	 * so getValue() and getValue? are equivalent.
	 *
	 * @param rows rows to be processed
	 */

	@Override
	protected void doRows(final Parse rows) {
		columnParameters = extractColumnParameters(rows);
		bind(rows.parts);
		super.doRows(rows.more);
	}

	@Override
	protected void doRow(Parse row) {
		hasExecuted = false;
		try {
			reset();
			super.doRow(row);
			if (!hasExecuted) {
				execute();
			}
		} catch (Exception e) {
			exception(row.leaf(), e);
		}
	}

	/**
	 * Replacement of {@code doCell} which resolves cross-references
	 * before calling the original {@code doCell} method of fit.
	 *
	 * @param cell   the cell to check
	 * @param column the selected column
	 * @see Fixture#doCell(Parse, int) fit.Fixture.doCell(Parse, int)
	 */
	@Override
	protected void doCell(final Parse cell, final int column) {
		ValueReceiver receiver = columnBindings[column];

		String currentCellParameter = saveGet(column, columnParameters);
		if (receiver != null && !cell.text().trim().isEmpty() && receiver.canSet()) {
			setValue(cell, receiver, currentCellParameter);
		} else {
			check(cell, receiver, currentCellParameter);
		}
	}

	private void setValue(Parse cell, ValueReceiver receiver, String currentCellParameter) {
		try {
			String text = validator.preProcess(cell);
			Object object = typeHandlerFactory.getHandler(receiver.getType(), currentCellParameter).parse(text);
			receiver.set(this, object);
		} catch (Exception e) {
			exception(cell, e);
		}
	}

	public void check(Parse cell, ValueReceiver valueReceiver, String currentCellParameter) {
		if (!hasExecuted) {
			try {
				execute();
			} catch (Exception e) {
				exception(cell, e);
			}
			hasExecuted = true;
		}
		super.check(cell, valueReceiver, currentCellParameter);
	}

	public void reset() throws Exception {
		// about to process first cell of row
	}

	public void execute() throws Exception {
		// about to process first method call of row
	}

	// Utility //////////////////////////////////

	protected void bind(Parse heads) {
		columnBindings = new ValueReceiver[heads.size()];
		for (int i = 0; heads != null; i++, heads = heads.more) {
			String name = heads.text();

			try {
				columnBindings[i] = createReceiver(this, name);
			} catch (Exception e) {
				exception(heads, e);
			}
		}
	}
}
