package smic;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import SMClasses.SMLogEntry;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import smcontrolpanel.SMUtilities;

public class ICConvertACCPAC extends java.lang.Object{

	private String m_sErrorMessage;
	private ArrayList<String>arrStatus = new ArrayList<String>(0);
	private long lStartingTime = 0;
	private static String BLANK_REPLACEMENT_CHARACTER = "_";
	public ICConvertACCPAC(
			){
		m_sErrorMessage = "";
	}
	public boolean convertICData(
			Connection conn,
			Connection conACCPAC,
			String sUserID,
			PrintWriter out
			){

		lStartingTime = System.currentTimeMillis();
		long lCounter = 0;
		ResultSet rs;
		String SQL = "";
		String sTable = "";
		
		//If the IC import flag is turned on, that means we're using live data and we don't
		//want to write to it:
		if (SMUtilities.getICImportFlag(conn)){
			m_sErrorMessage = "Your IC data is turned on - that indicates that the IC data is live"
			+ " and you cannot convert ACCPAC data into it."
			;
			return false;
		}
		
		//First, start a log for the conversion:
	    SMLogEntry log = new SMLogEntry(conn);
	    log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Starting IC Data conversion", "", "[1376509377]");

	    //Next, remove all the tables if they exist:
	    if (!dropTable(conn, log, "icitems", sUserID)){
	    	return false;
	    }
	    if (!dropTable(conn, log, "iccategories", sUserID)){
	    	return false;
	    }
	    if (!dropTable(conn, log, "icaccountsets", sUserID)){
	    	return false;
	    }
	    if (!dropTable(conn, log, "iccosts", sUserID)){
	    	return false;
	    }
	    if (!dropTable(conn, log, "icitemlocations", sUserID)){
	    	return false;
	    }
	    if (!dropTable(conn, log, "icitemprices", sUserID)){
	    	return false;
	    }
	    if (!dropTable(conn, log, "icitemstatistics", sUserID)){
	    	return false;
	    }
	    if (!dropTable(conn, log, "icvendoritems", sUserID)){
	    	return false;
	    }
	    //Leaving this one out for now:
	    //if (!dropTable(conn, log, "icoptions", sUserName)){
	    //	return false;
	    //}
	    if (!dropTable(conn, log, "icbatchentries", sUserID)){
	    	return false;
	    }
	    if (!dropTable(conn, log, "icentrylines", sUserID)){
	    	return false;
	    }
	    if (!dropTable(conn, log, "icbatches", sUserID)){
	    	return false;
	    }
	    if (!dropTable(conn, log, "ictransactions", sUserID)){
	    	return false;
	    }
	    if (!dropTable(conn, log, "ictransactiondetails", sUserID)){
	    	return false;
	    }
	    if (!dropTable(conn, log, "icphysicalinventories", sUserID)){
	    	return false;
	    }
	    if (!dropTable(conn, log, "icphysicalcounts", sUserID)){
	    	return false;
	    }
	    if (!dropTable(conn, log, "icphysicalcountlines", sUserID)){
	    	return false;
	    }
	    if (!dropTable(conn, log, "icinventoryworksheet", sUserID)){
	    	return false;
	    }
	    if (!dropTable(conn, log, "icvendorterms", sUserID)){
	    	return false;
	    }
	    //Create tables:
	    sTable = "icitems";
	    SQL =
	    	"CREATE TABLE `icitems` ("
	    	+ "`sitemnumber` varchar(24) NOT NULL default '',"
	    	+ " `sitemdescription` varchar(75) NOT NULL default '',"
	    	+ " `datlastmaintained` datetime NOT NULL default '0000-00-00 00:00:00',"
	    	+ " `iactive` int(11) NOT NULL default '1',"
	    	+ " `scategorycode` varchar(6) NOT NULL default '',"
	    	+ " `sdefaultpricelistcode` varchar(6) NOT NULL default '',"
	    	+ " `spickingsequence` varchar(10) NOT NULL default '',"
	    	+ " `datinactive` datetime NOT NULL default '0000-00-00 00:00:00',"
	    	+ " `scomment1` varchar(80) NOT NULL default '',"
	    	+ " `scomment2` varchar(80) NOT NULL default '',"
	    	+ " `scomment3` varchar(80) NOT NULL default '',"
	    	+ " `scomment4` varchar(80) NOT NULL default '',"
	    	+ " `slastedituser` varchar(128) NOT NULL default '',"
	    	+ " `itaxable` int(11) NOT NULL default '1',"
	    	+ " `scostunitofmeasure` varchar(10) NOT NULL default '',"
	    	+ " `sdedicatedtoordernumber` varchar(22) NOT NULL default '',"
	    	+ " `sreportgroup1` varchar(75) NOT NULL default '',"
	    	+ " `sreportgroup2` varchar(75) NOT NULL default '',"
	    	+ " `sreportgroup3` varchar(75) NOT NULL default '',"
	    	+ " `sreportgroup4` varchar(75) NOT NULL default '',"
	    	+ " `sreportgroup5` varchar(75) NOT NULL default '',"
	    	+ " `bdmostrecentcost` decimal(17,4) NOT NULL default '0.0000',"
	    	+ " `ilaboritem` int(11) NOT NULL default '0',"
	    	+ " `inonstockitem` int(11) NOT NULL default '0',"
	    	+ " `bdnumberoflabels` decimal(17,4) NOT NULL default '1.0000',"
	    	+ " PRIMARY KEY  (`sitemnumber`)"
	    	+ ", KEY uomkey (`scostunitofmeasure`)"
	    	+ ") ENGINE=InnoDB"
	    ;
	    if (!createTable(conn, log, sTable, sUserID, SQL)){
	    	return false;
	    }
	    sTable = "iccategories";
	    SQL =
			"CREATE TABLE `iccategories` ("
			+ " `scategorycode` varchar(6) NOT NULL default '',"
			+ " `sdescription` varchar(60) NOT NULL default '',"
			+ " `scostofgoodssoldacct` varchar(45) NOT NULL default '',"
			+ " `ssalesaccount` varchar(45) NOT NULL default '',"
			+ " `datlastmaintained` datetime NOT NULL default '0000-00-00 00:00:00',"
			+ " `iactive` int(11) NOT NULL default '1',"
			+ " `datinactive` datetime NOT NULL default '0000-00-00 00:00:00',"
			+ " `slastedituser` varchar(128) NOT NULL default '',"
			+ " PRIMARY KEY  (`scategorycode`)"
			+ ") ENGINE=InnoDB"
		;
	    if (!createTable(conn, log, sTable, sUserID, SQL)){
	    	return false;
	    }
	    sTable = "icaccountsets";
	    SQL =
			"CREATE TABLE `icaccountsets` ("
			+ " `saccountsetcode` varchar(6) NOT NULL default '',"
			+ " `slastedituser` varchar(128) NOT NULL default '',"
			+ " `sdescription` varchar(30) NOT NULL default '',"
			+ " `icostingmethod` int(11) NOT NULL default '0',"
			+ " `sinventoryaccount` varchar(45) NOT NULL default '',"
			+ " `spayablesclearingaccount` varchar(45) NOT NULL default '',"
			+ " `sadjustmentwriteoffaccount` varchar(45) NOT NULL default '',"
			+ " `snonstockclearingaccount` varchar(45) NOT NULL default '',"
			+ " `stransferclearingaccount` varchar(45) NOT NULL default '',"
			+ " `iactive` int(11) NOT NULL default '1',"
			+ " `datlastmaintained` datetime NOT NULL default '0000-00-00 00:00:00',"
			+ " `datinactive` datetime NOT NULL default '0000-00-00 00:00:00',"
			+ " PRIMARY KEY  (`saccountsetcode`)"
			+ " ) ENGINE=InnoDB"
		;
	    if (!createTable(conn, log, sTable, sUserID, SQL)){
	    	return false;
	    }
	    sTable = "iccosts";
	    SQL =
			"CREATE TABLE `iccosts` ("
			+ " `id` int(11) NOT NULL auto_increment,"
			+ " `sitemnumber` varchar(24) NOT NULL default '',"
			+ " `slocation` varchar(6) NOT NULL default '',"
			+ " `bdqty` decimal(17,4) NOT NULL default '0.0000',"
			+ " `isource` int(11) NOT NULL default '0',"
			+ " `sponumber` varchar(22) NOT NULL default '',"
			+ " `sremark` varchar(128) NOT NULL default '',"
			+ " `datcreationdate` datetime NOT NULL default '0000-00-00 00:00:00',"
			+ " `bdcost` decimal(17,2) NOT NULL default '0.00',"
			+ " `bdcostshipped` decimal(17,2) NOT NULL default '0.00',"
			+ " `bdqtyshipped` decimal(17,2) NOT NULL default '0.00',"
			+ " `sreceiptnumber` varchar(22) NOT NULL default '',"
			+ " `lreceiptlineid` int(11) NOT NULL default '-1',"
			+ " PRIMARY KEY  (`id`),"
			+ " KEY `itemnumberkey` (`sitemnumber`),"
			+ " KEY `locationkey` (`slocation`)"
			+ " ) ENGINE=InnoDB"
		;
	    if (!createTable(conn, log, sTable, sUserID, SQL)){
	    	return false;
	    }
	    sTable = "icitemlocations";
	    SQL =
			"CREATE TABLE `icitemlocations` ("
			+ " `slocation` varchar(6) NOT NULL default '',"
			+ " `sitemnumber` varchar(24) NOT NULL default '',"
			+ " `bdqtyonhand` decimal(17,4) NOT NULL default '0.0000',"
			+ " `bdtotalcost` decimal(17,2) NOT NULL default '0.00',"
			+ " PRIMARY KEY  (`slocation`,`sitemnumber`),"
			+ " KEY `itemnumberkey` (`sitemnumber`),"
			+ " KEY `locationkey` (`slocation`)"
			+ ") ENGINE=InnoDB"
		;
	    if (!createTable(conn, log, sTable, sUserID, SQL)){
	    	return false;
	    }
	    sTable = "icitemprices";
	    SQL =
			"CREATE TABLE `icitemprices` ("
			+ " `sitemnumber` varchar(24) NOT NULL default '',"
			+ " `spricelistcode` varchar(6) NOT NULL default '',"
			+ " `datlastmaintained` datetime NOT NULL default '0000-00-00 00:00:00',"
			+ " `slastedituser` varchar(128) NOT NULL default '',"
			+ " `bdbaseprice` decimal(17,2) NOT NULL default '0.00',"
			+ " `bdlevel1price` decimal(17,2) NOT NULL default '0.00',"
			+ " `bdlevel2price` decimal(17,2) NOT NULL default '0.00',"
			+ " `bdlevel3price` decimal(17,2) NOT NULL default '0.00',"
			+ " `bdlevel4price` decimal(17,2) NOT NULL default '0.00',"
			+ " `bdlevel5price` decimal(17,2) NOT NULL default '0.00',"
			+ " PRIMARY KEY  (`sitemnumber`,`spricelistcode`),"
			+ " KEY `sitemnumber` (`sitemnumber`),"
			+ " KEY `spricelistcode` (`spricelistcode`)"
			+ " ) ENGINE=InnoDB"
		;
	    if (!createTable(conn, log, sTable, sUserID, SQL)){
	    	return false;
	    }
	    sTable = "icitemstatistics";
	    SQL =
			"CREATE TABLE `icitemstatistics` ("
			+ " `sitemnumber` varchar(24) NOT NULL default '',"
			+ " `slocation` varchar(6) NOT NULL default '',"
			+ " `lyear` int(11) NOT NULL default '0',"
			+ " `lmonth` int(11) NOT NULL default '0',"
			+ " `bdqtysold` decimal(17,4) NOT NULL default '0.0000',"
			+ " `lcountsold` int(11) NOT NULL default '0',"
			+ " `bdamountsold` decimal(17,2) NOT NULL default '0.00',"
			+ " `bdcostofitemssold` decimal(17,2) NOT NULL default '0.00',"
			+ " `bdqtyreturned` decimal(17,4) NOT NULL default '0.0000',"
			+ " `lcountreturned` int(11) NOT NULL default '0',"
			+ " `bdamountreturned` decimal(17,2) NOT NULL default '0.00',"
			+ " `bdcostofitemsreturned` decimal(17,2) NOT NULL default '0.00',"
			+ " PRIMARY KEY  (`sitemnumber`,`slocation`,`lyear`,`lmonth`),"
			+ " KEY `sitemnumber` (`sitemnumber`)"
			+ " ) ENGINE=InnoDB"
		;
	    if (!createTable(conn, log, sTable, sUserID, SQL)){
	    	return false;
	    }
	    //Leaving this one out for now:
	    //sTable = "icoptions";
	    //SQL =
		//	"CREATE TABLE `icoptions` ("
		//	+ " `ibatchpostinginprocess` int(11) NOT NULL default '0',"
		//	+ " `suser` varchar(128) NOT NULL default '',"
		//	+ " `sprocess` varchar(128) NOT NULL default '',"
		//	+ " `datstartdate` datetime NOT NULL default '0000-00-00 00:00:00',"
		//	+ " `lcostingmethod` int(11) NOT NULL default '0',"
		//	+ " `lallownegativeqtys` int(11) NOT NULL default '1',"
		//	+ " `iflagimports` int(11) NOT NULL default '1',"
		//	+ " `ssistercompanyname1` varchar(128) NOT NULL default '',"
		//	+ " `ssistercompanyname2` varchar(128) NOT NULL default '',"
		//	+ " `ssistercompanydb1` varchar(128) NOT NULL default '',"
		//	+ " `ssistercompanydb2` varchar(128) NOT NULL default ''"
	    //  + " `iexportto` int(11) NOT NULL default '0'"
		//	+ " ) ENGINE=InnoDB"
		//;
	    //if (!createTable(conn, log, sTable, sUserName, SQL)){
	    //	return false;
	    //}

	    sTable = "icbatchentries";
	    SQL =
			"CREATE TABLE `icbatchentries` ("
			+ " `lid` int(11) NOT NULL auto_increment,"
			+ " `lbatchnumber` int(11) NOT NULL default '0',"
			+ " `lentrynumber` int(11) NOT NULL default '0',"
			+ " `ientrytype` int(11) NOT NULL default '0',"
			+ " `sdocnumber` varchar(75) NOT NULL default '',"
			+ " `sentrydescription` varchar(128) NOT NULL default '',"
			+ " `datentrydate` datetime NOT NULL default '0000-00-00 00:00:00',"
			+ " `llastline` int(11) NOT NULL default '0',"
			+ " `scontrolacct` varchar(75) NOT NULL default '',"
			+ " `bdentryamount` decimal(17,2) NOT NULL default '0.00',"
			+ " PRIMARY KEY  (`lid`),"
			+ " UNIQUE KEY `batch_entry_key` (`lbatchnumber`,`lentrynumber`),"
			+ " KEY `entrynumberkey` (`lentrynumber`)"
			+ " ) ENGINE=InnoDB"
		;
	    if (!createTable(conn, log, sTable, sUserID, SQL)){
	    	return false;
	    }
	    sTable = "icentrylines";
	    SQL =
			"CREATE TABLE `icentrylines` ("
			+ " `lid` int(11) NOT NULL auto_increment,"
			+ " `lbatchnumber` int(11) NOT NULL default '0',"
			+ " `lentrynumber` int(11) NOT NULL default '0',"
			+ " `llinenumber` int(11) NOT NULL default '0',"
			+ " `sitemnumber` varchar(24) NOT NULL default '',"
			+ " `slocation` varchar(6) NOT NULL default '',"
			+ " `bdqty` decimal(17,4) NOT NULL default '0.0000',"
			+ " `bdcost` decimal(17,4) NOT NULL default '0.00',"
			+ " `bdprice` decimal(17,4) NOT NULL default '0.00',"
			+ " `scontrolacct` varchar(75) NOT NULL default '',"
			+ " `sdistributionacct` varchar(75) NOT NULL default '',"
			+ " `sdescription` varchar(75) NOT NULL default '',"
			+ " `scomment` varchar(255) NOT NULL default '',"
			+ " `lentryid` int(11) NOT NULL default '0',"
			+ " `scategorycode` varchar(6) NOT NULL default '0.00',"
			+ " `sreceiptnum` varchar(22) NOT NULL default '',"
			+ " `lcostbucketid` int(11) NOT NULL default '-1',"
			+ " `stargetlocation` varchar(6) NOT NULL default '',"
			+ " `sinvoicenumber` varchar(15) NOT NULL default '',"
			+ " `linvoicelinenumber` int(11) NOT NULL default '0',"
			+ " `lreceiptlineid` int(11) NOT NULL default '-1',"
			+ " PRIMARY KEY  (`lid`),"
			+ " UNIQUE KEY `batch_entry_line_key` (`lbatchnumber`,`lentrynumber`, `llinenumber`),"
			+ " KEY `linenumberkey` (`llinenumber`)"
			+ " ) ENGINE=InnoDB"
		;
	    if (!createTable(conn, log, sTable, sUserID, SQL)){
	    	return false;
	    }
	    sTable = "icbatches";
	    SQL =
			"CREATE TABLE `icbatches` ("
			+ " `lbatchnumber` int(11) NOT NULL auto_increment,"
			+ " `datbatchdate` datetime NOT NULL default '0000-00-00 00:00:00',"
			+ " `ibatchstatus` int(11) NOT NULL default '0',"
			+ " `sbatchdescription` varchar(128) NOT NULL default '',"
			+ " `ibatchtype` int(11) NOT NULL default '0',"
			+ " `datlasteditdate` datetime NOT NULL default '0000-00-00 00:00:00',"
			+ " `lbatchlastentry` int(11) NOT NULL default '0',"
			+ " `screatedby` varchar(128) NOT NULL default '',"
			+ " `slasteditedby` varchar(128) NOT NULL default '',"
			+ " `smoduletype` char(2) NOT NULL default '',"
			+ " `datpostdate` datetime NOT NULL default '0000-00-00 00:00:00',"
			+ " PRIMARY KEY  (`lbatchnumber`)"
			+ " ) ENGINE=InnoDB"
		;
	    if (!createTable(conn, log, sTable, sUserID, SQL)){
	    	return false;
	    }
	    sTable = "ictransactions";
	    SQL =
			"CREATE TABLE `ictransactions` ("
			+ " `lid` int(11) NOT NULL auto_increment,"
			+ " `loriginalbatchnumber` int(11) NOT NULL default '0',"
			+ " `loriginalentrynumber` int(11) NOT NULL default '0',"
			+ " `loriginallinenumber` int(11) NOT NULL default '0',"
			+ " `sitemnumber` varchar(24) NOT NULL default '',"
			+ " `slocation` varchar(6) NOT NULL default '',"
			+ " `ientrytype` int(11) NOT NULL default '0',"
			+ " `sdocnumber` varchar(75) NOT NULL default '',"
			+ " `sentrydescription` varchar(128) NOT NULL default '',"
			+ " `slinedescription` varchar(75) NOT NULL default '',"
			+ " `datpostingdate` datetime NOT NULL default '0000-00-00 00:00:00',"
			+ " `bdqty` decimal(17,4) NOT NULL default '0.0000',"
			+ " `bdcost` decimal(17,4) NOT NULL default '0.00',"
			+ " `bdprice` decimal(17,4) NOT NULL default '0.00',"
			+ " `scontrolacct` varchar(75) NOT NULL default '',"
			+ " `sdistributionacct` varchar(75) NOT NULL default '',"
			+ " `llineid` int(11) NOT NULL default '0',"
			+ " `spostedby` varchar(128) NOT NULL default '',"
			+ " `sunitofmeasure` varchar(10) NOT NULL default '',"
			+ " PRIMARY KEY  (`lid`),"
			+ " KEY `itemnumberkey` (`sitemnumber`)"
			+ " ) ENGINE=InnoDB"
		;
	    if (!createTable(conn, log, sTable, sUserID, SQL)){
	    	return false;
	    }
	    
	    //ictransactiondetails:
	    sTable = "ictransactiondetails";
	    SQL =
	    	"create table ictransactiondetails ("
				 + "`lid` int(11) NOT NULL auto_increment,"
				 + "`ldetailnumber` int(11) NOT NULL DEFAULT '-1',"
				 + "`ltransactionid` int(11) NOT NULL DEFAULT '-1',"
				 + "`lcostbucketid` int(11) NOT NULL DEFAULT '-1',"
				 + "`dattimecostbucketcreation` datetime NOT NULL default '0000-00-00 00:00:00',"
				 + "`scostbucketlocation` varchar(6) NOT NULL DEFAULT '',"
				 + "`scostbucketremark` varchar(128) NOT NULL DEFAULT '',"
				 + "`lcostbucketreceiptlineid` int(11) NOT NULL DEFAULT '-1',"
				 + "`bdcostbucketcostbeforetrans` DECIMAL(17,2) NOT NULL DEFAULT '0.00',"
				 + "`bdcostchange` DECIMAL(17,2) NOT NULL DEFAULT '0.00',"
				 + "`bdcostbucketqtybeforetrans` DECIMAL(17,4) NOT NULL DEFAULT '0.0000',"
				 + "`bdqtychange` DECIMAL(17,4) NOT NULL DEFAULT '0.0000',"
				 + "PRIMARY KEY (lid),"
				 + "KEY `transactionidkey` (`ltransactionid`),"
				 + "KEY `costbucketidkey` (`lcostbucketid`)"
			+ ") engine=innodb"
		;
	    if (!createTable(conn, log, sTable, sUserID, SQL)){
	    	return false;
	    }
	    
	    sTable = "icphysicalinventories";
	    SQL =
			"CREATE TABLE `icphysicalinventories` ("
			+ " `lid` int(11) NOT NULL auto_increment,"
			+ " `sdesc` varchar(128) NOT NULL default '',"
			+ " `screatedby` varchar(128) NOT NULL default '',"
			+ " `datcreated` datetime NOT NULL default '0000-00-00 00:00:00',"
			+ " `istatus` int(11) NOT NULL default '1',"
			+ " `lbatchnumber` int(11) NOT NULL default '0',"
			+ " `slocation` varchar(6) NOT NULL default '',"
			+ " `sstartingitemnumber` varchar(24) NOT NULL default '',"
			+ " `sendingitemnumber` varchar(24) NOT NULL default '',"
			+ " PRIMARY KEY  (`lid`)"
			+ " ) ENGINE=InnoDB"
		;
	    if (!createTable(conn, log, sTable, sUserID, SQL)){
	    	return false;
	    }
	    sTable = "icphysicalcounts";
	    SQL =
			"CREATE TABLE `icphysicalcounts` ("
			+ " `lid` int(11) NOT NULL auto_increment,"
			+ " `lphysicalinventoryid` int(11) NOT NULL default '0',"
			+ " `sdesc` varchar(128) NOT NULL default '',"
			+ " `screatedby` varchar(128) NOT NULL default '',"
			+ " `datcreated` datetime NOT NULL default '0000-00-00 00:00:00',"
			+ " PRIMARY KEY  (`lid`)"
			+ " ) ENGINE=InnoDB"
			;
	    if (!createTable(conn, log, sTable, sUserID, SQL)){
	    	return false;
	    }
	    sTable = "icphysicalcountlines";
	    SQL =
			"CREATE TABLE `icphysicalcountlines` ("
			+ " `lid` int(11) NOT NULL auto_increment,"
			+ " `lphysicalinventoryid` int(11) NOT NULL default '0',"
			+ " `lcountid` int(11) NOT NULL default '0',"
			+ " `bdqty` decimal(17,4) NOT NULL default '0.0000',"
			+ " `sitemnumber` varchar(24) NOT NULL default '',"
			+ " `sunitofmeasure` varchar(10) NOT NULL default '',"
			+ " `datcreated` datetime NOT NULL default '0000-00-00 00:00:00',"
			+ " PRIMARY KEY  (`lid`),"
			+ " KEY `invcountkey` (`lphysicalinventoryid`, `lid`)"
			+ " ) ENGINE=InnoDB"
			;
	    if (!createTable(conn, log, sTable, sUserID, SQL)){
	    	return false;
	    }
	    sTable = "icinventoryworksheet";
	    SQL =
			"CREATE TABLE `icinventoryworksheet` ("
			+ " `lid` int(11) NOT NULL auto_increment,"
			+ " `lphysicalinventoryid` int(11) NOT NULL default '0',"
			+ " `slocation` varchar(6) NOT NULL default '',"
			+ " `sitemnumber` varchar(24) NOT NULL default '',"
			+ " `bdqtyonhand` decimal(17,4) NOT NULL default '0.0000',"
			+ " `bdmostrecentcost` decimal(17,4) NOT NULL default '0.00',"
			+ " `sinvacct` varchar(75) NOT NULL default '',"
			+ " `swriteoffacct` varchar(75) NOT NULL default '',"
			+ " PRIMARY KEY  (`lid`)"
			+ " ) ENGINE=InnoDB"	
		;
	    if (!createTable(conn, log, sTable, sUserID, SQL)){
	    	return false;
	    }
	    
	    sTable = "icvendoritems_temp";
	    if (!dropTable(conn, log, sTable, sUserID)){
	    	return false;
	    }
	    SQL =
	    	"CREATE TABLE `icvendoritems_temp` ("
	    	+ "`sitemnumber` varchar(24) NOT NULL default '',"
	    	+ " `svendor` varchar(12) NOT NULL default '',"
	    	+ " `svendoritemnumber` varchar(24) NOT NULL default '',"
	    	+ " `bdcost` decimal(17,4) NOT NULL default '0.0000',"
	    	+ " `scomment` varchar(128) NOT NULL default ''"
	    	+ ") ENGINE=InnoDB"
	    ;
	    if (!createTable(conn, log, sTable, sUserID, SQL)){
	    	return false;
	    }

	    sTable = "icvendorterms";
	    SQL =
			"CREATE TABLE `icvendorterms` ("
			+ " `stermscode` varchar(6) NOT NULL default '',"
			+ " `sdescription` varchar(60) NOT NULL default '',"
			+ " `iactive` int(11) NOT NULL default '1',"
			+ " `datlastmaintained` datetime NOT NULL default '0000-00-00 00:00:00',"
			+ " `bdDiscountPercent` decimal (17,4) NOT NULL default '0.0000',"
			+ " `idiscountnumberofdays` int(11) NOT NULL default '0',"
			+ " `idiscountdayofthemonth` int(11) NOT NULL default '0',"
			+ " `iduenumberofdays` int(11) NOT NULL default '0',"
			+ " `iduedayofthemonth` int(11) NOT NULL default '0',"
			+ " PRIMARY KEY  (`stermscode`)"
			+ ") ENGINE=InnoDB"
		;
	    if (!createTable(conn, log, sTable, sUserID, SQL)){
	    	return false;
	    }
	    
	    //Next convert each table:
	    //icaccountsets:
	    SQL =
	    	"SELECT *"
	    	//+ " ICACCT.CNTLACCT"
	    	//+ ", \"DESC\""
	    	//+ ", ICCACT.COSTMETHOD"
	    	//+ ", ICACCT.INVACCT"
	    	//+ ", ICACCT.PAYABLACCT"
	    	//+ ", ICACCT.ADJWRTACCT"
	    	//+ ", ICACCT.NONSTKACCT"
	    	//+ ", ICACCT.TRANSACCT"
	    	//+ ", ICACCT.INACTIVE"
	    	//+ ", ICACCT.DATELASTMN"
	    	//+ ", ICACCT.DATEINACTV"
	    	+ " FROM ICACCT"
	    	;
	    
    	try {
			rs = clsDatabaseFunctions.openResultSet(SQL, conACCPAC);
			while (rs.next()){
				SQL = 
					"INSERT INTO icaccountsets ("
			        + "saccountsetcode"
			        + ", slastedituser"
			        + ", sdescription"
			        + ", icostingmethod"
			        + ", sinventoryaccount"
			        + ", spayablesclearingaccount"
			        + ", sadjustmentwriteoffaccount"
			        + ", snonstockclearingaccount"
			        + ", stransferclearingaccount"
			        + ", iactive"
			        + ", datlastmaintained"
			        + ", datinactive"
			        + ") VALUES ("
			        + "'" + rs.getString("ICACCT.CNTLACCT").trim() + "'"
			        + ", 'CONVERSION'"
			        + ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("ICACCT.DESC").trim()) + "'"
			        + ", " + rs.getLong("ICACCT.COSTMETHOD")
			        + ", '" + rs.getString("ICACCT.INVACCT").trim().replace("-", "") + "'"
			        + ", '" + rs.getString("ICACCT.PAYABLACCT").trim().replace("-", "") + "'"
			        + ", '" + rs.getString("ICACCT.ADJWRTACCT").trim().replace("-", "") + "'"
			        + ", '" + rs.getString("ICACCT.NONSTKACCT").trim().replace("-", "") + "'"
			        + ", '" + rs.getString("ICACCT.TRANSACCT").trim().replace("-", "") + "'"
			        ;
					if (rs.getInt("ICACCT.INACTIVE") == 0){
						SQL += ", 1";
					}else{
						SQL += ", 0";
					}
					SQL +=
					", '" + convertACCPACLongDateToString(rs.getLong("ICACCT.DATELASTMN"), false) + "'" 
					+ ", '" + convertACCPACLongDateToString(rs.getLong("ICACCT.DATEINACTV"), false) + "'"
					+ ")"
			    ;
				if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
					log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error updating icaccountsets", SQL, "[1376509577]");
					m_sErrorMessage = "Error updating icaccountsets with SQL: " + SQL;
					rs.close();
					return false;
				}
				lCounter++;
			}
			rs.close();
		} catch (SQLException e) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error updating icaccountsets", e.getMessage(), "[1376509578]");
			m_sErrorMessage = "Error updating icaccountsets with SQL: " + e.getMessage();
			return false;			
		}
		
    	log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Successfully updated " + lCounter + " icaccountsets", "", "[1376509579]");
    	//arrStatus.add("Updated " + lCounter + " icaccountsets - " 
    	//	+ SMUtilities.getElapsedTime(lStartingTime) + " elapsed.");
    	
        //iccategories:
	    SQL =
	        "SELECT *"
	        //+ " ICCATG.CATEGORY"
	        //+ ", ICCATG.DESC"
	        //+ ", ICCATG.COGSACCT"
	        //+ ", ICCATG.REVENUACCT"
	        //+ ", DATELASTMN"
	        //+ ", ICATG.INACTIVE"
	        //+ ", ICATG.DATEINACTV"
	        + " FROM ICCATG"
	    	;
	    lCounter = 0;
    	try {
			rs = clsDatabaseFunctions.openResultSet(SQL, conACCPAC);
			while (rs.next()){
				SQL = 
					"INSERT INTO iccategories ("
			        + "scategorycode"
			        + ", sdescription"
			        + ", scostofgoodssoldacct"
			        + ", ssalesaccount"
			        + ", datlastmaintained"
			        + ", iactive"
			        + ", datinactive"
			        + ", slastedituser"
			        + ") VALUES ("
			        + "'" + rs.getString("ICCATG.CATEGORY").trim() + "'"
			        + ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("ICCATG.DESC").trim()) + "'"
			        + ", '" + rs.getString("ICCATG.COGSACCT").trim().replace("-", "") + "'"
			        + ", '" + rs.getString("ICCATG.REVENUACCT").trim().replace("-", "") + "'"
			        + ", '" + convertACCPACLongDateToString(rs.getLong("ICCATG.DATELASTMN"), false) + "'" 
			        ;
					if (rs.getInt("ICCATG.INACTIVE") == 0){
						SQL += ", 1";
					}else{
						SQL += ", 0";
					}
			        SQL += 
			        ", '" + convertACCPACLongDateToString(rs.getLong("ICCATG.DATEINACTV"), false) + "'" 
			        + ", 'CONVERSION'"
					+ ")"
			    ;
				if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
					log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error updating iccategories", SQL, "[1376509580]");
					m_sErrorMessage = "Error updating iccategories with SQL: " + SQL;
					rs.close();
					return false;
				}
				lCounter++;
			}
			rs.close();
		} catch (SQLException e) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error updating iccategories", e.getMessage(), "[1376509581]");
			m_sErrorMessage = "Error updating iccategories with SQL: " + e.getMessage();
			return false;			
		}
		
    	log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Successfully updated " + lCounter + " iccategories", "", "[1376509582]");
    	//arrStatus.add("Updated " + lCounter + " iccategories - " 
    	//	+ SMUtilities.getElapsedTime(lStartingTime) + " elapsed.");

        //icitems:
	    SQL =
	    	"SELECT *"
            //+ " ICITEM.ITEMNO"
            //+ ", ICITEM.DESC"
            //+ ", ICITEM.DATELASTMN"
            //+ ", ICITEM.INACTIVE"
            //+ ", ICITEM.CATEGORY"
            //+ ", ICITEM.STOCKUNIT"
            //+ ", ICITEM.DEFPRICLST"
            //+ ", ICITEM.PICKINGSEQ"
            //+ ", ICITEM.DATEINACTV"
            //+ ", ICITEM.COMMENT1"
            //+ ", ICITEM.COMMENT2"
            //+ ", ICITEM.COMMENT3"
            //+ ", ICITEM.COMMENT4"
            //+ ", ICITEM.COMMODIM"
            + " FROM ICITEM"
	    	;
	    lCounter = 0;
    	try {
			rs = clsDatabaseFunctions.openResultSet(SQL, conACCPAC);
			while (rs.next()){
				SQL = 
					"INSERT INTO icitems ("
		            + "sitemnumber"
		            + ", sitemdescription"
		            + ", datlastmaintained"
		            + ", iactive"
		            + ", scategorycode"
		            + ", scostunitofmeasure"
		            + ", sdefaultpricelistcode"
		            + ", spickingsequence"
		            + ", datinactive"
		            + ", scomment1"
		            + ", scomment2"
		            + ", scomment3"
		            + ", scomment4"
		            + ", slastedituser"
		            + ", itaxable"
		            + ", sdedicatedtoordernumber"
		            + ", sreportgroup5"
		            + ", ilaboritem"
			        + ") VALUES ("
			        + "'" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("ICITEM.ITEMNO").trim().replace("-", "").replace(" ", BLANK_REPLACEMENT_CHARACTER)) + "'"
			        + ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("ICITEM.DESC").trim()) + "'"
			        + ", '" + convertACCPACLongDateToString(rs.getLong("ICITEM.DATELASTMN"), false) + "'"
			        ;
					if (rs.getInt("ICITEM.INACTIVE") == 0){
						SQL += ", 1";
					}else{
						SQL += ", 0";
					}
			        SQL +=
			        ", '" + rs.getString("ICITEM.CATEGORY").trim() + "'"
			        + ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("ICITEM.STOCKUNIT").trim().replace(".", "")) + "'"
			        + ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("ICITEM.DEFPRICLST").trim()) + "'"
			        + ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("ICITEM.PICKINGSEQ").trim()) + "'"
			        + ", '" + convertACCPACLongDateToString(rs.getLong("ICITEM.DATEINACTV"), false) + "'"
			        + ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("ICITEM.COMMENT1").trim()) + "'"
			        + ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("ICITEM.COMMENT2").trim()) + "'"
			        + ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("ICITEM.COMMENT3").trim()) + "'"
			        + ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("ICITEM.COMMENT4").trim()) + "'"
			        + ", 'CONVERSION'"
			        + ", 1" //For now, every item is considered taxable
			        ;
			        
			        //Try to strip off the order number:
			        //SQL = SQL & ", IIf((IsNumericString([ITEMNO]) = true) And (Len(Trim([ITEMNO]))>7), Right(Trim([ITEMNO]),Len(Trim([ITEMNO]))-3), '') AS DEDICATEDORDER"
			        String sItemNum = rs.getString("ICITEM.ITEMNO").trim();
			        String sOrderNumber = "";
			        if (
			        		//If the item number is ALL numeric:
			        		(isStringNumeric(sItemNum))
			        		//AND if it's longer than 7
			        		&& (sItemNum.length() > 7)
			        ){
			        	//Assume it has an order number in it:
			        	sOrderNumber = sItemNum.substring(3, sItemNum.length());
			        }
			        SQL += ", '" + sOrderNumber + "'";
			        
			        //Commodity (carries the company cross link number for each item)
			        SQL += ", '" + rs.getString("ICITEM.COMMODIM").trim() + "'";
			        
			        //Labor item?:
			        if (sItemNum.length() > 2){
				        if (sItemNum.substring(0, 3).compareToIgnoreCase("LAB") == 0){
				        	SQL += ", 1";
				        }else{
				        	SQL += ", 0";
				        }
			        }else{
			        	SQL += ", 0";
			        }
					SQL += ")"
			    ;
				if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
					log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error updating icitems", SQL, "[1376509583]");
					m_sErrorMessage = "Error updating icitems with SQL: " + SQL;
					rs.close();
					return false;
				}
				lCounter++;
			}
			rs.close();
		} catch (SQLException e) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error updating icitems", e.getMessage(), "[1376509584]");
			m_sErrorMessage = "Error updating icitems with SQL: " + e.getMessage();
			return false;			
		}
		
    	log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Successfully updated " + lCounter + " icitems", "", "[1376509585]");
    	//arrStatus.add("Updated " + lCounter + " icitems - " 
    	//	+ SMUtilities.getElapsedTime(lStartingTime) + " elapsed.");
    
        //Update the most recent cost:
        SQL = "SELECT"
        	+ " * FROM ICILOC"
        	+ " WHERE ("
        		+ "RECENTCOST <> 0.00"
        	+ ")"
        	+ " ORDER BY ITEMNO, RECENTCOST DESC"
        	;
        String sLastItemNo = "";
        lCounter = 0;
    	try {
			rs = clsDatabaseFunctions.openResultSet(SQL, conACCPAC);
			while (rs.next()){
                //Skip it if we already got a cost for this item:
                if (rs.getString("ICILOC.ITEMNO") == sLastItemNo){
                }else{
                    SQL = "UPDATE icitems SET bdmostrecentcost = "
                    + clsManageBigDecimals.BigDecimalToScaledFormattedString(
                    	4, rs.getBigDecimal("ICILOC.RECENTCOST")).replace(",", "")
                    + " WHERE sitemnumber = '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("ICILOC.ITEMNO")) + "'"
                    ;
                }
                sLastItemNo = rs.getString("ICILOC.ITEMNO");
				if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
					log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error updating most recent cost", SQL, "[1376509586]");
					m_sErrorMessage = "Error updating most recent cost with SQL: " + SQL;
					rs.close();
					return false;
				}
				lCounter++;
			}
			rs.close();
		} catch (SQLException e) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error updating most recent cost", e.getMessage(), "[1376509587]");
			m_sErrorMessage = "Error updating most recent cost with SQL: " + e.getMessage();
			return false;			
		}
		
    	log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Successfully updated most recent cost on " + lCounter + " items", "", "[1376509588]");
    	//arrStatus.add("Updated most recent costs on " + lCounter + " items - " 
    	//	+ SMUtilities.getElapsedTime(lStartingTime) + " elapsed.");

    	//vendor items:
	    SQL =
	    	"SELECT"
            + " ICITMV.ITEMNO"
            + ", ICITMV.VENDNUM"
            + ", ICITMV.VENDITEM"
            + ", ICITMV.VENDCOST"
            + " FROM ICITMV"
	    	;
	    lCounter = 0;
    	try {
			rs = clsDatabaseFunctions.openResultSet(SQL, conACCPAC);
			while (rs.next()){
				SQL = 
					"INSERT INTO icvendoritems_temp ("
		            + "sitemnumber"
		            + ", svendor"
		            + ", svendoritemnumber"
		            + ", bdcost"
			        + ") VALUES ("
			        
			        + "'" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("ICITMV.ITEMNO").trim()).replace("-", "").replace(" ", BLANK_REPLACEMENT_CHARACTER) + "'"
			        + ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("ICITMV.VENDNUM").trim()) + "'"
			        + ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("ICITMV.VENDITEM").trim()) + "'"
			        + ", " + clsManageBigDecimals.BigDecimalToFormattedString(
				        	"#########0.0000", rs.getBigDecimal("ICITMV.VENDCOST"))
					+ ")"
			    ;
				try{
				    Statement stmt = conn.createStatement();
				    stmt.executeUpdate(SQL);
				}catch (Exception ex) {
					rs.close();
					log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error updating icvendoritems = " + ex.getMessage(), SQL, "[1376509589]");
					m_sErrorMessage = "Error updating icvendoritems with SQL: " + SQL;
				}
				lCounter++;
			}
			rs.close();
		} catch (SQLException e) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error updating icvendoritems", e.getMessage(), "[1376509590]");
			m_sErrorMessage = "Error updating icvendoritems with SQL: " + e.getMessage();
			return false;			
		}
		
		//Now remove any duplicates:
		SQL = 
			"CREATE TABLE icvendoritems AS"
			+ " SELECT * FROM icvendoritems_temp WHERE 1 GROUP BY sitemnumber,svendor"
		;
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error selecting unique icvendoritems = " + ex.getMessage(), SQL, "[1376509591]");
			m_sErrorMessage = "Error selecting unique icvendoritems with SQL: " + SQL;
		}
		
		//Add the unique index:
		SQL = 
			"ALTER TABLE icvendoritems ADD UNIQUE KEY vendoritemkey (`svendor`, sitemnumber)"
		;
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error adding unique key to icvendoritems = " + ex.getMessage(), SQL, "[1376509592]");
			m_sErrorMessage = "Error adding unique key to icvendoritems with SQL: " + SQL;
		}
		
		//Finally drop the original table:
		SQL = 
			"DROP TABLE icvendoritems_temp"
		;
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error dropping icvendoritems_temp table = " + ex.getMessage(), SQL, "[1376509593]");
			m_sErrorMessage = "Error dropping icvendoritems_temp table with SQL: " + SQL;
		}
		
    	log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Successfully updated " + lCounter + " icvendoritems", "", "[1376509594]");
    	//arrStatus.add("Updated " + lCounter + " icvendoritems - " 
    	//	+ SMUtilities.getElapsedTime(lStartingTime) + " elapsed.");

        //item locations:
	    SQL =
	    	"SELECT"
            + " ICILOC.ITEMNO"
            + ", ICILOC.LOCATION"
            + ", ICILOC.QTYONHAND"
            + ", ICILOC.TOTALCOST"
            + " FROM ICILOC"
	    	;
		lCounter = 0;
    	try {
			rs = clsDatabaseFunctions.openResultSet(SQL, conACCPAC);
			while (rs.next()){
			        SQL = "INSERT INTO icitemlocations ("
			        + " sitemnumber"
			        + ", slocation"
			        + ", bdqtyonhand"
			        + ", bdtotalcost"
			        + ") VALUES ("
			        
			        + "'" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("ICILOC.ITEMNO").trim().replace("-", "").replace(" ", BLANK_REPLACEMENT_CHARACTER)) + "'"
			        + ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("ICILOC.LOCATION").trim()) + "'"
			        + ", " + clsManageBigDecimals.BigDecimalToFormattedString(
			        	"#########0.0000", rs.getBigDecimal("ICILOC.QTYONHAND"))
			        + ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(rs.getBigDecimal("ICILOC.TOTALCOST"))
					+ ")"
			    ;
				if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
					log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error updating ic item locations", SQL, "[1376509595]");
					m_sErrorMessage = "Error updating ic item locations with SQL: " + SQL;
					rs.close();
					return false;
				}
				lCounter++;
			}
			rs.close();
		} catch (SQLException e) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error updating ic item locations", e.getMessage(), "[1376509596]");
			m_sErrorMessage = "Error updating ic item locations with SQL: " + e.getMessage();
			return false;			
		}
		
    	log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Successfully updated " + lCounter + " ic item locations", "", "[1376509597]");
    	//arrStatus.add("Updated " + lCounter + " ic item locations - " 
    	//	+ SMUtilities.getElapsedTime(lStartingTime) + " elapsed.");

    	//Having a lot of trouble with Pervasive error "LNA Session Closed", so we are going to try to 
    	//bring the table into MySQL, then do the updates from there:
	    if (!dropTable(conn, log, "ICCOST", sUserID)){
	    	return false;
	    }
    	
    	SQL = "CREATE TEMPORARY TABLE ICCOST ("
            + " ITEMNO varchar(24) NOT NULL DEFAULT ''"
            + ", LOCATION varchar(6) NOT NULL DEFAULT ''"
            + ", QTY decimal(17,4) NOT NULL DEFAULT '0.0000'"
            + ", COST decimal(17,4) NOT NULL DEFAULT '0.0000'"
            + ", SHIPQTY decimal(17,4) NOT NULL DEFAULT '0.0000'"
            + ", SHIPCOST decimal(17,4) NOT NULL DEFAULT '0.0000'"
            + ", REFERENCE varchar(60) NOT NULL DEFAULT ''"
            + ", TRANSDATE datetime NOT NULL DEFAULT '0000-00-00'"
            + ", RECEIPTNUM varchar(22) NOT NULL DEFAULT ''"
            + ", KEY itemnokey (`ITEMNO`)"
    		+ ") Engine=InnoDB"
    		;
		try {
			if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
				log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error creating temp ICCOST table", SQL, "[1376509598]");
				m_sErrorMessage = "Error creating temp ICCOST table " + SQL;
				return false;
			}
		} catch (SQLException e1) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error creating temp ICCOST table - " + e1.getMessage(), SQL, "[1376509599]");
			m_sErrorMessage = "Error creating temp ICCOST table - " + e1.getMessage() + SQL;
			return false;
		}    	
    	
    	//iccosts:
	    SQL =
	    	"SELECT"
            + " ICCOST.ITEMNO"
            + ", ICCOST.LOCATION"
            + ", ICCOST.QTY"
            + ", ICCOST.COST"
            + ", ICCOST.SHIPQTY"
            + ", ICCOST.SHIPCOST"
            + ", ICCOST.REFERENCE"
            + ", ICCOST.TRANSDATE"
            + ", ICCOST.RECEIPTNUM"
            + " FROM ICCOST LEFT JOIN ICITEM ON ICCOST.ITEMNO = ICITEM.ITEMNO"
	    	;
    	lCounter = 0;
	    try {
			rs = clsDatabaseFunctions.openResultSet(SQL, conACCPAC);
			while (rs.next()){
				SQL = "INSERT INTO ICCOST ("
		            + " ITEMNO"
		            + ", LOCATION"
		            + ", QTY"
		            + ", COST"
		            + ", SHIPQTY"
		            + ", SHIPCOST"
		            + ", REFERENCE"
		            + ", TRANSDATE"
		            + ", RECEIPTNUM"
		            + ") VALUES ("
			        + "'" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("ICCOST.ITEMNO").trim().replace("-", "").replace(" ", BLANK_REPLACEMENT_CHARACTER)) + "'"
			        + ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("ICCOST.LOCATION").trim()) + "'"
			        + ", " + clsManageBigDecimals.BigDecimalToFormattedString("#########0.0000", rs.getBigDecimal("ICCOST.QTY"))
			        + ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(rs.getBigDecimal("ICCOST.COST"))
			        + ", " + clsManageBigDecimals.BigDecimalToFormattedString("#########0.0000", rs.getBigDecimal("ICCOST.SHIPQTY"))
			        + ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(rs.getBigDecimal("ICCOST.SHIPCOST"))
			        + ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("ICCOST.REFERENCE").trim()) + "'"
			        + ", '" + convertACCPACLongDateToString(rs.getLong("ICCOST.TRANSDATE"), false) + "'"
			        + ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("ICCOST.RECEIPTNUM").trim()) + "'"
					+ ")"
		            ;
				if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
					log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error inserting ICCOST records", SQL, "[1376509600]");
					m_sErrorMessage = "Error inserting ICCOST records " + SQL;
					rs.close();
					return false;
				}
				lCounter++;
			}
			rs.close();
		} catch (SQLException e) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error inserting ic cost buckets", e.getMessage(), "[1376509600]");
			m_sErrorMessage = "Error inserting ic cost buckets with SQL: " + e.getMessage();
			return false;			
		}
		
		//Now update from MySQL to MySQL:
		SQL = "INSERT INTO iccosts ("
		    + " sitemnumber"
		    + ", slocation"
		    + ", bdqty"
		    + ", bdcost"
		    + ", bdqtyshipped"
		    + ", bdcostshipped"
		    + ", isource"
		    + ", sponumber"
		    + ", sremark"
		    + ", datcreationdate"
		    + ", sreceiptnumber"
		    
		    + ") SELECT "
		    
		    + " TRIM(ICCOST.ITEMNO)"
		    + ", TRIM(ICCOST.LOCATION)"
		    + ", (ICCOST.QTY - ICCOST.SHIPQTY)" //We store the QTY REMAINING, ACCPAC stores the original qty
		    + ", (ICCOST.COST - ICCOST.SHIPCOST)" //We store the COST REMAINING, ACCPAC stores the original cost
		    + ", ICCOST.SHIPQTY"
		    + ", ICCOST.SHIPCOST"
		    + ", 0" //Receipt is source type '0'
		    + ", ''" //PO Number
		    + ", ICCOST.REFERENCE"
		    + ", ICCOST.TRANSDATE"
		    + ", ICCOST.RECEIPTNUM"
		    + " FROM ICCOST LEFT JOIN icitems ON ICCOST.ITEMNO = icitems.sitemnumber"
		    ;

		try {
			if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
				log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error inserting iccost records", SQL, "[1376509601]");
				m_sErrorMessage = "Error inserting iccost records " + SQL;
				return false;
			}
		} catch (SQLException e1) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error inserting iccost records - " + e1.getMessage(), SQL, "[1376509602]");
			m_sErrorMessage = "Error inserting iccost records - " + e1.getMessage() + SQL;
			return false;
		}

		try {
			if (!clsDatabaseFunctions.executeSQL("DROP TABLE IF EXISTS ICCOST", conn)){
				log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error dropping ICCOST table", SQL, "[1376509603]");
				m_sErrorMessage = "Error dropping ICCOST table " + SQL;
			}
		} catch (SQLException e1) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error dropping ICCOST table - " + e1.getMessage(), SQL, "[1376509604]");
			m_sErrorMessage = "Error dropping ICCOST table - " + e1.getMessage() + SQL;
		}
		
		log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Successfully updated " + lCounter + " ic cost buckets", "", "[1376509605]");
    	//arrStatus.add("Updated " + lCounter + " ic cost buckets - " 
    	//	+ SMUtilities.getElapsedTime(lStartingTime) + " elapsed.");
	    if (!dropTable(conn, log, "ICILOC", sUserID)){
	    	return false;
	    }
	    //Create a temp table in MySQL:
	    SQL = "CREATE TEMPORARY TABLE ICILOC ("
        	+ " ITEMNO varchar(22)"
        	+ ", LOCATION varchar(6)"
        	+ ", QTYOFFSET decimal(17,4)"
        	+ ", COSTOFFSET decimal (17,4)"
        	+ ", PRIMARY KEY (ITEMNO, LOCATION)"
        	+ ", KEY itemnokey (`ITEMNO`)"
        	+ ") Engine=InnoDB"
        	;
		try {
			if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
				log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error creating temp ICILOC table", SQL, "[1376509606]");
				m_sErrorMessage = "Error creating temp ICILOC table " + SQL;
				return false;
			}
		} catch (SQLException e1) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error creating temp ICILOC table - " + e1.getMessage(), SQL, "[1376509607]");
			m_sErrorMessage = "Error creating temp ICILOC table - " + e1.getMessage() + SQL;
			return false;
		}    	
	    
        //Now get the offset qty and cost into an offset bucket in the cost file:
    	SQL = "SELECT"
        	+ " ICILOC.ITEMNO"
        	+ ", ICILOC.LOCATION"
        	+ ", ICILOC.QTYOFFSET"
        	+ ", ICILOC.COSTOFFSET"
        	+ " FROM ICILOC" // LEFT JOIN ICITEM ON ICILOC.ITEMNO = ICITEM.ITEMNO"
        	+ " WHERE ("
            	+ "(ICILOC.QTYOFFSET <> 0)"
            	+ " OR (ICILOC.COSTOFFSET <> 0.00)"
            + ")"	    
            ;
        lCounter = 0;
        try {
			rs = clsDatabaseFunctions.openResultSet(SQL, conACCPAC);
			while (rs.next()){
		        SQL = "INSERT INTO ICILOC ("
		        	+ " ITEMNO"
		        	+ ", LOCATION"
		        	+ ", QTYOFFSET"
		        	+ ", COSTOFFSET"
		        	+ ") VALUES ("
		        	+ "'" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("ICILOC.ITEMNO").trim().replace("-", "").replace(" ", BLANK_REPLACEMENT_CHARACTER)) + "'"
		        	+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("ICILOC.LOCATION").trim()) + "'"
		        	+ ", " + clsManageBigDecimals.BigDecimalToFormattedString(
		        		"#########0.0000", rs.getBigDecimal("ICILOC.QTYOFFSET"))
		        	+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(rs.getBigDecimal("ICILOC.COSTOFFSET"))
		        	+ ")"
				;
				if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
					log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error inserting ICILOC records", SQL, "[1376509608]");
					m_sErrorMessage = "Error inserting ICILOC records: " + SQL;
					rs.close();
					return false;
				}
				lCounter++;
			}
			rs.close();
		} catch (SQLException e) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error updating ic offset cost buckets", e.getMessage(), "[1376509609]");
			m_sErrorMessage = "Error updating ic offset cost buckets with SQL: " + e.getMessage();
			return false;			
		}
		
		//Now do a single MySQL to MySQL update:
		SQL = "INSERT INTO iccosts ("
		    + " sitemnumber"
		    + ", slocation"
		    + ", bdqty"
		    + ", bdcost"
		    + ", bdqtyshipped"
		    + ", bdcostshipped"
		    + ", isource"
		    + ", sponumber"
		    + ", sremark"
		    + ", datcreationdate"
		    + ") SELECT "
		    + " TRIM(ICILOC.ITEMNO)"
		    + ", TRIM(ICILOC.LOCATION)"
		    + ", ICILOC.QTYOFFSET"
		    + ", ICILOC.COSTOFFSET"
		    + ", 0.0000"
		    + ", 0.0000"
		    + ", 1" //Offset bucket is source type '1'
		    + ", ''" //PO Number
		    + ", 'Conversion from offset bucket'"
		    + ", Now() AS CREATIONDATE"
		    + " FROM ICILOC LEFT JOIN icitems ON ICILOC.ITEMNO = icitems.sitemnumber"
		    +" WHERE ("
		        + "(ICILOC.QTYOFFSET <> 0)"
		        + " OR (ICILOC.COSTOFFSET <> 0.00)"
		    + ")"
			;
		
		try {
			if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
				log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error inserting iccost offset records", SQL, "[1376509610]");
				m_sErrorMessage = "Error inserting iccost offset records " + SQL;
				return false;
			}
		} catch (SQLException e1) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error inserting iccost offset records - " + e1.getMessage(), SQL, "[1376509611]");
			m_sErrorMessage = "Error inserting iccost offset records - " + e1.getMessage() + SQL;
			return false;
		}

		try {
			if (!clsDatabaseFunctions.executeSQL("DROP TABLE IF EXISTS ICILOC", conn)){
				log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error dropping ICILOC table", SQL, "[1376509612]");
				m_sErrorMessage = "Error dropping ICCOST table " + SQL;
			}
		} catch (SQLException e1) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error dropping ICILOC table - " + e1.getMessage(), SQL, "[1376509613]");
			m_sErrorMessage = "Error dropping ICILOC table - " + e1.getMessage() + SQL;
		}
		
    	log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Successfully updated " + lCounter + " ic offset cost buckets", "", "[1376509614]");
    	//arrStatus.add("Updated " + lCounter + " ic offset cost buckets - " 
    	//	+ SMUtilities.getElapsedTime(lStartingTime) + " elapsed.");

    	
    	//icitemprices
        //Notes:
        //Base Price Type in ACCPAC is ICPRIC.BPRICETYPE
        // 1 is //Base price for single unit of measure
        // 2 is //Base price for multiple units of measure
        // 3 is //Base price calculated using a cost
        //(All in Tampa AND Charlotte are 1)
        
        //Selling Price Base On in ACCPAC is ICPRIC.PRICETYPE
        // 1 is Discount
        // 2 is Markup on Markup cost
        // 3 is Markup on standard cost
        // 4 is Markup on most recent cost
        // 5 is Markup on average cost
        // 6 is Markup on last unit cost
        // 7 is Markup on cost 1
        // 8 is Markup on cost 2
        //(Tampa - all are 1 or 2, Charlotte - all are 1 or 2)
        
        // 'Markup on cost by' in ACCPAC is ICPRIC.PRICEFMT
        // 1 is Percentage
        // 2 is Amount
        //(Tampa and Charlotte have some of each)
        
        // Price determined by in ACCPAC is ICPRIC.PRICEBASE
        // 1 is customer type
        // 2 is volume discounts
        //(Tampa and Charlotte have some of each)
        
        //Tampa has a few values in the 4 and or 5th columns of amount, and nothing in the other columns
        //(for weight and percentage)
        //Charlotte has no values in the 5 amount columns, the 5 percentage, OR the 5 weight columns
        
        //TJR - checked Tampa and Charlotte's inventory data on Sunday, 8/9/2009
    	
	    //Create an ICPRIC table:
	    SQL = "CREATE TEMPORARY TABLE ICPRIC ("
	    	+ "ITEMNO varchar(22) NOT NULL DEFAULT ''"
	    	+ ", PRICELIST varchar(6) NOT NULL DEFAULT ''"
	    	+ ", PRICEFMT int(11) NOT NULL DEFAULT '0'"
	    	+ ", PRICETYPE int(11) NOT NULL DEFAULT '0'"
	    	+ ", MARKUPCOST decimal(17,4) NOT NULL DEFAULT '0.0000'"
	    	+ ", AMOUNTLVL1 decimal(17,4) NOT NULL DEFAULT '0.0000'"
	    	+ ", AMOUNTLVL2 decimal(17,4) NOT NULL DEFAULT '0.0000'"
	    	+ ", AMOUNTLVL3 decimal(17,4) NOT NULL DEFAULT '0.0000'"
	    	+ ", AMOUNTLVL4 decimal(17,4) NOT NULL DEFAULT '0.0000'"
	    	+ ", AMOUNTLVL5 decimal(17,4) NOT NULL DEFAULT '0.0000'"
	    	+ ", PRIMARY KEY (ITEMNO, PRICELIST)"
	    	+ ", KEY itemnokey (`ITEMNO`)"
	    	+ ", KEY pricelistkey (`PRICELIST`)"
	    + ") Engine=Innodb"
	    ;
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error creating temp ICPRIC table - " + ex.getMessage(), SQL, "[1376509615]");
			m_sErrorMessage = "Error creating temp ICPRIC table " + SQL + " - " + ex.getMessage();
			return false;
		}
	    if (!dropTable(conn, log, "ICPRICP", sUserID)){
	    	return false;
	    }
	    //Create an ICPRICP table:
		SQL = "CREATE TEMPORARY TABLE ICPRICPWITHDUPES ("
	    	+ "ITEMNO varchar(22) NOT NULL DEFAULT ''"
	    	+ ", PRICELIST varchar(6) NOT NULL DEFAULT ''"
	    	+ ", UNITPRICE decimal(17,4) NOT NULL DEFAULT '0.0000'"
	    	+ ", DPRICETYPE int(11) NOT NULL DEFAULT '0'"
	    	+ ", QTYUNIT varchar(10) NOT NULL DEFAULT ''"
	    	+ ", KEY itemnokey (`ITEMNO`)"
	    	+ ", KEY pricelistkey (`PRICELIST`)"
		+ ") Engine=InnoDB"
		;
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error creating temp ICPRICPWITHDUPES table - " 
					+ ex.getMessage(), SQL, "[1376509616]");
			m_sErrorMessage = "Error creating temp ICPRICPWITHDUPES table " + SQL + " - " + ex.getMessage();
			return false;
		}
		
		//Copy records from ACCPAC to MySQL:
		SQL = "SELECT * FROM ICPRIC";
        try {
			rs = clsDatabaseFunctions.openResultSet(SQL, conACCPAC);
			while (rs.next()){
		        SQL = "INSERT INTO ICPRIC ("
			    	+ "ITEMNO"
			    	+ ", PRICELIST"
			    	+ ", PRICEFMT"
			    	+ ", PRICETYPE"
			    	+ ", MARKUPCOST"
			    	+ ", AMOUNTLVL1"
			    	+ ", AMOUNTLVL2"
			    	+ ", AMOUNTLVL3"
			    	+ ", AMOUNTLVL4"
			    	+ ", AMOUNTLVL5"
			    	+ ") VALUES ("
		        	
		            + "'" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("ICPRIC.ITEMNO").trim().replace("-", "").replace(" ", BLANK_REPLACEMENT_CHARACTER)) + "'"
		            + ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("ICPRIC.PRICELIST").trim()) + "'"
			    	+ ", " + rs.getLong("PRICEFMT")
			    	+ ", " + rs.getLong("PRICETYPE")
			    	+ ", " + clsManageBigDecimals.BigDecimalToFormattedString("########0.00", rs.getBigDecimal("MARKUPCOST"))
			    	+ ", " + clsManageBigDecimals.BigDecimalToFormattedString("########0.00", rs.getBigDecimal("AMOUNTLVL1"))
			    	+ ", " + clsManageBigDecimals.BigDecimalToFormattedString("########0.00", rs.getBigDecimal("AMOUNTLVL2"))
			    	+ ", " + clsManageBigDecimals.BigDecimalToFormattedString("########0.00", rs.getBigDecimal("AMOUNTLVL3"))
			    	+ ", " + clsManageBigDecimals.BigDecimalToFormattedString("########0.00", rs.getBigDecimal("AMOUNTLVL4"))
			    	+ ", " + clsManageBigDecimals.BigDecimalToFormattedString("########0.00", rs.getBigDecimal("AMOUNTLVL5"))

				+ ")"
				;
				try{
				    Statement stmt = conn.createStatement();
				    stmt.executeUpdate(SQL);
				}catch (Exception ex) {
					log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error copying ICPRIC records - " + ex.getMessage(), SQL, "[1376509617]");
					m_sErrorMessage = "Error copying ICPRIC records with SQL: " + SQL + " - " + ex.getMessage();
					rs.close();
					return false;
				}
				lCounter++;
			}
			rs.close();
		} catch (SQLException e) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error copying ICPRIC records", e.getMessage(), "[1376509618]");
			m_sErrorMessage = "Error copying ICPRIC records with SQL: " + e.getMessage();
			return false;			
		}
    	
		SQL = "SELECT * FROM ICPRICP";
        try {
			rs = clsDatabaseFunctions.openResultSet(SQL, conACCPAC);
			while (rs.next()){
		        SQL = "INSERT INTO ICPRICPWITHDUPES ("
			    	+ "ITEMNO"
			    	+ ", PRICELIST"
			    	+ ", UNITPRICE"
			    	+ ", DPRICETYPE"
			    	+ ", QTYUNIT"
		        	+ ") VALUES ("
		            + "'" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("ITEMNO").trim().replace("-", "").replace(" ", BLANK_REPLACEMENT_CHARACTER)) + "'"
		            + ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("PRICELIST").trim()) + "'"
			    	+ ", " + clsManageBigDecimals.BigDecimalToFormattedString("########0.00", rs.getBigDecimal("UNITPRICE"))
			    	+ ", " + rs.getLong("DPRICETYPE")
			    	+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("QTYUNIT").trim().replace(".", "")) + "'"

				+ ")"
				;
				try{
				    Statement stmt = conn.createStatement();
				    stmt.executeUpdate(SQL);
				}catch (Exception ex) {
					log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error copying ICPRICP records - " + ex.getMessage(), SQL, "[1376509619]");
					m_sErrorMessage = "Error copying ICPRICP records with SQL: " + SQL + " - " + ex.getMessage();
					rs.close();
					return false;
				}
				lCounter++;
			}
			rs.close();
		} catch (SQLException e) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error copying ICPRICP records", e.getMessage(), "[1376509620]");
			m_sErrorMessage = "Error copying ICPRICP records with SQL: " + e.getMessage();
			return false;			
		}
		
		//Now create an ICPRICP table with NO duplicates:
		SQL = "CREATE TEMPORARY TABLE ICPRICP AS "
			+ "SELECT * FROM ICPRICPWITHDUPES WHERE 1 GROUP BY "
			+ "CONCAT(ITEMNO, ' - ', PRICELIST, ' - ', CAST(DPRICETYPE AS CHAR), ' - ', QTYUNIT)"
		;
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error creating temp ICPRICP table - " 
					+ ex.getMessage(), SQL, "[1376509621]");
			m_sErrorMessage = "Error creating temp ICPRICP table " + SQL + " - " + ex.getMessage();
			return false;
		}
		SQL = "ALTER TABLE ICPRICP ADD KEY itemnokey (`ITEMNO`)";
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error executing SQL - " 
					+ ex.getMessage(), SQL, "[1376509622]");
			m_sErrorMessage = "Error executing " + SQL + " - " + ex.getMessage();
			return false;
		}
		SQL = "ALTER TABLE ICPRICP ADD KEY pricelistkey (`PRICELIST`)";
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error executing SQL - " 
					+ ex.getMessage(), SQL, "[1376509623]");
			m_sErrorMessage = "Error executing " + SQL + " - " + ex.getMessage();
			return false;
		}
		SQL = "ALTER TABLE ICPRICP ADD KEY qtyunitkey (`QTYUNIT`)";
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error executing SQL - " 
					+ ex.getMessage(), SQL, "[1376509624]");
			m_sErrorMessage = "Error executing " + SQL + " - " + ex.getMessage();
			return false;
		}
		
		//Now insert with a single MySQL statement:
        	//If the PRICEFMT is 1 (Percentage type), just set the price levels to zero
        	//but if the PRICEFMT is 2, then if the price type is 2 (MArkup on markup cost), do the math
        	//Checking all the companies' data, there are no other price types used except for
        	//1 and 2:
	    SQL = "INSERT INTO icitemprices ("
	        + " sitemnumber"
	        + ", spricelistcode"
	        + ", datlastmaintained"
	        + ", slastedituser"
	        + ", bdbaseprice"
	        + ", bdlevel1price"
	        + ", bdlevel2price"
	        + ", bdlevel3price"
	        + ", bdlevel4price"
	        + ", bdlevel5price"
	        
	        + ") SELECT "
	        + " TRIM(ICPRIC.ITEMNO)"
	        + ", TRIM(ICPRIC.PRICELIST)"
	        + ", Now() AS LASTMAINTAINEDDATE"
	        + ", 'CONVERSION' AS LASTEDITUSER"
	        + ", ICPRICP.UNITPRICE"
	        
	        //If the PRICEFMT is 1 (Percentage type), just set the price levels to zero
	        //but if the PRICEFMT is 2, then if the price type is 2 (MArkup on markup cost), do the math
	        //Checking all the companies' data, there are no other price types used except for
	        //1 and 2:
	        + ", IF(ICPRIC.PRICEFMT = 1,0.00, IF (ICPRIC.PRICETYPE = 2, ICPRIC.MARKUPCOST + ICPRIC.AMOUNTLVL1, 0.00))"
	        + ", IF(ICPRIC.PRICEFMT = 1,0.00, IF (ICPRIC.PRICETYPE = 2, ICPRIC.MARKUPCOST + ICPRIC.AMOUNTLVL2, 0.00))"
	        + ", IF(ICPRIC.PRICEFMT = 1,0.00, IF (ICPRIC.PRICETYPE = 2, ICPRIC.MARKUPCOST + ICPRIC.AMOUNTLVL3, 0.00))"
	        + ", IF(ICPRIC.PRICEFMT = 1,0.00, IF (ICPRIC.PRICETYPE = 2, ICPRIC.MARKUPCOST + ICPRIC.AMOUNTLVL4, 0.00))"
	        + ", IF(ICPRIC.PRICEFMT = 1,0.00, IF (ICPRIC.PRICETYPE = 2, ICPRIC.MARKUPCOST + ICPRIC.AMOUNTLVL5, 0.00))"
	        
	        + " FROM (ICPRIC LEFT JOIN ICPRICP ON "
	        + "(ICPRIC.PRICELIST = ICPRICP.PRICELIST) "
	        + "AND (ICPRIC.ITEMNO = ICPRICP.ITEMNO)) "
	        + "LEFT JOIN icitems ON ICPRIC.ITEMNO = icitems.sitemnumber"
	        
	        + " WHERE ("
	        + "(ICPRICP.DPRICETYPE = 1)"
	        + " AND (BINARY ICPRICP.QTYUNIT = icitems.scostunitofmeasure)"
	        + ")"
	        ;
	    
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "error updating ic itemprices - " + ex.getMessage(), SQL, "[1376509625]");
			m_sErrorMessage = "Error updating ic itemprices " + SQL + " - " + ex.getMessage();
			return false;
		}

		//Drop tables:
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate("DROP TABLE IF EXISTS ICPRIC");
		}catch (Exception ex) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error dropping ICPRIC table - " + ex.getMessage(), SQL, "[1376509626]");
			m_sErrorMessage = "Error dropping ICPRIC table " + SQL + " - " + ex.getMessage();
			return false;
		}

		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate("DROP TABLE IF EXISTS ICPRICPWITHDUPES");
		}catch (Exception ex) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error dropping ICPRICPWITHDUPES table - " + ex.getMessage(), SQL, "[1376509627]");
			m_sErrorMessage = "Error dropping ICPRICPWITHDUPES table " + SQL + " - " + ex.getMessage();
			return false;
		}
		
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate("DROP TABLE IF EXISTS ICPRICP");
		}catch (Exception ex) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error dropping ICPRICP table - " + ex.getMessage(), SQL,"[1376509628]");
			m_sErrorMessage = "Error dropping ICPRICP table " + SQL + " - " + ex.getMessage();
			return false;
		}
	
    	log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Successfully updated " + lCounter + " ic item prices", "", "[1376509629]");
    	//arrStatus.add("Updated " + lCounter + " ic item prices - " 
    	//	+ SMUtilities.getElapsedTime(lStartingTime) + " elapsed.");

    	//ic item statistics:
	    SQL =
	    	"SELECT"
            + " ICSTATI.ITEMNO"
            + ", ICSTATI.LOCATION"
            + ", ICSTATI.YEAR"
            + ", ICSTATI.PERIOD"
            + ", ICSTATI.SALESQTY"
            + ", ICSTATI.SALESCOUNT"
            + ", ICSTATI.SALESAMT"
            + ", ICSTATI.COGS"
            + ", ICSTATI.SALERTNQTY"
            + ", ICSTATI.SALERTNCNT"
            + ", ICSTATI.SALERTNAMT"
            + ", ICSTATI.COGSRTN"
            + " FROM ICSTATI"
	    	;
	    lCounter = 0;
	    try {
			rs = clsDatabaseFunctions.openResultSet(SQL, conACCPAC);
			while (rs.next()){
		        SQL = "INSERT INTO icitemstatistics ("
	            + " sitemnumber"
	            + ", slocation"
	            + ", lyear"
	            + ", lmonth"
	            + ", bdqtysold"
	            + ", lcountsold"
	            + ", bdamountsold"
	            + ", bdcostofitemssold"
	            + ", bdqtyreturned"
	            + ", lcountreturned"
	            + ", bdamountreturned"
	            + ", bdcostofitemsreturned"
		        + ") VALUES ("
		        + "'" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("ICSTATI.ITEMNO").trim().replace("-", "").replace(" ", BLANK_REPLACEMENT_CHARACTER)) + "'"
		        + ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("ICSTATI.LOCATION").trim()) + "'"
		        + ", " + rs.getLong("ICSTATI.YEAR")
		        + ", " + rs.getLong("ICSTATI.PERIOD")
		        + ", " + clsManageBigDecimals.BigDecimalToFormattedString(
		        	"#########0.0000", rs.getBigDecimal("ICSTATI.SALESQTY"))
		        + ", " + rs.getLong("ICSTATI.SALESCOUNT")
		        + ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(rs.getBigDecimal("ICSTATI.SALESAMT"))
		        + ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(rs.getBigDecimal("ICSTATI.COGS"))
		        + ", " + clsManageBigDecimals.BigDecimalToFormattedString(
		        	"#########0.0000", rs.getBigDecimal("ICSTATI.SALERTNQTY"))
		        + ", " + rs.getLong("ICSTATI.SALERTNCNT")
		        + ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(rs.getBigDecimal("ICSTATI.SALERTNAMT"))
		        + ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(rs.getBigDecimal("ICSTATI.COGSRTN"))
				+ ")"
			    ;
				if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
					log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error updating ic item statistics", SQL, "[1376509630]");
					m_sErrorMessage = "Error updating ic item statistics with SQL: " + SQL;
					rs.close();
					return false;
				}
				lCounter++;
			}
			rs.close();
		} catch (SQLException e) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error updating ic item statistics", e.getMessage(), "[1376509631]");
			m_sErrorMessage = "Error updating ic item statistics with SQL: " + e.getMessage();
			return false;			
		}
		
    	log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Successfully updated " + lCounter + " ic item statistics", "", "[1376509632]");
    	//arrStatus.add("Updated " + lCounter + " ic item statistics - " 
    	//	+ SMUtilities.getElapsedTime(lStartingTime) + " elapsed.");

    	//icvendorterms:
	    SQL =
	    	"SELECT"
	        + " APRTB.TERMSCODE"
	        + ", APRTA.CODEDESC"
	        + ", APRTA.SWACTV"
	        + ", APRTA.DATELASTMN"
	        + ", APRTB.PCTDISC"
	        + ", APRTB.DISNBRDAYS"
	        + ", APRTB.DISCDAY"
	        + ", APRTB.DUENBRDAYS"
	        + ", APRTB.DUEDAY"
	        + " FROM APRTA, APRTB"
	        + " WHERE ("
	        	+ "(APRTA.TERMSCODE = APRTB.TERMSCODE)"
	        + ")"
	        ;
	    lCounter = 0;
	    try {
			rs = clsDatabaseFunctions.openResultSet(SQL, conACCPAC);
			while (rs.next()){
		        SQL = "INSERT INTO icvendorterms ("
		        	+ " stermscode"
		        	+ ", sdescription"
		        	+ ", iactive"
		        	+ ", datlastmaintained"
		        	+ ", bddiscountpercent" 
		        	+ ", idiscountnumberofdays" 
		        	+ ", idiscountdayofthemonth" 
		        	+ ", iduenumberofdays" 
		        	+ ", iduedayofthemonth"
		        + ") VALUES ("
		        	+ "'" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("APRTB.TERMSCODE").trim()) + "'"
		        + ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("APRTA.CODEDESC").trim()) + "'"
		        + ", " + rs.getLong("APRTA.SWACTV")
		        + ", '" + convertACCPACLongDateToString(rs.getLong("APRTA.DATELASTMN"), false) + "'"
		        + ", " + clsManageBigDecimals.BigDecimalToFormattedString(
			        	"#########0.0000", rs.getBigDecimal("APRTB.PCTDISC"))
		        + ", " + rs.getLong("APRTB.DISNBRDAYS")
		        + ", " + rs.getLong("APRTB.DISCDAY")
		        + ", " + rs.getLong("APRTB.DUENBRDAYS")
		        + ", " + rs.getLong("APRTB.DUEDAY")
				+ ")"
			    ;
				try{
				    Statement stmt = conn.createStatement();
				    stmt.executeUpdate(SQL);
				}catch (Exception ex) {
					log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error updating icvendorterms", SQL, "[1376509633]");
					m_sErrorMessage = "Error updating icvendorterms with SQL: " + SQL + " - " + ex.getMessage();
					rs.close();
					return false;
				}
				lCounter++;
			}
			rs.close();
		} catch (SQLException e) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error updating icvendorterms", e.getMessage(), "[1376509634]");
			m_sErrorMessage = "Error updating icvendorterms with SQL: " + e.getMessage();
			return false;			
		}
		
    	log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Successfully updated " + lCounter + " icvendorterms", "", "[1376509635]");

		return true;
	}
	public boolean convertPOLineData(
			Connection conn,
			Connection conACCPAC,
			String sUserID,
			PrintWriter out
			){

		lStartingTime = System.currentTimeMillis();
		long lCounter = 0;
		ResultSet rs;
		String SQL = "";
		String sTable = "";
		
		//First, start a log for the conversion:
	    SMLogEntry log = new SMLogEntry(conn);
	    log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Starting PO Data conversion", "", "[1376509379]");
	    
	    //PO Lines:
	    sTable = "icpolines";
	    if (!dropTable(conn, log, sTable, sUserID)){
	    	return false;
	    }
	    SQL =
			"CREATE TABLE `icpolines` ("
	    	+ " `lid` int(11) NOT NULL auto_increment"
	    	+ ", `lpoheaderid` int(11) NOT NULL default '0'"
	    	+ ", `llinenumber` int(11) NOT NULL default '0'"
	    	+ ", `sitemnumber` varchar(24) NOT NULL default ''" 
			+ ", `slocation` varchar(6) NOT NULL default ''"
			+ ", `sitemdescription` varchar(75) NOT NULL default ''"
			+ ", `sunitofmeasure` varchar(10) NOT NULL default ''"
			+ ", `bdunitcost` decimal(17,6) NOT NULL default '0.00'"
			+ ", `bdextendedordercost` decimal(17,2) NOT NULL default '0.00'"
			+ ", `bdextendedreceivedcost` decimal(17,2) NOT NULL default '0.00'"
			//+ ", `bdextendedcanceledcost` decimal(17,2) NOT NULL default '0.00'"
			+ ", `bdqtyordered` decimal(17,4) NOT NULL default '0.0000'"
			+ ", `bdqtyreceived` decimal(17,4) NOT NULL default '0.0000'"
			//+ ", `bdqtycanceled` decimal(17,4) NOT NULL default '0.0000'"
			//+ ", `bdqtyextrareceived` decimal(17,4) NOT NULL default '0.0000'"
			//+ ", `bdqtyoutstanding` decimal(17,4) NOT NULL default '0.0000'"
			+ ", `sglexpenseacct` varchar(75) NOT NULL default ''"
			+ ", `datexpected` datetime NOT NULL default '0000-00-00'"
			+ ", `svendorsitemnumber` varchar(24) NOT NULL default ''"
			+ ", `sinstructions` text NOT NULL"
			+ ", `lnoninventoryitem` int(11) NOT NULL default '0'"
			+ ", `laccpaclinesequence` int(11) NOT NULL default '0'"
			+ ", `laccpacporcseq` int(11) NOT NULL default '0'"
			+ ", `porlseq` int(11) NOT NULL default '0'"
			+ ", PRIMARY KEY  (`lid`)"
			+ ", UNIQUE KEY headerlinenokey (`lpoheaderid`, `llinenumber`)"
			+ ", KEY porlseqkey (`porlseq`)"
			+ ", KEY laccpacporcseqkey (`laccpacporcseq`)"
			+ " ) ENGINE=InnoDB"	
		;
	    if (!createTable(conn, log, sTable, sUserID, SQL)){
	    	return false;
	    }

    	//Update PO Lines:
	    SQL =
	    	"SELECT"
            + " POPORL.PORHSEQ"
            + ", POPORL.PORLREV"
            + ", POPORL.PORCSEQ"
            + ", POPORL.PORLSEQ"
			+ ", POPORL.ITEMNO"
			+ ", POPORL.LOCATION"
			+ ", POPORL.ITEMDESC"
			+ ", POPORL.ORDERUNIT"
			+ ", POPORL.UNITCOST"
			+ ", POPORL.EXTENDED"
			+ ", POPORL.EXTRECEIVE"
			+ ", POPORL.EXTCANCEL"
			+ ", POPORL.OQORDERED"
			+ ", POPORL.OQRECEIVED"
			+ ", POPORL.OQCANCELED"
			+ ", POPORL.OQRCPEXTRA"
			+ ", POPORL.OQOUTSTAND"
			+ ", POPORL.GLACEXPENS"
			+ ", POPORL.EXPARRIVAL"
			+ ", POPORL.VENDITEMNO"
			+ ", POPORL.ITEMEXISTS"
            + " FROM POPORL"
            + " ORDER BY POPORL.PORHSEQ, POPORL.PORLREV"
	    	;
	    lCounter = 0;
	    long lCurrentPOHeaderSeq = 0;
	    long lLastPOHeaderSeq = 0;
	    long lPOLineNumber = 0;
	    String sNonInventoryItem;
	    try {
			rs = clsDatabaseFunctions.openResultSet(SQL, conACCPAC);
			while (rs.next()){
				lCurrentPOHeaderSeq = rs.getLong("POPORL.PORHSEQ");
				if (lCurrentPOHeaderSeq != lLastPOHeaderSeq){
					//New PO - reset the line counter:
					lPOLineNumber = 0;
				}
				if (rs.getLong("POPORL.ITEMEXISTS") == 0){
					sNonInventoryItem = "1";
				}else{
					sNonInventoryItem = "0";
				}
				lPOLineNumber++;
				SQL = "INSERT INTO icpolines ("
					+ "lpoheaderid"
					+ ", llinenumber"
					+ ", sitemnumber"
					+ ", slocation"
					+ ", sitemdescription"
					+ ", sunitofmeasure"
					+ ", bdunitcost"
					+ ", bdextendedordercost"
					+ ", bdextendedreceivedcost"
					//+ ", bdextendedcanceledcost"
					+ ", bdqtyordered"
					+ ", bdqtyreceived"
					//+ ", bdqtycanceled"
					//+ ", bdqtyextrareceived"
					//+ ", bdqtyoutstanding"
					+ ", sglexpenseacct"
					+ ", datexpected"
					+ ", svendorsitemnumber"
					+ ", laccpaclinesequence"
					+ ", laccpacporcseq"
					+ ", sinstructions"
					+ ", lnoninventoryitem"
					+ ", porlseq"
					+ ") SELECT"
					+ " lid"
					+ ", " + Long.toString(lPOLineNumber)
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("POPORL.ITEMNO").trim().replace("-", "").replace(" ", BLANK_REPLACEMENT_CHARACTER)) + "'"
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("POPORL.LOCATION").trim()) + "'"
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("POPORL.ITEMDESC").trim()) + "'"
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("POPORL.ORDERUNIT").trim()) + "'"
					+ ", " + clsManageBigDecimals.BigDecimalToFormattedString(
						"#########0.000000", rs.getBigDecimal("POPORL.UNITCOST"))
					+ ", " + clsManageBigDecimals.BigDecimalToFormattedString(
						"#########0.00", rs.getBigDecimal("POPORL.EXTENDED"))
					+ ", " + clsManageBigDecimals.BigDecimalToFormattedString(
						"#########0.00", rs.getBigDecimal("POPORL.EXTRECEIVE"))
					//+ ", " + SMUtilities.BigDecimalToFormattedString(
					//	"#########0.00", rs.getBigDecimal("POPORL.EXTCANCEL"))
					+ ", " + clsManageBigDecimals.BigDecimalToFormattedString(
						"#########0.0000", rs.getBigDecimal("POPORL.OQORDERED"))
					+ ", " + clsManageBigDecimals.BigDecimalToFormattedString(
						"#########0.0000", rs.getBigDecimal("POPORL.OQRECEIVED"))
					//+ ", " + SMUtilities.BigDecimalToFormattedString(
					//	"#########0.0000", rs.getBigDecimal("POPORL.OQCANCELED"))
					//+ ", " + SMUtilities.BigDecimalToFormattedString(
					//	"#########0.0000", rs.getBigDecimal("POPORL.OQRCPEXTRA"))
					//+ ", " + SMUtilities.BigDecimalToFormattedString(
					//	"#########0.0000", rs.getBigDecimal("POPORL.OQOUTSTAND"))
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("POPORL.GLACEXPENS").trim().replace("-", ""))+ "'"
					+ ", '" + convertACCPACLongDateToString(rs.getLong("POPORL.EXPARRIVAL"), false) + "'" 
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("POPORL.VENDITEMNO").trim()) + "'"
					+ ", " + Long.toString(rs.getLong("POPORL.PORLREV"))
					+ ", " + Long.toString(rs.getLong("POPORL.PORCSEQ"))
					+ ", ''"
					+ ", " + sNonInventoryItem
					+ ", " + Long.toString(rs.getLong("POPORL.PORLSEQ"))
					+ " from icpoheaders WHERE (laccpacheaderseq = " + Long.toString(lCurrentPOHeaderSeq) + ")"
					;
				
				try{
				    Statement stmt = conn.createStatement();
				    stmt.executeUpdate(SQL);
				}catch (Exception ex) {
					log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error updating PO Lines", SQL, "[1376509636]");
					m_sErrorMessage = "Error updating PO Lines with SQL: " + SQL + " - " + ex.getMessage();
					rs.close();
					return false;
				}
				lCounter++;
				lLastPOHeaderSeq = lCurrentPOHeaderSeq;
			}
			rs.close();
		} catch (SQLException e) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error updating PO Lines", e.getMessage(), "[1376509637]");
			m_sErrorMessage = "Error updating PO Lines with SQL: " + e.getMessage();
			return false;			
		}

		//Update the po line instructions from POPORC:
		// PORHSEQ is the same as POPORH1.PORHSEQ
		// PORCREV is the sequence for line number
		// COMMENTTYP is 1 for comment, 2 for instruction
		// COMMENT is the actual comment
	    SQL =
	    	"SELECT"
			+ " POPORC.PORCSEQ"
			+ ", POPORC.COMMENTTYP"
			+ ", POPORC.COMMENT"
            + " FROM POPORC"
            + " WHERE ("
            	+ "(POPORC.COMMENTTYP = 2)" //Instructions only
            + ")"
            + " ORDER BY POPORC.PORCSEQ, POPORC.COMMENTTYP"
	    	;
		
	    long lCurrentPOLineSeq = 0;
	    long lLastPOLineSeq = 0;
	    //long lCurrentPOHeaderSequence = 0;
	    //long lLastPOHeaderSequence = 0;
	    String sInstruction = "";
	    lCounter = 0;
	    try {
			rs = clsDatabaseFunctions.openResultSet(SQL, conACCPAC);
			while (rs.next()){
				lCurrentPOLineSeq = rs.getLong("POPORC.PORCSEQ");
				if (lCurrentPOLineSeq != lLastPOLineSeq){
					if (lLastPOLineSeq != 0){
						if (!writePOLineInstruction(lLastPOLineSeq, sInstruction, conn)){
							log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error updating PO Line instructions", SQL, "[1376509638]");
							rs.close();
							return false;
						}
						//Reset the instruction string:
						sInstruction = "";
					}
				}
			
				if (sInstruction.compareToIgnoreCase("") == 0){
					sInstruction = rs.getString("POPORC.COMMENT").trim();
				}else{
					sInstruction = sInstruction + "\n" + rs.getString("POPORC.COMMENT").trim();
				}

				lCounter++;
				lLastPOLineSeq = lCurrentPOLineSeq;
			}
			//Write the last instruction:
			if (lLastPOLineSeq != 0){
				if (!writePOLineInstruction(lLastPOLineSeq, sInstruction, conn)){
					log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error updating PO Line instructions", SQL, "[1376509639]");
					rs.close();
					return false;
				}
				//Reset the instruction string:
				sInstruction = "";
			}
			rs.close();
		} catch (SQLException e) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error updating PO Line instructions", e.getMessage(), "[1376509640]");
			m_sErrorMessage = "Error updating PO Line instructions with SQL: " + e.getMessage();
			return false;			
		}
		
    	log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Successfully updated " + lCounter + " PO Lines", "", "[1376509641]");
    	//arrStatus.add("Updated " + lCounter + " PO Lines - " 
    	//	+ SMUtilities.getElapsedTime(lStartingTime) + " elapsed.");

		return true;
	}
	public boolean convertPOHeaderData(
			Connection conn,
			Connection conACCPAC,
			String sUserID,
			PrintWriter out
			){

		lStartingTime = System.currentTimeMillis();
		long lCounter = 0;
		ResultSet rs;
		String SQL = "";
		String sTable = "";

		//First, start a log for the conversion:
	    SMLogEntry log = new SMLogEntry(conn);
	    log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Starting PO Data conversion", "", "[1376509378]");

	    //Vendors:
    	sTable = "icvendors";
	    if (!dropTable(conn, log, sTable, sUserID)){
	    	return false;
	    }
    	SQL =
			"CREATE TABLE `icvendors` ("
			+ "`svendoracct` varchar(12) NOT NULL DEFAULT ''"
			+ ", `sname` varchar(60) NOT NULL DEFAULT ''"
			+ ", `saddressline1` varchar(60) NOT NULL DEFAULT ''"
			+ ", `saddressline2` varchar(60) NOT NULL DEFAULT ''"
			+ ", `saddressline3` varchar(60) NOT NULL DEFAULT ''"
			+ ", `saddressline4` varchar(60) NOT NULL DEFAULT ''"
			+ ", `scity` varchar(30) NOT NULL DEFAULT ''"
			+ ", `sstate` varchar(30) NOT NULL DEFAULT ''"
			+ ", `spostalcode` varchar(20) NOT NULL DEFAULT ''"
			+ ", `scountry` varchar(30) NOT NULL DEFAULT ''"
			+ ", `scontactname` varchar(60) NOT NULL DEFAULT ''"
			+ ", `sphonenumber` varchar(30) NOT NULL DEFAULT ''"
			+ ", `sfaxnumber` varchar(30) NOT NULL DEFAULT ''"
			+ ", `sterms` varchar(6) NOT NULL DEFAULT ''"
			+ ", `scompanyaccountcode` varchar(64) NOT NULL DEFAULT ''"
			+ ", `swebaddress` varchar(128) NOT NULL DEFAULT ''"
			+ ", PRIMARY KEY  (`svendoracct`)"
			+ " ) ENGINE=InnoDB"	
		;
	    if (!createTable(conn, log, sTable, sUserID, SQL)){
	    	return false;
	    }
    	
    	//Update icvendors from APVEN:
	    SQL =
	    	"SELECT"
            + " APVEN.VENDORID"
            + ", APVEN.VENDNAME"
            + ", APVEN.TEXTSTRE1"
            + ", APVEN.TEXTSTRE2"
            + ", APVEN.TEXTSTRE3"
            + ", APVEN.TEXTSTRE4"
            + ", APVEN.NAMECITY"
            + ", APVEN.CODESTTE"
            + ", APVEN.CODEPSTL"
            + ", APVEN.CODECTRY"
            + ", APVEN.NAMECTAC"
            + ", APVEN.TEXTPHON1"
            + ", APVEN.TEXTPHON2"
            + ", APVEN.TERMSCODE"
            + " FROM APVEN"
	    	;
	    lCounter = 0;
	    try {
			rs = clsDatabaseFunctions.openResultSet(SQL, conACCPAC);
			while (rs.next()){
		        SQL = "INSERT INTO icvendors ("
		    		+ "`svendoracct`"
		    		+ ", `sname`"
		    		+ ", `saddressline1`"
		    		+ ", `saddressline2`"
		    		+ ", `saddressline3`"
		    		+ ", `saddressline4`"
		    		+ ", `scity`"
		    		+ ", `sstate`"
		    		+ ", `spostalcode`"
		    		+ ", `scountry`"
		    		+ ", `scontactname`"
		    		+ ", `sphonenumber`"
		    		+ ", `sfaxnumber`"
		    		+ ", `sterms`"
		        + ") VALUES ("
		        + "'" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("APVEN.VENDORID").trim()) + "'"
		        + ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("APVEN.VENDNAME").trim()) + "'"
		        + ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("APVEN.TEXTSTRE1").trim()) + "'"
		        + ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("APVEN.TEXTSTRE2").trim()) + "'"
		        + ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("APVEN.TEXTSTRE3").trim()) + "'"
		        + ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("APVEN.TEXTSTRE4").trim()) + "'"
		        + ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("APVEN.NAMECITY").trim()) + "'"
		        + ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("APVEN.CODESTTE").trim()) + "'"
		        + ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("APVEN.CODEPSTL").trim()) + "'"
		        + ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("APVEN.CODECTRY").trim()) + "'"
		        + ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("APVEN.NAMECTAC").trim()) + "'"
		        + ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("APVEN.TEXTPHON1").trim()) + "'"
		        + ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("APVEN.TEXTPHON2").trim()) + "'"
		        + ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("APVEN.TERMSCODE").trim()) + "'"
				+ ")"
			    ;
				if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
					log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error updating ic vendors", SQL, "[1376509642]");
					m_sErrorMessage = "Error updating ic vendors with SQL: " + SQL;
					rs.close();
					return false;
				}
				lCounter++;
			}
			rs.close();
		} catch (SQLException e) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error updating ic vendors", e.getMessage(), "[1376509643]");
			m_sErrorMessage = "Error updating ic vendors with SQL: " + e.getMessage();
			return false;			
		}
		
    	log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Successfully updated " + lCounter + " ic vendors", "", "[1376509644]");
    	//arrStatus.add("Updated " + lCounter + " ic vendors - " 
    	//	+ SMUtilities.getElapsedTime(lStartingTime) + " elapsed.");

    	//PO Ship Vias:
	    sTable = "icshipvias";
	    if (!dropTable(conn, log, sTable, sUserID)){
	    	return false;
	    }
	    SQL =
			"CREATE TABLE `icshipvias` ("
			+ "`sshipviacode` varchar(6) NOT NULL DEFAULT ''"
			+ ", `sshipvianame` varchar(60) NOT NULL DEFAULT ''"
			+ ", PRIMARY KEY  (`sshipviacode`)"
			+ " ) ENGINE=InnoDB"	
		;
	    if (!createTable(conn, log, sTable, sUserID, SQL)){
	    	return false;
	    }

    	//Update PO Ship Vias:
	    SQL =
	    	"SELECT"
            + " POVIA.CODE"
            + ", POVIA.NAME"
            + " FROM POVIA"
            + " ORDER BY POVIA.CODE"
	    	;
	    lCounter = 0;
	    try {
			rs = clsDatabaseFunctions.openResultSet(SQL, conACCPAC);
			while (rs.next()){
		        SQL = "INSERT INTO icshipvias ("
		        	+ "sshipviacode"
		        	+ ", sshipvianame"
		        + ") VALUES ("
		        + "'" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("POVIA.CODE").trim()) + "'"
		        + ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("POVIA.NAME").trim()) + "'"
				+ ")"
			    ;
				if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
					log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error updating PO Ship Vias", SQL, "[1376509645]");
					m_sErrorMessage = "Error updating PO Ship Vias with SQL: " + SQL;
					rs.close();
					return false;
				}
				lCounter++;
			}
			rs.close();
		} catch (SQLException e) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error updating IC PO Ship Vias", e.getMessage(), "[1376509646]");
			m_sErrorMessage = "Error updating IC PO Ship Vias with SQL: " + e.getMessage();
			return false;			
		}

	    //PO Headers:
	    sTable = "icpoheaders";
	    if (!dropTable(conn, log, sTable, sUserID)){
	    	return false;
	    }
	    SQL =
			"CREATE TABLE `icpoheaders` ("
	    	+ " `lid` int(11) NOT NULL auto_increment"
			+ ", `svendor` varchar(24) NOT NULL DEFAULT ''"
			+ ", `sponumber` varchar(22) NOT NULL DEFAULT ''"
			+ ", `laccpacheaderseq` int(11) NOT NULL DEFAULT '0'"
			+ ", `sreference` varchar(60) NOT NULL DEFAULT ''"
			+ ", `svendorname` varchar(60) NOT NULL DEFAULT ''"
			+ ", `lstatus` int(11) NOT NULL DEFAULT '0'"
			+ ", `datpodate` date NOT NULL DEFAULT '0000-00-00'"
			+ ", `sshipcode` varchar(6) NOT NULL DEFAULT ''"
			+ ", `sshipname` varchar(60) NOT NULL DEFAULT ''"
	    	+ ", `sshipaddress1` varchar(60) NOT NULL DEFAULT ''"
	    	+ ", `sshipaddress2` varchar(60) NOT NULL DEFAULT ''"
	    	+ ", `sshipaddress3` varchar(60) NOT NULL DEFAULT ''"
	    	+ ", `sshipaddress4` varchar(60) NOT NULL DEFAULT ''"
	    	+ ", `sshipcity` varchar(30) NOT NULL DEFAULT ''"
	    	+ ", `sshipstate` varchar(30) NOT NULL DEFAULT ''"
	    	+ ", `sshippostalcode` varchar(20) NOT NULL DEFAULT ''"
	    	+ ", `sshipcountry` varchar(30) NOT NULL DEFAULT ''"
	    	+ ", `sshipphone` varchar(30) NOT NULL DEFAULT ''"
	    	+ ", `sshipfax` varchar(30) NOT NULL DEFAULT ''"
	    	+ ", `sshipcontactname` varchar(60) NOT NULL DEFAULT ''"
			+ ", `sbillcode` varchar(6) NOT NULL DEFAULT ''"
			+ ", `sbillname` varchar(60) NOT NULL DEFAULT ''"
	    	+ ", `sbilladdress1` varchar(60) NOT NULL DEFAULT ''"
	    	+ ", `sbilladdress2` varchar(60) NOT NULL DEFAULT ''"
	    	+ ", `sbilladdress3` varchar(60) NOT NULL DEFAULT ''"
	    	+ ", `sbilladdress4` varchar(60) NOT NULL DEFAULT ''"
	    	+ ", `sbillcity` varchar(30) NOT NULL DEFAULT ''"
	    	+ ", `sbillstate` varchar(30) NOT NULL DEFAULT ''"
	    	+ ", `sbillpostalcode` varchar(20) NOT NULL DEFAULT ''"
	    	+ ", `sbillcountry` varchar(30) NOT NULL DEFAULT ''"
	    	+ ", `sbillphone` varchar(30) NOT NULL DEFAULT ''"
	    	+ ", `sbillfax` varchar(30) NOT NULL DEFAULT ''"
	    	+ ", `sbillcontactname` varchar(60) NOT NULL DEFAULT ''"
			+ ", `sshipviacode` varchar(6) NOT NULL DEFAULT ''"
			+ ", `sshipvianame` varchar(60) NOT NULL DEFAULT ''"
			+ ", `datexpecteddate` date NOT NULL DEFAULT '0000-00-00'"
			+ ", `datassigned` datetime NOT NULL DEFAULT '0000-00-00 00:00:00'"
			+ ", `sassignedtoname` varchar(128) NOT NULL DEFAULT ''"
			+ ", `scomment` varchar(254) NOT NULL DEFAULT ''"
			+ ", `sdescription` varchar(60) NOT NULL DEFAULT ''"
			+ ", `sdeletedby` varchar(128) NOT NULL DEFAULT ''"
			+ ", `datdeleted` datetime NOT NULL DEFAULT '0000-00-00 00:00:00'"
			+ ", PRIMARY KEY  (`lid`)"
			+ ", KEY laccpacheaderseqkey (`laccpacheaderseq`)"
			+ ", KEY ponumberkey (`sponumber`)"
			+ " ) ENGINE=InnoDB"	
		;
	    if (!createTable(conn, log, sTable, sUserID, SQL)){
	    	return false;
	    }

    	//Update PO Headers:
	    SQL =
	    	"SELECT"
            + " POPORH1.VDCODE"
            + ", POPORH1.PONUMBER"
            + ", POPORH1.PORHSEQ"
            + ", POPORH1.REFERENCE"
            + ", POPORH1.VDNAME"
            + ", POPORH1.ISCOMPLETE"
            + ", POPORH1.RCPCOUNT"
            + ", POPORH1.DATE"
            + ", POPORH1.VIACODE"
            + ", POPORH1.VIANAME"
            + ", POPORH1.EXPARRIVAL"
            + ", POPORH1.COMMENT"
            + ", POPORH1.DESCRIPTIO"
            + " FROM POPORH1"
            + " ORDER BY POPORH1.PORHSEQ"
	    	;
	    lCounter = 0;
	    String sPOStatus = "0";
	    try {
			rs = clsDatabaseFunctions.openResultSet(SQL, conACCPAC);
			while (rs.next()){
				if (rs.getLong("POPORH1.ISCOMPLETE") == 1){
					sPOStatus = "2"; //STATUS_COMPLETE = 2
				}else{
					if (rs.getLong("POPORH1.RCPCOUNT") > 1){
						sPOStatus = "1"; //STATUS_PARTIALLY_RECEIVED = 1
					}else{
						sPOStatus = "0"; //STATUS_ENTERED = 0
					}
				}
		        SQL = "INSERT INTO icpoheaders ("
		        	+ "svendor"
		        	+ ", sponumber"
		        	+ ", laccpacheaderseq"
					+ ", sreference"
					+ ", svendorname"
					+ ", lstatus"
					+ ", datpodate"
					+ ", sshipviacode"
					+ ", sshipvianame"
					+ ", datexpecteddate"
					+ ", datassigned"
					+ ", scomment"
					+ ", sdescription"
		        + ") VALUES ("
		        + "'" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("POPORH1.VDCODE").trim()) + "'"
		        + ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("POPORH1.PONUMBER").trim()) + "'"
		        + ", " + Long.toString(rs.getLong("POPORH1.PORHSEQ"))
		        + ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("POPORH1.REFERENCE").trim()) + "'"
		        + ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("POPORH1.VDNAME").trim()) + "'"
		        + ", " + sPOStatus
		        + ", '" + convertACCPACLongDateToString(rs.getLong("POPORH1.DATE"), false) + "'"
		        + ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("POPORH1.VIACODE").trim()) + "'"
		        + ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("POPORH1.VIANAME").trim()) + "'"
		        + ", '" + convertACCPACLongDateToString(rs.getLong("POPORH1.EXPARRIVAL"), false) + "'"
		        + ", '" + convertACCPACLongDateToString(rs.getLong("POPORH1.DATE"), false) + "'"
		        + ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("POPORH1.COMMENT").trim()) + "'"
		        + ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("POPORH1.DESCRIPTIO").trim()) + "'"
				+ ")"
			    ;
				if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
					log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error updating PO Headers", SQL, "[1376509647]");
					m_sErrorMessage = "Error updating PO Headers with SQL: " + SQL;
					rs.close();
					return false;
				}
				lCounter++;
			}
			rs.close();
		} catch (SQLException e) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error updating PO Headers", e.getMessage(), "[1376509648]");
			m_sErrorMessage = "Error updating PO Headers with SQL: " + e.getMessage();
			return false;			
		}
		//arrStatus.add("Successfully inserted " + lCounter + " PO Headers - " 
		//		+ SMUtilities.getElapsedTime(lStartingTime) + " elapsed.");
	    
	    if (!dropTable(conn, log, "poporh2", sUserID)){
	    	return false;
	    }
		//Create a temporary table for the ACCPAC records:
		SQL = "CREATE TEMPORARY TABLE poporh2 ("
			+ "stdesc varchar(60) NOT NULL default ''"
			+ ", stcode varchar(6) NOT NULL default ''"
			+ ", staddress1 varchar(60) NOT NULL default ''"
			+ ", staddress2 varchar(60) NOT NULL default ''"
			+ ", staddress3 varchar(60) NOT NULL default ''"
			+ ", staddress4 varchar(60) NOT NULL default ''"
			+ ", stcity varchar(30) NOT NULL default ''"
			+ ", ststate varchar(30) NOT NULL default ''"
			+ ", stzip varchar(20) NOT NULL default ''"
			+ ", stcountry varchar(30) NOT NULL default ''"
			+ ", stphone varchar(30) NOT NULL default ''"
			+ ", stfax varchar(30) NOT NULL default ''"
			+ ", stcontact varchar(60) NOT NULL default ''"
			+ ", btdesc varchar(60) NOT NULL default ''"
			+ ", btcode varchar(6) NOT NULL default ''"
			+ ", btaddress1 varchar(60) NOT NULL default ''"
			+ ", btaddress2 varchar(60) NOT NULL default ''"
			+ ", btaddress3 varchar(60) NOT NULL default ''"
			+ ", btaddress4 varchar(60) NOT NULL default ''"
			+ ", btcity varchar(30) NOT NULL default ''"
			+ ", btstate varchar(30) NOT NULL default ''"
			+ ", btzip varchar(20) NOT NULL default ''"
			+ ", btcountry varchar(30) NOT NULL default ''"
			+ ", btphone varchar(30) NOT NULL default ''"
			+ ", btfax varchar(30) NOT NULL default ''"
			+ ", btcontact varchar(60) NOT NULL default ''"
			+ ", porhseq int(11) NOT NULL default '0'"
			+ ", PRIMARY KEY (porhseq)"
			+ ", KEY porhseqkey (`porhseq`)"
			+ ")Engine=InnoDb"
			;
		
		try {
			if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
				m_sErrorMessage = "Error creating poporh2 table with SQL: " + SQL;
				return false;
			}
		} catch (SQLException e1) {
			m_sErrorMessage = "Error creating poporh2 table with SQL: " + SQL + " - " + e1.getMessage();
			return false;
		}
			
		//Get the data from the POPORH2 table:
	    SQL =
	    	"SELECT"
            + " POPORH2.STDESC"
            + ", POPORH2.STCODE"
            + ", POPORH2.STADDRESS1"
            + ", POPORH2.STADDRESS2"
            + ", POPORH2.STADDRESS3"
            + ", POPORH2.STADDRESS4"
            + ", POPORH2.STCITY"
            + ", POPORH2.STSTATE"
            + ", POPORH2.STZIP"
            + ", POPORH2.STCOUNTRY"
            + ", POPORH2.STPHONE"
            + ", POPORH2.STFAX"
            + ", POPORH2.BTDESC"
            + ", POPORH2.BTCODE"
            + ", POPORH2.BTADDRESS1"
            + ", POPORH2.BTADDRESS2"
            + ", POPORH2.BTADDRESS3"
            + ", POPORH2.BTADDRESS4"
            + ", POPORH2.BTCITY"
            + ", POPORH2.BTSTATE"
            + ", POPORH2.BTZIP"
            + ", POPORH2.BTCOUNTRY"
            + ", POPORH2.BTPHONE"
            + ", POPORH2.BTFAX"
            + ", POPORH2.STCONTACT"
            + ", POPORH2.BTCONTACT"
            + ", POPORH2.PORHSEQ"
            + " FROM POPORH2"
            + " ORDER BY POPORH2.PORHSEQ"
	    	;
	    lCounter = 0;
	    try {
			rs = clsDatabaseFunctions.openResultSet(SQL, conACCPAC);
			while (rs.next()){
				SQL = " INSERT INTO poporh2 ("
				+ "stdesc"
				+ ", stcode"
				+ ", staddress1"
				+ ", staddress2"
				+ ", staddress3"
				+ ", staddress4"
				+ ", stcity"
				+ ", ststate"
				+ ", stzip"
				+ ", stcountry"
				+ ", stphone"
				+ ", stfax"
				+ ", stcontact"
				+ ", btdesc"
				+ ", btcode"
				+ ", btaddress1"
				+ ", btaddress2"
				+ ", btaddress3"
				+ ", btaddress4"
				+ ", btcity"
				+ ", btstate"
				+ ", btzip"
				+ ", btcountry"
				+ ", btphone"
				+ ", btfax"
				+ ", btcontact"
				+ ", porhseq"
				+ ") VALUES ("
				+ "'" + clsDatabaseFunctions.FormatSQLStatement(getRecordsetStringValue(rs, "POPORH2.STDESC").trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getRecordsetStringValue(rs, "POPORH2.STCODE").trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getRecordsetStringValue(rs, "POPORH2.STADDRESS1").trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getRecordsetStringValue(rs, "POPORH2.STADDRESS2").trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getRecordsetStringValue(rs, "POPORH2.STADDRESS3").trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getRecordsetStringValue(rs, "POPORH2.STADDRESS4").trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getRecordsetStringValue(rs, "POPORH2.STCITY").trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getRecordsetStringValue(rs, "POPORH2.STSTATE").trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getRecordsetStringValue(rs, "POPORH2.STZIP").trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getRecordsetStringValue(rs, "POPORH2.STCOUNTRY").trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getRecordsetStringValue(rs, "POPORH2.STPHONE").trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getRecordsetStringValue(rs, "POPORH2.STFAX").trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getRecordsetStringValue(rs, "POPORH2.STCONTACT").trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getRecordsetStringValue(rs, "POPORH2.BTDESC").trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getRecordsetStringValue(rs, "POPORH2.BTCODE").trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getRecordsetStringValue(rs, "POPORH2.BTADDRESS1").trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getRecordsetStringValue(rs, "POPORH2.BTADDRESS2").trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getRecordsetStringValue(rs, "POPORH2.BTADDRESS3").trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getRecordsetStringValue(rs, "POPORH2.BTADDRESS4").trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getRecordsetStringValue(rs, "POPORH2.BTCITY").trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getRecordsetStringValue(rs, "POPORH2.BTSTATE").trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getRecordsetStringValue(rs, "POPORH2.BTZIP").trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getRecordsetStringValue(rs, "POPORH2.BTCOUNTRY").trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getRecordsetStringValue(rs, "POPORH2.BTPHONE").trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getRecordsetStringValue(rs, "POPORH2.BTFAX").trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getRecordsetStringValue(rs, "POPORH2.BTCONTACT").trim()) + "'"
				+ ", " + Long.toString(rs.getLong("POPORH2.PORHSEQ"))
				+ ")"
				;
				if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
					log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error inserting PO Headers from POPORH2", SQL, "[1376509649]");
					m_sErrorMessage = "Error inserting PO Headers  from POPORH2 with SQL: " + SQL;
					rs.close();
					return false;
				}
				lCounter++;
			}
			rs.close();
		} catch (SQLException e) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error updating PO Headers from POPORH2", e.getMessage(), "[1376509650]");
			m_sErrorMessage = "Error updating PO Headers from POPORH2 with SQL: " + e.getMessage();
			return false;			
		}
		
		log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Successfully inserted " + lCounter 
				+ " PO Headers 2 records into poporh2 - " 
				+ clsDateAndTimeConversions.getElapsedTime(lStartingTime) + " elapsed.", "", "[1376509651]");
		//arrStatus.add("Successfully inserted " + lCounter + " PO Headers 2 records into poporh2 - " 
		//		+ SMUtilities.getElapsedTime(lStartingTime) + " elapsed.");

		//Now update the icpoheaders table:
		SQL = "UPDATE icpoheaders, poporh2"
			+ " SET "
	        + "sshipname = poporh2.stdesc"
	        + ", sshipcode = poporh2.stcode"
		    + ", sshipaddress1 = poporh2.staddress1"
		    + ", sshipaddress2 = poporh2.staddress2"
		    + ", sshipaddress3 = poporh2.staddress3"
		    + ", sshipaddress4 = poporh2.staddress4"
		    + ", sshipcity = poporh2.stcity"
		    + ", sshipstate = poporh2.ststate"
		    + ", sshippostalcode = poporh2.stzip"
		    + ", sshipcountry = poporh2.stcountry"
		    + ", sshipphone = poporh2.stphone"
		    + ", sshipfax = poporh2.stfax"
		    + ", sshipcontactname = poporh2.stcontact"
	        + ", sbillcode = poporh2.btcode"
	        + ", sbillname = poporh2.btdesc"
		    + ", sbilladdress1 = poporh2.btaddress1"
		    + ", sbilladdress2 = poporh2.btaddress2"
		    + ", sbilladdress3 = poporh2.btaddress3"
		    + ", sbilladdress4 = poporh2.btaddress4"
		    + ", sbillcity = poporh2.btcity"
		    + ", sbillstate = poporh2.btstate"
		    + ", sbillpostalcode = poporh2.btzip"
		    + ", sbillcountry = poporh2.btcountry"
		    + ", sbillphone = poporh2.btphone"
		    + ", sbillfax = poporh2.btfax"
		    + ", sbillcontactname = poporh2.btcontact"
		    + ", sassignedtoname = poporh2.btcontact"
		    + " WHERE ("
		    	+ "icpoheaders.laccpacheaderseq = poporh2.porhseq"
		    + ")"
		    ;
		try {
			if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
				log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error updating from poporh2", SQL, "[1376509652]");
				m_sErrorMessage = "Error updating from poporh2 with SQL: " + SQL;
				return false;
			}
		} catch (SQLException e) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error updating from poporh2", e.getMessage(), "[1376509653]");
			m_sErrorMessage = "Error updating from poporh2 with SQL: " + SQL + " - " + e.getMessage();
		}
		
		//Now remove the temporary table:
		SQL = "DROP TEMPORARY TABLE IF EXISTS poporh2";
		try {
			if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
				log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error dropping temporary table poporh2", SQL, "[1376509654]");
				m_sErrorMessage = "Error dropping temporary table poporh2 with SQL: " + SQL;
				return false;
			}
		} catch (SQLException e) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error dropping temporary table poporh2", e.getMessage(), "[1376509655]");
			m_sErrorMessage = "Error dropping temporary table poporh2 with SQL: " + SQL + " - " + e.getMessage();
		}
		
		log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Successfully updated PO Headers  from POPORH2", "", "[1376509656]");
    	//arrStatus.add("Updated PO Headers from POPORH2 - " 
    	//	+ SMUtilities.getElapsedTime(lStartingTime) + " elapsed.");
    	
		return true;
	}
	public boolean convertPOReceiptData(
			Connection conn,
			Connection conACCPAC,
			String sUserID,
			PrintWriter out
			){

		lStartingTime = System.currentTimeMillis();
		ResultSet rs;
		String SQL = "";
		String sTable = "";

		//First, start a log for the conversion:
	    SMLogEntry log = new SMLogEntry(conn);
	    log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Starting PO Receipt conversion", "", "[1376509380]");

	    //PO Receipt Headers:
	    sTable = "icporeceiptheaders";
	    if (!dropTable(conn, log, sTable, sUserID)){
	    	return false;
	    }
	    SQL =
			"CREATE TABLE `icporeceiptheaders` ("
	    	+ " `lid` int(11) NOT NULL auto_increment"
	    	+ ", `lpoheaderid` int(11) NOT NULL default '0'"
	    	+ ", `datreceived` date NOT NULL DEFAULT '0000-00-00'"
	    	+ ", `sreceiptnumber` varchar(22) NOT NULL DEFAULT ''"
	    	+ ", `rcphseq` int(11) NOT NULL default '0'"
	    	+ ", `sponumber` varchar(22) NOT NULL DEFAULT ''"
	    	+ ", `sinvoicenumber` varchar(22) NOT NULL DEFAULT ''"
	    	+ ", `lpostedtoic` int(11) NOT NULL DEFAULT '0'"
			+ ", `sdeletedby` varchar(128) NOT NULL DEFAULT ''"
			+ ", `datdeleted` datetime NOT NULL DEFAULT '0000-00-00 00:00:00'"
			+ ", `lstatus` int(11) NOT NULL DEFAULT '0'"
			+ ", PRIMARY KEY  (`lid`)"
			+ ", KEY ponumberkey (`sponumber`)"
			+ ", KEY rcphseqkey (`rcphseq`)"
			+ " ) ENGINE=InnoDB"	
		;
	    if (!createTable(conn, log, sTable, sUserID, SQL)){
	    	return false;
	    }

    	//Update PO Receipt Headers:
	    SQL =
	    	"SELECT"
            + " PORCPH1.DATE"
            + ", PORCPH1.RCPNUMBER"
            + ", PORCPH1.RCPHSEQ"
            + ", PORCPH1.PONUMBER"
            + ", PORCPH1.INVNUMBER"
            + " FROM PORCPH1"
            + " ORDER BY PORCPH1.RCPHSEQ"
	    	;
	    try {
			rs = clsDatabaseFunctions.openResultSet(SQL, conACCPAC);
			while (rs.next()){
		        SQL = "INSERT INTO icporeceiptheaders ("
			    	+ "datreceived"
			    	+ ", sreceiptnumber"
			    	+ ", rcphseq"
			    	+ ", sponumber"
			    	+ ", sinvoicenumber"
			    	+ ", lpostedtoic"
		        + ") VALUES ("
		        + "'" + convertACCPACLongDateToString(rs.getLong("PORCPH1.DATE"), false) + "'"
		        + ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("PORCPH1.RCPNUMBER").trim()) + "'"
		        + ", " + Long.toString(rs.getLong("PORCPH1.RCPHSEQ"))
		        + ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("PORCPH1.PONUMBER").trim()) + "'"
		        + ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("PORCPH1.INVNUMBER").trim()) + "'"
		        + ", 1"
				+ ")"
			    ;
				try{
				    Statement stmt = conn.createStatement();
				    stmt.executeUpdate(SQL);
				}catch (Exception ex) {
					log.writeEntry(
							sUserID, 
							SMLogEntry.LOG_OPERATION_ICCONVERSION, 
							"Error updating PO Receipt Headers", 
							SQL + " - " + ex.getMessage(),
							"[1376509657]"
					);
					m_sErrorMessage = "Error updating PO Receipt Headers with SQL: " 
						+ SQL  + " - " + ex.getMessage();
					rs.close();
					return false;				
				}
			}
			rs.close();
		} catch (SQLException e) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error updating PO Receipt Headers", e.getMessage(), "[1376509658]");
			m_sErrorMessage = "Error updating PO Receipt Headers with SQL: " + e.getMessage();
			return false;			
		}
		//arrStatus.add("Successfully inserted " + lCounter + " PO Receipt Headers - " 
		//		+ SMUtilities.getElapsedTime(lStartingTime) + " elapsed.");
	    
		//Update the PO ID's:
		SQL = "UPDATE icporeceiptheaders, icpoheaders"
			+ " SET icporeceiptheaders.lpoheaderid = icpoheaders.lid"
			+ " WHERE ("
				+ "(icporeceiptheaders.sponumber = icpoheaders.sponumber)"
			+ ")"
			;
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			log.writeEntry(
					sUserID, 
					SMLogEntry.LOG_OPERATION_ICCONVERSION, 
					"Error updating PO Receipt Headers with po id", 
					SQL + " - " + ex.getMessage(),
					"[1376509659]"
			);
			m_sErrorMessage = "Error updating PO Receipt Headers with po id with SQL: " 
				+ SQL  + " - " + ex.getMessage();
			return false;				
		}

	    //PO Receipt Lines:
	    sTable = "icporeceiptlines";
	    if (!dropTable(conn, log, sTable, sUserID)){
	    	return false;
	    }
	    SQL =
			"CREATE TABLE `icporeceiptlines` ("
	    	+ " `lid` int(11) NOT NULL auto_increment"
	    	+ ", `llinenumber` int(11) NOT NULL default '0'"
	    	+ ", `lreceiptheaderid` int(11) NOT NULL default '0'"
	    	+ ", `lpolineid` int(11) NOT NULL default '0'"
	    	+ ", `bdqtyreceived` decimal(17,4) NOT NULL DEFAULT '0.0000'"
	    	+ ", `bdextendedcost` decimal(17,2) NOT NULL DEFAULT '0.00'"
	    	+ ", `sitemnumber` varchar(24) NOT NULL DEFAULT ''"
	    	+ ", `sitemdescription` varchar(75) NOT NULL DEFAULT ''"
	    	+ ", `slocation` varchar(6) NOT NULL DEFAULT ''"
	    	+ ", `sglexpenseacct` varchar(75) NOT NULL DEFAULT ''"
	    	+ ", `sunitofmeasure` varchar(10) NOT NULL DEFAULT ''"
	    	+ ", `lnoninventoryitem` int(11) NOT NULL default '0'"
	    	+ ", `rcphseq` int(11) NOT NULL DEFAULT '0'" //Unique po receipt header id
	    	+ ", `porlseq` int(11) NOT NULL DEFAULT '0'" //Unique po line id
	    	+ ", `sponumber` varchar(22) NOT NULL DEFAULT ''"
	    	+ ", `bdunitcost` decimal(17,6) NOT NULL DEFAULT '0.000000'"
	    	+ ", `lpoinvoiceid` int(11) NOT NULL DEFAULT '-1'"
			+ ", PRIMARY KEY  (`lid`)"
			+ ", KEY porlseqkey (`porlseq`)"
			+ ", KEY rcphseqkey (`rcphseq`)"
			//Have to add this key AFTER we get the records and update the receipt header ID -
			//Otherwise, we get duplicate keys:
			//+ ", UNIQUE KEY `headerlinenokey` (`lreceiptheaderid`, `llinenumber`)"
			+ " ) ENGINE=InnoDB"	
		;
	    if (!createTable(conn, log, sTable, sUserID, SQL)){
	    	return false;
	    }

	    //Insert po receipt line records:
	    //There are several qty fields in PORCPL that don't seem to be needed - after checking with
	    // queries (6/23/10) in a set of Washington's data up to mid-2008, it appears that all of the 
	    // quantities in fields starting with 'RQ' or 'SQ' are redundant - all the fields that start
	    // with 'OQ' appear to have the exact same data in them
	    SQL =
	    	"SELECT"
            + " PORCPL.RCPHSEQ"
            + ", PORCPL.POSTEDTOIC"
            + ", PORCPL.PONUMBER"
            + ", PORCPL.EXTENDED"
            + ", PORCPL.OQRECEIVED"
            + ", PORCPL.PORLSEQ"
            + ", PORCPL.ITEMNO"
            + ", PORCPL.ITEMDESC"
            + ", PORCPL.LOCATION"
            + ", PORCPL.GLACEXPENS"
            + ", PORCPL.RCPUNIT"
            + ", PORCPL.ITEMEXISTS"
            + ", PORCPL.UNITCOST"
            + " FROM PORCPL"
            + " ORDER BY PORCPL.RCPHSEQ, PORCPL.RCPLREV"
	    	;
	    long lLineNumber = 0;
	    long lRCPHSEQ = 0;
	    long lLastRCPHSEQ = 0;
	    try {
			rs = clsDatabaseFunctions.openResultSet(SQL, conACCPAC);
			while (rs.next()){
				lRCPHSEQ = rs.getLong("PORCPL.RCPHSEQ");
				if (lRCPHSEQ != lLastRCPHSEQ){
					lLineNumber = 0;
				}
				lLineNumber++;
				String sNonInventoryItem = "0";
				if (rs.getLong("PORCPL.ITEMEXISTS") != 1){
					sNonInventoryItem = "1";
				}
		        SQL = "INSERT INTO icporeceiptlines ("
			    	//+ "ipostedtoic"
			    	+ "bdqtyreceived"
			    	+ ", bdextendedcost"
			    	+ ", rcphseq"
			    	+ ", sponumber"
			    	+ ", porlseq"
			    	+ ", sitemnumber"
			    	+ ", sitemdescription"
			    	+ ", slocation"
			    	+ ", sglexpenseacct"
			    	+ ", sunitofmeasure"
			    	+ ", lnoninventoryitem"
			    	+ ", llinenumber"
			    	+ ", bdunitcost"
		        + ") VALUES ("
		        //+ Long.toString(rs.getLong("PORCPL.POSTEDTOIC"))
		        + clsManageBigDecimals.BigDecimalToFormattedString(
					"#########0.0000", rs.getBigDecimal("PORCPL.OQRECEIVED"))
		        + ", " + clsManageBigDecimals.BigDecimalToFormattedString(
					"#########0.00", rs.getBigDecimal("PORCPL.EXTENDED"))
				+ ", " + Long.toString(lRCPHSEQ)
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("PORCPL.PONUMBER").trim()) + "'"
				+ ", " + Long.toString(rs.getLong("PORCPL.PORLSEQ"))
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("PORCPL.ITEMNO").trim().replace("-", "").replace(" ", BLANK_REPLACEMENT_CHARACTER)) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("PORCPL.ITEMDESC").trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("PORCPL.LOCATION").trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("PORCPL.GLACEXPENS").trim().replace("-", "")) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rs.getString("PORCPL.RCPUNIT").trim()) + "'"
				+ ", " + sNonInventoryItem
				+ ", " + Long.toString(lLineNumber)
				+ ", " + clsManageBigDecimals.BigDecimalToFormattedString(
					"#########0.000000", rs.getBigDecimal("PORCPL.UNITCOST"))
				+ ")"
			    ;
				try{
				    Statement stmt = conn.createStatement();
				    stmt.executeUpdate(SQL);
				}catch (Exception ex) {
					log.writeEntry(
							sUserID, 
							SMLogEntry.LOG_OPERATION_ICCONVERSION, 
							"Error updating PO Receipt Lines", 
							SQL + " - " + ex.getMessage(),
							"[1376509660]"
					);
					m_sErrorMessage = "Error updating PO Receipt Lines with SQL: " 
						+ SQL  + " - " + ex.getMessage();
					rs.close();
					return false;				
				}
				lLastRCPHSEQ = lRCPHSEQ;
			}
			rs.close();
		} catch (SQLException e) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error updating PO Receipt Lines", e.getMessage(), "[1376509661]");
			m_sErrorMessage = "Error updating PO Receipt Lines with SQL: " + SQL + e.getMessage();
			return false;			
		}
		//arrStatus.add("Successfully inserted " + lCounter + " PO Receipt Lines - " 
		//		+ SMUtilities.getElapsedTime(lStartingTime) + " elapsed.");
		
		//Update the PO receipt line ID's:
		SQL = "UPDATE icporeceiptheaders, icporeceiptlines"
			+ " SET icporeceiptlines.lreceiptheaderid = icporeceiptheaders.lid"
			+ " WHERE ("
				+ "(icporeceiptheaders.rcphseq = icporeceiptlines.rcphseq)"
			+ ")"
			;
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			log.writeEntry(
					sUserID, 
					SMLogEntry.LOG_OPERATION_ICCONVERSION, 
					"Error updating PO Receipt Lines with po receipt header id", 
					SQL + " - " + ex.getMessage(),
					"[1376509662]"
			);
			m_sErrorMessage = "Error updating PO Receipt Lines with po receipt header with SQL: " 
				+ SQL  + " - " + ex.getMessage();
			return false;				
		}
		
		//Have to add this index now because before this, the receipt header ID was zero
		//and that would have created duplicate keys:
		SQL = "ALTER TABLE icporeceiptlines ADD UNIQUE KEY`headerlinenokey` (`lreceiptheaderid`, `llinenumber`)";
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			log.writeEntry(
					sUserID, 
					SMLogEntry.LOG_OPERATION_ICCONVERSION, 
					"Error adding headerlinekey to po receipt lines", 
					SQL + " - " + ex.getMessage(),
					"[1376509663]"
			);
			m_sErrorMessage = "Error adding headerlinekey to po receipt lines with SQL: " 
				+ SQL  + " - " + ex.getMessage();
			return false;				
		}
		
		//Update the po invoice ID on each of the po receipt lines if the receipt had been invoiced in ACCPAC.
		//Since we don't have a REAL PO Invoice ID, only an invoice number, we'll just set these to ZERO to indicate
		//that they are NOT UNinvoiced (they would have a -1 in that case), but they are not tied to any
		//real invoice in the new system, either:
		SQL = "UPDATE icporeceiptheaders, icporeceiptlines"
			+ " SET icporeceiptlines.lpoinvoiceid = 0"
			+ " WHERE ("
				+ "(icpolines.porlseq = icporeceiptlines.porlseq)"
				+ " AND (icporeceiptheaders.sinvoicenumber != '')"
			+ ")"
			;
		
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			log.writeEntry(
					sUserID, 
					SMLogEntry.LOG_OPERATION_ICCONVERSION, 
					"Error updating po invoice id on po receipt lines", 
					SQL + " - " + ex.getMessage(),
					"[1376509664]"
			);
			m_sErrorMessage = "Error updating po invoice id on po receipt lines with SQL: " 
				+ SQL  + " - " + ex.getMessage();
			return false;				
		}
		
		//TJR - commented this out 10/31/2011 - we'll leave it in in case people need to find their old invoice
		//numbers that were entered in ACCPAC against receipts.
		//Now we can remove the old 'invoicenumber' from the IC PO Receipt headers, since it's not used
		//in the new system:
		//SQL = "alter table icporeceiptheaders drop column sinvoicenumber";
		//try{
		//    Statement stmt = conn.createStatement();
		//    stmt.executeUpdate(SQL);
		//}catch (Exception ex) {
		//	log.writeEntry(
		//			sUserName, 
		//			SMLogEntry.LOG_OPERATION_ICCONVERSION, 
		//			"Error dropping sinvoicenumber from icporeceiptheaders", 
		//			SQL + " - " + ex.getMessage()
		//	);
		//	m_sErrorMessage = "Error updating po line id on po receipt lines with SQL: " 
		//		+ SQL  + " - " + ex.getMessage();
		//	return false;				
		//}
		
		SQL = "UPDATE icpolines, icporeceiptlines"
			+ " SET icporeceiptlines.lpolineid = icpolines.lid"
			+ " WHERE ("
				+ "(icpolines.porlseq = icporeceiptlines.porlseq)"
			+ ")"
			;
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			log.writeEntry(
					sUserID, 
					SMLogEntry.LOG_OPERATION_ICCONVERSION, 
					"Error updating po line id on po receipt lines", 
					SQL + " - " + ex.getMessage(),
					"[1376509665]"
			);
			m_sErrorMessage = "Error updating po line id on po receipt lines with SQL: " 
				+ SQL  + " - " + ex.getMessage();
			return false;				
		}

		//Update the iccosts table with the receipt line ID's for the cost buckets:
		SQL = "UPDATE iccosts, icporeceiptlines, icporeceiptheaders"
			+ " SET iccosts.lreceiptlineid = icporeceiptlines.lid"
			+ " WHERE ("
				+ "(icporeceiptheaders.lid = icporeceiptlines.lreceiptheaderid)"
				+ " AND (iccosts.sitemnumber = icporeceiptlines.sitemnumber)"  
				+ " AND (iccosts.sreceiptnumber = icporeceiptheaders.sreceiptnumber)"
		 	+ ")"
		;
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			log.writeEntry(
					sUserID, 
					SMLogEntry.LOG_OPERATION_ICCONVERSION, 
					"Error updating po receipt line id on iccosts", 
					SQL + " - " + ex.getMessage(),
					"[1376509666]"
			);
			m_sErrorMessage = "Error updating po receipt line id on iccosts with SQL: " 
				+ SQL  + " - " + ex.getMessage();
			return false;				
		}
		
		//Update the icporeceiptlines with the correct GL account.  If the line is an inventory item,
		//we need to get the payables clearing account for the particular line, based on the location:
		SQL = "UPDATE icporeceiptlines, Locations"
			+ " SET icporeceiptlines.sglexpenseacct = Locations.sglpayableclearingacct"
			+ " WHERE ("
				+ "(icporeceiptlines.slocation = Locations.sLocation)"
				+ " AND (icporeceiptlines.lnoninventoryitem = 0)"
				+ " AND (TRIM(icporeceiptlines.sglexpenseacct) = '')"
		 	+ ")"
		;
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			log.writeEntry(
					sUserID, 
					SMLogEntry.LOG_OPERATION_ICCONVERSION, 
					"Error updating po receipt line with payables clearing account", 
					SQL + " - " + ex.getMessage(),
					"[1376509667]"
			);
			m_sErrorMessage = "Error updating po receipt line with payables clearing account with SQL: " 
				+ SQL  + " - " + ex.getMessage();
			return false;				
		}
		
		//icpoinvoiceheaders:
	    sTable = "icpoinvoiceheaders";
	    if (!dropTable(conn, log, sTable, sUserID)){
	    	return false;
	    }
	    SQL =
			"CREATE TABLE `icpoinvoiceheaders` ("
	    	+ " `lid` int(11) NOT NULL auto_increment"
	    	+ ", `lreceiptid` int(11) NOT NULL default '0'"
	    	+ ", `sinvoicenumber` varchar(22) NOT NULL default ''"
	    	+ ", `datinvoice` date NOT NULL default '0000-00-00'"
	    	+ ", `sterms` varchar(6) NOT NULL DEFAULT ''"
	    	+ ", `datdiscount` date NOT NULL default '0000-00-00'"
	    	+ ", `datdue` date NOT NULL default '0000-00-00'"
	    	+ ", `bddiscount` decimal(17,4) NOT NULL DEFAULT '0.0000'"
	    	+ ", `bdinvoicetotal` decimal(17,2) NOT NULL DEFAULT '0.00'"
	    	+ ", `lexportsequencenumber` int(11) NOT NULL default '0'"
	    	+ ", `svendor` varchar(12) NOT NULL default ''"
	    	+ ", `svendorname` varchar(60) NOT NULL default ''"
	    	+ ", `lpoheaderid` int(11) NOT NULL default '0'"
	    	+ ", `bdreceivedamount` decimal(17,2) NOT NULL DEFAULT '0.00'"
	    	+ ", `sdescription` varchar(60) NOT NULL default ''"
	    	+ ", `itaxclass` int(11) NOT NULL default '0'"
	    	+ ", `staxgroup` varchar(12) NOT NULL default ''"
			+ ", PRIMARY KEY  (`lid`)"
			+ " ) ENGINE=InnoDB"	
		;
	    if (!createTable(conn, log, sTable, sUserID, SQL)){
	    	return false;
	    }
	    
		//icpoinvoicelines:
	    sTable = "icpoinvoicelines";
	    if (!dropTable(conn, log, sTable, sUserID)){
	    	return false;
	    }
	    SQL =
			"CREATE TABLE `icpoinvoicelines` ("
	    	+ " `lid` int(11) NOT NULL auto_increment"
	    	+ ", `lpoinvoiceheaderid` int(11) NOT NULL default '0'"
	    	+ ", `lporeceiptlineid` int(11) NOT NULL default '0'"
	    	+ ", `sinvoicenumber` varchar(22) NOT NULL default ''"
	    	+ ", `bdreceivedcost` decimal(17,2) NOT NULL DEFAULT '0.00'"
	    	+ ", `bdinvoicedcost` decimal(17,2) NOT NULL DEFAULT '0.00'"
	    	+ ", `sexpenseaccount` varchar(75) NOT NULL default ''"
	    	+ ", `bdqtyreceived` decimal(17,4) NOT NULL DEFAULT '0.0000'"
	    	+ ", `sitemnumber` varchar(24) NOT NULL default ''"
	    	+ ", `sitemdescription` varchar(75) NOT NULL default ''"
	    	+ ", `slocation` varchar(6) NOT NULL default ''"
	    	+ ", `sunitofmeasure` varchar(10) NOT NULL default ''"
	    	+ ", `lnoninventoryitem` int(11) NOT NULL default '0'"
	    	+ ", `lporeceiptid` int(11) NOT NULL default '-1'"
			+ ", PRIMARY KEY  (`lid`)"
			+ " ) ENGINE=InnoDB"	
		;
	    if (!createTable(conn, log, sTable, sUserID, SQL)){
	    	return false;
	    }
	    
		//icinvoiceexportsequences:
	    sTable = "icinvoiceexportsequences";
	    if (!dropTable(conn, log, sTable, sUserID)){
	    	return false;
	    }
	    SQL =
			"CREATE TABLE `icinvoiceexportsequences` ("
	    	+ " `lid` int(11) NOT NULL auto_increment"
	    	+ ", `suser` varchar(128) NOT NULL default ''"
	    	+ ", `datexported` datetime NOT NULL default '0000-00-00 00:00:00'"
	    	+ ", `scomment` varchar(128) NOT NULL DEFAULT ''"
			+ ", PRIMARY KEY  (`lid`)"
			+ " ) ENGINE=InnoDB"	
		;
	    if (!createTable(conn, log, sTable, sUserID, SQL)){
	    	return false;
	    }
		
		/*  LEAVE THESE IN UNTIL THIS IS ALL FINISHED SO WE DON'T HAVE TO RUN THE WHOLE PROGRAM
		 * EVERY TIME:
		
		SQL = "ALTER TABLE icitems DROP key uomkey";
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			log.writeEntry(
					sUserName, 
					SMLogEntry.LOG_OPERATION_ICCONVERSION, 
					"Error in SQL: ", 
					SQL + " - " + ex.getMessage()
			);
			m_sErrorMessage = "Error in SQL: " 
				+ SQL  + " - " + ex.getMessage();
			return false;				
		}
		
		//DROP the link fields that are no longer needed:
		SQL = "ALTER TABLE icporeceiptheaders DROP COLUMN rcphseq";
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			log.writeEntry(
					sUserName, 
					SMLogEntry.LOG_OPERATION_ICCONVERSION, 
					"Error dropping PO Receipt Headers column rcphseq", 
					SQL + " - " + ex.getMessage()
			);
			m_sErrorMessage = "Error dropping PO Receipt Headers column rcphseq " 
				+ SQL  + " - " + ex.getMessage();
			return false;				
		}
		
		SQL = "ALTER TABLE icporeceiptheaders DROP COLUMN sponumber";
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			log.writeEntry(
					sUserName, 
					SMLogEntry.LOG_OPERATION_ICCONVERSION, 
					"Error dropping PO Receipt Headers column sponumber", 
					SQL + " - " + ex.getMessage()
			);
			m_sErrorMessage = "Error dropping PO Receipt Headers column sponumber " 
				+ SQL  + " - " + ex.getMessage();
			return false;				
		}
		
		//DROP the link fields that are no longer needed:
		SQL = "ALTER TABLE icpolines DROP key porlseqkey";
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			log.writeEntry(
					sUserName, 
					SMLogEntry.LOG_OPERATION_ICCONVERSION, 
					"Error in SQL: ", 
					SQL + " - " + ex.getMessage()
			);
			m_sErrorMessage = "Error in SQL: " 
				+ SQL  + " - " + ex.getMessage();
			return false;				
		}
		SQL = "ALTER TABLE poporh2 DROP key porhseqkey";
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			log.writeEntry(
					sUserName, 
					SMLogEntry.LOG_OPERATION_ICCONVERSION, 
					"Error in SQL: ", 
					SQL + " - " + ex.getMessage()
			);
			m_sErrorMessage = "Error in SQL: " 
				+ SQL  + " - " + ex.getMessage();
			return false;				
		}

		SQL = "ALTER TABLE icpolines DROP key laccpacporcseqkey";
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			log.writeEntry(
					sUserName, 
					SMLogEntry.LOG_OPERATION_ICCONVERSION, 
					"Error in SQL: ", 
					SQL + " - " + ex.getMessage()
			);
			m_sErrorMessage = "Error in SQL: " 
				+ SQL  + " - " + ex.getMessage();
			return false;				
		}
		
		SQL = "ALTER TABLE icpolines DROP COLUMN porlseq";
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			log.writeEntry(
					sUserName, 
					SMLogEntry.LOG_OPERATION_ICCONVERSION, 
					"Error in SQL: ", 
					SQL + " - " + ex.getMessage()
			);
			m_sErrorMessage = "Error in SQL: " 
				+ SQL  + " - " + ex.getMessage();
			return false;				
		}
		
		//Now we can remove the 'laccpacheaderseq' field from PO Headers:
		SQL = "ALTER TABLE icpoheaders drop column `laccpacheaderseq`";
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			log.writeEntry(
					sUserName, 
					SMLogEntry.LOG_OPERATION_ICCONVERSION, 
					"Error in SQL: ", 
					SQL + " - " + ex.getMessage()
			);
			m_sErrorMessage = "Error in SQL: " 
				+ SQL  + " - " + ex.getMessage();
			return false;				
		}
		SQL = "ALTER TABLE icpoheaders drop column `ponumberkey`";
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			log.writeEntry(
					sUserName, 
					SMLogEntry.LOG_OPERATION_ICCONVERSION, 
					"Error in SQL: ", 
					SQL + " - " + ex.getMessage()
			);
			m_sErrorMessage = "Error in SQL: " 
				+ SQL  + " - " + ex.getMessage();
			return false;				
		}
		//laccpaclinesequence
		//Now we can remove the 'laccpaclinesequence' field from PO Lines:
		SQL = "ALTER TABLE icpolines drop column `laccpaclinesequence`";
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			log.writeEntry(
					sUserName, 
					SMLogEntry.LOG_OPERATION_ICCONVERSION, 
					"Error in SQL: ", 
					SQL + " - " + ex.getMessage()
			);
			m_sErrorMessage = "Error in SQL: " 
				+ SQL  + " - " + ex.getMessage();
			return false;				
		}
		
		SQL = "ALTER TABLE icpolines drop column `laccpacporcseq`";
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			log.writeEntry(
					sUserName, 
					SMLogEntry.LOG_OPERATION_ICCONVERSION, 
					"Error in SQL: ", 
					SQL + " - " + ex.getMessage()
			);
			m_sErrorMessage = "Error in SQL: " 
				+ SQL  + " - " + ex.getMessage();
			return false;				
		}
		SQL = "ALTER TABLE icporeceiptheaders drop column `ponumberkey`";
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			log.writeEntry(
					sUserName, 
					SMLogEntry.LOG_OPERATION_ICCONVERSION, 
					"Error in SQL: ", 
					SQL + " - " + ex.getMessage()
			);
			m_sErrorMessage = "Error in SQL: " 
				+ SQL  + " - " + ex.getMessage();
			return false;				
		}
		SQL = "ALTER TABLE icporeceiptheaders drop column `rcphseq`";
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			log.writeEntry(
					sUserName, 
					SMLogEntry.LOG_OPERATION_ICCONVERSION, 
					"Error in SQL: ", 
					SQL + " - " + ex.getMessage()
			);
			m_sErrorMessage = "Error in SQL: " 
				+ SQL  + " - " + ex.getMessage();
			return false;				
		}
		SQL = "ALTER TABLE icporeceiptlines DROP key porlseqkey";
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			log.writeEntry(
					sUserName, 
					SMLogEntry.LOG_OPERATION_ICCONVERSION, 
					"Error in SQL: ", 
					SQL + " - " + ex.getMessage()
			);
			m_sErrorMessage = "Error in SQL: " 
				+ SQL  + " - " + ex.getMessage();
			return false;				
		}
		SQL = "ALTER TABLE icporeceiptlines DROP key rcphseq";
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			log.writeEntry(
					sUserName, 
					SMLogEntry.LOG_OPERATION_ICCONVERSION, 
					"Error in SQL: ", 
					SQL + " - " + ex.getMessage()
			);
			m_sErrorMessage = "Error in SQL: " 
				+ SQL  + " - " + ex.getMessage();
			return false;				
		}
		SQL = "ALTER TABLE icpolines DROP column porlseq";
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			log.writeEntry(
					sUserName, 
					SMLogEntry.LOG_OPERATION_ICCONVERSION, 
					"Error in SQL: ", 
					SQL + " - " + ex.getMessage()
			);
			m_sErrorMessage = "Error in SQL: " 
				+ SQL  + " - " + ex.getMessage();
			return false;				
		}

		*/
		return true;
	}

	private boolean isStringNumeric(String sTest){
		
		try {
			@SuppressWarnings("unused")
			Long l = new Long(sTest.trim());
		} catch (NumberFormatException e) {
			return false;
		}
		//return sTest.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+");
		return true;
	}
	private String convertACCPACLongDateToString(long lDate, boolean bUseNowForNulls){
		
		if (lDate == 0L){
			if (bUseNowForNulls){
				return clsDateAndTimeConversions.now("yyyy-MM-dd");
			}else{
				return "0000-00-00";
			}
		}
		
		String sDate = Long.toString(lDate);
		return sDate.substring(0, 4) + "-" + sDate.substring(4, 6) + "-" + sDate.substring(6, 8);
	}
	private boolean dropTable(Connection con, SMLogEntry log, String sTable, String sUserID){
	    String SQL = "DROP TABLE IF EXISTS " + sTable;
	    log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Trying to drop " + sTable + " table", "", "[1376509382]");
		try{
		    Statement stmt = con.createStatement();
		    stmt.executeUpdate(SQL);
			arrStatus.add(
					"Successfully dropped table " + sTable + " - " 
					+ clsDateAndTimeConversions.getElapsedTime(lStartingTime) + " elapsed.");
		}catch (Exception ex) {
			//log.writeEntry(sUserName, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Error dropping " + sTable + " table", ex.getMessage());
			//m_sErrorMessage = "Error dropping " + sTable + " table - " + ex.getMessage();
		}

		return true;
	}
	private boolean createTable(
			Connection con, 
			SMLogEntry log,
			String sTable, 
			String sUserID, 
			String SQL
			){
	    log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICCONVERSION, "Trying to create " + sTable + " table", "", "[1376509381]");
		try{
		    Statement stmt = con.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			m_sErrorMessage = "Error creating " + sTable + " table - " + ex.getMessage();
			return false;
		}
		arrStatus.add("Successfully created table " + sTable + " - " 
				+ clsDateAndTimeConversions.getElapsedTime(lStartingTime) + " elapsed.");
		return true;
	}
	private String getRecordsetStringValue (ResultSet rs, String sFieldName){
		
		try {
			if (rs.getString(sFieldName) == null){
				return "";
			}else{
				return rs.getString(sFieldName);
			}
		} catch (SQLException e) {
			return "";
		}
	}
	private boolean writePOLineInstruction(
			long lPORCSEQ, 
			String sInstruction, 
			Connection conn
	){
	
		String SQL = "UPDATE icpolines"
			+ " SET icpolines.sinstructions = '" + clsDatabaseFunctions.FormatSQLStatement(sInstruction) + "'"
			+ " WHERE ("
				+ "(icpolines.laccpacporcseq = " + Long.toString(lPORCSEQ) + ")"
			+ ")"
			;
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			m_sErrorMessage = "Error updating po line instruction with SQL: " + SQL + " - " + ex.getMessage();
			return false;
		}
		return true;
	}
	public String getErrorMessage (){
		return m_sErrorMessage;
	}
	public ArrayList<String> getStatusMessages (){
		return arrStatus;
	}
}
