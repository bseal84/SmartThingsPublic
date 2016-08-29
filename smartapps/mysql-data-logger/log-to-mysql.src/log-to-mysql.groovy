/**
 *  Log_to_mysql
 *
 *  Copyright 2015 Brian Seal
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
definition(
    name: "Log_to_mysql",
    namespace: "MySQL Data Logger",
    author: "Brian Seal",
    description: "Log data to your personal MySQL database",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section("Log devices...") {
        input "temperatures", "capability.temperatureMeasurement", title: "Temperatures", required:false, multiple: true
        input "contacts", "capability.contactSensor", title: "Doors open/close", required: false, multiple: true
        input "accelerations", "capability.accelerationSensor", title: "Accelerations", required: false, multiple: true
        input "motions", "capability.motionSensor", title: "Motions", required: false, multiple: true
        input "presence", "capability.presenceSensor", title: "Presence", required: false, multiple: true
        input "switches", "capability.switch", title: "Switches", required: false, multiple: true
        input "energymeters", "capability.EnergyMeter", title: "Energy Meter", required: false, multiple: true
        input "powermeters", "capability.powerMeter", title: "Power Meter", required: false, multiple: true

    }
    section ("SQL Server Info") {
    input("confIpAddr", "string", title:"Thermostat IP Address",
        defaultValue:"192.168.1.20", required:true, displayDuringSetup: true)
    input("confTcpPort", "number", title:"Thermostat TCP Port",
        defaultValue:"80", required:true, displayDuringSetup:true)
    input("pathtest", "string", title:"path test",
        defaultValue:"/post_test.php?power=425&ac=1&heat=1", required:true, displayDuringSetup:true)
    }
    /*
    section ("SQL Server Info") {
        input "serverAddress", "text", title: "Server Address", required: true
        input "dbName", "text", title: "Database Name", required: true
        input "userName", "text", title: "User Name", required: true
        input "userPass", "password", title: "Password", required: true
        
    }
*/
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
    //subscribe(temperatures, "temperature", handleTemperatureEvent)
    //subscribe(contacts, "contact", handleContactEvent)
    //subscribe(accelerations, "acceleration", handleAccelerationEvent)
    //subscribe(motions, "motion", handleMotionEvent)
    //subscribe(presence, "presence", handlePresenceEvent)
    //subscribe(switches, "switch", handleSwitchEvent)
    //subscribe(energymeters, "energy", handleTemperatureEvent)
    subscribe(powermeters, "power", handleEnergyEvent)
	//push()
    setNetworkId(confIpAddr, confTcpPort)
    log.debug apiGet(pathtest)
}

// TODO: implement event handlers
def handleEnergyEvent(evt) {
    //log.debug "hanleEnergyEvent: ${evt}"
    //sendValue(evt) { it.toString() }
}

private push() {
  def ip = "192.168.1.20/post_test.php?power=125&ac=1&heat=1"
  //def deviceNetworkId = "C0A8000A:1F90"
  //sendHubCommand(new physicalgraph.device.HubAction("""GET /myurl HTTP/1.1\r\nHOST: $ip\r\n\r\n""", physicalgraph.device.Protocol.LAN, "${deviceNetworkId}"))
 def deviceNetworkId = "C0A8000A:1F90"
  sendHubCommand(new physicalgraph.device.HubAction("""GET /myurl HTTP/1.1\r\nHOST: $ip\r\n\r\n""", physicalgraph.device.Protocol.LAN))

  // Get Hub firmware version if needed
  log.debug location.hubs*.firmwareVersionString.findAll { it }
}

def gettest(){
    def result = new physicalgraph.device.HubAction(
        method: "GET",
        path: "/post_test.php",
        headers: [
            HOST: "192.168.1.20"
        ],
        query: [power: "95", ac: "2", heat: "1"]
    )
    log.debug "gettest Responce: ${result}"
    //parse(result) /post_test.php?power=425&ac=1&heat=1
}

private apiGet(String path) {
    TRACE("apiGet(${path})")

    def headers = [
        HOST:       "${confIpAddr}:${confTcpPort}",
        Accept:     "*/*"
    ]

    def httpRequest = [
        method:     'GET',
        path:       path,
        headers:    headers
    ]

    return new physicalgraph.device.HubAction(httpRequest)
}

// Sets device Network ID in 'AAAAAAAA:PPPP' format
private String setNetworkId(ipaddr, port) { 
    log.debug "setNetworkId(${ipaddr}, ${port})"

    def hexIp = ipaddr.tokenize('.').collect {
        String.format('%02X', it.toInteger())
    }.join()

    def hexPort = String.format('%04X', port.toInteger())
    device.deviceNetworkId = "${hexIp}:${hexPort}"
    log.debug "device.deviceNetworkId = ${device.deviceNetworkId}"
}