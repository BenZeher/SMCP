package companyweb;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;

public class ReportHits extends HttpServlet{

	private static final long serialVersionUID = 1L;
	private long lHitTotals = 0;
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		
		out.println("<HTML><BODY>");
		
		//Get the dates
		//If the dates are blank, use the maximum date range:
		String sStartingDate = request.getParameter("StartingDate");
		if (sStartingDate.equalsIgnoreCase("")){
			sStartingDate = getDefaultStartingDate(request, out);
		}else if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", sStartingDate)){
	    		out.println("Invalid start date - '" + sStartingDate);
	    		return;
	    }
		String sEndingDate = request.getParameter("EndingDate");
		if (sEndingDate.equalsIgnoreCase("")){
			sEndingDate = getDefaultEndingDate(request, out);
		}else if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", sEndingDate)){
	    		out.println("Invalid end date - '" + sEndingDate);
	    		return;
	    }

		//First get the totals:
		Date datStartingDate = new Date(0L);
		Date datEndingDate = new Date(System.currentTimeMillis());
		try{
	    	datStartingDate = clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyyy", sStartingDate);
	    	datEndingDate = clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyyy", sEndingDate);
		}catch(ParseException ex){
			System.out.println("Error when parsing start/end date:");
			System.out.println(ex.getMessage());
		}
		
		//Get the list of distinct site names here:
		String sSiteList = "";
		String SQL = "SELECT DISTINCT ssitename FROM sitehits ORDER BY ssitename";
		//First get the list of unique sites to build the header string:
    	try{
			//ResultSet rs = ServletUtilities.openResultSet(SQL, getServletContext(), request.getParameter("conffile"));
    		ResultSet rs = clsDatabaseFunctions.openResultSet(
    				SQL, 
    				getServletContext(), 
    				request.getParameter("conffile"), 
    				"MySQL", 
    				this.toString() + ".doPost-getting unique site names"
    				);
			while (rs.next()){
				String sSiteName = rs.getString("ssitename");
				sSiteList += ", " + sSiteName;
			}
			rs.close();
    	}catch(SQLException ex){
    		out.println("Error getting unique sitenames with SQL: '" + SQL + "' - " + ex.getMessage());
    		System.out.println("Error in " + this.toString() + " class!!");
    	    System.out.println("SQLException: " + ex.getMessage());
    	    System.out.println("SQLState: " + ex.getSQLState());
    	    System.out.println("SQL: " + ex.getErrorCode());
    	}
    	
    	if (sSiteList.length() > 2){
    		sSiteList = sSiteList.substring(2, sSiteList.length()); 
    	}
		out.println("<H3>Site hits for " 
				//+ 
				+ sSiteList
				+ " starting on "
				+ sStartingDate 
				+ " and ending on " + sEndingDate
				+ ":<BR></H3>");

		//Now use the distinct list to process each site's numbers:
    	try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(), request.getParameter("conffile")); 
			while (rs.next()){
				String sSiteName = rs.getString("ssitename");
				processSiteCounts(sSiteName, datStartingDate, datEndingDate, out, request.getParameter("conffile"));
			}
			rs.close();
    	}catch(SQLException ex){
    		out.println("Error getting unique sitenames to process each site with SQL: '" + SQL + "' - " + ex.getMessage());
    		System.out.println("Error in " + this.toString() + " class!!");
    	    System.out.println("SQLException: " + ex.getMessage());
    	    System.out.println("SQLState: " + ex.getSQLState());
    	    System.out.println("SQL: " + ex.getErrorCode());
    	}
    	
    	out.println(" - <B>TOTAL:</B> " + lHitTotals);
		//Read the hit counter table:

    	if (request.getParameter("ShowHits") != null){
	    	SQL = "SELECT * FROM sitehits"
				+ " WHERE "
				+ "(dathitdate >= '" + datStartingDate.toString() + " 00:00:00')"
				+ " AND (dathitdate <= '" + datEndingDate.toString() + " 23:59:59')"
				+ " ORDER BY dathitdate DESC";

			//System.out.println("SQL:");
			//System.out.println(SQL);
			out.println("<TABLE Border=1>");
			
			if (request.getParameter("ShowUserAgent") == null){
				out.println("<TR><TD><B>Row</B></TD><TD><B>Site</B></TD><TD><B>Time</B></TD><TD><B>Browser</B></TD><TD><B>User OS</B></TD></TR>");
			}else{
				out.println("<TR><TD><B>Row #</B></TD><TD><B>Site</B></TD><TD><B>Time</B></TD><TD><B>Browser</B></TD><TD><B>User OS</B></TD><TD><B>User-Agent</B></TD></TR>");
			}
			
	    	try{
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(), request.getParameter("conffile"));
				while (rs.next()){
					out.println("<TR>");
					out.println("<TD>" + lHitTotals + "</TD>");
					//out.println("<TD>" + rs.getLong("id") + "</TD>");
					out.println("<TD>" + rs.getString("ssitename") + "</TD>");
					out.println("<TD>" + rs.getTimestamp("dathitdate") + "</TD>");
					out.println("<TD>" + rs.getString("sbrowsertype") + "</TD>");
					out.println("<TD>" + rs.getString("suseros") + "</TD>");
					if (request.getParameter("ShowUserAgent") != null){
						out.println("<TD>" + rs.getString("suserheader") + "</TD>");
					}
					out.println("</TR>");
					lHitTotals--;
				}
				rs.close();
	    	}catch(SQLException ex){
	    		out.println("Error getting actual hits with SQL: '" + SQL + "' - " + ex.getMessage());
	    		System.out.println("Error in " + this.toString() + " class!!");
	    	    System.out.println("SQLException: " + ex.getMessage());
	    	    System.out.println("SQLState: " + ex.getSQLState());
	    	    System.out.println("SQL: " + ex.getErrorCode());
	    	}
			
	    	out.println("</TABLE>");
    	}
    	out.println("</BODY></HTML>");
	
	return;
	
	}
	private String getAveragePerDay(
			String sqlEndDate, 
			String sqlStartDate, 
			long lHits,
			String sConfFile,
			PrintWriter pwOut){
    	String SQL = 
    		"SELECT ("
    			+ Long.toString(lHits) + "/DATEDIFF("
    				+ sqlEndDate + ", " + sqlStartDate
    			+ ")"
    		+ ") AS avgdailyhits";
		
    	String sAvgHits = "0.0";
    	try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(), sConfFile); 
			rs.next();
			sAvgHits = Double.toString(rs.getDouble("avgdailyhits"));
			rs.close();
			return sAvgHits;
    	}catch(SQLException ex){
    		pwOut.println("Error getting abg daily hits with SQL: '" + SQL + "' - " + ex.getMessage());
    		System.out.println("Error in " + this.toString() + " class!!");
    	    System.out.println("SQLException: " + ex.getMessage());
    	    System.out.println("SQLState: " + ex.getSQLState());
    	    System.out.println("SQL: " + ex.getErrorCode());
    	    return "N/A";
    	}

	}
	private String getDefaultStartingDate(HttpServletRequest request, PrintWriter pwOut){
    	
		String SQL = "SELECT dathitdate FROM sitehits"
				+ " ORDER BY dathitdate ASC LIMIT 1";
		
    	String sStart = "0000-00-00";
    	try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(), request.getParameter("conffile")); 
			rs.next();
			sStart = clsDateAndTimeConversions.TimeStampToString(rs.getTimestamp("dathitdate"), "yyyy-MM-dd");
			rs.close();
			return sStart;
    	}catch(SQLException ex){
    		pwOut.println("Error getting default starting date with SQL: '" + SQL + "' - " + ex.getMessage());
    		System.out.println("Error in " + this.toString() + " class!!");
    	    System.out.println("SQLException: " + ex.getMessage());
    	    System.out.println("SQLState: " + ex.getSQLState());
    	    System.out.println("SQL: " + ex.getErrorCode());
    	    return "N/A";
    	}

	}
	private String getDefaultEndingDate(HttpServletRequest request, PrintWriter pwOut){
    	
		String SQL = "SELECT dathitdate FROM sitehits"
				+ " ORDER BY dathitdate DESC LIMIT 1";
		
    	String sEnd = "0000-00-00";
    	try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(), request.getParameter("conffile")); 
			rs.next();
			sEnd = clsDateAndTimeConversions.TimeStampToString(rs.getTimestamp("dathitdate"), "yyyy-MM-dd");
			rs.close();
			return sEnd;
    	}catch(SQLException ex){
    		pwOut.println("Error getting default ending date with SQL: '" + SQL + "' - " + ex.getMessage());
    		System.out.println("Error in " + this.toString() + " class!!");
    	    System.out.println("SQLException: " + ex.getMessage());
    	    System.out.println("SQLState: " + ex.getSQLState());
    	    System.out.println("SQL: " + ex.getErrorCode());
    	    return "N/A";
    	}

	}
	private void processSiteCounts(
		String sSiteName,
		Date datStartingDate,
		Date datEndingDate,
		PrintWriter pwOut, 
		String sConfFile){
		
		long lHitsPerSite = 0;
		
    	String SQL = "SELECT count(*) FROM sitehits"
			+ " WHERE ("
			+ "(ssitename = '" + sSiteName + "')"
			+ " AND (dathitdate >= '" + datStartingDate.toString() + " 00:00:00')"
			+ " AND (dathitdate <= '" + datEndingDate.toString() + " 23:59:59')"
			+ ")";
		
    	try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(), sConfFile); 
			rs.next();
			lHitsPerSite = rs.getLong(1);
			lHitTotals += lHitsPerSite;
			pwOut.println("<BR><B>" + sSiteName + ":</B> " + lHitsPerSite);
			rs.close();
    	}catch(SQLException ex){
    		pwOut.println("Error processing site counts with SQL: '" + SQL + "' - " + ex.getMessage());
    		System.out.println("Error in " + this.toString() + " class!!");
    	    System.out.println("SQLException: " + ex.getMessage());
    	    System.out.println("SQLState: " + ex.getSQLState());
    	    System.out.println("SQL: " + ex.getErrorCode());
    	}

    	pwOut.println("(" + getAveragePerDay(
    			"'" + datEndingDate.toString() + " 23:59:59'", 
    			"'" + datStartingDate.toString() + " 00:00:00'", 
    			lHitsPerSite,
    			sConfFile,
    			pwOut
    			) + "/day)");
		
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}

}
