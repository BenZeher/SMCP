package SMClasses;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTabledoingbusinessasaddresses;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTablemechanics;
import SMDataDefinition.SMTableorderdetails;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTablesmoptions;
import SMDataDefinition.SMTableusers;
import SMDataDefinition.SMTableworkorderdetails;
import SMDataDefinition.SMTableworkorderdetailsheets;
import SMDataDefinition.SMTableworkorders;
import ServletUtilities.clsAjaxFunctions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsMasterEntry;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsStringFunctions;
import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMMasterEditEntry;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class SMWorkOrderHeader extends clsMasterEntry{//java.lang.Object{
	public static final String DETAIL_SHEET_TABLE_BG_COLOR = "#CCFFB2";
	public static final String INSTRUCTIONS_TABLE_BG_COLOR = "#CCFFB2";
	public static final String MANAGERNOTES_TABLE_BG_COLOR = "#CCFFB2";
	public static final String SCHEDULEFIELDS_TABLE_BG_COLOR = "#CCFFB2";
	public static final String ITEMS_TABLE_BG_COLOR = "#FFBCA2";
	public static final String ITEMS_TABLE_ODD_ROW_COLOR = "#FFC7B3";
	public static final String ITEMS_TABLE_WORK_ORDER_ITEM_COMMENT_BG_COLOR = "#FF8484";
	public static final String ITEMS_TABLE_WORK_ORDER_DETAIL_COMMENT_BG_COLOR = "#FFFF66";
	public static final String ORDERCOMMANDS_TABLE_BG_COLOR = "#99CCFF";
	public static final String TERMS_TABLE_BG_COLOR = "#CCFFB2";
	public static final String WORKPERFORMED_TABLE_BG_COLOR = "#99CCFF";
	public static final String SIGNATUREBLOCK_TABLE_BG_COLOR = "#FFBCA2";
	public static final String COMMENTS_TABLE_BG_COLOR = "#F2C3FA";
	public static final String JOBTIMES_TABLE_BG_COLOR = "#F2C3FA";
	public static final String ITEM_QTY_FIELD_WIDTH = "8";
	public static final String FORM_NAME = "MAINFORM";
	public static final String CONVENIENCEPHRASECONTROL_MARKER = "CPM";
	public static final String WORK_ORDER_ITEMLINE_MARKER = "WOILM";
	public static final String WORK_ORDER_WORKPERFORMEDCODE_MARKER = "WOWPCLM";
	public static final String TOTAL_NUMBER_OF_WPC_CODES = "TOTALNUMOFWPCCODES";
	public static final int OVERALL_LENGTH_OF_PADDED_LINE_NUMBER = 8;
	public static final String NUMBER_OF_ITEM_LINES_USED = "NUMOFITEMLINESUSED";
	public static final String ADD_DETAIL_SHEET_BUTTON_LABEL = "Add selected deta<B><FONT COLOR=RED>i</FONT></B>l sheet"; //I
	public static final String ADD_DETAIL_SHEET_COMMAND_VALUE = "ADDDETAILSHEET";
	public static final String ADD_DETAIL_SHEET_DROPDOWN_NAME = "ADDDETAILSHEETDROPDOWN";
	public static final String SET_TO_ZERO_CHECKBOX_MARKER = "SETTOZEROCHECKBOXMARKER";
	public static final int SAVING_FROM_EDIT_SCREEN = 0;
	public static final int SAVING_FROM_CONFIGURE_SCREEN = 1;
	public static final int SAVING_FROM_IMPORT_SCREEN = 2;
	public static final int SAVING_FROM_SCHEDULE_HANDLER = 3;
	public static final int SAVING_FROM_ACCEPT_SIGNATURE = 4;
	public static final int SAVING_FROM_CUSTOM_DETAIL_TEXT_ENTRY = 5;
	public static final String NUMBER_OF_DETAIL_SHEET_ROWS_DISPLAYED = "20";
	
	//Constants used for calculating times:
	public static final String CALCULATE_TIMES_LABEL = "Calculate elapsed times";
	public static final String ELAPSEDTIME1 = "ELAPSEDTIME1";
	public static final String ELAPSEDTIME2 = "ELAPSEDTIME2";
	public static final String ELAPSEDTIME3 = "ELAPSEDTIME3";
	public static final String ELAPSEDTIME4 = "ELAPSEDTIME4";
	public static final String ELAPSEDTIME5 = "ELAPSEDTIME5";
	
	public static final String Paramlid  = "lid";
	public static final String Paramsmechanicinitials = "smechanicinitials";
	public static final String Paramsmechanicname = "smechanicname";
	public static final String Paramdattimeposted = "datimeposted";
	public static final String Paramdattimedone = "dattimedone";
	public static final String Paramstrimmedordernumber = "strimmedordernumber";
	public static final String Paramssignedbyname = "ssignedbyname";
	public static final String Paramdattimesigned = "dattimesigned";
	public static final String Parammsignature = "msignature";
	public static final String Parammcomments = "mcomments";
	public static final String Parammdetailsheettext = "mdetailsheettext";
	public static final String Parammadditionalworkcomments = "madditionalworkcomments";
	public static final String Paramiadditionalworkauthorized = "iadditionalworkauthorized";
	public static final String Paramiimported = "iimported";
	public static final String Paramiposted = "iposted";
	public static final String Paramltimestamp = "ltimestamp";
	public static final String Paramminstructions = "minstructions";
	public static final String Parammmanagernotes = "mmanagernotes";
	public static final String Paramimechid = "imechid";
	public static final String Paramdattimeleftprevious = "dattimeleftprevious";
	public static final String Paramdattimearrivedatcurrent = "dattimearrivedatcurrent";
	public static final String Paramdattimeleftcurrent = "dattimeleftcurrent";
	public static final String Paramdattimearrivedatnext = "dattimearrivedatnext";
	public static final String Paramhasdattimeleftprevious = "hasdattimeleftprevious";
	public static final String Paramhasdattimearrivedatcurrent = "hasdattimearrivedatcurrent";
	public static final String Paramhasdattimeleftcurrent = "hasdattimeleftcurrent";
	public static final String Paramhasdattimearrivedatnext = "hasdattimearrivedatnext";
	public static final String Paramsassistant = "sassistant";
	public static final String Paramsstartingtime = "sstartingtime";
	public static final String Paramscheduleddate = "datscheduleddate";
	public static final String Paramqtyofhours = "dQtyofHours";
	public static final String Paramworkdescription = "mworkdescription";
	public static final String Paramtravelhours = "dTravelHours";
	public static final String Parambackchargehours = "decBackChargeHours";
	public static final String Paramlasteditedbyfullname = "slasteditedbyfullname";
	public static final String Paramdatetimelastedit = "dattimelastedit";
	public static final String Paramijoborder = "ijoborder";
	public static final String Paramsschedulecomment = "sschedulecomment";
	public static final String Paramdattimelastschedulechange = "dattimelastschedulechange";
	public static final String Paramssschedulechangedbyfullname = "sschedulechangedbyfullname";
	public static final String Paramssgdoclink = "sgdoclink";
	public static final String Paramlsignaturboxwidth = "lsignaturboxwidth";
	public static final String Paramsdbaworkorderlogo = "sdbaworkorderlogo";
	//Params to display most recent items
	public static final String Paramsnumberofdays = "snumberofdays";
	public static final String Paramsnumberofitems = "snumberofitems";
	public static final String COMMONLY_USED_ITEMS_SEARCH_NO_OF_DAYS = "180";
	public static final String COMMONLY_USED_ITEMS_SEARCH_NO_OF_ITEMS = "50";
	
	public static final String Parambremovedmech = "REMOVEDMECH";
	public static final String BLANK_MECHANIC_NAME = "*** NONE ***";
	public static final String BLANK_MECHANIC_INITIALS = "";
	public static final String BLANK_MECHANIC_ID = "0";
	
	//This parameter should contain the last time the user read the record from the database:
	public static final String Paramlastreadrecordtimestamp = "lastreadrecordtimestamp";
	
//	JC2WO - add fields from job cost here:
	
	private String m_lid;
	private String m_smechanicinitials;
	private String m_smechanicname;
	private String m_imechid;
	private String m_dattimeposted;
	private String m_dattimedone;
	private String m_strimmedordernumber;
	private String m_ssignedbyname;
	private String m_dattimesigned;
	private String m_msignature;
	private String m_mcomments;
	private String m_mdetailsheettext;
	private String m_madditionalworkcomments;
	private String m_sadditionalworkauthorized;
	private String m_simported;
	private String m_sposted;
	private String m_stimestamp;
	private String m_minstructions;
	private String m_mmanagernotes;
	private String m_sdattimeleftprevious;
	private String m_sdattimearrivedatcurrent;
	private String m_sdattimeleftcurrent;
	private String m_sdattimearrivedatnext;
	private String m_sassistant;
	private String m_sstartingtime;
	private String m_sscheduleddate; //Always stored as MM/dd/yyyy
	private String m_sqtyofhours;
	private String m_sworkdescription;
	private String m_stravelhours;
	private String m_sbackchargehours;
	private String m_slasteditedbyfullname;
	private String m_sdatetimelastedit;
	private String m_ijoborder;
	private String m_sschedulecomment;
	private String m_dattimelastschedulechange;
	private String m_sschedulechangedbyfullname;
	private String m_sgdoclink;
	private String m_lsignatureboxwidth;
	private String m_mdbaaddress;
	private String m_mdbaremittoaddress;
	private String m_sdbaworkorderlogo;
	
	private String m_slastreadrecordtimestamp;
	
	//variables to display most recent items
	private String m_snumberofdays;
	private String m_snumberofitems;

	//This value indicates that this mechanic is 
	//no longer in the system so when the field is validated, 
	//it will not throw an error.
	//private boolean bRemovedMech = false;
	//private boolean bDebugMode = false;
	
	private ArrayList<SMWorkOrderDetail> LineArray;
	public static final String NO_ORDER_NUMBER_MARKER = "(NONE)";
	public static final String RETURN_TO_TRUCKSCHEDULE_PARAM = "ReturnToTruckSchedule";
	public static String LASTENTRYEDITED_PARAM = "LastEntryEdited";
	
	public SMWorkOrderHeader(
        ) {
		initWorkOrderVariables();
        }
	public void loadFromHTTPRequest(HttpServletRequest req) throws Exception{
		initWorkOrderVariables();
		setlid(clsManageRequestParameters.get_Request_Parameter(Paramlid, req).trim());
		if (getlid().compareToIgnoreCase("") == 0){
			setlid("-1");
		}
		
		setsnumberofdays(clsManageRequestParameters.get_Request_Parameter(Paramsnumberofdays, req).trim().replace("&quot;", "\""));
		if(m_snumberofdays.compareToIgnoreCase("") == 0){
			setsnumberofdays(COMMONLY_USED_ITEMS_SEARCH_NO_OF_DAYS);
		}
		
		setsnumberofitems(clsManageRequestParameters.get_Request_Parameter(Paramsnumberofitems, req).trim().replace("&quot;", "\""));
		if(m_snumberofitems.compareToIgnoreCase("") == 0){
			setsnumberofitems(COMMONLY_USED_ITEMS_SEARCH_NO_OF_ITEMS);
		}
		setmechanicsinitials(clsManageRequestParameters.get_Request_Parameter(Paramsmechanicinitials, req).trim().replace("&quot;", "\""));
		setmechanicsname(clsManageRequestParameters.get_Request_Parameter(Paramsmechanicname, req).trim().replace("&quot;", "\""));
		setmechid(clsManageRequestParameters.get_Request_Parameter(Paramimechid, req).trim().replace("&quot;", "\""));
		if (getmechid().compareToIgnoreCase("") == 0){
			setmechid("0");
		}

		setdattimeposted(clsManageRequestParameters.get_Request_Parameter(Paramdattimeposted, req).trim());
		if (getdattimeposted().compareToIgnoreCase("") == 0){
			setdattimeposted(EMPTY_DATETIME_STRING);
		}

		setdattimedone(clsManageRequestParameters.get_Request_Parameter(Paramdattimedone, req).trim());
		if (getdattimedone().compareToIgnoreCase("") == 0){
			setdattimedone(EMPTY_DATE_STRING);
		}
		
		setdattimesigned(clsManageRequestParameters.get_Request_Parameter(Paramdattimesigned, req).trim());
		if (getdattimesigned().compareToIgnoreCase("") == 0){
			setdattimesigned(EMPTY_DATE_STRING);
		}
		
		setstrimmedordernumber(clsManageRequestParameters.get_Request_Parameter(Paramstrimmedordernumber, req).trim());

		setssignedbyname(clsManageRequestParameters.get_Request_Parameter(Paramssignedbyname, req).trim().replace("&quot;", "\""));

		setmsignature(clsServletUtilities.URLDecode(clsManageRequestParameters.get_Request_Parameter(Parammsignature, req).trim()));
		setmcomments(clsManageRequestParameters.get_Request_Parameter(Parammcomments, req).trim().replace("&quot;", "\""));
		setmdetailsheettext(clsManageRequestParameters.get_Request_Parameter(Parammdetailsheettext, req).trim().replace("&quot;", "\""));
		//System.out.println("[1402432734] - SMUtilities.get_Request_Parameter(Parammdetailsheettext, req).trim().replace(&quot;) = " + SMUtilities.get_Request_Parameter(Parammdetailsheettext, req).trim().replace("&quot;", "\""));
		setmadditionalworkcomments(clsManageRequestParameters.get_Request_Parameter(Parammadditionalworkcomments, req).trim().replace("&quot;", "\""));
		//System.out.println("[1402432735] - get_mdetailsheettext() = " + get_mdetailsheettext());
		//System.out.println("[1429730373] req.getParameter(Paramiadditionalworkauthorized) = " + req.getParameter(Paramiadditionalworkauthorized));
		if (req.getParameter(Paramiadditionalworkauthorized) == null){
			setsadditionalworkauthorized("0");
		}else{
			setsadditionalworkauthorized("1");
		}

		setsimported(clsManageRequestParameters.get_Request_Parameter(Paramiimported, req).trim());
		if (getsimported().compareToIgnoreCase("") == 0){
			setsimported("0");
		}
		setsposted(clsManageRequestParameters.get_Request_Parameter(Paramiposted, req).trim());
		if (getsposted().compareToIgnoreCase("") == 0){
			setsposted("0");
		}
		setstimestamp(clsManageRequestParameters.get_Request_Parameter(Paramltimestamp, req).trim());
		if (getstimestamp().compareToIgnoreCase("") == 0){
			setstimestamp("0");
		}
		
		//Get the last time the user refreshed the work order:
		setslastreadrecordtimestamp(clsManageRequestParameters.get_Request_Parameter(Paramlastreadrecordtimestamp, req).trim());
		if (getslastreadrecordtimestamp().compareToIgnoreCase("") == 0){
			setslastreadrecordtimestamp("0");
		}
		
		setminstructions(clsManageRequestParameters.get_Request_Parameter(Paramminstructions, req).trim().replace("&quot;", "\""));
		
		setmmanagernotes(clsManageRequestParameters.get_Request_Parameter(Parammmanagernotes, req).trim().replace("&quot;", "\""));
		
		setsdattimelastschedulechange(clsManageRequestParameters.get_Request_Parameter(Paramdattimelastschedulechange, req).trim());
		if (this.getsdattimelastschedulechange().compareToIgnoreCase("") == 0){
			setsdattimelastschedulechange(EMPTY_DATETIME_STRING);
		}
		
		setsschedulechangedbyfullname(clsManageRequestParameters.get_Request_Parameter(Paramssschedulechangedbyfullname, req).trim().replace("&quot;", "\""));
		
		//Read the job times:
		if (clsManageRequestParameters.get_Request_Parameter(
				Paramhasdattimeleftprevious, req).trim().compareToIgnoreCase("1") == 0){

			m_sdattimeleftprevious = clsManageRequestParameters.get_Request_Parameter(
					Paramdattimeleftprevious, req).trim()
					+ " "
					+ clsManageRequestParameters.get_Request_Parameter(
							Paramdattimeleftprevious + "SelectedHour", req).trim()
							+ ":" 
							+ clsManageRequestParameters.get_Request_Parameter(
									Paramdattimeleftprevious + "SelectedMinute", req).trim()
									+ " "
									;

			if (clsManageRequestParameters.get_Request_Parameter(
					Paramdattimeleftprevious + "SelectedAMPM", req).trim().compareToIgnoreCase("1") == 0){
				m_sdattimeleftprevious = m_sdattimeleftprevious + "PM";
			}else{
				m_sdattimeleftprevious = m_sdattimeleftprevious + "AM";
			}
		}else{
				m_sdattimeleftprevious = EMPTY_DATETIME_STRING;
		}
		if (clsManageRequestParameters.get_Request_Parameter(
				Paramhasdattimearrivedatcurrent, req).trim().compareToIgnoreCase("1") == 0){

			m_sdattimearrivedatcurrent = clsManageRequestParameters.get_Request_Parameter(
					Paramdattimearrivedatcurrent, req).trim()
					+ " "
					+ clsManageRequestParameters.get_Request_Parameter(
							Paramdattimearrivedatcurrent + "SelectedHour", req).trim()
							+ ":" 
							+ clsManageRequestParameters.get_Request_Parameter(
									Paramdattimearrivedatcurrent + "SelectedMinute", req).trim()
									+ " "
									;

			if (clsManageRequestParameters.get_Request_Parameter(
					Paramdattimearrivedatcurrent + "SelectedAMPM", req).trim().compareToIgnoreCase("1") == 0){
				m_sdattimearrivedatcurrent = m_sdattimearrivedatcurrent + "PM";
			}else{
				m_sdattimearrivedatcurrent = m_sdattimearrivedatcurrent + "AM";
			}
		}else{
			m_sdattimearrivedatcurrent = EMPTY_DATETIME_STRING;
		}
		if (clsManageRequestParameters.get_Request_Parameter(
				Paramhasdattimeleftcurrent, req).trim().compareToIgnoreCase("1") == 0){

			m_sdattimeleftcurrent = clsManageRequestParameters.get_Request_Parameter(
					Paramdattimeleftcurrent, req).trim()
					+ " "
					+ clsManageRequestParameters.get_Request_Parameter(
							Paramdattimeleftcurrent + "SelectedHour", req).trim()
							+ ":" 
							+ clsManageRequestParameters.get_Request_Parameter(
									Paramdattimeleftcurrent + "SelectedMinute", req).trim()
									+ " "
									;

			if (clsManageRequestParameters.get_Request_Parameter(
					Paramdattimeleftcurrent + "SelectedAMPM", req).trim().compareToIgnoreCase("1") == 0){
				m_sdattimeleftcurrent = m_sdattimeleftcurrent + "PM";
			}else{
				m_sdattimeleftcurrent = m_sdattimeleftcurrent + "AM";
			}
		}else{
			m_sdattimeleftcurrent = EMPTY_DATETIME_STRING;
		}
		if (clsManageRequestParameters.get_Request_Parameter(
				Paramhasdattimearrivedatnext, req).trim().compareToIgnoreCase("1") == 0){

			m_sdattimearrivedatnext = clsManageRequestParameters.get_Request_Parameter(
					Paramdattimearrivedatnext, req).trim()
					+ " "
					+ clsManageRequestParameters.get_Request_Parameter(
							Paramdattimearrivedatnext + "SelectedHour", req).trim()
							+ ":" 
							+ clsManageRequestParameters.get_Request_Parameter(
									Paramdattimearrivedatnext + "SelectedMinute", req).trim()
									+ " "
									;

			if (clsManageRequestParameters.get_Request_Parameter(
					Paramdattimearrivedatnext + "SelectedAMPM", req).trim().compareToIgnoreCase("1") == 0){
				m_sdattimearrivedatnext = m_sdattimearrivedatnext + "PM";
			}else{
				m_sdattimearrivedatnext = m_sdattimearrivedatnext + "AM";
			}
		}else{
			m_sdattimearrivedatnext = EMPTY_DATETIME_STRING;
		}
		
		m_sassistant = clsManageRequestParameters.get_Request_Parameter(Paramsassistant, req).trim();
		m_sstartingtime = clsManageRequestParameters.get_Request_Parameter(Paramsstartingtime, req).trim();
		m_sscheduleddate = clsManageRequestParameters.get_Request_Parameter(Paramscheduleddate, req).trim();
		String sTest = clsManageRequestParameters.get_Request_Parameter(Paramqtyofhours, req).trim();
		if (sTest.compareToIgnoreCase("") != 0){
			m_sqtyofhours = sTest;
		}
		m_sworkdescription = clsManageRequestParameters.get_Request_Parameter(Paramworkdescription, req).trim();
		sTest = clsManageRequestParameters.get_Request_Parameter(Paramtravelhours, req).trim();
		if (sTest.compareToIgnoreCase("") != 0){
			m_stravelhours = sTest;
		}
		sTest = clsManageRequestParameters.get_Request_Parameter(Parambackchargehours, req).trim();
		if (sTest.compareToIgnoreCase("") != 0){
			m_sbackchargehours = sTest;
		}
		m_slasteditedbyfullname = clsManageRequestParameters.get_Request_Parameter(Paramlasteditedbyfullname, req).trim();
		m_sdatetimelastedit = clsManageRequestParameters.get_Request_Parameter(Paramdatetimelastedit, req).trim();
		m_ijoborder = clsManageRequestParameters.get_Request_Parameter(Paramijoborder, req).trim();
		m_sschedulecomment = clsManageRequestParameters.get_Request_Parameter(Paramsschedulecomment, req).trim();
		m_sgdoclink = clsManageRequestParameters.get_Request_Parameter(Paramssgdoclink, req).trim();
		m_lsignatureboxwidth = clsManageRequestParameters.get_Request_Parameter(Paramlsignaturboxwidth, req).trim();
		m_sdbaworkorderlogo = clsManageRequestParameters.get_Request_Parameter(Paramsdbaworkorderlogo, req).trim();
		//Load the work order details from the request:
		//Get the number of item lines used:
		int iNumberOfItemLines;
		String sPaddedLineNumber = clsManageRequestParameters.get_Request_Parameter(SMWorkOrderHeader.NUMBER_OF_ITEM_LINES_USED, req);
		if (sPaddedLineNumber.compareToIgnoreCase("") == 0){
			sPaddedLineNumber = "0";
		}
		try {
			iNumberOfItemLines = Integer.parseInt(sPaddedLineNumber);
		} catch (Exception e) {
			throw new Exception("Error [1391791196] parsing number of item lines used - value '" 
				+ clsManageRequestParameters.get_Request_Parameter(SMWorkOrderHeader.NUMBER_OF_ITEM_LINES_USED, req) 
				+ "' is not valid -  " + e.getMessage());
		}
		
		//NOTE: the line numbers are coming in one-based, not zero based:
		for (int i = 1; i <= iNumberOfItemLines; i++){
			//Read the data from each work order line:
			SMWorkOrderDetail detail = new SMWorkOrderDetail();
			//Qty used:
			detail.setsbdquantity(clsManageRequestParameters.get_Request_Parameter(
				SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(i), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Parambdquantity,
				req).replace("&quot;", "\""));
			
			//Just to be double sure (because we had an error on this), remove any commas:
			detail.setsbdquantity(detail.getsbdquantity().replaceAll(",", ""));
			
			BigDecimal bdQty = new BigDecimal("0.0000");
			if (detail.getsbdquantity().compareToIgnoreCase("") != 0 ){
				try {
					bdQty = new BigDecimal(detail.getsbdquantity().replace(",",""));
				} catch (Exception e) {
					//Don't do anything here, the bdQty will just remain at zero, which is OK
				}
			}else{
				//If it's blank, then set it to zero:
				detail.setsbdquantity(clsManageBigDecimals.BigDecimalToScaledFormattedString(
				    SMTableworkorderdetails.bdquantityDecimals, BigDecimal.ZERO));
			}
			//Qty assigned:
			detail.setsbdqtyassigned(clsManageRequestParameters.get_Request_Parameter(
				SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(i), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Parambdqtyassigned,
				req).replace("&quot;", "\""));

			BigDecimal bdQtyAssigned = new BigDecimal("0.0000");
			if (detail.getsbdqtyassigned().compareToIgnoreCase("") != 0 ){
				try {
					bdQtyAssigned = new BigDecimal(detail.getsbdqtyassigned().replace(",",""));
				} catch (Exception e) {
					//Make sure the bdQty will just remain at zero, which is OK
					bdQtyAssigned = new BigDecimal("0.0000");
				}
			}

			String sItemNumber = clsManageRequestParameters.get_Request_Parameter(
					SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
					+ clsStringFunctions.PadLeft(Integer.toString(i), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
					+  SMWorkOrderDetail.Paramsitemnumber,
					req).replace("&quot;", "\""); 
			//Process this detail if it has EITHER a qty used OR an assigned qty:
			if (
				(bdQty.compareTo(BigDecimal.ZERO) != 0)
				|| (bdQtyAssigned.compareTo(BigDecimal.ZERO) != 0)
				//|| (sItemNumber.compareToIgnoreCase("") !=0)
					
			){
				detail.setsdetailtype(clsManageRequestParameters.get_Request_Parameter(
					SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
					+ clsStringFunctions.PadLeft(Integer.toString(i), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
					+  SMWorkOrderDetail.Paramidetailtype,
					req));

				detail.setslid(clsManageRequestParameters.get_Request_Parameter(
						SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
						+ clsStringFunctions.PadLeft(Integer.toString(i), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
						+  SMWorkOrderDetail.Paramlid,
						req));
				//These get renumbered at the end:
				detail.setslinenumber("-1");

				detail.setsorderdetailnumber(clsManageRequestParameters.get_Request_Parameter(
						SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
						+ clsStringFunctions.PadLeft(Integer.toString(i), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
						+  SMWorkOrderDetail.Paramlorderdetailnumber,
						req));

				detail.setsworkperformedlinenumber(clsManageRequestParameters.get_Request_Parameter(
						SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
						+ clsStringFunctions.PadLeft(Integer.toString(i), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
						+  SMWorkOrderDetail.Paramlworkperformedlinenumber,
						req));

				detail.setsitemdesc(clsManageRequestParameters.get_Request_Parameter(
						SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
						+ clsStringFunctions.PadLeft(Integer.toString(i), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
						+  SMWorkOrderDetail.Paramsitemdesc,
						req).replace("&quot;", "\""));

				detail.setsitemnumber(sItemNumber);

				detail.setsuom(clsManageRequestParameters.get_Request_Parameter(
						SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
						+ clsStringFunctions.PadLeft(Integer.toString(i), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
						+  SMWorkOrderDetail.Paramsunitofmeasure,
						req).replace("&quot;", "\""));

				detail.setsworkperformed(clsManageRequestParameters.get_Request_Parameter(
						SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
						+ clsStringFunctions.PadLeft(Integer.toString(i), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
						+  SMWorkOrderDetail.Paramsworkperformed,
						req).replace("&quot;", "\""));
				
				detail.setsbdextendedprice(clsManageRequestParameters.get_Request_Parameter(
						SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
						+ clsStringFunctions.PadLeft(Integer.toString(i), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
						+  SMWorkOrderDetail.Parambdextendedprice,
						req).replace("&quot;", "\""));
				
				detail.setslocationcode(clsManageRequestParameters.get_Request_Parameter(
						SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
						+ clsStringFunctions.PadLeft(Integer.toString(i), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
						+  SMWorkOrderDetail.Paramslocationcode,
						req).replace("&quot;", "\""));
				
				//Set price to zero:
				if (req.getParameter(SMWorkOrderHeader.SET_TO_ZERO_CHECKBOX_MARKER 
						+ clsStringFunctions.PadLeft(Integer.toString(i), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
						+  SMWorkOrderDetail.Paramllsetpricetozero) == null){
					detail.setssetpricetozero("0");
				}else{
					detail.setssetpricetozero("1");
				}

				try {
					add_line(detail);
				} catch (Exception e) {
					throw new Exception("Error [1391805877] - " + e.getMessage() + ".");
				}
			}
			//System.out.println("[1447960558] - getDetailCount() = " + getDetailCount());
			//System.out.println("[1398350519] " + System.currentTimeMillis() + " Line number " + i + " readout after reading from request: "
			//		+ detail.read_out_debug_data()
			//		);
		}
		
		//Load the work performed codes from the request:
		//Get the number of WPC lines used:
		sPaddedLineNumber = clsManageRequestParameters.get_Request_Parameter(SMWorkOrderHeader.TOTAL_NUMBER_OF_WPC_CODES, req);
		if (sPaddedLineNumber.compareToIgnoreCase("") == 0){
			sPaddedLineNumber = "0";
		}
		int iNumberOfWPCLines = 0;
		try {
			iNumberOfWPCLines = Integer.parseInt(sPaddedLineNumber);
		} catch (Exception e) {
			throw new Exception("Error [1392062280] parsing number of work performed lines used - value '" 
				+ clsManageRequestParameters.get_Request_Parameter(SMWorkOrderHeader.TOTAL_NUMBER_OF_WPC_CODES, req) 
				+ "' is not valid -  " + e.getMessage());
		}
		//NOTE: Work performed codes are coming in one-based, not zero-based:
		for (int i = 1; i <= iNumberOfWPCLines; i++){
			//If the WPC was checked:
			if (req.getParameter(SMWorkOrderHeader.WORK_ORDER_WORKPERFORMEDCODE_MARKER
					+ clsStringFunctions.PadLeft(Integer.toString(i), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER)) != null){
				SMWorkOrderDetail detail = new SMWorkOrderDetail();
				detail.setsbdquantity("0.00");
				detail.setsbdqtyassigned("0.00");
				detail.setsbdunitprice("0.00");
				detail.setsdetailtype(Integer.toString(SMTableworkorderdetails.WORK_ORDER_DETAIL_TYPE_WORKPERFORMED));
				detail.setsitemdesc("");
				detail.setsitemnumber("");;
				//These get renumbered at the end:
				detail.setslinenumber("-1");
				detail.setsorderdetailnumber("-1");
				detail.setsuom("");
				detail.setsworkperformed(clsManageRequestParameters.get_Request_Parameter(
					SMWorkOrderHeader.WORK_ORDER_WORKPERFORMEDCODE_MARKER
					+ clsStringFunctions.PadLeft(Integer.toString(i), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
					+ SMWorkOrderDetail.Paramsworkperformed, 
					req).replace("&quot;", "\"")
				);
				detail.setsworkperformedcode(clsManageRequestParameters.get_Request_Parameter(
					SMWorkOrderHeader.WORK_ORDER_WORKPERFORMEDCODE_MARKER
					+ clsStringFunctions.PadLeft(Integer.toString(i), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
					+ SMWorkOrderDetail.Paramsworkperformedcode, 
					req).replace("&quot;", "\"")
				);
				try {
					add_line(detail);
				} catch (Exception e) {
					throw new Exception("Error [1392062281] - " + e.getMessage() + ".");
				}
			}
		}
		renumberLines();
	}

	/*
	public void loadDefaultLocationToDetailLines(HttpServletRequest req,  
								ServletContext context, 
								String sDBID, 
								String sUser) throws Exception{
	//This function must be used after the work order object is loaded
	//in order to get the default order location on the work order detail.
	for(int i = 0; i < this.getDetailCount(); i++){
		if(this.getDetailByIndex(i).getslocationcode().compareToIgnoreCase("") == 0){
			if(this.getstrimmedordernumber().compareToIgnoreCase("") != 0){
				SMOrderHeader order = new SMOrderHeader();
				order.setM_strimmedordernumber(this.getstrimmedordernumber());
				order.load(context, sDBID, sUser);
				this.getDetailByIndex(i).setslocationcode(order.getM_sLocation());
			}//else there is no default location for this detail line
		//Load location from the request
		}else{
			this.getDetailByIndex(i).setslocationcode(SMUtilities.get_Request_Parameter(
				SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ SMUtilities.PadLeft(Integer.toString(i), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Paramslocationcode,
				req).replace("&quot;", "\""));
			if(this.getDetailByIndex(i).getslocationcode() == null){
				this.getDetailByIndex(i).setslocationcode("");
			}			
		}
	}
}
*/
	public void saveFromConfigure(
		ServletContext context, 
		String sDBIB, 
		String sUserID,
		String sUserFullName,
		int iSavingFromWhichScreen
		) throws Exception{
		//System.out.println("[001] qty assigned = " + this.getDetailByIndex(0).getsbdqtyassigned());
		
		SMLogEntry log = new SMLogEntry(sDBIB, context);
		Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				context, 
				sDBIB, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".save [1438863739] - user: " + sUserID + " - " + sUserFullName);
		} catch (Exception e) {
			throw new Exception("Error [1391541096] getting connection - " + e.getMessage());
		}
		if(!clsDatabaseFunctions.start_data_transaction(conn)){
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067900]");
			throw new Exception ("Error [1391541097] - could not start data transaction.");
		}
		try {
			save_from_configure_without_data_transaction(conn, sUserID, sUserFullName, log, iSavingFromWhichScreen, sDBIB, context);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067901]");
			throw new Exception("Error saving work order - " + e.getMessage());
		}
		if(!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067902]");
			throw new Exception("Error [1391541099] committing transaction.");
		}
		clsDatabaseFunctions.freeConnection(context, conn, "[1547067903]");
		return;
	}

	private void saveDetailSheet(
			ServletContext context, 
			String sDBIB, 
			String sUserID,
			String sUserFullName,
			int iSavingFromWhichScreen
			) throws Exception{
			
			SMLogEntry log = new SMLogEntry(sDBIB, context);
			Connection conn = null;
			try {
				conn = clsDatabaseFunctions.getConnectionWithException(
					context, 
					sDBIB, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString()) + ".save [1438863740] - user: " + sUserID + " - " + sUserFullName);
			} catch (Exception e) {
				throw new Exception("Error [1430410108] getting connection - " + e.getMessage());
			}
			if(!clsDatabaseFunctions.start_data_transaction(conn)){
				clsDatabaseFunctions.freeConnection(context, conn, "[1547067794]");
				throw new Exception ("Error [1430410109] - could not start data transaction.");
			}
			try {
				save_detail_sheet_without_data_transaction(conn, sUserID, sUserFullName, log, context);
			} catch (Exception e) {
				clsDatabaseFunctions.rollback_data_transaction(conn);
				clsDatabaseFunctions.freeConnection(context, conn, "[1547067795]");
				throw new Exception("Error [1430410110] saving work order - " + e.getMessage());
			}
			if(!clsDatabaseFunctions.commit_data_transaction(conn)){
				clsDatabaseFunctions.rollback_data_transaction(conn);
				clsDatabaseFunctions.freeConnection(context, conn, "[1547067796]");
				throw new Exception("Error [1430410111] committing transaction.");
			}
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067797]");
			return;
		}
	public void saveFromAcceptanceScreen(
			ServletContext context, 
			String sDBIB, 
			String sUserID,
			String sUserFullName
			) throws Exception{
			
			SMLogEntry log = new SMLogEntry(sDBIB, context);
			Connection conn = null;
			try {
				conn = clsDatabaseFunctions.getConnectionWithException(
					context, 
					sDBIB, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString()) + ".save [1438863741] - user: " + sUserID + " - " + sUserFullName);
			} catch (Exception e) {
				throw new Exception("Error [1430331067] getting connection - " + e.getMessage());
			}
			if(!clsDatabaseFunctions.start_data_transaction(conn)){
				clsDatabaseFunctions.freeConnection(context, conn, "[1547067798]");
				throw new Exception ("Error [1430331068] - could not start data transaction.");
			}
			try {
				save_acceptance_screen_without_data_transaction(conn, sUserID, sUserFullName, log, sDBIB, context);
			} catch (Exception e) {
				clsDatabaseFunctions.rollback_data_transaction(conn);
				clsDatabaseFunctions.freeConnection(context, conn, "[1547067799]");
				throw new Exception("Error saving work order - " + e.getMessage());
			}
			if(!clsDatabaseFunctions.commit_data_transaction(conn)){
				clsDatabaseFunctions.rollback_data_transaction(conn);
				clsDatabaseFunctions.freeConnection(context, conn, "[1547067800]");
				throw new Exception("Error [1430331069] committing transaction.");
			}
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067801]");
			return;
		}
	public void saveFromEditScreen(
			ServletContext context, 
			String sDBIB, 
			String sUserID,
			String sUserFullName
			) throws Exception{
			
			SMLogEntry log = new SMLogEntry(sDBIB, context);
			Connection conn = null;
			try {
				conn = clsDatabaseFunctions.getConnectionWithException(
					context, 
					sDBIB, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString()) + ".save [1438863742] - user: " + sUserID + " - " + sUserFullName);
			} catch (Exception e) {
				throw new Exception("Error [1430407358] getting connection - " + e.getMessage());
			}
			if(!clsDatabaseFunctions.start_data_transaction(conn)){
				clsDatabaseFunctions.freeConnection(context, conn, "[1547067904]");
				throw new Exception ("Error [1430407359] - could not start data transaction.");
			}
			try {
				save_edit_screen_without_data_transaction(conn, sUserID, sUserFullName, log, sDBIB, context);
			} catch (Exception e) {
				clsDatabaseFunctions.rollback_data_transaction(conn);
				clsDatabaseFunctions.freeConnection(context, conn, "[1547067905]");
				throw new Exception("Error [1430407360] saving work order - " + e.getMessage());
			}
			if(!clsDatabaseFunctions.commit_data_transaction(conn)){
				clsDatabaseFunctions.rollback_data_transaction(conn);
				clsDatabaseFunctions.freeConnection(context, conn, "[1547067906]");
				throw new Exception("Error [1430407361] committing transaction.");
			}
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067907]");
			return;
		}
	private void save_from_configure_without_data_transaction (
			Connection conn, 
			String sUserID,
			String sUserFullName,
			SMLogEntry log,
			int iSavingFromWhichScreen,
			String sDBID,
			ServletContext context) throws Exception{
		
		if (WebContextParameters.getLogWorkOrderUpdates(context).compareToIgnoreCase("True") == 0){
			log.writeEntry(
				sUserID, 
				SMLogEntry.LOG_OPERATION_SMWORKORDERSAVEDEBUGGING, 
				Integer.toString(iSavingFromWhichScreen) + " WOID :" + getlid() + ", isWorkOrderPosted() = " + isWorkOrderPosted(),
				"Before validate_configure_screen_fields",
				"[1429125172]")
			;
		}
    	try{
    		validate_configure_screen_fields(conn, sUserID, sUserFullName, sDBID, context);
    	}catch (Exception ex){
    		throw new Exception("Error validating " + SMTableworkorders.ObjectName + " - " + ex.getMessage());
    	}

    	if (WebContextParameters.getLogWorkOrderUpdates(context).compareToIgnoreCase("True") == 0){
			log.writeEntry(
					sUserID, 
					SMLogEntry.LOG_OPERATION_SMWORKORDERSAVEDEBUGGING, 
					Integer.toString(iSavingFromWhichScreen) + " WOID :" + getlid() + ", isWorkOrderPosted() = " + isWorkOrderPosted(),
					"After validate_configure_screen_fields",
					"[1429125173]")
				;
    	}
    	//If we are updating from the 'Configure' screen or the 'Edit Handler', we'll need to manage some job sequencing:
    	//If the ID is -1, then it's a new record - this field has been validated so there's no chance that it could NOT be a number:
    	boolean bIsNewRecord = getlid().compareToIgnoreCase("-1") == 0;

    	//Record whether the job sequence on this work order has changed (only if it's an existing work order):
    	boolean bScheduleSequenceHasChanged = false;
    	//Record whether the schedule date or mechanic has changed, because if so it may affect other schedule entries:
    	boolean bDateOrMechanicHasChanged = false;
    	String sPreviousMechanicID = "";
    	String sPreviousScheduledDate = "";
    	if (!bIsNewRecord){
    		SMWorkOrderHeader current_wo = new SMWorkOrderHeader();
    		current_wo.setlid(getlid());
    		try {
				if (!current_wo.load(conn)){
					throw new Exception("Error [1426531820] loading current work order with ID: " + getlid() + " - " 
						+ current_wo.getErrorMessages());
				}
			} catch (Exception e) {
				throw new Exception("Error [1426531821] loading current work order with ID: " + getlid() + " - " 
					+ e.getMessage() + ".");
			}
    		bScheduleSequenceHasChanged = getsjoborder().compareToIgnoreCase(current_wo.getsjoborder()) != 0;
    		bDateOrMechanicHasChanged = (getsscheduleddate().compareToIgnoreCase(current_wo.getsscheduleddate()) != 0)
    			|| (getmechid().compareToIgnoreCase(current_wo.getmechid()) != 0);
    		sPreviousScheduledDate = current_wo.getsscheduleddate();
    		sPreviousMechanicID = current_wo.getmechid();
    		//If neither the sequence nor the mechanic nor the date has changed we can do a simple update of the record:
    		if (
    			(!bScheduleSequenceHasChanged) && (!bDateOrMechanicHasChanged)
    		){
        		updateFromConfigureWorkOrderWithNoScheduleChange(conn, sUserID, sUserFullName, isWorkOrderPosted(), log, iSavingFromWhichScreen, context);
        		return;
    		}else{
    			//If either the schedule date OR the mechanic is being changed, then we make sure
    			// that this work order isn't POSTED:
    			if (isWorkOrderPosted()){
    				throw new Exception("Error [1430490119] - you cannot re-schedule a POSTED work order.");
    			}
    		}
    	}
    	//But if either the job sequence or the schedule or the mechanic has changed
    	//we'll need to do some re-sequencing to get all our schedule entries in order:
    	//First we load ALL the work orders for this same day and mechanic into an array, including this one:
    	String SQL = "SELECT"
    		+ " " + SMTableworkorders.lid
    		+ ", " + SMTableworkorders.ijoborder
    		+ " FROM " + SMTableworkorders.TableName
    		+ " WHERE ("
    			+ "(" + SMTableworkorders.datscheduleddate + " = '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(getsscheduleddate()) + "')"
    			+ " AND (" + SMTableworkorders.imechid + " = " + getmechid() + ")"
    		+ ")"
    		+ " ORDER BY " + SMTableworkorders.ijoborder + ", " + SMTableworkorders.lid
    		;
    	ArrayList<SMWorkOrderHeader> arrWorkOrders = new ArrayList<SMWorkOrderHeader>(0);
    	try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()){
				SMWorkOrderHeader entry = new SMWorkOrderHeader();
				long lWorkOrderID = rs.getLong(SMTableworkorders.lid);
				entry.setlid(Long.toString(lWorkOrderID));
				if (!entry.load(conn)){
					rs.close();
					throw new Exception("Error [1426606257] loading Work Order ID: " + rs.getLong(SMTableworkorders.lid) 
						+ entry.getErrorMessages() +  ".");
				}
				//If THIS CURRENT work order belongs in the array here, we insert it:
				//This is how we get the correct job sequence in each record:
				//System.out.println("TEMPDEBUG [1427131682] 1 - iRecordCounter = " + iRecordCounter + " arrWorkOrders.size() = " + arrWorkOrders.size() + " - m_ijoborder = " + m_ijoborder);
				if (m_ijoborder.compareToIgnoreCase(Integer.toString((arrWorkOrders.size() + 1))) == 0){
					//System.out.println("TEMPDEBUG [1427131683] 2 - iRecordCounter = " + iRecordCounter + " Adding this current work order."); 
					arrWorkOrders.add(this);
					arrWorkOrders.get(arrWorkOrders.size() - 1).setsjoborder(Integer.toString(arrWorkOrders.size()));
					//System.out.println("TEMPDEBUG [1427131686] 3 - iRecordCounter + " + iRecordCounter + " job sequence = " + arrWorkOrders.get(arrWorkOrders.size() - 1).getsjoborder());
					arrWorkOrders.get(arrWorkOrders.size() - 1).setslasteditedbyfullname(sUserFullName);
				}
				
				//If we come across a record that matches the ID of THIS work order entry, we can 
				//just ignore it, because we are going to update it with THIS entry:
				if (getlid().compareToIgnoreCase(Long.toString(lWorkOrderID)) == 0){
					//System.out.println("TEMPDEBUG [1427131684] 4 - iRecordCounter - " + iRecordCounter + " ID is for current work order, not adding it.");
				}else{
					//System.out.println("TEMPDEBUG [1427131685] 5 - iRecordCounter + " + iRecordCounter + " Adding work order from the resultset.");
					arrWorkOrders.add(entry);
					arrWorkOrders.get(arrWorkOrders.size() - 1).setsjoborder(Integer.toString(arrWorkOrders.size()));
				}
			}
			rs.close();
		} catch (SQLException e) {
			throw new Exception("Error [1426606258] updating job sequences with SQL: " + SQL + " - " + e.getMessage());
		}
    	
    	//System.out.println("TEMPDEBUG [1427131687] 6  - arrWorkOrders.size() = " + arrWorkOrders.size());
		//If we haven't added THIS job cost entry (because it has a higher sequence than any of the
		//existing records) add it now:
		if (Integer.parseInt(getsjoborder()) > arrWorkOrders.size()){
			arrWorkOrders.add(this);
			arrWorkOrders.get(arrWorkOrders.size() - 1).setsjoborder(Integer.toString(arrWorkOrders.size()));
			arrWorkOrders.get(arrWorkOrders.size() - 1).setslasteditedbyfullname(sUserFullName);
		}
		//System.out.println("TEMPDEBUG [1427131688] 7  - arrWorkOrders.size() = " + arrWorkOrders.size());
		//Now the array should contain ALL the work order entries for this mechanic and this day, and we
		//should be able to just UPDATE all those records with the correct values:
		for (int i = 0; i < arrWorkOrders.size(); i++){
			//System.out.println("TEMPDEBUG [1427131689] 8  - i = " + i + " arrWorkOrders.get(i).getlid() = " + arrWorkOrders.get(i).getlid());
			if (arrWorkOrders.get(i).getlid().compareToIgnoreCase("-1") == 0){
				//System.out.println("TEMPDEBUG [1427131690] 9  - i = " + i + " going to insert work order");
				insertWorkOrder(arrWorkOrders.get(i), conn, sUserID, sUserFullName, log, iSavingFromWhichScreen, context);
			}else{
				//If we are updating THIS work order, we'll update several fields:
				if (arrWorkOrders.get(i).getlid().compareToIgnoreCase(getlid()) == 0){
					//System.out.println("TEMPDEBUG [1427131691] 10  - i = " + i + " going to updateFromConfigureWithScheduleChange");
					updateFromConfigureWithScheduleChange(
						arrWorkOrders.get(i), 
						conn, 
						sUserID, 
						sUserFullName, 
						log, 
						iSavingFromWhichScreen,
						bScheduleSequenceHasChanged,
						bDateOrMechanicHasChanged,
						context);
				//But if we are updating some OTHER work order, we only need to update a few fields:
				}else{
					//System.out.println("TEMPDEBUG [1427131692] 11  - i = " + i + " going to updateOtherAffectedWorkOrder");
					updateOtherAffectedWorkOrder(arrWorkOrders.get(i), conn, sUserID, sUserFullName, log, iSavingFromWhichScreen, context);
				}
			}
		}
		
		//Removed by BJZ on 12/11/17 in order to reduce number of geocode requests
		//Update the geocodes on the orders:
		//try {
		//	updateGeocodes(arrWorkOrders, conn, sUser);
		//} catch (Exception e) {
		//	throw new Exception("Error [1426628927] updating geocodes - " + e.getMessage());
		//}
	
		//NOW - if the date or mechanic on this record was updated, we'll have to make sure we update
		//the job order on the jobs that are still on that PREVIOUS day and mechanic.
		//And we'll have to update any unposted work orders as well (we've already checked in the validate function that this isn't a POSTED work order):
    	if (bDateOrMechanicHasChanged){
    		try {
				updateJobSequencesByDayAndMechanic(
					sPreviousMechanicID, 
					sPreviousScheduledDate, 
					conn, 
					sUserFullName,
					sUserID,
					log, 
					iSavingFromWhichScreen, 
					context);
			} catch (Exception e) {
				throw new Exception(e.getMessage());
			}
		}
    	return;
    }

	private void save_detail_sheet_without_data_transaction (
			Connection conn, 
			String sUserID,
			String sUseFullName,
			SMLogEntry log,
			ServletContext context) throws Exception{

		String SQL = " UPDATE " + SMTableworkorders.TableName + " SET"
	    	+ " " + SMTableworkorders.ltimestamp + " = UNIX_TIMESTAMP()"
	    	+ ", " + SMTableworkorders.slasteditedbyfullname + " = '" + clsDatabaseFunctions.FormatSQLStatement(sUseFullName) + "'"
	    	+ ", " + SMTableworkorders.llasteditedbyuserid + " = " + sUserID
	    	+ ", " + SMTableworkorders.mdetailsheettext + " = '" + clsDatabaseFunctions.FormatSQLStatement(getmdetailsheettext()) + "'"
			+ " WHERE (" 
				+ SMTableworkorders.lid + " = " + getlid() 
			+ ")";

			try {
				Statement stmt = conn.createStatement();
				stmt.execute(SQL);
			} catch (Exception e) {
				if (WebContextParameters.getLogWorkOrderUpdates(context).compareToIgnoreCase("True") == 0){
					log.writeEntry(
						sUserID, 
						SMLogEntry.LOG_OPERATION_SMWORKORDERSAVEDEBUGGING, 
						"ADD DETAIL SHEET WOID :" + getlid() + ", isWorkOrderPosted() = " + isWorkOrderPosted(),
						"In save_detail_sheet_without_data_transaction EXCEPTION CAUGHT - SQL: " + SQL + " - " + e.getMessage(),
						"[1430407563]")
					;
				}
				throw new Exception("Error [1430410296] adding detail sheet with SQL: " + SQL + " - " + e.getMessage() + ".");
			}
			if (WebContextParameters.getLogWorkOrderUpdates(context).compareToIgnoreCase("True") == 0){
				log.writeEntry(
						sUserID, 
					SMLogEntry.LOG_OPERATION_SMWORKORDERSAVEDEBUGGING, 
					"ADD DETAIL SHEET WOID :" + getlid() + ", isWorkOrderPosted() = " + isWorkOrderPosted(),
					"In save_detail_sheet_without_data_transaction - SQL: " + SQL,
					"[1430410298]")
				;
			}
    	return;
    }
	private void save_edit_screen_without_data_transaction (
			Connection conn, 
			String sUserID,
			String sUserFullName,
			SMLogEntry log,
			String sDBID,
			ServletContext context) throws Exception{
		if (WebContextParameters.getLogWorkOrderUpdates(context).compareToIgnoreCase("True") == 0){
			log.writeEntry(
				sUserID, 
				SMLogEntry.LOG_OPERATION_SMWORKORDERSAVEDEBUGGING, 
				"EDIT SCREEN WOID :" + getlid() 
				+ ", isWorkOrderPosted() = " + isWorkOrderPosted(),
				"Before validate_edit_screen_fields",
				"[1430407561]")
			;
		}
    	try{
    		validate_edit_screen_fields(conn, sUserID, sUserFullName, sDBID, context);
    	}catch (Exception ex){
    		throw new Exception("Error validating " + SMTableworkorders.ObjectName + " - " + ex.getMessage());
    	}
    	if (WebContextParameters.getLogWorkOrderUpdates(context).compareToIgnoreCase("True") == 0){
			log.writeEntry(
				sUserID, 
				SMLogEntry.LOG_OPERATION_SMWORKORDERSAVEDEBUGGING, 
				"EDIT SCREEN WOID :" + getlid() + ", isWorkOrderPosted() = " + isWorkOrderPosted(),
				"After validate_edit_screen_fields",
				"[1430407562]")
			;
    	}
    	String SQL = "";
		//These fields always get posted after 'Edit work order':
    	if (!isWorkOrderPosted()){
    		SQL = " UPDATE " + SMTableworkorders.TableName + " SET"
	    	+ " " + SMTableworkorders.ltimestamp + " = UNIX_TIMESTAMP()"
	    	+ ", " + SMTableworkorders.dattimeleftprevious + " = '" 
				+ super.ampmDateTimeToSQLDateTime(getdattimeleftprevious()) + "'"
			+ ", " + SMTableworkorders.dattimearrivedatcurrent + " = '" 
				+ super.ampmDateTimeToSQLDateTime(getdattimearrivedatcurrent()) + "'"
			+ ", " + SMTableworkorders.dattimeleftcurrent + " = '" 
				+ super.ampmDateTimeToSQLDateTime(getdattimeleftcurrent()) + "'"
			+ ", " + SMTableworkorders.dattimearrivedatnext + " = '" 
				+ super.ampmDateTimeToSQLDateTime(getdattimearrivedatnext()) + "'"
	    	+ ", " + SMTableworkorders.slasteditedbyfullname + " = '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
	    	+ ", " + SMTableworkorders.llasteditedbyuserid + " = " + sUserID
	    	+ ", " + SMTableworkorders.mdetailsheettext + " = '" + clsDatabaseFunctions.FormatSQLStatement(getmdetailsheettext()) + "'"
	    	+ ", " + SMTableworkorders.dattimedone + " = '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(getdattimedone()) + "'"
			+ ", " + SMTableworkorders.madditionalworkcomments + " = '" + clsDatabaseFunctions.FormatSQLStatement(getmadditionalworkcomments()) + "'"
			+ ", " + SMTableworkorders.mcomments + " = '" + clsDatabaseFunctions.FormatSQLStatement(getmcomments()) + "'"
			+ " WHERE (" 
				+ SMTableworkorders.lid + " = " + getlid() 
			+ ")";
    	}else{
    		SQL = " UPDATE " + SMTableworkorders.TableName + " SET"
	    	+ " " + SMTableworkorders.ltimestamp + " = UNIX_TIMESTAMP()"
	    	+ ", " + SMTableworkorders.dattimeleftprevious + " = '" 
				+ super.ampmDateTimeToSQLDateTime(getdattimeleftprevious()) + "'"
			+ ", " + SMTableworkorders.dattimearrivedatcurrent + " = '" 
				+ super.ampmDateTimeToSQLDateTime(getdattimearrivedatcurrent()) + "'"
			+ ", " + SMTableworkorders.dattimeleftcurrent + " = '" 
				+ super.ampmDateTimeToSQLDateTime(getdattimeleftcurrent()) + "'"
			+ ", " + SMTableworkorders.dattimearrivedatnext + " = '" 
				+ super.ampmDateTimeToSQLDateTime(getdattimearrivedatnext()) + "'"
		    + ", " + SMTableworkorders.slasteditedbyfullname + " = '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
		    + ", " + SMTableworkorders.llasteditedbyuserid + " = " + sUserID
	    	+ ", " + SMTableworkorders.mdetailsheettext + " = '" + clsDatabaseFunctions.FormatSQLStatement(getmdetailsheettext()) + "'"
			+ " WHERE (" 
				+ SMTableworkorders.lid + " = " + getlid() 
			+ ")";
    	}
			try {
				Statement stmt = conn.createStatement();
				stmt.execute(SQL);
			} catch (Exception e) {
				if (WebContextParameters.getLogWorkOrderUpdates(context).compareToIgnoreCase("True") == 0){
					log.writeEntry(
							sUserID, 
						SMLogEntry.LOG_OPERATION_SMWORKORDERSAVEDEBUGGING, 
						"EDIT SCREEN WOID :" + getlid() + ", isWorkOrderPosted() = " + isWorkOrderPosted(),
						"In save_edit_screen_without_data_transaction EXCEPTION CAUGHT - SQL: " + SQL + " - " + e.getMessage(),
						"[1430407563]")
					;
				}
				throw new Exception("Error [1430407565] updating work order with SQL: " + SQL + " - " + e.getMessage() + ".");
			}

			if (!isWorkOrderPosted()){
				try{
					save_lines(conn, sUserFullName, sUserID, log, SAVING_FROM_EDIT_SCREEN, context);
				}catch (Exception ex){
					throw new Exception(ex.getMessage());
				}
			}
			if (WebContextParameters.getLogWorkOrderUpdates(context).compareToIgnoreCase("True") == 0){
				log.writeEntry(
					sUserID, 
					SMLogEntry.LOG_OPERATION_SMWORKORDERSAVEDEBUGGING, 
					"EDIT SCREEN WOID :" + getlid() + ", isWorkOrderPosted() = " + isWorkOrderPosted(),
					"In save_edit_screen_without_data_transaction - SQL: " + SQL,
					"[1430407564]")
				;
			}
    	return;
    }
	public void save_import_without_data_transaction (
			Connection conn, 
			String sUserID,
			String sUserFullName,
			SMLogEntry log,
			ServletContext context) throws Exception{
		
		String SQL = " UPDATE " + SMTableworkorders.TableName + " SET"
	    		+ " " + SMTableworkorders.ltimestamp + " = UNIX_TIMESTAMP()"
	    		+ ", " + SMTableworkorders.slasteditedbyfullname + " = '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
			    + ", " + SMTableworkorders.llasteditedbyuserid + " = " + sUserID
	    		+ ", " + SMTableworkorders.iimported + " = 1"
	    		+ " WHERE (" + SMTableworkorders.lid + " = " + getlid() + ")";

			try {
				Statement stmt = conn.createStatement();
				stmt.execute(SQL);
			} catch (Exception e) {
				if (WebContextParameters.getLogWorkOrderUpdates(context).compareToIgnoreCase("True") == 0){
					log.writeEntry(
						sUserID, 
						SMLogEntry.LOG_OPERATION_SMWORKORDERSAVEDEBUGGING, 
						"IMPORT WOID :" + getlid() + ", isWorkOrderPosted() = " + isWorkOrderPosted(),
						"In save_import_without_data_transaction EXCEPTION CAUGHT - SQL: " + SQL + " - " + e.getMessage(),
						"[1430406364]")
					;
				}
				throw new Exception("Error [1430406365] updating work order with SQL: " + SQL + " - " + e.getMessage() + ".");
			}
			if (WebContextParameters.getLogWorkOrderUpdates(context).compareToIgnoreCase("True") == 0){
				log.writeEntry(
					sUserID, 
					SMLogEntry.LOG_OPERATION_SMWORKORDERSAVEDEBUGGING, 
					"IMPORT WOID :" + getlid() + ", isWorkOrderPosted() = " + isWorkOrderPosted(),
					"In save_import_without_data_transaction - SQL: " + SQL,
					"[1430406366]")
				;
			}
    	return;
    }
	private void save_acceptance_screen_without_data_transaction (
			Connection conn, 
			String sUserID,
			String sUserFullName,
			SMLogEntry log,
			String sDBID,
			ServletContext context) throws Exception{
		if (WebContextParameters.getLogWorkOrderUpdates(context).compareToIgnoreCase("True") == 0){
			log.writeEntry(
					sUserID, 
				SMLogEntry.LOG_OPERATION_SMWORKORDERSAVEDEBUGGING, 
				"ACCEPTANCE SCREEN WOID :" + getlid() 
				+ ", isWorkOrderPosted() = " + isWorkOrderPosted(),
				"Before validate_acceptance_screen_fields",
				"[1430332141]")
			;
		}
    	try{
    		validate_acceptance_screen_fields(conn, sUserID, sUserFullName, sDBID, context);
    	}catch (Exception ex){
    		throw new Exception("Error validating " + SMTableworkorders.ObjectName + " - " + ex.getMessage());
    	}
    	if (WebContextParameters.getLogWorkOrderUpdates(context).compareToIgnoreCase("True") == 0){
			log.writeEntry(
					sUserID, 
				SMLogEntry.LOG_OPERATION_SMWORKORDERSAVEDEBUGGING, 
				"ACCEPTANCE SCREEN WOID :" + getlid() + ", isWorkOrderPosted() = " + isWorkOrderPosted(),
				"After validate_acceptance_screen_fields",
				"[1430332142]")
			;
    	}
		String SQL = " UPDATE " + SMTableworkorders.TableName + " SET"
	    	+ " " + SMTableworkorders.ltimestamp + " = UNIX_TIMESTAMP()"
			+ ", " + SMTableworkorders.dattimesigned + " = '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(getdattimesigned()) + "'"
			+ ", " + SMTableworkorders.iadditionalworkauthorized + " = " + getsiadditionalworkauthorized()
			+ ", " + SMTableworkorders.msignature + " = '" + clsDatabaseFunctions.FormatSQLStatement(getmsignature()) + "'"
			+ ", " + SMTableworkorders.ssignedbyname + " = '" + clsDatabaseFunctions.FormatSQLStatement(getssignedbyname()) + "'"
			+ ", " + SMTableworkorders.slasteditedbyfullname + " = '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
		    + ", " + SMTableworkorders.llasteditedbyuserid + " = " + sUserID
			+ ", " + SMTableworkorders.lsignatureboxwidth + " = " + clsDatabaseFunctions.FormatSQLStatement(getlsignatureboxwidth())
			+ " WHERE (" 
				+ SMTableworkorders.lid + " = " + getlid() 
			+ ")";

			try {
				Statement stmt = conn.createStatement();
				stmt.execute(SQL);
			} catch (Exception e) {
				if (WebContextParameters.getLogWorkOrderUpdates(context).compareToIgnoreCase("True") == 0){
					log.writeEntry(
							sUserID, 
						SMLogEntry.LOG_OPERATION_SMWORKORDERSAVEDEBUGGING, 
						"ACCEPTANCE SCREEN WOID :" + getlid() + ", isWorkOrderPosted() = " + isWorkOrderPosted(),
						"In save_acceptance_screen_without_data_transaction EXCEPTION CAUGHT - SQL: " + SQL + " - " + e.getMessage(),
						"[1430332131]")
					;
				}
				throw new Exception("Error [1430332132] updating work order with SQL: " + SQL + " - " + e.getMessage() + ".");
			}
			if (WebContextParameters.getLogWorkOrderUpdates(context).compareToIgnoreCase("True") == 0){
				log.writeEntry(
						sUserID, 
					SMLogEntry.LOG_OPERATION_SMWORKORDERSAVEDEBUGGING, 
					"ACCEPTANCE SCREEN WOID :" + getlid() + ", isWorkOrderPosted() = " + isWorkOrderPosted(),
					"In save_acceptance_screen_without_data_transaction - SQL: " + SQL,
					"[1430332133]")
				;
			}
    	return;
    }
    private void updateJobSequencesByDayAndMechanic(
    		String sPreviousMechanicID, 
    		String sPreviousDayAsMdYYYY, 
    		Connection conn, 
    		String sUserFullName,
    		String sUserID,
    		SMLogEntry log,
    		int iSavingFromWhichScreen,
    		ServletContext context) throws Exception{
    	//The day and mechanic which were PREVIOUSLY on this work order get passed to this function.
    	//Here we load ALL the work order entries for this day and this mechanic into an array.
    	//This is done because if THIS entry has changed mechanic or day, then there will be a missing job order
    	//sequence on the day/mechanic from which it was moved.
    	//So we list all the entries for that 'previous' day/mechanic, and re-sequence them:
    	//Finally, we update all the entries:
    	//We do this with an UPDATE statement, rather than just using the 'save' method on each job cost entry
    	//because the 'save' method resequences everything again and we don't want that to happen.
    	String SQL = "SELECT"
    		+ " " + SMTableworkorders.lid
    		+ ", " + SMTableworkorders.ijoborder
    		+ " FROM " + SMTableworkorders.TableName
    		+ " WHERE ("
    			+ "(" + SMTableworkorders.datscheduleddate + " = '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(sPreviousDayAsMdYYYY) + "')"
    			+ " AND (" + SMTableworkorders.imechid + " = " + sPreviousMechanicID + ")"
    		+ ")"
    		+ " ORDER BY " + SMTableworkorders.ijoborder
    		;
    	
    	ArrayList<SMWorkOrderHeader> arrWorkOrders = new ArrayList<SMWorkOrderHeader>(0);
    	try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()){
				SMWorkOrderHeader entry = new SMWorkOrderHeader();
				long lWorkOrderID = rs.getLong(SMTableworkorders.lid);
				entry.setlid(Long.toString(lWorkOrderID));
				if (!entry.load(conn)){
					rs.close();
					throw new Exception("Error [1426706684] loading Work order ID: " + Long.toString(rs.getLong(SMTableworkorders.lid)) + ".");
				}
				arrWorkOrders.add(entry);
				arrWorkOrders.get(arrWorkOrders.size() - 1).setsjoborder(Integer.toString(arrWorkOrders.size()));
			}
			rs.close();
		} catch (SQLException e) {
			throw new Exception("Error [1426706685] updating job sequences with SQL: " + SQL + " - " + e.getMessage());
		}
		
		//Now the array should contain ALL the job cost entries for the mechanic and day which were PREVIOUSLY on this
    	//work order, before we did this current update, and we
		//should be able to just UPDATE all those records with the correct values:
		for (int i = 0; i < arrWorkOrders.size(); i++){
				SQL = "UPDATE " + SMTableworkorders.TableName + " SET"
				+ " " + SMTableworkorders.ijoborder + " = " + Integer.toString(i + 1)
				+ " WHERE ("
					+ "(" + SMTableworkorders.lid + " = " + arrWorkOrders.get(i).getlid() + ")"
				+ ")"
				;
    		try {
				Statement stmt = conn.createStatement();
				stmt.execute(SQL);
			} catch (SQLException e) {
				if (WebContextParameters.getLogWorkOrderUpdates(context).compareToIgnoreCase("True") == 0){
					log.writeEntry(
							sUserID, 
							SMLogEntry.LOG_OPERATION_SMWORKORDERSAVEDEBUGGING, 
							Integer.toString(iSavingFromWhichScreen) + " WOID :" + getlid() 
							+ ", isWorkOrderPosted() = " + isWorkOrderPosted()
							+ " in updateJobSequencesByDayAndMechanic",
							"EXCEPTION CAUGHT - SQL = " + SQL + " - " + e.getMessage(),
							"[1429282863]")
						;
				}
				throw new Exception("Error [1426777581] insert/updating work order record with SQL 2: " + SQL + " - " + e.getMessage());
			}
    		if (WebContextParameters.getLogWorkOrderUpdates(context).compareToIgnoreCase("True") == 0){
				log.writeEntry(
						sUserID, 
						SMLogEntry.LOG_OPERATION_SMWORKORDERSAVEDEBUGGING, 
						Integer.toString(iSavingFromWhichScreen) + " WOID :" + getlid() 
						+ ", isWorkOrderPosted() = " + isWorkOrderPosted()
						+ " in updateJobSequencesByDayAndMechanic",
						"UPDATE succeeded - SQL = " + SQL,
						"[1429282863]")
					;
    		}
		}
    }

 /*   private void updateGeocodes(
    		ArrayList<SMWorkOrderHeader>arrWorkOrders, 
    		Connection conn, 
    		String sUser) throws Exception {
    	
    	String SQL = "";
    	for (int i = 0; i < arrWorkOrders.size(); i++){
    		SQL = "SELECT " 
    			+ SMTableorderheaders.sgeocode
    			+ ", " + SMTableorderheaders.sShipToAddress1
    			+ ", " + SMTableorderheaders.sShipToAddress2
    			+ ", " + SMTableorderheaders.sShipToAddress3
    			+ ", " + SMTableorderheaders.sShipToAddress4
    			+ ", " + SMTableorderheaders.sShipToCity
    			+ ", " + SMTableorderheaders.sShipToState
    			+ ", " + SMTableorderheaders.sShipToZip
    			+ " FROM " + SMTableorderheaders.TableName
    			+ "  WHERE ("
    				+ "(" + SMTableorderheaders.strimmedordernumber + " = '" 
    				+ arrWorkOrders.get(i).getstrimmedordernumber() + "')"
    				+ " AND (" + SMTableorderheaders.sgeocode + " = '')"
    			+ ")"
    		;
    		ResultSet rs = SMUtilities.openResultSet(SQL, conn);
    		if (rs.next()){
    			//Get the geocode and update it:
				String sMapAddress = rs.getString(SMTableorderheaders.sShipToAddress1).trim();
				sMapAddress	= sMapAddress.trim() + " " + rs.getString(SMTableorderheaders.sShipToAddress2).trim();
				sMapAddress	= sMapAddress.trim() + " " + rs.getString(SMTableorderheaders.sShipToAddress3).trim();
				sMapAddress	= sMapAddress.trim() + " " + rs.getString(SMTableorderheaders.sShipToAddress4).trim();
				sMapAddress	= sMapAddress.trim() + " " + rs.getString(SMTableorderheaders.sShipToCity).trim();
				sMapAddress	= sMapAddress.trim() + " " + rs.getString(SMTableorderheaders.sShipToState).trim();
				sMapAddress	= sMapAddress.trim() + " " + rs.getString(SMTableorderheaders.sShipToZip).trim();
				//sMapAddress	= sMapAddress.trim() + " " + rs.getString(SMTableorderheaders.TableName + "." 
				//		+ SMTableorderheaders.sShipToCountry).trim();
				
				String sLatLng = "";
				//We are NOT going to stop the save if we code a geocode error:
				try {
					sLatLng = SMGeocoder.codeAddress(sMapAddress, conn, 0);
				} catch (XPathExpressionException e) {
					//throw new SQLException("Geocoder XPathExpressionException - " + e.getMessage());
				} catch (IOException e) {
					//throw new SQLException("Geocoder IOException - " + e.getMessage());
				} catch (ParserConfigurationException e) {
					//throw new SQLException("Geocoder ParserConfigurationException - " + e.getMessage());
				} catch (SAXException e) {
					//throw new SQLException("Geocoder SAXException - " + e.getMessage());
				}
				
				SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(conn);
				log.writeEntry(
					sUser, 
					SMLogEntry.LOG_OPERATION_GEOCODEREQUEST, 
					" Work Order: " + getlid() + "\n" 
					+ "Requested Address: " + sMapAddress + "\n" 
					+ "Returned Lat/Lng: " + sLatLng + "\n" 
					,
					"SMWorkOrderHeader.updateGeocodes",
					"[1512765699]");
				
				if (sLatLng.compareToIgnoreCase("") !=0){
					String sUpdateSQL = "UPDATE " + SMTableorderheaders.TableName
					+ " SET " + SMTableorderheaders.sgeocode + " = '" + sLatLng + "'"
					+ " WHERE ("
						+ "(" + SMTableorderheaders.strimmedordernumber + " = '" 
						+ arrWorkOrders.get(i).getstrimmedordernumber() + "')"
					+ ")"
					;
					try{
						Statement stmt = conn.createStatement();
						stmt.execute(sUpdateSQL);
					}catch (SQLException e){
						rs.close();
						throw new SQLException ("Error [1426629049] updating geocode with SQL: " + sUpdateSQL);
					}
				}
    		}
    		rs.close();
    	}
    }
    */
    private void updateFromConfigureWorkOrderWithNoScheduleChange(
    		Connection conn, 
    		String sUserID, 
    		String sUserFullName, 
    		boolean bPosted, 
    		SMLogEntry log,
    		int iSavingFromWhichScreen,
    		ServletContext context) throws Exception{
    	//WOSAVEERROR
    	String SQL = "UPDATE " + SMTableworkorders.TableName + " SET"
			+ " " + SMTableworkorders.bdbackchargehours + " = " + getsbackchargehours().replace(",", "")
			+ ", " + SMTableworkorders.bdqtyofhours + " = " + getsqtyofhours().replace(",", "")
			+ ", " + SMTableworkorders.bdtravelhours + " = " + getstravelhours().replace(",", "")
			+ ", " + SMTableworkorders.ltimestamp + " = UNIX_TIMESTAMP()"
			+ ", " + SMTableworkorders.minstructions + " = '" + clsDatabaseFunctions.FormatSQLStatement(getminstructions()) + "'"
			+ ", " + SMTableworkorders.mworkdescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsworkdescription()) + "'"
			+ ", " + SMTableworkorders.sassistant + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsassistant()) + "'"
			+ ", " + SMTableworkorders.slasteditedbyfullname + " = '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
		    + ", " + SMTableworkorders.llasteditedbyuserid + " = " + sUserID
			+ ", " + SMTableworkorders.sschedulecomment + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsschedulecomment()) + "'"
			+ ", " + SMTableworkorders.sstartingtime + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsstartingtime()) + "'"
			
			+ " WHERE ("
			+ "(" + SMTableworkorders.lid + " = " + getlid() + ")"
			
		+ ")"
		;
    	
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			if (WebContextParameters.getLogWorkOrderUpdates(context).compareToIgnoreCase("True") == 0){
				log.writeEntry(
						sUserID, 
						SMLogEntry.LOG_OPERATION_SMWORKORDERSAVEDEBUGGING, 
						Integer.toString(iSavingFromWhichScreen) + " WOID :" + getlid() + ", isWorkOrderPosted() = " + isWorkOrderPosted(),
						"In updateFromConfigureWorkOrderWithNoScheduleChange - EXCEPTION CAUGHT - SQL: " + SQL + " - " + e.getMessage(),
						"[1429125175]")
					;
			}
			throw new Exception("Error [1426778103] insert/updating job cost record with SQL: " + SQL + " - " + e.getMessage());
		}
		if (WebContextParameters.getLogWorkOrderUpdates(context).compareToIgnoreCase("True") == 0){
			log.writeEntry(
					sUserID, 
					SMLogEntry.LOG_OPERATION_SMWORKORDERSAVEDEBUGGING, 
					Integer.toString(iSavingFromWhichScreen) + " WOID :" + getlid() + ", isWorkOrderPosted() = " + isWorkOrderPosted(),
					"In updateFromConfigureWorkOrderWithNoScheduleChange - SQL: " + SQL,
					"[1429125175]")
				;
		}
		if (!bPosted){
			try{
				save_lines(conn, sUserFullName, sUserID, log, SAVING_FROM_CONFIGURE_SCREEN, context);
			}catch (Exception ex){
				throw new Exception("Error [1426629858] Failed to save " + SMTableworkorders.ObjectName + " lines."
					+ ex.getMessage());
			}
		}
    }
    private void updateFromConfigureWithScheduleChange(
    		SMWorkOrderHeader wo_header, 
    		Connection conn, 
    		String sUserID, 
    		String sUserFullName,
    		SMLogEntry log,
    		int iSavingFromWhichScreen,
    		boolean bScheduleSequenceHasChanged,
			boolean bDateOrMechanicHasChanged,
			ServletContext context) throws Exception {
    	
    	String SQL = "UPDATE " + SMTableworkorders.TableName + " SET"
			+ " " + SMTableworkorders.bdbackchargehours + " = " + wo_header.getsbackchargehours().replace(",", "")
			+ ", " + SMTableworkorders.bdqtyofhours + " = " + wo_header.getsqtyofhours().replace(",", "")
			+ ", " + SMTableworkorders.bdtravelhours + " = " + wo_header.getstravelhours().replace(",", "")
			+ ", " + SMTableworkorders.datscheduleddate + " = '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(wo_header.getsscheduleddate()) + "'"
			+ ", " + SMTableworkorders.ijoborder + " = " + wo_header.getsjoborder()
			+ ", " + SMTableworkorders.imechid + " = " + wo_header.getmechid()
			+ ", " + SMTableworkorders.ltimestamp + " = UNIX_TIMESTAMP()"
			+ ", " + SMTableworkorders.minstructions + " = '" + clsDatabaseFunctions.FormatSQLStatement(wo_header.getminstructions()) + "'"
			+ ", " + SMTableworkorders.mworkdescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(wo_header.getsworkdescription()) + "'"
			+ ", " + SMTableworkorders.sassistant + " = '" + clsDatabaseFunctions.FormatSQLStatement(wo_header.getsassistant()) + "'"
			+ ", " + SMTableworkorders.slasteditedbyfullname + " = '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
		    + ", " + SMTableworkorders.llasteditedbyuserid + " = " + sUserID
			+ ", " + SMTableworkorders.smechanicinitials + " = '" + clsDatabaseFunctions.FormatSQLStatement(wo_header.getmechanicsinitials()) + "'"
			+ ", " + SMTableworkorders.smechanicname + " = '" + clsDatabaseFunctions.FormatSQLStatement(wo_header.getmechanicsname()) + "'"
			+ ", " + SMTableworkorders.sschedulecomment + " = '" + clsDatabaseFunctions.FormatSQLStatement(wo_header.getsschedulecomment()) + "'"
			+ ", " + SMTableworkorders.sstartingtime + " = '" + clsDatabaseFunctions.FormatSQLStatement(wo_header.getsstartingtime()) + "'"
			;
    		if (bDateOrMechanicHasChanged){
    			SQL += ", " + SMTableworkorders.dattimelastschedulechange + " = NOW()"
    					+ ", " + SMTableworkorders.sschedulechangedbyfullname + " = '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
    				    + ", " + SMTableworkorders.lschedulechangedbyuserid + " = " + sUserID
    			;
    		}
			SQL += " WHERE ("
					+ "(" + SMTableworkorders.lid + " = " + getlid() + ")"
			+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			if (WebContextParameters.getLogWorkOrderUpdates(context).compareToIgnoreCase("True") == 0){
				log.writeEntry(
						sUserID, 
						SMLogEntry.LOG_OPERATION_SMWORKORDERSAVEDEBUGGING, 
						Integer.toString(iSavingFromWhichScreen) + " WOID :" + getlid() + ", isWorkOrderPosted() = " + isWorkOrderPosted(),
						"In updateFromConfigureWithScheduleChange - EXCEPTION CAUGHT - SQL: " + SQL + " - " + e.getMessage(),
						"[1429283040]")
				;
			}
			throw new Exception("Error [1426865946] insert/updating job cost record with SQL: " + SQL + " - " + e.getMessage());
		}
		if (WebContextParameters.getLogWorkOrderUpdates(context).compareToIgnoreCase("True") == 0){
			log.writeEntry(
					sUserID, 
					SMLogEntry.LOG_OPERATION_SMWORKORDERSAVEDEBUGGING, 
					Integer.toString(iSavingFromWhichScreen) + " WOID :" + getlid() + ", isWorkOrderPosted() = " + isWorkOrderPosted(),
					"In updateFromConfigureWithScheduleChange - SQL: " + SQL,
					"[1429125176]")
			;
		}
		if (!wo_header.isWorkOrderPosted()){
			try{
				save_lines(conn, sUserFullName, sUserID, log, iSavingFromWhichScreen, context);
			}catch (Exception ex){
				throw new Exception("Error [1426865947] Failed to save " + SMTableworkorders.ObjectName + " lines."
					+ ex.getMessage());
			}
		}
    }
    private void insertWorkOrder (
    		SMWorkOrderHeader wo_header, 
    		Connection conn, 
    		String sUserID, 
    		String sUserFullName, 
    		SMLogEntry log,
    		int iSavingFromWhichScreen,
    		ServletContext context) throws Exception {
		String SQL = "INSERT INTO " + SMTableworkorders.TableName + "("
		+ SMTableworkorders.bdbackchargehours
		+ ", " + SMTableworkorders.bdqtyofhours
		+ ", " + SMTableworkorders.bdtravelhours
		+ ", " + SMTableworkorders.datscheduleddate
		+ ", " + SMTableworkorders.ijoborder
		+ ", " + SMTableworkorders.imechid
		+ ", " + SMTableworkorders.ltimestamp
		+ ", " + SMTableworkorders.madditionalworkcomments
		+ ", " + SMTableworkorders.mcomments
		+ ", " + SMTableworkorders.mdetailsheettext
		+ ", " + SMTableworkorders.minstructions
		+ ", " + SMTableworkorders.msignature
		+ ", " + SMTableworkorders.mmanagersnotes
		+ ", " + SMTableworkorders.mworkdescription
		+ ", " + SMTableworkorders.sassistant
		+ ", " + SMTableworkorders.slasteditedbyfullname
		+ ", " + SMTableworkorders.llasteditedbyuserid
		+ ", " + SMTableworkorders.smechanicinitials
		+ ", " + SMTableworkorders.smechanicname
		+ ", " + SMTableworkorders.sschedulecomment
		+ ", " + SMTableworkorders.sscheduledbyfullname
		+ ", " + SMTableworkorders.lscheduledbyuserid
		+ ", " + SMTableworkorders.sstartingtime
		+ ", " + SMTableworkorders.strimmedordernumber
		+ ", " + SMTableworkorders.dattimelastschedulechange
		+ ", " + SMTableworkorders.sschedulechangedbyfullname
		+ ", " + SMTableworkorders.lschedulechangedbyuserid
		+ ", " + SMTableworkorders.mdbaaddress
		+ ", " + SMTableworkorders.mdbaremittoaddress
		+ ", " + SMTableworkorders.sdbaworkorderlogo
		+ ") VALUES ("
		+ " " + wo_header.getsbackchargehours().replace(",", "")
		+ ", " + wo_header.getsqtyofhours().replace(",", "")
		+ ", " + wo_header.getstravelhours().replace(",", "")
		+ ", '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(wo_header.getsscheduleddate()) + "'"
		+ ", " + wo_header.getsjoborder()
		+ ", " + wo_header.getmechid()
		+ ", UNIX_TIMESTAMP()"
		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(wo_header.getmadditionalworkcomments()) + "'"
		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(wo_header.getmdetailsheettext()) + "'"
		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(wo_header.getmcomments()) + "'"
		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(wo_header.getminstructions()) + "'"
		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(wo_header.getmsignature()) + "'"
		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(wo_header.getmmanagernotes()) + "'"
		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(wo_header.getsworkdescription()) + "'"
		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(wo_header.getsassistant()) + "'"
		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
		+ ", " + sUserID
		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(wo_header.getmechanicsinitials()) + "'"
		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(wo_header.getmechanicsname()) + "'"
		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(wo_header.getsschedulecomment()) + "'"
		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
		+ ", " + sUserID
		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(wo_header.getsstartingtime()) + "'"
		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(wo_header.getstrimmedordernumber()) + "'"
		+ ", NOW()"
		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
		+ ", " + sUserID
		+ ", '" + wo_header.getmdbaaddress() + "'"
		+ ", '" + wo_header.getmdbaremittoaddress() + "'"
		+ ", '" + wo_header.getSdbaworkorderlogo() + "'"
		+ ")"
		;
		
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			throw new Exception("Error [1426866274] insert/updating job cost record with SQL: " + SQL + " - " + e.getMessage());
		}
		
		//Update the ID:
		String sSQL = "SELECT last_insert_id()";
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
			if (rs.next()) {
				setlid(Long.toString(rs.getLong(1)));
			}else {
				setlid("0");
			}
			rs.close();
		} catch (SQLException e) {
			throw new Exception("Error [1426866275] Could not get last ID number - " + e.getMessage());
		}
		//If something went wrong, we can't get the last ID:
		if (getlid().compareToIgnoreCase("0") == 0){
			throw new Exception("Error [1426866276] Could not get last ID number.");
		}
	
    	try{
    		save_lines(conn, sUserFullName, sUserID, log, iSavingFromWhichScreen, context);
    	}catch (Exception ex){
    		throw new Exception("Error [1426866372] Failed to save " + SMTableworkorders.ObjectName + " lines."
    			+ ex.getMessage());
    	}
    }
    private void updateOtherAffectedWorkOrder(
    		SMWorkOrderHeader wo_header, 
    		Connection conn, 
    		String sUserID,
    		String sUserFullName,
    		SMLogEntry log,
    		int iSavingFromWhichScreen,
    		ServletContext context) throws Exception {
    	//The only thing we can affect on another order is the job sequence,
    	//and we can do that, even if the work order is posted:
    	String SQL = "UPDATE " + SMTableworkorders.TableName + " SET"
			+ " " + SMTableworkorders.ijoborder + " = " + wo_header.getsjoborder()
			+ ", " + SMTableworkorders.ltimestamp + " = UNIX_TIMESTAMP()"
			+ ", " + SMTableworkorders.slasteditedbyfullname + " = '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
		    + ", " + SMTableworkorders.llasteditedbyuserid + " = " + sUserID
			+ " WHERE ("
				+ "(" + SMTableworkorders.lid + " = " + wo_header.getlid() + ")"
			+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			if (WebContextParameters.getLogWorkOrderUpdates(context).compareToIgnoreCase("True") == 0){
				log.writeEntry(
						sUserID, 
						SMLogEntry.LOG_OPERATION_SMWORKORDERSAVEDEBUGGING, 
						Integer.toString(iSavingFromWhichScreen) + " WOID :" + getlid() + ", isWorkOrderPosted() = " + isWorkOrderPosted(),
						"In updateOtherAffectedWorkOrder EXCEPTION CAUGHT SQL: " + SQL + e.getMessage(),
						"[1429283160]")
					;
			}
			throw new Exception("Error [1426866715] updating affected work order with SQL: " + SQL + " - " + e.getMessage());
		}
		if (WebContextParameters.getLogWorkOrderUpdates(context).compareToIgnoreCase("True") == 0){
			log.writeEntry(
					sUserID, 
					SMLogEntry.LOG_OPERATION_SMWORKORDERSAVEDEBUGGING, 
					Integer.toString(iSavingFromWhichScreen) + " WOID :" + getlid() + ", isWorkOrderPosted() = " + isWorkOrderPosted(),
					"In updateOtherAffectedWorkOrder SQL: " + SQL,
					"[1429283161]")
				;
		}
    }
    
    public void ajax_Update_Left_Previous_Time(
			ServletContext context, 
			String sDBIB, 
			String sUserFullName
			) throws Exception{
		
		//Validate the 'left previous' time.
		setdattimeleftprevious(clsDateAndTimeConversions.stdDateTimeToSQLDateTimeString(getdattimeleftprevious()));
		if(getdattimeleftprevious().compareToIgnoreCase(clsDateAndTimeConversions.stdDateTimeToSQLDateTimeString("")) == 0) {
			throw new Exception("Invalid time entered."); 
		}
			//Update the  time
		    String SQL = "";
		    SQL = " UPDATE " + SMTableworkorders.TableName + " SET "
		    	+ SMTableworkorders.dattimeleftprevious + " = '" + getdattimeleftprevious() + "'"
				+ " WHERE (" 
					+ SMTableworkorders.lid + " = " + getlid() 
				+ ")";
		    try {	    	
		    	//Update the database
		    	clsAjaxFunctions.ajax_Update_MySQL(context, sDBIB, sUserFullName, SQL);
		    	
			} catch (Exception e) {
				throw new Exception(e.getMessage());
			}
			return;
		}
    
    public void ajax_Update_Arrived_Current_Time(
			ServletContext context, 
			String sDBIB, 
			String sUserFullName
			) throws Exception{
		
		//Validate the time.
		setdattimearrivedatcurrent(clsDateAndTimeConversions.stdDateTimeToSQLDateTimeString(getdattimearrivedatcurrent()));
		if(getdattimearrivedatcurrent().compareToIgnoreCase(clsDateAndTimeConversions.stdDateTimeToSQLDateTimeString("")) == 0) {
			throw new Exception("Invalid time entered."); 
		}
			//Update the time
		    String SQL = "";
		    SQL = " UPDATE " + SMTableworkorders.TableName + " SET "
		    	+ SMTableworkorders.dattimearrivedatcurrent + " = '" + getdattimearrivedatcurrent() + "'"
				+ " WHERE (" 
					+ SMTableworkorders.lid + " = " + getlid() 
				+ ")";
		    try {	    	
		    	//Update the database
		    	clsAjaxFunctions.ajax_Update_MySQL(context, sDBIB, sUserFullName, SQL);
		    	
			} catch (Exception e) {
				throw new Exception(e.getMessage());
			}
			return;
		}
    
    public void ajax_Update_Left_Current_Time(
			ServletContext context, 
			String sDBIB, 
			String sUserFullName
			) throws Exception{
		
		//Validate the time.
    	setdattimeleftcurrent(clsDateAndTimeConversions.stdDateTimeToSQLDateTimeString(getdattimeleftcurrent()));
		if(getdattimeleftcurrent().compareToIgnoreCase(clsDateAndTimeConversions.stdDateTimeToSQLDateTimeString("")) == 0) {
			throw new Exception("Invalid time entered."); 
		}
			//Update the time
		    String SQL = "";
		    SQL = " UPDATE " + SMTableworkorders.TableName + " SET "
		    	+ SMTableworkorders.dattimeleftcurrent + " = '" + getdattimeleftcurrent() + "'"
				+ " WHERE (" 
					+ SMTableworkorders.lid + " = " + getlid() 
				+ ")";
		    try {	    	
		    	//Update the database
		    	clsAjaxFunctions.ajax_Update_MySQL(context, sDBIB, sUserFullName, SQL);
		    	
			} catch (Exception e) {
				throw new Exception(e.getMessage());
			}
			return;
		}
    
    public void ajax_Update_Arrived_Next_Time(
			ServletContext context, 
			String sDBIB, 
			String sUserFullName
			) throws Exception{
		
		//Validate the time.
		setdattimearrivedatnext(clsDateAndTimeConversions.stdDateTimeToSQLDateTimeString(getdattimearrivedatnext()));
		if(getdattimearrivedatnext().compareToIgnoreCase(clsDateAndTimeConversions.stdDateTimeToSQLDateTimeString("")) == 0) {
			throw new Exception("Invalid time entered."); 
		}
			//Update the time
		    String SQL = "";
		    SQL = " UPDATE " + SMTableworkorders.TableName + " SET "
		    	+ SMTableworkorders.dattimearrivedatnext + " = '" + getdattimearrivedatnext() + "'"
				+ " WHERE (" 
					+ SMTableworkorders.lid + " = " + getlid() 
				+ ")";
		    try {	    	
		    	//Update the database
		    	clsAjaxFunctions.ajax_Update_MySQL(context, sDBIB, sUserFullName, SQL);
		    	
			} catch (Exception e) {
				throw new Exception(e.getMessage());
			}
			return;
		}
    
	public void post_without_data_transaction (
		ServletContext context, 
		String sDBIB, 
		String sUserID, 
		String sUserFullName,
		int iSavingFromWhichScreen) throws Exception{
		if(getsposted().compareToIgnoreCase("1") == 0){
			throw new Exception("This work order was already posted on " + getdattimeposted() + ".");
		}
		//try {
		//	save(context, sDBID, sUser, iSavingFromWhichScreen);
		//} catch (Exception e) {
		//	throw new Exception(e.getMessage());
		//
       	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBIB, 
    			"MySQL", 
    			this.toString() + " - user: " 
    			+ sUserID
    			+ " - "
    			+ sUserFullName
    			);
    	
    	if (conn == null){
    		throw new Exception("Error [1410875428] opening data connection.");
    	}
		String sSQL = "";
		
    	if(getstrimmedordernumber().compareToIgnoreCase("") != 0) {
    		SMOrderHeader order = new SMOrderHeader();
    		order.setM_strimmedordernumber(getstrimmedordernumber());
    		order.load(conn);	
    		if(!order.isDBAValid(conn)){
    			clsDatabaseFunctions.freeConnection(context, conn, "[1547067790]");
    			throw new Exception("The 'Doing Business As Address' on the order as been deleted or is invalid.");
    		} 
        	sSQL = "UPDATE " 
            	+ SMTableworkorders.TableName 
            	+ " LEFT JOIN " + SMTableorderheaders.TableName 
           		+ " ON " + SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber
           		+ " = " + SMTableworkorders.TableName + "." + SMTableworkorders.strimmedordernumber
           		+ " LEFT JOIN " + SMTabledoingbusinessasaddresses.TableName 
           		+ " ON " + SMTabledoingbusinessasaddresses.TableName + "." + SMTabledoingbusinessasaddresses.lid
           		+ " = " + SMTableorderheaders.TableName + "." + SMTableorderheaders.idoingbusinessasaddressid
           		+ " SET " + SMTableworkorders.TableName + "." + SMTableworkorders.dattimeposted + " = NOW()"
           		+ ", " + SMTableworkorders.TableName + "." + SMTableworkorders.iposted + " = 1"
           		+ ", " + SMTableworkorders.TableName + "." + SMTableworkorders.ltimestamp + " = UNIX_TIMESTAMP()"
           		+ ", " + SMTableworkorders.TableName + "." + SMTableworkorders.slasteditedbyfullname + " = '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
    		    + ", " + SMTableworkorders.TableName + "." + SMTableworkorders.llasteditedbyuserid + " = " + sUserID
            	+ ", " + SMTableworkorders.TableName + "." + SMTableworkorders.mdbaaddress + " = " + SMTabledoingbusinessasaddresses.TableName + "." + SMTabledoingbusinessasaddresses.mAddress
            	+ ", " + SMTableworkorders.TableName + "." + SMTableworkorders.mdbaremittoaddress + " = " + SMTabledoingbusinessasaddresses.TableName + "." + SMTabledoingbusinessasaddresses.mRemitToAddress
            	+ ", " + SMTableworkorders.TableName + "." + SMTableworkorders.sdbaworkorderlogo + " = "+ SMTabledoingbusinessasaddresses.TableName + "." + SMTabledoingbusinessasaddresses.sWorkOrderReceiptlogo
            	+ " WHERE ("
            		+ "(" + SMTableworkorders.TableName + "." + SMTableworkorders.lid + " = " + getlid() + ")"
            	+ ")"
            	;
    	}else {
        	
        	sSQL = "UPDATE " 
        		+ SMTableworkorders.TableName 
        		+ " SET " + SMTableworkorders.TableName + "." + SMTableworkorders.dattimeposted + " = NOW()"
        		+ ", " + SMTableworkorders.TableName + "." + SMTableworkorders.iposted + " = 1"
        		+ ", " + SMTableworkorders.TableName + "." + SMTableworkorders.ltimestamp + " = UNIX_TIMESTAMP()"
        		+ ", " + SMTableworkorders.TableName + "." + SMTableworkorders.slasteditedbyfullname + " = '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
    		    + ", " + SMTableworkorders.TableName + "." + SMTableworkorders.llasteditedbyuserid + " = " + sUserID   		
        		+ " WHERE ("
        			+ "(" + SMTableworkorders.TableName + "." + SMTableworkorders.lid + " = " + getlid() + ")"
        		+ ")"
        		;
    	}

   		SMLogEntry log = new SMLogEntry(sDBIB, context);

		try {
			clsDatabaseFunctions.executeSQL(sSQL, conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067791]");
			
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067792]");
			if (WebContextParameters.getLogWorkOrderUpdates(context).compareToIgnoreCase("True") == 0){
		    	log.writeEntry(
		    			sUserID, 
						SMLogEntry.LOG_OPERATION_SMWORKORDERSAVEDEBUGGING, 
						" WOID :" + getlid() + ", isWorkOrderPosted() = " + isWorkOrderPosted(),
						"In post_without_data_transaction EXCEPTION CAUGHT - SQL: " + sSQL
						+ " - error: " + e.getMessage(),
						"[1429282127]")
				;
			}
			throw new Exception(this.toString() + "Could not update to post " + SMTableworkorders.ObjectName + " - " + e.getMessage());
		}
		if (WebContextParameters.getLogWorkOrderUpdates(context).compareToIgnoreCase("True") == 0){
			log.writeEntry(
					sUserID, 
					SMLogEntry.LOG_OPERATION_SMWORKORDERSAVEDEBUGGING, 
					" WOID :" + getlid() + ", isWorkOrderPosted() = " + isWorkOrderPosted(),
					"In post_without_data_transaction - SQL: " + sSQL,
					"[1429282128]")
			;
		}
		clsDatabaseFunctions.freeConnection(context, conn, "[1547067793]");
		
    }

	public void mailPostingNotification(
		String sUserID,
		String sUserFullName,
		ServletContext context, 
		String sDBID,
		String sLicenseModuleLevel) throws Exception{
		
		//If the work order has NO order number, then just back out:
		if (getstrimmedordernumber().compareToIgnoreCase("") == 0){
			return;
		}
		
		//Declare all the variables we'll need here:
		String sSalespersonEmailAddress = "";
		String sSalespersonUserID = "";
		String sSalespersonCode = "";
		//We'll set the 'poster's full name to his username for now, just in case we can't read his full name later on:
		String sPostersFullName = sUserFullName;
		
		//Next get the salesperson's email address and user name:
		String SQL = "SELECT " 
			+ SMTableusers.TableName + "." + SMTableusers.semail 
			+ ", " +  SMTableusers.TableName + "." + SMTableusers.lid
			+ ", " +  SMTableusers.TableName + "." + SMTableusers.sDefaultSalespersonCode
			+ " FROM " 
			+ SMTableorderheaders.TableName
			+ " LEFT JOIN "
			+ SMTableusers.TableName
			+ " ON "
			+ SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson + " = " 
			+ SMTableusers.TableName + "." + SMTableusers.sDefaultSalespersonCode
			+ " WHERE ("
				+ "( " + SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber
				+ " = '" + getstrimmedordernumber() + "' )"
			+ ")"
		;
		
		try{
			ResultSet rsSalesperson = clsDatabaseFunctions.openResultSet(
				SQL, 
				context, 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".mailPostingNotification.getting salesperson email - user: " 
				+ sUserID
				+ " - "
				+ sUserFullName
					);

			if(rsSalesperson.next()){
				//We got an orderheader record, but that doesn't mean we got an email.  The user may HAVE no email listed, OR there may not even be a user associated
				//with that salesperson OR orderheader.
				//So we have to make sure that the email address isn't null or blank:
				sSalespersonEmailAddress = rsSalesperson.getString(SMTableusers.semail);
				if (sSalespersonEmailAddress == null){
					sSalespersonEmailAddress = "";
				}
				sSalespersonUserID = Integer.toString(rsSalesperson.getInt(SMTableusers.lid));
				if (sSalespersonUserID == null){
					sSalespersonUserID = "";
				}
				//Check to make sure that a user with an empty sales person code does not get emailed 
				//from work orders without a specified sales person.
				sSalespersonCode = rsSalesperson.getString(SMTableusers.sDefaultSalespersonCode);
				if (sSalespersonCode == null || sSalespersonCode.compareToIgnoreCase("") == 0){
					sSalespersonEmailAddress = "";
				}
				
			}
			rsSalesperson.close();
		}catch (Exception e){
			throw new Exception("Error [1441293152] getting salesperson's email with SQL: " + SQL + " - " + e.getMessage());	
		}
		
		//If the username or email is blank, just do nothing but return with no error:
		if ((sSalespersonEmailAddress.compareToIgnoreCase("") == 0) || (sSalespersonUserID.compareToIgnoreCase("") == 0)){
			return;
		}
		
		//Now confirm that the salesperson is 'permitted' to receive a notification - if not just return:
		if (!SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.SMReceiveWorkOrderPostNotification, 
			sSalespersonUserID, 
			context, 
			sDBID,
			sLicenseModuleLevel)){
			return;
		}
		
		//Since we know the salesperson HAS an email address AND is authorized to receive the notification, add him to the list of email addresses to be notified:
		ArrayList<String>arrNotifyEmails = new ArrayList<String>(0);
		arrNotifyEmails.add(sSalespersonEmailAddress);
		
		//Now we need to get the 'poster's full name to put in the email:
		SQL = MySQLs.Get_User_By_UserID(sUserID);
		try {
			ResultSet rsUser = clsDatabaseFunctions.openResultSet(
					SQL, 
					context, 
					sDBID, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString()) + ".mailPostingNotification.getting user full name - user: " + sUserID 
					+ " - "
					+ sUserFullName
					);
			//If we can read it we populate it - but if not 'sPostersFullName' will just show the username and that's OK:
			if (rsUser.next()){
				sPostersFullName = rsUser.getString(SMTableusers.sUserFirstName) + " "
					+ rsUser.getString(SMTableusers.sUserLastName);
			}
			rsUser.close();
		} catch (Exception e) {
			throw new Exception("Error [1441293153] getting user's full name with SQL: " + SQL + " - " + e.getMessage());
		}
		
		//Now go get the info we need to send an email:
		String sCurrentTime = "";
		String sSMTPServer = "";
		String sSMTPPort = "";
		String sSMTPSourceServerName = "";
		String sUserName = ""; 
		String sPassword = ""; 
		String sReplyToAddress = "";
		boolean bUsesSMTPAuthentication = false;

		try{
			SQL = "SELECT " + SMTablesmoptions.TableName + ".*"
			+ ", DATE_FORMAT(NOW(),'%c/%e/%Y %h:%i:%s %p')"
				+ " AS CURRENTTIME FROM " 
				+ SMTablesmoptions.TableName;
			ResultSet rsOptions = clsDatabaseFunctions.openResultSet(
					SQL, 
					context, 
					sDBID, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString()) + ".mailPostingNotification.getting email options - user: " + sUserID
					+ " - "
					+ sUserFullName
					);
			if(rsOptions.next()){
				sCurrentTime = rsOptions.getString("CURRENTTIME");
				sSMTPServer = rsOptions.getString(SMTablesmoptions.ssmtpserver).trim();
				sSMTPPort = rsOptions.getString(SMTablesmoptions.ssmtpport).trim();
				sSMTPSourceServerName = rsOptions.getString(SMTablesmoptions.ssmtpsourceservername).trim();
				sUserName = rsOptions.getString(SMTablesmoptions.ssmtpusername).trim();
				sPassword = rsOptions.getString(SMTablesmoptions.ssmtppassword).trim(); 
				sReplyToAddress = rsOptions.getString(SMTablesmoptions.ssmtpreplytoname).trim();
				bUsesSMTPAuthentication = (rsOptions.getInt(SMTablesmoptions.iusesauthentication) == 1);
				rsOptions.close();
			}else{
				rsOptions.close();
				throw new Exception("Error [1441293154] getting smoptions record to get email information.");
			}
		}catch(SQLException e){
			throw new Exception("Error [1441114359] getting email information from smoptions with SQL: " + SQL + "  - " + e.getMessage());
		}
		
		SMOrderHeader smOrder = new SMOrderHeader();
		smOrder.setM_strimmedordernumber(getstrimmedordernumber());
		
		if (!smOrder.load(context, sDBID, sUserID, sUserFullName)){
			throw new Exception("Error loading order to send notification email [1441294908] - " + smOrder.getErrorMessages());
		}
		
		//Now we have to REALOAD the work order, because not all the fields may be loaded, since we may have come from a screen
		//that didn't load all the fields:
		try {
			load(sDBID, sUserFullName, context);
		} catch (Exception e1) {
			//Couldn't re-load work order, but in this case we'll just go ahead - we might miss a few pieces of info, but it's not critical at this point.
		}
		
		String sBody = "Work Order '" + getlid() + "' was posted " + sCurrentTime
			+ " by " + sPostersFullName
			+ "\n\n"
			+ "*** Work Order Information" 
			+ "\nOrder Number: " + getstrimmedordernumber()
			+ "\nWork Order: " + getlid()
			+ "\nTechnician's Name: " + getmechanicsname()
			+ "\nTime Done: " + getdattimedone()
			+ "\nBill To Name: " + smOrder.getM_sBillToName()
			+ "\nShip To Name: " + smOrder.getM_sShipToName()
			;

		int iSMTPPort;
		try {
			iSMTPPort = Integer.parseInt(sSMTPPort);
		} catch (NumberFormatException e) {
			throw new Exception("Error parsing email port '" + sSMTPPort + "' [1441114360] - " + e.getMessage());
		}
		try {
			SMUtilities.sendEmail(
					sSMTPServer, 
					sUserName, 
					sPassword,
					sReplyToAddress,
					Integer.toString(iSMTPPort),
					"Work Order #" + getlid() + " Posted for " + smOrder.getM_sBillToName(),
					sBody,
					"SMCP@" + sSMTPSourceServerName,
					sSMTPSourceServerName, 
					arrNotifyEmails, 
					bUsesSMTPAuthentication,
					false
			);
		} catch (Exception e) {
			throw new Exception("Error sending email [1441114361] " + e.getMessage());
		}
		//Log the fact that the email was sent:
		SMLogEntry log = new SMLogEntry(sDBID, context);
		String sEmailAddresses = "";
		for (int i = 0; i < arrNotifyEmails.size(); i++){
			if (i == 0){
				sEmailAddresses += arrNotifyEmails.get(i);
			}else{
				sEmailAddresses += ", " + arrNotifyEmails.get(i);
			}
		}
		log.writeEntry(
				sUserID, 
			SMLogEntry.LOG_OPERATION_SMEMAILEDWOPOSTNOTIFICATION,
			"WO#" + getlid() + "' Post Notification sent to '" + sEmailAddresses, 
			sBody, 
			"[1441740428]");
		return;
	}
	public boolean load (
		String sDBIB,
		String sUserFullName,
		ServletContext context
	) throws Exception{
		
		Connection conn = clsDatabaseFunctions.getConnection(
			context, 
			sDBIB, 
			"MySQL", 
			SMUtilities.getFullClassName(this.toString()) + ".load [1438863883] - user: " + sUserFullName);
		boolean bResult = false;
		try {
			bResult = load(conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067788]");
			throw new Exception("Error loading - " + e.getMessage());
		}
		clsDatabaseFunctions.freeConnection(context, conn, "[1547067789]");
		return bResult;
	}
	public boolean load (Connection conn) throws Exception{
			//NOTE: this function will load a work order record if it has a valid ID:
			String SQL = "SELECT"
				+ " *"
				+ ", UNIX_TIMESTAMP() AS LOADRECORDTIMESTAMP"
				+ " FROM " + SMTableworkorders.TableName 
				+ " WHERE ("
					+ "(" + SMTableworkorders.lid + " = " + getlid() + ")"
				+ ")"
			; 
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn); 
				if (!rs.next()){
					rs.close();
					return false;
				}

				//Load the variables:
				setlid(Integer.toString(rs.getInt(SMTableworkorders.lid)));
				setmechid(Long.toString(rs.getLong(SMTableworkorders.imechid)));
				setmechanicsinitials(rs.getString(SMTableworkorders.smechanicinitials));
				setmechanicsname(rs.getString(SMTableworkorders.smechanicname));
				setdattimeposted(clsDateAndTimeConversions.resultsetDateTimeStringToString(rs.getString(SMTableworkorders.dattimeposted)));
				setdattimedone(clsDateAndTimeConversions.resultsetDateStringToString(rs.getString(SMTableworkorders.dattimedone)));
				setstrimmedordernumber(rs.getString(SMTableworkorders.strimmedordernumber));
				setssignedbyname(rs.getString(SMTableworkorders.ssignedbyname));
				setdattimesigned(clsDateAndTimeConversions.resultsetDateStringToString(rs.getString(SMTableworkorders.dattimesigned)));
				setmdetailsheettext(rs.getString(SMTableworkorders.mdetailsheettext));
				setmadditionalworkcomments(rs.getString(SMTableworkorders.madditionalworkcomments));
				setmsignature(rs.getString(SMTableworkorders.msignature));
				setmcomments(rs.getString(SMTableworkorders.mcomments));
				setsadditionalworkauthorized(Integer.toString(rs.getInt(SMTableworkorders.iadditionalworkauthorized)));
				setsimported(Integer.toString(rs.getInt(SMTableworkorders.iimported)));
				setsposted(Integer.toString(rs.getInt(SMTableworkorders.iposted)));
				setstimestamp(Long.toString(rs.getLong(SMTableworkorders.ltimestamp)));
				setminstructions(rs.getString(SMTableworkorders.minstructions));
				setmmanagernotes(rs.getString(SMTableworkorders.mmanagersnotes));
				setdattimeleftprevious(clsDateAndTimeConversions.resultsetDateTimeStringToString(
					rs.getString(SMTableworkorders.dattimeleftprevious)));
				setdattimearrivedatcurrent(clsDateAndTimeConversions.resultsetDateTimeStringToString(
					rs.getString(SMTableworkorders.dattimearrivedatcurrent)));
				setdattimeleftcurrent(clsDateAndTimeConversions.resultsetDateTimeStringToString(
					rs.getString(SMTableworkorders.dattimeleftcurrent)));
				setdattimearrivedatnext(clsDateAndTimeConversions.resultsetDateTimeStringToString(
					rs.getString(SMTableworkorders.dattimearrivedatnext)));
				setsassistant(rs.getString(SMTableworkorders.sassistant));
				setsstartingtime(rs.getString(SMTableworkorders.sstartingtime));
				setsworkdescription(rs.getString(SMTableworkorders.mworkdescription));
				setsscheduleddate(clsDateAndTimeConversions.resultsetDateStringToString(rs.getString(SMTableworkorders.datscheduleddate)));
				setsqtyofhours(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableworkorders.bdqtyofhours)));
				setstravelhours(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableworkorders.bdtravelhours)));
				setsbackchargehours(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableworkorders.bdbackchargehours)));
				setslasteditedbyfullname(rs.getString(SMTableworkorders.slasteditedbyfullname));
				setsjoborder(Long.toString(rs.getLong(SMTableworkorders.ijoborder)));
				setsschedulecomment(rs.getString(SMTableworkorders.sschedulecomment));
				setsdattimelastschedulechange(clsDateAndTimeConversions.resultsetDateTimeStringToString(
				rs.getString(SMTableworkorders.dattimelastschedulechange)));
				setsschedulechangedbyfullname(rs.getString(SMTableworkorders.sschedulechangedbyfullname));
				setsgdoclink(rs.getString(SMTableworkorders.sgdoclink));
				setlsignatureboxwidth(Integer.toString(rs.getInt(SMTableworkorders.lsignatureboxwidth)));
				setslastreadrecordtimestamp(Long.toString(rs.getLong("LOADRECORDTIMESTAMP")));
				setmdbaaddress(rs.getString(SMTableworkorders.mdbaaddress));
				setmdbaremittoaddress(rs.getString(SMTableworkorders.mdbaremittoaddress));
				setsdbaworkorderlogo(rs.getString(SMTableworkorders.sdbaworkorderlogo));
				rs.close();
			}catch (SQLException ex){
				throw new Exception("Error [1391438248] loading " + SMTableworkorders.ObjectName + " with SQL: " + SQL + " - " + ex.getMessage());
			}
			try {
				load_lines(conn);
			} catch (Exception e) {
				throw new Exception("Error [1391438249] loading lines - " + e.getMessage());
			}
			return true;
		}
	public void updateMechanicInfoFromMechID(ServletContext context, String sDBIB, String sUserID, String sUserFullName) throws Exception{
		String SQL = "SELECT"
			+ " " + SMTablemechanics.lid
			+ ", " + SMTablemechanics.sMechFullName
			+ ", " + SMTablemechanics.sMechInitial
			+ " FROM " + SMTablemechanics.TableName
			+ " WHERE ("
				+ "(" + SMTablemechanics.lid + " = '" + getmechid() + "')"
			+ ")";
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				context, 
				sDBIB, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".updateMechanicInfoFromMechID - user: " 
				+ sUserID
				+ " - "
				+ sUserFullName
					);
			if (rs.next()){
				setmechanicsinitials(rs.getString(SMTablemechanics.sMechInitial));
				setmechanicsname(rs.getString(SMTablemechanics.sMechFullName));
			}else{
				throw new Exception("Error [1391540167] technician with ID '" + getmechid() + "' not found.");
			}
			rs.close();
		} catch (SQLException e) {
			throw new Exception("Error [1391540168] updating technician info - " + e.getMessage());
		}
	}
	private void load_lines (Connection conn) throws Exception{
		if (getlid().trim().equalsIgnoreCase("")){
			throw new Exception(" Error [1391437427] Invalid " + SMTableworkorders.ObjectName + " - " + getlid());
		}
		String SQL = "SELECT"
			+ " " + SMTableworkorderdetails.lid
			+ " FROM " 
			+ SMTableworkorderdetails.TableName 
			+ " WHERE (" 
				+ SMTableworkorderdetails.lworkorderid + " = " + getlid() 
			+ ") ORDER BY " + SMTableworkorderdetails.llinenumber;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			LineArray.clear();
			while(rs.next()){
				//Add a new line class, and load it up:
				SMWorkOrderDetail line = new SMWorkOrderDetail();
				line.setslid(Integer.toString(rs.getInt(SMTableworkorderdetails.lid)));
				try {
					line.load(conn);
				} catch (Exception e) {
					throw new Exception("Error [1391808515] loading line - " + e.getMessage());
				}
				LineArray.add((SMWorkOrderDetail) line);
			}
			rs.close();
		}catch (SQLException ex){
			throw new Exception("Error [1391437426] loading lines with SQL: " + SQL + " - " + ex.getMessage());
		}
	}

	public boolean add_line(SMWorkOrderDetail line){
		line.setsworkorderid(getlid());
		LineArray.add((SMWorkOrderDetail) line);
		return true;
	}
    public SMWorkOrderDetail getDetailByIndex(int iDetailIndex) throws Exception{
    	
    	if (iDetailIndex > LineArray.size()){
    		throw new Exception("Error [1391438461] - Detail index > LineArray.size");
    	}
    	if (iDetailIndex < 0){
    		throw new Exception("Error [1391438462] - Detail index < 0");
    	}
    	SMWorkOrderDetail Line = (SMWorkOrderDetail) LineArray.get(iDetailIndex);
		return Line;
    }
    public SMWorkOrderDetail getDetailByOrderDetailNumber(String sOrderDetailNumber){
    	for (int i =0 ; i < LineArray.size(); i++){
    		if (LineArray.get(i).getsorderdetailnumber().compareToIgnoreCase(sOrderDetailNumber) == 0){
    			return LineArray.get(i);
    		}
    	}
    	return null;
    }
    public boolean isOrderDetailLineOnWorkOrder(String sDetailNumber) throws Exception{
    	int iDetailNumber;
		try {
			iDetailNumber = Integer.parseInt(sDetailNumber);
		} catch (Exception e1) {
			throw new Exception("Error [1391719431] parsing passed in detail number '" + sDetailNumber + "' - " + e1.getMessage() + ".");
		}
    	for (int i = 0; i < LineArray.size(); i++){
    		try {
				if (Integer.parseInt(LineArray.get(i).getsorderdetailnumber()) == iDetailNumber){
					return true;
				}
			} catch (NumberFormatException e) {
				//If the work order 'orderdetailnumber' field isn't formatted correctly, it's probably a blank, so 
				//it doesn't qualify as a match to the order detail number, so we just let it go
			}
    	}
    	return false;
    }
    public boolean isWorkPerformedCodeOnWorkOrder(String sWorkPerformedCode){
    	for (int i = 0; i < LineArray.size(); i++){
    		if (LineArray.get(i).getsdetailtype().compareToIgnoreCase(
    			Integer.toString(SMTableworkorderdetails.WORK_ORDER_DETAIL_TYPE_WORKPERFORMED)) == 0){
    			if (LineArray.get(i).getsworkperformedcode().compareToIgnoreCase(sWorkPerformedCode) == 0){
    				return true;
    			}
    		}
    	}
    	return false;
    }
    
    public void validate_edit_screen_fields (Connection conn, String sUserID, String sUserFullName, String sDBID, ServletContext context) throws Exception{
    	
    	boolean bValid = true;
    	long lID = 0;
    	String sErrors = "";
    	
		try {
			lID = Long.parseLong(getlid());
		} catch (NumberFormatException e) {
			bValid = false;
			sErrors += "Invalid ID: '" + getlid() + "'.  ";
		}
    	if (lID < -1){
    		bValid = false;
    		sErrors += "Invalid ID: '" + getlid() + "'.  ";
    	}
    	
    	//Check to see if someone else has saved this work order since this user got it:
    	if (lID > 0){
    		String sConcurrencyCheck = checkConcurrency(conn, getlid(), getstimestamp(), getslastreadrecordtimestamp(), sUserID, sUserFullName, sDBID, context);
    		if (sConcurrencyCheck.compareToIgnoreCase("") != 0){
    			bValid = false;
    			sErrors += sConcurrencyCheck + "  .";
    		}
    	}
    	
        if (getdattimedone().compareTo(EMPTY_DATE_STRING) != 0){
	        if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", getdattimedone())){
	        	bValid = false;
	        	sErrors += "Invalid date/time done: '" + getdattimedone() + "  .";
	        }
        }

       	//Save the four 'job times':
		if (getdattimeleftprevious().compareTo(EMPTY_DATETIME_STRING) != 0){
			if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy hh:mm a", getdattimeleftprevious())){
				sErrors += "Left previous site - '" + getdattimeleftprevious() + "' is invalid.  ";
				bValid = false;
			}
		}
		if (getdattimearrivedatcurrent().compareTo(EMPTY_DATETIME_STRING) != 0){
			if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy hh:mm a", getdattimearrivedatcurrent())){
				sErrors += "Arrived at current site - '" + getdattimearrivedatcurrent() + "' is invalid.  ";
				bValid = false;
			}
		}
		if (getdattimeleftcurrent().compareTo(EMPTY_DATETIME_STRING) != 0){
			if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy hh:mm a", getdattimeleftcurrent())){
				sErrors += "Left current site - '" + getdattimeleftcurrent() + "' is invalid.  ";
				bValid = false;
			}
		}
		if (getdattimearrivedatnext().compareTo(EMPTY_DATETIME_STRING) != 0){
			if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy hh:mm a", getdattimearrivedatnext())){
				sErrors += "Arrived at next site - '" + getdattimearrivedatnext() + "' is invalid.  ";
				bValid = false;
			}
		}
       
		if(getDetailCount()> 0 && getstrimmedordernumber().compareToIgnoreCase("") == 0){
			LineArray.clear();
			sErrors += "Error [1536347860] - Can not add items or work performed codes unless the Work Order is associated with a real order number.  ";
			bValid = false;
		}
		remove_zero_qty_lines();
		
    	if (!bValid){
    		throw new Exception(sErrors);
    	}
    	
    	return;
    }
    
    private String checkConcurrency(
    		Connection conn, 
    		String sLid, 
    		String sTimestamp, 
    		String sLastRecordReadTimeStamp,
    		String sUserID,
    		String sUserFullName,
    		String sDBID,
    		ServletContext context
    		) throws Exception{

    	String sResult = "";
    	String SQL = "SELECT "
			+ "DATE_FORMAT(FROM_UNIXTIME(" + SMTableworkorders.TableName + "." + SMTableworkorders.ltimestamp + "), '%c/%e/%Y %h:%i:%S %p') AS LASTSAVEDDATE"
			+ ", " + SMTableworkorders.TableName + "." + SMTableworkorders.ltimestamp
			+ ", " + SMTableworkorders.TableName + "." + SMTableworkorders.slasteditedbyfullname
			+ " FROM " + SMTableworkorders.TableName
			+ " WHERE ("
				+ SMTableworkorders.TableName + "." + SMTableworkorders.lid + " = " + sLid
			+ ")"
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				String sLastEditUserFullName = rs.getString(SMTableworkorders.TableName + "." + SMTableworkorders.slasteditedbyfullname);
				
				long lLastRead = 0;
				try {
					lLastRead = Long.parseLong(sLastRecordReadTimeStamp);
				} catch (Exception e) {
					//Don't do anything, just make sure it's still ZERO:
					lLastRead = 0;
				}
				
				if (Long.toString(rs.getLong(SMTableworkorders.ltimestamp)).compareToIgnoreCase(sTimestamp) != 0){
					
					sResult += "Error - " + sLastEditUserFullName + " updated this work order"
						+ " at " + rs.getString("LASTSAVEDDATE")
						+ " since you first refreshed or updated it"
					;
					if (lLastRead != 0L){
						SimpleDateFormat timeFormat = new SimpleDateFormat("M/dd/YYY hh:mm:ss a");
						Date dateUserLastRead = new Date(lLastRead * 1000); //The unix timestamp is in seconds, the Date constructor wants it in milliseconds
						//dateUserLastRead.setTime(tsUserLastRead.getTime());
						String sUserLastRead = timeFormat.format(dateUserLastRead);
						sResult += " at " + sUserLastRead
						;
					}
					sResult += " - go <B><I>back</I></B> in your browser to clear this error and try again.";
					
					SMLogEntry log = new SMLogEntry(sDBID, context);
					log.writeEntry(
							sUserID, 
						SMLogEntry.LOG_OPERATION_SMWORKORDERCONCURRENCYERROR, 
						"Occurred when user '" + sUserID + " - " + sUserFullName + "' tried to update work order # " + getlid(), 
						sResult, 
						"[1519696952]"
					);
					
				}
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1519694821] checking concurrency with SQL: '" + SQL + "' - " + e.getMessage());
		}
		return sResult;
    }
    private void remove_zero_qty_lines() throws Exception{
		ArrayList<SMWorkOrderDetail> tempLineArray = new ArrayList<SMWorkOrderDetail>(0);

		// Copy the lines into the temporary array:
		for (int i = 0; i < LineArray.size(); i++) {
			tempLineArray.add((SMWorkOrderDetail) LineArray.get(i));
		}

		// Now copy back ONLY the non-zero qty lines:
		LineArray.clear();
		for (int i = 0; i < tempLineArray.size(); i++) {
			SMWorkOrderDetail line = (SMWorkOrderDetail) tempLineArray.get(i);
			BigDecimal bdQtyOrdered = new BigDecimal("0.0000");
			BigDecimal bdQtyAssigned = new BigDecimal("0.0000");
			if (line.getsbdquantity().compareToIgnoreCase("") != 0){
				try {
					bdQtyOrdered = new BigDecimal(line.getsbdquantity().replace(",",""));
				} catch (Exception e) {
					throw new Exception("Error [1434479509] getting qty ordered: '" + line.getsbdquantity() + "' on item '" + line.getsitemnumber() + "'.");
				}
			}
			if (line.getsbdqtyassigned().compareToIgnoreCase("") != 0){
				try {
					bdQtyAssigned = new BigDecimal(line.getsbdqtyassigned().replace(",",""));
				} catch (Exception e) {
					throw new Exception("Error [1434479510] getting qty assigned: '" + line.getsbdqtyassigned() + "' on item '" + line.getsitemnumber() + "'.");
				}
			}
			if (
				(bdQtyOrdered.compareTo(BigDecimal.ZERO) != 0)
				|| (bdQtyAssigned.compareTo(BigDecimal.ZERO) != 0)
				|| (line.getsdetailtype().compareToIgnoreCase(Integer.toString(SMTableworkorderdetails.WORK_ORDER_DETAIL_TYPE_WORKPERFORMED)) == 0)
			){
				LineArray.add(line);
			}
		}
		renumberLines();
    }
    public void validate_configure_screen_fields (Connection conn, String sUserID, String sUserFullName, String sDBID, ServletContext context) throws Exception{
    	
    	boolean bValid = true;
    	long lID = 0;
    	String sErrors = "";
    	
		try {
			lID = Long.parseLong(getlid());
		} catch (NumberFormatException e) {
			bValid = false;
			sErrors += "Invalid ID: '" + getlid() + "'.  ";
		}
    	if (lID < -1){
    		bValid = false;
    		sErrors += "Invalid ID: '" + getlid() + "'.  ";
    	}
    	
    	//Check to see if someone else has saved this work order since this user got it:
    	if (lID > 0){
    		String sConcurrencyCheck = checkConcurrency(conn, getlid(), getstimestamp(), getslastreadrecordtimestamp(), sUserID, sUserFullName, sDBID, context);
    		if (sConcurrencyCheck.compareToIgnoreCase("") != 0){
    			bValid = false;
    			sErrors += sConcurrencyCheck + "  .";
    		}
    	}
       	
    	//Must have a valid schedule date:
        if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", getsscheduleddate())){
        	bValid = false;
        	sErrors += "Invalid scheduled date: '" + getsscheduleddate() + "  .";
        }
    	
        //Update mechanic info here:
		try {
			lID = Long.parseLong(getmechid());
		} catch (NumberFormatException e) {
			bValid = false;
			sErrors += "Invalid technician ID: '" + getmechid() + "'.  ";
		}
    	if (lID < -1){
    		bValid = false;
    		sErrors += "Invalid technician ID: '" + getmechid() + "'.  ";
    	}
    	updateMechanicInfo(conn);
        
    	setmechanicsinitials(getmechanicsinitials().trim());
    	if (getmechanicsinitials().length() > SMTableworkorders.smechanicinitialsLength){
    		bValid = false;
    		sErrors += "Mechanic initials are limited to " + Integer.toString(SMTableworkorders.smechanicinitialsLength) + " characters.  ";
    	}
    	
    	setmechanicsname(getmechanicsname().trim());
    	if (getmechanicsname().length() > SMTableworkorders.smechanicnameLength){
    		bValid = false;
    		sErrors += "Mechanic name is limited to " + Integer.toString(SMTableworkorders.smechanicnameLength) + " characters.  ";
    	}
    	
    	setstrimmedordernumber(getstrimmedordernumber().trim());
    	if (getstrimmedordernumber().length() > SMTableworkorders.strimmedordernumberLength){
    		bValid = false;
    		sErrors += "Trimmed order number is limited to " + Integer.toString(SMTableworkorders.strimmedordernumberLength) + " characters.  ";
    	}
    	if (getstrimmedordernumber().compareToIgnoreCase("") != 0){
	    	SMOrderHeader smhead = new SMOrderHeader();
	    	smhead.setM_strimmedordernumber(getstrimmedordernumber());
	    	if (!smhead.load(conn)){
	    		bValid = false;
	    		sErrors += "Invalid order number '" + getstrimmedordernumber() + "'.  ";
	    	}
	    	if (smhead.isOrderCanceled()){
	    		bValid = false;
	    		sErrors += "Order number '" + getstrimmedordernumber() + "' has been canceled.  ";
	    	}
	    	if (smhead.getM_iOrderType().compareToIgnoreCase(Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE)) == 0){
	    		bValid = false;
	    		sErrors += "Order number '" + getstrimmedordernumber() + "' is a QUOTE and can't be placed on the schedule.  ";
	    	}
        }
    	
        //m_sqtyofhours
        BigDecimal bdQtyOfHours = new BigDecimal(0);
        try{
        	bdQtyOfHours = new BigDecimal(m_sqtyofhours);
            if (bdQtyOfHours.compareTo(BigDecimal.ZERO) < 0){
            	sErrors += "Number of hours cannot be negative: " + m_sqtyofhours + ".  ";
            	bValid = false;
            }
        }catch(NumberFormatException e){
        	sErrors += "Invalid number of hours: '" + m_sqtyofhours + "'.  ";
    		bValid = false;
        }
        
        //m_stravelhours
        BigDecimal bdTravelHours = new BigDecimal(0);
        try{
        	bdTravelHours = new BigDecimal(m_stravelhours);
            if (bdTravelHours.compareTo(BigDecimal.ZERO) < 0){
            	sErrors += "Number of travel hours cannot be negative: " + m_stravelhours + ".  ";
            	bValid = false;
            }
        }catch(NumberFormatException e){
        	sErrors += "Invalid number of travel hours: '" + m_stravelhours + "'.  ";
    		bValid = false;
        }
        
        //m_sbackchargehours
        BigDecimal bdBackchargeHours = new BigDecimal(0);
        try{
        	bdBackchargeHours = new BigDecimal(m_sbackchargehours);
            if (bdBackchargeHours.compareTo(BigDecimal.ZERO) < 0){
            	sErrors += "Number of backcharge hours cannot be negative: " + m_sbackchargehours + ".  ";
            	bValid = false;
            }
        }catch(NumberFormatException e){
        	sErrors += "Invalid number of backcharge hours: '" + m_sbackchargehours + "'.  ";
    		bValid = false;
        }
        
        m_ijoborder = m_ijoborder.trim();
        if (m_ijoborder.compareToIgnoreCase("") == 0){
        	m_ijoborder = "0";
        }
        try {
			Long lJobOrder = Long.parseLong(m_ijoborder);
			if (lJobOrder < 0){
				sErrors += "Job order ('" + m_ijoborder + "')cannot be less than zero.";
				bValid = false;
			}
		} catch (NumberFormatException e) {
			sErrors += "Invalid job order (sequence): '" + m_ijoborder + "'.  ";
    		bValid = false;
		}
        
        if (getmechid().compareTo("0")==0){
        	sErrors += "Technician not selected.  ";
        	bValid = false;
        }
        
        if (getsassistant().length() > SMTableworkorders.sassistantLength){
        	sErrors += "Assistant name is too long.  ";
        	bValid = false;
        }
        if (getsstartingtime().length() > SMTableworkorders.sstartingtimeLength){
        	sErrors += "Starting time is too long.  ";
        	bValid = false;
        }
        
        if (getsschedulecomment().length() > SMTableworkorders.sschedulecommentLength){
        	sErrors += "Schedule comment is too long.  ";
        	bValid = false;
        }
        
		if(getDetailCount()> 0 && getstrimmedordernumber().compareToIgnoreCase("") == 0){
			LineArray.clear();
			sErrors += "Error [1536347859] - Can not add items or work performed codes unless the Work Order is associated with a real order number.  ";
			bValid = false;
		}
		
        remove_zero_qty_lines();
        
    	if (!bValid){
    		throw new Exception(sErrors);
    	}
    	
    	return;
    }
    public void validate_acceptance_screen_fields (Connection conn, String sUserID, String sUserFullName, String sDBID, ServletContext context) throws Exception{
    	
    	boolean bValid = true;
    	long lID = 0;
    	String sErrors = "";
    	
		try {
			lID = Long.parseLong(getlid());
		} catch (NumberFormatException e) {
			bValid = false;
			sErrors += "Invalid ID: '" + getlid() + "'.  ";
		}
    	if (lID <= 0){
    		bValid = false;
    		sErrors += "Invalid ID: '" + getlid() + "'.  ";
    	}
    	
    	//Check to see if someone else has saved this work order since this user got it:
		String sConcurrencyCheck = checkConcurrency(conn, getlid(), getstimestamp(), getslastreadrecordtimestamp(), sUserID, sUserFullName, sDBID, context);
		if (sConcurrencyCheck.compareToIgnoreCase("") != 0){
			bValid = false;
			sErrors += sConcurrencyCheck + "  .";
			
			String sSQL = "SELECT"
				+ " " + SMTableworkorders.madditionalworkcomments
				+ " FROM " + SMTableworkorders.TableName
				+ " WHERE ("
					+ "(" + SMTableworkorders.lid + " = " + this.getlid() + ")"
				+ ")"
			;
			try{
				ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
				if (rs.next()){
					if (rs.getString(SMTableworkorders.madditionalworkcomments).trim().compareToIgnoreCase("") == 0){
						setsadditionalworkauthorized("0");
					}
				}
				rs.close();
			}catch(Exception e){
				throw new Exception("Error [1519695361] reading additional work comments with SQL '" + sSQL + "' - " + e.getMessage());
			}
		}

		if (getdattimesigned().compareTo(EMPTY_DATE_STRING) != 0){
	        if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", getdattimesigned())){
	        	bValid = false;
	        	sErrors += "Invalid date/time signed: '" + getdattimesigned() + "  .";
	        }
        }

    	setssignedbyname(getssignedbyname().trim());
    	if (getssignedbyname().length() > SMTableworkorders.ssignedbynameLength){
    		bValid = false;
    		sErrors += "Signed by name is limited to " + Integer.toString(SMTableworkorders.ssignedbynameLength) + " characters.  ";
    	}

    	if (
    			(getsiadditionalworkauthorized().compareToIgnoreCase("0") != 0)
    			&& (getsiadditionalworkauthorized().compareToIgnoreCase("1") != 0)
    	){
    		bValid = false;
    		sErrors += "Additional work authorized has an invalid value: '" + getsiadditionalworkauthorized() + "'.  ";
    	}
	
		if (!bValid){
    		throw new Exception(sErrors);
    	}
    	return;
    }
    
    private void updateMechanicInfo(Connection conn) throws SQLException{
    	
		if ((getmechid().compareToIgnoreCase("") == 0)
				|| (getmechid().compareToIgnoreCase("0") == 0)
				|| (getmechid().compareToIgnoreCase("-1") == 0)
		){
			setmechid(BLANK_MECHANIC_ID);
			return;
		}
    	String SQL = "SELECT * FROM " + SMTablemechanics.TableName
    	+ " WHERE ("
    		+ "(" + SMTablemechanics.lid + " = '" + getmechid() + "')"
    	+ ")"
    	;
    	
    	try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				if (getmechanicsinitials().compareToIgnoreCase("") == 0){
					setmechanicsinitials(rs.getString(SMTablemechanics.sMechInitial));
				}
				if (getmechanicsname().compareToIgnoreCase("") == 0){
					setmechanicsname(rs.getString(SMTablemechanics.sMechFullName));
				}
				if (getsassistant().compareToIgnoreCase("") == 0){
					setsassistant(rs.getString(SMTablemechanics.sAssistant));
				}
				if (getsstartingtime().compareToIgnoreCase("") == 0){
					setsstartingtime(rs.getString(SMTablemechanics.sstartingtime));
				}
			}
			rs.close();
		} catch (Exception e) {
			throw new SQLException("Error [1426779914] reading mechanics table in " + this.toString() + " - " + e.getMessage());
		}
    	return;
    }
    public void delete(ServletContext context, String sDBIB, String sUserID, String sUserFullName, int iSavingFromWhichScreen) throws Exception{
    	//If the work order has been posted, we cannot delete it:
    	if (isWorkOrderPosted()){
    		throw new Exception("This work order has been posted and cannot be deleted.");
    	}
    	SMLogEntry log = new SMLogEntry(sDBIB, context);
    	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBIB, 
    			"MySQL", 
    			SMUtilities.getFullClassName(this.toString()) + ".delete [1438863884] - user: " + sUserID + " - " + sUserFullName);
    	if (conn == null){
    		throw new Exception("Error [1392133194] getting connection.");
    	}
    	if (!clsDatabaseFunctions.start_data_transaction(conn)){
    		clsDatabaseFunctions.freeConnection(context, conn, "[1547067784]");
    		throw new Exception("Error [1392133195] starting data transaction.");
    	}
    	try {
			delete_without_transaction(conn, sUserID, log, iSavingFromWhichScreen, context);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067785]");
			throw new Exception("Error [1392133196] deleting - " + e.getMessage());
		}
    	if (!clsDatabaseFunctions.commit_data_transaction(conn)){
    		clsDatabaseFunctions.rollback_data_transaction(conn);
    		clsDatabaseFunctions.freeConnection(context, conn, "[1547067786]");
			throw new Exception("Error [1392133197] committing data transaction.");
    	}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547067787]");
    }
    public void delete_without_transaction (Connection conn, String sUserID, SMLogEntry log, int iSavingFromWhichScreen, ServletContext context) throws Exception{
    	
    	if (getlid().compareToIgnoreCase("-1") == 0){
    		throw new Exception("Work order cannot be deleted because it has not been loaded - ID = -1.");
    	}
    	
    	//Rules for deleting:
    	try{
    		delete_current_lines(sUserID, conn, log, iSavingFromWhichScreen, context);
    	}catch(Exception ex){
    		throw new Exception("Failed to delete lines."
    							+ ex.getMessage());
    	}
    	String SQL = "DELETE FROM " + SMTableworkorders.TableName
    		+ " WHERE ("
    			+ "(" + SMTableworkorders.lid + " = " + getlid() + ")"
    		+ ")"
    	;
    	try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1392133193] deleting work order with SQL: " + SQL + " - " + e.getMessage());
		}
		//Initialize the work order variables:
		initWorkOrderVariables();
    	log.writeEntry(
    			sUserID, 
    		SMLogEntry.LOG_OPERATION_SMDELETEWO, 
    		"Deleted WO " + getlid(), 
    		"Order #: " + getstrimmedordernumber()
    		+ ", technician: " + getmechanicsname()
    		,
    		"[1392154145]");
    	if (WebContextParameters.getLogWorkOrderUpdates(context).compareToIgnoreCase("True") == 0){
	    	log.writeEntry(
	    			sUserID, 
					SMLogEntry.LOG_OPERATION_SMWORKORDERSAVEDEBUGGING, 
					Integer.toString(iSavingFromWhichScreen) + " WOID :" + getlid() + ", isWorkOrderPosted() = " + isWorkOrderPosted(),
					"In delete_without_transaction - SQL: " + SQL,
					"[1429297152]")
			;
    	}
    }
    private void save_lines (Connection conn, String sUserFullName, String sUserID, SMLogEntry log, int iSavingFromWhichScreen, ServletContext context) throws Exception{

    	//System.out.println("[1447960559] - LineArray.size() = " + LineArray.size());
    	
    	//First validate all the lines before we delete, so we know they can be saved:
    	for (int i=0;i<LineArray.size();i++){
    		SMWorkOrderDetail line = (SMWorkOrderDetail) LineArray.get(i);
    		line.validate_line_fields(conn);
    	}
    	try{
    		delete_current_lines(sUserID, conn, log, iSavingFromWhichScreen, context);
    	}catch(Exception ex){
    		throw new Exception("Failed to delete lines."
    							+ ex.getMessage());
    	}
    	for (int i=0;i<LineArray.size();i++){
    		SMWorkOrderDetail line = (SMWorkOrderDetail) LineArray.get(i);
    		line.setsworkorderid(getlid());
    		line.save_without_data_transaction(conn, sUserID, log, iSavingFromWhichScreen, context);
    	}
    }

    private void delete_current_lines(String sUserID, Connection conn, SMLogEntry log, int iSavingFromWhichScreen, ServletContext context) throws Exception{
    	
    	String SQL = "DELETE FROM " + SMTableworkorderdetails.TableName 
    		+ " WHERE (" + SMTableworkorderdetails.lworkorderid + " = " + getlid() + ")";
    	try {
    		clsDatabaseFunctions.executeSQL(SQL, conn);
    	}catch (SQLException e){
    		if (WebContextParameters.getLogWorkOrderUpdates(context).compareToIgnoreCase("True") == 0){
	    	   	log.writeEntry(
	    	   			sUserID, 
	    				SMLogEntry.LOG_OPERATION_SMWORKORDERSAVEDEBUGGING, 
	    				Integer.toString(iSavingFromWhichScreen) + " WOID :" + getlid() + ", isWorkOrderPosted() = " + isWorkOrderPosted(),
	    				"In delete_current_lines EXCEPTION CAUGHT - SQL: " + SQL + " - " + e.getMessage(),
	    				"[1429297234]")
	    		;
    		}
    		throw new Exception("Error deleting work order details - " + e.getMessage());
    	}
    	if (WebContextParameters.getLogWorkOrderUpdates(context).compareToIgnoreCase("True") == 0){
		   	log.writeEntry(
		   			sUserID, 
					SMLogEntry.LOG_OPERATION_SMWORKORDERSAVEDEBUGGING, 
					Integer.toString(iSavingFromWhichScreen) + " WOID :" + getlid() + ", isWorkOrderPosted() = " + isWorkOrderPosted(),
					"In delete_current_lines - SQL: " + SQL,
					"[1429297235]")
			;
    	}
    }
    public boolean isWorkOrderPosted(){
    	return (getsposted().compareToIgnoreCase("1") == 0);
    }

    private void renumberLines(){
    	for (int i = 0; i < LineArray.size(); i++){
    		LineArray.get(i).setslinenumber(Integer.toString(i + 1));
    	}
    }
    public void updateLineNumbersAfterLineDeletion(
    		Connection conn, 
    		String sUserID,
    		SMLogEntry log, 
    		int iSavingFromWhichScreen, 
    		ServletContext context) throws Exception{
    	
    	load_lines (conn);
    	renumberLines();
    	for (int i = 0; i < LineArray.size(); i++){
    		try {
				LineArray.get(i).save_without_data_transaction(conn, sUserID, log, iSavingFromWhichScreen, context);
			} catch (Exception e) {
				throw new Exception("Error [1391791198] saving " + SMWorkOrderDetail.ParamObjectName + " - " + e.getMessage());
			}
    	}
    }
    public BigDecimal getCurrentExtendedPrice(int iDetailNumber, SMOrderHeader orderheader, Connection conn) throws Exception{
    	SMWorkOrderDetail wo_line = getDetailByIndex(iDetailNumber);
    	BigDecimal bdQtyShippedOnWorkOrder = new BigDecimal(wo_line.getsbdquantity().replace(",", ""));
    	//If the line is already on the order, use the current prices if there is one:
    	if (Integer.parseInt(wo_line.getsorderdetailnumber()) > 0){
    		SMOrderDetail orderdetail = orderheader.getOrderDetailByDetailNumber(wo_line.getsorderdetailnumber());
    		BigDecimal bdQtyShippedOnOrder = new BigDecimal(orderdetail.getM_dQtyShipped().replace(",",""));
    		BigDecimal bdUnitPriceOnOrder = new BigDecimal(orderdetail.getM_dOrderUnitPrice().replace(",", ""));
    		BigDecimal bdExtendedPriceOnOrder = new BigDecimal(orderdetail.getM_dExtendedOrderPrice().replace(",", ""));
    		//Next, if that line on the order has a matching quantity, AND already has an extended price, we'll use that extended price:
    		if (
    			(bdQtyShippedOnOrder.compareTo(bdQtyShippedOnWorkOrder) == 0)
    			&& (bdExtendedPriceOnOrder.compareTo(BigDecimal.ZERO) != 0)
    		){
    			return bdExtendedPriceOnOrder;
    		}
    		//If the line is on an order, but the qty doesn't match OR there's no extended price, then we look for a unit price:
    		else{
    			//If there's a unit price, we just extend that:
    			if (bdUnitPriceOnOrder.compareTo(BigDecimal.ZERO) != 0){
    				return (bdUnitPriceOnOrder.multiply(bdQtyShippedOnWorkOrder));
    			}
    		}
    	}
    	//Else if the line is NOT already on the order or it has no price on the order, we have to calculate the price:
    	//We use the already built function for calculating line prices to guarantee that our prices will be consistent:
    	SMOrderDetail orderdetail = new SMOrderDetail();
    	orderdetail.setM_dQtyShipped(clsManageBigDecimals.BigDecimalToScaledFormattedString(
    		SMTableorderdetails.dQtyShippedScale, bdQtyShippedOnWorkOrder));
    	orderdetail.setM_sItemNumber(wo_line.getsitemnumber());
    	orderheader.updateLinePrice(orderdetail, conn);
    	return new BigDecimal(orderdetail.getM_dExtendedOrderPrice().replace(",",""));
    }
    public void unpostWorkOrder_without_data_transaction(ServletContext context, String sDBIB, String sUserID, String sUserFullName, int iSavingFromWhichScreen) throws Exception{
		if(getsposted().compareToIgnoreCase("0") == 0){
			throw new Exception("This work order is not posted.");
		}
    	String SQL = "";
    	SQL = "UPDATE " 
    		+ SMTableworkorders.TableName 
    		+ " SET " + SMTableworkorders.dattimeposted + " = '0000-00-00'"
    		+ ", " + SMTableworkorders.iposted + " = 0"
    		+ ", " + SMTableworkorders.ltimestamp + " = UNIX_TIMESTAMP()"
    		+ ", " + SMTableworkorders.slasteditedbyfullname + " = '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
    		+ ", " + SMTableworkorders.llasteditedbyuserid + " = " + sUserID
    		+ " WHERE ("
    			+ "(" + SMTableworkorders.lid + " = " + getlid() + ")"
    		+ ")"
    		;
    	SMLogEntry log = new SMLogEntry(sDBIB, context);
		try {
			clsDatabaseFunctions.executeSQL(
				SQL, 
				context, 
				sDBIB, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".post_without_data_transaction - user: " + sUserID + " - " + sUserFullName);
		} catch (Exception e) {
			if (WebContextParameters.getLogWorkOrderUpdates(context).compareToIgnoreCase("True") == 0){
		    	log.writeEntry(
						sUserID, 
						SMLogEntry.LOG_OPERATION_SMWORKORDERSAVEDEBUGGING, 
						" WOID :" + getlid() + ", isWorkOrderPosted() = " + isWorkOrderPosted(),
						"In unpostWorkOrder_without_data_transaction EXCEPTION CAUGHT - SQL: " + SQL
						+ " - error: " + e.getMessage(),
						"[1429282132]")
				;
			}
			throw new Exception(this.toString() + "Could not update to UNpost " + SMTableworkorders.ObjectName + " - " + e.getMessage());
		}
		log.writeEntry(
			sUserID, 
			SMLogEntry.LOG_OPERATION_SMUNPOSTINGWORKORDER, 
			"WO #" + getlid() + ", Work order #" + getlid(), 
			read_out_debug_data(), 
			"[1398096098]"
		);
    	return;
    }
	public String getlsignatureboxwidth(){
		return m_lsignatureboxwidth;
	}
	public void setlsignatureboxwidth(String lsignatureboxwidth){
		m_lsignatureboxwidth = lsignatureboxwidth;
	}
	public String getlid(){
		return m_lid;
	}
	public void setlid(String sLid){
		m_lid = sLid;
	}
	public String getmechanicsinitials(){
		return m_smechanicinitials;
	}
	public void setmechanicsinitials(String sMechanicsInitials){
		m_smechanicinitials = sMechanicsInitials;
	}
	public String getmechanicsname(){
		return m_smechanicname;
	}
	public void setmechanicsname(String sMechanicsName){
		m_smechanicname = sMechanicsName;
	}
	public String getmechid(){
		return m_imechid;
	}
	public void setmechid(String smechid){
		m_imechid = smechid;
	}
	public String getdattimeposted(){
		return m_dattimeposted;
	}
	public void setdattimeposted(String sdattimePosted){
		m_dattimeposted = sdattimePosted;
	}
	public String getdattimedone(){
		return m_dattimedone;
	}
	public void setdattimedone(String sdattimeDone){
		m_dattimedone = sdattimeDone;
	}
	public String getstrimmedordernumber(){
		return m_strimmedordernumber;
	}
	public void setstrimmedordernumber(String sTrimmedOrderNumber){
		m_strimmedordernumber = sTrimmedOrderNumber;
	}
	public String getssignedbyname(){
		return m_ssignedbyname;
	}
	public void setssignedbyname(String sSignedByName){
		m_ssignedbyname = sSignedByName;
	}
	public String getdattimesigned(){
		return m_dattimesigned;
	}
	public void setdattimesigned(String sdattimeSigned){
		m_dattimesigned = sdattimeSigned;
	}
	public String getmsignature(){
		return m_msignature;
	}
	public void setmsignature(String sSignature){
		m_msignature = sSignature;
	}
	public String getmcomments(){
		return m_mcomments;
	}
	public void setmcomments(String mComments){
		m_mcomments = mComments;
	}
	public String getmdetailsheettext(){
		return m_mdetailsheettext;
	}
	public void setmdetailsheettext(String sdetailsheettext){
		m_mdetailsheettext = sdetailsheettext;
	}
	public String getmadditionalworkcomments(){
		return m_madditionalworkcomments;
	}
	public void setmadditionalworkcomments(String madditionalworkComments){
		m_madditionalworkcomments = madditionalworkComments;
	}
	public String getsiadditionalworkauthorized(){
		return m_sadditionalworkauthorized;
	}
	public void setsadditionalworkauthorized(String sadditionalworkauthorized){
		m_sadditionalworkauthorized = sadditionalworkauthorized;
	}
	public String getsimported(){
		return m_simported;
	}
	public void setsimported(String sImported){
		m_simported = sImported;
	}
	public String getsposted(){
		return m_sposted;
	}
	public void setsposted(String sPosted){
		m_sposted = sPosted;
	}
	public String getstimestamp(){
		return m_stimestamp;
	}
	public void setstimestamp(String sTimestamp){
		m_stimestamp = sTimestamp;
	}
	public String getslastreadrecordtimestamp(){
		return m_slastreadrecordtimestamp;
	}
	public void setslastreadrecordtimestamp(String sTimestamp){
		m_slastreadrecordtimestamp = sTimestamp;
	}
	public String getminstructions(){
		return m_minstructions;
	}
	public void setminstructions(String mInstructions){
		m_minstructions = mInstructions;
	}
	public String getmmanagernotes(){
		return m_mmanagernotes;
	}
	public void setmmanagernotes(String mManagernotes){
		m_mmanagernotes = mManagernotes;
	}
	public String getdattimeleftprevious() {
		return m_sdattimeleftprevious;
	}
	public void setdattimeleftprevious(String m_dattimeleftprevious) {
		m_sdattimeleftprevious = m_dattimeleftprevious;
	}
	public String getdattimearrivedatcurrent() {
		return m_sdattimearrivedatcurrent;
	}
	public void setdattimearrivedatcurrent(String m_dattimearrivedatcurrent) {
		m_sdattimearrivedatcurrent = m_dattimearrivedatcurrent;
	}
	public String getdattimeleftcurrent() {
		return m_sdattimeleftcurrent;
	}
	public void setdattimeleftcurrent(String m_dattimeleftcurrent) {
		m_sdattimeleftcurrent = m_dattimeleftcurrent;
	}
	public String getdattimearrivedatnext() {
		return m_sdattimearrivedatnext;
	}
	public void setdattimearrivedatnext(String m_dattimearrivedatnext) {
		m_sdattimearrivedatnext = m_dattimearrivedatnext;
	}
	public void setsassistant (String sAssistant){
		m_sassistant = sAssistant;
	}
	public String getsassistant (){
		return m_sassistant;
	}
	public void setsstartingtime (String sStartingTime){
		m_sstartingtime = sStartingTime;
	}
	public String getsstartingtime (){
		return m_sstartingtime;
	}
    public void setsscheduleddate (String sScheduleDate){
    	m_sscheduleddate = sScheduleDate;
    }
    public String getsscheduleddate (){
    	return m_sscheduleddate;
    }
    public void setsqtyofhours (String sQtyOfHours){
    	m_sqtyofhours = sQtyOfHours;
    }
    public String getsqtyofhours (){
    	return m_sqtyofhours;
    }
    public void setstravelhours (String sTravelHours){
    	m_stravelhours = sTravelHours;
    }
    public String getstravelhours (){
    	return m_stravelhours;
    }
    public void setsbackchargehours (String sBackchargeHours){
    	m_sbackchargehours = sBackchargeHours;
    }
    public String getsbackchargehours (){
    	return m_sbackchargehours;
    }
	public void setsworkdescription (String sWorkDescription){
		m_sworkdescription = sWorkDescription;
	}
	public String getsworkdescription (){
		return m_sworkdescription;
	}
	public void setslasteditedbyfullname (String sLastEditedByFullName){
		m_slasteditedbyfullname = sLastEditedByFullName;
	}
	public String getslasteditedbyfullname (){
		return m_slasteditedbyfullname;
	}
	public void setsdattimelastedit (String sdattimeLastEdit){
		m_sdatetimelastedit = sdattimeLastEdit;
	}
	public String getsdattimelastedit (){
		return m_sdatetimelastedit;
	}
	public void setsschedulecomment (String sScheduleComment){
		m_sschedulecomment = sScheduleComment;
	}
	public String getsschedulecomment (){
		return m_sschedulecomment;
	}
	public void setsdattimelastschedulechange (String sdattimelastschedulechange){
		m_dattimelastschedulechange = sdattimelastschedulechange;
	}
	public String getsdattimelastschedulechange (){
		return m_dattimelastschedulechange;
	}
	public void setsschedulechangedbyfullname (String sschedulechangedbyfullname){
		m_sschedulechangedbyfullname = sschedulechangedbyfullname;
	}
	public String getsschedulechangedbyfullname (){
		return m_sschedulechangedbyfullname;
	}
	public void setsjoborder (String sJobOrder){
		m_ijoborder = sJobOrder;
	}
	public String getsjoborder (){
		return m_ijoborder;
	}
	public boolean isOrderNew(){
		return getlid().compareToIgnoreCase("-1") == 0;
	}
	public int getDetailCount(){
		return LineArray.size();
	}
	public void setsnumberofdays (String snumberofdays){
		m_snumberofdays = snumberofdays;
	}
	public String getsnumberofdays (){
		return m_snumberofdays;
	}
	public void setsnumberofitems (String snumberofitems){
		m_snumberofitems = snumberofitems;
	}
	public String getsnumberofitems (){
		return m_snumberofitems;
	}
	public void setsgdoclink (String sgdoclink){
		m_sgdoclink = sgdoclink;
	}
	public String getsgdoclink (){
		return m_sgdoclink;
	}
	

	public void setmdbaaddress (String mdbaaddress){
		m_mdbaaddress = mdbaaddress;
	}
	public String getmdbaaddress (){
		return m_mdbaaddress;
	}
	public void setmdbaremittoaddress (String mdbaremittoaddress){
		m_mdbaremittoaddress = mdbaremittoaddress;
	}
	public String getmdbaremittoaddress (){
		return m_mdbaremittoaddress;
	}

    public String read_out_debug_data(){
    	String sResult = "  ** SMWorkOrderHeader read out: ";
    	sResult += "\nDate time done: " + getdattimedone();
    	sResult += "\nDate time posted: " + getdattimeposted();
    	sResult += "\nDate time signed: " + getdattimesigned();
    	sResult += "\nID: " + getlid();
    	sResult += "\nComments: " + getmcomments();
    	sResult += "\nMechanic initials: " + getmechanicsinitials();
    	sResult += "\nMechanic name: " + getmechanicsname();
    	sResult += "\nSignature: " + getmsignature();
    	sResult += "\nSigned by name: " + getssignedbyname();
    	sResult += "\nTrimmed order number: " + getstrimmedordernumber();
    	sResult += "\nAdditional work comments: " + getmadditionalworkcomments();
    	sResult += "\nDetail sheet text: " + getmdetailsheettext();
    	sResult += "\nAdditional work authorized: " + getsiadditionalworkauthorized();
    	sResult += "\nImported: " + getsimported();
    	sResult += "\nPosted: " + getsposted();
    	sResult += "\nTimestamp: " + getstimestamp();
    	sResult += "\nInstructions: " + getminstructions();
    	sResult += "\nManager Notes: " + getmmanagernotes();
    	sResult += "\nLeft previous: " + getdattimeleftprevious();
    	sResult += "\nArrived at current: " + getdattimearrivedatcurrent();
    	sResult += "\nLeft current: " + getdattimeleftcurrent();
    	sResult += "\nArrived at next: " + getdattimearrivedatnext();
    	sResult += "\nScheduled date: " + getsscheduleddate();
    	sResult += "\nQty of hours: " + getsqtyofhours();
    	sResult += "\nWork Description: " + getsworkdescription();
    	sResult += "\nTravel hours: " + getstravelhours();
    	sResult += "\nBackcharge hours: " + getsbackchargehours();
    	sResult += "\nLast edited by: " + getslasteditedbyfullname();
    	sResult += "\nDate time of last edit: " + getsdattimelastedit();
    	sResult += "\nJob order: " + getsjoborder();
    	sResult += "\nSchedule comment: " + getsschedulecomment();
    	sResult += "\nSchedule last changed: " + getsdattimelastschedulechange();
    	sResult += "\nSchedule changed by: " + getsschedulechangedbyfullname();
    	sResult += "\nGDoc Link: " + getsgdoclink();
    	sResult += "\nSignature box width: " + getlsignatureboxwidth();
    	sResult += " --DETAIL LINES:--";
    	for (int i = 0; i < LineArray.size(); i++){
    		sResult += LineArray.get(i).read_out_debug_data();
    	}
    	return sResult;
    }
    private void initWorkOrderVariables(){
		m_lid  = "-1";
		m_smechanicinitials = "";
		m_smechanicname = "";
		m_imechid = "0";
		m_dattimeposted = EMPTY_DATETIME_STRING;
		m_dattimedone = EMPTY_DATE_STRING;
		m_strimmedordernumber = "";
		m_ssignedbyname = "";
		m_dattimesigned = EMPTY_DATE_STRING;
		m_mdetailsheettext = "";
		m_madditionalworkcomments = "";
		m_msignature = "";
		m_mcomments = "";
		m_sadditionalworkauthorized = "0";
		m_simported = "0";
		m_sposted = "0";
		m_stimestamp = "0";
		m_minstructions = "";
		m_mmanagernotes = "";
		m_sdattimeleftprevious = "0000-00-00 00:00:00";
		m_sdattimearrivedatcurrent = "0000-00-00 00:00:00";
		m_sdattimeleftcurrent = "0000-00-00 00:00:00";
		m_sdattimearrivedatnext = "0000-00-00 00:00:00";
		m_sassistant = "";
		m_sstartingtime = "";
		m_sscheduleddate = ""; //Always stored as /MM/dd/yyyy
		m_sqtyofhours = "0";;
		m_sworkdescription = "";
		m_stravelhours = "0";
		m_sbackchargehours = "0";
		m_slasteditedbyfullname = "";
		m_sdatetimelastedit = "0000-00-00 00:00:00";
		m_ijoborder = "0";
		m_sschedulecomment = "";
		m_dattimelastschedulechange = EMPTY_DATETIME_STRING;
		m_sschedulechangedbyfullname = "";
		m_sgdoclink = "";
		m_snumberofitems = COMMONLY_USED_ITEMS_SEARCH_NO_OF_ITEMS;
		m_snumberofdays = COMMONLY_USED_ITEMS_SEARCH_NO_OF_DAYS;
		m_lsignatureboxwidth = "0";
		m_mdbaaddress = "";
		m_mdbaremittoaddress = "";
		m_sdbaworkorderlogo = "";
		
		//This value indicates that this mech
		LineArray = new ArrayList<SMWorkOrderDetail>(0);
		super.initVariables();
    }
	public static final String createDetailSheetsTable(
			SMMasterEditEntry sm,
			SMWorkOrderHeader workorder,
			ServletContext context
			) throws Exception{
		String s = "";

		s += "<TABLE class = \" innermost \" style=\" title:DetailSheetsTable; background-color: "
				+ DETAIL_SHEET_TABLE_BG_COLOR + "; \" width=100% >\n";

		//Drop down list:
		s += "<TR><TD>";
		s += "<U><B>Detail sheets:&nbsp;</B></U>";
		try{
	        String sSQL = "SELECT * FROM " 
	        	+ SMTableworkorderdetailsheets.TableName
	        	+ " ORDER BY " + SMTableworkorderdetailsheets.sname 
	        ;
	        ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, context, sm.getsDBID());
	     	s += "<SELECT NAME=\"" + ADD_DETAIL_SHEET_DROPDOWN_NAME + "\">";
        	while (rs.next()){
        		s += "<OPTION VALUE=\"" + Long.toString(rs.getLong(SMTableworkorderdetailsheets.lid)) + "\">"
        			+ rs.getString(SMTableworkorderdetailsheets.sname)
        		;
        		if (rs.getInt(SMTableworkorderdetailsheets.itype) == SMTableworkorderdetailsheets.DETAIL_SHEET_TYPE_HTML){
        			s += " (" + SMTableworkorderdetailsheets.WEB_ENTRY_FORM_LABEL + ")";
        		}
        	}
        	rs.close();
		}catch (SQLException ex){
			throw new Exception("Error [1402344721] listing detail sheets - " + ex.getMessage());
		}
    	//End the drop down list:
        s += "</SELECT>";
        s += "&nbsp;" + adddetailsheetButton();
        s += "</TD></TR>";

        //Detail sheet text:
		s += "<TR>";
		s += "<TD class=\" fieldcontrol \" >"
			+ "<TEXTAREA NAME=\"" + SMWorkOrderHeader.Parammdetailsheettext + "\""
			+ " rows=\"" + NUMBER_OF_DETAIL_SHEET_ROWS_DISPLAYED + "\""
			//+ " cols=\"" + Integer.toString(iCols) + "\""
			+ "style=\"width:100%\""
			+ " id = \"" + SMWorkOrderHeader.Parammdetailsheettext + "\""
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ workorder.getmdetailsheettext().replace("\"", "&quot;")
			+ "</TEXTAREA>"
			+ "</TD>"
		;
		s += "</TR>";

		//Close the table:
		s += "</TABLE style = \" title:DetailSheetsTable; \">\n";
		return s;
	}
	private static String adddetailsheetButton(){
		return "<button type=\"button\""
			+ " value=\"" + ADD_DETAIL_SHEET_BUTTON_LABEL + "\""
			+ " name=\"" + ADD_DETAIL_SHEET_BUTTON_LABEL + "\""
			+ " onClick=\"adddetailsheet();\">"
			+ ADD_DETAIL_SHEET_BUTTON_LABEL
			+ "</button>\n"
		;
	}
	public void addDetailSheet(
			HttpServletRequest req, 
			SMMasterEditAction sm, 
			ServletContext context, 
			int iSavingFromWhichScreen) throws Exception{
		
		if (isWorkOrderPosted()){
			throw new Exception("Cannot add detail sheet to a posted work order.");
		}
		//Add the detail sheet to the end of the 'mdetailsheettext' field:
		String sWorkOrderDetailSheetID = clsManageRequestParameters.get_Request_Parameter(SMWorkOrderHeader.ADD_DETAIL_SHEET_DROPDOWN_NAME, req);
		if (sWorkOrderDetailSheetID.compareToIgnoreCase("") == 0){
			throw new Exception("You must select a detail sheet from the drop down list.");
		}
		String SQL = "";
		try {
			SQL = "SELECT"
				+ " " + SMTableworkorderdetailsheets.mtext
				+ " FROM " + SMTableworkorderdetailsheets.TableName
				+ " WHERE ("
					+ "(" + SMTableworkorderdetailsheets.lid + " = " + sWorkOrderDetailSheetID + ")"
				+ ")"
			;
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				context,
				sm.getsDBID(), 
				"MySQL", 
				"SMWorkOrderHeader.addDetailSheet - user: " + sm.getUserID()
				+ " - "
				+ sm.getFullUserName()
					);
			if (rs.next()){
				setmdetailsheettext(getmdetailsheettext() + "\n" + rs.getString(SMTableworkorderdetailsheets.mtext));
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error opening workorderdetailsheets table with SQL: " + SQL + " - " + e.getMessage());
		}

		try {
			saveDetailSheet(
				context, 
				sm.getsDBID(), 
				sm.getUserID(), 
				sm.getFullUserName(), 
				iSavingFromWhichScreen
			);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		return;
	}
	public void addCustomDetailSheetText(
		String sCustomDetailSheetText, 
		ServletContext context, 
		String sDBID, 
		String sUserID, 
		String sUserFullName, 
		int iSavingFromWhichScreen) throws Exception{
		
		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				context, 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString() + ".addCustomDetailSheet [1438863885] - user: " + sUserID + " - " + sUserFullName)
			);
		} catch (Exception e1) {
			throw new Exception("Error [1437062319] - " + e1.getMessage());
		}
		SMLogEntry log = new SMLogEntry(conn);
		setmdetailsheettext(getmdetailsheettext().trim() + sCustomDetailSheetText);
		
		String SQL = " UPDATE " + SMTableworkorders.TableName + " SET"
	    	+ " " + SMTableworkorders.ltimestamp + " = UNIX_TIMESTAMP()"
	    	+ ", " + SMTableworkorders.slasteditedbyfullname + " = '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
	    	+ ", " + SMTableworkorders.llasteditedbyuserid + " = " + sUserID
	    	+ ", " + SMTableworkorders.mdetailsheettext + " = '" + clsDatabaseFunctions.FormatSQLStatement(getmdetailsheettext()) + "'"
			+ " WHERE (" 
				+ SMTableworkorders.lid + " = " + getlid() 
			+ ")";

		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			if (WebContextParameters.getLogWorkOrderUpdates(context).compareToIgnoreCase("True") == 0){
				log.writeEntry(
					sUserID, 
					SMLogEntry.LOG_OPERATION_SMWORKORDERSAVEDEBUGGING, 
					Integer.toString(iSavingFromWhichScreen) + " ADD CUSTOM DETAIL SHEET WOID :" + getlid() + ", isWorkOrderPosted() = " + isWorkOrderPosted(),
					"In addCustomDetailSheetText EXCEPTION CAUGHT - SQL: " + SQL + " - " + e.getMessage(),
					"[1437062545]")
				;
			}
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067782]");
			throw new Exception("Error [1430410296] adding detail sheet with SQL: " + SQL + " - " + e.getMessage() + ".");
		}
		if (WebContextParameters.getLogWorkOrderUpdates(context).compareToIgnoreCase("True") == 0){
			log.writeEntry(
				sUserID, 
				SMLogEntry.LOG_OPERATION_SMWORKORDERSAVEDEBUGGING, 
				Integer.toString(iSavingFromWhichScreen) + " ADD CUSTOM DETAIL SHEET WOID :" + getlid() + ", isWorkOrderPosted() = " + isWorkOrderPosted(),
				"In addCustomDetailSheetText - SQL: " + SQL,
				"[1437062546]")
			;
		}
		clsDatabaseFunctions.freeConnection(context, conn, "[1547067783]");
    	return;
	}
		public String createWorkDescription(
			String sJobCostWorkDescription,
			String sWorkOrderComments,
			String sWorkOrderAdditionalWorkComments,
			String sWorkOrderDetailSheet
			) {
		String s = "";
		if (sJobCostWorkDescription != null){
			s += sJobCostWorkDescription.replace("\n", "<BR>");
		}
		String sMechanicComments = sWorkOrderComments;
		if (sMechanicComments == null){
			sMechanicComments = "";
		}
		if (sMechanicComments.compareToIgnoreCase("") !=0){
			if (s.compareToIgnoreCase("") != 0){
				s += "<BR>";
			}
			s += "<B><I>Technician comments for customer: </I></B>" + sMechanicComments.replace("\n", "<BR>"); 
		}
		String sWorkOrderAdditionalWork = sWorkOrderAdditionalWorkComments;
		if (sWorkOrderAdditionalWork == null){
			sWorkOrderAdditionalWork = "";
		}
		if (sWorkOrderAdditionalWork.compareToIgnoreCase("") !=0){
			if (s.compareToIgnoreCase("") != 0){
				s += "<BR>";
			}
			s += "<B><I>Add'l work required: </I></B>" + sWorkOrderAdditionalWork.replace("\n", "<BR>"); 
		}
		String sDetailSheetText = sWorkOrderDetailSheet;
		if (sDetailSheetText == null){
			sDetailSheetText = "";
		}
		if (sDetailSheetText.compareToIgnoreCase("") !=0){
			if (s.compareToIgnoreCase("") != 0){
				s += "<BR>";
			}
			s += "<B><I>Detail sheet: </I></B>" + sDetailSheetText.replace("\n", "<BR>"); 
		}
		return s;
	}
		
	public void add_most_recent_lines(SMWorkOrderHeader woHeader,
									  ServletContext context, 
									  String Conf,
									  String sUserID,
									  String sUserFullName) throws Exception{
		
		if (!woHeader.load(Conf, sUserFullName, context)){
			//TJR - 9/26/2017 - Don't hold up the show, but record this error temporarily, just for diagnostic purposes:
			clsServletUtilities.sysprint(
				this.toString(),
				sUserID + " - " + sUserFullName, 
				"[1506447274] - error loading WO '" + woHeader.getlid() + "' - " + woHeader.getErrorMessages()
			);
		}
		
		//Validate number of items and number of days
		try{
			Integer.parseInt(woHeader.getsnumberofitems());
		}catch(Exception e){
			throw new Exception("No. of items '" + woHeader.getsnumberofitems() + "' is not valid");
		}
		try{
			Integer.parseInt(woHeader.getsnumberofdays());
		}catch(Exception e){
			throw new Exception("No. of days '" + woHeader.getsnumberofdays() + "' is not valid");
		}	
	
		String SQL = "SELECT "
			+ SMTableworkorderdetails.TableName + "." + SMTableworkorderdetails.sitemnumber + ","
			+ SMTableworkorderdetails.TableName + "." + SMTableworkorderdetails.sitemdesc + ","
			+ SMTableworkorderdetails.TableName + "." + SMTableworkorderdetails.sunitofmeasure + ","
			+ "COUNT(" + SMTableworkorderdetails.TableName + "." + SMTableworkorderdetails.sitemnumber + ") AS COUNTS"
			+ " FROM " + SMTableworkorderdetails.TableName
			+ " LEFT JOIN " + SMTableworkorders.TableName 
			+ " ON " +  SMTableworkorderdetails.TableName + "." + SMTableworkorderdetails.lworkorderid
			+ " = " +  SMTableworkorders.TableName + "." + SMTableworkorders.lid					
			+ " LEFT JOIN " + SMTableicitems.TableName 
			+ " ON " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber
			+ " = " + SMTableworkorderdetails.TableName + "." + SMTableworkorderdetails.sitemnumber
			+ " WHERE ("
				+ " (" + SMTableworkorders.TableName + "." + SMTableworkorders.imechid + "=" + woHeader.getmechid() + ")"
				+ " AND (" + SMTableworkorderdetails.TableName + "." + SMTableworkorderdetails.sitemnumber + "> '')"
				+ " AND (DATEDIFF(NOW()," + SMTableworkorders.TableName + "." + SMTableworkorders.dattimedone + ")<=" + woHeader.getsnumberofdays()	 + ")"
				+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sDedicatedToOrderNumber + "='')"
			+ ")"
			+ " GROUP BY " + SMTableworkorderdetails.TableName + "." + SMTableworkorderdetails.sitemnumber
			+ " ORDER BY COUNTS DESC" 
		//+ " LIMIT " + woHeader.getsnumberofitems()	
	;
		
		//load order to get default location code for detail
		SMOrderHeader order = new SMOrderHeader();
		order.setM_strimmedordernumber(woHeader.getstrimmedordernumber());
		order.load(context, Conf, sUserID, sUserFullName);
		
		//Set the count to the number of items we want to see:
		int iRemainingCount = Integer.parseInt(woHeader.getsnumberofitems());
		try {
			ResultSet mostRecentItemDetail = clsDatabaseFunctions.openResultSet(
					SQL, 
					context, 
					Conf, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString()) + ".add_most_recent_lines - user: " 
					+ sUserID
					+ " - "
					+ sUserFullName
					);

			//If there's still another record, and we still haven't added the number we want to show, then keep looping:
			while ((mostRecentItemDetail.next()) && (iRemainingCount > 0)){
				SMWorkOrderDetail detailline = new SMWorkOrderDetail();
				detailline.setsitemdesc(mostRecentItemDetail.getString(SMTableworkorderdetails.sitemdesc));
				detailline.setsitemnumber(mostRecentItemDetail.getString(SMTableworkorderdetails.sitemnumber));
				detailline.setsuom(mostRecentItemDetail.getString(SMTableworkorderdetails.sunitofmeasure));
				detailline.setsdetailtype(Integer.toString(SMTableworkorderdetails.WORK_ORDER_DETAIL_TYPE_ITEM));
				detailline.setsorderdetailnumber("-1");
				detailline.setslid("0");
				detailline.setslocationcode(order.getM_sLocation());
				
				boolean bDuplicateDetailLine = false;
				for(int i = 0; i < woHeader.getDetailCount(); i++){
					if(detailline.getsitemnumber().compareToIgnoreCase(woHeader.getDetailByIndex(i).getsitemnumber()) == 0){
						bDuplicateDetailLine = true;					
					}
				}
				if(!bDuplicateDetailLine){
					woHeader.add_line(detailline);
					iRemainingCount--;
				}
			}
			mostRecentItemDetail.close();
		}catch(Exception e){
			throw new Exception("Error [1456849496] reading most commonly used items with SQL: " + SQL + " - " + e.getMessage());
		}
	}
	
	public String getSignatureBoxWidth(String sDBID, ServletContext context, String sUsername) throws Exception{
		SMOption smoptions = new SMOption();
		try {
			smoptions.load(sDBID, context, sUsername);
		} catch (Exception e) {
			throw new SQLException ("Error loading smoptions for siganture box size.");
		}
		String sSignatureboxwidth = smoptions.getisignatureboxwidth();
		return sSignatureboxwidth;
	}
	
	public String getSignatureBoxHeight(String sDBID, ServletContext context, String sUsername) throws Exception{
		int iSignatureWidth = Integer.parseInt(getSignatureBoxHeight(sDBID, context, sUsername ));
		return Integer.toString((int) (Math.round(iSignatureWidth/SMTablesmoptions.SIGNATURE_BOX_WIDTH_TO_HEIGHT_RATIO)));
	}
	public String getSdbaworkorderlogo() {
		return m_sdbaworkorderlogo;
	}
	public void setsdbaworkorderlogo(String sdbaworkorderlogo) {
		this.m_sdbaworkorderlogo = sdbaworkorderlogo;
	}
}