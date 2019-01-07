package smcontrolpanel;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import SMDataDefinition.SMTableusergeocodes;
import SMDataDefinition.SMTableusers;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class SMRecordGeocodeAction extends HttpServlet{
	
	public static final String LATITUDE_PARAMETER = "lat"; //"LATITUDEPARAM";
	public static final String LONGITUDE_PARAMETER = "lon"; //"LONGITUDEPARAM";
	public static final String SPEED_PARAMETER = "SPEEDPARAM";
	public static final String ALTITUDE_PARAMETER = "ALTITUDEPARAM";
	public static final String ACCURACY_PARAMETER = "ACCURACYPARAM";
	public static final String ALTITUDEACCURACY_PARAMETER = "ALTITUDEACCURACYPARAM";
	public static final String TIMESTAMP_PARAMETER = "t";// "TIMESTAMPPARAM";
	
	private static final long serialVersionUID = 1L;
	private boolean bDebugMode = false;
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), -1L)){return;}
		
		String sLatitude = clsManageRequestParameters.get_Request_Parameter(LATITUDE_PARAMETER, request);
		String sLongitude = clsManageRequestParameters.get_Request_Parameter(LONGITUDE_PARAMETER, request);
		String sSpeed = clsManageRequestParameters.get_Request_Parameter(SPEED_PARAMETER, request);
		String sAltitude = clsManageRequestParameters.get_Request_Parameter(ALTITUDE_PARAMETER, request);
		String sAccuracy = clsManageRequestParameters.get_Request_Parameter(ACCURACY_PARAMETER, request);
		String sAltitudeAccuracy = clsManageRequestParameters.get_Request_Parameter(ALTITUDEACCURACY_PARAMETER, request);
		//String sTimestamp = SMUtilities.get_Request_Parameter(TIMESTAMP_PARAMETER, request).trim();
		//String sEntryDateTime = "";
		String sSQL = "";
		if (
			(sLatitude.compareToIgnoreCase("") == 0)
			|| (sLongitude.compareToIgnoreCase("") == 0)
		){
			//Need to trap this:
			if (bDebugMode){
				System.out.println("In " + this.toString() + " - invalid geocode:"
						+ " sLatitude = '" + sLatitude + "', "
						+ " sLongitude = '" + sLongitude + "'."
				);
			}
		}else{
			//DateFormat SQLformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			//try {
			//	sEntryDateTime = SQLformat.format(new Timestamp(Long.parseLong(sTimestamp)));
			//} catch (NumberFormatException e1) {
			//	if (bDebugMode){
			//		System.out.println("In " + this.toString() + " - invalid timestamp:"
			//				+ "'" + sTimestamp + "'."
			//		);
			//	}
			//}
			
			//if (sEntryDateTime.compareToIgnoreCase("") != 0){
				sSQL = "INSERT INTO " + SMTableusergeocodes.TableName
					+ "("
					+ SMTableusergeocodes.lUserID
					+ ", " + SMTableusergeocodes.sLatitude
					+ ", " + SMTableusergeocodes.sLongitude
					+ ", " + SMTableusergeocodes.sSpeed
					+ ", " + SMTableusergeocodes.sAltitude
					+ ", " + SMTableusergeocodes.sAccuracy
					+ ", " + SMTableusergeocodes.sAltitudeAccuracy
					+ ", " + SMTableusergeocodes.sFirstName
					+ ", " + SMTableusergeocodes.sLastName
					+ ", " + SMTableusergeocodes.datimeEntry
					+ ") SELECT "
					+ "" + smaction.getUserID() + ""
					+ ", '" + sLatitude.trim() + "'"
					+ ", '" + sLongitude.trim() + "'"
					+ ", '" + sSpeed.trim() + "'"
					+ ", '" + sAltitude.trim() + "'"
					+ ", '" + sAccuracy.trim() + "'"
					+ ", '" + sAltitudeAccuracy.trim() + "'"
					+ ", " + SMTableusers.sUserFirstName
					+ ", " + SMTableusers.sUserLastName
					//+ ", '" + sEntryDateTime + "'"
					+ ", NOW()"
					+ " FROM " + SMTableusers.TableName
					+ " WHERE ("
						+ "(" + SMTableusers.lid + " = " + smaction.getUserID() + ")"
					+ ")"
				;
				try {
					clsDatabaseFunctions.executeSQL(
						sSQL, 
						getServletContext(), 
						smaction.getsDBID(), 
						"MySQL", 
						this.toString() + ".doPost - user: " + smaction.getUserName());
				} catch (SQLException e) {
					if (bDebugMode){
						System.out.println("In " + this.toString() + " - error inserting record with SQL: " + sSQL
								+ " - " + e.getMessage()
						);
					}
				}
			//}
		}
		response.setStatus(HttpServletResponse.SC_NO_CONTENT);
		return;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}