/**
 *  Light on when closed, light off when open.
 *
 *  Copyright 2014 Dav Glass
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
    name: "Take Action to Door Open/Close",
    namespace: "",
    author: "",
    description: "Turn things on/off when a door open/closes",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)


preferences {
    section("Which Door?") {
        input "door", "capability.contactSensor", multiple: false, required: true
    }
    section("When Door Closes Turn...") {
        input "closed_thingsOn", "capability.switch", title: "These On", multiple: true, required: false
        input "closed_thingsOff", "capability.switch", title: "These Off", multiple: true, required: false
    }
    section("When Door Opens Turn...") {
        input "opened_thingsOn", "capability.switch", title: "These On", multiple: true, required: false
        input "opened_thingsOff", "capability.switch", title: "These Off", multiple: true, required: false
    }
    section("Delay off for seconds?") {
    	input "seconds", "number", title: "Seconds", required: false
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"

    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"

    unsubscribe()
    unschedule()
    initialize()
}

def initialize() {
    subscribe(door, "contact", handler)
}

def closed_off() {
    closed_thingsOff.each {
    	log.debug "closed: Turning off ${it.displayName}"
        it.off()
	}
}

def closed_on() {
	closed_thingsOn.each {
        log.debug "closed: Turning on ${it.displayName}"
    	it.on()
	}
}

def open_off() {
    opened_thingsOff.each {
    	log.debug "opened: Turning off ${it.displayName}"
        it.off()
	}
}

def open_on() {
	opened_thingsOn.each {
        log.debug "opened: Turning on ${it.displayName}"
    	it.on()
	}
}

def handler(evt) {
	log.debug "${evt.displayName} is ${evt.value}"
	if (evt.value == "open") {
		open_off()
        open_on()
    } else {
		closed_on()
        closed_off()
    }
}