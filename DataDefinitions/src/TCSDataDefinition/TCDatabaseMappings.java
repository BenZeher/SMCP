package TCSDataDefinition;

public class TCDatabaseMappings {

	//Definitions for table and field mappings:
	
	/*
| ACFunctions                       | 13
| ACGroupFunctions                  | 14
| ACGroups                          | 15
| ACUserGroups                      | 16
| AccessLevels                      | 21
| AnniversaryNotificationRecipients | 17
| Departments                       | 04
| EmployeeAuxiliaryInfo             | 18
| EmployeeMilestones                | 03
| EmployeeStatus                    | 12
| EmployeeTypeAccess                | 19
| EmployeeTypeLinks                 | 20
| EmployeeTypes                     | 02
| Employees                         | 01
| LeaveAdjustmentTypes              | 22
| LeaveAdjustments                  | 23
| ManagerAccessControl              | 24
| Milestones                        | 25
| Notes                             | 26
| PayTypes                          | 11
| SpecialEntryTypes                 | 27
| SpecialNoteTypes                  | 28
| TimeEntries                       | 29
| TimeTotals                        | 30
| companyprofile                    | 31
| loginlinklist                     | 32
| madgicevents                      | 05
| madgiceventtypes                  | 06
| madgiceventusers                  | 07
| rawpunchevents                    | 08
| savedqueries                      | 09
| systemlog                         | 10

ALTER TABLE `madgicevents` CHANGE `lid` `lid` int(11) NOT NULL AUTO_INCREMENT COMMENT '[0501]';

ALTER TABLE `madgiceventtypes` CHANGE `lid` `lid` int(11) NOT NULL AUTO_INCREMENT COMMENT '[0601]';
ALTER TABLE `madgiceventtypes` CHANGE `sname` `sname` varchar(32) NOT NULL DEFAULT '' COMMENT '[0602]';

ALTER TABLE `madgiceventusers` CHANGE `lid` `lid` int(11) NOT NULL AUTO_INCREMENT COMMENT '[0702]';
ALTER TABLE `madgiceventusers` CHANGE `semployeeid` `semployeeid` varchar(9) NOT NULL DEFAULT '' COMMENT '';
ALTER TABLE `madgiceventusers` CHANGE `lmadgiceventid` `lmadgiceventid` int(11) NOT NULL DEFAULT '0' COMMENT '[0501]';

ALTER TABLE `Employees` CHANGE `sEmployeeID` `sEmployeeID` varchar(9) NOT NULL DEFAULT '' COMMENT '[0101]';
ALTER TABLE `Employees` CHANGE `iAccessLevel` `iAccessLevel` int(6) DEFAULT '0' COMMENT '[2101]';
ALTER TABLE `Employees` CHANGE `iDepartmentID` `iDepartmentID` int(11) DEFAULT '0' COMMENT '[0401]';
ALTER TABLE `Employees` CHANGE `iEmployeePayType` `iEmployeePayType` int(11) DEFAULT '0' COMMENT '[1101]';
ALTER TABLE `Employees` CHANGE `iEmployeeStatus` `iEmployeeStatus` int(11) DEFAULT '0' COMMENT '[1201]';
ALTER TABLE `Employees` CHANGE `sEmail` `sEmail` varchar(40) DEFAULT '' COMMENT '[0102]';

ALTER TABLE `AccessLevels` CHANGE `iLevelID` `iLevelID` int(6) NOT NULL DEFAULT '0' COMMENT '[2101]';

ALTER TABLE `Departments` CHANGE `iDeptID` `iDeptID` int(11) NOT NULL DEFAULT '0' COMMENT '[0401]';

ALTER TABLE `PayTypes` CHANGE `iTypeID` `iTypeID` int(11) NOT NULL DEFAULT '0' COMMENT '[1101]';

ALTER TABLE `EmployeeStatus` CHANGE `iStatusID` `iStatusID` int(11) NOT NULL DEFAULT '0' COMMENT '[1201]';

ALTER TABLE `rawpunchevents` CHANGE `id` `id` mediumint(9) NOT NULL AUTO_INCREMENT COMMENT '[0801]';
ALTER TABLE `rawpunchevents` CHANGE `sEmployeeID` `sEmployeeID` varchar(9) DEFAULT '' COMMENT '[0101]';

ALTER TABLE `savedqueries` CHANGE `id` `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '[0901]';
ALTER TABLE `savedqueries` CHANGE `suser` `suser` varchar(128) NOT NULL DEFAULT '' COMMENT '[0101]';

ALTER TABLE `systemlog` CHANGE `id` `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '[1001]';
ALTER TABLE `systemlog` CHANGE `suser` `suser` varchar(128) NOT NULL DEFAULT '' COMMENT '[0101]';

ALTER TABLE `ACFunctions` CHANGE `iFunctionID` `iFunctionID` int(10) NOT NULL DEFAULT '0' COMMENT '[1301]';
ALTER TABLE `ACFunctions` CHANGE `sFunctionName` `sFunctionName` varchar(50) NOT NULL DEFAULT '' COMMENT '[1302]';

ALTER TABLE `ACGroupFunctions` CHANGE `sGroupName` `sGroupName` varchar(50) NOT NULL DEFAULT '' COMMENT '[1502]';
ALTER TABLE `ACGroupFunctions` CHANGE `sFunction` `sFunction` varchar(50) NOT NULL DEFAULT '' COMMENT '[1302]';

ALTER TABLE `ACGroups` CHANGE `iGroupID` `iGroupID` int(10) NOT NULL DEFAULT '0' COMMENT '[1501]';
ALTER TABLE `ACGroups` CHANGE `sGroupName` `sGroupName` varchar(50) NOT NULL DEFAULT '' COMMENT '[1502]';

ALTER TABLE `ACUserGroups` CHANGE `sGroupName` `sGroupName` varchar(50) NOT NULL DEFAULT '' COMMENT '[1502]';
ALTER TABLE `ACUserGroups` CHANGE `sEmployeeID` `sEmployeeID` varchar(9) NOT NULL DEFAULT '' COMMENT '[0101]';

ALTER TABLE `AnniversaryNotificationRecipients` CHANGE `id` `id` smallint(6) NOT NULL AUTO_INCREMENT COMMENT '[1701]';
ALTER TABLE `AnniversaryNotificationRecipients` CHANGE `sEmail` `sEmail` varchar(50) DEFAULT NULL COMMENT '[0102]';

ALTER TABLE `EmployeeAuxiliaryInfo` CHANGE `id` `id` int(11) NOT NULL DEFAULT '0' COMMENT '[1801]';
ALTER TABLE `EmployeeAuxiliaryInfo` CHANGE `sEmployeeID` `sEmployeeID` varchar(9) NOT NULL DEFAULT '' COMMENT '[0101]';

ALTER TABLE `LeaveAdjustmentTypes` CHANGE `iTypeID` `iTypeID` int(11) NOT NULL DEFAULT '0' COMMENT '[2201]';
ALTER TABLE `LeaveAdjustmentTypes` CHANGE `sTypeTitle` `sTypeTitle` varchar(20) DEFAULT '' COMMENT '[2202]';

ALTER TABLE `LeaveAdjustments` CHANGE `id` `id` mediumint(9) NOT NULL AUTO_INCREMENT COMMENT '[2301]';
ALTER TABLE `LeaveAdjustments` CHANGE `sEmployeeID` `sEmployeeID` varchar(9) NOT NULL DEFAULT '' COMMENT '[0101]';
ALTER TABLE `LeaveAdjustments` CHANGE `iLeaveTypeID` `iLeaveTypeID` smallint(6) NOT NULL DEFAULT '0' COMMENT '[2201]';

ALTER TABLE `ManagerAccessControl` CHANGE `id` `id` int(4) NOT NULL AUTO_INCREMENT COMMENT '[2401]';
ALTER TABLE `ManagerAccessControl` CHANGE `sManagerID` `sManagerID` varchar(9) NOT NULL DEFAULT '' COMMENT '[0101]';
ALTER TABLE `ManagerAccessControl` CHANGE `iDepartmentID` `iDepartmentID` int(11) NOT NULL DEFAULT '0' COMMENT '[0401]';

ALTER TABLE `Notes` CHANGE `id` `id` mediumint(9) NOT NULL AUTO_INCREMENT COMMENT '[2601]';
ALTER TABLE `Notes` CHANGE `iLinkID` `iLinkID` mediumint(9) NOT NULL DEFAULT '0' COMMENT '[2901]';
ALTER TABLE `Notes` CHANGE `iNoteTypeID` `iNoteTypeID` int(11) NOT NULL DEFAULT '0' COMMENT '[2801]';

ALTER TABLE `SpecialNoteTypes` CHANGE `iTypeID` `iTypeID` int(11) NOT NULL DEFAULT '0' COMMENT '[2801]';
ALTER TABLE `SpecialNoteTypes` CHANGE `sTypeDesc` `sTypeDesc` text COMMENT '[2802]';
ALTER TABLE `SpecialNoteTypes` CHANGE `sTypeTitle` `sTypeTitle` varchar(20) DEFAULT '' COMMENT '[2803]';

ALTER TABLE `TimeEntries` CHANGE `id` `id` mediumint(9) NOT NULL AUTO_INCREMENT COMMENT '[2901]';
ALTER TABLE `TimeEntries` CHANGE `sEmployeeID` `sEmployeeID` varchar(9) DEFAULT '' COMMENT '[0101]';
ALTER TABLE `TimeEntries` CHANGE `iEntryTypeID` `iEntryTypeID` int(11) DEFAULT '0' COMMENT '[2902]';

ALTER TABLE `TimeTotals` CHANGE `id` `id` mediumint(9) NOT NULL AUTO_INCREMENT COMMENT '[3001]';
ALTER TABLE `TimeTotals` CHANGE `sEmployeeID` `sEmployeeID` varchar(9) DEFAULT '' COMMENT '[0101]';
ALTER TABLE `TimeTotals` CHANGE `datPeriodEndDate` `datPeriodEndDate` datetime DEFAULT '0000-00-00 00:00:00' COMMENT '[3002]';
ALTER TABLE `TimeTotals` CHANGE `sCreatorID` `sCreatorID` varchar(9) DEFAULT '' COMMENT '[0101]';

ALTER TABLE `companyprofile` CHANGE `scompanyname` `scompanyname` varchar(75) NOT NULL DEFAULT '' COMMENT '[3101]';

ALTER TABLE `loginlinklist` CHANGE `id` `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '[3201]';

ALTER TABLE `madgiceventtypes` CHANGE `lid` `lid` int(11) NOT NULL AUTO_INCREMENT COMMENT '[0601]';
ALTER TABLE `madgiceventtypes` CHANGE `sname` `sname` varchar(32) NOT NULL DEFAULT '' COMMENT '[0602]';

ALTER TABLE `madgicevents` CHANGE `lid` `lid` int(11) NOT NULL AUTO_INCREMENT COMMENT '[0501]';
ALTER TABLE `madgicevents` CHANGE `seventtypename` `seventtypename` varchar(32) NOT NULL DEFAULT '' COMMENT '[0602]';
ALTER TABLE `madgicevents` CHANGE `datevent` `datevent` date NOT NULL DEFAULT '0000-00-00' COMMENT '[0502]';

ALTER TABLE `madgiceventusers` CHANGE `lid` `lid` int(11) NOT NULL AUTO_INCREMENT COMMENT '[0701]';
ALTER TABLE `madgiceventusers` CHANGE `semployeeid` `semployeeid` varchar(9) NOT NULL DEFAULT '' COMMENT '[0101]';
ALTER TABLE `madgiceventusers` CHANGE `lmadgiceventid` `lmadgiceventid` int(11) NOT NULL DEFAULT '0' COMMENT '[0501]';

ALTER TABLE `rawpunchevents` CHANGE `id` `id` mediumint(9) NOT NULL AUTO_INCREMENT COMMENT '[0801]';
ALTER TABLE `rawpunchevents` CHANGE `sEmployeeID` `sEmployeeID` varchar(9) DEFAULT '' COMMENT '[0101]';
ALTER TABLE `rawpunchevents` CHANGE `iEntryType` `iEntryType` smallint(6) DEFAULT '0' COMMENT '[0802]';

ALTER TABLE `savedqueries` CHANGE `id` `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '[0901]';
ALTER TABLE `savedqueries` CHANGE `suser` `suser` varchar(128) NOT NULL DEFAULT '' COMMENT '[0101]';

ALTER TABLE `systemlog` CHANGE `id` `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '[1001]';
ALTER TABLE `systemlog` CHANGE `datlogdate` `datlogdate` datetime DEFAULT NULL COMMENT '[1002]';
ALTER TABLE `systemlog` CHANGE `suser` `suser` varchar(128) NOT NULL DEFAULT '' COMMENT '[0101]';
ALTER TABLE `systemlog` CHANGE `soperation` `soperation` varchar(128) DEFAULT NULL COMMENT '[1003]';

ALTER TABLE `Milestones` CHANGE `lid` `lid` int(11) NOT NULL AUTO_INCREMENT COMMENT '[2501]';
ALTER TABLE `Milestones` CHANGE `sEmployeeTypeID` `sEmployeeTypeID` varchar(9) NOT NULL DEFAULT '' COMMENT '[[040201]]';

ALTER TABLE `EmployeeTypes` CHANGE `lid` `lid` int(11) NOT NULL AUTO_INCREMENT COMMENT '[0201]';

ALTER TABLE `SpecialEntryTypes` CHANGE `iTypeID` `iTypeID` int(11) NOT NULL DEFAULT '0' COMMENT '[2701]';
	 */
	
}
