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
package org.openspice.jspice.boxes.post_office;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Agent {

	private Letter letter;
	private LetterBox letterBox;

	public final Letter getLetter() {
		return letter;
	}

	public final void setLetter( Letter letter ) {
		this.letter = letter;
	}

	public final LetterBox getLetterBox() {
		return letterBox;
	}

	public final void setLetterBox( LetterBox letterBox ) {
		this.letterBox = letterBox;
	}

	public void perform() {
		try {
			final String subject = letter.getSubject();
//			System.out.println( "subject = " + subject );
			final Class[] sig = letter.signature();
//			System.out.println( "sig = " + sig );
			final Class c = this.getClass();
//			System.out.println( "dynamic class = " + c );
			final Method m = c.getMethod( subject, sig );
//			System.out.println( "m = " + m );
			final Object[] args = letter.arguments();
			m.invoke( this, args );
		} catch ( NoSuchMethodException e ) {
			throw new RuntimeException( e );
		} catch ( IllegalAccessException e ) {
			throw new RuntimeException( e );
		} catch ( InvocationTargetException e ) {
			throw new RuntimeException( e );
		}
	}

	public Letter newLetter( final String subject ) {
		return this.getLetterBox().newLetter( subject );
	}

	public Letter newLetterTo( final LetterBox dst, final String subject ) {
		return this.getLetterBox().newLetterTo( dst, subject );
	}

	public Letter newReplyTo( final Letter letter, final String subject ) {
		return this.getLetterBox().newLetterTo( letter.getFrom(), subject );
	}

}
