/* Created by:  Ben Zeher 4/18/2019
 * Description: This script creates a folder by name in google drive if it does not already exist. 
 * Then it loads the google drive picker API to upload and view files from that folder.
 * It also changed the domain account to owner of the created files and folders if one is provided.
 * API credentials are provided outside this script (see global variables below)
 * 

These are the global variables this script has access to defined before loading this script. (example values included)
 
 var clientId = '910376449199-ij2k22dulac1q590psj4psvjs1qomh6sXXX.apps.googleusercontent.com';
 var developerKey = 'AIzaSyBcA9Iryl-34pnKzGAHneuogjla29tcbBwXXX';
 var folderName = '136625XXX';
 var recordtype = 'order';
 var domain = 'domain.com';
 var domainaccount = 'email@domain.com';
 var parentfolderid = '0ByxluPCydkjOQVM4THpSZVdZWTQ';
 var keyvalue = '136625';

*/
var pickerApiLoaded = false;
var scope = [ 'https://www.googleapis.com/auth/drive' ];
var folderID;
var picker;
var oauthToken;
var currentlyRunning = false;

function loadPicker() {
	//prevent mutiple clicks before function is finished.
	if(currentlyRunning === true){
		return;
	}
	currentlyRunning = true;
	
	//If we already searched for the folder ID Just display the picker
	if(folderID){
		picker.setVisible(true);
	//Otherwise authorize the user and load the APIs
	}else{
		//load the auth api
		gapi.load('auth', onAuthApiLoad);
	
	}
}
function loadClientDrive() {
	//load the client.drive api
	gapi.load('client', function() {
		gapi.client.load('drive', 'v3', loadDrivePicker);
	});
}

function loadDrivePicker() {
	//load the picker api
	gapi.load('picker', onPickerApiLoad);
}

function onPickerApiLoad() {
	pickerApiLoaded = true;
	createPicker();
}

function onAuthApiLoad() {
	//Try to authorize with current user session (no prompt). 
	window.gapi.auth.authorize({
		'client_id' : clientId,
		'scope' : scope,
		'hosted_domain' : domain,
		'prompt':'none'
	}, handleAuthResult);	
}

function handleAuthResult(authResult) {
	if (authResult) {
		if(authResult.error === 'immediate_failed'){
			//If immediate login fails, prompt to sign in.
			window.gapi.auth.authorize({
				'client_id' : clientId,
				'scope' : scope,
				'hosted_domain' : domain
			}, handleAuthResult);
		}else{
			oauthToken = authResult.access_token;
			loadClientDrive();
		}
	}
}

function createPicker() {
	//If the api is loaded and we have authorization
	if (pickerApiLoaded && oauthToken) {
		
		//Check of the folder already exists
		gapi.client.drive.files.list({
			'q' : "name='" + folderName + "' " +
				  "and parents= '" + parentfolderid + "' " +
				  "and trashed=false"  
		}).then(function(response) {
			console.log(response);
			var files = response.result.files;
			//If the folder already exists, do nothing...
			if (files && files.length > 0) {
				folderID = files[0].id;
				console.log(folderName + ' already exists with id: '
						+ files[0].id);
				console.log('Updateing data with URL...');
				 //Update folder url in database.
				var sgdoclinkElement = document.getElementsByName("sgdoclink")[0];
				if(sgdoclinkElement){
					sgdoclinkElement.value = 'https://drive.google.com/drive/folders/' + files[0].id;
				}
				asyncUpdateFolderURL('https://drive.google.com/drive/folders/' + files[0].id);
				buildPicker();
			//Otherwise, create  a new folder and save it to the database.
			} else {				
				var fileMetadata = {
						'name' : folderName,
						'mimeType' : 'application/vnd.google-apps.folder',
						'owners' : '',
						'parents' : [ parentfolderid ],
						'description' : 'Created from SMCP google drive picker.'
					};
					gapi.client.drive.files.create({
						resource : fileMetadata,
					}).then(function(response) {
						switch (response.status) {
						case 200:
							var file = response.result;
							folderID = file.id;
							console.log('Created Folder Id: ' + folderID);
	
							transferOwnership(folderID, domainaccount);
							
							    //Update folder url in database.
							    console.log('Updateing SMCP data with URL.');
							    var sgdoclinkElement = document.getElementsByName("sgdoclink")[0];
								if(sgdoclinkElement){
									sgdoclinkElement.value = 'https://drive.google.com/drive/folders/' + folderID;
								}
								asyncUpdateFolderURL('https://drive.google.com/drive/folders/' + folderID);
							buildPicker();
							break;
						default:
							break;
						}
					});		
			}
		});
	} 
}

function transferOwnership(fileId, domainaccount) {
	console.log('Transfering ownsership of files.');  
	var body = {
	    'emailAddress': domainaccount,
	    'type': 'user',
	    'role': 'owner'
	  };
	  var request = gapi.client.drive.permissions.create({
	    'fileId': fileId,
	    'transferOwnership' : true,
	    'resource': body
	  });
	  request.execute(function(resp) {
		  console.log('Transfered ownsership to ' + domainaccount + ' response: ' + resp);   
	  });
}

function buildPicker() {
	//Setup the docs view.
	var view = new google.picker.DocsView(google.picker.ViewId.DOCS);
	view.setParent(folderID).setIncludeFolders(true);
	
	//Setup the upload view
	var uploadView = new google.picker.DocsUploadView();
	uploadView.setParent(folderID);

	
	//Create the google drive picker
	picker = new google.picker.PickerBuilder()
			.enableFeature(google.picker.Feature.MULTISELECT_ENABLED)
			.setAppId(appId)
			.setOAuthToken(oauthToken)
			.addView(uploadView)
			.addView(view)
			.setDeveloperKey(developerKey)
			.setTitle(folderName)
			.setCallback(pickerCallback)
			.build();
	
	//Display the google drive picker
	picker.setVisible(true);	
}

function pickerCallback(data) {
	
	//If a user uploaded a file and there is domain account to tranfer to then transfer ownership of all those files
	if((data.action == google.picker.Action.PICKED) && (domainaccount)){
		var files = data.docs;
		for (var i = 0; i < files.length; i++) {
			var id = files[i].id;
			transferOwnership(id,domainaccount);
		}
	}
	
	//If the user picked a file then display it.
	if ((data.action == google.picker.Action.PICKED) && (!data.docs[0].isNew)) {
		var fileId = data.docs[0].id;
		picker.setVisible(true);
		window.open(data.docs[0].url);	
		
	}

	//Allow function to run again.
	currentlyRunning = false;
}

function asyncUpdateFolderURL(folderurl) {
	var xhr = new XMLHttpRequest();
	xhr.onreadystatechange = function(){
		console.log(this.responseText);
	};
	xhr.open("POST", "/sm/smcontrolpanel.SMCreateGDriveFolder");
	xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
	xhr.send("asyncrequest=Y&folderURL=" + folderurl + "&recordtype=" + recordtype + "&keyvalue=" + keyvalue.replace("&", ".ampersand.") );
	}


