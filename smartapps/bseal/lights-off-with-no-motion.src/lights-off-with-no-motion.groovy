definition(
    name: "Lights Off with No Motion",
    namespace: "bseal",
    author: "Brian Seal",
    description: "Turn lights off when no motion is detected",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_presence-outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_presence-outlet@2x.png"
)

preferences {
	section("Light switches to turn off") {
		input "switches", "capability.switch", title: "Choose light switches", multiple: true
	}
	section("Motion Sensor") {
		input "motionSensor", "capability.motionSensor", title: "Choose motion sensor"
	}
    section("Delay after motion stops") {
        input "delayMins", "number", title: "Delay in Minutes", defaultValue:0
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
	subscribe(motionSensor, "motion", motionHandler) 
}

def motionHandler(evt) {
	
  if("inactive" == evt.value) {
    log.debug "handler $evt.name: $evt.value"
    runIn(60*delayMins, scheduleCheck)
    
  } else if("active" == evt.value) {
    log.debug "handler $evt.name: $evt.value"
    unschedule("scheduleCheck")
  }
}


def scheduleCheck() {
	log.debug "scheduled check"
	def motionState = motionSensor.currentState("motion")
    if (motionState.value == "inactive") {
        def elapsed = now() - motionState.rawDateCreated.time
    	def threshold = 1000 * 60 * delayMins - 1000
        log.debug "elapsed: ${elapsed}, threshold: ${threshold}"
    	if (elapsed >= threshold) {
            log.debug "Motion has stayed inactive since last check ($elapsed ms):  turning lights off"
            switches.off()
    	} else {
        	log.debug "Motion has not stayed inactive long enough since last check ($elapsed ms): do nothing"
        }
    } else {
    	log.debug "Motion is active: do nothing"
    }
}