package smcontrolpanel;

import SMDataDefinition.*;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class SMSalesContactAction extends HttpServlet{
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMEditSalesContacts))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String title = "Updating sales contact....";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
	    //SimpleDateFormat SQLDateformatter = new SimpleDateFormat("yyyy-MM-dd");
	    String sOriginalURL = clsServletUtilities.URLEncode(clsManageRequestParameters.get_Request_Parameter("OriginalURL", request));
	    //System.out.println("Action OriginalURL = " + sOriginalURL);
	    
	    String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
	    //System.out.println("In " + this.toString() + " sCallingClass = " + sCallingClass);
	    //System.out.println("In " + this.toString() + " sOriginalURL = " + sOriginalURL);

    	
	    if(request.getParameter("SUBMITSAVE") != null){

		    //check to see if the entered customer number is valid or not.
		    //if user entered the customer code wrong (code for another customer), here won't catch it.

		    try{
				String sSQL = "SELECT * FROM " + SMTablearcustomer.TableName;
				if (request.getParameter("SelectedCustomer") != null){
						sSQL = sSQL + " WHERE" + 
							" " + SMTablearcustomer.sCustomerNumber + " = '" + request.getParameter("SelectedCustomer") + "'";

				}
				
		    	ResultSet rsCustomerCheck = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID, "MySQL", "smcontrolpanel.SMSalesContactAction");
		    	if (rsCustomerCheck.next()){
		    		//customer exists, proceed with saving
		    		if (request.getParameter("id") != null && 
		    			Integer.parseInt(request.getParameter("id")) > 0){
		    			sSQL = "UPDATE " 
		    			+ SMTablesalescontacts.TableName + " SET" 
		    			+ " " + SMTablesalescontacts.salespersoncode + " = '" + clsDatabaseFunctions.FormatSQLStatement(
		    				clsManageRequestParameters.get_Request_Parameter("SelectedSalesperson", request)) + "'" 
		    			+ ", " + SMTablesalescontacts.scustomernumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(
			    			clsManageRequestParameters.get_Request_Parameter("SelectedCustomer", request)) + "'"
			    		+ ", " + SMTablesalescontacts.scustomername + " = '" 
			    			+ clsDatabaseFunctions.FormatSQLStatement(Get_Customer_Name(clsDatabaseFunctions.FormatSQLStatement(
				    		clsManageRequestParameters.get_Request_Parameter("SelectedCustomer", request)), sDBID)) + "'"
			    		+ ", " + SMTablesalescontacts.scontactname + " = '" + clsDatabaseFunctions.FormatSQLStatement(
				    		clsManageRequestParameters.get_Request_Parameter("ContactName", request)) + "'" 
			    		+ ", " + SMTablesalescontacts.sphonenumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(
				    		clsManageRequestParameters.get_Request_Parameter("PhoneNumber", request)) + "'"
				    	+ ", " + SMTablesalescontacts.semailaddress + " = '" + clsDatabaseFunctions.FormatSQLStatement(
				    		clsManageRequestParameters.get_Request_Parameter("EmailAddress", request)) + "'"
				    	+ ", " + SMTablesalescontacts.binactive + " = " + Integer.parseInt(clsManageRequestParameters.get_Request_Parameter("IsInActive", request))
				    	+ ", " + SMTablesalescontacts.sdescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(
				    		clsManageRequestParameters.get_Request_Parameter("Description", request)) + "'"
				    	+ ", " + SMTablesalescontacts.mnotes + " = '" + clsDatabaseFunctions.FormatSQLStatement(
					    		clsManageRequestParameters.get_Request_Parameter("Note", request)) + "'"
		    					
		    			 + " WHERE (" 
		    			 	+ SMTablesalescontacts.id + " = " + clsManageRequestParameters.get_Request_Parameter("id", request)
		    			 + ")";
		    			
				    	if (clsDatabaseFunctions.executeSQL(sSQL, getServletContext(), sDBID) == false){
				    		out.println("Failed to update sales contact record.<BR><BR><BR>" 
				    			+ "<FONT SIZE=2><B>SQL statement:</B> " + sSQL);
				    	}else{
				    		try {
				    			if (sOriginalURL.compareToIgnoreCase("") != 0){
				    				response.sendRedirect(
				    						clsServletUtilities.URLDecode(sOriginalURL) + "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID);
				    			}else{
									response.sendRedirect(
											"" + SMUtilities.getURLLinkBase(getServletContext()) 
											+ sCallingClass
											+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
											+ "&id=" + request.getParameter("id")
											+ "&Status=" +  clsServletUtilities.URLEncode("Successfully updated sales contact record.")
										);
				    			}
							} catch (Exception e) {
								out.println("<R>Failed to reload edit screen - " + e.getMessage() + ".<BR>");
							}
				    	}
		    		}else{
		    			//check to see if customer code is properly filled in.
		    			sSQL = "SELECT * FROM" + 
		    					" " + SMTablesalescontacts.TableName + "," + 
		    					" " + SMTablesalesperson.TableName + "," +
		    					" " + SMTablearcustomerstatistics.TableName + 
		    				  " WHERE" + 
		    				  	" " + SMTablesalescontacts.TableName + "." + SMTablesalescontacts.salespersoncode + " =" + 
		    				  	" " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonCode + 
		    				  	" AND" + 
		    				  	" " + SMTablesalescontacts.TableName + "." + SMTablesalescontacts.scustomernumber + " =" + 
		    				  	" " + SMTablearcustomerstatistics.TableName + "." + SMTablearcustomerstatistics.sCustomerNumber;
		    				
		    			if (request.getParameter("SelectedSalesperson").compareTo("ALLSP") != 0){
		    				sSQL+= " AND "  + SMTablesalescontacts.salespersoncode + " = '" + request.getParameter("SelectedSalesperson") + "'";
		    			}
		    			
		    			//System.out.println("Customer = '" + sCustomer + "'");
		    			if (request.getParameter("SelectedCustomer") != null){
		    				if (request.getParameter("SelectedCustomer").trim().compareTo("") != 0){
		    					sSQL += " AND " + SMTablesalescontacts.TableName + "." + SMTablesalescontacts.scustomernumber + " = '" + request.getParameter("SelectedCustomer") + "'";
		    				}
		    			}
		    			
		    			if (request.getParameter("ContactName") != null){
		    				if (request.getParameter("ContactName").trim().compareTo("") != 0){
		    					sSQL = sSQL + " AND " + SMTablesalescontacts.TableName + "." + SMTablesalescontacts.scontactname + " = '" + request.getParameter("ContactName") + "'";
		    				}
		    			}
		    			
		    			sSQL += " ORDER BY" + 
		    					" " + SMTablesalescontacts.TableName + "." + SMTablesalescontacts.salespersoncode + "," + 
		    					" " + SMTablearcustomerstatistics.TableName + "." + SMTablearcustomerstatistics.sDateOfLastInvoice;
		    			
		    			ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
		    			if (rs.next()){
		    				//there is an existing salesperson-customer-contact trio, redirect user to that record.
		    				out.println("There is an existing record for the selected salesperson and customer:<BR><BR>");
		    				out.println("<TABLE BORDER=1>");
		    				out.println("<TR><TD ALIGN=CENTER><B>Record No.</B></TD>" +
		    									   "<TD ALIGN=CENTER><B>Salesperson</B></TD>" +
		    									   "<TD ALIGN=CENTER><B>Customer</B></TD>" +
		    									   "<TD ALIGN=CENTER><B>Contact Name</B></TD>" +
		    							"</TR>");
		    				out.println("<TR><TD ALIGN=CENTER><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMSalesContactEdit?id=" + rs.getInt(SMTablesalescontacts.id) + "&OriginalURL=" + sOriginalURL + "\">" + rs.getInt(SMTablesalescontacts.id) + "</A></TD>" +
										    "<TD ALIGN=CENTER>" + rs.getString(SMTablesalescontacts.salespersoncode) + "-" +
						      									  rs.getString(SMTablesalesperson.sSalespersonLastName) + ", " + 
						    									  rs.getString(SMTablesalesperson.sSalespersonFirstName) + "&nbsp;</TD>" +
										    "<TD ALIGN=CENTER>" + rs.getString(SMTablesalescontacts.scustomernumber) + "&nbsp;</TD>" +
										    "<TD ALIGN=CENTER>" + rs.getString(SMTablesalescontacts.scontactname) + "&nbsp;</TD>" +
										 "</TR>");
		    				out.println("</TABLE>");
		    			}else{
		    				
		    				//make sure all required information is filled in
		    				//at this point, customer number has already be checked.
		    				//System.out.println("Select Customer = '" + request.getParameter("SelectedCustomer") + "'");
		    				if (request.getParameter("ContactName").trim().length() == 0){
		    					//information incomplete. 
			    				out.println("<FONT COLOR=RED><B>Warning: contact name is not provided properly.</B></FONT><BR><BR>");
			    				out.println("Please use your browser's back button to goto previous page and make sure contact name are properly filled in before proceed.<BR><BR>");
		    				}else{
				    			sSQL = "INSERT INTO " + SMTablesalescontacts.TableName + "(" +
				    					" " + SMTablesalescontacts.salespersoncode + "," + 
				    					" " + SMTablesalescontacts.scustomernumber + "," +
				    					" " + SMTablesalescontacts.scustomername + "," +
				    					" " + SMTablesalescontacts.scontactname + "," +
				    					" " + SMTablesalescontacts.sphonenumber + "," +
				    					" " + SMTablesalescontacts.semailaddress + "," +
				    					" " + SMTablesalescontacts.binactive + "," +
				    					" " + SMTablesalescontacts.sdescription + "," +
				    					" " + SMTablesalescontacts.mnotes + ")" +
				    				" VALUES (" +
				    					" '" + request.getParameter("SelectedSalesperson") + "'," + 
				    					" '" + clsDatabaseFunctions.FormatSQLStatement(request.getParameter("SelectedCustomer")) + "'," + 
				    					" '" + clsDatabaseFunctions.FormatSQLStatement(Get_Customer_Name(request.getParameter("SelectedCustomer"), sDBID)) + "'," + 
				    					" '" + clsDatabaseFunctions.FormatSQLStatement( request.getParameter("ContactName")) + "'," + 
				    					" '" + request.getParameter("PhoneNumber") + "'," + 
				    					" '" + clsDatabaseFunctions.FormatSQLStatement(request.getParameter("EmailAddress")) + "'," + 
				    					" " + Integer.parseInt(request.getParameter("IsInActive")) + "," + 
				    					" '" + clsDatabaseFunctions.FormatSQLStatement(request.getParameter("Description")) + "'," + 
				    					" '" + clsDatabaseFunctions.FormatSQLStatement(request.getParameter("Note")) + "')"; 
				    				
	
						    	if (clsDatabaseFunctions.executeSQL(sSQL, getServletContext(), sDBID) == false){
						    		out.println("Failed to update sales contact record.<BR><BR><BR>" + 
						    					"<FONT SIZE=2><B>SQL statement:</B> " + sSQL);
						    	}else{
						    		//get id of latest sales contact created, hopefully it is the record you just created.
						    		sSQL = "SELECT " + 
						    					" " + SMTablesalescontacts.id + 
						    				" FROM " + 
						    					" " + SMTablesalescontacts.TableName +
						    				" ORDER BY" +
						    					" " + SMTablesalescontacts.id + " DESC" +
						    				" LIMIT 1";
						    		ResultSet rsLastID = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
						    		long lid = 0;
						    		if (rsLastID.next()){
						    			lid = rsLastID.getLong(SMTablebids.lid);
						    		}
						    		try {
											response.sendRedirect(
													"" + SMUtilities.getURLLinkBase(getServletContext()) + sCallingClass
													+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
													+ "&id=" + lid
													+ "&Status=" + clsServletUtilities.URLEncode("Successfully inserted sales contact record; new ID is: "
														+ lid)
												);
					
									} catch (Exception e) {
										out.println("<R>Failed to reload edit screen - " + e.getMessage() + ".<BR>");
									}
						    	}
		    				}
		    			}
		    			rs.close();
		    		}
		    	}else{
		    		//no matching customer, alert user to double check the customer code 
    				out.println("<FONT COLOR=RED><B>Warning: The provided customer code is not valid.</B></FONT><BR><BR>");
    				out.println("Please use your browser's back button to goto previous page and make sure a valid customer is properly filled in before proceed.<BR><BR>");
		    	}
		    	rsCustomerCheck.close();
		    	
		    }catch (SQLException ex){
	    		out.println("Error in " + SMUtilities.getFullClassName(this.toString()) + " - " + ex.getMessage() + "."); 
			}
	    	
	    }else if (request.getParameter("SUBMITREMOVE") != null){
	    	//check for accidental removal

		    	ArrayList<String> SQLStatements = new ArrayList<String>();
		    	SQLStatements.add("DELETE FROM " + SMTablesalescontacts.TableName +
						  " WHERE" + 
						  	" " + SMTablesalescontacts.id + " = " + Integer.parseInt(request.getParameter("id"))
						  	);
		    	SQLStatements.add("DELETE FROM " + SMTablecriticaldates.TableName +
						  " WHERE(" + 
						  	" (" + SMTablecriticaldates.sdocnumber + " = '" + Integer.parseInt(request.getParameter("id")) + "')"
						  	+ " AND "
						  	+ " (" +  SMTablecriticaldates.itype + " = " + Integer.toString(SMTablecriticaldates.SALES_CONTACT_RECORD_TYPE) + ")"
						  	+ ")"
						  	);
			    try{
			    	
			    	
			    	if (!clsDatabaseFunctions.executeSQLsInTransaction(SQLStatements, getServletContext(), sDBID)){
			    		out.println("Failed to delete sales contact record.<BR><BR><BR>" + 
			    					"<FONT SIZE=2><B>SQL statement:</B> " + SQLStatements.toString());
			    		
			    	}else{
			    		try {
			    			if (sOriginalURL.compareToIgnoreCase("") != 0){
			    				response.sendRedirect(
			    					clsServletUtilities.URLDecode(sOriginalURL) + "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID);
			    			}else{
								response.sendRedirect(
										"" + SMUtilities.getURLLinkBase(getServletContext()) 
										+ "smcontrolpanel.SMSalesContactSelect"
										+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
										+ "&Status=" + clsServletUtilities.URLEncode("Successfully deleted sales contact record with ID: ") 
										+ request.getParameter("id") + "."
									);
			    			}
						} catch (Exception e) {
							out.println("<R>Failed to reload edit screen - " + e.getMessage() + ".<BR>");
						}
			    	}
			    }catch (SQLException ex){
		    		out.println("Error in " + SMUtilities.getFullClassName(this.toString()) + " - " + ex.getMessage() + "."); 
				}
		    
	    }
	    out.println("</BODY></HTML>");
		}
	
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
			
			doPost(request, response);
		}
	private String Get_Customer_Name(String sCustomerCode, String sDBID){
		String sCustomerName = "N/A";
		
		try{
			String sSQL = Get_Customer_Name_SQL(sCustomerCode);
			ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID, "MySQL", "smcontrolpanel.SMSalesContactAction");
			
			if (rs.next()){
				sCustomerName = rs.getString(SMTablearcustomer.sCustomerName);
			}
			rs.close();
		}catch (SQLException ex){
			System.out.println("Error in SMSalesContactAction.Get_Customer_Name!!");
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("SQL: " + ex.getErrorCode());
		}
		
		return sCustomerName;
	}
	
	private String Get_Customer_Name_SQL(String sCode){
		
		String SQL = 
				"SELECT " + SMTablearcustomer.sCustomerName + 
				" FROM " + SMTablearcustomer.TableName + 
				" WHERE " + SMTablearcustomer.sCustomerNumber + " = '" + sCode + "'";
		//System.out.println(SQL);
		return SQL;
	}
	
}