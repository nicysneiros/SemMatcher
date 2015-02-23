/*
 * $Id: AServException.java 384 2007-02-02 11:09:40Z euzenat $
 *
 * Copyright (C) INRIA Rh�ne-Alpes, 2006
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

package fr.inrialpes.exmo.align.service;

import java.lang.Exception;

/**
 * Base class for all Alignment Service Exceptions.
 *
 *
 * @author J�r�me Euzenat
 * @version $Id: AServException.java 384 2007-02-02 11:09:40Z euzenat $
 */

public class AServException extends Exception {

    static final long serialVersionUID = 300;

    public AServException( String message ){
	super( message );
    }
    
    public AServException( String message, Exception e ){
	super( message, e );
    }
    
}

