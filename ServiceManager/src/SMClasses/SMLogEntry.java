package SMClasses;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;

import javax.servlet.ServletContext;

import SMDataDefinition.SMTablesystemlog;
import SMDataDefinition.SMTableusers;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsStringFunctions;
import smcontrolpanel.SMUtilities;

public class SMLogEntry {

	public final static String LOG_OPERATION_ICRECEIPTFLAGGEDASNONINVENTORY = "ICNONINVRCPTFLAGGED";
	public final static String LOG_OPERATION_FAFIXEDASSETS = "FAFIXEDASSETS";
	public final static String LOG_OPERATION_FIXEDASSETSTRANSACTIONCLEARING = "FATRANSACTIONCLEARING";
	public final static String LOG_OPERATION_SMCANCELORDER = "SMCANCELEDORDER";
	public final static String LOG_OPERATION_SMUNCANCELORDER = "SMUNCANCELEDORDER";
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
	//public final static String LOG_OPERATION_ICUPDATEINVNUMONRECEIPT = "ICUPDATEINVNUMONRECEIPT";
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
	public final static String LOG_OPERATION_ICDUPLICATEDPOASSIGNMENT = "ICASSIGNPO";
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
	public final static String LOG_OPERATION_GLTRIALBALANCE = "GLTRIALBALANCE";
	public final static String LOG_OPERATION_GLBATCHPOST = "GLBATCHPOST";
	public final static String LOG_OPERATION_GLCLEARPOSTEDBATCHES = "GLCLEARPOSTEDBATCHES";
	public final static String LOG_OPERATION_GLCLEARTRANS = "GLCLEARTRANS";
	public final static String LOG_OPERATION_GLCLEARFISCALDATA = "GLCLEARFISCALDATA";
	
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
    public static ArrayList<String> getOperationDescriptions(String sDelimiter){

    	ArrayList<String> m_arrOperationLabelValues = new ArrayList<String>(0);
    	ArrayList<String> m_arrOperationLabelDescriptions = new ArrayList<String>(0);

       	m_arrOperationLabelValues.add(LOG_OPERATION_ICRECEIPTFLAGGEDASNONINVENTORY);
    	m_arrOperationLabelDescriptions.add("Records when a non-inventory item is flagged during IC receipt processing");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_FAFIXEDASSETS);
    	m_arrOperationLabelDescriptions.add("Records one of several Fixed Assets events");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_FIXEDASSETSTRANSACTIONCLEARING);
    	m_arrOperationLabelDescriptions.add("Records clearing of Fixed Assets transactions");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMCANCELORDER);
    	m_arrOperationLabelDescriptions.add("Records cancellation of an SM sales order");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMUNCANCELORDER);
    	m_arrOperationLabelDescriptions.add("Records the UN-cancellation of an SM sales order");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ARACTIVITYREPORT);
    	m_arrOperationLabelDescriptions.add("Records running of the AR Activity report");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ARAGING);
    	m_arrOperationLabelDescriptions.add("Records running of the AR Aging report");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ARCLEARMONTHLYSTATISTICS);
    	m_arrOperationLabelDescriptions.add("Records AR monthly statistics CLEARING");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ARINVOICEIMPORT);
    	m_arrOperationLabelDescriptions.add("Records AR invoice import from SM");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ARRENUMBER);
    	m_arrOperationLabelDescriptions.add("Records a customer number change/merge in AR");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ARDISPLAYCUSTOMERINFO);
    	m_arrOperationLabelDescriptions.add("Records display of customer information in AR");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ARLISTCUSTOMERSONHOLD);
    	m_arrOperationLabelDescriptions.add("Records running of the AR Customers On Hold report");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ARLISTINACTIVECUST);
    	m_arrOperationLabelDescriptions.add("Records running of the AR List Inactive Customers report");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ARMISCCASHREPORT);
    	m_arrOperationLabelDescriptions.add("Records running of the AR Miscellaneous Cash report");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ARPOSTINGJOURNAL);
    	m_arrOperationLabelDescriptions.add("Records running of the AR Posting Journal");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ARPRINTSTATEMENT);
    	m_arrOperationLabelDescriptions.add("Records printing of an AR customer statement");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ARPRINTCALLSHEETS);
    	m_arrOperationLabelDescriptions.add("Records running of AR Call Sheets");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ARSETINACTIVECUSTOMER);
    	m_arrOperationLabelDescriptions.add("Records setting a customer to INACTIVE");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ARVIEWCHRONLOG);
    	m_arrOperationLabelDescriptions.add("Records running of the AR Chronological Log");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMAVERAGEMUREPORT);
    	m_arrOperationLabelDescriptions.add("Records running of the Average Mark Up report");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMBIDFOLLOWUPREPORT);
    	m_arrOperationLabelDescriptions.add("Records running of the Sales Lead Follow Up report");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMBIDREPORT);
    	m_arrOperationLabelDescriptions.add("Records running of the Sales Lead report");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMPENDINGBIDSREPORT);
    	m_arrOperationLabelDescriptions.add("Records running of the Pending Sales Lead report");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMCANCELEDJOBSREPORT);
    	m_arrOperationLabelDescriptions.add("Records running of the Canceled Jobs report");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMCREATECREDITNOTE);
    	m_arrOperationLabelDescriptions.add("Records credit note creation ");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMCREATECREDITNOTEPREVIEW);
    	m_arrOperationLabelDescriptions.add("Records credit note preview");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMCREATEMULTIPLEINVOICES);
    	m_arrOperationLabelDescriptions.add("Records running of the Create Multiple Invoices function");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMCRITICALDATEREPORT);
    	m_arrOperationLabelDescriptions.add("Records running of the Critical Dates report");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMCUSTOMITEMSONHANDNOTONSALESORDER);
    	m_arrOperationLabelDescriptions.add("Records running of the Custom Items On Hand Not On Sales Orders report");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMQUERY);
    	m_arrOperationLabelDescriptions.add("Records when queries are run");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMDISPLAYJOBCOSTINFO);
    	m_arrOperationLabelDescriptions.add("Records when Job Cost Summary is viewed");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMDISPLAYORDERINFORMATION);
    	m_arrOperationLabelDescriptions.add("Records when View Order Information screen is viewed");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMJOBCOSTDAILYREPORT);
    	m_arrOperationLabelDescriptions.add("Records running of the Job Cost Daily report");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_OTHER);
    	m_arrOperationLabelDescriptions.add("Records miscellaneous events");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMLISTNEARBYORDERREPORT);
    	m_arrOperationLabelDescriptions.add("Records running of the List Nearby Orders report");
    	
    	//m_arrOperationLabelValues.add(LOG_OPERATION_SMLISTORDERSFORSCHEDULING);
    	//m_arrOperationLabelDescriptions.add("SMLISTORDERSFORSCHEDULING");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMMONTHLYBILLINGREPORT);
    	m_arrOperationLabelDescriptions.add("Records running of the Monthly Billing report");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMPRINTMONTHLYSALESREPORT);
    	m_arrOperationLabelDescriptions.add("Records running of the Monthly Sales report");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMOPENORDERSREPORT);
    	m_arrOperationLabelDescriptions.add("Records running of the Open Orders report");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMCHANGEUSERPASSWORD);
    	m_arrOperationLabelDescriptions.add("Records when a user's password is changed ");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMORDERSOURCELISTING);
    	m_arrOperationLabelDescriptions.add("Records running of the Order Source listing");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMPRINTDELIVERYTICKET);
    	m_arrOperationLabelDescriptions.add("Records when a delivery ticket is printed");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMPRINTINVOICE);
    	m_arrOperationLabelDescriptions.add("Records when an invoice is printed");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMPRINTINVOICEAUDIT);
    	m_arrOperationLabelDescriptions.add("Records running of the Invoice Audit List");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMPRINTPREINVOICE);
    	m_arrOperationLabelDescriptions.add("Records running of the Pre-Invoice Audit List");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMPRODUCTIVITYREPORT);
    	m_arrOperationLabelDescriptions.add("Records running of the Productivity report");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMRECALIBRATECOUNTERS);
    	m_arrOperationLabelDescriptions.add("Records invoice/order number recalibration");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMSALESCONTACTREPORT);
    	m_arrOperationLabelDescriptions.add("Records running of the Sales Contact report");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMPRINTUNBILLEDCONTRACTREPORT);
    	m_arrOperationLabelDescriptions.add("Records running of the Unbilled Contract report");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMEMAILUNBILLEDCONTRACTREPORT);
    	m_arrOperationLabelDescriptions.add("Records emailing of the Unbilled Contract report");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMVIEWTRUCKSCHEDULE);
    	m_arrOperationLabelDescriptions.add("Records viewing of the Truck Schedule");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMWAGESCALEREPORT);
    	m_arrOperationLabelDescriptions.add("Records running of the Wage Scale report");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMWARRANTYSTATUSREPORT);
    	m_arrOperationLabelDescriptions.add("Records running of the Warranty Status report");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMTAXBYCATEGORYREPORT);
    	m_arrOperationLabelDescriptions.add("Records running of the SM Tax By Category report");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ICPOASSIGNMENT);
    	m_arrOperationLabelDescriptions.add("Records when a PO number is assigned");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ICUPDATERECEIPTSTATUS);
    	m_arrOperationLabelDescriptions.add("Records updating the status of receipt entries during posting");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ICCLEARITEMSTATISTICS);
    	m_arrOperationLabelDescriptions.add("Records clearing of IC Item Statistics");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ICCONVERSION);
    	m_arrOperationLabelDescriptions.add("Records running of the IC data conversion (from ACCPAC)");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ICITEMSRECEIVEDNOTINVOICED);
    	m_arrOperationLabelDescriptions.add("Records running of the IC Items Received Not Invoiced report");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ICITEMVALUATIONREPORT);
    	m_arrOperationLabelDescriptions.add("Records running of the IC Item Valuation report");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ICLISTUNUSEDPOS);
    	m_arrOperationLabelDescriptions.add("Records running of the IC List Unused POs report");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ICONHANDBYDESCRIPTION);
    	m_arrOperationLabelDescriptions.add("Records running of the IC Items On Hand Searched By Description report");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ICPHYSICALINVENTORYVARIANCEREPORT);
    	m_arrOperationLabelDescriptions.add("Records running of the IC Physical Variance report");
    	
    	//m_arrOperationLabelValues.add(LOG_OPERATION_ICUPDATEINVNUMONRECEIPT);
    	//m_arrOperationLabelDescriptions.add("UPDATEINVNUMONRECEIPT");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ICPHYSICALINVENTORYWORKSHEET);
    	m_arrOperationLabelDescriptions.add("Records printing of the IC Physical Inventory Worksheet");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ICPORECEIVINGREPORT);
    	m_arrOperationLabelDescriptions.add("Records running of the Ic PO Receiving report");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ICPRINTRECEIVINGLABELS);
    	m_arrOperationLabelDescriptions.add("Records printing Ic Receiving Labels");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ICTRANSACTIONHISTORYREPORT);
    	m_arrOperationLabelDescriptions.add("Records running of the Ic Transaction History report");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ICUNDERSTOCKEDITEMREPORT);
    	m_arrOperationLabelDescriptions.add("Records running of the IC Understocked Item report");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ICUPDATEPRICE);
    	m_arrOperationLabelDescriptions.add("Records updating of IC item prices");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ICVIEWITEMPRICINGREPORT);
    	m_arrOperationLabelDescriptions.add("Records running of the IC View Item Pricing report");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ARBATCHPOST);
    	m_arrOperationLabelDescriptions.add("Records psting of AR batches ");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMDELETEWO);
    	m_arrOperationLabelDescriptions.add("Records work order deletions ");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ARCLEARPAIDTRANS);
    	m_arrOperationLabelDescriptions.add("Records clearing of the AR paid transactions");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_APCLEARPAIDTRANS);
    	m_arrOperationLabelDescriptions.add("Records clearing of the AP paid transactions");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ARSMINVOICEIMPORT);
    	m_arrOperationLabelDescriptions.add("Records import of SM invoices into AR");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_BKDELETEBKSTMT);
    	m_arrOperationLabelDescriptions.add("Records deletion of BK bank statements");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMDELETEORDERLINESFAIL);
    	m_arrOperationLabelDescriptions.add("Records failed attempts to delete SM order lines");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMDELETEORDERLINESSUCCEED);
    	m_arrOperationLabelDescriptions.add("Records successful attempts to delete SM order lines");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMUNSHIPORDERLINESFAIL);
    	m_arrOperationLabelDescriptions.add("Records failed attempts to UNship SM order lines");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMUNSHIPORDERLINESSUCCEED);
    	m_arrOperationLabelDescriptions.add("Records successful attempts to UNship SM order lines");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMSHIPORDERLINESFAIL);
    	m_arrOperationLabelDescriptions.add("Records failed attempts to ship SM order lines");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMSHIPORDERLINESSUCCEED);
    	m_arrOperationLabelDescriptions.add("Records successful attempts to ship SM order lines");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMCREATEITEMORDERLINESFAIL);
    	m_arrOperationLabelDescriptions.add("Records failed attempts to create items for SM order lines");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMCREATEITEMORDERLINESSUCCEED);
    	m_arrOperationLabelDescriptions.add("Records successful attempts to create items for SM order lines");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMMOVEORDERLINESFAIL);
    	m_arrOperationLabelDescriptions.add("Records failed attempts to move SM order lines");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMMOVEORDERLINESSUCCEED);
    	m_arrOperationLabelDescriptions.add("Records successful attempts to ship SM order lines");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMCOPYORDERLINESFAIL);
    	m_arrOperationLabelDescriptions.add("Records failed attempts to copy SM order lines");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMSETDETAILMECHANICSFAIL);
    	m_arrOperationLabelDescriptions.add("Records failed attempts to set mechanics on SM order lines");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMSETDETAILMECHANICSSSUCCEED);
    	m_arrOperationLabelDescriptions.add("Records successful attempts to set mechanics on SM order lines");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMREPRICEQUOTEFAIL);
    	m_arrOperationLabelDescriptions.add("Records failed attempts to reprice an SM quote");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMREPRICEQUOTESUCCEED);
    	m_arrOperationLabelDescriptions.add("Records successful attempts to reprice an SM quote");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMUNPOSTINGWORKORDER);
    	m_arrOperationLabelDescriptions.add("Records UNPOSTING a work order");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMUSERLOGIN);
    	m_arrOperationLabelDescriptions.add("Records user logins");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMCREATEINVOICE);
    	m_arrOperationLabelDescriptions.add("Records invoice creation");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMEXECUTESQL);
    	m_arrOperationLabelDescriptions.add("Records running the SM Execute SQL function");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMPRINTSERVICETICKET);
    	m_arrOperationLabelDescriptions.add("Records printing service ticket");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMPURGEDATA);
    	m_arrOperationLabelDescriptions.add("Records data purges");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMQUERYDELETE);
    	m_arrOperationLabelDescriptions.add("Records deletion of SM queries");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMQUERYSAVE);
    	m_arrOperationLabelDescriptions.add("Records SM query saves");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMUPDATEDATA);
    	m_arrOperationLabelDescriptions.add("Records updates of SM data structures (during first login after program update");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMWORKORDEREMAIL);
    	m_arrOperationLabelDescriptions.add("Records emailing of work orders");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ICDUPLICATEDPOASSIGNMENT);
    	m_arrOperationLabelDescriptions.add("Records a duplicated PO number assignment");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ICCLEARTRANS);
    	m_arrOperationLabelDescriptions.add("Records IC transaction clearing");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ICBATCHPOST);
    	m_arrOperationLabelDescriptions.add("Records IC batch posting");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ICBATCHPOSTINVOICEUPDATE);
    	m_arrOperationLabelDescriptions.add("Records IC batch line costing update events during posting");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ICSMINVOICEIMPORT);
    	m_arrOperationLabelDescriptions.add("Records import of SM invoices into IC batches");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_APSENDNEWVENDOREMAIL);
    	m_arrOperationLabelDescriptions.add("Records emailing of new vendor notifications");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMMOVEDSCHEDULEENTRYFROMSCHEDULESCREEN);
    	m_arrOperationLabelDescriptions.add("Records when an entry is moved on the Truck Schedule");
    	
    	//m_arrOperationLabelValues.add(LOG_OPERATION_SMWORKORDERSAVEDEBUGGING);
    	//m_arrOperationLabelDescriptions.add("WOSAVEDEBUG");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMWORKORDERCOMMANDWOFULLDISPLAY);
    	m_arrOperationLabelDescriptions.add("Records when a user is warned for about clicking before the work order screen was fully displayed");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMVIEWUSERSESSIONINFO);
    	m_arrOperationLabelDescriptions.add("Records use of View Session Information function");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMVIEWSYSTEMCONFIGURATION);
    	m_arrOperationLabelDescriptions.add("Records use of View System Configuration function");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMCREATENEWDOCUMENTFOLDER);
    	m_arrOperationLabelDescriptions.add("Records new document folder creation (Google Drive integration)");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMEMAILEDWOPOSTNOTIFICATION);
    	m_arrOperationLabelDescriptions.add("Records emailing of work order posting notification");
    	
    	//m_arrOperationLabelValues.add(LOG_OPERATION_SMWORKORDERRECENTITEMSDISPLAY);
    	//m_arrOperationLabelDescriptions.add("RECENTITEMSDISPLAY");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMPOSTINGPERIODVIOLATION);
    	m_arrOperationLabelDescriptions.add("Records posting period violation");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SSSYSTEMREQUEST);
    	m_arrOperationLabelDescriptions.add("Records Security System request by the program");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SSSYSTEMERROR);
    	m_arrOperationLabelDescriptions.add("Records Security System error");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SSEMAILSENDERROR);
    	m_arrOperationLabelDescriptions.add("Records error sending emails from the Security System");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMSALESEFFORTCHECK);
    	m_arrOperationLabelDescriptions.add("Records running of the Check Sales Effort report");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMPURGEORDERSREPORT);
    	m_arrOperationLabelDescriptions.add("Records running of the Order History report");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SSDEVICESETBYSCHEDULE);
    	m_arrOperationLabelDescriptions.add("Records setting of a Security System device by schedule");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SSDEVICEUNSETBYSCHEDULE);
    	m_arrOperationLabelDescriptions.add("Records UNsetting of a Security System device by schedule");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SSSEQUENCESETBYSCHEDULE);
    	m_arrOperationLabelDescriptions.add("Records setting of a Security System sequence by schedule");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SSSEQUENCEUNSETBYSCHEDULE);
    	m_arrOperationLabelDescriptions.add("Records UNsetting of a Security System sequence by schedule");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMINVOICEEMAILED);
    	m_arrOperationLabelDescriptions.add("Records successful attempt to email an invoice");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMINVOICENOTMAILED);
    	m_arrOperationLabelDescriptions.add("Records failed attempt to email an invoice");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_APBATCHPOST);
    	m_arrOperationLabelDescriptions.add("Records AP batch posting");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_APAGING);
    	m_arrOperationLabelDescriptions.add("Records running of AP Aging report");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_APPRECHECK);
    	m_arrOperationLabelDescriptions.add("Records an AP check run");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_APVENDORTRANSACTIONS);
    	m_arrOperationLabelDescriptions.add("Records running of the AP Vendor Transactions report");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_APCHECKREGISTER);
    	m_arrOperationLabelDescriptions.add("Records running of the AP check register");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_APCHECKRUNPRINTED);
    	m_arrOperationLabelDescriptions.add("Records printing checks from an AP check run");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_APRENUMBER);
    	m_arrOperationLabelDescriptions.add("Records AP vendor number change/merge");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_APGENERATECHECKBATCH);
    	m_arrOperationLabelDescriptions.add("Records creation of an AP check batch");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMGEOCODEREQUEST);
    	m_arrOperationLabelDescriptions.add("Records an SM Geocode request");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMWORKORDERCONCURRENCYERROR);
    	m_arrOperationLabelDescriptions.add("Records an SM Work Order 'concurrency' error (two users trying to save the same work order simultaneously)");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMVIEWAPPOINTMENTCALENDAR);
    	m_arrOperationLabelDescriptions.add("Records viewing of the SM Appointment Calendar");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ICRECEIVINGPO);
    	m_arrOperationLabelDescriptions.add("Records IC PO receipt");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMIMPORTDATA);
    	m_arrOperationLabelDescriptions.add("Records use of the SM Import Data function");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_SMPRINTINTERACTIVEDELIVERYTICKET);
    	m_arrOperationLabelDescriptions.add("Records printing of the 'interactive' Delivery Ticket");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ARCLEARPOSTEDBATCHES);
    	m_arrOperationLabelDescriptions.add("Records clearing of AR Posted Batches");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_ICCLEARPOSTEDBATCHES);
    	m_arrOperationLabelDescriptions.add("Records clearing of IC Posted Batches");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_APCLEARPOSTEDBATCHES);
    	m_arrOperationLabelDescriptions.add("Records clearing of AP Posted Batches");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_BKCLEARPOSTEDBANKSTATEMENTS);
    	m_arrOperationLabelDescriptions.add("Records clearing of BK Bank Statements");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_GLTRIALBALANCE);
    	m_arrOperationLabelDescriptions.add("Records running of GL Trial Balance");

    	m_arrOperationLabelValues.add(LOG_OPERATION_GLBATCHPOST);
    	m_arrOperationLabelDescriptions.add("Records GL transaction batch posting events");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_GLCLEARPOSTEDBATCHES);
    	m_arrOperationLabelDescriptions.add("Records clearing of GL Posted Batches");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_GLCLEARTRANS);
    	m_arrOperationLabelDescriptions.add("Records clearing of historical GL transactions");
    	
    	m_arrOperationLabelValues.add(LOG_OPERATION_GLCLEARFISCALDATA);
    	m_arrOperationLabelDescriptions.add("Records clearing of GL fiscal data");
    	
    	ArrayList<String>arrCompleteListings = new ArrayList<String>(0);
    	for (int i = 0; i <= m_arrOperationLabelValues.size() - 1; i++){
    		arrCompleteListings.add(m_arrOperationLabelValues.get(i) + sDelimiter + m_arrOperationLabelDescriptions.get(i));
    	}
    	Collections.sort(arrCompleteListings);
    	
    	return arrCompleteListings;
    }

}
