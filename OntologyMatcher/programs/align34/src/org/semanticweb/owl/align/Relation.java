/*
 * $Id: Relation.java 631 2008-02-13 14:12:43Z jdavid $
 *
 * Copyright (C) INRIA Rh�ne-Alpes, 2003-2005, 2007
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

package org.semanticweb.owl.align;

import java.io.PrintWriter;

import org.xml.sax.ContentHandler;

/**
 *
 * @author J�r�me Euzenat
 * @version $Id: Relation.java 631 2008-02-13 14:12:43Z jdavid $
 */


public interface Relation
{
    /** Creation **/
    public void accept( AlignmentVisitor visitor ) throws AlignmentException;

    public Relation inverse();
    public Relation compose(Relation r);

    public boolean equals( Relation r );

    /** Housekeeping **/
    public void dump(ContentHandler h);
    //public void write( PrintStream writer );
    public void write( PrintWriter writer );

}


