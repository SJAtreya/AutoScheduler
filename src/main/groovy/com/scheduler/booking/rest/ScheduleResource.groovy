package com.scheduler.booking.rest

import javax.validation.Valid

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate

import com.scheduler.booking.dto.BookingRequestDto
import com.scheduler.booking.dto.NLPDataDto
import com.scheduler.booking.service.ScheduleService
import com.scheduler.booking.util.NLPClassifier

@RestController
@RequestMapping
@ConfigurationProperties(prefix="nlp")
class ScheduleResource {

	@Autowired
	ScheduleService scheduleService

	@Autowired
	RestTemplate restTemplate

	@Valid
	String url
	
	@Autowired
	NLPClassifier classifier

	@RequestMapping(value="/api/appointment", method=RequestMethod.POST)
	def bookAppointment(@RequestBody BookingRequestDto requestDto) {
		def retData = scheduleService.bookAppointment(requestDto)
		scheduleService.updateProbability()
		retData
	}

	@RequestMapping(value="/booking/count")
	def hoursBooked(@RequestParam("startDate") def startDate, @RequestParam(value="numDays", defaultValue="10") Integer numberOfDays) {
		def data = scheduleService.getHoursBookedForDuration(startDate, numberOfDays)
		def rangeStart = javax.xml.bind.DatatypeConverter.parseDateTime(startDate)
		def allDays = []
		rangeStart.add(Calendar.DAY_OF_YEAR,-1)
		numberOfDays.times {
			rangeStart.add(Calendar.DAY_OF_YEAR,1)
			allDays.add(rangeStart.getTime().format("yyyy-MM-dd"))
		}
		def foundDays = []
		def response = data.collect {
			foundDays.add(it.date)
			[label:it.date, y: it.hours]
		}
		(allDays-foundDays).each { response.add([label:it, y: 0]) }
		response
	}

	@RequestMapping(value="/api/appointment/finder/v1",method=RequestMethod.GET)
	def analyzeRequest(@RequestParam("message") message, @RequestParam("serviceId") serviceId ){
		def parsedData = restTemplate.getForObject(url+message,NLPDataDto.class)
		parsedData.service = serviceId
		scheduleService.findAvailableSlotsForNLTKResponse(parsedData)
	}
	
	@RequestMapping(value="/api/appointment/finder/v2",method=RequestMethod.GET)
	def analyzeRequestWithSentiment(@RequestParam("message") message, @RequestParam("serviceId") serviceId){
		def nlpResults =  classifier.classify(message.split("."))
		scheduleService.findAvailableSlotsForStanfordNLPResponse(parsedData)
	}
}
