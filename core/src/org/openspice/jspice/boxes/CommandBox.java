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
package org.openspice.jspice.boxes;

import java.io.PrintWriter;

interface CommandBox {
	String getLine();
	PrintWriter printWriter();
	public void flush();
	public void close();
	public boolean checkError();
	public void write(int c);
	public void write(char buf[], int off, int len);
	public void write(char buf[]);
	public void write(String s, int off, int len);
	public void write(String s);
	public void print(boolean b);
	public void print(char c);
	public void print(int i);
	public void print(long l);
	public void print(float f);
	public void print(double d);
	public void print(char s[]);
	public void print(String s);
	public void print(Object obj);
	public void println();
	public void println(boolean x);
	public void println(char x);
	public void println(int x);
	public void println(long x);
	public void println(float x);
	public void println(double x);
	public void println(char x[]);
	public void println(String x);
	public void println(Object x);
}
