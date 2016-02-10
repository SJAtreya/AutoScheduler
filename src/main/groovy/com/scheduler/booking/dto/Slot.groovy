package com.scheduler.booking.dto

class Slot {
	def startTime,endTime,date
	def duration (){
		endTime-startTime
	}
}
