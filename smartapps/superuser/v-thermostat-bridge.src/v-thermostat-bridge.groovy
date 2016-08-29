definition(
    name: "V-Thermostat Bridge",
    namespace: "",
    author: "Brian Seal",
    description: "Updates a Virtual Thermostat with an actual thermostats values",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")



preferences() {
	section("Choose thermostats... ") {
		input "P_thermostat", "capability.thermostat", title: "Parent Thermostat"
        input "V_thermostat", "capability.thermostat", title: "Virtual Thermostat"
	}
/*
	section("Button:") {
        input "thebutton", "capability.button", title: "which button"
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
*/
    
}


def installed() {
	subscribeToEvents()
    refresh()
    log.debug "Installing App..."
}

def updated()
{
	unsubscribe()
	subscribeToEvents()
    refresh()    
    log.debug "Updating App..."
}

def subscribeToEvents() {
    subscribe(P_thermostat, "temperature", temperatureHandler)
    subscribe(P_thermostat, "thermostatOperatingState", OperatingStateHandler)
    subscribe(P_thermostat, "thermostatFanMode", FanModeHandler)
    subscribe(P_thermostat, "thermostatMode", ModeHandler)
    //subscribe(V_thermostat, "heatingSetpoint", heatingSetpointHandler) 
    //subscribe(V_thermostat, "coolingSetpoint", coolingSetpointHandler) 
    //subscribe(thebutton, "button", buttonHandler)
}

def refresh() {
	V_thermostat.setTemperature(P_thermostat.currentTemperature)
    V_thermostat.setOperatingState(P_thermostat.currentOperatingState)
    V_thermostat.setThermostatFanMode(P_thermostat.currentThermostatFanMode)
    V_thermostat.setThermostatMode(P_thermostat.currentThermostatMode)
}

def temperatureHandler(evt) {
    V_thermostat.setTemperature(P_thermostat.currentTemperature)
    log.debug "temperatureHandler: ${evt.value}"
}

def OperatingStateHandler(evt){
	log.debug "OperatingStateHandler: ${evt.value}"
    V_thermostat.setOperatingState(P_thermostat.currentOperatingState)
}

def FanModeHandler(evt){
	log.debug "FanModeHandler: ${evt.value}"
    V_thermostat.setThermostatFanMode(P_thermostat.currentThermostatFanMode)
}

def ModeHandler(evt){
	log.debug "ModeHandler: ${evt.value}"
    V_thermostat.setThermostatMode(P_thermostat.currentThermostatMode)
}

/*
def heatingSetpointHandler(evt){
	log.debug "heatingSetpointHandler: ${evt.value}"
}

def coolingSetpointHandler(evt){
	log.debug "coolingSetpointHandler: ${evt.value}"
}
*/


/*  
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

}
*/  

def buttonHandler(evt) {
  log.debug "button was: ${evt}"
  //V_thermostat.sendEvent(name: "temperature", value: 72, unit: "F")
  //V_thermostat.sendEvent(name: "thermostatOperatingState", value: "idle")
  V_thermostat.SetOperatingState("idle")
  V_thermostat.setThermostatMode("cool") //cool, heat, off, auto
  V_thermostat.setThermostatFanMode("fanOn") //fanCirculate, fanOn, fanAuto

  //V_thermostat.setHeatingSetpoint(76) \\Start HVAC Temp
  //V_thermostat.setCoolingSetpoint(75) \\Stop HVAC Temp

}