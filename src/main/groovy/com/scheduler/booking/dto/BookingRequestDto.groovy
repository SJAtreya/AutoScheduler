package com.scheduler.booking.dto

class BookingRequestDto {
	def name = "Consumer"
	def email = "Consumer@email.com"
	def service
	def date
	def type = "AUTO_ALLOCATE"
	def time = "10:00"
	def serviceName
}