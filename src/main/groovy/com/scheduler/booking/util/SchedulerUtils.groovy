package com.scheduler.booking.util

import java.time.format.DateTimeFormatter;

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatterBuilder
import org.joda.time.format.DateTimeParser
import org.joda.time.format.ISODateTimeFormat

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

	static def translateToNLPFormat(nlpClassifications, serviceId) {
		def nlpResponses = []
		nlpClassifications.each { classification ->
			println classification
			if(classification.get("Sentiment") == ['Neutral']|| classification.get("Sentiment") == ['Positive']) {
				def temporals = classification.get("Temporals")
				println temporals
				if (!temporals){
					return [new NLPDataDto(date:null, startTime:null, endTime:null)]
				}
				def iterator = temporals.iterator()
				def counter = 0
				def dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm")
				def formatToString = DateTimeFormat.forPattern("yyyy-MM-dd")
				while (iterator.hasNext()) {
					def parsedDateTime = parseDateAndTimeFromSuTime(iterator[counter].toString())
					if (dependancyRequired(parsedDateTime)) {
						def endTime = null
						def startTime = null
						try {
							def firstSlot = iterator[counter].toString()
							if (firstSlot!=null) {
								DateTime dateTime = dateTimeFormatter.parseDateTime(firstSlot)
								startTime = dateTime.hourOfDay
								println "Starting Time:"
								println "${startTime}"
//								counter++
							}
						}
						catch (IllegalArgumentException ae) {
							continue;
						}
						try {
							def secondSlot = iterator[counter].toString()
							if (secondSlot != null) {
								DateTime dateTime = dateTimeFormatter.parseDateTime(secondSlot)
								endTime = dateTime.hourOfDay
								println "Ending Time:"
								println "${endTime}"
//								counter++
							}
						}
						catch (IllegalArgumentException iae) {
							// Fine, just add the start time alone.
						}
						nlpResponses.add(new NLPDataDto(date:parsedDateTime.date, startTime:startTime, endTime: endTime, service:serviceId))
					} else {
						nlpResponses.add(new NLPDataDto(date:parsedDateTime.date, startTime:parsedDateTime.startTime, endTime: parsedDateTime.endTime, service:serviceId))
					}
				}
			}
		}
		nlpResponses
	}

	static def dependancyRequired(parsedDateTime) {
		if (parsedDateTime.startTime != null || parsedDateTime.endTime !=null){
			return false
		}
		true
	}

	static def parseDate(inputDate) {
		try {
			if (inputDate==null) {
				return new Date().format("yyyy-MM-dd")
			}
			def currentDate = DateTime.now()
			def appendedDate = inputDate
			def dateSplit = inputDate.replaceAll("-","/").split("/")//.replaceAll("T"," ")
			if (dateSplit.length==3) {
				def month = dateSplit[0].replace("??",String.format("%02d", currentDate.monthOfYear))
				def date =  dateSplit[1].replace("??",String.format("%02d", currentDate.dayOfMonth))
				appendedDate = month+"/"+date+"/"+dateSplit[2]
			}
			// yyyy-MM-dd
			// MM/dd/yyyy
			// ??/EEE/yyyy -- Replace With current month
			// MM/EEE/yyyy
			// MM/??/yyyy -- Replace with current date
			DateTimeParser[] parsers = [DateTimeFormat.forPattern("yyyy/MM/dd").parser, DateTimeFormat.forPattern("MM/dd/yyyy").parser, DateTimeFormat.forPattern("MM/EEE/yyyy").parser]
			def formattedDate = new DateTimeFormatterBuilder().append(null, parsers).toFormatter().parseDateTime(appendedDate)
			while (formattedDate.toLocalDate().compareTo(currentDate.toLocalDate()) < 0){
				formattedDate = formattedDate.plusWeeks(1)
			}
			return DateTimeFormat.forPattern("yyyy-MM-dd").print(formattedDate)
		} catch (IllegalArgumentException iae) {
			// Let the caller decide.
		}
		return null
	}

	static def parseDateAndTimeFromSuTime(String date) {
		// [2016-02-17-WXX-3, 2016-02-14T15:00, 2016-02-14T17:00, 2016-02-08-WXX-1T17:00]
		//[[Temporals:[2016-02-14T10:00], Sentiment:[Neutral], NER:[O]],
		// [Temporals:[2016-02-13TMO], Sentiment:[Negative], NER:[O]],
		// [Temporals:[2016-02-13T11:00], Sentiment:[Negative], NER:[O]],
		// [Temporals:[2016-02-13T16:00], Sentiment:[Neutral], NER:[O]],
		// [Temporals:[2016-02-13T16:00], Sentiment:[Neutral], NER:[O]],
		// [Temporals:[2016-02-14], Sentiment:[Neutral], NER:[O]]]
		//		println date.getTime()
		//		println date.getTimeLabel()
		//		println date.getTimexValue()
		//		println date.getTimexType()
		//		switch (date.getTimexType()) {
		//			case TimexType.DATE:
		//			case TimexType.TIME:
		//			default:
		//				break;
		//		}
		def parsedDate, startTime, endTime = null
		try {
			parsedDate = parseDate(date)
		} catch (IllegalArgumentException iae) {
			// Proceed with next steps
		}
		if (parsedDate == null) {
			// Pure ISO
			try {
				ISODateTimeFormat.dateTime().parseDateTime(date)
				parsedDate = date.substring(0,10)
				startTime = date.substring(11,13)
			} catch (IllegalArgumentException e){
				// Try other formats
			}
			try {
				DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm").parseDateTime(date)
				parsedDate = date.substring(0,10)
				startTime = date.substring(11,13).asType(Integer.class)
			} catch (IllegalArgumentException e){
				// Try other formats
			}
		}

		// NOT ISO - Try the regular SuTime formats.
		if (parsedDate == null) {
			def tempDate = date.replaceAll("T","-").split("-")
			if ("MO" in tempDate) {
				//2016-02-13TMO or // 2016-02-17-WXX-3EV
				parsedDate = date.substring(0,10)
				startTime = 8.0
				endTime = 12.0
			} else if ("EV" in tempDate) {
				parsedDate = date.substring(0,10)
				startTime = 17.0
				endTime = 20.0
			} else if ("AF" in tempDate) {
				parsedDate = date.substring(0,10)
				startTime = 12.0
				endTime = 15.0
			}else if (tempDate.length == 5) {
				// Could be 2016-02-17-WXX-3
				parsedDate = date.substring(0,10)
			} else if (tempDate.length == 6){
				// 2016-02-08-WXX-1-17:00.
				parsedDate = date.substring(0,10)
				startTime = convertTime(tempDate[5])
			} else {
				//Sorry, I don't understand. I shall learn next time:).
			}
		}
		[date:parsedDate, startTime: startTime, endTime: endTime]
	}

	static def convertToTimeFormat(def time) {
		int hours=(int)time
		int minutes = (int)((time-hours)*60)
		String.format("%02d:%02d",hours,minutes)
	}
}
