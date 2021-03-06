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
import es.tid.TIDIdlc.util.Traces;
import es.tid.TIDIdlc.xml2java.structures.*;
import es.tid.TIDIdlc.xml2java.unions.*;

import java.io.*;
import java.util.StringTokenizer;
import java.util.Vector;

import org.w3c.dom.*;

/**
 * Generates Java for union declarations.
 */
class XmlUnion2Java
    implements Idl2XmlNames
{

    private boolean m_generate;

    /** Generate Java */
    public void generateJava(Element doc, String outputDirectory,
                             String genPackage, boolean generateCode)
        throws Exception
    {

        if (doc.getAttribute(OMG_fwd).equals(OMG_true))
            return;

        m_generate = generateCode;

        // Get package components
        String targetDirName = outputDirectory;
        if (targetDirName.charAt(targetDirName.length() - 1) == File.separatorChar) {
            targetDirName = targetDirName.substring(0, targetDirName.length() - 1);
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

        // Union generation
        fileName = doc.getAttribute(OMG_name) + ".java";
        if (generateCode) {
            Traces.println("XmlUnion2Java:->", Traces.DEEP_DEBUG);
            Traces.println("Generating : " + targetDirName + File.separatorChar
                           + fileName + "...", Traces.USER);
        }
        contents = generateJavaUnionDef(doc, outputDirectory, genPackage);
        if (generateCode) {
            writer = new FileWriter(targetDirName + File.separatorChar
                                    + fileName);
            buf_writer = new BufferedWriter(writer);
            buf_writer.write(contents);
            buf_writer.close();
        }

        // UnionHolder generation
        fileName = doc.getAttribute(OMG_name) + "Holder" + ".java";
        if (generateCode) {
            Traces.println("XmlUnion2Java:->", Traces.DEEP_DEBUG);
            Traces.println("Generating : " + targetDirName + File.separatorChar
                           + fileName + "...", Traces.USER);
        }
        String name = doc.getAttribute(OMG_name);
        contents = XmlJavaHolderGenerator.generate(genPackage, name, name);
        if (generateCode) {
            writer = new FileWriter(targetDirName + File.separatorChar
                                    + fileName);
            buf_writer = new BufferedWriter(writer);
            buf_writer.write(contents);
            buf_writer.close();
        }

        // UnionHelper generation
        fileName = doc.getAttribute(OMG_name) + "Helper" + ".java";
        if (generateCode) {
            Traces.println("XmlUnion2Java:->", Traces.DEEP_DEBUG);
            Traces.println("Generating : " + targetDirName + File.separatorChar
                           + fileName + "...", Traces.USER);
        }
        contents = generateJavaHelperDef(doc, genPackage, outputDirectory);
        if (generateCode) {
            writer = new FileWriter(targetDirName + File.separatorChar
                                    + fileName);
            buf_writer = new BufferedWriter(writer);
            buf_writer.write(contents);
            buf_writer.close();
        }

        generateJavaSubPackageDef(doc, outputDirectory, name, genPackage,
                                  generateCode);

    }

    private String generateJavaUnionDef(Element doc, String outputDirectory,
                                        String genPackage)
        throws Exception
    {
        StringBuffer buffer = new StringBuffer();
        String name = doc.getAttribute(OMG_name);
        String discriminatorType = getDiscriminatorType(doc, name,
                                                        outputDirectory,
                                                        genPackage);
        Element defaultEl = null;

        // Package header
        XmlJavaHeaderGenerator.generate(buffer, "union", name, genPackage);

        // Class header
        buffer.append("final public class ");
        buffer.append(name);
        buffer.append("\n   implements org.omg.CORBA.portable.IDLEntity {\n\n");
        buffer.append("  private " + discriminatorType + " _discriminator;\n");
        buffer.append("  private java.lang.Object _union_value;\n");
        buffer.append("  protected boolean _isDefault = true;\n\n");
        // Items definition
        Union union = UnionManager.getInstance().get(doc);
        Vector switchBody = union.getSwitchBody();
        String objectName = null, classType = null;
        for (int i = 0; i < switchBody.size(); i++) {
            UnionCase union_case = (UnionCase) switchBody.elementAt(i);
            Element type = union_case.m_type_spec;
            Element decl = union_case.m_declarator;
            String decl_tag = decl.getTagName();
            String type_tag = type.getTagName();
            if (decl_tag.equals(OMG_simple_declarator)) {
                objectName = decl.getAttribute(OMG_name);
            } else if (decl_tag.equals(OMG_array)) {
                objectName = decl.getAttribute(OMG_name) + "[]";
            }
            if (type_tag.equals(OMG_enum)) {
                // Added to support the declaration of enumerations into a Union body
                String newPackage;
                if (!genPackage.equals("")) {
                    newPackage = genPackage + "." + name + "Package";
                } else {
                    newPackage = name + "Package";
                }
                XmlEnum2Java gen = new XmlEnum2Java();
                gen.generateJava(type, outputDirectory, newPackage,
                                 this.m_generate);

            } else if (type_tag.equals(OMG_struct)) {
                // Added to support the declaration of structs into a Union body
                String newPackage;
                if (!genPackage.equals("")) {
                    newPackage = genPackage + "." + name + "Package";
                } else {
                    newPackage = name + "Package";
                }
                XmlStruct2Java gen = new XmlStruct2Java();
                gen.generateJava(type, outputDirectory, newPackage,
                                 this.m_generate);
            }

            classType = XmlType2Java.getType(type);
            // The accesor method for each branch
            generateJavaUnionAccesorMethod(buffer, classType, objectName,
                                           union_case, discriminatorType, union);

            // The modifier method for each branch
            buffer.append("  public void " + objectName + "(" + classType
                          + " value){\n");
            generateJavaUnionMutatorMethod(buffer, classType, objectName,
                                           union_case, discriminatorType,
                                           union, true);

            // The modifier method for each branch which has more than one case
            // label
            if (union_case.m_case_labels.size() > 1) {
                buffer.append("  public void " + objectName + "("
                              + discriminatorType + " discriminator, "
                              + classType + " value){\n");
                generateJavaUnionMutatorMethod(buffer, classType, objectName,
                                               union_case, discriminatorType,
                                               union, false);
            }
            buffer.append("\n");
        }

        if (!union.getHasDefault() && union.getDefaultAllowed()) {
            buffer.append("  public void __default(){\n");
            generateJavaUnionDefaultMethod(buffer, classType, objectName,
            		discriminatorType, union, true);
            buffer.append("  public void __default(" + discriminatorType + " discriminator){\n");
            generateJavaUnionDefaultMethod(buffer, classType, objectName,
            		discriminatorType, union, false);

        }

        buffer.append("  public " + discriminatorType + " discriminator(){\n");
        buffer.append("     return _discriminator;\n");
        buffer.append("  }\n\n");

        buffer.append("}\n");

        return buffer.toString();
    }

    private void generateJavaUnionAccesorMethod(StringBuffer buffer,
                                                String classType,
                                                String objectName,
                                                UnionCase union_case,
                                                String discriminatorType,
                                                Union union)
        throws Exception
    {
        Element doc = union.getUnionElement();
        Element type = union_case.m_type_spec;
        Element decl = union_case.m_declarator;
        Vector case_labels = union_case.m_case_labels;
        Element discriminator_type = (Element) doc.getFirstChild().getFirstChild();
        String inverseClassType = getInverseType(type, true);

        buffer.append("  public " + classType + " " + objectName + "(){\n");
        buffer.append("    if ");
        int case_size = case_labels.size();
        if (case_size > 1) {
            buffer.append("(");
        }
        for (int i = 0; i < case_size; i++) {
            if (i > 0)
                buffer.append(" || ");
            Element case_label = (Element) case_labels.elementAt(i);
            String value = null;
            try {
                Object expr = XmlExpr2Java.getExpr(case_label.getParentNode(),
                                                   union.getDiscKind());
                value = expr.toString();
            }
            catch (SemanticException e) {}

            if ((value == null) || value.equals("")) {
                // enum or boolean value (comprobar)
                String dis_name = discriminator_type.getTagName();
                if (dis_name.equals(OMG_scoped_name)) {
                    dis_name = union.getScopedDiscrimKind();
                }
                if (dis_name.equals(OMG_enum)) {
                    value = discriminatorType + "._"
                            + removeScope(case_label.getAttribute(OMG_name));
                    buffer.append("(_discriminator.value() == " + value + ")");
                } else if (dis_name.equals(OMG_char)
                           || dis_name.equals(OMG_wchar)) {
                    buffer.append("(_discriminator == '" + value + "')");
                } else if (union.getDiscKind().equals("boolean")) {
                    value = case_label.getAttribute(OMG_value);
                    buffer.append("(_discriminator == " + value.toLowerCase()
                                  + ")");
                } else {
                    value = discriminatorType + "._"
                            + removeScope(case_label.getAttribute(OMG_name));
                    buffer.append("(_discriminator == " + value + ")");
                }
            } else {
                String case_type = case_label.getTagName();
                if (case_type.equals(OMG_character_literal)) {
                    buffer.append("(_discriminator == '" + value + "')");
                } else {
                    String name;
                    if (union.getScopedDiscriminator() == null)
                        name = discriminator_type.getAttribute(OMG_kind);
                    else
                        name = union.getScopedDiscrimKind();
                    if (name.equals(OMG_longlong)
                        || name.equals(OMG_unsignedlonglong))
                        value += "L";
                    else if (union.getDiscKind().equals("boolean"))
                        value = case_label.getAttribute(OMG_value);
                    buffer.append("(_discriminator == " + value.toLowerCase()
                                  + ")");
                }
            }
        }
        if (case_size > 1) {
            buffer.append(")");
        }
        if (union_case.m_is_default) {
            buffer.append("(_isDefault)");
        }
        buffer.append(" {\n");
        buffer.append("       return " + inverseClassType + "\n");
        buffer.append("    }\n");
        buffer.append("    throw new org.omg.CORBA.BAD_OPERATION(\""
                      + objectName + "\");\n");
        buffer.append("  }\n\n");
    }

    private void generateJavaUnionMutatorMethod(StringBuffer buffer,
                                                String classType,
                                                String objectName,
                                                UnionCase union_case,
                                                String discriminatorType,
                                                Union union,
                                                boolean hasOneCaseLabel)
        throws Exception
    {
        Element doc = union.getUnionElement();
        Element type = union_case.m_type_spec;
        Element decl = union_case.m_declarator;
        Vector case_labels = union_case.m_case_labels;
        Element discriminator_type = (Element) doc.getFirstChild().getFirstChild();
        String inverseClassType = getInverseType(type, false);
        String value = "";

        if (union_case.m_is_default) {
            buffer.append("    _discriminator = ");
            if (!hasOneCaseLabel)
                value = "discriminator";
            else {
                String name;
                if (union.getScopedDiscriminator() == null)
                    name = discriminator_type.getAttribute(OMG_kind);
                else
                    name = union.getScopedDiscrimKind();

                if ((name == null) || name.equals("") || name.equals(OMG_enum))
                    // We have an enumeration
                    value = discriminatorType + "." + union.getDefaultValue();
                else if (name.equals(OMG_char) || name.equals(OMG_wchar)) {
                    // We have a char/wchar discriminator
                    value = "'" + union.getDefaultValue() + "'";
                } else {
                    // We have a basic discriminator
                    value = union.getDefaultValue().toLowerCase();
                    if (name.equals(OMG_longlong)
                        || name.equals(OMG_unsignedlonglong))
                        value += "L";
                }
            }
            buffer.append(value);
            buffer.append(";\n");
            buffer.append("    _union_value = " + inverseClassType
                          + "(value);\n");
            buffer.append("    _isDefault = true;\n");
        } else {
            Element case_label = (Element) case_labels.elementAt(0);
            try {
                Object expr = XmlExpr2Java.getExpr(case_label.getParentNode(),
                                                   union.getDiscKind());
                value = expr.toString();
            }
            catch (SemanticException e) {}

            if ((value == null) || value.equals("")) {
                String dis_name = discriminator_type.getTagName();
                if (dis_name.equals(OMG_scoped_name)) {
                    dis_name = union.getScopedDiscrimKind();
                }
                if (dis_name.equals(OMG_enum)) {
                    value = discriminatorType + "."
                            + removeScope(case_label.getAttribute(OMG_name));
                    buffer.append("    _discriminator = ");
                    if (!hasOneCaseLabel)
                        value = "discriminator";
                    buffer.append(value);
                    buffer.append(";\n");
                } else if (union.getDiscKind().equals("boolean")) {
                    // Provisional hasta que funcionen las expresiones booleanas
                    value = case_label.getAttribute(OMG_value);
                    buffer.append("    _discriminator = ");
                    if (!hasOneCaseLabel)
                        value = "discriminator";
                    buffer.append(value.toLowerCase());
                    buffer.append(";\n");
                } else {
                    value = discriminatorType + "."
                            + removeScope(case_label.getAttribute(OMG_name));
                    buffer.append("    _discriminator = ");
                    if (!hasOneCaseLabel)
                        value = "discriminator";
                    buffer.append(value);
                    buffer.append(";\n");
                }
            } else {
                buffer.append("    _discriminator = ");
                String case_type = case_label.getTagName();
                if (!hasOneCaseLabel)
                    value = "discriminator";
                String name;
                if (union.getScopedDiscriminator() == null)
                    name = discriminator_type.getAttribute(OMG_kind);
                else
                    name = union.getScopedDiscrimKind();
                if (name.equals(OMG_longlong)
                    || name.equals(OMG_unsignedlonglong))
                    value += "L";
                if (case_type.equals(OMG_character_literal) && hasOneCaseLabel) {
                    value = "'" + value + "'";
                    buffer.append(value);
                } else
                    buffer.append(value.toLowerCase());
                buffer.append(";\n");
            }
            buffer.append("    _union_value = " + inverseClassType
                          + "(value);\n");
            buffer.append("    _isDefault = false;\n");
        }
        buffer.append("  }\n\n");
    }

    private void generateJavaUnionDefaultMethod(StringBuffer buffer,
                                                String classType,
                                                String objectName,
                                                String discriminatorType,
                                                Union union, boolean noArgs)
        throws Exception
    {
        Element doc = union.getUnionElement();
        Element discriminator_type = (Element) doc
            .getFirstChild().getFirstChild();
        String value = "";
        if (noArgs) {
            buffer.append("    _discriminator = ");
            String name;
            if (union.getScopedDiscriminator() == null)
                name = discriminator_type.getAttribute(OMG_kind);
            else
                name = union.getScopedDiscrimKind();

            if ((name == null) || name.equals("") || name.equals(OMG_enum))
                // We have an enumeration
                value = discriminatorType + "." + union.getDefaultValue();
            else if (name.equals(OMG_char) || name.equals(OMG_wchar)) {
                // We have a char/wchar discriminator
                value = "'" + union.getDefaultValue() + "'";
            } else {
                // We have a basic discriminator
                value = union.getDefaultValue().toLowerCase();
                if (name.equals(OMG_longlong)
                    || name.equals(OMG_unsignedlonglong))
                    value += "L";
            }
            buffer.append(value + ";\n");
        } else {
            buffer.append("    if(");
            Vector switchBody = union.getSwitchBody();
            int case_size = switchBody.size();
            for (int z = 0; z < switchBody.size(); z++) {
                UnionCase union_case = (UnionCase) switchBody.elementAt(z);
                if (union_case.m_is_default)
                    continue; // default no tiene valor
                buffer.append("(");
                Vector case_labels = union_case.m_case_labels;
                for (int i = 0; i < case_labels.size(); i++) {
                    Element case_label = (Element) case_labels.elementAt(i);
                    value = null;
                    try {
                        Object expr = XmlExpr2Java.getExpr(case_label
                            .getParentNode(), union.getDiscKind());
                        value = expr.toString();
                    }
                    catch (SemanticException e) {}
                    if (i > 0)
                        buffer.append("(");
                    if ((value == null) || value.equals("")) {
                        String dis_name = discriminator_type.getTagName();
                        if (dis_name.equals(OMG_scoped_name)) {
                            dis_name = union.getScopedDiscrimKind();
                        }
                        if (dis_name.equals(OMG_enum)) {
                            value = discriminatorType
                                    + "._"
                                    + removeScope(case_label
                                        .getAttribute(OMG_name));
                            buffer.append("discriminator.value() == " + value);
                        } else if (union.getDiscKind().equals("boolean")) {
                            value = case_label.getAttribute(OMG_value);
                            buffer.append("discriminator == "
                                          + value.toLowerCase());
                        } else {
                            value = discriminatorType
                                    + "._"
                                    + removeScope(case_label
                                        .getAttribute(OMG_name));
                            buffer.append("discriminator == " + value);
                        }
                    } else {
                        String case_type = case_label.getTagName();
                        String name;
                        if (union.getScopedDiscriminator() == null)
                            name = discriminator_type.getAttribute(OMG_kind);
                        else
                            name = union.getScopedDiscrimKind();
                        if (name.equals(OMG_longlong)
                            || name.equals(OMG_unsignedlonglong))
                            value += "L";
                        if (case_type.equals(OMG_character_literal)) {
                            value = "'" + value + "'";
                            buffer.append("discriminator == " + value);
                        } else
                            buffer.append("discriminator == "
                                          + value.toLowerCase());
                    }
                    if (i < case_labels.size() - 1)
                        buffer.append(")||");

                }
                if (((z < switchBody.size() - 1) && (!union.getHasDefault()))
                    || (z < switchBody.size() - 2))
                    buffer.append(")||\n   ");
            }
            buffer.append("))\n\t");
            buffer.append(" {\n");
            buffer
                .append("      throw new org.omg.CORBA.BAD_OPERATION(\"__default\");\n");
            buffer.append("    }\n");
            buffer.append("    _discriminator = discriminator;\n");
        }
        buffer.append("    _union_value = null;\n");
        buffer.append("    _isDefault = true;\n");
        buffer.append("  }\n\n");
    }

    private static String getInverseType(Element doc, boolean accesor)
    {
        String tag = doc.getTagName();
        if (tag.equals(OMG_type)) {
            if (accesor)
                return accesorInverseMapping(doc.getAttribute(OMG_kind));
            else
                return mutatorInverseMapping(doc.getAttribute(OMG_kind));
        } else if (tag.equals(OMG_scoped_name)) {
            if (accesor)
                return accesorInverseMapping(XmlType2Java.getUnrolledName(doc));
            else
                return mutatorInverseMapping(XmlType2Java.getUnrolledName(doc));
        }
        // Added to support the declaration of sequences into a Unoin Body
        else if (tag.equals(OMG_sequence)) {
            NodeList nodes = doc.getChildNodes();
            Element type = (Element) nodes.item(0);
            if (accesor) {
                String inverse = XmlType2Java.basicMapping(type
                    .getAttribute(OMG_kind));
                return "(" + inverse + "[])_union_value;";
            } else
                return "";
        }
        // Added to support the declaration of enumerations, structs into a
        // Union body // DAVV - o una union...
        else if (tag.equals(OMG_enum) || tag.equals(OMG_struct)
                 || tag.equals(OMG_union)) {
            if (accesor)
                return accesorInverseMapping(XmlType2Java.getType(doc));
            else
                return mutatorInverseMapping(XmlType2Java.getType(doc));
        } else
            return "unknownType";
    }

    private static String accesorInverseMapping(String type)
    {
        if (type.equals(OMG_wchar)) {
            return "((java.lang.Character)_union_value).charValue();";
        } else if (type.equals(OMG_char)) {
            return "((java.lang.Character)_union_value).charValue();";
        } else if (type.equals(OMG_octet)) {
            return "((java.lang.Byte)_union_value).byteValue();";
        } else if (type.equals(OMG_string)) {
            return "(java.lang.String)_union_value;";
        } else if (type.equals(OMG_wstring)) {
            return "(java.lang.String)_union_value;";
        } else if (type.equals(OMG_short)) {
            return "((java.lang.Short)_union_value).shortValue();";
        } else if (type.equals(OMG_unsignedshort)) {
            return "((java.lang.Short)_union_value).shortValue();";
        } else if (type.equals(OMG_long)) {
            return "((java.lang.Integer)_union_value).intValue();";
        } else if (type.equals(OMG_unsignedlong)) {
            return "((java.lang.Integer)_union_value).intValue();";
        } else if (type.equals(OMG_longlong)) {
            return "((java.lang.Long)_union_value).longValue();";
        } else if (type.equals(OMG_unsignedlonglong)) {
            return "((java.lang.Long)_union_value).longValue();";
        } else if (type.equals(OMG_float)) {
            return "((java.lang.Float)_union_value).floatValue();";
        } else if (type.equals(OMG_double)) {
            return "((java.lang.Double)_union_value).doubleValue();";
        } else if (type.equals(OMG_fixed)) {
            return "(java.math.BigDecimal)_union_value;";
        } else if (type.equals(OMG_any)) {
            return "(org.omg.CORBA.Any)_union_value;";
        } else if (type.equals(OMG_boolean)) {
            return "((java.lang.Boolean)_union_value).booleanValue();";
        } else if (type.equals(OMG_Object)) {
            return "(org.omg.CORBA.Object)_union_value;";
        } else if (type.equals(OMG_TypeCode)) {
            return "(org.omg.CORBA.TypeCode)_union_value;";
        }
        return "(" + type + ")_union_value;";
    }

    private static String mutatorInverseMapping(String type)
    {
        if (type.equals(OMG_wchar)) {
            return "new java.lang.Character";
        } else if (type.equals(OMG_char)) {
            return "new java.lang.Character";
        } else if (type.equals(OMG_octet)) {
            return "new java.lang.Byte";
        } else if (type.equals(OMG_string)) {
            return "";
        } else if (type.equals(OMG_wstring)) {
            return "";
        } else if (type.equals(OMG_short)) {
            return "new java.lang.Short";
        } else if (type.equals(OMG_unsignedshort)) {
            return "new java.lang.Short";
        } else if (type.equals(OMG_long)) {
            return "new java.lang.Integer";
        } else if (type.equals(OMG_unsignedlong)) {
            return "new java.lang.Integer";
        } else if (type.equals(OMG_longlong)) {
            return "new java.lang.Long";
        } else if (type.equals(OMG_unsignedlonglong)) {
            return "new java.lang.Long";
        } else if (type.equals(OMG_float)) {
            return "new java.lang.Float";
        } else if (type.equals(OMG_double)) {
            return "new java.lang.Double";
        } else if (type.equals(OMG_fixed)) {
            return "";
        } else if (type.equals(OMG_any)) {
            return "";
        } else if (type.equals(OMG_boolean)) {
            return "new java.lang.Boolean";
        } else if (type.equals(OMG_Object)) {
            return "";
        } else if (type.equals(OMG_TypeCode)) {
            return "";
        }
        return "";
    }

    private String getDiscriminatorType(Element el, String name,
                                        String outputDir, String genPackage)
        throws Exception
    {
        Element discriminator = (Element) el.getFirstChild().getFirstChild();
        String tag = discriminator.getTagName();
        if (tag.equals(OMG_enum)) {
            String enumName = discriminator.getAttribute(OMG_name);
            String newPackage;
            if (!genPackage.equals("")) {
                newPackage = genPackage + "." + name + "Package";
            } else {
                newPackage = name + "Package";
            }
            XmlEnum2Java gen = new XmlEnum2Java();
            gen.generateJava(discriminator, outputDir, newPackage,
                             this.m_generate);
            return newPackage + "." + enumName;
        } else {
            return XmlType2Java.getType(discriminator);
        }
    }

    private String getDiscriminatorTypeCode(Element el, String name,
                                            String outputDir, String genPackage)
        throws Exception
    {
        Element discriminator = (Element) el.getFirstChild().getFirstChild();
        String tag = discriminator.getTagName();
        if (tag.equals(OMG_enum)) {
            return getDiscriminatorType(el, name, outputDir, genPackage)
                   + "Helper.type()";
        } else if (tag.equals(OMG_scoped_name)) {
            String type = getDiscriminatorType(el, name, outputDir, genPackage);
            if (isUnionBasicType(type)
                || XmlType2Java.isPrimitiveJavaType(type))
                return XmlType2Java.getTypecode(discriminator);
            else
                return type + "Helper.type()";
        } else
            return XmlType2Java.getTypecode(discriminator);
    }

    private boolean isUnionBasicType(String type)
    {
        if (type.equals(OMG_short) || type.equals(OMG_long)
            || type.equals(OMG_longlong) || type.equals(OMG_unsignedshort)
            || type.equals(OMG_unsignedlong)
            || type.equals(OMG_unsignedlonglong) || type.equals(OMG_char)
            || type.equals(OMG_wchar) || type.equals(OMG_boolean))
            return true;
        else
            return false;
    }

    private String generateJavaHelperDef(Element doc, String genPackage,
                                         String outputDirectory)
        throws Exception
    {

        String name = doc.getAttribute(OMG_name);
        String discriminatorType = getDiscriminatorType(doc, name,
                                                        outputDirectory,
                                                        genPackage);
        String discriminatorTypeCode = getDiscriminatorTypeCode(
                                                                doc,
                                                                name,
                                                                outputDirectory,
                                                                genPackage);
        Union union = UnionManager.getInstance().get(doc);
        Vector switchBody = union.getSwitchBody();
        StringBuffer buffer = new StringBuffer();
        // Header
        XmlJavaHeaderGenerator.generate(buffer, "helper", name + "Helper",
                                        genPackage);

        // Class header
        buffer.append("abstract public class ");
        buffer.append(name);
        buffer.append("Helper {\n\n");

        XmlJavaHelperGenerator.generateInsertExtract(buffer, name, name
                                                                   + "Holder");
        buffer.append("  private static org.omg.CORBA.ORB _orb() {\n");
        buffer.append("    return org.omg.CORBA.ORB.init();\n");
        buffer.append("  }\n\n");

        buffer.append("  private static org.omg.CORBA.TypeCode _type = null;\n");
        buffer.append("  public static org.omg.CORBA.TypeCode type() {\n");
        buffer.append("     if (_type == null) {\n");

        int numMembers = 0;
        for (int i = 0; i < switchBody.size(); i++) {
            UnionCase union_case = (UnionCase) switchBody.elementAt(i);
            numMembers += union_case.m_case_labels.size();
        }
        if (union.getHasDefault())
            numMembers++;

        buffer.append("       org.omg.CORBA.UnionMember[] members = new org.omg.CORBA.UnionMember["
                    + numMembers + "];\n");
        generateJavaUnionProcess(buffer, doc, new UnionType(union,
                                                            discriminatorType));
        buffer.append("       _type = _orb().create_union_tc(id(), \"");
        buffer.append(name);

        buffer.append("\", " + discriminatorTypeCode);
        buffer.append(", members);\n");
        buffer.append("     };\n");
        buffer.append("     return _type;\n");
        buffer.append("  };\n\n");

        XmlJavaHelperGenerator.generateRepositoryId(buffer, doc);

        generateJavaUnionHelperRead(doc, buffer, union, discriminatorType);
        generateJavaUnionHelperWrite(doc, buffer, union, discriminatorType);

        buffer.append("}\n");
        return buffer.toString();
    }

    private void generateJavaUnionHelperRead(Element doc, StringBuffer buffer,
                                             Union union,
                                             String discriminatorType)
        throws Exception
    {
        String name = doc.getAttribute(OMG_name);
        buffer.append("  public static ");
        buffer.append(name);
        buffer.append(" read(org.omg.CORBA.portable.InputStream is) {\n");

        Element union_el = union.getUnionElement();
        Element switch_el = (Element) union_el.getFirstChild();
        Element discriminator_type = (Element) switch_el.getFirstChild();
        Vector switchBody = union.getSwitchBody();

        // Read the discriminator from the InputStream
        buffer.append("    " + name);
        buffer.append(" _union_result = new " + name + "();\n");
        String read_discrim = XmlType2Java.getTypeReader(discriminator_type,
                                                         "is");
        buffer.append("    " + discriminatorType + " _disc_value = "
                      + read_discrim + ";\n");

        // Read the selected member from the InputStream, according with the
        // discriminator value
        UnionCase union_case = (UnionCase) switchBody.elementAt(switchBody
            .size() - 1);

        String dis_name = discriminator_type.getTagName();
        String dis_type = XmlType2Java.getType(discriminator_type);
        // Boolean, long_long or unsigned_long_long discriminator
        if (dis_type.equals(OMG_boolean) || dis_type.equals("long")) {
            for (int i = 0; i < switchBody.size(); i++) {
                if (i != 0) {
                    buffer.append("\n    else");
                }
                union_case = (UnionCase) switchBody.elementAt(i);
                Vector case_labels = union_case.m_case_labels;
                int j = 0;
                if (union_case.m_is_default) {
                    buffer.append("  {\n");
                } else {
                    for (j = 0; j < case_labels.size(); j++) {
                        Element label = (Element) case_labels.elementAt(j);
                        //String value = label.getAttribute(OMG_value);
                        String value = null;
                        try {
                            Object expr = XmlExpr2Java.getExpr(label.getParentNode(), union.getDiscKind());
                            value = expr.toString();
                        }
                        catch (SemanticException e) {}
                        if (dis_type.equals("long"))
                            value += "L";
                        else
                            value = label.getAttribute(OMG_value);
                        if (j == 0)
                            buffer.append("    if ((_disc_value == "
                                          + value.toLowerCase() + ")");
                        else
                            buffer.append(" || (_disc_value == "
                                          + value.toLowerCase() + ")");
                    }
                    buffer.append(") {\n");
                }
                Element type = union_case.m_type_spec;
                Element decl = union_case.m_declarator;
                String defType = XmlType2Java.getType(type);
                String defName = decl.getAttribute(OMG_name);
                String read_def;
                if (type.getTagName().equals(OMG_sequence)) {
                    buffer.append("       " + defType + " result;\n");
                    getSeqType(type, buffer, new StructReader());
                } else if (XmlType2Java
                    .getTypedefType(type).equals(Idl2XmlNames.OMG_string)
                           && (type.getFirstChild() != null)) {
                    buffer.append("       " + defType + " result;\n");
                    getBoundedStringType(type, buffer, new StructReader());
                } else {
                    read_def = XmlType2Java.getTypeReader(type, "is");
                    buffer.append("       " + defType + " _tmp = " + read_def + ";\n");
                }
                if (j > 1) {
                    if (type.getTagName().equals(OMG_sequence))
                        buffer.append("       _union_result." + defName
                                      + "(_disc_value,result);\n");
                    else if (XmlType2Java.getTypedefType(type).equals(Idl2XmlNames.OMG_string)
                             && (type.getFirstChild() != null))
                        buffer.append("       _union_result." + defName
                                      + "(_disc_value,result);\n");
                    else
                        buffer.append("       _union_result." + defName
                                      + "(_disc_value,_tmp);\n");
                } else {
                    if (type.getTagName().equals(OMG_sequence))
                        buffer.append("       _union_result." + defName
                                      + "(result);\n");
                    else if (XmlType2Java
                        .getTypedefType(type).equals(Idl2XmlNames.OMG_string)
                             && (type.getFirstChild() != null))
                        buffer.append("       _union_result." + defName
                                      + "(result);\n");
                    else
                        buffer.append("       _union_result." + defName
                                      + "(_tmp);\n");
                }
                buffer.append("    }\n");
            }
            if (!union.getHasDefault() && union.getDefaultAllowed()) {
                buffer.append("\n    else {\n");
                buffer.append("       _union_result.__default(_disc_value);\n");
                buffer.append("    }\n");
            }
        } else {
            if (dis_name.equals(OMG_scoped_name)) {
                dis_name = union.getScopedDiscrimKind();
            }
            if (dis_name.equals(OMG_enum)) {
                buffer.append("    switch (_disc_value.value()){\n");
            } else {
                buffer.append("    switch (_disc_value){");
            }
            for (int i = 0; i < switchBody.size(); i++) {
                union_case = (UnionCase) switchBody.elementAt(i);
                Vector case_labels = union_case.m_case_labels;
                int j;
                for (j = 0; j < case_labels.size(); j++) {
                    Element label = (Element) case_labels.elementAt(j);
                    String value = null;
                    try {
                        Object expr = XmlExpr2Java.getExpr(label
                            .getParentNode(), union.getDiscKind());
                        value = expr.toString();
                    }
                    catch (SemanticException e) {}
                    if ((value == null) || (value.equals(""))) {
                        value = discriminatorType + "._"
                                + removeScope(label.getAttribute(OMG_name));
                    }
                    if (label.getTagName().equals(OMG_character_literal))
                        value = "'" + value + "'";
                    buffer.append("\n     case " + value + ":");
                }
                if (union_case.m_is_default) {
                    buffer.append("\n     default: ");
                }
                Element type = union_case.m_type_spec;
                Element decl = union_case.m_declarator;
                String defType = XmlType2Java.getType(type);
                String defName = decl.getAttribute(OMG_name);
                buffer.append(" {\n");
                String read_def;
                if (type.getTagName().equals(OMG_sequence)) {
                    buffer.append("       " + defType + " result;\n");
                    getSeqType(type, buffer, new StructReader());
                } else if (XmlType2Java
                    .getTypedefType(type).equals(Idl2XmlNames.OMG_string)
                           && (type.getFirstChild() != null)) {
                    buffer.append("       " + defType + " result;\n");
                    getBoundedStringType(type, buffer, new StructReader());
                } else {
                    read_def = XmlType2Java.getTypeReader(type, "is");
                    buffer.append("       " + defType + " _tmp = " + read_def
                                  + ";\n");
                }
                if (j > 1) {
                    if (type.getTagName().equals(OMG_sequence))
                        buffer.append("       _union_result." + defName
                                      + "(_disc_value,result);\n");
                    else if (XmlType2Java.getTypedefType(type).equals(Idl2XmlNames.OMG_string)
                             && (type.getFirstChild() != null))
                        buffer.append("       _union_result." + defName
                                      + "(_disc_value,result);\n");
                    else
                        buffer.append("       _union_result." + defName
                                      + "(_disc_value,_tmp);\n");
                } else {
                    if (type.getTagName().equals(OMG_sequence))
                        buffer.append("       _union_result." + defName
                                      + "(result);\n");
                    else if (XmlType2Java.getTypedefType(type).equals(Idl2XmlNames.OMG_string)
                             && (type.getFirstChild() != null))
                        buffer.append("       _union_result." + defName
                                      + "(result);\n");
                    else
                        buffer.append("       _union_result." + defName
                                      + "(_tmp);\n");
                }
                buffer.append("       break;\n");
                buffer.append("     }");
            }
            if (!union.getHasDefault() && union.getDefaultAllowed()) {
                buffer.append("\n     default: {\n");
                buffer.append("       _union_result.__default(_disc_value);\n");
                buffer.append("       break;\n");
                buffer.append("     }\n");
            }
            buffer.append("\n    }\n");
        }

        buffer.append("    return _union_result;\n");
        buffer.append("\n  }\n\n");
    }

    private void generateJavaUnionHelperWrite(Element doc, StringBuffer buffer,
                                              Union union,
                                              String discriminatorType)
        throws Exception
    {
        String name = doc.getAttribute(OMG_name);
        buffer.append("  public static void write(org.omg.CORBA.portable.OutputStream os, ");
        buffer.append(name);
        buffer.append(" _value) {\n");

        Element union_el = union.getUnionElement();
        Element switch_el = (Element) union_el.getFirstChild();
        Element discriminator_type = (Element) switch_el.getFirstChild();
        Vector switchBody = union.getSwitchBody();

        // Write the discriminator to the OutputStream
        String write_discrim = XmlType2Java.getTypeWriter(discriminator_type, "os", "_value.discriminator()");
        buffer.append("    " + write_discrim + ";\n");

        // Write the selected member to the OutputStream
        UnionCase union_case = (UnionCase) switchBody.elementAt(switchBody.size() - 1);
        // Default case
        if (union_case.m_is_default) {
            Element type = union_case.m_type_spec;
            Element decl = union_case.m_declarator;
            String defType = XmlType2Java.getType(type);
            String defName = decl.getAttribute(OMG_name);
            buffer.append("    if (_value._isDefault){\n");
            String write_def;
            if (type.getTagName().equals(OMG_sequence)) {
                buffer.append("       " + defType + " val = _value." + defName
                              + "();\n");
                getSeqType(type, buffer, new StructWriter());
            } else {
                buffer.append("       " + defType + " " + defName
                              + " = _value." + defName + "();\n");
                write_def = XmlType2Java.getTypeWriter(type, "os", defName);
                buffer.append("       " + write_def + ";\n");
            }
            buffer.append("       return;\n");
            buffer.append("    }\n");
        }
        if (!union.getHasDefault() && union.getDefaultAllowed()) {
            buffer.append("\n    if (_value._isDefault) {\n");
            buffer.append("       return;\n");
            buffer.append("    }\n");
        }
        String dis_name = discriminator_type.getTagName();
        String dis_type = XmlType2Java.getType(discriminator_type);
        // Boolean, long_long or unsigned_long_long discriminator
        if (dis_type.equals(OMG_boolean) || dis_type.equals("long")) {
            for (int i = 0; i < switchBody.size(); i++) { // The default case is
                                                          // already considered
                union_case = (UnionCase) switchBody.elementAt(i);
                if (union_case.m_is_default)
                    continue;
                if (i != 0) {
                    buffer.append("\n    else");
                }
                Vector case_labels = union_case.m_case_labels;
                for (int j = 0; j < case_labels.size(); j++) {
                    Element label = (Element) case_labels.elementAt(j);
                    String value = null;
                    try {
                        Object expr = XmlExpr2Java.getExpr(label.getParentNode(), union.getDiscKind());
                        value = expr.toString();
                    }
                    catch (SemanticException e) {}
                    if (dis_type.equals("long"))
                        value += "L";
                    else
                        value = label.getAttribute(OMG_value);
                    if (j == 0)
                        buffer.append("    if ((_value.discriminator() == "
                                      + value.toLowerCase() + ")");
                    else
                        buffer.append(" || (_value.discriminator() == "
                                      + value.toLowerCase() + ")");
                }
                buffer.append(") {\n");
                Element type = union_case.m_type_spec;
                Element decl = union_case.m_declarator;
                String defType = XmlType2Java.getType(type);
                String defName = decl.getAttribute(OMG_name);
                String write_def;
                if (type.getTagName().equals(OMG_sequence)) {
                    buffer.append("       " + defType + " val = _value."
                                  + defName + "();\n");
                    getSeqType(type, buffer, new StructWriter());
                } else if (XmlType2Java.getTypedefType(type).equals(Idl2XmlNames.OMG_string)
                           && (type.getFirstChild() != null)) {
                    buffer.append("       " + defType + " val = _value."
                                  + defName + "();\n");
                    getBoundedStringType(type, buffer, new StructWriter());
                } else {
                    buffer.append("       " + defType + " " + defName
                                  + " = _value." + defName + "();\n");
                    write_def = XmlType2Java.getTypeWriter(type, "os", defName);
                    buffer.append("       " + write_def + ";\n");
                }
                buffer.append("    }\n");
            }
        } else {
            if (dis_name.equals(OMG_scoped_name)) {
                dis_name = union.getScopedDiscrimKind();
            }
            if (dis_name.equals(OMG_enum)) {
                buffer.append("    switch (_value.discriminator().value()){\n");
            } else {
                buffer.append("    switch (_value.discriminator()){");
            }
            for (int i = 0; i < switchBody.size(); i++) { // The default case is
                                                          // already considered
                union_case = (UnionCase) switchBody.elementAt(i);
                if (union_case.m_is_default)
                    continue;
                Vector case_labels = union_case.m_case_labels;
                for (int j = 0; j < case_labels.size(); j++) {
                    Element label = (Element) case_labels.elementAt(j);
                    //String value = label.getAttribute(OMG_value);
                    String value = null;
                    try {
                        Object expr = XmlExpr2Java.getExpr(label
                            .getParentNode(), union.getDiscKind());
                        value = expr.toString();
                    }
                    catch (SemanticException e) {}
                    if ((value == null) || (value.equals(""))) {
                        value = discriminatorType + "._"
                                + removeScope(label.getAttribute(OMG_name));
                    }
                    if (label.getTagName().equals(OMG_character_literal))
                        value = "'" + value + "'";
                    buffer.append("\n     case " + value + ":");
                }
                Element type = union_case.m_type_spec;
                Element decl = union_case.m_declarator;
                String defType = XmlType2Java.getType(type);
                String defName = decl.getAttribute(OMG_name);
                buffer.append(" {\n");
                String write_def;
                if (type.getTagName().equals(OMG_sequence)) {
                    buffer.append("       " + defType + " val = _value."
                                  + defName + "();\n");
                    getSeqType(type, buffer, new StructWriter());
                } else if (XmlType2Java.getTypedefType(type).equals(Idl2XmlNames.OMG_string)
                           && (type.getFirstChild() != null)) {
                    buffer.append("       " + defType + " val = _value."
                                  + defName + "();\n");
                    getBoundedStringType(type, buffer, new StructWriter());
                } else {
                    buffer.append("       " + defType + " " + defName
                                  + " = _value." + defName + "();\n");
                    write_def = XmlType2Java.getTypeWriter(type, "os", defName);
                    buffer.append("       " + write_def + ";\n");
                }
                buffer.append("       break;\n");
                buffer.append("     }");
            }
            buffer.append("\n    }\n");
        }
        buffer.append("\n  }\n\n");
    }

    private void generateJavaUnionProcess(StringBuffer buffer, Element doc,
                                          StructProcessor processor)
        throws Exception
    {
        String objectName = null;
        Element classType = null;
        Union union = UnionManager.getInstance().get(doc);
        Vector switchBody = union.getSwitchBody();
        Vector indexes = new Vector();
        Vector isArray = new Vector();

        for (int i = 0; i < switchBody.size(); i++) {
            UnionCase union_case = (UnionCase) switchBody.elementAt(i);
            Element el = union_case.m_declarator;
            String tag = el.getTagName();

            // Simple declarator
            if (tag.equals(OMG_simple_declarator)) {
                indexes.removeAllElements();
                isArray.removeAllElements();
                objectName = el.getAttribute(OMG_name);
                classType = union_case.m_type_spec;
                if ((XmlType2Java.getTypedefType(classType).equals(Idl2XmlNames.OMG_string) ||
                		XmlType2Java.getTypedefType(classType).equals(Idl2XmlNames.OMG_wstring)) && (classType.getFirstChild() != null)) {
                    // Bounded Strings
                    int val = 0;
                    Element expr = (Element) classType.getFirstChild();
                    if (expr != null) {
                        indexes.addElement(new Long(XmlExpr2Java.getIntExpr(expr)));
                    } else {
                        indexes.addElement(new String("length" + val));
                    }
                    isArray.addElement(new Boolean(false));
                }
            }
            // Array declarator
            else if (tag.equals(OMG_array)) {
                objectName = el.getAttribute(OMG_name);
                NodeList indexChilds = el.getChildNodes();
                for (int k = 0; k < indexChilds.getLength(); k++) {
                    Element indexChild = (Element) indexChilds.item(k);
                    if (indexChild != null) {
                        indexes.insertElementAt(new Long(XmlExpr2Java.getIntExpr(indexChild)), k);
                        isArray.insertElementAt(new Boolean(true), k);
                    }
                }
            }
            // Sequence
            else if (tag.equals(OMG_sequence)) {
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
            }
            // Other type
            else {
                indexes.removeAllElements();
                isArray.removeAllElements();
                classType = el;
                objectName = null;
            }

            if (objectName != null) {
                processor.generateJava(buffer, objectName, classType, indexes, isArray);
            }
        }
    }

    private String removeScope(String value)
    {
        StringTokenizer tokenizer = new StringTokenizer(value, Scope.SEP);
        String label = null;
        while (tokenizer.hasMoreTokens()) {
            label = (String) tokenizer.nextToken();
        }
        return label;
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
            processor.generateJava(buffer, objectName, classType, indexes,
                                   isArray);
        }
    }

    private void getBoundedStringType(Element seq, StringBuffer buffer,
                                      StructProcessor processor)
        throws Exception
    {
        String objectName = null;
        Element classType = null;
        NodeList nodes = seq.getParentNode().getChildNodes();
        Vector indexes = new Vector();
        Vector isArray = new Vector();
        int val = 0;

        Element expr = (Element) seq.getFirstChild();
        if (expr != null) {
            indexes.addElement(new Long(XmlExpr2Java.getIntExpr(expr)));
        } else {
            indexes.addElement(new String("length" + val));
        }
        isArray.addElement(new Boolean(false));

        classType = seq;
        objectName = null;
        processor.generateJava(buffer, objectName, classType, indexes, isArray);

    }

    private void generateJavaSubPackageDef(Element doc, String outputDir,
                                           String unionName, String genPackage,
                                           boolean generateCode)
        throws Exception
    {

        String newPackage;
        if (!genPackage.equals("")) {
            newPackage = genPackage + "." + unionName + "Package";
        } else {
            newPackage = unionName + "Package";
        }

        NodeList nodes = doc.getChildNodes();

        for (int i = 0; i < nodes.getLength(); i++) {

            Element el = (Element) nodes.item(i);
            String tag = el.getTagName();
            if (tag.equals(OMG_case)) {

                NodeList nodes2 = el.getChildNodes();
                for (int j = 0; j < nodes2.getLength(); j++) {
                    Element union_el = (Element) nodes2.item(j);
                    String tag2 = union_el.getTagName();
                    if (tag2.equals("value")) {
                        Element type = (Element) union_el.getFirstChild();
                        String typeTag = type.getTagName();

                        if (typeTag.equals(OMG_enum)) {
                            XmlEnum2Java gen = new XmlEnum2Java();
                            gen.generateJava(type, outputDir, newPackage,
                                             generateCode);
                        } else if (typeTag.equals(OMG_struct)) {
                            XmlStruct2Java gen = new XmlStruct2Java();
                            gen.generateJava(type, outputDir, newPackage,
                                             generateCode);
                        } else if (typeTag.equals(OMG_union)) {
                            XmlUnion2Java gen = new XmlUnion2Java();
                            gen.generateJava(type, outputDir, newPackage,
                                             generateCode);
                        }
                    }
                }
            }

        }
    }

}