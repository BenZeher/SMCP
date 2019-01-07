package TimeCardSystem;

import java.io.*;

//import java.util.Date;
import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsDatabaseFunctions;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;

/** Servlet that inserts In-Time records into the the time entry table.*/

public class PeriodTotalReportGenerate extends HttpServlet{

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {

		boolean bExportAsCommaDelimited = false;
		if(request.getParameter(PeriodTotalDispCriteriaSelection.SUBMIT_EXPORT_BUTTON_NAME) != null){
			bExportAsCommaDelimited = request.getParameter(PeriodTotalDispCriteriaSelection.SUBMIT_EXPORT_BUTTON_NAME)
					.compareToIgnoreCase(PeriodTotalDispCriteriaSelection.SUBMIT_EXPORT_BUTTON_VALUE) == 0;
		}

		PrintWriter out = response.getWriter();

		HttpSession CurrentSession = request.getSession();

		try {

			SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
			int iMultipleSelected = 0;
			if (request.getParameter("SelectMultiplePeriods") == null){
				iMultipleSelected = 0;
			}else{
				iMultipleSelected = 1;
			}

			Enumeration<?> paramNames = request.getParameterNames();
			ArrayList<String> alDepartments = new ArrayList<String>(0);
			while(paramNames.hasMoreElements()) {
				String s = paramNames.nextElement().toString();
				if (s.substring(0, 2).compareTo("!!") == 0){
					//if "all departments" is selected, disregard all other selections.
					if (s.substring(2).compareTo("ALLDEPT") == 0){
						alDepartments.clear();
						alDepartments.add(s.substring(2));
						//System.out.println("All departments selected");
						break;
					}else{
						alDepartments.add(s.substring(2));
					}
				}
			}
			for (int i=0;i<alDepartments.size();i++){
				//System.out.println("[1389641945] *" + alDepartments.get(i));
			}

			String sSQL = TimeCardSQLs.Get_Period_Total_Time_SQL(request.getParameter("SelectedPeriodFrom"),
					iMultipleSelected,
					request.getParameter("SelectedPeriodTo"),
					request.getParameter("SelectedEmployee"), 
					alDepartments);
			//out.println ("<BR> " + sSQL + "<BR>");
			ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
			if(bExportAsCommaDelimited){
				while(rs.next()){
					//Unless all records are not finalized do not export
					if (rs.getInt("iFinalized") == 0){
						response.sendRedirect(ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.PeriodTotalDispCriteriaSelection" 
								+ "?Warning=All time totals must be finalized before exporting to CSV.");
						rs.close();
						return;
					}
				}
				rs.beforeFirst();    	

				response.setContentType("text/csv");
				String disposition = "attachment; fileName= " + "PeriodTotalReport.csv";
				response.setHeader("Content-Disposition", disposition);
			}else{
				response.setContentType("text/html");
			}

			if(!bExportAsCommaDelimited){
				String title = "Time Summary Report - ";
				String subtitle = (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB);
				out.println(TimeCardUtilities.TCTitleSubBGColor(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));
				out.println("<BR><A HREF=\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) 
					+ "TimeCardSystem.AdminMain\">Return to main menu</A><BR><BR>");
			}
			//display this only when there is a parameter passed in

			String sPeriodFrom = "";
			String sPeriodTo = "";
			String sEmployee = "";
			//String sDepartment = "";

			if (iMultipleSelected == 0){
				if (request.getParameter("SelectedPeriodFrom").compareTo("") != 0){
					sPeriodFrom = request.getParameter("SelectedPeriodFrom");
					if(!bExportAsCommaDelimited){
						out.println("<B>Period date: " + formatter.format(Timestamp.valueOf(sPeriodFrom + " 00:00:00")) + "</B><BR>");	 
					}
				}
			}else{
				if (request.getParameter("SelectedPeriodFrom").compareTo("") != 0){
					sPeriodFrom = request.getParameter("SelectedPeriodFrom");
					if(!bExportAsCommaDelimited){
						out.println("<B>Starting period date: " + formatter.format(Timestamp.valueOf(sPeriodFrom + " 00:00:00")) + "</B><BR>");	 
					}
				}

				if (request.getParameter("SelectedPeriodTo").compareTo("") != 0){
					sPeriodTo = request.getParameter("SelectedPeriodTo");
					if(!bExportAsCommaDelimited){
						out.println("<B>Ending period date: " + formatter.format(Timestamp.valueOf(sPeriodTo + " 00:00:00")) + "</B><BR>");	    		
					}
				}
			}

			if(!bExportAsCommaDelimited){
				if (request.getParameter("SelectedEmployee").compareTo("") != 0){
					sEmployee = request.getParameter("SelectedEmployee");
					out.println("<B>Employee: " + request.getParameter("SelectedEmployee") + "</B><BR>");	    		
				}else{
					if ((alDepartments.size() > 0) && (alDepartments.get(0).toString().compareTo("ALLDEPT") != 0)) {
						String s = "<B>Department: ";
						for (int i=0; i < alDepartments.size(); i++){
							sSQL = TimeCardSQLs.Get_Department_Info_SQL(Integer.parseInt(alDepartments.get(i)));
							ResultSet rsDeptInfo = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
							if (rsDeptInfo.next()){
								//sDepartment = rsDeptInfo.getString("iDeptID");
								s += rsDeptInfo.getString("sDeptDesc") + ", ";
							}
							rsDeptInfo.close();
						}
						out.println(s.substring(0, s.length() - 2) + "</B><BR>");
					}else{
						out.println("<B>Department: ALL </B><BR>");
					}
					out.println("<B>Employee: ALL </B><BR>");
				}
			}
			//get current URL
			String sCurrentURL;
			sCurrentURL = TimeCardUtilities.URLEncode(request.getRequestURI().toString() + "?" + request.getQueryString());
			//sCurrentURL = sCurrentURL.replaceAll("&", "*");

			double dTotalRegular = 0;
			double dTotalDouble = 0;
			double dTotalOriTotal = 0;
			double dTotalOver = 0;
			double dTotalLeave = 0;
			String sCurrentDepartment = "";

			if(bExportAsCommaDelimited){
				out.println(
						"Employee ID," 
								+ "Employee," 
								+ "Period End," 
								+ "Total," 
								+ "Regular," 
								+ "Overtime," 
								+ "Doubletime," 
								+ "Leave"
						);
			}else{
				out.println("<HR><BR><TABLE BORDER=1 WIDTH=100%>");
				out.println("<TR>" +
						"<TD ALIGN=CENTER WIDTH=10%><H4>Employee ID</H4></TD>" +
						"<TD ALIGN=CENTER WIDTH=25%><H4>Employee</H4></TD>" +
						"<TD ALIGN=CENTER WIDTH=12%><H4>Period End</H4></TD>" +
						"<TD ALIGN=CENTER WIDTH=10%><H4>Total</H4></TD>" +
						"<TD ALIGN=CENTER WIDTH=10%><H4>Regular</H4></TD>" +
						"<TD ALIGN=CENTER WIDTH=10%><H4>Overtime</H4></TD>" +
						"<TD ALIGN=CENTER WIDTH=10%><H4>Doubletime</H4></TD>" +
						"<TD ALIGN=CENTER WIDTH=15%><H4>Leave</H4></TD>" +
						"<TD ALIGN=CENTER WIDTH=5%></TD>" +
						"<TD ALIGN=CENTER WIDTH=10%></TD></TR>");
			}
			while (rs.next()){
				if (sCurrentDepartment.compareTo(rs.getString("sDeptDesc")) != 0){
					sCurrentDepartment = rs.getString("sDeptDesc");
					if(!bExportAsCommaDelimited){
						out.println("<TR><TD COLSPAN=10><FONT SIZE=4><B>" + sCurrentDepartment + "</B></FONT></TD>");
					}
				}
				//Department
				if(!bExportAsCommaDelimited){
					out.println("<TR>");
					out.println("<TD ALIGN=CENTER>" + rs.getString("sEmployeeID") + "</TD>");  
					//employee name
					out.println("<TD ALIGN=CENTER>" + rs.getString("sEmployeeFirstName") + " " + rs.getString("sEmployeeLastName") + "</TD>");
					//Period end date
					out.println("<TD ALIGN=CENTER>" + formatter.format(rs.getTimestamp("datPeriodEndDate")) + "</TD>");    
					//Original Total
					out.println("<TD ALIGN=CENTER>" + rs.getDouble("dPeriodTotal") + "</TD>");
					dTotalOriTotal = dTotalOriTotal + rs.getDouble("dPeriodTotal");
					//Regular
					out.println("<TD ALIGN=CENTER>" + rs.getDouble("dPeriodRegular") + "</TD>");  
					dTotalRegular = dTotalRegular + rs.getDouble("dPeriodRegular");
					//Overtime
					out.println("<TD ALIGN=CENTER>" + rs.getDouble("dPeriodOverTime") + "</TD>");  
					dTotalOver = dTotalOver + rs.getDouble("dPeriodOverTime");
					//Double
					out.println("<TD ALIGN=CENTER>" + rs.getDouble("dPeriodDouble") + "</TD>");  
					dTotalDouble = dTotalDouble + rs.getDouble("dPeriodDouble");
					//Leave
					out.println("<TD ALIGN=CENTER>" + rs.getDouble("dPeriodLeave") + "</TD>");  
					dTotalLeave = dTotalLeave + rs.getDouble("dPeriodLeave");
				}else{
					out.print(rs.getString("sEmployeeID") + ",");
					out.print(rs.getString("sEmployeeFirstName") + ",");
					out.print(formatter.format(rs.getTimestamp("datPeriodEndDate")) + ",");
					out.print(Double.toString(rs.getDouble("dPeriodTotal")) + ",");
					out.print(Double.toString(rs.getDouble("dPeriodRegular")) + ",");
					out.print(Double.toString(rs.getDouble("dPeriodOverTime")) + ",");
					out.print(Double.toString(rs.getDouble("dPeriodDouble")) + ",");
					out.print(Double.toString(rs.getDouble("dPeriodLeave")) + ",\n");
				}

				//if the record has not been finalized
				if (rs.getInt("iFinalized") == 0){

					//edit link
					out.println("<TD ALIGN=CENTER><A HREF=\"" 
						+ ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) 
						+ "TimeCardSystem.PeriodTotalEdit" 
						+ "?RecID=" + Integer.toString(rs.getInt("id")) 
						+ "&Total=" + Double.toString(rs.getDouble("dPeriodTotal")) 
						+ "&Regular=" + Double.toString(rs.getDouble("dPeriodRegular"))
						+ "&Over=" + Double.toString(rs.getDouble("dPeriodOverTime")) 
						+ "&Double=" + Double.toString(rs.getDouble("dPeriodDouble")) 
						+ "&Leave=" + Double.toString(rs.getDouble("dPeriodLeave")) 
						+ "&OriginalURL=" + sCurrentURL 
						+ "\">Edit</A></TD>"
					);

					//display the finalize link
					out.println("<TD ALIGN=CENTER><A HREF=\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.PeriodTotalFinalize" 
						+ "?RecID=" + Integer.toString(rs.getInt("id")) 
						+ "&OriginalURL=" + sCurrentURL 
						+ "\">Finalize</A></TD>"
					);
				}

				if(!bExportAsCommaDelimited){
					out.println("</TR>");    			
				}

			}

			if(!bExportAsCommaDelimited){
				out.println("<TR><TD COLSPAN=2></TD>");
				out.println("<TD ALIGN=CENTER><FONT SIZE=4><B>TOTAL:</B></FONT></TD>");
				out.println("<TD ALIGN=CENTER><FONT SIZE=3><B>" + TimeCardUtilities.RoundHalfUp(dTotalOriTotal, 2) + "</B></FONT></TD>");
				out.println("<TD ALIGN=CENTER><FONT SIZE=3><B>" + TimeCardUtilities.RoundHalfUp(dTotalRegular, 2) + "</B></FONT></TD>");
				out.println("<TD ALIGN=CENTER><FONT SIZE=3><B>" + TimeCardUtilities.RoundHalfUp(dTotalOver, 2) + "</B></FONT></TD>");
				out.println("<TD ALIGN=CENTER><FONT SIZE=3><B>" + TimeCardUtilities.RoundHalfUp(dTotalDouble, 2) + "</B></FONT></TD>");
				out.println("<TD ALIGN=CENTER><FONT SIZE=3><B>" + TimeCardUtilities.RoundHalfUp(dTotalLeave, 2) + "</B></FONT></TD>");

				String sDepartmentParam = "";
				for (int i=0;i<alDepartments.size();i++){
					sDepartmentParam += "&RecDepartment" + alDepartments.get(i); 
				}

				if (iMultipleSelected == 0 && sPeriodFrom.compareTo("") != 0){
					out.println("<TD COLSPAN=2 ALIGN=CENTER><A HREF=\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.PeriodTotalFinalize" +
							"?RecID=0" +
							"&RecPeriod=" + sPeriodFrom + 
							"&RecEmployee=" + sEmployee + 
							sDepartmentParam + 
							"&OriginalURL=" + sCurrentURL + "\">Finalize ALL</A></TD></TR>");
				}else{
					if (sPeriodFrom.compareTo(sPeriodTo) == 0 && sPeriodFrom.compareTo("") != 0){
						out.println("<TD COLSPAN=2 ALIGN=CENTER><A HREF=\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.PeriodTotalFinalize" +
								"?RecID=0" +
								"&RecPeriod=" + sPeriodFrom + 
								"&RecEmployee=" + sEmployee + 
								sDepartmentParam + 
								"&OriginalURL=" + sCurrentURL + "\">Finalize ALL</A></TD></TR>");
					}else{
						out.println("<TD COLSPAN=2 ALIGN=CENTER>&nbsp;</TD></TR>");
					}
				}

				out.println("</TABLE>");
			}
			rs.close();

		} catch (Exception ex) {
			// handle any errors
			out.println("<BR><BR>Error [1465999347] - " + ex.toString() + "<BR>");
		}

		if(!bExportAsCommaDelimited){
			out.println("</BODY></HTML>");
		}
	}
}
