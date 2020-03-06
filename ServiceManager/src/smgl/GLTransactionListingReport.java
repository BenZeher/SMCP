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
import SMDataDefinition.SMTableglexternalcompanypulls;
import SMDataDefinition.SMTableglfinancialstatementdata;
import SMDataDefinition.SMTablegltransactionlines;
import ServletUtilities.clsDatabaseFunctions;
import smcontrolpanel.SMUtilities;

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
		ArrayList<String>alStartingSegmentIDs,
		ArrayList<String>alStartingSegmentValueDescriptions,
		ArrayList<String>alEndingSegmentIDs,
		ArrayList<String>alEndingSegmentValueDescriptions,
		boolean bAllowBatchViewing,
		String sExternalPull
		) throws Exception{
		
		String s = "";
		
		s += printTableHeading();
		s += buildListingReport(
			sStartingFiscalPeriod,
			sEndingFiscalPeriod,
			sStartingAccount,
			sEndingAccount,
			sStartingAccountGroup,
			sEndingAccountGroup,
			alStartingSegmentIDs,
			alStartingSegmentValueDescriptions,
			alEndingSegmentIDs,
			alEndingSegmentValueDescriptions,
			conn,
			sDBID, 
			context,
			bAllowBatchViewing,
			sExternalPull
		);			

		s += printTableFooting();
		
		return s;
	}
	
	private String buildListingReport(
			String sStartingFiscalPeriod,
			String sEndingFiscalPeriod,
			String sStartingAccount,
			String sEndingAccount,
			String sStartingAccountGroupCode,
			String sEndingAccountGroupCode,
			ArrayList<String>alStartingSegmentIDs,
			ArrayList<String>alStartingSegmentValueDescriptions,
			ArrayList<String>alEndingSegmentIDs,
			ArrayList<String>alEndingSegmentValueDescriptions,
			Connection conn,
			String sDBID, 
			ServletContext context,
			boolean bAllowBatchViewing,
			String sExternalPull
		) throws Exception{
		String s = "";
		
		s += printColumnHeadings();

		String sStartingFiscalYear = sStartingFiscalPeriod.substring(0, sStartingFiscalPeriod.indexOf(GLTransactionListingSelect.PARAM_VALUE_DELIMITER));
		String sStartingPeriod = sStartingFiscalPeriod.replace(sStartingFiscalYear + GLTransactionListingSelect.PARAM_VALUE_DELIMITER, "");
		String sEndingFiscalYear = sEndingFiscalPeriod.substring(0, sEndingFiscalPeriod.indexOf(GLTransactionListingSelect.PARAM_VALUE_DELIMITER));
		String sEndingPeriod = sEndingFiscalPeriod.replace(sEndingFiscalYear + GLTransactionListingSelect.PARAM_VALUE_DELIMITER, "");
		
		int iStartingFiscalPeriodProduct;
		try {
			iStartingFiscalPeriodProduct = (Integer.parseInt(sStartingFiscalYear) * 100) + Integer.parseInt(sStartingPeriod);
		} catch (Exception e) {
			throw new Exception("Error [20191981522491] " + "Could not parse starting fiscal year '" + sStartingFiscalYear 
				+ "', or starting fiscal period '" + sStartingPeriod + "'.");
		}
		int iEndingFiscalPeriodProduct;
		try {
			iEndingFiscalPeriodProduct = (Integer.parseInt(sEndingFiscalYear) * 100) + Integer.parseInt(sEndingPeriod);
		} catch (Exception e) {
			throw new Exception("Error [20191981522492] " + "Could not parse ending fiscal year '" + sEndingFiscalYear 
				+ "', or ending fiscal period '" + sEndingPeriod + "'.");
		}
		
		//First, get a recordset of all the accounts we need to list:
		String sSQL = "SELECT DISTINCT"
			+ " " + SMTableglaccounts.TableName + "." + SMTableglaccounts.sAcctID
			+ ", " + SMTableglaccounts.TableName + "." + SMTableglaccounts.sDesc
			+ " FROM " + SMTableglfinancialstatementdata.TableName
			+ " LEFT JOIN " + SMTableglaccounts.TableName 
			+ " ON " + SMTableglaccounts.TableName + "." + SMTableglaccounts.sAcctID
			+ " = " + SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.sacctid
			+ " LEFT JOIN " + SMTableglaccountgroups.TableName
			+ " ON " + SMTableglaccounts.TableName + "." + SMTableglaccounts.laccountgroupid 
			+ " = " + SMTableglaccountgroups.TableName + "." + SMTableglaccountgroups.lid
			+ " LEFT JOIN " + SMTableglaccountstructures.TableName
			+ " ON " + SMTableglaccountstructures.TableName + "." + SMTableglaccountstructures.lid + " = "
			+ SMTableglaccounts.TableName + "." + SMTableglaccounts.lstructureid
			
			+  " WHERE ("

			+ "("
			+ "((" + SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.ifiscalyear + " * 100) +  " 
			+ SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.ifiscalperiod + ") >= " + iStartingFiscalPeriodProduct
			+ ")"

			+ " AND ("
			+ "((" + SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.ifiscalyear + " * 100) +  " 
			+ SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.ifiscalperiod + ") <= " + iEndingFiscalPeriodProduct
			+ ")"

			+ " AND ("
			+ "(" + SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.bdopeningbalance + " + "  
			+ SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.bdtotalyeartodate + ") != 0.00"
			+ ")"
			
			//Account range:
			+ " AND (" + SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.sacctid + " >= '" + sStartingAccount + "')" + "\n"
			+ " AND (" + SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.sacctid + " <= '" + sEndingAccount + "')" + "\n"

			//Account group range:
			+ " AND (" + SMTableglaccountgroups.TableName + "." + SMTableglaccountgroups.sgroupcode + " >= '" + sStartingAccountGroupCode + "')" + "\n"
			+ " AND (" + SMTableglaccountgroups.TableName + "." + SMTableglaccountgroups.sgroupcode + " <= '" + sEndingAccountGroupCode + "')" + "\n"

			;

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

			sSQL += ")";

			sSQL += " ORDER BY " + SMTableglaccounts.TableName + "." + SMTableglaccounts.sAcctID
//				+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ifiscalyear
//				+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ifiscalperiod
//				+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ssourceledger
//				+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ssourcetype
//				+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.dattransactiondate
		;
		
		//This gives us a recordset of all the accounts we need to show in the listing
		try {
			ResultSet rsAccounts = clsDatabaseFunctions.openResultSet(sSQL, conn);
			long lRecordCounter = 0;
			while (rsAccounts.next()){
				lRecordCounter++;
				s += sProcessAccount(
					rsAccounts.getString(SMTableglaccounts.TableName + "." + SMTableglaccounts.sAcctID),
					rsAccounts.getString(SMTableglaccounts.TableName + "." + SMTableglaccounts.sDesc),
					sStartingFiscalYear,
					sEndingFiscalYear,
					sStartingPeriod,
					sEndingPeriod,
					iStartingFiscalPeriodProduct,
					iEndingFiscalPeriodProduct,
					conn,
					sDBID, 
					context,
					bAllowBatchViewing,
					sExternalPull,
					lRecordCounter
				);
			}
			rsAccounts.close();
		} catch (Exception e) {
			throw new Exception("Error [202064161333] " + " could not process accounts with SQL: '" + sSQL + " - " + e.getMessage());
		}
		
		return s;
	}
	
	private String sProcessAccount(
		String sAccount,
		String sAccountDescription,
		String sStartingFiscalYear,
		String sEndingFiscalYear,
		String sStartingFiscalPeriod,
		String sEndingFiscalPeriod,
		int iStartingFiscalPeriodProduct,
		int iEndingFiscalPeriodProduct,
		Connection conn,
		String sDBID, 
		ServletContext context,
		boolean bAllowBatchViewing,
		String sExternalPull,
		long lRecordCounter
		) throws Exception{
		
		String s = "";
		//For each account, first print the header line:
		
		//Unless it's the very first account, print a blank line first:
		if (lRecordCounter != 1){
			s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_WHITE + " \" >\n";
			
			s += "    <TD COLSPAN = 10 class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + " \" >"
				+  "&nbsp;"
				+ "</TD>\n"
							
				+ "  </TR>\n"
			; 
		}
		s += printAccountHeadingLine(
				sAccount, 
				sAccountDescription, 
				getStartingAccountBalance(
					conn,
					sAccount,
					sStartingFiscalYear,
					sStartingFiscalPeriod
				)
			);
		
		//Loop through the transactions for this account:
		String sSQL = "SELECT"
			+ " " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid + "\n"
			+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.bdamount + "\n"
			+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.dattransactiondate + "\n"
			+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ifiscalperiod + "\n"
			+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ifiscalyear + "\n"
			+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.loriginalbatchnumber + "\n"
			+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.loriginalentrynumber + "\n"
			+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.loriginallinenumber + "\n"
			+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sSourceledgertransactionlink + "\n"
			+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sdescription + "\n"
			+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sreference + "\n"
			+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ssourceledger + "\n"
			+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ssourcetype + "\n"
			//+ ", " + SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.bdnetchangeforperiod
			+ ", " + SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.bdopeningbalance + "\n"
			+ ", " + SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.bdtotalyeartodate + "\n"
			+ ", " + SMTableglaccounts.TableName + "." + SMTableglaccounts.sDesc + "\n"
			+ ", " + SMTableglaccounts.TableName + "." + SMTableglaccounts.inormalbalancetype + "\n"
			+ ", " + SMTableglaccounts.TableName + "." + SMTableglaccounts.sAcctType + "\n"
			+ " FROM " + SMTablegltransactionlines.TableName
			+ " LEFT JOIN " + SMTableglaccounts.TableName + " ON " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid
			+ " = " + SMTableglaccounts.TableName + "." + SMTableglaccounts.sAcctID
			+ " LEFT JOIN " + SMTableglfinancialstatementdata.TableName + " ON "
			+ "(" + SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.sacctid + " = "
			+ SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid + ")"
			+ " AND (" + SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.ifiscalyear + " = "
			+ SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ifiscalyear + ")"
			+ " AND (" + SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.ifiscalperiod + " = "
			+ SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ifiscalperiod + ")"
			;
		if(sExternalPull.compareToIgnoreCase("-1")!=0) {

			sSQL += " LEFT JOIN " + SMTableglexternalcompanypulls.TableName + "\n" 
			+ " ON " + SMTableglexternalcompanypulls.TableName + "." + SMTableglexternalcompanypulls.lid
			+ " = " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.lexternalcompanypullid + "\n"
			+ " WHERE ("
			+ "(" + SMTableglexternalcompanypulls.TableName + "." + SMTableglexternalcompanypulls.lid + " = " + sExternalPull + ")"
			+ "\n)";
		}else{

			sSQL +=  " WHERE (" + "\n"

				+ "("
				+ "((" + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ifiscalyear + " * 100) +  " 
				+ SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ifiscalperiod + ") >= " + iStartingFiscalPeriodProduct
				+ ")"

				+ " AND ("
				+ "((" + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ifiscalyear + " * 100) +  " 
				+ SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ifiscalperiod + ") <= " + iEndingFiscalPeriodProduct
				+ ")"

				//Account:
				+ " AND (" + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid + " = '" + sAccount + "')" + "\n"
				;

			//Include accounts with no activity?
			//if(!bIncludeAccountsWithNoActivity){
			//	sSQL += " AND ((" + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.bdtotalyeartodate
			//		+ " + " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.bdopeningbalance
			//		+ ")  != 0.00)" + "\n";
			//}
		}
		
		sSQL += ") ORDER BY " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid
				+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ifiscalyear
				+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ifiscalperiod
				+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ssourceledger
				+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ssourcetype
				+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.dattransactiondate
				+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.loriginalbatchnumber
				+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.loriginalentrynumber
				+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.loriginallinenumber
		;
		
		boolean bOddRow = true;
		String sStringBuffer = "";
		int iBufferLoopSize = 50;
		int iLoopCounter = 0;
		BigDecimal bdDebit = new BigDecimal("0.00");
		BigDecimal bdCredit = new BigDecimal("0.00");
		BigDecimal bdAmount = new BigDecimal("0.00");
		BigDecimal bdNetChangeForFiscalPeriod = new BigDecimal("0.00");
		BigDecimal bdEndingBalanceForPeriod = new BigDecimal("0.00");
		BigDecimal bdNetChangeForAccount = new BigDecimal("0.00");
		
		BigDecimal bdTotalDebitsForAccount = new BigDecimal("0.00");
		BigDecimal bdTotalCreditsForAccount = new BigDecimal("0.00");
		int iPreviousFiscalPeriod = 0;
		int iPreviousFiscalYear = 0;
		
		BigDecimal bdEndingBalanceForAccount = getEndingAccountBalance(
			conn,
			sAccount,
			Integer.parseInt(sEndingFiscalYear),
			Integer.parseInt(sEndingFiscalPeriod)
		);
		
		try {
			ResultSet rsTransactions = clsDatabaseFunctions.openResultSet(sSQL, conn);
			while(rsTransactions.next()){
				iLoopCounter++;
				//If the fiscal period has changed, print the fiscal period totals:
				if (
					(rsTransactions.getInt(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ifiscalperiod) != iPreviousFiscalPeriod)
					&& (iPreviousFiscalPeriod != 0)
				){
					sStringBuffer += printFiscalPeriodSubtotals(
						sAccount,
						sAccountDescription,
						iPreviousFiscalYear,
						iPreviousFiscalPeriod,
						bdNetChangeForFiscalPeriod,
						getEndingAccountBalance(
							conn,
							sAccount,
							iPreviousFiscalYear,
							iPreviousFiscalPeriod
						)
					);
					bdNetChangeForFiscalPeriod = BigDecimal.ZERO;
					bdEndingBalanceForPeriod = BigDecimal.ZERO;
				}
				
				bdDebit = BigDecimal.ZERO;
				bdCredit = BigDecimal.ZERO;
				bdAmount = rsTransactions.getBigDecimal(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.bdamount);
				
				//If the account is normally a debit balance:
				if (rsTransactions.getInt(SMTableglaccounts.TableName + "." + SMTableglaccounts.inormalbalancetype) == SMTableglaccounts.NORMAL_BALANCE_TYPE_DEBIT){
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
				bdNetChangeForAccount = bdNetChangeForAccount.add(bdAmount);
				
				bdNetChangeForFiscalPeriod = bdNetChangeForFiscalPeriod.add(bdAmount);
				//This value keeps being rewritten on every record within a fiscal period, but there's no harm in that:
				bdEndingBalanceForPeriod = rsTransactions.getBigDecimal(
					SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.bdopeningbalance).add(
							rsTransactions.getBigDecimal(SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.bdtotalyeartodate))
					;
				
				bdEndingBalanceForAccount = rsTransactions.getBigDecimal(
					SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.bdopeningbalance).add(
						rsTransactions.getBigDecimal(SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.bdtotalyeartodate))
					;
				
				String sBatchAndEntry = Long.toString(rsTransactions.getLong(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.loriginalbatchnumber)) 
					+ " - " 
					+ Long.toString(rsTransactions.getLong(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.loriginalentrynumber)
				);
				
				if (bAllowBatchViewing){
					sBatchAndEntry = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smgl.GLEditEntryEdit?"
						+ "lbatchnumber=" + Long.toString(rsTransactions.getLong(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.loriginalbatchnumber))
						+ "&lentrynumber=" + Long.toString(rsTransactions.getLong(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.loriginalentrynumber))
						//+ "&lid=" + Long.toString(rs.getLong(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.l))
						+ "&Editable=Yes"
						+ "&CallingClass=smgl.GLEditBatchesEdit"
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
						+ "\">" + sBatchAndEntry + "</A>"
					;
				}
				//Print a line for each transaction:
				sStringBuffer += printReportLine(
					rsTransactions.getString(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid),
					rsTransactions.getInt(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ifiscalyear),
					rsTransactions.getInt(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ifiscalperiod),
					rsTransactions.getString(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ssourceledger),
					rsTransactions.getString(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ssourcetype),
					ServletUtilities.clsDateAndTimeConversions.sqlDateToString(
						rsTransactions.getDate(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.dattransactiondate),
						ServletUtilities.clsDateAndTimeConversions.DATE_FORMAT_STD_Mdyyy),
					rsTransactions.getString(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sdescription),
					rsTransactions.getString(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sreference),
					sBatchAndEntry,
					bdDebit,
					bdCredit,
					rsTransactions.getString(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sSourceledgertransactionlink),
					context,
					sDBID,
					bOddRow
				);
				
				if ((iLoopCounter % iBufferLoopSize) == 0){
					s += sStringBuffer;
					sStringBuffer = "";
				}
				bOddRow = !bOddRow;
				
				iPreviousFiscalPeriod = rsTransactions.getInt(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ifiscalperiod);
				iPreviousFiscalYear = rsTransactions.getInt(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ifiscalyear);
			}
			rsTransactions.close();
		} catch (Exception e) {
			throw new Exception("Error [2020651222409] " + "reading transactions for account '" + sAccount + "' with SQL: '" + sSQL + "' - " + e.getMessage());
		}
		
		s += sStringBuffer;

		//If there were any transactions, print the last set of fiscal period subtotals:
		if (iLoopCounter != 0){
			s += printFiscalPeriodSubtotals(
				sAccount,
				sAccountDescription,
				iPreviousFiscalYear,
				iPreviousFiscalPeriod,
				bdNetChangeForFiscalPeriod,
				bdEndingBalanceForPeriod
			);
		}

		//Finally, print the totals line for the account:
		s += printAccountSubTotals(
			bdNetChangeForAccount,
			bdEndingBalanceForAccount,
			bdTotalDebitsForAccount,
			bdTotalCreditsForAccount,
			sAccount + " - " + sAccountDescription
		);
		
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
		ServletContext context,
		boolean bAllowBatchViewing,
		String sExternalPull) throws Exception{
		
		String s = "";
		
		s += printColumnHeadings();

		String sStartingFiscalYear = sStartingFiscalPeriod.substring(0, sStartingFiscalPeriod.indexOf(GLTransactionListingSelect.PARAM_VALUE_DELIMITER));
		String sStartingPeriod = sStartingFiscalPeriod.replace(sStartingFiscalYear + GLTransactionListingSelect.PARAM_VALUE_DELIMITER, "");
		String sEndingFiscalYear = sEndingFiscalPeriod.substring(0, sEndingFiscalPeriod.indexOf(GLTransactionListingSelect.PARAM_VALUE_DELIMITER));
		String sEndingPeriod = sEndingFiscalPeriod.replace(sEndingFiscalYear + GLTransactionListingSelect.PARAM_VALUE_DELIMITER, "");
		
		int iStartingFiscalPeriodProduct;
		try {
			iStartingFiscalPeriodProduct = (Integer.parseInt(sStartingFiscalYear) * 100) + Integer.parseInt(sStartingPeriod);
		} catch (Exception e) {
			throw new Exception("Error [20191981522491] " + "Could not parse starting fiscal year '" + sStartingFiscalYear 
				+ "', or starting fiscal period '" + sStartingFiscalPeriod + "'.");
		}
		int iEndingFiscalPeriodProduct;
		try {
			iEndingFiscalPeriodProduct = (Integer.parseInt(sEndingFiscalYear) * 100) + Integer.parseInt(sEndingPeriod);
		} catch (Exception e) {
			throw new Exception("Error [20191981522492] " + "Could not parse ending fiscal year '" + sEndingFiscalYear 
				+ "', or ending fiscal period '" + sEndingFiscalPeriod + "'.");
		}
		String sSQL = "SELECT" + "\n"
				+ " " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid + "\n"
				+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.bdamount
				+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.dattransactiondate
				+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ifiscalperiod
				+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ifiscalyear
				+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.loriginalbatchnumber
				+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.loriginalentrynumber
				+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.loriginallinenumber
				+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sSourceledgertransactionlink
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
				+ SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ifiscalperiod + ")";
		
		//Change the Where Clause when External Pull is not the Default Value
		//This disregards all the other inputs from the selections screen and will print the whole input from that external company pull
		if(sExternalPull.compareToIgnoreCase("-1")!=0) {

			sSQL += " LEFT JOIN " + SMTableglexternalcompanypulls.TableName + "\n" 
			+ " ON " + SMTableglexternalcompanypulls.TableName + "." + SMTableglexternalcompanypulls.lid
			+ " = " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.lexternalcompanypullid + "\n"
			+ " WHERE ("
			+ "(" + SMTableglexternalcompanypulls.TableName + "." + SMTableglexternalcompanypulls.lid + " = " + sExternalPull + ")"
			+ "\n)";
		}else{

			sSQL +=  " WHERE (" + "\n"

				+ "("
				+ "((" + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ifiscalyear + " * 100) +  " 
				+ SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ifiscalperiod + ") >= " + iStartingFiscalPeriodProduct
				+ ")"

				+ " AND ("
				+ "((" + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ifiscalyear + " * 100) +  " 
				+ SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ifiscalperiod + ") <= " + iEndingFiscalPeriodProduct
				+ ")"

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
			sSQL += ")";
		}

			sSQL += " ORDER BY " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid
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
		int iPreviousFiscalYear = 0;
		int iPreviousFiscalPeriod = 0;
		String sPreviousAccount = "";
		
		//System.out.println("[20191981532436] " + "001");
		
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
			while(rs.next()){
				
				//System.out.println("[1554320753] - into the loop...");
				
				//If the account has changed, print the account totals:
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
				
				//If the fiscal period has changed, print the fiscal period totals:
				if (
					(rs.getInt(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ifiscalperiod) != iPreviousFiscalPeriod)
					&& (iPreviousFiscalPeriod != 0)
				){
					sStringBuffer += printFiscalPeriodSubtotals(
						"",
						"",
						iPreviousFiscalYear,
						iPreviousFiscalPeriod,
						bdNetChangeForFiscalPeriod,
						bdEndingBalanceForPeriod
						);
					bdNetChangeForFiscalPeriod = BigDecimal.ZERO;
					bdEndingBalanceForPeriod = BigDecimal.ZERO;
				}
				
				//System.out.println("[1554320755] - into the loop...3");
				
				if (rs.getString(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid).compareTo(sPreviousAccount) != 0){
					sStringBuffer += printAccountHeadingLine(
						rs.getString(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid), 
						rs.getString(SMTableglaccounts.TableName + "." + SMTableglaccounts.sDesc), 
						getStartingAccountBalance(
							conn,
							rs.getString(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid),
							sStartingFiscalYear,
							sStartingPeriod
						)
					);
				}
				
				//System.out.println("[1554320756] - into the loop...4");
				
				bdDebit = BigDecimal.ZERO;
				bdCredit = BigDecimal.ZERO;
				bdAmount = rs.getBigDecimal(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.bdamount);
				
				//System.out.println("[1554320757] - into the loop...5");
				
				bdNetChangeForFiscalPeriod = bdNetChangeForFiscalPeriod.add(bdAmount);
				//System.out.println("[1554320758] - into the loop...6");
				
				//This value keeps being rewritten on every record within a fiscal period, but there's no harm in that:
				
//				System.out.println("[2019198154586] " + "Acct = '" + rs.getString(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid)
//					+ ", year = " + rs.getInt(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ifiscalyear)
//					+ ", period = " + rs.getInt(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ifiscalperiod)
//				);
//				
//				System.out.println("[20191981540566] " + "rs.getBigDecimal(" 
//						+ "SMTableglfinancialstatementdata.TableName .SMTableglfinancialstatementdata.bdopeningbalance) = " + rs.getBigDecimal(
//						SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.bdopeningbalance));
//				
//				System.out.println("[20191981540567] " + "rs.getBigDecimal(" 
//						+ "SMTableglfinancialstatementdata.TableName .SMTableglfinancialstatementdata.bdtotalyeartodate) = " + rs.getBigDecimal(
//						SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.bdtotalyeartodate));
				
				bdEndingBalanceForPeriod = rs.getBigDecimal(
						SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.bdopeningbalance).add(
								rs.getBigDecimal(SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.bdtotalyeartodate))
						;
				
				//System.out.println("[1554320757] - into the loop...10");
				
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
				
				String sBatchAndEntry = Long.toString(rs.getLong(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.loriginalbatchnumber)) 
					+ " - " 
					+ Long.toString(rs.getLong(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.loriginalentrynumber)
				);
				
				if (bAllowBatchViewing){
					sBatchAndEntry = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smgl.GLEditEntryEdit?"
						+ "lbatchnumber=" + Long.toString(rs.getLong(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.loriginalbatchnumber))
						+ "&lentrynumber=" + Long.toString(rs.getLong(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.loriginalentrynumber))
						//+ "&lid=" + Long.toString(rs.getLong(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.l))
						+ "&Editable=Yes"
						+ "&CallingClass=smgl.GLEditBatchesEdit"
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
						+ "\">" + sBatchAndEntry + "</A>"
					;
				}
				
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
					sBatchAndEntry,
					bdDebit,
					bdCredit,
					rs.getString(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sSourceledgertransactionlink),
					context,
					sDBID,
					bOddRow
				);
				
				if ((lRecordCounter % BUFFER_LOOP_LIMIT) == 0){
					s += sStringBuffer;
					sStringBuffer = "";
				}
				
				bOddRow = !bOddRow;
				lRecordCounter++;
				iPreviousFiscalPeriod = rs.getInt(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ifiscalperiod);
				iPreviousFiscalYear = rs.getInt(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.ifiscalyear);
				sPreviousAccount = rs.getString(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid);
				sPreviousAccountDescription = rs.getString(SMTableglaccounts.TableName + "." + SMTableglaccounts.sDesc);
			}
			rs.close();
		} catch (Exception e1) {
			throw new Exception("Error [1553381289] reading GL transactions with SQL: '" + sSQL + "' - " + e1.getMessage() + ".");
		}
		
		//System.out.println("[20191981532437] " + "002");
		
		s += sStringBuffer;
		
		s += printAccountSubTotals(
				bdNetChangeForAccount,
				bdEndingBalanceForAccount,
				bdTotalDebitsForAccount,
				bdTotalCreditsForAccount,
				sPreviousAccount + " - " + sPreviousAccountDescription
			);
		
		//Print the final fiscal period totals:
		s += printFiscalPeriodSubtotals(
			"",
			"",
			iPreviousFiscalYear,
			iPreviousFiscalPeriod,
			bdNetChangeForFiscalPeriod,
			bdEndingBalanceForPeriod
			);
	
		//add the last account balance:
		bdGrandBalanceTotal = bdGrandBalanceTotal.add(bdEndingBalanceForAccount);
		
		//System.out.println("[20191981532439] " + "004");
		
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
			String sSubledgerTransactionID,
			ServletContext context,
			String sDBID,
			boolean bOddRow
			) throws Exception{
		String s = "";
		
		try {
			if (bOddRow){
				s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTGREY + " \" >\n";
			}else{
				s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_WHITE + " \" >\n";
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
				//+  sDebit
				+ GLTransactionLinks.getSubledgerTransactionLink(sSourceLedger, sSubledgerTransactionID, context, sDBID, sDebit)
				+ "</TD>\n"
				
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + " \" >"
				//+  sCredit
				+ GLTransactionLinks.getSubledgerTransactionLink(sSourceLedger, sSubledgerTransactionID, context, sDBID, sCredit)
				+ "</TD>\n"

				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + " \" >"
				+  "&nbsp;"
				+ "</TD>\n"
				
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + " \" >"
				+  "&nbsp;"
				+ "</TD>\n"
				
				+ "  </TR>\n"
			;
		} catch (Exception e) {
			throw new Exception("Error [2019268151146] " + e.getMessage());
		}
		
		return s;
	}
	
	private String printFiscalPeriodSubtotals(
			String sAccount,
			String sAccountDesc,
			int iFiscalYear,
			int iFiscalPeriod,
			BigDecimal bdNetChange,
			BigDecimal bdBalance
			) throws Exception{
		String s = "";
		
		try {
			s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTBLUE + " \" >\n";
			
			s += "    <TD COLSPAN = 8 class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + " \" >"
				+  "<B>Account " + sAccount + " - " + sAccountDesc + " - Net Change and Ending Balance For Fiscal Year " + Integer.toString(iFiscalYear) + ", Period " + Integer.toString(iFiscalPeriod) + ":</B>"
				+ "</TD>\n"
								
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + " \" >"
				+  "<B>" + ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdNetChange) + "</B>"
				+ "</TD>\n"
				
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + " \" >"
				+  "<B>" + ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdBalance) + "</B>"
				+ "</TD>\n"
				
				+ "  </TR>\n"
			;
		} catch (Exception e) {
			throw new Exception("Error [20192681459572] " + e.getMessage());
		}
		
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

		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_YELLOW + " \" >\n";
		
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

			+ "    <TD COLSPAN=4 class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "<B>" + sDescription + "</B>"
			+ "</TD>"

			+ "    <TD COLSPAN=4 class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + " \" >"
			+  "<B>" + "Starting balance:" + "</B>"
			+ "</TD>"
			/*
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  "<B>" + "&nbsp;" + "</B>"
			+ "</TD>"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  "<B>" + "&nbsp;" + "</B>"
			+ "</TD>"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  "<B>" + "&nbsp;" + "</B>"
			+ "</TD>"
*/
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + " \" >"
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
			+  "Year<BR>- Period"
			+ "</TD>"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "Source"
			+ "</TD>"
			
			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "<a href=\"#documentdate\">Doc Date<SUP>1</SUP></a>"
			+ "</TD>"

			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "Line description/<BR>Line reference"
			+ "</TD>"

			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "Batch<BR>- entry"
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
				throw new Exception("Error [1554829782] - starting balance for account " 
					+ sAccount + " fiscal year " + sFiscalYear + ", period " + sStartingPeriod + " was not found.");
			}
		} catch (Exception e) {
			throw new Exception("Error [1554829783] getting starting balance for account " 
					+ sAccount + " fiscal year " + sFiscalYear + ", period " + sStartingPeriod + " - " + e.getMessage());
		}
		
		return bdStartingBalance;
		
	}
	
	private BigDecimal 	getEndingAccountBalance(
			Connection conn,
			String sAccount,
			int iFiscalYear,
			int iEndingPeriod
			) throws Exception{
			
			BigDecimal bdEndingBalance = new BigDecimal("0.00");
			String sSQL = "SELECT"
				+ " " + SMTableglfinancialstatementdata.bdopeningbalance
				+ ", " + SMTableglfinancialstatementdata.bdtotalyeartodate
				+ " FROM " + SMTableglfinancialstatementdata.TableName
				+ " WHERE ("
					+ "(" + SMTableglfinancialstatementdata.sacctid + " = '" + sAccount + "')"
					+ " AND (" + SMTableglfinancialstatementdata.ifiscalyear + " = " + Integer.toString(iFiscalYear) + ")"
					+ " AND (" + SMTableglfinancialstatementdata.ifiscalperiod + " = " + Integer.toString(iEndingPeriod) + ")"
				+ ")"
			;
			System.out.println("[202065134534] " + "SQL = '" + sSQL + "'");
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
				if (rs.next()){
					//Start with the opening balance for the year:
					bdEndingBalance = rs.getBigDecimal(SMTableglfinancialstatementdata.bdopeningbalance);
					//Add in the total year's changes to date:
					bdEndingBalance = bdEndingBalance.add(rs.getBigDecimal(SMTableglfinancialstatementdata.bdtotalyeartodate));
				}else{
					throw new Exception("Error [1554829882] - ending balance for account " 
						+ sAccount + " fiscal year " + Integer.toString(iFiscalYear) + ", period " + Integer.toString(iEndingPeriod) + " was not found.");
				}
			} catch (Exception e) {
				throw new Exception("Error [1554829883] getting ending balance for account " 
					+ sAccount + " fiscal year " + Integer.toString(iFiscalYear) + ", period " + Integer.toString(iEndingPeriod) + " - " + e.getMessage());
			}
			
			System.out.println("[202065134445] " + "bdEndingBalance for account '" + sAccount + "' = " + bdEndingBalance);
			
			return bdEndingBalance;
			
		}
	private String printTableHeading(){
		String s = "";
		
		s += "<TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\" >\n";
		
		return s;
	}

	private String printTableFooting(){
		String s = "";
		
		s += "</TABLE>\n";
		
		return s;
	}
}
