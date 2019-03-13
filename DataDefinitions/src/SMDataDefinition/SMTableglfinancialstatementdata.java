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
