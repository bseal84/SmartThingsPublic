

definition(
    name: "Lights Off with motion/door IF 1 person is present",
    namespace: "bseal",
    author: "Brian Seal",
    description: "Turn lights off when motion/door active/opens, and only 1 person is home.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_presence-outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_presence-outlet@2x.png"
)

preferences {
	section("When only 1 of these people are home") {                    
		input "people", "capability.presenceSensor", title: "People", multiple: true
	}
	section("And a Door opens, or Motion is detected from") {
		input "motionSensor", "capability.motionSensor", title: "Choose sensor(s)", multiple: true
        input "doorSensors", "capability.contactSensor", multiple: true
	}
	section("Turn off These Lights") {
		input "switches", "capability.switch", title: "Choose lights", multiple: true
	}
}

def installed() {
	subscribeToEvents()
}

def updated() {
	unsubscribe()
	subscribeToEvents()
}

def subscribeToEvents() {
	subscribe(motionSensor, "motion.active", sensornHandler) 
    subscribe(doorSensors, "contact.open", sensornHandler);
    subscribe(switches, "switch.on", switchOnHandler, [filterEvents: false])
    state.someOn = true
}

def sensornHandler(evt) {

	if (state.someOn == true) {
        if (OnlyOneHome()) {
            log.debug "${evt.descriptionText}, Only 1 is home, turning off selected Lights"
            lightsOff()
        } else {
        	log.debug "Some Lights are on and the ${evt.descriptionText}, but more than 1 is home, exiting..."
        }
   
	} else {
    	log.debug "sensornHandler1984: ${evt.descriptionText}, state.someOn == false - aka no lights need turned off"
    }
}

def switchOnHandler(evt) {
	log.debug "switchOnHandler1984: ${evt.descriptionText}, setting state.someOn = true"
	state.someOn = true
}

private OnlyOneHome() {
	def result = false

    def countPeople = people.findAll { it?.latestValue("presence") == "present" }
    
    if (countPeople.size() == 1) {
    	log.debug "OnlyOneHome = true. ${countPeople.size()} of ${people.size()} person(s) are home"
    	result = true
    } else {
    	log.debug "OnlyOneHome = false. ${countPeople.size()} of ${people.size()} person(s) are home"
    }
  return result
}

private lightsOff() {
    state.someOn = false
    switches.off()
}
