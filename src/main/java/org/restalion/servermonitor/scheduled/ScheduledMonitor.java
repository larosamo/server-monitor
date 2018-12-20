package org.restalion.servermonitor.scheduled;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.restalion.servermonitor.service.MonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ScheduledMonitor {
	
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	
	@Autowired
	MonitorService monitorService;

	@Scheduled(fixedRateString = "${fixedRate}")
	public void monitor() {
		log.debug("Launch scheduled monitor {}", dateFormat.format(new Date()));
		
		monitorService.monitor();
	}
}
