/**
 *  Double Tap Switch to Toggle
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
    name: "Double Tap Switch to Toggle",
    namespace: "Double Tap Switch to Toggle",
    author: "Brian Seal",
    description: "By double tapping a switch, either 'on' or 'off', you can toggle another switch / outlet",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("When this switch is double-tapped...") {
		input "master", "capability.switch", title: "Where?"
	}
	section("Which Outlet?") {
		input "myswitch", "capability.switch", title: "Outlet?"
	}
	section( "Notifications" ) {
		input "sendPushMessage", "enum", title: "Send a push notification?", metadata:[values:["Yes","No"]], required:false
		input "phoneNumber", "phone", title: "Send a Text Message?", required: false
	}
}

def installed() {

	subscribe(master, "switch", switchHandler, [filterEvents: false])
	subscribe(location, modeChangeHandler)

    def currentState = myswitch.currentState("switch").value
    log.debug "installed - myswitch State: $currentState"
}

def updated() {
	unsubscribe()
	subscribe(master, "switch", switchHandler, [filterEvents: false])
	subscribe(location, modeChangeHandler)

    def currentState = myswitch.currentState("switch").value
    log.debug "updated - myswitch State: $currentState"
}

def switchHandler(evt) {
	//log.info evt.value
    def currentState = myswitch.currentState("switch")
    log.debug "myswitch State: ${currentState.value}"
    
	// use Event rather than DeviceState because we may be changing DeviceState to only store changed values
	def recentStates = master.eventsSince(new Date(now() - 4000), [all:true, max: 10]).findAll{it.name == "switch"}
	log.debug "${recentStates?.size()} STATES FOUND, LAST AT ${recentStates ? recentStates[0].dateCreated : ''}"

	if (evt.isPhysical()) {
		if (evt.value == "on" && lastTwoStatesWere("on", recentStates, evt)) {
			log.debug "detected two 'on' taps"
			takeUpActions()
		} else if (evt.value == "off" && lastTwoStatesWere("off", recentStates, evt)) {
			log.debug "detected two 'off' taps"
			takeDownActions()
		}
	}
	else {
		log.trace "Skipping digital on/off event"
	}

}

def toggleSwitch() {
    def currentState = myswitch.currentState("switch").value
    log.debug "myswitch State: $currentState"
    
	if (currentState == "on") {  
		log.debug "Switch was on, so i'm turning OFF"
        myswitch.off()
    } else if (currentState == "off") {
    	log.debug "Switch was off, so i'm turning ON"
        myswitch.on()
    } 
}

def takeUpActions() {
	//log.debug "changeMode, location.mode = $location.mode, upMode = $upMode, location.modes = $location.modes"
    log.debug "takingUPaction..."
     toggleSwitch()
}

def takeDownActions() {
	//log.debug "changeMode, location.mode = $location.mode, downMode = $downMode, location.modes = $location.modes"
    log.debug "takingDOWNaction..."
     toggleSwitch()
}

private lastTwoStatesWere(value, states, evt) {
	def result = false
	if (states) {

		log.trace "unfiltered: [${states.collect{it.dateCreated + ':' + it.value}.join(', ')}]"
		def onOff = states.findAll { it.isPhysical() || !it.type }
		log.trace "filtered:   [${onOff.collect{it.dateCreated + ':' + it.value}.join(', ')}]"

		// This test was needed before the change to use Event rather than DeviceState. It should never pass now.
		if (onOff[0].date.before(evt.date)) {
			log.warn "Last state does not reflect current event, evt.date: ${evt.dateCreated}, state.date: ${onOff[0].dateCreated}"
			result = evt.value == value && onOff[0].value == value
		}
		else {
			result = onOff.size() > 1 && onOff[0].value == value && onOff[1].value == value
		}
	}
	result
}

private send(msg) {
	if ( sendPushMessage != "No" ) {
		log.debug( "sending push message" )
		sendPush( msg )
	}

	if ( phoneNumber ) {
		log.debug( "sending text message" )
		sendSms( phoneNumber, msg )
	}

	log.debug msg
}

def modeChangeHandler(evt) {
	state.modeStartTime=now()
}