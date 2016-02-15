package com.scheduler.booking.service

import java.text.SimpleDateFormat

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

import com.scheduler.booking.dao.AppointmentDAO
import com.scheduler.booking.dao.BookingRequestDAO
import com.scheduler.booking.dao.SchedulerDAO
import com.scheduler.booking.dao.ServiceDAO
import com.scheduler.booking.dto.Slot
import com.scheduler.booking.util.SchedulerUtils

@Component
class ScheduleService {

	@Autowired
	SchedulerDAO schedulerDAO

	@Autowired
	ServiceDAO serviceDAO

	@Autowired
	BookingRequestDAO bookingRequestDAO

	@Autowired
	AppointmentDAO appointmentDAO

	@Autowired
	ProbabilityDistributor probabilityDistributor

	@Autowired
	SlotFinderService slotFinderService

	@Transactional
	def bookAppointment(def dto) {
		def requestId = createRequest(dto)
		if ("BOOK_MY_APPOINTMENT".equals(dto.type)) {
			// Sense Pick / You Pick
			bookMyAppointment(dto, requestId)
		} else {
			// System allocates
			autoAllocate(dto, requestId)
		}
	}

	@Transactional
	def updateProbability(){
		probabilityDistributor.computeServiceLevelProbability()
	}

	def bookMyAppointment(dto, requestId){
		def startTime = SchedulerUtils.convertTime(dto.time)
		def service = serviceDAO.getServiceDuration(dto.service)
		def duration = service.duration?:0.5
		def endTime = startTime+duration
		if (!schedulerDAO.findIfSlotAvailable(dto.date, startTime, endTime)){
			appointmentDAO.create(requestId, dto.date, startTime, endTime)
		}
	}

	def autoAllocate(dto, requestId) {
		def slotToBook = null
		if (dto.date) {
			// Has asked for a specific date
			dto.date = dto.date.substring(0, 10).replaceAll("/", "-") 
			slotToBook = slotFinderService.findFirstAvailableSlot(dto,[new Slot(startTime:8.0,endTime:20.0)])
		}
		else {
			slotToBook = doAllocate(dto)
		}
		if (slotToBook != null) {
			appointmentDAO.create(requestId, slotToBook.date, slotToBook.startTime, slotToBook.endTime)
			return [requestedDate:dto.date.substring(0,10), proposedDate: slotToBook.date, proposedTime: slotToBook.startTime, serviceId:dto.service, serviceName:dto.serviceName, id:requestId]
		}
	}


	def doAllocate(dto) {
	}

	def createRequest(dto) {
		bookingRequestDAO.create(dto)
	}

	def getHoursBookedForDuration(startDate, numberOfDays) {
		def calendar = javax.xml.bind.DatatypeConverter.parseDateTime(startDate)
		calendar.add(Calendar.DAY_OF_YEAR, numberOfDays)
		bookingRequestDAO.getHoursBookedForDuration(startDate.substring(0,10), calendar.getTime().format("yyyy-MM-dd"))
	}


	def findAvailableSlotsForNLTKResponse(parsedData){
		// Identify Request Type - BEFORE / AFTER / EARLIEST / BETWEEN
		def type = SchedulerUtils.findConversationRequestType(parsedData)
		def results = slotFinderService."${'find'+type}" parsedData
//		createConversationResponse(results)
		createJSONResponse(results)
	}
	
	def findAvailableSlotsForStanfordNLPResponse(resultsMap){
		// Identify Request Type - BEFORE / AFTER / EARLIEST / BETWEEN
		def nlpData = SchedulerUtils.translateToNLPFormat(resultsMap)
		def type = SchedulerUtils.findConversationRequestType(resultsMap)
		def results = slotFinderService."${'find'+type}" nlpData
		createConversationResponse(results)
	}

	def createConversationResponse(results) {
		def response = "I am sorry. I am unable to find a slot in the near future. Please leave your contact number, we shall get back when we find a best slot for you."
		if (results?.size()>0) {
			if (results.size() == 1) {
				response = "Would a slot on ${results[0].date} at ${SchedulerUtils.convertToTimeFormat(results[0].startTime)} work for you?"
			} else {
				response = "I was able to find the following slots. Please let me know the best one that suits you."
				for (def index=0; index<results.size(); index++) {
					if (index>=3) {
						break;
					}
					response += "${'<br/>' + results[index].date + ' at ' + SchedulerUtils.convertToTimeFormat(results[index].startTime)}"
				}
			}
		}
		response.toString()
	}
	
	def createJSONResponse(results) {
		def response = [message:"I am sorry. I am unable to find a slot in the near future. Please leave your contact number, we shall get back when we find a best slot for you."]
		if (results?.size()>0) {
			if (results.size() == 1) {
				response = [message:"Would a slot on ${results[0].date} at ${SchedulerUtils.convertToTimeFormat(results[0].startTime)} work for you?"]
			} else {
				response = [message:"I was able to find the following slots. Please let me know the best one that suits you."]
				def options = []
				for (def index=0; index<results.size(); index++) {
					if (index>=3) {
						break;
					}
					options.add(results[index].date + ' at ' + SchedulerUtils.convertToTimeFormat(results[index].startTime))
				}
				response.put("options", options)
			}
		}
		response
	}
}