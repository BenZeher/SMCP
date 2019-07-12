package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ConnectionPool.WebContextParameters;
import SMClasses.SMLogEntry;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTablesystemlog;
import ServletUtilities.clsManageRequestParameters;

public class SMDisplayLoggingOperations  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String FIELD_DELIMITER = " - ";
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.SMDisplayLoggingOperations
		)
		){
			return;
		}
		PrintWriter out = response.getWriter();

		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		String title = "SM List Logging Operations";
		String subtitle = "";
		
		out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
		out.println(SMUtilities.getMasterStyleSheetLink());
		
		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
		//Print a link to the first page after login:
		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
		out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.SMDisplayLoggingOperations) 
				+ "\">Summary</A><BR><BR>");
		
		out.println("<I><B>LIST OF LOGGING OPERATIONS:</B></I>");
		out.println("<BR>    The 'marker', which appears in the '" + SMTablesystemlog.soperation + "' field of the '" + SMTablesystemlog.TableName
			+ "' table is on the left, description of the event being recorded is on the right.");
		
		String s = "";
		s += "<TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\""
				//+ " style = \" width:100%; \" "
				+ " ID = \"" + "LOGGINGOPERATIONS" + "\""
				+ ">\n";
		
		//Headings:
		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\" >\n";
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\" >"
				+ "LABEL"
				+ "</TD>\n"
			;
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\" >"
				+ "DESCRIPTION"
				+ "</TD>\n"
			;
	
		s += "  </TR>\n\n";
		
		ArrayList<String>arrOperationsList = SMLogEntry.getOperationDescriptions(FIELD_DELIMITER);
		boolean bOddRow = true;
		for (int i = 0; i < arrOperationsList.size(); i++){
			
			String[] sLine  = arrOperationsList.get(i).split(FIELD_DELIMITER);
			
			String sBackgroundColor = SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN;
			if (bOddRow){
				sBackgroundColor = SMMasterStyleSheetDefinitions.TABLE_ROW_ODD;
			}
			s += "  <TR class = \"" + sBackgroundColor + "\" > \n";
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED_WITH_BORDER + "\""
					+ " style = \" font-weight:bold; color: black; \" >"
		    		+ sLine[0]
					+ "</TD>\n"
				;
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED_WITH_BORDER + "\""
					+ " style = \" font-style: italic; color: black; \" >"
		    		+ sLine[1]
					+ "</TD>\n"
				;
			s += "  </TR> \n\n";
			
			bOddRow = !bOddRow;
		}
		
		s += "</TABLE>" + "\n";
		
		out.println(s);
		
		out.println("</BODY></HTML>");
		return;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}

