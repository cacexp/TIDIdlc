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

package es.tid.TIDIdlc.xml2java;

import es.tid.TIDIdlc.idl2xml.Idl2XmlNames;
import es.tid.TIDIdlc.xmlsemantics.*;

import java.io.*;
import java.util.StringTokenizer;
import java.util.Hashtable;

import org.w3c.dom.*;

/**
 * Generates Java for interface ties.
 */
class XmlInterfaceTie2Java extends XmlInterfaceUtils2Java
    implements Idl2XmlNames
{

    private Hashtable m_interface_parents = new Hashtable();

    public String generateJava(Element doc, String genPackage)
        throws Exception
    {
        StringBuffer buffer = new StringBuffer();
        // Header
        String name = doc.getAttribute(OMG_name);
        String delegateName = name + "Operations";

        XmlJavaHeaderGenerator.generate(buffer, "tie", name + "POATie", genPackage);

        // Class header
        buffer.append("public class ");
        buffer.append(name);
        buffer.append("POATie\n");
        buffer.append(" extends ");
        buffer.append(name);
        buffer.append("POA\n");
        buffer.append(" implements ");
        buffer.append(delegateName);
        buffer.append(" {\n\n");
        buffer.append("  private " + delegateName + " _delegate;\n");
        buffer.append("  public ");
        buffer.append(name);
        buffer.append("POATie(");
        buffer.append(delegateName);
        buffer.append(" delegate) {\n");
        buffer.append("    this._delegate = delegate;\n");
        buffer.append("  };\n\n");

        buffer.append("  public ");
        buffer.append(delegateName);
        buffer.append(" _delegate() {\n");
        buffer.append("    return this._delegate;\n");
        buffer.append("  };\n\n");
        buffer.append("  public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] objectID) {\n");
        buffer.append("    return __ids;\n");
        buffer.append("  };\n\n");
        buffer.append("  private static java.lang.String[] __ids = {\n");
        generateInterfacesSupported(buffer, doc);
        buffer.append("  };\n\n");
        generateJavaTieExportDef(buffer, doc);
        buffer.append("}\n");
        return buffer.toString();
    }

    private void generateJavaTieExportDef(StringBuffer buffer, Element doc)
        throws Exception
    {
        // Items definition
        NodeList nodes = doc.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            String tag = el.getTagName();
            if (tag.equals(OMG_op_dcl)) {
                buffer.append("  public ");
                generateJavaMethodHeader(buffer, el);
                generateJavaTieMethodBody(buffer, el);
                buffer.append(";\n\n");
            } else if (tag.equals(OMG_attr_dcl)) {
                generateJavaTieAttributeDecl(buffer, el);
            }
        }

        // Items definition
        Element el1 = (Element) doc.getFirstChild();
        if (el1 != null) {
            if (el1.getTagName().equals(OMG_inheritance_spec)) {
                nodes = el1.getChildNodes();
                for (int i = 0; i < nodes.getLength(); i++) {
                    Element el = (Element) nodes.item(i);
                    String clase = el.getAttribute(OMG_name);
                    Scope scope = Scope.getGlobalScopeInterface(clase);
                    Element inhElement = scope.getElement();
                    if (!m_interface_parents.containsKey(inhElement)) {
                        m_interface_parents.put(inhElement, "void");
                        generateJavaTieExportDef(buffer, inhElement);
                    }
                }
            }
        }
        buffer.append("\n");
    }

    private void generateJavaTieMethodBody(StringBuffer buffer, Element doc)
    {
        buffer.append(" {\n");

        NodeList nodes = doc.getChildNodes();
        String nombre = doc.getAttribute(OMG_name);

        buffer.append("    ");

        // Return type
        Element returnType = (Element) nodes.item(0);
        NodeList returnTypeL = returnType.getChildNodes();
        if (returnTypeL.getLength() > 0) {
            buffer.append("return ");
        }

        // Method name
        buffer.append("this._delegate.");
        buffer.append(nombre);
        buffer.append("(\n");

        // Parameters
        boolean firstParam = true;
        buffer.append("    ");
        for (int i = 1; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            if (el.getTagName().equals(OMG_parameter)) {
                Element paramType = (Element) el.getChildNodes().item(0);
                String paramName = el.getAttribute(OMG_name);
                if (!firstParam)
                    buffer.append(", \n    ");
                buffer.append(paramName);
                firstParam = false;
            }
        }
        if (!firstParam)
            buffer.append("\n    ");

        buffer.append(");\n");

        buffer.append("  }");
    }

    private void generateJavaTieAttributeDecl(StringBuffer buffer, Element doc)
    {
        // Get the type
        NodeList nodes = doc.getChildNodes();
        Element typeEl = (Element) nodes.item(0);
        String type = XmlType2Java.getType(typeEl);
        String readonly = doc.getAttribute(OMG_readonly);

        // Get & set methods
        for (int i = 1; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            String name = el.getAttribute(OMG_name);

            //
            // Accessor
            //
            buffer.append("  public " + type + " " + name + "() {\n");

            // Method name
            buffer.append("    return this._delegate.");
            buffer.append(name);
            buffer.append("();\n");

            buffer.append("  }\n\n");

            //
            // Modifier
            //
            if (readonly == null || !readonly.equals(OMG_true)) {
                buffer.append("  public void " + name + "(" + type + " value) {\n");

                // Method name
                buffer.append("    this._delegate.");
                buffer.append(name);
                buffer.append("(value);\n");

                buffer.append("  }\n\n");
            }

        }
    }

    public String generateLocalTie(Element doc, String genPackage)
        throws Exception
    {
        StringBuffer buffer = new StringBuffer();
        // Header
        String name = doc.getAttribute(OMG_name);
        String delegateName = name + "Operations";

        XmlJavaHeaderGenerator.generate(buffer, "tie", name + "LocalTie",
                                        genPackage);

        // Class header
        buffer.append("public class ");
        buffer.append(name);
        buffer.append("LocalTie\n");
        buffer.append(" extends ");
        buffer.append(name);
        buffer.append("LocalBase\n");
        buffer.append(" {\n\n");
        buffer.append("  private " + delegateName + " _delegate;\n");
        buffer.append("  public ");
        buffer.append(name);
        buffer.append("LocalTie(");
        buffer.append(delegateName);
        buffer.append(" delegate) {\n");
        buffer.append("    this._delegate = delegate;\n");
        buffer.append("  };\n\n");

        buffer.append("  public ");
        buffer.append(delegateName);
        buffer.append(" _delegate() {\n");
        buffer.append("    return this._delegate;\n");
        buffer.append("  };\n\n");
        generateJavaTieExportDef(buffer, doc);
        buffer.append("}\n");
        return buffer.toString();
    }

}