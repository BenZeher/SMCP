package smfa;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.servlet.ServletContext;

import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTablefaclasses;
import SMDataDefinition.SMTablefamaster;
import SMDataDefinition.SMTablefatransactions;
import SMDataDefinition.SMTableglaccounts;
import ServletUtilities.clsDatabaseFunctions;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
public class FATransactionList extends java.lang.Object{

	private String m_sErrorMessage;
	private SimpleDateFormat sdfDateOnly = new SimpleDateFormat("MM/dd/yyyy");
	
	public FATransactionList(){
		m_sErrorMessage = "";
	}
	public boolean processReport(
			Connection conn,
			String sDBID,
			String sUserID,
			String sStartingFY,
			String sStartingFP,
			String sEndingFY,
			String sEndingFP,
			String sGroupBy,
			boolean bPrintProvisional,
			boolean bPrintAdjustments,
			boolean bPrintActual,
			boolean bShowDetail,
			PrintWriter out,
			ServletContext context,
			String sLicenseModuleLevel,
			ArrayList<String>arrLocations,
			boolean bShowAllLocations
			){

		String sCurrentClass = "";
		String sCurrentGLAcct = "";
		String sCurrentGroupDescription = "";
		int iCurrentYear = -1;
		int iCurrentPeriod = -1;
		
	    String sCombinedStartingFiscalPeriod = String.valueOf((Long.parseLong(sStartingFY) * 100) + Long.parseLong(sStartingFP));
	    String sCombinedEndingFiscalPeriod = String.valueOf((Long.parseLong(sEndingFY) * 100) +Long.parseLong(sEndingFP));
	    
	    String SQL = "";
	    if (sGroupBy.compareToIgnoreCase(FATransactionListSelect.GROUPBY_VALUE_CLASS) == 0){
		    SQL = "SELECT" + "\n"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.datTransactionDate + " AS TRANSACTIONDATE," + "\n"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalPeriod + " AS FISCALPERIOD," + "\n"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalYear + " AS FISCALYEAR," + "\n"
					+ " (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalYear + " * 100) + " + "\n"
						+ SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalPeriod + " AS COMBINEDYEARANDPERIOD" + "," + "\n" 
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.dAmountDepreciated + " AS AMTDEPRECIATED," + "\n"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.datPostingDate + " AS POSTINGDATE," + "\n"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.iProvisionalPosting + " AS PROVISIONALPOSTING," + "\n"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransAccumulatedDepreciationGLAcct + " AS ACCUMDEPACCT," + "\n"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + " AS TRANSACTIONTYPE," + "\n"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransAssetNumber + " AS ASSETNUMBER," + "\n"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransComment + " AS COMMENT," + "\n"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransDepreciationGLAcct + " AS DEPACCT," + "\n"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransAccumulatedDepreciationGLAcct + " AS GLACCT," + "\n"
					+ " 0.00 AS DEBITAMT," + "\n"
					+ " 0.00 AS CREDITAMT," + "\n"
					+ " " + SMTablefamaster.TableName + "." + SMTablefamaster.sDescription + " AS DESCRIPTION," + "\n"
					+ " " + SMTablefamaster.TableName + "." + SMTablefamaster.sClass + " AS CLASS," + "\n"
					+ " " + SMTablefamaster.TableName + "." + SMTablefamaster.sLocation + " AS LOCATION," + "\n"
					+ " " + SMTablefaclasses.TableName + "." + SMTablefaclasses.sClassDescription + " AS GROUPDESCRIPTION" + "\n"
	
				+ " FROM"
					+ " " + SMTablefatransactions.TableName + "," 
					+ " " + SMTablefamaster.TableName + "\n"
					+ " LEFT JOIN " + SMTablefaclasses.TableName + "\n" 
					+ " ON " + SMTablefamaster.TableName + "." + SMTablefamaster.sClass + " = " + SMTablefaclasses.TableName + "." + SMTablefaclasses.sClass
	
				+ " WHERE (" + "\n"
					+ " (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransAssetNumber + " ="
					+ " " + SMTablefamaster.TableName + "." + SMTablefamaster.sAssetNumber + ")" + "\n"
					+ " AND"
					+ " (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalYear + " * 100 + " 
					+ 	  SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalPeriod + " >= "
					+ 	  sCombinedStartingFiscalPeriod + ")" + "\n"
					+ " AND"
					+ " (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalYear + " * 100 + " 
					+ 	  SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalPeriod + " <= "
					+ 	  sCombinedEndingFiscalPeriod + ")" + "\n";
	
		        //Lay out all the possible combinations of the user-selected checkboxes:
		        if (!bPrintProvisional && !bPrintAdjustments && !bPrintActual){
		            //Nothing will print in this case:
		            SQL += " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + "<> 'FA-ADJ')" + "\n" 
		            	+ " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + "<> '" + SMTablefatransactions.DEPRECIATION_FLAG + "')" + "\n";
		            
		        }else if (!bPrintProvisional && !bPrintAdjustments && bPrintActual){
		        	SQL += " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.iProvisionalPosting + "=0)" + "\n"
	         	   		   + " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + "= '" + SMTablefatransactions.DEPRECIATION_FLAG + "')" + "\n";
		        	
		        }else if (!bPrintProvisional && bPrintAdjustments && bPrintActual){
		        	SQL += " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.iProvisionalPosting + "=0)" + "\n";
		        	
		        }else if (!bPrintProvisional && bPrintAdjustments && !bPrintActual){
		        	SQL += " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.iProvisionalPosting + "=0)" + "\n"
	         	   		   + " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + "= '" + SMTablefatransactions.ADJUSTMENT_FLAG + "')" + "\n";
		        	
		        }else if (bPrintProvisional && !bPrintAdjustments && !bPrintActual){
		            SQL += " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.iProvisionalPosting + "=1)" + "\n"
	                	   + " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + "<> '" + SMTablefatransactions.ADJUSTMENT_FLAG + "')" + "\n";
		        }else if (bPrintProvisional && !bPrintAdjustments && bPrintActual){
	  	   		    SQL += " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + "= '" + SMTablefatransactions.DEPRECIATION_FLAG + "')" + "\n";
	  	   		    
		        }else if (bPrintProvisional && bPrintAdjustments && bPrintActual){
		            //Don't need any qualifier here, because every type can print
		            
		        }else if (bPrintProvisional && bPrintAdjustments && !bPrintActual){
	  	   		    SQL += " AND " 
				    		+ "(" + SMTablefatransactions.TableName + "." + SMTablefatransactions.iProvisionalPosting + "=1"
				    		+ " OR " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + "= '" + SMTablefatransactions.ADJUSTMENT_FLAG + "'"
				    		+ ")" + "\n";
		        }
		        
	  	   		//Qualify by location:
		        if (!bShowAllLocations){
		  	   		SQL += " AND (" + "\n";
		  	   		for (int i = 0; i < arrLocations.size(); i++){
		  	   			if (i > 0){
		  	   				SQL += " OR ";
		  	   			}
		  	   			SQL += "(" + SMTablefamaster.TableName + "." + SMTablefamaster.sLocation + " = '" + arrLocations.get(i) + "')" + "\n";
		  	   		}
			        SQL += ")" + "\n";
		        }
	  	   		
		    //End WHERE clause . . .
	  	   	SQL += ")" + "\n";
	  	   		
		    SQL += " ORDER BY"
		    	+ " " + "FISCALYEAR" + ","
		    	+ " " + "FISCALPERIOD" + "," 
		    	+ " " + "CLASS" + ","
		    	+ " " + "ASSETNUMBER" + "\n";
		    	/*
		    	SQL += " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalYear + ","
		    	    + " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalPeriod + "," 
		    	    + " " + SMTablefatransactions.TableName + "." + SMTablefatransactions. + ","
		    	    + " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransAssetNumber;
		    */
	    }else{
	    	//If we are grouping by GL Acct, then we need a UNION:
	    	SQL = "SELECT" + "\n"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.datTransactionDate + " AS TRANSACTIONDATE," + "\n" 
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalPeriod + " AS FISCALPERIOD," + "\n"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalYear + " AS FISCALYEAR," + "\n"
					+ " (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalYear + " * 100) + " 
						+ SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalPeriod + " AS COMBINEDYEARANDPERIOD" + "," + "\n" 
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.dAmountDepreciated + " AS AMTDEPRECIATED," + "\n"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.datPostingDate + " AS POSTINGDATE," + "\n"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.iProvisionalPosting + " AS PROVISIONALPOSTING," + "\n"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransAccumulatedDepreciationGLAcct + " AS ACCUMDEPACCT," + "\n"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + " AS TRANSACTIONTYPE," + "\n"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransAssetNumber + " AS ASSETNUMBER," + "\n"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransComment + " AS COMMENT," + "\n"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransDepreciationGLAcct + " AS DEPACCT," + "\n"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.dAmountDepreciated + " AS DEBITAMT," + "\n"
					+ " 0.00 AS CREDITAMT," + "\n"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransDepreciationGLAcct + " AS GLACCT," + "\n"
					+ " " + SMTablefamaster.TableName + "." + SMTablefamaster.sDescription + " AS DESCRIPTION," + "\n"
					+ " " + SMTablefamaster.TableName + "." + SMTablefamaster.sClass + " AS CLASS," + "\n"
					+ " " + SMTablefamaster.TableName + "." + SMTablefamaster.sLocation + " AS LOCATION," + "\n"
					+ " " + SMTableglaccounts.TableName + "." + SMTableglaccounts.sDesc + " AS GROUPDESCRIPTION" + "\n"
				+ " FROM"
				+ " " + SMTablefatransactions.TableName 
				+ " LEFT JOIN " + SMTablefamaster.TableName + "\n"
				+ " ON " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransAssetNumber + " ="
					+ " " + SMTablefamaster.TableName + "." + SMTablefamaster.sAssetNumber + "\n"
				+ " LEFT JOIN " + SMTableglaccounts.TableName + "\n" 
				+ " ON " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransDepreciationGLAcct 
					+ " = " + SMTableglaccounts.TableName + "." + SMTableglaccounts.sAcctID + "\n"
			+ " WHERE (" + "\n"
				+ " (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalYear + " * 100 + " 
				+ 	  SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalPeriod + " >= "
				+ 	  sCombinedStartingFiscalPeriod + ")" + "\n"
				+ " AND"
				+ " (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalYear + " * 100 + " 
				+ 	  SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalPeriod + " <= "
				+ 	  sCombinedEndingFiscalPeriod + ")" + "\n";
	
		        //Lay out all the possible combinations of the user-selected checkboxes:
		        if (!bPrintProvisional && !bPrintAdjustments && !bPrintActual){
		            //Nothing will print in this case:
		            SQL += " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + "<> 'FA-ADJ')" + "\n" 
		            	+ " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + "<> '" + SMTablefatransactions.DEPRECIATION_FLAG + "')" + "\n";
		            
		        }else if (!bPrintProvisional && !bPrintAdjustments && bPrintActual){
		        	SQL += " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.iProvisionalPosting + "=0)" + "\n"
	         	   		   + " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + "= '" + SMTablefatransactions.DEPRECIATION_FLAG + "')" + "\n";
		        	
		        }else if (!bPrintProvisional && bPrintAdjustments && bPrintActual){
		        	SQL += " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.iProvisionalPosting + "=0)" + "\n";
		        	
		        }else if (!bPrintProvisional && bPrintAdjustments && !bPrintActual){
		        	SQL += " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.iProvisionalPosting + "=0)" + "\n"
	         	   		   + " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + "= '" + SMTablefatransactions.ADJUSTMENT_FLAG + "')" + "\n";
		        	
		        }else if (bPrintProvisional && !bPrintAdjustments && !bPrintActual){
		            SQL += " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.iProvisionalPosting + "=1)" + "\n"
	                	   + " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + "<> '" + SMTablefatransactions.ADJUSTMENT_FLAG + "')" + "\n";
		        }else if (bPrintProvisional && !bPrintAdjustments && bPrintActual){
	  	   		    SQL += " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + "= '" + SMTablefatransactions.DEPRECIATION_FLAG + "')" + "\n";
	  	   		    
		        }else if (bPrintProvisional && bPrintAdjustments && bPrintActual){
		            //Don't need any qualifier here, because every type can print
		            
		        }else if (bPrintProvisional && bPrintAdjustments && !bPrintActual){
	  	   		    SQL += " AND " 
				    		+ "(" + "(" + SMTablefatransactions.TableName + "." + SMTablefatransactions.iProvisionalPosting + "=1)"
				    		+ " OR (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + "= '" + SMTablefatransactions.ADJUSTMENT_FLAG + "')"
				    		+ ")";
		        }
		        
	  	   		//Qualify by location:
		        if (!bShowAllLocations){
		  	   		SQL += " AND (" + "\n";
		  	   		for (int i = 0; i < arrLocations.size(); i++){
		  	   			if (i > 0){
		  	   				SQL += " OR ";
		  	   			}
		  	   			SQL += "(" + SMTablefamaster.TableName + "." + SMTablefamaster.sLocation + " = '" + arrLocations.get(i) + "')" + "\n";
		  	   		}
			        SQL += ")" + "\n";
		        }
	  	   		
		    //End WHERE clause . . .
	  	   	SQL += ")" + "\n"
	  	   			
   			+ " UNION ALL " + "\n" + "\n"
  	   			
  	   		+ "SELECT" + "\n"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.datTransactionDate + " AS TRANSACTIONDATE," + "\n"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalPeriod + " AS FISCALPERIOD," + "\n"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalYear + " AS FISCALYEAR," + "\n"
					+ " (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalYear + " * 100) + " 
						+ SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalPeriod + " AS COMBINEDYEARANDPERIOD" + "," + "\n" 
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.dAmountDepreciated + " AS AMTDEPRECIATED," + "\n"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.datPostingDate + " AS POSTINGDATE," + "\n"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.iProvisionalPosting + " AS PROVISIONALPOSTING," + "\n"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransAccumulatedDepreciationGLAcct + " AS ACCUMDEPACCT," + "\n"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + " AS TRANSACTIONTYPE," + "\n"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransAssetNumber + " AS ASSETNUMBER," + "\n"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransComment + " AS COMMENT," + "\n"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransDepreciationGLAcct + " AS DEPACCT," + "\n"
					+ " 0.00 AS DEBITAMT," + "\n"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.dAmountDepreciated + " AS CREDITAMT," + "\n"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransAccumulatedDepreciationGLAcct + " AS GLACCT," + "\n"
					+ " " + SMTablefamaster.TableName + "." + SMTablefamaster.sDescription + " AS DESCRIPTION," + "\n"
					+ " " + SMTablefamaster.TableName + "." + SMTablefamaster.sClass + " AS CLASS," + "\n"
					+ " " + SMTablefamaster.TableName + "." + SMTablefamaster.sLocation + " AS LOCATION," + "\n"
					+ " " + SMTableglaccounts.TableName + "." + SMTableglaccounts.sDesc + " AS GROUPDESCRIPTION" + "\n"
				+ " FROM"
					+ " " + SMTablefatransactions.TableName 
					+ " LEFT JOIN " + SMTablefamaster.TableName + "\n"
					+ " ON " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransAssetNumber + " ="
						+ " " + SMTablefamaster.TableName + "." + SMTablefamaster.sAssetNumber + "\n"
					+ " LEFT JOIN " + SMTableglaccounts.TableName + "\n" 
					+ " ON " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransAccumulatedDepreciationGLAcct 
						+ " = " + SMTableglaccounts.TableName + "." + SMTableglaccounts.sAcctID + "\n"
				+ " WHERE (" + "\n"
					+ " (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalYear + " * 100 + " 
					+ 	  SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalPeriod + " >= "
					+ 	  sCombinedStartingFiscalPeriod + ")" + "\n"
					+ " AND"
					+ " (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalYear + " * 100 + " 
					+ 	  SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalPeriod + " <= "
					+ 	  sCombinedEndingFiscalPeriod + ")" + "\n";
	
		        //Lay out all the possible combinations of the user-selected checkboxes:
		        if (!bPrintProvisional && !bPrintAdjustments && !bPrintActual){
		            //Nothing will print in this case:
		            SQL += " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + "<> 'FA-ADJ')" + "\n" 
		            	+ " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + "<> '" + SMTablefatransactions.DEPRECIATION_FLAG + "')" + "\n";
		            
		        }else if (!bPrintProvisional && !bPrintAdjustments && bPrintActual){
		        	SQL += " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.iProvisionalPosting + "=0)" + "\n"
	         	   		   + " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + "= '" + SMTablefatransactions.DEPRECIATION_FLAG + "')" + "\n";
		        	
		        }else if (!bPrintProvisional && bPrintAdjustments && bPrintActual){
		        	SQL += " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.iProvisionalPosting + "=0)" + "\n";
		        	
		        }else if (!bPrintProvisional && bPrintAdjustments && !bPrintActual){
		        	SQL += " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.iProvisionalPosting + "=0)" + "\n"
	         	   		   + " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + "= '" + SMTablefatransactions.ADJUSTMENT_FLAG + "')" + "\n";
		        	
		        }else if (bPrintProvisional && !bPrintAdjustments && !bPrintActual){
		            SQL += " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.iProvisionalPosting + "=1)" + "\n"
	                	   + " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + "<> '" + SMTablefatransactions.ADJUSTMENT_FLAG + "')" + "\n";
		        }else if (bPrintProvisional && !bPrintAdjustments && bPrintActual){
	  	   		    SQL += " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + "= '" + SMTablefatransactions.DEPRECIATION_FLAG + "')" + "\n";
	  	   		    
		        }else if (bPrintProvisional && bPrintAdjustments && bPrintActual){
		            //Don't need any qualifier here, because every type can print
		            
		        }else if (bPrintProvisional && bPrintAdjustments && !bPrintActual){
	  	   		    SQL += " AND " 
				    		+ "(" + "(" + SMTablefatransactions.TableName + "." + SMTablefatransactions.iProvisionalPosting + "=1)"
				    		+ " OR (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + "= '" + SMTablefatransactions.ADJUSTMENT_FLAG + "')"
				    		+ ")";
		        }
		        
	  	   		//Qualify by location:
		        if (!bShowAllLocations){
		  	   		SQL += " AND (" + "\n";
		  	   		for (int i = 0; i < arrLocations.size(); i++){
		  	   			if (i > 0){
		  	   				SQL += " OR ";
		  	   			}
		  	   			SQL += "(" + SMTablefamaster.TableName + "." + SMTablefamaster.sLocation + " = '" + arrLocations.get(i) + "')" + "\n";
		  	   		}
			        SQL += ")" + "\n";
		        }
	  	   		
		    //End WHERE clause . . .
	  	   	SQL += ")" + "\n";
	  	   	;
	  	   		
		    SQL += " ORDER BY"
		    	+ " " + "FISCALYEAR" + ","
		    	+ " " + "FISCALPERIOD" + "," 
		    	+ " " + "GLACCT" + ","
		    	+ " " + "ASSETNUMBER";
	    }
        
	    //System.out.println("[1548976073] - SQL = '" + SQL + "'");

        BigDecimal bdGroupAmount = BigDecimal.ZERO;
        BigDecimal bdGroupDebitAmount = BigDecimal.ZERO;
        BigDecimal bdGroupCreditAmount = BigDecimal.ZERO;
        BigDecimal bdFPTransactionAmount = BigDecimal.ZERO;
        BigDecimal bdFPDebitAmount = BigDecimal.ZERO;
        BigDecimal bdFPCreditAmount = BigDecimal.ZERO;
        BigDecimal bdGrandAmount = BigDecimal.ZERO;
        BigDecimal bdGrandDebitAmount = BigDecimal.ZERO;
        BigDecimal bdGrandCreditAmount = BigDecimal.ZERO;
        
        boolean iHasRecord = false;
        boolean bAllowAssetEditing = SMSystemFunctions.isFunctionPermitted(
        		SMSystemFunctions.FAManageAssets, 
        		sUserID, 
        		conn,
        		sLicenseModuleLevel);
        //print table header
   	 	out.println("<TABLE WIDTH = 100% CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\">");
        
        try{
	        ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
	        Print_Column_Header(out, sGroupBy);
	        
	        int iCount = 0;
	        while(rs.next()){
	        	iHasRecord = true;
	        	if (iCurrentPeriod < 0){
	        		iCurrentYear = rs.getInt("FISCALYEAR");
	        		iCurrentPeriod = rs.getInt("FISCALPERIOD");
	        		Print_Period_Header(iCurrentYear,
	        							iCurrentPeriod,
	        						    out);
	        	}
	        	if (
	        		(sCurrentClass.length() == 0)
	        		|| (sCurrentGLAcct.length() == 0)
	        		
	        	){
	        		sCurrentClass = rs.getString("CLASS");
	        		sCurrentGLAcct = rs.getString("GLACCT");
	        		sCurrentGroupDescription = rs.getString("GROUPDESCRIPTION");
	        		Print_Group_Header(
	        			out, 
	        			sGroupBy, 
	        			sCurrentClass, 
	        			sCurrentGLAcct,
	        			sCurrentGroupDescription		
	        		);
	        	}
	        	
	        	//check period - if it's changed, then print the totals, then the headers again:
	        	if (rs.getInt("FISCALYEAR")!= iCurrentYear || 
					rs.getInt("FISCALPERIOD") != iCurrentPeriod){
	        		Print_Group_Totals(
	        			bdGroupAmount,
	        			sCurrentGroupDescription,
						sCurrentClass,
						sCurrentGLAcct,
						bdGroupDebitAmount,
						bdGroupCreditAmount,
						sGroupBy,
						out);
	        		
	        		Print_Period_Totals(
	        			bdFPTransactionAmount,
	        			bdFPDebitAmount,
	    				bdFPCreditAmount,
	    				iCurrentYear,
				        iCurrentPeriod,
				        sGroupBy,
	        			out);

	                bdGroupAmount = BigDecimal.ZERO;
	                bdGroupDebitAmount = BigDecimal.ZERO;
	                bdGroupCreditAmount = BigDecimal.ZERO;
	                bdFPTransactionAmount = BigDecimal.ZERO;
	                bdFPDebitAmount = BigDecimal.ZERO;
	                bdFPCreditAmount = BigDecimal.ZERO;
		        	sCurrentClass = rs.getString("CLASS");
		        	sCurrentGLAcct = rs.getString("GLACCT");
		        	sCurrentGroupDescription = rs.getString("GROUPDESCRIPTION");
		        	iCurrentYear = rs.getInt("FISCALYEAR");
		        	iCurrentPeriod = rs.getInt("FISCALPERIOD");

		        	if (bShowDetail){
		        		Print_Column_Header(out, sGroupBy);
		        	}
		        	Print_Period_Header(rs.getInt("FISCALYEAR"),
										rs.getInt("FISCALPERIOD"),
									    out);
		        	
	        		Print_Group_Header(
		        			out, 
		        			sGroupBy, 
		        			sCurrentClass, 
		        			sCurrentGLAcct,
		        			sCurrentGroupDescription		
		        		);
	        	}else if (
	        			((rs.getString("CLASS").compareTo(sCurrentClass) != 0) && (sGroupBy.compareToIgnoreCase(FATransactionListSelect.GROUPBY_VALUE_CLASS) == 0))
	        			|| ((rs.getString("GLACCT").compareTo(sCurrentGLAcct) != 0) && (sGroupBy.compareToIgnoreCase(FATransactionListSelect.GROUPBY_VALUE_GL) == 0))
	        	){
	        		Print_Group_Totals(
		        			bdGroupAmount,
		        			sCurrentGroupDescription,
							sCurrentClass,
							sCurrentGLAcct,
							bdGroupDebitAmount,
							bdGroupCreditAmount,
							sGroupBy,
							out);

		                bdGroupAmount = BigDecimal.ZERO;
		                bdGroupDebitAmount = BigDecimal.ZERO;
		                bdGroupCreditAmount = BigDecimal.ZERO;
			        	sCurrentClass = rs.getString("CLASS");
			        	sCurrentGLAcct = rs.getString("GLACCT");
			        	sCurrentGroupDescription = rs.getString("GROUPDESCRIPTION");
			        	if (bShowDetail){
			        		Print_Column_Header(out, sGroupBy);
			        	}
		        		Print_Group_Header(
			        			out, 
			        			sGroupBy, 
			        			sCurrentClass, 
			        			sCurrentGLAcct,
			        			sCurrentGroupDescription		
			        		);
		        	}
	        	
	        	if (bShowDetail){
		        	Print_Transaction_Info(rs.getDate("TRANSACTIONDATE"),
			        			   		   rs.getString("ASSETNUMBER"),
			        			   		   rs.getString("DESCRIPTION"),
			        			   		   rs.getString("TRANSACTIONTYPE"),
			        			   		   rs.getString("DEPACCT"),
			        			   		   rs.getString("ACCUMDEPACCT"),
			        			   		   rs.getString("GLACCT"),
			        			   		   rs.getString("COMMENT"),
			        			   		   rs.getString("LOCATION"),
			        			   		   rs.getInt("PROVISIONALPOSTING"),
			        			   		   rs.getBigDecimal("AMTDEPRECIATED"),
			        			   		   rs.getBigDecimal("DEBITAMT"),
			        			   		   rs.getBigDecimal("CREDITAMT"),
			        			   		   bAllowAssetEditing,
			        			   		   out,
			        			   		   context,
			        			   		   sDBID,
			        			   		   iCount,
			        			   		   sGroupBy
			        			   		   );
	        	}

	        	bdGroupAmount = bdGroupAmount.add(rs.getBigDecimal("AMTDEPRECIATED"));
	        	bdGroupDebitAmount = bdGroupDebitAmount.add(rs.getBigDecimal("DEBITAMT"));
	        	bdGroupCreditAmount = bdGroupCreditAmount.add(rs.getBigDecimal("CREDITAMT"));
	        	bdFPTransactionAmount = bdFPTransactionAmount.add(rs.getBigDecimal("AMTDEPRECIATED"));
	        	bdFPDebitAmount = bdFPDebitAmount.add(rs.getBigDecimal("DEBITAMT"));
	        	bdFPCreditAmount = bdFPCreditAmount.add(rs.getBigDecimal("CREDITAMT"));
	        	bdGrandAmount = bdGrandAmount.add(rs.getBigDecimal("AMTDEPRECIATED"));
	        	bdGrandDebitAmount = bdGrandDebitAmount.add(rs.getBigDecimal("DEBITAMT"));
	        	bdGrandCreditAmount = bdGrandCreditAmount.add(rs.getBigDecimal("CREDITAMT"));
	        	iCount++;
	        }
			rs.close();
	    	}catch (SQLException e){
	    		m_sErrorMessage = "Error [1569348478] reading resultset with SQL '" + SQL + "' - " + e.getMessage();
	    		return false;
    	}
	    if (iHasRecord){
    		Print_Group_Totals(
        			bdGroupAmount,
        			sCurrentGroupDescription,
					sCurrentClass,
					sCurrentGLAcct,
					bdGroupDebitAmount,
					bdGroupCreditAmount,
					sGroupBy,
					out);
	    	
			Print_Period_Totals(
				bdFPTransactionAmount,
				bdFPDebitAmount,
				bdFPCreditAmount,
				iCurrentYear,
				iCurrentPeriod,
				sGroupBy,
				out);
	    }
	    Print_Grand_Totals(
	    	bdGrandAmount,
	    	bdGrandDebitAmount,
	    	bdGrandCreditAmount,
			out,
			sGroupBy);
	    out.println("</TABLE>");
	    //out.println(SQL);
		return true;
	}
	private void Print_Column_Header(PrintWriter out, String sGroupBy){
        if (sGroupBy.compareToIgnoreCase(FATransactionListSelect.GROUPBY_VALUE_CLASS) == 0){
        	Print_Column_Header_For_Classes(out);
        }else{
        	Print_Column_Header_For_GLAccts(out);
        }
	}
	private void Print_Group_Header(
			PrintWriter out, 
			String sGroupBy, 
			String sClass, 
		    String sGLAcct,
		    String sGroupDescription){
		
        if (sGroupBy.compareToIgnoreCase(FATransactionListSelect.GROUPBY_VALUE_CLASS) == 0){
        	Print_Class_Header(sClass + " - " + sGroupDescription, out);
        }else{
        	Print_GLAcct_Header(sGLAcct + " - " + sGroupDescription, out);
        }
	}
	private void Print_Column_Header_For_Classes(PrintWriter out){
		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\" >");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B>Tran Date</B></TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B>Location</B></TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B>Asset#</B></TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B>Description</B></TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B>Tran Type</B></TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B>Dep GL Acct</B></TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B>Accu Dep GL Acct</B></TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B>Comment</B></TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B>Provisional</B></TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B>Amount</B></TD>");
	    out.println("</TR>");
		
	}
	private void Print_Column_Header_For_GLAccts(PrintWriter out){
		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\" >");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B>Tran Date</B></TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B>Location</B></TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B>Asset#</B></TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B>Description</B></TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B>Tran Type</B></TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B>GL Acct</B></TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B>Comment</B></TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B>Provisional</B></TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B>Debit</B></TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B>Credit</B></TD>");
	    out.println("</TR>");
		
	}
	
	private void Print_Transaction_Info(Date datTranDate,
	    String sAssetNumber,
	    String sDesc,
	    String sTranType,
	    String sDepGLAcct,
	    String sAccuDepGLAcct,
	    String sGLAcct,
	    String sComment,
	    String sLocation,
	    int iProvisional,
	    BigDecimal bdAmount,
	    BigDecimal bdDebitAmount,
	    BigDecimal bdCreditAmount,
	    boolean bAllowAssetEditing,
	    PrintWriter out,
	    ServletContext context,
	    String sDBID,
	    int iCount,
	    String sGroupBy
	    ){
		
		String sAssetLink = sAssetNumber;
		if (bAllowAssetEditing){
			sAssetLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smfa.FAEditAssetsEdit?" +
					"AssetNumber=" + sAssetNumber +
					"&SubmitEdit=1" + 
					"&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" + sAssetNumber + "</A>"
			;
		}
		
		if(iCount % 2 == 0) {
			out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\" >");
		}else {
			out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\" >");
		}

		if (sGroupBy.compareToIgnoreCase(FATransactionListSelect.GROUPBY_VALUE_CLASS) == 0){
			out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + sdfDateOnly.format(datTranDate) + "</TD>");
			out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + sLocation + "</TD>");
			out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + sAssetLink + "</TD>");
			out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + sDesc + "</TD>");
			out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + sTranType + "</TD>");
			out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + sDepGLAcct + "</TD>");
			out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + sAccuDepGLAcct + "</TD>");
			out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + sComment + "</TD>");
			out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" +  ((iProvisional == 1)?"Yes":"No") + "</TD>");
			out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + bdAmount.setScale(2, BigDecimal.ROUND_HALF_UP) + "</TD>");
		    out.println("</TR>");
		}else{
			out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + sdfDateOnly.format(datTranDate) + "</TD>");
			out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + sLocation + "</TD>");
			out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + sAssetLink + "</TD>");
			out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + sDesc + "</TD>");
			out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + sTranType + "</TD>");
			out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + sGLAcct + "</TD>");
			out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + sComment + "</TD>");
			out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" +  ((iProvisional == 1)?"Yes":"No") + "</TD>");
			out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + bdDebitAmount.setScale(2, BigDecimal.ROUND_HALF_UP) + "</TD>");
			out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + bdCreditAmount.setScale(2, BigDecimal.ROUND_HALF_UP) + "</TD>");
		    out.println("</TR>");
		}
	}
	
	private void Print_Group_Totals(
		BigDecimal bdClassAmount,
		String sGroupDescription,
		String sClass,
		String sGLAcct,
		BigDecimal bdDebitAmt,
		BigDecimal bdCreditAmt,
		String sGroupBy,
		PrintWriter out){
	
		if (sGroupBy.compareToIgnoreCase(FATransactionListSelect.GROUPBY_VALUE_CLASS) == 0){
	   		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_FOOTER + "\" >");
			out.println("<TD COLSPAN = \"9\"  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BREAK + "\" >&nbsp;</TD><TD><HR></TD>");
			out.println("</TR>");
			out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_FOOTER + "\" >");
			out.println("<TD COLSPAN = \"9\"  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER 
				+ "\" ><B> SUBTOTAL FOR CLASS " + sClass + " - " + sGroupDescription + ": </B></TD>");
			out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + bdClassAmount.setScale(2, BigDecimal.ROUND_HALF_UP) + "</TD>");
		    out.println("</TR>");
			out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_FOOTER + "\" >");
			out.println("<TD COLSPAN = \"10\"  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BREAK + "\" >&nbsp;&nbsp;&nbsp;&nbsp;</TD>");
			out.println("</TR>");
        }else{
    		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_FOOTER + "\" >");
    		out.println("<TD COLSPAN = \"8\"  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BREAK + "\" >&nbsp;</TD><TD COLSPAN=2 ><HR></TD>");
    		out.println("</TR>");
    		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_FOOTER + "\" >");
    		out.println("<TD COLSPAN = \"8\"  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER 
    				+ "\" ><B> SUBTOTAL FOR GL ACCT " + sGLAcct + " - " + sGroupDescription + ": </B></TD>");
    		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + bdDebitAmt.setScale(2, BigDecimal.ROUND_HALF_UP) + "</TD>");
    		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + bdCreditAmt.setScale(2, BigDecimal.ROUND_HALF_UP) + "</TD>");
    		out.println("</TR>");
    		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_FOOTER + "\" >");
    		out.println("<TD COLSPAN = \"10\"  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BREAK + "\" >&nbsp;&nbsp;&nbsp;&nbsp;</TD>");
    		out.println("</TR>");
        }
	}
	
	private void Print_Period_Totals(
		BigDecimal bdFPAmount,
		BigDecimal bdFPDebitAmt,
		BigDecimal bdFPCreditAmt,
		int iFY,
		int iFP,
		String sGroupBy,
		PrintWriter out){

		if (sGroupBy.compareToIgnoreCase(FATransactionListSelect.GROUPBY_VALUE_CLASS) == 0){
			out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_FOOTER + "\" >");
			out.println("<TD COLSPAN = \"9\"  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BREAK + "\" >&nbsp;</TD><TD><HR></TD>");
			out.println("</TR>");
			out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_FOOTER + "\" >");
			out.println("<TD COLSPAN = \"9\"  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B> SUBTOTAL FOR YEAR " + iFY + ", PERIOD " + iFP + ": </B></TD>");
			out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + bdFPAmount.setScale(2, BigDecimal.ROUND_HALF_UP) + "</TD>");
		    out.println("</TR>");
			out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_FOOTER + "\" >");
			out.println("<TD COLSPAN = \"10\"  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BREAK + "\" >&nbsp;&nbsp;&nbsp;&nbsp;</TD>");
			out.println("</TR>");
		}else{
			out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_FOOTER + "\" >");
			out.println("<TD COLSPAN = \"8\"  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BREAK + "\" >&nbsp;</TD><TD><HR></TD>");
			out.println("</TR>");
			out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_FOOTER + "\" >");
			out.println("<TD COLSPAN = \"8\"  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B> SUBTOTAL FOR YEAR " + iFY + ", PERIOD " + iFP + ": </B></TD>");
			out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + bdFPDebitAmt.setScale(2, BigDecimal.ROUND_HALF_UP) + "</TD>");
			out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + bdFPCreditAmt.setScale(2, BigDecimal.ROUND_HALF_UP) + "</TD>");
		    out.println("</TR>");
			out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_FOOTER + "\" >");
			out.println("<TD COLSPAN = \"10\"  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BREAK + "\" >&nbsp;&nbsp;&nbsp;&nbsp;</TD>");
			out.println("</TR>");
		}
	}
	
	private void Print_Grand_Totals(
		BigDecimal bdGrandAmount,
		BigDecimal bdGrandDebitAmount,
		BigDecimal bdGrandCreditAmount,
		PrintWriter out,
		String sGroupBy){
		
		if (sGroupBy.compareToIgnoreCase(FATransactionListSelect.GROUPBY_VALUE_CLASS) == 0){
			out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\" >");
			out.println("<TD COLSPAN = \"10\"  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BREAK + "\" >&nbsp;</TD>");
			out.println("</TR>");
			out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\" >");
			out.println("<TD COLSPAN = \"9\"  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B> GRAND TOTAL: </B></TD>");
			out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + bdGrandAmount.setScale(2, BigDecimal.ROUND_HALF_UP) + "</TD>");
		    out.println("</TR>");
			out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\" >");
			out.println("<TD COLSPAN = \"10\"  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BREAK + "\" >&nbsp;&nbsp;&nbsp;&nbsp;</TD>");
			out.println("</TR>");
		} else {
			out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\" >");
			out.println("<TD COLSPAN = \"10\"  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BREAK + "\" >&nbsp;</TD>");
			out.println("</TR>");
			out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\" >");
			out.println("<TD COLSPAN = \"8\"  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B> GRAND TOTAL: </B></TD>");
			out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + bdGrandDebitAmount.setScale(2, BigDecimal.ROUND_HALF_UP) + "</TD>");
			out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + bdGrandCreditAmount.setScale(2, BigDecimal.ROUND_HALF_UP) + "</TD>");
		    out.println("</TR>");
			out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\" >");
			out.println("<TD COLSPAN = \"10\"  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BREAK + "\" >&nbsp;&nbsp;&nbsp;&nbsp;</TD>");
			out.println("</TR>");
		}

		
	}

	private void Print_Class_Header(
			String sClassDescription,
			PrintWriter out){
		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\" >");
		out.println("<TD COLSPAN = \"10\"  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BREAK + "\" >&nbsp;&nbsp;&nbsp;&nbsp;</TD>");
		out.println("</TR>");
		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\" >");
		out.println("<TD COLSPAN = \"10\"  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER 
			+ "\" ><B>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Asset Class:&nbsp;&nbsp;&nbsp;&nbsp;" + sClassDescription + "</B></TD>");
		out.println("</TR>");
	}
	
	private void Print_GLAcct_Header(
		    String sGLAcctDescription,
		    PrintWriter out){
			out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\" >");
			out.println("<TD COLSPAN = \"10\"  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BREAK + "\" >&nbsp;&nbsp;&nbsp;&nbsp;</TD>");
			out.println("</TR>");
			out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\" >");
			out.println("<TD COLSPAN = \"10\"  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER 
				+ "\" ><B>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;GL Account:&nbsp;&nbsp;&nbsp;&nbsp;" + sGLAcctDescription + "</B></TD>");
			out.println("</TR>");
}
	private void Print_Period_Header(int iFY,
									 int iFP,
								     PrintWriter out){
		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\" >");
		out.println("<TD COLSPAN = \"10\"  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B>Fiscal Year:&nbsp;&nbsp;&nbsp;&nbsp;" + iFY + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Fiscal Period:&nbsp;&nbsp;&nbsp;&nbsp;" + iFP + "</B></TD>");
		out.println("</TR>");

	}

	public String getErrorMessageString(){
		return m_sErrorMessage;
	}
}
