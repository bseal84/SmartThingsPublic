
definition(
    name: "Thermostat by Threshold",
    namespace: "",
    author: "Brian Seal",
    description: "Test app for thermostat program",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")



preferences() {
	section("Choose thermostat... ") {
		input "thermostat", "capability.thermostat"
	}
    
	section("While Home:") {
		input "homeMax", "decimal", title: "Start cooling when the temperature reaches:"
        input "homeCoolto", "decimal", title: "Cool Down To..."
	}
    
	section("While Away:") {
        input "awayMax", "decimal", title: "Start cooling when the temperature reaches:"
        input "awayCoolto", "decimal", title: "And Cool Down To..."
	}    
    
	section("At Night:") {
		input "nightMax", "decimal", title: "Start cooling when the temperature reaches:"
        input "nightCoolto", "decimal", title: "Cool Down To..."
	}

    
}


def installed()
{
	subscribeToEvents()
    log.debug "Installing App..."
    updateSetpoints()
    
}

def updated()
{
	unsubscribe()
	subscribeToEvents()
    log.debug "Updating App..."
    updateSetpoints()
    
}

def subscribeToEvents()
{
	subscribe(location, changedLocationMode)
    subscribe(thermostat, "temperature", temperatureHandler)
    subscribe(thermostat, "thermostatOperatingState.idle", StateHandler)
    subscribe(thermostat, "thermostatOperatingState.heating", StateHandler)
}


def changedLocationMode(evt)
{
	updateSetpoints()
    log.debug "Mode Change detected..." //sendNotificationEvent("Installing 'Sunrise/Set With Offset' with $switches.label as you requested: {$settings)")
}


def updateSetpoints()
{
    def curMode = location.currentMode
    def currentTemp = thermostat.currentTemperature
    //log.debug "The current mode name is: ${curMode.name}"
    switch (curMode) {
        case "Home":
            state.maxTemp = homeMax
            state.coolTo = homeCoolto
            break
        case "Away": awayMax
            //log.debug "Current Mode = 'Away'"
            state.maxTemp = awayMax
            state.coolTo = awayCoolto
            thermostat.setCoolingSetpoint(state.maxTemp + 2)
            break
        case "Night":
            //log.debug "Current Mode = 'Night'"
            state.maxTemp = nightMax
            state.coolTo = nightCoolto
            break
        default:
            //log.debug "Current Mode = '?', using default"
            state.maxTemp = homeMax
            state.coolTo = homeCoolto
            break	
    }	
    evaluate()
	log.debug "Mode = $curMode, MaxTemp: $state.maxTemp  ****  CootTo: $homeCoolto"
    sendNotificationEvent("$thermostat.label threshold set to start Cooling at $state.maxTemp,  and stop cooling at $state.coolTo")
/*     
    if (thermostat.currentthermostatOperatingState in ["cooling","cool"]){
        log.debug "updateSetpoints handler - ThermostatOperatingState is currently cooling, currentsetCoolingSetpoint(${thermostat.currentCoolingSetpoint}) --- thermostat.setCoolingSetpoint(${state.coolTo - 2})"
        thermostat.setCoolingSetpoint(state.coolTo - 2)
    }
    else {
        log.debug "updateSetpoints handler - ThermostatOperatingState is currently NOT Cooling. Checking Enviroment Settings and making required changes.)"
        evaluate()
    }
*/  


}


def temperatureHandler(evt)
{
	def currentTemp = thermostat.currentTemperature
	//log.debug "TemperatureHandler: $thermostat.currentTemperature F"
    //log.debug "temperatureHandler: Current_Temp ($currentTemp) ---- coolto ($coolto) ----  maxTemp ($maxTemp) ----  currentsetCoolingSetpoint(${thermostat.currentCoolingSetpoint})"
	evaluate()
}

StateHandler

def StateHandler(evt) {
	log.debug "opStateHandler: ${evt.descriptionText}"
}


private evaluate()
{
	
		def tm = thermostat.currentThermostatMode
		def currentTemp = thermostat.currentTemperature
        def currentState = thermostat.currentThermostatOperatingState
	   
/*       
		if (tm in ["cool","auto"]) {
            try{
                if ((currentTemp - maxTemp) >= 1) { //Turn on AC
                    thermostat.setCoolingSetpoint(coolto - 2)
                    log.debug "TURNING ON --- ct(${currentTemp}) - maxTemp(${maxTemp}) =  ${currentTemp - maxTemp} >= 1 --- thermostat.setCoolingSetpoint(${coolto - 2})"
                }
                else if ((coolto - currentTemp) >= 1) { //Turn off AC

                    thermostat.setCoolingSetpoint(maxTemp + 2)
                    log.debug "TURNING OFF --- coolto(${coolto}) -  ct(${currentTemp}) =  ${coolto - currentTemp} >= 1 --- thermostat.setCoolingSetpoint(${maxTemp + 2})"

                }
                else {
                    log.debug "No Condition Met: Current_Temp ($currentTemp) ---- coolto ($coolto) ----  maxTemp ($maxTemp) ----  currentsetCoolingSetpoint(${thermostat.currentCoolingSetpoint})"

                }
            }//END TRY
            catch(e){log.debug "NO TEMP READING FROM SENSOR"}
		}
*/

		if (tm in ["cool","auto"]) {
            try{
                if (currentTemp >= state.maxTemp) { //Turn on AC
                    thermostat.setCoolingSetpoint(state.coolTo - 2)
                    log.debug "TURNING ON --- ct(${currentTemp}) - maxTemp(${state.maxTemp}) =  ${currentTemp - state.maxTemp} >= 1 --- thermostat.setCoolingSetpoint(${state.coolTo - 2})"
                }
                else if (currentTemp <= state.coolTo) { //Turn off AC
                    thermostat.setCoolingSetpoint(state.maxTemp + 2)
                    log.debug "TURNING OFF --- coolto(${state.coolTo}) -  ct(${currentTemp}) =  ${state.coolTo - currentTemp} >= 1 --- thermostat.setCoolingSetpoint(${state.maxTemp + 2})"

                }
                else {
                    log.debug "No Condition Met: Current_Temp ($currentTemp) ---- coolto ($state.coolTo) ----  maxTemp ($state.maxTemp) ----  currentsetCoolingSetpoint(${thermostat.currentCoolingSetpoint})"

                }
            }//END TRY
            catch(e){log.debug "NO TEMP READING FROM SENSOR"}
		}

}


 

