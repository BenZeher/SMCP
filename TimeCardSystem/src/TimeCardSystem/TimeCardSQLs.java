package TimeCardSystem;

import java.sql.Date;
import java.sql.Types;
import java.util.ArrayList;

import ServletUtilities.clsDatabaseFunctions;
import TCSDataDefinition.*;

//this class hold all the SQL statements used in the program.
public class TimeCardSQLs {

	private static String SQL = "";
	
	

	public static String Get_Lone_Manager_List_SQL(){
		
		SQL = "SELECT DISTINCT" +
				" " + Employees.sEmployeeID + "," +
				" " + Employees.sEmployeeFirstName + "," +
				" " + Employees.sEmployeeMiddleName + "," +
				" " + Employees.sEmployeeLastName + 
			  " FROM " + Employees.TableName + " LEFT JOIN " + TCSDataDefinition.ManagerAccessControlDefinitions.TableName + 
			  " ON " + Employees.sEmployeeID + " = " + TCSDataDefinition.ManagerAccessControlDefinitions.sManagerID +
			  " WHERE " + TCSDataDefinition.ManagerAccessControlDefinitions.sManagerID + " IS NULL" +
			  	//" AND " + Employees.iAccessLevel + " >= " + TimeCardAccessLevels.MANAGER + 
			  	" AND " + Employees.sEmployeeID + " <> 'ADMIN'" +
			  " ORDER BY " + Employees.sEmployeeID;
	 	
		//System.out.println ("Get_Lone_Manager_List_SQL = " + SQL);
		return SQL;
	}
	
	public static String Get_Access_Control_Function_List(){
		
		SQL = "SELECT * FROM " + ACFunctions.TableName + 
			  " ORDER BY " + ACFunctions.TableName + "." + ACFunctions.sFunctionName;
		
		//System.out.println ("Get_Security_Group_List_SQL = " + SQL);
		return SQL;
	}
	
	public static String Get_Access_Control_Groups_SQL(){
		
		SQL = "SELECT * FROM " + ACGroups.TableName + " ORDER BY " + ACGroups.sGroupName;
		
		//System.out.println ("Get_Security_Group_List_SQL = " + SQL);
		return SQL;
	}

	public static String Get_Access_Control_Group_Functions_SQL(String sGroup){
		SQL = "SELECT * FROM " + ACGroupFunctions.TableName + 
			" WHERE (" +
			ACGroupFunctions.sGroupName + " = '" + sGroup + "'" +
			") ORDER BY " + ACGroupFunctions.sFunction;
		//System.out.println ("Get_Security_Group_Functions_SQL = " + SQL);
		return SQL;
	}
	
	public static String Get_Access_Control_Group_Users_SQL(String sGroup){
		SQL = "SELECT * FROM " + ACUserGroups.TableName + 
			" WHERE (" +
			" " + ACUserGroups.sGroupName + " = '" + sGroup + "'" +
			") ORDER BY " + ACUserGroups.sEmployeeID;
		//System.out.println ("Get_Security_Group_Functions_SQL = " + SQL);
		return SQL;
	}

	public static String Delete_Group_SQL(String sGroupName){
		SQL = "DELETE FROM " + ACGroups.TableName +
		" WHERE (" +
			" " + ACGroups.sGroupName + " = '" + sGroupName + "'" +
		")";
		//System.out.println ("Delete_Group_SQL = " + SQL);
		return SQL;
	}
	
	public static String Delete_Group_Functions_SQL(String sGroupName){

    	//System.out.println("Group Name#2 = " + sGroupName);
		SQL = "DELETE FROM " + ACGroupFunctions.TableName +
		" WHERE (" +
			" " + ACGroupFunctions.sGroupName + " = '" + sGroupName + "'" +
		")";
		//System.out.println ("Delete_Group_Functions_SQL = " + SQL);
		return SQL;
	}
	
	public static String Delete_Group_Users_SQL(String sGroupName){
		SQL = "DELETE FROM " + ACUserGroups.TableName +
		" WHERE (" +
			" " + ACUserGroups.sGroupName + " = '" + sGroupName + "'" +
		")";
		//System.out.println ("Delete_Group_Users_SQL = " + SQL);
		return SQL;
	}
	
	public static String Get_Access_Control_Group_SQL(String sGroupName){
		SQL = "SELECT * FROM " + ACGroups.TableName +
		" WHERE (" +
			" " +ACGroups.sGroupName + " = '" + sGroupName + "'" +
		")";
		//System.out.println ("Get_Security_Group_SQL = " + SQL);
		return SQL;
	}
	
	public static String Add_New_Group_SQL(String sGroupName){
		SQL = "INSERT INTO " + ACGroups.TableName +
		" (" + ACGroups.sGroupName + ")" +
		" VALUES ('" + sGroupName + "')";
		//System.out.println ("Add_New_Group_SQL = " + SQL);
		return SQL;
	}

	public static String Insert_Access_Control_Group_Function_SQL(String sGroupName, 
															String sFunction){
		
		SQL = "INSERT INTO " + ACGroupFunctions.TableName +
			"(" + ACGroupFunctions.sGroupName + ", " + ACGroupFunctions.sFunction + ") VALUES " +
			"('" + sGroupName + "', '" + sFunction + "')"; 
		//System.out.println ("Insert_Security_Group_Function_SQL = " + SQL);
		return SQL;		
	}
	
	public static String Insert_Access_Control_Group_User_SQL(String sGroupName, 
															  String sUser){
		
		SQL = "INSERT INTO " + ACUserGroups.TableName +
			"(" + ACUserGroups.sGroupName + ", " + ACUserGroups.sEmployeeID + " ) VALUES " +
			"('" + sGroupName + "', '" + sUser + "')"; 
		//System.out.println ("Insert_Security_Group_User_SQL = " + SQL);
		return SQL;		
	}
	
	public static String Get_Employee_Access_Control_Info(String sEmployeeID){
		
		SQL = "SELECT * FROM " + ACGroupFunctions.TableName + ", " + ACUserGroups.TableName +
			  " WHERE " + 
			  		ACGroupFunctions.TableName + "." + ACGroupFunctions.sGroupName  + " = " +
			  		ACUserGroups.TableName + "." + ACUserGroups.sGroupName + 
			  	" AND " +
			  		ACUserGroups.sEmployeeID + " = '" + sEmployeeID + "'" +
			  " ORDER BY " + ACGroupFunctions.TableName + "." + ACGroupFunctions.sFunction;
		
		//System.out.println ("Get_Security_Group_List_SQL = " + SQL);
		return SQL;
	}
	
	public static String Delete_Employee_Type_Users_SQL(String sEmployeeTypeID){

		SQL = "DELETE FROM " + TCSTableEmployeeTypeLinks.TableName +
		" WHERE (" +
			" " + TCSTableEmployeeTypeLinks.sEmployeeTypeID + " = '" + sEmployeeTypeID + "'" +
		")";
		return SQL;
	}
	
	public static String Delete_Employee_Type_Access_Users_SQL(String sEmployeeTypeID){

		SQL = "DELETE FROM " + TCSTableEmployeeTypeAccess.TableName +
		" WHERE (" +
			" " + TCSTableEmployeeTypeAccess.sEmployeeTypeID + " = '" + sEmployeeTypeID + "'" +
		")";
		return SQL;
	}
	
	public static String Insert_Employee_Type_Users_SQL(String sEmployeeTypeID, String sEmployeeID){

		SQL = "INSERT INTO " + TCSTableEmployeeTypeLinks.TableName +
				"(" + TCSTableEmployeeTypeLinks.sEmployeeTypeID + ", " + TCSTableEmployeeTypeLinks.sEmployeeID + " ) VALUES " +
				"('" + sEmployeeTypeID + "', '" + sEmployeeID + "')"; 
			return SQL;		
}
	
	public static String Insert_Employee_Type_Access_Users_SQL(String sEmployeeTypeID, String sEmployeeID){

		SQL = "INSERT INTO " + TCSTableEmployeeTypeAccess.TableName +
				"(" + TCSTableEmployeeTypeAccess.sEmployeeTypeID + ", " + TCSTableEmployeeTypeAccess.sEmployeeID + " ) VALUES " +
				"('" + sEmployeeTypeID + "', '" + sEmployeeID + "')"; 
			return SQL;		
}
	

	public static String Get_Update_Employee_Confidential_Info_SQL(String sEID, 
																   String sTime, 
																   String sEPN, 
																   String sESSN, 
																 //SSNMARKSCO
																   String sEBD,
																   String sEHD,
																   int iPTSum, 
																   int iSSum, 
																   double dWorkHour,
																   int iEIsAc){
														
		SQL = "UPDATE " + Employees.TableName + " SET" + 
				" " + Employees.tStartTime + " = \"" + sTime + "\"," +  
				" " + Employees.sPinNumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(sEPN) + "'," +
				" " + Employees.sSSN + " = " + "AES_ENCRYPT('"+ clsDatabaseFunctions.FormatSQLStatement(sESSN) +"','"+TimeCardSQLs.getEncryptionKey()+"')" + "," + //SSNMARKSCO
				" " + Employees.datBirthday + " = \"" + sEBD + "\"," +
				" " + Employees.datHiredDate + " = \"" + sEHD + "\"," +
				" " + Employees.iEmployeePayType + " = " + iPTSum + "," +
				" " + Employees.iEmployeeStatus + " = " + iSSum + "," +
				" " + Employees.dWorkHour + " = " + dWorkHour + "," +
				" " + Employees.iActive + " = " + iEIsAc +
			" WHERE" +
				" " + Employees.sEmployeeID + " = '" + sEID + "'";
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Current_Time_SQL(){
		String SQL = "SELECT LOCALTIME()";
		return SQL;
	}

	public static String Get_Employee_Type_Users_SQL(String sEmployeeTypeID){
		String SQL = "SELECT " + TCSTableEmployeeTypeLinks.TableName + "." + TCSTableEmployeeTypeLinks.sEmployeeID
			  + " FROM " + TCSTableEmployeeTypeLinks.TableName 
			  + " WHERE" 
			  +	" (" + TCSTableEmployeeTypeLinks.TableName + "." + TCSTableEmployeeTypeLinks.sEmployeeTypeID 
			  + " = '" + sEmployeeTypeID + "'"
			  + ")"
			  ;

		return SQL;
	}
	
	public static String Get_Employee_Type_Access_Users_SQL(String sEmployeeTypeID){
		String SQL = "SELECT " + TCSTableEmployeeTypeAccess.TableName + "." + TCSTableEmployeeTypeAccess.sEmployeeID
			  + " FROM " + TCSTableEmployeeTypeAccess.TableName 
			  + " WHERE" 
			  +	" (" + TCSTableEmployeeTypeAccess.TableName + "." + TCSTableEmployeeTypeAccess.sEmployeeTypeID 
			  + " = '" + sEmployeeTypeID + "'"
			  + ")"
			  ;

		return SQL;
	}
	
	public static String Get_Employee_List_SQL(boolean bIncludeInActive){
		String SQL = "SELECT *" + 
			  " FROM " + Employees.TableName + ", " + Departments.TableName +  
			  " WHERE" + 
			  	" " + Employees.TableName + "." + Employees.iDepartmentID + " = " + 
			  		  Departments.TableName + "." + Departments.iDeptID;
			  if (!bIncludeInActive) {
				  SQL = SQL + " AND " + Employees.TableName + "." + Employees.iActive + " = 1";
			  }
			  SQL = SQL + " ORDER By" +
			  	" " + Employees.TableName + "." + Employees.sEmployeeLastName + ", " + Employees.sEmployeeFirstName;
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String getEncryptionKey(){
		String s = "";
		s = TimeCardUtilities.sJob + TimeCardUtilities.sJobs + TimeCardUtilities.sPro ;
		return s;
	}

	public static String Get_Employee_Info_SQL(String sEID){
			String SQL = "SELECT"
	+ " " + Employees.sEmployeeID 
	+ ", " + Employees.sEmployeeFirstName 
	+ ", " + Employees.sEmployeeMiddleName 
	+ ", " + Employees.sEmployeeLastName 
	+ ", " + Employees.tStartTime 
	+ ", " + Employees.sPinNumber 
	+ ", " + "AES_DECRYPT("+ Employees.TableName + "." + Employees.sSSN +",'"+ getEncryptionKey() +"') AS decryptedSSN"
	+ ", " + Employees.iDepartmentID 
	+ ", " + Employees.iActive 
	+ ", " + Employees.sExtension 
	+ ", " + Employees.sOfficePhone 
	+ ", " + Employees.sCellPhone 
	+ ", " + Employees.sHomePhone 
	+ ", " + Employees.sEmail 
	+ ", " + Employees.iPhoneService 
	+ ", " + Employees.iPhoneInsured 
	+ ", " + Employees.sNextelDirectCall 
	+ ", " + Employees.datHiredDate 
	+ ", " + Employees.iEmployeePayType 
	+ ", " + Employees.iEmployeeStatus
	+ ", " + Employees.dWorkHour 
	+ ", " + Employees.datBirthday 
	+ ", " + Employees.sAddressLine1 
	+ ", " + Employees.sAddressLine2 
	//+ ", " + Employees.sAddressLine3 
	+ ", " + Employees.sAddressCity 
	+ ", " + Employees.sAddressState 
	+ ", " + Employees.sAddressZip 
	+ ", " + Employees.sAddressCountry 
	+ ", " + Employees.sBirthCity 
	+ ", " + Employees.sBirthState 
	+ ", " + Employees.sBirthCountry 
	+ ", " + Employees.sDriverLicenseNumber 
	+ ", " + Employees.datDriverLicenseExpDate 
	+ ", " + Employees.sEthnicGroup 
	+ ", " + Employees.sJobTitle 
				   + " FROM" +" " + Employees.TableName + " LEFT JOIN " + Departments.TableName +
				    " ON" + 
				    	" " + Employees.TableName + "." + Employees.iDepartmentID + " = " + Departments.TableName + "." + Departments.iDeptID + 
				    " WHERE" +
				    	" " + Employees.TableName + "." + Employees.sEmployeeID + " = '" + sEID + "'";
			return SQL;
		}

	public static String Get_Employee_Auxiliary_Info(String sEID){
		String SQL = "SELECT * FROM" +
			  	" " + EmployeeAuxiliaryInfo.TableName +
			    " WHERE" +
			    	" " + EmployeeAuxiliaryInfo.sEmployeeID + " = '" + sEID + "'";
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Remove_Employee_SQL(String sEID){
		String SQL = "DELETE FROM " + Employees.TableName +
			  " WHERE" +
			    " " + Employees.sEmployeeID + "= '" + sEID + "'";
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Remove_Employee_Auxiliary_SQL(String sEID){
		String SQL = "DELETE FROM " + EmployeeAuxiliaryInfo.TableName +
			  " WHERE" +
			    " " + EmployeeAuxiliaryInfo.sEmployeeID + " = '" + sEID + "'";
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Employee_Info_By_Pin_SQL(String sPinNumber){
		String SQL = "SELECT *" + 
			  " FROM" +
			  	" " + Employees.TableName + ", " + Departments.TableName +
			  " WHERE" +
			  	" " + Employees.iDepartmentID + " = " + Departments.iDeptID +
			  " AND" +
			  	" " + Employees.sPinNumber + " = '" + sPinNumber + "'";
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Department_List_SQL(){
		String SQL = "SELECT * FROM " + Departments.TableName + " ORDER BY " + Departments.iDeptID;
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Department_List_SQL(String sID){
		String SQL = "SELECT * FROM " + Departments.TableName + ", " + TCSDataDefinition.ManagerAccessControlDefinitions.TableName +
			  " WHERE " + Departments.iDeptID + " = " + TCSDataDefinition.ManagerAccessControlDefinitions.iDepartmentID +
			  	" AND " + TCSDataDefinition.ManagerAccessControlDefinitions.sManagerID + " = '" + sID + "'" + 
			  " ORDER BY " + Departments.iDeptID;
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Department_Info_SQL(int iDeptID){
		String SQL = "SELECT * FROM " + Departments.TableName + " WHERE " + Departments.iDeptID + " = " + iDeptID;
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Remove_Department_SQL(String sDID){
		String SQL = "DELETE FROM " + Departments.TableName +
			  " WHERE" +
			    " " + Departments.iDeptID + " = '" + sDID + "'";
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Retrieve_Special_Note_Types_SQL(){
		String SQL = "SELECT * FROM " + SpecialNoteTypes.TableName + " ORDER BY " + SpecialNoteTypes.sTypeTitle;
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Existing_Special_Notes_SQL(int iLinkID, int iNoteTypeID){
		String SQL = "SELECT * FROM " + Notes.TableName + 
			  " WHERE" + 
			 	" " + Notes.iLinkID + " = " + iLinkID + 
			  " AND" + 
				" " + Notes.iNoteTypeID + " = " + iNoteTypeID;
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Existing_Special_Notes_SQL(int iLinkID){
		String SQL = "SELECT * FROM " + Notes.TableName + 
			  " WHERE" + 
			 	" " + Notes.iLinkID + " = " + iLinkID;
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Remove_Special_Note_SQL(int iLinkID, int iNoteTypeID){
		String SQL = "DELETE FROM " + Notes.TableName +
			  " WHERE" + 
			 	" " + Notes.iLinkID + " = " + iLinkID +
			  " AND" + 
				" " + Notes.iNoteTypeID + " = " + iNoteTypeID;
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Change_Log_SQL(int iID){
		String SQL = "SELECT * FROM " + TimeEntries.TableName +
			  " WHERE" + 
			 	" " + TimeEntries.id + " = " + iID;
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Insert_Employee_SQL(String sEID, 
												 String sEFN, 
												 String sEMN, 
												 String sELN, 
												 String sED, 
												 String sEmployeeExtension,
												 String sEmployeeOfficePhone,
												 String sEmployeeCellPhone,
												 String sEmployeeHomePhone,
												 String sEmployeeEmail,
											     String sAddressLine1,
											     String sAddressLine2,
											     String sAddressCity,
											     String sAddressState,
											     String sAddressZipCode,
											     String sAddressCountry,
												 String sNextelDirectCall,
												 int iEPService, 
												 int iEPInsured){
		String SQL = "INSERT INTO " + Employees.TableName + " (" + 
				 " " + Employees.sEmployeeID + "," + 
				 " " + Employees.sEmployeeFirstName + "," +
				 " " + Employees.sEmployeeMiddleName + "," +
				 " " + Employees.sEmployeeLastName + "," +
				 " " + Employees.iDepartmentID + "," +
				 " " + Employees.iActive + "," + //make this employee active after creation
				 " " + Employees.sExtension + "," +
				 " " + Employees.sOfficePhone + "," +
				 " " + Employees.sCellPhone + "," +
				 " " + Employees.sHomePhone + "," +
				 " " + Employees.sEmail + "," + 
				 " " + Employees.sAddressLine1 + "," + 
				 " " + Employees.sAddressLine2 + "," + 
				 " " + Employees.sAddressCity + "," + 
				 " " + Employees.sAddressState + "," + 
				 " " + Employees.sAddressZip + "," + 
				 " " + Employees.sAddressCountry + "," + 
				 " " + Employees.sNextelDirectCall + "," + 
				 " " + Employees.iPhoneService + "," +
				 " " + Employees.iPhoneInsured + ")" +
			  " VALUES (" +
			  	 " '" + clsDatabaseFunctions.FormatSQLStatement(sEID) + "'," +
			  	 " '" + clsDatabaseFunctions.FormatSQLStatement(sEFN) + "'," +
			  	 " '" + clsDatabaseFunctions.FormatSQLStatement(sEMN) + "'," +
			  	 " '" + clsDatabaseFunctions.FormatSQLStatement(sELN) + "'," +
			  	 " " + clsDatabaseFunctions.FormatSQLStatement(sED) + "," +
			  	 " 1," +
			  	 " '" + clsDatabaseFunctions.FormatSQLStatement(sEmployeeExtension) + "'," +
			  	 " '" + clsDatabaseFunctions.FormatSQLStatement(sEmployeeOfficePhone) + "'," +
			  	 " '" + clsDatabaseFunctions.FormatSQLStatement(sEmployeeCellPhone) + "'," +
			  	 " '" + clsDatabaseFunctions.FormatSQLStatement(sEmployeeHomePhone) + "'," +
			  	 " '" + clsDatabaseFunctions.FormatSQLStatement(sEmployeeEmail) + "'," +
			  	 " '" + clsDatabaseFunctions.FormatSQLStatement(sAddressLine1) + "'," +
			  	 " '" + clsDatabaseFunctions.FormatSQLStatement(sAddressLine2) + "'," +
			  	 " '" + clsDatabaseFunctions.FormatSQLStatement(sAddressCity) + "'," +
			  	 " '" + clsDatabaseFunctions.FormatSQLStatement(sAddressState) + "'," +
			  	 " '" + clsDatabaseFunctions.FormatSQLStatement(sAddressZipCode) + "'," +
			  	 " '" + clsDatabaseFunctions.FormatSQLStatement(sAddressCountry) + "'," +
			  	 " '" + clsDatabaseFunctions.FormatSQLStatement(sNextelDirectCall) + "'," +
			  	 " " + iEPService + "," +
			  	 " " + iEPInsured +")";
		
		//System.out.println ("SQL = " + SQL); 
		return SQL;
	}

	public static String Get_Update_Employee_SQL(String sEID, 
												 String sEFN, 
												 String sEMN, 
												 String sELN, 
												 String sED, 
												 String sEmployeeExtension,
												 String sEmployeeOfficePhone,
												 String sEmployeeCellPhone,
												 String sEmployeeHomePhone,
												 String sEmployeeEmail,
											     String sAddressLine1,
											     String sAddressLine2,
											     String sAddressCity,
											     String sAddressState,
											     String sAddressZipCode,
											     String sAddressCountry,
												 String sNextelDirectCall,
												 int iEPService, 
												 int iEPInsured){
		
		String SQL = "UPDATE " + Employees.TableName + " SET" + 
				 " " + Employees.sEmployeeFirstName + " = '" + clsDatabaseFunctions.FormatSQLStatement(sEFN) + "'," +
				 " " + Employees.sEmployeeMiddleName + " = '" + clsDatabaseFunctions.FormatSQLStatement(sEMN) + "'," +
				 " " + Employees.sEmployeeLastName + " = '" + clsDatabaseFunctions.FormatSQLStatement(sELN) + "'," +
				 " " + Employees.iDepartmentID + " = " + clsDatabaseFunctions.FormatSQLStatement(sED) + "," +
				 " " + Employees.sExtension + " = '" + clsDatabaseFunctions.FormatSQLStatement(sEmployeeExtension) + "'," +
				 " " + Employees.sOfficePhone + " = '" + clsDatabaseFunctions.FormatSQLStatement(sEmployeeOfficePhone) + "'," +
				 " " + Employees.sCellPhone + " = '" + clsDatabaseFunctions.FormatSQLStatement(sEmployeeCellPhone) + "'," +
				 " " + Employees.sHomePhone + " = '" + clsDatabaseFunctions.FormatSQLStatement(sEmployeeHomePhone) + "'," +
				 " " + Employees.sEmail + " = '" + clsDatabaseFunctions.FormatSQLStatement(sEmployeeEmail) + "'," +
				 " " + Employees.sAddressLine1 + " = '" + clsDatabaseFunctions.FormatSQLStatement(sAddressLine1) + "'," +
				 " " + Employees.sAddressLine2 + " = '" + clsDatabaseFunctions.FormatSQLStatement(sAddressLine2) + "'," +
				 " " + Employees.sAddressCity + " = '" + clsDatabaseFunctions.FormatSQLStatement(sAddressCity) + "'," +
				 " " + Employees.sAddressState + " = '" + clsDatabaseFunctions.FormatSQLStatement(sAddressState) + "'," +
				 " " + Employees.sAddressZip + " = '" + clsDatabaseFunctions.FormatSQLStatement(sAddressZipCode) + "'," +
				 " " + Employees.sAddressCountry + " = '" + clsDatabaseFunctions.FormatSQLStatement(sAddressCountry) + "'," +
				 " " + Employees.sNextelDirectCall + " = '" + clsDatabaseFunctions.FormatSQLStatement(sNextelDirectCall) + "'," +
				 " " + Employees.iPhoneService + " = " + iEPService + "," +
				 " " + Employees.iPhoneInsured + " = " + iEPInsured +
			  " WHERE" +
			 	 " " + Employees.sEmployeeID + " = '" + sEID + "'";
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Insert_Department_SQL(int iSessionDID, 
										  		   String sDepartmentName, 
												   double dDepartmentRate, 
												   double dLateGracePeriod){
		String SQL = "INSERT INTO " + Departments.TableName + " (" + 
				 " " + Departments.iDeptID + "," + 
				 " " + Departments.sDeptDesc + "," +
				 " " + Departments.dDeptRate + "," + 
				 " " + Departments.dLateGracePeriod + ")" + 
			  " VALUES (" +
			  	 " " + iSessionDID + "," +
			  	 " '" + clsDatabaseFunctions.FormatSQLStatement(sDepartmentName) + "'," +
			  	 " " + dDepartmentRate + "," + 
			  	 " " + dLateGracePeriod + ")";
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Update_Department_SQL(int iSessionDID, 
										  		   String sDepartmentName, 
												   double dDepartmentRate, 
												   double dLateGracePeriod){
		
		String SQL = "UPDATE " + Departments.TableName + " SET" + 
				 " " + Departments.sDeptDesc + " = '" + clsDatabaseFunctions.FormatSQLStatement(sDepartmentName) + "'," +
				 " " + Departments.dDeptRate + " = " + dDepartmentRate + "," +
				 " " + Departments.dLateGracePeriod + " = " + dLateGracePeriod +
			  " WHERE" +
			 	 " " + Departments.iDeptID + " = " + iSessionDID;
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Employee_List_SQL(String sManagerID, 
											   boolean bIncludeInActive,
											   boolean bOrderByDepartment){
		String SQL = "SELECT * FROM " + Employees.TableName + ", " + TCSDataDefinition.ManagerAccessControlDefinitions.TableName +
			  " WHERE " + Employees.TableName + "." + Employees.iDepartmentID + " = " + 
			  			  TCSDataDefinition.ManagerAccessControlDefinitions.TableName + "." + TCSDataDefinition.ManagerAccessControlDefinitions.iDepartmentID +
			    " AND " + TCSDataDefinition.ManagerAccessControlDefinitions.TableName + "." + TCSDataDefinition.ManagerAccessControlDefinitions.sManagerID + " = '" + sManagerID + "'";
		  if (bIncludeInActive == false) {
			  SQL = SQL + " AND " + Employees.TableName + "." + Employees.iActive + " = 1";
		  }
		  SQL = SQL + " ORDER By";
		  if (bOrderByDepartment){
		    	SQL = SQL + " " + Employees.TableName + "." + Employees.iDepartmentID + ",";
		    }
		  SQL = SQL + " " + Employees.TableName + "." + Employees.sEmployeeLastName + ", " + Employees.sEmployeeFirstName;
	
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Employee_List_By_Department_SQL(String sDID, 
															 boolean bIncludeInActive){
		String SQL = "SELECT * FROM " + Employees.TableName +
			  " WHERE " + Employees.iDepartmentID + " = '" + sDID + "'";
		  if (!bIncludeInActive) {
			  SQL = SQL + " AND " + Employees.iActive + " = 1";
		  }
		  
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Employee_List_By_Department_SQL(ArrayList<String> sDIDs, 
															 boolean bIncludeInActive,
															 boolean bOrderByDepartment){
		/*
		for (int i=0;i<sDIDs.size();i++){
			System.out.println("sDIDs.get(" + i + ") = " + sDIDs.get(i).toString());
		}
		*/
		String SQL = "SELECT * FROM " + Employees.TableName +
			  " WHERE" + 
			  " " + Employees.iDepartmentID + " = " + sDIDs.get(0).toString();
		for (int i=1;i<sDIDs.size();i++){
			SQL = SQL + " OR " + Employees.iDepartmentID + " = " + sDIDs.get(i).toString();
		}
	    if (bIncludeInActive == false) {
	  	    SQL = SQL + " AND " + Employees.iActive + " = 1";
	    }
	    SQL = SQL + " ORDER By";
	    if (bOrderByDepartment){
	    	SQL = SQL + " " + Employees.iDepartmentID + ",";
	    }
	  	SQL = SQL + " " + Employees.TableName + "." + Employees.sEmployeeLastName + ", " + Employees.sEmployeeFirstName;
	
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Employee_TimeEntry_List_SQL(
		String sEID, 
		String sStartingDate, 
		String sEndingDate, 
		boolean bExcludeEmptyEntry,
		boolean bIncludeFinalizedEntry){
		
		String SQL = "SELECT * FROM " + TimeEntries.TableName
			+ " WHERE (" 
			+ "(" + TimeEntries.sEmployeeID + " = '" + sEID + "')"
			+ " AND ("
				+ "((" + TimeEntries.dtInTime + " >= '" + sStartingDate + "') AND (" + TimeEntries.dtInTime + " <= '" + sEndingDate + "'))"
				+ "OR ((" + TimeEntries.dtOutTime + " >= '" + sStartingDate + "') AND (" + TimeEntries.dtOutTime + " <= '" + sEndingDate + "'))"
			+ ")";
		if (bExcludeEmptyEntry){
			SQL += " AND (" + TimeEntries.dtInTime + " <> '0000-00-00')"
				+ " AND (" + TimeEntries.dtOutTime + " <> '0000-00-00')";
		}
		if (!bIncludeFinalizedEntry){
			SQL += " AND (" 
				+ "(" + TimeEntries.sPeriodDate + " = '0000-00-00')"
				+ " OR (" + TimeEntries.sPeriodDate + " = '" + TimeCardUtilities.TEMPORARY_POSTING_DATE + "')"
			  + ")";
		}
		SQL += ") ORDER BY " + TimeEntries.dtInTime;
		return SQL;
	}

	public static String Get_Employee_TimeEntry_List_SQL(String sEID, 
														 String sStartingDate, 
														 String sEndingDate, 
														 boolean bExcludeEmptyEntry,
														 boolean bIncludeFinalizedEntry,
														 String iEntryType){
		String SQL = "SELECT * FROM " + TimeEntries.TableName + 
			  " WHERE" + 
			  	" " + TimeEntries.sEmployeeID + " = '" + sEID + "'" +
			  " AND" + 
			  	" " + TimeEntries.iEntryTypeID + " = " + iEntryType +
			  " AND" + 
				" ((" + TimeEntries.dtInTime + " >= '" + sStartingDate + "' AND " + TimeEntries.dtInTime + " <= '" + sEndingDate + "') OR" +
				" (" + TimeEntries.dtOutTime + " >= '" + sStartingDate + "' AND " + TimeEntries.dtOutTime + " <= '" + sEndingDate + "'))";
		if (bExcludeEmptyEntry){
			SQL = SQL + 
			  " AND " + TimeEntries.dtInTime + " <> '0000-00-00'" + 
			  " AND " + TimeEntries.dtOutTime + " <> '0000-00-00'";
		}
		if (!bIncludeFinalizedEntry){
			SQL = SQL + 
			  " AND (" + TimeEntries.sPeriodDate + " = '0000-00-00'" + 
			  " OR " + TimeEntries.sPeriodDate + " = '" + TimeCardUtilities.TEMPORARY_POSTING_DATE + "')";
		}
		SQL = SQL +
			  " ORDER BY" + 
			  	" " + TimeEntries.dtInTime;
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Employee_TimeEntry_List_SQL(String sEID, 
														 String sStartingDate, 
														 String sEndingDate, 
														 boolean bExcludeEmptyEntry,
														 boolean bIncludeFinalizedEntry,
														 ArrayList<String> alTypes,
														 String sSortBy, 
														 int iSortOrder){
	
		String SQL = "SELECT * FROM " + TimeEntries.TableName + 
			  " WHERE" + 
			  	" " + TimeEntries.sEmployeeID + " = '" + sEID + "'";
		/*
		for (int i=0;i<alTypes.size();i++){
			System.out.println("Get_Employee_TimeEntry_List_SQL.alTypes(" + i + ") = " + alTypes.get(i).toString());
		}
		*/
		if (alTypes.get(0).toString().compareTo("ALLTYPES") != 0){
			SQL = SQL + " AND (";
			for (int i=0;i<alTypes.size();i++){
				if (alTypes.get(i).toString().compareTo("Late") == 0){
					SQL = SQL + " " + TimeEntries.iLate + " = 1 OR";
				}else{
					SQL = SQL + " " + TimeEntries.iEntryTypeID + " = " + alTypes.get(i).toString() + " OR";
				}
			}
			//delete the extra " OR"
			SQL = SQL.substring(0, SQL.length() - 3);
			SQL = SQL + ")";
		}else{
			SQL = SQL + " AND (" + TimeEntries.iEntryTypeID + " <> 0 OR " + TimeEntries.iLate + " = 1)";
		}
			  
	    SQL = SQL + " AND" + 
				" ((" + TimeEntries.dtInTime + " >= \"" + sStartingDate + "\" AND " + TimeEntries.dtInTime + " <= \"" + sEndingDate + "\") OR" +
				" (" + TimeEntries.dtOutTime + " >= \"" + sStartingDate + "\" AND " + TimeEntries.dtOutTime + " <= \"" + sEndingDate + "\"))";
		if (bExcludeEmptyEntry){
			SQL = SQL + 
			  " AND " + TimeEntries.dtInTime + " <> \"0000-00-00\"" + 
			  " AND " + TimeEntries.dtOutTime + " <> \"0000-00-00\"";
		}
		if (!bIncludeFinalizedEntry){
			SQL = SQL + 
			  " AND (" + TimeEntries.sPeriodDate + " = '0000-00-00'" + 
			  " OR " + TimeEntries.sPeriodDate + " = '" + TimeCardUtilities.TEMPORARY_POSTING_DATE + "')";
		}
		SQL = SQL +
			  " ORDER BY " + sSortBy;
		
		if (iSortOrder == 1){
			SQL = SQL + " DESC";
		}
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Employee_Workday_List_SQL(String sEID, String sStartingDate, String sEndingDate){
		String SQL = "SELECT * FROM " + TimeEntries.TableName + 
			  " WHERE" + 
			  	" sEmployeeID = '" + sEID + "'" +
			  " AND" + 
				" ((" + TimeEntries.dtInTime + " >= \"" + sStartingDate + "\" AND " + TimeEntries.dtInTime + " <= \"" + sEndingDate + "\") OR" +
				" (" + TimeEntries.dtOutTime + " >= \"" + sStartingDate + "\" AND " + TimeEntries.dtOutTime + " <= \"" + sEndingDate + "\"))";
		
		SQL = SQL +
			  " ORDER BY" + 
			  	" " + TimeEntries.dtInTime;
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Raw_TimeEntry_List_SQL(String sManagerID,
													String sStartingDate, 
													String sEndingDate, 
													String sEID, 
													String sDID){
		
		String SQL = "SELECT DISTINCT " + Employees.TableName + "." + Employees.sEmployeeID + "," +
					" " + Employees.TableName + "." + Employees.sEmployeeFirstName + "," + 
					" " + Employees.TableName + "." + Employees.sEmployeeLastName + "," + 
					" " + Employees.TableName + "." + Employees.sEmployeeMiddleName + "," +
					" " + TCTablerawpunchevents.TableName + "." + TCTablerawpunchevents.iInOut + "," +
					" " + TCTablerawpunchevents.TableName + "." + TCTablerawpunchevents.dtTime + "," +
					" " + TCTablerawpunchevents.TableName + "." + TCTablerawpunchevents.sIPAddress + "," +
					" " + TCTablerawpunchevents.TableName + "." + TCTablerawpunchevents.sgeocode +
		" FROM " + TCTablerawpunchevents.TableName + ", " + Employees.TableName + ", " + TCSDataDefinition.ManagerAccessControlDefinitions.TableName + 
		" WHERE" +
		  " " + Employees.TableName + "." + TCTablerawpunchevents.sEmployeeID + " = " + TCTablerawpunchevents.TableName + "." + Employees.sEmployeeID;
		
		if (sManagerID.compareTo("ADMIN") != 0){
			SQL = SQL + " AND " + TCSDataDefinition.ManagerAccessControlDefinitions.TableName + "." + TCSDataDefinition.ManagerAccessControlDefinitions.sManagerID + " = '" + sManagerID + "'" + 
						" AND " + Employees.TableName + "." + Employees.iDepartmentID + " = " + TCSDataDefinition.ManagerAccessControlDefinitions.TableName + "." + TCSDataDefinition.ManagerAccessControlDefinitions.iDepartmentID;
		}
		if (sEID.compareTo("") != 0){
			SQL = SQL + " AND " + Employees.TableName + "." + Employees.sEmployeeID + " = '" + sEID + "'";
		}else{
			if (sDID.compareTo("0") != 0){
				SQL = SQL + " AND " + Employees.TableName + "." + Employees.iDepartmentID + " = " + sDID;
			}
		}
		SQL = SQL + " AND" + 
				" " + TCTablerawpunchevents.dtTime + " >= \"" + sStartingDate + "\" AND " + TCTablerawpunchevents.dtTime + " < \"" + sEndingDate + "\"" +
				
				" ORDER BY" + 
			  	" " + TCTablerawpunchevents.dtTime + ", " + Employees.TableName + "." + Employees.sEmployeeID;
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Period_Total_Time_SQL(String sEndingDateFrom, 
												   int iMultipleEndingDates, 
												   String sEndingDateTo, 
												   String sEID, 
												   ArrayList<String> alDepartments){
		
		String SQL = "SELECT * FROM " + TimeTotals.TableName + ", " + 
								 Employees.TableName + ", " + 
								 Departments.TableName + 
			  " WHERE" +
			  	" " + TimeTotals.TableName + "." + TimeTotals.sEmployeeID + " = " + 
			  	 	  Employees.TableName + "." + Employees.sEmployeeID +
			  " AND" + 
			  	" " + Departments.TableName + "." + Departments.iDeptID + " = " + 
			  		  Employees.TableName + "." + Employees.iDepartmentID;
		if (iMultipleEndingDates == 0){
			if (sEndingDateFrom.length() != 0){
				SQL = SQL + " AND " + TimeTotals.TableName + "." + TimeTotals.datPeriodEndDate + " = \"" + sEndingDateFrom + "\"";
			}
		}else{
			if (sEndingDateFrom.length() != 0){
				SQL = SQL + " AND " + TimeTotals.TableName + "." + TimeTotals.datPeriodEndDate + " >= \"" + sEndingDateFrom + "\"";
			}
			if (sEndingDateTo.length() != 0){
				SQL = SQL + " AND " + TimeTotals.TableName + "." + TimeTotals.datPeriodEndDate + " <= \"" + sEndingDateTo + "\"";
			}
		}
		
		if (sEID.length() != 0){
			SQL = SQL + " AND " + Employees.TableName + "." + Employees.sEmployeeID + " = '" + sEID + "'";
		}else if (alDepartments.size() > 0 && alDepartments.get(0).toString().compareTo("ALLDEPT") != 0){
			SQL = SQL + " AND (" + Departments.TableName + "." + Departments.iDeptID + " = " + alDepartments.get(0).toString();
			for (int i=1;i<alDepartments.size();i++){
				SQL = SQL + " OR " + Departments.TableName + "." + Departments.iDeptID + " = " + alDepartments.get(i).toString();
			}
			SQL = SQL + ")";
		}
		SQL = SQL + " ORDER BY" + 
			  	" " + Departments.TableName + "." + Departments.iDeptID + ", " + 
			  		  TimeTotals.TableName + "." + TimeTotals.datPeriodEndDate + ", " + 
			  		  Employees.TableName + "." + Employees.sEmployeeID + ", " + 
			  		  TimeTotals.TableName + "." + TimeTotals.datPeriodEndDate;
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Period_Total_Time_SQL(String sEID){
		
		String SQL = "SELECT * FROM " + TimeTotals.TableName + 
			  " WHERE" +
			  	" " + TimeTotals.sEmployeeID + " = '" + sEID + "'" + 
			   " AND" + 
			  	" " + TimeTotals.iFinalized + " = 0";
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Period_Special_Note_SQL(int iNoteTypeID,
													 String sEndingDate, 
												     String sEID, 
												     String sDID){
		String SQL = "SELECT * FROM " + TimeEntries.TableName + ", " + Employees.TableName + ", " + Departments.TableName + ", " + Notes.TableName + ", " + SpecialNoteTypes.TableName + 
			  " WHERE" +
			  	" " + TimeEntries.TableName + "." + TimeEntries.sEmployeeID + " = " + Employees.TableName + "." + Employees.sEmployeeID +
			  " AND" + 
			  	" " + Departments.TableName + "." + Departments.iDeptID + " = " + Employees.TableName + "." + Employees.iDepartmentID +
			  " AND" + 
			  	" " + Notes.TableName + "." + Notes.iNoteTypeID + " = " + SpecialNoteTypes.TableName + "." + SpecialNoteTypes.iTypeID +
			  " AND" + 
			  	" " + TimeEntries.TableName + "." + TimeEntries.id + " = " + Notes.TableName + "." + Notes.iLinkID;
		if (iNoteTypeID != 0){
			SQL = SQL + " AND " + Notes.TableName + "." + Notes.iNoteTypeID + " = " + iNoteTypeID;
		}
		if (sEndingDate.length() != 0){
			SQL = SQL + " AND " + TimeEntries.TableName + "." + TimeEntries.sPeriodDate + " = \"" + sEndingDate + "\"";
		}
		if (sEID.length() != 0){
			SQL = SQL + " AND " + Employees.TableName + "." + Employees.sEmployeeID + " = '" + sEID + "'";
		}else if (sDID.length() != 0){
				SQL = SQL + " AND " + Departments.TableName + "." + Departments.iDeptID + " = " + sDID;
		}
		SQL = SQL + " ORDER BY" + 
			  	" " + Notes.TableName + "." + Notes.iNoteTypeID + ", " + 
			  		  Departments.TableName + "." + Departments.iDeptID + ", " + 
			  		  Employees.TableName + "." + Employees.sEmployeeID + ", " + 
			  		  TimeEntries.TableName + "." + TimeEntries.sPeriodDate;
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Insert_Time_Total_SQL(String sEmployeeID, 
											  	   String sTruePeriodEndDate,  
											  	   double dWeeklySum,  
											  	   double dRegular, 
											  	   double dOverTime, 
											  	   double dDoubleTime, 
											  	   double dLeaveTime, 
											  	   String sUserID){
		String SQL = "INSERT INTO " + TimeTotals.TableName + "(" + 
				" " + TimeTotals.sEmployeeID + "," + 
				" " + TimeTotals.datPeriodEndDate + "," + 
				" " + TimeTotals.dPeriodTotal + "," + 
				" " + TimeTotals.dPeriodRegular + "," + 
				" " + TimeTotals.dPeriodOverTime + "," + 
				" " + TimeTotals.dPeriodDouble + "," + 
				" " + TimeTotals.dPeriodLeave + "," + 
				" " + TimeTotals.sCreatorID + 
			  ") VALUES (" + 
				"'" + sEmployeeID + "'," + 
				"'" + sTruePeriodEndDate + "'," +  
				dWeeklySum + "," +  
				dRegular + "," +  
				dOverTime + "," +  
				dDoubleTime + "," +  
				dLeaveTime + "," +  
				"'" + sUserID + "'" +
			  ")";
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String flagTimeEntryAsPosted(int iID){
		String SQL = "UPDATE "
			+ TimeEntries.TableName 
			+ " SET "
			+ TimeEntries.sPeriodDate + " = '" + TimeCardUtilities.TEMPORARY_POSTING_DATE + "'";
		if (iID != 0){
			SQL += " WHERE ("
				+ "(" + TimeEntries.id + " = " + iID + ")"
				+ ")";
		}else{
			SQL += " WHERE ("
				+ "(" + TimeEntries.sPeriodDate + " = '" + TimeCardUtilities.TEMPORARY_POSTING_DATE + "')"
			+ ")";
		}
		return SQL;
	}

	public static String Get_Flag_Post_Time_Entry(String sTruePeriodEndDate,
												  String sEmp){
		String SQL = "UPDATE " + TimeEntries.TableName + " SET" +
				" " + TimeEntries.sPeriodDate + " = '" + sTruePeriodEndDate + "'" + 
		      " WHERE" + 
		   		" " + TimeEntries.sEmployeeID + " = '" + sEmp + "'" + 
		   		" AND " + 
		   		" " + TimeEntries.sPeriodDate + " = '" + TimeCardUtilities.TEMPORARY_POSTING_DATE + "'";
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Flag_Post_Leave_Adjustment(String sTruePeriodEndDate,
												 		int iID){
		String SQL = "UPDATE " + LeaveAdjustments.TableName + " SET" +
				" " + LeaveAdjustments.sPeriodDate + " = '" + sTruePeriodEndDate + "'";
		if (iID != 0){
		      SQL = SQL + " WHERE" + 
		   					" " + LeaveAdjustments.id + " = " + iID;
		}else{
			  SQL = SQL + " WHERE" + 
					   		" " + LeaveAdjustments.sPeriodDate + " = '" + TimeCardUtilities.TEMPORARY_POSTING_DATE + "'";
		}
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Flag_Post_Leave_Adjustment(String sTruePeriodEndDate,
												  		 String sEmp){
		String SQL = "UPDATE " + LeaveAdjustments.TableName + " SET" +
				" " + LeaveAdjustments.sPeriodDate + " = '" + sTruePeriodEndDate + "'" + 
		      " WHERE" + 
		   		" (" + LeaveAdjustments.sEmployeeID + " = '" + sEmp + "')" + 
		   		" AND " + 
		   		" (" + LeaveAdjustments.sPeriodDate + " = '" + TimeCardUtilities.TEMPORARY_POSTING_DATE + "')";
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Insert_In_Time_Entry_SQL(String sEID, 
			  										  String sInTime,
			  										  String sInTimeOri,
			  										  int iInModified, 
													  int iIsEarlyStart, 
													  int iIsLate,
													  int iLateMinute,
													  String sLog, 
													  int iTypeID){
		String SQL = "INSERT INTO " + TimeEntries.TableName + "(" + 
				" " + TimeEntries.sEmployeeID + "," +
				" " + TimeEntries.dtInTime + "," +
				" " + TimeEntries.dtInTimeOri + "," +
				" " + TimeEntries.iInModified + "," +
				" " + TimeEntries.iEarlyStart + "," +
				" " + TimeEntries.iLate + "," +
				" " + TimeEntries.iLateMinute + "," +
				" " + TimeEntries.mChangeLog + "," +
				" " + TimeEntries.iEntryTypeID +
			  ") VALUES (" +
				"'" + sEID + "'," + 
				"\"" + sInTime +  "\"," +
				"\"" + sInTimeOri +  "\"," +
				" " + iInModified + "," + 
				" " + iIsEarlyStart + "," +
				" " + iIsLate + "," +
				" " + iLateMinute + "," +
				" '" + clsDatabaseFunctions.FormatSQLStatement(sLog) + "'," + 
				" " + iTypeID +
				")";
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Insert_Out_Time_Entry_SQL(String sEID, 
			  										   String sTimeStamp,
			  										   int iOutModified, 
			  										   String sLog,
			  										   int iTypeID){
		String SQL = "INSERT INTO " + TimeEntries.TableName + "(" + 
				" " + TimeEntries.sEmployeeID + "," +
				" " + TimeEntries.dtOutTime + "," +
				" " + TimeEntries.iOutModified + "," +
				" " + TimeEntries.mChangeLog + "," +
				" " + TimeEntries.iEntryTypeID +
			  ") VALUES (" +
				"'" + sEID + "'," + 
				"\"" + sTimeStamp +  "\"," +
				" " + iOutModified + "," +
				" '" + clsDatabaseFunctions.FormatSQLStatement(sLog) + "'," + 
				" " + iTypeID +
				")";
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Update_In_time_SQL(int iID, 
												String sInTime, 
												int iModified, 
												int iEarlyStart, 
												int iLateFlag, 
												int iLateMinute, 
												String sLog,
												int iTypeID){
		String SQL = "UPDATE " + TimeEntries.TableName + " SET" + 
					" " + TimeEntries.dtInTime + " = \"" + sInTime +  "\"," + 
					" " + TimeEntries.iEarlyStart + " = " + iEarlyStart + "," + 
					" " + TimeEntries.iLate + " = " + iLateFlag + "," + 
					" " + TimeEntries.iLateMinute + " = " + iLateMinute + "," + 
					" " + TimeEntries.iEntryTypeID + " = " + iTypeID + "," + 
					" " + TimeEntries.iInModified + " = " + iModified;
		if (sLog.compareTo("KEEPTHENOTE") != 0){
	    SQL = SQL + ", " + TimeEntries.mChangeLog + " = '" + clsDatabaseFunctions.FormatSQLStatement(sLog) + "'";
		}
		SQL = SQL + " WHERE" +
			     " " + TimeEntries.id + " = " + iID;
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Update_Out_time_SQL(int iID, 
												 String sOutTime, 
												 int iModified, 
												 String sLog,
												 int iTypeID){
		String SQL = "UPDATE " + TimeEntries.TableName + " SET" + 
				 " " + TimeEntries.dtOutTime + " = \"" + sOutTime +  "\"," + 
				 " " + TimeEntries.iOutModified + " = " + iModified + "," + 
				 " " + TimeEntries.iEntryTypeID + " = " + iTypeID;
		if (sLog.compareTo("KEEPTHENOTE") != 0){
		    SQL = SQL + ", " + TimeEntries.mChangeLog + " = '" + clsDatabaseFunctions.FormatSQLStatement(sLog) + "'";
			}
			SQL = SQL + " WHERE " +
				     "" + TimeEntries.id + " = " + iID;
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Insert_Special_Note_SQL(int iTimeEntryID, 
											 int iTypeID , 
											 String sNote){
		String SQL = "INSERT INTO " + Notes.TableName + " (" + 
				" " + Notes.iLinkID + "," +
				" " + Notes.iNoteTypeID + "," +
				" " + Notes.mNote +
			  ") VALUES (" +
				"'" + iTimeEntryID + "'," + 
				"\"" + iTypeID +  "\"," +
				" '" + clsDatabaseFunctions.FormatSQLStatement(sNote) + "'" + 
				")";
	
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Update_Special_Note_SQL(int iTimeEntryID, 
													 int iTypeID , 
													 String sNote){
		String SQL = "UPDATE " + Notes.TableName + 
				" SET " + Notes.mNote + " = '" + clsDatabaseFunctions.FormatSQLStatement(sNote) + "'" +
			  " WHERE " +
				"" + Notes.iLinkID + " = " + iTimeEntryID + 
			  " AND " +
			  	"" + Notes.iNoteTypeID + " = " + iTypeID;
	
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Retrieve_Specific_Time_Entry(int iTimeEntryID){
		String SQL = "SELECT * FROM " + TimeEntries.TableName + " WHERE " + TimeEntries.id + " = " + iTimeEntryID;
	
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Remove_Specific_TimeEntry(int iTimeEntryID){
		String SQL = "DELETE FROM " + TimeEntries.TableName + " WHERE " + TimeEntries.id + " = " + iTimeEntryID;
	
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Update_Time_Total_SQL(int id, 
												   double dAdjustedRegular, 
												   double dAdjustedOverTime, 
												   double dAdjustedDouble, 
												   double dAdjustedLeave,
												   double dAdjustedTotal){
		String SQL = "UPDATE " + TimeTotals.TableName + " SET" + 
				" " + TimeTotals.dPeriodRegular + " = " + dAdjustedRegular + "," + 
				" " + TimeTotals.dPeriodOverTime + " = " + dAdjustedOverTime + "," + 
				" " + TimeTotals.dPeriodDouble + " = " + dAdjustedDouble + "," + 
				" " + TimeTotals.dPeriodLeave + " = " + dAdjustedLeave + "," + 
				" " + TimeTotals.dPeriodTotal + " = " + dAdjustedTotal + 
			  " WHERE" + 
			  	" " + TimeTotals.id + " = " + id;
	
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Retieve_Available_Periods_SQL(){
		String SQL = "SELECT DISTINCT " + TimeTotals.datPeriodEndDate + " FROM " + TimeTotals.TableName + " ORDER BY " + TimeTotals.datPeriodEndDate + " DESC";
	
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Late_Entries_SQL(String sManagerID, 
											  String sStartingDate, 
											  String sEndingDate,
											  String sEID, 
											  String sDID){
		String SQL = "SELECT DISTINCT " + Employees.TableName + "." + Employees.sEmployeeID + ", " +
							  " " + Employees.TableName + "." + Employees.sEmployeeFirstName + ", " +
							  " " + Employees.TableName + "." + Employees.sEmployeeMiddleName + "," +
							  " " + Employees.TableName + "." + Employees.sEmployeeLastName + "," +
							  " " + Employees.TableName + "." + Employees.tStartTime + "," +
							  " " + TimeEntries.TableName + "." + TimeEntries.dtInTime + "," +
							  " " + TimeEntries.TableName + "." + TimeEntries.dtOutTime + "," +
							  " " + TimeEntries.TableName + "." + TimeEntries.iLateMinute + "," +
							  " " + Departments.TableName + "." + Departments.sDeptDesc +		
			  " FROM " + TimeEntries.TableName + ", " + 
			  			 Employees.TableName + ", " + 
			  			 TCSDataDefinition.ManagerAccessControlDefinitions.TableName + ", " + 
			  			 Departments.TableName + 
			  " WHERE" +
				" " + TimeEntries.TableName + "." + TimeEntries.sEmployeeID + " = " + 
					  Employees.TableName + "." + Employees.sEmployeeID +
			  " AND" + 
			  	" " + Employees.TableName + "." + Employees.iDepartmentID + " = " + 
			  		  Departments.TableName + "." + Departments.iDeptID + 
			  " AND" + 
				" " + TimeEntries.TableName + "." + TimeEntries.iLate + " = 1" ;
		if (sManagerID.compareTo("ADMIN") != 0){
			SQL = SQL + " AND " + TCSDataDefinition.ManagerAccessControlDefinitions.TableName + "." + 
								  TCSDataDefinition.ManagerAccessControlDefinitions.sManagerID + " = '" + sManagerID + "'" + 
			  			" AND " + TCSDataDefinition.ManagerAccessControlDefinitions.TableName + "." + 
			  					  TCSDataDefinition.ManagerAccessControlDefinitions.iDepartmentID + " = " + 
			  					  Employees.TableName + "." + Employees.iDepartmentID;
		}
		if (sEID.compareTo("") != 0){
			SQL = SQL + " AND " + Employees.TableName + "." + Employees.sEmployeeID + " = '" + sEID + "'";
		}else{
			if (sDID.compareTo("0") != 0){
				SQL = SQL + " AND " + Employees.TableName + "." + Employees.iDepartmentID + " = " + sDID;
			}
		}
			SQL = SQL + " AND" + 
				" " + TimeEntries.TableName + "." + TimeEntries.dtInTime + " >= \"" + sStartingDate + "\" AND " + 
					  TimeEntries.TableName + "." + TimeEntries.dtInTime + " < \"" + sEndingDate + "\"" +
				
				" ORDER BY" + 
				" " + Employees.TableName + "." + Employees.iDepartmentID + ", " + 
					  Employees.TableName + "." + Employees.sEmployeeID + ", " + 
					  TimeEntries.TableName + "." + TimeEntries.dtInTime;
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_On_Clock_Employee_List_SQL(String sManagerID,
														String sDID){
		String SQL = "SELECT DISTINCT " + TimeEntries.TableName + "." + TimeEntries.dtInTime + "," +
					" " + TimeEntries.TableName + "." + TimeEntries.dtOutTime + "," +
					" " + Employees.TableName + "." + Employees.sEmployeeFirstName + "," +
					" " + Employees.TableName + "." + Employees.sEmployeeMiddleName + "," +
					" " + Employees.TableName + "." + Employees.sEmployeeLastName + "," +
					" " + Departments.TableName + "." + Departments.sDeptDesc +  
				" FROM " + TimeEntries.TableName + ", " + Employees.TableName + ", " + TCSDataDefinition.ManagerAccessControlDefinitions.TableName + ", " + Departments.TableName +
			  " WHERE" +
				" " + TimeEntries.TableName + "." + TimeEntries.sEmployeeID + " = " + 
					  Employees.TableName + "." + Employees.sEmployeeID +
			  " AND" + 
				" " + Employees.TableName + "." + Employees.iDepartmentID + " = " + 
					  Departments.TableName + "." + Departments.iDeptID;
		
		if (sManagerID.compareTo("ADMIN") != 0){ 
			  SQL = SQL + " AND " + TCSDataDefinition.ManagerAccessControlDefinitions.TableName + "." + TCSDataDefinition.ManagerAccessControlDefinitions.sManagerID + " = '" + sManagerID + "'" +
			  			  " AND " + TCSDataDefinition.ManagerAccessControlDefinitions.TableName + "." + TCSDataDefinition.ManagerAccessControlDefinitions.iDepartmentID + " = " + Employees.TableName + "." + Employees.iDepartmentID;
		} 
		  
		if (sDID.compareTo("0") != 0){ 
			  SQL = SQL + " AND" + 
				" " + Departments.TableName + "." + Departments.iDeptID + " = " + sDID;
		}
		
		SQL = SQL + " AND" + 
				" " + TimeEntries.TableName + "." + TimeEntries.dtInTime + " <> \"0000-00-00 00:00:00\" " +
			  "AND " +
			  	" " + TimeEntries.TableName + "." + TimeEntries.dtOutTime + " = \"0000-00-00 00:00:00\"" +
				
				" ORDER BY" + 
				" " + Departments.TableName + "." + Departments.iDeptID + ", " + 
					  Employees.TableName + "." + Employees.sEmployeeID + ", " + 
					  TimeEntries.TableName + "." + TimeEntries.dtInTime;
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Employee_Contact_Info_List_SQL(String sManagerID,
														String sDID, 
														String sSortBy){
		String SQL = "SELECT DISTINCT " + Employees.TableName + "." + Employees.sEmployeeID + "," +
					" " + Employees.TableName + "." + Employees.sEmployeeFirstName + "," +
					" " + Employees.TableName + "." + Employees.sEmployeeMiddleName + "," +
					" " + Employees.TableName + "." + Employees.sEmployeeLastName + "," +
					" " + Employees.TableName + "." + Employees.sExtension + "," +
					" " + Employees.TableName + "." + Employees.sOfficePhone + "," +
					" " + Employees.TableName + "." + Employees.sCellPhone + "," +
					" " + Employees.TableName + "." + Employees.sHomePhone + "," +
					" " + Employees.TableName + "." + Employees.sEmail + "," +
					" " + Employees.TableName + "." + Employees.sNextelDirectCall + "," + 
					" " + Departments.TableName + "." + Departments.iDeptID + "," + 
					" " + Departments.TableName + "." + Departments.sDeptDesc +  
			  " FROM " + Employees.TableName + ", " + Departments.TableName + ", " + TCSDataDefinition.ManagerAccessControlDefinitions.TableName + 
			  " WHERE " + Employees.TableName + "." + Employees.iDepartmentID + " = " + 
			  			  Departments.TableName + "." + Departments.iDeptID;
				
		
		if (sManagerID.compareTo("ADMIN") != 0){ 
			  SQL = SQL + " AND " + TCSDataDefinition.ManagerAccessControlDefinitions.TableName + "." + TCSDataDefinition.ManagerAccessControlDefinitions.sManagerID + " = '" + sManagerID + "'" +
			  			  " AND " + TCSDataDefinition.ManagerAccessControlDefinitions.TableName + "." + TCSDataDefinition.ManagerAccessControlDefinitions.iDepartmentID + " = " + 
			  			  			Employees.TableName + "." + Employees.iDepartmentID;
		} 
		  
		if (sDID.compareTo("0") != 0){ 
			  SQL = SQL + " AND" + 
				" " + Departments.TableName + "." + Departments.iDeptID + " = " + sDID;
		}
		if (sSortBy.compareTo("Department") == 0){
			SQL = SQL + " ORDER BY " + Departments.TableName + "." + Departments.iDeptID;
		}else if (sSortBy.compareTo("LastName") == 0){
			SQL = SQL + " ORDER BY " + Employees.TableName + "." + Employees.sEmployeeLastName;
		}else{
			SQL = SQL + " ORDER BY " + Employees.TableName + "." + Employees.sEmployeeLastName;
		}
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Lump_Sum_Detail_SQL(String sLeaveTypeID){
		//get a list of Lump Sum details associated with a particular type
		String SQL = "SELECT * FROM " + LeaveAdjustmentTypes.TableName  // + ", " + LeaveLumpSumRules.TableName +
//			  " WHERE" +
//			  	" " + LeaveAdjustmentTypes.TableName + "." + LeaveAdjustmentTypes.iTypeID + " = " + 
//			  		  LeaveLumpSumRules.TableName + "." + LeaveLumpSumRules.iTypeID +
//			  " AND" +
//			  	" " + LeaveLumpSumRules.TableName + "." + LeaveLumpSumRules.iTypeID + " = '" + sLeaveTypeID + "'" + 
//			  " ORDER BY" + 
//			  	" " + LeaveLumpSumRules.TableName + "." + LeaveLumpSumRules.dNumberOfMonths
			  	;
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}
	

	public static String Get_Change_Late_Flag_SQL(int iID, int iStatus){
		
		String SQL = "UPDATE " + TimeEntries.TableName + " SET " + TimeEntries.iLate + " = ";
		if (iStatus == 0){
			SQL = SQL + "1";
		}else{
			SQL = SQL + "0";
		}
		SQL = SQL + " WHERE" +
			  	" " + TimeEntries.id + " = " + iID;
		 	
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Manager_Access_Control_Info_SQL(){
		
		String SQL = "SELECT *" + 
			  " FROM " + TCSDataDefinition.ManagerAccessControlDefinitions.TableName + ", " + 
			  	 		 Employees.TableName + ", " + 
			  	 		 Departments.TableName + 
			  " WHERE " + TCSDataDefinition.ManagerAccessControlDefinitions.TableName + "." + TCSDataDefinition.ManagerAccessControlDefinitions.iDepartmentID + " = " + 
			  			  Departments.TableName + "." + Departments.iDeptID +
			  	" AND " + TCSDataDefinition.ManagerAccessControlDefinitions.TableName + "." + TCSDataDefinition.ManagerAccessControlDefinitions.sManagerID + " = " + 
			  			  Employees.TableName + "." + Employees.sEmployeeID + 
			  " ORDER BY " + TCSDataDefinition.ManagerAccessControlDefinitions.TableName + "." + TCSDataDefinition.ManagerAccessControlDefinitions.sManagerID + ", " + 
			  				 Departments.TableName + "." + Departments.sDeptDesc;
		 	
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Temp_Manager_Access_Control_By_Individual(String sManagerID){
		
		String SQL = "CREATE TEMPORARY TABLE IndividualMAC SELECT * FROM " + TCSDataDefinition.ManagerAccessControlDefinitions.TableName + " WHERE " + TCSDataDefinition.ManagerAccessControlDefinitions.sManagerID + " = '" + sManagerID + "'";
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Individual_Manager_Access_Control_Info_SQL(){
		
		String SQL = "SELECT *" + 
			  " FROM IndividualMAC RIGHT JOIN " + Departments.TableName + 
			  " ON IndividualMAC." + TCSDataDefinition.ManagerAccessControlDefinitions.iDepartmentID + " = " + Departments.iDeptID +
			  " ORDER BY " + Departments.sDeptDesc;
		 	
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Insert_Manager_Access_Control(String sMID, 
														   int iDeptID){
		String SQL = "INSERT INTO " + TCSDataDefinition.ManagerAccessControlDefinitions.TableName  +
					"(" + TCSDataDefinition.ManagerAccessControlDefinitions.sManagerID + ", " + TCSDataDefinition.ManagerAccessControlDefinitions.iDepartmentID + ") " + 
			  "VALUES " + 
			  		"('" + sMID + "', " + iDeptID + ")";
		 	
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Reset_Manager_Access_Control(String sMID){
		
		String SQL = "DELETE FROM " + TCSDataDefinition.ManagerAccessControlDefinitions.TableName +
			  " WHERE " + TCSDataDefinition.ManagerAccessControlDefinitions.sManagerID + " = '" + sMID + "'";
		 	
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Time_Entry_Type_Info_SQL(){
		
		String SQL = "SELECT * FROM " + SpecialEntryTypes.TableName;
	 	
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Time_Entry_Type_Info_SQL(String sTypeID){
		
		String SQL = "SELECT * FROM " + SpecialEntryTypes.TableName +
			  " WHERE " + SpecialEntryTypes.iTypeID + " = '" + sTypeID + "'";
		 	
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Remove_Time_Entry_Type_SQL(String sTID){
		String SQL = "DELETE FROM " + SpecialEntryTypes.TableName +
			  " WHERE " + SpecialEntryTypes.iTypeID + " = '" + sTID + "'";
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Insert_Time_Entry_Type_SQL(int iSessionTID, 
											  		    String sTypeTitle, 
													    String sTypeDesc,
													    int iWorkTime){
		String SQL = "INSERT INTO " + SpecialEntryTypes.TableName + "(" + 
				 " " + SpecialEntryTypes.iTypeID + "," +  
				 " " + SpecialEntryTypes.sTypeTitle + "," +
				 " " + SpecialEntryTypes.sTypeDesc + "," +
				 " " + SpecialEntryTypes.iWorkTime + ")" + 
			  "VALUES(" +
			  	 " " + iSessionTID + "," +
			  	 " '" + clsDatabaseFunctions.FormatSQLStatement(sTypeTitle) + "'," +
			  	 " '" + clsDatabaseFunctions.FormatSQLStatement(sTypeDesc) + "'," +
			  	 " " + iWorkTime + ")";
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Update_Time_Entry_Type_SQL(int iSessionTID, 
											  		    String sTypeTitle, 
													    String sTypeDesc,
													    int iWorkTime){
		
		String SQL = "UPDATE " + SpecialEntryTypes.TableName + " SET" +
		 		 " " + SpecialEntryTypes.sTypeTitle + " = '" + clsDatabaseFunctions.FormatSQLStatement(sTypeTitle) + "'," +
				 " " + SpecialEntryTypes.sTypeDesc + " = '" + clsDatabaseFunctions.FormatSQLStatement(sTypeDesc) + "'," +
				 " " + SpecialEntryTypes.iWorkTime + " = " + iWorkTime + 
			  " WHERE" +
			 	 " " + SpecialEntryTypes.iTypeID + " = " + iSessionTID;
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Pay_Type_Info_SQL(){
		
		String SQL = "SELECT * FROM " + PayTypes.TableName;
	 	
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Pay_Type_Info_SQL(String sTypeID){
		
		String SQL = "SELECT * FROM " + PayTypes.TableName +
			  " WHERE " + PayTypes.iTypeID + " = '" + sTypeID + "'";
		 	
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Remove_Pay_Type_SQL(String sTID){
		String SQL = "DELETE FROM " + PayTypes.TableName +
			  " WHERE " + PayTypes.iTypeID + " = '" + sTID + "'";
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Insert_Pay_Type_SQL(int iSessionTID, 
											  	 String sTypeTitle, 
												 String sTypeDesc){
		String SQL = "INSERT INTO " + PayTypes.TableName + " (" + 
				 " " + PayTypes.iTypeID + "," +  
				 " " + PayTypes.sTypeTitle + "," +
				 " " + PayTypes.sTypeDesc + ")" + 
			  "VALUES(" +
			  	 " " + iSessionTID + "," +
			  	 " '" + clsDatabaseFunctions.FormatSQLStatement(sTypeTitle) + "'," +
			  	 " '" + clsDatabaseFunctions.FormatSQLStatement(sTypeDesc) + "')";
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Update_Pay_Type_SQL(int iSessionTID, 
											  	 String sTypeTitle, 
												 String sTypeDesc){
		
		String SQL = "UPDATE " + PayTypes.TableName + " SET" +
		 		 " " + PayTypes.sTypeTitle + " ='" + clsDatabaseFunctions.FormatSQLStatement(sTypeTitle) + "'," +
				 " " + PayTypes.sTypeDesc + " ='" + clsDatabaseFunctions.FormatSQLStatement(sTypeDesc) + "'" +
			  " WHERE" +
			 	 " " + PayTypes.iTypeID + " = " + iSessionTID;
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Employee_Status_Info_SQL(){
		
		String SQL = "SELECT * FROM " + EmployeeStatus.TableName;
	 	
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Employee_Status_Info_SQL(String sStatusID){
		
		String SQL = "SELECT * FROM " + EmployeeStatus.TableName +
			  " WHERE " + EmployeeStatus.iStatusID+ " = '" + sStatusID + "'";
		 	
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Remove_Employee_Status_SQL(String sSID){
		String SQL = "DELETE FROM " + EmployeeStatus.TableName +
			  " WHERE " + EmployeeStatus.iStatusID + " = '" + sSID + "'";
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Insert_Employee_Status_SQL(int iSessionSID, 
											  		    String sStatusTitle, 
													    String sStatusDesc){
		String SQL = "INSERT INTO " + EmployeeStatus.TableName + "(" + 
				 "  " + EmployeeStatus.iStatusID + "," +  
				 "  " + EmployeeStatus.sStatusTitle + "," +
				 "  " + EmployeeStatus.sStatusDesc + ")" + 
			  "VALUES(" +
			  	 " " + iSessionSID + "," +
			  	 " '" + clsDatabaseFunctions.FormatSQLStatement(sStatusTitle) + "'," +
			  	 " '" + clsDatabaseFunctions.FormatSQLStatement(sStatusDesc) + "')";
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Update_Employee_Status_SQL(int iSessionSID, 
											  		    String sStatusTitle, 
													    String sStatusDesc){
		
		String SQL = "UPDATE " + EmployeeStatus.TableName + " SET" +
		 		 " " + EmployeeStatus.sStatusTitle + " ='" + clsDatabaseFunctions.FormatSQLStatement(sStatusTitle) + "'," +
				 " " + EmployeeStatus.sStatusDesc + " ='" + clsDatabaseFunctions.FormatSQLStatement(sStatusDesc) + "'" +
			  " WHERE" +
			 	 " " + EmployeeStatus.iStatusID + " = " + iSessionSID;
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Leave_Adjustments_SQL(String sEID, 
												   ArrayList<String> alTypes,
												   String sStartDate,
												   String sEndDate,
												   String sSortBy,
												   int iSortOrder,
												   boolean bIncludeSpecialAdjustment
												   ){
	
		String SQL = "SELECT * FROM " + LeaveAdjustments.TableName + ", " + LeaveAdjustmentTypes.TableName +
			  " WHERE " + LeaveAdjustments.TableName + "." + LeaveAdjustments.iLeaveTypeID + " = " + LeaveAdjustmentTypes.TableName + "." + LeaveAdjustmentTypes.iTypeID +
			  " AND" +
			  " " + LeaveAdjustments.TableName + "." + LeaveAdjustments.sEmployeeID + " = '" + sEID + "'" +
			  " AND" +
			  " " + LeaveAdjustments.TableName + "." + LeaveAdjustments.dtInTime + " >= '" + sStartDate + "'" + 
			  " AND" +
			  " " + LeaveAdjustments.TableName + "." + LeaveAdjustments.dtOutTime + " <= '" + sEndDate + "'";
		
		if (!bIncludeSpecialAdjustment){
			SQL = SQL + " AND " +  LeaveAdjustments.TableName + "." + LeaveAdjustments.iSpecialAdjustment + " = 0"; 
		}
		
		if (alTypes.get(0).toString().compareTo("ALLTYPES") != 0){
			SQL = SQL + " AND (" + LeaveAdjustmentTypes.TableName + "." + LeaveAdjustmentTypes.iTypeID + " = " + alTypes.get(0).toString();
			for (int i=1;i<alTypes.size();i++){
				SQL = SQL + " OR " + LeaveAdjustmentTypes.TableName + "." + LeaveAdjustmentTypes.iTypeID + " = " + alTypes.get(i).toString();
			}
			SQL = SQL + ")";
		}
		SQL = SQL + " ORDER BY " + LeaveAdjustments.TableName + "." + sSortBy; 
		
		if (iSortOrder == 1){
			SQL = SQL + " DESC";
		}
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Leave_Adjustments_SQL(String sEID, 
												   int iTID,
												   String sStartDate,
												   String sEndDate,
												   String sSortBy,
												   int iSortOrder, 
												   boolean bIncludeFinalized){
		
		//System.out.println("String sStartDate = " + sStartDate);
		String SQL = "SELECT * FROM " + LeaveAdjustments.TableName + ", " + LeaveAdjustmentTypes.TableName +
			  " WHERE " + LeaveAdjustments.iLeaveTypeID + " = " + LeaveAdjustmentTypes.iTypeID +
			  " AND" +
			  " " + LeaveAdjustments.sEmployeeID + " = '" + sEID + "'" +
			  " AND" + 
				" ((" + LeaveAdjustments.dtInTime + " >= '" + sStartDate + "' AND " + LeaveAdjustments.dtInTime + " <= '" + sEndDate + "') OR" +
				" (" + LeaveAdjustments.dtOutTime + " >= '" + sStartDate + "' AND " + LeaveAdjustments.dtOutTime + " <= '" + sEndDate + "'))";
		
		  if (iTID >= 0){
			  SQL = SQL + " AND (" + LeaveAdjustments.iLeaveTypeID + " = " + iTID + ")";
		  }
		  
		  if (!bIncludeFinalized){
			  SQL = SQL + " AND (" 
					  + "(" + LeaveAdjustments.sPeriodDate + " = '" + TimeCardUtilities.TEMPORARY_POSTING_DATE + "')" +
					  " OR " + "(" + LeaveAdjustments.sPeriodDate + " = '0000-00-00')"
			  + ")";
		  }
		
		SQL = SQL + " ORDER BY " + LeaveAdjustments.TableName + "." + sSortBy; 
		
		if (iSortOrder == 1){
			SQL = SQL + " DESC";
		}
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Leave_Adjustment_SQL(int iID){
		
		String SQL = "SELECT * FROM " + LeaveAdjustments.TableName + " WHERE id = " + iID;
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Leave_Adjustment_Types_SQL(){
		
		String SQL = "SELECT * FROM " + LeaveAdjustmentTypes.TableName + " ORDER BY " + LeaveAdjustmentTypes.iTypeID;
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Leave_Adjustment_Types_SQL(ArrayList<String> sTIDs){
		
		String SQL = "SELECT * FROM " + LeaveAdjustmentTypes.TableName;
		
		if (sTIDs.get(0).toString().compareTo("ALLTYPES") != 0){
			SQL = SQL + " WHERE" + 
		  	" " + LeaveAdjustmentTypes.iTypeID + " = " + sTIDs.get(0).toString();
			for (int i=1;i<sTIDs.size();i++){
				SQL = SQL + " OR " + LeaveAdjustmentTypes.iTypeID + " = " + sTIDs.get(i).toString();
			}
		}
		
		SQL = SQL + " ORDER BY " + LeaveAdjustmentTypes.iTypeID;
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Leave_Adjustment_Type_Info_SQL(String sTypeID){
		
		String SQL = "SELECT * FROM " + LeaveAdjustmentTypes.TableName + " WHERE " + LeaveAdjustmentTypes.iTypeID + " = " + sTypeID;
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Leave_Adjustment_Type_Info_SQL(int iTypeID){
		
		String SQL = "SELECT * FROM " + LeaveAdjustmentTypes.TableName + " WHERE " + LeaveAdjustmentTypes.iTypeID + " = " + iTypeID;
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Remove_Leave_Adjustment_Type_SQL(String sTID){
		String SQL = "DELETE FROM " + LeaveAdjustmentTypes.TableName +
			  " WHERE " + LeaveAdjustmentTypes.iTypeID + " = '" + sTID + "'";
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Insert_Leave_Adjustment_Type_SQL(int iSessionTID, 
  		  String sTypeTitle, 
		  String sTypeDesc
	      ){
		String SQL = "INSERT INTO " + LeaveAdjustmentTypes.TableName + " (" + 
				 " " + LeaveAdjustmentTypes.iTypeID + "," +  
				 " " + LeaveAdjustmentTypes.sTypeTitle + "," +
				 " " + LeaveAdjustmentTypes.sTypeDesc +
				 " )" + 
			  "VALUES(" +
			  	 " " + iSessionTID + "," +
			  	 " '" + clsDatabaseFunctions.FormatSQLStatement(sTypeTitle) + "'," +
			  	 " '" + clsDatabaseFunctions.FormatSQLStatement(sTypeDesc) + "'" + 
			  	 ")";
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Update_Leave_Adjustment_Type_SQL(
		int iSessionTID, 
  		String sTypeTitle, 
		String sTypeDesc
	    ){
	
		String SQL = "UPDATE " + LeaveAdjustmentTypes.TableName + " SET" +
		 		 " " + LeaveAdjustmentTypes.sTypeTitle + " = '" + clsDatabaseFunctions.FormatSQLStatement(sTypeTitle) + "'," +
				 " " + LeaveAdjustmentTypes.sTypeDesc + " = '" + clsDatabaseFunctions.FormatSQLStatement(sTypeDesc) + "'" +
			  " WHERE" +
			 	 " " + LeaveAdjustmentTypes.iTypeID + " = " + iSessionTID;
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Remove_Leave_Adjustment(int id){
		//remove all lump sum details for a Leave Type.
		//this is used when an entire leave time is removed.
		String SQL = "DELETE FROM " + LeaveAdjustments.TableName + " WHERE " + LeaveAdjustments.id + " = " + id;
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Insert_Leave_Adjustment_SQL(String sEID,
														 int iTID,
														 String sStartDate,
														 String sEndDate,
														 double dDuration,
														 int iSA,
														 String sNote){
		String SQL = "INSERT INTO " + LeaveAdjustments.TableName + " (" + 
				 " " + LeaveAdjustments.sEmployeeID + "," +  
				 " " + LeaveAdjustments.iLeaveTypeID + "," +
				 " " + LeaveAdjustments.dtInTime + "," +
				 " " + LeaveAdjustments.dtOutTime + "," +
				 " " + LeaveAdjustments.dDuration + "," +
				 " " + LeaveAdjustments.iSpecialAdjustment + "," +
				 " " + LeaveAdjustments.mNote + ")" + 
			  "VALUES(" +
			  	 " '" + sEID + "'," +
			  	 " " + iTID + "," +
			  	 " \"" + sStartDate + "\"," +
			  	 " \"" + sEndDate + "\"," +
			  	 " " + dDuration + ", " +
			  	 " " + iSA + ", " +
			  	 " '" + sNote + "')";
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Update_Leave_Adjustment_SQL(int id,
														 String sEID,
														 int iTID,
														 String sStartDate,
														 String sEndDate,
														 double dDuration,
														 int iSA,
														 String sNote){
		String SQL = "UPDATE " + LeaveAdjustments.TableName + " SET" +
		 		 " " + LeaveAdjustments.sEmployeeID + " = '" + sEID + "'," +
				 " " + LeaveAdjustments.iLeaveTypeID + " = " + iTID + "," +
				 " " + LeaveAdjustments.dtInTime + " = \"" + sStartDate + "\"," +
				 " " + LeaveAdjustments.dtOutTime + " = \"" + sEndDate + "\"," +
				 " " + LeaveAdjustments.dDuration + " = " + dDuration + "," +
				 " " + LeaveAdjustments.iSpecialAdjustment + " = " + iSA + "," +
				 " " + LeaveAdjustments.mNote + " = '" + sNote + "'" + 
			  " WHERE" +
			 	 " " + LeaveAdjustments.id + " = " + id;
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Milestone_SQL(String sMilestoneID){
		
		String SQL = "SELECT * FROM " + TCSTableMilestones.TableName 
			
				+ " WHERE " + TCSTableMilestones.lid + " = " + sMilestoneID
			;		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Insert_Vacation_Day_SQL(String sEmpID, 
													 Date datDate 
													 ){
		String SQL = "INSERT INTO " + LeaveAdjustments.TableName + " (" + 
				" " + LeaveAdjustments.sEmployeeID + "," +
				" " + LeaveAdjustments.iLeaveTypeID + "," + 
				" " + LeaveAdjustments.dtInTime + "," +
				" " + LeaveAdjustments.dtOutTime + "," +
				" " + LeaveAdjustments.dDuration + "," +
				" " + LeaveAdjustments.iSpecialAdjustment + "," +
				" " + LeaveAdjustments.mNote +
			  ") VALUES (" +
				"'" + sEmpID + "'," + 
				" 1," +
				"'" + datDate +  "'," +
				"'" + datDate +  "'," +
				" 8," +
				" 0," +
				" 'Recovered from a missing day.'" +  
				")";
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Add_New_Column_SQL(String sTableName, String sColumnName, int sType){
		
		String SQL = "ALTER TABLE " + sTableName + " ADD COLUMN `" + sColumnName + "`";
		
		switch (sType){
		case Types.VARCHAR:	SQL = SQL + " varchar(50) default ''";break;
		case Types.DATE:	SQL = SQL + " date default '1900-01-01'"; break;
		case Types.TIME:	SQL = SQL + " time default '00:00:00'"; break;
		case Types.DOUBLE: SQL = SQL + " double default 0"; break;
		case Types.INTEGER: SQL = SQL + " int(1) default 0"; break;
		}
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Update_Column_SQL(String sTableName, String sColumnOldName, String sColumnNewName, int sType){
		
		String SQL = "ALTER TABLE " + sTableName + " CHANGE `" + sColumnOldName + "` `" + sColumnNewName + "`";
		
		switch (sType){
		case Types.VARCHAR:	SQL = SQL + " varchar(50) default ''";break;
		case Types.DATE:	SQL = SQL + " date default '1900-01-01'"; break;
		case Types.TIME:	SQL = SQL + " time default '00:00:00'"; break;
		case Types.DOUBLE: SQL = SQL + " double default 0"; break;
		case Types.INTEGER: SQL = SQL + " int(1) default 0"; break;
		}
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

	public static String Get_Remove_Column_SQL(String sTableName, String sColumnName){
		
		String SQL = "ALTER TABLE " + sTableName + " DROP COLUMN `" + sColumnName + "`";
		
		//System.out.println ("SQL = " + SQL);
		return SQL;
	}

}
