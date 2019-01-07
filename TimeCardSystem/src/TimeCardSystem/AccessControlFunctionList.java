package TimeCardSystem;

public class AccessControlFunctionList {

	/*
	 * this class stores all the functions available in time card program
	 * for admins to designate to each user. It needs to be maintained by 
	 * programmer as codes need to be inserted into program  directly anyway.
	 *  +-------------+---------------------------------------+----------------------------------------------+
		| iFunctionID | sFunctionName                         | sFunctionDesc                                |
		+-------------+---------------------------------------+----------------------------------------------+
		|           1 | EditEmployeeGeneralInformation        | Edit Employee General Information            |
		|           2 | ViewEmployeeGeneralInformation        | View Employee General Information            |
		|           3 | EditEmployeeConfidentialInformation   | Edit Employee Confidential Information       |
		|           4 | ViewEmployeeConfidentialInformation   | View Employee Confidential Information       |
		|           5 | EmployeeLeaveManager                  | Employee Leave Manager                       |
		|           6 | EmployeeLeaveBalanceSummary           | Employee Leave Balance Summary               |
		|           7 | EmployeePerformanceReport             | Employee Performance Report                  |
		|           8 | OnClockEmployeeList                   | On-Clock Employee List                       |
		|           9 | ReviewPendingEmployeeLeaveRequest     | Review Pending Employee Leave Request        |
		|          10 | ReviewApprovedEmployeeLeaveRequest    | Review Approved Employee Leave Request       |
		|          11 | UNExcusedLateReport                   | UN-Excused Late Report                       |
		|          12 | EmployeeFeedbackReview                | Employee Feedback Review                     |
		|          13 | ViewEmployeeContactInformationList    | View Employee Contact Information List       |
		|          14 | ViewOfficeContactInformationList      | View Office Contact Information List         |
		|          15 | ManagerReviewListTimeEditing          | Manager Review List Time Editing             |
		|          16 | ManagerReviewListNoteEditing          | Manager Review List Note Editing             |
		|          17 | PeriodTotalTimeReport                 | Period Total Time Report                     |
		|          18 | PostPeriodTotalTime                   | Post Period Total Time                       |
		|          19 | RawTimeEntryList                      | Raw Time Entry List                          |
		|          20 | SpecialNoteReport                     | Special Note Report                          |
		|          21 | EditEmployeeAuxiliaryInformationField | Edit Employee Auxiliary Information Field    |
		|          22 | ManageUserAccessControl               | Manage User Access Control                   |
		|          23 | EditDepartmentInformation             | Edit Department Information                  |
		|          24 | EditOfficeInformation                 | Edit Office Information                      |
		|          25 | ManagerDepartmentDesignation          | Manager-Department Designation               |
		|          26 | EditTimeEntryType                     | Edit Time Entry Type                         |
		|          27 | EditEmployeeStatus                    | Edit Employee Status                         |
		|          28 | EditPayType                           | Edit Pay Type                                |
		|          29 | EditLeaveAdjustmentType               | Edit Leave Adjustment Type                   |
		|          30 | EditLumpSumRuleForLeaveAdjustmentType | Edit Lump Sum Rule For Leave Adjustment Type |
		|          31 | EditAnniversaryNotificationRecipient  | Edit Anniversary Notification Recipient      |
		|          32 | AnniversaryNotificationRecipient      | Anniversary Notification Recipient           |
		|          33 | LogEmployeeLeaveRequest               | Log Employee Leave Request                   |
		+-------------+---------------------------------------+----------------------------------------------+


	 */
	
	/*  Manage Employees
	 *  Edit Employee Confidential Information
		Edit Employee General Information
		Employee Leave Balance Summary
		Employee Leave Manager
		Employee Performance Report
		On-Clock Employee List
		Review Approved Employee Leave Request (NOT logged)
		Review Pending Employee Leave Request
		UN-Excused Late Report 
	 */

	public final static String EditEmployeeGeneralInformation = "EditEmployeeGeneralInformation";
	public final static String ViewEmployeeGeneralInformation = "ViewEmployeeGeneralInformation";
	public final static String EditEmployeeConfidentialInformation = "EditEmployeeConfidentialInformation";
	public final static String ViewEmployeeConfidentialInformation = "ViewEmployeeConfidentialInformation";
	public final static String EditEmployeeInsuranceInformation = "EditEmployeeInsuranceInformation";
	public final static String ViewEmployeeInsuranceInformation = "ViewEmployeeInsuranceInformation";
	public final static String EmployeeLeaveManager = "EmployeeLeaveManager";
	public final static String EmployeeLeaveBalanceSummary = "EmployeeLeaveBalanceSummary";
	public final static String EmployeePerformanceReport = "EmployeePerformanceReport";
	public final static String OnClockEmployeeList = "OnClockEmployeeList";
	public final static String ReviewPendingEmployeeLeaveRequest = "ReviewPendingEmployeeLeaveRequest";
	public final static String ReviewApprovedEmployeeLeaveRequest = "ReviewApprovedEmployeeLeaveRequest";
	public final static String UNExcusedLateReport = "UNExcusedLateReport";
	
	/*  General Reports
	 *  Employee Feedback Review
		Employee Contact Information
		Office Contact Information 
	 */
	public final static String EmployeeFeedbackReview = "EmployeeFeedbackReview";
	public final static String ViewEmployeeContactInformationList = "ViewEmployeeContactInformationList";
	public final static String ViewOfficeContactInformationList = "ViewOfficeContactInformationList";
	
	//Query tool:
	public final static String MANAGE_QUERIES = "ManageQueries";
	
	/*  Payroll Process
	 *  Manager Review List
		Period Total Time Report
		Post Period Total Time
		Raw Time Entry List
		Special Note Report 
	 */
	public final static String ManagerReviewListTimeEditing = "ManagerReviewListTimeEditing";
	public final static String ManagerReviewListNoteEditing = "ManagerReviewListNoteEditing";
	public final static String PeriodTotalTimeReport = "PeriodTotalTimeReport";
	public final static String PostPeriodTotalTime = "PostPeriodTotalTime";
	public final static String RawTimeEntryList = "RawTimeEntryList";
	public final static String SpecialNoteReport = "SpecialNoteReport";
	public final static String MADGICReport = "MADGICReport";
	
	/*	System Configuration
	 * 
		Edit Employee Auxiliary Information Fields
		Manage User Access Control
		Edit Department Information
		Edit Office Information
		Manager Department Designation
		Edit Time Entry Type
		Edit Employee Status
		Edit Pay Type
		Edit Leave Adjustment Type
		Edit Lump Sum Rules for Leave Adjustment Type
		Edit Anniversary Notification Recipient 
	 */
	public final static String EditEmployeeAuxiliaryInformationField = "EditEmployeeAuxiliaryInformationField";
	public final static String ManageUserAccessControl = "ManageUserAccessControl";
	public final static String EditCompanyProfile = "EditCompanyProfile";
	public final static String EditDepartmentInformation = "EditDepartmentInformation";
	public final static String EditOfficeInformation = "EditOfficeInformation";
	public final static String ManagerDepartmentDesignation = "ManagerDepartmentDesignation";
	public final static String EditTimeEntryType = "EditTimeEntryType";
	public final static String EditEmployeeStatus = "EditEmployeeStatus";
	public final static String EditPayType = "EditPayType";
	public final static String EditLeaveAdjustmentType = "EditLeaveAdjustmentType";
	public final static String EditLumpSumRuleForLeaveAdjustmentType = "EditLumpSumRuleForLeaveAdjustmentType";
	public final static String EditInsurance = "EditInsurance";
	
	//Milestone functions:
	public final static String EditEmployeeTypes = "EditEmployeeTypes";
	public final static String EditMilestones = "EditMilestones";
	public final static String ViewPersonalMilestoneReport = "ViewPersonalMilestoneReport";
	public final static String ViewMilestoneReport = "ViewMilestoneReport";
	
	//MADGIC functions:
	public final static String EditMADGICEventTypes = "EditMADGICEventTypes";
	public final static String EditMADGICEvents = "EditMADGICEvents";
	public final static String ViewPersonalMADGICReport = "ViewPersonalMADGICReport";
	
	//Special rights\status
	//public final static String CreateNewEntry = "CreateNewEntry";
	public final static String AnniversaryNotificationRecipient = "AnniversaryNotificationRecipient";
	public final static String LogEmployeeLeaveRequest = "LogEmployeeLeaveRequest";
	public final static String LeaveRequestRecipient = "LeaveRequestRecipient";
	
	//Permission to the 'Admin' menu:
	public final static String ViewAdministrationMenu = "ViewAdministrationMenu";
	
	/*
	public final static String  = "";
	*/
}
