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
import SMDataDefinition.SMTablegltransactionlines;
import ServletUtilities.clsDatabaseFunctions;

public class GLTransactionListingReport  extends java.lang.Object{

	private static int BUFFER_LOOP_LIMIT = 150;
	
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

		String sFiscalYear = sStartingFiscalPeriod.substring(0, sStartingFiscalPeriod.indexOf(GLTransactionListingSelect.PARAM_VALUE_DELIMITER));
		String sStartingPeriod = sStartingFiscalPeriod.replace(sFiscalYear + GLTransactionListingSelect.PARAM_VALUE_DELIMITER, "");
		String sEndingYear = sEndingFiscalPeriod.substring(0, sEndingFiscalPeriod.indexOf(GLTransactionListingSelect.PARAM_VALUE_DELIMITER));
		String sEndingPeriod = sEndingFiscalPeriod.replace(sEndingYear + GLTransactionListingSelect.PARAM_VALUE_DELIMITER, "");
		
		String sSQL = "SELECT" + "\n"
			+ " " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid + "\n"
			
			+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.bdamount
			+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.dattransactiondate
			+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ifiscalperiod
			+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ifiscalyear
			+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.loriginalbatchnumber
			+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.loriginalentrynumber
			+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.loriginallinenumber
			+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.lsourceledgertransactionlineid
			+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sdescription
			+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sreference
			+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ssourceledger
			+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ssourcetype
			//+ ", " + SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.bdnetchangeforperiod
			+ ", " + SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.bdopeningbalance
			+ ", " + SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.bdtotalyeartodate
			+ ", " + SMTableglaccounts.TableName + "." + SMTableglaccounts.sDesc + "\n"
			+ ", " + SMTableglaccounts.TableName + "." + SMTableglaccounts.inormalbalancetype + "\n"
			+ ", " + SMTableglaccounts.TableName + "." + SMTableglaccounts.sAcctType + "\n"
			+ " FROM " + SMTablegltransactionlines.TableName + "\n"
			+ " LEFT JOIN " + SMTableglaccounts.TableName + "\n" 
			+ " ON " + SMTableglaccounts.TableName + "." + SMTableglaccounts.sAcctID
			+ " = " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid + "\n"
			+ " LEFT JOIN " + SMTableglaccountgroups.TableName + "\n"
			+ " ON " + SMTableglaccounts.TableName + "." + SMTableglaccounts.laccountgroupid 
				+ " = " + SMTableglaccountgroups.TableName + "." + SMTableglaccountgroups.lid + "\n"
			+ " LEFT JOIN " + SMTableglaccountstructures.TableName + "\n"
			+ " ON " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.lid + " = "
			+ SMTableglaccounts.TableName + "." + SMTableglaccounts.lstructureid + "\n"
			+ " LEFT JOIN " + SMTableglfinancialstatementdata.TableName + " ON "
			+ "(" + SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.sacctid + " = "
				+ SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid + ")"
			+ " AND (" + SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.ifiscalyear + " = "
				+ SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ifiscalyear + ")"
			+ " AND (" + SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.ifiscalperiod + " = "
				+ SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ifiscalperiod + ")"
			
			+ " WHERE (" + "\n"
				+ "(" + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ifiscalyear + " >= " + sFiscalYear + ")" + "\n"
				+ " AND (" + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ifiscalperiod + " >= " + sStartingPeriod + ")" + "\n"
				+ " AND (" + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ifiscalperiod + " <= " + sEndingPeriod + ")" + "\n"
				
				//Account range:
				+ " AND (" + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid + " >= '" + sStartingAccount + "')" + "\n"
				+ " AND (" + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid + " <= '" + sEndingAccount + "')" + "\n"
				
				//Account group range:
				+ " AND (" + SMTableglaccountgroups.TableName + "." + SMTableglaccountgroups.sgroupcode + " >= '" + sStartingAccountGroupCode + "')" + "\n"
				+ " AND (" + SMTableglaccountgroups.TableName + "." + SMTableglaccountgroups.sgroupcode + " <= '" + sEndingAccountGroupCode + "')" + "\n"
			;
		
			//Include accounts with no activity?
			//if(!bIncludeAccountsWithNoActivity){
			//	sSQL += " AND ((" + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.bdtotalyeartodate
			//		+ " + " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.bdopeningbalance
			//		+ ")  != 0.00)" + "\n";
			//}
			
			//Now process the segments:

			for (int i = 0; i < alStartingSegmentIDs.size(); i++){

				//SEGMENT 1:
				//Check the starting value:
				sSQL += " AND ("
					//IF the segments match then...
					+ "IF (" + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.lsegmentid1 + " = " + alStartingSegmentIDs.get(i)
					
					//THEN, if the segment in the account number >= the starting selected value for the segment:
			 		+ ", IF(SUBSTRING(" 
				 			+ SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid
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
				 			+ SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid
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
				 			+ SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid
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
				 			+ SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid
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
				 			+ SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid
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
				 			+ SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid
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
				 			+ SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid
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
				 			+ SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid
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
				 			+ SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid
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
				 			+ SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid
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
				 			+ SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid
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
				 			+ SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid
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
				 			+ SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid
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
				 			+ SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid
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
				 			+ SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid
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
				 			+ SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid
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
				 			+ SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid
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
				 			+ SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid
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
				 			+ SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid
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
				 			+ SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid
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
			+ " ORDER BY " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid
				+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ifiscalyear
				+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ifiscalperiod
				+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ssourceledger
				+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ssourcetype
				+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.dattransactiondate
		;
		
		//System.out.println("[1553548501] - SQL = '" + sSQL + "'");
			
		boolean bOddRow = false;
		BigDecimal bdGrandDebitTotal = new BigDecimal("0.00");
		BigDecimal bdGrandCreditTotal = new BigDecimal("0.00");
		BigDecimal bdGrandNetChangeTotal = new BigDecimal("0.00");
		BigDecimal bdGrandBalanceTotal = new BigDecimal("0.00");
		BigDecimal bdDebit = new BigDecimal("0.00");
		BigDecimal bdCredit = new BigDecimal("0.00");
		BigDecimal bdAmount = new BigDecimal("0.00");
		BigDecimal bdNetChangeForFiscalPeriod = new BigDecimal("0.00");
		BigDecimal bdEndingBalanceForPeriod = new BigDecimal("0.00");
		BigDecimal bdNetChangeForAccount = new BigDecimal("0.00");
		BigDecimal bdEndingBalanceForAccount = new BigDecimal("0.00");
		BigDecimal bdTotalDebitsForAccount = new BigDecimal("0.00");
		BigDecimal bdTotalCreditsForAccount = new BigDecimal("0.00");
		String sPreviousAccountDescription = "";
		
		long lRecordCounter = 0;
		String sStringBuffer = "";
		int iPreviousFiscalPeriod = 0;
		String sPreviousAccount = "";
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
			while(rs.next()){
				
				//System.out.println("[1554320753] - into the loop...");
				
				if (rs.getString(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid).compareTo(sPreviousAccount) != 0){
					sStringBuffer += printAccountHeadingLine(
						rs.getString(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid), 
						rs.getString(SMTableglaccounts.TableName + "." + SMTableglaccounts.sDesc), 
						getStartingAccountBalance(
							conn,
							rs.getString(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid),
							sFiscalYear,
							sStartingPeriod
						)
					);
				}
				
				//If the fiscal period has changed, print the fiscal period totals:
				if (
					(rs.getInt(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ifiscalperiod) != iPreviousFiscalPeriod)
					&& (iPreviousFiscalPeriod != 0)
				){
					sStringBuffer += printFiscalPeriodSubtotals(
						iPreviousFiscalPeriod,
						bdNetChangeForFiscalPeriod,
						bdEndingBalanceForPeriod
						);
					bdNetChangeForFiscalPeriod = BigDecimal.ZERO;
					bdEndingBalanceForPeriod = BigDecimal.ZERO;
				}
				
				if (
					(rs.getString(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid).compareToIgnoreCase(sPreviousAccount) != 0)
					&& (sPreviousAccount.compareToIgnoreCase("") != 0)
				){
					sStringBuffer += printAccountSubTotals(
						bdNetChangeForAccount,
						bdEndingBalanceForAccount,
						bdTotalDebitsForAccount,
						bdTotalCreditsForAccount,
						sPreviousAccount + " - " + sPreviousAccountDescription
						);
					
					bdGrandBalanceTotal = bdGrandBalanceTotal.add(bdEndingBalanceForAccount);
					
					bdNetChangeForAccount = BigDecimal.ZERO;
					bdEndingBalanceForAccount = BigDecimal.ZERO;
					bdTotalDebitsForAccount = BigDecimal.ZERO;
					bdTotalCreditsForAccount = BigDecimal.ZERO;
				}
				
				bdDebit = BigDecimal.ZERO;
				bdCredit = BigDecimal.ZERO;
				bdAmount = rs.getBigDecimal(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.bdamount);
				
				bdNetChangeForFiscalPeriod = bdNetChangeForFiscalPeriod.add(bdAmount);
				//This value keeps being rewritten on every record within a fiscal period, but there's no harm in that:
				bdEndingBalanceForPeriod = rs.getBigDecimal(
						SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.bdopeningbalance).add(
								rs.getBigDecimal(SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.bdtotalyeartodate))
						;
				
				bdNetChangeForAccount = bdNetChangeForAccount.add(bdAmount);
				bdEndingBalanceForAccount = rs.getBigDecimal(
					SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.bdopeningbalance).add(
						rs.getBigDecimal(SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.bdtotalyeartodate))
					;
				
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
				
				bdTotalDebitsForAccount = bdTotalDebitsForAccount.add(bdDebit);
				bdTotalCreditsForAccount = bdTotalCreditsForAccount.add(bdCredit);
				bdGrandDebitTotal = bdGrandDebitTotal.add(bdDebit);
				bdGrandCreditTotal = bdGrandCreditTotal.add(bdCredit);
				bdGrandNetChangeTotal = bdGrandNetChangeTotal.add(bdAmount);
				
				sStringBuffer += printReportLine(
					rs.getString(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid),
					rs.getInt(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ifiscalyear),
					rs.getInt(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ifiscalperiod),
					rs.getString(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ssourceledger),
					rs.getString(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ssourcetype),
					ServletUtilities.clsDateAndTimeConversions.sqlDateToString(
						rs.getDate(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.dattransactiondate),
						ServletUtilities.clsDateAndTimeConversions.DATE_FORMAT_STD_Mdyyy),
					rs.getString(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sdescription),
					rs.getString(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sreference),
					Long.toString(rs.getLong(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.loriginalbatchnumber)) 
						+ " - " 
						+ Long.toString(rs.getLong(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.loriginalentrynumber)),
					bdDebit,
					bdCredit,
					bOddRow
				);
				
				if ((lRecordCounter % BUFFER_LOOP_LIMIT) == 0){
					s += sStringBuffer;
					sStringBuffer = "";
				}
				
				bOddRow = !bOddRow;
				lRecordCounter++;
				iPreviousFiscalPeriod = rs.getInt(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ifiscalperiod);
				sPreviousAccount = rs.getString(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid);
				sPreviousAccountDescription = rs.getString(SMTableglaccounts.TableName + "." + SMTableglaccounts.sDesc);
			}
			rs.close();
		} catch (Exception e1) {
			throw new Exception("Error [1553381089] reading GL transactions with SQL - " + e1.getMessage() + ".");
		}
		
		s += sStringBuffer;
		
		//Print the final fiscal period totals:
		s += printFiscalPeriodSubtotals(
			iPreviousFiscalPeriod,
			bdNetChangeForFiscalPeriod,
			bdEndingBalanceForPeriod
			);
	
		s += printAccountSubTotals(
			bdNetChangeForAccount,
			bdEndingBalanceForAccount,
			bdTotalDebitsForAccount,
			bdTotalCreditsForAccount,
			sPreviousAccount + " - " + sPreviousAccountDescription
		);

		//add the last account balance:
		bdGrandBalanceTotal = bdGrandBalanceTotal.add(bdEndingBalanceForAccount);
		
		s += printGrandTotals(
			bdGrandDebitTotal, 
			bdGrandCreditTotal, 
			bdGrandNetChangeTotal,
			bdGrandBalanceTotal
		);
		
		return s;
	}

	private String printReportLine(
			String sAccount,
			int iFiscalYear,
			int iFiscalPeriod,
			String sSourceLedger,
			String sSourceType,
			String sDocumentDate,
			String sDescription,
			String sReference,
			String sBatchAndEntry,
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
		
		String sDebit = "&nbsp;";
		if (bdDebitAmt.compareTo(BigDecimal.ZERO) != 0){
			sDebit = ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdDebitAmt);
		}
		
		String sCredit = "&nbsp;";
		if (bdCreditAmt.compareTo(BigDecimal.ZERO) != 0){
			sCredit = ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdCreditAmt);
		}
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + " \" >"
			+  sAccount
			+ "</TD>\n"
				
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + " \" >"
			+  Integer.toString(iFiscalYear) + " - " + Integer.toString(iFiscalPeriod)
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + " \" >"
			+  sSourceLedger + "-" + sSourceType
			+ "</TD>\n"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + " \" >"
			+  sDocumentDate
			+ "</TD>\n"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + " \" >"
			+  sDescription + "<BR>" + sReference
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + " \" >"
			+  sBatchAndEntry
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + " \" >"
			+  sDebit
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + " \" >"
			+  sCredit
			+ "</TD>\n"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + " \" >"
			+  "&nbsp;"
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + " \" >"
			+  "&nbsp;"
			+ "</TD>\n"
			
			+ "  </TR>\n"
		;
		
		return s;
	}
	
	private String printFiscalPeriodSubtotals(
			int iFiscalPeriod,
			BigDecimal bdNetChange,
			BigDecimal bdBalance
			) throws Exception{
		String s = "";
		
		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_WHITE + " \" >\n";
		
		s += "    <TD COLSPAN = 8 class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + " \" >"
			+  "<B>Net Change and Ending Balance For Fiscal Period " + Integer.toString(iFiscalPeriod) + ":</B>"
			+ "</TD>\n"
							
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + " \" >"
			+  ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdNetChange)
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + " \" >"
			+  ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdBalance)
			+ "</TD>\n"
			
			+ "  </TR>\n"
		;
		
		return s;
	}

	private String printGrandTotals(BigDecimal bdDebitTotal, BigDecimal bdCreditTotal, BigDecimal bdNetChangeTotal, BigDecimal bdBalanceTotal){
		String s = "";

		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_WHITE + " \" >\n";
		
		s += "    <TD colspan=6 class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\""
			+ " style = \" border-top: 2px solid black; \""
			+ " >"
			+  "<B> Report totals:" + "</B>"
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
			BigDecimal bdNetChangeForAccount, 
			BigDecimal bdBalanceForAccount, 
			BigDecimal bdTotalDebits,
			BigDecimal bdTotalCredits,
			String sAcctDesc
			){
		String s = "";

		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_WHITE + " \" >\n";
		
		s += "    <TD COLSPAN = 6 class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + " \" >"
			+  "<B>Totals For " + sAcctDesc + ":</B>"
			+ "</TD>\n"
						
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + " \" >"
			+  "<B>" + ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalDebits) + "</B>"
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + " \" >"
			+  "<B>" + ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalCredits) + "</B>"
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + " \" >"
			+  "<B>" + ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdNetChangeForAccount) + "</B>"
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + " \" >"
			+  "<B>" + ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdBalanceForAccount) + "</B>"
			+ "</TD>\n"
			
			+ "  </TR>\n"
		;
		return s;
	}
	private String printAccountHeadingLine(String sAccount, String sDescription, BigDecimal bdBalance){
		String s = "";
		
		s += "  <TR style = \"" + " background-color: #ccffdd;" + " \" >\n";
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "<B>" + sAccount + "</B>"
			+ "</TD>"

			+ "    <TD COLSPAN=4 class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
			+  "<B>" + sDescription + "</B>"
			+ "</TD>"

			+ "    <TD COLSPAN=5 class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + " \" >"
			+  "<B>" + ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdBalance) + "</B>"
			+ "</TD>\n"
		;	
		
		s += "  </TR>\n";
		
		return s;
	}
	private String printColumnHeadings(){
		String s = "";
		
		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_WHITE + " \" >\n";
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "Account<BR>number"
			+ "</TD>"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "Year - Period"
			+ "</TD>"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "Source"
			+ "</TD>"
			
			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "Doc Date"
			+ "</TD>"

			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
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
	private BigDecimal 	getStartingAccountBalance(
		Connection conn,
		String sAccount,
		String sFiscalYear,
		String sStartingPeriod
		) throws Exception{
		
		BigDecimal bdStartingBalance = new BigDecimal("0.00");
		String sSQL = "SELECT"
			+ " " + SMTableglfinancialstatementdata.bdopeningbalance
			+ ", " + SMTableglfinancialstatementdata.bdtotalyeartodate
			+ ", " + SMTableglfinancialstatementdata.bdnetchangeforperiod
			+ " FROM " + SMTableglfinancialstatementdata.TableName
			+ " WHERE ("
				+ "(" + SMTableglfinancialstatementdata.sacctid + " = '" + sAccount + "')"
				+ " AND (" + SMTableglfinancialstatementdata.ifiscalyear + " = " + sFiscalYear + ")"
				+ " AND (" + SMTableglfinancialstatementdata.ifiscalperiod + " = " + sStartingPeriod + ")"
			+ ")"
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
			if (rs.next()){
				//Start with the opening balance for the year:
				bdStartingBalance = rs.getBigDecimal(SMTableglfinancialstatementdata.bdopeningbalance);
				//Add in the total year's changes to date:
				bdStartingBalance = bdStartingBalance.add(rs.getBigDecimal(SMTableglfinancialstatementdata.bdtotalyeartodate));
				//And finally subtract the changes for this period, to get back to the balance just BEFORE the starting period:
				bdStartingBalance = bdStartingBalance.subtract(rs.getBigDecimal(SMTableglfinancialstatementdata.bdnetchangeforperiod));
			}else{
				throw new Exception("Error [1554829782] - ending balance for account " 
					+ sAccount + " fiscal year " + sFiscalYear + ", period " + sStartingPeriod + " was not found.");
			}
		} catch (Exception e) {
			throw new Exception("Error [1554829783] getting ending balance for account " 
					+ sAccount + " fiscal year " + sFiscalYear + ", period " + sStartingPeriod + " - " + e.getMessage());
		}
		
		return bdStartingBalance;
		
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
