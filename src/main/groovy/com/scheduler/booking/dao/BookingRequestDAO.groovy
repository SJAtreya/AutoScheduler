package com.scheduler.booking.dao

import java.text.SimpleDateFormat

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository

import com.fasterxml.jackson.databind.util.ISO8601Utils;
import com.scheduler.booking.util.SchedulerUtils;

@Repository
class BookingRequestDAO {

	@Autowired
	JdbcTemplate jdbcTemplate

	@Autowired
	NamedParameterJdbcTemplate namedParameterJdbcTemplate

	def getBookingRequestForService(id) {
		jdbcTemplate.queryForObject("select count(*) from booking_request where service =?", Integer.class, id)
	}

	def getCountOfBookingRequests() {
		jdbcTemplate.queryForObject("select count(*) from booking_request",Integer.class)
	}

	def updateProbabilityForServices(weekDays) {
		def query = "update service_configuration sc set historical_average = ((select count(*) from booking_request br where br.service = sc.id) / (select count(*) from booking_request))"
//		def queryForCurrentWeek = "update service_configuration sc set current_week_avg = (select historical_average - (select ifnull((select count(*) from appointment app inner join booking_request br on app.booking_request_id = br.id where br.service = sc.id and date_format(app.date, '%Y-%m-%d') in (:weekDays)) / (select  count(*) from appointment app where date_format(app.date, '%Y-%m-%d') in (:weekDays)),0)))"
		def queryForCurrentWeek = "update service_configuration sc set  current_week_avg = (select  1 - (select  ifnull((select  count(*) from appointment app inner join booking_request br ON app.booking_request_id = br.id where br.service = sc.id and date_format(app.date, '%Y-%m-%d') in (:weekDays)) / (select  (count(*) / count(distinct app.date) * 5) 'weekly_average' from appointment app inner join booking_request br ON app.booking_request_id = br.id where br.service = sc.id), 0)))"
		jdbcTemplate.update(query);
		namedParameterJdbcTemplate.update(queryForCurrentWeek,["weekDays": weekDays])
	}

	def updateProbabilityForServicesDayWise() {
		//		def query = "update service_daywise_probability sdp set  probability = ((((select count(*) from booking_request br where br.service = sc.id and br.day=sdp.day) * duration) / 60)"
		//		jdbcTemplate.update(query);
	}

	def create(dto) {
		def insertQuery = "INSERT INTO booking_request (name, email, service, date, type, time, day) VALUES (?, ?, ?, ?, ?, ?, ?)"
		def date = new SimpleDateFormat("yyyy-MM-dd").parse(dto.date.substring(0,10))
		jdbcTemplate.update(insertQuery, dto.name, dto.email, dto.service, date, dto.type, SchedulerUtils.convertTime(dto.time), date.format("EEE").toUpperCase())
		jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class)
	}
	
	def getHoursBookedForDuration(startDate, endDate) {
		def query = "select date_format(date, '%Y-%m-%d') as date, sum(end_time-start_time) as hours from appointment where date between ? and ? group by date"
		jdbcTemplate.queryForList(query,startDate, endDate)
	}
}
