package ConnectionPool;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Vector;

import javax.servlet.ServletContext;

public class ConnectionPool implements Runnable {
	public static final String LABELED_CONNECTION_STATUS_AVAILABLE = "0";
	public static final String LABELED_CONNECTION_STATUS_BUSY = "1";
	public static final String CONNECTION_STATE = "CONNECTIONSTATE";
	public static final String CONNECTION_ID = "CONNECTIONID";
	private static final long MAX_CONNECTION_AGE = 28700000;
	private String sDatabaseID;
	private String sCurrentType;
	private String sCurrentCallingClass;
	private ServletContext sCurrentContext;
	private int maxConnections;
	private boolean bwaitIfBusy;
	private Vector<LabelledConnection> availableConnections, busyConnections;

	//If there is a connection waiting to be opened, this variable will be true:
	private boolean connectionPending = false;
	private boolean bDebugMode = false;

	//If we tried and got an Exception when we tried to open a new database connection, we want to know that
	//so we don't try again. This variable keeps track of that:
	private boolean bFailedOpeningDBConnection = false;
	private String sFailedOpeningDBConnectionMesg = "";

	public ConnectionPool(
			String dbID,
			String DBType,
			String CallingClass,
			ServletContext context,
			int initialConnections,
			int maxConnections,
			boolean bwaitIfBusy)
	throws Exception {
		if (bDebugMode){
			System.out.println("03 ConnectionPool.ConnectionPool - "
					+ "dbID = " + dbID
					+ "DBType = " + DBType
					+ "CallingClass = " + CallingClass
					+ "initialConnections = " + initialConnections
					+ "maxConnections = " + maxConnections
			);
		}
		this.sDatabaseID = dbID;
		this.sCurrentType = DBType;
		this.sCurrentCallingClass = CallingClass;
		this.sCurrentContext = context;
		this.maxConnections = maxConnections;
		this.bwaitIfBusy = bwaitIfBusy;
		if (initialConnections > maxConnections) {
			initialConnections = maxConnections;
		}
		availableConnections = new Vector<LabelledConnection>();
		busyConnections = new Vector<LabelledConnection>();
		for(int i=0; i<initialConnections; i++) {
			if (bDebugMode){
				System.out.println("04 ConnectionPool.ConnectionPool - "
						+ "dbID = " + dbID
						+ "DBType = " + DBType
						+ "CallingClass = " + CallingClass
						+ "initialConnections = " + initialConnections
						+ "maxConnections = " + maxConnections
				);
			}
			try{
				availableConnections.addElement(PoolUtilities.OpenDatabaseConnection(dbID, DBType, CallingClass, context));
			}catch(Exception e){
				throw e;
			}
			//System.out.println("added element to available list in run(). (new connection created)");
		}
	}

	public synchronized Connection getConnection(String sDbID, String sType, String sCallingClass)
	throws Exception {
		String sDbURL = "";
		Connection existingConnection = null;
		long lConnectionID = 0L;
		this.sDatabaseID = sDbID;
		String sCurrentLabel = sDbID;  
		if (bDebugMode){
			System.out.println("In ConnectionPool.getConnection - starting out - sDbID = " + sDbID + ", sType = " + sType + ", sCallingClass = " + sCallingClass);
		}
		sCurrentType = sType;
		sCurrentCallingClass = sCallingClass;
		Timestamp tsCreationTimeStamp = null;
		int i = 0;
		//If there are some available connections, go get one:
		try {
			if (!availableConnections.isEmpty()) {
				if (bDebugMode){
					System.out.println(PoolUtilities.SystemTime() 
							+ "In ConnectionPool.getConnection - !availableConnections.isEmpty() - there ARE available connections - trying to "
							+ " find matches for  label: " + sCurrentLabel + " and type: " + sType);
				}
				//find the connection with accepted label
				for (i=0;i < availableConnections.size();i++){
					//System.out.println(PoolUtilities.SystemTime() + "processing element: " + i);
					//System.out.println(PoolUtilities.SystemTime() + "passed in label: " + sLabel);
					//System.out.println(PoolUtilities.SystemTime() + "passed in type: " + sType);
					//System.out.println(PoolUtilities.SystemTime() + "element label: " + ((LabelledConnection)availableConnections.elementAt(i)).Get_Label());
					//System.out.println(PoolUtilities.SystemTime() + "element type: " + ((LabelledConnection)availableConnections.elementAt(i)).Get_Type());
					if (bDebugMode){
						System.out.println(PoolUtilities.SystemTime() 
								+ "In ConnectionPool.getConnection - "
								+ "looping through available connections - label: " 
								+ ((LabelledConnection)availableConnections.elementAt(i)).Get_Label()
								+ ", type: " 
								+ ((LabelledConnection)availableConnections.elementAt(i)).Get_Type());
					}
					if (((LabelledConnection)availableConnections.elementAt(i)).Get_Label().compareTo(sCurrentLabel) == 0 &&
							((LabelledConnection)availableConnections.elementAt(i)).Get_Type().compareTo(sType) == 0){
						//found 
						if (bDebugMode){
							System.out.println(PoolUtilities.SystemTime() 
									+ "In ConnectionPool.getConnection - "
									+ "found connection with matching label and type");
						}
						//System.out.println(PoolUtilities.SystemTime() + "got connection.");
						LabelledConnection lblconn = ((LabelledConnection)availableConnections.elementAt(i));
						existingConnection = lblconn.Get_Connection();
						tsCreationTimeStamp = lblconn.get_Creation_Timestamp();
						lConnectionID = lblconn.get_Connection_ID();
						sDbURL = lblconn.Get_sDatabaseURL();
						//TJR - 9/13/2013 - confirm that the connection still matches what we need:
						if ((lblconn.Get_Label().compareTo(sCurrentLabel) != 0)){
							//Destroy the connection:
							existingConnection = null;
							System.out.println(PoolUtilities.SystemTime() 
								+ " [1428610479] In ConnectionPool.getConnection - "
								+ "selected connection no longer matches label");
						}
						break;
					}
				}
			}
		} catch (Exception e1) {
			throw new Exception("Error [1389965523] in getConnection - " + e1.getMessage());
		}

		//If we did get a connection, then take that connection out of the 'available' pool for now:
		if (existingConnection != null){
			if (bDebugMode){
				System.out.println(PoolUtilities.SystemTime() 
						+ "In ConnectionPool.getConnection - "
						+ "got a non-null connection");
			}
			availableConnections.removeElementAt(i);
			// If connection on available list is closed (e.g., it timed out) or too old, then remove it from available list and repeat 
			// the process of obtaining a connection.  Also wake up threads that were waiting for a connection because maxConnection 
			// limit isn't reached now. 
			if ((tsCreationTimeStamp.getTime() < System.currentTimeMillis()- MAX_CONNECTION_AGE) || existingConnection.isClosed()) { //discard connections older than 8 hours
				if (bDebugMode){
					System.out.println("[1398083410]" + PoolUtilities.SystemTime() 
							+ "In ConnectionPool.getConnection - "
							+ "got an invalid connection - freeing up thread");
				}
				//If it's too old a connection, we'll close it here:
				if ((tsCreationTimeStamp.getTime() < System.currentTimeMillis()- MAX_CONNECTION_AGE) && !existingConnection.isClosed()){
					existingConnection.close();
				}
				notifyAll(); // Free up a spot for anybody waiting
				try {
					return(getConnection(sDbID, sType, sCallingClass));
				} catch (Exception e) {
					throw new Exception("Error getting connection [1385069433]:" + e.getMessage());
				}
			}else{
				//Now we've got a good, matching connection:
				if (bDebugMode){
					System.out.println(PoolUtilities.SystemTime() 
							+ "In ConnectionPool.getConnection - "
							+ "adding connection to busyConnections");
				}
				//Add it to the 'busy connections' pool and return it:
				busyConnections.addElement(
					new LabelledConnection(existingConnection, sCurrentLabel, sType, sCallingClass, sDbURL, tsCreationTimeStamp, lConnectionID)); 
				return(existingConnection);
			}
		}else{
			//If we did NOT get a connection, go try to open one:
			if (bDebugMode){
				System.out.println("[1398083465]" + PoolUtilities.SystemTime() 
						+ "In ConnectionPool.getConnection - "
						+ "didn't get a connection - trying now to open one");
			}
			//If we haven't reached the maximum number of connections AND if there is no connection pending,
			//then we can go to makeBackgroundConnection and try to get a connection for ourselves
			//TJR - 4/29/2014 - keeps going back to this line and looping endlessly...
			if ((totalConnections() < maxConnections) && !connectionPending) {
				if (bDebugMode){
					System.out.println("[1398083522]in ConnectionPool.getConnection - going to makeBackgroundConnection");
				}
				try {
					//This tells the ConnectionPool class to start a new thread and then that invokes the 'run' method:
					//Make sure once more that the database ID is updated at the last second so we don't get the wrong database:
					this.sDatabaseID = sDbID;
					makeBackgroundConnection();
				} catch (Exception e) {
					if (bDebugMode){
						System.out.println(PoolUtilities.SystemTime() 
								+ " makeBackgroundConnection threw exception");
					}
					throw new Exception(
							"in ConnectionPool.getConnection, makeBackgroundConnection triggered "
							+ "Exception with "
							+ "sLabel = '" + sCurrentLabel + "'"
							+ ", sConf = '" + sDbID + "'"
							+ ", sType = '" + sType + "'"
							+ ", sCallingClass = '" + sCallingClass + "'"
					);			
				}
			} else if (!bwaitIfBusy) {
				if (bDebugMode){
					System.out.println(PoolUtilities.SystemTime() + "connection limit reached OR another person is already"
							+ " waiting");
				}
				throw new Exception("Connection limit reached");
			}
			// Wait for either a new connection to be established (if you called makeBackgroundConnection) or for an existing connection
			//to be freed up.
			try {
				if (bDebugMode){
					System.out.println(PoolUtilities.SystemTime() 
							+ "In ConnectionPool.getConnection - "
							+ "calling wait()");
				}
				//If someone's trying to open a connection, just wait.  We'll be freed up again when they finish
				//trying to connect because then they'll send out a 'notify'
				//LTO 20140429 Removed the condition statement so the thread will always wait for other thread to wake it
				// up after failed to either grab a connection nor create a connection.
				//if (connectionPending){
				wait();  
				//}

				if (bDebugMode){
					System.out.println(PoolUtilities.SystemTime() 
							+ "In ConnectionPool.getConnection - "
							+ "after wait()");
				}
			} catch(InterruptedException ie) {
				if (bDebugMode){
					System.out.println(PoolUtilities.SystemTime() 
							+ "In ConnectionPool.getConnection - "
							+ " caught InterruptedException");
				}
				throw new Exception(
						"in ConnectionPool.getConnection, existingConnection == null, triggered "
						+ "InterruptedException with "
						+ "sLabel = '" + sDbID + "'"
						+ ", sType = '" + sType + "'"
						+ ", sCallingClass = '" + sCallingClass + "'"
				);
			}	
		}

		//If we failed in opening a connection, then we don't want to call this same function recursively again.
		//But if we didn't fail, call this function again and try again
		if (!bFailedOpeningDBConnection){
			// Someone freed up a connection, so try again.
			if (bDebugMode){
				System.out.println("[1398086751]" + PoolUtilities.SystemTime() 
						+ "In ConnectionPool.getConnection - "
						+ " return(getConnection(sLabel, sType, sCallingClass))"
						+ " - we want to try to connect again");
			}
			try {
				return(getConnection(sDbID, sType, sCallingClass));
			} catch (Exception e) {
				System.out.println("[1385068434] Error getting connection - db ID: " 
					+ sDbID 
					+ ", calling class: " 
					+ sCallingClass 
					+ " - " 
					+ e.getMessage()
				);
				throw new Exception("Error getting conn [1385068435] - db ID: " + sDbID +", calling class: " + sCallingClass + " - " + e.getMessage());
			}
		}else{
			// Failed to open connection
			if (bDebugMode){
				System.out.println(PoolUtilities.SystemTime() 
						+ "In ConnectionPool.getConnection - "
						+ " Unrecoverable failure to open connection - throwing Exception");
			}
			connectionPending = false;
			throw new Exception(sFailedOpeningDBConnectionMesg);
		}
	}	

	// You can't just make a new connection in the foreground
	// when none are available, since this can take several
	// seconds with a slow network connection. Instead,
	// start a thread that establishes a new connection,
	// then wait. You get woken up either when the new connection
	// is established or if someone finishes with an existing
	// connection.

	private void makeBackgroundConnection() throws Exception{
		connectionPending = true;
		if (bDebugMode){
			System.out.println(PoolUtilities.SystemTime() + "in makeBackgroundConnection");
		}
		try {
			Thread connectThread = new Thread(this);

			//This makes a new call to getConnection:
			connectThread.start();
		} catch(OutOfMemoryError oome) {
			// Give up on new connection
			throw new Exception(
					"OutOfMemoryError in makeBackgroundConnection - failed to make connection - " + oome.getMessage());
		}
	}

	public void run() {

		synchronized(this) {
			//System.out.println("Let's see what is it: sConfName = " + sConfName);
			if (bDebugMode){
				System.out.println(PoolUtilities.SystemTime() + "in ConnectionPool.run"
						+ " - going to try to open a database connection."	  
				);
			}
			try {
				if (bDebugMode){
					System.out.println(PoolUtilities.SystemTime() + "run(): Current label/type = \"" 
							+ sDatabaseID + "\"/\"" + sCurrentType + "\""
							+ " - current database = " + sDatabaseID);
				}
				bFailedOpeningDBConnection = false;
				LabelledConnection connection = PoolUtilities.OpenDatabaseConnection(
						sDatabaseID, 
						sCurrentType, 
						sCurrentCallingClass,
						sCurrentContext
				);
				if (connection == null){
					if (bDebugMode){
						System.out.println("Connection pool [1386362885] " + PoolUtilities.SystemTime() + " in 'run' connection==null");
					}
					//TJR - added this 2/13/11:
					//connectionPending = false;
					//notifyAll();
					
					//TJR - added this 12/6/2013 - trying to eliminate endless looping to get a connection that crashes host server:
					bFailedOpeningDBConnection = true;
				}else{
					availableConnections.addElement(connection);
					if (bDebugMode){
						System.out.println(PoolUtilities.SystemTime() + "added element to available list in run(). (new connection created)");
					}
					connectionPending = false;
					notifyAll();
				}
			} catch(Exception e) { //Exception or OutOfMemory
				// Give up on new connection and wait for existing one
				// to free up.
				if (bDebugMode){
					System.out.println(PoolUtilities.SystemTime() + "run() caused exception.");
					System.out.println(PoolUtilities.SystemTime() + "Exception.getMessage() = " + e.getMessage());
				}
				//TJR - added these to try to stop hanging 2/13/2011:
				//connectionPending = false;
				sFailedOpeningDBConnectionMesg = "Failed to get database connection - " + e.getMessage();
				bFailedOpeningDBConnection = true;
				notifyAll();
				//return;
			}
		}
	}

	// This explicitly makes a new connection. Called in
	// the foreground when initializing the ConnectionPool,
	// and called in the background when running.

	public synchronized void free(Connection connection) {
		int i;
		for (i = 0;i < busyConnections.size();i++){
			if (connection.equals(((LabelledConnection)busyConnections.elementAt(i)).Get_Connection())){
				availableConnections.addElement(busyConnections.elementAt(i));
				busyConnections.removeElementAt(i);
				//System.out.println(PoolUtilities.SystemTime() + "add element to available list in free(). (old connection is made reusable again)");
				break;
			}
		}
		// Wake up threads that are waiting for a connection
		notifyAll(); 
	}

	public synchronized void free(int i) {
		availableConnections.addElement(busyConnections.elementAt(i));
		busyConnections.removeElementAt(i);

		// Wake up threads that are waiting for a connection
		notifyAll(); 
	}

	public synchronized void free(int i, String sConnectionState) throws Exception{

		try{
			if (sConnectionState.compareToIgnoreCase(LABELED_CONNECTION_STATUS_AVAILABLE) == 0){
				//destroy an available connection by .... destroying it
				LabelledConnection lc = (LabelledConnection) availableConnections.get(i);
				lc.Get_Connection().close();
				availableConnections.removeElementAt(i);
			}else{
				//free a busy connection by moving it back to available pool
				availableConnections.addElement(busyConnections.elementAt(i));
				busyConnections.removeElementAt(i);
			}
			// Wake up threads that are waiting for a connection
			notifyAll(); 
		}catch(Exception ex){
			throw new Exception("ConnectionPool.free threw Exception - Connection failed to close - " + ex.getMessage());
		}
	}

	public synchronized int totalConnections() throws Exception {
		
		try {
			return availableConnectionNumber() + busyConnectionNumber();
		
		} catch (Exception e) {
			System.out.println("[1395775702] Error getting total connections - " + e.getMessage());
			throw new Exception("Error getting total connection count.");
		}
		
	}

	public synchronized int availableConnectionNumber() {
		return(availableConnections.size());
	}

	public synchronized int busyConnectionNumber() {
		return(busyConnections.size());
	}

	/** Close all the connections. Use with caution:
	 *  be sure no connections are in use before
	 *  calling. Note that you are not required to
	 *  call this when done with a ConnectionPool, since
	 *  connections are guaranteed to be closed when
	 *  garbage collected. But this method gives more 
	 *  control regarding when the connections are closed.
	 */

	public synchronized void closeAllConnections() {
		closeConnections(availableConnections);
		availableConnections = new Vector<LabelledConnection>();
		closeConnections(busyConnections);
		busyConnections = new Vector<LabelledConnection>();
		System.gc();
	}

	private void closeConnections(Vector<LabelledConnection> connections) {
		try {
			for(int i=0; i<connections.size(); i++) {
				LabelledConnection connection = (LabelledConnection) connections.elementAt(i);
				if (!connection.Get_Connection().isClosed()) {
					connection.Get_Connection().close();
				}
			}
		} catch(Exception e) {
			// Ignore errors; garbage collect anyhow
		}
	}

	public String Get_Connection_Pool_Connection_Summary(){
		return "<B>Connection Pool Status(available/busy/max): " + availableConnections.size() + "/" + busyConnections.size() + "/" + maxConnections + "</B>";
	}

	public ArrayList<String> Get_Connection_Pool_Connection_List (String sExecutioner) throws Exception{

		ArrayList<String> alConnectionList = new ArrayList<String>(0);
		alConnectionList.add("&nbsp;&nbsp;&nbsp;&nbsp;<B>Available Connections:</B>");
		if (!availableConnections.isEmpty()){
			for(int i=0;i<availableConnections.size();i++){
				try {
					String sDescription = "";
					LabelledConnection lbconn = (LabelledConnection) availableConnections.get(i);
					if (lbconn != null){
						sDescription = "<A HREF=\"" + sExecutioner + i + "&" + CONNECTION_STATE + "=" + LABELED_CONNECTION_STATUS_AVAILABLE 
							+ "\">kill</A>&nbsp;&nbsp;&nbsp;&nbsp;"
							+ Integer.toString(i + 1) + "&nbsp;&nbsp;"
							+ "<B><I>Process ID:</I></B>&nbsp;" + Long.toString(lbconn.get_Connection_ID()) + "&nbsp;&nbsp;"
							+ lbconn.Get_sDatabaseURL() + ":" + lbconn.Get_Label() + " - <B><I>created:</I></B>&nbsp;" + lbconn.get_Creation_Timestamp().toString();
						if (lbconn.Get_Connection().isClosed()){
							sDescription += "  (Connection closed)";
						}else{
							sDescription += "  (" + lbconn.Get_Connection().getMetaData().getDatabaseProductName() + ")";
						}
						alConnectionList.add(sDescription);
					}
				} catch (Exception e) {
					throw new Exception("Error with AVAILABLE connection " + i + " - " + e.getMessage() + ".");
				}
			}
		}else{
			alConnectionList.add("&nbsp;&nbsp;&nbsp;&nbsp;<B>No available connections.</B>");
		}
		alConnectionList.add("&nbsp;");
		alConnectionList.add("&nbsp;&nbsp;&nbsp;&nbsp;<B>Busy Connections:</B>");
		if (!busyConnections.isEmpty()){
			for(int i=0;i<busyConnections.size();i++){
				try {
					String sDescription = "";
					LabelledConnection lbconn = (LabelledConnection) busyConnections.get(i);
					if (lbconn != null){
						sDescription = "<A HREF=\"" + sExecutioner + i + "&" + CONNECTION_STATE + "=" + LABELED_CONNECTION_STATUS_BUSY 
							+ "\">free</A>&nbsp;&nbsp;&nbsp;&nbsp;" 
							+ Integer.toString(i + 1) + "&nbsp;&nbsp;"
							+ "<B><I>Process ID:</I></B>&nbsp;" + Long.toString(lbconn.get_Connection_ID()) + "&nbsp;&nbsp;"
							+ lbconn.Get_sDatabaseURL() + ":" + lbconn.Get_Label() + " - <B><I>created:</I></B>&nbsp;" + lbconn.get_Creation_Timestamp().toString();
						if (lbconn.Get_Connection().isClosed()){
							sDescription += "  (Connection closed)";
						}else{
							sDescription += "  (" + lbconn.Get_Connection().getMetaData().getDatabaseProductName() + ")";
						}
						sDescription += " - CallingClass: " +  lbconn.Get_CallingClass();
						alConnectionList.add(sDescription);
					}else{
						alConnectionList.add("NULL");
					}
				} catch (Exception e) {
					throw new Exception("Error with BUSY connection " + i + " - " + e.getMessage() + ".");
				}
			}
		}else{
			alConnectionList.add("&nbsp;&nbsp;&nbsp;&nbsp;No busy connections.");
		}
		return alConnectionList;
	}

	public synchronized void PrintStatus(boolean bPrintConnectionList) throws Exception{

		System.out.println(PoolUtilities.SystemTime() + "Connection Pool Status(available/busy/max): " + availableConnections.size() + "/" + busyConnections.size() + "/" + maxConnections);

		if (bPrintConnectionList){
			System.out.println("");
			System.out.println(PoolUtilities.SystemTime() + " Available Connections:");
			if (!availableConnections.isEmpty()){
				for(int i=0;i<availableConnections.size();i++){

					if ((LabelledConnection) availableConnections.get(i) != null){
						System.out.print(((LabelledConnection) availableConnections.get(i)).Get_Label());
						System.out.print(" - ");
						System.out.print((((LabelledConnection) availableConnections.get(i)).get_Creation_Timestamp()).toString());
						System.out.print("  (");
						System.out.print(((LabelledConnection) availableConnections.get(i)).Get_Connection().getMetaData().getDatabaseProductName());
						System.out.println(")");
					}
				}
			}else{
				System.out.println(PoolUtilities.SystemTime() + "No available connections.");
			}
			System.out.println("");
			System.out.println(PoolUtilities.SystemTime() + " Busy Connections:");
			if (busyConnections.size() > 0){
				for(int i=0;i<busyConnections.size();i++){
					if ((LabelledConnection) busyConnections.get(i) != null){
						System.out.print(((LabelledConnection) busyConnections.get(i)).Get_Label());
						System.out.print(" - ");
						System.out.print((((LabelledConnection) busyConnections.get(i)).get_Creation_Timestamp()).toString());
						System.out.print("  (");
						System.out.print(((LabelledConnection) busyConnections.get(i)).Get_Connection().getMetaData().getDatabaseProductName());
						System.out.println(")");
					}
				}
			}else{
				System.out.println(PoolUtilities.SystemTime() + "No busy connections.");
			}
		}
	}
}