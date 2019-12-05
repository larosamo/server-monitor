package org.restalion.servermonitor.service;

import java.util.List;

import org.restalion.servermonitor.dto.LastReportSentDto;
import org.restalion.servermonitor.dto.ManagerDto;
import org.restalion.servermonitor.dto.MonitorDto;
import org.restalion.servermonitor.dto.ServerDto;
import org.restalion.servermonitor.dto.ServerManagerDto;

public interface MonitorService {
	public List<MonitorDto> monitor();
	public List<MonitorDto> historic(String serverName);
	public List<MonitorDto> lastStatus();
	public Boolean add(ServerDto server);
	public Boolean activate(String serverName);
	public Boolean deactivate(String serverName);
	public List<ServerDto> getServers();
	public ServerDto save(ServerDto server);
	public void remove(ServerDto server);
	public ManagerDto save(ManagerDto manager);
	public void remove(ManagerDto manager);
	public List<ManagerDto> getManagers();
	public void remove(String serverName);
	public List<ServerDto> getAvailableServerByManager(String managerName);
	public List<ServerManagerDto> getManagedServersByServer(String serverName);
	public List<ServerManagerDto> getManagedServersByManager(String managerName);
	public ServerManagerDto save(ServerManagerDto serverManager);
	public List<ServerManagerDto> saveAll(List<ServerManagerDto> serverManagers);
	public void remove(ServerManagerDto managedServer);
	public LastReportSentDto save(LastReportSentDto lastReportSent);
	public void remove(LastReportSentDto lastReportSent);
}