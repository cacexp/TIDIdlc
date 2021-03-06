/*
* MORFEO Project
* http://www.morfeo-project.org
*
* Component: TIDIdlc
* Programming Language: Java
*
* File: $Source$
* Version: $Revision: 242 $
* Date: $Date: 2008-03-03 15:29:05 +0100 (Mon, 03 Mar 2008) $
* Last modified by: $Author: avega $
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
import java.math.BigInteger;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Class for Unions. It checks the correctness of the union in various points,
 * such as the scoped switch type, the case labels and its range.
 */

public abstract class Union
    implements Idl2XmlNames
{

    protected String m_scoped_name; // The name of the scoped discriminator

    protected Scope m_father_scope; // The scope where the union appears

    protected NodeList m_enum_values; // If the discriminator is of enumeration
                                      // type, this

    // list contains it's identifiers

    protected String m_scoped_discrim_kind; // The kind of the scoped
                                            // discriminator

    protected Element m_union_element; // The Union element

    protected Vector m_switch_body;

    protected boolean m_default_allowed = true;

    protected boolean m_has_default = false;

    protected String m_default_value = null;

    protected String m_disc_kind = null;

    public Union()
    {
        m_scoped_name = null;
        m_father_scope = null;
        m_enum_values = null;
    }

    public Union(Scope father_scope, Element union_element)
    {
        this.m_father_scope = father_scope;
        this.m_union_element = union_element;
    }

    // Checks the Type of a scoped discriminator. It throws a SemanticException
    // if its not a previously defined integer (short, long, long long, unsigned
    // short,
    // unsigned long, unsigned long long), char (char, wchar) boolean or enum
    // type.
    // It returns the kind of the scoped discriminator

    public String checkSwitchType()
        throws SemanticException
    {
        String name = m_scoped_name.substring(m_scoped_name.lastIndexOf("::") + 2);
        Scope scope_of_type = m_father_scope.getScopeOfType(m_scoped_name);
        Element e = scope_of_type.getElement();

        NodeList typedef_list = e.getElementsByTagName(OMG_typedef);

        // Check the typedefs
        for (int i = typedef_list.getLength() - 1; i >= 0; i--) {
            Element e_kind = (Element) (typedef_list.item(i)).getFirstChild();
            NodeList decl_list = typedef_list.item(i).getChildNodes();
            for (int j = 1; j < decl_list.getLength(); j++) {
            	Element e_type = (Element) decl_list.item(j);
            	String e_type_name        = e_type.getAttribute(OMG_name);
            	String e_type_scoped_name = e_type.getAttribute(OMG_scoped_name);
            	String e_type_tag_name    = e_type.getTagName();
            	// Fix bug [#380] Incorrect semantic error between different scopes
                if(e_type_name.equals(name) && e_type_scoped_name.equals(m_scoped_name)) {
                    String kind = e_kind.getAttribute(OMG_kind);
                    if (kind.equals("") && e_type_tag_name.equals(OMG_simple_declarator)) {
                        kind = TypedefManager.getInstance().getDefinitionType(m_scoped_name);
                        m_scoped_discrim_kind = kind;
                        if (kind.equals(OMG_enum)) {
                            m_scoped_name = e_kind.getAttribute(OMG_name);
                            name = m_scoped_name.substring(m_scoped_name.lastIndexOf("::") + 2);
                            break;
                        } else if (kind.equals(OMG_kind)) {
                        	m_scoped_name = e_kind.getAttribute(OMG_name);
                            name = m_scoped_name.substring(m_scoped_name.lastIndexOf("::") + 2);
                            continue;
                        } else if (!(kind.equals(OMG_long)
                                || kind.equals(OMG_longlong)
                                || kind.equals(OMG_short)
                                || kind.equals(OMG_unsignedlong)
                                || kind.equals(OMG_unsignedlonglong)
                                || kind.equals(OMG_unsignedshort)
                                || kind.equals(OMG_char)
                                || kind.equals(OMG_wchar) 
                                || kind.equals(OMG_boolean))) {
                               SemanticException se = new SemanticException(
                                            "Error in Union. Discriminator: '"
                                            + m_scoped_name
                                            + "' -> Type: '"
                                            + e_kind.getAttribute(OMG_kind)
                                            + "'.\nIt must be a previously defined integer, char, boolean or enum type.");
                               se.locate(m_union_element);
                               throw se;
                        }
                    }

                    // We have found the typedef, so we return its kind
                    this.m_scoped_discrim_kind = kind;
                    return kind;
                }
            }
        }

        
/*        
        for (int i = 0; i < typedef_list.getLength(); i++) {
            Element e_kind = (Element) (typedef_list.item(i)).getFirstChild();
            NodeList decl_list = typedef_list.item(i).getChildNodes();
            for (int j = 1; j < decl_list.getLength(); j++) { // DAVV - por
                                                              // declaraciones
                                                              // m�ltiples...
                //Element e_type = (Element)
                // ((Element)typedef_list.item(i)).getLastChild();
                Element e_type = (Element) decl_list.item(j);
                //if (e_type.getAttribute(OMG_name).equals(scoped_name)) {

                if (e_type.getAttribute(OMG_name).equals(name)
                    || e_type.getAttribute(OMG_scoped_name).equals(m_scoped_name)) {
                    String kind = e_kind.getAttribute(OMG_kind);
                    if (kind.equals("") && e_type.getTagName().equals(OMG_simple_declarator)) {
                        kind = TypedefManager.getInstance().getDefinitionType(m_scoped_name);
                        
                        m_scoped_discrim_kind = kind;
                        
                        if(kind.equals(OMG_enum)) {
                            m_scoped_name = e_kind.getAttribute(OMG_name);
                            name = m_scoped_name.substring(m_scoped_name.lastIndexOf("::") + 2);
                            break;
                        }
                    }

                	if (!(kind.equals(OMG_long)
                         || kind.equals(OMG_longlong)
                         || kind.equals(OMG_short)
                         || kind.equals(OMG_unsignedlong)
                         || kind.equals(OMG_unsignedlonglong)
                         || kind.equals(OMG_unsignedshort)
                         || kind.equals(OMG_char)
                         || kind.equals(OMG_wchar) 
                         || kind.equals(OMG_boolean))) {
                        SemanticException se = new SemanticException(
                                     "Error in Union. Discriminator: '"
                                     + m_scoped_name
                                     + "' -> Type: '"
                                     + e_kind.getAttribute(OMG_kind)
                                     + "'.\nIt must be a previously defined integer, char, boolean or enum type.");
                        se.locate(m_union_element);
                        throw se;
                    }
                    // We have found the typedef, so we return its kind
                    this.m_scoped_discrim_kind = kind;
                    return kind;
                }
            }
        }
*/
        NodeList enumeration_list = e.getElementsByTagName(OMG_enum);
        // We check the Enumerations
        for (int i = 0; i < enumeration_list.getLength(); i++) {
            //String enum_name =
            // ((Element)enumeration_list.item(i)).getAttribute(OMG_name);
            Element enum_el = (Element) enumeration_list.item(i);
            if (enum_el.getAttribute(OMG_name).equals(name)
                && enum_el.getAttribute(OMG_scoped_name).equals(m_scoped_name)) {
                // We have found the enumeration, so we return
                this.m_enum_values = ((Element) enumeration_list.item(i))
                    .getElementsByTagName(OMG_enumerator);
                this.m_scoped_discrim_kind = OMG_enum;
                return OMG_enum;
            }
        }

        SemanticException se = new SemanticException(
                                       "Error in Union. Discriminator '"
                                       + m_scoped_name
                                       + "' must be a previously defined integer, char, boolean or enum type.");
        se.locate(m_union_element);
        throw se;
    }

    // Checks the type of the case labels. If the discriminator is a scoped
    // type,
    // we must check all the case expresions with the original type of the
    // discriminator.
    // If it's an enumeration, the expresions must be of one of the valid names
    // defined in
    // the enumeration. If it's a single type (integer, char or boolean) the
    // case expresion
    // must be of the consistent type. Also, we must check that the range of the
    // expresion
    // matches the discriminator type.

    public abstract void checkCaseLabel(Element switch_el, Scope union_scope)
        throws SemanticException;

    public void fillSwitchBody()
    {
        m_switch_body = new java.util.Vector();
        //NodeList caseList = union_element.getElementsByTagName(OMG_case); //
        // DAVV - no sirve; si hacemos:
        //   union theOne switch (long) {
        //      case 1: union theTwo switch (boolean) {
        //          case true: ...
        //  ... }
        // incluye todos los case en theOne
        NodeList caseList = m_union_element.getChildNodes();
        for (int i = 0; i < caseList.getLength(); i++) {
            if (((Element) caseList.item(i)).getTagName().equals(OMG_case)) {
                Element value = (Element) ((Element) caseList.item(i))
                    .getLastChild();
                Element type = (Element) value.getFirstChild();
                Element declarator = (Element) value.getLastChild();
                java.util.Vector case_labels = new java.util.Vector();
                //NodeList exprList =
                // ((Element)caseList.item(i)).getElementsByTagName(OMG_expr);
                // // DAVV - tampoco vale, por lo mismo que un poco mas arriba
                NodeList exprList = caseList.item(i).getChildNodes();
                for (int j = 0; j < exprList.getLength(); j++) {
                    if (((Element) exprList.item(j))
                        .getTagName().equals(OMG_expr)) {
                        //if
                        // (((Element)exprList.item(j).getParentNode()).getTagName().equals(OMG_case)){
                        // // DAVV - ahora esta de mas
                        Element cons_expr = (Element) ((Element) exprList
                            .item(j)).getFirstChild();
                        case_labels.addElement(cons_expr);
                    }
                }
                boolean isDefault;
                //if(exprList.getLength() == 0){
                if (case_labels.size() == 0) {
                    this.m_has_default = true;
                    isDefault = true;
                } else {
                    isDefault = false;
                }
                UnionCase uc = new UnionCase(case_labels, type, declarator,
                                             isDefault);
                m_switch_body.addElement(uc);
            }
        }
    }

    public abstract void checkCaseLabelValues()
        throws SemanticException;

    public abstract String getDiscKind();

    protected BigInteger getDiscriminatorRange()
    {
        Element disc = (Element) m_union_element
            .getFirstChild().getFirstChild();
        if (m_scoped_name == null) {
            String name = disc.getAttribute(OMG_kind);
            if ((name == null) || name.equals(""))
                // We have an enumeration
                return calculateEnumRange();
            else
                // We have a basic discriminator
                return calculateBasicRange(name);
        } else {
            if ((m_scoped_discrim_kind == null)
                || m_scoped_discrim_kind.equals(OMG_enum))
                return calculateEnumRange();
            else
                return calculateBasicRange(m_scoped_discrim_kind);
        }
    }

    private BigInteger calculateBasicRange(String name)
    {
        if (name.equals(OMG_short) || name.equals(OMG_unsignedshort)
            || name.equals(OMG_wchar)) {
            return BigInteger.valueOf(65536L);
        } else if (name.equals(OMG_long) || name.equals(OMG_unsignedlong)) {
            return BigInteger.valueOf(4294967296L);
        } else if (name.equals(OMG_longlong)
                   || name.equals(OMG_unsignedlonglong)) {
            return new BigInteger("18446744073709551616");
        } else if (name.equals(OMG_char)) {
            return BigInteger.valueOf(256L);
        } else if (name.equals(OMG_boolean)) {
            return BigInteger.valueOf(2L);
        } else
            return BigInteger.ZERO;
    }

    private String calculateBasicDefaultValue(String name, Hashtable table)
    {
        if (name.equals(OMG_unsignedlong) || name.equals(OMG_unsignedshort)
            || name.equals(OMG_unsignedlonglong)) {
            BigInteger min = BigInteger.ZERO;
            while (true) {
                String val = min.toString();
                if (!table.containsKey(val))
                    return val;
                min = min.add(BigInteger.ONE);
            }
        } else if (name.equals(OMG_short)) {
            BigInteger min = new BigInteger("-32768");
            while (true) {
                String val = min.toString();
                if (!table.containsKey(val))
                    return val;
                min = min.add(BigInteger.ONE);
            }
        } else if (name.equals(OMG_long)) {
            BigInteger min = new BigInteger("-2147483648");
            while (true) {
                String val = min.toString();
                if (!table.containsKey(val))
                    return val;
                min = min.add(BigInteger.ONE);
            }
        } else if (name.equals(OMG_longlong)) {
            BigInteger min = new BigInteger("-9223372036854775808");
            while (true) {
                String val = min.toString();
                if (!table.containsKey(val))
                    return val;
                min = min.add(BigInteger.ONE);
            }
        } else if (name.equals(OMG_char)) {
            byte min = Byte.MIN_VALUE;
            while (true) {
                char c = (char) min;
                String val = String.valueOf(c);
                if (!table.containsKey(val))
                    return val;
                min++;
            }
        } else if (name.equals(OMG_wchar)) {
            short min = Short.MIN_VALUE;
            while (true) {
                char c = (char) min;
                String val = String.valueOf(c);
                if (!table.containsKey(val))
                    return val;
                min++;
            }
        } else if (name.equals(OMG_boolean)) {
            String val = "FALSE";
            if (!table.containsKey(val))
                return val;
            else
                return "TRUE";
        } else
            return null;
    }

    private BigInteger calculateEnumRange()
    {
        return BigInteger.valueOf((long) m_enum_values.getLength());
    }

    private String calculateEnumDefaultValue(Hashtable table)
    {
    	for (int i = 0; i < m_enum_values.getLength(); i++) {
            Element my_enum = (Element) m_enum_values.item(i);
            String enum_name = my_enum.getAttribute(OMG_name);
            StringTokenizer token = new StringTokenizer(m_scoped_name, "::");
            String scope = "";
            while (token.countTokens() > 1) {
                scope += token.nextElement();
            }

            //PRA
            if ((!table.containsKey(enum_name) && !table.containsKey(scope + "::" + enum_name))
            	|| !m_default_allowed) /* first item is the default value for _reset() */
                return enum_name;
            //EPRA
    	}

    	return null;
    }

    protected String calculateDefaultValue(Hashtable table)
    {
        Element disc = (Element) m_union_element
            .getFirstChild().getFirstChild();
        if (m_scoped_name == null) {
            String name = disc.getAttribute(OMG_kind);
            if ((name == null) || name.equals(""))
                // We have an enumeration
                return calculateEnumDefaultValue(table);
            else
                // We have a basic discriminator
                return calculateBasicDefaultValue(name, table);
        } else {
            if ((m_scoped_discrim_kind == null)
                || m_scoped_discrim_kind.equals(OMG_enum))
                return calculateEnumDefaultValue(table);
            else
                return calculateBasicDefaultValue(m_scoped_discrim_kind, table);
        }
    }

    public java.util.Vector getSwitchBody()
    {
        return this.m_switch_body;
    }

    public void setScopedDiscriminator(String scoped_name)
    {
        this.m_scoped_name = scoped_name;
    }

    public String getScopedDiscriminator()
    {
        return this.m_scoped_name;
    }

    // If a SemanticException is founded, the number of the line is added in
    // checkCaseLabel method
    protected void checkShortRange(String value)
        throws SemanticException
    {
        BigInteger bigValue = new BigInteger(value);
        BigInteger basicRange = calculateBasicRange(OMG_short);
        BigInteger lower = basicRange.shiftRight(1).negate();
        BigInteger higher = basicRange.shiftRight(1).subtract(BigInteger.ONE);
        if ((bigValue.compareTo(lower) == -1)
            || (bigValue.compareTo(higher) == 1))
            throw new SemanticException("Short value out of range. Value: "
                                        + bigValue.toString());
    }

    protected void checkUShortRange(String value)
        throws SemanticException
    {
        BigInteger bigValue = new BigInteger(value);
        BigInteger basicRange = calculateBasicRange(OMG_short);
        BigInteger lower = BigInteger.ZERO;
        BigInteger higher = basicRange.subtract(BigInteger.ONE);
        if ((bigValue.compareTo(lower) == -1)
            || (bigValue.compareTo(higher) == 1))
            throw new SemanticException(
                          "Unsigned Short value out of range. Value: "
                          + bigValue.toString());
    }

    protected void checkLongRange(String value)
        throws SemanticException
    {
        BigInteger bigValue = new BigInteger(value);
        BigInteger basicRange = calculateBasicRange(OMG_long);
        BigInteger lower = basicRange.shiftRight(1).negate();
        BigInteger higher = basicRange.shiftRight(1).subtract(BigInteger.ONE);
        if ((bigValue.compareTo(lower) == -1)
            || (bigValue.compareTo(higher) == 1))
            throw new SemanticException("Long value out of range. Value: "
                                        + bigValue.toString());
    }

    protected void checkULongRange(String value)
        throws SemanticException
    {
        BigInteger bigValue = new BigInteger(value);
        BigInteger basicRange = calculateBasicRange(OMG_long);
        BigInteger lower = BigInteger.ZERO;
        BigInteger higher = basicRange.subtract(BigInteger.ONE);
        if ((bigValue.compareTo(lower) == -1)
            || (bigValue.compareTo(higher) == 1))
            throw new SemanticException(
                          "Unsigned Long value out of range. Value: "
                          + bigValue.toString());
    }

    protected void checkLongLongRange(String value)
        throws SemanticException
    {
        BigInteger bigValue = new BigInteger(value);
        BigInteger basicRange = calculateBasicRange(OMG_longlong);
        BigInteger lower = basicRange.shiftRight(1).negate();
        BigInteger higher = basicRange.shiftRight(1).subtract(BigInteger.ONE);
        if ((bigValue.compareTo(lower) == -1)
            || (bigValue.compareTo(higher) == 1))
            throw new SemanticException("Long value out of range. Value: "
                                        + bigValue.toString());
    }

    protected void checkULongLongRange(String value)
        throws SemanticException
    {
        BigInteger bigValue = new BigInteger(value);
        BigInteger basicRange = calculateBasicRange(OMG_longlong);
        BigInteger lower = BigInteger.ZERO;
        BigInteger higher = basicRange.subtract(BigInteger.ONE);
        if ((bigValue.compareTo(lower) == -1)
            || (bigValue.compareTo(higher) == 1))
            throw new SemanticException(
                          "Unsigned Long value out of range. Value: "
                          + bigValue.toString());
    }

    public void checkEnumRange(Element expr)
        throws SemanticException
    {
        String labelName = expr.getAttribute(OMG_name); // tal y como se escribe
                                                        // en la union
        labelName = m_father_scope
            .getChild(m_union_element.getAttribute(OMG_name))
            .getCompleteName(labelName);
        

        for (int i = 0; i < m_enum_values.getLength(); i++) {
            String enumId = ((Element) m_enum_values.item(i))
                .getAttribute(OMG_name); // sin scope, tal y como se define en
                                         // el enum
            enumId = m_scoped_name
                .substring(0, m_scoped_name.lastIndexOf("::"))
                     + "::" + enumId; // le a�adimos el scope
            if (labelName.equals(enumId) || labelName.equals("::" + enumId)) {
                // Id founded, so we return
                return;
            }
        }
        // We haven't found the id, an exception is raised
        SemanticException se = new SemanticException(
                                       "The identifier '"
                                       + labelName
                                       + "' isn't a valid value for the enumeration '"
                                       + this.m_scoped_name
                                       + "'.");
        se.locate(expr);
        throw se;
    }

    public Element getUnionElement()
    {
        return this.m_union_element;
    }

    public String getScopedDiscrimKind()
    {
        return this.m_scoped_discrim_kind;
    }

    public boolean getDefaultAllowed()
    {
        return m_default_allowed;
    }

    public boolean getHasDefault()
    {
        return m_has_default;
    }

    public String getDefaultValue()
    {
        return m_default_value;
    }

    public Scope getFatherScope()
    {
        return m_father_scope;
    }
}