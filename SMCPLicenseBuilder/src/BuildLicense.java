

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;

import smcontrolpanel.SMUtilities;

public class BuildLicense {

	public static void main(String args[]) throws InterruptedException {

		ArrayList<String>arrCompanyIDs = new ArrayList<String>(0);
		ArrayList<String>arrModuleLevels = new ArrayList<String>(0);
		ArrayList<String>arrExpirationDates = new ArrayList<String>(0);

		//TEST:
		//String s = "";
		//try {
		//	s = SMUtilities.decryptLicenseLine("123117057052058056060063");
		//} catch (Exception e1) {
		//	// TODO Auto-generated catch block
		//	e1.printStackTrace();
		//}
		//System.out.println(s);
		
		System.out.println("NOTE: You need a folder in your root directory called smcplicense, with a subfolder called WEB-INF before you can run this program.");
		System.out.println("Type 'C' to continue, 'Q' to quit:");

		Scanner reader = new Scanner(System.in);  // Reading from System.in
		String sResponse = reader.nextLine();
		if (sResponse.compareToIgnoreCase("C") != 0){
			reader.close();
			System.exit(0);
		}

		System.out.println("Each server, whether it has one or more company databases in it, gets one license file.");
		System.out.println("Input each pair of company ids and module levels that you need for this server license file.");

		boolean bAddAnotherCompany = true;
		while (bAddAnotherCompany){
			sResponse = "";
			while (sResponse.compareToIgnoreCase("") == 0){
				System.out.println("Enter the company ID:");
				sResponse = reader.nextLine().trim();
				if (sResponse.compareTo("") == 0){
					System.out.println("Company ID cannot be blank");
				}
			}
			arrCompanyIDs.add(sResponse);

			sResponse = "";
			while (sResponse.compareToIgnoreCase("") == 0){
				System.out.println("Enter the module level:");
				sResponse = reader.nextLine().trim();
				try {
					@SuppressWarnings("unused")
					long lModuleLevel = Long.parseLong(sResponse);
				} catch (NumberFormatException e) {
					System.out.println("Invalid module level: '" + sResponse + "'.");
					sResponse = "";
					continue;
				}
				if (sResponse.compareTo("") == 0){
					System.out.println("Module level cannot be blank");
				}
			}
			arrModuleLevels.add(sResponse);
			
			
			sResponse = "";
			while (sResponse.compareToIgnoreCase("") == 0){
				System.out.println("Enter the expiration date in YYYY-MM-DD format:");
				sResponse = reader.nextLine().trim();
				if (!ServletUtilities.clsDateAndTimeConversions.IsValidDateString(SMUtilities.DATE_FORMAT_FOR_SQL, sResponse)){
					System.out.println("Invalid expiration date: '" + sResponse + "'.");
					sResponse = "";
					continue;
				}
				if (sResponse.compareTo("") == 0){
					System.out.println("Expiration date cannot be blank");
				}
			}
			arrExpirationDates.add(sResponse);
			
			System.out.println("Add another company? (Y/N)");
			sResponse = reader.nextLine().trim();
			if (sResponse.compareToIgnoreCase("Y") != 0){
				bAddAnotherCompany = false;
			}
		}

		System.out.println("Company IDs, modules and expiration dates:");
		for (int i = 0; i < arrCompanyIDs.size(); i++){
			System.out.println(arrCompanyIDs.get(i) + "," + arrModuleLevels.get(i) + "," + arrExpirationDates.get(i));
		}
		
		//If all goes well, then we 'encrypt' by converting each character to ascii values and add a constant:
		String sFullLicenseFileName =
			System.getProperty("file.separator")
			+ SMUtilities.SMCP_LICENSE_DIRECTORY
			+ System.getProperty("file.separator")
			+ SMUtilities.SMCP_LICENSE_SUBDIRECTORY
			+ System.getProperty("file.separator")
			+ SMUtilities.SMCP_LICENSE_FILE
			;
		try {
			writeLicenseFile (sFullLicenseFileName, arrCompanyIDs, arrModuleLevels, arrExpirationDates);
		} catch (Exception e) {
			System.out.println("Error writing license file - " + e.getMessage());
			reader.close();
			System.exit(0);
		}
		reader.close();
		
		System.out.println("Successfully created license file:");
		System.out.println("Going to create WAR file now...");
		
		//Remove any previous WAR file:
		String sCommand = "rm "
				+ System.getProperty("file.separator")
				+ SMUtilities.SMCP_LICENSE_DIRECTORY
				+ System.getProperty("file.separator")
				+ SMUtilities.SMCP_LICENSE_WAR_FILE
		;
		@SuppressWarnings("unused")
		String sCommandResult = "";
		try {
			sCommandResult = executeSystemCommand(sCommand);
		} catch (Exception e) {
			//Don't stop for this...
			//System.out.println("Error removing previous WAR file - " + e.getMessage());
			//System.exit(0);
		}	
		
		//Create the new WAR file:
		sCommand = 
			"jar -cvfM " 
			+ System.getProperty("file.separator")
			+ SMUtilities.SMCP_LICENSE_DIRECTORY
			+ System.getProperty("file.separator")
			+ SMUtilities.SMCP_LICENSE_WAR_FILE 
			+ " -C "
			+ System.getProperty("file.separator")
			+ SMUtilities.SMCP_LICENSE_DIRECTORY
			+ " "
			+ SMUtilities.SMCP_LICENSE_SUBDIRECTORY
		;
		try {
			sCommandResult = executeSystemCommand(sCommand);
		} catch (Exception e) {
			System.out.println("Error creating WAR file - " + e.getMessage());
			System.exit(0);
		}
		
		System.out.println("WAR file '" 
			+ System.getProperty("file.separator")
			+ SMUtilities.SMCP_LICENSE_DIRECTORY
			+ System.getProperty("file.separator")
			+ SMUtilities.SMCP_LICENSE_WAR_FILE 
			+ "/"
			+ " was created successfully."
		);
		
		System.exit(0);
	}
	private static void writeLicenseFile (
			String sLicenseFileName, 
			ArrayList<String>arrCompanyIDS, 
			ArrayList<String>arrModuleLevels,
			ArrayList<String>arrExpirationDates
			) throws Exception{
		
		BufferedWriter bw = null;
		try {
			// OVERWRITE MODE SET HERE
			bw = new BufferedWriter(new FileWriter(sLicenseFileName, false));
			for (int i = 0; i < arrCompanyIDS.size(); i++){
				String sEncryptedLine = SMUtilities.encryptLicenseLine(arrCompanyIDS.get(i), arrModuleLevels.get(i), arrExpirationDates.get(i));
				bw.write(sEncryptedLine);
				bw.newLine();
				bw.flush();
			}
		} catch (IOException e) {
			throw new Exception("Error [1467302837] writing to file '" + sLicenseFileName + "' - " + e.getMessage());
		} finally {                       // always close the file
			if (bw != null) try {
				bw.close();
			} catch (IOException ioe2) {
				// just ignore it
			}
		}
	}
	private static String executeSystemCommand(String sCommandWithOptions) throws Exception{
		String sResult = "";
		String sErrorString = "";
		int iExitValue = 0;
		try {

			Process process = Runtime.getRuntime().exec(sCommandWithOptions);
			iExitValue = process.waitFor();
			//if (iExitValue != 0) {
			//	throw new Exception("Error [1464012751] program exited abnormally with exit status: '" + Integer.toString(exitValue) + "'.");
			//}
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(process.getInputStream()));
			BufferedReader stdError = new BufferedReader(new
					InputStreamReader(process.getErrorStream()));
			String line;
			String sError;
			while ((line = reader.readLine()) != null) {
				sResult += line + "\n";
			}
			while ((sError = stdError.readLine()) != null) {
				sErrorString += sError + "\n";
			}
			reader.close();
			stdError.close();
		}catch(Exception e){
			throw new Exception("Error [1465225327] executing command '" + sCommandWithOptions + "' - " + e.getMessage());
		}
		if (iExitValue != 0){
			throw new Exception("Error [1465225328] executing command '" + sCommandWithOptions + "' - " + sErrorString);
		}
		return sResult;
	}
}

