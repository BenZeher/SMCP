package smcontrolpanel;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import SMDataDefinition.SMTabledeliveryticketterms;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;


public class SMEditDeliveryTicketTerms  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static final String ParammTerms = "mTerms";
	public static final String ParamsDescription = "sDescription";
	public static final String UPDATE_BUTTON_LABEL = "UPDATE";
	public static final String FORM_NAME = "MAINFORM";
	public static final String OBJECT_NAME = "Delivery Ticket Terms";



	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		boolean isNewRecord = false;
		
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				OBJECT_NAME,
				SMUtilities.getFullClassName(this.toString()),
				"smcontrolpanel.SMEditDeliveryTicketTerms",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.SMEditDeliveryTicketTerms
				);
		
		String sWarnings = "";
		String sStatus = "";
		//If its an update, process that:
		smedit.processSession(getServletContext(), SMSystemFunctions.SMEditDeliveryTicketTerms);
		
		HttpSession currentSession = smedit.getCurrentSession();
		String sDBID = (String) currentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		if(request.getParameter(UPDATE_BUTTON_LABEL) != null){
			try{
				String m_mTerms = request.getParameter(ParammTerms);
				String m_sDescription = request.getParameter(ParamsDescription);
				save_without_data_transaction(getServletContext(), 
						sDBID, 
						smedit.getUserID(),
						smedit.getFullUserName(),
						m_mTerms, m_sDescription, 
						isNewRecord);
				
			}catch (Exception e){
				sWarnings = "Error saving terms:" + e.getMessage();
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + "smcontrolpanel.SMEditDeliveryTicketTerms"
						+ "?Warning=" + sWarnings
						+ "&Status=" 
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID());
				return;
			}
			sStatus = "Terms saved successfully. ";
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + "smcontrolpanel.SMEditDeliveryTicketTerms"
					+ "?Status=" + sStatus
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID());
			return;
		}
		
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.SMManageDeliveryTickets)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		//Get the company name from the session
		String sCompanyName = "";
		try {
			sCompanyName = (String) currentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		} catch (Exception e1) {
			smedit.getPWOut().println("Error [1415807186] getting session attribute - " + e1.getMessage());
			return;
		}
		//*HTML Starts Here
		smedit.getPWOut().println(getHeaderString("edit delivery ticket terms", 
				"", 
				SMUtilities.getInitBackGroundColor(getServletContext(), smedit.getsDBID()), 
				SMUtilities.DEFAULT_FONT_FAMILY,
				sCompanyName));
		
		//If there is a warning from trying to input previously, print it here:
		sWarnings = clsManageRequestParameters.get_Request_Parameter("Warning", smedit.getRequest());
			if (sWarnings.compareToIgnoreCase("") != 0){
				smedit.getPWOut().println("<B><FONT COLOR=\"RED\">WARNING: " + sWarnings + "</FONT></B><BR>");
			}
		//If there is a status from trying to input previously, print it here:
		sStatus = clsManageRequestParameters.get_Request_Parameter("Status", smedit.getRequest());
			if (sStatus.compareToIgnoreCase("") != 0){
				smedit.getPWOut().println("<B>" + sStatus + "</B><BR>");
			}
		
		//Print a link to the first page after login:
		smedit.getPWOut().println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
			+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID() + "\">Return to user login</A><BR>");
		
		//Start form
	
		smedit.getPWOut().println("<FORM NAME='" + FORM_NAME + " ACTION='" 
				+ SMUtilities.getURLLinkBase(getServletContext()) + smedit.getCalledClass() + "' METHOD='POST'>");
		smedit.getPWOut().println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + smedit.getsDBID() + "'>");
		smedit.getPWOut().println("<INPUT TYPE=HIDDEN NAME=\"" + "CallingClass" + "\" VALUE=\"" 
					+ SMUtilities.getFullClassName(this.toString()) + "\">");
			
		String mTerms = "";
		String sDescription	= "";
		
		try{
			//Get the record to edit:
			String sSQL = "SELECT * FROM " + SMTabledeliveryticketterms.TableName;
			
			ResultSet rsTerms = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), smedit.getsDBID());

			if(rsTerms.next()){
				mTerms = rsTerms.getString(SMTabledeliveryticketterms.mTerms);
				sDescription = rsTerms.getString(SMTabledeliveryticketterms.sDescription);
			}else{
				isNewRecord = true;
				try{
				save_without_data_transaction(getServletContext(), 
						sDBID, 
						smedit.getUserID(),
						smedit.getFullUserName(),
						"", "", 
						isNewRecord);
				}catch(Exception e){
					sWarnings = "Error inserting new terms record:" + e.getMessage();
					response.sendRedirect(
							"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + "smcontrolpanel.SMEditDeliveryTicketTerms"
							+ "?Warning=" + sWarnings
							+ "&Status=" 
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID());
					rsTerms.close();
					return;
				}
			};
			
			rsTerms.close();
		}catch (SQLException ex){
			smedit.getPWOut().println("<BR>Error reading delivery ticket terms information - " + ex.getMessage());
		}
		//Start the outer table here:
		smedit.getPWOut().println("<TABLE BORDER=12 CELLSPACING=2>");
		smedit.getPWOut().println("<TR><TD ALIGN=RIGHT> Description:</TD><TD ALIGN=LEFT>"
				+ "<INPUT TYPE=TEXT NAME='" + ParamsDescription + "' VALUE='"  + sDescription 
				+ "' MAXLENGTH='" + SMTabledeliveryticketterms.sDescriptionLength +"'></TD></TR>");
		smedit.getPWOut().println("<TR><TD ALIGN=RIGHT> Terms:</TD><TD><TEXTAREA NAME=\"" + ParammTerms + "\" rows=10 cols=120 >" + mTerms + "</TEXTAREA></TD></TR>");	
		smedit.getPWOut().println("</TABLE");
		
		smedit.getPWOut().println("<BR><P><INPUT TYPE=SUBMIT NAME='" + UPDATE_BUTTON_LABEL + "' VALUE='Update " + OBJECT_NAME 
				+ "' STYLE='height: 0.24in'></P>");
		
		smedit.getPWOut().println("</FORM>");
	}
		
		
	private String getHeaderString(
			String title, 
			String subtitle, 
			String sbackgroundcolor, 
			String sfontfamily, 
			String scompanyname){
		String s = SMUtilities.DOCTYPE
		+ "<HTML>"
		+ "<HEAD>";
		s += "<TITLE>" + subtitle + "</TITLE>"
		+ SMUtilities.faviconLink()
		//This line should keep the font widths 'screen' wide:
		+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />"
		+ "</HEAD>\n" 
		+ "<BODY BGCOLOR="
		+ "\"" 
		+ sbackgroundcolor
		+ "\""
		+ " style=\"font-family: " + sfontfamily + ";\""
		+ "\">"
		;
		s += "<TABLE BORDER=0>"
		+"<TR><TD VALIGN=BOTTOM><H3>" + scompanyname + ": " + title + "</H3></TD>"
		;

		if (subtitle.compareTo("") != 0){  
			s = s + "<TD VALIGN=BOTTOM><H4>&nbsp;-&nbsp;" + subtitle + "</H4></TD>";
		}

		s = s + "</TR></TABLE>";
		return s;
	}
	
public void save_without_data_transaction (ServletContext context, 
								String sConf, 
								String sUserID,
								String sUserFullName,
								String sTerms,
								String sDescription,
								boolean isNewRecord) throws Exception{
    	
       	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sConf, 
    			"MySQL", 
    			this.toString() + " - user: " + sUserID + " - " + sUserFullName
    			);
    	
    	if (conn == null){
    		throw new Exception("Error [1446751982] opening data connection.");
    	}
    	String SQL = "";
    	try {
    		//Statement to update all records.
    		if(!isNewRecord){
    			SQL = " UPDATE " + SMTabledeliveryticketterms.TableName + " SET "
    			+ " " + SMTabledeliveryticketterms.mTerms + " = '" + sTerms + "'"
    			+ ", " + SMTabledeliveryticketterms.sDescription + " = '" + sDescription + "'"
    			+ ", " + SMTabledeliveryticketterms.datLastMaintained + " = NOW()"
    			;
    		}else{
    			SQL = " INSERT INTO " + SMTabledeliveryticketterms.TableName + " ("
    	    			+  SMTabledeliveryticketterms.mTerms 
    	    			+ ", " + SMTabledeliveryticketterms.sDescription
    	    			+ ", " + SMTabledeliveryticketterms.datLastMaintained + ") VALUES ('"
    	    			+ sTerms +"', '" + sDescription + "', NOW())"
    	    			;
    		}
    		try{
    		    Statement stmt = conn.createStatement();
    		    stmt.executeUpdate(SQL);
    		}catch (Exception ex) {
    			
    			throw new Exception ("Error [1446751983] in insert/update with SQL: " + SQL + " - " + ex.getMessage());
    		}
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080487]");
			throw new Exception(e.getMessage());
		}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547080488]");
    	
    }

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}


