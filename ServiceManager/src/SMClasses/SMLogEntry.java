package SMClasses;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;

import javax.servlet.ServletContext;

import SMDataDefinition.SMTablesystemlog;
import SMDataDefinition.SMTableusers;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsStringFunctions;
import smcontrolpanel.SMUtilities;

public class SMLogEntry {

	public final static String LOG_OPERATION_RECEIPTFLAGGEDASNONINVENTORY = "NONINVRCPTFLAGGED";
	public final static String LOG_OPERATION_FIXEDASSETS = "FIXEDASSETS";
	public final static String LOG_OPERATION_FIXEDASSETSTRANSACTIONCLEARING = "FATRANSACTIONCLEARING";
	public final static String LOG_OPERATION_CANCELORDER = "CANCELEDORDER";
	public final static String LOG_OPERATION_UNCANCELORDER = "UNCANCELEDORDER";
	public final static String LOG_OPERATION_ARACTIVITYREPORT = "ARACTIVITYREPORT";
	public final static String LOG_OPERATION_ARAGING = "ARAGING";
	public final static String LOG_OPERATION_ARCLEARMONTHLYSTATISTICS= "ARCLEARMONTHLYSTATISTICS";
	public final static String LOG_OPERATION_ARINVOICEIMPORT= "ARINVOICEIMPORT";
	public final static String LOG_OPERATION_ARRENUMBER = "ARRENUMBER";
	public final static String LOG_OPERATION_ARDISPLAYCUSTOMERINFO= "ARDISPLAYCUSTOMERINFO";
	public final static String LOG_OPERATION_ARLISTCUSTOMERSONHOLD = "ARLISTCUSTOMERSONHOLD";
	public final static String LOG_OPERATION_ARLISTINACTIVECUST= "ARLISTINACTIVECUST";
	public final static String LOG_OPERATION_ARMISCCASHREPORT = "ARMISCCASHREPORT";
	public final static String LOG_OPERATION_ARPOSTINGJOURNAL = "ARPOSTINGJOURNAL";
	public final static String LOG_OPERATION_ARPRINTSTATEMENT = "ARPRINTSTATEMENT";
	public final static String LOG_OPERATION_ARPRINTCALLSHEETS = "ARPRINTCALLSHEETS";
	public final static String LOG_OPERATION_ARSETINACTIVECUSTOMER = "ARSETINACTIVECUSTOMER";
	public final static String LOG_OPERATION_ARVIEWCHRONLOG = "ARVIEWCHRONLOG";
	public final static String LOG_OPERATION_SMAVERAGEMUREPORT= "SMAVERAGEMUREPORT";
	public final static String LOG_OPERATION_SMBIDFOLLOWUPREPORT = "SMBIDFOLLOWUPREPORT";
	public final static String LOG_OPERATION_SMBIDREPORT = "SMBIDREPORT";
	public final static String LOG_OPERATION_SMPENDINGBIDSREPORT = "SMPENDINGBIDSREPORT";
	public final static String LOG_OPERATION_SMCANCELEDJOBSREPORT = "SMCANCELEDJOBSREPORT";
	public final static String LOG_OPERATION_SMCREATECREDITNOTE = "SMCREATECREDITNOTE";
	public final static String LOG_OPERATION_SMCREATECREDITNOTEPREVIEW = "SMCREATECREDITNOTEPREVIEW";
	public final static String LOG_OPERATION_SMCREATEMULTIPLEINVOICES = "SMCREATEMULTIPLEINVOICES";
	public final static String LOG_OPERATION_SMCRITICALDATEREPORT = "SMCRITICALDATEREPORT";
	public final static String LOG_OPERATION_SMCUSTOMITEMSONHANDNOTONSALESORDER = "SMCUSTOMITEMSONHANDNOTONSALESORDER";
	public final static String LOG_OPERATION_SMQUERY = "SMQUERY";
	public final static String LOG_OPERATION_SMDISPLAYJOBCOSTINFO = "SMDISPLAYJOBCOSTINFO";
	public final static String LOG_OPERATION_SMDISPLAYORDERINFORMATION= "SMDISPLAYORDERINFORMATION";
	public final static String LOG_OPERATION_SMJOBCOSTDAILYREPORT = "SMJOBCOSTDAILYREPORT";
	public final static String LOG_OPERATION_OTHER = "OTHER";
	public final static String LOG_OPERATION_SMLISTNEARBYORDERREPORT = "SMLISTNEARBYORDERREPORT";
	//public final static String LOG_OPERATION_SMLISTORDERSFORSCHEDULING = "SMLISTORDERSFORSCHEDULING";
	public final static String LOG_OPERATION_SMMONTHLYBILLINGREPORT = "SMMONTHLYBILLINGREPORT";
	public final static String LOG_OPERATION_SMPRINTMONTHLYSALESREPORT = "SMPRINTMONTHLYSALESREPORT";
	public final static String LOG_OPERATION_SMOPENORDERSREPORT = "SMOPENORDERSREPORT";
	public final static String LOG_OPERATION_SMCHANGEUSERPASSWORD = "SMCHANGEUSERPASSWORD";
	public final static String LOG_OPERATION_SMORDERSOURCELISTING = "SMORDERSOURCELISTING";
	public final static String LOG_OPERATION_SMPRINTDELIVERYTICKET = "SMPRINTDELIVERYTICKET";
	public final static String LOG_OPERATION_SMPRINTINVOICE = "SMPRINTINVOICE";
	public final static String LOG_OPERATION_SMPRINTINVOICEAUDIT = "SMPRINTINVOICEAUDIT";
	public final static String LOG_OPERATION_SMPRINTPREINVOICE = "SMPRINTPREINVOICE";
	public final static String LOG_OPERATION_SMPRODUCTIVITYREPORT = "SMPRODUCTIVITYREPORT";
	public final static String LOG_OPERATION_SMRECALIBRATECOUNTERS = "SMRECALIBRATECOUNTERS";
	public final static String LOG_OPERATION_SMSALESCONTACTREPORT = "SMSALESCONTACTREPORT";
	public final static String LOG_OPERATION_SMPRINTUNBILLEDCONTRACTREPORT = "SMPRINTUNBILLEDCONTRACTREPORT";
	public final static String LOG_OPERATION_SMEMAILUNBILLEDCONTRACTREPORT = "SMEMAILUNBILLEDCONTRACTREPORT";
	public final static String LOG_OPERATION_SMVIEWTRUCKSCHEDULE = "SMVIEWTRUCKSCHEDULE";
	public final static String LOG_OPERATION_SMWAGESCALEREPORT = "SMWAGESCALEREPORT";
	public final static String LOG_OPERATION_SMWARRANTYSTATUSREPORT = "SMWARRANTYSTATUSREPORT";
	public final static String LOG_OPERATION_SMTAXBYCATEGORYREPORT = "SMTAXBYCATEGORYREPORT";
	public final static String LOG_OPERATION_ICPOASSIGNMENT = "ICPOASSIGNMENT";
	public final static String LOG_OPERATION_ICUPDATERECEIPTSTATUS = "ICUPDATERECEIPTSTATUS";
	public final static String LOG_OPERATION_ICCLEARITEMSTATISTICS = "ICCLEARITEMSTATISTICS";
	public final static String LOG_OPERATION_ICCONVERSION = "ICCONVERSION";
	public final static String LOG_OPERATION_ICITEMSRECEIVEDNOTINVOICED = "ICITEMSRECEIVEDNOTINVOICED";
	public final static String LOG_OPERATION_ICITEMVALUATIONREPORT = "ICITEMVALUATIONREPORT";
	public final static String LOG_OPERATION_ICLISTUNUSEDPOS = "ICLISTUNUSEDPOS";
	public final static String LOG_OPERATION_ICONHANDBYDESCRIPTION = "ICONHANDBYDESCRIPTION";
	public final static String LOG_OPERATION_ICPHYSICALINVENTORYVARIANCEREPORT = "ICPHYSICALINVENTORYVARIANCEREPORT";
	public final static String LOG_OPERATION_ICUPDATEINVNUMONRECEIPT = "ICUPDATEINVNUMONRECEIPT";
	public final static String LOG_OPERATION_ICPHYSICALINVENTORYWORKSHEET= "ICPHYSICALINVENTORYWORKSHEET";
	public final static String LOG_OPERATION_ICPORECEIVINGREPORT = "ICPORECEIVINGREPORT";
	public final static String LOG_OPERATION_ICPRINTRECEIVINGLABELS = "ICPRINTRECEIVINGLABELS";
	public final static String LOG_OPERATION_ICTRANSACTIONHISTORYREPORT = "ICTRANSACTIONHISTORYREPORT";
	public final static String LOG_OPERATION_ICUNDERSTOCKEDITEMREPORT = "ICUNDERSTOCKEDITEMREPORT";
	public final static String LOG_OPERATION_ICUPDATEPRICE = "ICUPDATEPRICE";
	public final static String LOG_OPERATION_ICVIEWITEMPRICINGREPORT = "ICVIEWITEMPRICINGREPORT";
	public final static String LOG_OPERATION_ARBATCHPOST = "ARBATCHPOST";
	public final static String LOG_OPERATION_SMDELETEWO = "SMDELETEWO";
	public final static String LOG_OPERATION_ARCLEARPAIDTRANS = "ARCLEARPAIDTRANS";
	public final static String LOG_OPERATION_APCLEARPAIDTRANS = "APCLEARPAIDTRANS";
	public final static String LOG_OPERATION_ARSMINVOICEIMPORT = "ARSMINVOICEIMPORT";
	public final static String LOG_OPERATION_BKDELETEBKSTMT = "BKDELETEBKSTMT";
	public final static String LOG_OPERATION_SMDELETEORDERLINESFAIL = "SMDELETEORDERLINESFAIL";
	public final static String LOG_OPERATION_SMDELETEORDERLINESSUCCEED = "SMDELETEORDERLINESSUCCEED";
	public final static String LOG_OPERATION_SMUNSHIPORDERLINESFAIL = "SMUNSHIPORDERLINESFAIL";
	public final static String LOG_OPERATION_SMUNSHIPORDERLINESSUCCEED = "SMUNSHIPORDERLINESSUCCEED";
	public final static String LOG_OPERATION_SMSHIPORDERLINESFAIL = "SMSHIPORDERLINESFAIL";
	public final static String LOG_OPERATION_SMSHIPORDERLINESSUCCEED = "SMSHIPORDERLINESSUCCEED";
	public final static String LOG_OPERATION_SMCREATEITEMORDERLINESFAIL = "SMCREATEITEMORDERLINESFAIL";
	public final static String LOG_OPERATION_SMCREATEITEMORDERLINESSUCCEED = "SMCREATEITEMORDERLINESSUCCEED";
	public final static String LOG_OPERATION_SMMOVEORDERLINESFAIL = "SMMOVEORDERLINESFAIL";
	public final static String LOG_OPERATION_SMMOVEORDERLINESSUCCEED = "SMMOVEORDERLINESSUCCEED";
	public final static String LOG_OPERATION_SMCOPYORDERLINESFAIL = "SMCOPYORDERLINESFAIL";
	public final static String LOG_OPERATION_SMSETDETAILMECHANICSFAIL = "SMSETDETAILMECHANICSFAIL";
	public final static String LOG_OPERATION_SMSETDETAILMECHANICSSSUCCEED = "SMSETDETAILMECHANICSSSUCCEED";
	public final static String LOG_OPERATION_SMREPRICEQUOTEFAIL = "SMREPRICEQUOTEFAIL";
	public final static String LOG_OPERATION_SMREPRICEQUOTESUCCEED = "SMREPRICEQUOTESUCCEED";
	public final static String LOG_OPERATION_SMUNPOSTINGWORKORDER = "SMUNPOSTINGWORKORDER";
	public final static String LOG_OPERATION_SMUSERLOGIN = "SMUSERLOGIN";
	public final static String LOG_OPERATION_SMCREATEINVOICE = "SMCREATEINVOICE";
	public final static String LOG_OPERATION_SMEXECUTESQL = "SMEXECUTESQL";
	public final static String LOG_OPERATION_SMPRINTSERVICETICKET = "SMPRINTSERVICETICKET";
	public final static String LOG_OPERATION_SMPURGEDATA = "SMPURGEDATA";
	public final static String LOG_OPERATION_SMQUERYDELETE = "SMQUERYDELETE";
	public final static String LOG_OPERATION_SMQUERYSAVE = "SMQUERYSAVE";
	public final static String LOG_OPERATION_SMUPDATEDATA = "SMUPDATEDATA";
	public final static String LOG_OPERATION_SMWORKORDEREMAIL = "SMWORKORDEREMAIL";
	public final static String LOG_OPERATION_ICASSIGNPO = "ICASSIGNPO";
	public final static String LOG_OPERATION_ICCLEARTRANS = "ICCLEARTRANS";
	public final static String LOG_OPERATION_ICBATCHPOST = "ICBATCHPOST";
	public final static String LOG_OPERATION_ICBATCHPOSTINVOICEUPDATE = "ICBATCHPOSTINVOICEUPDATE";
	public final static String LOG_OPERATION_ICSMINVOICEIMPORT = "ICSMINVOICEIMPORT";
	public final static String LOG_OPERATION_APSENDNEWVENDOREMAIL = "APSENDNEWVENDOREMAIL";
	public final static String LOG_OPERATION_SMMOVEDSCHEDULEENTRYFROMSCHEDULESCREEN = "SMMOVEDSCHEDULEENTRYFROMSCHEDULE";
	public final static String LOG_OPERATION_SMWORKORDERSAVEDEBUGGING = "SMWOSAVEDEBUG";
	public final static String LOG_OPERATION_SMWORKORDERCOMMANDWOFULLDISPLAY = "SMWOCOMMANDWOFULLDISPLAY";
	public final static String LOG_OPERATION_SMVIEWUSERSESSIONINFO = "SMVIEWUSERSESSIONINFO";
	public final static String LOG_OPERATION_SMVIEWSYSTEMCONFIGURATION = "SMVIEWSYSTEMCONFIGURATION";
	public final static String LOG_OPERATION_SMCREATENEWDOCUMENTFOLDER = "SMCREATENEWDOCUMENTFOLDER";
	public final static String LOG_OPERATION_SMEMAILEDWOPOSTNOTIFICATION = "SMEMAILEDWOPOSTNOTIFICATION";
	//public final static String LOG_OPERATION_SMWORKORDERRECENTITEMSDISPLAY = "SMRECENTITEMSDISPLAY";
	public final static String LOG_OPERATION_SMPOSTINGPERIODVIOLATION = "SMPOSTINGPERIODVIOLATION";
	public final static String LOG_OPERATION_SSSYSTEMREQUEST = "SSSYSTEMREQUEST";
	public final static String LOG_OPERATION_SSSYSTEMERROR = "SSSYSTEMERROR";
	public final static String LOG_OPERATION_SSEMAILSENDERROR = "SSEMAILSENDERROR";
	public final static String LOG_OPERATION_SMSALESEFFORTCHECK = "SMSALESEFFORTCHECK";
	public final static String LOG_OPERATION_SMPURGEORDERSREPORT = "SMPURGEORDERSREPORT";
	public final static String LOG_OPERATION_SSDEVICESETBYSCHEDULE = "SSDEVICESETBYSCHEDULE";
	public final static String LOG_OPERATION_SSDEVICEUNSETBYSCHEDULE = "SSDEVICEUNSETBYSCHEDULE";
	public final static String LOG_OPERATION_SSSEQUENCESETBYSCHEDULE = "SSSEQUENCESETBYSCHEDULE";
	public final static String LOG_OPERATION_SSSEQUENCEUNSETBYSCHEDULE = "SSSEQUENCEUNSETBYSCHEDULE";
	public final static String LOG_OPERATION_SMINVOICEEMAILED = "SMINVOICEMAILED";
	public final static String LOG_OPERATION_SMINVOICENOTMAILED = "SMINVOICENOTMAILED";
	public final static String LOG_OPERATION_APBATCHPOST = "APBATCHPOST";
	public final static String LOG_OPERATION_APAGING = "APAGING";
	public final static String LOG_OPERATION_APPRECHECK = "APPRECHECK";
	public final static String LOG_OPERATION_APVENDORTRANSACTIONS = "APVENDORTRANSACTIONS";
	public final static String LOG_OPERATION_APCHECKREGISTER = "APCHECKREGISTER";
	public final static String LOG_OPERATION_APCHECKRUNPRINTED = "APCHECKRUNPRINTED";
	public final static String LOG_OPERATION_APRENUMBER = "APRENUMBER";
	public final static String LOG_OPERATION_APGENERATECHECKBATCH = "APGENERATECHECKBATCH";
	public final static String LOG_OPERATION_SMGEOCODEREQUEST = "SMGEOCODEREQUEST";
	public final static String LOG_OPERATION_SMWORKORDERCONCURRENCYERROR = "SMWORKORDERCONCURRENCYERROR";
	public final static String LOG_OPERATION_SMVIEWAPPOINTMENTCALENDAR = "SMAPPOINTMENTCALENDAR";
	public final static String LOG_OPERATION_ICRECEIVINGPO = "ICRECEIVINGPO";
	public final static String LOG_OPERATION_SMIMPORTDATA = "SMIMPORTDATA";
	public final static String LOG_OPERATION_SMPRINTINTERACTIVEDELIVERYTICKET = "SMPRINTINTERACTIVEDELIVERYTICKET";
	public final static String LOG_OPERATION_ARCLEARPOSTEDBATCHES = "ARCLEARPOSTEDBATCHES";
	public final static String LOG_OPERATION_ICCLEARPOSTEDBATCHES = "ICCLEARPOSTEDBATCHES";
	public final static String LOG_OPERATION_APCLEARPOSTEDBATCHES = "APCLEARPOSTEDBATCHES";
	public final static String LOG_OPERATION_BKCLEARPOSTEDBANKSTATEMENTS = "BKCLEARPOSTEDBANKSTATEMENTS";
	
	private Connection conn;
	private ServletContext context;
	private String m_DBID;
	
    public SMLogEntry(Connection cn)
    {
    	conn = cn;
    }
    public SMLogEntry(String sDBID, ServletContext cont)
    {
    	context = cont;
    	m_DBID = sDBID;
    	
    }

    public boolean writeEntry (
    		String sUserID,
    		String sOperation,
    		String sDescription,
    		String sComment,
    		String sreferenceid
   		){
    	String m_sOperation = "";
    	String m_sUserFullName = ""; 
    	String m_sUserID = "";
    	int m_iUserID = 0;
    	boolean bSystemUser = false;
    	try {
			m_sOperation = sOperation.substring(0, SMTablesystemlog.soperationLength - 1);
		} catch (Exception e1) {
			m_sOperation = sOperation;
		}
    	
    	try {
    		m_sUserID = sUserID.substring(0, SMTablesystemlog.suseridLength - 1);
		} catch (Exception e1) {
			m_sUserID = sUserID;
		}

    	if(clsStringFunctions.isInteger(m_sUserID.trim())) {
    		m_iUserID = Integer.parseInt(m_sUserID);
    		if(m_iUserID <= 0) {
    			bSystemUser = true;
    		}
    	}
    	if (sComment.length() > SMTablesystemlog.mcommentLength){
    		sComment = sComment.substring(0, SMTablesystemlog.mcommentLength - 1);
    	}
    	if (sDescription.length() > SMTablesystemlog.mdescriptionLength){
    		sDescription = sDescription.substring(0, SMTablesystemlog.mdescriptionLength - 1);
    	}

    	String SQL = "";
    		SQL = "INSERT INTO " + SMTablesystemlog.TableName
    			+ " ("
    	    		+ SMTablesystemlog.datloggingtime
    	    		+ ", " + SMTablesystemlog.mdescription
    	    		+ ", " + SMTablesystemlog.soperation
    	    		+ ", " + SMTablesystemlog.mcomment
    	    		+ ", " + SMTablesystemlog.suserid
    	    		+ ", " + SMTablesystemlog.suserfullname
    	    		+ ", " + SMTablesystemlog.sreferenceid
    	    		+ ") ";
    		if(bSystemUser) {
    			SQL += "VALUES ("
       				 + " NOW()"
        				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sDescription) + "'"
        				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sOperation) + "'"
        				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sComment) + "'"
        				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sUserID) + "'"
        				+ ",'SYSTEM'"
        				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sreferenceid) + "'" 
       				 + ")";	
    		}else {    	   
       		 SQL += "SELECT "
       				+ " NOW()"
       				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sDescription) + "'"
       				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sOperation) + "'"
       				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sComment) + "'"
       				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sUserID) + "'"
       				+ ", CONCAT(" + SMTableusers.sUserFirstName + ",\" \"," +  SMTableusers.sUserLastName + ")"
       				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sreferenceid) + "'"
       				+ " FROM " + SMTableusers.TableName
       				+ " WHERE (";
       		 	if(clsStringFunctions.isInteger(m_sUserID.trim())){
       		 		SQL += "(" + SMTableusers.lid + "=" + m_sUserID + "))";
       		 	}else {
       		 		SQL += "(" + SMTableusers.sUserName + "='" + m_sUserID + "'))";
       		 	} 
    		}
    		
    		//System.out.println(SQL);
		if (conn != null){
			try {
				Statement stmt = conn.createStatement();
				stmt.execute(SQL);
			} catch (Exception e) {
				System.out.println("Error [1413216633]" + Long.toString(System.currentTimeMillis()) 
						+ " - logging operation '" + sOperation + "' in " + SMUtilities.getFullClassName(this.toString()) 
						+ " error using connnection: " + e.getMessage() + ".");
				return false;
			}

		}else{
			try {
				clsDatabaseFunctions.executeSQL(SQL, context, m_DBID, "MySQL", SMUtilities.getFullClassName(this.toString()) + " - user: " + m_sUserFullName);
			} catch (Exception e) {
				System.out.println(System.currentTimeMillis() + " Error [1413216634] executing SQL command for " 
						+ "logging operation '" + sOperation + "', and reference ID " + sreferenceid + ", in " 
						+ SMUtilities.getFullClassName(this.toString()) 
						+ " error using context: **" + context.toString() + "** and DBID: '" + m_DBID + "' - " + e.getMessage() + ".");
					return false;
			}
		}
    	return true;
    }
    public static String getOperationDescriptions(){

    	String s = "";
    	ArrayList<String> m_arrOperationLabelValues = new ArrayList<String>(0);
    	ArrayList<String> m_arrOperationLabelDescriptions = new ArrayList<String>(0);

       	m_arrOperationLabelValues.add(LOG_OPERATION_RECEIPTFLAGGEDASNONINVENTORY);
    	m_arrOperationLabelDescriptions.add("NONINVRCPTFLAGGED");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_FIXEDASSETS);
    	m_arrOperationLabelDescriptions.add("FIXEDASSETS");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_FIXEDASSETSTRANSACTIONCLEARING);
    	m_arrOperationLabelDescriptions.add("FATRANSACTIONCLEARING");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_CANCELORDER);
    	m_arrOperationLabelDescriptions.add("CANCELEDORDER");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_UNCANCELORDER);
    	m_arrOperationLabelDescriptions.add("UNCANCELEDORDER");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ARACTIVITYREPORT);
    	m_arrOperationLabelDescriptions.add("ARACTIVITYREPORT");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ARAGING);
    	m_arrOperationLabelDescriptions.add("ARAGING");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ARCLEARMONTHLYSTATISTICS);
    	m_arrOperationLabelDescriptions.add("CLEAR MONTHLY STATISTICS");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ARINVOICEIMPORT);
    	m_arrOperationLabelDescriptions.add("ARINVOICEIMPORT");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ARRENUMBER);
    	m_arrOperationLabelDescriptions.add("ARRENUMBER");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ARDISPLAYCUSTOMERINFO);
    	m_arrOperationLabelDescriptions.add("DISPLAYCUSTOMERINFO");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ARLISTCUSTOMERSONHOLD);
    	m_arrOperationLabelDescriptions.add("ARLISTCUSTOMERSONHOLD");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ARLISTINACTIVECUST);
    	m_arrOperationLabelDescriptions.add("AR LIST INACTIVE CUST");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ARMISCCASHREPORT);
    	m_arrOperationLabelDescriptions.add("ARMISCCASHREPORT");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ARPOSTINGJOURNAL);
    	m_arrOperationLabelDescriptions.add("ARPOSTINGJOURNAL");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ARPRINTSTATEMENT);
    	m_arrOperationLabelDescriptions.add("ARPRINTSTATEMENT");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ARPRINTCALLSHEETS);
    	m_arrOperationLabelDescriptions.add("ARPRINTCALLSHEETS");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ARSETINACTIVECUSTOMER);
    	m_arrOperationLabelDescriptions.add("SETINACTIVECUSTOMER");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ARVIEWCHRONLOG);
    	m_arrOperationLabelDescriptions.add("ARVIEWCHRONLOG");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMAVERAGEMUREPORT);
    	m_arrOperationLabelDescriptions.add("AVERAGEMUREPORT");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMBIDFOLLOWUPREPORT);
    	m_arrOperationLabelDescriptions.add("BIDFOLLOWUPREPORT");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMBIDREPORT);
    	m_arrOperationLabelDescriptions.add("SMBIDREPORT");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMPENDINGBIDSREPORT);
    	m_arrOperationLabelDescriptions.add("PENDINGBIDSREPORT");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMCANCELEDJOBSREPORT);
    	m_arrOperationLabelDescriptions.add("SMCANCELEDJOBSREPORT");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMCREATECREDITNOTE);
    	m_arrOperationLabelDescriptions.add("CREATECREDITNOTE");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMCREATECREDITNOTEPREVIEW);
    	m_arrOperationLabelDescriptions.add("CREATECREDITNOTEPREVIEW");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMCREATEMULTIPLEINVOICES);
    	m_arrOperationLabelDescriptions.add("CREATEMULTIPLEINVOICES");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMCRITICALDATEREPORT);
    	m_arrOperationLabelDescriptions.add("CRITICALDATEREPORT");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMCUSTOMITEMSONHANDNOTONSALESORDER);
    	m_arrOperationLabelDescriptions.add("CUSTOMITEMSONHANDNOTONSALESORDER");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMQUERY);
    	m_arrOperationLabelDescriptions.add("SMQUERY");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMDISPLAYJOBCOSTINFO);
    	m_arrOperationLabelDescriptions.add("DISPLAYJOBCOSTINFO");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMDISPLAYORDERINFORMATION);
    	m_arrOperationLabelDescriptions.add("DISPLAYORDERINFORMATION");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMJOBCOSTDAILYREPORT);
    	m_arrOperationLabelDescriptions.add("SMJOBCOSTDAILYREPORT");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_OTHER);
    	m_arrOperationLabelDescriptions.add("OTHER");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMLISTNEARBYORDERREPORT);
    	m_arrOperationLabelDescriptions.add("LISTNEARBYORDERREPORT");
    	
    	//m_arrOperationLabelValues.add(LOG_OPERATION_SMLISTORDERSFORSCHEDULING);
    	//m_arrOperationLabelDescriptions.add("SMLISTORDERSFORSCHEDULING");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMMONTHLYBILLINGREPORT);
    	m_arrOperationLabelDescriptions.add("MONTHLYBILLINGREPORT");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMPRINTMONTHLYSALESREPORT);
    	m_arrOperationLabelDescriptions.add("PRINTMONTHLYSALESREPORT");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMOPENORDERSREPORT);
    	m_arrOperationLabelDescriptions.add("SMOPENORDERSREPORT");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMCHANGEUSERPASSWORD);
    	m_arrOperationLabelDescriptions.add("SMCHANGEUSERPASSWORD");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMORDERSOURCELISTING);
    	m_arrOperationLabelDescriptions.add("SMORDERSOURCELISTING");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMPRINTDELIVERYTICKET);
    	m_arrOperationLabelDescriptions.add("PRINTDELIVERYTICKET");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMPRINTINVOICE);
    	m_arrOperationLabelDescriptions.add("PRINTINVOICE");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMPRINTINVOICEAUDIT);
    	m_arrOperationLabelDescriptions.add("PRINTINVOICEAUDIT");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMPRINTPREINVOICE);
    	m_arrOperationLabelDescriptions.add("PRINTPREINVOICE");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMPRODUCTIVITYREPORT);
    	m_arrOperationLabelDescriptions.add("SMPRODUCTIVITYREPORT");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMRECALIBRATECOUNTERS);
    	m_arrOperationLabelDescriptions.add("RECALIBRATECOUNTERS");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMSALESCONTACTREPORT);
    	m_arrOperationLabelDescriptions.add("SMSALESCONTACTREPORT");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMPRINTUNBILLEDCONTRACTREPORT);
    	m_arrOperationLabelDescriptions.add("PRINTUNBILLEDCONTRACTREPORT");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMEMAILUNBILLEDCONTRACTREPORT);
    	m_arrOperationLabelDescriptions.add("EMAILUNBILLEDCONTRACTREPORT");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMVIEWTRUCKSCHEDULE);
    	m_arrOperationLabelDescriptions.add("VIEWTRUCKSCHEDULE");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMWAGESCALEREPORT);
    	m_arrOperationLabelDescriptions.add("SMWAGESCALEREPORT");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMWARRANTYSTATUSREPORT);
    	m_arrOperationLabelDescriptions.add("SMWARRANTYSTATUSREPORT");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMTAXBYCATEGORYREPORT);
    	m_arrOperationLabelDescriptions.add("TAXBYCATEGORYREPORT");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ICPOASSIGNMENT);
    	m_arrOperationLabelDescriptions.add("POASSIGNMENT");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ICUPDATERECEIPTSTATUS);
    	m_arrOperationLabelDescriptions.add("UPDATERECEIPTSTATUS");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ICCLEARITEMSTATISTICS);
    	m_arrOperationLabelDescriptions.add("CLEARITEMSTATISTICS");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ICCONVERSION);
    	m_arrOperationLabelDescriptions.add("ICCONVERSION");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ICITEMSRECEIVEDNOTINVOICED);
    	m_arrOperationLabelDescriptions.add("ICITEMSRECEIVEDNOTINVOICED");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ICITEMVALUATIONREPORT);
    	m_arrOperationLabelDescriptions.add("ICITEMVALUATIONREPORT");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ICLISTUNUSEDPOS);
    	m_arrOperationLabelDescriptions.add("LISTUNUSEDPOS");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ICONHANDBYDESCRIPTION);
    	m_arrOperationLabelDescriptions.add("ICONHANDBYDESCRIPTION");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ICPHYSICALINVENTORYVARIANCEREPORT);
    	m_arrOperationLabelDescriptions.add("ICPHYSICALINVENTORYVARIANCEREPORT");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ICUPDATEINVNUMONRECEIPT);
    	m_arrOperationLabelDescriptions.add("UPDATEINVNUMONRECEIPT");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ICPHYSICALINVENTORYWORKSHEET);
    	m_arrOperationLabelDescriptions.add("ICPHYSICALINVENTORYWORKSHEET");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ICPORECEIVINGREPORT);
    	m_arrOperationLabelDescriptions.add("ICPORECEIVINGREPORT");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ICPRINTRECEIVINGLABELS);
    	m_arrOperationLabelDescriptions.add("ICPRINTRECEIVINGLABELS");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ICTRANSACTIONHISTORYREPORT);
    	m_arrOperationLabelDescriptions.add("ICTRANSACTIONHISTORYREPORT");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ICUNDERSTOCKEDITEMREPORT);
    	m_arrOperationLabelDescriptions.add("ICUNDERSTOCKEDITEMREPORT");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ICUPDATEPRICE);
    	m_arrOperationLabelDescriptions.add("ICUPDATEPRICE");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ICVIEWITEMPRICINGREPORT);
    	m_arrOperationLabelDescriptions.add("ICVIEWITEMPRICINGREPORT");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ARBATCHPOST);
    	m_arrOperationLabelDescriptions.add("ARBATCHPOST");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMDELETEWO);
    	m_arrOperationLabelDescriptions.add("DELETE WO");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ARCLEARPAIDTRANS);
    	m_arrOperationLabelDescriptions.add("ARCLEARPAIDTRANS");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_APCLEARPAIDTRANS);
    	m_arrOperationLabelDescriptions.add("APCLEARPAIDTRANS");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ARSMINVOICEIMPORT);
    	m_arrOperationLabelDescriptions.add("ARSMINVOICEIMPORT");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_BKDELETEBKSTMT);
    	m_arrOperationLabelDescriptions.add("DELETEBKSTMT");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMDELETEORDERLINESFAIL);
    	m_arrOperationLabelDescriptions.add("DELETEORDERLINESFAIL");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMDELETEORDERLINESSUCCEED);
    	m_arrOperationLabelDescriptions.add("DELETEORDERLINESSUCCEED");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMUNSHIPORDERLINESFAIL);
    	m_arrOperationLabelDescriptions.add("UNSHIPORDERLINESFAIL");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMUNSHIPORDERLINESSUCCEED);
    	m_arrOperationLabelDescriptions.add("UNSHIPORDERLINESSUCCEED");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMSHIPORDERLINESFAIL);
    	m_arrOperationLabelDescriptions.add("SHIPORDERLINESFAIL");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMSHIPORDERLINESSUCCEED);
    	m_arrOperationLabelDescriptions.add("SHIPORDERLINESSUCCEED");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMCREATEITEMORDERLINESFAIL);
    	m_arrOperationLabelDescriptions.add("CREATEITEMORDERLINESFAIL");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMCREATEITEMORDERLINESSUCCEED);
    	m_arrOperationLabelDescriptions.add("CREATEITEMORDERLINESSUCCEED");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMMOVEORDERLINESFAIL);
    	m_arrOperationLabelDescriptions.add("MOVEORDERLINESFAIL");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMMOVEORDERLINESSUCCEED);
    	m_arrOperationLabelDescriptions.add("MOVEORDERLINESSUCCEED");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMCOPYORDERLINESFAIL);
    	m_arrOperationLabelDescriptions.add("COPYORDERLINESFAIL");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMSETDETAILMECHANICSFAIL);
    	m_arrOperationLabelDescriptions.add("SETDETAILMECHANICSFAIL");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMSETDETAILMECHANICSSSUCCEED);
    	m_arrOperationLabelDescriptions.add("SETDETAILMECHANICSSSUCCEED");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMREPRICEQUOTEFAIL);
    	m_arrOperationLabelDescriptions.add("REPRICEQUOTEFAIL");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMREPRICEQUOTESUCCEED);
    	m_arrOperationLabelDescriptions.add("REPRICEQUOTESUCCEED");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMUNPOSTINGWORKORDER);
    	m_arrOperationLabelDescriptions.add("UNPOSTINGWORKORDER");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMUSERLOGIN);
    	m_arrOperationLabelDescriptions.add("USERLOGIN");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMCREATEINVOICE);
    	m_arrOperationLabelDescriptions.add("CREATEINVOICE");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMEXECUTESQL);
    	m_arrOperationLabelDescriptions.add("SMEXECUTESQL");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMPRINTSERVICETICKET);
    	m_arrOperationLabelDescriptions.add("PRINTSERVICETICKET");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMPURGEDATA);
    	m_arrOperationLabelDescriptions.add("PURGEDATA");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMQUERYDELETE);
    	m_arrOperationLabelDescriptions.add("SMQUERYDELETE");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMQUERYSAVE);
    	m_arrOperationLabelDescriptions.add("SMQUERYSAVE");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMUPDATEDATA);
    	m_arrOperationLabelDescriptions.add("UPDATEDATA");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMWORKORDEREMAIL);
    	m_arrOperationLabelDescriptions.add("WORKORDEREMAIL");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ICASSIGNPO);
    	m_arrOperationLabelDescriptions.add("ICASSIGNPO");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ICCLEARTRANS);
    	m_arrOperationLabelDescriptions.add("ICCLEARTRANS");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ICBATCHPOST);
    	m_arrOperationLabelDescriptions.add("ICBATCHPOST");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ICBATCHPOSTINVOICEUPDATE);
    	m_arrOperationLabelDescriptions.add("ICBATCHPOSTINVOICEUPDATE");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ICSMINVOICEIMPORT);
    	m_arrOperationLabelDescriptions.add("ICSMINVOICEIMPORT");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_APSENDNEWVENDOREMAIL);
    	m_arrOperationLabelDescriptions.add("SENDNEWVENDOREMAIL");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMMOVEDSCHEDULEENTRYFROMSCHEDULESCREEN);
    	m_arrOperationLabelDescriptions.add("MOVEDSCHEDULEENTRYFROMSCHEDULE");
    	
    	//m_arrOperationLabelValues.add(LOG_OPERATION_SMWORKORDERSAVEDEBUGGING);
    	//m_arrOperationLabelDescriptions.add("WOSAVEDEBUG");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMWORKORDERCOMMANDWOFULLDISPLAY);
    	m_arrOperationLabelDescriptions.add("WOCOMMANDWOFULLDISPLAY");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMVIEWUSERSESSIONINFO);
    	m_arrOperationLabelDescriptions.add("VIEWUSERSESSIONINFO");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMVIEWSYSTEMCONFIGURATION);
    	m_arrOperationLabelDescriptions.add("VIEWSYSTEMCONFIGURATION");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMCREATENEWDOCUMENTFOLDER);
    	m_arrOperationLabelDescriptions.add("CREATENEWDOCUMENTFOLDER");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMEMAILEDWOPOSTNOTIFICATION);
    	m_arrOperationLabelDescriptions.add("EMAILEDWOPOSTNOTIFICATION");
    	
    	//m_arrOperationLabelValues.add(LOG_OPERATION_SMWORKORDERRECENTITEMSDISPLAY);
    	//m_arrOperationLabelDescriptions.add("RECENTITEMSDISPLAY");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMPOSTINGPERIODVIOLATION);
    	m_arrOperationLabelDescriptions.add("POSTINGPERIODVIOLATION");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SSSYSTEMREQUEST);
    	m_arrOperationLabelDescriptions.add("SSSYSTEMREQUEST");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SSSYSTEMERROR);
    	m_arrOperationLabelDescriptions.add("SSSYSTEMERROR");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SSEMAILSENDERROR);
    	m_arrOperationLabelDescriptions.add("ASEMAILSENDERROR");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMSALESEFFORTCHECK);
    	m_arrOperationLabelDescriptions.add("SALESEFFORTCHECK");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMPURGEORDERSREPORT);
    	m_arrOperationLabelDescriptions.add("SMPURGEORDERSREPORT");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SSDEVICESETBYSCHEDULE);
    	m_arrOperationLabelDescriptions.add("SSDEVICESETBYSCHEDULE");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SSDEVICEUNSETBYSCHEDULE);
    	m_arrOperationLabelDescriptions.add("SSDEVICEUNSETBYSCHEDULE");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SSSEQUENCESETBYSCHEDULE);
    	m_arrOperationLabelDescriptions.add("SSSEQUENCESETBYSCHEDULE");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SSSEQUENCEUNSETBYSCHEDULE);
    	m_arrOperationLabelDescriptions.add("SSSEQUENCEUNSETBYSCHEDULE");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMINVOICEEMAILED);
    	m_arrOperationLabelDescriptions.add("INVOICEMAILED");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMINVOICENOTMAILED);
    	m_arrOperationLabelDescriptions.add("INVOICENOTMAILED");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_APBATCHPOST);
    	m_arrOperationLabelDescriptions.add("APBATCHPOST");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_APAGING);
    	m_arrOperationLabelDescriptions.add("APAGING");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_APPRECHECK);
    	m_arrOperationLabelDescriptions.add("APPRECHECK");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_APVENDORTRANSACTIONS);
    	m_arrOperationLabelDescriptions.add("APVENDORTRANSACTIONS");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_APCHECKREGISTER);
    	m_arrOperationLabelDescriptions.add("APCHECKREGISTER");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_APCHECKRUNPRINTED);
    	m_arrOperationLabelDescriptions.add("APCHECKRUNPRINTED");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_APRENUMBER);
    	m_arrOperationLabelDescriptions.add("APRENUMBER");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_APGENERATECHECKBATCH);
    	m_arrOperationLabelDescriptions.add("APGENERATECHECKBATCH");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMGEOCODEREQUEST);
    	m_arrOperationLabelDescriptions.add("GEOCODEREQUEST");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMWORKORDERCONCURRENCYERROR);
    	m_arrOperationLabelDescriptions.add("WORKORDERCONCURRENCYERROR");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMVIEWAPPOINTMENTCALENDAR);
    	m_arrOperationLabelDescriptions.add("APPOINTMENTCALENDAR");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ICRECEIVINGPO);
    	m_arrOperationLabelDescriptions.add("RECEIVINGPO");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMIMPORTDATA);
    	m_arrOperationLabelDescriptions.add("IMPORTDATA");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMPRINTINTERACTIVEDELIVERYTICKET);
    	m_arrOperationLabelDescriptions.add("PRINTINTERACTIVEDELIVERYTICKET");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ARCLEARPOSTEDBATCHES);
    	m_arrOperationLabelDescriptions.add("ARCLEARPOSTEDBATCHES");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ICCLEARPOSTEDBATCHES);
    	m_arrOperationLabelDescriptions.add("ICCLEARPOSTEDBATCHES");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_APCLEARPOSTEDBATCHES);
    	m_arrOperationLabelDescriptions.add("APCLEARPOSTEDBATCHES");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_BKCLEARPOSTEDBANKSTATEMENTS);
    	m_arrOperationLabelDescriptions.add("BKCLEARPOSTEDBANKSTATEMENTS");

    	for (int i = 0; i <= m_arrOperationLabelValues.size(); i++){
    		s += "<BR>Label: " + m_arrOperationLabelValues.get(i) + " - " + m_arrOperationLabelDescriptions.get(i);
    	}
    	return s;
    }

}
