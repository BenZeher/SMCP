package smbk;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import smcontrolpanel.SMMasterEditSelect;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMTablebkbanks;
import SMDataDefinition.SMTablebkstatements;
import ServletUtilities.clsDatabaseFunctions;

public class BKEditStatementSelect extends HttpServlet {

	private static final long serialVersionUID = 1L;
	

	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		String sCalledClassName = "smbk.BKEditStatementEdit";
		String sJavaScriptFunctionName = "preventDoubleClickForm";
		String sOnSubmit = "onsubmit = \""+sJavaScriptFunctionName+"(this);\"";
		
		SMMasterEditSelect smeditselect = new SMMasterEditSelect(
				request,
				response,
				BKBankStatement.ObjectName,
				SMUtilities.getFullClassName(this.toString()),
				sCalledClassName,
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.BKEditStatements
				);
		
		if (!smeditselect.processSession(getServletContext(), SMSystemFunctions.BKEditStatements)){
			PrintWriter m_out = response.getWriter();
			m_out.println("Error in process session: " + smeditselect.getErrorMessages());
			return;
		}
		smeditselect.printHeaderTable();
		smeditselect.getPrintWriter().println(
			"<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smbk.BKMainMenu?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
				+ smeditselect.getsDBID() + "\">Return to Bank Functions Main Menu</A><BR>");
		smeditselect.showAddNewButton(false);
		smeditselect.showEditButton(false);
	    try {
	    	smeditselect.createEditFormWithJavaScript(getEditHTML(smeditselect, request, sJavaScriptFunctionName),sOnSubmit);
		} catch (SQLException e) {
    		smeditselect.getPrintWriter().println("Could not create edit form - " + e.getMessage());
    		smeditselect.getPrintWriter().println("</HTML>");
			return;
		}
	    return;

	}
	private String getEditHTML(SMMasterEditSelect smselect, HttpServletRequest req, String sJavaScriptFunctionName) throws SQLException{

		String s = "";
	    String sID = "";
	    if (req.getParameter(BKBankStatement.Paramlid) != null){
	    	sID = req.getParameter(BKBankStatement.Paramlid);
	    }
	    
	    s += 
	    	"<B>Bank statement:<BR>"
	    	+ "<SELECT NAME=\"" + BKBankStatement.Paramlid + "\">"
	    	+ "<OPTION VALUE=\"" + "" + "\">*** Select bank statement ***";
	    
	    //Drop down the existing bank statements:
	    String SQL = "SELECT "
	    	+ " " + SMTablebkstatements.TableName + "." + SMTablebkstatements.lid
	    	+ ", " + SMTablebkbanks.TableName + "." + SMTablebkbanks.sshortname
	    	+ ", " + SMTablebkstatements.TableName + "." + SMTablebkstatements.datstatementdate
	    	+ ", IF(" + SMTablebkstatements.TableName + "." + SMTablebkstatements.iposted + " = 1, '(POSTED)', '') AS POSTED"
	    	+ " FROM " + SMTablebkstatements.TableName + " LEFT JOIN " + SMTablebkbanks.TableName + " ON "
	    	+ SMTablebkstatements.TableName + "." + SMTablebkstatements.lbankid + " = " + SMTablebkbanks.TableName + "." + SMTablebkbanks.lid  
	    	+ " ORDER BY " + SMTablebkstatements.TableName + "." + SMTablebkstatements.lid
	    ;
	    
	    try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(),
					smselect.getsDBID(), "MySQL", SMUtilities
							.getFullClassName(this.toString())
							+ ".getEditHTML - user: " 
							+ smselect.getUserID()
							+ " - "
							+ smselect.getFullUserName()
					);
			while (rs.next()) {
				String sReadCode = Long.toString(rs.getLong(SMTablebkstatements.TableName + "." + SMTablebkstatements.lid));
				s += "<OPTION";
				if (sReadCode.compareToIgnoreCase(sID) == 0) {
					s += " SELECTED=yes";
				}
				s += " VALUE=\"" + sReadCode + "\">" + sReadCode + " - "
					+ rs.getString(SMTablebkbanks.TableName + "." + SMTablebkbanks.sshortname)
					+ " " + rs.getString(SMTablebkstatements.TableName + "." + SMTablebkstatements.datstatementdate)
					+ " " + rs.getString("POSTED")
				;
			}
			rs.close();
		} catch (SQLException e) {
			s += "</SELECT><BR><B>Error reading bank statement data - " + e.getMessage();
		}
		s+= "</SELECT>";
		
		//Add a button to 'Edit':
		s += "<BR><INPUT TYPE=SUBMIT NAME='" + SMMasterEditSelect.SUBMIT_EDIT_BUTTON_NAME 
			+ "' VALUE='Edit Selected " + BKBankStatement.ObjectName
			+ "' STYLE='height: 0.24in'>&nbsp;&nbsp;";
		//
		s += "<INPUT TYPE=SUBMIT NAME='" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME 
			+ "' VALUE='Add New " + BKBankStatement.ObjectName
			+ "' STYLE='height: 0.24in'>";
		
		s += 
	    	"<B> For Bank:&nbsp;"
	    	+ "<SELECT NAME=\"" + BKBankStatement.Paramlbankid + "\">"
	    ;
		s += preventDoubleClickjavaScript(SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME,SMMasterEditSelect.SUBMIT_EDIT_BUTTON_NAME, sJavaScriptFunctionName);
	    //Drop down the banks:
	    SQL = "SELECT "
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
				smselect.getsDBID(), "MySQL", SMUtilities
					.getFullClassName(this.toString())
					+ ".getEditHTML - user: " 
					+ smselect.getUserID()
					+ " - "
					+ smselect.getFullUserName()
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
		
		return s;
	}
	public String preventDoubleClickjavaScript(String submitButtonName, String editButtonName, String sJavaScriptFunctionName){
		String s = "";
		s += " <script>\n"
		  +  " function "+sJavaScriptFunctionName+"(form) {\n"
		  +  "     document.documentElement.style.cursor = \"wait\";\n"
		  +  " form."+submitButtonName+".style = \"color: grey; pointer-events:none; cursor: not-allowed; display: inline-block; text-decoration: none;\";\n"
		  +  " form."+editButtonName+".style = \"color: grey; pointer-events:none; cursor: not-allowed; display: inline-block; text-decoration: none;\";\n"
		  +  " }\n"
		  +  " </script>\n";
		return s;
	}
	

	
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}