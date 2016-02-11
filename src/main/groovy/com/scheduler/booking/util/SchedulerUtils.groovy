package com.scheduler.booking.util

class SchedulerUtils {

	static def convertTime(String time) {
		def timeLen = time.length()
		def hoursAndMinutes = time.split(':')
		Double.valueOf(hoursAndMinutes[0]) + (Double.valueOf(hoursAndMinutes[1]))/60
	}

	static def findConversationRequestType(requestData){
		def requestType
		if (requestData.date == null && requestData.endTime == null && requestData.startTime == null) {
			requestType = ConversationRequestType.Earliest
		} else if (requestData.date != null && requestData.endTime == null && requestData.startTime == null) {
			requestType = ConversationRequestType.On
		} else if (requestData.startTime == null && requestData.endTime != null) {
			requestType = ConversationRequestType.Before
		} else if (requestData.startTime != null && requestData.endTime == null) {
			requestType = ConversationRequestType.After
		} else {
			if (requestData.startTime == requestData.endTime) {
				requestType = ConversationRequestType.At
			} else {
				requestType = ConversationRequestType.Between
			}
		}
		requestType
	}
	
	static def convertToTimeFormat(def time) {
		int hours=(int)time
		int minutes = (int)((time-hours)*60)
		String.format("%02d:%02d",hours,minutes) 
	}
}
