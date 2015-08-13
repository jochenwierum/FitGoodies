/*
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

package de.cologneintelligence.fitgoodies.typehandler;

import java.text.ParseException;

public class CharTypeHandler extends TypeHandler<Character> {
	public CharTypeHandler(String convertParameter) {
		super(convertParameter);
	}

	@Override
	public Class<Character> getType() {
		return Character.class;
	}

	@Override
	public Character unsafeParse(String input) throws ParseException {
		if (input.length() != 1) {
			throw new ParseException("Expected String of length 1", 0);
		}
		return input.charAt(0);
	}
}
