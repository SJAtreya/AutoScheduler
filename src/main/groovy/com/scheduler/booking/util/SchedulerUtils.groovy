package com.scheduler.booking.util


import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatterBuilder
import org.joda.time.format.DateTimeParser

import com.scheduler.booking.dto.NLPDataDto

class SchedulerUtils {

	static def convertTime(String time) {
		def timeLen = time.length()
		def hoursAndMinutes = time.split(':')
		Double.valueOf(hoursAndMinutes[0]) + (Double.valueOf(hoursAndMinutes[1]))/60
	}

	static def findConversationRequestType(NLPDataDto requestData){
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

	static def translateToNLPFormat(Map response) {
		def preferences = response.collect( {it.sentiment == 'Neutral' || it.sentiment == 'Positive'} )
		def temporals = preferences["Temporals"]
		if (!preferences || !temporals){
			return new NLPDataDto(date:null, startTime:null, endTime:null)
		}
		def iterator = temporals.iterator()
		while (iterator.hasNext()) {
			def date = parseDate(iterator.next())
			if (dependancyRequired()) {
				// Check first
				// Check second
			}
		}
	}

	static def parseDate(inputDate) {
		if (inputDate==null) {
			return new Date().format("yyyy-MM-dd")
		}
		def currentDate = DateTime.now()
		def dateSplit = inputDate.replaceAll("-","/").replaceAll("T"," ").split("/")
		def month = dateSplit[0].replace("??",String.format("%02d", currentDate.monthOfYear))
		def date =  dateSplit[1].replace("??",String.format("%02d", currentDate.dayOfMonth))
		def appendedDate = month+"/"+date+"/"+dateSplit[2]
		// yyyy-MM-dd
		// MM/dd/yyyy
		// ??/EEE/yyyy -- Replace With current month
		// MM/EEE/yyyy
		// MM/??/yyyy -- Replace with current date
		DateTimeParser[] parsers = [
			DateTimeFormat.forPattern("yyyy/MM/dd").parser,
			DateTimeFormat.forPattern("MM/dd/yyyy").parser,
			DateTimeFormat.forPattern("MM/EEE/yyyy").parser,
			DateTimeFormat.forPattern("yyyy/MM/dd HH:mi").parser
		]
		def formattedDate = new DateTimeFormatterBuilder().append(null, parsers).toFormatter().parseDateTime(appendedDate)
		while (formattedDate.toLocalDate().compareTo(currentDate.toLocalDate()) < 0){
			formattedDate = formattedDate.plusWeeks(1)
		}
		DateTimeFormat.forPattern("yyyy-MM-dd").print(formattedDate)
	}

	static def parseDateWithDayWeekData(String date) {
		// [2016-02-17-WXX-3, 2016-02-14T15:00, 2016-02-14T17:00, 2016-02-08-WXX-1T17:00]
		//[[Temporals:[2016-02-14T10:00], Sentiment:[Neutral], NER:[O]],
		// [Temporals:[2016-02-13TMO], Sentiment:[Negative], NER:[O]],
		// [Temporals:[2016-02-13T11:00], Sentiment:[Negative], NER:[O]],
		// [Temporals:[2016-02-13T16:00], Sentiment:[Neutral], NER:[O]],
		// [Temporals:[2016-02-13T16:00], Sentiment:[Neutral], NER:[O]],
		// [Temporals:[2016-02-14], Sentiment:[Neutral], NER:[O]]]
		def parsedDate = null
		try {
			parsedDate = parseDate(date)
		} catch (IllegalArgumentException iae) {
			// Proceed with next steps
		}
		if (parsedDate == null) {
			// Try other formats
			def tempDate = date.replaceAll("T","-").split("-")
			if (tempDate.length == 4) {
				parsedDate = date.substring(0,10)
			}else if (tempDate.length == 5) {
				// 2016-02-17-WXX-3
				parsedDate = date.substring(0,10)
			} else if (tempDate.length == 6){
				// 2016-02-08-WXX-1T17:00. How to parse this time?
				parsedDate = date.substring(0,10)
			} else {
				// Sorry, I don't understand. I shall learn next time:).
			}
		} 
		parsedDate
	}

	static def convertToTimeFormat(def time) {
		int hours=(int)time
		int minutes = (int)((time-hours)*60)
		String.format("%02d:%02d",hours,minutes)
	}
}
