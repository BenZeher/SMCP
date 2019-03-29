package smgl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.servlet.ServletContext;

import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableglaccountgroups;
import SMDataDefinition.SMTableglaccounts;
import SMDataDefinition.SMTableglaccountstructures;
import SMDataDefinition.SMTableglfinancialstatementdata;
import ServletUtilities.clsDatabaseFunctions;

public class GLTransactionListingReport  extends java.lang.Object{

	public GLTransactionListingReport(){
		
	}
	
	public String processReport(
		Connection conn,
		String sDBID,
		ServletContext context,
		String sStartingAccount,
		String sEndingAccount,
		String sStartingAccountGroup,
		String sEndingAccountGroup,
		String sStartingFiscalPeriod,
		String sEndingFiscalPeriod,
		boolean bIncludeAccountsWithNoActivity,
		ArrayList<String>alStartingSegmentIDs,
		ArrayList<String>alStartingSegmentValueDescriptions,
		ArrayList<String>alEndingSegmentIDs,
		ArrayList<String>alEndingSegmentValueDescriptions
		) throws Exception{
		
		String s = "";

		
		s += printTableHeading();
		s += buildTransactionListingReport(
			sStartingFiscalPeriod,
			sEndingFiscalPeriod,
			sStartingAccount,
			sEndingAccount,
			sStartingAccountGroup,
			sEndingAccountGroup,
			bIncludeAccountsWithNoActivity,
			alStartingSegmentIDs,
			alStartingSegmentValueDescriptions,
			alEndingSegmentIDs,
			alEndingSegmentValueDescriptions,
			conn,
			sDBID, 
			context
		);			

		s += printTableFooting();
		
		return s;
	}
	
	private String buildTransactionListingReport(
		String sStartingFiscalPeriod,
		String sEndingFiscalPeriod,
		String sStartingAccount,
		String sEndingAccount,
		String sStartingAccountGroupCode,
		String sEndingAccountGroupCode,
		boolean bIncludeAccountsWithNoActivity,
		ArrayList<String>alStartingSegmentIDs,
		ArrayList<String>alStartingSegmentValueDescriptions,
		ArrayList<String>alEndingSegmentIDs,
		ArrayList<String>alEndingSegmentValueDescriptions,
		Connection conn,
		String sDBID, 
		ServletContext context) throws Exception{
		
		String s = "";
		
		s += printColumnHeadings();

		String sStartingYear = sStartingFiscalPeriod.substring(0, sStartingFiscalPeriod.indexOf(GLTransactionListingSelect.PARAM_VALUE_DELIMITER));
		String sStartingPeriod = sStartingFiscalPeriod.replace(sStartingYear + GLTransactionListingSelect.PARAM_VALUE_DELIMITER, "");
		String sEndingYear = sEndingFiscalPeriod.substring(0, sEndingFiscalPeriod.indexOf(GLTransactionListingSelect.PARAM_VALUE_DELIMITER));
		String sEndingPeriod = sEndingFiscalPeriod.replace(sEndingYear + GLTransactionListingSelect.PARAM_VALUE_DELIMITER, "");
		
		String sSQL = "SELECT" + "\n"
			+ " " + SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.sacctid + "\n"
			+ ", (" + SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.bdtotalyeartodate + "\n" 
				+ " + " + SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.bdopeningbalance
				+ ") AS CURRENTBALANCE" + "\n"
			+ ", " + SMTableglaccounts.TableName + "." + SMTableglaccounts.sDesc + "\n"
			+ ", " + SMTableglaccounts.TableName + "." + SMTableglaccounts.inormalbalancetype + "\n"
			+ ", " + SMTableglaccounts.TableName + "." + SMTableglaccounts.sAcctType + "\n"
			+ " FROM " + SMTableglfinancialstatementdata.TableName + "\n"
			+ " LEFT JOIN " + SMTableglaccounts.TableName + "\n" 
			+ " ON " + SMTableglaccounts.TableName + "." + SMTableglaccounts.sAcctID
			+ " = " + SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.sacctid + "\n"
			+ " LEFT JOIN " + SMTableglaccountgroups.TableName + "\n"
			+ " ON " + SMTableglaccounts.TableName + "." + SMTableglaccounts.laccountgroupid 
				+ " = " + SMTableglaccountgroups.TableName + "." + SMTableglaccountgroups.lid + "\n"
			+ " LEFT JOIN " + SMTableglaccountstructures.TableName + "\n"
			+ " ON " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.lid + " = "
			+ SMTableglaccounts.TableName + "." + SMTableglaccounts.lstructureid + "\n"
			+ " WHERE (" + "\n"
				+ "(" + SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.ifiscalyear + " >= " + sStartingYear + ")" + "\n"
				+ " AND (" + SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.ifiscalperiod + " >= " + sStartingPeriod + ")" + "\n"
				
				//Account range:
				+ " AND (" + SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.sacctid + " >= '" + sStartingAccount + "')" + "\n"
				+ " AND (" + SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.sacctid + " <= '" + sEndingAccount + "')" + "\n"
				
				//Account group range:
				+ " AND (" + SMTableglaccountgroups.TableName + "." + SMTableglaccountgroups.sgroupcode + " >= '" + sStartingAccountGroupCode + "')" + "\n"
				+ " AND (" + SMTableglaccountgroups.TableName + "." + SMTableglaccountgroups.sgroupcode + " <= '" + sEndingAccountGroupCode + "')" + "\n"
			;
		
			//Include accounts with no activity?
			if(!bIncludeAccountsWithNoActivity){
				sSQL += " AND ((" + SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.bdtotalyeartodate
					+ " + " + SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.bdopeningbalance
					+ ")  != 0.00)" + "\n";
			}
			
			//Now process the segments:

			for (int i = 0; i < alStartingSegmentIDs.size(); i++){

				//SEGMENT 1:
				//Check the starting value:
				sSQL += " AND ("
					//IF the segments match then...
					+ "IF (" + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.lsegmentid1 + " = " + alStartingSegmentIDs.get(i)
					
					//THEN, if the segment in the account number >= the starting selected value for the segment:
			 		+ ", IF(SUBSTRING(" 
				 			+ SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.sacctid
				 			+ ", " 
				 			+ "1, " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength1 + ") >= '" + alStartingSegmentValueDescriptions.get(i) + "'"
				 	//IF the segment value if the account is greater then or equal to the selected starting value, then qualify the record:
				 	//If not, the DISqualify the record:
				 	+ ", TRUE"
					+ ", FALSE"
					+ ")"
					//If this ISN'T the segment we're looking for, then don't disqualify the record:
					+ ", TRUE"
					+ ")"
				+ ")" + "\n"
				;

				//Check the ending value:
				sSQL += " AND ("
					//IF the segments match then...
					+ "IF (" + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.lsegmentid1 + " = " + alEndingSegmentIDs.get(i)
					
					//THEN, if the segment in the account number >= the starting selected value for the segment:
			 		+ ", IF(SUBSTRING(" 
				 			+ SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.sacctid
				 			+ ", " 
				 			+ "1, " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength1 + ") <= '" + alEndingSegmentValueDescriptions.get(i) + "'"
				 	//IF the segment value if the account is greater then or equal to the selected starting value, then qualify the record:
				 	//If not, the DISqualify the record:
				 	+ ", TRUE"
					+ ", FALSE"
					+ ")"
					//If this ISN'T the segment we're looking for, then don't disqualify the record:
					+ ", TRUE"
					+ ")"
				+ ")" + "\n"
				;

				//SEGMENT 2:
				//Check the starting value:
				sSQL += " AND ("
					//IF the segments match then...
					+ "IF (" + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.lsegmentid2 + " = " + alStartingSegmentIDs.get(i)
					
					//THEN, if the segment in the account number >= the starting selected value for the segment:
			 		+ ", IF(SUBSTRING(" 
				 			+ SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.sacctid
				 			+ ", " 
				 			+ "1 + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength1
				 			+ ", " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength2 + ") >= '" + alStartingSegmentValueDescriptions.get(i) + "'"
				 	//IF the segment value if the account is greater then or equal to the selected starting value, then qualify the record:
				 	//If not, the DISqualify the record:
				 	+ ", TRUE"
					+ ", FALSE"
					+ ")"
					//If this ISN'T the segment we're looking for, then don't disqualify the record:
					+ ", TRUE"
					+ ")"
				+ ")" + "\n"
				;

				//Check the ending value:
				sSQL += " AND ("
					//IF the segments match then...
					+ "IF (" + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.lsegmentid1 + " = " + alEndingSegmentIDs.get(i)
					
					//THEN, if the segment in the account number >= the starting selected value for the segment:
			 		+ ", IF(SUBSTRING(" 
				 			+ SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.sacctid
				 			+ ", " 
				 			+  "1 + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength1
				 			+ ", " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength2 + ") <= '" + alEndingSegmentValueDescriptions.get(i) + "'"
				 	//IF the segment value if the account is greater then or equal to the selected starting value, then qualify the record:
				 	//If not, the DISqualify the record:
				 	+ ", TRUE"
					+ ", FALSE"
					+ ")"
					//If this ISN'T the segment we're looking for, then don't disqualify the record:
					+ ", TRUE"
					+ ")"
				+ ")" + "\n"
				;
				
				//SEGMENT 3:
				//Check the starting value:
				sSQL += " AND ("
					//IF the segments match then...
					+ "IF (" + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.lsegmentid3 + " = " + alStartingSegmentIDs.get(i)
					
					//THEN, if the segment in the account number >= the starting selected value for the segment:
			 		+ ", IF(SUBSTRING(" 
				 			+ SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.sacctid
				 			+ ", " 
				 			+  "1 + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength1
				 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength2
				 			+ ", " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength3 + ") >= '" + alStartingSegmentValueDescriptions.get(i) + "'"
				 	//IF the segment value if the account is greater then or equal to the selected starting value, then qualify the record:
				 	//If not, the DISqualify the record:
				 	+ ", TRUE"
					+ ", FALSE"
					+ ")"
					//If this ISN'T the segment we're looking for, then don't disqualify the record:
					+ ", TRUE"
					+ ")"
				+ ")" + "\n"
				;

				//Check the ending value:
				sSQL += " AND ("
					//IF the segments match then...
					+ "IF (" + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.lsegmentid3 + " = " + alEndingSegmentIDs.get(i)
					
					//THEN, if the segment in the account number >= the starting selected value for the segment:
			 		+ ", IF(SUBSTRING(" 
				 			+ SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.sacctid
				 			+ ", " 
				 			 + "1 + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength1
			 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength2
				 			+ ", " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength3 + ") <= '" + alEndingSegmentValueDescriptions.get(i) + "'"
				 	//IF the segment value if the account is greater then or equal to the selected starting value, then qualify the record:
				 	//If not, the DISqualify the record:
				 	+ ", TRUE"
					+ ", FALSE"
					+ ")"
					//If this ISN'T the segment we're looking for, then don't disqualify the record:
					+ ", TRUE"
					+ ")"
				+ ")" + "\n"
				;
				
				//SEGMENT 4:
				//Check the starting value:
				sSQL += " AND ("
					//IF the segments match then...
					+ "IF (" + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.lsegmentid4 + " = " + alStartingSegmentIDs.get(i)
					
					//THEN, if the segment in the account number >= the starting selected value for the segment:
			 		+ ", IF(SUBSTRING(" 
				 			+ SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.sacctid
				 			+ ", " 
				 			+ "1 + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength1
				 			+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength2
				 			+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength3
				 			+ ", " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength4 + ") >= '" + alStartingSegmentValueDescriptions.get(i) + "'"
				 	//IF the segment value if the account is greater then or equal to the selected starting value, then qualify the record:
				 	//If not, the DISqualify the record:
				 	+ ", TRUE"
					+ ", FALSE"
					+ ")"
					//If this ISN'T the segment we're looking for, then don't disqualify the record:
					+ ", TRUE"
					+ ")"
				+ ")" + "\n"
				;

				//Check the ending value:
				sSQL += " AND ("
					//IF the segments match then...
					+ "IF (" + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.lsegmentid4 + " = " + alEndingSegmentIDs.get(i)
					
					//THEN, if the segment in the account number >= the starting selected value for the segment:
			 		+ ", IF(SUBSTRING(" 
				 			+ SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.sacctid
				 			+ ", " 
				 			+ "1 + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength1
			 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength2
			 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength3
				 			+ ", " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength4 + ") <= '" + alEndingSegmentValueDescriptions.get(i) + "'"
				 	//IF the segment value if the account is greater then or equal to the selected starting value, then qualify the record:
				 	//If not, the DISqualify the record:
				 	+ ", TRUE"
					+ ", FALSE"
					+ ")"
					//If this ISN'T the segment we're looking for, then don't disqualify the record:
					+ ", TRUE"
					+ ")"
				+ ")" + "\n"
				;
				
				//SEGMENT 5:
				//Check the starting value:
				sSQL += " AND ("
					//IF the segments match then...
					+ "IF (" + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.lsegmentid5 + " = " + alStartingSegmentIDs.get(i)
					
					//THEN, if the segment in the account number >= the starting selected value for the segment:
			 		+ ", IF(SUBSTRING(" 
				 			+ SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.sacctid
				 			+ ", " 
				 			+ "1 + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength1
				 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength2
				 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength3
				 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength4
				 			+ ", " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength5 + ") >= '" + alStartingSegmentValueDescriptions.get(i) + "'"
				 	//IF the segment value if the account is greater then or equal to the selected starting value, then qualify the record:
				 	//If not, the DISqualify the record:
				 	+ ", TRUE"
					+ ", FALSE"
					+ ")"
					//If this ISN'T the segment we're looking for, then don't disqualify the record:
					+ ", TRUE"
					+ ")"
				+ ")" + "\n"
				;

				//Check the ending value:
				sSQL += " AND ("
					//IF the segments match then...
					+ "IF (" + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.lsegmentid5 + " = " + alEndingSegmentIDs.get(i)
					
					//THEN, if the segment in the account number >= the starting selected value for the segment:
			 		+ ", IF(SUBSTRING(" 
				 			+ SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.sacctid
				 			+ ", " 
				 			+ "1 + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength1
			 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength2
			 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength3
			 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength4
				 			+ ", " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength5 + ") <= '" + alEndingSegmentValueDescriptions.get(i) + "'"
				 	//IF the segment value if the account is greater then or equal to the selected starting value, then qualify the record:
				 	//If not, the DISqualify the record:
				 	+ ", TRUE"
					+ ", FALSE"
					+ ")"
					//If this ISN'T the segment we're looking for, then don't disqualify the record:
					+ ", TRUE"
					+ ")"
				+ ")" + "\n"
				;
				
				//SEGMENT 6:
				//Check the starting value:
				sSQL += " AND ("
					//IF the segments match then...
					+ "IF (" + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.lsegmentid6 + " = " + alStartingSegmentIDs.get(i)
					
					//THEN, if the segment in the account number >= the starting selected value for the segment:
			 		+ ", IF(SUBSTRING(" 
				 			+ SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.sacctid
				 			+ ", " 
				 			+ "1 + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength1
				 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength2
				 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength3
				 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength4
				 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength5
				 			+ ", " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength6 + ") >= '" + alStartingSegmentValueDescriptions.get(i) + "'"
				 	//IF the segment value if the account is greater then or equal to the selected starting value, then qualify the record:
				 	//If not, the DISqualify the record:
				 	+ ", TRUE"
					+ ", FALSE"
					+ ")"
					//If this ISN'T the segment we're looking for, then don't disqualify the record:
					+ ", TRUE"
					+ ")"
				+ ")" + "\n"
				;

				//Check the ending value:
				sSQL += " AND ("
					//IF the segments match then...
					+ "IF (" + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.lsegmentid6 + " = " + alEndingSegmentIDs.get(i)
					
					//THEN, if the segment in the account number >= the starting selected value for the segment:
			 		+ ", IF(SUBSTRING(" 
				 			+ SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.sacctid
				 			+ ", " 
				 			+ "1 + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength1
			 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength2
			 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength3
			 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength4
			 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength5
				 			+ ", " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength6 + ") <= '" + alEndingSegmentValueDescriptions.get(i) + "'"
				 	//IF the segment value if the account is greater then or equal to the selected starting value, then qualify the record:
				 	//If not, the DISqualify the record:
				 	+ ", TRUE"
					+ ", FALSE"
					+ ")"
					//If this ISN'T the segment we're looking for, then don't disqualify the record:
					+ ", TRUE"
					+ ")"
				+ ")" + "\n"
				;
				
				//SEGMENT 7:
				//Check the starting value:
				sSQL += " AND ("
					//IF the segments match then...
					+ "IF (" + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.lsegmentid7 + " = " + alStartingSegmentIDs.get(i)
					
					//THEN, if the segment in the account number >= the starting selected value for the segment:
			 		+ ", IF(SUBSTRING(" 
				 			+ SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.sacctid
				 			+ ", " 
				 			+ "1 + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength1
				 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength2
				 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength3
				 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength4
				 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength5
				 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength6
				 			+ ", " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength7 + ") >= '" + alStartingSegmentValueDescriptions.get(i) + "'"
				 	//IF the segment value if the account is greater then or equal to the selected starting value, then qualify the record:
				 	//If not, the DISqualify the record:
				 	+ ", TRUE"
					+ ", FALSE"
					+ ")"
					//If this ISN'T the segment we're looking for, then don't disqualify the record:
					+ ", TRUE"
					+ ")"
				+ ")" + "\n"
				;

				//Check the ending value:
				sSQL += " AND ("
					//IF the segments match then...
					+ "IF (" + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.lsegmentid7 + " = " + alEndingSegmentIDs.get(i)
					
					//THEN, if the segment in the account number >= the starting selected value for the segment:
			 		+ ", IF(SUBSTRING(" 
				 			+ SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.sacctid
				 			+ ", " 
				 			+ "1 + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength1
			 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength2
			 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength3
			 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength4
			 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength5
			 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength6
				 			+ ", " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength7 + ") <= '" + alEndingSegmentValueDescriptions.get(i) + "'"
				 	//IF the segment value if the account is greater then or equal to the selected starting value, then qualify the record:
				 	//If not, the DISqualify the record:
				 	+ ", TRUE"
					+ ", FALSE"
					+ ")"
					//If this ISN'T the segment we're looking for, then don't disqualify the record:
					+ ", TRUE"
					+ ")"
				+ ")" + "\n"
				;
				
				//SEGMENT 8:
				//Check the starting value:
				sSQL += " AND ("
					//IF the segments match then...
					+ "IF (" + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.lsegmentid8 + " = " + alStartingSegmentIDs.get(i)
					
					//THEN, if the segment in the account number >= the starting selected value for the segment:
			 		+ ", IF(SUBSTRING(" 
				 			+ SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.sacctid
				 			+ ", " 
				 			+ "1 + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength1
				 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength2
				 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength3
				 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength4
				 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength5
				 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength6
				 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength7
				 			+ ", " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength8 + ") >= '" + alStartingSegmentValueDescriptions.get(i) + "'"
				 	//IF the segment value if the account is greater then or equal to the selected starting value, then qualify the record:
				 	//If not, the DISqualify the record:
				 	+ ", TRUE"
					+ ", FALSE"
					+ ")"
					//If this ISN'T the segment we're looking for, then don't disqualify the record:
					+ ", TRUE"
					+ ")"
				+ ")" + "\n"
				;

				//Check the ending value:
				sSQL += " AND ("
					//IF the segments match then...
					+ "IF (" + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.lsegmentid8 + " = " + alEndingSegmentIDs.get(i)
					
					//THEN, if the segment in the account number >= the starting selected value for the segment:
			 		+ ", IF(SUBSTRING(" 
				 			+ SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.sacctid
				 			+ ", " 
				 			+ "1 + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength1
			 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength2
			 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength3
			 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength4
			 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength5
			 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength6
			 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength7
				 			+ ", " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength8 + ") <= '" + alEndingSegmentValueDescriptions.get(i) + "'"
				 	//IF the segment value if the account is greater then or equal to the selected starting value, then qualify the record:
				 	//If not, the DISqualify the record:
				 	+ ", TRUE"
					+ ", FALSE"
					+ ")"
					//If this ISN'T the segment we're looking for, then don't disqualify the record:
					+ ", TRUE"
					+ ")"
				+ ")" + "\n"
				;
				
				//SEGMENT 9:
				//Check the starting value:
				sSQL += " AND ("
					//IF the segments match then...
					+ "IF (" + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.lsegmentid9 + " = " + alStartingSegmentIDs.get(i)
					
					//THEN, if the segment in the account number >= the starting selected value for the segment:
			 		+ ", IF(SUBSTRING(" 
				 			+ SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.sacctid
				 			+ ", " 
				 			+ "1 + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength1
				 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength2
				 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength3
				 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength4
				 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength5
				 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength6
				 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength7
				 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength8
				 				
				 			+ ", " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength9 + ") >= '" + alStartingSegmentValueDescriptions.get(i) + "'"
				 	//IF the segment value if the account is greater then or equal to the selected starting value, then qualify the record:
				 	//If not, the DISqualify the record:
				 	+ ", TRUE"
					+ ", FALSE"
					+ ")"
					//If this ISN'T the segment we're looking for, then don't disqualify the record:
					+ ", TRUE"
					+ ")"
				+ ")" + "\n"
				;

				//Check the ending value:
				sSQL += " AND ("
					//IF the segments match then...
					+ "IF (" + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.lsegmentid9 + " = " + alEndingSegmentIDs.get(i)
					
					//THEN, if the segment in the account number >= the starting selected value for the segment:
			 		+ ", IF(SUBSTRING(" 
				 			+ SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.sacctid
				 			+ ", " 
				 			+ "1 + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength1
			 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength2
			 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength3
			 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength4
			 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength5
			 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength6
			 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength7
			 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength8
				 			+ ", " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength9 + ") <= '" + alEndingSegmentValueDescriptions.get(i) + "'"
				 	//IF the segment value if the account is greater then or equal to the selected starting value, then qualify the record:
				 	//If not, the DISqualify the record:
				 	+ ", TRUE"
					+ ", FALSE"
					+ ")"
					//If this ISN'T the segment we're looking for, then don't disqualify the record:
					+ ", TRUE"
					+ ")"
				+ ")" + "\n"
				;

				//SEGMENT 10:
				//Check the starting value:
				sSQL += " AND ("
					//IF the segments match then...
					+ "IF (" + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.lsegmentid10 + " = " + alStartingSegmentIDs.get(i)
					
					//THEN, if the segment in the account number >= the starting selected value for the segment:
			 		+ ", IF(SUBSTRING(" 
				 			+ SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.sacctid
				 			+ ", " 
				 			+ "1 + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength1
				 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength2
				 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength3
				 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength4
				 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength5
				 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength6
				 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength7
				 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength8
				 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength9
				 				
				 			+ ", " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength10 + ") >= '" + alStartingSegmentValueDescriptions.get(i) + "'"
				 	//IF the segment value if the account is greater then or equal to the selected starting value, then qualify the record:
				 	//If not, the DISqualify the record:
				 	+ ", TRUE"
					+ ", FALSE"
					+ ")"
					//If this ISN'T the segment we're looking for, then don't disqualify the record:
					+ ", TRUE"
					+ ")"
				+ ")" + "\n"
				;

				//Check the ending value:
				sSQL += " AND ("
					//IF the segments match then...
					+ "IF (" + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.lsegmentid10 + " = " + alEndingSegmentIDs.get(i)
					
					//THEN, if the segment in the account number >= the starting selected value for the segment:
			 		+ ", IF(SUBSTRING(" 
				 			+ SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.sacctid
				 			+ ", " 
				 			+ "1 + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength1
			 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength2
			 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength3
			 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength4
			 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength5
			 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength6
			 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength7
			 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength8
			 				+ " + " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength9
				 			+ ", " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.llength10 + ") <= '" + alEndingSegmentValueDescriptions.get(i) + "'"
				 	//IF the segment value if the account is greater then or equal to the selected starting value, then qualify the record:
				 	//If not, the DISqualify the record:
				 	+ ", TRUE"
					+ ", FALSE"
					+ ")"
					//If this ISN'T the segment we're looking for, then don't disqualify the record:
					+ ", TRUE"
					+ ")"
				+ ")" + "\n"
				;

			}
			
			sSQL += ")"
			+ " ORDER BY " + SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.sacctid
		;
		
		//System.out.println("[1553548501] - SQL = '" + sSQL + "'");
			
		boolean bOddRow = false;
		BigDecimal bdDebitTotal = new BigDecimal("0.00");
		BigDecimal bdCreditTotal = new BigDecimal("0.00");
		BigDecimal bdEarningsTotal = new BigDecimal("0.00");
		BigDecimal bdNetChangeTotal = new BigDecimal("0.00");
		BigDecimal bdBalanceTotal = new BigDecimal("0.00");
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
			while(rs.next()){
				BigDecimal bdDebit = new BigDecimal("0.00");
				BigDecimal bdCredit = new BigDecimal("0.00");
				BigDecimal bdAmount = rs.getBigDecimal("CURRENTBALANCE");
				//If the account is normally a debit balance:
				if (rs.getInt(SMTableglaccounts.TableName + "." + SMTableglaccounts.inormalbalancetype) == SMTableglaccounts.NORMAL_BALANCE_TYPE_DEBIT){
					if (bdAmount.compareTo(BigDecimal.ZERO) > 0){
						bdDebit = bdAmount;
						bdCredit = BigDecimal.ZERO;
					}else{
						bdDebit = BigDecimal.ZERO;
						bdCredit = bdAmount.negate();
					}
				// But if the account is normally a credit balance:
				}else{
					if (bdAmount.compareTo(BigDecimal.ZERO) < 0){
						bdDebit = BigDecimal.ZERO;
						bdCredit = bdAmount.negate();
					}else{
						bdDebit = bdAmount;
						bdCredit = BigDecimal.ZERO;
					}
				}
				
				bdDebitTotal = bdDebitTotal.add(bdDebit);
				bdCreditTotal = bdCreditTotal.add(bdCredit);
				
				//If it's an income statement account, add it to the earnings total:
				if (rs.getString(SMTableglaccounts.TableName + "." + SMTableglaccounts.sAcctType).compareToIgnoreCase(SMTableglaccounts.ACCOUNT_TYPE_INCOME_STATEMENT) == 0){
					//For each record, one of these will be zero, but it's simpler to just add and subtract both each time,
					// than to worry about which case it is for each record:
					bdEarningsTotal = bdEarningsTotal.subtract(bdDebit);
					bdEarningsTotal = bdEarningsTotal.add(bdCredit);
				}
				
				s += printReportLine(
						rs.getString(SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.sacctid),
						rs.getString(SMTableglaccounts.TableName + "." + SMTableglaccounts.sDesc),
						bdDebit,
						bdCredit,
						bOddRow);
				
				bOddRow = !bOddRow;
			}
			rs.close();
		} catch (Exception e1) {
			throw new Exception("Error [1553381089] reading GL transactions with SQL - " + e1.getMessage() + ".");
		}
		
		s += printGrandTotals(
			bdDebitTotal, 
			bdCreditTotal, 
			bdNetChangeTotal,
			bdBalanceTotal
		);
		
		return s;
	}

	private String printReportLine(
			String sGLAccount,
			String sAccountDescription,
			BigDecimal bdDebitAmt,
			BigDecimal bdCreditAmt,
			boolean bOddRow
			) throws Exception{
		String s = "";
		
		if (bOddRow){
			s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_WHITE + " \" >\n";
		}else{
			s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTBLUE + " \" >\n";
		}
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  sGLAccount
			+ "</TD>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  sAccountDescription
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdDebitAmt)
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdCreditAmt)
			+ "  </TR>\n"
		;
		
		return s;
	}
	

	private String printGrandTotals(BigDecimal bdDebitTotal, BigDecimal bdCreditTotal, BigDecimal bdNetChangeTotal, BigDecimal bdBalanceTotal){
		String s = "";

		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_WHITE + " \" >\n";
		
		s += "    <TD COLSPAN=2 class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  "&nbsp;"
			+ "</TD>\n"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\""
				+ " style = \" border-top: 2px solid black; \""
			+ " >"
			+  "&nbsp;"
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\""
			+ " style = \" border-top: 2px solid black; \""
			+ " >"
			+  "&nbsp;"
			+ "</TD>\n"
			
			+ "    <TD colspan=3 class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\""
			+ " style = \" border-top: 2px solid black; \""
			+ " >"
			+  "<B> Report totals" + "</B>"
			+ "</TD>\n"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\""
			+ " style = \" border-top: 2px solid black; \""
			+ " >"
			+  "<B>" + ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdDebitTotal) + "</B>"
			+ "</TD>\n"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\""
			+ " style = \" border-top: 2px solid black; \""
			+ " >"
			+  "<B>" + ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdCreditTotal) + "</B>"
			+ "</TD>\n"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\""
			+ " style = \" border-top: 2px solid black; \""
			+ " >"
			+  "<B>" + ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdNetChangeTotal) + "</B>"
			+ "</TD>\n"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\""
			+ " style = \" border-top: 2px solid black; \""
			+ " >"
			+  "<B>" + ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdBalanceTotal) + "</B>"
			+ "</TD>\n"

			+ "  </TR>\n"
			;
		return s;
	}
	
	private String printAccountSubTotals(
			String sFiscalPeriod,
			BigDecimal bdNetChangeForPeriod, 
			BigDecimal bdBalanceForPeriod, 
			BigDecimal bdTotalDebits,
			BigDecimal bdTotalCredits,
			BigDecimal bdTotalNetChange,
			BigDecimal bdTotalBalance
			){
		String s = "";

		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_WHITE + " \" >\n";
		
		s += "    <TD COLSPAN=2 class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  "&nbsp;"
			+ "</TD>\n"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\""
				+ " style = \" border-top: 2px solid black; \""
			+ " >"
			+  "&nbsp;"
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\""
			+ " style = \" border-top: 2px solid black; \""
			+ " >"
			+  "&nbsp;"
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\""
			+ " style = \" border-top: 2px solid black; \""
			+ " >"
			+  "<B> Net change and ending balance for fiscal period " + sFiscalPeriod + "</B>"
			+ "</TD>\n"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\""
			+ " style = \" border-top: 2px solid black; \""
			+ " >"
			+  "&nbsp;"
			+ "</TD>\n"
		
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\""
			+ " style = \" border-top: 2px solid black; \""
			+ " >"
			+  "&nbsp;"
			+ "</TD>\n"
		
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\""
			+ " style = \" border-top: 2px solid black; \""
			+ " >"
			+  "&nbsp;"
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\""
			+ " style = \" border-top: 2px solid black; \""
			+ " >"
			+  "<B>" + ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdNetChangeForPeriod) + "</B>"
			+ "</TD>\n"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\""
			+ " style = \" border-top: 2px solid black; \""
			+ " >"
			+  "<B>" + ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdBalanceForPeriod) + "</B>"
			+ "</TD>\n"
			
		
		+ "  </TR>\n"
		;
		
		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_WHITE + " \" >\n";
		
		s += "    <TD COLSPAN=2 class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  "&nbsp;"
			+ "</TD>\n"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\""
				+ " style = \" border-top: 2px solid black; \""
			+ " >"
			+  "&nbsp;"
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\""
			+ " style = \" border-top: 2px solid black; \""
			+ " >"
			+  "&nbsp;"
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\""
			+ " style = \" border-top: 2px solid black; \""
			+ " >"
			+  "<B> Net change and ending balance for fiscal period " + sFiscalPeriod + "</B>"
			+ "</TD>\n"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\""
			+ " style = \" border-top: 2px solid black; \""
			+ " >"
			+  "&nbsp;"
			+ "</TD>\n"
		
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\""
			+ " style = \" border-top: 2px solid black; \""
			+ " >"
			+  "<B>" + ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalDebits) + "</B>"
			+ "</TD>\n"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\""
			+ " style = \" border-top: 2px solid black; \""
			+ " >"
			+  "<B>" + ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalCredits) + "</B>"
			+ "</TD>\n"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\""
			+ " style = \" border-top: 2px solid black; \""
			+ " >"
			+  "<B>" + ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalNetChange) + "</B>"
			+ "</TD>\n"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\""
			+ " style = \" border-top: 2px solid black; \""
			+ " >"
			+  "<B>" + ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalBalance) + "</B>"
			+ "</TD>\n"

			+ "  </TR>\n"
			;
		;
		return s;
	}

	private String printColumnHeadings(){
		String s = "";
		
		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_WHITE + " \" >\n";
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "Account<BR>number/<BR>Year/period"
			+ "</TD>"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "Source"
			+ "</TD>"
			
			+"    <TD COLSPAN class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "Doc Date"
			+ "</TD>"

			+"    <TD>"
			+  "Description/Reference"
			+ "</TD>"

			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "Batch - entry"
			+ "</TD>"

			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
			+  "Debits"
			+ "</TD>"

			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
			+  "Credits"
			+ "</TD>"
			
			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
			+  "Net change"
			+ "</TD>"
			
			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
			+  "Balance"
			+ "</TD>"

		;	
		
		s += "  </TR>\n";
		
		return s;
	}
	
	private String printTableHeading(){
		String s = "";
		
		s += "<TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITHOUT_BORDER + "\" >\n";
		
		return s;
	}

	private String printTableFooting(){
		String s = "";
		
		s += "</TABLE>\n";
		
		return s;
	}
}
