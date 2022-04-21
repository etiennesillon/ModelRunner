package com.indirectionsoftware.backend.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.indirectionsoftware.utils.IDCUtils;

public class IDCDbQueryResult {

	/************************************************************************************************/

	private ResultSet rs;
	private Statement stmt;

	/**************************************************************************************************/
	// Constructor ...
	/**************************************************************************************************/

    public IDCDbQueryResult(Statement stmt, ResultSet rs) {

    	this.stmt = stmt;
    	this.rs = rs;

    }

	/**************************************************************************************************/
	// Close Statement ...
	/**************************************************************************************************/

    public ResultSet getResultSet() {
    	return rs;
    }

	/**************************************************************************************************/
	// Close Statement ...
	/**************************************************************************************************/

    public void close() throws Error {

    	IDCUtils.traceStart("close()");

    	try {
    		rs.close();
    		stmt.close();
    	} catch(SQLException e) {
    		throw new Error("SQLException: " + e.getMessage());
    	}

    	IDCUtils.traceEnd("close()");

    }

}