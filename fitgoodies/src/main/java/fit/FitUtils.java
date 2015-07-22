package fit;/*
 * Copyright (c) 2009-2015  Cologne Intelligence GmbH
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.StringTokenizer;

public final class FitUtils {
	private FitUtils() {};

	public static String HTML_GREEN = "#cfffcf";
	public static String HTML_RED = "#ffcfcf";
	public static String HTML_GREY = "#efefef";
	public static String HTML_YELLOW = "#ffffcf";

	public static String label(String string) {
		return " <font size=-1 color=\"#c08080\"><i>" + string + "</i></font>";
	}

	public static String escape(String string) {
		string = string.replaceAll("&", "&amp;");
		string = string.replaceAll("<", "&lt;");
		string = string.replaceAll("  ", " &nbsp;");
		string = string.replaceAll("\r\n", "<br />");
		string = string.replaceAll("\r", "<br />");
		string = string.replaceAll("\n", "<br />");
		return string;
	}

	public static String camel(String name) {
		StringBuilder b = new StringBuilder(name.length());
		StringTokenizer t = new StringTokenizer(name);
		if (!t.hasMoreTokens())
			return name;
		b.append(t.nextToken());
		while (t.hasMoreTokens()) {
			String token = t.nextToken();
			b.append(token.substring(0, 1).toUpperCase());      // replace spaces with camelCase
			b.append(token.substring(1));
		}
		return b.toString();
	}


	public static void right(Parse cell) {
		cell.addToTag(" bgcolor=\"" + HTML_GREEN + "\"");
	}

	public static void wrong(Parse cell) {
		cell.addToTag(" bgcolor=\"" + HTML_RED + "\"");
		cell.body = escape(cell.text());
	}

	public static void wrong(Parse cell, String actual) {
		wrong(cell);
		cell.addToBody(label("expected") + "<hr>" + escape(actual) + label("actual"));
	}

	public static void info(Parse cell, String message) {
		cell.addToBody(info(message));
	}

	protected static String info(String message) {
		return " <font color=\"#808080\">" + escape(message) + "</font>";
	}

	protected static void ignore(Parse cell) {
		cell.addToTag(" bgcolor=\"" + HTML_GREY + "\"");
	}

	protected static void error(Parse cell, String message) {
		cell.body = escape(cell.text());
		cell.addToBody("<hr><pre>" + escape(message) + "</pre>");
		cell.addToTag(" bgcolor=\"" + HTML_YELLOW + "\"");
	}

	protected static void exception(Parse cell, Throwable exception) {
		while (exception.getClass().equals(InvocationTargetException.class)) {
			exception = ((InvocationTargetException) exception).getTargetException();
		}
		final StringWriter buf = new StringWriter();
		exception.printStackTrace(new PrintWriter(buf));
		error(cell, buf.toString());
	}
}