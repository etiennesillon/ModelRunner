package com.indirectionsoftware.backend.database;

public class IDCDatabaseColumn {
	
	/**************************************************************************************************/
	// Fields ...
	/**************************************************************************************************/
	
	private String name, sqlTypeName, quote;
	
	private int sqlType, linkType;
	
	public static final int NA=-1, BYTE=0, INTEGER=1, LONG=2, DOUBLE=3, STRING=4;
	
	public static final int JOINTREF=0, NAMESPACE=1, BACKREF=2, BACKREF_EDITABLE=3, EXTENSION=4;
	
	private IDCDatabaseTable refTable;
	
	private IDCDatabaseColumn refCol;
	
	boolean isSqlType, isQuoteNeeded;
	
	private IDCDatabaseTable table;
	
	/**************************************************************************************************/
	// Constructors ...
	/**************************************************************************************************/
	
	public IDCDatabaseColumn(IDCDatabaseTable table, String colName, int sqlType, int len, int linkType, IDCDatabaseTable refTable, IDCDatabaseColumn refCol) {
		
		this.table = table;
		
		this.name = colName;
		this.sqlType = sqlType;
		this.linkType = linkType;
		this.refTable = refTable;
		this.refCol = refCol;
		sqlTypeName = "";
    	
    	switch(sqlType) {
    		
    		case INTEGER:
    			sqlTypeName = "INTEGER";
    			break;

    		case BYTE:
    			sqlTypeName = "TINYINT";
    			break;

    		case LONG: 
    			sqlTypeName = "BIGINT";
    			break;

    		case DOUBLE: 
    			sqlTypeName = "DOUBLE";
    			break;

    		case STRING:
    			sqlTypeName = "VARCHAR(" + len + ")";
    			break;

    	}
    	
		isSqlType = sqlType != NA;
		isQuoteNeeded = sqlType == STRING;
		quote = (isQuoteNeeded ? "'" : "");

	}

	/**************************************************************************************************/
	// Getters ...
	/**************************************************************************************************/
	
	public IDCDatabaseTable getTable() {
		return table;
	}

	public String getName() {
		return name;
	}

	public int getSqlType() {
		return sqlType;
	}

	public int getLinkType() {
		return linkType;
	}

	public IDCDatabaseTable getRefTable() {
		return refTable;
	}

	public IDCDatabaseColumn getRefCol() {
		return refCol;
	}

	public boolean isSQLType() {
		return isSqlType;
	}
	
    public String getSqlTypeName() { 
    	return sqlTypeName;
    }

    public boolean isQuoteNeeded() { 
		return isQuoteNeeded;
    }

    public String getQuote() { 
    	return quote;
    }

	/**************************************************************************************************/
	// Setters ...
	/**************************************************************************************************/
	
	public void setRefTable(IDCDatabaseTable refTable) {
		this.refTable = refTable;
	}

	public void setRefCol(IDCDatabaseColumn refCol) {
		this.refCol = refCol;
	}

}