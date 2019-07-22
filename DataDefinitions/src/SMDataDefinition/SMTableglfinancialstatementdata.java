package SMDataDefinition;

public class SMTableglfinancialstatementdata {

	public static String TableName = "glfinancialstatementdata";
	
	//Field names:
	public static String sacctid = "sacctid";
	public static String ifiscalyear = "ifiscalyear";
	public static String ifiscalperiod = "ifiscalperiod";
	public static String bdnetchangeforperiod = "bdnetchangeforperiod";
	public static String bdnetchangeforperiodpreviousyear = "bdnetchangeforperiodpreviousyear";
	public static String bdtotalyeartodate = "bdtotalyeartodate";
	public static String bdtotalpreviousyeartodate = "bdtotalpreviousyeartodate";
	public static String bdopeningbalancepreviousyear = "bdopeningbalancepreviousyear";
	public static String bdopeningbalance = "bdopeningbalance";
	public static String bdnetchangeforpreviousperiod = "bdnetchangeforpreviousperiod";
	public static String bdnetchangeforpreviousperiodpreviousyear = "bdnetchangeforpreviousperiodpreviousyear";
	
	public static int sacctidLength = 45;
	
	//SMTablefiscalsets fields:
	/*
	public static String sAcctID = "sacctid";
	public static String ifiscalyear = "ifiscalyear";
	public static String bdopeningbalance = "bdopeningbalance";
	public static String bdnetchangeperiod1 = "bdnetchangeperiod1";
	public static String bdnetchangeperiod2 = "bdnetchangeperiod2";
	public static String bdnetchangeperiod3 = "bdnetchangeperiod3";
	public static String bdnetchangeperiod4 = "bdnetchangeperiod4";
	public static String bdnetchangeperiod5 = "bdnetchangeperiod5";
	public static String bdnetchangeperiod6 = "bdnetchangeperiod6";
	public static String bdnetchangeperiod7 = "bdnetchangeperiod7";
	public static String bdnetchangeperiod8 = "bdnetchangeperiod8";
	public static String bdnetchangeperiod9 = "bdnetchangeperiod9";
	public static String bdnetchangeperiod10 = "bdnetchangeperiod10";
	public static String bdnetchangeperiod11 = "bdnetchangeperiod11";
	public static String bdnetchangeperiod12 = "bdnetchangeperiod12";
	public static String bdnetchangeperiod13 = "bdnetchangeperiod13";
	public static String bdnetchangeperiod14 = "bdnetchangeperiod14";
	public static String bdnetchangeperiod15 = "bdnetchangeperiod15";
	*/
	
	/*
CREATE TABLE `glfinancialstatementdata` (
  `sacctid` varchar(45) NOT NULL DEFAULT '' COMMENT '[090101]',
  `ifiscalyear` int(11) NOT NULL DEFAULT '0',
  `ifiscalperiod` int(11) NOT NULL DEFAULT '0',
  `bdnetchangeforperiod` decimal(17,2) NOT NULL DEFAULT '0.00',
  `bdnetchangeforperiodpreviousyear` decimal(17,2) NOT NULL DEFAULT '0.00',
  `bdtotalyeartodate` decimal(17,2) NOT NULL DEFAULT '0.00',
  `bdtotalpreviousyeartodate` decimal(17,2) NOT NULL DEFAULT '0.00',
  `bdopeningbalancepreviousyear` decimal(17,2) NOT NULL DEFAULT '0.00',
  `bdopeningbalance` decimal(17,2) NOT NULL DEFAULT '0.00',
  `bdnetchangeforpreviousperiod` decimal(17,2) NOT NULL DEFAULT '0.00',
  `bdnetchangeforpreviousperiodpreviousyear` decimal(17,2) NOT NULL DEFAULT '0.00',
  PRIMARY KEY (`sacctid`,`ifiscalyear`,`ifiscalperiod`)
) ENGINE=InnoDB
	 */
	
	
}
