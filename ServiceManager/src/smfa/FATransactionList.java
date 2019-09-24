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
import SMDataDefinition.SMTablefamaster;
import SMDataDefinition.SMTablefatransactions;
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
		int iCurrentYear = -1;
		int iCurrentPeriod = -1;
		
	    String sCombinedStartingFiscalPeriod = String.valueOf((Long.parseLong(sStartingFY) * 100) + Long.parseLong(sStartingFP));
	    String sCombinedEndingFiscalPeriod = String.valueOf((Long.parseLong(sEndingFY) * 100) +Long.parseLong(sEndingFP));
	    
	    String SQL = "";
	    if (sGroupBy.compareToIgnoreCase(FATransactionListSelect.GROUPBY_VALUE_CLASS) == 0){
	    
		    SQL = "SELECT"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.datTransactionDate + " AS TRANSACTIONDATE," 
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalPeriod + " AS FISCALPERIOD," 
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalYear + " AS FISCALYEAR,"
					+ " (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalYear + " * 100) + " 
						+ SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalPeriod + " AS COMBINEDYEARANDPERIOD" + "," 
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.dAmountDepreciated + " AS AMTDEPRECIATED,"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.datPostingDate + " AS POSTINGDATE,"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.iProvisionalPosting + " AS PROVISIONALPOSTING,"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransAccumulatedDepreciationGLAcct + " AS ACCUMDEPACCT,"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + " AS TRANSACTIONTYPE,"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransAssetNumber + " AS ASSETNUMBER,"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransComment + " AS COMMENT,"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransDepreciationGLAcct + " AS DEPACCT,"
					+ " " + SMTablefamaster.TableName + "." + SMTablefamaster.sDescription + " AS DESCRIPTION,"
					+ " " + SMTablefamaster.TableName + "." + SMTablefamaster.sClass + " AS CLASS"
					+ ", " + SMTablefamaster.TableName + "." + SMTablefamaster.sLocation + " AS LOCATION"
	
				+ " FROM"
					+ " " + SMTablefatransactions.TableName + "," 
					+ " " + SMTablefamaster.TableName
	
				+ " WHERE ("
					+ " (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransAssetNumber + " ="
					+ " " + SMTablefamaster.TableName + "." + SMTablefamaster.sAssetNumber + ")"
					+ " AND"
					+ " (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalYear + " * 100 + " 
					+ 	  SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalPeriod + " >= "
					+ 	  sCombinedStartingFiscalPeriod + ")"
					+ " AND"
					+ " (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalYear + " * 100 + " 
					+ 	  SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalPeriod + " <= "
					+ 	  sCombinedEndingFiscalPeriod + ")";
	
		        //Lay out all the possible combinations of the user-selected checkboxes:
		        if (!bPrintProvisional && !bPrintAdjustments && !bPrintActual){
		            //Nothing will print in this case:
		            SQL += " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + "<> 'FA-ADJ')" 
		            	+ " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + "<> '" + SMTablefatransactions.DEPRECIATION_FLAG + "')";
		            
		        }else if (!bPrintProvisional && !bPrintAdjustments && bPrintActual){
		        	SQL += " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.iProvisionalPosting + "=0)"
	         	   		   + " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + "= '" + SMTablefatransactions.DEPRECIATION_FLAG + "')";
		        	
		        }else if (!bPrintProvisional && bPrintAdjustments && bPrintActual){
		        	SQL += " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.iProvisionalPosting + "=0)";
		        	
		        }else if (!bPrintProvisional && bPrintAdjustments && !bPrintActual){
		        	SQL += " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.iProvisionalPosting + "=0)"
	         	   		   + " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + "= '" + SMTablefatransactions.ADJUSTMENT_FLAG + "')";
		        	
		        }else if (bPrintProvisional && !bPrintAdjustments && !bPrintActual){
		            SQL += " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.iProvisionalPosting + "=1)"
	                	   + " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + "<> '" + SMTablefatransactions.ADJUSTMENT_FLAG + "')";
		        }else if (bPrintProvisional && !bPrintAdjustments && bPrintActual){
	  	   		    SQL += " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + "= '" + SMTablefatransactions.DEPRECIATION_FLAG + "')";
	  	   		    
		        }else if (bPrintProvisional && bPrintAdjustments && bPrintActual){
		            //Don't need any qualifier here, because every type can print
		            
		        }else if (bPrintProvisional && bPrintAdjustments && !bPrintActual){
	  	   		    SQL += " AND " 
				    		+ "(" + SMTablefatransactions.TableName + "." + SMTablefatransactions.iProvisionalPosting + "=1"
				    		+ " OR " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + "= '" + SMTablefatransactions.ADJUSTMENT_FLAG + "'"
				    		+ ")";
		        }
		        
	  	   		//Qualify by location:
		        if (!bShowAllLocations){
		  	   		SQL += " AND (";
		  	   		for (int i = 0; i < arrLocations.size(); i++){
		  	   			if (i > 0){
		  	   				SQL += " OR ";
		  	   			}
		  	   			SQL += "(" + SMTablefamaster.TableName + "." + SMTablefamaster.sLocation + " = '" + arrLocations.get(i) + "')";
		  	   		}
			        SQL += ")";
		        }
	  	   		
		    //End WHERE clause . . .
	  	   	SQL += ")";
	  	   		
		    SQL += " ORDER BY"
		    	+ " " + "FISCALYEAR" + ","
		    	+ " " + "FISCALPERIOD" + "," 
		    	+ " " + "CLASS" + ","
		    	+ " " + "ASSETNUMBER";
		    	/*
		    	SQL += " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalYear + ","
		    	    + " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalPeriod + "," 
		    	    + " " + SMTablefatransactions.TableName + "." + SMTablefatransactions. + ","
		    	    + " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransAssetNumber;
		    */
	    }else{
	    	//If we are grouping by GL Acct, then we need a UNION:
		    SQL = "SELECT"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.datTransactionDate + " AS TRANSACTIONDATE," 
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalPeriod + " AS FISCALPERIOD," 
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalYear + " AS FISCALYEAR,"
					+ " (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalYear + " * 100) + " 
						+ SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalPeriod + " AS COMBINEDYEARANDPERIOD" + "," 
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.dAmountDepreciated + " AS AMTDEPRECIATED,"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.datPostingDate + " AS POSTINGDATE,"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.iProvisionalPosting + " AS PROVISIONALPOSTING,"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransAccumulatedDepreciationGLAcct + " AS ACCUMDEPACCT,"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + " AS TRANSACTIONTYPE,"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransAssetNumber + " AS ASSETNUMBER,"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransComment + " AS COMMENT,"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransDepreciationGLAcct + " AS DEPACCT,"
					+ " 0.00 AS DEBITAMT"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.dAmountDepreciated + " AS CREDITAMT,"
					+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransAccumulatedDepreciationGLAcct + " AS GLACCT,"
					+ " " + SMTablefamaster.TableName + "." + SMTablefamaster.sDescription + " AS DESCRIPTION,"
					+ " " + SMTablefamaster.TableName + "." + SMTablefamaster.sClass + " AS CLASS"
					+ ", " + SMTablefamaster.TableName + "." + SMTablefamaster.sLocation + " AS LOCATION"
	
				+ " FROM"
					+ " " + SMTablefatransactions.TableName + "," 
					+ " " + SMTablefamaster.TableName
	
				+ " WHERE ("
					+ " (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransAssetNumber + " ="
					+ " " + SMTablefamaster.TableName + "." + SMTablefamaster.sAssetNumber + ")"
					+ " AND"
					+ " (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalYear + " * 100 + " 
					+ 	  SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalPeriod + " >= "
					+ 	  sCombinedStartingFiscalPeriod + ")"
					+ " AND"
					+ " (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalYear + " * 100 + " 
					+ 	  SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalPeriod + " <= "
					+ 	  sCombinedEndingFiscalPeriod + ")";
	
		        //Lay out all the possible combinations of the user-selected checkboxes:
		        if (!bPrintProvisional && !bPrintAdjustments && !bPrintActual){
		            //Nothing will print in this case:
		            SQL += " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + "<> 'FA-ADJ')" 
		            	+ " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + "<> '" + SMTablefatransactions.DEPRECIATION_FLAG + "')";
		            
		        }else if (!bPrintProvisional && !bPrintAdjustments && bPrintActual){
		        	SQL += " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.iProvisionalPosting + "=0)"
	         	   		   + " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + "= '" + SMTablefatransactions.DEPRECIATION_FLAG + "')";
		        	
		        }else if (!bPrintProvisional && bPrintAdjustments && bPrintActual){
		        	SQL += " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.iProvisionalPosting + "=0)";
		        	
		        }else if (!bPrintProvisional && bPrintAdjustments && !bPrintActual){
		        	SQL += " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.iProvisionalPosting + "=0)"
	         	   		   + " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + "= '" + SMTablefatransactions.ADJUSTMENT_FLAG + "')";
		        	
		        }else if (bPrintProvisional && !bPrintAdjustments && !bPrintActual){
		            SQL += " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.iProvisionalPosting + "=1)"
	                	   + " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + "<> '" + SMTablefatransactions.ADJUSTMENT_FLAG + "')";
		        }else if (bPrintProvisional && !bPrintAdjustments && bPrintActual){
	  	   		    SQL += " AND (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + "= '" + SMTablefatransactions.DEPRECIATION_FLAG + "')";
	  	   		    
		        }else if (bPrintProvisional && bPrintAdjustments && bPrintActual){
		            //Don't need any qualifier here, because every type can print
		            
		        }else if (bPrintProvisional && bPrintAdjustments && !bPrintActual){
	  	   		    SQL += " AND " 
				    		+ "(" + SMTablefatransactions.TableName + "." + SMTablefatransactions.iProvisionalPosting + "=1"
				    		+ " OR " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + "= '" + SMTablefatransactions.ADJUSTMENT_FLAG + "'"
				    		+ ")";
		        }
		        
	  	   		//Qualify by location:
		        if (!bShowAllLocations){
		  	   		SQL += " AND (";
		  	   		for (int i = 0; i < arrLocations.size(); i++){
		  	   			if (i > 0){
		  	   				SQL += " OR ";
		  	   			}
		  	   			SQL += "(" + SMTablefamaster.TableName + "." + SMTablefamaster.sLocation + " = '" + arrLocations.get(i) + "')";
		  	   		}
			        SQL += ")";
		        }
	  	   		
		    //End WHERE clause . . .
	  	   	SQL += ")";
	  	   		
		    SQL += " ORDER BY"
		    	+ " " + "FISCALYEAR" + ","
		    	+ " " + "FISCALPERIOD" + "," 
		    	+ " " + "GLACCT" + ","
		    	+ " " + "ASSETNUMBER";
	    }
        
	    System.out.println("[1548976073] - SQL = '" + SQL + "'");

        BigDecimal bdClassAmount = BigDecimal.ZERO;
        BigDecimal bdFPAmount = BigDecimal.ZERO;
        BigDecimal bdGrandAmount = BigDecimal.ZERO;
        
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
	        Print_Column_Header(out);
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
	        	if (sCurrentClass.length() == 0){
	        		sCurrentClass = rs.getString("CLASS");
	        		Print_Class_Header(sCurrentClass, 
	        						   rs.getString("CLASS"),
	        						   out);
	        	}
	        	
	        	//check period
	        	if (rs.getInt("FISCALYEAR")!= iCurrentYear || 
					rs.getInt("FISCALPERIOD") != iCurrentPeriod){
	        		Print_Class_Totals(bdClassAmount,
			 						   sCurrentClass,
			 						   out);
	        		Print_Period_Totals(bdFPAmount,
				        				iCurrentYear,
				        				iCurrentPeriod,
	        						    out);

	                bdClassAmount = BigDecimal.ZERO;
	                bdFPAmount = BigDecimal.ZERO;
		        	sCurrentClass = rs.getString("CLASS");
		        	iCurrentYear = rs.getInt("FISCALYEAR");
		        	iCurrentPeriod = rs.getInt("FISCALPERIOD");

		        	if (bShowDetail){
		        		Print_Column_Header(out);
		        	}
		        	Print_Period_Header(rs.getInt("FISCALYEAR"),
										rs.getInt("FISCALPERIOD"),
									    out);
		        	
	        		Print_Class_Header(sCurrentClass, 
			 						   rs.getString("CLASS"),
			 						   out);
	        	}else if (rs.getString("CLASS").compareTo(sCurrentClass) != 0){
		        		Print_Class_Totals(bdClassAmount,
		        						   sCurrentClass,
		        						   out);

		                bdClassAmount = BigDecimal.ZERO;
			        	sCurrentClass = rs.getString("CLASS");

			        	if (bShowDetail){
			        		Print_Column_Header(out);
			        	}
		        		Print_Class_Header(sCurrentClass, 
	 						   rs.getString("CLASS"),
	 						   out);
		        		iCount = 0;
		        	}
	        	
	        	if (bShowDetail){
		        	Print_Transaction_Info(rs.getDate("TRANSACTIONDATE"),
			        			   		   rs.getString("ASSETNUMBER"),
			        			   		   rs.getString("DESCRIPTION"),
			        			   		   rs.getString("TRANSACTIONTYPE"),
			        			   		   rs.getString("DEPACCT"),
			        			   		   rs.getString("ACCUMDEPACCT"),
			        			   		   rs.getString("COMMENT"),
			        			   		   rs.getString("LOCATION"),
			        			   		   rs.getInt("PROVISIONALPOSTING"),
			        			   		   rs.getBigDecimal("AMTDEPRECIATED"),
			        			   		   bAllowAssetEditing,
			        			   		   out,
			        			   		   context,
			        			   		   sDBID,
			        			   		   iCount
			        			   		   );
	        	}

	        	bdClassAmount = bdClassAmount.add(rs.getBigDecimal("AMTDEPRECIATED"));
	        	bdFPAmount = bdFPAmount.add(rs.getBigDecimal("AMTDEPRECIATED"));
	        	bdGrandAmount = bdGrandAmount.add(rs.getBigDecimal("AMTDEPRECIATED"));
	        	iCount++;
	        }
			rs.close();
	    	}catch (SQLException e){
	    		m_sErrorMessage = "Error reading resultset - " + e.getMessage();
	    		return false;
    	}
	    if (iHasRecord){
	    	Print_Class_Totals(bdClassAmount,
							   sCurrentClass,
							   out);
	    	
			Print_Period_Totals(bdFPAmount,
							    iCurrentYear,
							    iCurrentPeriod,
							    out);
	    }
	    Print_Grand_Totals(bdGrandAmount,
						   out);
	    out.println("</TABLE>");
	    //out.println(SQL);
		return true;
	}
	
	private void Print_Column_Header(PrintWriter out){
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
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B>Amount</B></TD>");
	    out.println("</TR>");
		
	}
	
	private void Print_Transaction_Info(Date datTranDate,
	    String sAssetNumber,
	    String sDesc,
	    String sTranType,
	    String sDepGLAcct,
	    String sAccuDepGLAcct,
	    String sComment,
	    String sLocation,
	    int iProvisional,
	    BigDecimal bdAmount,
	    boolean bAllowAssetEditing,
	    PrintWriter out,
	    ServletContext context,
	    String sDBID,
	    int iCount
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
	}
	
	private void Print_Class_Totals(BigDecimal bdClassAmount,
									String sClass,
									PrintWriter out){

		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_FOOTER + "\" >");
		out.println("<TD COLSPAN = \"9\"  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BREAK + "\" >&nbsp;</TD><TD><HR></TD>");
		out.println("</TR>");
		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_FOOTER + "\" >");
		out.println("<TD COLSPAN = \"9\"  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B> SUBTOTAL FOR CLASS " + sClass + ": </B></TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + bdClassAmount.setScale(2, BigDecimal.ROUND_HALF_UP) + "</TD>");
	    out.println("</TR>");
		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_FOOTER + "\" >");
		out.println("<TD COLSPAN = \"10\"  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BREAK + "\" >&nbsp;&nbsp;&nbsp;&nbsp;</TD>");
		out.println("</TR>");
		
		
	}
	
	private void Print_Period_Totals(BigDecimal bdFPAmount,
									 int iFY,
									 int iFP,
									 PrintWriter out){

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
		
		
	}
	
	private void Print_Grand_Totals(BigDecimal bdGrandAmount,
									PrintWriter out){
		

		
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

		
	}

	private void Print_Class_Header(String sClass, 
								    String sClassDescription,
								    PrintWriter out){
		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\" >");
		out.println("<TD COLSPAN = \"10\"  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BREAK + "\" >&nbsp;&nbsp;&nbsp;&nbsp;</TD>");
		out.println("</TR>");
		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\" >");
		out.println("<TD COLSPAN = \"10\"  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Asset Class:&nbsp;&nbsp;&nbsp;&nbsp;" + sClass + "</B></TD>");
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
