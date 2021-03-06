/*
* MORFEO Project
* http://www.morfeo-project.org
*
* Component: TIDIdlc
* Programming Language: Java
*
* File: $Source$
* Version: $Revision: 117 $
* Date: $Date: 2006-02-22 13:25:35 +0100 (Wed, 22 Feb 2006) $
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

package es.tid.TIDIdlc.xml2java;

import es.tid.TIDIdlc.idl2xml.Idl2XmlNames;
import es.tid.TIDIdlc.xmlsemantics.*;

import java.util.StringTokenizer;

import org.w3c.dom.*;

/**
 * Generates Java mapping for basic types and parameters.
 */
public class XmlType2Java
    implements Idl2XmlNames
{

    /**
     * Returns the type for typedefs.
     * 
     * @param doc
     *            The XML node where the type is.
     * @return The converted type.
     */
    public static String getTypedefType(Element doc)
    {
        String tag = doc.getTagName();
        if (tag.equals(OMG_type)) {
            return doc.getAttribute(OMG_kind); // no conversion is done
        } else if (tag.equals(OMG_scoped_name)) {
            return getUnrolledName(doc); // no conversion is done
        } else if (tag.equals(OMG_sequence)) {
            NodeList nodes = doc.getChildNodes();
            Element type = (Element) nodes.item(0);
            return getType(type) + "[]"; // sequences are converted in getType
        } else
            return "unknownType";
    }

    /**
     * Returns the Java type for an XML node.
     * 
     * @param doc
     *            The XML node where the type is.
     * @return The Java type.
     */
    public static String getType(Element doc)
    {
        String tag = doc.getTagName();
        if (tag.equals(OMG_type)) {
            return basicMapping(doc.getAttribute(OMG_kind));
        } else if (tag.equals(OMG_scoped_name)) {
            return basicMapping(getUnrolledName(doc));
        } else if (tag.equals(OMG_sequence)) {
            NodeList nodes = doc.getChildNodes();
            Element type = (Element) nodes.item(0);
            return getType(type) + "[]";
        } else if (tag.equals(OMG_enum) || tag.equals(OMG_struct)
                   || tag.equals(OMG_union)) {
            String name = getUnrolledName(doc.getAttribute(OMG_scoped_name));
            return name;
        } else
            return "unknownType";
    }


    /**
     * Returns the Java type for method parameter (in/out/inout).
     * 
     * @param doc
     *            The XML node where the type is.
     * @param out
     *            True if it is an OUT or INOUT parameter.
     * @return The Java type.
     */
    public static String getParamType(Element doc, boolean out)
    {
        String tag = doc.getTagName();
        if (tag.equals(OMG_type)) {
            String att = doc.getAttribute(OMG_kind);
            if (out) {
                att = basicOutMapping(att) + "Holder";
            } else {
                att = basicMapping(att);
            }
            return att;
        } else if (tag.equals(OMG_scoped_name)) {
            if (out) {
                return getUnrolledHolderName(doc) + "Holder";
            } else
                return basicMapping(getUnrolledName(doc));
        } else
            return "unknownType";
    }

    /**
     * Returns the Java helper type for an XML node.
     * 
     * @param doc
     *            The XML node where the type is.
     * @return The Java type.
     */
    public static String getHelperType(Element doc)
    {
        String tag = doc.getTagName();
        if (tag.equals(OMG_type)) {
            return null;
        } else if (tag.equals(OMG_scoped_name)) {
            String helper = getUnrolledHelperName(doc);
            if (helper != null)
                return helper + "Helper";
            else
                return null;
        } else
            return "unknownType";
    }

    /**
     * Returns the Java typecode for an XML node.
     * 
     * @param doc
     *            The XML node where the type is.
     * @return The Java typecode.
     */
    public static String getTypecode(Element doc)
        throws Exception
    {
        String tag = doc.getTagName();
        if (tag.equals(OMG_type)) {
            String s = basicORBTcKindMapping(doc);
            return "org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_"
                   + s + ")";
        } else if (tag.equals(OMG_scoped_name)) {
            String helper = getUnrolledHelperName(doc);
            String s = basicORBTcKindMapping(doc);
            if (helper == null)
                return "org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_"
                       + s + ")";
            else
                return helper + "Helper.type()";
        } else if (tag.equals(OMG_enum) || tag.equals(OMG_struct)
                   || tag.equals(OMG_union)) {
            // Added to support the declaration of enumerations, structs into a
            // Union body
            String helper = getUnrolledName(doc.getAttribute(OMG_scoped_name));
            String s = basicORBTcKindMapping(doc);
            if (helper == null)
                return "org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_"
                       + s + ")";
            else {
                //return getElementInUnionTypeCode(helper); DAVV - PROVISIONAL
                // (por value boxed)
                return helper + "Helper.type()";
            }
        } else if (tag.equals(OMG_sequence)) {
            // Added to support the declaration of enumerations, structs into a
            // Union body
            NodeList nodes = doc.getChildNodes();
            Element type = (Element) nodes.item(0);
            String elem_typecode = getTypecode(type);
            Element expr = (Element) type.getNextSibling();
            String value = "0";
            if (expr != null) {
                value = String.valueOf(XmlExpr2Java.getIntExpr(expr));
            }
            return "_orb().create_sequence_tc(" + value + "," + elem_typecode
                   + ")";
        } else
            return "unknownType";
    }

    /**
     * Returns the Java type reader for an XML node.
     * 
     * @param doc
     *            The XML node where the type is.
     * @param inputStreamName
     *            name of the inputStream to be used.
     * @return The Java type reader.
     * @throws Exception 
     */
    public static String getTypeReader(Element doc, String inputStreamName) throws Exception
    {
        String tag = doc.getTagName();
        if (tag.equals(OMG_type)) {
            String s = basicORBTypeMapping(doc);
            return inputStreamName + ".read_" + s + "()";
        } else if (tag.equals(OMG_scoped_name)) {
            String helper = getUnrolledHelperName(doc);
            String s = basicORBTypeMapping(doc);
            if (helper == null)
                return inputStreamName + ".read_" + s + "()";
            else
                return helper + "Helper.read(" + inputStreamName + ")";
        } else if (tag.equals(OMG_enum) || tag.equals(OMG_struct)
                   || tag.equals(OMG_union)) {
            String helper = getUnrolledHelperName(doc.getAttribute(OMG_scoped_name));
            String s = basicORBTypeMapping(doc);
            if (helper == null)
                return inputStreamName + ".read_" + s + "()";
            else
                return helper + "Helper.read(" + inputStreamName + ")";
        } else if (tag.equals(OMG_sequence)) {
            // Added to support the declaration of enumerations, structs into a
            // Union body
            NodeList nodes = doc.getChildNodes();
            Element type = (Element) nodes.item(0);
            String elem_type = basicORBTcKindMapping(type);
            if (elem_type.equals(OMG_any)) {
            	return "org.omg.CORBA.AnySeqHelper.read(" + inputStreamName +")";
            } else if (elem_type.equals(OMG_boolean)) {
            	return "org.omg.CORBA.BooleanSeqHelper.read(" + inputStreamName +")";
            } else if (elem_type.equals(OMG_char)) {
            	return "org.omg.CORBA.CharSeqHelper.read(" + inputStreamName +")";
            } else if (elem_type.equals(OMG_double)) {
            	return "org.omg.CORBA.DoubleSeqHelper.read(" + inputStreamName +")";
            } else if (elem_type.equals(OMG_float)) {
            	return "org.omg.CORBA.FloatSeqHelper.read(" + inputStreamName +")";
            } else if (elem_type.equals(OMG_longlong)) {
            	return "org.omg.CORBA.LongLongSeqHelper.read(" + inputStreamName +")";
            } else if (elem_type.equals(OMG_long)) {
            	return "org.omg.CORBA.LongSeqHelper.read(" + inputStreamName +")";
            } else if (elem_type.equals(OMG_octet)) {
            	return "org.omg.CORBA.OctetSeqHelper.read(" + inputStreamName +")";
            } else if (elem_type.equals(OMG_short)) {
            	return "org.omg.CORBA.ShortSeqHelper.read(" + inputStreamName +")";
            } else if (elem_type.equals(OMG_string)) {
            	return "org.omg.CORBA.CharSeqHelper.read(" + inputStreamName +")";
            } else if (elem_type.equals(OMG_unsignedlonglong)) {
            	return "org.omg.CORBA.ULongLongSeqHelper.read(" + inputStreamName +")";
            } else if (elem_type.equals(OMG_unsignedlong)) {
               	return "org.omg.CORBA.ULongSeqHelper.read(" + inputStreamName +")";
            } else if (elem_type.equals(OMG_unsignedshort)) {
               	return "org.omg.CORBA.UShortSeqHelper.read(" + inputStreamName +")";
            } else if (elem_type.equals(OMG_wchar)) {
               	return "org.omg.CORBA.WCharLongSeqHelper.read(" + inputStreamName +")";
            } else if (elem_type.equals(OMG_wstring)) {
               	return "org.omg.CORBA.WStringLongSeqHelper.read(" + inputStreamName +")";
            } else
                return "reading_unknownType()";
        } else
            return "reading_unknownType()";
    }

    /**
     * Returns the Java type writer for an XML node.
     * 
     * @param doc
     *            The XML node where the type is.
     * @param outputStreamName
     *            Name of the outputStream to be used.
     * @param outputData
     *            Name of the output data.
     * @return The Java type writer.
     */
    public static String getTypeWriter(Element doc, String outputStreamName,
                                       String outputData)
    {
        String tag = doc.getTagName();
        if (tag.equals(OMG_type)) {
            String s = basicORBTypeMapping(doc);
            return outputStreamName + ".write_" + s + "(" + outputData + ")";
        } else if (tag.equals(OMG_scoped_name)) {
            String helper = getUnrolledHelperName(doc);
            String s = basicORBTypeMapping(doc);
            if (helper == null)
                return outputStreamName + ".write_" + s + "(" + outputData + ")";
            else
                return helper + "Helper.write(" + outputStreamName + "," + outputData + ")";
        } else if (tag.equals(OMG_enum) || tag.equals(OMG_struct)
                   || tag.equals(OMG_union)) {
            String helper = getUnrolledHelperName(doc
                .getAttribute(OMG_scoped_name));
            String s = basicORBTypeMapping(doc);
            if (helper == null)
                return outputStreamName + ".write_" + s + "(" + outputData + ")";
            else
                return helper + "Helper.write(" + outputStreamName + "," + outputData + ")";
        } else if (tag.equals(OMG_sequence)) {
            // Added to support the declaration of enumerations, structs into a
            // Union body
            NodeList nodes = doc.getChildNodes();
            Element type = (Element) nodes.item(0);
            String elem_type = basicORBTcKindMapping(type);
            if (elem_type.equals(OMG_any)) {
            	return "org.omg.CORBA.AnySeqHelper.write(" + outputStreamName + ", " + outputData + ")";
            } else if (elem_type.equals(OMG_boolean)) {
            	return "org.omg.CORBA.BooleanSeqHelper.write(" + outputStreamName + ", " + outputData + ")";
            } else if (elem_type.equals(OMG_char)) {
            	return "org.omg.CORBA.CharSeqHelper.write(" + outputStreamName + ", " + outputData + ")";
            } else if (elem_type.equals(OMG_double)) {
            	return "org.omg.CORBA.DoubleSeqHelper.write(" + outputStreamName + ", " + outputData + ")";
            } else if (elem_type.equals(OMG_float)) {
            	return "org.omg.CORBA.FloatSeqHelper.write(" + outputStreamName + ", " + outputData + ")";
            } else if (elem_type.equals(OMG_longlong)) {
            	return "org.omg.CORBA.LongLongSeqHelper.write(" + outputStreamName + ", " + outputData + ")";
            } else if (elem_type.equals(OMG_long)) {
            	return "org.omg.CORBA.LongSeqHelper.write(" + outputStreamName + ", " + outputData + ")";
            } else if (elem_type.equals(OMG_octet)) {
            	return "org.omg.CORBA.OctetSeqHelper.write(" + outputStreamName + ", " + outputData + ")";
            } else if (elem_type.equals(OMG_short)) {
            	return "org.omg.CORBA.ShortSeqHelper.write(" + outputStreamName + ", " + outputData + ")";
            } else if (elem_type.equals(OMG_string)) {
            	return "org.omg.CORBA.CharSeqHelper.write(" + outputStreamName + ", " + outputData + ")";
            } else if (elem_type.equals(OMG_unsignedlonglong)) {
            	return "org.omg.CORBA.ULongLongSeqHelper.write(" + outputStreamName + ", " + outputData + ")";
            } else if (elem_type.equals(OMG_unsignedlong)) {
               	return "org.omg.CORBA.ULongSeqHelper.write(" + outputStreamName + ", " + outputData + ")";
            } else if (elem_type.equals(OMG_unsignedshort)) {
               	return "org.omg.CORBA.UShortSeqHelper.write(" + outputStreamName + ", " + outputData + ")";
            } else if (elem_type.equals(OMG_wchar)) {
               	return "org.omg.CORBA.WCharLongSeqHelper.write(" + outputStreamName + ", " + outputData + ")";
            } else if (elem_type.equals(OMG_wstring)) {
               	return "org.omg.CORBA.WStringLongSeqHelper.write(" + outputStreamName + ", " + outputData + ")";
            } else
               	return "writing_unknownType()";
        } else
            return "writing_unknownType()";
    }

    /**
     * Returns the Java type for an IDL basic type.
     * 
     * @param type
     *            The IDL type.
     * @return The Java type.
     */
    public static String basicMapping(String type)
    {
        if (type.equals(OMG_wchar)) {
            return "char";
        } else if (type.equals(OMG_octet)) {
            return "byte";
        } else if (type.equals(OMG_string)) {
            return "java.lang.String";
        } else if (type.equals(OMG_wstring)) {
            return "java.lang.String";
        } else if (type.equals(OMG_unsignedshort)) {
            return "short";
        } else if (type.equals(OMG_long)) {
            return "int";
        } else if (type.equals(OMG_unsignedlong)) {
            return "int";
        } else if (type.equals(OMG_longlong)) {
            return "long";
        } else if (type.equals(OMG_unsignedlonglong)) {
            return "long";
        } else if (type.equals(OMG_fixed)) {
            return "java.math.BigDecimal";
        } else if (type.equals(OMG_any)) {
            return "org.omg.CORBA.Any";
        } else if (type.equals(OMG_boolean)) {
            return "boolean";
        } else if (type.equals(OMG_Object)) {
            return "org.omg.CORBA.Object";
        } else if (type.equals(OMG_TypeCode)) {
            return "org.omg.CORBA.TypeCode";
        } else if (type.equals(OMG_ValueBase)) {
            return "java.io.Serializable";
        } else if (type.equals(OMG_AbstractBaseCode)) {
            return "java.lang.Object";
        }

        return type;
    }

    /**
     * Returns the Java Holder type for an IDL basic type.
     * 
     * @param type
     *            The IDL type.
     * @return The Java Holder type.
     */
    public static String basicOutMapping(String type)
    {
        if (type.equals(OMG_char)) {
            return "org.omg.CORBA.Char";
        } else if (type.equals(OMG_wchar)) {
            return "org.omg.CORBA.Char";
        } else if (type.equals(OMG_octet)) {
            return "org.omg.CORBA.Byte";
        } else if (type.equals(OMG_string)) {
            return "org.omg.CORBA.String";
        } else if (type.equals(OMG_wstring)) {
            return "org.omg.CORBA.String";
        } else if (type.equals(OMG_short)) {
            return "org.omg.CORBA.Short";
        } else if (type.equals(OMG_unsignedshort)) {
            return "org.omg.CORBA.Short";
        } else if (type.equals(OMG_long)) {
            return "org.omg.CORBA.Int";
        } else if (type.equals(OMG_double)) {
            return "org.omg.CORBA.Double";
        } else if (type.equals(OMG_float)) {
            return "org.omg.CORBA.Float";
        } else if (type.equals(OMG_unsignedlong)) {
            return "org.omg.CORBA.Int";
        } else if (type.equals(OMG_longlong)) {
            return "org.omg.CORBA.Long";
        } else if (type.equals(OMG_unsignedlonglong)) {
            return "org.omg.CORBA.Long";
        } else if (type.equals(OMG_fixed)) {
            return "org.omg.CORBA.Fixed";
        } else if (type.equals(OMG_any)) {
            return "org.omg.CORBA.Any";
        } else if (type.equals(OMG_boolean)) {
            return "org.omg.CORBA.Boolean";
        } else if (type.equals(OMG_Object)) {
            return "org.omg.CORBA.Object";
        } else if (type.equals(OMG_TypeCode)) {
            return "org.omg.CORBA.TypeCode";
        }
        return null;
    }

    /**
     * Returns the ORB mapping for an XML node. ORB methods considered: read,
     * write, insert, extract.
     * 
     * @param el
     *            The XML node where the type is.
     * @return The ORB type.
     */
    public static String basicORBTypeMapping(Element el)
    {
        String type = el.getAttribute(OMG_kind);
        if (type.equals("")) {
            type = getUnrolledName(el);
        }
        return basicORBTypeMapping(type);
    }

    /**
     * Returns the ORB mapping (for TCKind) for an XML node.
     * 
     * @param el
     *            The XML node where the type is.
     * @return The ORB type.
     */
    public static String basicORBTcKindMapping(Element el)
    {
        String type = el.getAttribute(OMG_kind);
        if (type.equals("")) {
            type = getUnrolledName(el);
        }
        return basicORBTcKindMapping(type);
    }

    // IDL -> ORB (read, write, insert, extract)
    private static String basicORBTypeMapping(String type)
    {
        if (type.equals(OMG_unsignedshort)) {
            return "ushort";
        } else if (type.equals(OMG_unsignedlong)) {
            return "ulong";
        } else if (type.equals(OMG_longlong)) {
            return "longlong";
        } else if (type.equals(OMG_unsignedlonglong)) {
            return "ulonglong";
        } else if (type.equals(OMG_fixed)) {
            return "fixed";
        } else if (type.equals(OMG_Object)) {
            return "Object";
        } else if (type.equals(OMG_TypeCode)) {
            return "TypeCode";
        }
        return type;
    }

    // IDL -> ORB (TCKind)
    private static String basicORBTcKindMapping(String type)
    {
        if (type.equals(OMG_unsignedshort)) {
            return "ushort";
        } else if (type.equals(OMG_unsignedlong)) {
            return "ulong";
        } else if (type.equals(OMG_longlong)) {
            return "longlong";
        } else if (type.equals(OMG_unsignedlonglong)) {
            return "ulonglong";
        } else if (type.equals(OMG_fixed)) {
            return "fixed";
        } else if (type.equals(OMG_Object)) {
            return "objref";
        } else if (type.equals(OMG_TypeCode)) {
            return "TypeCode";
        }
        return type;
    }

    public static String getUnrolledName(String scopedName)
    {
        String unrolled = TypedefManager.getInstance().getUnrolledType(scopedName);
        if (unrolled != null)
            return unrolled;
        else
            return TypeManager.convert(scopedName);
    }

    public static String getUnrolledName(Element doc)
    {
        String scopedName = "";
        if (doc.getTagName().equals(OMG_scoped_name)) {
            scopedName = doc.getAttribute(OMG_scoped_name);
        }
        if (scopedName == null || scopedName.equals(""))
            scopedName = doc.getAttribute(OMG_name);
        String unrolled = TypedefManager.getInstance().getUnrolledType(scopedName);
        if (unrolled != null)
            return unrolled;
        else
            return TypeManager.convert(scopedName);
    }
    
    private static String getUnrolledHolderName(Element doc)
    {
        String scopedName = doc.getAttribute(OMG_name);
        String unrolled = TypedefManager.getInstance().getUnrolledHolderType(scopedName);
        if (unrolled != null)
            return TypeManager.convert(unrolled);
        else
            return TypeManager.convert(scopedName);
    }

    private static String getUnrolledHelperName(String scopedName)
    {
        String unrolled = TypedefManager.getInstance().getUnrolledHelperType(scopedName);
        if (unrolled != null) {
            if (unrolled.equals("")) {
                return null;
            } else
                return TypeManager.convert(unrolled);
        } else
            return TypeManager.convert(scopedName);
    }

    private static String getUnrolledHelperName(Element doc)
    {
        String scopedName = doc.getAttribute(OMG_name);
        String unrolled = TypedefManager.getInstance().getUnrolledHelperType(scopedName);
        if (unrolled != null) {
            if (unrolled.equals("")) {
                return null;
            } else
                return TypeManager.convert(unrolled);
        } else
            return TypeManager.convert(scopedName);
    }

    private static String getScopedName(Element doc)
    {
        String scopedName = doc.getAttribute(OMG_name);
        return TypeManager.convert(scopedName);
    }

    private static String getElementInUnionType(String scopedName)
    {
        StringTokenizer tok = new StringTokenizer(scopedName, ".");
        String prefix = "", actual = "";
        if (tok.hasMoreTokens())
            prefix = tok.nextToken();
        while (tok.hasMoreTokens()) {
            actual = tok.nextToken();
            if (tok.hasMoreTokens())
                prefix += "." + actual;
        }
        return prefix + "Package." + actual;
    }

    private static String getElementInUnionTypeCode(String scopedName)
    {
        StringTokenizer tok = new StringTokenizer(scopedName, ".");
        String prefix = "", actual = "";
        if (tok.hasMoreTokens())
            prefix = tok.nextToken();
        while (tok.hasMoreTokens()) {
            actual = tok.nextToken();
            if (tok.hasMoreTokens())
                prefix += "." + actual;
        }
        return prefix + "Package." + actual + "Helper.type()";
    }

    public static String getDeepType(Element doc)
    {
        // DAVV - solucion de compromiso
        // para hallar lo que hay al fondo de un 'retypedef'
        // traido desde XmlType2Cpp.java

        String name = doc.getAttribute(OMG_scoped_name);

        if (!name.equals("")) {
            Node parent = doc.getParentNode();
            Document documentRoot = parent.getOwnerDocument();
            Node him = findFirstOccurrence(name, documentRoot
                .getDocumentElement());
            //Node
            if (him != null) {
                String definitionType = him.getNodeName();
                if (definitionType.equals(OMG_simple_declarator)) {// es un
                                                                   // retypedef...
                    while (definitionType.equals(OMG_simple_declarator)) {
                        parent = him.getParentNode();
                        if (parent != null) {
                            NodeList nl = parent.getChildNodes();
                            NamedNodeMap atl = nl.item(0).getAttributes();
                            if (atl.getNamedItem(OMG_name) != null) {
                                String parentName = atl.getNamedItem(OMG_name).getNodeValue();

                                him = findFirstOccurrence(
                                                          parentName,
                                                          documentRoot
                                                              .getDocumentElement());
                                if (him != null)
                                    definitionType = him.getNodeName();
                                else
                                    return "UNKNOWN";
                            } else if (atl.getNamedItem(OMG_kind) != null)
                                return XmlType2Java.basicMapping(atl.getNamedItem(OMG_kind).getNodeValue());
                            else if (((Element) nl)
                                .getTagName().equals(OMG_typedef)) {
                                Element el = (Element) ((Element) nl).getFirstChild();
                                return el.getTagName();
                            } else
                                return "UNKNOWN";
                        }
                    }
                }
                return definitionType;
            }
        }// name equals ""
        else {
            if (doc.getTagName().equals(OMG_sequence)) {
                return getDeepType((Element) doc.getFirstChild());
            } else if (doc.getAttribute(OMG_kind) != null)
                return OMG_kind;

        }
        return "UNKNOWN";
    }

    /**
     * finds the first occurrence of a name inside of the Xml Dom in order to
     * access its declaration attributes. Could fail in the case of find the
     * declaration from the declaration. Its a little 'bullshit'
     * 
     * @param name
     *            The type name
     * @param doc
     *            The position from the look up will start.
     * @return
     */

    // DAVV traido desde XmlType2Cpp.java
    private static Element findFirstOccurrence(String name, Element doc)
    {
        if (name.startsWith("::"))
            name = name.substring(2);
        String currentName = doc.getAttribute(OMG_name);
        if (currentName != null) {
            if (currentName.equals(name))
                return doc;
        }
        if (!currentName.equals("")) {
            if (!name.startsWith(currentName)) // Si estoy buscando algo que no
                                               // es esto y no es hijo de esto.
                return null; // me piro.
        }
        NodeList nl = doc.getChildNodes();
        String childsName = "";
        if (nl == null)
            return null;
        else {// si tengo hijos. quito del nombre de busqueda el nombre actual.
            if (!currentName.equals(""))
                childsName = name.substring(currentName.length());
            // por aquello de los "::"
            else
                childsName = name; // No avanzamos.
        }

        Element temp;
        Node aux;
        for (int i = 0; i < nl.getLength(); i++) {
            aux = nl.item(i);
            temp = findFirstOccurrence(childsName, (Element) aux);
            if (temp != null)
                return temp;
        }
        return null;

    }

    public static boolean isPrimitiveJavaType(String type)
    { // DAVV
        return (type.equals("boolean") || type.equals("char")
                || type.equals("byte") || type.equals("short")
                || type.equals("int") || type.equals("long")
                || type.equals("float") || type.equals("double"));
    }
}