package com.scheduler.booking.dao

import java.text.SimpleDateFormat

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

import com.scheduler.booking.dto.Appointment

@Repository
class AppointmentDAO {

	@Autowired
	JdbcTemplate jdbcTemplate

	def create(requestId, date, startTime, endTime) {
		def insertQuery = "INSERT INTO appointment (booking_request_id, date, start_time, end_time) VALUES (?, ?, ?, ?)"
		jdbcTemplate.update(insertQuery, requestId, new SimpleDateFormat("yyyy-MM-dd").parse(date), startTime, endTime)
	}

	def findAppointmentsForDate(date) {
		def query = "SELECT * FROM appointment where date_format(date,'%Y-%m-%d') = ? order by start_time"
		jdbcTemplate.query(query, new BeanPropertyRowMapper(Appointment.class), date)
	}
}
