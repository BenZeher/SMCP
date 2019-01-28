package smbk;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTablebkaccountentries;
import SMDataDefinition.SMTablebkbanks;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class BKEntryImportSelect extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public static final String INCLUDES_BKENTRY_HEADER_ROW = "INCLUDESHEADERROW";
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.BKImportBankEntries)){
	    	return;
	    }
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sCalledClassName = "BKEntryImportAction";
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) +
	    				" " + (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    
	    String title = "Import bank account entries.";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    
		//If there is a warning from trying to input previously, print it here:
		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
	    if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
		String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
	    if (! sStatus.equalsIgnoreCase("")){
			out.println("<B>STATUS: " + sStatus + "</B><BR>");
		}
	    
	    //Print a link to the first page after login:
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smbk.BKMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to bank functions main menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.BKImportBankEntries) 
	    		+ "\">Summary</A><BR><BR>");
	    
	    out.println("<B>NOTE:</B> The import file must have a separate entry on each line,"
	    		+ " and each column must be separated by a comma."
	    		+ "  The first column must have the check number,"
	    		+ " the second column must have the issue date in M/D/YYYY format, and the third column "
	    		+ "must have the amount (zeroes and commas allowed, but no blanks)."
	    		+ "  The file can have more columns after the first three (if you want to include"
	    		+ " things like description, etc., for your own purposes), but the"
	    		+ " import will ignore these.<BR><B>NOTE ALSO:</B> This function will not import an entry"
	    		+ " if an entry with the same document number is already in the system.<BR>"
	    );
	    
	    out.println("<FORM NAME=\"MAINFORM\" action=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smbk." + sCalledClassName + "\"" 
	    		+ " method=\"post\""
	    		+ " enctype=\"multipart/form-data\""
	    		+ ">");

	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='CallingClass' VALUE='" 
	    	+ SMUtilities.getFullClassName(this.toString()) + "'>");
	    
		out.println("<TABLE BORDER=1>");

	    //Choose import file:
	    out.println("<TR>");
	    out.println("<TD ALIGN=RIGHT>");
	    out.println("Choose import file:");
	    out.println("</TD>");
	    out.println("<TD>");
	    out.println("<INPUT TYPE=FILE NAME='ImportFile'>");
	    out.println("</TD>");
	    out.println("</TR>");
	    
	    //Checkbox to indicate if the file includes a header row:
	    out.println("<TR>");
	    out.println("<TD ALIGN=RIGHT>");
	    out.println("File to be imported includes a header row?");
	    out.println("</TD>");
	    out.println("<TD>");
	    out.println(
			"<INPUT TYPE=CHECKBOX NAME=" + "\"" + INCLUDES_BKENTRY_HEADER_ROW + "\" " + "" + " width=0.25>" 
			+ ""
		);
	    out.println("</TD>");
	    out.println("</TR>");
	    
	    //Bank:
	    out.println("<TR>");
	    out.println("<TD ALIGN=RIGHT>");
	    out.println("Bank:");
	    out.println("</TD>");
	    out.println("<TD>");
	    //Drop down the banks:
	    String sID = "";
	    if (request.getParameter(BKBankStatement.Paramlid) != null){
	    	sID = request.getParameter(BKBankStatement.Paramlid);
	    }
		String s = "<SELECT NAME=\"" + BKBankStatement.Paramlbankid + "\">";
	    String SQL = "SELECT "
	    	+ " " + SMTablebkbanks.lid
	    	+ ", " + SMTablebkbanks.sshortname
	    	+ " FROM " + SMTablebkbanks.TableName
	    	+ " WHERE ("
	    		+ "(" + SMTablebkbanks.iactive + " = 1)"
	    	+ ")"
	    	+ " ORDER BY " + SMTablebkbanks.sshortname
	    ;
	    try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(),
				sDBID, "MySQL", SMUtilities
					.getFullClassName(this.toString())
					+ ".getEditHTML - user: " 
					+ sUserID
					+ " - "
					+ sUserFullName
					);
			while (rs.next()) {
				String sReadCode = Long.toString(rs.getLong(SMTablebkbanks.lid));
				s += "<OPTION";
				if (sReadCode.compareToIgnoreCase(sID) == 0) {
					s += " SELECTED=yes";
				}
				s += " VALUE=\"" + sReadCode + "\">" + sReadCode + " - "
						+ rs.getString(SMTablebkbanks.sshortname);
			}
			rs.close();
		} catch (SQLException e) {
			s += "</SELECT><BR><B>Error reading bank data - " + e.getMessage();
		}
		s+= "</SELECT>";
		out.println(s);
		out.println("</TD>");
		out.println("</TR>");
	    
	    //Description:
	    out.println("<TR>");
	    out.println("<TD ALIGN=RIGHT>");
	    out.println("Description (this will appear on each entry):");
	    out.println("</TD>");
	    out.println("<TD>");
	    
	    out.println("<INPUT TYPE=TEXT NAME=\"" + SMTablebkaccountentries.sdescription + "\""
	    		+ " VALUE=\"" 
	    			+ clsManageRequestParameters.get_Request_Parameter(SMTablebkaccountentries.sdescription, request)+ "\""
	    		+ " MAXLENGTH='" + Integer.toString(SMTablebkaccountentries.sdescriptionlength) + "'"
	    		+ " SIZE='70'"
	    		+ ">"
	    );
	    out.println("</TD>");
	    out.println("</TR>");

	    out.println("</TABLE>");
	    out.println("<INPUT TYPE=SUBMIT NAME='SubmitFile' VALUE='Import file' STYLE='width: 2.00in; height: 0.24in'>");
		out.println("</FORM>");
		out.println("</BODY></HTML>");
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}