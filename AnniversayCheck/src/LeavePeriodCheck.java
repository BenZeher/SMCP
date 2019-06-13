/*     */ import java.io.BufferedReader;
/*     */ import java.io.FileReader;
/*     */ //import java.io.PrintStream;
/*     */ import java.math.BigDecimal;
/*     */ import java.sql.Connection;
/*     */ import java.sql.DriverManager;
/*     */ import java.sql.ResultSet;
/*     */ import java.sql.Statement;
/*     */ import java.sql.Timestamp;
/*     */ import java.text.SimpleDateFormat;
/*     */ import java.util.ArrayList;
import java.util.Calendar;
/*     */ 
/*     */ public class LeavePeriodCheck
/*     */ {
/*     */   public static void main(String[] paramArrayOfString)
/*     */   {
/*     */     try
/*     */     {
/*  18 */       String sDatabaseURL = "127.0.0.1"; //str1
/*  19 */       String sDatabaseName = "Database";  //str2
/*  20 */       String sUserName = "UserName";  //str3
/*  21 */       String sPassword = "P@ssword";  //str4
/*  22 */       String sCompanyName = "Company";   //str5
/*     */ 
/*  25 */       BufferedReader brConffile = new BufferedReader(new FileReader(paramArrayOfString[0]));
/*     */ 
/*     */       String s;
/*  27 */       while ((s = brConffile.readLine()) != null)
/*     */       {
/*  29 */         if (s.startsWith("DatabaseURL")) {
/*  30 */           sDatabaseURL = s.substring(s.indexOf("=") + 1);
/*  31 */           System.out.println("DatabaseURL = " + sDatabaseURL); }
/*  32 */         if (s.startsWith("DatabaseName")) {
/*  33 */           sDatabaseName = s.substring(s.indexOf("=") + 1);
/*  34 */           System.out.println("DatabaseName = " + sDatabaseName); }
/*  35 */         if (s.startsWith("UserName")) {
/*  36 */           sUserName = s.substring(s.indexOf("=") + 1);
/*  37 */           System.out.println("UserName = " + sUserName); }
/*  38 */         if (s.startsWith("Password")) {
/*  39 */           sPassword = s.substring(s.indexOf("=") + 1);
/*  40 */           System.out.println("Password = " + sPassword); }
/*  41 */         if (s.startsWith("CompanyName")){
/*  42 */         	sCompanyName = s.substring(s.indexOf("=") + 1);
/*  43 */         	System.out.println("CompanyName = " + sCompanyName);
					}
/*     */       }
/*     */ 
/*  46 */       brConffile.close();
/*     */       Connection conn;
/*     */       try
/*     */       {
/*  57 */         conn = DriverManager.getConnection("jdbc:mysql://" + sDatabaseURL + ":3306/" + sDatabaseName + "?noDatetimeStringSync=true&connectTimeout=28800000&interactiveClient=True", sUserName, sPassword);
/*     */       }
/*     */       catch (Exception ex) {
/*  60 */         Class.forName("com.mysql.jdbc.Driver").newInstance();
/*  61 */         conn = DriverManager.getConnection("jdbc:mysql://" + sDatabaseURL + ":3306/" + sDatabaseName + "?noDatetimeStringSync=true&connectTimeout=28800000&interactiveClient=True", sUserName, sPassword);
/*     */       }
/*  63 */       Statement statement1 = conn.createStatement();
/*  63 */       Statement statement2 = conn.createStatement();
/*  63 */       Statement statement3 = conn.createStatement();
/*  63 */       Statement statement4 = conn.createStatement();
/*     */ 
/*  68 */       Timestamp tsNow = new Timestamp(System.currentTimeMillis()); //localTimeStamp1
/*  69 */       Calendar cYesterday = Calendar.getInstance(); //localCalendar1
/*  70 */       cYesterday.setTimeInMillis(System.currentTimeMillis() - 86400000L);
/*     */ 
/*  72 */       System.out.println("Leave Check #" + cYesterday.getTime().toString());
/*     */ 
/*  74 */       SimpleDateFormat sdfSQLDate = new SimpleDateFormat("yyyy-MM-dd");
/*     */ 
/*  77 */       String sSQL = "SELECT * FROM Employees WHERE iActive=1";
				System.out.println("1. " + sSQL);
/*  78 */       ResultSet rsEmployeeList = statement1.executeQuery(sSQL);
/*     */ 
/*  81 */       sSQL = "SELECT * FROM LeaveAdjustmentTypes";
				System.out.println("2. " + sSQL);
/*  82 */       ResultSet rsLeaveTypes = statement2.executeQuery(sSQL);
				if (rsLeaveTypes.next()){
					System.out.println("Got leave type records.");
				}else{
					System.out.println("Didn't get leave type records.");
				}
/*     */ 
/*  84 */       while (rsEmployeeList.next()) {
/*  85 */         	System.out.println("Employee: " + rsEmployeeList.getString("sEmployeeID") + " - " + rsEmployeeList.getString("sEmployeeFirstName") + " " + rsEmployeeList.getString("sEmployeeLastName"));
/*  86 */         	rsLeaveTypes.beforeFirst();
				  	System.out.println("1");
/*     */         	while (rsLeaveTypes.next()){
					  	System.out.println("2");
	/*  90 */           if (EvaluateEligibility(rsLeaveTypes.getDate("dtEffectiveDate"), 
												rsLeaveTypes.getInt("iEligibleEmployeePayType"), 
												rsEmployeeList.getInt("iEmployeePayType"), 
												rsLeaveTypes.getInt("iEligibleEmployeeStatus"), 
												rsEmployeeList.getInt("iEmployeeStatus"), 
												rsLeaveTypes.getDouble("dMinimumHourWorked"), 
												rsEmployeeList.getDouble("dWorkHour"))){
		
							System.out.println("3");
		/*     */ 
		/*  95 */           double dPositiveDuration = 0.0D; //d1
		/*  96 */           double dNegativeDuration = 0.0D; //d2
						    //localTimestamp2
		/*  97 */           Timestamp tsPeriodStartDate = FindStartDate(rsLeaveTypes.getString("sAwardPeriod"), 
																		new java.sql.Date(tsNow.getTime()), 
																		rsEmployeeList.getDate("datHiredDate")); 
		/*     */ 
		/* 108 */           Calendar cPeriodStartDate = Calendar.getInstance();
		/* 109 */           cPeriodStartDate.setTimeInMillis(tsPeriodStartDate.getTime()); //localCalendar2
		/* 110 */           Calendar cNow = Calendar.getInstance();
		/* 111 */           cNow.setTimeInMillis(tsNow.getTime()); //localCalendar3
		/*     */ /*
		                    if ((cPeriodStartDate.get(Calendar.MONTH) == cNow.get(Calendar.MONTH)) && 
								(cPeriodStartDate.get(Calendar.DAY_OF_MONTH) == cNow.get(Calendar.DAY_OF_MONTH))){
			
							  tsPeriodStartDate = FindStartDate(rsLeaveTypes.getString("sAwardPeriod"), new java.sql.Date(cYesterday.getTimeInMillis()), rsEmployeeList.getDate("datHiredDate")); */
		/*     */ 
		/* 121 */             System.out.println("Start Date #2: " + tsPeriodStartDate.toString());
		/*     */ 
		/* 124 */             sSQL = "SELECT * FROM " +
										"LeaveAdjustmentTypes, " +
										"LeaveLumpSumRules " +
									 "WHERE " +
									 	"LeaveAdjustmentTypes.iTypeID = LeaveLumpSumRules.iTypeID " +
									 	"AND " +
									 	"LeaveLumpSumRules.iTypeID = '" + rsLeaveTypes.getInt("iTypeID") + "' " + 
									 "ORDER BY " + 
									 	"LeaveLumpSumRules.dNumberOfMonths";
		/*     */ 
		/* 131 */             System.out.println("Particular leave adjustment type info = " + sSQL);
		/* 132 */             ResultSet rsLeaveAdjustmentTypes = statement3.executeQuery(sSQL); //localResultSet3
		/*     */ 			  
							  //d3  find out how many hours does this employee entitled to.
		/* 135 */             double dLeaveTotal = RoundHalfUp(CalculateLeaveTotal(rsLeaveTypes.getString("sAwardPeriod"), 
																				   rsLeaveTypes.getDouble("dAwardType"), 
																				   rsEmployeeList.getDate("datHiredDate"), 
																				   rsLeaveAdjustmentTypes, 
																				   new java.sql.Date(cYesterday.getTimeInMillis())), 
																2);
		/*     */ 			  //now find out how many hours have been used/gained
		/* 146 */             sSQL = "SELECT * " +
									 "FROM " +
									 	"LeaveAdjustments, " +
									 	"LeaveAdjustmentTypes " +
									 "WHERE " +
									 	"LeaveAdjustments.iLeaveTypeID = LeaveAdjustmentTypes.iTypeID AND " +
									 	"LeaveAdjustments.sEmployeeID = '" + rsEmployeeList.getString("sEmployeeID") + "' " + 
									 	"AND " + 
									 	"LeaveAdjustments.dtInTime >= '" + sdfSQLDate.format(new java.sql.Date(tsPeriodStartDate.getTime())) + "' " + 
									 	"AND " + 
									 	"LeaveAdjustments.dtOutTime <= '" + sdfSQLDate.format(cYesterday.getTime()) + "' " + 
									 	"AND " +
									 	"LeaveAdjustments.iLeaveTypeID = " + rsLeaveTypes.getInt("iTypeID");
		/*     */ 
		/* 156 */             System.out.println("Leave adjustment records = " + sSQL);
		/* 157 */             ResultSet rsLeaveAdjustments = statement4.executeQuery(sSQL); //localResultSet4
		/*     */ 
		/* 159 */             while (rsLeaveAdjustments.next())
		/*     */             {
		/* 161 */               double dAdjustmentDuration = rsLeaveAdjustments.getDouble("dDuration"); //d4
		/* 162 */               if (dAdjustmentDuration < 0.0D)
		/* 163 */                 dNegativeDuration += dAdjustmentDuration;
		/*     */               else {
		/* 165 */                 dPositiveDuration += dAdjustmentDuration;
		/*     */               }
		/*     */             }
		/* 168 */             rsLeaveAdjustments.close();
		/* 169 */             System.out.println("Accumulated Hour for last year = " + dLeaveTotal);
		/* 170 */             double dAdjustmentDuration = RoundHalfUp(dPositiveDuration + dNegativeDuration, 2);
		/* 171 */             System.out.println("Logged Hour from last year = " + dAdjustmentDuration);
		/*     */ 
		/* 173 */             double dRemainingHours = dLeaveTotal - dAdjustmentDuration; //d5
		/* 174 */             System.out.println("Remaining Hour from last year = " + dRemainingHours);
		/*     */ 
		/* 176 */             if (dRemainingHours > 0.0D){
			
		/* 178 */               sSQL = "INSERT INTO LeaveAdjustments( " +
											"sEmployeeID, " +
											"iLeaveTypeID, " +
											"dtInTime, " +
											"dtOutTime, " +
											"dDuration, " +
											"iSpecialAdjustment, " +
											"mNote " +
											")VALUES( " +
											"'" + rsEmployeeList.getString("sEmployeeID") + "'," + 
											" " + rsLeaveTypes.getInt("iTypeID") + "," + 
											" '" + sdfSQLDate.format(cYesterday.getTime()) + "'," + 
											" '" + sdfSQLDate.format(cYesterday.getTime()) + "'," + 
											" " + dRemainingHours + ", " + 
											" 1, " + 
											" 'Adjustment Entry: Un-Used Time Removed.')";
		/*     */ 
		/* 195 */               System.out.println("sSQL #5 = " + sSQL);
		/* 196 */               //statement.execute(sSQL);
		/*     */ 
		/* 199 */               if (rsLeaveTypes.getInt("iCarriedOver") == 1){
		/* 201 */                 rsLeaveAdjustmentTypes.beforeFirst();
		/* 202 */                 dLeaveTotal = RoundHalfUp(CalculateLeaveTotal(rsLeaveTypes.getString("sAwardPeriod"), 
																				rsLeaveTypes.getDouble("dAwardType"), 
																				rsEmployeeList.getDate("datHiredDate"), 
																				rsLeaveAdjustmentTypes, 
																				new java.sql.Date(tsNow.getTime())), 
															2);
		/*     */ 
		/* 208 */                 if ((dLeaveTotal + dRemainingHours > rsLeaveTypes.getDouble("dMaximumHourAvailable")) && (rsLeaveTypes.getDouble("dMaximumHourAvailable") != 0.0D))
		/*     */                 {
		/* 211 */                   double dAvailableHours = rsLeaveTypes.getDouble("dMaximumHourAvailable") - dLeaveTotal; //d6
		/*     */ 
		/* 213 */                   sSQL = "INSERT INTO LeaveAdjustments( sEmployeeID, iLeaveTypeID, dtInTime, dtOutTime, dDuration, iSpecialAdjustment, mNote )VALUES( '" + rsEmployeeList.getString("sEmployeeID") + "'," + " " + rsLeaveTypes.getInt("iTypeID") + "," + " '" + sdfSQLDate.format(new java.sql.Date(tsNow.getTime())) + "'," + " '" + sdfSQLDate.format(new java.sql.Date(tsNow.getTime())) + "'," + " " + (0.0D - dAvailableHours) + ", " + " 1," + " 'Adjustment Entry: " + dAvailableHours + " of " + dRemainingHours + " Un-Used Time Carried Over due to exceeding Maximum Time Allowed.')";
		/*     */ 
		/* 230 */                   System.out.println("sSQL #6.1 = " + sSQL);
		/*     */                 }
		/*     */                 else {
		/* 233 */                   sSQL = "INSERT INTO LeaveAdjustments( sEmployeeID, iLeaveTypeID, dtInTime, dtOutTime, dDuration, iSpecialAdjustment, mNote )VALUES( '" + rsEmployeeList.getString("sEmployeeID") + "'," + " " + rsLeaveTypes.getInt("iTypeID") + "," + " '" + sdfSQLDate.format(new java.sql.Date(tsNow.getTime())) + "'," + " '" + sdfSQLDate.format(new java.sql.Date(tsNow.getTime())) + "'," + " " + (0.0D - dRemainingHours) + ", " + " 1," + " 'Adjustment Entry: Un-Used Time Carried Over.')";
		/*     */ 
		/* 250 */                   System.out.println("sSQL #6.2 = " + sSQL);
		/*     */                 }
		/* 252 */                 //statement.execute(sSQL);
		/*     */               }
		/* 254 */             } else if (dRemainingHours < 0.0D){
		/* 256 */               System.out.println("There are over used time.");
		/*     */             }
		/* 258 */             rsLeaveAdjustmentTypes.close();
	/*     */             }
/*     */			 }
/*     */       }
/*     */ 
/* 268 */       rsEmployeeList.close();
/* 269 */       rsLeaveTypes.close();
/* 270 */       statement1.close();
/* 270 */       statement2.close();
/* 270 */       statement3.close();
/* 270 */       statement4.close();
/* 274 */       conn.close();
		}catch (Exception localException1){
/* 278 */       System.out.println("Exception in LeavePeriodCheck!!");
/* 279 */       System.out.println("Exception.toString(): " + localException1.toString());
/*     */}
	}
/*     */ 
/*     */   @SuppressWarnings("deprecation")
private static double CalculateLeaveTotal(String sAwardPeriod,  			//paramString "employeeyear"
													  double dAwardType, 				//paraDouble  "-1"
													  java.sql.Date datHiredDate, 		//paraDate1
													  ResultSet rsLeaveAdjustmentTypes, //paraResultSet
													  java.sql.Date datYesterday)		//paraDate2
/*     */   {
/*     */     try{
	
/* 292 */       double dNumberOfHours = 0.0D; //d1
/*     */ 
/* 294 */       Calendar cYesterday = Calendar.getInstance(); //localCalendar1
/* 295 */       cYesterday.setTime(datYesterday);
/*     */ 
/* 297 */       Timestamp tsStartDate = FindStartDate(sAwardPeriod, datYesterday, datHiredDate); //localTimestamp1
/*     */       Calendar cHireDate = Calendar.getInstance();

/* 302 */       if (dAwardType >= 0.0D){ //this means the award type is continuous accumulation.
/*     */         int i;
/* 307 */         if (tsStartDate.getMonth() <= 1) {
/* 308 */           cHireDate.set(cYesterday.get(Calendar.YEAR), 11, 31);
/* 309 */           i = cHireDate.get(Calendar.DAY_OF_YEAR);
/*     */         } else {
/* 311 */           cHireDate.set(cYesterday.get(Calendar.YEAR) + 1, 11, 31);
/* 312 */           i = cHireDate.get(Calendar.DAY_OF_YEAR);
/*     */         }
/*     */ 
/* 317 */         if (datHiredDate.compareTo(cHireDate.getTime()) > 0)
/* 318 */           dNumberOfHours = dAwardType * (cYesterday.getTimeInMillis() - datHiredDate.getTime()) / 86400000.0D / i;
/*     */         else {
/* 320 */           dNumberOfHours = dAwardType * (cYesterday.getTimeInMillis() - tsStartDate.getTime()) / 86400000.0D / i;
/*     */         }
/*     */ 
/*     */       }else{ //this means the award type is lumpsum gain
/* 328 */         cHireDate.setTime(datHiredDate);
/*     */ 		 
				//d2
/* 330 */         double dEmployeedMonth = (cYesterday.get(Calendar.YEAR) - cHireDate.get(Calendar.YEAR)) * 12 + cYesterday.get(Calendar.MONTH) - cHireDate.get(Calendar.MONTH);
/* 331 */         if (cYesterday.get(Calendar.DAY_OF_MONTH) < cHireDate.get(Calendar.DAY_OF_MONTH)) {
/* 332 */           dEmployeedMonth -= 1.0D;
/*     */         }
/*     */ 
/* 337 */         ArrayList<Double> alNumberOfMonth = new ArrayList<Double>(0); //localArrayList1
/* 338 */         ArrayList<Double> alNumberOfHours = new ArrayList<Double>(0); //localArrayList2
/*     */ 
/* 341 */         while (rsLeaveAdjustmentTypes.next()){
/* 353 */           System.out.println("rule: " + rsLeaveAdjustmentTypes.getDouble("dNumberOfMonths") + " months of service get " + rsLeaveAdjustmentTypes.getDouble("dNumberOfHours") + " hours of leave time.");
/* 354 */           if (rsLeaveAdjustmentTypes.getDouble("dNumberOfMonths") > dEmployeedMonth) break;
/* 355 */           dNumberOfHours = rsLeaveAdjustmentTypes.getDouble("dNumberOfHours"); //d1
/*     */ 
/* 357 */           alNumberOfMonth.add(Double.valueOf(rsLeaveAdjustmentTypes.getDouble("dNumberOfMonths")));
/* 358 */           alNumberOfHours.add(Double.valueOf(rsLeaveAdjustmentTypes.getDouble("dNumberOfHours")));
/*     */         }
/*     */ 
/* 382 */         System.out.println("Total number of step-ups: " + alNumberOfMonth.size());
/* 383 */         for (int j = 0; j < alNumberOfMonth.size(); ++j) {
						//localTimestamp2
	/* 384 */           Timestamp tsStepUPTime = Locate_Stepup_Time(new Timestamp(datHiredDate.getTime()), 
																	Double.parseDouble(alNumberOfMonth.get(j).toString()));
	/*     */ 
	/* 391 */           if (tsStepUPTime.getTime() > tsStartDate.getTime()){
							//this mean a step up event happened within this period, therefore needs to be considered differently
		/* 406 */           dNumberOfHours = Get_Leave_Length(datHiredDate,
															  dNumberOfHours,
															  alNumberOfMonth, 
															  alNumberOfHours, 
															  tsStartDate);
		/*     */ 
		/* 413 */           break;
	/*     */           }else{
							//This means the step up happened before the beginning of this persiod, therefore just use the hour on record.
							dNumberOfHours = (Double) alNumberOfHours.get(j);
							alNumberOfMonth.remove(j);
							alNumberOfHours.remove(j);
						}
/*     */         	}
/*     */       }
/*     */ 
/* 430 */       return dNumberOfHours;
/*     */     }
/*     */     catch (Exception localException) {
/* 433 */       System.out.println("[1560447031] Error in EmployeeLeaveManager.CalculateLeaveTotal!!<BR>");
/* 434 */       System.out.println("[1560447031] Exception: " + localException.getMessage() + "<BR>"); }
/* 435 */     return 0.0D;
/*     */   }
/*     */ 
/*     */   private static Timestamp FindStartDate(String sAwardPeriod, 		//paramString 
												   java.sql.Date datYesterday,  //paramDate1
												   java.sql.Date datHiredDate) 	//paramDate2
/*     */   {
/* 441 */     Calendar cYesterday = Calendar.getInstance();
/* 442 */     cYesterday.setTime(datYesterday); //localCalendar1
/*     */     Timestamp tsTemp; //localTimestamp;
/* 444 */     if (sAwardPeriod.compareTo("calendaryear") == 0){
/* 447 */       tsTemp = Timestamp.valueOf(cYesterday.get(Calendar.YEAR) + "-01-01 00:00:00");
/*     */ 
/* 450 */       if (tsTemp.getTime() < datHiredDate.getTime())
/* 451 */         tsTemp = new Timestamp(datHiredDate.getTime());
/*     */     }else if (sAwardPeriod.compareTo("employeeyear") == 0){
/* 456 */       Calendar cHiredDate = Calendar.getInstance();
/* 457 */       cHiredDate.setTime(datHiredDate); //localCalendar2
/* 458 */       String str = "";
/*     */ 
/* 461 */       if (cYesterday.get(Calendar.MONTH) < cHiredDate.get(Calendar.MONTH))
/*     */       {
/* 463 */         str = str + (cYesterday.get(Calendar.YEAR) - 1);
/* 464 */       } else if (cYesterday.get(Calendar.MONTH) > cHiredDate.get(Calendar.MONTH))
/*     */       {
/* 466 */         str = str + cYesterday.get(Calendar.YEAR);
/* 467 */       } else if (cYesterday.get(Calendar.MONTH) == cHiredDate.get(Calendar.MONTH)) {
/* 468 */         if (cYesterday.get(Calendar.DAY_OF_MONTH) < cHiredDate.get(Calendar.DAY_OF_MONTH))
/*     */         {
/* 470 */           str = str + (cYesterday.get(Calendar.YEAR) - 1);
/*     */         }
/*     */         else {
/* 473 */           str = str + cYesterday.get(Calendar.YEAR);
/*     */         }
/*     */       }
/* 476 */       if (cHiredDate.get(Calendar.MONTH) + 1 < 10)
/* 477 */         str = str + "-0" + (cHiredDate.get(Calendar.MONTH) + 1);
/*     */       else {
/* 479 */         str = str + "-" + (cHiredDate.get(Calendar.MONTH) + 1);
/*     */       }
/* 481 */       if (cHiredDate.get(Calendar.DAY_OF_MONTH) < 10)
/* 482 */         str = str + "-0" + cHiredDate.get(Calendar.DAY_OF_MONTH);
/*     */       else {
/* 484 */         str = str + "-" + cHiredDate.get(Calendar.DAY_OF_MONTH);
/*     */       }
/*     */ 
/* 487 */       tsTemp = Timestamp.valueOf(str + " 00:00:00");
/*     */     }
/*     */     else {
/* 490 */       tsTemp = new Timestamp(System.currentTimeMillis());
/*     */     }
/*     */ 
/* 493 */     return tsTemp;
/*     */   }
/*     */ 
/*     */   private static double RoundHalfUp(double paramDouble, int paramInt) {
/* 497 */     BigDecimal localBigDecimal = new BigDecimal(paramDouble);
/* 498 */     localBigDecimal = localBigDecimal.setScale(paramInt, 4);
/* 499 */     paramDouble = localBigDecimal.doubleValue();
/* 500 */     return paramDouble;
/*     */   }
/*     */ 
/*     */   private static boolean EvaluateEligibility(java.sql.Date dtEffectiveDate, 	// paramDate, 
													   int iEligibleEmployeePayType, 	//paramInt1, 
													   int iEmployeePayType, 			//paramInt2, 
													   int iEligibleEmployeeStatus, 	//paramInt3, 
													   int iEmployeeStatus, 			//paramInt4, 
													   double dMinimumHourWorked, 		//paramDouble1, 
													   double dWorkHour) 				//paramDouble2)
/*     */   {
/*     */     try
/*     */     {
/*     */ 
/* 513 */       if (dtEffectiveDate.getTime() > System.currentTimeMillis()) {
/* 514 */         return false;
/*     */       }
/*     */ 
/* 519 */       if (!(TypeDirectCheck(iEligibleEmployeePayType, iEmployeePayType)))
/*     */       {
/* 521 */         return false;
/*     */       }
/*     */ 
/* 528 */       if (!(TypeDirectCheck(iEligibleEmployeeStatus, iEmployeeStatus)))
/*     */       {
/* 530 */         return false;
/*     */       }
/*     */ 
/* 535 */       if (dMinimumHourWorked > dWorkHour) {
/* 536 */         return false;
/*     */       }
/*     */ 
/* 541 */       return true;
/*     */     }
/*     */     catch (Exception localException) {
/* 544 */       System.out.println("<BR><BR>Error in EmployeeLeaveManager.EvaluateEligibility!!");
/* 545 */       System.out.println("Exception: " + localException.getMessage()); }
/* 546 */     return false;
/*     */   }
/*     */ 
/*     */   private static boolean TypeDirectCheck(int paramInt1, int paramInt2){
/* 553 */     return ((paramInt1 ^ paramInt2) < paramInt1);
/*     */   }
/*     */ 
/*     */   private static double Get_Leave_Length(java.sql.Date datHiredDate,
												   double dStartLength,
												   ArrayList<Double> alNumberOfMonth, 	//paramArrayList1, 
												   ArrayList<Double> alNumberOfHours, 	//paramArrayList2, 
												   Timestamp tsStartDate){ 		//paramTimestamp2)
				int i = 0;
/* 584 */       double dCurrentStepHour = dStartLength; //Double.parseDouble(alNumberOfHours.get(i).toString());
			  	Timestamp tsNextStepUpDate = Locate_Stepup_Time(new Timestamp(datHiredDate.getTime()), 
			  													   Double.parseDouble(alNumberOfMonth.get(i).toString()));
/* 585 */       double dLength = 0.0D; //d2
/* 587 */       Calendar cDatePointer = Calendar.getInstance(); //localCalendar
/*     */ 	    cDatePointer.setTimeInMillis(tsStartDate.getTime());
				
				while (cDatePointer.getTimeInMillis() < System.currentTimeMillis()) {
					while (cDatePointer.getTimeInMillis() < tsNextStepUpDate.getTime()){
						if (IsLeapYear(cDatePointer.get(Calendar.YEAR)))
							dLength += dCurrentStepHour / 366.0D;
						else{
							dLength += dCurrentStepHour / 365.0D;
						}
						cDatePointer.setTimeInMillis(cDatePointer.getTimeInMillis() + 86400000L);
					}
					if (cDatePointer.getTimeInMillis() < System.currentTimeMillis()){
						dCurrentStepHour = Double.parseDouble(alNumberOfHours.get(i).toString());
						try{
							tsNextStepUpDate = Locate_Stepup_Time(new Timestamp(datHiredDate.getTime()), 
								   								  Double.parseDouble(alNumberOfMonth.get(i++).toString()));
						}catch (Exception e){
							//hit the end of number of month array.
							tsNextStepUpDate = new Timestamp (System.currentTimeMillis());
						}
					}
				}
				return dLength;
			}
/*     */   private static Timestamp Locate_Stepup_Time(Timestamp tsHiredDate, //paramTimestamp
													    double dNOofMonth)     //paramDouble
														{
/* 653 */     Calendar c = Calendar.getInstance();
/* 654 */     c.setTimeInMillis(tsHiredDate.getTime());
/* 655 */     c.add(Calendar.MONTH, (int)dNOofMonth);
/*     */ 
/* 657 */     c.add(Calendar.DAY_OF_MONTH, (int)((dNOofMonth - (int)dNOofMonth) * 30.4375D));
/*     */ 
/* 659 */     Timestamp ts = new Timestamp(c.getTimeInMillis());
/* 660 */     System.out.println("Step up date: " + ts.toString());
/*     */ 
/* 662 */     return ts;
/*     */   }
/*     */ 
/*     */   private static boolean IsLeapYear(int iYear)
/*     */   {
/* 672 */     if (iYear % 4 == 0) {
/* 673 */       if (iYear % 100 != 0) {
/* 674 */         return true;
/*     */       }
/* 676 */       return (iYear % 400 == 0);
/*     */     }
/*     */ 
/* 681 */     return false;
/*     */   }
}
