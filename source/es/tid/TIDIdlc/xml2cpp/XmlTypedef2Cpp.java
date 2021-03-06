/*
* MORFEO Project
* http://www.morfeo-project.org
*
* Component: TIDIdlc
* Programming Language: Java
*
* File: $Source: /cvsroot/tididlc/TIDIdlc/source/es/tid/TIDIdlc/xml2cpp/XmlTypedef2Cpp.java,v $
* Version: $Revision: 328 $
* Date: $Date: 2010-08-03 14:16:36 +0200 (Tue, 03 Aug 2010) $
* Last modified by: $Author: avega $
*
* (C) Copyright 2004 Telef???nica Investigaci???n y Desarrollo
*     S.A.Unipersonal (Telef???nica I+D)
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
import es.tid.TIDIdlc.util.FileManager;
import es.tid.TIDIdlc.util.Traces;
import es.tid.TIDIdlc.util.XmlUtil;
import es.tid.TIDIdlc.xmlsemantics.TypedefManager;
import es.tid.TIDIdlc.xmlsemantics.SemanticException;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
//import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//import java.io.BufferedWriter;
import java.io.File;
//import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Vector;

//import javax.swing.plaf.SplitPaneUI;

/**
 * Generates Cpp for typedefs.
 */
class XmlTypedef2Cpp
    implements Idl2XmlNames
{

    private boolean m_generate;

    private final static String INTERNAL_ARRAY_STRING = "TIDorb::types::String_InternalMember";

    private final static String INTERNAL_ARRAY_WSTRING = "TIDorb::types::WString_InternalMember";

    public XmlTypedef2Cpp()
    {}

    /** Generate Cpp */
    public void generateCpp(Element doc, String sourceDirectory,
                            String headerDirectory, String genPackage,
                            boolean generateCode, boolean expanded, String h_ext, String c_ext)
        throws Exception
    {
    	
        // No genero c???digo si tiene Do_Not_Generate_Code
        String doNotGenerateCode = doc.getAttribute(OMG_Do_Not_Generate_Code);
        if (doNotGenerateCode!=null&&doNotGenerateCode.equals("TRUE"))
        	return;
    	
        m_generate = generateCode;

        NodeList nodes = doc.getChildNodes();
        
        //<typedef>
        //(item 0) ==> type
        //(item 1) ==> decl
        // ...
        //(item n) ==> decl
        //</typedef>
                  
        Element type = (Element) nodes.item(0);

        if (type.getTagName().equals(OMG_sequence)) { // aqu??? tenemos lo
                                                      // que deber???a generar
                                                      // secuencias
            // podr???a ser un ARRAY de secuencias
            for (int i = 1; i < nodes.getLength(); i++) {
                Element decl = (Element) nodes.item(i);
                generateTypeDefSequence(type, decl, sourceDirectory,
                                        headerDirectory, genPackage, expanded, h_ext, c_ext);
            }

        } else { // aqui construimos typedefs de cualquier
            // cosa que no sea sequence - ARRAYS incluidos
            for (int i = 1; i < nodes.getLength(); i++) {
                Element decl = (Element) nodes.item(i);
                generateTypeDef(type, decl, sourceDirectory, headerDirectory,
                                genPackage, expanded, h_ext, c_ext);
            }
        }

    }

    /**
     * Generates typedefs and arrays It doesn't handle sequences (unless defined
     * before)
     * 
     * @param type
     * @param decl
     * @param sourceDirectory
     * @param headerDirectory
     * @param genPackage
     * @throws Exception
     */

    private void generateTypeDef(Element type, Element decl,
                                 String sourceDirectory,
                                 String headerDirectory, String genPackage, 
								 boolean expanded, String h_ext, String c_ext)
        throws Exception
    {

        // generaci???n de typedefs y arrays

        //FileWriter writer;
        //BufferedWriter buf_writer;
//    	 Gets the FileManager
        FileManager fm = FileManager.getInstance();
        StringBuffer contents_buffer;
        String fileName;
        //String contents;
        StringBuffer holderBuff;
        StringBuffer headerBuff;
        StringBuffer sourceBuff;
        StringBuffer tmpBuff;
        // BUG #42
        boolean noHolder = false;
        
        if (genPackage.startsWith("::"))
            genPackage = genPackage.substring(2);
        // Get package components
        String headerDir = Xml2Cpp.getDir(genPackage, headerDirectory,
                                           this.m_generate);
        String sourceDir = Xml2Cpp.getDir(genPackage, sourceDirectory,
                                           this.m_generate);

        String name = decl.getAttribute(OMG_name);
        String nameWithPackage = genPackage.equals("") ? name : genPackage
                                                                + "::" + name;

        //Xml2Cpp.generateForwardUtilSimbols(genPackage,name); // DAVV - RELALO
        headerBuff = new StringBuffer();
        sourceBuff = new StringBuffer();

        /*
         * String helperClass = (genPackage + "::_" + name + "Helper"); String
         * holderClass = (genPackage + "::_" + name + "Holder");
         * TypedefManager.getInstance() .typedef(genPackage + "::" + name,
         * genPackage + "::" + name, holderClass, helperClass, null, null);
         */
        XmlHppHeaderGenerator.generate(type, headerBuff, "typedef", name,
                                       genPackage);
        XmlCppHeaderGenerator.generate(sourceBuff, "typedef", name, genPackage);

        // _tc_ Type Code Generation. //vv Static or Extern
        headerBuff.append(XmlType2Cpp.getTypeStorageForTypeCode(
                        (Element) type.getParentNode()));
        headerBuff.append("const ::CORBA::TypeCode_ptr _tc_");
        headerBuff.append(name);
        headerBuff.append(";\n\n");

        String internalType = XmlType2Cpp.getTypeTypedef(type);       
        
        // generaci???n del c???digo

        tmpBuff = new StringBuffer();
        
        String definitionType = XmlType2Cpp.getDefinitionType(type);
        
        if (decl.getTagName().equals(OMG_array)) { // construcci???n de un
                                                   // array

            if (XmlType2Cpp.isAWString(type)) // ya sea type.getTagName
                                              // OMG_type o OMG_scoped_name
                internalType = INTERNAL_ARRAY_WSTRING;
            else if (XmlType2Cpp.isAString(type))
                internalType = INTERNAL_ARRAY_STRING;
            else if (type.getTagName().equals(OMG_scoped_name)) {                
                if (definitionType.equals(OMG_valuetype)
                    || definitionType.equals(OMG_interface)) {
                    internalType += "_var";
                } /*else if (definitionType.equals(OMG_array)) {
                    throw new SemanticException(
                                  "Array of scoped array not supported yet.",
                                  decl);
                }*/
            }

            generateArrayHpp(headerBuff, decl, internalType, name,
                             nameWithPackage); //  - H -
            generateArrayCpp(tmpBuff, decl, name, nameWithPackage, internalType, type); // - C -  
     

        } else { 
            noHolder = true;
            // typedef de una declaraci???n simple
            // solo a???ade al - H - (excepto en typedefs de (w)strings)
            if (type.getTagName().equals(OMG_type)) {
                if(type.getAttribute(OMG_kind).equals(OMG_Object))
                {
                    internalType = internalType.substring(0,internalType.indexOf("_ptr"));
                }
                headerBuff.append("typedef " + internalType + " " + name
                                  + ";\n");
                // For each IDL Fixed-point typedef, a corresponding _out type is
                // defined as a reference to the fixed-point type.
                if(type.getAttribute(OMG_kind).equals(OMG_fixed)) {
                    noHolder=false;
                    headerBuff.append("typedef " + name + "& " + name + "_out;\n");
                }
                else if (XmlType2Cpp.isAString(type)) {
                    headerBuff.append("typedef CORBA::String_var " + name
                                      + "_var;\n");
                    headerBuff.append("typedef CORBA::String_out " + name
                                      + "_out;\n");
                    String storage = "";
                    if (XmlType2Cpp.getDefinitionType(
                            (Element) decl.getParentNode().getParentNode())
                            != OMG_module)
                        storage = "static ";
                    headerBuff.append(storage + "char* " + name
                                      + "_alloc(CORBA::ULong len);\n");
                    headerBuff.append(storage + "char* " + name
                                      + "_dup(const char* str);\n");
                    headerBuff.append(storage + "void " + name
                                      + "_free(char* str);\n");
                    tmpBuff.append("char* " + nameWithPackage
                                   + "_alloc(CORBA::ULong len) {\n");
                    tmpBuff.append("\treturn CORBA::string_alloc(len);\n");
                    tmpBuff.append("}\n");
                    tmpBuff.append("char* " + nameWithPackage
                                   + "_dup(const char* str) {\n");
                    tmpBuff.append("\treturn CORBA::string_dup(str);\n");
                    tmpBuff.append("}\n");
                    tmpBuff.append("void " + nameWithPackage
                                   + "_free(char* str) {\n");
                    tmpBuff.append("\tCORBA::string_free(str);\n");
                    tmpBuff.append("}\n");
                } else if (XmlType2Cpp.isAWString(type)) {
                    headerBuff.append("typedef CORBA::WString_var " + name
                                      + "_var;\n");
                    headerBuff.append("typedef CORBA::WString_out " + name
                                      + "_out;\n");
                    String storage = "";
                    if (XmlType2Cpp.getDefinitionType(
                            (Element) decl.getParentNode().getParentNode()) 
                            != OMG_module)
                        storage = "static ";
                    headerBuff.append(storage + "CORBA::WChar* " + name
                                      + "_alloc(CORBA::ULong len);\n");
                    headerBuff.append(storage + "CORBA::WChar* " + name
                                      + "_dup(const CORBA::WChar* wstr);\n");
                    headerBuff.append(storage + "void " + name
                                      + "_free(CORBA::WChar* wstr);\n");
                    tmpBuff.append("CORBA::WChar* " + nameWithPackage
                                   + "_alloc(CORBA::ULong len) {\n");
                    tmpBuff.append("\treturn CORBA::wstring_alloc(len);\n");
                    tmpBuff.append("}\n");
                    tmpBuff.append("CORBA::WChar* " + nameWithPackage
                                   + "_dup(const CORBA::WChar* wstr) {\n");
                    tmpBuff.append("\treturn CORBA::wstring_dup(wstr);\n");
                    tmpBuff.append("}\n");
                    tmpBuff.append("void " + nameWithPackage
                                   + "_free(CORBA::WChar* wstr) {\n");
                    tmpBuff.append("\tCORBA::wstring_free(wstr);\n");
                    tmpBuff.append("}\n");
                } else if (type.getAttribute(OMG_kind).equals(OMG_Object)) {
                    headerBuff.append("typedef " + internalType + "_var "
                                      + name + "_var;\n");
                    headerBuff.append("typedef " + internalType + "_ptr "
                                      + name + "_ptr;\n");
                    headerBuff.append("typedef " + internalType + "_out "
                                      + name + "_out;\n");
                } else if (XmlType2Cpp.isABasicDataType(type)) {
                    headerBuff.append("typedef " + name + "& " + name + "_out;\n");
                }

            } else if (type.getTagName().equals(OMG_scoped_name)) {                
                
                /*
                 * internalType = type.getAttribute(OMG_name); // DAVV - RELALO
                 * if(internalType.startsWith("::") ) internalType =
                 * internalType.substring(2);
                 */

                if (definitionType.equals(OMG_exception)
                    || definitionType.equals(OMG_interface)
                    || definitionType.equals(OMG_struct)
                    || definitionType.equals(OMG_union)
                    || definitionType.equals(OMG_sequence)
                    || (definitionType.equals(OMG_kind) 
                        && XmlType2Cpp.getDeepKind(type).equals(OMG_Object))) {
                    if(definitionType.equals(OMG_kind) && XmlType2Cpp.getDeepKind(type).equals(OMG_Object))
                    {
                        internalType = internalType.substring(0,internalType.indexOf("_ptr"));
                    }
                    
                    String typedef = ("typedef " + internalType);
                    headerBuff.append(typedef + " " + name + ";\n");
                    headerBuff.append(typedef + "_var " + name + "_var;\n");
                    headerBuff.append(typedef + "_ptr " + name + "_ptr;\n");
                    headerBuff.append(typedef + "_out " + name + "_out;\n");
                    if (definitionType.equals(OMG_interface)) {
                        String internalPackage = "";
                        String internalStub = internalType;
                        if (internalType.lastIndexOf("::") >= 0) {
                            internalStub = internalType.substring(
                                      internalType.lastIndexOf("::") + 2);
                            internalPackage = internalType.substring(0,
                                           internalType.lastIndexOf("::") + 2);
                        }
                        internalStub = internalPackage + "_" + internalStub
                                       + "Stub";
                        headerBuff.append("typedef " + internalStub + " _"
                                          + name + "Stub;\n");
                    }
                } else if (definitionType.equals(OMG_enum)) {                
                    String typedef = ("typedef " + internalType);
                    headerBuff.append(typedef + " " + name + ";\n");
                    headerBuff.append(typedef + "_ptr " + name + "_ptr;\n");
                    headerBuff.append(typedef + "_out " + name + "_out;\n");
                } else if (definitionType.equals(OMG_valuetype)) {                    
                    String typedef = ("typedef " + internalType);
                    headerBuff.append(typedef + " " + name + ";\n");
                    headerBuff.append(typedef + "_var " + name + "_var;\n");
                    headerBuff.append(typedef + "_ptr " + name + "_ptr;\n");
                    headerBuff.append(typedef + "_out " + name + "_out;\n");
                } else if (definitionType.equals(OMG_array)) {
                    noHolder = false;
                    String typedef = ("typedef " + internalType);
                    headerBuff.append(typedef + " " + name + ";\n");
                    headerBuff.append(typedef + "_slice " + name + "_slice;\n");
                    headerBuff.append(typedef + "_var " + name + "_var;\n");
                    headerBuff.append(typedef + "_out " + name + "_out;\n");
                    headerBuff.append(typedef + "_forany " + name
                                      + "_forany;\n\n");

                    String storage = "";
                    if (XmlType2Cpp.getDefinitionType(
                            (Element) decl.getParentNode().getParentNode())
                            != OMG_module)
                        storage = "static ";
                    headerBuff.append(storage + name + "_slice* " + name
                                      + "_alloc();\n");
                    headerBuff.append(storage + name + "_slice* " + name
                                      + "_dup(const " + name + "_slice*);\n");
                    headerBuff.append(storage + "void " + name + "_copy("
                                      + name + "_slice* to, const " + name
                                      + "_slice* from);\n");
                    headerBuff.append(storage + "void " + name + "_free("
                                      + name + "_slice* it);\n");

                    tmpBuff.append(nameWithPackage + "_slice* "
                                   + nameWithPackage + "_alloc() {\n");
                    tmpBuff.append("\treturn " + internalType + "_alloc();\n");
                    tmpBuff.append("}\n\n");
                    tmpBuff.append(nameWithPackage + "_slice* "
                                   + nameWithPackage + "_dup(const "
                                   + nameWithPackage + "_slice* arrayIn) {\n");
                    tmpBuff.append("\treturn " + internalType
                                   + "_dup(arrayIn);\n");
                    tmpBuff.append("}\n\n");
                    tmpBuff.append("void " + nameWithPackage + "_copy("
                                   + nameWithPackage + "_slice* to, const "
                                   + nameWithPackage + "_slice* from) {\n");
                    tmpBuff.append("\t" + internalType + "_copy(to, from);\n");
                    tmpBuff.append("}\n\n");
                    tmpBuff.append("void " + nameWithPackage + "_free("
                                   + nameWithPackage + "_slice* it) {\n");
                    tmpBuff.append("\t" + internalType + "_free(it);\n");
                    tmpBuff.append("}\n\n");
                } else if (XmlType2Cpp.isAString(type)) {
                    headerBuff.append("typedef " + internalType + " " + name
                                      + ";\n");
                    headerBuff.append("typedef " + internalType + "_var "
                                      + name + "_var;\n");
                    headerBuff.append("typedef " + internalType + "_out "
                                      + name + "_out;\n");
                    String storage = "";
                    if (XmlType2Cpp.getDefinitionType(
                            (Element) decl.getParentNode().getParentNode())
                            != OMG_module)
                        storage = "static ";
                    headerBuff.append(storage + "char* " + name
                                      + "_alloc(CORBA::ULong len);\n");
                    headerBuff.append(storage + "char* " + name
                                      + "_dup(const char* str);\n");
                    headerBuff.append(storage + "void " + name
                                      + "_free(char* str);\n");
                    tmpBuff.append("char* " + nameWithPackage
                                   + "_alloc(CORBA::ULong len) {\n");
                    tmpBuff.append("\treturn " + internalType
                                   + "_alloc(len);\n");
                    tmpBuff.append("}\n");
                    tmpBuff.append("char* " + nameWithPackage
                                   + "_dup(const char* str) {\n");
                    tmpBuff.append("\treturn " + internalType + "_dup(str);\n");
                    tmpBuff.append("}\n");
                    tmpBuff.append("void " + nameWithPackage
                                   + "_free(char* str) {\n");
                    tmpBuff.append("\t" + internalType + "_free(str);\n");
                    tmpBuff.append("}\n");
                } else if (XmlType2Cpp.isAWString(type)) {
                    headerBuff.append("typedef " + internalType + " " + name
                                      + ";\n");
                    headerBuff.append("typedef " + internalType + "_var "
                                      + name + "_var;\n");
                    headerBuff.append("typedef " + internalType + "_out "
                                      + name + "_out;\n");
                    String storage = "";
                    if (XmlType2Cpp.getDefinitionType(
                            (Element) decl.getParentNode().getParentNode()) 
                            != OMG_module)
                        storage = "static ";
                    headerBuff.append(storage + "CORBA::WChar* " + name
                                      + "_alloc(CORBA::ULong len);\n");
                    headerBuff.append(storage + "CORBA::WChar* " + name
                                      + "_dup(const CORBA::WChar* wstr);\n");
                    headerBuff.append(storage + "void " + name
                                      + "_free(CORBA::WChar* wstr);\n");
                    tmpBuff.append("CORBA::WChar* " + nameWithPackage
                                   + "_alloc(CORBA::ULong len) {\n");
                    tmpBuff.append("\treturn " + internalType
                                   + "_alloc(len);\n");
                    tmpBuff.append("}\n");
                    tmpBuff.append("CORBA::WChar* " + nameWithPackage
                                   + "_dup(const CORBA::WChar* wstr) {\n");
                    tmpBuff
                        .append("\treturn " + internalType + "_dup(wstr);\n");
                    tmpBuff.append("}\n");
                    tmpBuff.append("void " + nameWithPackage
                                   + "_free(CORBA::WChar* wstr) {\n");
                    tmpBuff.append("\t" + internalType + "_free(wstr);\n");
                    tmpBuff.append("}\n");
                } else { // The parent type is scoped but is from a basic type.
                         // (definition.equals(OMG_kind))
                    headerBuff.append("typedef " + internalType + " " + name
                                      + ";\n");                                      
                }
            }
        }
        
       

        // generaci???n de Helpers

        /*
         * contents = generateHppHelperDef(type, decl, genPackage);
         * headerBuff.append("\n" + contents);
         */
        
        headerBuff.append("\n"
                          + XmlCppHelperGenerator.generateHpp(type, decl,
                                                              genPackage, noHolder));

        //contents = generateCppHelperDef(type, decl, genPackage, typeHolder);
        sourceBuff.append(XmlCppHelperGenerator.generateCpp(type, decl,
                                                            genPackage, noHolder));
        contents_buffer = generateCppHelperDef(type, decl, genPackage); // solo
                                                                 // contiene la
                                                                 // implementacion
                                                                 // del holder
        sourceBuff.append(contents_buffer);
        sourceBuff.append(tmpBuff);

        // generacion de Holders
        String holder_contents = "";  
        
        String holderClass = XmlType2Cpp.getHolderName(nameWithPackage);
    
        if (decl.getTagName().equals(OMG_array) 
            || definitionType.equals(OMG_array)) {
            //generateArrayCpp(sourceBuff, decl, typeHolder, genPackage + "::"
            // + name, name);
        	contents_buffer = XmlCppHolderGenerator
                .generateHpp(OMG_Holder_Array, // DAVV
                             genPackage, nameWithPackage,
                             //TypedefManager.getInstance().getUnrolledHolderType(nameWithPackage));
                             XmlType2Cpp.getHolderName(nameWithPackage));
            
            holder_contents = XmlCppHolderGenerator.generateCpp(genPackage, name,
                                                                holderClass);
            
            
        } else if (decl.getTagName().equals(OMG_sequence)
                   || definitionType.equals(OMG_sequence)) {
            // Es probable que haya que modificar eso por eso la comparacion
            // doble., futurible.
            // Source Content.
        	contents_buffer = XmlCppHolderGenerator.generateHpp(OMG_Holder_Complex, 
                                   genPackage, nameWithPackage,
                                   XmlType2Cpp.getHolderName(nameWithPackage));
            
            holder_contents = XmlCppHolderGenerator.generateCpp(genPackage, name,
                                                                holderClass);
        } else if(definitionType.equals(OMG_fixed) ||
                  type.getAttribute(OMG_kind).equals(OMG_fixed)){
        	contents_buffer = XmlCppHolderGenerator.generateHpp(
                           OMG_Holder_Simple, genPackage,
                           nameWithPackage,
                           XmlType2Cpp.getHolderName(nameWithPackage)); 
            
            holder_contents = XmlCppHolderGenerator.generateCpp(genPackage, name,
                                                                holderClass);
            /*
            if (XmlType2Cpp.isAString(type)) 
                contents = XmlCppHolderGenerator.generateHpp(
                                 OMG_Holder_String, genPackage,
                                 nameWithPackage,
                                 XmlType2Cpp.getHolderName(nameWithPackage));
            else if (XmlType2Cpp.isAWString(type))
                contents = XmlCppHolderGenerator.generateHpp(
                                 OMG_Holder_WString, genPackage,
                                 nameWithPackage,
                                 XmlType2Cpp.getHolderName(nameWithPackage));
            else if (definitionType.equals(OMG_struct)
                     || definitionType.equals(OMG_union)
                     || definitionType.equals(OMG_exception)
                     ||(definitionType.equals(OMG_kind) 
                         && XmlType2Cpp.getDeepKind(type).equals(OMG_any))) {
                if (definitionType.equals(OMG_kind)) // Any
                    contents = XmlCppHolderGenerator.generateHpp(OMG_Holder_Any, 
                                     genPackage, nameWithPackage,
                                     XmlType2Cpp.getHolderName(nameWithPackage));
                else
                    contents = XmlCppHolderGenerator.generateHpp(
                                     OMG_Holder_Complex, 
                                     genPackage, nameWithPackage,
                                     XmlType2Cpp.getHolderName(nameWithPackage));
            } else if (definitionType.equals(OMG_interface)) {
                contents = XmlCppHolderGenerator.generateHpp(
                                 OMG_Holder_Ref, genPackage, nameWithPackage,
                                 XmlType2Cpp.getHolderName(nameWithPackage));
            } 
            	
            	
            */
        }
        
        headerBuff.append(contents_buffer);
        sourceBuff.append(holder_contents);
        

        // Generaci???n de pie de archivo

        XmlHppHeaderGenerator.generateFoot(headerBuff, "typedef", name,
                                           genPackage);

        String idl_fn = XmlUtil.getIdlFileName(type);

        // Volcado de ficheros
        if (this.m_generate) { // Source Writer
            fileName = name + c_ext; // Only One file for everything.
            Traces.println("XmlTypedef2Cpp:->", Traces.DEEP_DEBUG);
            Traces.println("Generating : " + sourceDir + File.separatorChar
                           + fileName + "...", Traces.USER);
            //writer = new FileWriter(sourceDir + File.separatorChar + fileName);
            //buf_writer = new BufferedWriter(writer);
            //buf_writer.write(sourceBuff.toString());
            //buf_writer.close();
            fm.addFile(sourceBuff, fileName, sourceDir, idl_fn, FileManager.TYPE_MAIN_SOURCE);

            // Header Writer
            fileName = name + h_ext; // Only One file for everything.
            Traces.println("XmlTypedef2Cpp:->", Traces.DEEP_DEBUG);
            Traces.println("Generating : " + headerDir + File.separatorChar
                           + fileName + "...", Traces.USER);
            //writer = new FileWriter(headerDir + File.separatorChar + fileName);
            //buf_writer = new BufferedWriter(writer);
            //buf_writer.write(headerBuff.toString());
            //buf_writer.close();
            fm.addFile(headerBuff, fileName, headerDir, idl_fn, FileManager.TYPE_MAIN_HEADER);
        }

        headerBuff = new StringBuffer();

        // External any operations
        // Design of the header files, Any operations outside main file.
        StringBuffer re_buffer = new StringBuffer();
        XmlHppExternalOperationsGenerator.generateHpp(type, re_buffer,
                                                      OMG_typedef, name,
                                                      genPackage);
        headerBuff.append(re_buffer.toString());

        if (this.m_generate) {
            fileName = name + "_ext" + h_ext;
            //writer = new FileWriter(headerDir + File.separatorChar + fileName);
            //buf_writer = new BufferedWriter(writer);
            //buf_writer.write(headerBuff.toString());
            //buf_writer.close();
            fm.addFile(headerBuff, fileName, headerDir, idl_fn, FileManager.TYPE_MAIN_HEADER_EXT);
        }

        // Para cuando incluyen orb.idl que aparezcan los respectivos holder y
        // helper.
        //Xml2Cpp.generateForwardUtilSimbols(genPackage,name); 

    }

    private StringBuffer generateCppHelperDef(Element type, Element decl,
                                        String genPackage)
        throws Exception
    {

        StringBuffer buffer = new StringBuffer();
        String name = decl.getAttribute(OMG_name);
        String nameWithPackage = genPackage.equals("") ? name : genPackage
                                                                + "::" + name;

        //String helperClass = TypedefManager.getInstance()
        //        .getUnrolledHelperType(nameWithPackage);
        //String holderClass = TypedefManager.getInstance()
        //       .getUnrolledHolderType(nameWithPackage);
        // DAVV - Cualquier typedef del IDL tiene asociado un typedef en C++;
        // por eso en C++ no
        // es necesario desenrollar nada como pasa en Java.
        // TypedefManager no tiene sentido.
        String holderClass = XmlType2Cpp.getHolderName(nameWithPackage);

        boolean isSequence = XmlType2Cpp.isASequence(type);
        Vector indexes = new Vector();
        Vector isArray = new Vector();

        NodeList indexChilds = decl.getChildNodes(); // ARRAY (decl tiene
                                                     // hijos)
        for (int k = 0; k < indexChilds.getLength(); k++) {

            Element indexChild = (Element) indexChilds.item(k);

            if (indexChild != null) {
                indexes.insertElementAt(new Long(XmlExpr2Cpp
                    .getIntExpr(indexChild)), k);
                isArray.insertElementAt(new Boolean(true), k);
            }
        }

        Element el = type;
        if (isSequence) { // DAVV - sequence (type es sequence)

            int val = 0;
            String tag = type.getTagName();
            el = type;

            while (tag.equals(OMG_sequence)) {
                el = (Element) el.getFirstChild();

                Element expr = (Element) el.getNextSibling();

                if (expr != null && !(expr.getTagName().equals(OMG_typedef))) {
                    indexes.addElement(new Long(XmlExpr2Cpp.getIntExpr(expr)));
                } else {
                    indexes.addElement(new String("length" + val));
                }

                isArray.addElement(new Boolean(false));
                isArray.addElement(nameWithPackage); // is a Sequence and This
                                                     // is its name,
                tag = el.getTagName();
                val++;
            }

        }
        //} else if (isBoundedString) { // DAVV - no son excluyentes!! puede
        // aparecer 'typedef sequence<string<3>, 4> pugnetero'

        boolean isBoundedString = (XmlType2Cpp.isAString(el) || XmlType2Cpp.isAWString(el))
                                	&& el.getFirstChild() != null;

        if (isBoundedString) { // DAVV - cadena limitada
            Element expr = (Element) el.getFirstChild();

            if (expr != null)
                indexes.addElement(new Long(XmlExpr2Cpp.getIntExpr(expr)));

            isArray.addElement(new Boolean(false));
        }
             
        return buffer;
    }

    private StringBuffer generateSequenceHpp(Element type, Element decl)
        throws Exception
    {

        // type es el nodo 'sequence'
        // decl la declaracion de la misma

        String name = decl.getAttribute(OMG_name);
        StringBuffer buffer = new StringBuffer();
        Element internal = (Element) type.getFirstChild();
        String internalType = XmlType2Cpp.getType(internal);
        String definitionType = XmlType2Cpp.getDefinitionType(internal);

        // _tc_ Type Code Generation. //vv Static or Extern
        buffer.append(XmlType2Cpp.getTypeStorageForTypeCode((Element) type
            .getParentNode()));
        buffer.append("const ::CORBA::TypeCode_ptr _tc_");
        buffer.append(name);
        buffer.append(";\n\n");

        // Comprobamos si es una Bounded Sequence..
        String bounds = "";
        if (type.getChildNodes().getLength() > 1) { // siempre habra al
                                                    // menos un nodo hijo: el
                                                    // tipo del contenido del
                                                    // contenido de la secuencia
            Element el = (Element) type.getLastChild();
            if (el != null) {
                Element expr = (Element) el.getFirstChild();
                if (expr != null) {
                    //if(expr.getTagName().equals(OMG_expr))
                    bounds = "" + XmlExpr2Cpp.getIntExpr(expr);
                }
            }
        }

        // casos conflictivos: STRING (incluye scoped_name)
        if (XmlType2Cpp.isAString(internal)) {
            if (!bounds.equals("")) {
                generateHppBoundedStringSequence(buffer, name, bounds);
            } else
                generateHppStringSequence(buffer, name);

            // casos conflictivos: WSTRING (incluye scoped_name)
        } else if (XmlType2Cpp.isAWString(internal)) {
            if (!bounds.equals("")) {
                generateHppBoundedWStringSequence(buffer, name, bounds);
            } else
                generateHppWStringSequence(buffer, name);

        } else {
            // casos conflictivos: interface (incluye scoped_name)
            if (definitionType.equals(OMG_interface)
                || (internalType.equals(XmlType2Cpp.basicMapping(OMG_Object)))) {
                internalType = (internalType.endsWith("_ptr") 
                    ? internalType.substring(0, internalType.length() - 4) 
                        : internalType); // para Objects
                if (!bounds.equals("")) {
                    generateHppBoundedInterfaceSequence(buffer, internalType,
                                                        name, bounds, false);
                } else {
                    generateHppInterfaceSequence(buffer, internalType, name,
                                                 false);
                }
            } else {
                // casos conflictivos: valuetype (incluye scoped_name)
                if (definitionType.equals(OMG_valuetype)) {
                    if (!bounds.equals("")) {
                        generateHppBoundedInterfaceSequence(buffer,
                                                            internalType, name,
                                                            bounds, true);
                    } else {
                        generateHppInterfaceSequence(buffer, internalType,
                                                     name, true);
                    }
                    // caso general
                } else {
                    if (!bounds.equals("")) {
                        generateHppBoundedSequence(buffer, internalType, name,
                                                   bounds, type);
                    } else {
                        generateHppSequence(buffer, internalType, name,type);
                    }
                }
            }
        }

        if (definitionType.equals(OMG_interface))
            internalType += "_ptr";

        buffer.append("typedef " + name + "* " + name + "_ptr;\n");
        buffer.append("typedef ::TIDorb::templates::");
        buffer.append("SequenceT_var<"); // lleva sus propios templates
                                         // por operador []
        buffer.append(name + ", " + internalType);
        buffer.append("> ");
        buffer.append(name);
        buffer.append("_var;\n");
        buffer.append("typedef ::TIDorb::templates::");
        buffer.append("SequenceT_out<");
        buffer.append(name + ", " + internalType);
        buffer.append("> ");
        buffer.append(name);
        buffer.append("_out;\n");

        return buffer;
    }

    private void generateArrayHpp(StringBuffer buffer, Element decl,
                                  String internalType, String name,
                                  String nameWithPackage)
        throws Exception
    {

        NodeList nl = decl.getChildNodes();
        StringBuffer arrayBounds = new StringBuffer();
        StringBuffer sliceBounds = new StringBuffer();
        Element el = null;
        
        ArrayList objectNameEnumFirstElement = new ArrayList();
        boolean isEnumType = false;
		

        
        for (int i = 0; i < nl.getLength(); i++) {
            el = (Element) nl.item(i);
            if (el != null) {
                Element expr = (Element) el.getFirstChild();
                if (expr != null) {// Si viene el tama?o se lo fijamos.
                    arrayBounds.append("[" + XmlExpr2Cpp.getIntExpr(expr) + "]");
                    if (i > 0)
                        sliceBounds.append("[" + XmlExpr2Cpp.getIntExpr(expr)
                                           + "]");
                }
            }
        }
        
        
    		Element root = el.getOwnerDocument().getDocumentElement();
    		NodeList enums = root.getElementsByTagName(OMG_enum);

    		for (int k = 0; k < enums.getLength();k++){
    			Element en = (Element) enums.item(k);
    			if (en.getAttribute(OMG_scoped_name).equals("::" + internalType)){
    				 isEnumType = true;
    				 NodeList subchild = en.getElementsByTagName(OMG_enumerator);
    				 Element child = (Element) subchild.item(0);
                                 // objectNameEnumFirstElement.add (k, child.getAttribute(OMG_name));
    				 objectNameEnumFirstElement.add (child.getAttribute(OMG_name));
    			}
    		}
        //}    
            
        
        

        

        buffer.append("typedef " + internalType + " " + name);
        buffer.append(arrayBounds.toString());
        buffer.append(";\n");
        buffer.append("typedef " + internalType + " " + name + "_slice"
                      + sliceBounds + ";\n");

        buffer.append("\n");

        String storage = "";
        if (XmlType2Cpp.getDefinitionType(
                 (Element) decl.getParentNode().getParentNode()) 
                 != OMG_module)
            storage = "static ";
        buffer.append(storage + name + "_slice *" + name + "_alloc();\n");
        buffer.append(storage + name + "_slice *" + name + "_dup(const " + name
                      + "_slice*);\n");
        buffer.append(storage + "void " + name + "_copy(" + name
                      + "_slice* to, const " + name + "_slice* from);\n");
        buffer.append(storage + "void " + name + "_free(" + name
                      + "_slice* it);\n\n");

        // Declaracion de [array]_var (T_var<[array]> no sirve) -- DAVV
        buffer.append("\nclass " + name + "_var {\n");
        buffer.append("  public:\n");
        buffer.append("\t" + name + "_var() : m_slice(");
//         if (isEnumType){
//         int z = 0;
//         while (z < objectNameEnumFirstElement.size()){
//         		buffer.append(objectNameEnumFirstElement.get(z) + ") {}\n");
//         	z++;
//         }
//         } else {
        buffer.append("0) {}\n");
        //        }
        buffer.append("\t" + name + "_var(" + name
                      + "_slice* s) : m_slice(s) {}\n");
        buffer.append("\t" + name + "_var(const " + name + "_var&);\n");
        buffer.append("\t~" + name + "_var() {" + nameWithPackage
                      + "_free(m_slice);}\n\n");
        buffer.append("\t" + name + "_var& operator=(" + name + "_slice*);\n");
        buffer.append("\t" + name + "_var& operator=(const " + name
                      + "_var&);\n\n");
        buffer.append("\t"
                    + name
                    + "_slice& operator[] (CORBA::ULong index) {return m_slice[index];}\n");
        buffer.append("\tconst "
                    + name
                    + "_slice& operator[] (CORBA::ULong index) const {return m_slice[index];}\n\n");
        buffer.append("\tconst " + name
                      + "_slice* in() const {return m_slice;}\n");
        buffer.append("\t" + name + "_slice* inout() {return m_slice;}\n");
        // Fix to bug #301 out() accessor returns _slice* instead of _slice*& for variable arrays
        String variable_size = decl.getAttribute(OMG_variable_size_type);
        boolean isVariable = variable_size.equals("true"); 
        if (isVariable) {
        	buffer.append("\t" + name + "_slice*& out();\n");
        } else {
        	buffer.append("\t" + name + "_slice* out();\n");	
        }              
        buffer.append("\t" + name + "_slice* _retn();\n\n");
        buffer.append("\toperator " + name + "_slice*() {return m_slice;}\n");
        buffer.append("\toperator const " + name
                      + "_slice*() const {return (const " + name
                      + "_slice*) m_slice;}\n\n");
        buffer.append("  private:\n");
        buffer.append("\t" + name + "_slice* m_slice;\n\n");
        buffer.append("};  // class " + name + "_var\n\n");

        // Declaracion de [array]_out (T_out<[array]> no sirve) -- DAVV
        buffer.append("\nclass " + name + "_out {\n");
        buffer.append("  public:\n");
        buffer.append("\t" + name + "_out(" + name
                      + "_slice* s) : m_slice(s) {m_slice");
        
//         if (isEnumType){
//         int l = 0;
//         while (l < objectNameEnumFirstElement.size()){
//         		buffer.append(" = " + objectNameEnumFirstElement.get(l) + "}\n");
//         	l++;
//         }
//         } else
//         {
        buffer.append(" = 0;}\n");
        //        }
        
        buffer.append("\t" + name + "_out(" + name + "_var& v);\n");
        buffer.append("\t" + name + "_out(const " + name
                      + "_out& o) : m_slice(o.m_slice) {}\n\n");
        buffer.append("\t" + name + "_out& operator=(" + name + "_slice*);\n");
        buffer.append("\t" + name + "_out& operator=(const " + name
                      + "_out&);\n\n");
        buffer.append("\t"
                      + name
                      + "_slice& operator[] (CORBA::ULong index) {return m_slice[index];}\n");
        buffer.append("\toperator " + name + "_slice*&() {return m_slice;}\n");
        buffer.append("\t" + name + "_slice*& ptr() {return m_slice;}\n\n");
        buffer.append("  private:\n");
        buffer.append("\t" + name + "_slice* m_slice;\n");
        buffer.append("\t// assigment from T_var not allowed\n");
        buffer.append("\tvoid operator=(const " + name + "_var&) {}\n\n");
        buffer.append("};  // class " + name + "_out\n\n");

        // Declaracion de [array]_forany -- DAVV
        buffer.append("\nclass " + name + "_forany {\n");
        buffer.append("  public:\n");
        buffer.append("\t" + name
                      + "_forany() : m_slice(");
        
 //        if (isEnumType) {
//         int k = 0;
//         while (k < objectNameEnumFirstElement.size()){
//         		buffer.append(objectNameEnumFirstElement.get(k) + ")");
//         	k++;
//         }
//         } else {
        	buffer.append("0)");
                //        }
        
		buffer.append(", nocopy_flag(false) {}\n");
        //buffer.append("\t" + name + "_forany(" + name + " s) : m_slice(s),
        // nocopy(false) {}\n");
        buffer.append("\t" + name + "_forany(" + name
                      + "_slice* s) : m_slice(s), nocopy_flag(false) {}\n");
        buffer.append("\t"
                      + name
                      + "_forany("
                      + name
                      + "_slice* s, CORBA::Boolean nocopy) : m_slice(s), nocopy_flag(nocopy) {}\n");
        buffer.append("\t" + name + "_forany(const " + name + "_forany&);\n");
        //buffer.append("\t~" + name + "_forany() {" + nameWithPackage +
        // "_free(m_slice);}\n\n"); - DAVV - no libera almacenamiento; Any
        // retiene propiedad
        buffer.append("\t" + name + "_forany& operator=(" + name
                      + "_slice*);\n");
        buffer.append("\t" + name + "_forany& operator=(const " + name
                      + "_forany&);\n\n");
        buffer.append("\t"
                      + name
                      + "_slice& operator[] (CORBA::ULong index) {return m_slice[index];}\n");
        buffer.append("\tconst "
                      + name
                      + "_slice& operator[] (CORBA::ULong index) const {return m_slice[index];}\n\n");
        buffer.append("\tconst " + name
                      + "_slice* in() const {return m_slice;}\n");
        buffer.append("\t" + name + "_slice* inout() {return m_slice;}\n");
        buffer.append("\t" + name + "_slice* out();\n");
        buffer.append("\t" + name + "_slice* _retn();\n\n");
        buffer.append("\toperator " + name + "_slice*() {return m_slice;}\n");
        buffer.append("\toperator const " + name
                      + "_slice*() const {return (const " + name
                      + "_slice*) m_slice;}\n\n");
        buffer.append("\tCORBA::Boolean nocopy() const {return nocopy_flag;}\n\n");
        buffer.append("  private:\n");
        buffer.append("\t" + name + "_slice* m_slice;\n");
        buffer.append("\tCORBA::Boolean nocopy_flag;\n\n");
        buffer.append("};  // class " + name + "_forany\n\n");

    }

    private void generateArrayCpp(StringBuffer buffer, Element decl,
                                  String name, String nameWithPackage,
    							  String InternalType, Element type)
        throws Exception
    {

        // Comprobamos que no sea un Array.
        NodeList nl = decl.getChildNodes();
        StringBuffer arrayBounds = new StringBuffer();
        String firstBound = "";
        Element el = null;
        String definitionType = XmlType2Cpp.getDefinitionType(type);
        int dimensions = 0;
        for (int i = 0; i < nl.getLength(); i++) {
            el = (Element) nl.item(i);
            if (el != null) {
                Element expr = (Element) el.getFirstChild();
                if (expr != null) {// Si viene el tama?o se lo fijamos.
                    if (dimensions > 0)
                        arrayBounds.append("*");
                    arrayBounds.append(XmlExpr2Cpp.getIntExpr(expr));
                    if (dimensions == 0)
                        firstBound = arrayBounds.toString();
                    dimensions++;
                }

            }

        }
        if (dimensions > 0) {

            buffer.append(nameWithPackage + "_var::" + name + "_var(const "
                          + nameWithPackage + "_var& array_var)\n{\n");
            buffer.append("\tm_slice = 0;\n");
            buffer.append("\tif (array_var.m_slice != 0)\n");
            buffer.append("\t\tm_slice = " + nameWithPackage
                          + "_dup(array_var.m_slice);\n");
            buffer.append("}// end of _var copy constructor \n\n");

            buffer.append(nameWithPackage + "_var& " + nameWithPackage
                          + "_var::operator= (" + nameWithPackage
                          + "_slice* array_slice)\n{\n");
            buffer.append("\tif (array_slice != m_slice) {\n");
            buffer.append("\t\t" + nameWithPackage + "_free(m_slice);\n");
            buffer.append("\t\tm_slice = array_slice;\n");
            buffer.append("\t}\n\treturn *this;\n");
            buffer.append("}// end of _var _slice* assignment operator \n\n");

            buffer.append(nameWithPackage + "_var& " + nameWithPackage
                          + "_var::operator= (const " + nameWithPackage
                          + "_var& array_var)\n{\n");
            buffer.append("\tif (this != &array_var) {\n");
            buffer.append("\t\t" + nameWithPackage + "_free(m_slice);\n");
            buffer.append("\t\tif (array_var.m_slice != 0)\n");
            buffer.append("\t\t\tm_slice = " + nameWithPackage
                          + "_dup(array_var.m_slice);\n");
            buffer.append("\t\telse\n");
            buffer.append("\t\t\tm_slice = 0;\n");
            buffer.append("\t}\n\treturn *this;\n");
            buffer.append("}// end of _var array assignment operator \n\n");
			// Fix to bug #301 out() accessor returns _slice* instead of _slice*& for variable arrays
			String variable_size = decl.getAttribute(OMG_variable_size_type);
			boolean isVariable = variable_size.equals("true"); 
			if (isVariable) 
				buffer.append(nameWithPackage + "_slice*& " + nameWithPackage
						      + "_var::out()\n{\n");
            else
            	buffer.append(nameWithPackage + "_slice* " + nameWithPackage
            				  + "_var::out()\n{\n");
            buffer.append("\tif (m_slice != 0) {\n");
            buffer.append("\t\t" + nameWithPackage + "_free(m_slice);\n");
            buffer.append("\t\tm_slice = 0;\n");
            buffer.append("\t}\n\treturn m_slice;\n");
            buffer.append("}// end of _var out() \n\n");

            buffer.append(nameWithPackage + "_slice* " + nameWithPackage
                          + "_var::_retn() \n{\n");
            buffer.append("\t" + nameWithPackage + "_slice* _tmp = m_slice;\n");
            buffer.append("\t m_slice = 0;\n");
            buffer.append("\treturn _tmp;\n");
            buffer.append("}// end of _var _retn() \n\n");

            buffer.append(nameWithPackage + "_out::" + name + "_out("
                          + nameWithPackage
                          + "_var& array_var) : m_slice(array_var.out())\n{\n");
            buffer.append("\t" + nameWithPackage + "_free(m_slice);\n");
            buffer.append("\tm_slice = 0;\n");
            buffer.append("}// end of _out copy constructor from _var\n\n");

            buffer.append(nameWithPackage + "_out& " + nameWithPackage
                          + "_out::operator= (const " + nameWithPackage
                          + "_out& array_out)\n{\n");
            buffer.append("\tm_slice = array_out.m_slice;\n");
            buffer.append("\treturn *this;\n");
            buffer.append("}// end of _out assignment operator \n\n");

            buffer.append(nameWithPackage + "_out& " + nameWithPackage
                          + "_out::operator= (" + nameWithPackage
                          + "_slice* array_slice)\n{\n");
            buffer.append("\tm_slice = array_slice;\n");
            buffer.append("\treturn *this;\n");
            buffer.append("}// end of _out _slice* assignment operator \n\n");

            buffer.append(nameWithPackage + "_forany::" + name
                          + "_forany(const " + nameWithPackage
                          + "_forany& array_forany)\n{\n");
            buffer.append("\tnocopy_flag = array_forany.nocopy_flag;\n");
            buffer.append("\tm_slice = 0;\n");
            buffer.append("\tif (array_forany.m_slice != 0)\n");
            buffer.append("\t\tm_slice = " + nameWithPackage
                          + "_dup(array_forany.m_slice);\n");
            buffer.append("}// end of _forany copy constructor \n\n");

            buffer.append(nameWithPackage + "_forany& " + nameWithPackage
                          + "_forany::operator= (" + nameWithPackage
                          + "_slice* array_slice)\n{\n");
            buffer.append("\tnocopy_flag = false;\n");
            buffer.append("\tif (array_slice != m_slice) {\n");
            buffer.append("\t\t" + nameWithPackage + "_free(m_slice);\n");
            buffer.append("\t\tm_slice = array_slice;\n");
            buffer.append("\t}\n\treturn *this;\n");
            buffer.append("}// end of _forany _slice* assignment operator \n\n");

            buffer.append(nameWithPackage + "_forany& " + nameWithPackage
                          + "_forany::operator= (const " + nameWithPackage
                          + "_forany& array_forany)\n{\n");
            buffer.append("\tnocopy_flag = array_forany.nocopy_flag;\n");
            buffer.append("\tif (this != &array_forany) {\n");
            buffer.append("\t\t" + nameWithPackage + "_free(m_slice);\n");
            buffer.append("\t\tif (array_forany.m_slice != 0)\n");
            buffer.append("\t\t\tm_slice = " + nameWithPackage
                          + "_dup(array_forany.m_slice);\n");
            buffer.append("\t\telse\n");
            buffer.append("\t\t\tm_slice = 0;\n");
            buffer.append("\t}\n\treturn *this;\n");
            buffer.append("}// end of _forany array assignment operator \n\n");

            buffer.append(nameWithPackage + "_slice* " + nameWithPackage
                          + "_forany::out()\n{\n");
            buffer.append("\tif (m_slice != 0) {\n");
            buffer.append("\t\t" + nameWithPackage + "_free(m_slice);\n");
            buffer.append("\t\tm_slice = 0;\n");
            buffer.append("\t}\n\treturn m_slice;\n");
            buffer.append("}// end of _forany out() \n\n");

            buffer.append(nameWithPackage + "_slice* " + nameWithPackage
                          + "_forany::_retn() \n{\n");
            buffer.append("\t" + nameWithPackage + "_slice* _tmp = m_slice;\n");
            buffer.append("\t m_slice = 0;\n");
            buffer.append("\treturn _tmp;\n");
            buffer.append("}// end of _forany _retn() \n\n");

            buffer.append(nameWithPackage + "_slice *" + nameWithPackage
                          + "_alloc()\n{\n\t");
            buffer.append("return new " + nameWithPackage + "_slice[" + firstBound + "];\n");
            buffer.append("}// end of _alloc \n\n");

            buffer.append(nameWithPackage + "_slice *" + nameWithPackage
                          + "_dup(const " + nameWithPackage
                          + "_slice* arrayIn)\n{\n\t");
            buffer.append(nameWithPackage + "_slice* temp_copy = "
                          + nameWithPackage + "_alloc();\n");
            buffer.append("\tif (!temp_copy)  return 0;\n");
            String bounds = arrayBounds.toString();
            
            if ((dimensions == 1) && (bounds.indexOf("*") < 1) &&
            		XmlType2Cpp.isABasicDataType(type) ){
            	    buffer.append("\tmemcpy(temp_copy, arrayIn," + bounds + "*sizeof(" + InternalType + "));\n");
            } else{                    
	            for (int i = 0; i < dimensions; i++) {
	                int pos = bounds.indexOf("*");
	                if (pos > 0) {
	                    buffer.append("\tfor (CORBA::ULong i" + i + "=0;i" + i
	                                  + "<" + bounds.substring(0, pos) + ";i" + i
	                                  + "++)\n");
	                    bounds = bounds.substring(pos + 1);
	                    for (int j = 0; j <= i; j++)
	                        buffer.append("\t");
	                } else {
	                    buffer.append("\tfor (CORBA::ULong i" + i + "=0;i" + i
	                                  + "<" + bounds + ";i" + i + "++)\n");
	                    for (int j = 0; j <= i; j++)
	                        buffer.append("\t");
	                }
	            }
	            //buffer.append("\tfor (int i=0;i<" + arrayFirstBound.toString() +
	            // ";i++)\n\t{\n\t\t");


                    if (decl.getTagName().equals(OMG_array) && 
                        type.getTagName().equals(OMG_scoped_name) &&
                        definitionType.equals(OMG_array) ) {
                        // is a scoped array
                        buffer.append("\t" + InternalType +"_copy(temp_copy");
                        for (int i = 0; i < dimensions; i++) {
                            buffer.append("[i" + i + "]");
                        }
                        buffer.append(",arrayIn");
                        for (int i = 0; i < dimensions; i++) {
                            buffer.append("[i" + i + "]");
                        }
                        buffer.append(");\n");

                    } else {
	            buffer.append("\ttemp_copy");
	            for (int i = 0; i < dimensions; i++) {
	                buffer.append("[i" + i + "]");
	            }
	            buffer.append("=arrayIn");
	            for (int i = 0; i < dimensions; i++) {
	                buffer.append("[i" + i + "]");
	            }
	            buffer.append(";\n");
                    }
            }
            buffer.append("\treturn temp_copy;\n");
            buffer.append("}// end of _dup \n\n");

            buffer.append("void " + nameWithPackage + "_copy("
                          + nameWithPackage + "_slice* to, const "
                          + nameWithPackage + "_slice* from)\n{\n");
            bounds = arrayBounds.toString();
            if ((dimensions == 1) && (bounds.indexOf("*") < 1) &&
            		XmlType2Cpp.isABasicDataType(type) ){
        	        buffer.append("\tmemcpy(to, from," + bounds + "*sizeof(" + InternalType + "));\n");
            } else{             	       
	            for (int i = 0; i < dimensions; i++) {
	                int pos = bounds.indexOf("*");
	                if (pos > 0) {
	                    buffer.append("\tfor (CORBA::ULong i" + i + "=0;i" + i
	                                  + "<" + bounds.substring(0, pos) + ";i" + i
	                                  + "++)\n");
	                    bounds = bounds.substring(pos + 1);
	                    for (int j = 0; j <= i; j++)
	                        buffer.append("\t");
	                } else {
	                    buffer.append("\tfor (CORBA::ULong i" + i + "=0;i" + i
	                                  + "<" + bounds + ";i" + i + "++)\n");
	                    for (int j = 0; j <= i; j++)
	                        buffer.append("\t");
	                }
	            }
	            //buffer.append("\tfor (int i=0;i<" + arrayFirstBound.toString() +
	            // ";i++)\n");

                    if (decl.getTagName().equals(OMG_array) && 
                        type.getTagName().equals(OMG_scoped_name) &&
                        definitionType.equals(OMG_array) ) {
                        // Is a Scoped array
                        buffer.append("\t" + InternalType +"_copy(to");
                        for (int i = 0; i < dimensions; i++) {
                            buffer.append("[i" + i + "]");
                        }
                        buffer.append(",from");
                        for (int i = 0; i < dimensions; i++) {
                            buffer.append("[i" + i + "]");
                        }
                        buffer.append(");\n");

                    } else {

	            buffer.append("\tto");
	            for (int i = 0; i < dimensions; i++) {
	                buffer.append("[i" + i + "]");
	            }
	            buffer.append("=from");
	            for (int i = 0; i < dimensions; i++) {
	                buffer.append("[i" + i + "]");
	            }
	            buffer.append(";\n");
                    }
            }
            buffer.append("}// end of _copy \n\n");

            buffer.append("void " + nameWithPackage + "_free("
                          + nameWithPackage + "_slice* it)\n{\n");
            buffer.append("\tdelete[] it;\n");
            buffer.append("}// end of _free \n\n");
        }//dimensions >0

    }

    private static void generateHppBoundedStringSequence(StringBuffer buffer,
                                                         String name,
                                                         String size)
    {
        buffer.append("class " + name + "{ // Bounded String Sequence\n");
        buffer.append("\n\tpublic:\n\n");
        buffer.append("\t  "
                    + name
                    + "() : m_buffer(NULL), m_release(true), m_length(0), m_member(&m_release) {}\n");
        buffer.append("\t  "
                    + name
                    + "(CORBA::ULong length, char** data, CORBA::Boolean release = false)\n");
        buffer.append("\t  : m_buffer(data), m_release(release),	m_length(length) , m_member(&m_release){}\n");
        buffer.append("\t  " + name + "(const " + name + "& other);\n");
        buffer.append("\t  ~" + name + "();\n");
        //buffer.append("\t {\n"); // DAVV - implementado en cpp
        //buffer.append("\t \tif(m_release)\n");
        //buffer.append("\t \tfreebuf(m_buffer);\n");
        //buffer.append("\t }\n");
        buffer.append("\t  " + name + "& operator=(const " + name + "&);\n\n");
        buffer.append("\t  CORBA::ULong maximum() const {return m_bound;}\n");
        buffer.append("\t  void length(CORBA::ULong v);\n");
        buffer.append("\t  CORBA::ULong length() const {return m_length;}\n");
        //buffer.append("\t char*& operator[](CORBA::ULong index);\n");
        buffer.append("\t  ::TIDorb::types::String_SequenceMember& operator[](CORBA::ULong index);\n");
        //buffer.append("\t const "+internalType+"& operator[](CORBA::ULong
        // index) const {return (const "+internalType+"&) m_buffer[index];}\n");
        buffer.append("\t  const char*& operator[](CORBA::ULong index) const;\n");
        buffer.append("\t  CORBA::Boolean release() const {return m_release;}\n\n");
        buffer.append("\t  char** get_buffer(CORBA::Boolean orphan = false);\n");
        buffer.append("\t  const char* const* get_buffer() const;\n");
        //buffer.append("\t {\n"); // DAVV - implementado en cpp
        //buffer.append("\t \tif(!m_buffer)\n");
        //buffer.append("\t \t\tm_buffer = (const_cast("+name+"
        // *)(this))->allocbuf();\n");
        //buffer.append("\t return (const "+internalType+"*) m_buffer;\n");
        //buffer.append("\t }\n");
        buffer.append("\t  void replace(CORBA::ULong length, char** data, CORBA::Boolean release = false);\n\n");
        buffer.append("\t  static char** allocbuf();\n");
        buffer.append("\t  static void freebuf(char** buf);\n");
        buffer.append("\n\tprivate:\n\n");
        //buffer.append("\t void freebuf();\n\n");
        buffer.append("\t  char** m_buffer;\n");
        buffer.append("\t  CORBA::Boolean m_release;\n");
        buffer.append("\t  CORBA::ULong m_length;\n");
        buffer.append("\t  static const CORBA::ULong m_bound=" + size + ";\n");
        buffer.append("\t  ::TIDorb::types::String_SequenceMember m_member;\n");
        buffer.append("\n\t}; // \n\n");

    }

    private static void generateHppBoundedSequence(StringBuffer buffer,
                                                   String internalType,
                                                   String name, String size, Element el)
    {
        /*
        ArrayList objectNameSeqFirstElement = new ArrayList();
        boolean isEnum = false;       

   		Element root = el.getOwnerDocument().getDocumentElement();
   		String my_enum = XmlType2Cpp.getSequenceType(el,name);
   		NodeList enums = root.getElementsByTagName(OMG_enum);
   		for (int k = 0; k < enums.getLength();k++){
   			Element en = (Element) enums.item(k);
   			if (en.getAttribute(OMG_scoped_name).equals(my_enum)){
   				 isEnum = true;
   				 NodeList subchild = en.getElementsByTagName(OMG_enumerator);
   				 Element child = (Element) subchild.item(0);
   				 // Fix to bug #304	java.lang.IndexOutOfBoundsException with a enum sequence
   				 //objectNameSeqFirstElement.add (k, child.getAttribute(OMG_name));
   				objectNameSeqFirstElement.add(child.getAttribute(OMG_name));
   			}
   		}
        */
    	buffer.append("class " + name + "{ // Bounded Sequence\n");
        buffer.append("\n\tpublic:\n\n");
        buffer.append("\t  "
                + name
                + "() : m_buffer(");
//         if (isEnum){
//         	int z = 0;
//             while (z < objectNameSeqFirstElement.size()){
//             	buffer.append(objectNameSeqFirstElement.get(z) + ")");
//     	        z++;
//             }
//             buffer.append(", m_release(true), m_length(0) {}\n");
//        } else {
    	    buffer.append("NULL), m_release(true), m_length(0) {}\n");	
            //       }
        
        buffer.append("\t  " + name + "(CORBA::ULong length, " + internalType
                      + "* data, CORBA::Boolean release = false)\n");
        buffer
            .append("\t  : m_buffer(data), m_release(release),	m_length(length) {}\n");
        buffer.append("\t  " + name + "(const " + name + "& other);\n");
        buffer.append("\t  ~" + name + "();\n");
        //buffer.append("\t {\n"); // DAVV - implementado en cpp
        //buffer.append("\t \tif(m_release)\n");
        //buffer.append("\t \tfreebuf(m_buffer);\n");
        //buffer.append("\t }\n");
        buffer.append("\t  " + name + "& operator=(const " + name + "&);\n\n");
        buffer.append("\t  CORBA::ULong maximum() const {return m_bound;}\n");
        buffer.append("\t  void length(CORBA::ULong v);\n");
        buffer.append("\t  CORBA::ULong length() const {return m_length;}\n");
        //buffer.append("\t "+internalType+"& operator[](CORBA::ULong index)
        // {return m_buffer[index];}\n");
        buffer.append("\t  " + internalType
                      + "& operator[](CORBA::ULong index);\n");
        //buffer.append("\t const "+internalType+"& operator[](CORBA::ULong
        // index) const {return (const "+internalType+"&) m_buffer[index];}\n");
        buffer.append("\t  const " + internalType
                      + "& operator[](CORBA::ULong index) const;\n");
        buffer
            .append("\t  CORBA::Boolean release() const {return m_release;}\n\n");
        buffer.append("\t  " + internalType
                      + "* get_buffer(CORBA::Boolean orphan = false);\n");
        buffer.append("\t  const " + internalType + "* get_buffer() const;\n");
        buffer.append("\t  void replace(CORBA::ULong length, " + internalType
                      + "* data, CORBA::Boolean release = false);\n\n");
        buffer.append("\t  static " + internalType + "* allocbuf();\n");
        buffer.append("\t  static void freebuf(" + internalType + "* buf);\n");
        buffer.append("\n\tprivate:\n\n");
        //buffer.append("\t void freebuf();\n\n");
        buffer.append("\t  " + internalType + "* m_buffer;\n");
        buffer.append("\t  CORBA::Boolean m_release;\n");
        buffer.append("\t  CORBA::ULong m_length;\n");
        buffer.append("\t  static const CORBA::ULong m_bound=" + size + ";\n");
        buffer.append("}; // \n");

    }

    private static void generateHppBoundedWStringSequence(StringBuffer buffer,
                                                          String name,
                                                          String size)
    {
        buffer.append("class " + name + "{ // Bounded WString Sequence\n");
        buffer.append("\n\tpublic:\n\n");
        buffer.append("\t  "
                    + name
                    + "() : m_buffer(NULL), m_release(true), m_length(0), m_member(&m_release) {}\n");
        buffer.append("\t  "
                    + name
                    + "(CORBA::ULong length, CORBA::WChar** data, CORBA::Boolean release = false)\n");
        buffer.append("\t  : m_buffer(data), m_release(release), m_length(length), m_member(&m_release) {}\n");
        buffer.append("\t  " + name + "(const " + name + "& other);\n");
        buffer.append("\t  ~" + name + "();\n");
        //buffer.append("\t {\n"); // DAVV - implementado en cpp
        //buffer.append("\t \tif(m_release)\n");
        //buffer.append("\t \tfreebuf(m_buffer);\n");
        //buffer.append("\t }\n");
        buffer.append("\t  " + name + "& operator=(const " + name + "&);\n\n");
        buffer.append("\t  CORBA::ULong maximum() const {return m_bound;}\n");
        buffer.append("\t  void length(CORBA::ULong v);\n");
        buffer.append("\t  CORBA::ULong length() const {return m_length;}\n");
        //buffer.append("\t char*& operator[](CORBA::ULong index);\n");
        buffer.append("\t  ::TIDorb::types::WString_SequenceMember& operator[](CORBA::ULong index);\n");
        //buffer.append("\t const "+internalType+"& operator[](CORBA::ULong
        // index) const {return (const "+internalType+"&) m_buffer[index];}\n");
        buffer.append("\t  const CORBA::WChar*& operator[](CORBA::ULong index) const;\n");
        buffer.append("\t  CORBA::Boolean release() const {return m_release;}\n\n");
        buffer.append("\t  CORBA::WChar** get_buffer(CORBA::Boolean orphan = false);\n");
        buffer.append("\t  const CORBA::WChar* const* get_buffer() const;\n");
        //buffer.append("\t {\n"); // DAVV - implementado en cpp
        //buffer.append("\t \tif(!m_buffer)\n");
        //buffer.append("\t \t\tm_buffer = (const_cast("+name+"
        // *)(this))->allocbuf();\n");
        //buffer.append("\t return (const "+internalType+"*) m_buffer;\n");
        //buffer.append("\t }\n");
        buffer
            .append("\t  void replace(CORBA::ULong length, CORBA::WChar** data, CORBA::Boolean release = false);\n\n");
        buffer.append("\t  static CORBA::WChar** allocbuf();\n");
        buffer.append("\t  static void freebuf(CORBA::WChar** buf);\n");
        buffer.append("\n\tprivate:\n\n");
        //buffer.append("\t void freebuf();\n\n");
        buffer.append("\t  CORBA::WChar** m_buffer;\n");
        buffer.append("\t  CORBA::Boolean m_release;\n");
        buffer.append("\t  CORBA::ULong m_length;\n");
        buffer.append("\t  static const CORBA::ULong m_bound=" + size + ";\n");
        buffer
            .append("\t  ::TIDorb::types::WString_SequenceMember m_member;\n");
        buffer.append("\n\t}; // \n\n");

    }

    private static void generateHppBoundedInterfaceSequence(
                                                            StringBuffer buffer,
                                                            String internalType,
                                                            String name,
                                                            String size,
                                                            boolean isValuetype)
    {
        //String shortName = XmlType2Cpp.getUnrolledNameWithoutPackage(name);
        String definition = "Interface";
        if (isValuetype)
            definition = "Valuetype";

        buffer.append("typedef ::TIDorb::templates::" + definition
                      + "T_ptr_SequenceMember<" + internalType + "> _"
                      + /* shortName */name + "_Member;\n\n");

        buffer.append("class " + name + "{ // Bounded " + definition
                      + " Sequence\n");
        buffer.append("\n\tpublic:\n\n");
        buffer.append("\t  "
                    + name
                    + "() : m_buffer(NULL), m_release(false), m_length(0), m_member(&m_release) {}\n");
        buffer.append("\t  " + name + "(CORBA::ULong length, " + internalType
                      + "_ptr* data, CORBA::Boolean release = false)\n");
        buffer.append("\t  : m_buffer(data), m_release(release), m_length(length), m_member(&m_release) {}\n");
        buffer.append("\t  " + name + "(const " + name + "& other);\n");
        buffer.append("\t  ~" + name + "();\n");
        //buffer.append("\t{\n"); // DAVV - implementado en cpp
        //buffer.append("\t\tif(m_release)\n");
        //buffer.append("\t\t\tfreebuf(m_buffer);\n");
        //buffer.append("\t} \n");
        buffer.append("\t  " + name + "& operator=(const " + name + "&);\n\n");
        buffer.append("\t  CORBA::ULong maximum() const {return m_bound;}\n");
        buffer.append("\t  void length(CORBA::ULong v);\n");
        buffer.append("\t  CORBA::ULong length() const {return m_length;}\n");
        //buffer.append("\t"+internalType+"*& operator[](CORBA::ULong index)
        // {return m_buffer[index];}\n");
        //buffer.append("\t "+internalType+"_ptr& operator[](CORBA::ULong
        // index);\n");
        buffer.append("\t  _" + /* shortName */name
                      + "_Member& operator[](CORBA::ULong index);\n");
        //buffer.append("\tconst "+internalType+"*& operator[](CORBA::ULong
        // index) const {return (const "+internalType+"*&)
        // m_buffer[index];}\n");
        buffer.append("\t  const " + internalType
                      + "_ptr& operator[](CORBA::ULong index) const;\n");
        buffer
            .append("\t  CORBA::Boolean release() const {return m_release;}\n\n");
        buffer.append("\t  " + internalType
                      + "_ptr* get_buffer(CORBA::Boolean orphan = false);\n");
        buffer.append("\t  const " + internalType
                      + "_ptr* get_buffer() const;\n");
        /*
         * buffer.append("\t{\n"); DAVV - implementado en cpp
         * buffer.append("\t\tif(!m_buffer)\n"); buffer.append("\t\t\tm_buffer =
         * (const_cast("+name+" *)(this))->allocbuf();\n");
         * buffer.append("\t\treturn (const "+internalType+"**) m_buffer;\n");
         * buffer.append("\t}\n");
         */
        buffer.append("\t  void replace(CORBA::ULong length, " + internalType
                      + "_ptr* data, CORBA::Boolean release = false);\n\n");
        buffer.append("\t  static " + internalType + "_ptr* allocbuf();\n");
        buffer.append("\t  static void freebuf(" + internalType + "_ptr*);\n");
        buffer.append("\n\tprivate:\n\n");
        buffer.append("\t  " + internalType + "_ptr* m_buffer;\n");
        buffer.append("\t  CORBA::Boolean m_release;\n");
        buffer.append("\t  CORBA::ULong m_length;\n");
        buffer.append("\t  static const CORBA::ULong m_bound=" + size + ";\n");
        buffer.append("\t  _" + /* shortName */name + "_Member m_member;\n");
        buffer.append("\n\t}; // \n\n");
    }

    private static void generateHppStringSequence(StringBuffer buffer,
                                                  String name)
    {
        buffer.append("class " + name + "{ // String Sequence\n");

        buffer.append("\n\tpublic:\n\n");

        //buffer.append("//\t friend class
        // ::TIDorb::types::String_SequenceMember;\n\n");

        buffer.append("\t  "
                    + name
                    + "() : m_buffer(NULL), m_release(true), m_length(0), m_max_length(0), m_member(&m_release){}\n");
        buffer.append("\t  "
                    + name
                    + "(CORBA::ULong max): m_buffer(NULL), m_release(true), m_length(0), m_max_length(max), m_member(&m_release){}\n");
        buffer.append("\t  "
                    + name
                    + "(CORBA::ULong max, CORBA::ULong length, char** data, CORBA::Boolean release = false)\n");
        buffer.append("\t  : m_buffer(data), m_release(release),	m_length(length),  m_max_length(max), m_member(&m_release) {}\n");
        buffer.append("\t  " + name + "(const " + name + "& other);\n");
        buffer.append("\t  ~" + name + "();\n");
        //buffer.append("\t {\n");
        //buffer.append("\t\tfreebuf();\n");
        //buffer.append("\t } \n");
        buffer.append("\t  " + name + "& operator=(const " + name + "&);\n\n");

        buffer.append("\t  CORBA::ULong maximum() const {return m_max_length;}\n");
        buffer.append("\t  void length(CORBA::ULong v);\n");
        buffer.append("\t  CORBA::ULong length() const {return m_length;}\n");
        //buffer.append("\t char*& operator[](CORBA::ULong index);\n");
        buffer.append("\t  ::TIDorb::types::String_SequenceMember& operator[](CORBA::ULong index);\n");
        buffer.append("\t  const char*& operator[](CORBA::ULong index) const;\n");
        buffer.append("\t  CORBA::Boolean release() const {return m_release;}\n\n");

        buffer.append("\t  char** get_buffer(CORBA::Boolean orphan = false);\n");
        buffer.append("\t  const char* const* get_buffer() const;\n"); 
        //  introducido 'const' entre 'char*' y '*'
        buffer.append("\t  void replace(CORBA::ULong max, CORBA::ULong length, char** data, CORBA::Boolean release = false);\n\n");

        buffer.append("\t  static char** allocbuf(CORBA::ULong size);\n");
        //buffer.append("\t {\n");
        //buffer.append("\t \treturn new char* [size];\n");
        //buffer.append("\t }\n");
        buffer.append("\t  static void freebuf(char** buf);\n");
        //buffer.append("\t {\n");
        //buffer.append("\t \tdelete[] buf;\n");
        //buffer.append("\t }\n");

        buffer.append("\n\tprivate:\n\n");

        buffer.append("\t  void allocbuf();\n");
        buffer.append("\t  void freebuf();\n");
        buffer.append("\t  char** m_buffer;\n");
        buffer.append("\t  CORBA::Boolean m_release;\n");
        buffer.append("\t  CORBA::ULong m_length;\n");
        buffer.append("\t  CORBA::ULong m_max_length;\n");
        buffer.append("\t  ::TIDorb::types::String_SequenceMember m_member;\n");
        buffer.append("\t}; //\n");
    }

    private static void generateHppWStringSequence(StringBuffer buffer,
                                                   String name)
    {
        buffer.append("class " + name + "{ // Wide String Sequence \n");

        buffer.append("\n\tpublic:\n\n");

        buffer.append("\t  "
                    + name
                    + "() : m_buffer(NULL), m_release(true), m_length(0), m_max_length(0), m_member(&m_release){}\n");
        buffer.append("\t  "
                    + name
                    + "(CORBA::ULong max): m_buffer(NULL), m_release(true), m_length(0), m_max_length(max), m_member(&m_release){}\n");
        buffer.append("\t  "
                    + name
                    + "(CORBA::ULong max, CORBA::ULong length, CORBA::WChar** data, CORBA::Boolean release = false)\n");
        buffer.append("\t  : m_buffer(data), m_release(release),	m_length(length),  m_max_length(max), m_member(&m_release) {}\n");
        buffer.append("\t  " + name + "(const " + name + "& other);\n");
        buffer.append("\t  ~" + name + "();\n");
        //buffer.append("\t {\n");
        //buffer.append("\t\tfreebuf();\n");
        //buffer.append("\t } \n");
        buffer.append("\t  " + name + "& operator=(const " + name + "&);\n\n");

        buffer.append("\t  CORBA::ULong maximum() const {return m_max_length;}\n");
        buffer.append("\t  void length(CORBA::ULong v);\n");
        buffer.append("\t  CORBA::ULong length() const {return m_length;}\n");
        //buffer.append("\t char*& operator[](CORBA::ULong index);\n");
        buffer.append("\t  ::TIDorb::types::WString_SequenceMember& operator[](CORBA::ULong index);\n");
        buffer.append("\t  const CORBA::WChar*& operator[](CORBA::ULong index) const;\n");
        buffer.append("\t  CORBA::Boolean release() const {return m_release;}\n\n");

        buffer.append("\t  CORBA::WChar** get_buffer(CORBA::Boolean orphan = false);\n");
        buffer.append("\t  const CORBA::WChar* const* get_buffer() const;\n"); // DAVV
                                                                               // -
                                                                               // introducido
                                                                               // 'const'
                                                                               // entre
                                                                               // 'CORBA::WChar*'
                                                                               // y
                                                                               // '*'
        buffer.append("\t  void replace(CORBA::ULong max, CORBA::ULong length, CORBA::WChar** data, CORBA::Boolean release = false);\n\n");

        buffer.append("\t  static CORBA::WChar** allocbuf(CORBA::ULong size);\n");
        //buffer.append("\t {\n");
        //buffer.append("\t \treturn new CORBA::WChar* [size];\n");
        //buffer.append("\t }\n");
        buffer.append("\t  static void freebuf(CORBA::WChar** buf);\n");
        //buffer.append("\t {\n");
        //buffer.append("\t \tdelete[] buf;\n");
        //buffer.append("\t }\n");

        buffer.append("\n\tprivate:\n\n");

        buffer.append("\t  void allocbuf();\n");
        buffer.append("\t  void freebuf();\n");
        buffer.append("\t  CORBA::WChar** m_buffer;\n");
        buffer.append("\t  CORBA::Boolean m_release;\n");        
        buffer.append("\t  CORBA::ULong m_length;\n"); 
        buffer.append("\t  CORBA::ULong m_max_length;\n");
        buffer.append("\t  ::TIDorb::types::WString_SequenceMember m_member;\n");
        buffer.append("\t}; //\n");

    }

    private static void generateHppSequence(StringBuffer buffer,
                                            String internalType, String name, Element el)
    {
        ArrayList objectNameSeqFirstElement = new ArrayList();
/*        boolean isEnum = false;       

    		Element root = el.getOwnerDocument().getDocumentElement();
    		NodeList enums = root.getElementsByTagName(OMG_enum);
    		for (int k = 0; k < enums.getLength();k++){
    			Element en = (Element) enums.item(k);
    			if (en.getAttribute(OMG_scoped_name).equals("::" + internalType)){
    				 isEnum = true;
    				 NodeList subchild = en.getElementsByTagName(OMG_enumerator);
    				 Element child = (Element) subchild.item(0);
    				 objectNameSeqFirstElement.add (child.getAttribute(OMG_name));
    			}
    		}*/
        

    	
    	
        buffer.append("class " + name + "{ // Sequence \n\n");

        buffer.append("\tpublic:\n\n");

        buffer.append("\t  "
                    + name
                    + "() : m_buffer(NULL), m_release(true), m_length(0), m_max_length(0) {}\n");
/*                    + "() : m_buffer(");
        if (isEnum){
        int z = 0;
        while (z < objectNameSeqFirstElement.size()){
        		buffer.append(objectNameSeqFirstElement.get(z) + ")");
        	z++;
        }
        buffer.append(", m_release(true), m_length(0), m_max_length(0) {}\n");
        }else {
        	buffer.append("NULL), m_release(true), m_length(0), m_max_length(0) {}\n");	
        }*/
        
        
        buffer.append("\t  "
                    + name
                    + "(CORBA::ULong max): m_buffer(NULL), m_release(true), m_length(0), m_max_length(max){}\n");
        buffer.append("\t  " + name + "(CORBA::ULong max, CORBA::ULong length, "
                      + XmlType2Cpp.basicMapping(internalType)
                      + "* data, CORBA::Boolean release = false)\n");
        buffer.append("\t  : m_buffer(data), m_release(release),	m_length(length),  m_max_length(max) {}\n");
        buffer.append("\t  " + name + "(const " + name + "& other);\n");
        buffer.append("\t  ~" + name + "();\n");
        buffer.append("\t  " + name + "& operator=(const " + name + "&);\n\n");
        buffer.append("\t  CORBA::ULong maximum() const {return m_max_length;}\n");
        buffer.append("\t  void length(CORBA::ULong v);\n");
        buffer.append("\t  CORBA::ULong length() const {return m_length;}\n");
        buffer.append("\t  " + XmlType2Cpp.basicMapping(internalType)
                      + "& operator[](CORBA::ULong index);\n");
        buffer.append("\t  const " + XmlType2Cpp.basicMapping(internalType)
                      + "& operator[](CORBA::ULong index) const;\n");
        buffer.append("\t  CORBA::Boolean release() const {return m_release;}\n\n");
        buffer.append("\t  " + XmlType2Cpp.basicMapping(internalType)
                      + "* get_buffer(CORBA::Boolean orphan = false);\n");
        buffer.append("\t  const " + internalType + "* get_buffer() const;\n");
        buffer.append("\t  void replace(CORBA::ULong max, CORBA::ULong length, "
                    + XmlType2Cpp.basicMapping(internalType)
                    + "* data, CORBA::Boolean release = false);\n\n");
        buffer.append("\t  static " + XmlType2Cpp.basicMapping(internalType)
                      + "* allocbuf(CORBA::ULong size);\n");
        buffer.append("\t  static void freebuf(" + XmlType2Cpp.basicMapping(internalType) + "* buf);\n");

        buffer.append("\n\tprivate:\n\n");

        buffer.append("\t  void allocbuf();\n");
        buffer.append("\t  void freebuf();\n");
        buffer.append("\t  " + XmlType2Cpp.basicMapping(internalType) + "* m_buffer;\n");
        buffer.append("\t  CORBA::Boolean m_release;\n");
        buffer.append("\t  CORBA::ULong m_length;\n");
        buffer.append("\t  CORBA::ULong m_max_length;\n");

        buffer.append("\n\t}; //\n\n");
    }

    private static void generateHppInterfaceSequence(StringBuffer buffer,
                                                     String internalType,
                                                     String name,
                                                     boolean isValuetype)
    {
        //String shortName = XmlType2Cpp.getUnrolledNameWithoutPackage(name);
        String definition = "Interface";
        if (isValuetype)
            definition = "Valuetype";

        buffer.append("typedef ::TIDorb::templates::" + definition
                      + "T_ptr_SequenceMember<" + internalType + "> _"
                      + /* shortName */name + "_Member;\n\n");

        buffer.append("class " + name + "{ // " + definition + " Sequence\n");
        buffer.append("\n\tpublic:\n\n");
        buffer.append("\t  "
                    + name
                    + "() : m_buffer(NULL), m_release(true), m_length(0), m_max_length(0), m_member(&m_release) {}\n");
        buffer.append("\t  "
                    + name
                    + "(CORBA::ULong max): m_buffer(NULL), m_release(true), m_length(0), m_max_length(max), m_member(&m_release){}\n");
        buffer.append("\t  " + name
                      + "(CORBA::ULong max, CORBA::ULong length, "
                      + internalType
                      + "_ptr* data, CORBA::Boolean release = false)\n");
        buffer.append("\t  : m_buffer(data), m_release(release), m_length(length), m_max_length(max), m_member(&m_release) {}\n");
        buffer.append("\t  " + name + "(const " + name + "& other);\n");
        buffer.append("\t  ~" + name + "();\n");
        buffer.append("\t  " + name + "& operator=(const " + name + "&);\n\n");
        buffer.append("\t  CORBA::ULong maximum() const {return m_max_length;}\n");
        buffer.append("\t  void length(CORBA::ULong v);\n");
        buffer.append("\t  CORBA::ULong length() const {return m_length;}\n");
        //buffer.append(" \t"+internalType+"_ptr& operator[](CORBA::ULong
        // index) {return m_buffer[index];}\n");
        //buffer.append("\t "+internalType+"_ptr& operator[](CORBA::ULong
        // index);\n");
        buffer.append("\t  _" + /* shortName */name
                      + "_Member& operator[](CORBA::ULong index);\n");
        //buffer.append(" \tconst "+internalType+"_ptr& operator[](CORBA::ULong
        // index) const {return (const "+internalType+"_ptr&)
        // m_buffer[index];}\n");
        buffer.append("\t  const " + internalType
                      + "_ptr& operator[](CORBA::ULong index) const;\n");
        buffer.append("\t  CORBA::Boolean release() const {return m_release;}\n\n");
        buffer.append("\t  " + internalType
                      + "_ptr* get_buffer(CORBA::Boolean orphan = false);\n");
        buffer.append("\t  const " + internalType
                      + "_ptr* get_buffer() const; \n");
        buffer.append("\t  void replace(CORBA::ULong max, CORBA::ULong length, "
                    + internalType
                    + "_ptr* data, CORBA::Boolean release = false);\n\n");
        buffer.append("\t  static " + internalType
                      + "_ptr* allocbuf(CORBA::ULong size);\n");
        buffer.append("\t  static void freebuf(" + internalType
                      + "_ptr* buf);\n");
        buffer.append("\n\tprivate:\n\n");
        buffer.append("\t  void allocbuf();\n");
        buffer.append("\t  void freebuf();\n");
        buffer.append("\t  " + internalType + "_ptr* m_buffer;\n");
        buffer.append("\t  CORBA::Boolean m_release;\n");
        buffer.append("\t  CORBA::ULong m_length;\n");
        buffer.append("\t  CORBA::ULong m_max_length;\n");
        buffer.append("\t  _" + /* shortName */name + "_Member m_member;\n");
        buffer.append("\n\t}; //\n\n");
    }

    private static void generateCppBoundedInterfaceSequence(
                                StringBuffer buffer,
                                String internalType,
                                String nameWithPackage,
                                boolean isValuetype)
    {
        String forConstructor;
        String sequenceMember;
        if (nameWithPackage.lastIndexOf("::") >= 0) {
            forConstructor = 
                nameWithPackage.substring(nameWithPackage.lastIndexOf("::") + 2);
            sequenceMember = 
                nameWithPackage.substring(0, nameWithPackage.lastIndexOf("::"))
                + "::_" + forConstructor + "_Member";
        } else {
            forConstructor = nameWithPackage;
            sequenceMember = "_" + forConstructor + "_Member";
        }
        buffer.append(nameWithPackage
                    + "::"
                    + forConstructor
                    + "(const "
                    + nameWithPackage
                    + "& other): m_release(true),m_length(other.m_length), m_member(&m_release)\n");
        buffer.append("{\n");
        buffer.append("\tm_buffer =allocbuf();\n");
        buffer.append("\tfor(CORBA::ULong i = 0; i < m_length; i++)\n");
        if (isValuetype)
            buffer.append("\t\tm_buffer[i] = " + internalType
                          + "::_downcast(other.m_buffer[i]->_add_ref());\n");
        else
            buffer.append("\t\tm_buffer[i] = " + internalType
                          + "::_duplicate(other.m_buffer[i]);\n");
        buffer.append("}\n");

        buffer.append(nameWithPackage + "::~" + forConstructor + "()\n");
        buffer.append("{\n");
        buffer.append("\tif (m_release && m_buffer) {\n");
        buffer.append("\t\tfor(CORBA::ULong i = 0; i < m_length; i++)\n");
        if (isValuetype)
            buffer.append("\t\t\tm_buffer[i]->_remove_ref();\n");
        else
            buffer.append("\t\t\tCORBA::release(m_buffer[i]);\n");
        buffer.append("\t\tfreebuf(m_buffer);\n");
        buffer.append("\t}\n");
        buffer.append("}\n");

        buffer.append(nameWithPackage + "& " + nameWithPackage
                      + "::operator=(const " + nameWithPackage + "& other)\n");
        buffer.append("{\n");
        buffer.append("\tif(this == &other)\n");
        buffer.append("\t\treturn *this;\n");
        buffer.append("\tif(m_release && m_buffer) {\n");
        buffer.append("\t\tfor(CORBA::ULong i = 0; i < m_length; i++)\n");
        if (isValuetype)
            buffer.append("\t\t\tm_buffer[i]->_remove_ref();\n");
        else
            buffer.append("\t\t\tCORBA::release(m_buffer[i]);\n");
        buffer.append("\t\tfreebuf(m_buffer);\n");
        buffer.append("\t}\n");
        buffer.append("\tm_release = true;\n");
        buffer.append("\tm_length = other.m_length;\n");
        buffer.append("\tm_buffer = allocbuf();\n");
        buffer.append("\tfor(CORBA::ULong i = 0; i < m_length; i++)\n");
        if (isValuetype)
            buffer.append("\t\tm_buffer[i] = " + internalType
                          + "::_downcast(other.m_buffer[i]->_add_ref());\n");
        else
            buffer.append("\t\tm_buffer[i] = " + internalType
                          + "::_duplicate(other.m_buffer[i]);\n");
        buffer.append("\treturn *this;\n");
        buffer.append("}\n");

        buffer.append("void " + nameWithPackage + "::length(CORBA::ULong v)\n");
        buffer.append("{\n");
        buffer.append("\tif(!m_buffer) {\n");
        buffer.append("\t\tm_buffer = allocbuf();\n");
        buffer.append("\t\tm_release = true;\n");
        buffer.append("\t}\n");
        buffer.append("\tCORBA::ULong new_length = (v > m_bound)? m_bound : v;\n");
        buffer.append("\tif(m_length > new_length) {\n");
        buffer.append("\t\tfor(CORBA::ULong i = new_length; i < m_length; i++)\n");
        if (isValuetype)
            buffer.append("\t\t\tm_buffer[i]->_remove_ref();\n");
        else
            buffer.append("\t\t\tCORBA::release(m_buffer[i]);\n");
        buffer.append("\t} else if(m_length < new_length) {\n");
        buffer.append("\t\tfor(CORBA::ULong i = m_length; i < new_length; i++)\n");
        if (isValuetype)
            buffer.append("\t\t\tm_buffer[i] = NULL;\n");
        else
            buffer
                .append("\t\t\tm_buffer[i] = " + internalType + "::_nil();\n");
        buffer.append("\t}\n");
        buffer.append("\tm_length = new_length;\n");
        buffer.append("}\n");

        //buffer.append(internalType + "_ptr& " + nameWithPackage +
        // "::operator[](CORBA::ULong index)\n");
        buffer.append(sequenceMember + "& " + nameWithPackage
                      + "::operator[](CORBA::ULong index)\n");
        buffer.append("{\n");
        buffer.append("\tif (index < m_length) {\n");
        buffer.append("\t\tm_member.asignar_ptr(m_buffer+index);\n");
        buffer.append("\t\treturn m_member;\n\t}\n");
        buffer.append("\telse\n");
        buffer.append("\t\tthrow CORBA::BAD_PARAM(\"Current sequence length exceeded\");\n ");
        buffer.append("}\n");

        buffer.append("const " + internalType + "_ptr& " + nameWithPackage
                      + "::operator[] (CORBA::ULong index) const\n");
        buffer.append("{\n");
        buffer.append("\tif (index < m_length)\n");
        buffer.append("\t\treturn (const " + internalType
                      + "_ptr&) m_buffer[index];\n");
        buffer.append("\telse\n");
        buffer.append("\t\tthrow CORBA::BAD_PARAM(\"Current sequence length exceeded\");\n");
        buffer.append("}\n");

        buffer.append(internalType + "_ptr* " + nameWithPackage
                      + "::get_buffer(CORBA::Boolean orphan)\n");
        buffer.append("{\n");
        buffer.append("\tif(!m_buffer)\n");
        buffer.append("\t{\n");
        buffer.append("\t\tm_buffer = allocbuf();\n");
        buffer.append("\t\tm_release = true;\n");
        buffer.append("\t}\n");
        buffer.append("\tif(orphan) {\t // caller assumes ownership\n");
        buffer.append("\t\t" + internalType + "_ptr* aux;\n");
        buffer.append("\t\tif(!m_release)\n"); // condicion invertida
                                               // (vease mapping)
        buffer.append("\t\t\taux = NULL;\n");
        buffer.append("\t\telse {\n");
        buffer.append("\t\t\taux = m_buffer;\n");
        buffer.append("\t\t}\n");
        buffer.append("\t\tm_release = true;\n"); // debe quedar como con
                                                  // constructor por defecto
        buffer.append("\t\tm_buffer = NULL;\n");
        buffer.append("\t\tm_length = 0;\n");
        buffer.append("\t\treturn aux;\n");
        buffer.append("\t}\n");
        buffer.append("\treturn m_buffer;\n");
        buffer.append("}\n");

        buffer.append("const " + internalType + "_ptr* " + nameWithPackage
                      + "::get_buffer() const\n");
        buffer.append("{\n");
        buffer.append("\tif(!m_buffer)\n");
        buffer.append("\t{\n");
        //buffer.append("\t\tm_buffer = (const_cast< "+nameWithPackage+"* >
        // (this))->allocbuf();\n");
        buffer.append("\t\t(const_cast< " + nameWithPackage
                      + "* > (this))->m_buffer = allocbuf();\n");
        //buffer.append("\t\tm_release = true;\n");
        buffer.append("\t\t(const_cast< " + nameWithPackage
                      + "* > (this))->m_release = true;\n");
        buffer.append("\t}\n");
        buffer.append("\treturn (const " + internalType + "_ptr*) m_buffer;\n");
        buffer.append("}\n");

        buffer.append("void " + nameWithPackage
                      + "::replace(CORBA::ULong length," + internalType
                      + "_ptr* data,CORBA::Boolean release)\n");
        buffer.append("{\n");
        buffer.append("\tif(m_release && m_buffer) {\n");
        buffer.append("\t\tfor(CORBA::ULong i = 0; i < m_length; i++)\n");
        if (isValuetype)
            buffer.append("\t\t\tm_buffer[i]->_remove_ref();\n");
        else
            buffer.append("\t\t\tCORBA::release(m_buffer[i]);\n");
        buffer.append("\t\tfreebuf(m_buffer);\n");
        buffer.append("\t}\n");
        buffer.append("\tm_buffer = data;\n");
        buffer.append("\tm_length = length;\n");
        buffer.append("\tm_release = release;\n");
        buffer.append("}\n");

        buffer.append("" + internalType + "_ptr* " + nameWithPackage
                      + "::allocbuf()\n");
        buffer.append("{\n");
        buffer.append("\treturn new " + internalType + "_ptr [m_bound];\n");
        buffer.append("}\n");

        buffer.append("void " + nameWithPackage + "::freebuf(" + internalType
                      + "_ptr* buffer)\n");
        buffer.append("{\n");
        buffer.append("\tdelete[] buffer;\n");
        buffer.append("}\n");
    }

    private static void generateCppBoundedSequence(StringBuffer buffer,
                                                   String internalType,
                                                   String name,
                                                   boolean is_basic_type,
                                                   boolean is_basic_data_type)
    {
        String forConstructor;
        if (name.lastIndexOf("::") >= 0)
            forConstructor = name.substring(name.lastIndexOf("::") + 2);
        else
            forConstructor = name;

        buffer.append(name + "::" + forConstructor + "(const " + name
                      + "& other): m_release(true),m_length(other.m_length)\n");
        buffer.append("{\n");
        buffer.append("\tm_buffer = allocbuf();\n");
        if (is_basic_data_type) {
                        buffer.append("\tif (m_length)\n");
                        buffer.append("\t\tmemcpy(m_buffer, other.m_buffer, m_length*sizeof(" + internalType + "));\n");
        } else{
        		buffer.append("\tfor(CORBA::ULong i = 0; i < m_length; i++)\n");
        		buffer.append("\t\tm_buffer[i] = other.m_buffer[i];\n");
        }
        buffer.append("}\n");

        buffer.append(name + "::~" + forConstructor + "()\n");
        buffer.append("{\n");
        buffer.append("\tif (m_release && m_buffer)\n");
        buffer.append("\t\tfreebuf(m_buffer);\n");
        buffer.append("}\n");

        buffer.append(name + "& " + name + "::operator=(const " + name
                      + "& other)\n");
        buffer.append("{\n");
        buffer.append("\tif(this == &other)\n");
        buffer.append("\t\treturn *this;\n");
        buffer.append("\tif(m_release && m_buffer)\n");
        buffer.append("\t\tfreebuf(m_buffer);\n");
        buffer.append("\tm_release = true;\n");
        buffer.append("\tm_length = other.m_length;\n");
        buffer.append("\tm_buffer = allocbuf();\n");
        if (is_basic_data_type) {
                buffer.append("\tif (m_length)\n");
        	buffer.append("\t\tmemcpy(m_buffer, other.m_buffer, m_length*sizeof(" + internalType + "));\n");

        } else{
        		buffer.append("\tfor(CORBA::ULong i = 0; i < m_length; i++)\n");
        		buffer.append("\t\tm_buffer[i] = other.m_buffer[i];\n");
        }
        buffer.append("\treturn *this;\n");
        buffer.append("}\n");

        buffer.append("void " + name + "::length(CORBA::ULong v)\n");
        buffer.append("{\n");
        buffer.append("\tif(!m_buffer)\n");
        buffer.append("\t{\n");
        buffer.append("\t\tm_buffer = allocbuf();\n");
        buffer.append("\t\tm_release = true;\n");
        buffer.append("\t}\n");
        buffer.append("\tCORBA::ULong new_length = (v > m_bound)? m_bound : v;\n");
        // Performance improvements for Octet Sequences: doesn't need initialization
        if (!is_basic_type /* !internalType.equals("CORBA::Octet")*/){
        	//System.out.println("intertalType: " + internalType);
        	buffer.append("\tif(new_length > m_length) {\n");
        	buffer.append("\t\t" + internalType + " _default; \n");
        	buffer.append("\t\tfor(CORBA::ULong i = m_length; i < new_length; i++) {\n");
        	buffer.append("\t\t\tm_buffer[i] = _default;\n");
        	buffer.append("\t\t}\n");
        	buffer.append("\t}\n");
        }       
        buffer.append("\tm_length = new_length;\n");
        buffer.append("} \n");

        buffer.append(internalType + "& " + name
                      + "::operator[](CORBA::ULong index)\n");
        buffer.append("{\n");
        buffer.append("\tif (index < m_length)\n");
        buffer.append("\t\treturn m_buffer[index];\n");
        buffer.append("\telse\n");
        buffer.append("\t\tthrow CORBA::BAD_PARAM(\"Current sequence length exceeded\");\n ");
        buffer.append("}\n");

        buffer.append("const " + internalType + "& " + name
                      + "::operator[] (CORBA::ULong index) const\n");
        buffer.append("{\n");
        buffer.append("\tif (index < m_length)\n");
        buffer.append("\t\treturn (const " + internalType
                      + "&) m_buffer[index];\n");
        buffer.append("\telse\n");
        buffer.append("\t\tthrow CORBA::BAD_PARAM(\"Current sequence length exceeded\");\n ");
        buffer.append("}\n");

        buffer.append("" + internalType + "* " + name
                      + "::get_buffer(CORBA::Boolean orphan)\n");
        buffer.append("{\n");
        buffer.append("\tif(!m_buffer)\n");
        buffer.append("\t{\n");
        buffer.append("\t\tm_buffer = allocbuf();\n");
        buffer.append("\t\tm_release = true;\n");
        buffer.append("\t}\n");
        buffer.append("\tif(orphan) {\t // caller assumes ownership\n");
        buffer.append("\t\t" + internalType + "* aux;\n");
        buffer.append("\t\tif(!m_release)\n"); // DAVV - condicion invertida
                                               // (vease mapping)
        buffer.append("\t\t\taux = NULL;\n");
        buffer.append("\t\telse {\n");
        buffer.append("\t\t\taux = m_buffer;\n");
        buffer.append("\t\t}\n");
        buffer.append("\t\tm_release = true;\n"); // DAVV - debe quedar como con
                                                  // constructor por defecto
        buffer.append("\t\tm_buffer = NULL;\n");
        buffer.append("\t\tm_length = 0;\n");
        buffer.append("\t\treturn aux;\n");
        buffer.append("\t}\n");
        buffer.append("\treturn m_buffer;\n");
        buffer.append("}\n");

        buffer.append("const " + internalType + "* " + name
                      + "::get_buffer() const\n");
        buffer.append("{\n");
        buffer.append("\tif(!m_buffer)\n");
        buffer.append("\t{\n");
        //buffer.append("\t\tm_buffer = (const_cast< "+name+"* >
        // (this))->allocbuf();\n");
        buffer.append("\t\t(const_cast< " + name
                      + "* > (this))->m_buffer = allocbuf();\n");
        //buffer.append("\t\tm_release = true;\n");
        buffer.append("\t\t(const_cast< " + name
                      + "* > (this))->m_release = true;\n");
        buffer.append("\t}\n");
        buffer.append("\treturn (const " + internalType + "*) m_buffer;\n");
        buffer.append("}\n");

        buffer.append("void " + name + "::replace(CORBA::ULong length,"
                      + internalType + "* data,CORBA::Boolean release)\n");
        buffer.append("{\n");
        buffer.append("\tif(m_buffer && m_release)\n");
        buffer.append("\t\tfreebuf(m_buffer);\n");
        buffer.append("\tm_buffer = data;\n");
        buffer.append("\tm_length = length;\n");
        buffer.append("\tm_release = release;\n");
        buffer.append("}\n");

        buffer.append(internalType + "* " + name + "::allocbuf()\n");
        buffer.append("{\n");
        //buffer.append("\tif(m_bound) \n"); // DAVV Si m_bound == 0, hay que
        // reservar un buffer de 0 elementos (mapping)
        buffer.append("\t\treturn new " + internalType + "[m_bound];\n");
        //buffer.append("\telse\n");
        //buffer.append("\t\treturn NULL;\n");
        buffer.append("}\n");

        buffer.append("void " + name + "::freebuf(" + internalType
                      + "* buffer)\n");
        buffer.append("{\n");
        buffer.append("\t\tdelete[] buffer;\n");
        buffer.append("} // \n");

    }

    private static void generateCppBoundedStringSequence(StringBuffer buffer,
                                                         String name)
    {
        String forConstructor;
        if (name.lastIndexOf("::") >= 0)
            forConstructor = name.substring(name.lastIndexOf("::") + 2);
        else
            forConstructor = name;

        buffer.append(name
                    + "::"
                    + forConstructor
                    + "(const "
                    + name
                    + "& other): m_release(true),m_length(other.m_length), m_member(&m_release)\n");
        buffer.append("{\n");
        buffer.append("\tm_buffer =allocbuf();\n");
        buffer.append("\tfor(CORBA::ULong i = 0; i < m_length; i++)\n");
        buffer.append("\t\tm_buffer[i] = CORBA::string_dup(other.m_buffer[i]);\n");
        buffer.append("}\n");

        buffer.append(name + "::~" + forConstructor + "()\n");
        buffer.append("{\n");
        buffer.append("\tif (m_release && m_buffer) {\n");
        buffer.append("\t\tfor(CORBA::ULong i = 0; i < m_length; i++)\n");
        buffer.append("\t\t\tCORBA::string_free(m_buffer[i]);\n");
        buffer.append("\t\tfreebuf(m_buffer);\n");
        buffer.append("\t}\n");
        buffer.append("}\n");

        buffer.append(name + "& " + name + "::operator=(const " + name
                      + "& other) \n");
        buffer.append("{\n");
        buffer.append("\tif(this == &other)\n");
        buffer.append("\t\treturn *this;\n");
        buffer.append("\tif(m_release && m_buffer) {\n");
        buffer.append("\t\tfor(CORBA::ULong i = 0; i < m_length; i++)\n");
        buffer.append("\t\t\tCORBA::string_free(m_buffer[i]);\n");
        buffer.append("\t\tfreebuf(m_buffer);\n");
        buffer.append("\t}\n");
        buffer.append("\tm_release = true;\n");
        buffer.append("\tm_length = other.m_length;\n");
        buffer.append("\tm_buffer = allocbuf();\n");
        buffer.append("\tfor(CORBA::ULong i = 0; i < m_length; i++)\n");
        buffer.append("\t\tm_buffer[i] = CORBA::string_dup(other.m_buffer[i]);\n");
        buffer.append("\treturn *this;\n");
        buffer.append("}\n");

        buffer.append("void " + name + "::length(CORBA::ULong v)\n");
        buffer.append("{\n");
        buffer.append("\tif(!m_buffer) {\n");
        buffer.append("\t\tm_buffer = allocbuf();\n");
        buffer.append("\t\tm_release = true;\n");
        buffer.append("\t}\n");
        buffer.append("\tCORBA::ULong new_length = (v > m_bound)? m_bound : v;\n");
        buffer.append("\tif(m_length > new_length) {\n");
        buffer.append("\t\tfor(CORBA::ULong i = new_length; i < m_length; i++)\n");
        buffer.append("\t\t\tCORBA::string_free(m_buffer[i]);\n");
        buffer.append("\t} else if(m_length < new_length) {\n");
        buffer.append("\t\tfor(CORBA::ULong i = m_length; i < new_length; i++)\n");
        buffer.append("\t\t\tm_buffer[i] = CORBA::string_dup(\"\");\n");
        buffer.append("\t}\n");
        buffer.append("\tm_length = new_length;\n");
        buffer.append("}\n");

        //buffer.append("char*& " + name + "::operator[](CORBA::ULong
        // index)\n");
        buffer.append("::TIDorb::types::String_SequenceMember& " + name
                      + "::operator[](CORBA::ULong index)\n");
        buffer.append("{\n");
        buffer.append("\tif (index < m_length) {\n");
        buffer.append("\t\tm_member.asignar_ptr(m_buffer+index);\n");
        buffer.append("\t\treturn m_member;\n\t}\n");
        buffer.append("\telse\n");
        buffer.append("\t\tthrow CORBA::BAD_PARAM(\"Current sequence length exceeded\");\n ");
        buffer.append("}\n");

        buffer.append("const char*& " + name
                      + "::operator[] (CORBA::ULong index) const\n");
        buffer.append("{\n");
        buffer.append("\tif (index < m_length)\n");
        buffer.append("\t\treturn (const char*&) m_buffer[index];\n");
        buffer.append("\telse\n");
        buffer.append("\t\tthrow CORBA::BAD_PARAM(\"Current sequence length exceeded\");\n ");
        buffer.append("}\n");

        buffer.append("char** " + name
                      + "::get_buffer(CORBA::Boolean orphan)\n");
        buffer.append("{\n");
        buffer.append("\tif(!m_buffer)\n");
        buffer.append("\t{\n");
        buffer.append("\t\tm_buffer = allocbuf();\n");
        buffer.append("\t\tm_release = true;\n");
        buffer.append("\t}\n");
        buffer.append("\tif(orphan) {\t // caller assumes ownership\n");
        buffer.append("\t\tchar** aux;\n");
        buffer.append("\t\tif(!m_release)\n"); // DAVV - condicion invertida
                                               // (vease mapping)
        buffer.append("\t\t\taux = NULL;\n");
        buffer.append("\t\telse {\n");
        buffer.append("\t\t\taux = m_buffer;\n");
        buffer.append("\t\t}\n");
        buffer.append("\t\tm_release = true;\n"); // DAVV - debe quedar como con
                                                  // constructor por defecto
        buffer.append("\t\tm_buffer = NULL;\n");
        buffer.append("\t\tm_length = 0;\n");
        buffer.append("\t\treturn aux;\n");
        buffer.append("\t}\n");
        buffer.append("\treturn m_buffer;\n");
        buffer.append("}\n");

        buffer.append("const char* const* " + name + "::get_buffer() const\n");
        buffer.append("{\n");
        buffer.append("\tif(!m_buffer)\n");
        buffer.append("\t{\n");
        //buffer.append("\t\tm_buffer = (const_cast< "+name+"* >
        // (this))->allocbuf();\n");
        buffer.append("\t\t(const_cast< " + name
                      + "* > (this))->m_buffer = allocbuf();\n");
        //buffer.append("\t\tm_release = true;\n");
        buffer.append("\t\t(const_cast< " + name
                      + "* > (this))->m_release = true;\n");
        buffer.append("\t}\n");
        buffer.append("\treturn (const char* const*) m_buffer;\n");
        buffer.append("}\n");

        buffer.append("void "
                    + name
                    + "::replace(CORBA::ULong length,char** data,CORBA::Boolean release)\n");
        buffer.append("{\n");
        buffer.append("\tif(m_release && m_buffer) {\n");
        buffer.append("\t\tfor(CORBA::ULong i = 0; i < m_length; i++)\n");
        buffer.append("\t\t\tCORBA::string_free(m_buffer[i]);\n");
        buffer.append("\t\tfreebuf(m_buffer);\n");
        buffer.append("\t}\n");
        buffer.append("\tm_buffer = data;\n");
        buffer.append("\tm_length = length;\n");
        buffer.append("\tm_release = release;\n");
        buffer.append("}\n");

        buffer.append("char** " + name + "::allocbuf()\n");
        buffer.append("{\n");
        //buffer.append("\tif(m_bound)\n");
        //buffer.append("\t\tm_buffer = new char* [m_bound];\n");
        //buffer.append("\telse\n");
        //buffer.append("\t\tm_buffer = 0;\n");
        buffer.append("\treturn new char*[m_bound];\n");
        buffer.append("}\n");

        buffer.append("void " + name + "::freebuf(char** buf)\n");
        buffer.append("{\n");
        buffer.append("\tdelete [] buf;\n");
        buffer.append("}\n");

    }

    private static void generateCppBoundedWStringSequence(StringBuffer buffer,
                                                          String name)
    {
        String forConstructor;
        if (name.lastIndexOf("::") >= 0)
            forConstructor = name.substring(name.lastIndexOf("::") + 2);
        else
            forConstructor = name;

        buffer.append(name
                    + "::"
                    + forConstructor
                    + "(const "
                    + name
                    + "& other): m_release(true),m_length(other.m_length), m_member(&m_release)\n");
        buffer.append("{\n");
        buffer.append("\tm_buffer =allocbuf();\n");
        buffer.append("\tfor(CORBA::ULong i = 0; i < m_length; i++)\n");
        buffer.append("\t\tm_buffer[i] = CORBA::wstring_dup(other.m_buffer[i]);\n");
        buffer.append("}\n");

        buffer.append(name + "::~" + forConstructor + "()\n");
        buffer.append("{\n");
        buffer.append("\tif (m_release && m_buffer) {\n");
        buffer.append("\t\tfor(CORBA::ULong i = 0; i < m_length; i++)\n");
        buffer.append("\t\t\tCORBA::wstring_free(m_buffer[i]);\n");
        buffer.append("\t\tfreebuf(m_buffer);\n");
        buffer.append("\t}\n");
        buffer.append("}\n");

        buffer.append(name + "& " + name + "::operator=(const " + name
                      + "& other) \n");
        buffer.append("{\n");
        buffer.append("\tif(this == &other)\n");
        buffer.append("\t\treturn *this;\n");
        buffer.append("\tif(m_release && m_buffer) {\n");
        buffer.append("\t\tfor(CORBA::ULong i = 0; i < m_length; i++)\n");
        buffer.append("\t\t\tCORBA::wstring_free(m_buffer[i]);\n");
        buffer.append("\t\tfreebuf(m_buffer);\n");
        buffer.append("\t}\n");
        buffer.append("\tm_release = true;\n");
        buffer.append("\tm_length = other.m_length;\n");
        buffer.append("\tm_buffer = allocbuf();\n");
        buffer.append("\tfor(CORBA::ULong i = 0; i < m_length; i++)\n");
        buffer.append("\t\tm_buffer[i] = CORBA::wstring_dup(other.m_buffer[i]);\n");
        buffer.append("\treturn *this;\n");
        buffer.append("}\n");

        buffer.append("void " + name + "::length(CORBA::ULong v)\n");
        buffer.append("{\n");
        buffer.append("\tif(!m_buffer) {\n");
        buffer.append("\t\tm_buffer = allocbuf();\n");
        buffer.append("\t\tm_release = true;\n");
        buffer.append("\t}\n");
        buffer.append("\tCORBA::ULong new_length = (v > m_bound)? m_bound : v;\n");
        buffer.append("\tif(m_length > new_length) {\n");
        buffer.append("\t\tfor(CORBA::ULong i = new_length; i < m_length; i++)\n");
        buffer.append("\t\t\tCORBA::wstring_free(m_buffer[i]);\n");
        buffer.append("\t}else if (m_length < new_length) {\n");
        buffer.append("\t\tfor(CORBA::ULong i = m_length; i < new_length; i++)\n");
        buffer.append("\t\t\tm_buffer[i] = CORBA::wstring_dup(L\"\");\n");
        buffer.append("\t}\n");
        buffer.append("\tm_length = new_length;\n");
        buffer.append("}\n");

        //buffer.append("char*& " + name + "::operator[](CORBA::ULong
        // index)\n");
        buffer.append("::TIDorb::types::WString_SequenceMember& " + name
                      + "::operator[](CORBA::ULong index)\n");
        buffer.append("{\n");
        buffer.append("\tif (index < m_length) {\n");
        buffer.append("\t\tm_member.asignar_ptr(m_buffer+index);\n");
        buffer.append("\t\treturn m_member;\n\t}\n");
        buffer.append("\telse\n");
        buffer.append("\t\tthrow CORBA::BAD_PARAM(\"Current sequence length exceeded\");\n ");
        buffer.append("}\n");

        buffer.append("const CORBA::WChar*& " + name
                      + "::operator[] (CORBA::ULong index) const\n");
        buffer.append("{\n");
        buffer.append("\tif (index < m_length)\n");
        buffer.append("\t\treturn (const CORBA::WChar*&) m_buffer[index];\n");
        buffer.append("\telse\n");
        buffer.append("\t\tthrow CORBA::BAD_PARAM(\"Current sequence length exceeded\");\n ");
        buffer.append("}\n");

        buffer.append("CORBA::WChar** " + name
                      + "::get_buffer(CORBA::Boolean orphan)\n");
        buffer.append("{\n");
        buffer.append("\tif(!m_buffer)\n");
        buffer.append("\t{\n");
        buffer.append("\t\tm_buffer = allocbuf();\n");
        buffer.append("\t\tm_release = true;\n");
        buffer.append("\t}\n");
        buffer.append("\tif(orphan) {\t // caller assumes ownership\n");
        buffer.append("\t\tCORBA::WChar** aux;\n");
        buffer.append("\t\tif(!m_release)\n"); // DAVV - condicion invertida
                                               // (vease mapping)
        buffer.append("\t\t\taux = NULL;\n");
        buffer.append("\t\telse {\n");
        buffer.append("\t\t\taux = m_buffer;\n");
        buffer.append("\t\t}\n");
        buffer.append("\t\tm_release = true;\n"); // DAVV - debe quedar como con
                                                  // constructor por defecto
        buffer.append("\t\tm_buffer = NULL;\n");
        buffer.append("\t\tm_length = 0;\n");
        buffer.append("\t\treturn aux;\n");
        buffer.append("\t}\n");
        buffer.append("\treturn m_buffer;\n");
        buffer.append("}\n");

        buffer.append("const CORBA::WChar* const* " + name
                      + "::get_buffer() const\n");
        buffer.append("{\n");
        buffer.append("\tif(!m_buffer)\n");
        buffer.append("\t{\n");
        //buffer.append("\t\tm_buffer = (const_cast< "+name+"* >
        // (this))->allocbuf();\n");
        buffer.append("\t\t(const_cast< " + name
                      + "* > (this))->m_buffer = allocbuf();\n");
        //buffer.append("\t\tm_release = true;\n");
        buffer.append("\t\t(const_cast< " + name
                      + "* > (this))->m_release = true;\n");
        buffer.append("\t}\n");
        buffer.append("\treturn (const CORBA::WChar* const*) m_buffer;\n");
        buffer.append("}\n");

        buffer.append("void "
                    + name
                    + "::replace(CORBA::ULong length,CORBA::WChar** data,CORBA::Boolean release)\n");
        buffer.append("{\n");
        buffer.append("\tif(m_release && m_buffer) {\n");
        buffer.append("\t\tfor(CORBA::ULong i = 0; i < m_length; i++)\n");
        buffer.append("\t\t\tCORBA::wstring_free(m_buffer[i]);\n");
        buffer.append("\t\tfreebuf(m_buffer);\n");
        buffer.append("\t}\n");
        buffer.append("\tm_buffer = data;\n");
        buffer.append("\tm_length = length;\n");
        buffer.append("\tm_release = release;\n");
        buffer.append("}\n");

        buffer.append("CORBA::WChar** " + name + "::allocbuf()\n");
        buffer.append("{\n");
        //buffer.append("\tif(m_bound)\n");
        //buffer.append("\t\tm_buffer = new CORBA::WChar* [m_bound];\n");
        //buffer.append("\telse\n");
        //buffer.append("\t\tm_buffer = 0;\n");
        buffer.append("\treturn new CORBA::WChar*[m_bound];\n");
        buffer.append("}\n");

        buffer.append("void " + name + "::freebuf(CORBA::WChar** buf)\n");
        buffer.append("{\n");
        buffer.append("\tdelete [] buf;\n");
        buffer.append("}\n");

    }

    private static void generateCppInterfaceSequence(StringBuffer buffer,
                                                     String internalType,
                                                     String name,
                                                     boolean isValuetype)
    {
        String forConstructor;
        String sequenceMember;
        if (name.lastIndexOf("::") >= 0) {
            forConstructor = name.substring(name.lastIndexOf("::") + 2);
            sequenceMember = name.substring(0, name.lastIndexOf("::")) + "::_"
                             + forConstructor + "_Member";
        } else {
            forConstructor = name;
            sequenceMember = "_" + forConstructor + "_Member";
        }
        buffer.append(name
                    + "::"
                    + forConstructor
                    + "(const "
                    + name
                    + "& other): m_release(true), m_length(other.m_length), m_max_length(other.m_max_length), m_member(&m_release)\n");
        buffer.append("{\n");
        buffer.append("\tallocbuf();\n");
        buffer.append("\tfor(CORBA::ULong i = 0; i < m_length; i++)\n");
        //buffer.append("\t\tm_buffer[i] = other.m_buffer[i];\n");
        if (isValuetype)
            buffer.append("\t\tm_buffer[i] = " + internalType
                          + "::_downcast((other.m_buffer[i])->_add_ref());\n");
        else
            buffer.append("\t\tm_buffer[i] = " + internalType
                          + "::_duplicate(other.m_buffer[i]);\n");
        buffer.append("}\n");

        buffer.append(name + "::~" + forConstructor + "()\n");
        buffer.append("{\n");
        buffer.append("\tfreebuf();\n");
        buffer.append("}\n");

        buffer.append("" + name + "& " + name + "::operator=(const " + name
                      + "& other)\n");
        buffer.append("{\n");
        buffer.append("\tif(this == &other)\n");
        buffer.append("\t\treturn *this;\n");
        //buffer.append("\tif(m_release)\n"); DAVV - no es necesario
        buffer.append("\tfreebuf();\n");
        buffer.append("\tm_release = true;\n");
        buffer.append("\tm_length = other.m_length;\n");
        buffer.append("\tm_max_length = other.m_max_length;\n");
        buffer.append("\tallocbuf();\n");
        buffer.append("\tfor(CORBA::ULong i = 0; i < m_length; i++)\n");
        //buffer.append("\t\tm_buffer[i] = other.m_buffer[i];\n");
        if (isValuetype)
            buffer.append("\t\tm_buffer[i] = " + internalType
                          + "::_downcast((other.m_buffer[i])->_add_ref());\n");
        else
            buffer.append("\t\tm_buffer[i] = " + internalType
                          + "::_duplicate(other.m_buffer[i]);\n");
        buffer.append("\treturn *this;\n");
        buffer.append("}\n");

        buffer.append("void " + name + "::length(CORBA::ULong v)\n");
        buffer.append("{\n");
        buffer.append("\tif(!m_buffer) {\n");
        buffer.append("\t\tm_release = true;\n");
        buffer.append("\t\tm_length = v;\n");
        buffer.append("\t\tm_max_length = (v>m_max_length)? v : m_max_length;\n");
        buffer.append("\t\tallocbuf();\n");
        buffer.append("\t\tfor(CORBA::ULong i = 0; i < m_length; i++)\n");
        if (isValuetype)
            buffer.append("\t\t\tm_buffer[i] = NULL;\n");
        else
            buffer.append("\t\t\tm_buffer[i] = " + internalType + "::_nil();\n");
        buffer.append("\t\treturn;\n");
        buffer.append("\t}\n");
        buffer.append("\tif(v > m_length) {\n");
        buffer.append("\t\tif(v > m_max_length) {\n");
        buffer.append("\t\t\t" + internalType + "_ptr* aux = new "
                      + internalType + "_ptr[v];\n");
        buffer.append("\t\t\tfor(CORBA::ULong i = 0; i < m_length; i++) {\n");
        //buffer.append("\t\t\t\t{\n");
        if (isValuetype)
            buffer.append("\t\t\t\taux[i] = " + internalType
                          + "::_downcast((m_buffer[i])->_add_ref());\n");
        else
            buffer.append("\t\t\t\taux[i] = " + internalType
                          + "::_duplicate(m_buffer[i]);\n");
        buffer.append("\t\t\t}\n");
        buffer.append("\t\t\tfreebuf();\n");
        buffer.append("\t\t\tm_buffer = aux; \n");
        buffer.append("\t\t\tm_release = true;\n");
        buffer.append("\t\t\tm_max_length = v;\n");
        buffer.append("\t\t}\n");
        buffer.append("\t\tfor(CORBA::ULong i = m_length; i < v; i++)\n");
        if (isValuetype)
            buffer.append("\t\t\tm_buffer[i] = NULL;\n");
        else
            buffer.append("\t\t\tm_buffer[i] = " + internalType + "::_nil();\n");
        buffer.append("\t} else if (v < m_length){\n");
        buffer.append("\t\tfor(CORBA::ULong i = v; i < m_length; i++) \n");
        if (isValuetype)
            buffer.append("\t\t\tm_buffer[i]->_remove_ref();\n");
        else
            buffer.append("\t\t\tCORBA::release(m_buffer[i]);\n");
        buffer.append("\t\t}\n");
        buffer.append("\tm_length = v;\n");
        buffer.append("\treturn ;//*this;\n");
        buffer.append("}\n");

        //buffer.append(internalType + "_ptr& " + name +
        // "::operator[](CORBA::ULong index)\n");
        buffer.append(sequenceMember + "& " + name
                      + "::operator[](CORBA::ULong index)\n");
        buffer.append("{\n");
        buffer.append("\tif (index < m_length) {\n");
        buffer.append("\t\tm_member.asignar_ptr(m_buffer+index);\n");
        buffer.append("\t\treturn m_member;\n\t}\n");
        buffer.append("\telse\n");
        buffer.append("\t\tthrow CORBA::BAD_PARAM(\"Current sequence length exceeded\");\n ");
        buffer.append("}\n");

        buffer.append("const " + internalType + "_ptr& " + name
                      + "::operator[] (CORBA::ULong index) const\n");
        buffer.append("{\n");
        buffer.append("\tif (index < m_length)\n");
        buffer.append("\t\treturn (const " + internalType
                      + "_ptr&) m_buffer[index];\n");
        buffer.append("\telse\n");
        buffer.append("\t\tthrow CORBA::BAD_PARAM(\"Current sequence length exceeded\");\n ");
        buffer.append("}\n");

        buffer.append(internalType + "_ptr* " + name
                      + "::get_buffer(CORBA::Boolean orphan)\n");
        buffer.append("{\n");
        buffer.append("\tif(!m_buffer)\n");
        buffer.append("\t\tallocbuf();\n");
        buffer.append("\tif(orphan) {\t // caller assumes ownership\n");
        buffer.append("\t\t" + internalType + "_ptr* aux;\n");
        buffer.append("\t\tif(!m_release)\n"); // condicion invertida
                                               // (vease mapping)
        buffer.append("\t\t\taux = NULL;\n");
        buffer.append("\t\telse {\n");
        buffer.append("\t\t\taux = m_buffer;\n");
        buffer.append("\t\t}\n");
        buffer.append("\t\tm_release = true;\n"); // debe quedar como con
                                                  // constructor por defecto
        buffer.append("\t\tm_buffer = NULL;\n");
        buffer.append("\t\tm_max_length = 0;\n");
        buffer.append("\t\tm_length = 0;\n");
        buffer.append("\t\treturn aux;\n");
        buffer.append("\t}\n");
        buffer.append("\treturn m_buffer;\n");
        buffer.append("}\n");

        buffer.append("const " + internalType + "_ptr* " + name
                      + "::get_buffer() const\n");
        buffer.append("{\n");
        buffer.append("\tif(!m_buffer)\n");
        buffer.append("\t\t(const_cast< " + name + "* > (this))->allocbuf();\n");
        buffer.append("\treturn (const " + internalType + "_ptr*) m_buffer;\n");
        buffer.append("}\n");

        buffer.append("void " + name
                      + "::replace(CORBA::ULong max,CORBA::ULong length,"
                      + internalType + "_ptr* data,CORBA::Boolean release)\n");
        buffer.append("{\n");
        //buffer.append("\tif(m_buffer && m_release) {\n"); DAVV - no hace
        // falta
        buffer.append("\tfreebuf();\n");
        //buffer.append("\t}\n");
        buffer.append("\tm_buffer = data;\n");
        buffer.append("\tm_length = length;\n");
        buffer.append("\tm_max_length = max;\n");
        buffer.append("\tm_release = release;\n");
        buffer.append("}\n");

        buffer.append("void " + name + "::allocbuf()\n");
        buffer.append("{\n");
        buffer.append("\tif(m_max_length > 0) {\n");
        buffer.append("\t\tm_buffer =  new " + internalType
                      + "_ptr[m_max_length];\n");
        buffer.append("\t\tm_release = true;\n");
        buffer.append("\t}\n");
        buffer.append("\telse\n");
        buffer.append("\t\tm_buffer = NULL;\n");
        buffer.append("}\n");

        buffer.append(internalType + "_ptr* " + name
                      + "::allocbuf(CORBA::ULong size)\n");
        buffer.append("{\n");
        buffer.append("\treturn new " + internalType + "_ptr[size];\n");
        buffer.append("}\n");

        buffer.append("void " + name + "::freebuf()\n");
        buffer.append("{\n");
        buffer.append("\tif(m_release && m_buffer) {\n");
        buffer.append("\t\tfor(CORBA::ULong i = 0; i < m_length; i++)\n");
        if (isValuetype)
            buffer.append("\t\t\tm_buffer[i]->_remove_ref();\n");
        else
            buffer.append("\t\t\tCORBA::release(m_buffer[i]);\n");
        buffer.append("\t\tdelete[] m_buffer;\n");
        buffer.append("\t}\n");
        buffer.append("}\n");

        buffer.append("void " + name + "::freebuf(" + internalType
                      + "_ptr* buf)\n");
        buffer.append("{\n");
        buffer.append("\tdelete[] buf;\n");
        buffer.append("}\n");

    }

    private static void generateCppSequence(StringBuffer buffer,
                                            String internalType, 
                                            String name,
                                            boolean is_array,
                                            boolean is_basic_type,
                                            boolean is_basic_data_type)
    {
       
        
        String forConstructor;
        if (name.lastIndexOf("::") >= 0)
            forConstructor = name.substring(name.lastIndexOf("::") + 2);
        else
            forConstructor = name;

        buffer.append(name
                    + "::"
                    + forConstructor
                    + "(const "
                    + name
                    + "& other): m_release(true),m_length(other.m_length),m_max_length(other.m_max_length)\n");
        buffer.append("{\n");
        buffer.append("\tallocbuf();\n");
        
        
        if(is_array) {
        		buffer.append("\tfor(CORBA::ULong i = 0; i < m_length; i++)\n");
            buffer.append("\t\t");
            buffer.append(internalType);
            buffer.append("_copy("); 
            buffer.append("m_buffer[i], other.m_buffer[i]);\n");
            
        } else {
        		if (is_basic_data_type){
                                buffer.append("\tif (m_length) \n");        			
        			buffer.append("\t\tmemcpy(m_buffer, other.m_buffer, m_length*sizeof(" + internalType + "));\n");
        			

        		} else{
        			buffer.append("\tfor(CORBA::ULong i = 0; i < m_length; i++)\n");
        			buffer.append("\t\tm_buffer[i] = other.m_buffer[i];\n");
        		}
        }
        
        buffer.append("}\n");
        

        buffer.append(name + "::~" + forConstructor + "()\n");
        buffer.append("{\n");
        buffer.append("\tfreebuf();\n");
        buffer.append("}\n");

        buffer.append(name + "& " + name + "::operator=(const " + name
                      + "& other)\n");
        buffer.append("{\n");
        buffer.append("\tif(this == &other)\n");
        buffer.append("\t\treturn *this;\n");
        //buffer.append("\tif(m_release)\n");
        buffer.append("\tfreebuf();\n");
        buffer.append("\tm_release = true;\n");
        buffer.append("\tm_length = other.m_length;\n");
        buffer.append("\tm_max_length = other.m_max_length;\n");
        buffer.append("\tallocbuf();\n");
        
        
        if(is_array) {
        		buffer.append("\tfor(CORBA::ULong i = 0; i < m_length; i++)\n");
            buffer.append("\t\t");
            buffer.append(internalType);
            buffer.append("_copy("); 
            buffer.append("m_buffer[i], other.m_buffer[i]);\n");
        } else {
            if (is_basic_data_type) { 
                                buffer.append("\tif (m_length)\n");

        			buffer.append("\t\tmemcpy(m_buffer, other.m_buffer, m_length*sizeof(" + internalType + "));\n");
        		} else{
        			buffer.append("\tfor(CORBA::ULong i = 0; i < m_length; i++)\n");
        			buffer.append("\t\tm_buffer[i] = other.m_buffer[i];\n");
        		}
        }        
        
        buffer.append("\treturn *this;\n");
        buffer.append("}\n");

        buffer.append("void " + name + "::length(CORBA::ULong v) \n");
        buffer.append("{ \n");
        buffer.append("\tif(!m_buffer) {\n");
        buffer.append("\t\tm_release = true;\n");
        buffer.append("\t\tm_length = v;\n");
        buffer.append("\t\tm_max_length = (v > m_max_length)? v : m_max_length;\n");
        buffer.append("\t\tallocbuf();\n");
        buffer.append("\t\treturn; \n");
        buffer.append("\t}\n");
        buffer.append("\tif(v > m_max_length) {\n");
        buffer.append("\t\t" + internalType + "* aux = new " + internalType
                      + "[v];\n");
        

        if(is_array) {
        	    buffer.append("\t\tfor(CORBA::ULong i = 0; i < m_length; i++)\n");
            buffer.append("\t\t{\n");
            buffer.append("\t\t\t");
            buffer.append(internalType);
            buffer.append("_copy("); 
            buffer.append("aux[i], m_buffer[i]);\n");
            buffer.append("\t\t}\n");
        } else {
            if (is_basic_data_type) {
                                buffer.append("\tif (m_length)\n");
        			buffer.append("\t\tmemcpy(aux, m_buffer, m_length*sizeof(" + internalType + "));\n");
                                } else{
        			buffer.append("\t\tfor(CORBA::ULong i = 0; i < m_length; i++)\n");
        			buffer.append("\t\t\taux[i] = m_buffer[i];\n");
        		}
        }
        
        
        buffer.append("\t\tfreebuf();\n");
        buffer.append("\t\tm_buffer = aux;\n");
        buffer.append("\t\tm_release = true;\n");
        buffer.append("\t\tm_max_length = v;\n");
        buffer.append("\t\tm_length = v;\n"); // Fix to bug 145 and 137
        buffer.append("\t}\n");
        /* �Bug [#137] Unnecessary alloc in sequence length member implementation? */
        if(is_array) {
        	buffer.append("\tif(v > m_length) {\n");
        	buffer.append("\t\t" +
      		      internalType + "_slice* _default = " + internalType + "_alloc();\n");
        	buffer.append("\t\tfor(CORBA::ULong i = m_length; i < v; i++) {\n");
            buffer.append("\t\t\t" +
            		      internalType + "_copy(m_buffer[i], _default);\n");
            buffer.append("\t\t}\n");
            buffer.append("\t\t" +
      		              internalType + "_free(_default);\n");
            buffer.append("\t}\n");
        } else {
            // Performance improvements for basic types sequences: doesn't need initialization
        	if (!is_basic_type /* !internalType.equals("CORBA::Octet")*/) {
                buffer.append("\tif(v > m_length) {\n");
        		buffer.append("\t\t" + internalType + " _default;\n");
        		buffer.append("\t\tfor(CORBA::ULong i = m_length; i < v; i++) {\n");
        		buffer.append("\t\t\tm_buffer[i] = _default;\n");
        		buffer.append("\t\t}\n");
        		buffer.append("\t}\n");
        	}
        }
                
        buffer.append("\tm_length = v;\n");
        buffer.append("} \n");

        buffer.append(internalType + "& " + name
                      + "::operator[](CORBA::ULong index)\n");
        buffer.append("{\n");
        buffer.append("\tif (index < m_length)\n");
        buffer.append("\t\treturn m_buffer[index];\n");
        buffer.append("\telse\n");
        buffer.append("\t\tthrow CORBA::BAD_PARAM(\"Current sequence length exceeded\");\n ");
        buffer.append("}\n");

        buffer.append("const " + internalType + "& " + name
                      + "::operator[] (CORBA::ULong index) const\n");
        buffer.append("{\n");
        buffer.append("\tif (index < m_length)\n");
        // TODO : revisar
        buffer.append("\t\treturn m_buffer[index];\n");
        //buffer.append("\t\treturn (const " + internalType
        //              + "&) m_buffer[index];\n");
        
        buffer.append("\telse\n");
        buffer.append("\t\tthrow CORBA::BAD_PARAM(\"Current sequence length exceeded\");\n ");
        buffer.append("}\n");

        buffer.append(internalType + "* " + name
                      + "::get_buffer(CORBA::Boolean orphan)\n");
        buffer.append("{\n");
        buffer.append("\tif(!m_buffer)\n");
        buffer.append("\t\tallocbuf();\n");
        buffer.append("\tif(orphan) {\t // caller assumes ownership\n");
        buffer.append("\t\t" + internalType + "* aux;\n");
        buffer.append("\t\tif(!m_release)\n"); // condicion invertida
                                               // (vease mapping)
        buffer.append("\t\t\taux = NULL;\n");
        buffer.append("\t\telse {\n");
        buffer.append("\t\t\taux = m_buffer;\n");
        buffer.append("\t\t}\n");
        buffer.append("\t\tm_release = true;\n"); // debe quedar como con
                                                  // constructor por defecto
        buffer.append("\t\tm_buffer = NULL;\n");
        buffer.append("\t\tm_max_length = 0;\n");
        buffer.append("\t\tm_length = 0;\n");
        buffer.append("\t\treturn aux;\n");
        buffer.append("\t}\n");
        buffer.append("\treturn m_buffer;\n");
        buffer.append("}\n");

        buffer.append("const " + internalType + "* " + name
                      + "::get_buffer() const\n");
        buffer.append("{\n");
        buffer.append("\tif(!m_buffer)\n");
        buffer.append("\t\t(const_cast< " + name + "* > (this))->allocbuf();\n");
        buffer.append("\treturn (const " + internalType + "*) m_buffer;\n");
        buffer.append("}\n");

        buffer.append("void " + name
                      + "::replace(CORBA::ULong max, CORBA::ULong length, "
                      + internalType + "* data, CORBA::Boolean release )\n");
        buffer.append("{\n");
        //buffer.append("\tif(m_buffer && m_release)\n");
        //buffer.append("\t\tfreebuf(m_buffer);\n");
        buffer.append("\tfreebuf();\n"); // DAVV - usa su metodo privado, hace
                                         // la comprobacion por si mismo
        buffer.append("\tm_buffer = data;\n");
        buffer.append("\tm_length = length;\n");
        buffer.append("\tm_max_length = max;\n");
        buffer.append("\tm_release = release;\n");
        buffer.append("}\n");

        buffer.append("void " + name + "::allocbuf()\n");
        buffer.append("{\n");
        buffer.append("\tif(m_max_length > 0) {\n");
        buffer.append("\t\tm_buffer =  new " + internalType
                      + "[m_max_length];\n");
        buffer.append("\t\tm_release = true;\n");
        buffer.append("\t}\n");
        buffer.append("\telse\n");
        buffer.append("\t\tm_buffer = NULL;\n");
        buffer.append("} \n");

        buffer.append("" + internalType + "* " + name
                      + "::allocbuf(CORBA::ULong size)\n");
        buffer.append("{\n");
        buffer.append("\treturn new " + internalType + "[size];\n");
        buffer.append("}\n");

        buffer.append("void " + name + "::freebuf()\n");
        buffer.append("{\n");
        buffer.append("\tif(m_release && m_buffer)\n"); 

        buffer.append("\t\tdelete[] m_buffer;\n");
        buffer.append("}\n");

        buffer.append("void " + name + "::freebuf(" + internalType + "* buf)\n");
        buffer.append("{\n");
        buffer.append("\tdelete[] buf;\n");
        buffer.append("}\n");

    }

    private static void generateCppStringSequence(StringBuffer buffer,
                                                  String name)
    {
        String forConstructor;
        if (name.lastIndexOf("::") >= 0)
            forConstructor = name.substring(name.lastIndexOf("::") + 2);
        else
            forConstructor = name;

        buffer.append(name
                    + "::"
                    + forConstructor
                    + "(const "
                    + name
                    + "& other): m_release(true), m_length(other.m_length), m_max_length(other.m_max_length), m_member(&m_release)\n");
        buffer.append("{\n");
        buffer.append("\tallocbuf();\n");
        buffer.append("\tfor(CORBA::ULong i = 0; i < m_length; i++)\n");
        buffer.append("\t\tm_buffer[i] = CORBA::string_dup(other.m_buffer[i]);\n");
        buffer.append("}\n");

        buffer.append(name + "::~" + forConstructor + "()\n");
        buffer.append("{\n");
        buffer.append("\tfreebuf();\n");
        buffer.append("}\n");

        buffer.append(name + "& " + name + "::operator=(const " + name
                      + "& other) \n");
        buffer.append("{\n");
        buffer.append("\tif(this == &other)\n");
        buffer.append("\t\treturn *this;\n");
        //buffer.append("\tif(m_release && m_buffer) {\n");
        //buffer.append("\t\tfor(CORBA::ULong i = 0; i < m_length; i++)\n"); // DAVV -
        // se pasa a freebuf()
        //buffer.append("\t\t\tCORBA::string_free(m_buffer[i]);\n");
        buffer.append("\tfreebuf();\n");
        //buffer.append("\t}\n");
        buffer.append("\tm_release = true;\n");
        buffer.append("\tm_length = other.m_length;\n");
        buffer.append("\tm_max_length = other.m_max_length;\n");
        buffer.append("\tallocbuf();\n");
        buffer.append("\tfor(CORBA::ULong i = 0; i < m_length; i++)\n");
        buffer.append("\t\tm_buffer[i] = CORBA::string_dup(other.m_buffer[i]);\n");
        buffer.append("\treturn *this;\n");
        buffer.append("}\n");

        buffer.append("void " + name + "::length(CORBA::ULong v)\n");
        buffer.append("{\n");
        buffer.append("\tif(!m_buffer) {\n");
        buffer.append("\t\tm_release = true;\n");
        buffer.append("\t\tm_length = v;\n");
        buffer.append("\t\tm_max_length = (v>m_max_length)? v : m_max_length;\n");
        buffer.append("\t\tallocbuf();\n");
        buffer.append("\t\tfor(CORBA::ULong i = 0; i < m_length; i++)\n");
        buffer.append("\t\t\tm_buffer[i] = CORBA::string_dup(\"\");\n");
        buffer.append("\t\treturn;\n\t}\n");
        //buffer.append("\tCORBA::ULong new_length = (v > m_bound)? m_bound :
        // v;\n");
        buffer.append("\tif(v > m_max_length) {\n");
        //buffer.append("\t\tfor(CORBA::ULong i = new_length; < i < m_length; i++)\n");
        //buffer.append("\t\t\tCORBA::string_free(m_buffer[i]);\n");
        //buffer.append("\t} else if(m_length < new_length)\n");
        //buffer.append("\t\tfor(CORBA::ULong i = m_length; < i < new_length; i++)\n");
        //buffer.append("\t\t\tm_buffer[i] = 0;\n");
        buffer.append("\t\tchar** aux = new char*[v];\n");
        buffer.append("\t\tfor(CORBA::ULong i = 0; i < m_length; i++)\n");
        //buffer.append("\t\t{\n");
        buffer.append("\t\t\taux[i] = CORBA::string_dup(m_buffer[i]);\n");
        //buffer.append("\t\t\tCORBA::string_free(m_buffer[i]);\n"); DAVV
        // incorporado a freebuf()
        //buffer.append("\t\t}\n");
        buffer.append("\t\tfor(CORBA::ULong j = m_length; j < v; j++)\n");
        buffer.append("\t\t\taux[j] = CORBA::string_dup(\"\");\n");
        buffer.append("\t\tfreebuf();\n");
        buffer.append("\t\tm_buffer = aux;\n");
        buffer.append("\t\tm_release = true;\n");
        buffer.append("\t\tm_max_length = v;\n");
        buffer.append("\t\tm_length = v;\n"); // Fix to bug 145 and 137
        buffer.append("\t}\n");
        buffer.append("\tif(m_length > v) {\n");
        buffer.append("\t\tfor(CORBA::ULong i = v; i < m_length; i++)\n");
        buffer.append("\t\t\tCORBA::string_free(m_buffer[i]);\n");
        buffer.append("\t} else if(m_length < v) {\n");
        buffer.append("\t\tfor(CORBA::ULong i = m_length; i < v; i++)\n");
        buffer.append("\t\t\tm_buffer[i] = CORBA::string_dup(\"\");\n");
        buffer.append("\t}\n");
        buffer.append("\tm_length = v;\n");
        buffer.append("}\n");

        //buffer.append("char*& " + name + "::operator[](CORBA::ULong
        // index)\n");
        buffer.append("::TIDorb::types::String_SequenceMember& " + name
                      + "::operator[](CORBA::ULong index)\n");
        buffer.append("{\n");
        buffer.append("\tif (index < m_length) {\n");
        buffer.append("\t\tm_member.asignar_ptr(m_buffer+index);\n");
        buffer.append("\t\treturn m_member;\n\t}\n");
        buffer.append("\telse\n");
        buffer.append("\t\tthrow CORBA::BAD_PARAM(\"Current sequence length exceeded\");\n ");
        buffer.append("}\n");

        buffer.append("const char*& " + name
                      + "::operator[] (CORBA::ULong index) const\n");
        buffer.append("{\n");
        buffer.append("\tif (index < m_length)\n");
        buffer.append("\t\treturn (const char*&) m_buffer[index];\n");
        buffer.append("\telse\n");
        buffer.append("\t\tthrow CORBA::BAD_PARAM(\"Current sequence length exceeded\");\n ");
        buffer.append("}\n");

        //buffer.append("const char*& "+name+"::operator[](CORBA::ULong index)
        // const {\n");
        //buffer.append("\tconst char *& _tmp = (const char
        // *&)m_buffer[index];\n");
        //buffer.append("\treturn _tmp;\n\t}\n\n");

        buffer.append("char** " + name
                      + "::get_buffer(CORBA::Boolean orphan)\n");
        buffer.append("{\n");
        buffer.append("\tif(!m_buffer)\n");
        buffer.append("\t\tallocbuf();\n");
        buffer.append("\tif(orphan) {\t // caller assumes ownership\n");
        buffer.append("\t\tchar** aux;\n");
        buffer.append("\t\tif(!m_release)\n"); // DAVV - condicion invertida
                                               // (vease mapping)
        buffer.append("\t\t\taux = NULL;\n");
        buffer.append("\t\telse {\n");
        buffer.append("\t\t\taux = m_buffer;\n");
        buffer.append("\t\t}\n");
        buffer.append("\t\tm_release = true;\n"); // DAVV - debe quedar como con
                                                  // constructor por defecto
        buffer.append("\t\tm_buffer = NULL;\n");
        buffer.append("\t\tm_max_length = 0;\n");
        buffer.append("\t\tm_length = 0;\n");
        buffer.append("\t\treturn aux;\n");
        buffer.append("\t}\n");
        buffer.append("\treturn m_buffer;\n");
        buffer.append("}\n");

        buffer.append("const char* const* " + name + "::get_buffer() const\n");
        buffer.append("{\n");
        buffer.append("\tif(!m_buffer)\n");
        buffer.append("\t\t(const_cast< " + name + "* > (this))->allocbuf();\n");
        buffer.append("\treturn (const char* const*) m_buffer;\n");
        buffer.append("}\n");

        buffer.append("void "
                    + name
                    + "::replace(CORBA::ULong max, CORBA::ULong length,char** data,CORBA::Boolean release)\n");
        buffer.append("{\n");
        //buffer.append("\tif(m_release && m_buffer) {\n");
        //buffer.append("\t\tfor(CORBA::ULong i = 0; i < m_length; i++)\n");
        //buffer.append("\t\t\tCORBA::string_free(m_buffer[i]);\n");
        //buffer.append("\t\tfreebuf(m_buffer);\n");
        buffer.append("\tfreebuf();\n");
        //buffer.append("}\n");
        buffer.append("\tm_buffer = data;\n");
        buffer.append("\tm_length = length;\n");
        buffer.append("\tm_max_length = max;\n");
        buffer.append("\tm_release = release;\n");
        buffer.append("}\n");

        buffer.append("void " + name + "::allocbuf()\n");
        buffer.append("{\n");
        buffer.append("\tif(m_max_length > 0) {\n");
        buffer.append("\t\tm_buffer = new char* [m_max_length];\n");
        buffer.append("\t\tm_release = true;\n\t}\n");
        buffer.append("\telse\n");
        buffer.append("\t\tm_buffer = NULL;\n");
        buffer.append("}\n");

        buffer.append("char** " + name + "::allocbuf(CORBA::ULong size)\n");
        buffer.append("{\n");
        buffer.append("\treturn new char*[size];\n");
        buffer.append("}\n");

        buffer.append("void " + name + "::freebuf()\n");
        buffer.append("{\n");
        buffer.append("\tif(m_release && m_buffer) {\n");
        buffer.append("\t\tfor(CORBA::ULong i = 0; i < m_length; i++)\n");
        buffer.append("\t\t\tCORBA::string_free(m_buffer[i]);\n");
        buffer.append("\t\tdelete [] m_buffer;\n");
        buffer.append("\t}\n");
        buffer.append("}\n");

        buffer.append("void " + name + "::freebuf(char** buf)\n");
        buffer.append("{\n");
        buffer.append("\tdelete[] buf;\n");
        buffer.append("}\n");

    }

    private static void generateCppWStringSequence(StringBuffer buffer,
                                                   String name)
    {
        String forConstructor;
        if (name.lastIndexOf("::") >= 0)
            forConstructor = name.substring(name.lastIndexOf("::") + 2);
        else
            forConstructor = name;

        buffer.append(name
                    + "::"
                    + forConstructor
                    + "(const "
                    + name
                    + "& other): m_release(true), m_length(other.m_length), m_max_length(other.m_max_length), m_member(&m_release)\n");
        buffer.append("{\n");
        buffer.append("\tallocbuf();\n");
        buffer.append("\tfor(CORBA::ULong i = 0; i < m_length; i++)\n");
        buffer.append("\t\tm_buffer[i] = CORBA::wstring_dup(other.m_buffer[i]);\n");
        buffer.append("}\n");

        buffer.append(name + "::~" + forConstructor + "()\n");
        buffer.append("{\n");
        buffer.append("\tfreebuf();\n");
        buffer.append("}\n");

        buffer.append(name + "& " + name + "::operator=(const " + name
                      + "& other) \n");
        buffer.append("{\n");
        buffer.append("\tif(this == &other)\n");
        buffer.append("\t\treturn *this;\n");
        //buffer.append("\tif(m_release && m_buffer) {\n");
        //buffer.append("\t\tfor(CORBA::ULong i = 0; i < m_length; i++)\n"); // 
        // se pasa a freebuf()
        //buffer.append("\t\t\tCORBA::string_free(m_buffer[i]);\n");
        buffer.append("\tfreebuf();\n");
        //buffer.append("\t}\n");
        buffer.append("\tm_release = true;\n");
        buffer.append("\tm_length = other.m_length;\n");
        buffer.append("\tm_max_length = other.m_max_length;\n");
        buffer.append("\tallocbuf();\n");
        buffer.append("\tfor(CORBA::ULong i = 0; i < m_length; i++)\n");
        buffer.append("\t\tm_buffer[i] = CORBA::wstring_dup(other.m_buffer[i]);\n");
        buffer.append("\treturn *this;\n");
        buffer.append("}\n");

        buffer.append("void " + name + "::length(CORBA::ULong v)\n");
        buffer.append("{\n");
        buffer.append("\tif(!m_buffer) {\n");
        buffer.append("\t\tm_release = true;\n");
        buffer.append("\t\tm_length = v;\n");
        buffer.append("\t\tm_max_length = (v > m_max_length)? v : m_max_length;\n");
        buffer.append("\t\tallocbuf();\n");
        buffer.append("\t\tfor(CORBA::ULong i = 0; i < m_length; i++)\n");
        buffer.append("\t\t\tm_buffer[i] = CORBA::wstring_dup(L\"\");\n");
        buffer.append("\t\treturn;\n\t}\n");
        //buffer.append("\tCORBA::ULong new_length = (v > m_bound)? m_bound :
        // v;\n");
        buffer.append("\tif(v > m_max_length) {\n");
        //buffer.append("\t\tfor(CORBA::ULong i = new_length; < i < m_length; i++)\n");
        //buffer.append("\t\t\tCORBA::string_free(m_buffer[i]);\n");
        //buffer.append("\t} else if(m_length < new_length)\n");
        //buffer.append("\t\tfor(CORBA::ULong i = m_length; < i < new_length; i++)\n");
        //buffer.append("\t\t\tm_buffer[i] = 0;\n");
        buffer.append("\t\tCORBA::WChar** aux = new CORBA::WChar*[v];\n");
        buffer.append("\t\tfor(CORBA::ULong i = 0; i < m_length; i++)\n");
        //buffer.append("\t\t{\n");
        buffer.append("\t\t\taux[i] = CORBA::wstring_dup(m_buffer[i]);\n");
        //buffer.append("\t\t\tCORBA::string_free(m_buffer[i]);\n"); DAVV
        // incorporado a freebuf()
        //buffer.append("\t\t}\n");
        buffer.append("\t\tfor(CORBA::ULong j = m_length; j < v; j++)\n");
        buffer.append("\t\t\taux[j] = CORBA::wstring_dup(L\"\");\n");
        buffer.append("\t\tfreebuf();\n");
        buffer.append("\t\tm_buffer = aux;\n");
        buffer.append("\t\tm_release = true;\n");
        buffer.append("\t\tm_max_length = v;\n");
        buffer.append("\t\tm_length = v;\n"); // Fix to bug 145 and 137
        buffer.append("\t}\n");
        buffer.append("\tif(m_length > v) {\n");
        buffer.append("\t\tfor(CORBA::ULong i = v; i < m_length; i++)\n");
        buffer.append("\t\t\tCORBA::wstring_free(m_buffer[i]);\n");
        buffer.append("\t} else if(m_length < v) {\n");
        buffer.append("\t\tfor(CORBA::ULong i = m_length; i < v; i++)\n");
        buffer.append("\t\t\tm_buffer[i] = CORBA::wstring_dup(L\"\");\n");
        buffer.append("\t}\n");
        buffer.append("\tm_length = v;\n");
        buffer.append("}\n");

        //buffer.append("char*& " + name + "::operator[](CORBA::ULong
        // index)\n");
        buffer.append("::TIDorb::types::WString_SequenceMember& " + name
                      + "::operator[](CORBA::ULong index)\n");
        buffer.append("{\n");
        buffer.append("\tif (index < m_length) {\n");
        buffer.append("\t\tm_member.asignar_ptr(m_buffer+index);\n");
        buffer.append("\t\treturn m_member;\n\t}\n");
        buffer.append("\telse\n");
        buffer.append("\t\tthrow CORBA::BAD_PARAM(\"Current sequence length exceeded\");\n ");
        buffer.append("}\n");

        buffer.append("const CORBA::WChar*& " + name
                      + "::operator[] (CORBA::ULong index) const\n");
        buffer.append("{\n");
        buffer.append("\tif (index < m_length)\n");
        buffer.append("\t\treturn (const CORBA::WChar*&) m_buffer[index];\n");
        buffer.append("\telse\n");
        buffer.append("\t\tthrow CORBA::BAD_PARAM(\"Current sequence length exceeded\");\n ");
        buffer.append("}\n");

        //buffer.append("const char*& "+name+"::operator[](CORBA::ULong index)
        // const {\n");
        //buffer.append("\tconst char *& _tmp = (const char
        // *&)m_buffer[index];\n");
        //buffer.append("\treturn _tmp;\n\t}\n\n");

        buffer.append("CORBA::WChar** " + name
                      + "::get_buffer(CORBA::Boolean orphan)\n");
        buffer.append("{\n");
        buffer.append("\tif(!m_buffer)\n");
        buffer.append("\t\tallocbuf();\n");
        buffer.append("\tif(orphan) {\t // caller assumes ownership\n");
        buffer.append("\t\tCORBA::WChar** aux;\n");
        buffer.append("\t\tif(!m_release)\n"); // condicion invertida
                                               // (vease mapping)
        buffer.append("\t\t\taux = NULL;\n");
        buffer.append("\t\telse {\n");
        buffer.append("\t\t\taux = m_buffer;\n");
        buffer.append("\t\t}\n");
        buffer.append("\t\tm_release = true;\n"); // debe quedar como con
                                                  // constructor por defecto
        buffer.append("\t\tm_buffer = NULL;\n");
        buffer.append("\t\tm_max_length = 0;\n");
        buffer.append("\t\tm_length = 0;\n");
        buffer.append("\t\treturn aux;\n");
        buffer.append("\t}\n");
        buffer.append("\treturn m_buffer;\n");
        buffer.append("}\n");

        buffer.append("const CORBA::WChar* const* " + name
                      + "::get_buffer() const\n");
        buffer.append("{\n");
        buffer.append("\tif(!m_buffer)\n");
        buffer.append("\t\t(const_cast< " + name + "* > (this))->allocbuf();\n");
        buffer.append("\treturn (const CORBA::WChar* const*) m_buffer;\n");
        buffer.append("}\n");

        buffer.append("void "
                    + name
                    + "::replace(CORBA::ULong max, CORBA::ULong length,CORBA::WChar** data,CORBA::Boolean release)\n");
        buffer.append("{\n");
        //buffer.append("\tif(m_release && m_buffer) {\n");
        //buffer.append("\t\tfor(CORBA::ULong i = 0; i < m_length; i++)\n");
        //buffer.append("\t\t\tCORBA::string_free(m_buffer[i]);\n");
        //buffer.append("\t\tfreebuf(m_buffer);\n");
        buffer.append("\tfreebuf();\n");
        //buffer.append("}\n");
        buffer.append("\tm_buffer = data;\n");
        buffer.append("\tm_length = length;\n");
        buffer.append("\tm_max_length = max;\n");
        buffer.append("\tm_release = release;\n");
        buffer.append("}\n");

        buffer.append("void " + name + "::allocbuf()\n");
        buffer.append("{\n");
        buffer.append("\tif(m_max_length > 0) {\n");
        buffer.append("\t\tm_buffer = new CORBA::WChar* [m_max_length];\n");
        buffer.append("\t\tm_release = true;\n\t}\n");
        buffer.append("\telse\n");
        buffer.append("\t\tm_buffer = NULL;\n");
        buffer.append("}\n");

        buffer.append("CORBA::WChar** " + name
                      + "::allocbuf(CORBA::ULong size)\n");
        buffer.append("{\n");
        buffer.append("\treturn new CORBA::WChar*[size];\n");
        buffer.append("}\n");

        buffer.append("void " + name + "::freebuf()\n");
        buffer.append("{\n");
        buffer.append("\tif(m_release && m_buffer) {\n");
        buffer.append("\t\tfor(CORBA::ULong i = 0; i < m_length; i++)\n");
        buffer.append("\t\t\tCORBA::wstring_free(m_buffer[i]);\n");
        buffer.append("\t\tdelete [] m_buffer;\n");
        buffer.append("\t}\n");
        buffer.append("}\n");

        buffer.append("void " + name + "::freebuf(CORBA::WChar** buf)\n");
        buffer.append("{\n");
        buffer.append("\tdelete[] buf;\n");
        buffer.append("}\n");

    }

    private StringBuffer generateSequenceCpp(Element type, Element decl,
                                       String genPackage)
        throws Exception
    {

        String nameWithPackage = genPackage + "::"
                                 + decl.getAttribute(OMG_name);
        if (nameWithPackage.startsWith("::"))
            nameWithPackage = nameWithPackage.substring(2);
        StringBuffer buffer = new StringBuffer();
        Element internal = (Element) type.getFirstChild();
        String internalType = XmlType2Cpp.getType(internal);

        // Comprobamos si es una Bounded Sequence..
        String bounds = "";
        if (type.getChildNodes().getLength() > 1) {
            Element el = (Element) type.getLastChild();
            if (el != null) {
                Element expr = (Element) el.getFirstChild();
                if (expr != null)
                    bounds = "" + XmlExpr2Cpp.getIntExpr(expr);
            }
        }

        // STRING (incluido scoped_name)
        if (XmlType2Cpp.isAString(internal)) {
            if (!bounds.equals(""))
                generateCppBoundedStringSequence(buffer, nameWithPackage);
            else
                generateCppStringSequence(buffer, nameWithPackage);

            // WSTRING (incluido scoped_name)
        } else if (XmlType2Cpp.isAWString(internal)) {
            if (!bounds.equals(""))
                generateCppBoundedWStringSequence(buffer, nameWithPackage);
            else
                generateCppWStringSequence(buffer, nameWithPackage);

        } else {
            // interface
            String definitionType = XmlType2Cpp.getDefinitionType(internal);
            if (definitionType.equals(OMG_interface)
                || (internalType.equals(XmlType2Cpp.basicMapping(OMG_Object)))) {
                internalType = (internalType.endsWith("_ptr") 
                    ? internalType.substring(0, internalType.length() - 4) 
                        : internalType); // para Objects
                if (!bounds.equals(""))
                    generateCppBoundedInterfaceSequence(buffer, internalType,
                                                        nameWithPackage, false);
                else
                    generateCppInterfaceSequence(buffer, internalType,
                                                 nameWithPackage, false);
            } else {
                // valuetype
                if (definitionType.equals(OMG_valuetype)) {
                    if (!bounds.equals(""))
                        generateCppBoundedInterfaceSequence(buffer,
                                                            internalType,
                                                            nameWithPackage,
                                                            true);
                    else
                        generateCppInterfaceSequence(buffer, internalType,
                                                     nameWithPackage, true);
                    // caso general
                } else {
                    if (!bounds.equals(""))
                        generateCppBoundedSequence(buffer, internalType,
                                                   nameWithPackage, 
                                                   XmlType2Cpp.isABasicType(internal),
                                                   XmlType2Cpp.isABasicDataType(internal));
                    else
                        generateCppSequence(buffer, internalType,
                                            nameWithPackage,
                                            XmlType2Cpp.isAnArray(internal),
                                            XmlType2Cpp.isABasicType(internal),
                                            XmlType2Cpp.isABasicDataType(internal));
                }
            }
        }

        String holderClass = XmlType2Cpp.getHolderName(nameWithPackage);
        String name = decl.getAttribute(OMG_name);        
        
        String holder_contents = XmlCppHolderGenerator.generateCpp(genPackage, name,
                                                            holderClass);
        
        buffer.append(holder_contents);
        
        return buffer;
    }

    private Element generateInternalSequenceImplementation(
                                                  Element doc,
                                                  Element decl,
                                                  String sourceDirectory,
                                                  String headerDirectory,
                                                  String genPackage,
                                                  boolean createDir, 
												  boolean expanded, 
												  String h_ext, 
												  String c_ext)
        throws Exception
    {
        // El objetivo de este metodo es desarrollar un subArbol XML para el
        // nodo Sequence que desarolle
        // XmlTypedef2Cpp y generar el nombre para esta secuencia.
        // Esto es doc,
        //<sequence>
        //      <type kind="string" VL_Type="true"/>
        //</sequence>
        //----
        //<simple name="Viar" line="4" column="21" scopedName="::A::L::Viar"
        // VL_Type="false"/>
        // Y esto de arriba lo que viene despues que utilizara el metodo que sea
        // para generar el nombre del tipo.
        //
        // Lo que espera un TYPEDEF para Sequence es
        //<typedef VL_Type="true">
        //   <sequence>
        //      <type kind="string" VL_Type="true"/>
        //   </sequence>
        //   <simple name="aR" line="2" column="29" scoped_name="::A::aR"
        // VL_Type="true" scopedName="::A::aR"/>
        //</typedef>

 
        //Element def= (Element) doc.getNextSibling(); 
        // Sacamos el Elemento con el nombre del attributo
        //EL padre del padre tiene un hijo simple con el nombre,
        Element el = doc.getOwnerDocument().createElement(OMG_typedef);
        el.setAttribute(OMG_variable_size_type, "true");// PORQUE ES UNA
                                                        // SECUENCIA
        Element copy = (Element) doc.cloneNode(true);
        el.appendChild(copy);
        Element internalDeclaration = 
            doc.getOwnerDocument().createElement(OMG_simple_declarator);
        String name = decl.getAttribute(OMG_name);
        //name+="_Internal"; 
        // que se define con la coletilla internal. 
        // como el mapping no especifica nada, seguimos la forma que S???
        // especifica para sequences en structs:
        name = "_" + name + "_seq";
        String scopedName = genPackage + "::" + name;
        if (!scopedName.startsWith("::"))
            scopedName = "::" + scopedName;
        //Xml2Cpp.generateForwardUtilSimbols(genPackage,name); 
        el.appendChild(internalDeclaration);
        internalDeclaration.setAttribute(OMG_name, name);
        internalDeclaration.setAttribute(OMG_scoped_name, scopedName);
        internalDeclaration.setAttribute("line", decl.getAttribute("line"));
        internalDeclaration.setAttribute("column", decl.getAttribute("column"));

        Element scoped = doc.getOwnerDocument().createElement(OMG_scoped_name);
        scoped.setAttribute(OMG_name, scopedName);
        scoped.setAttribute("line", decl.getAttribute("line"));
        scoped.setAttribute("column", decl.getAttribute("column"));
        scoped.setAttribute(OMG_scoped_name, scopedName);

        Element temp = (Element) doc.getParentNode();
        while (!temp.getTagName().equals(OMG_typedef))
            temp = (Element) temp.getParentNode();
        Element parent = (Element) temp.getParentNode();
        parent.insertBefore(el, temp);

        doc.getParentNode().insertBefore(scoped, doc);
        doc.getParentNode().removeChild(doc);

        XmlTypedef2Cpp gen = new XmlTypedef2Cpp();
        gen.generateCpp(el, sourceDirectory, headerDirectory, genPackage,
                        createDir, expanded, h_ext, c_ext);
        //def.setAttribute(OMG_name,name);
        TypedefManager.getInstance().typedef(scopedName, null, null, null,
                                             OMG_sequence, null);
        // Para que no falle XmlType2Cpp.getDefinitionType()
        // Ni siquiera aunque se use -package_to
        // No hace falta 'inscibirlo' en el TypeManager, porque el nombre que se
        // le pone al nodo ya esta modificado
        // caso de afectarle package_to, asi que getType tp fallara

        // si no volvemos a borrar del arbol los nodos insertados 
        // tendremos un error:
        //      el typedef nuevo se inserta antes del antiguo, por lo que se 
        //      modifica el numero de hijos del padre de ambos; si ???ste es, 
        //      por ejemplo, un m???dulo, en el bucle 'for' de XmlModule2Cpp
        //      que revisa cada hijo del m???dulo, habr??? cambiado el ???ndice del 
        //      typedef amtiguo, y se volver??? a procesar, pero incorrectamente,
        //      puesto que su tipo ya no es un sequence, 
        //      sino un OMG_scoped_name, y se deja de a???adir al .h el include
        //      del tipo interno
        el.getParentNode().removeChild(el);
        return (Element) scoped.getParentNode();
    }

    private void generateTypeDefSequence(Element type, Element decl,
                                         String sourceDirectory,
                                         String headerDirectory,
                                         String genPackage, 
										 boolean expanded, 
										 String h_ext,
										 String c_ext)
        throws Exception
    {

        // generaci???n de sequences [y arrays de sequences, in the future]

        //FileWriter writer;
        //BufferedWriter buf_writer;
//   	 Gets the FileManager
        FileManager fm = FileManager.getInstance();
        StringBuffer contents_buffer;
        String fileName;
        //String contents;
        StringBuffer headerBuff;
        StringBuffer sourceBuff;
        if (genPackage.startsWith("::")) // por si acaso, aunque esto ya
                                         // deber???a estar superado...
            genPackage = genPackage.substring(2);

        String headerDir = Xml2Cpp.getDir(genPackage, headerDirectory,
                                           this.m_generate);
        String sourceDir = Xml2Cpp.getDir(genPackage, sourceDirectory,
                                           this.m_generate);

        String name = decl.getAttribute(OMG_name); // DAVV - nombre de la
                                                   // secuencia
        String nameWithPackage = genPackage.equals("") ? name : genPackage
                                                                + "::" + name; 
           // nombre completo (con namespace) de la secuencia

        //Xml2Cpp.generateForwardUtilSimbols(genPackage,name); 
        headerBuff = new StringBuffer();
        sourceBuff = new StringBuffer();

        // cabeceras de ambos ficheros
        XmlHppHeaderGenerator.generate(type, headerBuff, "typedef", name,
                                       genPackage);
        XmlCppHeaderGenerator.generate(sourceBuff, "typedef", name, genPackage);

        if (((Element) type.getFirstChild()).getTagName().equals(OMG_sequence)) {
            type = generateInternalSequenceImplementation(
                                (Element) type.getFirstChild(), 
                                decl, sourceDirectory, headerDirectory,
                                genPackage, this.m_generate, expanded, h_ext, c_ext);
            String newName = 
                ((Element) type.getFirstChild()).getAttribute(OMG_name);
            newName = newName.substring(newName.lastIndexOf("::") + 2);
            headerBuff.append("#include \"" + newName + h_ext+"\"\n\n");
        }

        // contenido del header de sequence - H -
        contents_buffer = generateSequenceHpp(type, decl);
        headerBuff.append(contents_buffer);

        // header del Helper
        /*
         * contents = generateHppHelperDef(type, decl, genPackage);
         * headerBuff.append(contents);
         */

        headerBuff.append(XmlCppHelperGenerator.generateHpp(type, decl,
                                                            genPackage,false));

        // source del Helper
        sourceBuff.append(XmlCppHelperGenerator.generateCpp(type, decl,
                                                            genPackage, false));
        contents_buffer = generateCppHelperDef(type, decl, genPackage); 
        // solo contiene la implementacion del holder

        // source de sequence - C -
        contents_buffer.append(generateSequenceCpp(type, decl, genPackage));
        sourceBuff.append(contents_buffer);

        // ArrayHolder generation. Only generate the holder for the sequence
        // type
        contents_buffer = XmlCppHolderGenerator.generateHpp(OMG_Holder_Complex, 
                                 genPackage, nameWithPackage,
                                 XmlType2Cpp.getHolderName(nameWithPackage));

        headerBuff.append(contents_buffer);

        XmlHppHeaderGenerator.generateFoot(headerBuff, type.getTagName(), name,
                                           genPackage);

        //XmlCppFooterGenerator.generate(type, sourceBuff,type.getTagName(),
        // name,genPackage);
        
        String idl_fn = XmlUtil.getIdlFileName(type);

        if (this.m_generate) { // Source Writer
            fileName = name + c_ext; // Only One file for everything.
            Traces.println("XmlTypedef2Cpp:->", Traces.DEEP_DEBUG);
            Traces.println("Generating : " + sourceDir + File.separatorChar
                           + fileName + "...", Traces.USER);
            //writer = new FileWriter(sourceDir + File.separatorChar + fileName);
            //buf_writer = new BufferedWriter(writer);
            //buf_writer.write(sourceBuff.toString());
            //buf_writer.close();
            
            fm.addFile(sourceBuff, fileName, sourceDir, idl_fn, FileManager.TYPE_MAIN_SOURCE);

            // Header Writer
            fileName = name + h_ext; // Only One file for everything.
            Traces.println("XmlTypedef2Cpp:->", Traces.DEEP_DEBUG);
            Traces.println("Generating : " + headerDir + File.separatorChar
                           + fileName + "...", Traces.USER);
            //writer = new FileWriter(headerDir + File.separatorChar + fileName);
            //buf_writer = new BufferedWriter(writer);
            //buf_writer.write(headerBuff.toString());
            //buf_writer.close();
            
            fm.addFile(headerBuff, fileName, headerDir, idl_fn, FileManager.TYPE_MAIN_HEADER);
        }

        headerBuff = new StringBuffer();

        // External any operations
        // Design of the header files, Any operations outside main file.
        StringBuffer re_buffer = new StringBuffer();
        XmlHppExternalOperationsGenerator.generateHpp(type, re_buffer,
                                                      OMG_typedef, name,
                                                      genPackage);
        headerBuff.append(re_buffer);

        if (this.m_generate) {
            fileName = name + "_ext" + h_ext;
            //writer = new FileWriter(headerDir + File.separatorChar + fileName);
            //buf_writer = new BufferedWriter(writer);
            //buf_writer.write(headerBuff.toString());
            //buf_writer.close();
            
            fm.addFile(headerBuff, fileName, headerDir, idl_fn, FileManager.TYPE_MAIN_HEADER_EXT);
        }

    }

}

