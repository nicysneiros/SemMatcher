/*
 * $Id: Parameters.java 670 2008-03-02 00:06:16Z euzenat $
 *
 * Copyright (C) INRIA Rh�ne-Alpes, 2004, 2008
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 */

package org.semanticweb.owl.align; 

import java.util.Enumeration;

/**
 * A set of parameters to be passed to an alignment or evaluation method.
 * This interface is a minimal one: add, remove, get parameter value, get
 * a list of available parameters as an enumeration.
 * I does not provide any integrity checking (set and unset silently and
 * erase the previous value is there were one).
 *
 * getParameter must return (null) if no corresponding parameter exist.
 * JE: this may be deprecated at some point to use the standard Java
 * java.lang.Properties class.
 *
 * @author J�r�me Euzenat
 * @version $Id: Parameters.java 670 2008-03-02 00:06:16Z euzenat $ 
 */

public interface Parameters {
 
    public void setParameter(String name, Object value);
    public void unsetParameter(String name);
    public Object getParameter(String name);
    public Enumeration<String> getNames();

    public void write();
    //public static Parameters read(String filename);
    //public static Parameters read(Parameters params, String filename);
}
