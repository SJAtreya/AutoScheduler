package com.scheduler.booking.util

class SchedulerUtils {

	static def convertTime(String time) {
		def timeLen = time.length()
		def hoursAndMinutes = time.split(':')
		Double.valueOf(hoursAndMinutes[0]) + (Double.valueOf(hoursAndMinutes[1]))/60
	}
	
	
}
