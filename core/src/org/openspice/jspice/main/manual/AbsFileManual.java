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
package org.openspice.jspice.main.manual;

import org.openspice.jspice.conf.JSpiceConf;

public abstract class AbsFileManual implements Manual {

	private final JSpiceConf jspice_conf;
	private final String manual_name;
	private final String manual_folder_name;
	private final String default_topic;

	protected AbsFileManual( final JSpiceConf jspice_conf, final String manual_name, final String manual_folder_name, final String default_topic ) {
		this.jspice_conf = jspice_conf;
		this.manual_name = manual_name;
		this.manual_folder_name = manual_folder_name;
		this.default_topic = default_topic;
	}

	protected AbsFileManual( final JSpiceConf jspice_conf, final String manual_name, final String default_topic ) {
		this( jspice_conf, manual_name, manual_name, default_topic );
	}

	public String getManualName() {
		return this.manual_name;
	}

	public String getManualFolderName() {
		return this.manual_folder_name;
	}

	public String getDefaultTopic() {
		return this.default_topic;
	}

	public JSpiceConf getJSpiceConf() {
		return jspice_conf;
	}

}
