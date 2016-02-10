package com.scheduler.booking.dao

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class SchedulerDAO {

	@Autowired
	JdbcTemplate jdbcTemplate

	def findIfSlotAvailable(date, startTime, endTime) {
		jdbcTemplate.queryForList("select * from appointment where DATE_FORMAT(date, '%m/%d/%Y')  = ? and (start_time < ?)  and  (end_time > ?)", date, endTime, startTime)
	}
	
	def findProbabilityOfAllocations(slotDuration){
		def query = "select distinct 1 from service_configuration s where s.duration <= ? and current_week_avg > 0.1"
		def cnt = jdbcTemplate.queryForObject(query, Integer.class, slotDuration)
		cnt != null && cnt > 0
	}
}
