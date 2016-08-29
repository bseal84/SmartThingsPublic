/**
 *  ST_Anything Doors Multiplexer - ST_Anything_Doors_Multiplexer.smartapp.groovy
 *
 *  Copyright 2015 Daniel Ogorchock
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
 *  Change History:
 *
 *    Date        Who            What
 *    ----        ---            ----
 *    2015-01-10  Dan Ogorchock  Original Creation
 *    2015-01-11  Dan Ogorchock  Reduced unnecessary chatter to the virtual devices
 *    2015-01-18  Dan Ogorchock  Added support for Virtual Temperature/Humidity Device
 *
 */
 
definition(
    name: "ST_Anything Doors Multiplexer",
    namespace: "ogiewon",
    author: "Daniel Ogorchock",
    description: "Connects single Arduino with multiple DoorControl and ContactSensor devices to their virtual device counterparts.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	page(name: "selectArduino")
}

def getLabel(myCmd, n, xname) {
	def result = input myCmd, "text", title: "Label for $xname", required: true
}


def selectArduino() {
	dynamicPage(name: "selectArduino", title: "Arduino Gateway and Devices", uninstall: true, install: true) {
		section(" ") {
			input "arduino", "capability.temperatureMeasurement", title: "Arduino Gateway", required: true, multiple: false
			input "Remove", "capability.button", title: "Remove button", required: true, multiple: false, submitOnChange: true
			//input "howMany", "number", title: "How many Lutron devices?", required: true, submitOnChange: true
		}
        
        if (Remove) {
            section("Select Lutron Devices") {
                def x = 0
                def theAtts = arduino.supportedAttributes
                theAtts.each {att ->
                    def temp = att.name.contains("temperature")
                    def humid = att.name.contains("humidity")
                    if ( temp || humid )  {
                        log.debug "Skipping: ${att.name}"
                    } else {
                        def thisLabel = "dLabel$x"
                        getLabel(thisLabel, x + 1, att.name)
                        x = x + 1
                                //paragraph(" ")
                    }
                }
            }
        }
	}
}

def installed() {
	initialize()
}

def updated() {
	unsubscribe()
	initialize()
}

def initialize() {
	state.myDevices = [:]
	
	def i = 0
    def theAtts = arduino.supportedAttributes
    theAtts.each {att ->
    	def temp = att.name.contains("temperature")
        def humid = att.name.contains("humidity")
        if ( temp || humid )  {
        	log.debug "Skipping: ${att.name}"
        } else {
			def thisLabel = settings.find {it.key == "dLabel$i"}
            def deviceId = "VT" + "_" + att.name
			def myDevice = getChildDevice(deviceId)
			if(!myDevice) def childDevice = addChildDevice("ogiewon", "Virtual Temperature", deviceId, null, [label: thisLabel.value, name: att.name, completedSetup: true])
			myDevice = getChildDevice(deviceId)
			subscribe(arduino, att.name, UpdateTemp)
            
            //def currentT = arduino.currentState(att.name)
            //def currentT = arduino.currentValue("${att.name}")
            def currentT = arduino.currentValue(att.name)
            log.debug "${myDevice.name} is ${currentT}"
            if (currentT == null) {
				myDevice.updateTemperature("?")
                log.debug "currentValue(${att.name}) temp is unknown"
            } else {
                myDevice.updateTemperature(currentT)
                log.debug "currentValue(${att.name}) temp is ${currentT}"
            }
            
            //log.debug "thisLabel = ${thisLabel}, deviceId = ${deviceId}, myDevice = ${myDevice}"
			i = i + 1
        	//log.debug "Supported Attribute: ${att.name}"
        }
    }
    //subscribe(Lutron, "msgRcvd", LutronHandler)
    subscribe(Remove, "button.held", removeHandler) 
    //subscribe(Remove, "button.held", temp)
}
/*
def LutronHandler(evt) {
	if (evt.value.startsWith("LZC") && !evt.value.endsWith("CHG")) {  // process on/off of zone
		def ndx = evt.value.substring(evt.value[4] == "0" ? 5 : 4, 6)
		def device = getChildDevice(state.myDevices["$ndx"])
		device.sendEvent(name: "switch", value: evt.value.endsWith("ON ") ? "on" : "off")
	} else if (evt.value.startsWith("ZMP")) {			  // process zmp report to tie BP to zones
		def zmp = evt.value.substring(3)
		for (int i = 1; i <= 32; i++) if(state.zmp[i] != zmp[i]) { // state changed
			def device = getChildDevice(state.myDevices["$i"])
			device.sendEvent(name: "switch", value: zmp[i] == "1" ? "on" : "off")
		}
		state.zmp = zmp
	}
}
*/

def UpdateTemp(evt)
{
    def child_id = "VT_$evt.name"
    def child = getChildDevice(child_id)
    log.debug "child.name: $child.name"
    child.updateTemperature(evt.value)
}

def temp(evt) {
	//log.debug "Button Event Recieved"
    def MTemp = arduino.currentValue("MasterBed")
    def TyTemp = arduino.currentValue("TylerRoomT")

    log.debug "currentMasterBed: ${MTemp}, currentTylerRoomT: ${TyTemp}"
	//unsubscribe()
    //uninstalled()
}

def removeHandler(evt) {
	unsubscribe()
    uninstalled()
}

def uninstalled() {
	removeChildDevices(getChildDevices())
}

private removeChildDevices(delete) {
	delete.each {deleteChildDevice(it.deviceNetworkId)}
}