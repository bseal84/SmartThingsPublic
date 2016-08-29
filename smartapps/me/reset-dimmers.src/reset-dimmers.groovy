/**
 *  Turn on bright
 *
 *  Copyright 2015 Eric Roberts
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
    name: "Reset Dimmers",
    namespace: "me",
    author: "Brian Seal",
    description: "Everyday between specified times if a door is opened, reset selected dimmers to 100% by turning them all on for 2 sec. then off",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    page(name: "configure")
}

def configure() {
    dynamicPage(name: "configure", title: "Configure Switch and Phrase", install: true, uninstall: true) {
            section ("When the door opens...") {
                input "contact1", "capability.contactSensor", title: "Which Door?", required: true, multiple: false
            }
            section("Reset the following dimmers to 100%") {
                input "dimmers", "capability.switchLevel", title: "Which dimmers", required: true, multiple: true
            }
            section("Only during the following times...") {
                input "timeBegin", "time", title: "Start time", required: true
                input "timeEnd", "time", title: "End Time", required: true
            }  
            section("Time to reset") {
                input "time1", "time", required: true
            }
            section("Lights will be set to 100% for... ") {
                input "offDelaySec", "number", title: "Seconds", required: true
            }

            def actions = location.helloHome?.getPhrases()*.label
            if (actions) {
            actions.sort()
                 section("Hello Home Actions") {
                      log.trace actions
                	  input "PhraseAction", "enum", title: "Routine to execute", options: actions, required: true
                }
            }
    }
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
	unschedule()
    //subscribe(dimmers, "switch.on", onHandler)
    //resetRan2day()
    state.ran2day = false
    //subscribe(location, changedLocationMode)
    //subscribe(location, "mode", modeChangeHandler)
    subscribe(contact1, "contact.open", openHandler)
    timebetween()
    schedule(time1, resetRan2day)
	//def stamp = new Date().format('yyyy-MM-dd hh:mm:ss',location.timeZone)
    //log.debug "timestamp: ${stamp}"  
 }
 
//def modeChangeHandler(evt) {
//    log.debug "mode changed to ${evt.value}"
    //if
//    state.ran2day = false
//}

def openHandler(evt) {
	//dimmers.setLevel(100)
    //betweentimes = timebetween()
	if(timebetween() && state.ran2day == false){
    	log.debug "contact: ${evt}, ran2day = ${state.ran2day}, resetting dimmers"
		dimmers.setLevel(100)
        //def delay = offDelaySec * 60
        //log.debug "Turning Dimmers off in (${turnOffDimmers} seconds)"
        runIn(offDelaySec, turnOffDimmers)
        state.ran2day = true
        location.helloHome?.execute(settings.PhraseAction)
    } else{
    	log.debug "contact: ${evt}, ran2day = ${state.ran2day}, ${timebetween()} - not resetting dimmers"
    }
}


def turnOffDimmers() {
	log.debug "Turning Dimmers off now"
	dimmers.setLevel(0)
}

def timebetween(){
    def now = new Date()
    def startCheck = timeToday(timeBegin)
    def stopCheck = timeToday(timeEnd)
    
    log.debug "now: ${now}"
    log.debug "startCheck: ${startCheck}"
    log.debug "stopCheck: ${stopCheck}"
    
    def between = timeOfDayIsBetween(startCheck, stopCheck, now, location.timeZone)
    
    log.debug "between: ${between}"
    return between
}

def resetRan2day() {
	state.ran2day = false
    sendNotificationEvent("Debug: Reset state.ran2day = ${state.ran2day}")

}