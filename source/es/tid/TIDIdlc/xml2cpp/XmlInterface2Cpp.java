/*
* MORFEO Project
* http://www.morfeo-project.org
*
* Component: TIDIdlc
* Programming Language: Java
*
* File: $Source$
* Version: $Revision: 310 $
* Date: $Date: 2009-06-09 09:37:19 +0200 (Tue, 09 Jun 2009) $
* Last modified by: $Author: avega $
*
* (C) Copyright 2004 Telefnica Investigacin y Desarrollo
*     S.A.Unipersonal (Telefnica I+D)
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

import es.tid.TIDIdlc.CompilerConf;
import es.tid.TIDIdlc.idl2xml.Idl2XmlNames;
import es.tid.TIDIdlc.util.*;
import es.tid.TIDIdlc.xmlsemantics.*;

import java.io.*;

//import java.util.StringTokenizer;

import org.w3c.dom.*;

/**
 * Generates Cpp for interfaces.
 */
class XmlInterface2Cpp extends XmlInterfaceUtils2Cpp
    implements Idl2XmlNames
{

    private boolean m_generate;

    /** Generate Cpp */
    public void generateCpp(Element doc, String sourceDirectory,
                            String headerDirectory, String genPackage,
                            boolean generateCode, boolean expanded, String h_ext, String c_ext)
        throws Exception
    {

    	// Gets the File Manager
    	FileManager fm = FileManager.getInstance();

        m_generate = generateCode;

        String isAbstractS = doc.getAttribute(OMG_abstract);
        boolean isAbstract = (isAbstractS != null)
                             && (isAbstractS.equals(OMG_true));
        String isLocalS = doc.getAttribute(OMG_local);
        boolean isLocal = (isLocalS != null) && (isLocalS.equals(OMG_true));

        if (doc.getAttribute(OMG_fwd).equals(OMG_true))
            return; // it is a forward declaration

        // Get package components
        String headerDir = Xml2Cpp.getDir(genPackage, headerDirectory,
                                           generateCode);
        String sourceDir = Xml2Cpp.getDir(genPackage, sourceDirectory,
                                           generateCode);

        String POAheaderDir = "";
        String POAsourceDir = "";
        if (!isLocal && !isAbstract) {
            if (genPackage != "") { // interfaces en un m?dulo
                POAheaderDir = Xml2Cpp.getDir("POA_" + genPackage,
                                               headerDirectory, generateCode);
                POAsourceDir = Xml2Cpp.getDir("POA_" + genPackage,
                                               sourceDirectory, generateCode);
            } else { // interfaces en ?mbito global
                POAheaderDir = Xml2Cpp.getDir(genPackage, headerDirectory,
                                               generateCode);
                POAsourceDir = Xml2Cpp.getDir(genPackage, sourceDirectory,
                                               generateCode);
            }
        }

        //FileWriter writer;
        //BufferedWriter buf_writer;
        String sourceFileName;
        StringBuffer sourceContents;
        String headerFileName;
        StringBuffer headerContents;

        String name = doc.getAttribute(OMG_name);
        //Xml2Cpp.generateForwardUtilSimbols(genPackage,name); 
        String nameWithPackage = genPackage.equals("") ? name : genPackage
                                                                + "::" + name;
        //String
        // holderClass=TypedefManager.getInstance().getUnrolledHolderType(nameWithPackage);
        String holderClass = XmlType2Cpp.getHolderName(nameWithPackage);
        sourceFileName = name + c_ext;
        headerFileName = name + h_ext;

        if (isAbstract) {
            // Interface generation
            //source
            if (generateCode) {
                Traces.println("XmlInterface2Cpp:->", Traces.DEEP_DEBUG);
                Traces.println("Generating : " + sourceDir + File.separatorChar
                               + sourceFileName + "...", Traces.USER);
            }
            sourceContents = generateCppAbstractInterfaceDef(doc, genPackage);

            //HEADER
            if (generateCode) {
                Traces.println("XmlInterface2Cpp:->", Traces.DEEP_DEBUG);
                Traces.println("Generating : " + headerDir + File.separatorChar
                               + headerFileName + "...", Traces.USER);
            }
            headerContents = generateHppAbstractInterfaceDef(doc, genPackage);
            //throw new SemanticException("Abstract interfaces are not
            // supported yet.", doc);

        } else {
            // Interface generation
            //source
            if (generateCode) {
                Traces.println("XmlInterface2Cpp:->", Traces.DEEP_DEBUG);
                Traces.println("Generating : " + sourceDir + File.separatorChar
                               + sourceFileName + "...", Traces.USER);
            }
            sourceContents = generateCppInterfaceDef(doc, genPackage);

            //header
            if (generateCode) {
                Traces.println("XmlInterface2Cpp:->", Traces.DEEP_DEBUG);
                Traces.println("Generating : " + headerDir + File.separatorChar
                               + headerFileName + "...", Traces.USER);
            }
            headerContents = generateHppInterfaceDef(doc, genPackage);

            // _InterfaceStub generation
            if ((!CompilerConf.getNoStub()) && (!isLocal)) {
                String stubSourceFileName = "_" + name + "Stub" + c_ext;
                //String stubContents = "";

                if (generateCode) {
                    Traces.println("XmlInterface2Cpp:->", Traces.DEEP_DEBUG);
                    Traces.println("Generating : " + sourceDir
                                   + File.separatorChar + stubSourceFileName
                                   + "...", Traces.USER);
                }

            	XmlInterfaceStub2Cpp genStub = new XmlInterfaceStub2Cpp();
                String idl_fn = XmlUtil.getIdlFileName(doc);

                if (generateCode) {

                    StringBuffer stubContents = genStub.generateCpp(doc, genPackage);
                	
                	fm.addFile(stubContents, stubSourceFileName, sourceDir, idl_fn, FileManager.TYPE_MAIN_SOURCE);
                    //writer = new FileWriter(sourceDir + File.separatorChar
                    //                        + stubSourceFileName);
                    //buf_writer = new BufferedWriter(writer);
                    //buf_writer.write(stubContents);
                    //buf_writer.close();
                }

                String stubHeaderFileName = "_" + name + "Stub" + h_ext;

                if (generateCode) {
                    Traces.println("XmlInterface2Cpp:->", Traces.DEEP_DEBUG);
                    Traces.println("Generating : " + headerDir
                                   + File.separatorChar + stubHeaderFileName
                                   + "...", Traces.USER);
                }

                if (generateCode) {
                	StringBuffer stubContents = genStub.generateHpp(doc, genPackage);
                	fm.addFile(stubContents, stubHeaderFileName, headerDir, idl_fn, FileManager.TYPE_MAIN_HEADER);
                	//writer = new FileWriter(headerDir + File.separatorChar
                    //                        + stubHeaderFileName);
                    //buf_writer = new BufferedWriter(writer);
                    //buf_writer.write(stubContents);
                    //buf_writer.close();
                }

            } // End of CompilerConf.getNo_Stub() && !isLocal

            // InterfacePOA generation
            if ((!CompilerConf.getNoSkel()) && (!isLocal)) {
                //source
                String POASourcefileName;
                if (genPackage != "") // DAVV - interfaces dentro de un m?dulo
                    POASourcefileName = doc.getAttribute(OMG_name) + c_ext;
                else
                    // DAVV - interfaces con ?mbito global
                    POASourcefileName = "POA_" + doc.getAttribute(OMG_name)
                                        + c_ext;
                StringBuffer POAContents;
                StringBuffer POATieContents;

                if (generateCode) {
                    Traces.println("XmlInterface2Cpp:->", Traces.DEEP_DEBUG);
                    Traces.println("Generating : " + POAsourceDir
                                   + File.separatorChar + POASourcefileName
                                   + "...", Traces.USER);
                }

                XmlInterfaceSkeleton2Cpp genSkeleton = new XmlInterfaceSkeleton2Cpp();
                POAContents = genSkeleton.generateCpp(doc, genPackage);
                XmlInterfaceTie2Cpp genTie = new XmlInterfaceTie2Cpp();

                if (!CompilerConf.getNoTie()) {// InterfacePOATie generation
                    POATieContents = genTie.generateCpp(doc, genPackage);
                    POAContents.append("\n");
                    POAContents.append(POATieContents);
                }
                String idl_fn = XmlUtil.getIdlFileName(doc);
                
                if (generateCode) {
                	fm.addFile(POAContents, POASourcefileName, POAsourceDir, idl_fn, FileManager.TYPE_POA_SOURCE);
                    //writer = new FileWriter(POAsourceDir + File.separatorChar
                    //                        + POASourcefileName);
                    //buf_writer = new BufferedWriter(writer);
                    //buf_writer.write(POAContents);
                    //buf_writer.close();
                }

                //header
                String POAHeaderfileName;
                if (genPackage != "") // interfaces en un m?dulo
                    POAHeaderfileName = name + h_ext;
                else
                    // interfaces en ?mbito global
                    POAHeaderfileName = "POA_" + name + h_ext;

                if (generateCode) {
                    Traces.println("XmlInterface2Cpp:->", Traces.DEEP_DEBUG);
                    Traces.println("Generating : " + POAheaderDir
                                   + File.separatorChar + POAHeaderfileName
                                   + "...", Traces.USER);
                }

                POAContents = genSkeleton.generateHpp(doc, genPackage);

                if (!CompilerConf.getNoTie()) {// InterfacePOATie generation
                    POATieContents = genTie.generateHpp(doc, genPackage);
                    POAContents.append("\n");
                    POAContents.append(POATieContents);
                }

                if (generateCode) {
                	fm.addFile(POAContents, POAHeaderfileName, POAheaderDir, idl_fn, FileManager.TYPE_POA_HEADER);
                    //writer = new FileWriter(POAheaderDir + File.separatorChar
                    //                        + POAHeaderfileName);
                    //buf_writer = new BufferedWriter(writer);
                    //buf_writer.write(POAContents);
                    //buf_writer.close();
                }

            } // End of CompilerConf.getNo_Skel()

        } // End of isAbstract is false..

        sourceContents.append(generateCppHelperDef(doc, genPackage));
        headerContents.append(XmlCppHelperGenerator.generateHpp(doc, null, genPackage,false));

        if (!isLocal)
            headerContents.append(XmlCppHolderGenerator.generateHpp(OMG_Holder_Ref,genPackage,nameWithPackage,holderClass));

        StringBuffer cont = new StringBuffer();
        XmlHppHeaderGenerator.generateFoot(cont, "interface", name, genPackage);
        headerContents.append(cont);
        String idl_fn = XmlUtil.getIdlFileName(doc);

        if (generateCode) {

        	fm.addFile(sourceContents, sourceFileName, sourceDir, idl_fn, FileManager.TYPE_MAIN_SOURCE);
        	fm.addFile(headerContents, headerFileName, headerDir, idl_fn, FileManager.TYPE_MAIN_HEADER);

            //writer = new FileWriter(sourceDir + File.separatorChar
            //                        + sourceFileName);
            //buf_writer = new BufferedWriter(writer);
            //buf_writer.write(sourceContents);
            //buf_writer.close();
            //writer = new FileWriter(headerDir + File.separatorChar
            //                        + headerFileName);
            //buf_writer = new BufferedWriter(writer);
            //buf_writer.write(headerContents);
            //buf_writer.close();
        }

        // External any operations
        // Design of the header files, Any operations outside main file.
        StringBuffer buffer = new StringBuffer();
        XmlHppExternalOperationsGenerator.generateHpp(doc, buffer,
                                                      OMG_interface, name,
                                                      genPackage);
        //headerContents = buffer.toString();

        if (generateCode) {
            String fileName = name + "_ext" + h_ext;
            fm.addFile(buffer, fileName, headerDir, idl_fn, FileManager.TYPE_MAIN_HEADER_EXT);
            //writer = new FileWriter(headerDir + File.separatorChar + fileName);
            //buf_writer = new BufferedWriter(writer);
            //buf_writer.write(headerContents);
            //buf_writer.close();
        }

        generateCppSubPackageDef(doc, sourceDirectory, headerDirectory, name,
                                 genPackage, expanded, h_ext, c_ext);

    }

    private StringBuffer generateCppAbstractInterfaceDef(Element doc,
                                                   String genPackage)
        throws Exception
    {

        // The same as the previous method, but abstract??
        StringBuffer buffer = new StringBuffer();
        String name = doc.getAttribute(OMG_name);
        String nameWithPackage = genPackage.equals("") ? name : genPackage
                                                                + "::" + name;
        String helperClass = XmlType2Cpp.getHelperName(nameWithPackage);

        // Package header
        XmlCppHeaderGenerator.generate(buffer, "interface", name, genPackage);

        //buffer.append("//The operations\n");
        generateCppExportDef(buffer, doc, name, genPackage);

        buffer.append("//Static Members\n");
        // narrow
        buffer.append(nameWithPackage);
        buffer.append("_ptr ");
        buffer.append(nameWithPackage);
        buffer.append("::_narrow(const ::CORBA::AbstractBase_ptr obj) " + "\n{\n");
        buffer.append("\treturn ");
        // buffer.append(helperClass);
        // buffer.append("::narrow(obj, false);\n");
        buffer.append("0; // Not implemented yet\n");
        buffer.append("}\n\n");
        // unchecked narrow
        buffer.append(nameWithPackage);
        buffer.append("_ptr ");
        buffer.append(nameWithPackage);
        buffer.append("::_unchecked_narrow(const ::CORBA::AbstractBase_ptr obj) "
                      + "\n{\n");
        buffer.append("\treturn ");
        // buffer.append(helperClass);
        // buffer.append("::narrow(obj, true);\n");
        buffer.append("0; // Not implemented yet\n ");
        buffer.append("}\n\n");
        // Duplicate
        buffer.append(nameWithPackage);
        buffer.append("_ptr ");
        buffer.append(nameWithPackage);
        buffer.append("::_duplicate(" + nameWithPackage + "_ptr ref){\n");
        buffer.append("\tCORBA::AbstractBase::_duplicate(ref);\n");
        buffer.append("\treturn ref;\n");           
        buffer.append("}// end of Duplicate.\n\n");

        // _nil
        buffer.append(nameWithPackage);
        buffer.append("_ptr ");
        buffer.append(nameWithPackage);
        buffer.append("::_nil(){\n\t return ");
        buffer.append(nameWithPackage
                      + "::_narrow(CORBA::AbstractBase::_nil());\n}\n\n");

        return buffer;
    }

    /**
     * @deprecated No hay implementacion de los accessors,
     */
 
    private void generateCppConstDecl(StringBuffer buffer, Element doc,
                                      String className)
        throws SemanticException
    {

        NodeList nodes = doc.getChildNodes();
        String scopedName = doc.getAttribute(OMG_scoped_name);

        // inicializacion
        // Value generation
        Element typeEl = (Element) nodes.item(0);
        String type = XmlType2Cpp.getType(typeEl);
        buffer.append("\t\t");
        buffer.append("const ");
        buffer.append(type);
        buffer.append(" ");
        buffer.append(className);
        buffer.append("::");
        buffer.append(doc.getAttribute(OMG_name));
        buffer.append(" = ");
        // Expre generation
        Object expr = IdlConstants.getInstance().getValue(scopedName);
        String typeExpr = IdlConstants.getInstance().getType(scopedName);
        String kind = XmlType2Cpp.getDeepKind(typeEl);
        if (typeExpr =="char*") {
        	buffer.append("\"");
        	buffer.append(XmlExpr2Cpp.toString(expr, typeExpr));
        	buffer.append("\"");
        } else if (typeExpr =="CORBA::Char" /*PRA*/ || kind.equals("char")) {
           	buffer.append("'");
           	buffer.append(XmlExpr2Cpp.toString(expr, typeExpr));
           	buffer.append("'");
        } else {
       		buffer.append(XmlExpr2Cpp.toString(expr, typeExpr));
        }
        
        buffer.append(";\n\n");
    }

    private void generateCppExportDef(StringBuffer buffer, Element doc,
                                      String interfaceName, String genPackage)
        throws Exception
    {

        // Items definition
        NodeList nodes = doc.getChildNodes();
        String className = genPackage + "::" + interfaceName;

        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            String tag = el.getTagName();
            // MACP, There is no implementation of the accesors.
            // Only VIRTUAL ... =0;
            /*
             * if (tag.equals(OMG_attr_dcl)) {// Attribute declaration
             * generateCppAttributeDecl(buffer, el,className); } else
             */
            if (tag.equals(OMG_const_dcl)) // Constant declaration
                generateCppConstDecl(buffer, el, className);
        }

        buffer.append("\n");
    }

    private String generateCppHelperDef(Element doc, String genPackage)
        throws Exception
    {

        StringBuffer buffer = new StringBuffer();

        // Header
        String name = doc.getAttribute(OMG_name);
        String isAbstractS = doc.getAttribute(OMG_abstract);
        boolean isAbstract = (isAbstractS != null)
                             && (isAbstractS.equals(OMG_true));
        String isLocalS = doc.getAttribute(OMG_local);
        boolean isLocal = (isLocalS != null) && (isLocalS.equals(OMG_true));
        String interfaceNameWithPackage = genPackage.equals("") ? name
            : genPackage + "::" + name;
        //String helperClass = TypedefManager.getInstance()
        //				   .getUnrolledHelperType(interfaceNameWithPackage);
        //String holderClass = TypedefManager.getInstance()
        //				   .getUnrolledHolderType(interfaceNameWithPackage);
        // Cualquier typedef del IDL tiene asociado un typedef en C++;
        // por eso en C++ no es necesario desenrollar nada como pasa en Java.
        // TypedefManager no tiene sentido.
        String helperClass = 
            XmlType2Cpp.getHelperName(interfaceNameWithPackage);
        String holderClass = 
            XmlType2Cpp.getHolderName(interfaceNameWithPackage);

        if (!isAbstract) {
            buffer.append(XmlCppHelperGenerator.generateCpp(doc, null,
                                                            genPackage,false));

        } else if (isAbstract) { //is an Abstract Interface
            buffer.append("\t/* Methods write, read, insert, extract and narrow\n");
            buffer.append("\tare not implemented yet for abstract interfaces */\n");
            buffer.append("void ");
            buffer.append(helperClass);
            buffer.append("::read(::TIDorb::portable::InputStream& is, ");
            buffer.append(interfaceNameWithPackage);
            buffer.append("_ptr& val)\n{\n");
            buffer.append("\tval= 0; \n");
            buffer.append("\treturn;\n}\n\n");
            buffer.append("void ");
            buffer.append(helperClass);
            buffer.append("::write(::TIDorb::portable::OutputStream& os,const ");
            buffer.append(interfaceNameWithPackage);
            buffer.append("_ptr val)\n{\n}\n\n");

            buffer.append(interfaceNameWithPackage);
            buffer.append("_ptr ");
            buffer.append(helperClass);
            buffer.append("::narrow(const CORBA::Object_ptr obj, bool is_a) {\n");
            buffer.append("\treturn 0; // Not implemented yet \n");
            buffer.append("}\n\n");

            buffer.append("CORBA::TypeCode_ptr " + helperClass + "::type() {\n");
            XmlCppHelperGenerator.generateTypeImplementation(doc, buffer, name, genPackage);
            buffer.append("}\n\n");

            String tc = genPackage.equals("") ? "_tc_" : genPackage + "::_tc_";
            buffer.append("const ::CORBA::TypeCode_ptr " + tc);
            buffer.append(name);
            buffer.append("=");
            buffer.append(helperClass);
            buffer.append("::type();\n\n");

        }
        if (!isLocal) {
            String contents = XmlCppHolderGenerator.generateCpp(genPackage,
                                                                name,
                                                                holderClass);
            buffer.append(contents);
        }
        return buffer.toString();
    }

  

    private StringBuffer generateCppInterfaceDef(Element doc, String genPackage)
        throws Exception
    {

        StringBuffer buffer = new StringBuffer();

        String isLocalS = doc.getAttribute(OMG_local);
        boolean isLocal = (isLocalS != null) && (isLocalS.equals(OMG_true));

        String name = doc.getAttribute(OMG_name);
        String nameWithPackage = genPackage.equals("") ? name : genPackage
                                                                + "::" + name;
        String stub = genPackage.equals("") ? "_" + name + "Stub" : genPackage
                                                                    + "::_"
                                                                    + name
                                                                    + "Stub";

        String helperClass = XmlType2Cpp.getHelperName(nameWithPackage);

        // Package header
        XmlCppHeaderGenerator.generate(buffer, "interface", name, genPackage);


        //buffer.append("//The operations\n");
        generateCppExportDef(buffer, doc, name, genPackage);

        buffer.append("//Static Members\n");
        // narrow
        buffer.append(nameWithPackage);
        buffer.append("_ptr ");
        buffer.append(nameWithPackage);
        buffer.append("::_narrow(const ::CORBA::Object_ptr obj) "
                      + /* throw (CORBA::SystemException) */"\n{\n");
        buffer.append("\treturn ");
        buffer.append(helperClass);
        buffer.append("::narrow(obj, false);\n");
        buffer.append("}\n\n");
        // unchecked narrow
        buffer.append(nameWithPackage);
        buffer.append("_ptr ");
        buffer.append(nameWithPackage);
        buffer.append("::_unchecked_narrow(const ::CORBA::Object_ptr obj) "
                      + /* throw (CORBA::SystemException) */"\n{\n");
        buffer.append("\treturn ");
        buffer.append(helperClass);
        buffer.append("::narrow(obj, true);\n");
        buffer.append("}\n\n");
        // Duplicate
        buffer.append(nameWithPackage);
        buffer.append("_ptr ");
        buffer.append(nameWithPackage);
        buffer.append("::_duplicate(" + nameWithPackage + "_ptr ref){\n");
        buffer.append("\tCORBA::Object::_duplicate(ref);\n");
        buffer.append("\treturn ref;\n");           
        buffer.append("}// end of Duplicate.\n\n");

        // _nil
        buffer.append(nameWithPackage);
        buffer.append("_ptr ");
        buffer.append(nameWithPackage);
        buffer.append("::_nil(){\n\t return ");
        buffer.append(nameWithPackage
                      + "::_narrow(CORBA::Object::_nil());\n}\n\n");

        return buffer;
    }

    private void generateCppSubPackageDef(Element doc, String sourceDirectory,
                                          String headerDirectory,
                                          String interfaceName,
                                          String genPackage,
										  boolean expanded, 
										  String h_ext, 
										  String c_ext)
        throws Exception
    {

        String newPackage;
        if (!genPackage.equals(""))
            newPackage = genPackage + "::" + interfaceName + "";
        else
            newPackage = interfaceName + "";

        // Items definition
        NodeList nodes = doc.getChildNodes();

        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            String tag = el.getTagName();
 
            if (tag.equals(OMG_enum)) {
                XmlEnum2Cpp gen = new XmlEnum2Cpp();
                gen.generateCpp(el, sourceDirectory, headerDirectory,
                                newPackage, this.m_generate, expanded, h_ext, c_ext);
            } else if (tag.equals(OMG_struct)) {
                XmlStruct2Cpp gen = new XmlStruct2Cpp();
                gen.generateCpp(el, sourceDirectory, headerDirectory,
                                newPackage, this.m_generate, expanded, h_ext, c_ext);
            } else if (tag.equals(OMG_union)) {
                XmlUnion2Cpp gen = new XmlUnion2Cpp();
                gen.generateCpp(el, sourceDirectory, headerDirectory,
                                newPackage, this.m_generate, expanded, h_ext, c_ext);
            } else if (tag.equals(OMG_exception)) {
                XmlException2Cpp gen = new XmlException2Cpp();
                gen.generateCpp(el, sourceDirectory, headerDirectory,
                                newPackage, this.m_generate, expanded, h_ext, c_ext);
            } else if (tag.equals(OMG_typedef)) {
                XmlTypedef2Cpp gen = new XmlTypedef2Cpp();
                gen.generateCpp(el, sourceDirectory, headerDirectory,
                                newPackage, this.m_generate, expanded, h_ext, c_ext);
            } else if (tag.equals(OMG_native)) {
                // aqui habria que poner algo para facilitar al usuario
                // la inserci?n de tipos native
                // en la jerarqu?a de includes
            }
        } // end of loop for.
    }

    private StringBuffer generateHppAbstractInterfaceDef(Element doc,
                                                   String genPackage)
        throws Exception
    {

        StringBuffer buffer = new StringBuffer();
        String name = doc.getAttribute(OMG_name);
        String nameWithPackage = genPackage + "::" + name;
        // Package header
        XmlHppHeaderGenerator.generate(doc, buffer, "interface", name,
                                       genPackage);

        // _tc_ Type Code Generation.
        buffer.append(XmlType2Cpp.getTypeStorageForTypeCode(doc));
        buffer.append("const ::CORBA::TypeCode_ptr _tc_");
        buffer.append(name);
        buffer.append(";\n\n");


        // Class header
        buffer.append("class ");
        buffer.append(name);
        buffer.append("\n   : ");

        String inh = generateHppInheritance(doc, true, true);

        if (inh.length() > 0) {
            buffer.append(inh);
            buffer.append("\n\t\t");
        }

        buffer.append("public virtual CORBA::AbstractBase\n");
        buffer.append("{\n\n");
        buffer.append("\tpublic:\n");
        buffer.append("\t\ttypedef " + name + "_ptr _ptr_type;\n");
        buffer.append("\t\ttypedef " + name + "_var _var_type;\n\n");
        
        buffer.append("\t// Constructors & operators \n");
        buffer.append("\tprotected:\n");
        buffer.append("\t\t" + name + "() {};\n");
        buffer.append("\t\t" + name + "(const " + name + "_ptr obj ) {};\n");
        buffer.append("\t\tvirtual ~" + name + "() {};\n\n");
        buffer.append("\tprivate:\n");
        buffer.append("\t\tvoid operator= (" + name + "_ptr obj) {};\n\n");
        buffer.append("\n\tpublic: // Static members\n");
        buffer.append("\t\tstatic ");
        buffer.append(nameWithPackage);
        buffer.append("_ptr _narrow(const ::CORBA::AbstractBase_ptr obj) " + ";\n");
        buffer.append("\t\tstatic ");
        buffer.append(nameWithPackage);
        buffer.append("_ptr _unchecked_narrow(const ::CORBA::AbstractBase_ptr obj) " + ";\n");
        buffer.append("\t\tstatic ");
        buffer.append(nameWithPackage);
        buffer.append("_ptr _duplicate(" + nameWithPackage + "_ptr val);\n");
        buffer.append("\t\tstatic ");
        buffer.append(nameWithPackage);
        buffer.append("_ptr _nil();\n\n");



        buffer.append("\tpublic: //Operations, Constants & Attributes Declaration \n");
        generateHppExportDef(buffer, doc);
        buffer.append("}; // end of " + name + "header definition\n\n");
        XmlHppHeaderGenerator.generateFoot(buffer, "interface", name,
                                           genPackage);

        return buffer;
    }

    private void generateHppConstDecl(StringBuffer buffer, Element doc)
    {

        NodeList nodes = doc.getChildNodes();

        // Value generation
        Element typeEl = (Element) nodes.item(0);
        String type = XmlType2Cpp.getType(typeEl);
        buffer.append("\t\t");
        buffer.append("static const ");
        buffer.append(type);
        buffer.append(" ");
        buffer.append(doc.getAttribute(OMG_name));
        buffer.append(";\n\n");
    }

    private void generateHppExportDef(StringBuffer buffer, Element doc)
        throws Exception
    {

        // Items definition
        NodeList nodes = doc.getChildNodes();

        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            String tag = el.getTagName();
            if (tag.equals(OMG_op_dcl)) { // operation declaration
                buffer.append("\t\t");
                generateHppMethodHeader(buffer, el, true, true, "\t\t");
                buffer.append(";\n\n");
            } else if (tag.equals(OMG_attr_dcl)) { // Attribute declaration
                generateHppAttributeDecl(buffer, el, true, "\t\t");
            } else if (tag.equals(OMG_const_dcl)) { // Constant declaration
                generateHppConstDecl(buffer, el);
            }
        }
        buffer.append("\n");
    }

    private String generateHppInheritance(Element doc, boolean doAbstracts,
                                          boolean finalComma)
    {

        StringBuffer buffer = new StringBuffer();

        // Items definition
        Element el1 = (Element) doc.getFirstChild();
        if (el1 == null) // nothing to do
            return buffer.toString();

        if (el1.getTagName().equals(OMG_inheritance_spec)) {
            NodeList nodes = el1.getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                buffer.append(" public virtual ");
                String clase = el.getAttribute(OMG_name);
                if (doAbstracts) {
                    if ((Scope.getKind(clase) == Scope.KIND_INTERFACE_FWD_ABS)
                        || (Scope.getKind(clase) == Scope.KIND_INTERFACE_ABS))
                        buffer.append(TypeManager.convert(clase));
                    else {
                        buffer.append(TypeManager.convert(clase));
                        buffer.append("Operations"); 
                    }
                    if ((i != nodes.getLength() - 1) || finalComma)
                        buffer.append(", ");
                } else {
                    if (!((Scope.getKind(clase) == Scope.KIND_INTERFACE_FWD_ABS) || (Scope
                        .getKind(clase) == Scope.KIND_INTERFACE_ABS))) {
                        buffer.append(TypeManager.convert(clase));
                        if ((i != nodes.getLength() - 1) || finalComma)
                            buffer.append(", ");
                    }
                }
            }
        }
        return buffer.toString();
    }

    private StringBuffer generateHppInterfaceDef(Element doc, String genPackage)
        throws Exception
    {

        // This should be the header file of the interface.
        StringBuffer buffer = new StringBuffer();
        String name = doc.getAttribute(OMG_name);
        String nameWithPackage = genPackage + "::" + name;
        // Package header
        XmlHppHeaderGenerator.generate(doc, buffer, "interface", name,
                                       genPackage);

        // _tc_ Type Code Generation.
        buffer.append(XmlType2Cpp.getTypeStorageForTypeCode(doc));
        buffer.append("const ::CORBA::TypeCode_ptr _tc_");
        buffer.append(name);
        buffer.append(";\n\n");

        //buffer.append("typedef
        // ::TIDorb::templates::InterfaceT_ptr_SequenceMember<" + name + "> " +
        // name + "_ptr_SequenceMember;\n\n");

        // Class header
        buffer.append("class ");
        buffer.append(name);
        buffer.append(" :  ");

        String inh = generateHppInheritance(doc, false, false);

        // Is there inheritance? -> It doesn't make sense
        // to inherit from CORBA::Object.
        if (inh.length() > 0) {
            buffer.append("           ");
            buffer.append(inh);
            buffer.append("\n");
        }
        else {
        	buffer.append("public virtual CORBA::Object\n"); // Usual Interface.
        }
        /*
         * -- DAVV - lee final para saber por que String localS =
         * doc.getAttribute(OMG_local); // by macp if ((localS != null) &&
         * (localS.equals(OMG_true)))// Local interface Implementation Corba
         * 3.0. buffer.append("public virtual CORBA::LocalObject\n"); //
         * inheritance from Local not from Object. else DAVV - como bien pone 3
         * lineas antes esto entra por 'Local interface IMPLEMENTATION' - bueno,
         * pues esto no es la clase de implementaci?n, esa la pone el usuario,
         * no el compilador, y es la que DEBE heredar de CORBA::LocalObject (y
         * adem?s de ?sta)
         */
        
        buffer.append("{\n\n");
        buffer.append("\tpublic:\n");
        buffer.append("\t\ttypedef " + name + "_ptr _ptr_type;\n");
        buffer.append("\t\ttypedef " + name + "_var _var_type;\n\n");
        buffer.append("\t// Constructors & operators \n");
        buffer.append("\tprotected:\n");
        buffer.append("\t\t" + name + "() {};\n");
        buffer.append("\t\tvirtual ~" + name + "() {};\n\n");
        buffer.append("\tprivate:\n");
        // buffer.append("\t\t" + name + "(const " + name + "& obj): ::CORBA::Object(obj) {};\n");
        buffer.append("\t\tvoid operator= (" + name + "_ptr obj) {};\n\n");
        // is_a debe retornar siempre excepcion en las locales, y esa
        // implementacion se hereda de LocalObject
        /*
         * if ((localS != null) && (localS.equals(OMG_true))) {
         * buffer.append("\tpublic:\n"); buffer.append("\t\tvirtual
         * CORBA::Boolean _is_a(const char* id);\n\n"); }
         */
        //buffer.append("\t\tvirtual CORBA::Boolean _is_a(const char*
        // id);\n\n");
        XmlHppHeaderGenerator.includeForwardDeclarations(doc, buffer,
                                                         "interface", name,
                                                         genPackage);
        XmlHppHeaderGenerator.includeChildrenHeaderFiles(doc, buffer,
                                                         "interface", name,
                                                         genPackage);
        buffer.append("\n\tpublic: // Static members\n");
        buffer.append("\t\tstatic ");
        buffer.append(nameWithPackage);
        buffer.append("_ptr _narrow(const ::CORBA::Object_ptr obj) "
                      + /* throw (CORBA::SystemException) */";\n");
        buffer.append("\t\tstatic ");
        buffer.append(nameWithPackage);
        buffer.append("_ptr _unchecked_narrow(const ::CORBA::Object_ptr obj) "
                      + /* throw (CORBA::SystemException) */";\n");
        buffer.append("\t\tstatic ");
        buffer.append(nameWithPackage);
        buffer.append("_ptr _duplicate(" + nameWithPackage + "_ptr val);\n");
        buffer.append("\t\tstatic ");
        buffer.append(nameWithPackage);
        buffer.append("_ptr _nil();\n\n");

        buffer.append("\tpublic: //Operations, Constants & Attributes Declaration \n");
        generateHppExportDef(buffer, doc);

        /*
         * if ((localS != null) && (localS.equals(OMG_true))) {// Local
         * interface Implementation Corba 3.0. buffer.append("\t\t//Local
         * Interface Operation Only"); // inheritance from Local not from
         * Object. buffer.append("\n\t\tstatic "+nameWithPackage+"_ptr
         * _create_reference("+nameWithPackage+"& value);\n\n"); }
         */

        buffer.append("}; // end of " + name + "header definition\n\n");

        return buffer;
    }
}
