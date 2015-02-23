/*
 * $Id: SubsDistNameAlignment.java 690 2008-03-31 10:26:08Z euzenat $
 *
 * Copyright (C) INRIA Rh�ne-Alpes, 2003-2005, 2007-2008
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
 * This class implements alignment based on substring distance
 * of class and property labels
 * THIS CLASS IS ONLY HERE FOR COMPATIBILITY PURPOSES
 *
 * @author J�r�me Euzenat
 * @version $Id: SubsDistNameAlignment.java 690 2008-03-31 10:26:08Z euzenat $ 
 */

public class SubsDistNameAlignment extends StringDistAlignment {

    /** Creation **/
    public SubsDistNameAlignment(){
	methodName = "subStringDistance";
    };

}
