package smic;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMRecreateExportAction;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import ConnectionPool.WebContextParameters;
import SMClasses.SMBatchStatuses;
import SMClasses.SMModuleTypes;
import SMDataDefinition.SMTableicbatchentries;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsStringFunctions;

public class ICEditBatchesEdit extends HttpServlet {

	private static final long serialVersionUID = 1L;
	/*
	 * Parameters:
	 * BatchNumber - batch number
	 * BatchType - batch type - an integer passed as a string
	 */
	private static String sObjectName = "Batch";
	private static String sDBID = "";
	private static String sUserName = "";
	private static String sUserID = "";
	private static String sUserFullName = "";
	private static String sCompanyName = "";
	
	//public static String CREATEGLBATCH_BUTTON_NAME = "CREATEGLBATCH";
	//public static String CREATEGLBATCH_BUTTON_LABEL = "Re-create GL batch";
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ICEditBatches))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    
		String title = "";
		String subtitle = "";
		
    	//User has chosen to edit:
		if (request.getParameter("BatchNumber") != null){
			if (request.getParameter("BatchNumber").compareToIgnoreCase("-1") == 0){
				title = "Add new batch";
			}else{
				title = "Edit " + sObjectName + ": " + (String) request.getParameter("BatchNumber");
			}
		}
		out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
		
	    //Display any warnings:
	    if (request.getParameter("Warning") != null){
	    	String sWarning = request.getParameter("Warning");
	    	if (!sWarning.equalsIgnoreCase("")){
	    		out.println("<B><FONT COLOR=\"RED\">" + sWarning + "</FONT></B><BR>");
	    	}
	    }
	    //Display any status messages:
	    if (request.getParameter("Status") != null){
	    	String sStatus = request.getParameter("Status");
	    	if (!sStatus.equalsIgnoreCase("")){
	    		out.println("<B>" + sStatus + "</B><BR>");
	    	}
	    }
	    
	    //Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Inventory Main Menu</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditBatches?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Batch List</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICEditBatches) 
	    		+ "\">Summary</A><BR>");
	    
	    if (request.getParameter("BatchNumber") == null){
	    	out.println("Invalid batch number passed.");
	    	out.println("</BODY></HTML>");
	    	return;
	    }
	    ICEntryBatch batch = new ICEntryBatch((String) request.getParameter("BatchNumber"));

	    if (request.getParameter("BatchType") == null){
	    	out.println("Invalid batch type passed.");
	    	out.println("</BODY></HTML>");
	    	return;
	    }

	    if (! batch.sBatchType((String) request.getParameter("BatchType"))){
	    	out.println("Invalid batch type passed.");
	    	out.println("</BODY></HTML>");
	    	return;
	    }
	    
		if (batch.lBatchNumber() != -1){
			if (!batch.load(getServletContext(), sDBID)){
				out.println("Could not load batch number " + batch.sBatchNumber() + ".");
			}
		}
	    
		//Get the IC export type:
		ICOption icopt = new ICOption();
		try {
			icopt.load(sDBID, getServletContext(), sUserName);
		} catch (Exception e) {
			out.println("<FONT COLOR=RED><B><BR>Error [1474644838] reading IC Options to get export type - " 
				+ icopt.getErrorMessage()
				+ " <BR></B></FONT>");
		}
		
    	//If it's a posted batch, we need to add a button for creating a bank rec:
        if (batch.iBatchStatus() == SMBatchStatuses.POSTED){
        	out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMRecreateExportAction"
        		+ "?" + SMRecreateExportAction.BATCHNUMBER_PARAM + "=" + batch.sBatchNumber()
        		+ "&" + SMRecreateExportAction.BATCHLABEL_PARAM + "=" + batch.sBatchTypeLabel()
        		+ "&" + SMRecreateExportAction.SOURCELEDGER_PARAM + "=" + SMModuleTypes.IC
        		+ "&" + SMRecreateExportAction.EXPORTTYPE_PARAM + "=" + icopt.getExportTo()
        		+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
        		+ "&" + SMRecreateExportAction.OTHER_PARAMETERS_PARAM + "="
        			+ "*BatchNumber=" + batch.sBatchNumber()
        			+ "*BatchType=" + batch.sBatchType()
           		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">Re-create export file</A><BR><BR>");
        }
		
	    Edit_Record(batch, sUserName, out, request, sDBID, false);
	    out.println(getJavaScript());
		out.println("</BODY></HTML>");
	}
	
	public String getJavaScript() {
		String s = "<script>";
		 s +=  "   window.addEventListener(\"beforeunload\",function(){\n" 
				  +  "     document.body.setAttribute(\"style\",\"pointer-events: none; color: black; cursor: not-allowed;\");\n"
				  +  "     document.documentElement.style.cursor = \"wait\";\n"
				  +"      });\n";
		 s += "</script>";
		return s;
	}
	
	private void Edit_Record(
			ICEntryBatch batch,
			String sUserName,
			PrintWriter pwOut, 
			HttpServletRequest req,
			String sConf,
			boolean bAddNew){
		
		pwOut.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditBatchesUpdate' METHOD='POST'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"BatchNumber\" VALUE=\"" + batch.sBatchNumber() + "\">");
		
		//Store the fields:
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"BatchType\" VALUE=\"" + batch.sBatchType() + "\">");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ICEntryBatch.ibatchstatus 
				+ "\" VALUE=\"" + batch.sBatchStatus() + "\">");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ICEntryBatch.screatedbyfullname 
				+ "\" VALUE=\"" + batch.sGetCreatedByFullName() + "\">");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ICEntryBatch.lcreatedbyid 
				+ "\" VALUE=\"" + batch.sGetCreatedByID() + "\">");
		
        //Display fields:
        //Date last maintained:
        pwOut.println("<B>" + batch.sBatchTypeLabel() + "</B> batch");
        pwOut.println(" last edited <B>" + batch.sGetLastEditDate() + "</B>");
        pwOut.println(" by user <B>" + batch.sGetLastEditedByFullName() + "</B><BR>");
        pwOut.println("Batch Date: <B>"+ batch.getBatchDate() + "</B>");
        pwOut.println("     Status: <B>"+ batch.sBatchStatusLabel() + "</B>");
        
        if (batch.iBatchStatus() == SMBatchStatuses.POSTED){
        	pwOut.println(" on <B>"+ batch.getPostingDateString() + "</B>");
        }
        pwOut.println("<BR>");
        
        if (batch.bEditable()){
            //Description:
            pwOut.println(clsCreateHTMLFormFields.Create_Edit_Form_Text_Input_Field(
            		ICEntryBatch.sbatchdescription, 
            		clsStringFunctions.filter(batch.sBatchDescription()),  
            		ICEntryBatch.sBatchDescriptionLength, 
            		"Description: ", 
            		"")
            );
            
            //Add an editable cell for the batch date:
    		pwOut.println(clsCreateHTMLFormFields.Create_Edit_Form_Text_Input_Field(
    				ICEntryBatch.datbatchdate, 
            		clsStringFunctions.filter(batch.getBatchDateInStdFormat()), 
            		10, 
            		"Batch&nbsp;date:", 
            		""
            		)
            );
    		
    		pwOut.println(SMUtilities.getDatePickerString(ICEntryBatch.datbatchdate, getServletContext()));

            //Here, we warn the user if we are near the end of the month:
            int iToday = Integer.parseInt(clsDateAndTimeConversions.now("d"));
	    	if ((iToday > 27) || (iToday < 4)
	    		){
	    		pwOut.println("<BR><B><FONT COLOR=\"RED\">" 
	    			+ "WARNING! It's after the 27th or before the 4 of the month"
	    				+ " - review the document dates below "
	    			+ "and make sure they are correct."
	    			+ "</FONT></B>");
	    	}
            
        	pwOut.println("<BR><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Save "
        			+ sObjectName + "' STYLE='height: 0.24in'>");
        	
        	if (
        			(batch.iBatchStatus() == SMBatchStatuses.ENTERED)
        			|| (batch.iBatchStatus() == SMBatchStatuses.IMPORTED)
        	){
        		pwOut.println("  <INPUT TYPE=SUBMIT NAME='Post' VALUE='Post " + sObjectName + "' STYLE='height: 0.24in'>");
        		pwOut.println("  Check to confirm posting: <INPUT TYPE=CHECKBOX NAME=\"ConfirmPost\">");
        	}
        	pwOut.println("  <INPUT TYPE=SUBMIT NAME='Delete' VALUE='Delete " + sObjectName + "' STYLE='height: 0.24in'>");
        	pwOut.println("  Check to confirm deletion: <INPUT TYPE=CHECKBOX NAME=\"ConfirmDelete\">");
        	
        	pwOut.println("</FORM>");
        	
        	//IF it's a new batch, we don't want to display all the batch details on it:
        	if (batch.lBatchNumber() == -1){
        		return;
        	}

        	//Add links to create new entries:
    		if (batch.iBatchType() == ICBatchTypes.IC_ADJUSTMENT){
        		pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditAdjustmentEntry?" 
	    	    		+ "BatchNumber=" + batch.sBatchNumber()
	    	    		+ "&BatchType=" + batch.sBatchType()
	    	    		+ "&DocumentType=" + ICBatchTypes.IC_ADJUSTMENT
	    	    		+ "&Editable=Yes"
	    	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    	    		+ "\">"
	    	    		+ "Create adjustment entry</A>"
	    	    		);
        		pwOut.println("&nbsp&nbsp");
    		}
    		if (batch.iBatchType() == ICBatchTypes.IC_RECEIPT){
        		pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditReceiptEntry?" 
	    	    		+ "BatchNumber=" + batch.sBatchNumber()
	    	    		+ "&BatchType=" + batch.sBatchType()
	    	    		+ "&DocumentType=" + ICBatchTypes.IC_RECEIPT
	    	    		+ "&Editable=Yes"
	    	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    	    		+ "\">"
	    	    		+ "Create receipt entry</A>"
	    	    		);

    		pwOut.println("&nbsp&nbsp");
    		}
    		if (batch.iBatchType() == ICBatchTypes.IC_SHIPMENT){
        		pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditShipmentEntry?" 
	    	    		+ "BatchNumber=" + batch.sBatchNumber()
	    	    		+ "&BatchType=" + batch.sBatchType()
	    	    		+ "&DocumentType=" + ICBatchTypes.IC_SHIPMENT
	    	    		+ "&Editable=Yes"
	    	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    	    		+ "\">"
	    	    		+ "Create shipment entry</A>"
	    	    		);

    		pwOut.println("&nbsp&nbsp");
    		}
    		if (batch.iBatchType() == ICBatchTypes.IC_TRANSFER){
        		pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditTransferEntry?" 
	    	    		+ "BatchNumber=" + batch.sBatchNumber()
	    	    		+ "&BatchType=" + batch.sBatchType()
	    	    		+ "&DocumentType=" + ICBatchTypes.IC_TRANSFER
	    	    		+ "&Editable=Yes"
	    	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    	    		+ "\">"
	    	    		+ "Create transfer entry</A>"
	    	    		);
    		pwOut.println("&nbsp&nbsp");
    		}
    		if (batch.iBatchType() == ICBatchTypes.IC_PHYSICALCOUNT){
        		pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditPhysicalCountEntry?" 
	    	    		+ "BatchNumber=" + batch.sBatchNumber()
	    	    		+ "&BatchType=" + batch.sBatchType()
	    	    		+ "&DocumentType=" + ICBatchTypes.IC_PHYSICALCOUNT
	    	    		+ "&Editable=Yes"
	    	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    	    		+ "\">"
	    	    		+ "Create physical inventory entry</A>"
	    	    		);

    		pwOut.println("&nbsp&nbsp");
    		}

        }
        else{
        	pwOut.println("Description: <B>"+ batch.sBatchDescription() + "</B><BR>");
        	pwOut.println(batch.sBatchStatusLabel() + " batches cannot be edited or deleted.");
        }

    	//if (batch.iBatchStatus() == SMBatchStatuses.POSTED){
    	//	pwOut.println("  <INPUT TYPE=SUBMIT NAME='" + CREATEGLBATCH_BUTTON_NAME 
    	//		+ "' VALUE='" + CREATEGLBATCH_BUTTON_LABEL + "' STYLE='height: 0.24in'>");
    	//}
        //List out the entries on the screen as links to edit:
        //pwOut.println("<BR>");
        pwOut.println("<TABLE BORDER=1 CELLSPACING=2 style=\"font-size:75%\">");

    	pwOut.println("<TR>");
    	pwOut.println("<TD>");
    	pwOut.println("<B><U>Entry #</B></U>");
    	pwOut.println("</TD>");
    	
    	pwOut.println("<TD>");
    	pwOut.println("<B><U>Entry Type</B></U>");
    	pwOut.println("</TD>");
    	
    	pwOut.println("<TD>");
    	pwOut.println("<B><U>Entry date</B></U>");
    	pwOut.println("</TD>");

    	pwOut.println("<TD>");
    	pwOut.println("<B><U>Doc #</B></U>");
    	pwOut.println("</TD>");
    	
    	pwOut.println("<TD>");
    	pwOut.println("<B><U>Description</B></U>");
    	pwOut.println("</TD>");
    	
    	pwOut.println("<TD>");
    	pwOut.println("<B><U>Entry net cost</B></U>");
    	pwOut.println("</TD>");
    	
    	pwOut.println("</TR>");

    	if (batch.lBatchNumber() != -1){
    		BigDecimal bdCostTotal = new BigDecimal(0);
    		try{
    			String SQL = "SELECT * " 
    				+ " FROM " + SMTableicbatchentries.TableName
    				+ " WHERE (" 
    				+ SMTableicbatchentries.lbatchnumber + " = " + batch.sBatchNumber()
    				+ ")";
		        ResultSet rsEntries = clsDatabaseFunctions.openResultSet(
		        	SQL, 
		        	getServletContext(), 
		        	sConf,
		        	"MySQL",
		        	this.toString() + ".Edit_Record - User: " + sUserID
		        	+ " - "
		        	+ sUserFullName
		        		);
	
		        while(rsEntries.next()){
		        	pwOut.println("<TR>");
		        	//Entry number
		        	pwOut.println("<TD>");
		        	
		        	String sEditableBatch = "No";
		        	if(batch.bEditable()){
		        		sEditableBatch = "Yes";
		        	}
		        	
		    	    //Set the name of the class which will handle the processing of the entry, depending on the doc type:
		    	    String sEntryProcessorClass = "";
		    	    if (rsEntries.getLong(SMTableicbatchentries.ientrytype) 
		    	    		== ICEntryTypes.ADJUSTMENT_ENTRY){
		    	    	sEntryProcessorClass = "ICEditAdjustmentEntry";
		    	    }
		    	    if (rsEntries.getLong(SMTableicbatchentries.ientrytype) 
		    	    		== ICEntryTypes.RECEIPT_ENTRY){
		    	    	sEntryProcessorClass = "ICEditReceiptEntry";
		    	    }
		    	    if (rsEntries.getLong(SMTableicbatchentries.ientrytype) 
		    	    		== ICEntryTypes.SHIPMENT_ENTRY){
		    	    	sEntryProcessorClass = "ICEditShipmentEntry";
		    	    }
		    	    if (rsEntries.getLong(SMTableicbatchentries.ientrytype) 
		    	    		== ICEntryTypes.TRANSFER_ENTRY){
		    	    	sEntryProcessorClass = "ICEditTransferEntry";
		    	    }
		    	    if (rsEntries.getLong(SMTableicbatchentries.ientrytype) 
		    	    		== ICEntryTypes.PHYSICALCOUNT_ENTRY){
		    	    	sEntryProcessorClass = "ICEditPhysicalCountEntry";
		    	    }
		    	    //if (batch.bEditable()){
		        	pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic." 
//		        			//Depending on the type of batch, we'll call the different 'edit' classes here:
		    	    		+ sEntryProcessorClass + "?" 
		    	    		+ "BatchNumber=" + batch.sBatchNumber() 
		    	    		+ "&EntryNumber=" + rsEntries.getString(SMTableicbatchentries.lentrynumber)
		    	    		+ "&Editable=" + sEditableBatch
		    	    		+ "&BatchType=" + batch.sBatchType()
		    	    		+ "&EntryType=" + rsEntries.getString(SMTableicbatchentries.ientrytype)
		    	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		    	    		+ "\">"
		    	    		+ clsStringFunctions.PadLeft(
		    	    			rsEntries.getString(SMTableicbatchentries.lentrynumber), "0", 5)
		    	    		+ "</A>"
		    	    		);
		    	    //} else{
		    	    //	pwOut.println(rsEntries.getString(SMTableentries.ientrynumber));
		    	    //}
		        	pwOut.println("</TD>");

		        	//Entry type
		        	pwOut.println("<TD>");
		        	pwOut.println(ICEntryTypes.Get_Entry_Type(
		        		rsEntries.getInt(SMTableicbatchentries.ientrytype)));
		        	pwOut.println("</TD>");

		        	//Entry date
		        	pwOut.println("<TD>");
		        	pwOut.println(clsDateAndTimeConversions.utilDateToString(
		        		rsEntries.getDate(SMTableicbatchentries.datentrydate),"MM-dd-yyyy"));
		        	pwOut.println("</TD>");
	
		        	//Doc Number
		        	pwOut.println("<TD>");
		        	pwOut.println(rsEntries.getString(SMTableicbatchentries.sdocnumber));
		        	pwOut.println("</TD>");
		        	
		        	//Description
		        	pwOut.println("<TD>");
		        	pwOut.println(
		        			clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(
		        					rsEntries.getString(SMTableicbatchentries.sentrydescription)));
		        	pwOut.println("</TD>");	        	
		        	
		        	//Entry amount - just display the absolute value, since we don't care about the arithmetic sign here:
		        	pwOut.println("<TD ALIGN=RIGHT>");
		        	//Transfers have an amount stored, but they always net to zero, so we
		        	//want to show a zero in the list:
		        	BigDecimal bdEntryCost = rsEntries.getBigDecimal(SMTableicbatchentries.bdentryamount);
		    	    if (rsEntries.getLong(SMTableicbatchentries.ientrytype) 
		    	    		== ICEntryTypes.TRANSFER_ENTRY){
		    	    	bdEntryCost = BigDecimal.ZERO;
		    	    }
		        	pwOut.println(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdEntryCost));
		        	pwOut.println("</TD>");
		    		bdCostTotal = bdCostTotal.add(bdEntryCost);
		        	
		        	pwOut.println("</TR>");
		        }
		        rsEntries.close();
		        //Print the total lines:
	        	pwOut.println("<TR>");
	        	pwOut.println("<TD>&nbsp;</TD>");
	        	pwOut.println("<TD>&nbsp;</TD>");
	        	pwOut.println("<TD>&nbsp;</TD>");
	        	pwOut.println("<TD>&nbsp;</TD>");
	        	pwOut.println("<TD ALIGN = RIGHT><B>TOTALS:</B></TD>");
	        	pwOut.println("<TD ALIGN = RIGHT>" 
	        		+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdCostTotal) + "</TD>");
		        
				pwOut.println("</TABLE>");
				pwOut.println("<BR>");
		        
			}catch (SQLException ex){
		    	System.out.println("Error in " + this.toString()+ " class!!");
		        System.out.println("SQLException: " + ex.getMessage());
		        System.out.println("SQLState: " + ex.getSQLState());
		        System.out.println("SQL: " + ex.getErrorCode());
				//return false;
			}
    	} //end if
	}
		
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
