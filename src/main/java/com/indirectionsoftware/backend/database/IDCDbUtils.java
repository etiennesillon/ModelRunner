package com.indirectionsoftware.backend.database;

import java.io.File;
import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.indirectionsoftware.metamodel.IDCApplication;
import com.indirectionsoftware.metamodel.IDCAttribute;
import com.indirectionsoftware.metamodel.IDCDatabaseRef;
import com.indirectionsoftware.metamodel.IDCModelParser;
import com.indirectionsoftware.metamodel.IDCType;
import com.indirectionsoftware.runtime.IDCXMLImportParser;
import com.indirectionsoftware.utils.IDCUtils;

import io.grpc.LoadBalancerRegistry;

public class IDCDbUtils {
	
	private static final String METAMODEL = "/MetaModel.xml";

	private static final String SUPERADMINMODEL = "/SuperAdmin.xml";
	private static final String SUPERADMINRUNTIMEPROPERTIES = "/SuperAdminRuntime.properties";

	private static final String ADMINMODEL = "/ModelAdmin.xml";
	private static final String ADMINRUNTIMEPROPERTIES = "/AdminRuntime.properties";
	
	private static final String ONTOLOGY = "/IDCGlobalLexicon.txt";
	
	private static final String[] OPTIONS = {"Setup", "ListRegs", "ListUsers", "ReCreateUsers"};
	private static final int SETUP=0, LISTREGS=1, LISTUSERS=2, RECREATEUSERS=3;

	/*******************************************************************************************************/
	
	public static void main(String[] args) {
		
		if(args.length >= 6) {

			String dir = args[0];
			
			int option = getOption(args[1]);
			
			if(option == -1) {
				IDCUtils.error("IDCDbUtils.main(): incorrect parameters");
			} else {
				
				IDCSuperAdminDbManager superAdminDbManager = IDCSuperAdminDbManager.getIDCDbSuperAdminManager(dir + SUPERADMINRUNTIMEPROPERTIES, args[2], args[3]);
				
				IDCAdminDbManager adminDbManager = IDCAdminDbManager.getIDCDbAdminManager(dir + ADMINRUNTIMEPROPERTIES, false, args[2], args[3]);

				if(superAdminDbManager != null && adminDbManager != null) {
					
					switch(option) {
						
						case SETUP:
	
							superAdminDbManager.init(dir + METAMODEL, dir + SUPERADMINMODEL);
							adminDbManager.init(dir + METAMODEL, dir + ONTOLOGY, dir + ADMINMODEL, args[4], args[5]);
							break;
							
						case LISTREGS:
							System.out.print(superAdminDbManager.getSuperAdminApplication().getAllRegistrationsCSV());
							break;
							
						case LISTUSERS:
							System.out.print(adminDbManager.getAdminApplication().getAllUsersCSV());
							break;
						
						case RECREATEUSERS:
							if(args.length == 8) {
								adminDbManager.getAdminApplication().addUser(args[6], args[7]);
								System.out.print("Added user=" +  args[6] + "pwd=" + args[7]);
							} else {
								IDCUtils.error("IDCDbUtils.main(): incorrect parameters");
							}
							
							break;
						
					}
	
				} else {
					IDCUtils.error("IDCDbUtils.main(): superAdminDbManager == null || adminDbManager == null");
				}

			}

		} else {
			IDCUtils.error("IDCDbUtils.main(): incorrect parameters");
		}
		
	}

	/*******************************************************************************************************/
	
	private static int getOption(String option) {

		int ret = -1;
		
		for(int nOpt = 0; nOpt < OPTIONS.length; nOpt++) {
			if(option.equals(OPTIONS[nOpt])) {
				ret = nOpt;
				break;
			}
		}
		
		return ret;
		
	}

}