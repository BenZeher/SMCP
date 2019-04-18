package smgl;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ConnectionPool.WebContextParameters;
import SMClasses.SMBatchStatuses;
import SMClasses.SMModuleTypes;
import SMDataDefinition.SMTablegltransactionbatchentries;
import SMDataDefinition.SMTablegltransactionbatches;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsStringFunctions;
import smar.SMOption;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMMasterEditSelect;
import smcontrolpanel.SMRecreateExportAction;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class GLEditBatchesEdit extends HttpServlet {

	public static final String BATCH_EDITABLE_PARAMETER = "Editable";
	public static final String BATCH_EDITABLE_PARAMETER_VALUE_TRUE = "Yes";
	
	private static final String ROW_BACKGROUND_HIGHLIGHT_COLOR = "YELLOW";
	private static final String TABLE_ROW_ODD_ROW_BACKGROUND_COLOR = "#DCDCDC";
	private static final String TABLE_ROW_EVEN_ROW_BACKGROUND_COLOR = "#FFFFFF";
	
	private static final long serialVersionUID = 1L;
	/*
	 * Parameters:
	 * BatchNumber - batch number
	 * BatchType - batch type - an integer passed as a string
	 */
	private static final String sObjectName = "Batch";
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.GLEditBatches))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    
		String title = "";
		String subtitle = "";
		
		//First, load any batch data from the request:
		GLTransactionBatch batch = new GLTransactionBatch(request);
		
		//If there's a batch in the session, get that:
		if (CurrentSession.getAttribute(GLTransactionBatch.OBJECT_NAME) != null){
			//System.out.println("[1543592820] - batch in the session");
			batch = (GLTransactionBatch)CurrentSession.getAttribute(GLTransactionBatch.OBJECT_NAME);
			CurrentSession.removeAttribute(GLTransactionBatch.OBJECT_NAME);
		}else{
			if (batch.getsbatchnumber().compareToIgnoreCase("-1") != 0){
				try {
					//System.out.println("[1543592821]");
					batch.load(getServletContext(), sDBID, sUserID);
					//System.out.println("[1543592822] - type = '" + batch.getsbatchtype());
				} catch (Exception e) {
					out.println("Error [1555350877] - Could not load batch number " + batch.getsbatchnumber() + " - " + e.getMessage() + "\n");
					return;
				}
			}
		}
		
    	//User has chosen to edit:
		String sFunction = "Edit";
		if (!batch.bEditable()){
			sFunction = "View";
		}
		
		if (request.getParameter(SMTablegltransactionbatches.lbatchnumber) != null){
			if (request.getParameter(SMTablegltransactionbatches.lbatchnumber).compareToIgnoreCase("-1") == 0){
				title = "Add new batch";
			}else{
				title = sFunction + " " + sObjectName + ": " + (String) request.getParameter(SMTablegltransactionbatches.lbatchnumber);
			}
		}
		out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
		out.println(sCommandScript());
		
	    //Display any warnings:
	    if (request.getParameter("Warning") != null){
	    	String sWarning = request.getParameter("Warning");
	    	if (!sWarning.equalsIgnoreCase("")){
	    		out.println("<BR><B><FONT COLOR=\"RED\">" + sWarning + "</FONT></B><BR>\n");
	    	}
	    }
	    //Display any status messages:
	    if (request.getParameter("Status") != null){
	    	String sStatus = request.getParameter("Status");
	    	if (!sStatus.equalsIgnoreCase("")){
	    		out.println("<B>" + sStatus + "</B><BR>\n");
	    	}
	    }
	    
	    //Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>\n");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.GLMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Accounts Payable Main Menu</A><BR>\n");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.GLEditBatchesSelect?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Batch List</A><BR>\n");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" 
				+ Long.toString(SMSystemFunctions.GLEditBatches) + "\">Summary</A><BR>\n");
		
	    Edit_Record(batch, sUserID, out, request, sDBID, false, (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));
		out.println("</BODY></HTML>\n");
		
	}

	private void Edit_Record(
			GLTransactionBatch batch,
			String sUserID,
			PrintWriter pwOut, 
			HttpServletRequest req,
			String sDBID,
			boolean bAddNew,
			String sLicenseModuleLevel){
		
		pwOut.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.GLEditBatchesAction' METHOD='POST'>\n");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>\n");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + SMTablegltransactionbatches.lbatchnumber + "\" VALUE=\"" + batch.getsbatchnumber() + "\">\n");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + SMTablegltransactionbatches.ibatchstatus + "\" VALUE=\"" + batch.getsbatchstatus() + "\">\n");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + SMTablegltransactionbatches.lcreatedby + "\" VALUE=\"" + batch.getlcreatedby() + "\">\n");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + SMTablegltransactionbatches.datlasteditdate + "\" VALUE=\"" + batch.getslasteditdate() + "\">\n");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + SMTablegltransactionbatches.datpostdate + "\" VALUE=\"" + batch.getsposteddate() + "\">\n");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + SMTablegltransactionbatches.lbatchlastentry + "\" VALUE=\"" + batch.getsbatchlastentry() + "\">\n");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + SMTablegltransactionbatches.llasteditedby + "\" VALUE=\"" + batch.getllasteditedby() + "\">\n");
		
		pwOut.println("<INPUT TYPE=HIDDEN"
			+ " NAME=\"" + "CallingClass" + "\""
			+ " VALUE=\"" + SMUtilities.getFullClassName(this.toString()) + "\">\n"
		);
		
        //Display fields:
        //Date last maintained:
        pwOut.println("<B>" + "GL Transaction Batch</B>\n");
        pwOut.println(" last edited <B>" + batch.getslasteditdate() + "</B>\n");
        pwOut.println(" by user <B>" + SMUtilities.getFullNamebyUserID(batch.getllasteditedby(), getServletContext(), sDBID, "") + "</B><BR>\n");
        pwOut.println("Batch Date: <B>"+ batch.getsbatchdate() + "</B>\n");
        pwOut.println("     Status: <B>"+ batch.getsbatchstatuslabel() + "</B>\n");
        
        if (batch.getsbatchstatus().compareToIgnoreCase(Integer.toString(SMBatchStatuses.POSTED)) == 0){
        	pwOut.println(" on <B>"+ batch.getsposteddate() + "</B>\n");
        }
        pwOut.println("<BR>\n");
        
        if (batch.bEditable()){
            //Description:
        	pwOut.println(
        		"Description:&nbsp;"
        		+ "<INPUT TYPE=TEXT NAME=\"" + SMTablegltransactionbatches.sbatchdescription + "\""
        		+ " VALUE=\"" + clsStringFunctions.filter(batch.getsbatchdescription()) + "\""
        		+ " MAXLENGTH=" + Integer.toString(SMTablegltransactionbatches.sBatchDescriptionLength)
        		+ " SIZE = " + "30"
        		+ ">"
        		+ "\n"
        	);
 
            //Add an editable cell for the batch date:
        	pwOut.println(
        		"Batch&nbsp;date:&nbsp;"
        		+ "<INPUT TYPE=TEXT NAME=\"" + SMTablegltransactionbatches.datbatchdate + "\""
        		+ " VALUE=\"" + clsStringFunctions.filter(batch.getsbatchdate()) + "\""
        		+ " MAXLENGTH=" + "10"
        		+ " SIZE = " + "8"
        		+ ">"
        		+ "\n"
        	);
    		pwOut.println(SMUtilities.getDatePickerString(SMTablegltransactionbatches.datbatchdate, getServletContext()) + "\n");

            //Here, we warn the user if we are near the end of the month:
            int iToday = Integer.parseInt(clsDateAndTimeConversions.now("d"));
	    	if ((iToday > 27) || (iToday < 4)
	    		){
	    		pwOut.println("<BR><B><FONT COLOR=\"RED\">" 
	    			+ "WARNING! It's after the 27th or before the 4 of the month"
	    				+ " - review the document dates below "
	    			+ "and make sure they are correct."
	    			+ "</FONT></B>\n");
	    	}
            
        	pwOut.println("<BR><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Save "
        			+ sObjectName + "' STYLE='height: 0.24in'>\n");
        	
        	if (
        			(batch.getsbatchstatus().compareToIgnoreCase(Integer.toString(SMBatchStatuses.ENTERED)) == 0)
        			|| (batch.getsbatchstatus().compareToIgnoreCase(Integer.toString(SMBatchStatuses.IMPORTED)) == 0)
        	){
        		pwOut.println("  <INPUT TYPE=SUBMIT NAME='Post' VALUE='Post " + sObjectName + "' STYLE='height: 0.24in'>");
        		pwOut.println("  <LABEL>Check to confirm posting: <INPUT TYPE=CHECKBOX NAME=\"ConfirmPost\"></LABEL>\n");
        	}
        	pwOut.println("  <INPUT TYPE=SUBMIT NAME='Delete' VALUE='Delete " + sObjectName + "' STYLE='height: 0.24in'>");
        	pwOut.println("  <LABEL>Check to confirm deletion: <INPUT TYPE=CHECKBOX NAME=\"ConfirmDelete\"></LABEL>\n");
        	
        	pwOut.println("</FORM>\n");
        	
        	//IF it's a new batch, we don't want to display all the batch details on it:
        	if (batch.getsbatchnumber().compareToIgnoreCase("-1") == 0){
        		return;
        	}

    		pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLEditEntryEdit" 
	    		+ "?" + SMTablegltransactionbatches.lbatchnumber + "=" + batch.getsbatchnumber()
	    		+ "&" + BATCH_EDITABLE_PARAMETER + "=" + BATCH_EDITABLE_PARAMETER_VALUE_TRUE
	    		+ "&" + SMTablegltransactionbatchentries.lid + "=-1"
	    		+ "&" + "CallingClass=" + SMUtilities.getFullClassName(this.toString())
	    		+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=Y"
	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    		+ "\">"
	    		+ "Create transaction batch"
	    		+ "</A>"
	    		+ "\n"
	    		);
        }
        else{
        	pwOut.println("Description: <B>"+ batch.getsbatchdescription() + "</B><BR>");
       		pwOut.println(batch.getsbatchstatuslabel() + " batches cannot be edited or deleted.\n");
        }
        
        int iColumnCount = 0;
        
        //List out the entries on the screen as links to edit:
        pwOut.println("<BR><I><B>NOTE:</B> Out-of-balance entry amounts and entry dates outside the posting period are displayed in RED.</I>");
        pwOut.println("<TABLE BORDER=1 CELLSPACING=2 style=\"font-size:75%\">\n");

    	pwOut.println("  <TR>\n");
    	pwOut.println("    <TD>");
    	pwOut.println("<B><U>Entry #</B></U>");
    	pwOut.println("</TD>\n");
    	iColumnCount++;
    	    	
    	pwOut.println("    <TD>");
    	pwOut.println("<B><U>Entry date</B></U>");
    	pwOut.println("</TD>\n");
    	iColumnCount++;
    	    	
    	pwOut.println("    <TD>");
    	pwOut.println("<B><U>Description</B></U>");
    	pwOut.println("</TD>\n");
    	iColumnCount++;
    	
    	pwOut.println("    <TD>");
    	pwOut.println("<B><U>Number<BR>of&nbsp;lines</B></U>");
    	pwOut.println("</TD>\n");
    	iColumnCount++;
    	
    	pwOut.println("    <TD>");
    	pwOut.println("<B><U>Total debits</B></U>");
    	pwOut.println("</TD>\n");
    	iColumnCount++;

    	pwOut.println("    <TD>");
    	pwOut.println("<B><U>Total credits</B></U>");
    	pwOut.println("</TD>\n");
    	iColumnCount++;
    	
    	pwOut.println("  </TR>\n");

    	//Just read the entries out of the batch here:
    	if (batch.getsbatchnumber().compareToIgnoreCase("-1") != 0){
    		
    		Connection conn = null;
			try {
				conn = clsDatabaseFunctions.getConnectionWithException(
					getServletContext(), 
					sDBID, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString() + ".Edit_Record - userID: " + sUserID)
				);
			} catch (Exception e) {
				pwOut.println("<BR><B><FONT COLOR=RED>Error [1555351511] getting connection - " + e.getMessage() + "</FONT></B><BR>");
			}
    		SMOption opt = new SMOption();
    		if(!opt.load(conn)){
    			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1555351512]");
    			pwOut.println("<BR><B><FONT COLOR=RED>Error [1555351513] getting SM Options - " + opt.getErrorMessage() + "</FONT></B><BR>");
    		}
    		
    		boolean bOddRow = true;
    		for (int i = 0; i < batch.getBatchEntryArray().size(); i++){
    			GLTransactionBatchEntry entry = batch.getBatchEntryArray().get(i);
				String sBackgroundColor = TABLE_ROW_EVEN_ROW_BACKGROUND_COLOR;
				if (bOddRow){
					sBackgroundColor = TABLE_ROW_ODD_ROW_BACKGROUND_COLOR;
				}
    			pwOut.println("  <TR style = \"  background-color:" + sBackgroundColor + ";  \""
					+ " onmouseout=\"setRowBackgroundColor(this, '" + sBackgroundColor + "');\""
					+ " onmousemove=\"setRowBackgroundColor(this, '" + ROW_BACKGROUND_HIGHLIGHT_COLOR + "');\""
					+ ">\n"
				);
	        	//Entry number
	        	pwOut.println("    <TD>");
	        	
	        	String sEditableBatch = "No";
	        	if(batch.bEditable()){
	        		sEditableBatch = "Yes";
	        	}
	        	
	        	pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap." 
    	    		+ "smgl.GLEditEntryEdit" 
    	    		+ "?" + SMTablegltransactionbatchentries.lbatchnumber + "=" + entry.getsbatchnumber() 
    	    		+ "&" + SMTablegltransactionbatchentries.lentrynumber + "=" + entry.getsentrynumber()
    	    		+ "&" + SMTablegltransactionbatchentries.lid + "=" + entry.getslid()
    	    		+ "&" + "Editable=" + sEditableBatch
    	    		+ "&" + "CallingClass=" + SMUtilities.getFullClassName(this.toString())
    	    		//+ "&" + SMTablegltransactionbatches.ibatchtype + "=" + batch.getsbatchtype()
    	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    	    		+ "\">"
    	    		+ clsStringFunctions.PadLeft(
    	    			entry.getsentrynumber(), "0", 5)
    	    		+ "</A>"
    	    		);
	        	pwOut.println("</TD>\n");

	        	//Entry date
	        	pwOut.println("    <TD>");
	        	String sEntryDate = entry.getsdatentrydate();

	    		try {
	    			opt.checkDateForPosting(
	    				entry.getsdatentrydate(), 
	    				"ENTRY DATE", 
	    				conn, 
	    				sUserID
	    			);
	    		} catch (Exception e2) {
	    			sEntryDate = "<div style = \" color: red; font-weight: bold; \">" + sEntryDate + "</div>"; 
	    		}
	        	pwOut.println(sEntryDate);
	        	pwOut.println("</TD>\n");
	        	
	        	//Description
	        	pwOut.println("    <TD>");
	        	pwOut.println(clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(entry.getsentrydescription()));
	        	pwOut.println("</TD>\n");
	        	
	        	//Number of entries:
	        	pwOut.println("    <TD ALIGN=RIGHT>");
	        	pwOut.println(Integer.toString(entry.getLineArray().size()));
	        	pwOut.println("</TD>\n");
	        	
	        	//Debit total
	        	try {
					pwOut.println("    <TD ALIGN=RIGHT>");
					pwOut.println(ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(entry.getDebitTotal()));
					pwOut.println("</TD>\n");
				} catch (Exception e) {
					pwOut.println("<BR><B><FONT COLOR=RED>Error [1555534073] getting entry debit totals - " + e.getMessage() + "</FONT></B><BR>");
				}

	        	//Credit total
	        	try {
					pwOut.println("    <TD ALIGN=RIGHT>");
					pwOut.println(ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(entry.getCreditTotal()));
					pwOut.println("</TD>\n");
				} catch (Exception e) {
					pwOut.println("<BR><B><FONT COLOR=RED>Error [1555534074] getting entry credit totals - " + e.getMessage() + "</FONT></B><BR>");
				}
	    		
	        	pwOut.println(" </TR>\n");
	        	bOddRow = !bOddRow;
    		}
    		
        	//Print the 'totals' line:
        	try {
				pwOut.println("  <TR>\n"
					+ "    <TD ALIGN=RIGHT COLSPAN=" + Integer.toString(iColumnCount - 3) + ">"
					+ "<B>TOTALS:</B>"
					+ "    </TD>\n"
					+ "    <TD ALIGN=RIGHT >"
					+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(batch.getTotalDebits())
					+ "    </TD>\n"
					+ "    <TD ALIGN=RIGHT >"
					+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(batch.getTotalCredits())
					+ "    </TD>\n"
					+ "  </TR>\n"
				);
			} catch (Exception e) {
				pwOut.println("<BR><B><FONT COLOR=RED>Error [1555534072] getting debit and credit totals - " + e.getMessage() + "</FONT></B><BR>");
			}
        	
    		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547047712]");
    	} //end if
	}
	private String sCommandScript(){
		String s = "";
		s += "<NOSCRIPT>\n"
				+ "    <font color=red>\n"
				+ "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
				+ "    </font>\n"
				+ "</NOSCRIPT>\n"
			;
		s += "<script type='text/javascript'>\n";
		
		//Function for changing row backgroundcolor:
		s += "function setRowBackgroundColor(row, color) { \n"
			+ "    row.style.backgroundColor = color; \n"
    		+ "} \n"
		;
		
		 s +=  "   window.addEventListener(\"beforeunload\",function(){\n" 
				 +  "      document.documentElement.style.cursor = \"not-allowed\";\n "
				  +  "     document.documentElement.style.cursor = \"wait\";\n"
				  +"      });\n";
		 
		s += "</script>\n";
		return s;
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
