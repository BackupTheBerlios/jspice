/**
 *	JSpice, an Open Spice interpreter and library.
 *	Copyright (C) 2003, Stephen F. K. Leach
 *
 * 	This program is free software; you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 * 	the Free Software Foundation; either version 2 of the License, or
 * 	(at your option) any later version.
 *
 * 	This program is distributed in the hope that it will be useful,
 * 	but WITHOUT ANY WARRANTY; without even the implied warranty of
 * 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 * 	along with this program; if not, write to the Free Software
 *	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.openspice.jspice.parse;

public final class Prec {
	private final static int step = 100;
	private final static int level = 10000;

	public final static int zero = 0;
	public final static int one = 1;

	private final static int loose = 1 * level;
	public final static int comma = 2 * level;
	final static int up_to_comma = comma + 1;
	public final static int relop = 3 * level;
	private final static int arith = 4 * level;
	private final static int tight = 5 * level;

	public final static int assign = loose + 1 * step;
	public final static int or_absent = loose + 2 * step;
	public final static int and_absent = loose + 3 * step;
	public final static int or = loose + 4 * step;
	public final static int and = loose + 5 * step;


	public final static int up_to_relop = relop + 1;
	final static int not = relop;
	public final static int lt = relop + 0 * step;
	public final static int at = relop + 2 * step;

	public final static int append = arith + 1 * step;
	public final static int arith1 = arith + 2 * step;
	public final static int arith2 = arith + 3 * step;
	public final static int arith3 = arith + 4 * step;

	public final static int maplet = tight + 1 * step;
	public final static int explode = tight + 2 * step;
	public final static int dot = tight + 3 * step;
	public final static int bracket = tight + 4 * step;
	public final static int paren = tight + 5 * step;
}
