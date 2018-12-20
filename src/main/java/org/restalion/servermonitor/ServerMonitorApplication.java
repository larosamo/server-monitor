package org.restalion.servermonitor;

import org.restalion.servermonitor.repository.MonitorRepository;
import org.restalion.servermonitor.repository.ServerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableScheduling
@Slf4j
public class ServerMonitorApplication {
	
	@Value("${initialize}")
	Boolean initialize;
	
	@Value("${resetHistoric}")
	Boolean resetHistoric;
	
	@Autowired
	private ServerRepository repo;
	
	@Autowired
	private MonitorRepository monitorRepo;
	

	public static void main(String[] args) {
		SpringApplication.run(ServerMonitorApplication.class, args);
	}
	
	@EventListener(ApplicationReadyEvent.class)
	public void doSomethingAfterStartup() {
		
		initialize();
		
		resetHistoric();
	}
	
	private void initialize() {
		log.debug("Finalizing initialization...");
		
		if (initialize) {
			
			log.debug("Removing servers from monitor list ...");

			// remove previous values
			repo.deleteAll();
		}
	}
	
	private void resetHistoric() {
		if (resetHistoric) {
			log.debug("Cleaning previous monitor history ...");
			monitorRepo.deleteAll();
		}
	}
}
