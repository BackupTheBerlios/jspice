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
package snippets;

import org.openspice.graphics2d.ImageFromFile;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

public class DisplayMessage {


	public static final void main( String[] args ) throws IOException {
		JLabel msgLabel = new JLabel();
		JButton yesButton = new JButton();
		JButton noButton = new JButton();

		msgLabel.setText( "Do you really want to quit?" );
		msgLabel.setBorder( new EmptyBorder( 10, 10, 10, 10 ) );
		yesButton.setText( "Yes" );
		noButton.setText( "No" );

		JFrame win = new JFrame( "Message" );
		JPanel buttonBox = new JPanel();

		win.getContentPane().setLayout(  new BorderLayout() );
		buttonBox.setLayout( new FlowLayout() );

		final Image image = ImageIO.read( new File( "example3.png" ) );


		JPanel xxx = (
			new JPanel() {
				public void paintComponent( Graphics graphics ) {
					graphics.drawImage( image, 0, 0, null );
				}
			}
		);
		xxx.setPreferredSize( new Dimension( 200, 100 ) );

		buttonBox.add( yesButton );
		buttonBox.add( noButton );
		buttonBox.add( xxx );

		win.getContentPane().add( buttonBox, "South" );

		yesButton.addActionListener(
			new ActionListener() {
				public void actionPerformed( ActionEvent e ) { System.exit( 0 ); }
			}
		);

		noButton.addActionListener(
			new ActionListener() {
				public void actionPerformed( ActionEvent e ) { System.exit( 1 ); }
			}
		);



		win.pack();
		win.show();


	}
}
