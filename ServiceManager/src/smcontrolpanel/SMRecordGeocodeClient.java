package smcontrolpanel;
import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import SMDataDefinition.SMGoogleMapAPIKey;
import ServletUtilities.clsManageRequestParameters;

public class SMRecordGeocodeClient extends HttpServlet {

	public static final String STATUS_BOX = "STATUS_BOX";
	public static final String COUNTER_BOX = "COUNTER_BOX";
	public static final String LATITUDE_BOX = "LATITUDE_BOX";
	public static final String LONGITUDE_BOX = "LONGITUDE_BOX";
	public static final String SPEED_BOX = "SPEED_BOX";
	public static final String ALTITUDE_BOX = "ALTITUDE_BOX";
	public static final String ACCURACY_BOX = "ACCURACY_BOX";
	public static final String ALTITUDEACCURACY_BOX = "ALTITUDEACCURACY_BOX";
	public static final String TRACKING_BUTTON = "TRACKINGBUTTON";
	public static final String START_TRACKING_LABEL = "Start tracking";
	public static final String STOP_TRACKING_LABEL = "Stop tracking";
	public static final String START_TIMER_PARAM = "START_TIMER";
	
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
				SMSystemFunctions.SMRecordGeocode)
		){
			return;
		}
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    
	    String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
	    
	    String title = "Record GPS Positions";
	    String subtitle = "";
	    
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
	    
	    out.println(sCommandScripts(request));
		//Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "\">Return to user login</A><BR><BR>");
	    
    	out.println ("<FORM NAME='MAINFORM' ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) 
    			+ "smcontrolpanel.SMRecordGeocodeAction\">");
    	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
    	
		out.println("<INPUT TYPE=HIDDEN NAME=\"" + SMRecordGeocodeAction.LATITUDE_PARAMETER 
				+ "\" VALUE=\"" + "" + "\""
				+ " id=\"" + SMRecordGeocodeAction.LATITUDE_PARAMETER + "\""
				+ "\">"
		);
		out.println("<INPUT TYPE=HIDDEN NAME=\"" + SMRecordGeocodeAction.LONGITUDE_PARAMETER
				+ "\" VALUE=\"" + "" + "\""
				+ " id=\"" + SMRecordGeocodeAction.LONGITUDE_PARAMETER + "\""
				+ "\">"
		);
		out.println("<INPUT TYPE=HIDDEN NAME=\"" + SMRecordGeocodeAction.SPEED_PARAMETER
				+ "\" VALUE=\"" + "" + "\""
				+ " id=\"" + SMRecordGeocodeAction.SPEED_PARAMETER + "\""
				+ "\">"
		);
		out.println("<INPUT TYPE=HIDDEN NAME=\"" + SMRecordGeocodeAction.ALTITUDE_PARAMETER
				+ "\" VALUE=\"" + "" + "\""
				+ " id=\"" + SMRecordGeocodeAction.ALTITUDE_PARAMETER + "\""
				+ "\">"
		);
		out.println("<INPUT TYPE=HIDDEN NAME=\"" + SMRecordGeocodeAction.ACCURACY_PARAMETER
				+ "\" VALUE=\"" + "" + "\""
				+ " id=\"" + SMRecordGeocodeAction.ACCURACY_PARAMETER + "\""
				+ "\">"
		);
		out.println("<INPUT TYPE=HIDDEN NAME=\"" + SMRecordGeocodeAction.ALTITUDEACCURACY_PARAMETER
				+ "\" VALUE=\"" + "" + "\""
				+ " id=\"" + SMRecordGeocodeAction.ALTITUDEACCURACY_PARAMETER + "\""
				+ "\">"
		);
		out.println("<INPUT TYPE=HIDDEN NAME=\"" + SMRecordGeocodeAction.TIMESTAMP_PARAMETER
				+ "\" VALUE=\"" + "" + "\""
				+ " id=\"" + SMRecordGeocodeAction.TIMESTAMP_PARAMETER + "\""
				+ "\">"
		);
		
    	out.println("<input type=\"button\" id = \"" + TRACKING_BUTTON + "\" onclick=\"startTimer();\""  
            + " value=\"" + START_TRACKING_LABEL + "\"/>");
    	
		out.println("&nbsp;&nbsp;&nbsp;<B>Status</B>:&nbsp;<INPUT TYPE=TEXT readonly=\"readonly\" NAME = \"" 
				+ STATUS_BOX + "\""
				+ " ID = \"" + STATUS_BOX + "\""	
				+ " SIZE=" + "80"
				+ ">"
		);
    	
    	out.println("<TABLE BORDER=0>");
    	out.println("<TR><TD>");
    	out.println("<TABLE BORDER=0>");
		out.println("<TR><TD ALIGN=RIGHT><B>Update count</B>:</TD><TD><INPUT TYPE=TEXT readonly=\"readonly\" NAME = \"" + COUNTER_BOX + "\""
				+ " ID = \"" + COUNTER_BOX + "\""	
				+ " SIZE=" + "8"
				+ "></TD></TR>"
		);
		out.println("<TR><TD ALIGN=RIGHT><B>Latitude</B>:</TD><TD><INPUT TYPE=TEXT readonly=\"readonly\" NAME = \"" + LATITUDE_BOX + "\""
				+ " ID = \"" + LATITUDE_BOX + "\""	
				+ " SIZE=" + "15"
				+ "></TD></TR>"
		);
		out.println("<TR><TD ALIGN=RIGHT><B>Longitude</B>:</TD><TD><INPUT TYPE=TEXT readonly=\"readonly\" NAME = \"" + LONGITUDE_BOX + "\""
				+ " ID = \"" + LONGITUDE_BOX + "\""	
				+ " SIZE=" + "15"
				+ "></TD></TR>"
		);
		out.println("<TR><TD ALIGN=RIGHT><B>Speed</B>:</TD><TD><INPUT TYPE=TEXT readonly=\"readonly\" NAME = \"" + SPEED_BOX + "\""
				+ " ID = \"" + SPEED_BOX + "\""	
				+ " SIZE=" + "15"
				+ "></TD></TR>"
		);
		out.println("<TR><TD ALIGN=RIGHT><B>Altitude</B>:</TD><TD><INPUT TYPE=TEXT readonly=\"readonly\" NAME = \"" + ALTITUDE_BOX + "\""
				+ " ID = \"" + ALTITUDE_BOX + "\""	
				+ " SIZE=" + "15"
				+ "></TD></TR>"
		);
		out.println("<TR><TD ALIGN=RIGHT><B>Geocode accuracy in meters</B>:</TD><TD><INPUT TYPE=TEXT readonly=\"readonly\" NAME = \"" + ACCURACY_BOX + "\""
				+ " ID = \"" + ACCURACY_BOX + "\""	
				+ " SIZE=" + "15"
				+ "></TD></TR>"
		);
		out.println("<TR><TD ALIGN=RIGHT><B>Altitude accuracy in meters</B>:</TD><TD><INPUT TYPE=TEXT readonly=\"readonly\" NAME = \"" + ALTITUDEACCURACY_BOX + "\""
				+ " ID = \"" + ALTITUDEACCURACY_BOX + "\""	
				+ " SIZE=" + "15"
				+ "></TD></TR>"
		);
		//Close the left table:
		out.println("</TABLE>");
		out.println("</TD>");
		out.println("<TD>");
    	out.println ("<div class=\"mapblock\" id = \"map\"");
    	out.println (" style=\"height:200;width:200;overflow:auto;border:1px solid blue;display:block;\"");
    	out.println (">");
    	out.println ("</div>");
    	out.println("</TD>");
    	out.println("</TR>");
    	//Close the outer table:
    	out.println("</TABLE>");
    	
    	out.println ("</FORM>");

    	
    	//out.println("<div id=\"map_canvas\" style=\"width:100%; height:100%\"></div>");
    	
	    out.println("</BODY></HTML>");
	}
	private String sCommandScripts(HttpServletRequest req){
		String s = "";
		
		s += "<NOSCRIPT>\n"
			+ "    <font color=red>\n"
			+ "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
			+ "    </font>\n"
			+ "</NOSCRIPT>\n"
		;

		s += "  <script type=\"text/javascript\"\n"
	    //+ "    src=\"https://maps.googleapis.com/maps/api/js?sensor=false\">\n"
	    + "    src=\"https://maps.googleapis.com/maps/api/js?key=" + SMGoogleMapAPIKey.SMCP_GMAPS_API_KEY1 + "\">\n"
	    + "  </script>\n"
		;
		s += "<script type=\"text/javascript\">\n";

		/*
		if (SMUtilities.get_Request_Parameter(START_TIMER_PARAM, req).compareToIgnoreCase("") != 0){
			s += "window.onload = function(){\n"
				//+ "    document.forms[\"MAINFORM\"].elements[\"" + TRACKING_BUTTON + "\"].hide;\n"
				+ "    startTimer();\n"
				+ "}\n\n"
			;
		}
		*/
		
		s += "var watchID;\n";
		s += "var geoLoc;\n";
		
		s += "var c=0;\n";
		s += "var t;\n";
		s += "var timer_is_on=0;\n";

		s += "\n";
		s += "function showLocation(position) {\n";
		s += "    updateStatus(position);\n";
		s += "    submitValues(position);\n";
		s += "    mapThisGoogle(position.coords.latitude, position.coords.longitude);\n";
		s += "}\n";
		s += "\n";
		s += "function errorHandler(err) {\n";
		s += "    if(err.code == 1) {\n";
		s += "        alert('Error: Access is denied!');\n";
		s += "    }else if( err.code == 2) {\n";
		s += "        alert('Error: Position is unavailable!');\n";
		s += "    }\n";
		s += "}\n";
		s += "\n";


		s += "function startTimer()\n";
		s += "{\n";
		s += "    if (!timer_is_on)\n";
		s += "    {\n";
		s += "        timer_is_on=1;\n";
		s += "        getGeocode();\n";
		s += "    }\n";
		s += "}\n";
		s += "\n";

		s += "function getGeocode(){\n";
		s += "    if(navigator.geolocation){\n";
		          // timeout at 60000 milliseconds (60 seconds)
		s += "        var options = {enableHighAccuracy:true, maximumAge:120000, timeout:45000};\n";
		//s += "        geoLoc = navigator.geolocation;\n";
		s += "        navigator.geolocation.getCurrentPosition(showLocation, errorHandler, options);\n";
		s += "        c=c+1;\n";
		s += "        document.forms[\"MAINFORM\"].elements[\"" + COUNTER_BOX + "\"].value = c;\n";
		s += "        t=setTimeout(\"getGeocode()\",60000);\n";
		s += "    }else{\n";
		s += "        alert('Browser does not support geolocation!');\n";
		s += "    }\n";
		s += "}\n";
		/*
		s += "function toggleTracking(button){\n";
		s += "    if (button.value == '" + START_TRACKING_LABEL + "'){\n";
		s += "        if(navigator.geolocation){\n";
		              // timeout at 60000 milliseconds (60 seconds)
		s += "            var options = {enableHighAccuracy:true, maximumAge:60000, timeout:30000};\n";
		s += "            geoLoc = navigator.geolocation;\n";
		s += "            watchID = geoLoc.watchPosition(showLocation, errorHandler, options);\n";
		s += "            document.forms[\"MAINFORM\"].elements[\"" + TRACKING_BUTTON + "\"].value = 'Stop tracking';\n";
		s += "        }else{\n";
		s += "            alert('Browser does not support geolocation!');\n";
		s += "        }\n";
		s += "    } else {\n";
		s += "        stopWatch();\n";
		s += "    }\n";
		s += "}\n";
		
		s += "function stopWatch(){\n";
		s += "    geoLoc.clearWatch(watchID);\n";
		s += "    watchID = null;\n";
		s += "    document.forms[\"MAINFORM\"].elements[\"" + TRACKING_BUTTON + "\"].value = 'Start tracking';\n";
		s += "}\n";
		s += "\n";
		*/
		
		s += "function submitValues(position){\n"
			+ "    document.getElementById(\"" + SMRecordGeocodeAction.LATITUDE_PARAMETER + "\").value = " 
				+ "position.coords.latitude;\n"
			+ "    document.getElementById(\"" + SMRecordGeocodeAction.LONGITUDE_PARAMETER + "\").value = " 
				+ "position.coords.longitude;\n"
			+ "    document.getElementById(\"" + SMRecordGeocodeAction.SPEED_PARAMETER + "\").value = " 
				+ "position.coords.speed;\n"
			+ "    document.getElementById(\"" + SMRecordGeocodeAction.ALTITUDE_PARAMETER + "\").value = " 
				+ "position.coords.altitude;\n"
			+ "    document.getElementById(\"" + SMRecordGeocodeAction.ACCURACY_PARAMETER + "\").value = " 
				+ "position.coords.accuracy;\n"
			+ "    document.getElementById(\"" + SMRecordGeocodeAction.ALTITUDEACCURACY_PARAMETER + "\").value = " 
				+ "position.coords.altitudeaccuracy;\n"
			+ "    document.getElementById(\"" + SMRecordGeocodeAction.TIMESTAMP_PARAMETER + "\").value = " 
				+ "position.timestamp;\n"
			+ "    document.forms[\"MAINFORM\"].submit();\n"
			+ "}\n"
		;
/*
		s += "function submitValues(){\n"
			+ "    document.getElementById(\"" + SMRecordGeocodeAction.LATITUDE_PARAMETER + "\").value = " 
				+ "'LAT';\n"
			+ "    document.getElementById(\"" + SMRecordGeocodeAction.LONGITUDE_PARAMETER + "\").value = " 
				+ "'LON';\n"
			+ "    document.getElementById(\"" + SMRecordGeocodeAction.SPEED_PARAMETER + "\").value = " 
				+ "'SPD';\n"
			+ "    document.getElementById(\"" + SMRecordGeocodeAction.ALTITUDE_PARAMETER + "\").value = " 
				+ "'ALT';\n"
			+ "    document.getElementById(\"" + SMRecordGeocodeAction.ACCURACY_PARAMETER + "\").value = " 
				+ "'ACC';\n"
			+ "    document.getElementById(\"" + SMRecordGeocodeAction.ALTITUDEACCURACY_PARAMETER + "\").value = " 
				+ "'AAC';\n"
			+ "    document.getElementById(\"" + SMRecordGeocodeAction.TIMESTAMP_PARAMETER + "\").value = " 
				+ "'TS';\n"
			+ "    document.forms[\"MAINFORM\"].submit();\n"
			+ "}\n"
		;
*/		
		s += "function updateStatus(position) {\n";
		s += "    document.forms[\"MAINFORM\"].elements[\"" + COUNTER_BOX + "\"].value = c;\n";
		s += "    document.forms[\"MAINFORM\"].elements[\"" + STATUS_BOX + "\"].value = "
				+ "'Last updated: ' + new Date(position.timestamp);\n";
		s += "    document.forms[\"MAINFORM\"].elements[\"" + LATITUDE_BOX + "\"].value = position.coords.latitude;\n";
		s += "    document.forms[\"MAINFORM\"].elements[\"" + LONGITUDE_BOX + "\"].value = position.coords.longitude;\n";
		s += "    document.forms[\"MAINFORM\"].elements[\"" + SPEED_BOX + "\"].value = position.coords.speed * 2.2369 + ' MPH';\n";
		s += "    document.forms[\"MAINFORM\"].elements[\"" + ACCURACY_BOX + "\"].value = position.coords.accuracy;\n";
		s += "    document.forms[\"MAINFORM\"].elements[\"" + ALTITUDEACCURACY_BOX + "\"].value = position.coords.altitudeaccuracy;\n";
		s += "}\n\n";

		s += "function getTime() {\n";
		s += "    var dTime = new Date();\n";
		s += "    var hours = dTime.getHours();\n";
		s += "    var minute = dTime.getMinutes();\n";
		s += "    var second = dTime.getSeconds();\n";
		s += "    var period = 'AM';\n";
		s += "    if (hours > 12) {\n";
		s += "        period = 'PM';\n";
		s += "    }\n";
		s += "    else {\n";
		s += "        period = 'AM';\n";
		s += "    }\n";
		s += "    hours = ((hours > 12) ? hours - 12 : hours);\n";
		s += "    return hours + ':' + minute + ':' + second + ' ' + period;\n";
		s += "}\n";
		s += "\n";
		
		s += "function mapThisGoogle(latitude,longitude)\n";
		s += "{\n";
	    s += "      var myOptions = {\n"
	    + "        zoom: 9,\n"
	    + "        center: new google.maps.LatLng(latitude, longitude),\n"
	    + "        mapTypeId: google.maps.MapTypeId.ROADMAP\n"
	    + "      };\n"
	    
	    + "      var mapDiv = document.getElementById('map');\n"
	    + "      map = new google.maps.Map(mapDiv,myOptions);\n"
	    ;
		s += "}\n\n";
		
		
		s += "</script>\n";
		return s;
	}
}
