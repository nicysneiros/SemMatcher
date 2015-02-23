/*
 * $Id: NonTransitiveImplicationRelation.java 631 2008-02-13 14:12:43Z jdavid $
�*
 * Copyright (C) INRIA Rh�ne-Alpes, 2004-2005
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

package fr.inrialpes.exmo.align.impl.rel; 

import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;

import fr.inrialpes.exmo.align.impl.BasicRelation;

/**
 * Represents a non transitive implication relation.
 * as it can be found in C-OWL and other works
 *
 * @author J�r�me Euzenat
 * @version $Id: NonTransitiveImplicationRelation.java 631 2008-02-13 14:12:43Z jdavid $ 
 */

public class NonTransitiveImplicationRelation extends BasicRelation
{
    public void accept( AlignmentVisitor visitor) throws AlignmentException {
        visitor.visit( this );
    }
    /**
     * It is intended that the value of the relation is =, < or >.
     * But this can be any string in other applications.
     */

    /** Creation **/
    public NonTransitiveImplicationRelation(){
	super("~>");
    }
    
    
}


