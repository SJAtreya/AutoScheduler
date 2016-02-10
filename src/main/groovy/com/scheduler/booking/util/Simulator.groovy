package com.scheduler.booking.util


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import com.scheduler.booking.service.ScheduleService

@RestController
@RequestMapping("/simulate")
class Simulator {

	@Autowired
	ScheduleService scheduleService

	@RequestMapping(method=RequestMethod.GET)
	def simulate(@RequestParam(value = "request", defaultValue = "10") Integer requestCount,
		@RequestParam(value = "dayRange", defaultValue = "2") Integer dayRange
		) {
		def services = 1..16
		def date = new Date()
		def random = new Random()
		def generatedId = null
		def countPerDay = Math.round(requestCount/dayRange)
		def countsTriggered=0
		requestCount.times {
			generatedId =random.nextInt(17)
			generatedId  = (generatedId==0)?1:generatedId
			scheduleService.bookAppointment([name:'Consumer', email: 'Consumer@email.com', service: generatedId, date: date.format("MM/dd/yyyy"), time:"10:00"])
			countsTriggered++
			if (countsTriggered>=countPerDay) {
				date = addADay(date) 
				countsTriggered=0
			}
		}
		"Completed"
	}
		
	def addADay(date) {
		def newDate = Calendar.getInstance()
		newDate.setTime(date)
		newDate.add(Calendar.DAY_OF_YEAR, 1)
		newDate.getTime()
	} 
}
