/*
* MORFEO Project
* http://www.morfeo-project.org
*
* Component: TIDIdlc
* Programming Language: Java
*
* File: $Source$
* Version: $Revision: 45 $
* Date: $Date: 2005-06-20 19:45:00 +0200 (Mon, 20 Jun 2005) $
* Last modified by: $Author: pra $
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

package es.tid.TIDIdlc;

import es.tid.TIDIdlc.idl2xml.*;
import es.tid.TIDIdlc.xmlsemantics.*;
import es.tid.TIDIdlc.xml2cpp.*; // By MACP, for new Cpp generation.
import es.tid.TIDIdlc.util.FileManager;
import es.tid.TIDIdlc.util.IncludeFileManager;
import es.tid.TIDIdlc.util.Traces;

import org.w3c.dom.*;

import java.io.*;

/**
 * Main class of the Idl2Cpp compiler
 */
public class Idl2Cpp
{

    private static Idl2Xml parser;
    /**
     * The core of the compiler, this method throws the parser, the semantic
     * analyzer and finally the source generator for java.
     */

    private synchronized static void compile()
        throws Exception
    {
        try {
        	IncludeFileManager ifm = IncludeFileManager.getInstance();
        	ifm.addIdlFile(CompilerConf.getFileName());
        	
            Preprocessor.getInstance().preprocess(CompilerConf.getFile(),
                                                  CompilerConf.getSearchPath());
            if (CompilerConf.getJust_Expand()) {
                BufferedReader reader = new BufferedReader(Preprocessor
                    .getInstance().getReader());
                String line;
                while ((line = reader.readLine()) != null)
                    System.out.println(line);
                System.exit(0); 
            }

            if (parser == null) {
            	parser = new Idl2Xml(Preprocessor.getInstance().getReader());
            } else {
            	Idl2Xml.ReInit(Preprocessor.getInstance().getReader());
            }
            
            Traces.println("IDL file preprocessed successfully.", Traces.USER);
        }
        catch (FileNotFoundException e) {
            System.err.println("File not found: " + CompilerConf.getFile());
            throw e;
        }
        catch (Exception e) {
            System.err.println("Unexpected error during preprocess: ");
            throw e;
        }

        // Parsing
        Document dom = null;
        try {
            dom = parser.parse();
            Traces.println("IDL file parsed successfully.", Traces.USER);
        }
        catch (ParseException e) {
            System.err.println("Encountered errors during parse: ");
            throw e;
        }
        catch (TokenMgrError e) {
            System.err.println("Encountered errors during parse: ");
            throw e;
        }
        catch (Exception e) {
            System.err.println("Unexpected error during parse: ");
            throw e;
        }

        // Semantics & Cpp Generation
        try {

        	// Gets the FileManager and writes all of it.
        	FileManager fm = FileManager.getInstance();
        	fm.setOutputDir(CompilerConf.getOutputPath());
        	if(!CompilerConf.getOutputHeaderDir().equals("")) {
        		fm.setHeaderDir(CompilerConf.getOutputHeaderDir());
        	} else {
        		fm.setHeaderDir(CompilerConf.getOutputPath());
        	}
        	fm.setFileName(CompilerConf.getFileName().substring(0,CompilerConf.getFileName().lastIndexOf('.')));
        	fm.setOriginalFileName(CompilerConf.getFileName().substring(0,CompilerConf.getFileName().lastIndexOf('.')));
        	fm.setHeaderExtension(CompilerConf.getHeaderExtension());
        	fm.setSourceExtension(CompilerConf.getSourceExtension());
        	
            Xml2Cpp xml2cpp = new Xml2Cpp(dom);
            xml2cpp.generateCpp(CompilerConf.getOutputPath(), 
                                CompilerConf.getPackageUsed(), 
                                CompilerConf.getModule_Packaged(),
                                CompilerConf.getFilePackaged(), 
                                CompilerConf.getPackageToTable(), 
                                CompilerConf.getPackageToError());

        	if(CompilerConf.st_expanded)
        		fm.write(FileManager.WRITE_EXPANDED);
        	else
        		fm.write(FileManager.WRITE_CONDENSED);

            Traces.println("Semantic process finished successfully.",
                           Traces.USER);

        }
        catch (SemanticException e) {
            System.err.println("Encountered semantic errors: ");
            throw e;
        }
        catch (Exception e) {
            System.err.println("Unexpected error: ");
            throw e;
        }
    }

    /**
     * @param args
     *            Idl file to be compiled
     */
    public static void main(String args[])
    {
        Traces.setLevel(Traces.NONE);
        try {
            CompilerConf.setCompilerType("Cpp");
            Arguments.parse(args);
            compile();
        }
        catch (Exception e) {
            if (Traces.getLevel() >= Traces.DEBUG) {
                e.printStackTrace();
            } else { 
                System.err.println(e.toString());
            }
            System.exit(1);
        }
    }
}// end of Class.
