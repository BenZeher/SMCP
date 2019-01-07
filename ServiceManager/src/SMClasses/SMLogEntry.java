package SMClasses;

import java.sql.Connection;
import java.sql.Statement;

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
	public final static String LOG_OPERATION_CLEARMONTHLYSTATISTICS= "CLEAR MONTHLY STATISTICS";
	public final static String LOG_OPERATION_ARINVOICEIMPORT= "ARINVOICEIMPORT";
	public final static String LOG_OPERATION_ARRENUMBER = "ARRENUMBER";
	public final static String LOG_OPERATION_DISPLAYCUSTOMERINFO= "DISPLAYCUSTOMERINFO";
	public final static String LOG_OPERATION_ARLISTCUSTOMERSONHOLD = "ARLISTCUSTOMERSONHOLD";
	public final static String LOG_OPERATION_ARLISTINACTIVECUST= "AR LIST INACTIVE CUST";
	public final static String LOG_OPERATION_ARMISCCASHREPORT = "ARMISCCASHREPORT";
	public final static String LOG_OPERATION_ARPOSTINGJOURNAL = "ARPOSTINGJOURNAL";
	public final static String LOG_OPERATION_ARPRINTSTATEMENT = "ARPRINTSTATEMENT";
	public final static String LOG_OPERATION_ARPRINTCALLSHEETS = "ARPRINTCALLSHEETS";
	public final static String LOG_OPERATION_SETINACTIVECUSTOMER = "SETINACTIVECUSTOMER";
	public final static String LOG_OPERATION_ARVIEWCHRONLOG = "ARVIEWCHRONLOG";
	public final static String LOG_OPERATION_AVERAGEMUREPORT= "AVERAGEMUREPORT";
	public final static String LOG_OPERATION_BIDFOLLOWUPREPORT = "BIDFOLLOWUPREPORT";
	public final static String LOG_OPERATION_SMBIDREPORT = "SMBIDREPORT";
	public final static String LOG_OPERATION_PENDINGBIDSREPORT = "PENDINGBIDSREPORT";
	public final static String LOG_OPERATION_SMCANCELEDJOBSREPORT = "SMCANCELEDJOBSREPORT";
	public final static String LOG_OPERATION_CREATECREDITNOTE = "CREATECREDITNOTE";
	public final static String LOG_OPERATION_CREATECREDITNOTEPREVIEW = "CREATECREDITNOTEPREVIEW";
	public final static String LOG_OPERATION_CREATEMULTIPLEINVOICES = "CREATEMULTIPLEINVOICES";
	public final static String LOG_OPERATION_CRITICALDATEREPORT = "CRITICALDATEREPORT";
	public final static String LOG_OPERATION_CUSTOMITEMSONHANDNOTONSALESORDER = "CUSTOMITEMSONHANDNOTONSALESORDER";
	public final static String LOG_OPERATION_SMQUERY = "SMQUERY";
	public final static String LOG_OPERATION_DISPLAYJOBCOSTINFO = "DISPLAYJOBCOSTINFO";
	public final static String LOG_OPERATION_DISPLAYORDERINFORMATION= "DISPLAYORDERINFORMATION";
	public final static String LOG_OPERATION_SMJOBCOSTDAILYREPORT = "SMJOBCOSTDAILYREPORT";
	public final static String LOG_OPERATION_OTHER = "OTHER";
	public final static String LOG_OPERATION_LISTNEARBYORDERREPORT = "LISTNEARBYORDERREPORT";
	public final static String LOG_OPERATION_SMLISTORDERSFORSCHEDULING = "SMLISTORDERSFORSCHEDULING";
	public final static String LOG_OPERATION_MONTHLYBILLINGREPORT = "MONTHLYBILLINGREPORT";
	public final static String LOG_OPERATION_PRINTMONTHLYSALESREPORT = "PRINTMONTHLYSALESREPORT";
	public final static String LOG_OPERATION_SMOPENORDERSREPORT = "SMOPENORDERSREPORT";
	public final static String LOG_OPERATION_SMCHANGEUSERPASSWORD = "SMCHANGEUSERPASSWORD";
	public final static String LOG_OPERATION_SMORDERSOURCELISTING = "SMORDERSOURCELISTING";
	public final static String LOG_OPERATION_PRINTDELIVERYTICKET = "PRINTDELIVERYTICKET";
	public final static String LOG_OPERATION_PRINTINVOICE = "PRINTINVOICE";
	public final static String LOG_OPERATION_PRINTINVOICEAUDIT = "PRINTINVOICEAUDIT";
	public final static String LOG_OPERATION_PRINTPREINVOICE = "PRINTPREINVOICE";
	public final static String LOG_OPERATION_SMPRODUCTIVITYREPORT = "SMPRODUCTIVITYREPORT";
	public final static String LOG_OPERATION_RECALIBRATECOUNTERS = "RECALIBRATECOUNTERS";
	public final static String LOG_OPERATION_SMSALESCONTACTREPORT = "SMSALESCONTACTREPORT";
	public final static String LOG_OPERATION_PRINTUNBILLEDCONTRACTREPORT = "PRINTUNBILLEDCONTRACTREPORT";
	public final static String LOG_OPERATION_EMAILUNBILLEDCONTRACTREPORT = "EMAILUNBILLEDCONTRACTREPORT";
	public final static String LOG_OPERATION_VIEWTRUCKSCHEDULE = "VIEWTRUCKSCHEDULE";
	public final static String LOG_OPERATION_SMWAGESCALEREPORT = "SMWAGESCALEREPORT";
	public final static String LOG_OPERATION_SMWARRANTYSTATUSREPORT = "SMWARRANTYSTATUSREPORT";
	public final static String LOG_OPERATION_TAXBYCATEGORYREPORT = "TAXBYCATEGORYREPORT";
	public final static String LOG_OPERATION_POASSIGNMENT = "POASSIGNMENT";
	public final static String LOG_OPERATION_UPDATERECEIPTSTATUS = "UPDATERECEIPTSTATUS";
	public final static String LOG_OPERATION_CLEARITEMSTATISTICS = "CLEARITEMSTATISTICS";
	public final static String LOG_OPERATION_ICCONVERSION = "ICCONVERSION";
	public final static String LOG_OPERATION_ICITEMSRECEIVEDNOTINVOICED = "ICITEMSRECEIVEDNOTINVOICED";
	public final static String LOG_OPERATION_ICITEMVALUATIONREPORT = "ICITEMVALUATIONREPORT";
	public final static String LOG_OPERATION_LISTUNUSEDPOS = "LISTUNUSEDPOS";
	public final static String LOG_OPERATION_ICONHANDBYDESCRIPTION = "ICONHANDBYDESCRIPTION";
	public final static String LOG_OPERATION_ICPHYSICALINVENTORYVARIANCEREPORT = "ICPHYSICALINVENTORYVARIANCEREPORT";
	public final static String LOG_OPERATION_UPDATEINVNUMONRECEIPT = "UPDATEINVNUMONRECEIPT";
	public final static String LOG_OPERATION_ICPHYSICALINVENTORYWORKSHEET= "ICPHYSICALINVENTORYWORKSHEET";
	public final static String LOG_OPERATION_ICPORECEIVINGREPORT = "ICPORECEIVINGREPORT";
	public final static String LOG_OPERATION_ICPRINTRECEIVINGLABELS = "ICPRINTRECEIVINGLABELS";
	public final static String LOG_OPERATION_ICTRANSACTIONHISTORYREPORT = "ICTRANSACTIONHISTORYREPORT";
	public final static String LOG_OPERATION_ICUNDERSTOCKEDITEMREPORT = "ICUNDERSTOCKEDITEMREPORT";
	public final static String LOG_OPERATION_ICUPDATEPRICE = "ICUPDATEPRICE";
	public final static String LOG_OPERATION_ICVIEWITEMPRICINGREPORT = "ICVIEWITEMPRICINGREPORT";
	public final static String LOG_OPERATION_ARBATCHPOST = "ARBATCHPOST";
	public final static String LOG_OPERATION_DELETEWO = "DELETE WO";
	public final static String LOG_OPERATION_ARCLEARPAIDTRANS = "ARCLEARPAIDTRANS";
	public final static String LOG_OPERATION_APCLEARPAIDTRANS = "APCLEARPAIDTRANS";
	public final static String LOG_OPERATION_ARSMINVOICEIMPORT = "ARSMINVOICEIMPORT";
	public final static String LOG_OPERATION_DELETEBKSTMT = "DELETEBKSTMT";
	public final static String LOG_OPERATION_DELETEORDERLINESFAIL = "DELETEORDERLINESFAIL";
	public final static String LOG_OPERATION_DELETEORDERLINESSUCCEED = "DELETEORDERLINESSUCCEED";
	public final static String LOG_OPERATION_UNSHIPORDERLINESFAIL = "UNSHIPORDERLINESFAIL";
	public final static String LOG_OPERATION_UNSHIPORDERLINESSUCCEED = "UNSHIPORDERLINESSUCCEED";
	public final static String LOG_OPERATION_SHIPORDERLINESFAIL = "SHIPORDERLINESFAIL";
	public final static String LOG_OPERATION_SHIPORDERLINESSUCCEED = "SHIPORDERLINESSUCCEED";
	public final static String LOG_OPERATION_CREATEITEMORDERLINESFAIL = "CREATEITEMORDERLINESFAIL";
	public final static String LOG_OPERATION_CREATEITEMORDERLINESSUCCEED = "CREATEITEMORDERLINESSUCCEED";
	public final static String LOG_OPERATION_MOVEORDERLINESFAIL = "MOVEORDERLINESFAIL";
	public final static String LOG_OPERATION_MOVEORDERLINESSUCCEED = "MOVEORDERLINESSUCCEED";
	public final static String LOG_OPERATION_COPYORDERLINESFAIL = "COPYORDERLINESFAIL";
	public final static String LOG_OPERATION_SETDETAILMECHANICSFAIL = "SETDETAILMECHANICSFAIL";
	public final static String LOG_OPERATION_SETDETAILMECHANICSSSUCCEED = "SETDETAILMECHANICSSSUCCEED";
	public final static String LOG_OPERATION_REPRICEQUOTEFAIL = "REPRICEQUOTEFAIL";
	public final static String LOG_OPERATION_REPRICEQUOTESUCCEED = "REPRICEQUOTESUCCEED";
	public final static String LOG_OPERATION_UNPOSTINGWORKORDER = "UNPOSTINGWORKORDER";
	public final static String LOG_OPERATION_USERLOGIN = "USERLOGIN";
	public final static String LOG_OPERATION_CREATEINVOICE = "CREATEINVOICE";
	public final static String LOG_OPERATION_SMEXECUTESQL = "SMEXECUTESQL";
	public final static String LOG_OPERATION_PRINTSERVICETICKET = "PRINTSERVICETICKET";
	public final static String LOG_OPERATION_PURGEDATA = "PURGEDATA";
	public final static String LOG_OPERATION_SMQUERYDELETE = "SMQUERYDELETE";
	public final static String LOG_OPERATION_SMQUERYSAVE = "SMQUERYSAVE";
	public final static String LOG_OPERATION_UPDATEDATA = "UPDATEDATA";
	public final static String LOG_OPERATION_WORKORDEREMAIL = "WORKORDEREMAIL";
	public final static String LOG_OPERATION_ICASSIGNPO = "ICASSIGNPO";
	public final static String LOG_OPERATION_ICCLEARTRANS = "ICCLEARTRANS";
	public final static String LOG_OPERATION_ICBATCHPOST = "ICBATCHPOST";
	public final static String LOG_OPERATION_ICBATCHPOSTINVOICEUPDATE = "ICBATCHPOSTINVOICEUPDATE";
	public final static String LOG_OPERATION_ICSMINVOICEIMPORT = "ICSMINVOICEIMPORT";
	public final static String LOG_OPERATION_SENDNEWVENDOREMAIL = "SENDNEWVENDOREMAIL";
	public final static String LOG_OPERATION_MOVEDSCHEDULEENTRYFROMSCHEDULESCREEN = "MOVEDSCHEDULEENTRYFROMSCHEDULE";
	public final static String LOG_OPERATION_WORKORDERSAVEDEBUGGING = "WOSAVEDEBUG";
	public final static String LOG_OPERATION_WORKORDERCOMMANDWOFULLDISPLAY = "WOCOMMANDWOFULLDISPLAY";
	public final static String LOG_OPERATION_VIEWUSERSESSIONINFO = "VIEWUSERSESSIONINFO";
	public final static String LOG_OPERATION_VIEWSYSTEMCONFIGURATION = "VIEWSYSTEMCONFIGURATION";
	public final static String LOG_OPERATION_CREATENEWDOCUMENTFOLDER = "CREATENEWDOCUMENTFOLDER";
	public final static String LOG_OPERATION_EMAILEDWOPOSTNOTIFICATION = "EMAILEDWOPOSTNOTIFICATION";
	public final static String LOG_OPERATION_WORKORDERRECENTITEMSDISPLAY = "RECENTITEMSDISPLAY";
	public final static String LOG_OPERATION_POSTINGPERIODVIOLATION = "POSTINGPERIODVIOLATION";
	public final static String LOG_OPERATION_SSSYSTEMREQUEST = "SSSYSTEMREQUEST";
	public final static String LOG_OPERATION_SSSYSTEMERROR = "SSSYSTEMERROR";
	public final static String LOG_OPERATION_ASEMAILSENDERROR = "ASEMAILSENDERROR";
	public final static String LOG_OPERATION_SALESEFFORTCHECK = "SALESEFFORTCHECK";
	public final static String LOG_OPERATION_SMPURGEORDERSREPORT = "SMPURGEORDERSREPORT";
	public final static String LOG_OPERATION_SSDEVICESETBYSCHEDULE = "SSDEVICESETBYSCHEDULE";
	public final static String LOG_OPERATION_SSDEVICEUNSETBYSCHEDULE = "SSDEVICEUNSETBYSCHEDULE";
	public final static String LOG_OPERATION_SSSEQUENCESETBYSCHEDULE = "SSSEQUENCESETBYSCHEDULE";
	public final static String LOG_OPERATION_SSSEQUENCEUNSETBYSCHEDULE = "SSSEQUENCEUNSETBYSCHEDULE";
	public final static String LOG_OPERATION_INVOICEEMAILED = "INVOICEMAILED";
	public final static String LOG_OPERATION_INVOICENOTMAILED = "INVOICENOTMAILED";
	public final static String LOG_OPERATION_APBATCHPOST = "APBATCHPOST";
	public final static String LOG_OPERATION_APAGING = "APAGING";
	public final static String LOG_OPERATION_APPRECHECK = "APPRECHECK";
	public final static String LOG_OPERATION_APVENDORTRANSACTIONS = "APVENDORTRANSACTIONS";
	public final static String LOG_OPERATION_APCHECKREGISTER = "APCHECKREGISTER";
	public final static String LOG_OPERATION_APCHECKRUNPRINTED = "APCHECKRUNPRINTED";
	public final static String LOG_OPERATION_APRENUMBER = "APRENUMBER";
	public final static String LOG_OPERATION_APGENERATECHECKBATCH = "APGENERATECHECKBATCH";
	public final static String LOG_OPERATION_GEOCODEREQUEST = "GEOCODEREQUEST";
	public final static String LOG_OPERATION_WORKORDERCONCURRENCYERROR = "WORKORDERCONCURRENCYERROR";
	public final static String LOG_OPERATION_VIEWAPPOINTMENTCALENDAR = "APPOINTMENTCALENDAR";
	public final static String LOG_OPERATION_RECEIVINGPO = "RECEIVINGPO";
	public final static String LOG_OPERATION_IMPORTDATA = "IMPORTDATA";
	public final static String LOG_OPERATION_PRINTINTERACTIVEDELIVERYTICKET = "PRINTINTERACTIVDELIVERYTICKET";
	
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
}
