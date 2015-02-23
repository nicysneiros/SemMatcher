/*
 * $Id: AlignmentMetadata.java 535 2007-08-22 21:43:48Z euzenat $
 *
 * Copyright (C) INRIA Rh�ne-Alpes, 2006-2007
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package fr.inrialpes.exmo.align.service;

import org.semanticweb.owl.align.Parameters;

/**
 * Contains the messages that should be sent according to the protocol
 */

public class AlignmentMetadata extends Success {

    public AlignmentMetadata ( int surr, Message rep, String from, String to, String cont, Parameters param ) {
	super( surr, rep, from, to, cont, param );
    }

    public String HTMLString() {
	return "Metadata not implemented";
    }
    public String SOAPString(){
	return "<id>"+surrogate+"</id>"+"<in-reply-to>"+inReplyTo+"</in-reply-to><metadata>"+content+"</metadata>";	
    }
}
