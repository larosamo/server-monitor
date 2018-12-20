package org.restalion.servermonitor.controller;

import java.util.List;

import org.restalion.servermonitor.dto.MonitorDto;
import org.restalion.servermonitor.dto.ServerDto;
import org.restalion.servermonitor.service.MonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class MonitorController {
	
	@Autowired
	private MonitorService service;
	
	@PostMapping("/monitor")
	public List<MonitorDto> test() {
		log.debug("Launching monitor process");
		List<MonitorDto> result = service.monitor();
		return result;
	}
	
	@GetMapping("/historic") 
	public List<MonitorDto> historic(@RequestParam("serverName") String serverName) {
		log.debug("Launching debug process for server " + serverName);
		List<MonitorDto> result = service.historic(serverName);
		return result;
	}
	
	@PostMapping("/add")
	public Boolean add(@RequestBody ServerDto server) {
		log.debug("Adding new server: " + server);
		return service.add(server);
	}
	
	@GetMapping("/servers")
	public List<ServerDto> getServers() {
		return service.getServers();
	}
	
	@GetMapping("/activate")
	public Boolean activate(@RequestParam("serverName") String serverName) {
		return service.activate(serverName);
	}
	
	@GetMapping("/deactivate")
	public Boolean deactivate(@RequestParam("serverName") String serverName) {
		return service.deactivate(serverName);
	}
}
