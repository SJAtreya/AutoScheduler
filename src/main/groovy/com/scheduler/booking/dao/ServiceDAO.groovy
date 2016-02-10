package com.scheduler.booking.dao

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

import com.scheduler.booking.dto.Service

@Repository
class ServiceDAO {

	@Autowired
	JdbcTemplate jdbcTemplate
	
	def getServiceDuration(id) {
		jdbcTemplate.queryForObject("select * from service_configuration where id =?", new BeanPropertyRowMapper(Service.class), id)
	}
}
