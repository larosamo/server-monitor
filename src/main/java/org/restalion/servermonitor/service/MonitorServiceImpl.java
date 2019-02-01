package org.restalion.servermonitor.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.restalion.servermonitor.config.Config;
import org.restalion.servermonitor.dto.MonitorDto;
import org.restalion.servermonitor.dto.ServerDto;
import org.restalion.servermonitor.repository.MonitorRepository;
import org.restalion.servermonitor.repository.ServerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MonitorServiceImpl implements MonitorService {
	
	@Autowired
	Config config;
	
	@Autowired
	ServerRepository repo;
	
	@Autowired
	MonitorRepository monitorRepo;

	public List<MonitorDto> monitor() {
		
		List<ServerDto> servers = repo.findAll();
		
		List<MonitorDto> salida = new ArrayList<>();
		
		for (ServerDto server:servers) {
			if (server.getActive()) {
				try {
					ResponseEntity<String> response = config.getRestTemplate().getForEntity(server.getUrl(), String.class);
					HttpStatus statusCode = response.getStatusCode();
					salida.add(MonitorDto.builder().serverName(server.getName()).status(statusCode.name()).time(LocalDateTime.now()).build());
				}
				catch (Exception e) {
					salida.add(MonitorDto.builder().serverName(server.getName()).status(HttpStatus.NOT_FOUND.name()).time(LocalDateTime.now()).build());
				}
			} else {
				log.debug("Deactivated sever {} not monitored.", server.getName());
				//salida.add(MonitorDto.builder().serverName(server.getName()).status(HttpStatus.NOT_IMPLEMENTED.name()).time(LocalDateTime.now()).build());
			}
		}
		
		monitorRepo.insert(salida);
		
		return salida;
	}
	
	public List<MonitorDto> historic(String serverName) {
		return monitorRepo.findByServerName(serverName);
	}
	
	public Boolean add(ServerDto server) {
		Boolean value = Boolean.TRUE;
		if (repo.findByName(server.getName()).size() == 0) {
			repo.insert(server);
		} else {
			value = Boolean.FALSE;
		}
		return value;
	}
	
	public Boolean activate(String serverName) {
		return changeStatus(serverName, Boolean.TRUE);
	}
	
	public Boolean deactivate(String serverName) {
		return changeStatus(serverName, Boolean.FALSE);
	}
	
	private Boolean changeStatus(String serverName, Boolean status) {
		List<ServerDto> servers = repo.findByName(serverName);
		if (!servers.isEmpty()) {
			ServerDto server = servers.get(0);
			server.setActive(status);
			repo.save(server);
			return Boolean.TRUE;
		} else {
			return Boolean.FALSE;
		}
	}
	
	public List<ServerDto> getServers() {
		return repo.findAll();
	}
	
	public ServerDto save(ServerDto server) {
		return repo.save(server);
	}
	
	public void remove(ServerDto server) {
		repo.delete(server);
	}
}
