/**
 *  tomysql2
 *
 *  Copyright 2016 Brian Seal
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
    name: "tomysql2",
    namespace: "toMySql2",
    author: "Brian Seal",
    description: "to my mysql",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Log the Following to MySQL...") {
		//input "tempSensors", "capability.temperatureMeasurement", title: "Which temp sensors? Logs with each of its own events", required: false, multiple: true
        input "tempSensors", "capability.temperatureMeasurement", title: "Which temp sensors? Logs only when a Thermostat temp is logged", required: false, multiple: true
        input "thermostats", "capability.thermostat", title: "Thermostats", required: false, multiple: true
        input "contact1", "capability.contactSensor", title: "Which Doors?", required: false, multiple: true 
        input "switches", "capability.switch", title: "Which switches?", required: false, multiple: true 
        input "people", "capability.presenceSensor", title: "Log These People Coming & Going", required: false, multiple: true
        input "motionSensors", "capability.motionSensor", title: "Choose motion sensor", required: false, multiple: true
        input "energymeters", "capability.EnergyMeter", title: "Energy Meter", required: false, multiple: true
        input "powermeters", "capability.powerMeter", title: "Power Meter", required: false, multiple: true
        }
    section("Mysql info") {   
        input("ServerIP", "string", title:"Server IP Address", required: true)
        input("SecsBetweenEnergy", "number", title:"Minutes Between Energy logging (15)", required: true)
        input("SecsBetweenPower", "number", title:"Minutes Between Power logging (2)", required: true)
        input("wattsDelta", "number", title:"Min Change in Watts for logging", required: true)
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
	unschedule()
    //subscribe(tempSensors, "temperature", temperatureHandler)
    subscribe(thermostats, "temperature", temperatureHandler)
    subscribe(thermostats, "thermostatOperatingState", opStateHandler)
    subscribe(contact1, "contact", doorHandler)
    subscribe(switches, "switch", switchHandler)
    subscribe(people, "presence", presenceHandler)
    subscribe(motionSensors, "motion", motionHandler)
    subscribe(energymeters, "energy", energyHandler)
    subscribe(powermeters, "power", powerHandler)
    
    state.lastTempLogged = now() - (20 * 60)
    state.lastEnergyLogged = now() - (20 * 60)
    state.lastPowerLogged = now() - (20 * 60)
    state.lastPowerValue = 1000
    log.debug "State.lastE: ${state.lastEnergyLogged} , Now(): ${now()}"

 }



def temperatureHandler(evt) {
    def TimeSince = (now() - state.lastTempLogged) / 1000
		log.debug "tempHandler TimeSince = ${TimeSince}"

	if(TimeSince >= 3) {
        def url = "http://${ServerIP}/st/event.php?table=temp&deviceName=${URLEncoder.encode(evt.displayName)}&event=${URLEncoder.encode(evt.descriptionText)}&source=${evt.source}&value=${evt.value}&date=${evt.isoDate}"
        httpGet(url) { 
            response ->
            if (response.status != 200 ) {
                log.trace "temperatureHandler (failed) URL: ${url}"
                //log.debug "failed, status = ${response.status}"
            } else {
                log.trace "temperatureHandler (success) URL: ${url}"
                //log.debug "success, status = ${response.status}"
            }
        }

        for (mySensor in tempSensors) {
            def temp = mySensor.currentValue("temperature")
            def name = mySensor.name
            def label = mySensor.label
            //log.debug "Sensor Name: ${name}, Label: ${label}, Value: ${temp}"

            url = "http://${ServerIP}/st/event.php?table=temp&deviceName=${URLEncoder.encode(name)}&source=${evt.source}&value=${temp}&date=${evt.isoDate}"
            httpGet(url) { 
                response ->
                if (response.status != 200 ) {
                    log.trace "tempSensorsLoop (failed) URL: ${url}"
                    //log.debug "failed, status = ${response.status}"
                } else {
                    log.trace "tempSensorsLoop (success) URL: ${url}"
                    //log.debug "success, status = ${response.status}"
                }
            }
            pause(55)
        }
        state.lastTempLogged = now()
    } else {
    	log.debug "TimeSince(${TimeSince}) Not Greater than 3."
    }
}
            
/*            
def temperatureHandler(evt) {
	if (evt.isStateChange()){
    	def url = "http://${ServerIP}/st/event.php?table=temp&stDeviceID=${URLEncoder.encode(evt.deviceId)}&deviceName=${URLEncoder.encode(evt.displayName)}&event=${URLEncoder.encode(evt.descriptionText)}&source=${evt.source}&value=${evt.value}&date=${evt.isoDate}"
        httpGet(url) { 
            response ->
            if (response.status != 200 ) {
            	log.trace "temperatureHandler (failed) URL: ${url}"
                //log.debug "failed, status = ${response.status}"
            } else {
            	log.trace "temperatureHandler (success) URL: ${url}"
                //log.debug "success, status = ${response.status}"
            }
        }
    }
}
*/

def opStateHandler(evt) {
	log.debug "opStateHandler: ${evt.descriptionText}"
	if (evt.isStateChange()){
    	 def tm = thermostats.currentThermostatMode
    	 //def name = "${evt.displayName} (${evt.value})"
         //thermostat.currentThermostatOperatingState
         def url = "http://${ServerIP}/st/event.php?table=switches&stDeviceID=${URLEncoder.encode(evt.deviceId)}&deviceName=${URLEncoder.encode(evt.displayName)}&event=${URLEncoder.encode(evt.descriptionText)}&source=${evt.source}&value=${evt.value}&date=${evt.isoDate}&tm=${tm}"
        //log.debug "URL: ${url}"
        httpGet(url) {
            response ->
            if (response.status != 200 ) {
            	log.trace "opStateHandler (failed) URL: ${url}"
                //log.debug "failed, status = ${response.status}"
            } else {
            	log.trace "opStateHandler (success) URL: ${url}"
                //log.debug "success, status = ${response.status}"
            }
        }
    }
}
def doorHandler(evt) {
	if (evt.isStateChange()){
    	def url = "http://${ServerIP}/st/event.php?table=doors&stDeviceID=${URLEncoder.encode(evt.deviceId)}&deviceName=${URLEncoder.encode(evt.displayName)}&event=${URLEncoder.encode(evt.descriptionText)}&source=${evt.source}&value=${evt.value}&date=${evt.isoDate}"
        httpGet(url) {
            response ->
            if (response.status != 200 ) {
            	log.trace "doorHandler (failed) URL: ${url}"
                //log.debug "failed, status = ${response.status}"
            } else {
            	log.trace "doorHandler (success) URL: ${url}"
                //log.debug "success, status = ${response.status}"
            }
        }
    }
}

/*
def dimmerHandler(evt) {
	if (evt.isStateChange()){
        def url = "http://${ServerIP}/st/event.php?table=switches&stDeviceID=${URLEncoder.encode(evt.deviceId)}&deviceName=${URLEncoder.encode(evt.displayName)}&event=${URLEncoder.encode(evt.descriptionText)}&source=${evt.source}&value=${evt.value}&date=${evt.isoDate}&smartapp=${evt.installedSmartAppId}"
        httpGet(url) {
            response ->
            if (response.status != 200 ) {
            	log.trace "dimmerHandler (failed) URL: ${url}"
                //log.debug "failed, status = ${response.status}"
            } else {
            	log.trace "dimmerHandler (success) URL: ${url}"
                //log.debug "success, status = ${response.status}"
            }
        }
    }
}
*/
def switchHandler(evt) { 
	if (evt.isStateChange()){
    	def url = "http://${ServerIP}/st/event.php?table=switches&stDeviceID=${URLEncoder.encode(evt.deviceId)}&deviceName=${URLEncoder.encode(evt.displayName)}&event=${URLEncoder.encode(evt.descriptionText)}&source=${evt.source}&value=${evt.value}&date=${evt.isoDate}&smartapp=${evt.installedSmartAppId}&level=${level}"
        if (evt.device?.hasAttribute("level")) {
            def level = evt.device.currentState("level").stringValue
			url = url + "&level=" + level
        }
        httpGet(url) {
            response ->
            if (response.status != 200 ) {
                log.trace "switchHandler (failed) URL: ${url}"
                //log.debug "failed, status = ${response.status}"
            } else {
                log.trace "switchHandler (success) URL: ${url}"
                //log.debug "success, status = ${response.status}"
            }
        }
    }
}

def presenceHandler(evt) {    	
    	def url = "http://${ServerIP}/st/event.php?table=people&stDeviceID=${URLEncoder.encode(evt.deviceId)}&deviceName=${URLEncoder.encode(evt.displayName)}&event=${URLEncoder.encode(evt.descriptionText)}&source=na&value=${URLEncoder.encode(evt.value)}&date=${evt.isoDate}"
        //def url = "http://${ServerIP}/st/event.php?table=people&stDeviceID=uknownDeviceID&deviceName=${URLEncoder.encode(evt.displayName)}&event=${URLEncoder.encode(evt.descriptionText)}&source=uknownSource&value=uknownValue"
       	httpGet(url) {
            response ->
            if (response.status != 200 ) {
            	log.trace "presenceHandler (failed) URL: ${url}"
                //log.debug "failed, status = ${response.status}"
            } else {
            	log.trace "presenceHandler (success) URL: ${url}"
                //log.debug "success, status = ${response.status}"
            }
        }
}


def motionHandler(evt) {
	if (evt.isStateChange()){
    	def url = "http://${ServerIP}/st/event.php?table=motion&stDeviceID=${URLEncoder.encode(evt.deviceId)}&deviceName=${URLEncoder.encode(evt.displayName)}&event=${URLEncoder.encode(evt.descriptionText)}&source=${evt.source}&value=${evt.value}&date=${evt.isoDate}"
        httpGet(url) {
            response ->
            if (response.status != 200 ) {
            	log.trace "motionHandler (failed) URL: ${url}"
                //log.debug "failed, status = ${response.status}"
            } else {
            	log.trace "motionHandler (success) URL: ${url}"
                //log.debug "success, status = ${response.status}"
            }
        }
    }
}

def energyHandler(evt) {
	if (evt.isStateChange()){
    	def lastlogged = (now() - state.lastEnergyLogged)/1000
        def delay = ((SecsBetweenEnergy *60) - 1)
        
        if (lastlogged >= delay) { //log only ever 15 minutes
        	log.debug "Energy last logged: ${lastlogged} >= ${delay}"
            def url = "http://${ServerIP}/st/event.php?table=energy&stDeviceID=${URLEncoder.encode(evt.deviceId)}&deviceName=${URLEncoder.encode(evt.displayName)}&event=${URLEncoder.encode(evt.descriptionText)}&source=${evt.source}&value=${evt.value}&date=${evt.isoDate}"
            httpGet(url) {
                response ->
                if (response.status != 200 ) {
                    log.trace "energyHandler (failed) URL: ${url}"
                    //log.debug "failed, status = ${response.status}"
                } else {
                    log.trace "energyHandler (success) URL: ${url}"
                    //log.debug "success, status = ${response.status}"
                    state.lastEnergyLogged = now()
                }
            }
        } else {
        	log.debug "Sorry, can only logged energy every ${delay} Seconds, its only been= ${lastlogged}"
        }
    }
}

def powerHandler(evt) {
	if (evt.isStateChange()){
    	def TimeSince = (now() - state.lastPowerLogged) / 1000
        def DeltaSince = (state.lastPowerValue - evt.floatValue).abs()
        def delay = ((SecsBetweenPower * 60) - 1)

			def url = ""
            def onSwitches = ""
            for (myswitch in switches) {
                if (myswitch.currentValue("switch") == "on") {
                    def evt2 = myswitch.events(max: 1)
                    onSwitches = onSwitches + myswitch.displayName + ": " + evt2.isoDate + ","
                }
            }
            /*
            for (mytherm in thermostats) {
                if (mytherm.currentValue("switch") == "on") {
                    def evt2 = mytherm.events(max: 1)
                    def tm = mytherm.currentThermostatMode
                    if (tm == "[heat]") {
                    	def ThermDispName = mytherm.displayName + " - heating"
                    } else if (tm == "[cool]") {
                    	def ThermDispName = mytherm.displayName + " - cooling"
                    }
                    
                    onSwitches = onSwitches + mytherm.displayName + ": " + evt2.isoDate + ","
                    
                }
            }
            */
            if (onSwitches == "") {
                url = "http://${ServerIP}/st/event.php?table=power&stDeviceID=${URLEncoder.encode(evt.deviceId)}&deviceName=${URLEncoder.encode(evt.displayName)}&event=${URLEncoder.encode(evt.descriptionText)}&source=${evt.source}&value=${evt.value}&date=${evt.isoDate}"
            } else {
            	url = "http://${ServerIP}/st/event.php?table=power&stDeviceID=${URLEncoder.encode(evt.deviceId)}&deviceName=${URLEncoder.encode(evt.displayName)}&event=${URLEncoder.encode(evt.descriptionText)}&source=${evt.source}&value=${evt.value}&date=${evt.isoDate}&switchesOn=${URLEncoder.encode(onSwitches)}"
            }

        log.debug "powerHandler --> ULR: ${url}"
    	if (TimeSince >= delay ) { //log only ever 2 minutes 

        	log.debug "Power TimeSince last logged: ${TimeSince} >= ${delay}"
        	if (DeltaSince >= wattsDelta ) {
                log.debug "Power wattsDelta last logged: ${DeltaSince} >= ${wattsDelta}"
                //def url = "http://${ServerIP}/st/event.php?table=power&stDeviceID=${URLEncoder.encode(evt.deviceId)}&deviceName=${URLEncoder.encode(evt.displayName)}&event=${URLEncoder.encode(evt.descriptionText)}&source=${evt.source}&value=${evt.value}&date=${evt.isoDate}"
                httpGet(url) {
                    response ->
                    if (response.status != 200 ) {
                        log.trace "powerHandler (failed) URL: ${url}"
                        //log.debug "failed, status = ${response.status}"
                    } else {
                        log.trace "powerHandler (success) URL: ${url}"
                        //log.debug "success, status = ${response.status}"
                        state.lastPowerLogged = now()
                        state.lastPowerValue = evt.floatValue
                    }
                }
            }
        } else {
        	log.debug "Sorry, can only logged Power every ${delay} Seconds, its only been= ${TimeSince}"
        }
    }
}