


// Automatically generated. Make future change here.
definition(
    name: "Sunrise/Set With Offset",
    namespace: "",
    author: "",
    description: "Turn ON light(s) and/or dimmer(s) when there's movement and the room is dark with luminescence threshold and/or between sunset and sunrise. Then turn OFF after X minute(s) when the brightness of the room is above the luminescence threshold or turn OFF after X minute(s) when there is no movement.",
    category: "My Apps",
    iconUrl: "http://neiloseman.com/wp-content/uploads/2013/08/stockvault-bulb128619.jpg",
    iconX2Url: "http://neiloseman.com/wp-content/uploads/2013/08/stockvault-bulb128619.jpg",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
    section("Lights") {
        input "switches", "capability.switchLevel", title: "Which lights to turn on?"
        input "dimLevel", "number", title: "Brightness Level (10-100)"
        //input "testbutton", "capability.switch", title: "test"
    }
    
    section("Offsets") {
        input "sunriseOffset", "number", title: "Turn off this many minutes before sunrise"
        input "sunsetOffset", "number", title: "Turn on this many minutes after sunset"
    }   
    

}


def installed() {
	sendNotificationEvent("Installing 'Sunrise/Set With Offset'with $switches.label as you requested: {$settings)")
    initialize()
}


def updated() 
{
	sendNotificationEvent("Updating On/Off Times for $switches.label as you requested: ($settings)")
    unsubscribe()
    initialize()
}

def initialize() {
	
    subscribe(location, "sunset", sunsetHandler)
    //subscribe(testbutton, "switch.on", myHandler)
    //subscribe(testbutton, "switch.off", myHandler)
    
    //schedule it to run today too
    //scheduleTurnOn(location.currentValue("sunsetTime"))
    scheduleNextSunset()
    scheduleNextSunrise()
}


def myHandler(evt)
{
	myswitch.setLevel(65)
	log.debug "myhandler evt: $evt.value"
}


def sunsetHandler(evt) {
	runIn(${sunsetOffset * 60}, turnOn)
    log.debug "The Sun Has Set..."
    scheduleNextSunrise()
}


def scheduleNextSunrise() {
	//def sunriseWoffset = getSunriseAndSunset(sunriseOffset: "-00:${offset}")
    
    scheduleTurnOff(location.currentValue("sunriseTime"))
}

def scheduleNextSunset() {
	//def sunriseWoffset = getSunriseAndSunset(sunriseOffset: "-00:${offset}")
    
    scheduleTurnOn(location.currentValue("sunsetTime"))
}

def scheduleTurnOff(sunrisetString) {
    def sunsetTime = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", sunrisetString)

    //calculate the offset
    def timeBeforeSunrise = new Date(sunsetTime.time - (sunriseOffset * 60 * 1000))

    log.debug "Scheduling (off) for: $timeBeforeSunrise (sunrise is $sunrisetString)"

    //schedule this to run one time
    runOnce(timeBeforeSunrise, turnOff)
}


def scheduleTurnOn(sunsetString) {
    //get the Date value for the string
    //log.debug "sunsetString: $sunsetString"
    def sunsetTime = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", sunsetString)

    //calculate the offset
    def timeAfterSunset = new Date(sunsetTime.time + (sunsetOffset * 60 * 1000))

    log.debug "Scheduling (On) for: $timeAfterSunset (sunrise is $sunsetTime)"

    //schedule this to run one time
    runOnce(timeAfterSunset, turnOn)
}


def turnOn() {
    log.debug "turning on lights"
    switches.setLevel(dimLevel)
    sendNotificationEvent("The sunset $sunsetOffset minutes ago. Dimming the $switches.label to ${dimLevel}% as you requested")
}

def turnOff() {
    log.debug "turning off lights"
    sendNotificationEvent("The sun will rise in $sunriseOffset minutes. Turning the $switches.label off as you requested")
    switches.off()
}