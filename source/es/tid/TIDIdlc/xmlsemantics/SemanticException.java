/*
* MORFEO Project
* http://www.morfeo-project.org
*
* Component: TIDIdlc
* Programming Language: Java
*
* File: $Source$
* Version: $Revision: 2 $
* Date: $Date: 2005-04-15 14:20:45 +0200 (Fri, 15 Apr 2005) $
* Last modified by: $Author: rafa $
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

import es.tid.TIDIdlc.idl2xml.*;
import org.w3c.dom.*;

/**
 * Class for semantic exceptions.
 */
public class SemanticException extends Exception
{

    public SemanticException(String msg)
    {
        super(msg);
    }

    public SemanticException(String msg, Element el)
    {
        super(msg);
        locate(el);
    }

    public SemanticException(String msg, int line, int column)
    {
        super(msg);
        m_line = line;
        m_column = column;
    }

    public void locate(Element el)
    {
        String line = el.getAttribute("line");
        String col = el.getAttribute("column");
        if ((line != null) && !line.equals("") && (col != null)
            && !col.equals("")) {
            m_line = Integer.parseInt(line);
            m_column = Integer.parseInt(col);
        }
    }

    public String getMessage()
    {
        String err;
        if (m_line >= 0) {
            err = "Semantic error at line "
                  + Preprocessor.getInstance().locate(m_line) + ", column "
                  + m_column + "\n";
            err += super.getMessage() + "\n";
        } else {
            err = "Semantic error.\n";
            err += super.getMessage() + "\n";
        }
        return err;
    }

    public boolean isLocated()
    {
        return (m_line != -1 && m_column != -1);
    }

    private int m_line = -1;

    private int m_column = -1;
}