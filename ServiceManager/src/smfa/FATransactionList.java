package smfa;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import javax.servlet.ServletContext;

import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMTablefamaster;
import SMDataDefinition.SMTablefatransactions;
import ServletUtilities.clsDatabaseFunctions;
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
			String sLicenseModuleLevel
			){

		String sCurrentClass = "";
		int iCurrentYear = -1;
		int iCurrentPeriod = -1;
		
	    String sCombinedStartingFiscalPeriod = String.valueOf((Long.parseLong(sStartingFY) * 100) + Long.parseLong(sStartingFP));
	    String sCombinedEndingFiscalPeriod = String.valueOf((Long.parseLong(sEndingFY) * 100) +Long.parseLong(sEndingFP));
	    
	    String SQL = "SELECT" + 
	    				" " + SMTablefatransactions.TableName + "." + SMTablefatransactions.datTransactionDate + "," + 
	    				" " + SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalPeriod + "," + 
	    				" " + SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalYear + "," +
	    				" (" + SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalYear + " * 100) + " + SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalPeriod + " AS COMBINEDYEARANDPERIOD" + "," + 
	    				" " + SMTablefatransactions.TableName + "." + SMTablefatransactions.dAmountDepreciated + "," + 
						" " + SMTablefatransactions.TableName + "." + SMTablefatransactions.datPostingDate + "," + 
	    				" " + SMTablefatransactions.TableName + "." + SMTablefatransactions.iProvisionalPosting + "," + 
	    				" " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransAccumulatedDepreciationGLAcct + "," + 
	    				" " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + "," + 
	    				" " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransAssetNumber + "," + 
	    				" " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransComment + "," + 
	    				" " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransDepreciationGLAcct + "," + 
	    				" " + SMTablefamaster.TableName + "." + SMTablefamaster.sDescription + "," + 
	    				" " + SMTablefamaster.TableName + "." + SMTablefamaster.sClass + 
	    
	    			" FROM" +
	    				" " + SMTablefatransactions.TableName + "," + 
	    				" " + SMTablefamaster.TableName + 
	    
	    			" WHERE" +
	    				" " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransAssetNumber + " =" +
	    				" " + SMTablefamaster.TableName + "." + SMTablefamaster.sAssetNumber +
	    				" AND" +
	    				" " + SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalYear + " * 100 + " + 
	    					  SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalPeriod + " >= " +
	    					  sCombinedStartingFiscalPeriod + 
	    				" AND" +
	    				" " + SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalYear + " * 100 + " + 
		  					  SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalPeriod + " <= " +
		  					  sCombinedEndingFiscalPeriod;
	    
	        //Lay out all the possible combinations of the user-selected checkboxes:
	        if (!bPrintProvisional && !bPrintAdjustments && !bPrintActual){
	            //Nothing will print in this case:
	            SQL += " AND " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + "<> 'FA-ADJ'" +
	            	   " AND " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + "<> '" + SMTablefatransactions.DEPRECIATION_FLAG + "'";
	            
	        }else if (!bPrintProvisional && !bPrintAdjustments && bPrintActual){
	        	SQL += " AND " + SMTablefatransactions.TableName + "." + SMTablefatransactions.iProvisionalPosting + "=0" +
         	   		   " AND " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + "= '" + SMTablefatransactions.DEPRECIATION_FLAG + "'";
	        	
	        }else if (!bPrintProvisional && bPrintAdjustments && bPrintActual){
	        	SQL += " AND " + SMTablefatransactions.TableName + "." + SMTablefatransactions.iProvisionalPosting + "=0";
	        	
	        }else if (!bPrintProvisional && bPrintAdjustments && !bPrintActual){
	        	SQL += " AND " + SMTablefatransactions.TableName + "." + SMTablefatransactions.iProvisionalPosting + "=0" +
         	   		   " AND " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + "= '" + SMTablefatransactions.ADJUSTMENT_FLAG + "'";
	        	
	        }else if (bPrintProvisional && !bPrintAdjustments && !bPrintActual){
	            SQL += " AND " + SMTablefatransactions.TableName + "." + SMTablefatransactions.iProvisionalPosting + "=1" +
                	   " AND " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + "<> '" + SMTablefatransactions.ADJUSTMENT_FLAG + "'";
	        }else if (bPrintProvisional && !bPrintAdjustments && bPrintActual){
  	   		    SQL += " AND " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + "= '" + SMTablefatransactions.DEPRECIATION_FLAG + "'";
  	   		    
	        }else if (bPrintProvisional && bPrintAdjustments && bPrintActual){
	            //Don't need any qualifier here, because every type can print
	            
	        }else if (bPrintProvisional && bPrintAdjustments && !bPrintActual){
  	   		    SQL += " AND " + 
			    			"(" + SMTablefatransactions.TableName + "." + SMTablefatransactions.iProvisionalPosting + "=1" +
			    			" OR " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransactionType + "= '" + SMTablefatransactions.ADJUSTMENT_FLAG + "'" +
			    			")";
	        }
	        
	    //End WHERE clause . . .
	    
	    SQL += " ORDER BY " +
	    		" " + SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalYear + "," +
	    		" " + SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalPeriod + "," + 
	    		" " + SMTablefamaster.TableName + "." + SMTablefamaster.sClass + "," + 
	    		" " + SMTablefatransactions.TableName + "." + SMTablefatransactions.sTransAssetNumber;
        
        //System.out.println("SQL = " + SQL);

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
        out.println("<TABLE BORDER=0 cellspacing=0 cellpadding=1 WIDTH=100%>");
        
        try{
	        ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
	        Print_Column_Header(out);
	        
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
		        	}
	        	
	        	if (bShowDetail){
		        	Print_Transaction_Info(rs.getDate(SMTablefatransactions.datTransactionDate),
			        			   		   rs.getString(SMTablefatransactions.sTransAssetNumber),
			        			   		   rs.getString(SMTablefamaster.sDescription),
			        			   		   rs.getString(SMTablefatransactions.sTransactionType),
			        			   		   rs.getString(SMTablefatransactions.sTransDepreciationGLAcct),
			        			   		   rs.getString(SMTablefatransactions.sTransAccumulatedDepreciationGLAcct),
			        			   		   rs.getString(SMTablefatransactions.sTransComment),
			        			   		   rs.getInt(SMTablefatransactions.iProvisionalPosting),
			        			   		   rs.getBigDecimal(SMTablefatransactions.dAmountDepreciated),
			        			   		   bAllowAssetEditing,
			        			   		   out,
			        			   		   context,
			        			   		   sDBID
			        			   		   );
	        	}

	        	bdClassAmount = bdClassAmount.add(rs.getBigDecimal(SMTablefatransactions.dAmountDepreciated));
	        	bdFPAmount = bdFPAmount.add(rs.getBigDecimal(SMTablefatransactions.dAmountDepreciated));
	        	bdGrandAmount = bdGrandAmount.add(rs.getBigDecimal(SMTablefatransactions.dAmountDepreciated));
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
		
		out.println("<TR>" +
						"<TD style=\"border-style:solid; border-color:black; border-width:1px;\">Tran Date</TD>" +
						"<TD style=\"border-style:solid; border-color:black; border-width:1px;\">Asset#</TD>" +
						"<TD style=\"border-style:solid; border-color:black; border-width:1px;\">Description</TD>" +
						"<TD style=\"border-style:solid; border-color:black; border-width:1px;\">Tran Type</TD>" +
						"<TD style=\"border-style:solid; border-color:black; border-width:1px;\">Dep GL Acct</TD>" +
						"<TD style=\"border-style:solid; border-color:black; border-width:1px;\">Accu Dep GL Acct</TD>" +
						"<TD style=\"border-style:solid; border-color:black; border-width:1px;\">Comment</TD>" +
						"<TD style=\"border-style:solid; border-color:black; border-width:1px;\">Provisional?</TD>" +
						"<TD style=\"border-style:solid; border-color:black; border-width:1px;\">Amount</TD>" +
					"</TR>");
		
	}
	
	private void Print_Transaction_Info(Date datTranDate,
									    String sAssetNumber,
									    String sDesc,
									    String sTranType,
									    String sDepGLAcct,
									    String sAccuDepGLAcct,
									    String sComment,
									    int iProvisional,
									    BigDecimal bdAmount,
									    boolean bAllowAssetEditing,
									    PrintWriter out,
									    ServletContext context,
									    String sDBID
									    ){
		
		String sAssetLink = sAssetNumber;
		if (bAllowAssetEditing){
			sAssetLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smfa.FAEditAssetsEdit?" +
					"AssetNumber=" + sAssetNumber +
					"&SubmitEdit=1" + 
					"&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" + sAssetNumber + "</A>"
			;
		}
		
		out.println("<TR>" +
						"<TD ALIGN=CENTER>" + sdfDateOnly.format(datTranDate) + "</TD>" +
						"<TD ALIGN=CENTER>" + sAssetLink + "</TD>" +
						"<TD ALIGN=LEFT>" + sDesc + "</TD>" +
						"<TD ALIGN=LEFT>" + sTranType + "</TD>" +
						"<TD ALIGN=LEFT>" + sDepGLAcct + "</TD>" +
						"<TD ALIGN=LEFT>" + sAccuDepGLAcct + "</TD>" +
						"<TD ALIGN=LEFT>" + sComment + "</TD>" +
						"<TD ALIGN=LEFT>" + ((iProvisional == 1)?"Yes":"No") + "</TD>" +
						"<TD ALIGN=RIGHT>" + bdAmount.setScale(2, BigDecimal.ROUND_HALF_UP) + "</TD>" +
					"</TR>");
		
	}
	
	private void Print_Class_Totals(BigDecimal bdClassAmount,
									String sClass,
									PrintWriter out){

		out.println("<TR>" +
						"<TD COLSPAN=8>&nbsp;</TD><TD><HR></TD>" +
					"</TR><TR>" +
						"<TD ALIGN=RIGHT COLSPAN=8>Subtotal for class " + sClass + "</TD>" +
						"<TD ALIGN=RIGHT>" + bdClassAmount.setScale(2, BigDecimal.ROUND_HALF_UP) + "</TD>" +
					"</TR>" +
					"<TR><TD>&nbsp;</TD></TR>"
					);
		
		
	}
	
	private void Print_Period_Totals(BigDecimal bdFPAmount,
									 int iFY,
									 int iFP,
									 PrintWriter out){

		out.println("<TR>" +
						"<TD COLSPAN=8>&nbsp;</TD><TD><HR></TD>" +
					"</TR><TR>" +
						"<TD ALIGN=RIGHT COLSPAN=8>Subtotal for year " + iFY + ",period " + iFP + "</TD>" +
						"<TD ALIGN=RIGHT>" + bdFPAmount.setScale(2, BigDecimal.ROUND_HALF_UP) + "</TD>" +
					"</TR>" +
					"<TR><TD>&nbsp;</TD></TR>"
					);
		
		
	}
	
	private void Print_Grand_Totals(BigDecimal bdGrandAmount,
									PrintWriter out){

		out.println("<TR>" +
						"<TD COLSPAN=9>&nbsp;</TD>" + 
					"</TR><TR>" +
						"<TD COLSPAN=9><HR></TD>" + 
					"</TR><TR>" +
						"<TD ALIGN=RIGHT COLSPAN=8><FONT SIZE=3><B>GRAND TOTAL</B></FONT></TD>" +
						"<TD ALIGN=RIGHT><FONT SIZE=3><B>" + bdGrandAmount.setScale(2, BigDecimal.ROUND_HALF_UP) + "</B></FONT></TD>" +
					"</TR>");
		
	}

	private void Print_Class_Header(String sClass, 
								    String sClassDescription,
								    PrintWriter out){
		out.println("<TR>" +
						"<TD ALIGN=LEFT COLSPAN=9>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Asset Class:&nbsp;&nbsp;&nbsp;&nbsp;" + sClass + "</TD>" + // + "&nbsp;&nbsp;" + sClassDescription
					"</TR>");
	}

	private void Print_Period_Header(int iFY,
									 int iFP,
								     PrintWriter out){
		out.println("<TR>" + 
						"<TD ALIGN=LEFT COLSPAN=9>Fiscal Year:&nbsp;&nbsp;&nbsp;&nbsp;" + iFY + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Fiscal Period:&nbsp;&nbsp;&nbsp;&nbsp;" + iFP + "</TD>" + 
					"</TR>");
	}

	public String getErrorMessageString(){
		return m_sErrorMessage;
	}
}
