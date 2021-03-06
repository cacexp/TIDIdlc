/*
* MORFEO Project
* http://www.morfeo-project.org
*
* Component: TIDIdlc
* Programming Language: Java
*
* File: $Source$
* Version: $Revision: 28 $
* Date: $Date: 2005-05-13 13:10:50 +0200 (Fri, 13 May 2005) $
* Last modified by: $Author: aarranz $
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

package es.tid.TIDIdlc.util;

import java.util.ArrayList;
import java.util.Hashtable;


/**
 * @author rafa
 */
public class IncludeFileManager {

	/**
     * The unique instance of the singleton class.
     */
	private static IncludeFileManager st_my_instance = null;

	/**
	 * The name of the file.
	 */
	private String m_file_name;

	/**
	 * The list of idls files.
	 */
	private Hashtable m_files_list;
	
	/**
	 * Method for getting the unique instance of this class. Singleton.
	 * @return The unique instance of this class.
	 */
	public static IncludeFileManager getInstance() {
		if (st_my_instance == null) {
			st_my_instance = new IncludeFileManager();
		}
		return st_my_instance;
	}
	
	public static void Shutdown() {
		st_my_instance = null;
	}

	/**
	 * Private constructor of the class.
	 * @param file_name The file's name.
	 */
	private IncludeFileManager() {
		this.m_files_list = new Hashtable();
	}
	
	/**
	 * Adds files to the include_list.
	 * @param code Code to add.
	 */
	public void addIdlFile(String name) {
		if(!thereIsFile(name)) {
			IdlFile idl_file = new IdlFile(name);
			this.m_files_list.put(name, idl_file);
		}
	}
	
	public void addIncludeToIdlFile(String idl_file, String include){
		if(!thereIsFile(idl_file)) {
			addIdlFile(idl_file);
		}
		if(!thereIsFile(include)) {
			addIdlFile(include);
		}
		IdlFile file = (IdlFile)this.m_files_list.get(idl_file);
		file.AddIncludeToFile(include);
		this.m_files_list.put(idl_file, file);
		
	}

	public void addModuleToIdlFile(String idl_file, String module){
		if(!thereIsFile(idl_file)) {
			addIdlFile(idl_file);
		}
		IdlFile file = (IdlFile)this.m_files_list.get(idl_file);
		file.AddModuleToFile(module);
		this.m_files_list.put(idl_file, file);
		
	}

	public ArrayList getIncludesFromIdlFile(String name){
		ArrayList lista = null;
		if(thereIsFile(name)) {
			IdlFile file = (IdlFile)this.m_files_list.get(name);
			lista = file.getIncludesFromIdlFile();
		}

		return lista;
		
	}

	public ArrayList getModulesFromIdlFile(String name){
		ArrayList lista = null;
		if(thereIsFile(name)) {
			IdlFile file = (IdlFile)this.m_files_list.get(name);
			lista = file.getModulesFromIdlFile();
		}

		return lista;
		
	}

	public boolean thereIsFile(String name){
		IdlFile file = (IdlFile)this.m_files_list.get(name);
		if(file==null)
			return false;
		else
			return true;
	}

	/**
	 * Gets files from the include_list.
	 * @param code Code to add.
	 */
//	public IncludeFileManager getIdlFile(String name) {
		
//		for(int i=0;i<this.m_include_list.size();i++) {
//			IncludeFileManager inc = (IncludeFileManager)this.m_include_list.get(i);
//			if(inc.m_file_name.equals(name)) {
//				return inc;
//			}
//		}
//		return null;
//	}	

}

