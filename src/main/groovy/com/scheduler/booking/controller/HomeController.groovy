package com.scheduler.booking.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

@RequestMapping("/")
@Controller
class HomeController {
	
	@RequestMapping(method=RequestMethod.GET)
	def index() {
		"index.html"
	}
	
}
