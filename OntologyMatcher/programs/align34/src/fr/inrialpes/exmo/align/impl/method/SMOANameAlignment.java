/*
 * $Id: SMOANameAlignment.java 690 2008-03-31 10:26:08Z euzenat $
 *
 * Copyright (C) INRIA Rh�ne-Alpes, 2003-2008
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 */

package fr.inrialpes.exmo.align.impl.method; 

/**
 * This class aligns ontology with regard to the editing distance between 
 * class names.
 * THIS CLASS IS ONLY HERE FOR COMPATIBILITY PURPOSES
 *
 * @author J�r�me Euzenat
 * @version $Id: SMOANameAlignment.java 690 2008-03-31 10:26:08Z euzenat $ 
 */

public class SMOANameAlignment extends StringDistAlignment {

    /** Creation **/
    public SMOANameAlignment(){
	methodName = "smoaDistance";
    };

}

