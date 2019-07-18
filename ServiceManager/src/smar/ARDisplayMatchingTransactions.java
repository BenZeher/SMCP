package smar;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.*;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsServletUtilities;
public class ARDisplayMatchingTransactions extends HttpServlet {

	
	private static final long serialVersionUID = 1L;
	/*
	 * Parameters:
	 * MatchedTransactionID
	 * OpenTransactionsOnly (This is only used to pass this parameter back to the activity display screen)
	 */
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
			request, 
			response, 
			getServletContext(), 
			SMSystemFunctions.ARCustomerActivity)){
			return;
		}
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    		+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    
	    //Get the variables for the class:
	    String sCustomerNumber = "";
	    
	    if (request.getParameter("CustomerNumber") == null){
			out.println(
					"<META http-equiv='Refresh' content='0;URL=" 
					+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARActivityInquiry"
					+ "?Warning=No customer number passed to ARDisplayMatchingTransactions!"
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "'>"		
				);

	    } else{
	    	//System.out.println("CUST NUMBER = " + request.getParameter("CustomerNumber"));
	    	sCustomerNumber = request.getParameter("CustomerNumber");
	    }

	    //MatchedTransactionID
	    String sMatchedTransactionID = "";
	    if (request.getParameter("MatchedTransactionID") == null){
			out.println(
					"<META http-equiv='Refresh' content='0;URL=" 
					+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARActivityInquiry"
					+ "?Warning=No MatchedTransactionID passed to " + ARClassNames.MATCHING_ACTIVITY_DISPLAY + "!"
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "'>"		
				);

	    } else{
	    	sMatchedTransactionID = request.getParameter("MatchedTransactionID");
	    }

	    //DocNumber
	    String sDocNumber = "";
	    if (request.getParameter("DocNumber") == null){
			out.println(
					"<META http-equiv='Refresh' content='0;URL=" 
					+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARActivityInquiry"
					+ "?Warning=No DocNumber passed to " + ARClassNames.MATCHING_ACTIVITY_DISPLAY + "!"
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "'>"		
				);

	    } else{
	    	sDocNumber = request.getParameter("DocNumber");
	    }

	    ARCustomer cust = new ARCustomer(sCustomerNumber);
	    
	    if (!cust.load(getServletContext(), sDBID)){
			out.println(
					"<META http-equiv='Refresh' content='0;URL=" 
					+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARActivityInquiry"
					+ "?Warning=Error loading customer from database!"
					+ "&CustomerNumber=" + clsServletUtilities.URLEncode(cust.getM_sCustomerNumber())
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "'>"		
				);
	    }
		
	    String title = "";
	    String subtitle = "Matching transactions for " + cust.getM_sCustomerNumber()
	    	+ " - document ID " + sMatchedTransactionID + ","
	    	+ " document number " + sDocNumber + ".";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

	    //Print a link to the first page after login:
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Accounts Receivable Main Menu</A><BR>");

	    //Build table header for listing:
	    out.println("<TABLE BORDER=1 CELLSPACING=2 style=\"font-size:75%\">");
	    out.println("<TR>");
	    out.println("<TD>Doc. Date</TD>");
	    out.println("<TD>Doc. ID</TD>");
	    out.println("<TD>Doc. #</TD>");
	    out.println("<TD>Doc. Type</TD>");
	    out.println("<TD>Matching Amt.</TD>");
	    //out.println("<TD>Order #</TD>");
	    out.println("<TD>Description</TD>");
	    out.println("<TD>Orig. Doc Date</TD>");
	    out.println("<TD>Orig. Doc ID</TD>");
	    out.println("<TD>Orig. Doc#</TD>");
	    out.println("<TD>Orig. Doc Type</TD>");
	    out.println("<TD>Orig. Doc Amt</TD>");
	    
	    out.println("</TR>");
	    
	    try{
	        String sSQL = "SELECT *"
	    			+ " FROM " + SMTablearmatchingline.TableName
	    			+ " LEFT JOIN " + SMTableartransactions.TableName
	    			+ " ON (" + SMTableartransactions.TableName + "." + SMTableartransactions.spayeepayor + " = "
	    				+ SMTablearmatchingline.TableName + "." + SMTablearmatchingline.spayeepayor
	    				+ " AND " + SMTableartransactions.TableName + "." + SMTableartransactions.sdocnumber + " = "
	    					+ SMTablearmatchingline.TableName + "." + SMTablearmatchingline.sdocnumber + ")"
	    			+ " WHERE ("
	    				+ "(" + SMTablearmatchingline.TableName + "." 
	    				+ SMTablearmatchingline.ldocappliedtoid + " = " + sMatchedTransactionID + ")"
	    			+ ")"
	    			+ " ORDER BY "
	    				+ SMTablearmatchingline.TableName + "." + SMTablearmatchingline.lid;
	    			//System.out.println("In ARDisplayMatchingTransactions, sSQL = " + sSQL);
	        ResultSet rs = clsDatabaseFunctions.openResultSet(
	        	sSQL, 
	        	getServletContext(), 
	        	sDBID,
	        	"MySQL",
	        	this.toString() + ".doPost - User: " + sUserID
	        	+ " - "
	        	+ sUserFullName
	        		);
	        
        	while (rs.next()){
        		//Start a row:
        		out.println("<TR>");
        		out.println(
        			Build_Row(		
	        			out,
	        			getServletContext(),
	        			sDBID,
	        			clsDateAndTimeConversions.TimeStampToString(rs.getTimestamp(SMTablearmatchingline.dattransactiondate),"MM-dd-yyyy"),
	        			rs.getLong(SMTableartransactions.TableName + "." + SMTableartransactions.lid),
	        			rs.getString(SMTablearmatchingline.TableName + "." + SMTablearmatchingline.sdocnumber),
	        			rs.getInt(SMTableartransactions.TableName + "." + SMTableartransactions.idoctype),
	        			//Negate this so that negatives REDUCE customer liability, and positives INCREASE it:
	        			clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTablearmatchingline.TableName + "." + SMTablearmatchingline.damount).negate()),
	        			rs.getString(SMTablearmatchingline.TableName + "." + SMTablearmatchingline.sdescription),
	        			rs.getLong(SMTablearmatchingline.TableName + "." + SMTablearmatchingline.lparenttransactionid),
	        			sUserID,
	        			sUserFullName
        			));
        		//End the row:
        		out.println("</TR>");
        	}
        	rs.close();
	        out.println ("<BR>");
		}catch (SQLException ex){
	    	System.out.println("Error in " + this.toString() + " class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			//return false;
		}
		
		out.println("</TABLE>");
		
		//***************************************
		out.println("</BODY></HTML>");
	}

	private static String Build_Row (
			PrintWriter pwout,
			ServletContext context,
			String sDBID,
			String sTransactionDate,
			long lDocID,
			String sDocNumber,
			int iDocType,
			String sAmount,
			String sDocDesc,
			long lParentTransactionID,
			String sUserID,
			String sUserFullName
			){

		/*
		 * Date
		 * id
		 * number
		 * type
		 * amount 
		 * desc
		 */
		String sOutPut = "<TD>" + sTransactionDate + "</TD>";
		sOutPut += "<TD>" + lDocID + "</TD>";
		sOutPut += "<TD>" + sDocNumber + "</TD>";
		sOutPut += "<TD>" + ARDocumentTypes.Get_Document_Type_Label(iDocType) + "</TD>";
		sOutPut += "<TD ALIGN = RIGHT>" + sAmount + "</TD>";
		sOutPut += "<TD>" + clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(sDocDesc) + "</TD>";
		
		String SQL = "SELECT * FROM " + SMTableartransactions.TableName
			+ " WHERE ("
				+ SMTableartransactions.lid + " = " + Long.toString(lParentTransactionID)
			+ ")"
			;

		try{
			ResultSet rsParent = clsDatabaseFunctions.openResultSet(
		        	SQL, 
		        	context, 
		        	sDBID,
		        	"MySQL",
		        	"ARDisplayMatchingTransactions.Build_Row - User: " + sUserID
		        	+ " - "
		        	+ sUserFullName
					);
			if (rsParent.next()){
				sOutPut += "<TD>" + clsDateAndTimeConversions.TimeStampToString(rsParent.getTimestamp(SMTableartransactions.datdocdate),"MM-dd-yyyy") + "</TD>";
				sOutPut += "<TD>" + Long.toString(lParentTransactionID) + "</TD>";
				sOutPut += "<TD>" + rsParent.getString(SMTableartransactions.sdocnumber).trim() + "</TD>";
				sOutPut += "<TD>" + ARDocumentTypes.Get_Document_Type_Label(rsParent.getInt(SMTableartransactions.idoctype)) + "</TD>";
				sOutPut += "<TD ALIGN=RIGHT>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsParent.getBigDecimal(SMTableartransactions.doriginalamt)) + "</TD>";
			}else{
				sOutPut += "<TD>N/A</TD>";
				sOutPut += "<TD>N/A</TD>";
				sOutPut += "<TD>N/A</TD>";
				sOutPut += "<TD>N/A</TD>";
				sOutPut += "<TD>N/A</TD>";
			}
			rsParent.close();
		}catch(SQLException e){
			sOutPut += "<TD>N/A</TD>";
			sOutPut += "<TD>N/A</TD>";
			sOutPut += "<TD>N/A</TD>";
			sOutPut += "<TD>N/A</TD>";
			sOutPut += "<TD>N/A</TD>";
		}

		return sOutPut;
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}