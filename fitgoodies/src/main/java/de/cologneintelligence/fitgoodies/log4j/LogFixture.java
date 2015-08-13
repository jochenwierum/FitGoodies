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

import de.cologneintelligence.fitgoodies.Fixture;
import de.cologneintelligence.fitgoodies.Parse;
import de.cologneintelligence.fitgoodies.util.FitUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.spi.AppenderAttachable;

import java.util.Map;

/**
 * Fixture to analyze captured log data. Log messages can be analyzed by searing
 * strings in their messages and exception information.<br>
 * The fixture contains 4 rows: the name of the logger, the name of the appender,
 * a command which can have parameters, and a expression to search. The parameter
 * column supports Cross References.
 * Only captured loggers can be analyzed. To capture loggers, see {@link SetupFixture}.
 * <p/>
 * The root logger is named &quot;rootLogger&quot;.
 * Valid parameters are &quot;Thread&quot; and &quot;MinLevel&quot;.
 * <p/>
 * <p/>
 * Example:
 * <p/>
 * <p/>
 * <table border="1" summary=""><tr><td>fitgoodies.log4j.LogFixture</td></tr>
 * <tr><td>rootLogger</td><td>R</td><td>contains</td><td>successfully initialized</td></tr>
 * <tr><td>rootLogger</td><td>R</td><td>notContains</td><td>critical error</td></tr>
 * <tr>
 * <td>org.example.MyClass</td>
 * <td>stdout</td>
 * <td>containsException</td>
 * <td>IllegalArgumentException</td>
 * </tr>
 * <tr>
 * <td>org.example.MyClass</td>
 * <td>stdout</td>
 * <td>notContainsException</td>
 * <td>not found</td>
 * </tr>
 * <tr>
 * <td>org.example.MyClass</td>
 * <td>stdout</td>
 * <td>contains[Thread = main, MinLevel = Error]</td>
 * <td>timeout</td>
 * </tr>
 * </table>
 */
public class LogFixture extends Fixture {
	private final LoggerProvider loggerProvider;
	private final LogEventAnalyzerFactory logEventAnalyzerFactory;

	private static final int LOGGER_COLUMN = 0;
	private static final int APPENDER_COLUMN = 1;
	private static final int COMMAND_COLUMN = 2;
	private static final int CHECK_EXPRESSION_COLUMN = 3;

	private CaptureAppender appender;
	private Parse cells;

	/**
	 * Initializes a new {@code LogFixture} using a {@link LoggerProvider}
	 * and a {@link LogEventAnalyzerFactory}.
	 *
	 * @see #LogFixture(LoggerProvider, LogEventAnalyzerFactory)
	 * LogFixture(LoggerProvider, CellArgumentParserFactory, LogEventAnalyzerFactory)
	 */
	public LogFixture() {
		this(new LoggerProvider(), new LogEventAnalyzerFactory());
	}

	/**
	 * Initializes a new LogFixture.
	 *
	 * @param logs                      {@code LoggerProvider} to receive loggers.
	 * @param logEventAnalyzerFactory   {@code LogEventAnalyzerFactory}
	 *                                  to analyze log entries
	 */
	public LogFixture(final LoggerProvider logs,
	                  final LogEventAnalyzerFactory logEventAnalyzerFactory) {
		this.loggerProvider = logs;
		this.logEventAnalyzerFactory = logEventAnalyzerFactory;
	}

	/**
	 * Processes the table row {@code cells}.
	 *
	 * @param cells row to parse and process
	 */
	@Override
	protected void doCells(final Parse cells) {
		this.cells = cells;
		this.appender = getAppender();

		if (appender != null) {
			try {
				executeCommand();
			} catch (final IllegalArgumentException e) {
				error(this.cells.at(COMMAND_COLUMN), "Illegal format");
			}
		}
	}

	private void executeCommand() {
		Map<String, String> parameters =
				FitUtils.extractCellParameterMap(cells.at(COMMAND_COLUMN));
		String command = cells.at(COMMAND_COLUMN).text();
		getExpressionCellContent();

		dispatchCommand(command, parameters);
	}

	private String getExpressionCellContent() {
		Parse cell = cells.at(CHECK_EXPRESSION_COLUMN);
		return validator.preProcess(cell);
	}

	private CaptureAppender getAppender() {
		String loggerName = cells.at(LOGGER_COLUMN).text();
		AppenderAttachable logger = getLogger(loggerName);

		if (logger == null) {
			error(cells.at(LOGGER_COLUMN), "Invalid logger");
			return null;
		}

		String appenderName = cells.at(APPENDER_COLUMN).text();
		String captureAppenderName = CaptureAppender.getAppenderNameFor(appenderName);

		final Appender appender = logger.getAppender(captureAppenderName);
		if (appender == null) {
			error(cells.at(APPENDER_COLUMN), "Invalid appender or appender not captured");
			return null;
		}
		return (CaptureAppender) appender;
	}

	private AppenderAttachable getLogger(String loggerName) {
		if ("rootLogger".equalsIgnoreCase(loggerName)) {
			return loggerProvider.getRootLogger();
		} else {
			return loggerProvider.getLogger(loggerName);
		}
	}

	private void dispatchCommand(String command, Map<String, String> parameters) {

		LogEventAnalyzer analyzer = logEventAnalyzerFactory.getLogEventAnalyzerFor(
				counts(), validator, cells.at(CHECK_EXPRESSION_COLUMN), appender.getAllEvents());

		if ("contains".equalsIgnoreCase(command)) {
			analyzer.processContains(parameters);
		} else if ("notContains".equalsIgnoreCase(command)) {
			analyzer.processNotContains(parameters);
		} else if ("containsException".equalsIgnoreCase(command)) {
			analyzer.processContainsException(parameters);
		} else if ("notContainsException".equalsIgnoreCase(command)) {
			analyzer.processNotContainsException(parameters);
		} else {
			error(cells.at(COMMAND_COLUMN), "unknown command");
		}
	}
}
