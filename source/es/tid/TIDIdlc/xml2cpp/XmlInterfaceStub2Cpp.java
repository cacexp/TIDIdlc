/*
* MORFEO Project
* http://www.morfeo-project.org
*
* Component: TIDIdlc
* Programming Language: Java
*
* File: $Source$
* Version: $Revision: 326 $
* Date: $Date: 2010-01-18 13:12:39 +0100 (Mon, 18 Jan 2010) $
* Last modified by: $Author: avega $
*
* (C) Copyright 2004 Telef?nica Investigaci?n y Desarrollo
*     S.A.Unipersonal (Telef?nica I+D)
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

package es.tid.TIDIdlc.xml2cpp;

import es.tid.TIDIdlc.idl2xml.Idl2XmlNames;
import es.tid.TIDIdlc.xmlsemantics.Scope;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Collections;
import java.util.Collection;
import java.util.Vector;
import java.util.Iterator;

/**
 * Generates Cpp for interface stubs.
 */
class XmlInterfaceStub2Cpp extends XmlInterfaceUtils2Cpp
    implements Idl2XmlNames
{

    //private Hashtable interface_parentsForHeader = new Hashtable();
    private Hashtable m_interface_parents_for_cpp = new Hashtable();

    private Hashtable m_interface_parents = null;
    private int key_cont = 0;

    public StringBuffer generateCpp(Element doc, String genPackage)
        throws Exception
    {
        m_interface_parents = new java.util.Hashtable();
        initInterfaceParents(doc);
        StringBuffer buffer = new StringBuffer();
        String name = doc.getAttribute(OMG_name);
        String stubClassName = "_" + name + "Stub";

        // Class header
        XmlCppHeaderGenerator.generate(buffer, "stub", stubClassName,
                                       genPackage);

        stubClassName = genPackage.equals("") ? stubClassName 
            : genPackage + "::" + stubClassName;
        buffer.append("const CORBA::RepositoryIdSeq_ptr " + stubClassName
                      + "::__init_ids(){\n");
        buffer.append("\tCORBA::RepositoryIdSeq_ptr ids = new  CORBA::RepositoryIdSeq();\n");
        StringBuffer bufferTemp = new StringBuffer();
        int num = generateInterfacesSupported(bufferTemp, doc, 0);
        buffer.append("\tids->length(" + (num + 1) + ");\n");
        buffer.append(bufferTemp.toString());
        buffer.append("\treturn ids;\n");
        buffer.append("}\n\n");

        buffer.append("const CORBA::RepositoryIdSeq_ptr " + stubClassName
                      + "::__ids =" + stubClassName + "::__init_ids();\n\n");

        buffer.append("const CORBA::RepositoryIdSeq_ptr ");
        buffer.append(stubClassName);
        buffer.append("::_ids()\n{\n");
        buffer.append("\treturn __ids;\n");
        buffer.append("}\n\n");

        generateCppStubExportDef(buffer, doc, stubClassName, genPackage);
        Enumeration elements = m_interface_parents.elements();
        while (elements.hasMoreElements())
            generateCppStubExportDef(buffer, (Element) elements.nextElement(),
                                     stubClassName, genPackage);
      
        return buffer;
    }

    private void generateCppStubExportDef(StringBuffer buffer, Element doc,
                                          String className, String genPackage)
        throws Exception
    {
        // Items definition
        NodeList nodes = doc.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            String tag = el.getTagName();
            if (tag.equals(OMG_op_dcl)) {
                generateCppMethodHeader(buffer, el, className);
                generateCppStubMethodBody(buffer, el, genPackage);
                buffer.append("\n\n");
            } else if (tag.equals(OMG_attr_dcl)) {
                generateCppStubAttributeDecl(buffer, el, className);
            }
        }

    }

    private void generateCppStubAttributeDecl(StringBuffer buffer, Element doc,
                                              String className)
        throws Exception
    {
        // Get the type
        NodeList nodes = doc.getChildNodes();
        Element typeEl = (Element) nodes.item(0);
        String type = XmlType2Cpp.getParamType((Element) nodes.item(0), "in");
        String returntype = XmlType2Cpp.getReturnType(typeEl);
        ;
        String readonly = doc.getAttribute(OMG_readonly);

        // Get & set methods
        for (int i = 1; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            String name = el.getAttribute(OMG_name);
            // Accessor
            buffer.append(returntype + " " + className + "::" 
                          + name + "() {\n");
            // Method name
            buffer.append("\tCORBA::Request_var _request = this->_request(\"_get_");
            buffer.append(name);
            buffer.append("\");\n");
            //String helper = XmlType2Cpp.getHelperType(typeEl);
            //if (!readonly.equalsIgnoreCase("true")) { 
	            buffer.append("\t_request->set_return_type(");
	            
	            //String intermediateTk_Type = XmlType2Cpp.getTypecode(typeEl);
	            String intermediateTk_Type = XmlType2Cpp.getTypecodeName(typeEl);
	            String tk_type=null;
	            tk_type = intermediateTk_Type;
	            
/*	            if (intermediateTk_Type.indexOf("Marshalling::")==0||intermediateTk_Type.indexOf("CORBA::")==0) {
	            	// Is a complex type	
	            	
	            	tk_type = intermediateTk_Type.replaceFirst("_", "_tc_");
	            	tk_type = tk_type.replaceFirst("Helper.*", "");
	            	
	            }
	            else {
	            	// Is a simple type
	            	if (!intermediateTk_Type.startsWith("CORBA::")) {
	            		// If starts with CORBA::, there is nothing to do!! 
	            		int leftBracket = intermediateTk_Type.indexOf("(");
	            		tk_type = intermediateTk_Type.substring(leftBracket+1, intermediateTk_Type.length()-1);
	            		tk_type = tk_type.replaceFirst("tk","_tc");
	            	}
	
	            }*/
	            if (tk_type.equals("")) {
	            	buffer.append(intermediateTk_Type);
	            }else {
	            	buffer.append(tk_type);
	            }
	            buffer.append(");\n");
           // }
            // Invocation
            buffer.append("\t_request->invoke();\n");

            // Catch exceptions
            buffer.append("\tCORBA::Exception* _exception = _request->env()->exception();\n");
            buffer.append("\tif(_exception != NULL)\n\t{\n");
            buffer.append("\t\tCORBA::SystemException_ptr __systemException = CORBA::SystemException::_downcast(_exception);\n");
            buffer.append("\t\tif (__systemException != NULL)\n\t\t{\n");
            buffer.append("\t\t\t__systemException->_raise();\n");
            buffer.append("\t\t}\n");
            buffer.append("\t\tthrow ::CORBA::INTERNAL();\n");
            buffer.append("\t}\n");

            // Return the value
            //generate_return_type_definition(buffer,typeEl,"_l_result","_request->return_value()");
            buffer.append("\t" + returntype + " _l_result;\n");
            generateReturnTypeExtraction(buffer, typeEl, "_l_result",
                                         "_request->return_value()", "\t");
            buffer.append("\t\treturn _l_result;\n");
            buffer.append("\t} else\n");
            buffer.append("\t\tthrow ::CORBA::INTERNAL();\n");
            buffer.append("}\n\n");

            // Modifier
            if (readonly == null || !readonly.equals(OMG_true)) {
                buffer.append("void " + className + "::" + name + "(");
                buffer.append(type);
                buffer.append(" pvalue) {\n");
                //buffer.append("throw (::CORBA::SystemException)\n{\n"); //
                //- antes throw()

                // Method name
                buffer.append("\tCORBA::Request_var _request = this->_request(\"_set_");
                buffer.append(name);
                buffer.append("\");\n");

                // Input parameter
                buffer.append("\t::CORBA::Any& __value = _request->add_in_arg();\n");
                buffer.append("\t__value <<="); // 3rd parameter inout== false
                                                // -> in;
                buffer.append(XmlType2Cpp.getAnyInsertion(typeEl, "pvalue",
                                                          false));
                buffer.append(";\n");

                // Invocation
                buffer.append("\t_request->invoke();\n");

                // Catch exceptions
                buffer.append("\tCORBA::Exception* _exception = _request->env()->exception();\n");
                buffer.append("\tif(_exception!=NULL)\n\t{\n");
                buffer.append("\t\tCORBA::SystemException_ptr __systemException = CORBA::SystemException::_downcast(_exception);\n");
                buffer.append("\t\tif (__systemException != NULL)\n\t\t{\n");
                buffer.append("\t\t\t__systemException->_raise();\n");
                buffer.append("\t\t}\n");
                buffer.append("\t\tthrow ::CORBA::INTERNAL();\n");
                buffer.append("\t}\n");
                buffer.append("}\n\n");
            }
        }
    }

    private void generateCppStubMethodBody(StringBuffer buffer, Element doc, String genPackage)
        throws Exception
    {
        buffer.append("{\n");
        buffer.append("    ");

        // Method name
        String nombre = doc.getAttribute(OMG_name);
        buffer.append("\tCORBA::Request_var _request = this->_request(\"");
        buffer.append(nombre);
        buffer.append("\");\n");

        // Return type
        NodeList nodes = doc.getChildNodes();
        Element returnType = (Element) nodes.item(0);
        NodeList returnTypeL = returnType.getChildNodes();
        if (returnTypeL.getLength() > 0) {
            buffer.append("\t_request->set_return_type(");
            Element ret = (Element) returnTypeL.item(0);
            buffer.append(XmlType2Cpp.getTypecodeName(ret));
            buffer.append(");\n");
        }

        // Parameters
        for (int i = 1; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            if (el.getTagName().equals(OMG_parameter)) {
                Element paramType = (Element) el.getChildNodes().item(0);
                String paramName = el.getAttribute(OMG_name);
                //boolean in = el.getAttribute(OMG_kind).equals("in");
                boolean inout = el.getAttribute(OMG_kind).equals("inout");
                boolean out = el.getAttribute(OMG_kind).equals("out");
                if (out) {
                    buffer.append("\tCORBA::Any& __my_");
                    buffer.append(paramName);
                    buffer.append(" = _request->add_out_arg();\n");
                    buffer.append("\t__my_");
                    buffer.append(paramName);
                    //buffer.append("->type("); DAVV - si la variable es
                    // CORBA::Any&, el accesor '->' no sirve
                    // Fix bug [#394] Change all internal calls of TIDorb::portable::Any from type(tc) 
                    // to set_type(tc) 
                    //buffer.append(".type(");
                    buffer.append(".delegate().set_type(");
                    buffer.append(XmlType2Cpp.getTypecodeName(paramType));
                    buffer.append(");\n");
                } else {
                    buffer.append("\t::CORBA::Any& __my_");
                    buffer.append(paramName);
                    if (inout)
                        buffer.append(" = _request->add_inout_arg();\n");
                    else
                        buffer.append(" = _request->add_in_arg();\n");
                    /*    
                    buffer.append("\t__");
                    buffer.append(paramName);// The Any.
                    buffer.append(" <<= ");
                    // to implement the sign to insert inside an Any.
                    // if (inout==false) then in. else inout.
                    buffer.append(XmlType2Cpp.getAnyInsertion(paramType,
                                                              paramName, inout));
                    buffer.append(";\n");
					*/
					//jagd  
                    /*
                    String tagg = XmlType2Cpp.getDefinitionType(paramType);
                    if (tagg.equals(OMG_sequence)) // avg07 ||tagg.equals(OMG_struct)) 
                    {
 
                      buffer.append("\t");
                      //buffer.append(XmlType2Cpp.getHelperName(XmlType2Cpp.getTypeName(paramType))+"::insert(__");
                      buffer.append(XmlType2Cpp.getHelperName(XmlType2Cpp.getTypeName(paramType))+"::insert(__");
                      buffer.append(paramName);// The Any.
                      buffer.append(",("+XmlType2Cpp.getTypeName(paramType)+"*)&");
                      buffer.append(XmlType2Cpp.getAnyInsertion(paramType,
                                                              paramName, inout));
                      buffer.append(",false);\n"); 
                    }
                    else
                    {
*/
                      buffer.append("\t__my_");
                      buffer.append(paramName);// The Any.
                      buffer.append(" <<= ");
                      // to implement the sign to insert inside an Any.
                      // if (inout==false) then in. else inout.
                      buffer.append(XmlType2Cpp.getAnyInsertion(paramType,
                                                              paramName, inout));
                      buffer.append(";\n");
                    //}
                    //bug 65
                    if (inout && XmlType2Cpp.getDefinitionType(paramType).equals(OMG_interface))
                    		buffer.append("\t::CORBA::release("+paramName+");\n");
                    // FIX bug #270
                    if (inout && XmlType2Cpp.getDefinitionType(paramType).equals(OMG_valuetype))
                		buffer.append("\t" + paramName + "->_remove_ref();\n");
                }
            }
        }

        // Exceptions
        for (int i = 1; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            if (el.getTagName().equals(OMG_raises)) {
                NodeList exceps = el.getChildNodes();
                for (int j = 0; j < exceps.getLength(); j++) {                    
                	Element ex = (Element) exceps.item(j);
                    buffer.append("\t_request->exceptions()->add(");
                    buffer.append(XmlType2Cpp.getTypecodeName(ex));
                    buffer.append(");\n");
                }
                break;
            }
        }

        String oneway = doc.getAttribute(OMG_oneway);
        if (oneway.equals(OMG_true)) {
            // Oneway invocation
            buffer.append("\t_request->send_oneway();\n");
        } else {
            // Invocation
            buffer.append("\t_request->invoke();\n");
            // Catch exceptions
            buffer.append("\tCORBA::Exception* _exception = _request->env()->exception();\n");
            buffer.append("\tif (_exception != NULL)\n\t{\n");
            for (int i = 1; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                if (el.getTagName().equals(OMG_raises)) {
                    NodeList exceps = el.getChildNodes();
                    buffer.append("\t\tCORBA::UnknownUserException * _userException = CORBA::UnknownUserException::_downcast(_exception);\n");
                    buffer.append("\t\tif ( _userException !=NULL)\n\t\t{\n");
                    for (int j = 0; j < exceps.getLength(); j++) {
                        Element ex = (Element) exceps.item(j);
                        buffer.append("\t\t\tif (_userException->exception().type()->equal(");
                        buffer.append(XmlType2Cpp.getTypecodeName(ex));                        
                        buffer.append("))\n\t\t\t{\n"); // es un
                                                                // metodo
                                                                // estatico
                        //buffer.append("\t\t\t\tthrow "); 
                        //buffer.append(XmlType2Cpp.getHelperType(ex));
                        //buffer.append("->extract(_userException->except);\n");
                        buffer.append("\t\t\tconst " + XmlType2Cpp.getType(ex)
                                      + "* _exc_tmp;\n"); // DAVV
                        buffer.append("\t\t\t" + XmlType2Cpp.getHelperType(ex)); // DAVV
                        buffer.append("::extract(_userException->exception(), _exc_tmp);\n"); 
                        // extract es estatico
                        buffer.append("\t\t\t\tthrow *_exc_tmp;\n"); // DAVV
                        buffer.append("\t\t\t}\n");
                    }
                    buffer.append("\t\t\t\tthrow ::CORBA::UNKNOWN();\n\t\t}\n");
                    break;
                }
            }
            buffer.append("\t\tCORBA::SystemException_ptr __systemException = CORBA::SystemException::_downcast(_exception);\n");
            buffer.append("\t\tif (__systemException != NULL)\n\t\t{\n");
            buffer.append("\t\t\t__systemException->_raise();\n");
            buffer.append("\t\t}\n");
            buffer.append("\t\tthrow ::CORBA::INTERNAL();\n");
            buffer.append("\t}\n");

            // Return out parameters
            for (int i = 1; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                if (el.getTagName().equals(OMG_parameter)) {
                    Element paramType = (Element) el.getChildNodes().item(0);
                    String paramName = el.getAttribute(OMG_name);
                    boolean in = el.getAttribute(OMG_kind).equals("in");
                    boolean inout = el.getAttribute(OMG_kind).equals("inout");
                    if (!in) {
                        generateInoutArgumentExtraction(buffer, paramType,
                                                        paramName, 
                                                        "__my_" + paramName,
                                                        "\t", inout, false);
                        // FIX bug #270
                        if (XmlType2Cpp.getDefinitionType(paramType).equals(OMG_valuetype))
                        	buffer.append("\t" + "CORBA::add_ref(" + paramName + ");\n"); // NEW
                    }                    
                }
            }

            // Get the result
            if (returnTypeL.getLength() > 0) {
                Element ret = (Element) returnTypeL.item(0);
                //generate_return_type_definition(buffer,ret,"_l_result","_request->return_value()");
                buffer.append("\t" + XmlType2Cpp.getReturnType(ret)
                              + " _l_result;\n");
                generateReturnTypeExtraction(buffer, ret, "_l_result",
                                             "_request->return_value()", "\t");
                // Return the result
                buffer.append("\t\treturn _l_result;\n");
                buffer.append("\t} else\n");
                buffer.append("\t\tthrow ::CORBA::INTERNAL();\n");
            }
        }
        buffer.append("}");
    }


    public StringBuffer generateHpp(Element doc, String genPackage)
        throws Exception
    {
        m_interface_parents = new java.util.Hashtable();
        initInterfaceParents(doc);
        StringBuffer buffer = new StringBuffer();
        // Header
        String name = doc.getAttribute(OMG_name);
        XmlHppHeaderGenerator.generate(doc, buffer, "stub",
                                       "_" + name + "Stub", genPackage);
        // Class header
        buffer.append("class _");
        buffer.append(name);
        buffer.append("Stub:\n");
        // Como el padre ya es ::TIDorb::portable::Object no hace falta.
        buffer.append("\tpublic virtual ::TIDorb::portable::Stub,\n");
        buffer.append("\tpublic virtual ");
        buffer.append(genPackage + "::" + name);
        buffer.append("\n{\n\n");

        buffer.append("\tpublic:\n");
        buffer.append("\t\tvirtual const CORBA::RepositoryIdSeq_ptr _ids();\n\n");

        buffer.append("\tprivate:\n");
        buffer.append("\t\tstatic const CORBA::RepositoryIdSeq_ptr __ids;\n");
        buffer.append("\t\tstatic const CORBA::RepositoryIdSeq_ptr __init_ids();\n\n");

        buffer.append("\tpublic:\n ");
        generateHppStubExportDef(buffer, doc);
      	Enumeration elements= m_interface_parents.elements();     
      	while (elements.hasMoreElements())
            generateHppStubExportDef(buffer, (Element) elements.nextElement());

        buffer.append("\n}; //End of _StubClass\n");
        XmlHppHeaderGenerator.generateFoot(buffer, "stub", "_" + name + "Stub",
                                           genPackage);
        return buffer;

    }

    private void generateHppStubExportDef(StringBuffer buffer, Element doc)
        throws Exception
    {
        // Items definition
        NodeList nodes = doc.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            String tag = el.getTagName();
            if (tag.equals(OMG_op_dcl)) {
                generateHppMethodHeader(buffer, el, false, false, "\t\t"); // No
                                                                    // virtual
                                                                    // Methods.
                buffer.append(";\n\n");
            } else if (tag.equals(OMG_attr_dcl)) {
                generateHppAttributeDecl(buffer, el, false, "\t\t");
            }
        }

    }

    private void initInterfaceParents(Element doc)
    {
        NodeList nodes = doc.getChildNodes();
        Element el1 = (Element) doc.getFirstChild();
        if (el1 != null) {
            if (el1.getTagName().equals(OMG_inheritance_spec)) {
                nodes = el1.getChildNodes();
                for (int i = 0; i < nodes.getLength(); i++) {
                    Element el = (Element) nodes.item(i);
                    String clase = el.getAttribute(OMG_name);
                    Scope scope = Scope.getGlobalScopeInterface(clase);
                    Element inhElement = scope.getElement();
                    // Generate the operation for all the interface parents
                    //if (!interface_parentsForCpp.containsKey(inhElement))
                    if (!m_interface_parents.contains(inhElement)) {
                        // This is to avoid the duplication of the operation
                        // when there's multiple
                        // inheritance and one of the father inherits from the
                        // other
                        //interface_parentsForCpp.put(inhElement,"void");
                        m_interface_parents.put(new java.lang.Integer(key_cont++), inhElement);
                        initInterfaceParents(inhElement);
                    }
                }
            }
        }
    }
}
