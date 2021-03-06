/*
* MORFEO Project
* http://www.morfeo-project.org
*
* Component: TIDIdlc
* Programming Language: Java
*
* File: $Source$
* Version: $Revision: 109 $
* Date: $Date: 2006-01-31 17:41:17 +0100 (Tue, 31 Jan 2006) $
* Last modified by: $Author: iredondo $
*
* (C) Copyright 2004 Telefónica Investigación y Desarrollo
*     S.A.Unipersonal (Telefónica I+D)
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

/**
 * Idlc Compilador IDL a Java y C++ 
 */
import org.w3c.dom.Element;
import es.tid.TIDIdlc.xml2java.XmlType2Java;

public class XmlJavaSemanticType extends XmlType
{

    public XmlJavaSemanticType()
    {}

    public String getTypedefType(Element doc)
    {
        return XmlType2Java.getTypedefType(doc);
    }

    /*
     * en los contextos en que es usado, no aporta ninguna diferencia con
     * respecto a getTypedefType public String getAbsoluteTypedefType(Element
     * doc) { return XmlType2Java.getAbsoluteTypedefType(doc) ; }
     */
    public String getType(Element doc)
    {
        return XmlType2Java.getType(doc);
    }

    public String getParamType(Element doc, boolean out)
    {
        return XmlType2Java.getParamType(doc, out);
    }

    public String getHelperType(Element doc)
    {
        return XmlType2Java.getHelperType(doc);
    }

    public String getTypecode(Element doc)
        throws Exception
    {
        return XmlType2Java.getTypecode(doc);
    }

    public String getTypeReader(Element doc, String inputStreamName) throws Exception
    {
        return XmlType2Java.getTypeReader(doc, inputStreamName);
    }

    public String getTypeWriter(Element doc, String outputStreamName,
                                String outputData)
    {
        return XmlType2Java.getTypeWriter(doc, outputStreamName, outputData);
    }

    public String basicMapping(String type)
    {
        return XmlType2Java.basicMapping(type);
    }

    public String basicOutMapping(String type)
    {
        return XmlType2Java.basicOutMapping(type);
    }

    public String basicORBTypeMapping(Element el)
    {
        return XmlType2Java.basicORBTypeMapping(el);
    }

    public String basicORBTcKindMapping(Element el)
    { // DAVV - never used
        return XmlType2Java.basicORBTcKindMapping(el);
    }

    /*
     * public String getUnrolledName(String scopedName) { return
     * XmlType2Java.getUnrolledName(scopedName) ; } public String
     * getUnrolledName(Element doc) { return XmlType2Java.getUnrolledName(doc) ; }
     */
    /*
     * no se usa nunca y no aporta nada con respecto a getUnrolledName!!
     * public String getAbsoluteUnrolledName(Element doc) { return
     * XmlType2Java.getAbsoluteUnrolledName(doc) ; }
     */
    /*
     * public String getUnrolledNameWithoutPackage(String
     * scopedName) { return
     * XmlType2Java.getUnrolledNameWithoutPackage(scopedName) ; } public String
     * getUnrolledNameWithoutPackage(Element doc) { return
     * XmlType2Java.getUnrolledNameWithoutPackage(doc) ; }
     */
    public String getDefaultConstructor(String type)
    {
        return "";//XmlType2Java.getDefaultConstructor(type);
    }

    public String getDeepType(Element doc)
    {
        return XmlType2Java.getDeepType(doc);
    }
}