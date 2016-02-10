package com.scheduler.booking.service

import java.text.SimpleDateFormat

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

import com.scheduler.booking.dao.BookingRequestDAO

@Component
class ProbabilityDistributor {

	@Autowired
	BookingRequestDAO bookingRequestDAO

	@Scheduled(fixedDelay=3600000l)
	def void updateProbability(){
		computeServiceLevelProbability()
		computeDayWiseServiceLevelProbability()
	}

	@Transactional
	def computeServiceLevelProbability() {
		def currentDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
		def date = []
		currentDayOfWeek = (currentDayOfWeek >6)?6:currentDayOfWeek
		(2..currentDayOfWeek).each {
			date.add(getDate(it))
		}
		bookingRequestDAO.updateProbabilityForServices(date)
	}
	
	def getDate(day) {
		def date = Calendar.getInstance()
		date.set(Calendar.DAY_OF_WEEK, day)
		date.format("yyyy-MM-dd")
	}

	@Transactional
	def computeDayWiseServiceLevelProbability () {
		bookingRequestDAO.updateProbabilityForServicesDayWise()
	}
}
