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
			slotToBook = allocateForDate(dto)
		}
		else {
			slotToBook = doAllocate(dto)
		}
		if (slotToBook != null) {
			appointmentDAO.create(requestId, slotToBook.date, slotToBook.startTime, slotToBook.endTime)
			return [requestedDate:dto.date.substring(0,10), proposedDate: slotToBook.date, proposedTime: slotToBook.startTime, service:dto.serviceName, id:requestId]
		}
	}

	def allocateForDate(dto) {
		def service = 	serviceDAO.getServiceDuration(dto.service)
		dto.serviceName = service.service
		def serviceDuration = 	service.duration
		def date = dto.date.substring(0,10)
		def slotForBooking = null
		def slotRemainderTime = null
		def breakPoint = 0
		while (slotForBooking == null){
			def availableSlots = findAvailableSlotsForDate(date)
			for (def slot:availableSlots){
				if (slot.duration() >= serviceDuration) {
					slotRemainderTime = slot.duration() - serviceDuration
					if ((slotRemainderTime == 0) || findProbableFill(slotRemainderTime)) {
						slotForBooking = new Slot(startTime:slot.startTime,endTime:slot.startTime+serviceDuration, date:date)
						break;
					}
				}
			}
			date = getNextDate(date)
			breakPoint++
			if (breakPoint>100) {
				// Enough is enough - Not more than 3 to 4 months in advance.
				break
			}
		}
		slotForBooking
	}

	def getNextDate(date) {
		def calendar = Calendar.getInstance()
		calendar.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(date.substring(0,10)))
		def currentDay = calendar.get(Calendar.DAY_OF_WEEK)
		def daysToAdd = (currentDay < 6) ? 1 : (7-currentDay + 2)
		calendar.add(Calendar.DATE, daysToAdd)
		calendar.getTime().format("yyyy-MM-dd")
	}
	def findAvailableSlotsForDate(date) {
		def appointments = appointmentDAO.findAppointmentsForDate(date)
		def hoursOfOperation = [new Slot(startTime:8.0,endTime:20.0)]
		appointments.each  { appointment->
			def newHoursOfOperation = []
			hoursOfOperation.each {
				if (!overlaps(it.startTime,it.endTime,appointment.start_time,appointment.end_time)) {
					newHoursOfOperation.add(new Slot(startTime:it.startTime,endTime:it.endTime))
				}
				else {
					if (it.endTime > appointment.end_time && it.startTime < appointment.start_time) {
						newHoursOfOperation.add(new Slot(startTime:it.startTime,endTime:appointment.start_time))
						newHoursOfOperation.add(new Slot(startTime:appointment.end_time,endTime:it.endTime))
					}
					else if (it.startTime<appointment.start_time && it.endTime <= appointment.end_time) {
						newHoursOfOperation.add(new Slot(startTime:it.startTime,endTime:appointment.start_time))
					} else if (it.startTime>= appointment.start_time && it.endTime > appointment.end_time) {
						newHoursOfOperation.add(new Slot(startTime:appointment.end_time,endTime:it.endTime))
						//						println newHoursOfOperation
					}
				}
			}
			//			println newHoursOfOperation
			hoursOfOperation = newHoursOfOperation
			//			println hoursOfOperation
		}
		//		println hoursOfOperation
		hoursOfOperation
	}

	def overlaps (startTime, endTime, appointmentStartTime, appointmentEndTime) {
		(startTime < appointmentEndTime)  && (endTime > appointmentStartTime)
	}

	def findProbableFill(timeLeft) {
		bookingRequestDAO.getCountOfBookingRequests() < 1000 || schedulerDAO.findProbabilityOfAllocations(timeLeft)
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
	
}
