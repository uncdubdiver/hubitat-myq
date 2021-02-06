/**
 *  MyQ Garage Door Opener (switch)
 *
 *  Dean Berman
 *  DB Development
 *  LU: 2021-02-05
 *  Updated after cleanup and another hub integration.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (
    	name: "MyQ Garage Door Opener (switch)",
		version: "v3",
        namespace: "dbdevelopment",
        author: "Dean Berman - DB Development"
    ) {
		capability "Actuator"				//	Represents that a device has commands
		capability "Switch"					//	Allow control of a switch
		//capability "Garage Door Control"	//	Allow control of a switch
		//capability "Contact Sensor"			//	Allows reading the value of a contact sensor device - REQUIRED FOR ALEXA TO RECOGNIZE IT
		capability "Refresh"				//	Allow the execution of the refresh command for devices that support it
		capability "Sensor"					//	Represents that a device has attributes
		capability "Health Check"			//	** Doesn't existing in the documentation - REQURED FOR ACTIONTILES TO RECOGNIZE IT
		//capability "Polling"				//	Required to allow for the switch to ping/poll for status changes
	}

	simulator {
	}

	tiles {
		standardTile("toggle", "device.switch", width: 2, height: 2) {
			state("closed", label:'${name}', action:"open", icon:"st.doors.garage.garage-closed", backgroundColor:"#00A0DC", nextState:"turningOn")
			state("open", label:'${name}', action:"close", icon:"st.doors.garage.garage-open", backgroundColor:"#e86d13", nextState:"turningOff")
			state("turningOn", label:'${name}', icon:"st.doors.garage.garage-closed", backgroundColor:"#e86d13")
			state("turningOff", label:'${name}', icon:"st.doors.garage.garclosingage-open", backgroundColor:"#00A0DC")
		}
        standardTile("refresh", "refresh", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
            state("default", label: 'Refresh Btn', action:"refresh", icon:"https://cdn1.iconfinder.com/data/icons/ui-glynh-02-of-5/100/UI_Glyph_03-18-512.png")
        }
		standardTile("open", "device.switch", inactiveLabel: false, decoration: "flat") {
			state("default", label:'Open', action:"open", icon:"https://cdn4.iconfinder.com/data/icons/thin-smart-home-automation/24/thin_garage_opening-512.png", nextState:"off")
		}
		standardTile("closed", "device.switch", inactiveLabel: false, decoration: "flat") {
			state("default", label:'Closed', action:"closed", icon:"https://cdn4.iconfinder.com/data/icons/thin-smart-home-automation/24/thin_garage_closing-512.png")
		}

		main "toggle"
		details(["toggle", "refresh", "open", "close"])
	}
}


preferences {
	section("MyQ Account Login") {
		// TODO: put inputs here
        input("username", "email", title: "Username", description: "MyQ Username (email address)", required: true)
        input("password", "password", title: "Password", description: "MyQ password", required: true)
        input("devicename", "devicename", title: "Device Name", description: "Name of the device to manage", required: true)
	}
}


def DEBUG = ""
def expiration = ""
def myqSecurityToken = ""
def myqAccountID = ""
def myqDeviceSN = ""
def myqDeviceDoorState = ""



//	----------------
//	SETTER FUNCTIONS
//	----------------
def set_sessionexp(id) {
	if(get_DEBUG() == "on") { log.debug "set_sessionexp(${id}) {..." }
	state.expiration = id
}
def set_myqSecurityToken(id) {
	if(get_DEBUG() == "on") { log.debug "set_myqSecurityToken(${id}) {..." }
	state.myqSecurityToken = id
}
def set_myqAccountID(id) {
	state.myqAccountID = id
}
def set_myqDeviceSN(id) {
	state.myqDeviceSN = id
}
def set_myqDeviceDoorState(id) {
	state.myqDeviceDoorState = id
}
def set_DEBUG(id) {
	state.DEBUG = id
}



//	-----------------------
//	GETTER FUNCTIONS - URLS
//	-----------------------
def get_myqURL() {
	return "https://api.myqdevice.com"
}
def get_myqNonSecureURL() {
	return "http://api.myqdevice.com"
}

def get_myqURLAPILoginPath() {
	return "/api/v5/Login"
}
def get_myqURLAPIAccountPath() {
	return "/api/v5/My"
}
def get_myqURLAPIDevicesURL() {
	return "/api/v5.1/Accounts/" + get_myqAccountID() + "/Devices"
}
def get_myqURLAPIDeviceActionURL() {
	return "/api/v5.1/Accounts/" + get_myqAccountID() + "/Devices/" + get_myqDeviceSN() + "/actions"
}


//	-----------------------
//	GETTER FUNCTIONS - DATA
//	-----------------------
def get_myqApplicationId() {
	return "JVM/G9Nwih5BwKgNCjLxiFUQxQijAebyyg8QUHr7JOrP+tuPb8iHfRHKwTmDzHOu"
}
def get_sessionexp() {
	return state.expiration
}
def get_myqSecurityToken() {
	if(get_DEBUG() == "on") { log.debug "get_myqSecurityToken() {... returning ${state.myqSecurityToken}" }
	return state.myqSecurityToken
}
def get_myqAccountID() {
	if(get_DEBUG() == "on") { log.debug "get_myqAccountID() {... returning ${state.myqAccountID}" }
	return state.myqAccountID
}
def get_myqDeviceSN() {
	return state.myqDeviceSN
}
def get_myqDeviceDoorState() {
	return state.myqDeviceDoorState
}
def get_DEBUG() {
	return state.DEBUG
}




def parse(String description) {
	if(get_DEBUG() == "off") { log.trace "parse($description)" }
}



def refresh() {
	////////////////////
	//////////////////// TURN THIS ON OR OFF to enable or disable debugging in the GUI
	//////////////////// VALUES: on, off
	////////////////////
	set_DEBUG("off")
	
	log.debug "DEBUG MODE=" + get_DEBUG()
	
	if(get_DEBUG() == "on") { log.trace "refresh()..." }
    
    def isloggedin = checkLoggedIn()
    
    if(get_DEBUG() == "on") { log.debug "[refresh()] - if(${isloggedin}) {" }
    if(isloggedin) {
		if(get_DEBUG() == "on") { log.debug "[refresh()] -- already logged in, continuing to door details" }
		
	} else {
		if(get_DEBUG() == "on") { log.debug "[refresh()] -- not currently logged in" }
		
		def loginret = callURL_Login()
		
		if(get_DEBUG() == "on") { log.debug "[refresh()] --- if(${loginret}) {" }
		if(loginret) {
			if(get_DEBUG() == "on") { log.debug "[refresh()] ---- logged in successfully" }
			
			//	Now to verify the AccountID is set...
			def accountid = checkAccountID()
			
			if(get_DEBUG() == "on") { log.debug "[refresh()] ---- if(${accountid}) {" }
			if(accountid) {
				if(get_DEBUG() == "on") { log.debug "[refresh()] ----- accountid set properly" }
				
			} else {
				if(get_DEBUG() == "on") { log.debug "[refresh()] ----- accountid not set..." }
				
				accountid = callURL_AccountID()
				
				if(get_DEBUG() == "on") { log.debug "[refresh()] ----- if(${accountid}) {" }
				if(accountid) {
					if(get_DEBUG() == "on") { log.debug "[refresh()] ------ accountid now set properly, continuing to door details" }
					
				} else {
					if(get_DEBUG() == "on") { log.error "[refresh()] ------ accountid did not get set properly through api... unable to continue, return false" }
					return false
				}
			}
			
		} else {
			if(get_DEBUG() == "on") { log.error "[refresh()] -- failed to login, return false and exiting" }
			return false
		}
	}
	
	//	Now to get the list of devices for this account, but only handle the actions for the single myq device assigned to this device...
	def devices = callURL_Devices()
	
	if(get_DEBUG() == "on") { log.debug "[refresh()] -- if(${devices})" }
	if(devices) {
		if(get_DEBUG() == "on") { log.debug "[refresh()] --- devices are set properly, continuing" }
		if(get_DEBUG() == "on") { 
			log.debug "----- list of devices retrieved and selected myq device captured properly..."
			log.debug "----- myqDeviceSN="+get_myqDeviceSN()
			log.debug "----- myqDeviceDoorState="+get_myqDeviceDoorState()
			log.debug "----- myqDeviceSN="+get_myqDeviceSN()
		}
		
		//	Now based on the get_myqDeviceDoorState() value, set sendEvent accordingly...
		if(get_myqDeviceDoorState() == 'open') {
			if(get_DEBUG() == "on") { log.debug "STATUS: Door Event set to OPEN" }
			sendEvent(name: "switch", value: "on")//, isStateChange: true, display: true, displayed: true)
		} else if(get_myqDeviceDoorState() == 'closed') {
			if(get_DEBUG() == "on") { log.debug "STATUS: Door Event set to CLOSED" }
			sendEvent(name: "switch", value: "off")//, isStateChange: true, display: true, displayed: true)
		}
		
		//return true
		
	} else {
		if(get_DEBUG() == "on") { log.error "[refresh()] --- failed to set/retrieve devices on the account, exiting" }
		return false;
	}
	
	unschedule()
	// Set it to run once a minute (continuous polling)
	if(get_DEBUG() == "on") { log.debug "[refresh()] ---- Polling/Refresh every 2 minutes" }
	runIn(120, refresh)
}

def checkLoggedIn() {
	if(get_DEBUG() == "on") { log.trace "checkLoggedIn()..." }
	
	def securitytoken = get_myqSecurityToken()
	def accountid = get_myqAccountID()
	
	if(get_DEBUG() == "on") { log.debug "[checkLoggedIn()] - if(${securitytoken} != null && ${accountid} != null) {" }
	if(securitytoken != null && accountid != null) {
		if(get_DEBUG() == "on") { log.debug "[checkLoggedIn()] -- Currently logged in...now checking session expiration" }
		
		//	Now to check that the users session hasn't already expired, and if so, then return false...
		try {
			def sessexp = get_sessionexp()
			if(get_DEBUG() == "on") { log.debug "[checkLoggedIn()] -- if(${sessexp} != null) {" }
			if(sessexp != null) {
				//	GTG
				if(get_DEBUG() == "on") { log.debug "[checkLoggedIn()] --- Expiration not empty, now checking DTG (${sessexp})" }
			} else {
				//	This probably won't get hit, but putting it in here just incase...
				if(get_DEBUG() == "on") { log.error "[checkLoggedIn()] --- Expiration set to NULL, returning false (${sessexp})" }
				return false
			}
		} catch(e) {
			//	If it fall here, then it has expired...
			if(get_DEBUG() == "on") { log.error "[checkLoggedIn()] --- XXXXX Expiration caught by exception (${e})" }
			return false
		}
		
		if(get_DEBUG() == "on") { log.debug "[checkLoggedIn()] ---- if("+now()+" > ${sessexp}) {" }
		if(now() > get_sessionexp()) {
			if(get_DEBUG() == "on") { log.error "[checkLoggedIn()] ----- Session has expired, returning FALSE (to force re-login)" }
			return false
			
		} else {
			if(get_DEBUG() == "on") { log.debug "[checkLoggedIn()] ----- Session has not yet expired, returning TRUE" }
			return true
		}
	} else {
		if(get_DEBUG() == "on") { log.error "[checkLoggedIn()] -- SecurityToken and/or AccountID not properly set, return FALSE" }
		return false
	}
}

def checkAccountID() {
	if(get_DEBUG() == "on") { log.trace "checkAccountID()..." }
	
	if(get_myqAccountID()) {
		if(get_DEBUG() == "on") { log.debug "- get_myqAccountID() = "+get_myqAccountID() }
		if(get_DEBUG() == "on") { log.debug "- so returning true" }
		return true
	} else {
	if(get_DEBUG() == "on") { log.debug "- returning false" }
		return false
	}
}






def installed() {
	if(get_DEBUG() == "on") { log.trace "installed()..." }
	initialize()
    
    // runIn(120, refresh)
}

def updated() {
	if(get_DEBUG() == "on") { log.trace "updated()..." }
	initialize()
    
    // runIn(120, refresh)
    //runEvery5Minutes(refresh)
}

private initialize() {
	if(get_DEBUG() == "on") { log.trace "initialize()..." }
    refresh()

	sendEvent(name: "DeviceWatch-DeviceStatus", value: "online")
	sendEvent(name: "healthStatus", value: "online")
	//sendEvent(name: "DeviceWatch-Enroll", value: [protocol: "cloud", scheme:"untracked"].encodeAsJson(), displayed: false)
}



//	-------------
//	STILL TO DO!!
//	-------------
def on() {
	if(get_DEBUG() == "on") { log.trace "on()..." }
	
	//	Updating the event/button within ST...
	if(get_DEBUG() == "on") { log.debug "[on()] - executing sendEvent turningOn" }
	sendEvent(name: "switch", value: "turningOn")//, isStateChange: true, display: true, displayed: true)
	//	Executing the API call...
	if(get_DEBUG() == "on") { log.debug "[on()] - executing callURL_DeviceAction to open the door" }
	callURL_DeviceAction('open')
	//	Marking it as FINISHED turningOn...
    runIn(20, finishTurningOn)
}
def finishTurningOn() {
	if(get_DEBUG() == "on") { log.trace "finishTurningOn()..." }
	
	if(get_DEBUG() == "on") { log.debug "[finishTurningOn()] - executing sendEvent open" }
	sendEvent(name: "switch", value: "on")//, isStateChange: true, display: true, displayed: true)
	
    refresh()
}


def off() {
	if(get_DEBUG() == "on") { log.trace "off()..." }
	
	//	Updating the event/button within ST...
	if(get_DEBUG() == "on") { log.debug "[off()] - executing sendEvent turningOff" }
    sendEvent(name: "switch", value: "turningOff")//, isStateChange: true, display: true, displayed: true)
	//	Executing the API call...
	if(get_DEBUG() == "on") { log.debug "[off()] - executing callURL_DeviceAction to close the door" }
	callURL_DeviceAction('close')
	//	Marking it as FINISHED turningOff...
	runIn(40, finishTurningOff)
}
def finishTurningOff() {
	if(get_DEBUG() == "on") { log.trace "finishTurningOff()..." }
	
	if(get_DEBUG() == "on") { log.debug "[finishTurningOff()] - executing sendEvent closed" }
    sendEvent(name: "switch", value: "off")//, isStateChange: true, display: true, displayed: true)
	
    refresh()
}








//	--------------
//	CURL FUNCTIONS
//	--------------
def callURL_Login() {
	if(get_DEBUG() == "on") { log.trace "callURL_Login()..." }
	
    def params
    
	params = [
		uri     : get_myqURL(),
		path    : get_myqURLAPILoginPath(),					//	/api/v5/Login
		headers : ['MyQApplicationId': get_myqApplicationId(), 'Content-Type': 'application/json'],
		body    : '{ "Username": "' + settings.username + '", "Password": "' + settings.password + '" }',
		query   : null,
	]
    
	if(get_DEBUG() == "on") { 
		log.debug "URL=${params.uri}${params.path}"
		log.debug "HEADERS=${params.headers}"
		log.debug "QUERY=${params.query}"
		log.debug "BODY=${params.body}"
	}
	
    //changed to httpPost
	try {
		if(get_DEBUG() == "on") { log.debug "[callURL_Login()] - executing httpPost()..." }
		
		httpPost(params) { resp ->
			if(get_DEBUG() == "on") { 
				//log.debug "DATA="+resp.data
				//log.debug "- SecurityToken="+resp.data.SecurityToken
			}
			
			set_myqSecurityToken(resp.data.SecurityToken)
			set_sessionexp(now() + (5 * 60 * 1000))

			if(get_myqSecurityToken() != null) {
				if(get_DEBUG() == "on") { log.debug "[callURL_Login()] -- all variables set successfully, continuing on..." }
			} else {
				if(get_DEBUG() == "on") { log.error "[callURL_Login()] -- failed to set variables, returning FALSE" }
				return false
			}
		}
	} catch (groovyx.net.http.HttpResponseException e) {
		if(get_DEBUG() == "on") { log.error "[callURL_Login()] -- FAILED - EXCEPTION: ${e}, returning false" }
		
		return false
	}
	
	if(get_DEBUG() == "on") { log.debug "[callURL_Login()] - default returning true..." }
	return true
}

def callURL_AccountID() {
	if(get_DEBUG() == "on") { log.trace "callURL_AccountID()" }
	
    def params
    
	params = [
		uri     : get_myqURL(),
		path    : get_myqURLAPIAccountPath(),				//	/api/v5/My
		headers : ['MyQApplicationId': get_myqApplicationId(), 'Content-Type': 'application/json', 'SecurityToken': get_myqSecurityToken()],
		body    : null,
		query   : null,
	]
    
	if(get_DEBUG() == "on") { 
		log.debug "URL=${params.uri}${params.path}"
		log.debug "HEADERS=${params.headers}"
		log.debug "QUERY=${params.query}"
		log.debug "BODY=${params.body}"
	}
	
    //changed to httpGet
	try {
		if(get_DEBUG() == "on") { log.debug "[callURL_AccountID()] - executing httpGet()..." }
		
		httpGet(params) { resp ->
			if(get_DEBUG() == "on") { 
				//log.debug "DATA="+resp.data
				//log.debug "- Account.href="+resp.data.Account.href
			}
			
			if(get_DEBUG() == "on") { log.debug "[callURL_AccountID()] -- checking if Account.href is not empty (to get accountid)..." }
			if(resp.data.Account.href != null) {
				def origurl = resp.data.Account.href
				def accountid = origurl.replaceAll(get_myqNonSecureURL() + "/api/v5/Accounts/", "")
				
				if(get_DEBUG() == "on") { log.debug "[callURL_AccountID()] --- accountid=${accountid}..." }
				set_myqAccountID(accountid);
			}
			
			if(get_myqAccountID() != null) {
				if(get_DEBUG() == "on") { log.debug "[callURL_AccountID()] --- accountid is set and not empty, returning true" }
				//return true
			} else {
				if(get_DEBUG() == "on") { log.error "[callURL_AccountID()] --- accountid is not set or empty, returning false" }
				return false
			}
		}
	} catch (groovyx.net.http.HttpResponseException e) {
		if(get_DEBUG() == "on") { log.error "[callURL_AccountID()] --- Exception Error: ${e}" }

		return false
	}
	
	if(get_DEBUG() == "on") { log.debug "[callURL_Login()] - default returning true..." }
	return true
}

def callURL_Devices() {
	if(get_DEBUG() == "on") { log.trace "callURL_Devices()" }
	
    def params
    
	params = [
		uri     : get_myqURL(),
		path    : get_myqURLAPIDevicesURL(),				//	/api/v5.1/Accounts/%myqAccountID/Devices
		headers : ['MyQApplicationId': get_myqApplicationId(), 'Content-Type': 'application/json', 'SecurityToken': get_myqSecurityToken()],
		body    : null,
		query   : null,
	]
    
	if(get_DEBUG() == "on") { 
		log.debug "URL=${params.uri}${params.path}"
		log.debug "HEADERS=${params.headers}"
		log.debug "QUERY=${params.query}"
		log.debug "BODY=${params.body}"
	}
	
    //changed to httpGet
	try {
		if(get_DEBUG() == "on") { log.debug "[callURL_Devices()] - executing httpGet()..." }
		
		httpGet(params) { resp ->
			if(get_DEBUG() == "on") { 
				//log debug "RESP="
				//log.debug "HEADERS="+resp.headers
				//log.debug "DATA="+resp.data
				//log.debug "- Account.href="+resp.data.Account.href
				//log.debug "Current DoorState="+resp.data.doorstate
			}
			
			//	Now to loop through the resp.data.items and look for the settings.devicename
			for(item in resp.data.items) {
				if(get_DEBUG() == "on") { log.debug "[callURL_Devices()] -- checking values for devicename and device_family" }
				if(item.name == settings.devicename && item.device_family == 'garagedoor') {
					if(get_DEBUG() == "on") { log.debug "[callURL_Devices()] --- device name found: ${item.name}" }
					
					if(get_DEBUG() == "on") { log.debug "[callURL_Devices()] --- setting device serial_number and door_state" }
					set_myqDeviceSN(item.serial_number);
					set_myqDeviceDoorState(item.state.door_state);
					
					//	Now to confirm these options got set appropriately...
					if(get_DEBUG() == "on") { log.debug "[callURL_Devices()] --- making sure values are set properly" }
					if(get_myqDeviceSN() != null && get_myqDeviceDoorState() != null) {
						if(get_DEBUG() == "on") { log.debug "[callURL_Devices()] ---- values are set properly, returning true" }
						//return true
					} else {
						if(get_DEBUG() == "on") { log.error "[callURL_AccountID()] ---- serial_number and door_state did not get set properly, returning false" }
						return false
					}
				}
			}
			
			return false
		}
	} catch (groovyx.net.http.HttpResponseException e) {
		if(get_DEBUG() == "on") { log.error "[callURL_Devices()] --- Exception Error: ${e}" }

		return false
	}
	
	if(get_DEBUG() == "on") { log.debug "[callURL_Devices()] - default returning true..." }
	return true
}

def callURL_DeviceAction(actiontype) {
	if(get_DEBUG() == "on") { log.trace "callURL_DeviceAction("+actiontype+")" }
	
    def params
    
	params = [
		uri     : get_myqURL(),
		path    : get_myqURLAPIDeviceActionURL(),
		headers : ['MyQApplicationId': get_myqApplicationId(), 'Content-Type': 'application/json', 'SecurityToken': get_myqSecurityToken()],
		body    : '{"action_type": "'+actiontype+'"}',
		query   : null,
	]
    
	if(get_DEBUG() == "on") { 
		log.debug "[URL]=${params.uri}${params.path}"
		log.debug "[HEADERS]=${params.headers}"
		log.debug "[QUERY]=${params.query}"
		log.debug "[BODY]=${params.body}"
	}
	
    //changed to httpPut
	try {
		if(get_DEBUG() == "on") { log.debug "[callURL_DeviceAction()] - executing httpPutJson()..." }
		
		httpPutJson(params) { resp ->
			if(get_DEBUG() == "on") { 
				//log debug "RESP="
				//log.debug "HEADERS="+resp.headers
				//log.debug "DATA="+resp.data
				//log.debug "- Account.href="+resp.data.Account.href
				//log.debug "Current DoorState="+resp.data.doorstate
			}
			
			if(get_DEBUG() == "on") { log.debug "[callURL_DeviceAction()] -- json executed, assuming gtg..." }
			return true
		}
	} catch (groovyx.net.http.HttpResponseException e) {
		if(get_DEBUG() == "on") { log.error "X callURL_DeviceAction() >> PUT >> Error: e.statusCode ${e}" }

		return false
	}
	
	if(get_DEBUG() == "on") { log.debug "[callURL_DeviceAction()] - default returning true..." }
	return true
}


