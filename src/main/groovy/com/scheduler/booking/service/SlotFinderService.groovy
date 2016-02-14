package com.scheduler.booking.service

import java.text.SimpleDateFormat

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatterBuilder
import org.joda.time.format.DateTimeParser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import com.scheduler.booking.dao.AppointmentDAO
import com.scheduler.booking.dao.BookingRequestDAO
import com.scheduler.booking.dao.SchedulerDAO
import com.scheduler.booking.dao.ServiceDAO
import com.scheduler.booking.dto.Slot
import com.scheduler.booking.util.SchedulerUtils;

@Service
class SlotFinderService {

	@Autowired
	AppointmentDAO appointmentDAO

	@Autowired
	SchedulerDAO schedulerDAO

	@Autowired
	ServiceDAO serviceDAO

	@Autowired
	BookingRequestDAO bookingRequestDAO

	def findAt(requestData){
		def endTime = requestData.startTime + serviceDAO.getServiceDuration(requestData.service).duration
		findAllSlots(requestData, [new Slot(startTime:requestData.startTime,endTime:endTime)], 1)
	}

	def findOn(requestData) {
		findAllSlots(requestData, [new Slot(startTime:8.0,endTime:20.0)],10)
	}

	def findBefore(requestData){
		findAllSlots(requestData, [new Slot(startTime:8.0,endTime:requestData.endTime)],10)
	}

	def findAfter(requestData){
		findAllSlots(requestData, [new Slot(startTime:requestData.startTime,endTime:20.0)],10)
	}

	def findEarliest(requestData){
		findAllSlots(requestData, [new Slot(startTime:8.0,endTime:20.0)],10)
	}

	def findBetween(requestData) {
		findAllSlots(requestData, [new Slot(startTime:requestData.startTime,endTime:requestData.endTime)],10)
	}

	def findAllSlots(dto, slots, range) {
		def service = 	serviceDAO.getServiceDuration(dto.service)
		dto.serviceName = service.service
		def serviceDuration = 	service.duration
		def date = SchedulerUtils.parseDate(dto.date)
		def slotRemainderTime = null
		def breakPoint = 0
		def slotsAvailable = []
		while (slotsAvailable.size()<3){
			def availableSlots = findAvailableSlotsForDateAndTime(date, slots, serviceDuration)
			for (def slot:availableSlots){
				if (slot.duration() >= serviceDuration) {
					slotRemainderTime = slot.duration() - serviceDuration
					if ((slotRemainderTime == 0) || findProbableFill(slotRemainderTime)) {
						slotsAvailable.add(new Slot(startTime:slot.startTime,endTime:slot.startTime+serviceDuration, date:date))
					}
				}
			}
			date = getNextDate(date,dto.date)
			breakPoint++
			if (breakPoint>range) {
				// Enough is enough - Not more than 3 to 4 months in advance.
				break
			}
		}
		slotsAvailable
	}

	def findFirstAvailableSlot(dto, slots) {
		def allSlots = findAllSlots(dto, slots,10)
		(allSlots.size()>0)?allSlots[0]:[]
	}

	def getNextDate(date, requestedDate) {
		def calendar = Calendar.getInstance()
		calendar.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(date.substring(0,10)))
		def currentDay = calendar.get(Calendar.DAY_OF_WEEK)
		def daysToAdd
		if (requestedDate.replaceAll("-","/").split("/")[1].length()>2) {
			daysToAdd = 7
		} else {
		 	daysToAdd = (currentDay < 6) ? 1 : (7-currentDay + 2)
		}
		calendar.add(Calendar.DATE, daysToAdd)
		calendar.getTime().format("yyyy-MM-dd")
	}

	def findProbableFill(timeLeft) {
		bookingRequestDAO.getCountOfBookingRequests() < 1000 || schedulerDAO.findProbabilityOfAllocations(timeLeft)
	}

	def findAvailableSlotsForDateAndTime(date, slots, serviceDuration) {
		def appointments = appointmentDAO.findAppointmentsForDate(date)
		def hoursOfOperation = slots
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
					}
				}
			}
			hoursOfOperation = newHoursOfOperation
		}
		def newHoursOfOperation = []
		hoursOfOperation.each {
			def startTime = it.startTime
			def endTime = it.endTime
			while ((endTime - startTime) >= serviceDuration) {
				newHoursOfOperation.add(new Slot(startTime:startTime, endTime: startTime+serviceDuration))
				startTime+=serviceDuration
			} 
		}
		newHoursOfOperation
	}

	def overlaps (startTime, endTime, appointmentStartTime, appointmentEndTime) {
		(startTime < appointmentEndTime)  && (endTime > appointmentStartTime)
	}



	static main(String[] args) {
		println SchedulerUtils.parseDate("2016-02-11")
		println SchedulerUtils.parseDate("02/11/2016")
		println SchedulerUtils.parseDate("02/MON/2016")
		println SchedulerUtils.parseDate("??/FRI/2016")
		println SchedulerUtils.parseDate("??/??/2016")
		println SchedulerUtils.parseDate("02/??/2016")
	}
}