definition(
    name: "Thermostat by Threshold_2",
    namespace: "",
    author: "Brian Seal",
    description: "Test app for thermostat program",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")



preferences() {
	section("Choose thermostat... ") {
		input "thermostat", "capability.thermostat", title: "Parent Thermostat"
        input "V_thermostat", "capability.thermostat", title: "Virtual Thermostat"
	}
/*     
	section("Temperature Ranges") {
		input "homeMax", "decimal", title: "Start cooling when the temperature reaches:"
        input "homeCoolto", "decimal", title: "Cool Down To..."
	}
*/
    
}


def installed() {
	subscribeToEvents()
    log.debug "Installing App..."
	//atomicState.startTemp = V_thermostat.currentHeatingSetpoint
	//atomicState.stopTemp = V_thermostat.currentCoolingSetpoint
    evaluate()
    
}

def updated() {
	unsubscribe()
	subscribeToEvents()
    log.debug "Updating App..."
	//atomicState.startTemp = V_thermostat.currentHeatingSetpoint
	//atomicState.stopTemp = V_thermostat.currentCoolingSetpoint
    evaluate()
    
}

def subscribeToEvents() {
	subscribe(location, changedLocationMode)
    subscribe(thermostat, "temperature", temperatureHandler)
    //subscribe(thermostat, "thermostatOperatingState.idle", idleStateHandler)
    //subscribe(thermostat, "thermostatOperatingState.cooling", coolingStateHandler)
    
    subscribe(V_thermostat, "heatingSetpoint", startSetpointHandler) 
    subscribe(V_thermostat, "coolingSetpoint", stopSetpointHandler) 
    //log.debug "Subscribe: (Mode: ${location.currentMode}) -- Start Cooling at: $V_thermostat.currentHeatingSetpoint, Stop cooling at: $V_thermostat.currentCoolingSetpoint"
}

def startSetpointHandler(evt){
	//atomicState.startTemp = V_thermostat.currentHeatingSetpoint
	//log.debug "Start Cooling CHanged to: ${V_thermostat.currentHeatingSetpoint} -- (Mode: ${location.currentMode})"
    log.debug "Start Cooling changed to: ${V_thermostat.currentHeatingSetpoint}"
    evaluate()
}

def stopSetpointHandler(evt){
	//atomicState.stopTemp = V_thermostat.currentCoolingSetpoint
	//log.debug "Stop Cooling CHanged to: ${V_thermostat.currentHeatingSetpoint} -- (Mode: ${location.currentMode})"
    log.debug "Stop Cooling CHanged to: ${V_thermostat.currentCoolingSetpoint}"
    evaluate()
}

def changedLocationMode(evt)
{	
	/*
	if(atomicState.startTemp == null){
    	V_thermostat.setHeatingSetpoint(75) //Start HVAC Temp
        log.debug "StartTemp was Null, setting to default of 75"
        
    } else {
    	V_thermostat.setHeatingSetpoint(V_thermostat.currentHeatingSetpoint) //Start HVAC Temp
    }
    
	if(atomicState.stopTemp == null){
    	V_thermostat.setCoolingSetpoint(73) //Stop HVAC Temp
        log.debug "stopTemp == Null, setting to default of 73"
    } else {
    	V_thermostat.setCoolingSetpoint(V_thermostat.currentCoolingSetpoint) //Stop HVAC Temp
    }
    
    evaluate()
    log.debug "Mode: ${evt.value} -- $thermostat.label threshold set to start Cooling at $V_thermostat.currentHeatingSetpoint, and stop cooling at $V_thermostat.currentCoolingSetpoint"
    sendNotificationEvent("Mode Change detected...(${evt.value}) -- $thermostat.label threshold set to start Cooling at $V_thermostat.currentHeatingSetpoint, and stop cooling at $V_thermostat.currentCoolingSetpoint")
    */
}


def temperatureHandler(evt)
{
	//def currentTemp = thermostat.currentTemperature
	evaluate()
}



private evaluate()
{
    def tm = thermostat.currentThermostatMode
    def currentTemp = thermostat.currentTemperature
    def currentState = thermostat.currentThermostatOperatingState
    
    def start = V_thermostat.currentHeatingSetpoint
    def stop = V_thermostat.currentCoolingSetpoint
    
    //def start = atomicState.startTemp
    //def stop = atomicState.stopTemp

    if (tm in ["cool","auto"]) {
        //try{
        	//log.debug "Try - CurrentTemp: ${currentTemp}, StartTemp: ${start}, StopTemp: ${stop}"
            if (currentTemp >= start) { //Turn on AC
                thermostat.setCoolingSetpoint(stop - 2)
                //log.debug "TURNING ON --- ct(${currentTemp}) - maxTemp(${start}) =  ${currentTemp - start} >= 1 --- thermostat.setCoolingSetpoint(${stop - 2})"
                log.debug "TURNING ON -- CurrentTemp: ${currentTemp}, StartTemp: ${start}, StopTemp: ${stop}, thermostat.setCoolingSetpoint(${stop - 2})"
            }
            else if (currentTemp <= stop) { //Turn off AC
                thermostat.setCoolingSetpoint(start + 2)
                //log.debug "TURNING OFF --- coolto(${stop}) -  ct(${currentTemp}) =  ${stop - currentTemp} >= 1 --- thermostat.setCoolingSetpoint(${start + 2})"
				log.debug "TURNING OFF -- CurrentTemp: ${currentTemp}, StartTemp: ${start}, StopTemp: ${stop}, thermostat.setCoolingSetpoint(${start + 2})" 
            }
            else {
                log.debug "No Condition Met -- CurrentTemp: ${currentTemp}, StartTemp: ${start}, StopTemp: ${stop}"
				//log.debug "No Condition Met"
            }
       // }//END TRY
        //catch(e){log.debug "NO TEMP READING FROM SENSOR -- currentTemp: $currentTemp, coolto $stop, maxTemp: $start"}
    }
	//log.debug "Does it work? - startTemp: ${atomicState.startTemp}, stopTemp: ${atomicState.stopTemp}"
}