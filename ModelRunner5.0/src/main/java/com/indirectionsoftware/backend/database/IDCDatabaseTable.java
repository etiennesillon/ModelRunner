package com.indirectionsoftware.backend.database;

import java.util.ArrayList;
import java.util.List;


public class IDCDatabaseTable {
	
	/**************************************************************************************************/
	// Fields ...
	/**************************************************************************************************/
	
	String name, idColName;
	
	List <IDCDatabaseColumn> cols;
	
	/**************************************************************************************************/
	// Constructors ...
	/**************************************************************************************************/
	
	public IDCDatabaseTable(String tableName, String idColName) {
		
		this.name = tableName;
		this.idColName = idColName;
		
		cols = new ArrayList<IDCDatabaseColumn>();
		
	}
	
	public void addColumn(IDCDatabaseColumn col) {
		cols.add(col);
	}

	/**************************************************************************************************/
	// Getters ...
	/**************************************************************************************************/
	
	public String getName() {
		return name;
	}

	public String getIdColName() {
		return idColName;
	}

	public int getColumnsCount() {
		return cols.size();
	}

	public List<IDCDatabaseColumn> getColumns() {
		return cols;
	}

	public IDCDatabaseColumn getColumn(int nCol) {
		return cols.get(nCol);
	}

}