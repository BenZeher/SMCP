package smcontrolpanel;

import SMDataDefinition.SMTablecolortable;
import SMDataDefinition.SMTablemechanics;
import SMDataDefinition.SMTablelocations;
import SMDataDefinition.SMTableservicetypes;
import ServletUtilities.clsDatabaseFunctions;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class SMEditMechanicsEdit extends HttpServlet{
	private static final long serialVersionUID = 1L;
	
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	     
		String sMechLocation = "";
		Integer iMechType = 0;
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
	    String sUserID =(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFirstName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME);
	    String sUserLastName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sMechanicID = "";
		
	    String title = "Manage Mechanics";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

	    //Print a link to the first page after login:
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println(SMUtilities.getColorPickerIncludeString(getServletContext()));
	    //LTO 20120814 color picker for mechanic.
		out.println("<script type='text/javascript' src='/sm/scripts/jquery-1.8.0.min.js'></script>");
		out.println("<script type='text/javascript' src='/sm/scripts/jquery.simple-color.js'></script>");

	    boolean bNewMechanic = false;
    	//get connection
    	Connection conn = clsDatabaseFunctions.getConnection(
    		getServletContext(),
    		sDBID,
    		"MySQL",
    		"smcontrolpanel.SMEditMechanicsEdit - User: " 
    		+ sUserID
    		+ " - "
    		+ sUserFirstName
    		+ " "
    		+ sUserLastName
    	);
	    
	    try {    	
	    	String sLink = "";
	    	boolean bRestricted = (request.getParameter("RESTRICTED").compareTo("YES") == 0)?true:false;
	    	if (bRestricted){
	    		sLink = SMUtilities.getURLLinkBase(getServletContext()) + "" + this.getClass().getName() + "?"
													+ "RESTRICTED=NO"
													+ "&MECHANIC=" + request.getParameter("MECHANIC")
													+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID;
	    	}else{
	    		sLink = SMUtilities.getURLLinkBase(getServletContext()) + "" + this.getClass().getName() + "?"
													+ "RESTRICTED=YES"
													+ "&MECHANIC=" + request.getParameter("MECHANIC")
													+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID;
	    	}
	    	String sMechColorDescription = "Click on desired color to assign it to mechanic icon in schedule map. <BR>" +
				"<BR>" +
				"<FONT SIZE=2>Colors that have already been assigned to other mechanics are represented with a gray line struck through it. UNassigned colors are STILL available for use.<BR>" +
				"Click <A HREF = \"" + sLink + "\">here</A> to show/hide color usage outside of current mechanic's location and service type.</FONT>";
	        //get mechanic info
	        String sSQL = "SELECT"
	        	+ " " + SMTablemechanics.TableName + "." + SMTablemechanics.lid
	        	+ ", " + SMTablemechanics.TableName + "." + SMTablemechanics.sMechInitial
	        	+ ", " + SMTablemechanics.TableName + "." + SMTablemechanics.sMechFullName  
	        	+ ", " + SMTablemechanics.TableName + "." + SMTablemechanics.iMechType
	        	+ ", " + SMTablemechanics.TableName + "." + SMTablemechanics.sMechLocation
	        	+ ", " + SMTablemechanics.TableName + "." + SMTablemechanics.sAssistant
	        	+ ", " + SMTablemechanics.TableName + "." + SMTablemechanics.sVehicleLabel
	        	+ ", " + SMTablemechanics.TableName + "." + SMTablemechanics.sstartingtime
	        	+ ", " + SMTablemechanics.TableName + "." + SMTablemechanics.sMechColorCodeRow
	        	+ ", " + SMTablemechanics.TableName + "." + SMTablemechanics.sMechColorCodeCol
	        	+ ", " + SMTablemechanics.TableName + "." + SMTablemechanics.semployeeid
	        	+ ", " + SMTablelocations.TableName + "." + SMTablelocations.sLocationDescription
	        	+ " FROM" 
	        	+ " " + SMTablemechanics.TableName + " LEFT JOIN " + SMTablelocations.TableName + " ON" 
	        	+ " " + SMTablemechanics.TableName + "." + SMTablemechanics.sMechLocation 
	        	+ "=" + SMTablelocations.TableName + "." + SMTablelocations.sLocation 
	        	+ " WHERE (" 
	        		+ "(" + SMTablemechanics.TableName + "." + SMTablemechanics.lid + " = '" + request.getParameter("MECHANIC") + "')"
	        	+ ")"
	        	;
	        ResultSet rsMechInfo = clsDatabaseFunctions.openResultSet(sSQL, conn);
	        if (rsMechInfo.next()){
	        	//save the original ID
		    	bNewMechanic = false;
	        	sMechanicID = Long.toString(rsMechInfo.getLong(SMTablemechanics.TableName + "." + SMTablemechanics.lid));
	        	sMechLocation = rsMechInfo.getString(SMTablemechanics.sMechLocation);
	        	iMechType = rsMechInfo.getInt(SMTablemechanics.iMechType);
		        out.println ("<H3>Mechanic Information: " + rsMechInfo.getString(SMTablemechanics.sMechFullName) + "</H3>");
	
	        	out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditMechanicsSave\">");
	        	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	        	out.println("<INPUT TYPE=HIDDEN NAME=\"MECHANICID\" VALUE=\"" + sMechanicID + "\">");
	        	out.println ("<TABLE BORDER=12 CELLSPACING=2>" );
	        	
	        	//Mechanic ID:
	        	out.println ("<TR><TD ALIGN=RIGHT><B>Mechanic ID</B></TD>");
	        	out.println("<TD><B>" + sMechanicID + "</B></TD>");
	        	out.println ("<TD>(ID number - assigned automatically by the system.) </TD></TR>");
	        	
	        	//Mechanic's name:
	        	out.println ("<TR><TD ALIGN=RIGHT><B>Mechanic Name</B></TD>");
	        	out.println("<TD><INPUT TYPE=TEXT SIZE=50 MAXLENGTH=" + SMTablemechanics.sMechFullNameLength + " NAME=\"MECHNAME\" VALUE=\"" + rsMechInfo.getString(SMTablemechanics.sMechFullName)+ "\"></TD>");
	        	out.println ("<TD>(Mechanic's full name, maximum 50 alphanumerics, including space. e.g. John Doe) </TD></TR>");
	        	
	        	//Initials:
	        	out.println ("<TR><TD ALIGN=RIGHT><B>Mechanic Initials</B></TD>");
	        	out.println("<TD><INPUT TYPE=TEXT SIZE=3 MAXLENGTH=" + SMTablemechanics.sMechInitialLength + " NAME=\"MECHINIT\" VALUE=\"" + rsMechInfo.getString(SMTablemechanics.sMechInitial)+ "\"></TD>");
	        	out.println("<TD>Mechanic's Initials. Maximum 3 letters - <B><I>must be unique</I></B>. (e.g. ABC)</TD></TR>");

	        	//Employee ID:
	        	out.println ("<TR><TD ALIGN=RIGHT><B>Employee ID</B></TD>");
	        	out.println("<TD><INPUT TYPE=TEXT SIZE=18 MAXLENGTH=" + SMTablemechanics.semployeeidLength 
	        		+ " NAME=\"EMPLOYEEID\" VALUE=\"" + rsMechInfo.getString(SMTablemechanics.semployeeid)+ "\"></TD>");
	        	out.println("<TD>Internal employee ID from Time Card System (if used)</TD></TR>");
	        	
	        	out.println ("<TR><TD ALIGN=RIGHT><B>Mechanic Location</B></TD>");
	        	out.println("<TD>");
        		out.println ("<INPUT TYPE=\"RADIO\" NAME=\"SELECTEDLOC\" VALUE=0>N/A<BR>");
        		String s;
    	        //get location info
    	        sSQL = SMMySQLs.Get_Locations_SQL();
    	        try{
	    	        ResultSet rsLocations = clsDatabaseFunctions.openResultSet(sSQL, conn);
	        		while (rsLocations.next()){
	        			s = "<INPUT TYPE=\"RADIO\" NAME=\"SELECTEDLOC\" VALUE=\"" + rsLocations.getString(SMTablelocations.sLocation); 
		        		if (rsLocations.getString(SMTablelocations.sLocation).equals(rsMechInfo.getString(SMTablemechanics.sMechLocation))){
		        			s = s + "\" CHECKED>";
		        		}else{
		        			s = s + "\">";
		        		}
	        			s = s + rsLocations.getString(SMTablelocations.sLocationDescription)+"<BR>";
	        			out.println(s);
	        		}
	        		rsLocations.close();
    	        } catch (SQLException e){
    	        	out.println("<BR>Error [1412709841] reading locations to edit mechanic with SQL: " 
    	        		+ sSQL + " - " + e.getMessage() + ".<BR>");
    	        }
        		out.println ("</TD>");
        		out.println ("<TD>Select the mechanic's location</TD>");
	        	out.println ("</TR>"); 	
	        	out.println ("<TR><TD ALIGN=RIGHT><B>Mechanic Type</B></TD>");
	        	out.println("<TD>");
	        	
		        //get service type info
		        sSQL = SMMySQLs.Get_Servicetypes_SQL();
		        try{
			        ResultSet rsServiceTypes = clsDatabaseFunctions.openResultSet(sSQL, conn);
	        		while (rsServiceTypes.next()){
	        			s = "<INPUT TYPE=\"CHECKBOX\" NAME=\"SELECTEDTYPE\" VALUE=\"" + rsServiceTypes.getString(SMTableservicetypes.iTypeID) + " - " + rsServiceTypes.getString(SMTableservicetypes.sCode) + "\"";  
	        			if (SMUtilities.IsServiceType(rsMechInfo.getInt(SMTablemechanics.iMechType), rsServiceTypes.getInt(SMTableservicetypes.iTypeID))){
		        			s = s + " CHECKED>";
		        		}else{
		        			s = s + ">";
		        		} 
	        			s = s + rsServiceTypes.getString(SMTableservicetypes.sName) + "<BR>";
	        			//System.out.println("s = " + s);
	        			out.println(s);
	        		}
	        		rsServiceTypes.close();
		        } catch (SQLException e){
		        	out.println("<BR>Error [1412709842] reading service types to edit mechanic with SQL: " 
		        		+ sSQL + " - " + e.getMessage() + ".<BR>");
		        }
        		out.println ("</TD>");
        		out.println ("<TD>Select one or more service types</TD>");
	        	out.println ("</TR>");

	      		//Assistant Name
	      		out.println ("<TR><TD ALIGN=RIGHT><B>Assistant Name</B></TD>");
	      		out.println("<TD><INPUT TYPE=TEXT SIZE=50 MAXLENGTH=" + SMTablemechanics.sAssistantLength + " NAME=\"MECHASSISTANT\" VALUE=\"" + rsMechInfo.getString(SMTablemechanics.sAssistant) + "\"></TD>");
	      		out.println("<TD>Assistant's name, maximum 60 alphanumerics, including space. e.g. John Doe) </TD></TR>");
	
	      		//Vehicle Label
	      		out.println ("<TR><TD ALIGN=RIGHT><B>Vehicle Label</B></TD>");
	      		out.println("<TD><INPUT TYPE=TEXT SIZE=50 MAXLENGTH=" + SMTablemechanics.sVehicleLabelLength 
	      				+ " NAME=\"MECHVEHICLELABEL\" VALUE=\"" + rsMechInfo.getString(SMTablemechanics.sVehicleLabel) + "\"></TD>");
	      		out.println("<TD>Vehicle/Equipment label, maximum 10 alphanumerics, including space. (e.g. Commercial F-150) </TD></TR>");
	      		
	      		//Starting time
	      		out.println ("<TR><TD ALIGN=RIGHT><B>Starting time</B></TD>");
	      		out.println("<TD><INPUT TYPE=TEXT SIZE=50 MAXLENGTH=" + SMTablemechanics.sstartingtimeLength 
	      				+ " NAME=\"STARTINGTIME\" VALUE=\"" 
	      				+ rsMechInfo.getString(SMTablemechanics.sstartingtime) + "\"></TD>");
	      		out.println("<TD>Starting time, maximum 10 alphanumerics, including space. e.g. '6:00 AM'</TD></TR>");
	
	      		out.println("<TR><TD ALIGN=RIGHT><B>Mechanic's icon color code:</B></TD>");
	      		out.println("<TD>"); 
	      		//print_mech_color_selection_table("000000", out);
	      		print_mech_color_selection_map(rsMechInfo.getInt(SMTablemechanics.sMechColorCodeRow),
	      									   rsMechInfo.getInt(SMTablemechanics.sMechColorCodeCol), 
	      									   bRestricted,
	      									   out,
	      									   conn,
	      									   sMechLocation,
	      									   iMechType);
	      		out.println("</TD><TD VALIGN=CENTRE>" + sMechColorDescription + "</TD>"
	    				+ "</TR>");
	        	
	        }else{
	        	bNewMechanic = true;
	        	sMechanicID = SMEditMechanics.NEW_MECHANIC_ID;
		        out.println ("<H3>Mechanic Information: New Mechanic</H3>");
	        	out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditMechanicsSave\">");
	        	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	        	out.println("<INPUT TYPE=HIDDEN NAME=\"MECHANICID\" VALUE=\"" + sMechanicID + "\">");
	        	out.println ("<TABLE BORDER=12 CELLSPACING=2>" );
	        	
	        	//Mechanic ID:
	        	out.println ("<TR><TD ALIGN=RIGHT><B>Mechanic ID</B></TD>");
	        	out.println("<TD><B>" + "(NEW)" + "</B></TD>");
	        	out.println ("<TD>(ID number - assigned automatically by the system.) </TD></TR>");
	        	
	        	//Mechanic name:
	        	out.println ("<TR><TD ALIGN=RIGHT><B>Mechanic Name</B></TD>");
	        	out.println("<TD><INPUT TYPE=TEXT SIZE=50 MAXLENGTH=" + SMTablemechanics.sMechFullNameLength + " NAME=\"MECHNAME\" VALUE=\"\"></TD>");
	        	out.println("<TD>Mechanic's full name, maximum 50 alphanumerics, including space. e.g. John Doe)</TD></TR>");
	        	
	        	//Initials:
	        	out.println ("<TR><TD ALIGN=RIGHT><B>Mechanic Initial</B></TD>");
	        	out.println("<TD><INPUT TYPE=TEXT SIZE=3 MAXLENGTH=" + SMTablemechanics.sMechInitialLength + " NAME=\"MECHINIT\" VALUE=\"\"></TD>");
	        	out.println("<TD>Mechanic's Initials. Maximum 3 letters. e.g. ABC)</TD></TR>");
	        	
	        	//Employee ID:
	        	out.println ("<TR><TD ALIGN=RIGHT><B>Employee ID</B></TD>");
	        	out.println("<TD><INPUT TYPE=TEXT SIZE=18 MAXLENGTH=" + SMTablemechanics.semployeeidLength 
	        		+ " NAME=\"EMPLOYEEID\" VALUE=\"" + ""+ "\"></TD>");
	        	out.println("<TD>Internal employee ID from Time Card System (if used)</TD></TR>");
	        	
	        	//list all possible locations
	        	out.println ("<TR><TD ALIGN=RIGHT><B>Mechanic Location</B></TD>");
	        	out.println("<TD>");
	      		out.println ("<INPUT TYPE=\"RADIO\" NAME=\"SELECTEDLOC\" VALUE=0>N/A<BR>");
	      		
    	        sSQL = SMMySQLs.Get_Locations_SQL();
    	        try{
	    	        ResultSet rsLocations = clsDatabaseFunctions.openResultSet(sSQL, conn);
		      		while (rsLocations.next()){
		      			out.println("<INPUT TYPE=\"RADIO\" NAME=\"SELECTEDLOC\" VALUE=\"" + rsLocations.getString(SMTablelocations.sLocation) + "\">" + rsLocations.getString(SMTablelocations.sLocationDescription)+"<BR>");
		      		}
		      		rsLocations.close();
    	        } catch (SQLException e){
    	        	out.println("<BR>Error [1412709851] reading locations to edit mechanic with SQL: " 
    	        		+ sSQL + " - " + e.getMessage() + ".<BR>");
    	        }
	      		out.println ("</TD>");
	      		out.println ("<TD>Locations</TD></TR>");
	      		
	      		//list all possible service types
	      		out.println ("<TR><TD ALIGN=RIGHT><B>Mechanic Type</B></TD>");
	      		out.println("<TD>");
	      		
		        sSQL = SMMySQLs.Get_Servicetypes_SQL();
		        try{
			        ResultSet rsServiceTypes = clsDatabaseFunctions.openResultSet(sSQL, conn);
		      		while (rsServiceTypes.next()){
		      			out.println("<INPUT TYPE=\"CHECKBOX\" NAME=\"SELECTEDTYPE\" VALUE=\"" + rsServiceTypes.getString(SMTableservicetypes.iTypeID) + " - " + rsServiceTypes.getString(SMTableservicetypes.sCode) + "\"" + ">" + rsServiceTypes.getString(SMTableservicetypes.sName) + "<BR>");
		      		}
	      		rsServiceTypes.close();
		        } catch (SQLException e){
    	        	out.println("<BR>Error [1412709852] reading locations to edit mechanic with SQL: " 
        	        	+ sSQL + " - " + e.getMessage() + ".<BR>");
		        }
	      		out.println ("</TD>");
	      		out.println ("<TD>Service types</TD></TR>");	 
	      		
	      		//Assistant Name
	      		out.println ("<TR><TD ALIGN=RIGHT><B>Assistant Name</B></TD>");
	      		out.println("<TD><INPUT TYPE=TEXT SIZE=50 MAXLENGTH=" + SMTablemechanics.sAssistantLength + " NAME=\"MECHASSISTANT\" VALUE=\"\"></TD>");
	      		out.println("<TD>Assistant's name, maximum 60 alphanumerics, including space. e.g. John Doe) </TD></TR>");
	
	      		//Vehicle Label
	      		out.println ("<TR><TD ALIGN=RIGHT><B>Vehicle Label</B></TD>");
	      		out.println("<TD><INPUT TYPE=TEXT SIZE=50 MAXLENGTH=" + SMTablemechanics.sVehicleLabelLength + " NAME=\"MECHVEHICLELABEL\" VALUE=\"\"></TD>");
	      		out.println("<TD>Vehicle/Equipment label, maximum 10 alphanumerics, including space. (e.g. Commercial F-150) </TD></TR>");
	      		
	      		//Starting time
	      		out.println ("<TR><TD ALIGN=RIGHT><B>Starting time</B></TD>");
	      		out.println("<TD><INPUT TYPE=TEXT SIZE=50 MAXLENGTH=" + SMTablemechanics.sstartingtimeLength 
	      				+ " NAME=\"STARTINGTIME\" VALUE=\"\"></TD>");
	      		out.println("<TD>Starting time, maximum 10 alphanumerics, including space. e.g. '6:00 AM'</TD></TR>");

	      		out.println("<TR><TD ALIGN=RIGHT><B>Mechanic's icon color code:</B></TD>");
	      		out.println("<TD>"); 
	      		//print_mech_color_selection_table("000000", out);
	      		print_mech_color_selection_map(0,
											   0, 
	      									   bRestricted,
											   out,
											   conn,
											   sMechLocation,
											   iMechType);
	      		out.println("</TD><TD VALIGN=CENTRE>" + sMechColorDescription + "</TD>"
	    				+ "</TR>");
	        } 
	        rsMechInfo.close();
	    } catch (SQLException ex) {
	        // handle any errors
	    	out.println("<BR><BR>Error [1412709513] reading mechanic information - " + ex.getMessage() + ".<BR>");
	    }
        clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080499]");
      	out.println ("</Table>");
      	out.println ("<BR>");
      	out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Save----\">");
      	out.println ("</FORM>");
      	
      	if (bNewMechanic == false){
	        	//delete current record.
	        	out.println("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditMechanicsRemove\">");
	        	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	        	out.println("<INPUT TYPE=HIDDEN NAME=\"Mechanic\" VALUE=\"" + sMechanicID + "\">");
	        	out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Delete----\">");
	        	out.println("<INPUT TYPE=CHECKBOX NAME=DoubleCheck VALUE=1>Check here if you want to delete this mechanic.");
	        	out.println("</FORM>");
      	}
      	out.println("</BODY></HTML>");
	}

	private void print_mech_color_selection_map(int iRow, 
												int iCol, 
												boolean bRestricted, 
												PrintWriter out, 
												Connection conn,
												String sMechLocation,
												int iMechType) throws SQLException{
		
		//get mechanics's color
		String sMechColor = "000000";
		String sSQL = "SELECT * FROM" + 
						" " + SMTablecolortable.TableName + 
						" WHERE" +
						" " + SMTablecolortable.irow + " = " + iRow + 
						" AND" +
						" " + SMTablecolortable.icol + " = " + iCol;
		//System.out.println("[1345066542] SQL = " + sSQL);
		ResultSet rsColorCode = clsDatabaseFunctions.openResultSet(sSQL, conn);
		if (rsColorCode.next()){
			sMechColor = rsColorCode.getString(SMTablecolortable.scolorcode);
		}
		rsColorCode.close();
		out.println("<div id='colorselector' style='position:relative;'>");
		//here we circle all the used colors so user knows what NOT to pic.
		sSQL = "SELECT DISTINCT" +
					" " + SMTablemechanics.sMechColorCodeRow + "," +
					" " + SMTablemechanics.sMechColorCodeCol +
				" FROM " + 
				" " + SMTablemechanics.TableName;
		
		if (bRestricted){
			sSQL += " WHERE" +
					" " + SMTablemechanics.sMechLocation + " = '" + sMechLocation + "'" +
					" AND" +
					" " + SMTablemechanics.iMechType + " & " + iMechType + " <> 0";
		}
		
		//System.out.println("[1345229718] SQL = " + sSQL);
		
		ResultSet rsUsedColors = clsDatabaseFunctions.openResultSet(sSQL, conn);
		int iSuffix = 0;
		while (rsUsedColors.next()){
			//System.out.println("[1345210046] Row = " + rsUsedColors.getInt(SMTablemechanics.sMechColorCodeRow));
			//System.out.println("[1345210046] Col = " + rsUsedColors.getInt(SMTablemechanics.sMechColorCodeCol));
			//System.out.println("[1345210046] Coor = (" + (rsUsedColors.getInt(SMTablemechanics.sMechColorCodeRow) * 15 + 20) + 
			//											  ", " +
			//											 (rsUsedColors.getInt(SMTablemechanics.sMechColorCodeCol) * 18 + Math.abs(rsUsedColors.getInt(SMTablemechanics.sMechColorCodeRow) - 6) * 9) +
			//										")"
			//				  );
			out.println("<div id='mc" + iSuffix + "' " +
						 	"style='visibility:visible; " +
					 			"position:absolute; " +
					 			"top:" + (rsUsedColors.getInt(SMTablemechanics.sMechColorCodeRow) * 15 + 30) + "px; " +
					 			"left:" + (rsUsedColors.getInt(SMTablemechanics.sMechColorCodeCol) * 18 + Math.abs(rsUsedColors.getInt(SMTablemechanics.sMechColorCodeRow) - 6) * 9 + 4) + "px; " +
					 			"width:12px; " +
					 			"height:2px; " +
					 			"background-image:url(\"images/usedcolor.png\")'" +
					 			">" +
					 	"</div>");
			iSuffix++;
		}
		rsUsedColors.close();
		
		out.println("<div id='selectedColor' " +
						 "style='visibility:hidden; " +
						 		"position:relative; " +
						 		"width:20px; " +
						 		"height:20px; " +
						 		"background-image:url(\"images/selectedcolor.gif\")'" +
						 		">" +
					"</div>" +
				 	
					"<img style='margin-right:2px;' src=\"images/colormap.png\" usemap='#colormap' alt='colormap' />" +
					"<map id='colormap' name='colormap' onmouseout='mouseOutMap()'>" +
					print_area_list());

		out.println("</map>");
		out.println("<script type='text/javascript'>"); //intialize color selector with mech info.
		
		out.println("	document.getElementById('selectedColor').style.top='" + (iRow * 15 + 20) + "px';");
		out.println("	document.getElementById('selectedColor').style.left='" + (iCol * 18 + Math.abs(iRow - 6) * 9) + "px';");
		out.println("	document.getElementById('selectedColor').style.visibility='visible';");
		/*
		//circle used colors
		out.println("	document.getElementById('mc1').style.top='45px';");
		out.println("	document.getElementById('mc1').style.left='127px';");
		out.println("	document.getElementById('mc1').style.visibility='visible';");

		out.println("	document.getElementById('mc2').style.top='65px';");
		out.println("	document.getElementById('mc2').style.left='147px';");
		out.println("	document.getElementById('mc2').style.visibility='visible';");

		out.println("	document.getElementById('mc3').style.top='85px';");
		out.println("	document.getElementById('mc3').style.left='167px';");
		out.println("	document.getElementById('mc3').style.visibility='visible';");
		*/
		out.println("</script>");
		out.println("<div style='width:300px;padding-top:33px;padding-left:66px;margin-bottom:30px;'>" +
						"<div id='divpreview' style='float: left; height: 20px; width: 100px; border-width: 1px 1px medium; border-style: solid solid none; border-color: rgb(212, 212, 212) rgb(212, 212, 212) -moz-use-text-color; -moz-border-top-colors: none; -moz-border-right-colors: none; -moz-border-bottom-colors: none; -moz-border-left-colors: none; -moz-border-image: none; background-color: #" + sMechColor + ";'>&nbsp;</div>" +
						"<div id='divpreviewtxt' style='float:left; height: 20px; width:50px;padding-left:5px;padding-top:7px;'>" + sMechColor +"</div>" +
						"<input type=hidden style='height: 20px; width:70px;' name='colorhex' id='colorhex' value='#" + sMechColor +"' />" +
					"</div>");
		//out.println("<BR><BR>Selected color: <input style='width:70px;' name='colorhex' id='colorhex' value='#FF0000' />");
		
		out.println("</div>"); //end of color selector div
		
		out.println("<script type='text/javascript'>");
		out.println("<!--");
		out.println("var colorhex=\"#" + sMechColor +"\"");
		
		out.println("function mouseOverColor(hex){");
		out.println("	document.getElementById('divpreview').style.backgroundColor=hex;");
		out.println("	document.getElementById('divpreviewtxt').innerHTML=hex;");
		out.println("	document.body.style.cursor='pointer';");
		out.println("}");

		out.println("function mouseOutMap(){");
		out.println("	document.getElementById('divpreview').style.backgroundColor=colorhex;");
		out.println("	document.getElementById('divpreviewtxt').innerHTML=colorhex;");
		out.println("	document.body.style.cursor='';");
		out.println("}");

		out.println("function clickColor(hex,seltop,selleft){");
		out.println("	var xhttp,c");
		out.println("	if (hex==0){");
		out.println("		c=document.getElementById('colorhex').value;");
		out.println("	}else{");
		out.println("		c=hex;");
		out.println("	}");
		out.println("	if (c.substr(0,1)=='#'){");
		out.println("		c=c.substr(1);");
		out.println("	}");
		out.println("	colorhex='#' + c;");
		
		out.println("	document.getElementById('colorhex').value=colorhex;");
		/*
		out.println("	if (window.XMLHttpRequest){");
		out.println("	  xhttp=new XMLHttpRequest();");
		out.println("	 }else{");
		out.println("	  xhttp=new ActiveXObject('Microsoft.XMLHTTP');");
		out.println("	 }");
		out.println("	xhttp.open('GET','http_colorshades.asp?colorhex=' + c + '&r=' + Math.random(),false);");
		out.println("	xhttp.send('');");
		out.println("	document.getElementById('colorshades').innerHTML=xhttp.responseText;");
		*/
		out.println("	if (seltop>-1 && selleft>-1){");
		out.println("		document.getElementById('selectedColor').style.top=seltop + 'px';");
		out.println("		document.getElementById('selectedColor').style.left=selleft + 'px';");
		out.println("		document.getElementById('selectedColor').style.visibility='visible';");
		/*
		out.println("		document.getElementById('mc1').style.top=seltop + 'px';");
		out.println("		document.getElementById('mc1').style.left=selleft + 'px';");
		out.println("		document.getElementById('mc1').style.visibility='visible';");
		*/
		out.println("	}else{");
		out.println("		document.getElementById('divpreview').style.backgroundColor=colorhex;");
		out.println("		document.getElementById('divpreviewtxt').innerHTML=colorhex;");
		out.println("		document.getElementById('selectedColor').style.visibility='hidden';");
		out.println("	}");
		out.println("}");
		//-->
		out.println("</script>");
		
		return;
	}
	
	private String print_area_list(){
		
		return 
		"<area style='cursor:pointer' shape='poly' coords='63,0,72,4,72,15,63,19,54,15,54,4' onclick='clickColor(\"#000000\",20,54)' onmouseover='mouseOverColor(\"#000000\")' alt='#000000' />" +
		"<area style='cursor:pointer' shape='poly' coords='81,0,90,4,90,15,81,19,72,15,72,4' onclick='clickColor(\"#336699\",20,72)' onmouseover='mouseOverColor(\"#336699\")' alt='#336699' />" +
		"<area style='cursor:pointer' shape='poly' coords='99,0,108,4,108,15,99,19,90,15,90,4' onclick='clickColor(\"#3366CC\",20,90)' onmouseover='mouseOverColor(\"#3366CC\")' alt='#3366CC' />" +
		"<area style='cursor:pointer' shape='poly' coords='117,0,126,4,126,15,117,19,108,15,108,4' onclick='clickColor(\"#003399\",20,108)' onmouseover='mouseOverColor(\"#003399\")' alt='#003399' />" +
		"<area style='cursor:pointer' shape='poly' coords='135,0,144,4,144,15,135,19,126,15,126,4' onclick='clickColor(\"#000099\",20,126)' onmouseover='mouseOverColor(\"#000099\")' alt='#000099' />" +
		"<area style='cursor:pointer' shape='poly' coords='153,0,162,4,162,15,153,19,144,15,144,4' onclick='clickColor(\"#0000CC\",20,144)' onmouseover='mouseOverColor(\"#0000CC\")' alt='#0000CC' />" +
		"<area style='cursor:pointer' shape='poly' coords='171,0,180,4,180,15,171,19,162,15,162,4' onclick='clickColor(\"#000066\",20,162)' onmouseover='mouseOverColor(\"#000066\")' alt='#000066' />" +
		"<area style='cursor:pointer' shape='poly' coords='54,15,63,19,63,30,54,34,45,30,45,19' onclick='clickColor(\"#006666\",35,45)' onmouseover='mouseOverColor(\"#006666\")' alt='#006666' />" +
		"<area style='cursor:pointer' shape='poly' coords='72,15,81,19,81,30,72,34,63,30,63,19' onclick='clickColor(\"#006699\",35,63)' onmouseover='mouseOverColor(\"#006699\")' alt='#006699' />" +
		"<area style='cursor:pointer' shape='poly' coords='90,15,99,19,99,30,90,34,81,30,81,19' onclick='clickColor(\"#0099CC\",35,81)' onmouseover='mouseOverColor(\"#0099CC\")' alt='#0099CC' />" +
		"<area style='cursor:pointer' shape='poly' coords='108,15,117,19,117,30,108,34,99,30,99,19' onclick='clickColor(\"#0066CC\",35,99)' onmouseover='mouseOverColor(\"#0066CC\")' alt='#0066CC' />" +
		"<area style='cursor:pointer' shape='poly' coords='126,15,135,19,135,30,126,34,117,30,117,19' onclick='clickColor(\"#0033CC\",35,117)' onmouseover='mouseOverColor(\"#0033CC\")' alt='#0033CC' />" +
		"<area style='cursor:pointer' shape='poly' coords='144,15,153,19,153,30,144,34,135,30,135,19' onclick='clickColor(\"#0000FF\",35,135)' onmouseover='mouseOverColor(\"#0000FF\")' alt='#0000FF' />" +
		"<area style='cursor:pointer' shape='poly' coords='162,15,171,19,171,30,162,34,153,30,153,19' onclick='clickColor(\"#3333FF\",35,153)' onmouseover='mouseOverColor(\"#3333FF\")' alt='#3333FF' />" +
		"<area style='cursor:pointer' shape='poly' coords='180,15,189,19,189,30,180,34,171,30,171,19' onclick='clickColor(\"#333399\",35,171)' onmouseover='mouseOverColor(\"#333399\")' alt='#333399' />" +
		"<area style='cursor:pointer' shape='poly' coords='45,30,54,34,54,45,45,49,36,45,36,34' onclick='clickColor(\"#669999\",50,36)' onmouseover='mouseOverColor(\"#669999\")' alt='#669999' />" +
		"<area style='cursor:pointer' shape='poly' coords='63,30,72,34,72,45,63,49,54,45,54,34' onclick='clickColor(\"#009999\",50,54)' onmouseover='mouseOverColor(\"#009999\")' alt='#009999' />" +
		"<area style='cursor:pointer' shape='poly' coords='81,30,90,34,90,45,81,49,72,45,72,34' onclick='clickColor(\"#33CCCC\",50,72)' onmouseover='mouseOverColor(\"#33CCCC\")' alt='#33CCCC' />" +
		"<area style='cursor:pointer' shape='poly' coords='99,30,108,34,108,45,99,49,90,45,90,34' onclick='clickColor(\"#00CCFF\",50,90)' onmouseover='mouseOverColor(\"#00CCFF\")' alt='#00CCFF' />" +
		"<area style='cursor:pointer' shape='poly' coords='117,30,126,34,126,45,117,49,108,45,108,34' onclick='clickColor(\"#0099FF\",50,108)' onmouseover='mouseOverColor(\"#0099FF\")' alt='#0099FF' />" +
		"<area style='cursor:pointer' shape='poly' coords='135,30,144,34,144,45,135,49,126,45,126,34' onclick='clickColor(\"#0066FF\",50,126)' onmouseover='mouseOverColor(\"#0066FF\")' alt='#0066FF' />" +
		"<area style='cursor:pointer' shape='poly' coords='153,30,162,34,162,45,153,49,144,45,144,34' onclick='clickColor(\"#3366FF\",50,144)' onmouseover='mouseOverColor(\"#3366FF\")' alt='#3366FF' />" +
		"<area style='cursor:pointer' shape='poly' coords='171,30,180,34,180,45,171,49,162,45,162,34' onclick='clickColor(\"#3333CC\",50,162)' onmouseover='mouseOverColor(\"#3333CC\")' alt='#3333CC' />" +
		"<area style='cursor:pointer' shape='poly' coords='189,30,198,34,198,45,189,49,180,45,180,34' onclick='clickColor(\"#666699\",50,180)' onmouseover='mouseOverColor(\"#666699\")' alt='#666699' />" +
		"<area style='cursor:pointer' shape='poly' coords='36,45,45,49,45,60,36,64,27,60,27,49' onclick='clickColor(\"#339966\",65,27)' onmouseover='mouseOverColor(\"#339966\")' alt='#339966' />" +
		"<area style='cursor:pointer' shape='poly' coords='54,45,63,49,63,60,54,64,45,60,45,49' onclick='clickColor(\"#00CC99\",65,45)' onmouseover='mouseOverColor(\"#00CC99\")' alt='#00CC99' />" +
		"<area style='cursor:pointer' shape='poly' coords='72,45,81,49,81,60,72,64,63,60,63,49' onclick='clickColor(\"#00FFCC\",65,63)' onmouseover='mouseOverColor(\"#00FFCC\")' alt='#00FFCC' />" +
		"<area style='cursor:pointer' shape='poly' coords='90,45,99,49,99,60,90,64,81,60,81,49' onclick='clickColor(\"#00FFFF\",65,81)' onmouseover='mouseOverColor(\"#00FFFF\")' alt='#00FFFF' />" +
		"<area style='cursor:pointer' shape='poly' coords='108,45,117,49,117,60,108,64,99,60,99,49' onclick='clickColor(\"#33CCFF\",65,99)' onmouseover='mouseOverColor(\"#33CCFF\")' alt='#33CCFF' />" +
		"<area style='cursor:pointer' shape='poly' coords='126,45,135,49,135,60,126,64,117,60,117,49' onclick='clickColor(\"#3399FF\",65,117)' onmouseover='mouseOverColor(\"#3399FF\")' alt='#3399FF' />" +
		"<area style='cursor:pointer' shape='poly' coords='144,45,153,49,153,60,144,64,135,60,135,49' onclick='clickColor(\"#6699FF\",65,135)' onmouseover='mouseOverColor(\"#6699FF\")' alt='#6699FF' />" +
		"<area style='cursor:pointer' shape='poly' coords='162,45,171,49,171,60,162,64,153,60,153,49' onclick='clickColor(\"#6666FF\",65,153)' onmouseover='mouseOverColor(\"#6666FF\")' alt='#6666FF' />" +
		"<area style='cursor:pointer' shape='poly' coords='180,45,189,49,189,60,180,64,171,60,171,49' onclick='clickColor(\"#6600FF\",65,171)' onmouseover='mouseOverColor(\"#6600FF\")' alt='#6600FF' />" +
		"<area style='cursor:pointer' shape='poly' coords='198,45,207,49,207,60,198,64,189,60,189,49' onclick='clickColor(\"#6600CC\",65,189)' onmouseover='mouseOverColor(\"#6600CC\")' alt='#6600CC' />" +
		"<area style='cursor:pointer' shape='poly' coords='27,60,36,64,36,75,27,79,18,75,18,64' onclick='clickColor(\"#339933\",80,18)' onmouseover='mouseOverColor(\"#339933\")' alt='#339933' />" +
		"<area style='cursor:pointer' shape='poly' coords='45,60,54,64,54,75,45,79,36,75,36,64' onclick='clickColor(\"#00CC66\",80,36)' onmouseover='mouseOverColor(\"#00CC66\")' alt='#00CC66' />" +
		"<area style='cursor:pointer' shape='poly' coords='63,60,72,64,72,75,63,79,54,75,54,64' onclick='clickColor(\"#00FF99\",80,54)' onmouseover='mouseOverColor(\"#00FF99\")' alt='#00FF99' />" +
		"<area style='cursor:pointer' shape='poly' coords='81,60,90,64,90,75,81,79,72,75,72,64' onclick='clickColor(\"#66FFCC\",80,72)' onmouseover='mouseOverColor(\"#66FFCC\")' alt='#66FFCC' />" +
		"<area style='cursor:pointer' shape='poly' coords='99,60,108,64,108,75,99,79,90,75,90,64' onclick='clickColor(\"#66FFFF\",80,90)' onmouseover='mouseOverColor(\"#66FFFF\")' alt='#66FFFF' />" +
		"<area style='cursor:pointer' shape='poly' coords='117,60,126,64,126,75,117,79,108,75,108,64' onclick='clickColor(\"#66CCFF\",80,108)' onmouseover='mouseOverColor(\"#66CCFF\")' alt='#66CCFF' />" +
		"<area style='cursor:pointer' shape='poly' coords='135,60,144,64,144,75,135,79,126,75,126,64' onclick='clickColor(\"#99CCFF\",80,126)' onmouseover='mouseOverColor(\"#99CCFF\")' alt='#99CCFF' />" +
		"<area style='cursor:pointer' shape='poly' coords='153,60,162,64,162,75,153,79,144,75,144,64' onclick='clickColor(\"#9999FF\",80,144)' onmouseover='mouseOverColor(\"#9999FF\")' alt='#9999FF' />" +
		"<area style='cursor:pointer' shape='poly' coords='171,60,180,64,180,75,171,79,162,75,162,64' onclick='clickColor(\"#9966FF\",80,162)' onmouseover='mouseOverColor(\"#9966FF\")' alt='#9966FF' />" +
		"<area style='cursor:pointer' shape='poly' coords='189,60,198,64,198,75,189,79,180,75,180,64' onclick='clickColor(\"#9933FF\",80,180)' onmouseover='mouseOverColor(\"#9933FF\")' alt='#9933FF' />" +
		"<area style='cursor:pointer' shape='poly' coords='207,60,216,64,216,75,207,79,198,75,198,64' onclick='clickColor(\"#9900FF\",80,198)' onmouseover='mouseOverColor(\"#9900FF\")' alt='#9900FF' />" +
		"<area style='cursor:pointer' shape='poly' coords='18,75,27,79,27,90,18,94,9,90,9,79' onclick='clickColor(\"#006600\",95,9)' onmouseover='mouseOverColor(\"#006600\")' alt='#006600' />" +
		"<area style='cursor:pointer' shape='poly' coords='36,75,45,79,45,90,36,94,27,90,27,79' onclick='clickColor(\"#00CC00\",95,27)' onmouseover='mouseOverColor(\"#00CC00\")' alt='#00CC00' />" +
		"<area style='cursor:pointer' shape='poly' coords='54,75,63,79,63,90,54,94,45,90,45,79' onclick='clickColor(\"#00FF00\",95,45)' onmouseover='mouseOverColor(\"#00FF00\")' alt='#00FF00' />" +
		"<area style='cursor:pointer' shape='poly' coords='72,75,81,79,81,90,72,94,63,90,63,79' onclick='clickColor(\"#66FF99\",95,63)' onmouseover='mouseOverColor(\"#66FF99\")' alt='#66FF99' />" +
		"<area style='cursor:pointer' shape='poly' coords='90,75,99,79,99,90,90,94,81,90,81,79' onclick='clickColor(\"#99FFCC\",95,81)' onmouseover='mouseOverColor(\"#99FFCC\")' alt='#99FFCC' />" +
		"<area style='cursor:pointer' shape='poly' coords='108,75,117,79,117,90,108,94,99,90,99,79' onclick='clickColor(\"#CCFFFF\",95,99)' onmouseover='mouseOverColor(\"#CCFFFF\")' alt='#CCFFFF' />" +
		"<area style='cursor:pointer' shape='poly' coords='126,75,135,79,135,90,126,94,117,90,117,79' onclick='clickColor(\"#CCCCFF\",95,117)' onmouseover='mouseOverColor(\"#CCCCFF\")' alt='#CCCCFF' />" +
		"<area style='cursor:pointer' shape='poly' coords='144,75,153,79,153,90,144,94,135,90,135,79' onclick='clickColor(\"#CC99FF\",95,135)' onmouseover='mouseOverColor(\"#CC99FF\")' alt='#CC99FF' />" +
		"<area style='cursor:pointer' shape='poly' coords='162,75,171,79,171,90,162,94,153,90,153,79' onclick='clickColor(\"#CC66FF\",95,153)' onmouseover='mouseOverColor(\"#CC66FF\")' alt='#CC66FF' />" +
		"<area style='cursor:pointer' shape='poly' coords='180,75,189,79,189,90,180,94,171,90,171,79' onclick='clickColor(\"#CC33FF\",95,171)' onmouseover='mouseOverColor(\"#CC33FF\")' alt='#CC33FF' />" +
		"<area style='cursor:pointer' shape='poly' coords='198,75,207,79,207,90,198,94,189,90,189,79' onclick='clickColor(\"#CC00FF\",95,189)' onmouseover='mouseOverColor(\"#CC00FF\")' alt='#CC00FF' />" +
		"<area style='cursor:pointer' shape='poly' coords='216,75,225,79,225,90,216,94,207,90,207,79' onclick='clickColor(\"#9900CC\",95,207)' onmouseover='mouseOverColor(\"#9900CC\")' alt='#9900CC' />" +
		"<area style='cursor:pointer' shape='poly' coords='9,90,18,94,18,105,9,109,0,105,0,94' onclick='clickColor(\"#003300\",110,0)' onmouseover='mouseOverColor(\"#003300\")' alt='#003300' />" +
		"<area style='cursor:pointer' shape='poly' coords='27,90,36,94,36,105,27,109,18,105,18,94' onclick='clickColor(\"#009933\",110,18)' onmouseover='mouseOverColor(\"#009933\")' alt='#009933' />" +
		"<area style='cursor:pointer' shape='poly' coords='45,90,54,94,54,105,45,109,36,105,36,94' onclick='clickColor(\"#33CC33\",110,36)' onmouseover='mouseOverColor(\"#33CC33\")' alt='#33CC33' />" +
		"<area style='cursor:pointer' shape='poly' coords='63,90,72,94,72,105,63,109,54,105,54,94' onclick='clickColor(\"#66FF66\",110,54)' onmouseover='mouseOverColor(\"#66FF66\")' alt='#66FF66' />" +
		"<area style='cursor:pointer' shape='poly' coords='81,90,90,94,90,105,81,109,72,105,72,94' onclick='clickColor(\"#99FF99\",110,72)' onmouseover='mouseOverColor(\"#99FF99\")' alt='#99FF99' />" +
		"<area style='cursor:pointer' shape='poly' coords='99,90,108,94,108,105,99,109,90,105,90,94' onclick='clickColor(\"#CCFFCC\",110,90)' onmouseover='mouseOverColor(\"#CCFFCC\")' alt='#CCFFCC' />" +
		"<area style='cursor:pointer' shape='poly' coords='117,90,126,94,126,105,117,109,108,105,108,94' onclick='clickColor(\"#FFFFFF\",110,108)' onmouseover='mouseOverColor(\"#FFFFFF\")' alt='#FFFFFF' />" +
		"<area style='cursor:pointer' shape='poly' coords='135,90,144,94,144,105,135,109,126,105,126,94' onclick='clickColor(\"#FFCCFF\",110,126)' onmouseover='mouseOverColor(\"#FFCCFF\")' alt='#FFCCFF' />" +
		"<area style='cursor:pointer' shape='poly' coords='153,90,162,94,162,105,153,109,144,105,144,94' onclick='clickColor(\"#FF99FF\",110,144)' onmouseover='mouseOverColor(\"#FF99FF\")' alt='#FF99FF' />" +
		"<area style='cursor:pointer' shape='poly' coords='171,90,180,94,180,105,171,109,162,105,162,94' onclick='clickColor(\"#FF66FF\",110,162)' onmouseover='mouseOverColor(\"#FF66FF\")' alt='#FF66FF' />" +
		"<area style='cursor:pointer' shape='poly' coords='189,90,198,94,198,105,189,109,180,105,180,94' onclick='clickColor(\"#FF00FF\",110,180)' onmouseover='mouseOverColor(\"#FF00FF\")' alt='#FF00FF' />" +
		"<area style='cursor:pointer' shape='poly' coords='207,90,216,94,216,105,207,109,198,105,198,94' onclick='clickColor(\"#CC00CC\",110,198)' onmouseover='mouseOverColor(\"#CC00CC\")' alt='#CC00CC' />" +
		"<area style='cursor:pointer' shape='poly' coords='225,90,234,94,234,105,225,109,216,105,216,94' onclick='clickColor(\"#660066\",110,216)' onmouseover='mouseOverColor(\"#660066\")' alt='#660066' />" +
		"<area style='cursor:pointer' shape='poly' coords='18,105,27,109,27,120,18,124,9,120,9,109' onclick='clickColor(\"#336600\",125,9)' onmouseover='mouseOverColor(\"#336600\")' alt='#336600' />" +
		"<area style='cursor:pointer' shape='poly' coords='36,105,45,109,45,120,36,124,27,120,27,109' onclick='clickColor(\"#009900\",125,27)' onmouseover='mouseOverColor(\"#009900\")' alt='#009900' />" +
		"<area style='cursor:pointer' shape='poly' coords='54,105,63,109,63,120,54,124,45,120,45,109' onclick='clickColor(\"#66FF33\",125,45)' onmouseover='mouseOverColor(\"#66FF33\")' alt='#66FF33' />" +
		"<area style='cursor:pointer' shape='poly' coords='72,105,81,109,81,120,72,124,63,120,63,109' onclick='clickColor(\"#99FF66\",125,63)' onmouseover='mouseOverColor(\"#99FF66\")' alt='#99FF66' />" +
		"<area style='cursor:pointer' shape='poly' coords='90,105,99,109,99,120,90,124,81,120,81,109' onclick='clickColor(\"#CCFF99\",125,81)' onmouseover='mouseOverColor(\"#CCFF99\")' alt='#CCFF99' />" +
		"<area style='cursor:pointer' shape='poly' coords='108,105,117,109,117,120,108,124,99,120,99,109' onclick='clickColor(\"#FFFFCC\",125,99)' onmouseover='mouseOverColor(\"#FFFFCC\")' alt='#FFFFCC' />" +
		"<area style='cursor:pointer' shape='poly' coords='126,105,135,109,135,120,126,124,117,120,117,109' onclick='clickColor(\"#FFCCCC\",125,117)' onmouseover='mouseOverColor(\"#FFCCCC\")' alt='#FFCCCC' />" +
		"<area style='cursor:pointer' shape='poly' coords='144,105,153,109,153,120,144,124,135,120,135,109' onclick='clickColor(\"#FF99CC\",125,135)' onmouseover='mouseOverColor(\"#FF99CC\")' alt='#FF99CC' />" +
		"<area style='cursor:pointer' shape='poly' coords='162,105,171,109,171,120,162,124,153,120,153,109' onclick='clickColor(\"#FF66CC\",125,153)' onmouseover='mouseOverColor(\"#FF66CC\")' alt='#FF66CC' />" +
		"<area style='cursor:pointer' shape='poly' coords='180,105,189,109,189,120,180,124,171,120,171,109' onclick='clickColor(\"#FF33CC\",125,171)' onmouseover='mouseOverColor(\"#FF33CC\")' alt='#FF33CC' />" +
		"<area style='cursor:pointer' shape='poly' coords='198,105,207,109,207,120,198,124,189,120,189,109' onclick='clickColor(\"#CC0099\",125,189)' onmouseover='mouseOverColor(\"#CC0099\")' alt='#CC0099' />" +
		"<area style='cursor:pointer' shape='poly' coords='216,105,225,109,225,120,216,124,207,120,207,109' onclick='clickColor(\"#993399\",125,207)' onmouseover='mouseOverColor(\"#993399\")' alt='#993399' />" +
		"<area style='cursor:pointer' shape='poly' coords='27,120,36,124,36,135,27,139,18,135,18,124' onclick='clickColor(\"#333300\",140,18)' onmouseover='mouseOverColor(\"#333300\")' alt='#333300' />" +
		"<area style='cursor:pointer' shape='poly' coords='45,120,54,124,54,135,45,139,36,135,36,124' onclick='clickColor(\"#669900\",140,36)' onmouseover='mouseOverColor(\"#669900\")' alt='#669900' />" +
		"<area style='cursor:pointer' shape='poly' coords='63,120,72,124,72,135,63,139,54,135,54,124' onclick='clickColor(\"#99FF33\",140,54)' onmouseover='mouseOverColor(\"#99FF33\")' alt='#99FF33' />" +
		"<area style='cursor:pointer' shape='poly' coords='81,120,90,124,90,135,81,139,72,135,72,124' onclick='clickColor(\"#CCFF66\",140,72)' onmouseover='mouseOverColor(\"#CCFF66\")' alt='#CCFF66' />" +
		"<area style='cursor:pointer' shape='poly' coords='99,120,108,124,108,135,99,139,90,135,90,124' onclick='clickColor(\"#FFFF99\",140,90)' onmouseover='mouseOverColor(\"#FFFF99\")' alt='#FFFF99' />" +
		"<area style='cursor:pointer' shape='poly' coords='117,120,126,124,126,135,117,139,108,135,108,124' onclick='clickColor(\"#FFCC99\",140,108)' onmouseover='mouseOverColor(\"#FFCC99\")' alt='#FFCC99' />" +
		"<area style='cursor:pointer' shape='poly' coords='135,120,144,124,144,135,135,139,126,135,126,124' onclick='clickColor(\"#FF9999\",140,126)' onmouseover='mouseOverColor(\"#FF9999\")' alt='#FF9999' />" +
		"<area style='cursor:pointer' shape='poly' coords='153,120,162,124,162,135,153,139,144,135,144,124' onclick='clickColor(\"#FF6699\",140,144)' onmouseover='mouseOverColor(\"#FF6699\")' alt='#FF6699' />" +
		"<area style='cursor:pointer' shape='poly' coords='171,120,180,124,180,135,171,139,162,135,162,124' onclick='clickColor(\"#FF3399\",140,162)' onmouseover='mouseOverColor(\"#FF3399\")' alt='#FF3399' />" +
		"<area style='cursor:pointer' shape='poly' coords='189,120,198,124,198,135,189,139,180,135,180,124' onclick='clickColor(\"#CC3399\",140,180)' onmouseover='mouseOverColor(\"#CC3399\")' alt='#CC3399' />" +
		"<area style='cursor:pointer' shape='poly' coords='207,120,216,124,216,135,207,139,198,135,198,124' onclick='clickColor(\"#990099\",140,198)' onmouseover='mouseOverColor(\"#990099\")' alt='#990099' />" +
		"<area style='cursor:pointer' shape='poly' coords='36,135,45,139,45,150,36,154,27,150,27,139' onclick='clickColor(\"#666633\",155,27)' onmouseover='mouseOverColor(\"#666633\")' alt='#666633' />" +
		"<area style='cursor:pointer' shape='poly' coords='54,135,63,139,63,150,54,154,45,150,45,139' onclick='clickColor(\"#99CC00\",155,45)' onmouseover='mouseOverColor(\"#99CC00\")' alt='#99CC00' />" +
		"<area style='cursor:pointer' shape='poly' coords='72,135,81,139,81,150,72,154,63,150,63,139' onclick='clickColor(\"#CCFF33\",155,63)' onmouseover='mouseOverColor(\"#CCFF33\")' alt='#CCFF33' />" +
		"<area style='cursor:pointer' shape='poly' coords='90,135,99,139,99,150,90,154,81,150,81,139' onclick='clickColor(\"#FFFF66\",155,81)' onmouseover='mouseOverColor(\"#FFFF66\")' alt='#FFFF66' />" +
		"<area style='cursor:pointer' shape='poly' coords='108,135,117,139,117,150,108,154,99,150,99,139' onclick='clickColor(\"#FFCC66\",155,99)' onmouseover='mouseOverColor(\"#FFCC66\")' alt='#FFCC66' />" +
		"<area style='cursor:pointer' shape='poly' coords='126,135,135,139,135,150,126,154,117,150,117,139' onclick='clickColor(\"#FF9966\",155,117)' onmouseover='mouseOverColor(\"#FF9966\")' alt='#FF9966' />" +
		"<area style='cursor:pointer' shape='poly' coords='144,135,153,139,153,150,144,154,135,150,135,139' onclick='clickColor(\"#FF6666\",155,135)' onmouseover='mouseOverColor(\"#FF6666\")' alt='#FF6666' />" +
		"<area style='cursor:pointer' shape='poly' coords='162,135,171,139,171,150,162,154,153,150,153,139' onclick='clickColor(\"#FF0066\",155,153)' onmouseover='mouseOverColor(\"#FF0066\")' alt='#FF0066' />" +
		"<area style='cursor:pointer' shape='poly' coords='180,135,189,139,189,150,180,154,171,150,171,139' onclick='clickColor(\"#CC6699\",155,171)' onmouseover='mouseOverColor(\"#CC6699\")' alt='#CC6699' />" +
		"<area style='cursor:pointer' shape='poly' coords='198,135,207,139,207,150,198,154,189,150,189,139' onclick='clickColor(\"#993366\",155,189)' onmouseover='mouseOverColor(\"#993366\")' alt='#993366' />" +
		"<area style='cursor:pointer' shape='poly' coords='45,150,54,154,54,165,45,169,36,165,36,154' onclick='clickColor(\"#999966\",170,36)' onmouseover='mouseOverColor(\"#999966\")' alt='#999966' />" +
		"<area style='cursor:pointer' shape='poly' coords='63,150,72,154,72,165,63,169,54,165,54,154' onclick='clickColor(\"#CCCC00\",170,54)' onmouseover='mouseOverColor(\"#CCCC00\")' alt='#CCCC00' />" +
		"<area style='cursor:pointer' shape='poly' coords='81,150,90,154,90,165,81,169,72,165,72,154' onclick='clickColor(\"#FFFF00\",170,72)' onmouseover='mouseOverColor(\"#FFFF00\")' alt='#FFFF00' />" +
		"<area style='cursor:pointer' shape='poly' coords='99,150,108,154,108,165,99,169,90,165,90,154' onclick='clickColor(\"#FFCC00\",170,90)' onmouseover='mouseOverColor(\"#FFCC00\")' alt='#FFCC00' />" +
		"<area style='cursor:pointer' shape='poly' coords='117,150,126,154,126,165,117,169,108,165,108,154' onclick='clickColor(\"#FF9933\",170,108)' onmouseover='mouseOverColor(\"#FF9933\")' alt='#FF9933' />" +
		"<area style='cursor:pointer' shape='poly' coords='135,150,144,154,144,165,135,169,126,165,126,154' onclick='clickColor(\"#FF6600\",170,126)' onmouseover='mouseOverColor(\"#FF6600\")' alt='#FF6600' />" +
		"<area style='cursor:pointer' shape='poly' coords='153,150,162,154,162,165,153,169,144,165,144,154' onclick='clickColor(\"#FF5050\",170,144)' onmouseover='mouseOverColor(\"#FF5050\")' alt='#FF5050' />" +
		"<area style='cursor:pointer' shape='poly' coords='171,150,180,154,180,165,171,169,162,165,162,154' onclick='clickColor(\"#CC0066\",170,162)' onmouseover='mouseOverColor(\"#CC0066\")' alt='#CC0066' />" +
		"<area style='cursor:pointer' shape='poly' coords='189,150,198,154,198,165,189,169,180,165,180,154' onclick='clickColor(\"#660033\",170,180)' onmouseover='mouseOverColor(\"#660033\")' alt='#660033' />" +
		"<area style='cursor:pointer' shape='poly' coords='54,165,63,169,63,180,54,184,45,180,45,169' onclick='clickColor(\"#996633\",185,45)' onmouseover='mouseOverColor(\"#996633\")' alt='#996633' />" +
		"<area style='cursor:pointer' shape='poly' coords='72,165,81,169,81,180,72,184,63,180,63,169' onclick='clickColor(\"#CC9900\",185,63)' onmouseover='mouseOverColor(\"#CC9900\")' alt='#CC9900' />" +
		"<area style='cursor:pointer' shape='poly' coords='90,165,99,169,99,180,90,184,81,180,81,169' onclick='clickColor(\"#FF9900\",185,81)' onmouseover='mouseOverColor(\"#FF9900\")' alt='#FF9900' />" +
		"<area style='cursor:pointer' shape='poly' coords='108,165,117,169,117,180,108,184,99,180,99,169' onclick='clickColor(\"#CC6600\",185,99)' onmouseover='mouseOverColor(\"#CC6600\")' alt='#CC6600' />" +
		"<area style='cursor:pointer' shape='poly' coords='126,165,135,169,135,180,126,184,117,180,117,169' onclick='clickColor(\"#FF3300\",185,117)' onmouseover='mouseOverColor(\"#FF3300\")' alt='#FF3300' />" +
		"<area style='cursor:pointer' shape='poly' coords='144,165,153,169,153,180,144,184,135,180,135,169' onclick='clickColor(\"#FF0000\",185,135)' onmouseover='mouseOverColor(\"#FF0000\")' alt='#FF0000' />" +
		"<area style='cursor:pointer' shape='poly' coords='162,165,171,169,171,180,162,184,153,180,153,169' onclick='clickColor(\"#CC0000\",185,153)' onmouseover='mouseOverColor(\"#CC0000\")' alt='#CC0000' />" +
		"<area style='cursor:pointer' shape='poly' coords='180,165,189,169,189,180,180,184,171,180,171,169' onclick='clickColor(\"#990033\",185,171)' onmouseover='mouseOverColor(\"#990033\")' alt='#990033' />" +
		"<area style='cursor:pointer' shape='poly' coords='63,180,72,184,72,195,63,199,54,195,54,184' onclick='clickColor(\"#663300\",200,54)' onmouseover='mouseOverColor(\"#663300\")' alt='#663300' />" +
		"<area style='cursor:pointer' shape='poly' coords='81,180,90,184,90,195,81,199,72,195,72,184' onclick='clickColor(\"#996600\",200,72)' onmouseover='mouseOverColor(\"#996600\")' alt='#996600' />" +
		"<area style='cursor:pointer' shape='poly' coords='99,180,108,184,108,195,99,199,90,195,90,184' onclick='clickColor(\"#CC3300\",200,90)' onmouseover='mouseOverColor(\"#CC3300\")' alt='#CC3300' />" +
		"<area style='cursor:pointer' shape='poly' coords='117,180,126,184,126,195,117,199,108,195,108,184' onclick='clickColor(\"#993300\",200,108)' onmouseover='mouseOverColor(\"#993300\")' alt='#993300' />" +
		"<area style='cursor:pointer' shape='poly' coords='135,180,144,184,144,195,135,199,126,195,126,184' onclick='clickColor(\"#990000\",200,126)' onmouseover='mouseOverColor(\"#990000\")' alt='#990000' />" +
		"<area style='cursor:pointer' shape='poly' coords='153,180,162,184,162,195,153,199,144,195,144,184' onclick='clickColor(\"#800000\",200,144)' onmouseover='mouseOverColor(\"#800000\")' alt='#800000' />" +
		"<area style='cursor:pointer' shape='poly' coords='171,180,180,184,180,195,171,199,162,195,162,184' onclick='clickColor(\"#660000\",200,162)' onmouseover='mouseOverColor(\"#660000\")' alt='#660000' />";
		
	}
}