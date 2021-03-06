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

package es.tid.TIDIdlc.xml2java;

import es.tid.TIDIdlc.idl2xml.Idl2XmlNames;
import es.tid.TIDIdlc.xmlsemantics.*;
import es.tid.TIDIdlc.util.Traces;

import java.io.*;
import java.util.StringTokenizer;
import java.util.Vector;
import es.tid.TIDIdlc.xml2java.structures.*;

import org.w3c.dom.*;

/**
 * Generates Java for valuetypes.
 */
class XmlValuetype2Java extends XmlValuetypeUtils2Java
    implements Idl2XmlNames
{

    private boolean m_generate;

    /** Generate Java */
    public void generateJava(Element doc, String outputDirectory,
                             String genPackage, boolean generateCode)
        throws Exception
    {

        m_generate = generateCode;
        String isAbstractS = doc.getAttribute(OMG_abstract);
        boolean isAbstract = (isAbstractS != null) && (isAbstractS.equals(OMG_true));

        if (doc.getAttribute(OMG_fwd).equals(OMG_true)) {
            return; // it is a forward declaration
        }

        // Get package components
        String targetDirName = outputDirectory;
        if (targetDirName.charAt(targetDirName.length() - 1) == File.separatorChar) {
            targetDirName = targetDirName.substring(0,
            		targetDirName.length() - 1);
        }
        StringTokenizer tok = new StringTokenizer(genPackage, ".");
        while (tok.hasMoreTokens()) {
            targetDirName = targetDirName + File.separatorChar + tok.nextToken();
        }

        if (generateCode) {
            // Make target directory
            File targetDir = new File(targetDirName);
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }
        }

        FileWriter writer;
        BufferedWriter buf_writer;
        String fileName, contents;

        // Valuetype generation
        fileName = doc.getAttribute(OMG_name) + ".java";
        contents = generateJavaValuetypeDef(doc, outputDirectory, genPackage);
        if (generateCode && !contents.equals("")) {
            Traces.println("XmlValuetype2Java:->", Traces.DEEP_DEBUG);
            Traces.println("Generating : " + targetDirName + File.separatorChar
                           + fileName + "...", Traces.USER);
            writer = new FileWriter(targetDirName + File.separatorChar + fileName);
            buf_writer = new BufferedWriter(writer);
            buf_writer.write(contents);
            buf_writer.close();
        }

        // ValuetypeHolder generation
        fileName = doc.getAttribute(OMG_name) + "Holder" + ".java";
        if (generateCode) {
            Traces.println("XmlValuetype2Java:->", Traces.DEEP_DEBUG);
            Traces.println("Generating : " + targetDirName + File.separatorChar
                           + fileName + "...", Traces.USER);
        }
        contents = generateJavaHolderDef(doc, genPackage); 
        if (generateCode) {
            writer = new FileWriter(targetDirName + File.separatorChar + fileName);
            buf_writer = new BufferedWriter(writer);
            buf_writer.write(contents);
            buf_writer.close();
        }

        // ValuetypeHelper generation
        fileName = doc.getAttribute(OMG_name) + "Helper" + ".java";
        if (generateCode) {
            Traces.println("XmlValuetype2Java:->", Traces.DEEP_DEBUG);
            Traces.println("Generating : " + targetDirName + File.separatorChar
                           + fileName + "...", Traces.USER);
        }
        contents = generateJavaHelperDef(doc, genPackage);
        if (generateCode) {
            writer = new FileWriter(targetDirName + File.separatorChar + fileName);
            buf_writer = new BufferedWriter(writer);
            buf_writer.write(contents);
            buf_writer.close();
        }
        // ValuetypeFactory generation
        fileName = doc.getAttribute(OMG_name) + "ValueFactory" + ".java";
        contents = generateJavaFactoryDef(doc, genPackage);
        if (generateCode && contents != null) {
            Traces.println("XmlValuetype2Java:->", Traces.DEEP_DEBUG);
            Traces.println("Generating : " + targetDirName + File.separatorChar
                           + fileName + "...", Traces.USER);
            writer = new FileWriter(targetDirName + File.separatorChar + fileName);
            buf_writer = new BufferedWriter(writer);
            buf_writer.write(contents);
            buf_writer.close();
        }
    }

    private String generateJavaValuetypeDef(Element doc, String outputDir,
                                            String genPackage)
        throws Exception
    {
        StringBuffer buffer = new StringBuffer();
        String name = doc.getAttribute(OMG_name);
        String isBoxedS = doc.getAttribute(OMG_boxed);
        boolean isBoxed = (isBoxedS != null) && (isBoxedS.equals(OMG_true));
        // Package header
        if (!isBoxed) {
            XmlJavaHeaderGenerator.generate(buffer, "valuetype", name, genPackage);
            buffer.append(generateJavaValuetype(doc, outputDir, genPackage));
        } else {
            NodeList nodes = doc.getChildNodes();
            Element typeEl = (Element) nodes.item(0);
            String type = XmlType2Java.getType(typeEl);
            if (XmlType2Java.isPrimitiveJavaType(type)) { 
            	// DAVV - s�lo para
            	// 'value boxes' de
            	// tipos primitivos
                XmlJavaHeaderGenerator.generate(buffer, "valuetype", name,
                                                genPackage);
                buffer.append(generateJavaBoxedValuetype(doc));
            } else {
                String tag = typeEl.getTagName();
                if (tag.equals(OMG_struct) || tag.equals(OMG_union)
                    || tag.equals(OMG_enum))
                    generateJavaSubPackageDef(typeEl, outputDir, name,
                                              genPackage);
            }

        }

        return buffer.toString();
    }

    private String generateJavaValuetype(Element doc, String outputDir,
                                         String genPackage)
        throws Exception
    {
        StringBuffer buffer = new StringBuffer();
        String name = doc.getAttribute(OMG_name);

        String isAbstractS = doc.getAttribute(OMG_abstract);
        boolean isAbstract = (isAbstractS != null)
                             && (isAbstractS.equals(OMG_true));

        // Class header
        if (isAbstract)
            buffer.append("public interface ");
        else
            buffer.append("public abstract class ");
        buffer.append(name);
        generateJavaInheritance(buffer, doc, genPackage);
        generateJavaExportDef(buffer, doc, outputDir, name, genPackage);
        buffer.append("}\n");

        return buffer.toString();
    }

    private String generateJavaBoxedValuetype(Element doc)
        throws Exception
    {
        StringBuffer buffer = new StringBuffer();
        String name = doc.getAttribute(OMG_name);
        NodeList nodes = doc.getChildNodes();
        Element typeEl = (Element) nodes.item(0);
        String type = XmlType2Java.getType(typeEl);
        // Class header
        buffer.append("public class " + name + "\n");
        buffer.append("   implements org.omg.CORBA.portable.ValueBase{\n\n");
        buffer.append("  public " + type + " value;\n\n");
        buffer.append("  public " + name + "(" + type
                      + " initial){ value=initial; }\n\n");
        buffer.append("  public static String[] _ids = { " + name
                      + "Helper.id()};\n\n");
        buffer.append("  public String[] _truncatable_ids(){ return _ids; }\n\n");
        buffer.append("}\n");

        return buffer.toString();
    }

    private void generateJavaInheritance(StringBuffer buffer, Element doc,
                                         String genPackage)
        throws Exception
    {

        int inheritances = 0, statefuls = 0, customs = 0;
        Vector extend = new Vector();
        Vector implement = new Vector();
        Vector truncatable = new Vector();
        String isAbstractS = doc.getAttribute(OMG_abstract);
        boolean isAbstract = (isAbstractS != null) && (isAbstractS.equals(OMG_true));
        String isCustomS = doc.getAttribute(OMG_custom);
        boolean isCustom = (isCustomS != null) && (isCustomS.equals(OMG_true));

        String name = doc.getAttribute(OMG_name);
        String pack_name;
        if (genPackage.equals(""))
            pack_name = name;
        else
            pack_name = genPackage + "." + name;

        NodeList list = doc.getElementsByTagName(OMG_value_inheritance_spec);

        Element inheritance = (Element) list.item(0);
        String isTruncatableS = inheritance.getAttribute(OMG_truncatable);
        boolean isTruncatable = (isTruncatableS != null) && (isTruncatableS.equals(OMG_true));
        NodeList inherits = inheritance.getChildNodes();

        for (int k = 0; k < inherits.getLength(); k++) {
            Element inheritedScopeEl = (Element) inherits.item(k);
            String inherited_tag = inheritedScopeEl.getTagName();
            if (inherited_tag.equals(OMG_scoped_name)) {
                String inheritedScope = inheritedScopeEl.getAttribute(OMG_name);
                Scope inhScope = Scope.getGlobalScopeInterface(inheritedScope);
                Element elfather = inhScope.getElement();
                String father_tag = elfather.getTagName();
                if (father_tag.equals(OMG_valuetype)) {
                    String fatherIsAbstractS = elfather.getAttribute(OMG_abstract);
                    boolean fatherIsAbstract = (fatherIsAbstractS != null) && (fatherIsAbstractS.equals(OMG_true));
                    String fatherIsCustomS = elfather.getAttribute(OMG_custom);
                    boolean fatherIsCustom = (fatherIsCustomS != null) && (fatherIsCustomS
                                                 .equals(OMG_true));
                    if (isAbstract || (!fatherIsAbstract)) {
                        extend.add(TypeManager.convert(inheritedScope));
                        if (isTruncatable && (!isAbstract))
                            truncatable.add(TypeManager.convert(inheritedScope));
                    } else
                        implement.add(TypeManager.convert(inheritedScope));
                    if (fatherIsCustom)
                        customs++;
                    if (!fatherIsAbstract)
                        statefuls++;
                    inheritances++;
                }
            } else if (inherited_tag.equals(OMG_supports)) {
                NodeList supports = inheritedScopeEl.getChildNodes();
                for (int j = 0; j < supports.getLength(); j++) {
                    Element supportedScopeEl = (Element) supports.item(j);
                    String supported_tag = supportedScopeEl.getTagName();
                    if (supported_tag.equals(OMG_scoped_name)) {
                        String supportedScope = supportedScopeEl.getAttribute(OMG_name);
                        Scope inhScope = Scope.getGlobalScopeInterface(supportedScope);
                        Element elfather = inhScope.getElement();
                        String father_tag = elfather.getTagName();
                        if (father_tag.equals(OMG_interface)) {
                            if (isAbstract)
                                extend.add(TypeManager.convert(supportedScope));
                            else {
                                String operations = "";
                                String fatherIsAbstractS = elfather.getAttribute(OMG_abstract);
                                boolean fatherIsAbstract = (fatherIsAbstractS != null) && (fatherIsAbstractS.equals(OMG_true));
                                if (!fatherIsAbstract)
                                    operations = "Operations";
                                implement.add(TypeManager.convert(supportedScope) + operations);

                            }
                        }
                    }
                }
            }
        }

        if (isAbstract) {
            if (inheritances == 0)
                extend.add("org.omg.CORBA.portable.ValueBase");
        } else {
            if (inheritances == 0) {
                if (isCustom)
                    implement.add("org.omg.CORBA.portable.CustomValue");
                else
                    implement.add("org.omg.CORBA.portable.StreamableValue");
            } else {
                if (isCustom) {
                    if (customs == 0)
                        implement.add("org.omg.CORBA.portable.CustomValue");
                } else {
                    if (statefuls == 0)
                        implement.add("org.omg.CORBA.portable.StreamableValue");
                }
            }
        }

        for (int i = 0; i < extend.size(); i++) {
            if (i == 0)
                buffer.append("\n   extends " + extend.elementAt(i));
            else
                buffer.append(",\n           " + extend.elementAt(i));
        }

        for (int i = 0; i < implement.size(); i++) {
            if (i == 0)
                buffer.append("\n   implements " + implement.elementAt(i));
            else
                buffer.append(",\n              " + implement.elementAt(i));
        }
        buffer.append("{\n\n");

        if (implement.contains("org.omg.CORBA.portable.ValueBase")
            || implement.contains("org.omg.CORBA.portable.CustomValue")
            || implement.contains("org.omg.CORBA.portable.StreamableValue")
            || (statefuls > 0)) {

            buffer.append("  private static String[] _truncatable_ids = {\n");
            if (isTruncatable) {
                for (int i = 0; i < truncatable.size(); i++) {
                    buffer.append("      " + truncatable.elementAt(i) + "Helper.id(),\n");
                }
            }

            buffer.append("      " + pack_name + "Helper.id()\n  };\n\n");
            buffer.append("  public String[] _truncatable_ids(){\n");
            buffer.append("    return _truncatable_ids;\n");
            buffer.append("  }\n\n");
        }

        if (implement.contains("org.omg.CORBA.portable.Streamable")
            || implement.contains("org.omg.CORBA.portable.StreamableValue")
            || (statefuls > 0)) {

            StringBuffer _read = new StringBuffer();
            StringBuffer _write = new StringBuffer();
            NodeList listStateMember = doc.getElementsByTagName(OMG_state_member);
            for (int i = 0; i < listStateMember.getLength(); i++) {
                Element stateMemberEl = (Element) listStateMember.item(i);
                Element type = (Element) stateMemberEl.getFirstChild();
                String type_tag = type.getTagName();
                NodeList member_list = stateMemberEl.getElementsByTagName(OMG_simple_declarator);
                for (int j = 0; j < member_list.getLength(); j++) {
                    String member_name = ((Element) member_list.item(j)).getAttribute(OMG_name);
                    _read.append("    this." + member_name + " = "
                                 + XmlType2Java.getTypeReader(type, "is")
                                 + ";\n");
                    _write.append("    "
                                  + XmlType2Java.getTypeWriter(type, "os", "this."
                                  + member_name)
                                  + ";\n");
                }
            }

            buffer
                .append("  public void _read(org.omg.CORBA.portable.InputStream is) {\n");
            if (statefuls > 0)
                buffer.append("    super._read(is);\n");
            buffer.append(_read.toString());
            buffer.append("  }\n\n");

            buffer.append("  public void _write(org.omg.CORBA.portable.OutputStream os) {\n");
            if (statefuls > 0)
                buffer.append("    super._write(os);\n");
            buffer.append(_write.toString());
            buffer.append("  }\n\n");

            buffer.append("  public org.omg.CORBA.TypeCode _type(){\n");
            buffer.append("    return " + pack_name + "Helper.type();\n");
            buffer.append("  }\n\n");
        }

    }

    private void generateJavaExportDef(StringBuffer buffer, Element doc,
                                       String outputDir, String valuetypeName,
                                       String genPackage)
        throws Exception
    {
        // Items definition
        NodeList nodes = doc.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            String tag = el.getTagName();
            if (tag.equals(OMG_op_dcl)) {
                buffer.append("  ");
                generateJavaMethodHeader(buffer, el);
                buffer.append(";\n\n");
            } else if (tag.equals(OMG_attr_dcl)) {
                generateJavaAttributeDecl(buffer, el);
            } else if (tag.equals(OMG_state_member)) {
                generateJavaStateMemberDecl(buffer, el);
            } else if (tag.equals(OMG_const_dcl)) {
                generateJavaConstDecl(buffer, el);
            } else {
                generateJavaSubPackageDef(el, outputDir, valuetypeName, genPackage);
            }
        }
        buffer.append("\n");
    }

    private void generateJavaSubPackageDef(Element doc, String outputDir,
                                           String valuetypeName,
                                           String genPackage)
        throws Exception
    {
        String newPackage;
        if (!genPackage.equals("")) {
            newPackage = genPackage + "." + valuetypeName + "Package";
        } else {
            newPackage = valuetypeName + "Package";
        }
        Element definition = doc;
        String tag = definition.getTagName();
        if (tag.equals(OMG_const_dcl)) {
            XmlConst2Java gen = new XmlConst2Java();
            gen.generateJava(definition, outputDir, newPackage, this.m_generate);
        } else if (tag.equals(OMG_enum)) {
            XmlEnum2Java gen = new XmlEnum2Java();
            gen.generateJava(definition, outputDir, newPackage, this.m_generate);
        } else if (tag.equals(OMG_struct)) {
            XmlStruct2Java gen = new XmlStruct2Java();
            gen.generateJava(definition, outputDir, newPackage, this.m_generate);
        } else if (tag.equals(OMG_union)) {
            XmlUnion2Java gen = new XmlUnion2Java();
            gen.generateJava(definition, outputDir, newPackage, this.m_generate);
        } else if (tag.equals(OMG_exception)) {
            XmlException2Java gen = new XmlException2Java();
            gen.generateJava(definition, outputDir, newPackage, this.m_generate);
        } else if (tag.equals(OMG_typedef)) {
            XmlTypedef2Java gen = new XmlTypedef2Java();
            gen.generateJava(definition, outputDir, newPackage, this.m_generate);
        }
    }

    private void generateJavaAttributeDecl(StringBuffer buffer, Element doc)
    {
        // Get type
        NodeList nodes = doc.getChildNodes();
        String type = XmlType2Java.getType((Element) nodes.item(0));
        String readonly = doc.getAttribute(OMG_readonly);

        // Accessors generation
        for (int i = 1; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            String name = el.getAttribute(OMG_name);
            buffer.append("  public abstract " + type + " " + name + "();\n"); 
            // DAVV
            // -
            // a�adido
            // 'public
            // anstract'
            if (readonly == null || !readonly.equals(OMG_true))
                buffer.append("  public abstract void " + name + "(" + type
                              + " value);\n"); 
            // DAVV - a�adido 'public
            // abstract'
            buffer.append("\n");
        }
    }

    private void generateJavaStateMemberDecl(StringBuffer buffer, Element doc)
    {
        // Get type
        NodeList nodes = doc.getChildNodes();
        String type = XmlType2Java.getType((Element) nodes.item(0));
        String kind = doc.getAttribute(OMG_kind);

        // Accessors generation
        for (int i = 1; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            String name = el.getAttribute(OMG_name);
            if (kind.equals(OMG_private))
                buffer.append("  protected " + type + " " + name + ";\n");
            else if (kind.equals(OMG_public))
                buffer.append("  public " + type + " " + name + ";\n");
            buffer.append("\n");
        }
    }

    private void generateJavaConstDecl(StringBuffer buffer, Element doc)
        throws SemanticException
    {
        NodeList nodes = doc.getChildNodes();
        String scopedName = doc.getAttribute(OMG_scoped_name);

        // Value generation
        Element typeEl = (Element) nodes.item(0);
        String type = XmlType2Java.getType(typeEl);
        buffer.append("  ");
        buffer.append(type);
        buffer.append(" ");
        buffer.append(doc.getAttribute(OMG_name));
        buffer.append(" = (");
        buffer.append(type);
        buffer.append(")");

        // Expre generation
        Element exprEl = (Element) nodes.item(1);
        Object expr = XmlExpr2Java.getExpr(exprEl, type);
        IdlConstants.getInstance().add(scopedName, type, expr);
        buffer.append(XmlExpr2Java.toString(expr, type));
        buffer.append(";\n\n");
    }

    private String generateJavaHelperDef(Element doc, String genPackage)
        throws Exception
    {
        StringBuffer buffer = new StringBuffer();
        String name = doc.getAttribute(OMG_name);
        String isBoxedS = doc.getAttribute(OMG_boxed);
        boolean isBoxed = (isBoxedS != null) && (isBoxedS.equals(OMG_true));

        XmlJavaHeaderGenerator.generate(buffer, "helper", name + "Helper", genPackage);

        if (!isBoxed)
            buffer.append(generateJavaValuetypeHelper(doc, genPackage));
        else
            buffer.append(generateJavaBoxedValuetypeHelper(doc, genPackage));

        return buffer.toString();
    }

    private String generateJavaValuetypeHelper(Element doc, String genPackage)
        throws Exception
    {
        StringBuffer buffer = new StringBuffer();
        String name = doc.getAttribute(OMG_name);
        String pack_name;
        if (genPackage.equals(""))
            pack_name = name;
        else
            pack_name = genPackage + "." + name;

        String id = RepositoryIdManager.getInstance().get(doc);
        //Header
        buffer.append("abstract public class " + name + "Helper{\n\n");

        buffer.append("  private static org.omg.CORBA.ORB _orb() {\n");
        buffer.append("      return org.omg.CORBA.ORB.init();\n");
        buffer.append("  }\n\n");
        //_id
        buffer.append("  private static String  _id = \"" + id + "\";\n\n");
        //insert
        buffer.append("  public static void insert(org.omg.CORBA.Any a, "
                      + pack_name + " t){\n");
        buffer.append("    a.insert_Value((java.io.Serializable) t , type());\n");
        buffer.append("  }\n\n");
        //extract
        buffer.append("  public static " + pack_name
                      + " extract(org.omg.CORBA.Any a){\n");
        buffer.append("    java.io.Serializable v = a.extract_Value();\n");
        buffer.append("    if(v instanceof " + pack_name + ")\n");
        buffer.append("      return (" + pack_name + ") v;\n");
        buffer.append("    else \n");
        buffer.append("      throw new org.omg.CORBA.BAD_PARAM(\"Any does not contains a "
                    + pack_name + " value.\");\n");
        buffer.append("  }\n\n");
        //type()
        buffer.append("  private static org.omg.CORBA.TypeCode _type = null;\n");
        buffer.append("  public static org.omg.CORBA.TypeCode type(){\n");
        buffer.append("    if (_type == null){\n");

        //Members
        StringBuffer members = new StringBuffer();
        NodeList listStateMember = doc.getElementsByTagName(OMG_state_member);
        int num_members = 0;
        for (int i = 0; i < listStateMember.getLength(); i++) {
            Element stateMemberEl = (Element) listStateMember.item(i);

            String kind = stateMemberEl.getAttribute(OMG_kind);
            String visibility;
            if (kind.equals(OMG_private))
                visibility = "org.omg.CORBA.PRIVATE_MEMBER.value";
            else
                visibility = "org.omg.CORBA.PUBLIC_MEMBER.value";

            Element type = (Element) stateMemberEl.getFirstChild();
            String type_tag = type.getTagName();
            NodeList list = stateMemberEl.getElementsByTagName(OMG_simple_declarator);
            for (int j = 0; j < list.getLength(); j++) {
                String member_name = ((Element) list.item(j)).getAttribute(OMG_name);

                if (pack_name.equals(XmlType2Java.getType(type))) {
                    members.append("          member_tc = org.omg.CORBA.ORB.init().create_recursive_tc(_id);\n");
                } else {
                    members.append("          member_tc = "
                                   + XmlType2Java.getTypecode(type) + ";\n");
                }
                members.append("          _members[" + num_members
                               + "] = new org.omg.CORBA.ValueMember(\""
                               + member_name
                               + "\", \"\", _id, \"\", member_tc, null, "
                               + visibility + ");\n\n");
                num_members++;
            }
        }
        buffer
            .append("          org.omg.CORBA.ValueMember[] _members = new org.omg.CORBA.ValueMember["
                    + (num_members) + "];\n");
        buffer.append("          org.omg.CORBA.TypeCode member_tc = null;\n\n");
        buffer.append(members.toString());

        //Typecode
        String value_type;
        String isAbstractS = doc.getAttribute(OMG_abstract);
        boolean isAbstract = (isAbstractS != null) && (isAbstractS.equals(OMG_true));
        String isCustomS = doc.getAttribute(OMG_custom);
        boolean isCustom = (isCustomS != null) && (isCustomS.equals(OMG_true));
        String isTruncatableS = doc.getAttribute(OMG_truncatable);
        boolean isTruncatable = (isTruncatableS != null) && (isTruncatableS.equals(OMG_true));
        if (isCustom)
            value_type = "org.omg.CORBA.VM_CUSTOM.value";
        else if (isTruncatable)
            value_type = "org.omg.CORBA.VM_TRUNCATABLE.value";
        else if (isAbstract)
            value_type = "org.omg.CORBA.VM_ABSTRACT.value";
        else
            value_type = "org.omg.CORBA.VM_NONE.value";
        buffer.append("          _type = org.omg.CORBA.ORB.init ().create_value_tc(_id, \""
                    + name + "\", " + value_type + ", null, _members);\n");

        buffer.append("    }\n");
        buffer.append("    return _type;\n");
        buffer.append("  }\n\n");
        //id()
        buffer.append("  public static String id(){\n");
        buffer.append("    return _id;\n");
        buffer.append("  }\n\n");
        //read
        buffer.append("  public static " + pack_name
                      + " read(org.omg.CORBA.portable.InputStream is){\n");
        buffer.append("    return ("
                    + pack_name
                    + ")((org.omg.CORBA_2_3.portable.InputStream) is).read_value(id());\n");
        buffer.append("  }\n\n");
        //write
        buffer.append("  public static void write(org.omg.CORBA.portable.OutputStream os, "
                    + pack_name + " val){\n");
        buffer.append("    ((org.omg.CORBA_2_3.portable.OutputStream) os).write_value(val, id());\n");
        buffer.append("  }\n\n");
        //factories
        NodeList nodes = doc.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            String tag = el.getTagName();
            if (tag.equals(OMG_factory))
                generateHelperJavaFactory(buffer, el, pack_name);
        }

        buffer.append("}\n");
        return buffer.toString();
    }

    private String generateJavaBoxedValuetypeHelper(Element doc,
                                                    String genPackage)
        throws Exception
    {
        StringBuffer buffer = new StringBuffer();
        String name = doc.getAttribute(OMG_name);
        String pack_name;
        if (genPackage.equals(""))
            pack_name = name;
        else
            pack_name = genPackage + "." + name;
        String id = RepositoryIdManager.getInstance().get(doc);
        NodeList nodes = doc.getChildNodes();
        Element typeEl = (Element) nodes.item(0);
        String type = XmlType2Java.getType(typeEl);
        if (!XmlType2Java.isPrimitiveJavaType(type))
            pack_name = type;

        //Header
        buffer.append("public class " + name + "Helper\n");
        buffer.append("   implements org.omg.CORBA.portable.BoxedValueHelper{\n\n");

        buffer.append("  private static org.omg.CORBA.ORB _orb() {\n");
        buffer.append("      return org.omg.CORBA.ORB.init();\n");
        buffer.append("  }\n\n");
        //_id
        buffer.append("  private static String  _id = \"" + id + "\";\n\n");
        //_instance
        buffer.append("  private static " + name + "Helper _instance = new "
                      + name + "Helper();\n\n");
        //insert
        buffer.append("  public static void insert(org.omg.CORBA.Any a, "
                      + pack_name + " t){\n");
        buffer.append("    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();\n");
        buffer.append("    a.type (type ());\n");
        buffer.append("    write (out, t);\n");
        buffer.append("    a.read_value (out.create_input_stream (), type ());\n");
        buffer.append("  }\n\n");
        //extract
        buffer.append("  public static " + pack_name
                      + " extract(org.omg.CORBA.Any a){\n");
        buffer.append("    return read (a.create_input_stream ());\n");
        buffer.append("  }\n\n");
        //type
        buffer.append("  private static org.omg.CORBA.TypeCode _type = null;\n");
        buffer.append("  public static org.omg.CORBA.TypeCode type(){\n");
        buffer.append("    if (_type == null){\n");

        buffer.append("          _type = "
                      + XmlType2Java.getTypecode((Element) doc.getFirstChild())
                      + ";\n");
        buffer.append("          _type = org.omg.CORBA.ORB.init ().create_value_box_tc (_id, \""
                    + name + "\", _type);");
        buffer.append("    }\n");
        buffer.append("    return _type;\n");
        buffer.append("  }\n\n");
        //id()
        buffer.append("  public static String id(){\n");
        buffer.append("    return _id;\n");
        buffer.append("  }\n\n");
        //read
        buffer.append("  public static " + pack_name
                      + " read(org.omg.CORBA.portable.InputStream is){\n");
        buffer.append("    if (!(is instanceof org.omg.CORBA_2_3.portable.InputStream)){\n");
        buffer.append("      throw new org.omg.CORBA.BAD_PARAM();\n    }\n");
        buffer.append("    return ("
                    + pack_name
                    + ")((org.omg.CORBA_2_3.portable.InputStream)is).read_value(_instance);\n");
        buffer.append("  }\n\n");
        //write
        buffer.append("  public static void write(org.omg.CORBA.portable.OutputStream os, "
                    + pack_name + " val){\n");
        buffer.append("    if (!(os instanceof org.omg.CORBA_2_3.portable.OutputStream)){\n");
        buffer.append("      throw new org.omg.CORBA.BAD_PARAM();\n    }\n");
        buffer.append("    ((org.omg.CORBA_2_3.portable.OutputStream)os).write_value(val, _instance);\n");
        buffer.append("  }\n\n");
        //read_value
        buffer.append("  public java.io.Serializable read_value(org.omg.CORBA.portable.InputStream is){\n");
        if (((Element) doc.getFirstChild()).getTagName().equals(OMG_sequence)) {

            buffer.append("    "
                          + XmlType2Java.getType((Element) doc.getFirstChild())
                          + " result;\n");
            getSeqType((Element) doc.getFirstChild(), buffer,
                       new StructReader());
            //buffer.append(" return new "+pack_name+"(result);\n"); DAVV
            buffer.append("    return result;\n");
        } else {
            if (XmlType2Java.isPrimitiveJavaType(type)) // DAVV
                buffer.append("    return new "
                              + pack_name
                              + "("
                              + XmlType2Java.getTypeReader((Element) doc
                                  .getFirstChild(), "is") + ");\n");
            else
                buffer.append("    return "
                              + XmlType2Java.getTypeReader((Element) doc
                                  .getFirstChild(), "is") + ";\n");
        }
        buffer.append("  }\n\n");
        //write_value
        buffer.append("  public void write_value(org.omg.CORBA.portable.OutputStream os, java.io.Serializable _value){\n");
        buffer.append("    if (_value instanceof " + pack_name + "){\n");
        if (((Element) doc.getFirstChild()).getTagName().equals(OMG_sequence)) {

            buffer.append("    "
                          + XmlType2Java.getType((Element) doc.getFirstChild())
                          + " val = (" + pack_name + ") _value;\n");
            getSeqType(((Element) doc.getFirstChild()), buffer,
                       new StructWriter());
        } else {
            if (XmlType2Java.isPrimitiveJavaType(type)) // DAVV
                buffer.append("             "
                              + XmlType2Java.getTypeWriter((Element) doc
                                  .getFirstChild(), "os", "((" + pack_name
                                                          + ") _value).value")
                              + ";\n");
            else
                buffer.append("             "
                            + XmlType2Java.getTypeWriter((Element) doc
                                .getFirstChild(), "os", "(" + pack_name
                                                        + ") _value") + ";\n");
        }
        buffer.append("    }\n");
        buffer.append("    else throw new org.omg.CORBA.BAD_PARAM();\n");
        buffer.append("  }\n\n");
        //get_id
        buffer.append("  public java.lang.String get_id(){\n");
        buffer.append("    return _id;\n");
        buffer.append("  }\n\n");
        buffer.append("}\n");

        return buffer.toString();
    }

    private void getSeqType(Element seq, StringBuffer buffer,
                            StructProcessor processor)
        throws Exception
    {
        String objectName = null;
        Element classType = null;
        NodeList nodes = seq.getParentNode().getChildNodes();
        Vector indexes = new Vector();
        Vector isArray = new Vector();

        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            String tag = el.getTagName();
            if (!tag.equals(OMG_sequence) && !tag.equals(OMG_array))
                continue;
            indexes.removeAllElements();
            isArray.removeAllElements();
            int val = i;
            while (tag.equals(OMG_sequence)) {
                el = (Element) el.getFirstChild();
                Element expr = (Element) el.getNextSibling();
                if (expr != null) {
                    indexes.addElement(new Long(XmlExpr2Java.getIntExpr(expr)));
                } else {
                    indexes.addElement(new String("length" + val));
                }
                isArray.addElement(new Boolean(false));
                tag = el.getTagName();
                val++;
            }
            classType = el;
            objectName = null;
            processor.generateJava(buffer, objectName, classType, indexes, isArray);
        }
    }

    private String generateJavaHolderDef(Element doc, String genPackage)
    {
        StringBuffer buffer = new StringBuffer();
        String name = doc.getAttribute(OMG_name);
        String isBoxedS = doc.getAttribute(OMG_boxed);
        boolean isBoxed = (isBoxedS != null) && (isBoxedS.equals(OMG_true));

        if (!isBoxed)
            buffer.append(XmlJavaHolderGenerator.generate(genPackage, name, name));
        else {
            NodeList nodes = doc.getChildNodes();
            Element typeEl = (Element) nodes.item(0);
            String type = XmlType2Java.getType(typeEl);
            if (XmlType2Java.isPrimitiveJavaType(type))
            	// DAVV - s�lo para
            	// 'value boxes' de
            	// tipos primitivos
                buffer.append(XmlJavaHolderGenerator.generate(genPackage, name,
                                                              name));
            else {
                buffer.append(XmlJavaHolderGenerator.generate(genPackage, name,
                                                              type));
            }
        }

        return buffer.toString();
    }

    private String generateJavaValuetypeHolder(Element doc, String genPackage)
    {
        StringBuffer buffer = new StringBuffer();
        String name = doc.getAttribute(OMG_name);
        String pack_name;
        if (genPackage.equals(""))
            pack_name = name;
        else
            pack_name = genPackage + "." + name;
        //Header
        buffer.append("final public class " + name + "Holder\n");
        buffer.append("   implements org.omg.CORBA.portable.Streamable{\n\n");
        //value
        buffer.append("  public " + pack_name + " value = null;\n\n");
        //Holder
        buffer.append("  public " + name + "Holder(){}\n\n");
        //Holder()
        buffer.append("  public " + name + "Holder(" + pack_name
                      + " initial){\n");
        buffer.append("    value = initial;");
        buffer.append("  }\n\n");
        //_read
        buffer.append("  public void _read(org.omg.CORBA.portable.InputStream is){\n");
        buffer.append("    value = " + pack_name + "Helper.read(is);\n");
        buffer.append("  }\n\n");
        //_write
        buffer.append("  public void _write(org.omg.CORBA.portable.OutputStream os){\n");
        buffer.append("    " + pack_name + "Helper.write(os,value);\n");
        buffer.append("  }\n\n");
        //_type()
        buffer.append("  public org.omg.CORBA.TypeCode _type(){\n");
        buffer.append("    return " + pack_name + "Helper.type();\n");
        buffer.append("  }\n\n");

        buffer.append("}\n");
        return buffer.toString();
    }

    private String generateJavaBoxedValuetypeHolder(Element doc,
                                                    String genPackage)
    {
        StringBuffer buffer = new StringBuffer();
        String name = doc.getAttribute(OMG_name);
        NodeList nodes = doc.getChildNodes();
        Element typeEl = (Element) nodes.item(0);
        String type = XmlType2Java.getType(typeEl);
        String pack_name;
        if (genPackage.equals(""))
            pack_name = name;
        else
            pack_name = genPackage + "." + name;
        //Header
        buffer.append("final public class " + name + "Holder\n");
        buffer.append("   implements org.omg.CORBA.portable.Streamable{\n\n");
        //value
        buffer.append("  public " + type + " value;\n\n");
        //Holder
        buffer.append("  public " + name + "Holder(){}\n\n");
        //Holder()
        buffer.append("  public " + name + "Holder(" + name + " initial){}\n\n");
        //_read
        buffer.append("  public void _read(org.omg.CORBA.portable.InputStream is){\n");
        buffer.append("    value = " + pack_name + "Helper.read(is);\n");
        buffer.append("  }\n\n");
        //_write
        buffer.append("  public void _write(org.omg.CORBA.portable.OutputStream os){\n");
        buffer.append("    " + pack_name + "Helper.write(os,value);\n");
        buffer.append("  }\n\n");
        //_type
        buffer.append("  public org.omg.CORBA.TypeCode _type(){\n");
        buffer.append("    return " + pack_name + "Helper.type();\n");
        buffer.append("  }\n\n");
        buffer.append("}\n");

        return buffer.toString();
    }

    private String generateJavaFactoryDef(Element doc, String genPackage)
    {
        int factories = 0;
        StringBuffer buffer = new StringBuffer();
        String name = doc.getAttribute(OMG_name);
        String isBoxedS = doc.getAttribute(OMG_boxed);
        boolean isBoxed = (isBoxedS != null) && (isBoxedS.equals(OMG_true));
        if (isBoxed)
            return null;

        XmlJavaHeaderGenerator.generate(buffer, "valuetype factory",
                                        name + "ValueFactory", genPackage);
        buffer.append("public interface " + name + "ValueFactory\n");
        buffer.append("   extends org.omg.CORBA.portable.ValueFactory{\n\n");
        NodeList nodes = doc.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            String tag = el.getTagName();
            if (tag.equals(OMG_factory)) {
                buffer.append("  ");
                generateJavaFactoryHeader(buffer, el, name);
                buffer.append(";\n\n");
                factories++;
            }
        }
        buffer.append("}\n");
        if (factories == 0)
            return null;
        return buffer.toString();
    }

}