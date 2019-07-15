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
			boolean bPrintProvisional,
			boolean bPrintAdjustments,
			boolean bPrintActual,
			boolean bShowDetail,
			PrintWriter out,
			ServletContext context,
			String sLicenseModuleLevel,
			ArrayList<String>arrLocations
			){

		String sCurrentClass = "";
		int iCurrentYear = -1;
		int iCurrentPeriod = -1;
		
	    String sCombinedStartingFiscalPeriod = String.valueOf((Long.parseLong(sStartingFY) * 100) + Long.parseLong(sStartingFP));
	    String sCombinedEndingFiscalPeriod = String.valueOf((Long.parseLong(sEndingFY) * 100) +Long.parseLong(sEndingFP));
	    
	    String SQL = "SELECT"
	    				+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.datTransactionDate + "," 
	    				+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalPeriod + "," 
	    				+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalYear + ","
	    				+ " (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalYear + " * 100) + " 
	    					+ SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalPeriod + " AS COMBINEDYEARANDPERIOD" + "," 
	    				+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.dAmountDepreciated + ","
	    				+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.datPostingDate + ","
	    				+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.iProvisionalPosting + ","
	    				+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransAccumulatedDepreciationGLAcct + ","
	    				+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + ","
	    				+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransAssetNumber + ","
	    				+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransComment + ","
	    				+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransDepreciationGLAcct + ","
	    				+ " " + SMTablefamaster.TableName + "." + SMTablefamaster.sDescription + ","
	    				+ " " + SMTablefamaster.TableName + "." + SMTablefamaster.sClass
	    				+ ", " + SMTablefamaster.TableName + "." + SMTablefamaster.sLocation
	    
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
  	   		SQL += " AND (";
  	   		for (int i = 0; i < arrLocations.size(); i++){
  	   			if (i > 0){
  	   				SQL += " OR ";
  	   			}
  	   			SQL += "(" + SMTablefamaster.TableName + "." + SMTablefamaster.sLocation + " = '" + arrLocations.get(i) + "')";
  	   		}
	        SQL += ")";
  	   		
	    //End WHERE clause . . .
  	   	SQL += ")";
  	   		
	    SQL += " ORDER BY"
	    	+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalYear + ","
	    	+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalPeriod + "," 
	    	+ " " + SMTablefamaster.TableName + "." + SMTablefamaster.sClass + ","
	    	+ " " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransAssetNumber;
        
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
   	 out.println("<TABLE WIDTH = 100% CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITHOUT_BORDER + "\">");
        
        try{
	        ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
	        Print_Column_Header(out);
	        int iCount = 0;
	        while(rs.next()){
	        	iHasRecord = true;
	        	if (iCurrentPeriod < 0){
	        		iCurrentYear = rs.getInt(SMTablefatransactions.iFiscalYear);
	        		iCurrentPeriod = rs.getInt(SMTablefatransactions.iFiscalPeriod);
	        		Print_Period_Header(iCurrentYear,
	        							iCurrentPeriod,
	        						    out);
	        	}
	        	if (sCurrentClass.length() == 0){
	        		sCurrentClass = rs.getString(SMTablefamaster.sClass);
	        		Print_Class_Header(sCurrentClass, 
	        						   rs.getString(SMTablefamaster.sClass),
	        						   out);
	        	}
	        	
	        	//check period
	        	if (rs.getInt(SMTablefatransactions.iFiscalYear)!= iCurrentYear || 
					rs.getInt(SMTablefatransactions.iFiscalPeriod) != iCurrentPeriod){
	        		Print_Class_Totals(bdClassAmount,
			 						   sCurrentClass,
			 						   out);
	        		Print_Period_Totals(bdFPAmount,
				        				iCurrentYear,
				        				iCurrentPeriod,
	        						    out);

	                bdClassAmount = BigDecimal.ZERO;
	                bdFPAmount = BigDecimal.ZERO;
		        	sCurrentClass = rs.getString(SMTablefamaster.sClass);
		        	iCurrentYear = rs.getInt(SMTablefatransactions.iFiscalYear );
		        	iCurrentPeriod = rs.getInt(SMTablefatransactions.iFiscalPeriod);

		        	if (bShowDetail){
		        		Print_Column_Header(out);
		        	}
		        	Print_Period_Header(rs.getInt(SMTablefatransactions.iFiscalYear),
										rs.getInt(SMTablefatransactions.iFiscalPeriod),
									    out);
		        	
	        		Print_Class_Header(sCurrentClass, 
			 						   rs.getString(SMTablefamaster.sClass),
			 						   out);
	        	}else if (rs.getString(SMTablefamaster.sClass).compareTo(sCurrentClass) != 0){
		        		Print_Class_Totals(bdClassAmount,
		        						   sCurrentClass,
		        						   out);

		                bdClassAmount = BigDecimal.ZERO;
			        	sCurrentClass = rs.getString(SMTablefamaster.sClass);

			        	if (bShowDetail){
			        		Print_Column_Header(out);
			        	}
		        		Print_Class_Header(sCurrentClass, 
	 						   rs.getString(SMTablefamaster.sClass),
	 						   out);
		        		iCount = 0;
		        	}
	        	
	        	if (bShowDetail){
		        	Print_Transaction_Info(rs.getDate(SMTablefatransactions.datTransactionDate),
			        			   		   rs.getString(SMTablefatransactions.sTransAssetNumber),
			        			   		   rs.getString(SMTablefamaster.sDescription),
			        			   		   rs.getString(SMTablefatransactions.sTransactionType),
			        			   		   rs.getString(SMTablefatransactions.sTransDepreciationGLAcct),
			        			   		   rs.getString(SMTablefatransactions.sTransAccumulatedDepreciationGLAcct),
			        			   		   rs.getString(SMTablefatransactions.sTransComment),
			        			   		   rs.getString(SMTablefamaster.sLocation),
			        			   		   rs.getInt(SMTablefatransactions.iProvisionalPosting),
			        			   		   rs.getBigDecimal(SMTablefatransactions.dAmountDepreciated),
			        			   		   bAllowAssetEditing,
			        			   		   out,
			        			   		   context,
			        			   		   sDBID,
			        			   		   iCount
			        			   		   );
	        	}

	        	bdClassAmount = bdClassAmount.add(rs.getBigDecimal(SMTablefatransactions.dAmountDepreciated));
	        	bdFPAmount = bdFPAmount.add(rs.getBigDecimal(SMTablefatransactions.dAmountDepreciated));
	        	bdGrandAmount = bdGrandAmount.add(rs.getBigDecimal(SMTablefatransactions.dAmountDepreciated));
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
