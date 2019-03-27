package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.SMTablecolortable;
import SMDataDefinition.SMTablemechanics;
import SMDataDefinition.SMTablemechanicservicetypes;
import ServletUtilities.clsDatabaseFunctions;

public class SMEditMechanicsSave extends HttpServlet{
	private static final long serialVersionUID = 1L;
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		response.setContentType("text/html");

		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMEditMechanics))
		{
			return;
		}

		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		String title = "Manage Mechanics";
		String subtitle = "";
		out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

		//Print a link to the first page after login:
		out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");


		//need to check integrity of input when adding new mechanics.
		if (
			(request.getParameter("MECHINIT").compareTo("") == 0)
			|| (request.getParameter("MECHNAME").compareTo("") == 0) 
			|| (request.getParameter("MECHANICID").compareTo("") == 0)
		){
			out.println ("<BR>");
			out.println ("<H4>Invalid input - Mechanic's name and initials must BOTH have a value - Please try again.</H4><BR>");
		}else{
			//IF it's an 'ADD':
			String sSQL;
			if (request.getParameter("MECHANICID").toString().compareTo(SMEditMechanics.NEW_MECHANIC_ID) == 0){
				//get color coordinates
				int iRow = 0;
				int iCol = 0;
				sSQL = "SELECT * FROM " + SMTablecolortable.TableName + " WHERE " + SMTablecolortable.scolorcode + " = '" + request.getParameter("colorhex").substring(1) + "'";
				//System.out.println("[1345120679] SQL = " + sSQL);
				try{
					ResultSet rsCoor = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
					if (rsCoor.next()){
						iRow = rsCoor.getInt(SMTablecolortable.irow);
						iCol = rsCoor.getInt(SMTablecolortable.icol);
					}
				}catch(SQLException ex){
					System.out.println("[1345120679] Error retrieving color coordinates for mechanic " + request.getParameter("MECHNAME"));
					System.out.println("[1345120779] SQLException: " + ex.getErrorCode() + " - " + ex.getMessage());
				}
				
				sSQL = "INSERT INTO " + SMTablemechanics.TableName + "(" 
					+ ", " + SMTablemechanics.sAssistant 
					+ ", " + SMTablemechanics.semployeeid
					+ ", " + SMTablemechanics.sMechFullName 
					+ ", " + SMTablemechanics.sMechInitial 
					+ ", " + SMTablemechanics.sMechLocation 
					//+ ", " + SMTablemechanics.sMechSSN //MechSSNSCO 
					+ ", " + SMTablemechanics.sstartingtime 
					+ ", " + SMTablemechanics.sMechColorCodeRow 
					+ ", " + SMTablemechanics.sMechColorCodeCol 
					+ ", " + SMTablemechanics.sVehicleLabel 
					+ ") VALUES (" 
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(request.getParameter("MECHASSISTANT")) + "'"
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(request.getParameter("EMPLOYEEID")) + "'"
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(request.getParameter("MECHNAME")) + "'"
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(request.getParameter("MECHINIT")).trim() + "'"
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(request.getParameter("SELECTEDLOC")) + "'"
					//+ ", '" + SMUtilities.FormatSQLStatement(request.getParameter("MECHSSN"))  + "'" //MechSSNSCO
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(request.getParameter("STARTINGTIME")) + "'"
					+ ", " + Integer.toString(iRow) 
					+ ", " + Integer.toString(iCol)
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(request.getParameter("MECHVEHICLELABEL")) + "'"
					+ ")";
				try {
					clsDatabaseFunctions.executeSQL(sSQL, getServletContext(), sDBID, "MySQL", SMUtilities.getFullClassName(this.toString()));
				} catch (SQLException e) {
					out.println("Error inserting mechanics with SQL: " + sSQL + " - " + e.getMessage() + "<BR>");
				}
			}else{
				
				//get color coordinates
				int iRow = 0;
				int iCol = 0;
				sSQL = "SELECT * FROM " + SMTablecolortable.TableName 
					+ " WHERE " + SMTablecolortable.scolorcode + " = '" 
					+ request.getParameter("colorhex").substring(1) + "'";
				try{
					ResultSet rsCoor = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
					if (rsCoor.next()){
						iRow = rsCoor.getInt(SMTablecolortable.irow);
						iCol = rsCoor.getInt(SMTablecolortable.icol);
					}
				}catch(SQLException ex){
					System.out.println("[1345120679] Error retrieving color coordinates for mechanic " + request.getParameter("MECHNAME"));
					System.out.println("[1345120879] SQLException: " + ex.getErrorCode() + " - " + ex.getMessage());
				}
				
				//save mechanic info
				sSQL = "UPDATE " + SMTablemechanics.TableName
					+ " SET " 
					+ SMTablemechanics.sMechInitial + " = '" + clsDatabaseFunctions.FormatSQLStatement(request.getParameter("MECHINIT")).trim() + "'"
					+ ", " + SMTablemechanics.semployeeid + " = '" + clsDatabaseFunctions.FormatSQLStatement(request.getParameter("EMPLOYEEID")) + "'"
					+ ", " + SMTablemechanics.sMechFullName + " = '" + clsDatabaseFunctions.FormatSQLStatement(request.getParameter("MECHNAME")) + "'"
					+ ", " + SMTablemechanics.sMechLocation + " = '" + clsDatabaseFunctions.FormatSQLStatement(request.getParameter("SELECTEDLOC")) + "'"
					+ ", " + SMTablemechanics.sAssistant + " = '" + clsDatabaseFunctions.FormatSQLStatement(request.getParameter("MECHASSISTANT")) + "'"
					+ ", " + SMTablemechanics.sVehicleLabel + " = '" + clsDatabaseFunctions.FormatSQLStatement(request.getParameter("MECHVEHICLELABEL")) + "'"
					+ ", " + SMTablemechanics.sstartingtime + " = '" + clsDatabaseFunctions.FormatSQLStatement(request.getParameter("STARTINGTIME")) + "'"
					+ ", " + SMTablemechanics.sMechColorCodeRow + " = " + Integer.toString(iRow)
					+ ", " + SMTablemechanics.sMechColorCodeCol + " = " + Integer.toString(iCol)
					//+ ", " + SMTablemechanics.sMechSSN + " = '" + SMUtilities.FormatSQLStatement(request.getParameter("MECHSSN"))  + "'"//MechSSNSCO
					+ " WHERE " + SMTablemechanics.lid + " = " + request.getParameter("MECHANICID").toString(); 
				try {
					clsDatabaseFunctions.executeSQL(sSQL, getServletContext(), sDBID, "MySQL", SMUtilities.getFullClassName(this.toString()));
				} catch (SQLException e) {
					out.println("Error updating mechanics with SQL: " + sSQL + " - " + e.getMessage() + "<BR>");
				}
			out.println ("<BR>");
			out.println ("<H4>Information saved.</H4><BR><BR>");
			}
			
			//save the service types
			String sMechServiceTypes[];
			sMechServiceTypes = request.getParameterValues("SELECTEDTYPE");
			
			//First delete all the service types for this mechanic
			sSQL = "DELETE FROM " + SMTablemechanicservicetypes.TableName 
					+ " WHERE " + SMTablemechanicservicetypes.imechanicid + "=" + request.getParameter("MECHANICID");
			try {
				clsDatabaseFunctions.executeSQL(sSQL, getServletContext(), sDBID, "MySQL", SMUtilities.getFullClassName(this.toString()));
			} catch (SQLException e) {
				out.println("Error deleting from mechanics service type with SQL: " + sSQL + " - " + e.getMessage() + "<BR>");
			}
			
			//Insert all the selected service types if any were selected.
			if(sMechServiceTypes.length > 0) {
				sSQL = "INSERT INTO "  + SMTablemechanicservicetypes.TableName 
					+ " (" + SMTablemechanicservicetypes.imechanicid +  "," + SMTablemechanicservicetypes.sservicetypecode + ")"
					+ " VALUES ";
				for (int i = 0; i < sMechServiceTypes.length; i++){
					if(i > 0) {
						sSQL += ",";
					}
					sSQL += " (" + request.getParameter("MECHANICID") + ",'" + sMechServiceTypes[i] + "')";
				}
				try {
					clsDatabaseFunctions.executeSQL(sSQL, getServletContext(), sDBID, "MySQL", SMUtilities.getFullClassName(this.toString()));
				} catch (SQLException e) {
					out.println("Error inserting mechanics service type with SQL: " + sSQL + " - " + e.getMessage() + "<BR>");
				}
			}
		}	  
		out.println("</BODY></HTML>");
	}
}