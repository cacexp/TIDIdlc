/*
* MORFEO Project
* http://www.morfeo-project.org
*
* Component: TIDIdlc
* Programming Language: Java
*
* File: $Source$
* Version: $Revision: 28 $
* Date: $Date: 2005-05-13 13:10:50 +0200 (Fri, 13 May 2005) $
* Last modified by: $Author: aarranz $
*
* (C) Copyright 2004 Telef�nica Investigaci�n y Desarrollo
*     S.A.Unipersonal (Telef�nica I+D)
*
* Info about members and contributors of the MORFEO project
* is available at:
*
*   http://www.morfeo-project.org/TIDIdlc/CREDITS
*
* This program is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation; either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
*
* If you want to use this software an plan to distribute a
* proprietary application in any way, and you are not licensing and
* distributing your source code under GPL, you probably need to
* purchase a commercial license of the product.  More info about
* licensing options is available at:
*
*   http://www.morfeo-project.org/TIDIdlc/Licensing
*/ 

package es.tid.TIDIdlc.xmlsemantics;

import java.util.Hashtable;

/**
 * Keeps value and type of the constants declared the Idl file.
 */
public class IdlConstants
{

    public static IdlConstants getInstance()
    {
        if (st_the_instance == null)
            st_the_instance = new IdlConstants();
        return st_the_instance;
    }
    
    public static void Shutdown()
    {
    	st_the_instance = null;
    }

    public void add(String name, String type, Object value)
    {
        m_constants.put(name, new IdlConstant(value, type));
    }

    public String getType(String name)
        throws SemanticException
    {
        IdlConstant constant = (IdlConstant) m_constants.get(name);
        return constant.m_type;
    }

    public Object getValue(String name)
        throws SemanticException
    {
        IdlConstant constant = (IdlConstant) m_constants.get(name);
        return constant.m_value;
    }

    private IdlConstants()
    {}

    private Hashtable m_constants = new Hashtable();

    private static IdlConstants st_the_instance = null;

}

class IdlConstant
{
    public Object m_value;

    public String m_type;

    public IdlConstant(Object value, String type)
    {
        this.m_value = value;
        this.m_type = type;
    }
}

