package SMClasses;

import SMDataDefinition.*;
import ServletUtilities.clsDatabaseFunctions;

public class MySQLs {
	private static String SQL = "";

	//User SQLS:
	public static String Add_New_User_SQL(String sUserName){
		SQL = "INSERT into " + SMTableusers.TableName +
		" (" + SMTableusers.sUserName + ")" +
		" VALUES ('" + sUserName + "')";
		//System.out.println ("Add_New_User_SQL = " + SQL);
		return SQL;
	}
	
	public static String Update_User_SQL(
		  String sUserName,
		  String sDefaultSalespersonCode,
		  String sIdentifierInitials,
		  String sUserFirstName,
		  String sUserLastName,
		  String sEmail,
		  String sMechanicInitials,
		  int iRow,
		  int iCol,
		  int iActive
		  ){
		
		SQL = "UPDATE " + SMTableusers.TableName
		
		+ " SET " 
		
		+ SMTableusers.sDefaultSalespersonCode + " = '" + sDefaultSalespersonCode + "', "
		+ SMTableusers.sIdentifierInitials + " = '" + sIdentifierInitials + "', "
		+ SMTableusers.sUserFirstName + " = '" + clsDatabaseFunctions.FormatSQLStatement(sUserFirstName) + "', "
		+ SMTableusers.sUserLastName + " = '" + clsDatabaseFunctions.FormatSQLStatement(sUserLastName) + "', "
		+ SMTableusers.semail + " = '" + sEmail + "', "
		+ SMTableusers.smechanicinitials + " = '" + sMechanicInitials + "', "
		+ SMTableusers.iactive + " = " + iActive + ", "
		+ SMTableusers.susercolorcoderow + " = " + iRow + ", "
		+ SMTableusers.susercolorcodecol + " = " + iCol
		+ " WHERE " + SMTableusers.sUserName + " = '" + sUserName + "'";
		//System.out.println ("[1390507003] Update_User_SQL = " + SQL);
		return SQL;
	}
	
	public static String Get_User_Info_By_Password_SQL(String sUserName, String sPassword){
		SQL = "SELECT *" + 
			  " FROM" +
			  	" " + SMTableusers.TableName +
			  " WHERE (" +
			  		"(" + SMTableusers.sUserName + " = '" + sUserName + "')" +
			  " AND" +
			  	" (" + SMTableusers.sHashedPw + " = SHA('" + clsDatabaseFunctions.FormatSQLStatement(sPassword) + "'))" +
				")";
		
		//System.out.println ("Get_User_Info_By_Password_SQL = " + SQL);
		return SQL;
	}
	
	public static String Get_User_List_SQL(boolean bIncludeInactiveUsers){
		SQL = "SELECT * FROM " + SMTableusers.TableName;
		if (!bIncludeInactiveUsers){
			SQL += " WHERE " + SMTableusers.iactive + "=1";
		}
		SQL += " ORDER BY " + SMTableusers.sUserFirstName;
		//System.out.println ("Get_User_List_SQL = " + SQL);
		return SQL;
	}	
	
	//Get_User_By_Username
	public static String Get_User_By_Username(String sUserName){
		SQL = "SELECT" +
				" " + SMTableusers.lid + "," +
				" " + SMTableusers.iactive + "," +
				" " + SMTableusers.sDefaultSalespersonCode + "," +
				" " + SMTableusers.semail + "," +
				" " + SMTableusers.sHashedPw + "," +
				" " + SMTableusers.sIdentifierInitials + "," +
				" " + SMTableusers.smechanicinitials + "," +
				" " + SMTableusers.sUserFirstName + "," +
				" " + SMTableusers.sUserLastName + "," +
				" " + SMTableusers.sUserName +					
			  " FROM " +
			  SMTableusers.TableName +
			  " WHERE (" +
			  		"(" + SMTableusers.sUserName + " = '" + sUserName + "')" +
				")";
		
		//System.out.println ("[1390507002] Get_User_By_Username = " + SQL);
		return SQL;
	}
	
	public static String Get_User_By_UserID(String sUserID){
		SQL = "SELECT" +
				" " + SMTableusers.iactive + "," +
				" " + SMTableusers.sDefaultSalespersonCode + "," +
				" " + SMTableusers.semail + "," +
				" " + SMTableusers.sHashedPw + "," +
				" " + SMTableusers.sIdentifierInitials + "," +
				" " + SMTableusers.smechanicinitials + "," +
				" " + SMTableusers.sUserFirstName + "," +
				" " + SMTableusers.sUserLastName + "," +
				" " + SMTableusers.sUserName +					
			  " FROM " +
			  SMTableusers.TableName +
			  " WHERE (" +
			  		"(" + SMTableusers.lid + " = " + sUserID + ")" +
				")";
		
		//System.out.println ("[1390507002] Get_User_By_Username = " + SQL);
		return SQL;
	}
	
	public static String Get_Security_Group_List_SQL(){
		SQL = "SELECT * FROM " + SMTablesecuritygroups.TableName + " ORDER BY " + SMTablesecuritygroups.sSecurityGroupName;
		return SQL;
	}
	
	public static String Get_Security_Group_SQL(String sGroupName){
		SQL = "SELECT * FROM " + SMTablesecuritygroups.TableName +
		" WHERE (" +
			SMTablesecuritygroups.sSecurityGroupName + " = '" + sGroupName + "'" +
		")";
		return SQL;
	}
	
	public static String Add_New_Group_SQL(String sGroupName){
		SQL = "INSERT into " + SMTablesecuritygroups.TableName +
		" (" + SMTablesecuritygroups.sSecurityGroupName + ")" +
		" VALUES ('" + sGroupName + "')";
		return SQL;
	}

	public static String Delete_Group_SQL(String sGroupName){
		SQL = "DELETE FROM " + SMTablesecuritygroups.TableName +
		" WHERE (" +
			SMTablesecuritygroups.sSecurityGroupName + " = '" + sGroupName + "'" +
		")";
		return SQL;
	}
	
	public static String Delete_Group_Functions_SQL(String sGroupName){
		SQL = "DELETE FROM " + SMTablesecuritygroupfunctions.TableName +
		" WHERE (" +
			SMTablesecuritygroupfunctions.sGroupName + " = '" + sGroupName + "'" +
		")";
		return SQL;
	}
	
	public static String Delete_Group_Users_SQL(String sGroupName){
		SQL = "DELETE FROM " + SMTablesecurityusergroups.TableName +
		" WHERE (" +
			SMTablesecurityusergroups.sSecurityGroupName + " = '" + sGroupName + "'" +
		")";
		return SQL;
	}
	
	public static String Delete_User_From_Security_User_Groups(String sUserID){
		SQL = "DELETE FROM " + SMTablesecurityusergroups.TableName +
		" WHERE (" 
			+ SMTablesecurityusergroups.luserid + " = " + sUserID + ""
			+ ")";
		return SQL;
	}
	
	public static String Delete_User_From_Appointment_User_Groups(String sUserID){
		SQL = "DELETE FROM " + SMTableappointmentusergroups.TableName +
		" WHERE (" 
			+ SMTableappointmentusergroups.luserid + " = " + sUserID + ""
			+ ")";
		return SQL;
	}
	
	public static String Delete_User_From_Alarm_Sequences(String sUserID){
		SQL = "DELETE FROM " + SMTablessalarmsequenceusers.TableName +
		" WHERE (" 
			+ SMTablessalarmsequenceusers.luserid + " = " + sUserID + ""
			+ ")";
		return SQL;
	}
	
	public static String Delete_User_From_Device_Users(String sUserID){
		SQL = "DELETE FROM " + SMTablessdeviceusers.TableName +
		" WHERE (" 
			+ SMTablessdeviceusers.luserid + " = " + sUserID + ""
			+ ")";
		return SQL;
	}
	
	public static String Delete_Users_Custom_Links(String sUserID){
		SQL = "DELETE FROM " + SMTableuserscustomlinks.TableName +
		" WHERE (" 
			+ SMTableuserscustomlinks.luserid + " = " + sUserID + ""
			+ ")";
		return SQL;
	}
	
	public static String Insert_Security_User_Groups_SQL(
			String sGroupName, 
			String sUserID
			){
		
		SQL = "INSERT INTO " + SMTablesecurityusergroups.TableName
			+ "("
			+ SMTablesecurityusergroups.sSecurityGroupName + ""
			+ ", " + SMTablesecurityusergroups.luserid
			+ ") VALUES ("
			+ "'" + sGroupName + "'"
			+ ", " + sUserID + ""
			+")"
			; 
		return SQL;		
	}

	public static String Insert_Appointment_User_Groups_SQL(
			String sGroupName, 
			String sUserID
			){
		
		SQL = "INSERT INTO " + SMTableappointmentusergroups.TableName
			+ "("
			+ SMTableappointmentusergroups.sappointmentgroupname + ""
			+ ", " + SMTableappointmentusergroups.luserid
			+ ") VALUES ("
			+ "'" + sGroupName 
			+ "', " + sUserID + ""
			+")"
			; 
		return SQL;		
	}
	
	public static String Insert_Alarm_Sequence_User_SQL(
			String sAlarmSequenceID, 
			String sUserID
			){
		
		SQL = "INSERT INTO " + SMTablessalarmsequenceusers.TableName
			+ "("
			+ SMTablessalarmsequenceusers.lalarmsequenceid + ""
			+ ", " + SMTablessalarmsequenceusers.luserid
			+ ") VALUES ("
			+ "'" + sAlarmSequenceID 
			+ "', " + sUserID + ""
			+")"
			; 
		return SQL;		
	}
	
	public static String Insert_Device_User_SQL(
			String sDeviceID, 
			String sUserID
			){
		
		SQL = "INSERT INTO " + SMTablessdeviceusers.TableName
			+ "("
			+ SMTablessdeviceusers.ldeviceid + ""
			+ ", " + SMTablessdeviceusers.luserid
			+ ") VALUES ("
			+ "'" + sDeviceID 
			+ "', " + sUserID + ""
			+")"
			; 
		return SQL;		
	}
	
	public static String Insert_Users_Custom_Links_SQL(
			String sCustomLinkID, 
			String sUserID
			){
		
		SQL = "INSERT INTO " + SMTableuserscustomlinks.TableName
			+ "("
			+ SMTableuserscustomlinks.icustomlinkid 
			+ ", " + SMTableuserscustomlinks.luserid
			+ ") VALUES ("
			+ sCustomLinkID 
			+ ", " + sUserID 
			+")"
			; 
		return SQL;		
	}
	public static String Insert_Security_Group_Function_SQL(
			String sGroupName, 
			String sFunctionID){
		
		SQL = "INSERT INTO " + SMTablesecuritygroupfunctions.TableName
			+ "("
			+ SMTablesecuritygroupfunctions.sGroupName + ""
			+ ", " + SMTablesecuritygroupfunctions.ifunctionid
			+ ") VALUES ("
			+ "'" + sGroupName 
			+ "', '" + sFunctionID + "'"
			+")"
			; 
		return SQL;		
	}
	
	public static String Insert_Security_Group_User_SQL(
			String sGroupName, 
			String sUserID){
		
		SQL = "INSERT INTO " + SMTablesecurityusergroups.TableName +
			"(" + SMTablesecurityusergroups.sSecurityGroupName + ", " + SMTablesecurityusergroups.luserid + ") VALUES " +
			"('" + sGroupName + "', " + sUserID + ")"; 
		return SQL;		
	}
	
	public static String Get_Security_List_SQL(String sListBy){
		
		String SQL = "";
		if (sListBy.compareTo("User") == 0){
			SQL = "SELECT * FROM" + 
					" " + SMTablesecurityusergroups.TableName + "," +
					" " + SMTablesecuritygroupfunctions.TableName + "," +
					" " + SMTableusers.TableName + 
			" WHERE " +
				" " + SMTablesecurityusergroups.TableName + "." + SMTablesecurityusergroups.sSecurityGroupName + " =" +
				" " + SMTablesecuritygroupfunctions.TableName + "." + SMTablesecuritygroupfunctions.sGroupName +
				" AND" + 
				" " + SMTablesecurityusergroups.TableName + "." + SMTablesecurityusergroups.luserid + " =" +
				" " + SMTableusers.TableName + "." + SMTableusers.lid +
				" AND" + 
				" " + SMTableusers.TableName + "." + SMTableusers.iactive + " = 1" +
			" ORDER BY" + 
				" " + SMTableusers.TableName + "." + SMTableusers.sUserFirstName + "," +
				" " + SMTablesecuritygroupfunctions.TableName + "." + SMTablesecuritygroupfunctions.sFunction + "," +
				" " + SMTablesecuritygroupfunctions.TableName + "." + SMTablesecuritygroupfunctions.sGroupName;
		}

		if (sListBy.compareTo("Group") == 0){
			SQL = "SELECT * FROM" + 
					" " + SMTablesecurityusergroups.TableName + "," +
					" " + SMTablesecuritygroupfunctions.TableName + "," +
					" " + SMTableusers.TableName + 
			" WHERE " +
				" " + SMTablesecurityusergroups.TableName + "." + SMTablesecurityusergroups.sSecurityGroupName + " =" +
				" " + SMTablesecuritygroupfunctions.TableName + "." + SMTablesecuritygroupfunctions.sGroupName +
				" AND" + 
				" " + SMTablesecurityusergroups.TableName + "." + SMTablesecurityusergroups.luserid + " =" +
				" " + SMTableusers.TableName + "." + SMTableusers.lid +
				" AND" + 
				" " + SMTableusers.TableName + "." + SMTableusers.iactive + " = 1" +
			" ORDER BY" + 
				" " + SMTablesecuritygroupfunctions.TableName + "." + SMTablesecuritygroupfunctions.sGroupName + "," +
				" " + SMTablesecuritygroupfunctions.TableName + "." + SMTablesecuritygroupfunctions.sFunction + "," +
				" " + SMTableusers.TableName + "." + SMTableusers.sUserFirstName;
		}
		if (sListBy.compareTo("Function") == 0){
			SQL = "SELECT * FROM" + 
					" " + SMTablesecurityusergroups.TableName + "," +
					" " + SMTablesecuritygroupfunctions.TableName + "," +
					" " + SMTableusers.TableName + 
			" WHERE " +
				" " + SMTablesecurityusergroups.TableName + "." + SMTablesecurityusergroups.sSecurityGroupName + " =" +
				" " + SMTablesecuritygroupfunctions.TableName + "." + SMTablesecuritygroupfunctions.sGroupName +
				" AND" + 
				" " + SMTablesecurityusergroups.TableName + "." + SMTablesecurityusergroups.luserid + " =" +
				" " + SMTableusers.TableName + "." + SMTableusers.lid +
				" AND" + 
				" " + SMTableusers.TableName + "." + SMTableusers.iactive + " = 1" +
			" ORDER BY" + 
				" " + SMTablesecuritygroupfunctions.TableName + "." + SMTablesecuritygroupfunctions.sFunction + "," +
				" " + SMTablesecuritygroupfunctions.TableName + "." + SMTablesecuritygroupfunctions.sGroupName + "," +
				" " + SMTableusers.TableName + "." + SMTableusers.sUserFirstName;
		}
		if (sListBy.compareTo("GroupList") == 0){
			SQL = "SELECT * FROM" +
					" " + SMTablesecuritygroups.TableName + 
				  " ORDER BY" +
				  	" " + SMTablesecuritygroups.sSecurityGroupName;
		}
		if (sListBy.compareTo("FunctionList") == 0){
			SQL = "SELECT * FROM" +
					" " + SMTablesecurityfunctions.TableName + 
				  " ORDER BY" +
				  	" " + SMTablesecurityfunctions.sFunctionName;
		}
		return SQL;
	}

	public static String Get_Security_Group_Functions_SQL(String sGroup){
		SQL = "SELECT * FROM " + SMTablesecuritygroupfunctions.TableName +
			" WHERE (" +
			SMTablesecuritygroupfunctions.sGroupName + "= '" + sGroup + "'" +
			") ORDER BY " + SMTablesecuritygroupfunctions.sFunction;
		return SQL;
	}
	
	public static String Get_Security_Group_Users_SQL(String sGroup){
		SQL = "SELECT * FROM " + SMTablesecurityusergroups.TableName +
			" LEFT JOIN " + SMTableusers.TableName 
			+ " ON " + SMTablesecurityusergroups.TableName + "." + SMTablesecurityusergroups.luserid
			+ " = " + SMTableusers.TableName + "." + SMTableusers.lid
			+ " WHERE (" +
			SMTablesecurityusergroups.TableName + "." + SMTablesecurityusergroups.sSecurityGroupName + " = '" + sGroup + "'" +
			") ORDER BY " + SMTableusers.TableName + "." + SMTableusers.sUserFirstName
			+ ", " + SMTableusers.TableName + "." + SMTableusers.sUserLastName
			;
		return SQL;
	}
	
	public static String Get_Appointment_Group_Users_SQL(String sGroup){
		SQL = "SELECT * FROM " + SMTableappointmentusergroups.TableName 
			+ " LEFT JOIN " + SMTableusers.TableName
			+ " ON " + SMTableappointmentusergroups.TableName + "." + SMTableappointmentusergroups.luserid 
			+ " = " + SMTableusers.TableName + "." + SMTableusers.lid 
			+ " WHERE (" +
			SMTableappointmentusergroups.TableName + "." + SMTableappointmentusergroups.sappointmentgroupname + " = '" + sGroup + "'" +
			") ORDER BY " + SMTableusers.TableName + "." + SMTableusers.sUserFirstName;
		return SQL;
	}

	public static String Get_User_Password_Check_SQL(
			String sUserName, 
			String sCurrentPassword
			){
		SQL = "SELECT " + SMTableusers.sHashedPw + ", SHA('" + sCurrentPassword + "') AS HashedCurrentPassword" + 
			  " FROM " +
			  	SMTableusers.TableName +
			  " WHERE (" +
			  		"(" + SMTableusers.sUserName + " = '" + sUserName + "')" +
			  ")";
		
		//System.out.println ("Get_User_Password_Check_SQL = " + SQL);
		return SQL;
	}
	
	public static String Update_User_Password_SQL(
			String sUserName, 
			String sNewPassword
			){
		SQL = "UPDATE " + SMTableusers.TableName +  
			  " SET " + SMTableusers.sHashedPw + " = SHA('" + sNewPassword + "')" +
			  " WHERE (" +
			  		"(" + SMTableusers.sUserName + " = '" + sUserName + "')" +
			  ")";
		
		//System.out.println ("Update_User_Password_SQL = " + SQL);
		return SQL;
	}
	
	//Location table SQLS:
	public static String Get_Locations_SQL(){
		SQL = "SELECT * FROM " +
				SMTablelocations.TableName +
		" ORDER BY " + SMTablelocations.sLocation;
		
		//System.out.println ("Get_Locations_SQL = " + SQL);
		return SQL;
	}
	
	public static String Get_Location_By_Code(String sCode){
		SQL = "SELECT * FROM " + SMTablelocations.TableName + 
		" WHERE " + SMTablelocations.sLocation + " = '" + sCode + "'";
		//System.out.println ("Get_Location_By_Code = " + SQL);
		return SQL;
	}
	
	public static String Delete_Location_SQL(String sCode){
		SQL = "DELETE FROM " +
				SMTablelocations.TableName +
			  " WHERE (" +
			  		"(" + SMTablelocations.sLocation + " = '" + sCode + "')" +
				")";
		
		//System.out.println ("Delete_Salesperson_SQL = " + SQL);
		return SQL;
	}

	public static String Add_New_Location_SQL(String sCode){
		SQL = "INSERT into " + SMTablelocations.TableName +
		" (" 
			+ SMTablelocations.sLocation
			+ ", " + SMTablelocations.sAdditionalNotes
		+ ")" + " VALUES ("
			+ "'" + sCode + "'" 
			+ ", ''"
		+ ")";
		//System.out.println ("Add_New_Location_SQL = " + SQL);
		return SQL;
	}
		
	//Service types table SQLs:
	public static String Get_Servicetypes_SQL(){
		SQL = "SELECT * FROM " +
		SMTableservicetypes.TableName +
		" ORDER BY " + SMTableservicetypes.sCode;
		
		//System.out.println ("Get_Servicetypes_SQL = " + SQL);
		return SQL;
	}
	
	//Salesperson SQLs:
	public static String Get_Salesperson_List_SQL(){
		SQL = "SELECT * FROM " + SMTablesalesperson.TableName + " ORDER BY " + SMTablesalesperson.sSalespersonCode;
		//System.out.println ("Get_Salesperson_List_SQL = " + SQL);
		return SQL;
	}
	
	public static String Get_Salesperson_By_Salescode(String sCode){
		SQL = "SELECT * FROM " + SMTablesalesperson.TableName + 
		" WHERE " + SMTablesalesperson.sSalespersonCode + " = '" + sCode + "'";
		//System.out.println ("Get_Salesperson_By_Salescode = " + SQL);
		return SQL;
	}
	
	public static String Add_New_Salesperson_SQL(String sCode){
		SQL = "INSERT into " + SMTablesalesperson.TableName + " (" 
			+ SMTablesalesperson.sSalespersonCode
			+ ", " + SMTablesalesperson.mSignature
			+ ") VALUES ("
			+ "'" + sCode + "'"
			+ ", ''"
			+ ")";
		return SQL;
	}
	
	public static String Update_Salesperson_SQL(
			String sSalespersonCode,
			String iShowInSalesReport,
			String sSalespersonFirstName,
			String sSalespersonLastName,
			String sSalespersonType,
			String sSalespersonUserID, 
			String sSalespersonTitle,
			String sDirectDial,
			String sSalespersonEmail
			  ){
		
			if(sSalespersonUserID.compareToIgnoreCase("") == 0) {
				sSalespersonUserID = "0";
			}
			
			SQL = "UPDATE " + SMTablesalesperson.TableName
			
			+ " SET " 
			
			+ SMTablesalesperson.iShowInSalesReport + " = " + iShowInSalesReport + ","
			+ SMTablesalesperson.sSalespersonFirstName + " = '" + sSalespersonFirstName + "',"
			+ SMTablesalesperson.sSalespersonLastName + " = '" + sSalespersonLastName + "',"
			+ SMTablesalesperson.sSalespersonType + " = '" + sSalespersonType + "',"
			+ SMTablesalesperson.lSalespersonUserID + " = " + sSalespersonUserID + ","
			+ SMTablesalesperson.sSalespersonTitle + " = '" + sSalespersonTitle + "',"
			+ SMTablesalesperson.sDirectDial + " = '" + sDirectDial + "',"
			+ SMTablesalesperson.sSalespersonEmail + " = '" + sSalespersonEmail + "'"
			
			+ " WHERE " + SMTablesalesperson.sSalespersonCode + " = '" + sSalespersonCode + "'";

			//System.out.println ("Update_Salesperson_SQL = " + SQL);
			return SQL;
		}

//	Convenience Phrase SQLs:
	public static String Get_ConveniencePhrase_List_SQL(){
		SQL = "SELECT * FROM " + SMTableconveniencephrases.TableName + " ORDER BY " + SMTableconveniencephrases.lPhraseID;
		//System.out.println ("Get_ConveniencePhrase_List_SQL = " + SQL);
		return SQL;
	}
	
	public static String Get_ConveniencePhrase_By_ID(String sID){
		SQL = "SELECT * FROM " + SMTableconveniencephrases.TableName + 
		" WHERE " + SMTableconveniencephrases.lPhraseID + " = " + sID;
		//System.out.println ("Get_ConveniencePhrase_By_ID = " + SQL);
		return SQL;
	}
	
	public static String Delete_ConveniencePhrase_SQL(String sID){
		SQL = "DELETE FROM " +
				SMTableconveniencephrases.TableName +
			  " WHERE (" +
			  		"(" + SMTableconveniencephrases.lPhraseID + " = " + sID + ")" +
				")";
		
		//System.out.println ("Delete_ConveniencePhrase_SQL = " + SQL);
		return SQL;
	}
	
	public static String Add_New_ConveniencePhrase_SQL(String sID){
		SQL = "INSERT into " + SMTableconveniencephrases.TableName +
		" (" + SMTableconveniencephrases.lPhraseID + ")" +
		" VALUES (" + sID + ")";
		//System.out.println ("Add_New_ConveniencePhrase_SQL = " + SQL);
		return SQL;
	}

	public static String Update_ConveniencePhrase_SQL(
			String iID,
			String mPhraseText
			  ){
			
			SQL = "UPDATE " + SMTableconveniencephrases.TableName
			+ " SET " 
			+ SMTableconveniencephrases.mPhraseText + " = '" + mPhraseText + "'"
			+ " WHERE " + SMTableconveniencephrases.lPhraseID + " = " + iID;

			//System.out.println ("Update_ConveniencePhrase_SQL = " + SQL);
			return SQL;
		}

	//Project Type SQLs:
	public static String Get_Project_Type_List_SQL(){
		SQL = "SELECT * FROM " + SMTableprojecttypes.TableName + " ORDER BY " + SMTableprojecttypes.iTypeId;
		//System.out.println ("Get_Project_Type_List_SQL = " + SQL);
		return SQL;
	}
	
	public static String Get_Project_Type_By_ID(String sID){
		SQL = "SELECT * FROM " + SMTableprojecttypes.TableName + 
		" WHERE " + SMTableprojecttypes.iTypeId + " = " + sID;
		//System.out.println ("Get_Project_Type_By_ID = " + SQL);
		return SQL;
	}
	
	public static String Delete_Project_Type_SQL(String sID){
		SQL = "DELETE FROM " + SMTableprojecttypes.TableName +
			  " WHERE (" +
			  		"(" + SMTableprojecttypes.iTypeId + " = " + sID + ")" +
				")";
		
		//System.out.println ("Delete_Project_Type_SQL = " + SQL);
		return SQL;
	}
	
	public static String Add_New_Project_Type_SQL(String sID){
		SQL = "INSERT into " + SMTableprojecttypes.TableName +
				" (" + SMTableprojecttypes.iTypeId + ")" +
				" VALUES (" + sID + ")";
		//System.out.println ("Add_New_Project_Type_SQL = " + SQL);
		return SQL;
	}
	
	public static String Update_Project_Type_SQL(String iID,
												 String sTypeCode,
												 String sTypeDesc
											  	 ){
			
			SQL = "UPDATE " + SMTableprojecttypes.TableName
			+ " SET " 
			+ SMTableprojecttypes.sTypeCode + " = '" + sTypeCode + "', "
			+ SMTableprojecttypes.sTypeDesc + " = '" + sTypeDesc + "'"
			
			+ " WHERE " + SMTableprojecttypes.iTypeId + " = " + iID;

			//System.out.println ("Update_Project_Type_SQL = " + SQL);
			return SQL;
		}
	
	//Labor type SQLs:
	public static String Get_LaborType_List_SQL(){
		SQL = "SELECT * FROM " + SMTablelabortypes.TableName + " ORDER BY " + SMTablelabortypes.sID;
		//System.out.println ("Get_LaborType_List_SQL = " + SQL);
		return SQL;
	}	
	
	public static String Get_LaborType_By_ID(String sID){
		SQL = "SELECT * FROM " + SMTablelabortypes.TableName + 
		" WHERE " + SMTablelabortypes.sID + " = " + sID;
		//System.out.println ("Get_LaborType_By_ID = " + SQL);
		return SQL;
	}

	public static String Delete_LaborType_SQL(String sID){
		SQL = "DELETE FROM " +
				SMTablelabortypes.TableName +
		  " WHERE (" +
		  		"(" + SMTablelabortypes.sID + " = " + sID + ")" +
			")";
		
		//System.out.println ("Delete_LaborType_SQL = " + SQL);
		return SQL;
	}
	
	public static String Add_New_LaborType_SQL(String sID){
		SQL = "INSERT into " + SMTablelabortypes.TableName +
		" (" + SMTablelabortypes.sID + ")" +
		" VALUES (" + sID + ")";
		//System.out.println ("Add_New_LaborType_SQL = " + SQL);
		return SQL;
	}
	
	public static String Update_LaborType_SQL(
		String sID,
		String sLaborName, 
		String dMarkUpAmount, 
		String sItemNumber, 
		String sCategory
		  ){
		
		SQL = "UPDATE " + SMTablelabortypes.TableName
		+ " SET " 
		+ SMTablelabortypes.sLaborName + " = '" + sLaborName + "', "
		+ SMTablelabortypes.dMarkupAmount + " = " + dMarkUpAmount + ", "
		+ SMTablelabortypes.sItemNumber + " = '" + sItemNumber + "', "
		+ SMTablelabortypes.sCategory + " = '" + sCategory + "'"
		
		+ " WHERE " + SMTablelabortypes.sID+ " = " + sID;

		//System.out.println ("Update_LaborType_SQL = " + SQL);
		return SQL;
	}
	
	//Order Source SQLs:
	public static String Get_OrderSource_List_SQL(){
		SQL = "SELECT * FROM " + SMTableordersources.TableName + " ORDER BY " + SMTableordersources.iSourceID;
		//System.out.println ("Get_OrderSource_List_SQL = " + SQL);
		return SQL;
	}
	
	public static String Get_OrderSource_By_ID(String sID){
		SQL = "SELECT * FROM " + SMTableordersources.TableName + 
		" WHERE " + SMTableordersources.iSourceID + " = " + sID;
		//System.out.println ("Get_OrderSource_By_ID = " + SQL);
		return SQL;
	}
	
	public static String Delete_OrderSource_SQL(String sID){
		SQL = "DELETE FROM " +
				SMTableordersources.TableName +
			  " WHERE (" +
			  		"(" + SMTableordersources.iSourceID + " = " + sID + ")" +
				")";
		
		//System.out.println ("Delete_OrderSource_SQL = " + SQL);
		return SQL;
	}
	
	public static String Add_New_OrderSource_SQL(String sID){
		SQL = "INSERT into " + SMTableordersources.TableName +
		" (" + SMTableordersources.iSourceID + ")" +
		" VALUES (" + sID + ")";
		//System.out.println ("Add_New_OrderSource_SQL = " + SQL);
		return SQL;
	}
	
	public static String Update_OrderSource_SQL(
			String iID,
			String sDesc
			  ){
			
			SQL = "UPDATE " + SMTableordersources.TableName
			+ " SET " 
			+ SMTableordersources.sSourceDesc + " = '" + sDesc + "'"
			
			+ " WHERE " + SMTableordersources.iSourceID + " = " + iID;

			//System.out.println ("Update_OrderSource_SQL = " + SQL);
			return SQL;
		}
	
	//Work performed code SQLS:
	public static String Get_WorkPerformedCode_List_SQL(){
		SQL = "SELECT * FROM " + SMTableworkperformedcodes.TableName + " ORDER BY " + SMTableworkperformedcodes.sWorkPerformedCode;
		//System.out.println ("Get_WorkPerformedCode_List_SQL = " + SQL);
		return SQL;
	}
	
	public static String Get_WorkPerformedCode_By_Code(String sWPCode, String sServiceType){
		SQL = "SELECT * FROM " + SMTableworkperformedcodes.TableName + 
		" WHERE (" + 
			"(" + SMTableworkperformedcodes.sWorkPerformedCode + " = '" + sWPCode + "')" +
			" AND (" + SMTableworkperformedcodes.sCode + " = '" + sServiceType + "')" +
		")";
		//System.out.println ("Get_WorkPerformedCode_By_Code = " + SQL);
		return SQL;
	}
	
	public static String Delete_WorkPerformedCode_SQL(String sWPCode, String sServiceType){
		SQL = "DELETE FROM " +
				SMTableworkperformedcodes.TableName +
			  " WHERE (" +
			  		"(" + SMTableworkperformedcodes.sWorkPerformedCode + " = '" + sWPCode + "')" +
			  		" AND (" + SMTableworkperformedcodes.sCode + " = '" + sServiceType + "')" +
				")";
		
		//System.out.println ("Delete_ConveniencePhrase_SQL = " + SQL);
		return SQL;
	}
	
	public static String Add_New_WorkPerformedCode_SQL(String sWPCode, String sServiceType){
		SQL = "INSERT into " + SMTableworkperformedcodes.TableName +
		" (" + 
			SMTableworkperformedcodes.sWorkPerformedCode + ", " +
			SMTableworkperformedcodes.sCode +
		")" +
		" VALUES ('" + sWPCode + "', '" + sServiceType + "')";
		//System.out.println ("Add_New_WorkPerformedCode_SQL = " + SQL);
		return SQL;
	}
	
	public static String Update_WorkPerformedCode_SQL(
			String sServiceType,
			String sWPCode,
			String sSortOrder,
			String sWorkPerformedPhrase
			  ){
			
			SQL = "UPDATE " + SMTableworkperformedcodes.TableName
			+ " SET " 
			+ SMTableworkperformedcodes.iSortOrder + " = " + sSortOrder + ", "
			+ SMTableworkperformedcodes.sWorkPerformedPhrase + " = '" + sWorkPerformedPhrase + "'"
			
			+ " WHERE (" + 
				"(" + SMTableworkperformedcodes.sCode + " = '" + sServiceType + "')" +
				" AND (" + SMTableworkperformedcodes.sWorkPerformedCode + " = '" + sWPCode + "')" +
			")";

			//System.out.println ("Update_Tax_SQL = " + SQL);
			return SQL;
		}
	
	//Company Profile SQLs:
	public static String Get_CompanyProfile_SQL(){
		SQL = "SELECT * FROM " + SMTablecompanyprofile.TableName;
		//System.out.println ("Get_CompanyProfile_SQL = " + SQL);
		return SQL;
	}
	
	public static String Update_CompanyProfile_By_ID_SQL(
			String sDatabaseID,
			String sAddress01,
			String sAddress02,
			String sAddress03,
			String sAddress04,
			String sCity,
			String sCompanyName,
			String sContactName,
			String sCountry,
			String sFaxNumber,
			String sPhoneNumber,
			String sState,
			String sZipCode
		//	String sWOReceiptHeaderComment
			){
			
			SQL = "UPDATE " + SMTablecompanyprofile.TableName
			+ " SET " 
			+ SMTablecompanyprofile.sAddress01 + " = '" + sAddress01 + "', "
			+ SMTablecompanyprofile.sAddress02 + " = '" + sAddress02 + "', "
			+ SMTablecompanyprofile.sAddress03 + " = '" + sAddress03 + "', "
			+ SMTablecompanyprofile.sAddress04 + " = '" + sAddress04 + "', "
			+ SMTablecompanyprofile.sCity + " = '" + sCity + "', "
			+ SMTablecompanyprofile.sCompanyName + " = '" + sCompanyName + "', "
			+ SMTablecompanyprofile.sContactName + " = '" + sContactName + "', "
			+ SMTablecompanyprofile.sCountry + " = '" + sCountry + "', "
			+ SMTablecompanyprofile.sFaxNumber + " = '" + sFaxNumber + "', "
			+ SMTablecompanyprofile.sPhoneNumber + " = '" + sPhoneNumber + "', "
			+ SMTablecompanyprofile.sState + " = '" + sState + "', "
			+ SMTablecompanyprofile.sZipCode + " = '" + sZipCode + "' "
		//	+ SMTablecompanyprofile.sWOReceiptHeaderComment + " = '" + sWOReceiptHeaderComment + "'"
			
			+ " WHERE " + SMTablecompanyprofile.sDatabaseID + " = '" + sDatabaseID + "'";

			//System.out.println ("Update_Location_SQL = " + SQL);
			return SQL;
		}
	
	//Site Locations
	public static String Get_SiteLocations_SQL(){
		SQL = "SELECT "
			+ SMTablesitelocations.sAcct 
			+ ", " + SMTablesitelocations.sShipToCode 
			+ ", " + SMTablesitelocations.sLabel
			+ " FROM " + SMTablesitelocations.TableName 
			+ " ORDER BY " 
			+ SMTablesitelocations.sAcct + ", " 
			+ SMTablesitelocations.sShipToCode + ", " 
			+ SMTablesitelocations.sLabel;
		
		//System.out.println ("Get_SiteLocations_SQL = " + SQL);
		return SQL;
	}
	
	public static String Get_SiteLocations_By_Code(
		String sCustomerNumber, 
		String sCustomerShipTo,
		String sLabel
		){
		SQL = "SELECT * FROM " + SMTablesitelocations.TableName + 
		" WHERE (" + 
			"(" + SMTablesitelocations.sAcct + " = '" + sCustomerNumber + "')" +
			" AND (" + SMTablesitelocations.sShipToCode + " = '" + sCustomerShipTo + "')" +
			" AND (" + SMTablesitelocations.sLabel + " = '" + sLabel + "')" +
		")";
		//System.out.println ("Get_SiteLocations_By_Code = " + SQL);
		return SQL;
	}

	public static String Delete_SiteLocation_SQL(
			String sCustomerNumber, 
			String sCustomerShipTo,
			String sLabel
			){
		SQL = "DELETE FROM " +
				SMTablesitelocations.TableName +
			  " WHERE (" +
			  		"(" + SMTablesitelocations.sAcct + " = '" + sCustomerNumber + "')" +
			  		" AND (" + SMTablesitelocations.sShipToCode + " = '" + sCustomerShipTo + "')" +
			  		" AND (" + SMTablesitelocations.sLabel + " = '" + sLabel + "')" +
				")";
		
		//System.out.println ("Delete_SiteLocation_SQL = " + SQL);
		return SQL;
	}
	
	public static String Add_New_SiteLocation_SQL(
			String sCustomerNumber, 
			String sCustomerShipTo,
			String sLabel
			){
		SQL = "INSERT into " + SMTablesitelocations.TableName +
		" (" + 
			SMTablesitelocations.sAcct + ", " +
			SMTablesitelocations.sShipToCode + ", " +
			SMTablesitelocations.sLabel +
		")" +
		" VALUES ('" + sCustomerNumber + "', '" + sCustomerShipTo + "', '" + sLabel + "')";
		//System.out.println ("Add_New_SiteLocation_SQL = " + SQL);
		return SQL;
	}
	
	public static String Update_SiteLocation_SQL(
			String sCustomerNumber,
			String sCustomerShipTo,
			String sLabel,
			String sComment
			  ){
			
			SQL = "UPDATE " + SMTablesitelocations.TableName
			+ " SET " 
			+ SMTablesitelocations.sComment + " = '" + sComment + "'"
			
			+ " WHERE (" + 
				"(" + SMTablesitelocations.sAcct + " = '" + sCustomerNumber + "')" +
				" AND (" + SMTablesitelocations.sShipToCode + " = '" + sCustomerShipTo + "')" +
				" AND (" + SMTablesitelocations.sLabel + " = '" + sLabel + "')" +
			")";

			//System.out.println ("Update_SiteLocation_SQL = " + SQL);
			return SQL;
		}
	
	//Default item categories:
	/* Commented out on 7/13/12 - BPK
	public static String Get_DefaultItemCategories_SQL(){
		SQL = "SELECT * FROM " +
		SMTables.DATA_TABLE_SM_DEFAULTITEMCATEGORIES +
		" ORDER BY " + 
		SMTabledefaultitemcategories.ServiceTypeCode + ", " + 
		SMTabledefaultitemcategories.LocationCode;
		
		//System.out.println ("Get_DefaultItemCategories_SQL = " + SQL);
		return SQL;
	}*/
	
	public static String Get_DefaultItemCategory_By_Code(String sServiceCode, String sLocationCode){
		SQL = "SELECT * FROM " + SMTabledefaultitemcategories.TableName+ 
		" WHERE (" + 
			"(" + SMTabledefaultitemcategories.ServiceTypeCode + " = '" + sServiceCode + "')" + 
			" AND (" + SMTabledefaultitemcategories.LocationCode + " = '" + sLocationCode + "')" + 
		")";
		//System.out.println ("Get_DefaultItemCategory_By_Code  " + SQL);
		return SQL;
	}
	
	/* Commented out on 7/13/12 - BPK
	public static String Add_New_DefaultItemCategory_SQL(String sServiceCode, String sLocationCode){
		SQL = "INSERT into " + SMTables.DATA_TABLE_SM_DEFAULTITEMCATEGORIES +
		" (" + 
		SMTabledefaultitemcategories.ServiceTypeCode + ", " +
		SMTabledefaultitemcategories.LocationCode +
		")" +
		" VALUES ('" + sServiceCode + "', '" + sLocationCode + "')";
		//System.out.println ("Get_DefaultItemCategory_By_Code = " + SQL);
		return SQL;
	}*/
	
	public static String Update_DefaultItemCategory_SQL(
			String sServiceCode, 
			String sLocationCode, 
			String sDefaultCategory, 
			String sInitialItem
			){
		
		SQL = "UPDATE " + SMTabledefaultitemcategories.TableName
		+ " SET " 
		+ SMTabledefaultitemcategories.DefaultItemCategory + " = '" + sDefaultCategory + "', "
		+ SMTabledefaultitemcategories.InitialItem + " = '" + sInitialItem + "'"
		+ " WHERE (" + 
			"(" + SMTabledefaultitemcategories.ServiceTypeCode + " = '" + sServiceCode + "')" +
			" AND (" + SMTabledefaultitemcategories.LocationCode + " = '" + sLocationCode + "')" +
		")";

		//System.out.println ("Get_DefaultItemCategory_By_Code = " + SQL);
		return SQL;
	}
	
	/* Commented out on 7/13/12 - BPK
	public static String Delete_DefaultItemCategory_SQL(String sServiceCode, String sLocationCode){
		SQL = "DELETE FROM " +
		SMTables.DATA_TABLE_SM_DEFAULTITEMCATEGORIES +
		" WHERE (" + 
		"(" + SMTabledefaultitemcategories.ServiceTypeCode + " = '" + sServiceCode + "')" +
		" AND (" + SMTabledefaultitemcategories.LocationCode + " = '" + sLocationCode + "')" + ")";
		
		//System.out.println ("Get_DefaultItemCategory_By_Code = " + SQL);
		return SQL;
	}*/
	
	public static String Statistics_OrderHeaders_Count_SQL(){
		return "SELECT COUNT(*) as CNT FROM " + SMTableorderheaders.TableName;
	}
	
	public static String Statistics_OrderDetails_Count_SQL(){
		return "SELECT COUNT(*) as CNT FROM " + SMTableorderdetails.TableName;
	}

	public static String Statistics_OrderHeadersOldest_SQL(){
		return "SELECT " +
			SMTableorderheaders.datOrderDate + 
			" FROM " + SMTableorderheaders.TableName + 
			" ORDER BY " + SMTableorderheaders.datOrderDate + " " + 
			"LIMIT 1";
	}
	
	public static String Statistics_InvoiceDetails_Count_SQL(){
		return "SELECT COUNT(*) as CNT FROM " + SMTableinvoicedetails.TableName;
	}

	public static String Statistics_InvoiceHeaders_Count_SQL(){
		return "SELECT COUNT(*) as CNT FROM " + SMTableinvoiceheaders.TableName;
	}
	
	public static String Statistics_CriticalDates_Count_SQL(){
		return "SELECT COUNT(*) as CNT FROM " + SMTablecriticaldates.TableName;
	}
	
	public static String Statistics_SiteLocations_Count_SQL(){
		return "SELECT COUNT(*) as CNT FROM " + SMTablesitelocations.TableName;
	}
	
	public static String Statistics_SpeedSearchHeaders_Count_SQL(){
		return "SELECT COUNT(*) as CNT FROM " + SMTablessorderheaders.TableName;
	}
	
	public static String Statistics_SpeedSearchOldest_SQL(){
		return "SELECT " +
			SMTablessorderheaders.ORDDATE + " " +
			"FROM " + SMTablessorderheaders.TableName + 
			" ORDER BY " + SMTablessorderheaders.ORDDATE + " " + 
			"LIMIT 1";
	}

	public static String Statistics_Users_Count_SQL(){
		return "SELECT COUNT(*) as CNT FROM " + SMTableusers.TableName;
	}
	
	public static String Get_Orders_For_Client(String sCustomerNumber){
		SQL = "SELECT "
			+ SMTableorderheaders.sCustomerCode 
			+ " FROM " + SMTableorderheaders.TableName
			+ " WHERE ("
				+ "(" + SMTableorderheaders.sCustomerCode + " = '" + sCustomerNumber + "')"
			+ ")";
		//System.out.println ("Get_Orders_For_Client = " + SQL);
		return SQL;
	}
	
	//GL:
	public static String Get_GL_Account_List_SQL(boolean bIncludeInactive){
		
		String sSQL = "SELECT " +
		SMTableglaccounts.sAcctID + ", " +
		SMTableglaccounts.sDesc +
 		" FROM " + SMTableglaccounts.TableName;
		
		if (!bIncludeInactive){
			sSQL +=
				" WHERE ("
					+ SMTableglaccounts.lActive + " = 1"
				+ ")"
				;
		}
		
		sSQL += " ORDER BY " + SMTableglaccounts.sAcctID;
		
		//System.out.println("Get_GL_Account_List_SQL = " + sSQL);
		return sSQL;
	}
	
	public static String Get_GL_Account_SQL(String sGLAccountID){
		
		String sSQL = "SELECT *" 
			+ " FROM " + SMTableglaccounts.TableName
			+ " WHERE ("
				+ SMTableglaccounts.sAcctID + " = '" + sGLAccountID + "'"
			+ ")";
		
		//System.out.println("Get_GL_Account_SQL = " + sSQL);
		return sSQL;
	}
	
	public static String Update_GL_Account_SQL(
			String sGLAccountID,
			String sFormattedID,
			String sDescription,
			String sAcctType,
			String sActive,
			String sCostCenter,
			String sAllowAsAPOExpense,
			String sStructureID,
			String sAccountGroupID
			){
		
		String sSQL = "UPDATE " + SMTableglaccounts.TableName 
			+ " SET "
			+ SMTableglaccounts.sFormattedAcct + " = '" + sFormattedID + "'"
			+ ", " + SMTableglaccounts.sDesc + " = '" + sDescription + "'"
			+ ", " + SMTableglaccounts.sAcctType + " = '" + sAcctType + "'"
			+ ", " + SMTableglaccounts.lActive + " = " + sActive
			+ ", " + SMTableglaccounts.iCostCenterID + " = " + sCostCenter
			+ ", " + SMTableglaccounts.iallowaspoexpense + " = " + sAllowAsAPOExpense
			+ ", " + SMTableglaccounts.lstructureid + " = " + sStructureID
			+ ", " + SMTableglaccounts.laccountgroupid + " = " + sAccountGroupID
			+ " WHERE ("
				+ SMTableglaccounts.sAcctID + " = '" + sGLAccountID + "'"
			+ ")";
		
		//System.out.println("Update_GL_Account_SQL = " + sSQL);
		return sSQL;
	}
	
	public static String Insert_GL_Account_SQL(
			String sGLAccountID,
			String sFormattedID,
			String sDescription,
			String sAcctType,
			String sActive,
			String sCostCenter,
			String sAllowAsAPOExpense,
			String sStructureID,
			String sAccountGroupID
			){
		
		String sSQL = "INSERT INTO " 
			+ SMTableglaccounts.TableName
			+ "("
				+ SMTableglaccounts.sAcctID
				+ ", " + SMTableglaccounts.sFormattedAcct
				+ ", " + SMTableglaccounts.sDesc
				+ ", " + SMTableglaccounts.sAcctType
				+ ", " + SMTableglaccounts.lActive
				+ ", " + SMTableglaccounts.iCostCenterID
				+ ", " + SMTableglaccounts.iallowaspoexpense
				+ ", " + SMTableglaccounts.lstructureid
				+ ", " + SMTableglaccounts.laccountgroupid
			+ ") VALUES ("
				+ "'" + sGLAccountID + "'"
				+ ", '" + sFormattedID + "'"
				+ ", '" + sDescription + "'"
				+ ", '" + sAcctType + "'"
				+ ", " + sActive
				+ ", " + sCostCenter 
				+ ", " + sAllowAsAPOExpense
				+ ", " + sStructureID
				+ ", " + sAccountGroupID
			+ ")";
		
		//System.out.println("Insert_GL_Account_SQL = " + sSQL);
		return sSQL;
	}
	
	//Get_SM_Invoice()
	public static String Get_SM_Invoice(String sInvoiceNumber){
		SQL = "SELECT * FROM "  + SMTableinvoiceheaders.TableName
			+ " WHERE "
				+ "(" + SMTableinvoiceheaders.sInvoiceNumber + " = '" + sInvoiceNumber + "')"
			;
		return SQL;
	}
	
	public static String Get_SM_Invoice_Details_Grouped_By_RevenueGL(String sInvoiceNumber){
		SQL = "SELECT SUM(" + SMTableinvoicedetails.dExtendedPriceAfterDiscount + ") AS revenueamt"
			+ ", " + SMTableinvoicedetails.sRevenueGLAcct
			+ " FROM "  + SMTableinvoicedetails.TableName
			+ " WHERE ("
				+ "(" + SMTableinvoicedetails.sInvoiceNumber + " = '" + sInvoiceNumber + "')"
			+ ")"
			+ " GROUP BY " + SMTableinvoicedetails.sRevenueGLAcct;
		//System.out.println("Get_SM_Invoice_Details_Grouped_By_RevenueGL = " + SQL);
		return SQL;
	}
	
	public static String Get_SM_Invoice_Details(String sInvoiceNumber){
		SQL = "SELECT * FROM "  + SMTableinvoicedetails.TableName
			+ " WHERE ("
				+ "(" + SMTableinvoicedetails.sInvoiceNumber + " = '" + sInvoiceNumber + "')"
			+ ")"
			+ " ORDER BY " + SMTableinvoicedetails.iLineNumber;
		//System.out.println("Get_SM_Invoice_Details = " + SQL);
		return SQL;
	}
	
	public static String Delete_Invoice_Details(String sInvoiceNumber){
		SQL = "DELETE FROM "  + SMTableinvoicedetails.TableName
			+ " WHERE "
				+ SMTableinvoicedetails.sInvoiceNumber + " = '" + sInvoiceNumber + "'";
		return SQL;
	}
	
	public static String Insert_Invoice_Details(
			String iIsStockItem,
			String dExtendedPrice,
			String dQtyShipped,
			String dUnitPrice,
			String iDetailNumber,
			String iTaxable,
			String iLaborItem,
			String iLineNumber,
			String mDetailInvoiceComment,
			String sDesc,
			String sInvoiceNumber,
			String sItemCategory,
			String sItemNumber,
			String sLocationCode,
			String sUnitOfMeasure,
			String sInventoryGLAcct,
			String sExpenseGLAcct,
			String sRevenueGLAcct,
			String dExtendedCost,
			String dExtendedPriceAfterDiscount,
			String dLineTaxAmount,
			String iMatchingInvoiceLineNumber,
			String sMechID,
			String sMechInitial,
			String sMechFullName,
			String sLabel,
			String sSuppressDetailOnInvoice,
			String bdExpensedCost
			){
		SQL = "INSERT INTO "  + SMTableinvoicedetails.TableName
			+ "("
				+ SMTableinvoicedetails.dExtendedCost
				+ ", " + SMTableinvoicedetails.dExtendedPrice
				+ ", " + SMTableinvoicedetails.dExtendedPriceAfterDiscount
				+ ", " + SMTableinvoicedetails.bdlinesalestaxamount
				+ ", " + SMTableinvoicedetails.dQtyShipped
				+ ", " + SMTableinvoicedetails.dUnitPrice
				+ ", " + SMTableinvoicedetails.iDetailNumber
				+ ", " + SMTableinvoicedetails.iIsStockItem
				+ ", " + SMTableinvoicedetails.iLineNumber
				+ ", " + SMTableinvoicedetails.iMatchingInvoiceLineNumber
				+ ", " + SMTableinvoicedetails.iTaxable
				+ ", " + SMTableinvoicedetails.mDetailInvoiceComment
				+ ", " + SMTableinvoicedetails.sDesc
				+ ", " + SMTableinvoicedetails.sExpenseGLAcct
				+ ", " + SMTableinvoicedetails.sInventoryGLAcct
				+ ", " + SMTableinvoicedetails.sInvoiceNumber
				+ ", " + SMTableinvoicedetails.sItemCategory
				+ ", " + SMTableinvoicedetails.sItemNumber
				+ ", " + SMTableinvoicedetails.sLabel
				+ ", " + SMTableinvoicedetails.sLocationCode
				+ ", " + SMTableinvoicedetails.sMechFullName
				+ ", " + SMTableinvoicedetails.sMechInitial
				+ ", " + SMTableinvoicedetails.ilaboritem
				+ ", " + SMTableinvoicedetails.imechid
				+ ", " + SMTableinvoicedetails.sRevenueGLAcct
				+ ", " + SMTableinvoicedetails.sUnitOfMeasure
				+ ", " + SMTableinvoicedetails.isuppressdetailoninvoice
				+ ", " + SMTableinvoicedetails.bdexpensedcost
			+ ") VALUES ("
				+ dExtendedCost
				+ ", " + dExtendedPrice
				+ ", " + dExtendedPriceAfterDiscount
				+ ", " + dLineTaxAmount
				+ ", " + dQtyShipped
				+ ", " + dUnitPrice
				+ ", " + iDetailNumber
				+ ", " + iIsStockItem
				+ ", " + iLineNumber
				+ ", " + iMatchingInvoiceLineNumber
				+ ", " + iTaxable
				+ ", '" + mDetailInvoiceComment + "'"
				+ ", '" + sDesc + "'"
				+ ", '" + sExpenseGLAcct + "'"
				+ ", '" + sInventoryGLAcct + "'"
				+ ", '" + sInvoiceNumber + "'"
				+ ", '" + sItemCategory + "'"
				+ ", '" + sItemNumber + "'"
				+ ", '" + sLabel + "'"
				+ ", '" + sLocationCode + "'"
				+ ", '" + sMechFullName + "'"
				+ ", '" + sMechInitial + "'"
				+ ", " + iLaborItem
				+ ", " + sMechID 
				+ ", '" + sRevenueGLAcct + "'"
				+ ", '" + sUnitOfMeasure + "'"
				+ ", " + sSuppressDetailOnInvoice
				+ ", " + bdExpensedCost
			+ ")";
		//System.out.println("Insert_Invoice_Details = " + SQL);
		return SQL;
	}
	
	public static String Insert_SM_Invoice(
			String datInvoiceDate,
			String dDiscountAmount,
			String dTaxAmount,
			String iCustomerDiscountLevel,
			String iNumberOfLinesOnInvoice,
			String iRequisitionDueDay,
			String mInvoiceComments,
			String sBillToAddressLine1,
			String sBillToAddressLine2,
			String sBillToAddressLine3,
			String sBillToAddressLine4,
			String sBillToCity,
			String sBillToContact,
			String sBillToCountry,
			String sBillToFax,
			String sBillToName,
			String sBillToPhone,
			String sBillToState,
			String sBillToZip,
			String sCustomerCode,
			String sDefaultPriceListCode,
			String sInvoiceNumber,
			String sLocation,
			String sOrderNumber,
			String sPONumber,
			String sSalesperson,
			String sServiceTypeCode,
			String sServiceTypeCodeDescription,
			String sShipToAddress1,
			String sShipToAddress2,
			String sShipToAddress3,
			String sShipToAddress4,
			String sShipToCity,
			String sShipToCode,
			String sShipToContact,
			String sShipToCountry,
			String sShipToFax,
			String sShipToName,
			String sShipToPhone,
			String sShipToState,
			String sShipToZip,
			String sTaxExemptNumber,
			String sTaxGroup,
			String sTerms,
			String dDiscountPercentage,
			String sDesc,
			String sCustomerControlAcctSet,
			String datDueDate,
			String datTermsDiscountDate,
			String dTermsDiscountPercentage,
			String dTermsDiscountAvailable,
			String iExportedToAR,
			String iDayEndNumber,
			String datOrderDate,
			String dTaxRate,
			String dTaxBase,
			String dPrePayment,
			String sDiscountDesc,
			String iTransactionType,
			String sMatchingInvoiceNumber,
			String iIsCredited,
			String iExportedToIC,
			String sCreatedByFullName,
			String lCreatedByID,
			String iOrderSourceID,
			String sOrderSourceDesc,
			String iSalesGroup,
			String strimmedordernumber,
			String m_itaxid,
			String m_staxtype,
			String m_icalculatetaxonpurchaseorsale,
			String m_idisplaytaxoncustomerinvoice,
			String m_sdbalogo,
			String m_mdbadescription,
			String m_mdbaaddress,
			String m_mdbaremittoaddress,
			String m_mdbainvoicelogo
			){
		
		SQL = "INSERT INTO " + SMTableinvoiceheaders.TableName
			+ "("
			+ SMTableinvoiceheaders.datInvoiceDate
			+ ", " + SMTableinvoiceheaders.dDiscountAmount
			+ ", " + SMTableinvoiceheaders.bdsalestaxamount
			+ ", " + SMTableinvoiceheaders.iCustomerDiscountLevel
			+ ", " + SMTableinvoiceheaders.iNumberOfLinesOnInvoice
			+ ", " + SMTableinvoiceheaders.iRequisitionDueDay
			+ ", " + SMTableinvoiceheaders.mInvoiceComments
			+ ", " + SMTableinvoiceheaders.sBillToAddressLine1
			+ ", " + SMTableinvoiceheaders.sBillToAddressLine2
			+ ", " + SMTableinvoiceheaders.sBillToAddressLine3
			+ ", " + SMTableinvoiceheaders.sBillToAddressLine4
			+ ", " + SMTableinvoiceheaders.sBillToCity
			+ ", " + SMTableinvoiceheaders.sBillToContact
			+ ", " + SMTableinvoiceheaders.sBillToCountry
			+ ", " + SMTableinvoiceheaders.sBillToFax
			+ ", " + SMTableinvoiceheaders.sBillToName
			+ ", " + SMTableinvoiceheaders.sBillToPhone
			+ ", " + SMTableinvoiceheaders.sBillToState
			+ ", " + SMTableinvoiceheaders.sBillToZip
			+ ", " + SMTableinvoiceheaders.sCustomerCode
			+ ", " + SMTableinvoiceheaders.sDefaultPriceListCode
			+ ", " + SMTableinvoiceheaders.sInvoiceNumber
			+ ", " + SMTableinvoiceheaders.sLocation
			+ ", " + SMTableinvoiceheaders.sOrderNumber
			+ ", " + SMTableinvoiceheaders.sPONumber
			+ ", " + SMTableinvoiceheaders.sSalesperson
			+ ", " + SMTableinvoiceheaders.sServiceTypeCode
			+ ", " + SMTableinvoiceheaders.sServiceTypeCodeDescription
			+ ", " + SMTableinvoiceheaders.sShipToAddress1
			+ ", " + SMTableinvoiceheaders.sShipToAddress2
			+ ", " + SMTableinvoiceheaders.sShipToAddress3
			+ ", " + SMTableinvoiceheaders.sShipToAddress4
			+ ", " + SMTableinvoiceheaders.sShipToCity
			+ ", " + SMTableinvoiceheaders.sShipToCode
			+ ", " + SMTableinvoiceheaders.sShipToContact
			+ ", " + SMTableinvoiceheaders.sShipToCountry
			+ ", " + SMTableinvoiceheaders.sShipToFax
			+ ", " + SMTableinvoiceheaders.sShipToName
			+ ", " + SMTableinvoiceheaders.sShipToPhone
			+ ", " + SMTableinvoiceheaders.sShipToState
			+ ", " + SMTableinvoiceheaders.sShipToZip
			+ ", " + SMTableinvoiceheaders.sTaxExemptNumber
			+ ", " + SMTableinvoiceheaders.staxjurisdiction
			+ ", " + SMTableinvoiceheaders.sTerms
			+ ", " + SMTableinvoiceheaders.dDiscountPercentage
			+ ", " + SMTableinvoiceheaders.sDesc
			+ ", " + SMTableinvoiceheaders.sCustomerControlAcctSet
			+ ", " + SMTableinvoiceheaders.datDueDate
			+ ", " + SMTableinvoiceheaders.datTermsDiscountDate
			+ ", " + SMTableinvoiceheaders.dTermsDiscountPercentage
			+ ", " + SMTableinvoiceheaders.dTermsDiscountAvailable
			+ ", " + SMTableinvoiceheaders.iExportedToAR
			+ ", " + SMTableinvoiceheaders.iDayEndNumber
			+ ", " + SMTableinvoiceheaders.datOrderDate
			+ ", " + SMTableinvoiceheaders.bdtaxrate
			+ ", " + SMTableinvoiceheaders.bdsalestaxbase
			+ ", " + SMTableinvoiceheaders.dPrePayment
			+ ", " + SMTableinvoiceheaders.sDiscountDesc
			+ ", " + SMTableinvoiceheaders.iTransactionType
			+ ", " + SMTableinvoiceheaders.sMatchingInvoiceNumber
			+ ", " + SMTableinvoiceheaders.iIsCredited
			+ ", " + SMTableinvoiceheaders.iExportedToIC
			+ ", " + SMTableinvoiceheaders.sCreatedByFullName
			+ ", " + SMTableinvoiceheaders.lCreatedByID
			+ ", " + SMTableinvoiceheaders.datInvoiceCreationDate
			+ ", " + SMTableinvoiceheaders.iOrderSourceID
			+ ", " + SMTableinvoiceheaders.sOrderSourceDesc
			+ ", " + SMTableinvoiceheaders.iSalesGroup
			+ ", " + SMTableinvoiceheaders.strimmedordernumber
			+ ", " + SMTableinvoiceheaders.itaxid
			+ ", " + SMTableinvoiceheaders.staxtype
			+ ", " + SMTableinvoiceheaders.icalculatetaxonpurchaseorsale
			+ ", " + SMTableinvoiceheaders.icalculatetaxoncustomerinvoice
			+ ", " + SMTableinvoiceheaders.sdbalogo
			+ ", " + SMTableinvoiceheaders.sdbadescription
			+ ", " + SMTableinvoiceheaders.mdbaaddress
			+ ", " + SMTableinvoiceheaders.mdbaremittoaddress
			+ ", " + SMTableinvoiceheaders.sdbainvoicelogo
			+ ") VALUES ("
			+ "'" + datInvoiceDate + "'"
			+ ", " + dDiscountAmount
			+ ", " + dTaxAmount
			+ ", " + iCustomerDiscountLevel
			+ ", " + iNumberOfLinesOnInvoice
			+ ", " + iRequisitionDueDay
			+ ", '" + mInvoiceComments + "'"
			+ ", '" + sBillToAddressLine1 + "'"
			+ ", '" + sBillToAddressLine2 + "'"
			+ ", '" + sBillToAddressLine3 + "'"
			+ ", '" + sBillToAddressLine4 + "'"
			+ ", '" + sBillToCity + "'"
			+ ", '" + sBillToContact + "'"
			+ ", '" + sBillToCountry + "'"
			+ ", '" + sBillToFax + "'"
			+ ", '" + sBillToName + "'"
			+ ", '" + sBillToPhone + "'"
			+ ", '" + sBillToState + "'"
			+ ", '" + sBillToZip + "'"
			+ ", '" + sCustomerCode + "'"
			+ ", '" + sDefaultPriceListCode + "'"
			+ ", '" + sInvoiceNumber + "'"
			+ ", '" + sLocation + "'"
			+ ", '" + sOrderNumber + "'"
			+ ", '" + sPONumber + "'"
			+ ", '" + sSalesperson + "'"
			+ ", '" + sServiceTypeCode + "'"
			+ ", '" + sServiceTypeCodeDescription + "'"
			+ ", '" + sShipToAddress1 + "'"
			+ ", '" + sShipToAddress2 + "'"
			+ ", '" + sShipToAddress3 + "'"
			+ ", '" + sShipToAddress4 + "'"
			+ ", '" + sShipToCity + "'"
			+ ", '" + sShipToCode + "'"
			+ ", '" + sShipToContact + "'"
			+ ", '" + sShipToCountry + "'"
			+ ", '" + sShipToFax + "'"
			+ ", '" + sShipToName + "'"
			+ ", '" + sShipToPhone + "'"
			+ ", '" + sShipToState + "'"
			+ ", '" + sShipToZip + "'"
			+ ", '" + sTaxExemptNumber + "'"
			+ ", '" + sTaxGroup + "'"
			+ ", '" + sTerms + "'"
			+ ", " + dDiscountPercentage
			+ ", '" + sDesc + "'"
			+ ", '" + sCustomerControlAcctSet + "'"
			+ ", '" + datDueDate + "'"
			+ ", '" + datTermsDiscountDate + "'"
			+ ", " + dTermsDiscountPercentage
			+ ", " + dTermsDiscountAvailable
			+ ", " + iExportedToAR
			+ ", " + iDayEndNumber
			+ ", '" + datOrderDate + "'"
			+ ", " + dTaxRate
			+ ", " + dTaxBase
			+ ", " + dPrePayment
			+ ", '" + sDiscountDesc + "'"
			+ ", " + iTransactionType
			+ ", '" + sMatchingInvoiceNumber + "'"
			+ ", " + iIsCredited
			+ ", " + iExportedToIC
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sCreatedByFullName) + "'"
			+ ", " + lCreatedByID + ""
			+ ", NOW()"
			+ ", " + iOrderSourceID
			+ ", '" + sOrderSourceDesc + "'"
			+ ", " + iSalesGroup
			+ ", '" + strimmedordernumber + "'"
			+ ", " + m_itaxid
			+ ", '" + m_staxtype + "'"
			+ ", " + m_icalculatetaxonpurchaseorsale
			+ ", " + m_idisplaytaxoncustomerinvoice
			+ ", '" + m_sdbalogo + "'"
			+ ", '" + m_mdbadescription + "'"
			+ ", '" + m_mdbaaddress + "'"
			+ ", '" + m_mdbaremittoaddress + "'"
			+ ", '"+m_mdbainvoicelogo+"'"
			+ ")";
		//System.out.println("Insert_SM_Invoice");
		return SQL;
	}
}
