/*
* MORFEO Project
* http://www.morfeo-project.org
*
* Component: TIDIdlc
* Programming Language: Java
*
* File: $Source$
* Version: $Revision: 282 $
* Date: $Date: 2008-09-15 16:27:08 +0200 (Mon, 15 Sep 2008) $
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
import es.tid.TIDIdlc.xmlsemantics.*;
import org.w3c.dom.*;

import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Generates Cpp mapping for basic types and parameters.
 */
public class XmlType2Cpp
    implements Idl2XmlNames
{
    /**
     * Returns the Cpp type for an XML node
     * 
     * @param doc
     *            The XML node where the type is.
     * @return The Cpp type.
     */
    public static String getType(Element doc)
    {
        String tag = doc.getTagName();
        if (tag.equals(OMG_type)) {
            return basicMapping(doc.getAttribute(OMG_kind));
        } else if (tag.equals(OMG_scoped_name)) {
            return TypeManager.convert(doc.getAttribute(OMG_name));
            //return basicMapping(getUnrolledName(doc)); // DAVV - RELALO
        } else if (tag.equals(OMG_sequence)) {
            NodeList nodes = doc.getChildNodes();
            Element type = (Element) nodes.item(0);
            String convertedType = getType(type);
            String definition = getDefinitionType(type);
            if (definition.equals(OMG_kind) || definition.equals(OMG_interface))
                return convertedType;
            return convertedType + "*";
        } else if (tag.equals(OMG_enum) /*|| tag.equals(OMG_struct)*/
                   || tag.equals(OMG_union) || tag.equals(OMG_exception)
                   || tag.equals(OMG_interface)) {
            return TypeManager.convert(doc.getAttribute(OMG_scoped_name));
            // String name = getUnrolledName(doc.getAttribute(OMG_scoped_name));
            //return getElementInUnionType(name);
        } else if (tag.equals(OMG_struct)) {
        	return TypeManager.convertName(doc.getAttribute(OMG_scoped_name));
        } else
            return "unknownType";
    }
    
    public static String getTypeHelper(Element doc)
    {
        String tag = doc.getTagName();
        if (tag.equals(OMG_scoped_name)) {
            return TypeManager.convertName(doc.getAttribute(OMG_name));
            //return basicMapping(getUnrolledName(doc)); // DAVV - RELALO
        } else if (tag.equals(OMG_sequence)) {
            NodeList nodes = doc.getChildNodes();
            Element type = (Element) nodes.item(0);
            String convertedType = getType(type);
            String definition = getDefinitionType(type);
            if (definition.equals(OMG_kind) || definition.equals(OMG_interface))
                return convertedType;
            return convertedType + "*";
        } else if ( (tag.equals(OMG_struct)) || 
                    (tag.equals(OMG_union))  ||
                    (tag.equals(OMG_enum))) {
            // Fix bug [#682] Compilation Error: unknownType with nested structs and unions
            return TypeManager.convert(doc.getAttribute(OMG_scoped_name));
        }  else if /*(tag.equals(OMG_enum) || tag.equals(OMG_struct)
                || tag.equals(OMG_union) || tag.equals(OMG_exception)
                ||*/ (tag.equals(OMG_interface)) {
	         return TypeManager.convertName(doc.getAttribute(OMG_scoped_name));
	         // String name = getUnrolledName(doc.getAttribute(OMG_scoped_name));
	         //return getElementInUnionType(name);
     } else
            return "unknownType";
    }
    
    public static String getTypeTypedef (Element doc){
    	String tag = doc.getTagName();
        if (tag.equals(OMG_type)) {
            return basicMapping(doc.getAttribute(OMG_kind));
        } else if (tag.equals(OMG_scoped_name)) {
            return TypeManager.convertName(doc.getAttribute(OMG_name));
            //return basicMapping(getUnrolledName(doc)); // DAVV - RELALO
        } else
            return "unknownType";
    }
    /**
     * Returns the Cpp type for an XML node
     * 
     * @param doc
     *            The XML node where the type is.
     * @return The Cpp type.
     */
    public static String getTypeName(Element doc)
    {
        String tag = doc.getTagName();
        if (tag.equals(OMG_type)) {
            return basicMapping(doc.getAttribute(OMG_kind));
        } else if (tag.equals(OMG_scoped_name)) {
            return TypeManager.convertName(doc.getAttribute(OMG_name));
            //return basicMapping(getUnrolledName(doc)); // DAVV - RELALO
        } else if (tag.equals(OMG_sequence)) {
            NodeList nodes = doc.getChildNodes();
            Element type = (Element) nodes.item(0);
            String convertedType = getTypeName(type);
            String definition = getDefinitionType(type);
            if (definition.equals(OMG_kind) || definition.equals(OMG_interface))
                return convertedType;
            return convertedType + "*";
        } else if (tag.equals(OMG_enum) || tag.equals(OMG_struct)
                   || tag.equals(OMG_union) || tag.equals(OMG_exception)
                   || tag.equals(OMG_interface)) {
            return TypeManager.convert(doc.getAttribute(OMG_scoped_name));
            // String name = getUnrolledName(doc.getAttribute(OMG_scoped_name));
            //return getElementInUnionType(name);
        } else
            return "unknownType";
    }

    /**
     * Returns the Cpp type for method parameter (in/out/inout). Table 1-3 pag
     * 106 IDL C++ mapping, inout and out parameters are equals except with not
     * fixed (variable) types.
     * 
     * @param doc
     *            The XML node where the type is.
     * @param kind
     *            True if it is an OUT or INOUT parameter.
     * @return The Cpp type.
     */
    /* RELALO */
    public static String getParamType(Element doc, String kind)
    {
        String tag = doc.getTagName();
        boolean in = kind.equals("in");
        boolean inout = kind.equals("inout");
        boolean out = kind.equals("out");
        if (tag.equals(OMG_type)) {
            String att = doc.getAttribute(OMG_kind);
            if (out) {
                att = basicOutMapping(att);
            } else if (in) {
                if (att.equals(OMG_any) || att.equals(OMG_fixed))
                    att = "const " + basicMapping(att) + "&";
                else if (isAString(doc) || isAWString(doc))
                    att = "const " + basicMapping(att);
                else {
                		att = basicMapping(att);
                }
                    
            } else if (inout) {
                att = basicInOutMapping(att);
            }
            String named = "";
            named = doc.getAttribute(OMG_name) == "" ? att : doc.getAttribute(OMG_name);
            if (named != "")
            	return att;
            else
            	return named;
        } else if (tag.equals(OMG_scoped_name)) {
            String definition = getDefinitionType(doc);

            if (definition.equals(OMG_array)) {
                if (in)
                    return "const " + getType(doc); //doc.getAttribute(OMG_name);
                else if (out)
                    if (isVariableSizeType(doc))
                        return /* doc.getAttribute(OMG_name) */getType(doc)
                                                              + "_slice*&";
                    else
                        return /* doc.getAttribute(OMG_name) */getType(doc);
                else if (inout)
                    return /* doc.getAttribute(OMG_name) */getType(doc);
            } else if (definition.equals(OMG_sequence)) {
                if (in)
                    return "const "
                           + /* doc.getAttribute(OMG_name) */getType(doc) + "&";
                if (out)
                    return /* doc.getAttribute(OMG_name) */getType(doc) + "*&";
                if (inout)
                    return /* doc.getAttribute(OMG_name) */getType(doc) + "&";
            } else if (definition.equals(OMG_struct)
                       || definition.equals(OMG_union)
                       || definition.equals(OMG_exception)) {// Structura de
                                                             // tama?o variable.
                if (out) {
                    if (isVariableSizeType(doc))
                        //return getUnrolledHolderName(doc) + "*&"; 
                        return getType(doc) + "*&";
                    else
                        //return getUnrolledHolderName(doc) + "&"; 
                        return getType(doc) + "&";
                } else if (in) {
                    return "const " + getType(doc) + "&";
                } else if (inout) {
                    //return getUnrolledHolderName(doc) + "&"; 
                    return getType(doc) + "&";
                }
            } else if (definition.equals(OMG_kind)
                       || definition.equals(OMG_enum)) {
                String deep_kind = getDeepKind(doc);
                if (out) {
                    if (deep_kind.equals(OMG_any))
                        return getType(doc) + "*&";
                    else if (isAString(doc) || isAWString(doc))
                        return basicMapping(getDeepKind(doc)) + "&";
                    else if(deep_kind.equals(OMG_Object))
                        return getType(doc) + "_ptr&";
                    else
                        return getType(doc) + "&"; // getUnrolledHolderName(doc)
                                                   // + "&";
                } else if (in) {
                    
                    if (deep_kind.equals(OMG_any)
                        || deep_kind.equals(OMG_fixed))
                        return "const " + getType(doc) + "&";
                    else if (isAString(doc) || isAWString(doc))
                        return "const " + basicMapping(getDeepKind(doc));
                        //basicMapping(getUnrolledName(doc));
                    else if(deep_kind.equals(OMG_Object))
                        return getType(doc) + "_ptr";
                    else 
                        return getType(doc);
                        //basicMapping(getUnrolledName(doc));
                } else if (inout) {
                    if (isAString(doc) || isAWString(doc))
                        return basicMapping(getDeepKind(doc)) + "&";
                    else if(deep_kind.equals(OMG_Object))
                        return getType(doc) + "_ptr&";
                    else
                        return getType(doc) + "&"; // getUnrolledHolderName(doc)
                                                   // + "&";
                }
            } else if (definition.equals(OMG_interface)) {
                if (out) {
                    //return getUnrolledHolderName(doc) + "_out"; 
                    return getType(doc) + "_ptr&";
                } else if (in) {
                    //return getUnrolledName(doc) + "_ptr"; 
                    return getType(doc) + "_ptr";
                } else if (inout) {
                    //return getUnrolledHolderName(doc) + "_ptr&"; 
                    return getType(doc) + "_ptr&";
                }
            } else if (definition.equals(OMG_native)) {
                if (out) {
                    //return getUnrolledHolderName(doc) + "&"; 
                    return getType(doc) + "&";
                } else if (in) {
                    //return
                    // getUnrolledName(doc);//basicMapping(getUnrolledName(doc))
                    // + "*"; 
                    return getType(doc);
                } else if (inout) {
                    //return getUnrolledHolderName(doc) + "&"; 
                    return getType(doc) + "&";
                }
            } else if (definition.equals(OMG_valuetype)) {
                if (out)
                    return getType(doc) + "*&";
                else if (in)
                    return getType(doc) + "*";
                else if (inout)
                    return getType(doc) + "*&";
            }
        }
        /*
         * la siguiente parte se incorpora para facilitar la generacion
         * del constructor con parametros de las clase OBV_ generada para
         * valuetypes, cuya semantica es la misma que la de parametros 'in'
         */
        else if (tag.equals(OMG_struct) || tag.equals(OMG_union)
                 || tag.equals(OMG_exception) || tag.equals(OMG_sequence)) {
            if (in)
                return "const " + getType(doc) + "&";
            if (out)
                return /* doc.getAttribute(OMG_name) */getType(doc) + "*&";
            if (inout)
                return /* doc.getAttribute(OMG_name) */getType(doc) + "&";
        } else if (tag.equals(OMG_enum)) {
            if (in)
                return "const " + getType(doc);
        }
        return "unknownType";
    }
    
    /** Returns the Cpp type for member of an structured type
    * @param doc
    *            The XML node where the type is.    
    * @return The Cpp type.
    */
   /* RELALO */
   public static String getMemberType(Element doc)
   {
       String tag = doc.getTagName();  
       
       if (tag.equals(OMG_type)) {           
           String att = doc.getAttribute(OMG_kind);
           
           if (att.equals(OMG_Object)) {
               att = "CORBA::Object_var";
           } else if (isAString(doc)) {               
               att = "CORBA::String_var";
           } else if (isAWString(doc)){
               att = "CORBA::WString_var";
           }else {
           		att = basicMapping(att);
           }           
           String named = "";
           
           named = doc.getAttribute(OMG_name) == "" ? att : doc.getAttribute(OMG_name);
           
           if (named != "")
           	return att;
           else
           	return named;
           
       } else if (tag.equals(OMG_scoped_name)) {
           
           String definition = getDefinitionType(doc);

           if (definition.equals(OMG_kind)
                      || definition.equals(OMG_enum)) 
           {               
               String deep_kind = getDeepKind(doc);
               
               if(deep_kind.equals(OMG_Object)                   
                  || deep_kind.equals(OMG_value))
               {
                       return getType(doc) + "_var";
               } else if (isAString(doc)) {               
                   return "CORBA::String_var";
               } else if (isAWString(doc)){
                   return "CORBA::WString_var";
               }  else {
                   return getType(doc);
               }     
               
           } else if (definition.equals(OMG_interface) 
                      || definition.equals(OMG_valuetype)) {            
                   return getType(doc) + "_var";  
           } else {
               return getType(doc);
           }
       } else {
           return getType(doc);
       }       
   } 
   
   
   
   

 /** Returns the Cpp type for member of an structured type
  * @param doc
  *            The XML node where the type is. 
  * @param valueType 
  *             the Container valuetype
  * @param member
  *             the member name   
  * @return The Cpp type.
  */
 /* RELALO */
 public static String getMemberReturnAccesor(Element doc, String valueType, String member)
 {
     String tag = doc.getTagName();       
     
     
     if (tag.equals(OMG_type)) {
         String att = doc.getAttribute(OMG_kind);
         if (att.equals(OMG_Object)) {
             return " return CORBA::Object::_duplicate("+ member+ ");\n";
         } else if (att.equals(OMG_value)) {
             return "CORBA::add_ref((("+ valueType + "*)this)->" + member+");\n\t" +
             		"return (("+ valueType + "*)this)->" + member +";\n";    
         } else {
             return "return " + member + ";\n";
         }         
         
     } else if (tag.equals(OMG_scoped_name)) {
         
         String definition = getDefinitionType(doc);

         if (definition.equals(OMG_kind)
                    || definition.equals(OMG_enum)) 
         {               
             String deep_kind = getDeepKind(doc);             
             
             if (deep_kind.equals(OMG_Object)) {
                 return " return CORBA::Object::_duplicate("+member+");\n";
             }else if (deep_kind.equals(OMG_value)) {
                 return "CORBA::add_ref((("+ valueType + "*)this)->"  + member+");\n\t" +
             		"return (("+ valueType + "*)this)->" + member +";\n";
             } else {
                 return "return " + member + ";\n";
             }    
             
         } else if (definition.equals(OMG_interface)) {
             String type = getType(doc);
             return "return " + type + "::_duplicate(" + member + " );\n";
         }else if(definition.equals(OMG_valuetype)) {            
             return "CORBA::add_ref((("+ valueType + "*)this)->"  + member+");\n\t" +
      		        "return (("+ valueType + "*)this)->" + member +";\n";  
         } else {
             return "return " + member + ";\n";
         }
     } else {
         return "return " + member + ";\n";
     }       
 } 
    /**
     * Returns the Cpp helper type for an XML node.
     * 
     * @param doc
     *            The XML node where the type is.
     * @return The Cpp type.
     */
    public static String getHelperType(Element doc)
    {
        String tag = doc.getTagName();
        if (tag.equals(OMG_type)) {
            return null;
        } else if (tag.equals(OMG_scoped_name)
                   || (doc.getAttribute(OMG_scoped_name) != null 
                       && doc.getAttribute(OMG_scoped_name) != "") || tag.equals(OMG_sequence)) {
            //String helper = getUnrolledHelperName(doc); // DAVV - RELALO
            //return helper;
            return getHelperName(getTypeHelper(doc));
        } else
            return "unknownType";
    }
    
    

    /**
     * Returns the Cpp typecode for an XML node.
     * 
     * @param doc
     *            The XML node where the type is.
     * @return The Cpp typecode.
     */
    public static String getTypecode(Element doc)
        throws Exception
    {
        String tag = doc.getTagName();
        
        if (tag.equals(OMG_type)) {
            String s = basicORBTcKindMapping(doc);
            String ret = "";
            if ((s.equals(OMG_string) || s.equals(OMG_wstring))
                && doc.getFirstChild() != null) {
                Element expr = (Element) doc.getFirstChild();
                ret = "TIDorb::portable::TypeCodeFactory::create_" + s + "_tc("
                      + XmlExpr2Cpp.getIntExpr(expr) + ")";
            } else if ( (s.equals(OMG_fixed)) && (doc.getFirstChild() != null) &&
                        (doc.getFirstChild() != null) ) {
                Element digits = (Element) doc.getFirstChild();
                Element scale = (Element) doc.getLastChild();
                ret = "TIDorb::portable::TypeCodeFactory::create_" + s + "_tc("
                    + XmlExpr2Cpp.getIntExpr(digits) + ", " 
                    + XmlExpr2Cpp.getIntExpr(scale) + ")";
            } else if ( (s.equals(OMG_value)) ) {
                ret = "TIDorb::portable::TypeCodeFactory::create_" + s + 
                    "_tc((const ::CORBA::RepositoryId )id(), (const ::CORBA::Identifier ) " +
                    /* TODO ((Element)doc.getParentNode()).getAttribute(OMG_name) + */  
                    "\"\"" + ", 0 , 0 , 0)";

            } else
                ret = "TIDorb::portable::TypeCodeFactory::get_basic_TypeCode(::CORBA::tk_"
                      + s + ")";
            return ret;
        } else if (tag.equals(OMG_enum) || tag.equals(OMG_struct)
                   || tag.equals(OMG_union) || tag.equals(OMG_exception)) {
            // Added to support the declaration of enumerations, structs into a
            // Union body
            String helper = getHelperType(doc);
            if (helper == null) { 
                String s = basicORBTcKindMapping(doc);
                return "::TIDorb::portable::TypeCodeFactory::get_basic_TypeCode(::CORBA::tk_"
                       + s + ")";
            } else
                return helper + "::type()";
        } else if (tag.equals(OMG_scoped_name)) {
        	Node father = doc.getParentNode().getParentNode();
        	try{
        		Node grandfather = doc.getParentNode().getParentNode().getParentNode();
        		String namefather = ((Element) father).getAttribute(OMG_name); 
        		String namegrandfather = ((Element) grandfather).getAttribute(OMG_name);
                                         
        		if ((father != null) && (grandfather != null)
        				&& getType(doc).equals(namegrandfather + "::" + namefather)
        				&& !((Element) father).getTagName().equals(OMG_sequence)
        				&& ((Element) father).getTagName().equals(OMG_valuetype))
        			// DAVV - para tipos recursivos: (ver CORBA 2.6, 4.11.3, pag
        			// 201, ejemplo 'valuetype V {public V member;};'
        		{
        			if (((Element) father).getTagName().equals(OMG_valuetype))
        				return "TIDorb::portable::TypeCodeFactory::create_recursive_tc(id())";
        		}
        
        		else if (father != null
                     && ((Element) father).getTagName().equals(OMG_sequence)) {
        			father = father.getParentNode();
        			if (father != null
        					&& ((Element) father).getTagName().equals(OMG_typedef)) {
        				father = father.getParentNode();
        				if (father != null
        						&& ((Element) father).getTagName().equals(OMG_sequence)) {
        					// esta condicion es por la forma en que
        					// se generan las sequences anonimas dentro de structs,
        					// unions...
        					father = father.getParentNode();
        					if (father != null
        							&& getType((Element) father).equals(getType(doc)))
        						return "TIDorb::portable::TypeCodeFactory::create_recursive_tc(CORBA::string_dup(\""
                                   + RepositoryIdManager.getInstance().get((Element) father)
                                   + "\"))";
        				}
        			}
        		}
        		else if (father != null){
        			// For this construction:
        			// struct Foo; // Forward declaration
        			// typedef sequence<Foo> FooSeq;
        			// struct Bar {
        			//   long value;
        			//   FooSeq chain; //Illegal, Foo is not an enclosing struct or union
        			// };
            	          	
            		father = doc.getParentNode();
            		namefather = ((Element) father).getAttribute(OMG_name);                
            		if ( (father != null) ){                	
            			if (((Element) father).getTagName().equals(OMG_struct)) {            	                                   	
            				String type = getType(doc);                 	
            				String definition_type = getDefinitionType(doc);
            				String type2;
            				type2 = type.replaceAll("[a-zA-Z]*::","");                    
            				if (definition_type.equals(OMG_sequence)){                    		
            					if (getSequenceType(doc, type2).equals("::" + getType((Element)father))){
            						return "TIDorb::portable::TypeCodeFactory::create_recursive_tc("
                    					+ "_" + type2 + "Helper::id()" 
                    					+ ")";
            					}
                    		}                    			
                    	}
                    }
        		}
        	} catch (Exception e){               	
        	}

            String helper = getHelperType(doc);
            if (helper == null) { // creo q en C++ es un caso imposible,
                                  // pero por si acaso lo mantendr???...
                String s = basicORBTcKindMapping(doc);
                return "::TIDorb::portable::TypeCodeFactory::get_basic_TypeCode(::CORBA::tk_"
                       + s + ")";
            } else
                return helper + "::type()";
        } else if (tag.equals(OMG_sequence)) {
            // Added to support the declaration of enumerations, structs into a
            // Union body
            NodeList nodes = doc.getChildNodes();
            Element type = (Element) nodes.item(0);
            String elem_typecode = getTypecode(type);
            Element expr = (Element) type.getNextSibling();
            String value = "0";
            if (expr != null)
                value = String.valueOf(XmlExpr2Cpp.getIntExpr(expr));
            return "TIDorb::portable::TypeCodeFactory::create_sequence_tc("
                   + value + ", " + elem_typecode + ")";
        } else if (tag.equals(OMG_interface)) {
            boolean isLocal = doc.getAttribute(OMG_local).equals(OMG_true);
            String ret = "TIDorb::portable::TypeCodeFactory::create_";
            if (isLocal)
                ret += "local_";
            ret += "interface_tc((const ::CORBA::RepositoryId )id(), (const ::CORBA::Identifier ) \""
                   + doc.getAttribute(OMG_name) + "\")";
            return ret;
        } else
            return "unknownType";
    }

    
    /**
     * Returns the Cpp type reader for an XML node.
     * 
     * @param doc
     *            The XML node where the type is.
     * @param inputStreamName
     *            name of the inputStream to be used.
     * @return The Cpp type reader.
     */
    public static String getTypeReader(Element doc, String inputStreamName)
    {
        String tag = doc.getTagName();
        if (tag.equals(OMG_type)) {
            String s = basicORBTypeMapping(doc);
            return inputStreamName + ".read_" + s + "(";
        } else if (tag.equals(OMG_scoped_name)) {
            String name = getHelperType(doc);//getUnrolledHelperName(doc); 
            if (name == null) {
                String s = basicORBTypeMapping(doc);
                return inputStreamName + ".read_" + s + "(";
            } else
                return /* getHelperFromScopedName(name) */name + "::read("
                                                         + inputStreamName
                                                         + ",";
        } else if (tag.equals(OMG_enum) || tag.equals(OMG_struct)
                   || tag.equals(OMG_union) || tag.equals(OMG_exception)  || tag.equals(OMG_sequence)) {
            String name = getHelperType(doc); //getUnrolledHelperName(doc.getAttribute(OMG_scoped_name));
            //name = getElementInUnionType(name);
            if (name == null) {
                String s = basicORBTypeMapping(doc);
                return inputStreamName + ".read_" + s + "(";
            } else
                return /* getHelperFromScopedName(name) */name + "::read("
                                                         + inputStreamName
                                                         + ",";
        } else
            return "reading_unknownType";
    }

    /**
     * Returns the Cpp type writer for an XML node.
     * 
     * @param doc
     *            The XML node where the type is.
     * @param outputStreamName
     *            Name of the outputStream to be used.
     * @param outputData
     *            Name of the output data.
     * @return The Cpp type writer.
     */
    public static String getTypeWriter(Element doc, String outputStreamName,
                                       String outputData)
        throws SemanticException
    {
        String tag = doc.getTagName();
        if (tag.equals(OMG_type)) {
            String s = basicORBTypeMapping(doc);
            if (s.equals(OMG_fixed))
                return outputStreamName + ".write_" + s + "(" + outputData
                       + ", " + XmlExpr2Cpp.getIntExpr(doc.getFirstChild())
                       + ", " + XmlExpr2Cpp.getIntExpr(doc.getLastChild())
                       + ")";
            else
                return outputStreamName + ".write_" + s + "(" + outputData
                       + ")";
        } else if (tag.equals(OMG_scoped_name)) {
            String name = getHelperType(doc);/* getUnrolledHelperName(doc); */
            if (name == null) { // DAVV - esto en realidad no pasa ya nunca
                String s = basicORBTypeMapping(doc);
                return outputStreamName + ".write_" + s + "(" + outputData
                       + ")";
            } else {
                return name + "::write(" + outputStreamName + "," + outputData
                       + ")";
            }
        } else if (tag.equals(OMG_enum) || tag.equals(OMG_struct)
                   || tag.equals(OMG_union) || tag.equals(OMG_exception) || tag.equals(OMG_sequence)){
            String name = getHelperType(doc);
            if (name == null) {
                String s = basicORBTypeMapping(doc);
                return outputStreamName + ".write_" + s + "(" + outputData
                       + ")";
            } else
                return name + "::write(" + outputStreamName + "," + outputData
                       + ")";
        } else
            return "writing_unknownType()";
    }

    /**
     * Returns the Cpp type for an IDL basic type.
     * 
     * @param type
     *            The IDL type.
     * @return The Cpp type.
     */
    public static String basicMapping(String type)
    {
        if (type.equals(OMG_char)) {
            return "CORBA::Char";
        } else
        if (type.equals(OMG_wchar)) {
            return "CORBA::WChar";
        } else if (type.equals(OMG_octet)) {
            return "CORBA::Octet";
        } else if (type.equals(OMG_string)) {
            return "char*"; // MACP: for mapping Strings to BasicString not
                            // CORBA::String.
        } else if (type.equals(OMG_wstring)) {
            //return "wchar*"; // MACP: for mapping Strings to BasicString not
            // CORBA::String.
            return "CORBA::WChar*"; 
        } else if (type.equals(OMG_unsignedshort)) {
            return "CORBA::UShort";
        } else if (type.equals(OMG_long)) {
            return "CORBA::Long";
        } else if (type.equals(OMG_unsignedlong)) {
            return "CORBA::ULong";
        } else if (type.equals(OMG_longlong)) {
            return "CORBA::LongLong";
        } else if (type.equals(OMG_unsignedlonglong)) {
            return "CORBA::ULongLong";
        } else if (type.equals(OMG_fixed)) {
            return "CORBA::Fixed";
        } else if (type.equals(OMG_any)) {
            return "CORBA::Any";
        } else if (type.equals(OMG_boolean)) {
            return "CORBA::Boolean";
        } else if (type.equals(OMG_Object)) {
            return "CORBA::Object_ptr";
        } else if (type.equals(OMG_TypeCode)) {
            return "CORBA::TypeCode_ptr";
        } else if (type.equals(OMG_ValueBase)) {
            return "CORBA::ValueBase_ptr";
        } else if (type.equals(OMG_AbstractBaseCode)) {
            //return "CORBA::Object_ptr";
        	return "CORBA::AbstractBase_ptr";
        } else if (type.equals(OMG_AbstractBase)) {
        	return "CORBA::AbstractBase_ptr";
        } else if (type.equals(OMG_short)) {
            return "CORBA::Short";
        } else if (type.equals(OMG_float)) {
            return "CORBA::Float";
        } else if (type.equals(OMG_double)) {
            return "CORBA::Double";
        } else if (type.equals(OMG_longdouble)) {
            return "CORBA::LongDouble";
        }

        return type;
    }

    /**
     * Returns the Cpp Holder type for an IDL basic OUT type.
     * 
     * @param type
     *            The IDL type.
     * @return The Cpp Holder type.
     */
    public static String basicOutMapping(String type)
    {
        if (type.equals(OMG_char)) {
            return "CORBA::Char_out";
        } else if (type.equals(OMG_wchar)) {
            return "CORBA::WChar_out";
        } else if (type.equals(OMG_octet)) {
            return "CORBA::Octet_out";
        } else if (type.equals(OMG_string)) {
            return "char*&";
        } else if (type.equals(OMG_wstring)) {
            return "CORBA::WChar*&";
        } else if (type.equals(OMG_short)) {
            return "CORBA::Short_out";
        } else if (type.equals(OMG_unsignedshort)) {
            return "CORBA::UShort_out";
        } else if (type.equals(OMG_long)) {
            return "CORBA::Long_out";
        } else if (type.equals(OMG_unsignedlong)) {
            return "CORBA::ULong_out";
        } else if (type.equals(OMG_longlong)) {
            return "CORBA::LongLong_out";
        } else if (type.equals(OMG_unsignedlonglong)) {
            return "CORBA::ULongLong_out";
        } else if (type.equals(OMG_fixed)) {
            return "CORBA::Fixed_out";
        } else if (type.equals(OMG_any)) {
            return "CORBA::Any*&";
        } else if (type.equals(OMG_boolean)) {
            return "CORBA::Boolean_out";
        } else if (type.equals(OMG_Object)) {
            return "CORBA::Object_out";
        } else if (type.equals(OMG_TypeCode)) {
            return "CORBA::TypeCode_out";
        } else if (type.equals(OMG_ValueBase)) {
            return "CORBA::ValueBase_out";
        } else if (type.equals(OMG_float)) {
            return "CORBA::Float_out";
        } else if (type.equals(OMG_double)) {
            return "CORBA::Double_out";
        } else if (type.equals(OMG_longdouble)) {
            return "CORBA::LongDouble_out";
        }

        return null;
    }

    /**
     * Returns the Cpp Holder type for an IDL basic INOUT type.
     * 
     * @param type
     *            The IDL type.
     * @return The Cpp Holder type.
     */
    public static String basicInOutMapping(String type)
    {
        if (type.equals(OMG_char)) {
            return "CORBA::Char&";
        } else if (type.equals(OMG_wchar)) {
            return "CORBA::WChar&";
        } else if (type.equals(OMG_octet)) {
            return "CORBA::Octet&";
        } else if (type.equals(OMG_string)) {
            return "char*&";
        } else if (type.equals(OMG_wstring)) {
            return "CORBA::WChar*&";
        } else if (type.equals(OMG_short)) {
            return "CORBA::Short&";
        } else if (type.equals(OMG_unsignedshort)) {
            return "CORBA::UShort&";
        } else if (type.equals(OMG_long)) {
            return "CORBA::Long&";
        } else if (type.equals(OMG_unsignedlong)) {
            return "CORBA::ULong&";
        } else if (type.equals(OMG_longlong)) {
            return "CORBA::LongLong&";
        } else if (type.equals(OMG_unsignedlonglong)) {
            return "CORBA::ULongLong&";
        } else if (type.equals(OMG_fixed)) {
            return "CORBA::Fixed&";
        } else if (type.equals(OMG_any)) {
            return "CORBA::Any&";
        } else if (type.equals(OMG_boolean)) {
            return "CORBA::Boolean&";
        } else if (type.equals(OMG_Object)) {
            return "CORBA::Object_ptr&";
        } else if (type.equals(OMG_TypeCode)) {
            return "CORBA::TypeCode&";
        } else if (type.equals(OMG_ValueBase)) {
            return "CORBA::ValueBase&";
        } else if (type.equals(OMG_float)) {
            return "CORBA::Float&";
        } else if (type.equals(OMG_double)) {
            return "CORBA::Double&";
        } else if (type.equals(OMG_longdouble)) {
            return "CORBA::LongDouble&";
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
            type = getDeepKind(el);// getUnrolledName(el); 
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
            type = getDeepKind(el);// getUnrolledName(el); 
        }
        return basicORBTcKindMapping(type);
    }

    /**
     * IDL -> ORB (read, write, insert, extract) es para componer read_+s; por
     * lo tanto no son tipos enteros
     */

    public static String basicORBTypeMapping(String type)
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
        } else if (type.equals(OMG_ValueBase)) {
            return "Value";        
        } else if (type.equals(OMG_TypeCode)) {
            return "TypeCode";
        } else if (type.equals(OMG_longdouble)) {
            return "longdouble"; // DAVV
        }
        return type;
    }

    /**
     * return the IDL to ORB Mapping based on TCKind
     * 
     * @param type
     *            the OMG_IDL name of the type.
     * @return the TCKind fot the type.
     */
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
            // return "Object"; - 
            // al cambiar en getTypecode los '_tc_'
            // por 'tk_' para tipos basicos
            return "objref"; // se cambia el nombre del typecode para Objects
        } else if (type.equals(OMG_TypeCode)) {
            return "TypeCode";
        } else if (type.equals(OMG_longdouble)) {
            return "longdouble"; 
        } else if (type.equals(OMG_ValueBase)) {
			// Fix bug [#685] Compilation Error: ValueBase typedefs
            return "value"; 
        }
        return type;
    }

    /**
     * Returns the Cpp default constructor for an IDL type.
     * 
     * @param doc
     *            The IDL type.
     * @return The Cpp type.
     *  
     */
    public static String getDefaultConstructor(Element doc)
    {
        String type = "";
        String name = doc.getAttribute(OMG_name);
        String definedAs = getDefinitionType(doc);
        if (doc.getTagName().equals(OMG_scoped_name))
            type = XmlType2Cpp.getType(doc);
        else
            type = XmlType2Cpp.basicMapping(name);
        if (definedAs.equals(OMG_sequence)) {
            return "new " + name + "()";
        }
        if (type.equals(""))
            type = doc.getAttribute(OMG_kind);
        if (type.equals(OMG_wchar)) {
            return "\'\0'";
        } else if (type.equals(OMG_octet)) {
            return "\'\0'";
        } //else if (type.equals(OMG_string) || type.equals("char*")) {
        else if (isAString(doc)) {
            return "CORBA::string_dup(\"\")"; // MACP: for mapping Strings to
                                              // BasicString not CORBA::String.
        } // else if (type.equals(OMG_wstring)|| type.equals("wchar*") ||
          // type.equals("CORBA::WChar*")) {
        else if (isAWString(doc)) {
            return "CORBA::wstring_dup(L\"\")"; // DAVV - for wstrings
        } else if (type.equals(OMG_short)) {
            return "0";
        } else if (type.equals(OMG_unsignedshort)) {
            return "0";
        } else if (type.equals(OMG_long)) {
            return "0";
        } else if (type.equals(OMG_unsignedlong)) {
            return "0";
        } else if (type.equals(OMG_longlong)) {
            return "0L";
        } else if (type.equals(OMG_unsignedlonglong)) {
            return "0UL";
        } else if (type.equals(OMG_fixed)) {
            return "CORBA::Fixed()";
        } else if (type.equals(OMG_any)) {
            return "CORBA::Any()";
        } else if (type.equals(OMG_boolean)) {
            return "false";
        } else if (type.equals(OMG_Object)) {
            return "CORBA::Object::_nil()";
        } else if (type.equals(OMG_TypeCode)) {
            return "CORBA::TypeCode::_nil()";
        } else if (type.equals(OMG_ValueBase)) {
            return "NULL";
        } else if (type.equals(OMG_AbstractBaseCode)) {
            return "NULL";
        }

        Scope scope = Scope.getGlobalScopeInterface(type);
        if (scope != null) {
            Element definition = scope.getElement();
            if (definition != null) {
                String tag = definition.getTagName();
                if (tag.equals(OMG_interface))
                    return type + "::_nil()";
            }
        }
        return "new " + name + "()";
    }

    /**
     * There are some types that needs a little modification to be extracted
     * from an Any. 1byte types, Strings... other.
     * 
     * @param element
     *            The type.
     * @param varName
     *            The name of the variable that has the space for the extracted
     *            value.
     * @return the extraction sintaxis.
     */
    public static String getAnyExtraction(Element element, String varName)
    {
        StringBuffer buffer = new StringBuffer();
        String type = element.getAttribute(OMG_kind);
        if (element.getTagName().equals(OMG_scoped_name))
            if (XmlType2Cpp.getDefinitionType(element).equals(OMG_kind))
                type = XmlType2Cpp.getDeepKind(element);
        if (type.equals(OMG_wchar)) {
            buffer.append("CORBA::Any::to_wchar(");
        } else if (type.equals(OMG_octet)) {
            buffer.append("CORBA::Any::to_octet(");
        } else if (type.equals(OMG_string)) {
            buffer.append("(");
        } else if (type.equals(OMG_wstring)) {
            buffer.append("(");
        } else if (type.equals(OMG_fixed)) {
            buffer.append("CORBA::Any::to_fixed(");
        } else if (type.equals(OMG_char)) {
            buffer.append("CORBA::Any::to_char(");
        } else if (type.equals(OMG_boolean)) {
            buffer.append("CORBA::Any::to_boolean(");
        } else if (type.equals(OMG_Object)) {
            buffer.append("CORBA::Any::to_object(");
        } else {
            buffer.append("(");
        }

        buffer.append(varName);
        if (type.equals(OMG_fixed))
            buffer.append(", 0, 0");
        buffer.append(")");
        return buffer.toString();

    }

    /**
     * Hay ciertos tipos basicos corba que hay que diferenciar.
     */
    public static String getAnyInsertion(Element element, String varName,
                                         boolean noncopy)
        throws SemanticException
    { // varName is declared in function of inout parameter.

        boolean isArray = getDefinitionType(element).equals(OMG_array);
        StringBuffer buffer = new StringBuffer();
        String type = element.getAttribute(OMG_kind);
        if (element.getTagName().equals(OMG_scoped_name))
            if (XmlType2Cpp.getDefinitionType(element).equals(OMG_kind))
                type = XmlType2Cpp.getDeepKind(element);

        long bounds = 0;
        Element expr = (Element) element.getFirstChild();
        if (expr != null)
            bounds = XmlExpr2Cpp.getIntExpr(expr);

        if (type.equals(OMG_wchar)) {
            buffer.append("CORBA::Any::from_wchar(");
        } else if (type.equals(OMG_octet)) {
            buffer.append("CORBA::Any::from_octet(");
        } else if (type.equals(OMG_string)) {
            buffer.append("CORBA::Any::from_string(");
        } else if (type.equals(OMG_wstring)) {
            buffer.append("CORBA::Any::from_wstring(");
        } else if (type.equals(OMG_fixed)) {
            buffer.append("CORBA::Any::from_fixed(");
        } else if (type.equals(OMG_char)) {
            buffer.append("CORBA::Any::from_char(");
        } else if (type.equals(OMG_boolean)) {
            buffer.append("CORBA::Any::from_boolean(");
        } else if (isArray) {
            buffer.append(XmlType2Cpp.getType(element) + "_forany(("
                          + XmlType2Cpp.getType(element) + "_slice*) ");
        } else {
            buffer.append("(");
        }

        buffer.append(varName);
        if (type.equals(OMG_string) || type.equals(OMG_wstring)) {
            buffer.append(", " + bounds);
            if(noncopy) {
                buffer.append(", true");
            }
        	
        } if (type.equals(OMG_fixed)){
            buffer.append(", 0, 0");
        }
        buffer.append(")");
        return buffer.toString();
    }

    /**
     * Returns the Cpp type for an XML node.
     * 
     * @param doc
     *            The XML node where the type is.
     * @return The Cpp type.
     */
    public static String getReturnType(Element doc)
    {
        /* RELALO */
        String tag = doc.getTagName();
        if (tag.equals(OMG_type)) {
            String retu = doc.getAttribute(OMG_kind); 
            if (retu.equals(OMG_any))
                return basicMapping(retu) + "*";
            return basicMapping(retu);
        } else if (tag.equals(OMG_scoped_name)) {
            String definition = getDefinitionType(doc);
            if (definition.equals(OMG_sequence)
                || definition.equals(OMG_valuetype))
                return getType(doc) + "*";
            else if (definition.equals(OMG_array))
                return /* TypeManager.convert(doc.getAttribute(OMG_name)) */getType(doc)
                                                                           + "_slice*";
            else if (definition.equals(OMG_kind) || definition.equals(OMG_enum)
                     || !isVariableSizeType(doc)) { // incluye structs de
                                                    // tama???o fijo
                if (definition.equals(OMG_kind)
                    && XmlType2Cpp.getDeepKind(doc).equals(OMG_any)) // Any
                    return getType(doc)+ "*";
                    /* TypeManager.convert(doc.getAttribute(OMG_name)) */
                
                else if (isAString(doc) || isAWString(doc))
                    return basicMapping(getDeepKind(doc));
                
                else if (XmlType2Cpp.getDeepKind(doc).equals(OMG_Object))  
                    return getType(doc) + "_ptr";
                else
                    return getType(doc); /* TypeManager.convert(doc.getAttribute(OMG_name)) */
                    //basicMapping(getUnrolledName(doc));
                    // native
                    // entra
                    // aqui
            } else if (definition.equals(OMG_interface)) {
                return getType(doc) + "_ptr"; /* TypeManager.convert(doc.getAttribute(OMG_scoped_name)) */
                //return getValidID(getUnrolledName(doc)) + "_ptr";
            }
            return getType(doc) + "*"; /* TypeManager.convert(doc.getAttribute(OMG_scoped_name)) */
            // se juntan structs de tama???o variables, unions (siempre tam.
            // variable) y valuetypes
            
        } else
            return "unknownType";
    }

    /**
     * Finds the OMG_type of the element when it was defined inside the
     * hierarchy
     *  
     */

    public static String getDefinitionType(Element doc)
    {
        String tag = doc.getTagName();
        if (tag.equals(OMG_type))
            return OMG_kind;
        else if (tag.equals(OMG_scoped_name)) {
            String name = doc.getAttribute(OMG_name);
            String definition = TypedefManager
                .getInstance().getDefinitionType(name);
            if (definition != null)
                return definition;

            if (!name.equals("")) {
                Node parent = doc.getParentNode();
                Document documentRoot = parent.getOwnerDocument();
                Node him = findFirstOccurrence(name, 
                               documentRoot.getDocumentElement());
                //Node
                if (him != null) {
                    String definitionType = him.getNodeName();
                    if (definitionType.equals(OMG_simple_declarator)) { 
                        // es un (re)typedef...
                        if (((Element) parent).getTagName().equals(OMG_struct)) { 
                            // Para comprobar si es una secuencia definida
                            // dentro de una estructura.
                            definition = 
                                findInternalStructuredTypeDefinition((Element) him);
                            if (!definition.equals("UNKNOWN"))
                                return definition;
                        }
                        while (definitionType.equals(OMG_simple_declarator)) {
                            parent = him.getParentNode();
                            if (parent != null) {
                                if (((Element) parent).getTagName().equals(OMG_struct)) {
                                    // Para los casos en los que se esta
                                    // buscando una secuencia dentro de una
                                    // estructura
                                    // que generar sino un bucle infinito
                                    // sin desboradmiento de pila.
                                    // la definicion esta mas abajo que el
                                    // uso por aquello de que no se puede
                                    // insertar los hijos sino apendizar.
                                    // intentamos un recorrido en orden
                                    // inverso.
                                    NodeList nl = parent.getChildNodes();
                                    for (int i = nl.getLength(); i > 1; i--) {
                                        Element revChild = 
                                            (Element) nl.item(i - 1);
                                        if (revChild.getTagName().equals(OMG_typedef)) {
                                            Element typeChild = 
                                                (Element) revChild.getLastChild();
                                            if (typeChild.getAttribute(OMG_name).equals(name))
                                                return revChild.getFirstChild().getNodeName();
                                        }
                                    }
                                }
                                NodeList nl = parent.getChildNodes();
                                NamedNodeMap atl = nl.item(0).getAttributes();
                                if (atl.getNamedItem(OMG_name) != null) {
                                    String parentName = 
                                        atl.getNamedItem(OMG_name).getNodeValue();
                                    him = findFirstOccurrence(parentName,
                                            documentRoot.getDocumentElement());
                                    if (him != null)
                                        definitionType = him.getNodeName();
                                    else
                                        return "UNKNOWN";
                                } else if (atl.getNamedItem(OMG_kind) != null)
                                    return OMG_kind;
                                else if (((Element) nl).getTagName().equals(OMG_typedef)) {
                                    Element el = 
                                        (Element) ((Element) nl).getFirstChild();
                                    return el.getTagName();
                                } else
                                    return "UNKNOWN";
                            }
                        }
                        return definitionType;
                    } else
                        return definitionType; // es un array 
                }
            } else { // name equals ""
                if (doc.getTagName().equals(OMG_sequence)) {
                    return getDefinitionType((Element) doc.getFirstChild());
                } else if (doc.getAttribute(OMG_kind) != null
                    && doc.getAttribute(OMG_kind) != "")
                    return OMG_kind;
            }
            return "UNKNOWN";
            
        } else
            return tag; // OMG_enum, OMG_struct, OMG_sequence...
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
    public static Element findFirstOccurrence(String name, Element doc)
    {
        if (name.startsWith("::"))
            name = name.substring(2);
        String currentName = doc.getAttribute(OMG_name);

        if (currentName != null) // finalizacion: coincidencia de ultima
                                 // parte del scoped_name
            if (currentName.equals(name))
                return doc;

        // En la siguiente condicion la segunda parte se a???ade por la necesidad
        // (en C++) de incorporar al ???rbol sint???ctico un conjunto de nodos
        // correspondientes a los par???metros de entrada -package_to; estos nodos
        // ser???n siempre m???dulos y no tienen n???mero de l???nea (puesto que no
        // estar???n en el IDL original); caso de encontrarse uno, hay que
        // proseguir la b???squeda por debajo de ???l, no terminarla
        if (!currentName.equals("")
            && !(doc.getTagName().equals(OMG_module) 
                && doc.getAttribute("line").equals(""))) {
            if (!name.startsWith(currentName)) // Si estoy buscando algo que no
                                               // es esto y no es hijo de esto.
                return null; 
        }

        NodeList nl = doc.getChildNodes(); // Coincide el nombre actual
                                           // con el comienzo del scoped_name
                                           // buscado
        String childsName = "";
        if (nl == null)
            return null;
        else {
            // s??? tengo hijos; quito del nombre de busqueda el nombre actual.
            // pero s???lo si adem???s no estoy buscando por debajo de un nodo
            // introducido por el par???metro -package_to, en cuyo caso el nombre
            // del mismo no estar??? incluido en los atributos OMG_scoped_name del
            // ???rbol sint???ctico, puesto que ???stos se generan antes de incluir
            // los nodos nuevos en el ???rbol
            if (!currentName.equals("")
                && !(doc.getTagName().equals(OMG_module) 
                    && doc.getAttribute("line").equals("")))
                childsName = name.substring(currentName.length());
            else
                childsName = name; // No avanzamos.
        }

        Element temp;
        Node aux;
        for (int i = 0; i < nl.getLength(); i++) { // seguimos la
                                                   // b???squeda en cada uno de
                                                   // los hijos
            aux = nl.item(i);
            temp = findFirstOccurrence(childsName, (Element) aux);
            if (temp != null)
                return temp;
        }
        return null;

    }

    /**
     * The type code needs a variable defition depends on if it is defined
     * inside a module (extern) or inside a class (static). It is not deeply
     * chequed. (y tanto que no !!)
     * 
     * @param nameWithPackage
     *            the name of the type for the type code.
     */

    /*
     * public static String getTypeStorageForTypeCode(String nameWithPackage) { //
     * ver debajo correcion java.util.StringTokenizer st = new
     * java.util.StringTokenizer(nameWithPackage, "::"); if (st.countTokens() >
     * 2) // Static storage class is used only inside classes or structures or
     * unions. ie: // Module::[One of interface,struct,union]::Third or more
     * element. // So .... STATIC. return "static "; else return "extern ";
     *  }
     */

    /**
     * The type code needs a variable defition depends on if it is defined
     * inside a module ( extern ) or inside a class (static). Now it is better
     * checked
     * 
     * @param el
     *            the element to build a TypeCode
     */

    public static String getTypeStorageForTypeCode(Element el)
    {
        // Static storage class is used only inside classes or structures or
        // unions. ie:
        // Module::[One of interface,struct,union]::Third or more element.
        // So .... STATIC.
        // DAVV - mejor lo comprobamos DE VERDAD
        String parentType = ((Element) el.getParentNode()).getTagName();
        if (!parentType.equals(OMG_module)
            && !parentType.equals(OMG_specification))
            return "static ";
        else
            return "extern ";

    }

    /**
     * evaluation of the type looking for String;
     */

    public static boolean isAString(Element doc)
    {
        String definitionType;
        if (doc.getTagName().equals(OMG_type))
            definitionType = doc.getAttribute(OMG_kind);
        else {
            definitionType = getDefinitionType(doc);
            if (definitionType.equals(OMG_kind))
                definitionType = getDeepKind(doc);
        }
        return definitionType.equals(OMG_string);
    }

    /**
     * evaluation of the type looking for String (or WString);
     */

    public static boolean isAWString(Element doc)
    {
        String definitionType;
        if (doc.getTagName().equals(OMG_type))
            definitionType = doc.getAttribute(OMG_kind);
        else {
            definitionType = getDefinitionType(doc);
            if (definitionType.equals(OMG_kind))
                definitionType = getDeepKind(doc);
        }
        return definitionType.equals(OMG_wstring);
    }

    /**
     * return the attribute variable size of the definition of the element.
     * 
     * @param doc
     */

    public static boolean isVariableSizeType(Element doc)
    {
        String clase = doc.getAttribute(OMG_name);
        if (clase.equals("") || !doc.getTagName().equals(OMG_scoped_name)) {
            // no voy a encontrar el scope.
            String variableSize = doc.getAttribute(OMG_variable_size_type);
            if (variableSize != null)
                return variableSize.equals("true");
        }
        Element theNode = findFirstOccurrence(clase, doc
            .getOwnerDocument().getDocumentElement());
        if (theNode != null) {
            String variableSize = theNode.getAttribute(OMG_variable_size_type);
            if (variableSize != null)
                return variableSize.equals("true");
        }

        return false;
    }

    /**
     * return if the type insede doc is a sequence.
     * 
     * @param doc
     * @return
     */

    public static boolean isASequence(Element doc)
    {
        return doc.getTagName().equals(OMG_sequence)
               || XmlType2Cpp.getDefinitionType(doc).equals(OMG_sequence);
    }

    /**
     * return if the type insede doc is a basic, i.e. OMG_kind (or enum).
     * 
     * @param doc
     * @return
     */

    public static boolean isABasicType(Element doc)
    {
        String definitionType = XmlType2Cpp.getDefinitionType(doc);
        return definitionType.equals(OMG_kind)
               || definitionType.equals(OMG_enum) || //||
                                                     // !isVariableSizeType(doc);
               doc.getTagName().equals(OMG_kind)
               || doc.getTagName().equals(OMG_enum); // a???adido por DAVV
    }

    
    /**
     * return if the type insede doc is a basic data type :
     * 1.5 Mapping for Basic Data Types: 
     *  { short, long, long long, unsigned short, unsigned long, unsigned long long,
     *    float, double, long dobule, char, wchar, boolean, octet }
     * 
     * @param doc
     * @return
     */
    public static boolean isABasicDataType(Element doc)
    {
    	return (XmlType2Cpp.isABasicType(doc) && !XmlType2Cpp.isVariableSizeType(doc));
    }                 
    
    
    /**
     * return if the type insede doc is an Interface
     * 
     * @param doc
     * @return
     */

    public static boolean isAnInterface(Element doc)
    {
        String definitionType = XmlType2Cpp.getDefinitionType(doc);
        return definitionType.equals(OMG_interface)
               || doc.getTagName().equals(OMG_interface);
    }

    /**
     * return if the type insede doc is an Interface
     * 
     * @param doc
     * @return
     */

    public static boolean isAnValuetype(Element doc)
    {
        String definitionType = XmlType2Cpp.getDefinitionType(doc);
        return definitionType.equals(OMG_valuetype)
               || doc.getTagName().equals(OMG_valuetype);
    }
    /**
     * return if the type insede doc is an Array
     * 
     * @param doc
     * @return
     */
    public static boolean isAnArray(Element doc)
    {
        String definitionType = XmlType2Cpp.getDefinitionType(doc);
        return definitionType.equals(OMG_array)
               || doc.getTagName().equals(OMG_array);
    }

    /**
     * return a way to make a deep copy of the element.
     * 
     * @param doc
     * @param source
     * @param copied
     * @return
     */
    public static String getDeepCopy(Element doc, String source, String copied)
    {
        String name = doc.getAttribute(OMG_name);
        String type = "";
        String definedAs = getDefinitionType(doc);
        if (doc.getTagName().equals(OMG_scoped_name))
            type = XmlType2Cpp.getType(doc);
        else
            type = XmlType2Cpp.basicMapping(name);
        if (definedAs.equals(OMG_sequence)) {
            StringBuffer buffer = new StringBuffer();
            buffer.append(source + "=new " + name + "();\n\t");
            buffer.append("(*" + source + ")= (*" + copied + ")");
            return buffer.toString();
        }
        if (type.equals(""))
            type = doc.getAttribute(OMG_kind);
        if (type.equals(OMG_string) || type.equals(OMG_wstring)
            || type.equals("char*") || type.equals("wchar*")) {
            return source + " = " + "CORBA::string_dup(" + copied + ")"; 
            // MACP: for mapping Strings to BasicString not CORBA::String.
        }
        if (type.equals(OMG_wchar) || type.equals(OMG_octet)
            || type.equals(OMG_short) || type.equals(OMG_unsignedshort)
            || type.equals(OMG_long) || type.equals(OMG_unsignedlong)
            || type.equals(OMG_longlong) || type.equals(OMG_unsignedlonglong)
            || type.equals(OMG_fixed) || type.equals(OMG_any)
            || type.equals(OMG_boolean)) {
            return source + " = " + copied;
        } else if (type.equals(OMG_Object)) {
            return source + " = " + "CORBA::Object::_duplicate(" + copied + ")";
        } else if (type.equals(OMG_TypeCode)) {
            return source + " = " + "CORBA::TypeCode::_duplicate(" + copied
                   + ")";
        } else if (type.equals(OMG_ValueBase)) {
            return source + " = " + "NULL";
        } else if (type.equals(OMG_AbstractBaseCode)) {
            return source + " = " + "NULL";
        }

        Scope scope = Scope.getGlobalScopeInterface(type);
        if (scope != null) {
            Element definition = scope.getElement();
            if (definition != null) {
                String tag = definition.getTagName();
                if (tag.equals(OMG_interface))
                    return source + " = " + type + "::_duplicate(" + copied
                           + ")";
            }
        }
        //String tag = TypedefManager.getInstance().getUnrolledType(type);
        if (type != null)
            if (type.equals(OMG_string) || type.equals(OMG_wstring)
                || type.equals("string"))
                return source + " = " + copied;//"CORBA::string_dup("+copied+")";
        return source + " = " + copied;
    }

    /**
     * return a way to delete a reference, for structs.
     * 
     * @param doc
     * @param copied
     * @return
     */
    public static String getRelease(Element doc, String copied)
    {
        String name = doc.getAttribute(OMG_name);
        String type = "";
        String definedAs = getDefinitionType(doc);
        if (doc.getTagName().equals(OMG_scoped_name))
            type = XmlType2Cpp.getType(doc);
        else
            type = XmlType2Cpp.basicMapping(name);
        if (definedAs.equals(OMG_sequence))
            return "delete (" + copied + ")";
        if (type.equals(""))
            type = doc.getAttribute(OMG_kind);
        if (type.equals(OMG_string) || type.equals(OMG_wstring)
            || type.equals("char*") || type.equals("wchar*")) {
            return "CORBA::string_free(" + copied + ")"; 
            // MACP: for mapping Strings to BasicString not CORBA::String.
        }
        if (type.equals(OMG_wchar) || type.equals(OMG_octet)
            || type.equals(OMG_short) || type.equals(OMG_unsignedshort)
            || type.equals(OMG_long) || type.equals(OMG_unsignedlong)
            || type.equals(OMG_longlong) || type.equals(OMG_unsignedlonglong)
            || type.equals(OMG_fixed) || type.equals(OMG_any)
            || type.equals(OMG_boolean)) {
            return "// Nothing to release for " + copied;
        }
        Scope scope = Scope.getGlobalScopeInterface(type);
        if (scope != null) {
            Element definition = scope.getElement();
            if (definition != null) {
                String tag = definition.getTagName();
                if (tag.equals(OMG_interface))
                    return "CORBA::release(" + copied + ")";
            }
        }
        return "delete (" + copied + ")";

    }

    private static String findInternalStructuredTypeDefinition(Element him)
    {
        Element parent = (Element) him.getParentNode(); // el struct...
        NodeList nl = parent.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            if (el.getTagName().equals(OMG_typedef)) { 

                Element lastChild = (Element) el.getLastChild(); 
                // es la forma en que se introducen 'articialmente'
                // las sequence anonimas en XmlStruct2Cpp
                if (lastChild.getAttribute(OMG_name).startsWith(him.getAttribute(OMG_name)))
                    return OMG_sequence;
            }
        }
        return "UNKNOWN";
    }

    /**
     * Return the paramete type from which is able to insert it into an Any.
     * 
     * @param element
     * @param genPackage
     * @return
     */

    public static String getAnyInsertionParameter(Element element, String name,
                                                  String genPackage,
                                                  boolean nonCopy)
    {
        String tag = element.getTagName();
        //if(tag.equals(OMG_scoped_name)) tag=getDefinitionType(element); //
        String type = "";
        if (tag.equals(OMG_kind) || tag.equals(OMG_enum)) {
            // void operator <<=(Any&, T)
            // Argumentos normalmente pasados por valor
            // OMG_Kind, (Salvo OMG_string)
            // OMG_enum
            // OMG_interface(T_ptr)
            // Pointers to Valuetypes 
            if (!genPackage.equals(""))
                type = genPackage + "::";
            type += element.getAttribute(OMG_name);
            return type;

        } else if (tag.equals(OMG_interface)) {
            if (!genPackage.equals(""))
                type = genPackage + "::";
            type += element.getAttribute(OMG_name) + "_ptr";
            if (nonCopy)
                type += "*"; // version non-copying
            return type;

        } else if (tag.equals(OMG_struct) || tag.equals(OMG_union)
                   || tag.equals(OMG_any) || tag.equals(OMG_exception)) {
            // OMG_string OMG_wstring
            // void operator <<=(Any& const char*); || void operator <<=(Any&
            // const WChar*);
            if (isAString(element) || isAWString(element)) {
                type = "const ";
                if (!genPackage.equals(""))
                    type += genPackage + "::";
                type += element.getAttribute(OMG_name);
                return type;
            }
            // void operator <<=(Any&, const T&); || void operator <<=(Any&,
            // const T*);
            // Para tipos demasiado largos para pasarlos por Valor.
            // OMG_struct, OMG_union, OMG_sequence, OMG_Any, OMG_exception
            if (!nonCopy) // DAVV
                type = "const ";
            if (!genPackage.equals(""))
                type += genPackage + "::";
            type += element.getAttribute(OMG_name) + (!nonCopy ? "&" : "*"); 
            return type;

        } else if (tag.equals(OMG_sequence)) {
            if (!nonCopy) // DAVV
                type = "const ";
            if (!genPackage.equals(""))
                type += genPackage + "::";
            //type+=((Element)element.getNextSibling()).getAttribute(OMG_name)+(!nonCopy?"&":"*");
            // no sirve con declaraciones m???ltiples: typedef
            // sequnce<long> type1, type2;
            type += name + (!nonCopy ? "&" : "*");
            return type;

        } else if (tag.equals(OMG_valuetype)) {
            if (!genPackage.equals(""))
                type = genPackage + "::";
            type += element.getAttribute(OMG_name) + "*";
            if (nonCopy)
                type += "*";
            return type;
        }
        // Arrays needs Array for Any types-

        // Typedefs here for basic types.
        String kind = element.getAttribute(OMG_kind);
        if (kind != null) {
            type = "const " + element.getAttribute(OMG_name);
            return type;
        }
        return "UNKNOWN";
    }

    /**
     * Return the type of the parameter to be used as extraction form Any
     * parameter.
     * 
     * @param doc
     *            The element that contains the type
     * @param genPackage
     *            The Scoped package of the type.
     * @return
     */
    public static String getAnyExtractionParameter(Element doc, String name,
                                                   String genPackage)
    {
        String tag = doc.getTagName();
        String type = "";
        if (tag.equals(OMG_kind) || tag.equals(OMG_enum)) {
            // void operator >>=(Any&, T&)
            if (!genPackage.equals(""))
                type += genPackage + "::";
            type += doc.getAttribute(OMG_name) + "&";
            return type;
        } else if (tag.equals(OMG_interface)) {
            if (!genPackage.equals(""))
                type += genPackage + "::";
            type += doc.getAttribute(OMG_name) + "_ptr&";
            return type;
        } else if (tag.equals(OMG_struct) || tag.equals(OMG_union)
                   || tag.equals(OMG_any) || tag.equals(OMG_exception)) {
            // void operator >>=(Any&, const T*&);
            if (isAString(doc) || isAWString(doc)) {
                type = "const ";
                if (!genPackage.equals(""))
                    type += genPackage + "::";
                type += doc.getAttribute(OMG_name);
                return type;
            }
            //if(isVariableSize) DAVV - da igual si son de tama???o fijo o
            // variable , mapping dice SIEMPRE const T*&
            {
                type = "const ";
                if (!genPackage.equals(""))
                    type += genPackage + "::";
                type += doc.getAttribute(OMG_name);
                type += "*&";
            }
            /*
             * else { if(!genPackage.equals("")) type+=genPackage+"::";
             * type+=element.getAttribute(OMG_name); type+="&"; }
             */// DAVV
            return type;
            // OMG_string OMG_wstring
            // void operator <<=(Any& const char*); || void operator <<=(Any&
            // const WChar*);
            // Arrays needs Array for Any types-

        } else if (tag.equals(OMG_sequence)) {
            type = "const ";
            if (!genPackage.equals(""))
                type += genPackage + "::";
            //type+=((Element)element.getNextSibling()).getAttribute(OMG_name)+"*&";
            // DAVv - no sirve con typedefs multiples: typedef sequence<long>
            // type1, type2;
            type += name + "*&";
            return type;

        } else if (tag.equals(OMG_valuetype)) {
            if (!genPackage.equals(""))
                type += genPackage + "::";
            type += doc.getAttribute(OMG_name) + "*&";
            return type;
        }
        // Arrays needs Array for Any types-
        String kind = doc.getAttribute(OMG_kind);
        if (kind != null) {
            type = "const " + doc.getAttribute(OMG_name) + "&";
            return type;

        }
        return "UNKNOWN";
    }

    public static String getDeepKind(Element doc)
    {

        String firstTry = doc.getAttribute(OMG_kind);
        if (firstTry != null && !firstTry.equals(""))
            return firstTry;

        if (doc.getTagName().equals(OMG_scoped_name)) {
            String name = doc.getAttribute(OMG_name);
            String definition = TypedefManager.getInstance().getKind(name);
            if (definition != null)
                return definition;

            Node parent = doc.getParentNode();
            Document documentRoot = parent.getOwnerDocument();
            Node him = findFirstOccurrence(name, documentRoot
                .getDocumentElement());
            //Node
            if (him != null) {
                String definitionType = him.getNodeName();
                if (definitionType.equals(OMG_simple_declarator)) {
                    while (definitionType.equals(OMG_simple_declarator)) {
                        parent = him.getParentNode();
                        if (parent != null) {
                            NodeList nl = parent.getChildNodes();
                            NamedNodeMap atl = nl.item(0).getAttributes();
                            if (atl.getNamedItem(OMG_name) != null) { // retypedef
                                String parentName = 
                                    atl.getNamedItem(OMG_name).getNodeValue();
                                him = findFirstOccurrence(
                                          parentName,
                                          documentRoot.getDocumentElement());
                                if (him != null)
                                    definitionType = him.getNodeName();
                                else
                                    return "UNKNOWN";
                            } else if (atl.getNamedItem(OMG_kind) != null) {
                                return atl.getNamedItem(OMG_kind).getNodeValue();
                            } else
                                return "UNKNOWN";
                        }
                    }
                }
            }
        }
        return "UNKNOWN";
    }

    public static String getAccesorType(Element doc)
    {
        // Proporciona tipo para accesores en unions, valuetypes y valueboxes

        String tag = doc.getTagName();
        String typeStr = getType(doc);
        String kind = "";

        if (tag.equals(OMG_scoped_name)) {
            tag = getDefinitionType(doc);
            //typeStr = doc.getAttribute(OMG_name); 
            if (tag.equals(OMG_kind))
                kind = getDeepKind(doc);
        } else if (tag.equals(OMG_type)) {
            kind = doc.getAttribute(OMG_kind);
            tag = OMG_kind;
        } //else if (tag.equals(OMG_sequence))
        //  return null;

        if (tag.equals(OMG_kind)) {
            if (kind.equals(OMG_ValueBase))
                return typeStr + "*";
            /*
             * Para typedefs de cadenas, la forma 'const nombre', con 'typedef
             * char* nombre' no equivale a 'const char*' else if
             * (kind.equals(OMG_string) || kind.equals(OMG_wstring)) return
             * typeStr;
             */
            else if (kind.equals(OMG_string))
                return "const char*";
            else if (kind.equals(OMG_wstring))
                return "const CORBA::WChar*";
            else if (kind.equals(OMG_fixed) || kind.equals(OMG_any))
                return "const " + typeStr + "&";
            else
                return typeStr;
        } else if (tag.equals(OMG_enum))
            return typeStr;
        else if (tag.equals(OMG_struct) || tag.equals(OMG_union) // scoped o no
                 || tag.equals(OMG_sequence) || tag.equals(OMG_exception)) 
                        // solo puede ser scoped
            
            return "const " + typeStr + "&";
        else if (tag.equals(OMG_array)) // solo puede ser scoped
            return "const " + typeStr + "_slice*";
        else if (tag.equals(OMG_interface)) // solo puede ser scoped
            return typeStr + "_ptr";
        else if (tag.equals(OMG_valuetype)) // solo puede ser scoped
            return typeStr + "*";

        return null;
    }

    public static String getModifierType(Element doc)
    {
        // Proporciona tipo para modificadores en unions, valuetypes y
        // valueboxes

        String tag = doc.getTagName();
        String typeStr = getType(doc);
        String kind = "";

        if (tag.equals(OMG_scoped_name)) {
            tag = getDefinitionType(doc);
            //typeStr = doc.getAttribute(OMG_name); 
            if (tag.equals(OMG_kind))
                kind = getDeepKind(doc);
        } else if (tag.equals(OMG_type)) {
            kind = doc.getAttribute(OMG_kind);
            tag = OMG_kind;
        } //else if (tag.equals(OMG_sequence))
        //  return null;

        if (tag.equals(OMG_kind)) {
            if (kind.equals(OMG_ValueBase))
                return typeStr + "*";
            /*
             * Para typedefs de cadenas, la forma 'const nombre', con 'typedef
             * char* nombre' no equivale a 'const char*' else if
             * (kind.equals(OMG_string) || kind.equals(OMG_wstring)) return
             * typeStr;
             */
            else if (kind.equals(OMG_string))
                return "char*";
            else if (kind.equals(OMG_wstring))
                return "CORBA::WChar*";
            else if (kind.equals(OMG_fixed) || kind.equals(OMG_any))
                return "const " + typeStr + "&";
            else
                return typeStr;
        } else if (tag.equals(OMG_enum))
            return typeStr;
        else if (tag.equals(OMG_struct) || tag.equals(OMG_union) // scoped o no
                 || tag.equals(OMG_sequence) || tag.equals(OMG_exception)) 
            return "const " + typeStr + "&";
        else if (tag.equals(OMG_array)) // solo puede ser scoped
            return "const " + typeStr;
        else if (tag.equals(OMG_interface)) // solo puede ser scoped
            return typeStr + "_ptr";
        else if (tag.equals(OMG_valuetype)) // solo puede ser scoped
            return typeStr + "*";

        return null;

    }

    public static String getReferentType(Element doc)
    {
        // Proporciona tipo para referentes en unions, valuetypes y valueboxes

        String tag = doc.getTagName();
        String typeStr = getType(doc);
        String kind = "";

        if (tag.equals(OMG_scoped_name)) {
            tag = getDefinitionType(doc);
            //typeStr = doc.getAttribute(OMG_name); 
            if (tag.equals(OMG_kind))
                kind = getDeepKind(doc);
        } else if (tag.equals(OMG_type)) {
            kind = doc.getAttribute(OMG_kind);
            tag = OMG_kind;
        }

        if (tag.equals(OMG_kind)) {
            if (kind.equals(OMG_fixed) || kind.equals(OMG_any))
                return typeStr + "&";
        } else if (tag.equals(OMG_struct) || tag.equals(OMG_union) // scoped o no
                   || tag.equals(OMG_sequence) || tag.equals(OMG_exception)) 
                                                         // solo puede ser scoped            
            return typeStr + "&";
        else if (tag.equals(OMG_array))
            return typeStr + "_slice*";

        return null;

    }

    public static String getTypecodeName(Element doc)
    {
                
        String tag = doc.getTagName();
        if (tag.equals(OMG_type)) {
            
            return "CORBA::_tc_" + basicORBTypeMapping(doc);                       
            
        } else if (tag.equals(OMG_scoped_name)
                   || (doc.getAttribute(OMG_scoped_name) != null 
                       && doc.getAttribute(OMG_scoped_name) != "")) {
            //String helper = getUnrolledHelperName(doc); // DAVV - RELALO
            //return helper;
            String type = getTypeName(doc);
        
	        String helper = type;
	        StringTokenizer tok = new StringTokenizer(helper, "::");
	        String prefix = "", actual = "";
	        while (tok.hasMoreTokens()) {
	            actual = tok.nextToken();
	            if (tok.hasMoreTokens())
	                prefix += actual + "::";
	        }
	        return prefix + "_tc_" + actual;
	    
        } else {
            return "unknownType";
        }
	}
            
    
    public static String getHelperName(String typeName)
    {
        String helper = typeName;
        StringTokenizer tok = new StringTokenizer(helper, "::");
        String prefix = "", actual = "";
        while (tok.hasMoreTokens()) {
            actual = tok.nextToken();
            if (tok.hasMoreTokens())
                prefix += actual + "::";
        }
        return prefix + "_" + actual + "Helper";
    }

    public static String getHolderName(String typeName)
    {
        String holder = typeName;
        StringTokenizer tok = new StringTokenizer(holder, "::");
        String prefix = "", actual = "";
        while (tok.hasMoreTokens()) {
            actual = tok.nextToken();
            if (tok.hasMoreTokens())
                prefix += actual + "::";
        }
        return prefix + "_" + actual + "Holder";
    }

    public static int countMembers(Element doc)
    {
        // cuenta los miembros de un elemento estructurado o los
        // elementos definidos en un enum
        // si 'doc' es otra cosa devuelve 0
        String def = doc.getTagName();
        NodeList children = doc.getChildNodes();
        int numMembers = 0;
        if (def.equals(OMG_enum)) {
            numMembers = children.getLength();
        } else if (def.equals(OMG_struct) || def.equals(OMG_exception)) {
            for (int i = 0; i < children.getLength(); i++) {
                Element el = (Element) children.item(i);
                String tag = el.getTagName();
                if (tag.equals(OMG_simple_declarator) || tag.equals(OMG_array))
                    numMembers++;
            }
        } else if (def.equals(OMG_union)) {
            Union union = UnionManager.getInstance().get(doc);
            Vector switchBody = union.getSwitchBody();
            for (int i = 0; i < switchBody.size(); i++) {
                UnionCase union_case = (UnionCase) switchBody.elementAt(i);
                numMembers += union_case.m_case_labels.size();
            }
            if (union.getHasDefault())
                numMembers++;
        } else if (def.equals(OMG_valuetype)) {
            for (int i = 0; i < children.getLength(); i++) {
                Element el = (Element) children.item(i);
                String tag = el.getTagName();
                if (tag.equals(OMG_state_member)){
                		NodeList nodes2 = el.getChildNodes();                     
                    for (int j = 1; j < nodes2.getLength(); j++) {   // recorrido de tantas declaraciones como se
                        												// incluyan	
                    		numMembers++	;
                    }
                }
            }
        }
        return numMembers;
    }

    /**
     * It gets the internal type of a sequence.
     * 
     * @param doc
     * @param copied
     * @return
     */
    public static String getSequenceType(Element doc, String name)
    {
    	Element parent = (Element) doc.getOwnerDocument().getDocumentElement();
    	
    	NodeList nl = parent.getElementsByTagName(OMG_typedef);
    	Element seq = null;
    	String seqtype = "UNKNOWN";
    	boolean isSequence = false;
    	
    	for (int i = 0; i < nl.getLength(); i++){
    		NodeList nlt = nl.item(i).getChildNodes();
    		
    		for (int j = 0; j < nlt.getLength(); j++){
        		Element el = (Element)nlt.item(j);
        		if(isASequence(el)){
        			isSequence = true;
        			seq = el;
        		} else
        		if(isSequence && (el.getTagName().equals(OMG_simple_declarator))){
        			if(el.getAttribute(OMG_name).equals(name)){
        				Element type = (Element) seq.getFirstChild();
        				if (type.getNodeName().equals(OMG_scoped_name)){
        					seqtype = type.getAttribute(OMG_name);
        				} else {
        					seqtype = type.getAttribute(OMG_kind);
        				}
        				isSequence = false;
        			}
        		}
    		}
    	}
    	
    	return seqtype;
    }


	/**
	 * It gets the internal type of a sequence.
	 * 
	 * @param doc
	 * @param copied
	 * @return
	 */
	public static String getSequenceMaximum(Element doc, String name)
	{
		Element parent = (Element) doc.getOwnerDocument().getDocumentElement();// .getParentNode();
		
		NodeList nl = parent.getElementsByTagName(OMG_typedef);
		Element seq = null;
		String seqtype = "UNKNOWN";
		boolean isSequence = false;
		
		for (int i = 0; i < nl.getLength(); i++){
			NodeList nlt = nl.item(i).getChildNodes();
			
			for (int j = 0; j < nlt.getLength(); j++){
	    		Element el = (Element)nlt.item(j);
	    		if(isASequence(el)){
	    			isSequence = true;
	    			seq = el;
	    		} else
	    		if(isSequence && (el.getTagName().equals(OMG_simple_declarator))){
	    			if(el.getAttribute(OMG_name).equals(name)){
	    				NodeList children = seq.getElementsByTagName(OMG_expr);
	    				if (children.getLength()!=0){
	    				    Element type = (Element) children.item(0);
	    				    Element subtype = (Element) type.getFirstChild();
	    				    seqtype = subtype.getAttribute(OMG_value);
	    				}
	    				else
	    				    seqtype = "";
	    				isSequence = false;
	    			}
	    		}
			}
		}
		return seqtype;
	}

}
