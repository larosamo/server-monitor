package org.restalion.servermonitor.service;

import java.util.List;

import org.restalion.servermonitor.dto.MonitorDto;
import org.restalion.servermonitor.dto.ServerDto;

public interface MonitorService {
	public List<MonitorDto> monitor();
	public List<MonitorDto> historic(String serverName);
	public Boolean add(ServerDto server);
	public Boolean activate(String serverName);
	public Boolean deactivate(String serverName);
	public List<ServerDto> getServers();
	public ServerDto save(ServerDto server);
	public void remove(ServerDto server);
}
