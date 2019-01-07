/*    */ import java.sql.Timestamp;
/*    */ 
/*    */ public class LeaveTypeTotal
/*    */ {
/*    */   private int id;
/*    */   private String sTitle;
/*    */   private String sDesc;
/*    */   private double dTotalLogged;
/*    */   private double dTotalCredit;
/*    */   private Timestamp tsStartDate;
/*    */ 
/*    */   public int getID()
/*    */   {
/* 15 */     return this.id;
/*    */   }
/*    */ 
/*    */   public String getTitle() {
/* 19 */     return this.sTitle;
/*    */   }
/*    */ 
/*    */   public String getDesc() {
/* 23 */     return this.sDesc;
/*    */   }
/*    */ 
/*    */   public double getTotalLogged() {
/* 27 */     return this.dTotalLogged;
/*    */   }
/*    */ 
/*    */   public void setTotalLogged(double paramDouble) {
/* 31 */     this.dTotalLogged = paramDouble;
/*    */   }
/*    */ 
/*    */   public double getTotalCredit() {
/* 35 */     return this.dTotalCredit;
/*    */   }
/*    */ 
/*    */   public void setTotalCredit(double paramDouble) {
/* 39 */     this.dTotalCredit = paramDouble;
/*    */   }
/*    */ 
/*    */   public Timestamp getStartDate() {
/* 43 */     return this.tsStartDate;
/*    */   }
/*    */ 
/*    */   public void setStartDate(Timestamp paramTimestamp) {
/* 47 */     this.tsStartDate = paramTimestamp;
/*    */   }
/*    */ 
/*    */   public LeaveTypeTotal() {
/* 51 */     this.id = 0;
/* 52 */     this.sTitle = "";
/* 53 */     this.sDesc = "";
/* 54 */     this.dTotalLogged = 0.0D;
/* 55 */     this.dTotalCredit = 0.0D;
/* 56 */     this.tsStartDate = null;
/*    */   }
/*    */ 
/*    */   public LeaveTypeTotal(int paramInt, String paramString1, String paramString2, double paramDouble1, double paramDouble2, Timestamp paramTimestamp)
/*    */   {
/* 65 */     this.id = paramInt;
/* 66 */     this.sTitle = paramString1;
/* 67 */     this.sDesc = paramString2;
/* 68 */     this.dTotalLogged = paramDouble1;
/* 69 */     this.dTotalCredit = paramDouble2;
/* 70 */     this.tsStartDate = paramTimestamp;
/*    */   }
/*    */ }

/* Location:           C:\Users\Li Tong\workspace\AnniversayCheck\src\AnniversaryCheck.jar
 * Qualified Name:     LeaveTypeTotal
 * Java Class Version: 5 (49.0)
 * JD-Core Version:    0.5.3
 */