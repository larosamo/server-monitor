package org.restalion.servermonitor.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.mail.internet.MimeMessage;

import org.restalion.servermonitor.config.Config;
import org.restalion.servermonitor.dto.LastReportSentDto;
import org.restalion.servermonitor.dto.ManagerDto;
import org.restalion.servermonitor.dto.MonitorDto;
import org.restalion.servermonitor.dto.ServerDto;
import org.restalion.servermonitor.dto.ServerManagerDto;
import org.restalion.servermonitor.repository.LastReportSentRepository;
import org.restalion.servermonitor.repository.ManagerRepository;
import org.restalion.servermonitor.repository.MonitorRepository;
import org.restalion.servermonitor.repository.ServerManagerRepository;
import org.restalion.servermonitor.repository.ServerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Configuration
public class MonitorServiceImpl implements MonitorService {
	
	@Autowired
	Config config;
	
	@Autowired
	ServerRepository repo;
	
	@Autowired
	MonitorRepository monitorRepo;
	
	@Autowired
	ManagerRepository managerRepo;

	@Autowired
	ServerManagerRepository serverManagerRepo;

	@Autowired
	LastReportSentRepository lastReportSentRepo;

	@Autowired
	private JavaMailSender sender;
	
	
	@Value("${mail.enabled}")
    private Boolean mailEnabled;

	@Value("${mail.from}")
    private String mailFrom;

	
	public List<MonitorDto> monitor() {
		
		List<ServerDto> servers = repo.findAll();
		
		List<ServerManagerDto> managersByServer =  new ArrayList<>();
		
		List<MonitorDto> salida = new ArrayList<>();
		
		Boolean notOk = false;
		for (ServerDto server:servers) {
			if (server.getActive()) {
				try {
					ResponseEntity<String> response = config.getRestTemplate().getForEntity(server.getUrl(), String.class);
					HttpStatus statusCode = response.getStatusCode();
					if (!statusCode.name().equals("OK")) {
						notOk = true;
					} else {
						managersByServer = serverManagerRepo.findByServerName(server.getName());
						//For each manager we update the last status sent to ok removing date
						for (ServerManagerDto serverManager: managersByServer) {
							LastReportSentDto lastSentDto = LastReportSentDto.builder()
									.managerName(serverManager.getManagerName())
									.serverName(server.getName()).build();
							this.save(lastSentDto);
						}
					}
					salida.add(MonitorDto.builder().serverName(server.getName()).status(statusCode.name()).time(LocalDateTime.now()).build());
				}
				catch (Exception e) {
					salida.add(MonitorDto.builder().serverName(server.getName()).status(HttpStatus.NOT_FOUND.name()).time(LocalDateTime.now()).build());
					notOk = true;
				}
			} else {
				log.debug("Deactivated sever {} not monitored.", server.getName());
			}
			
		}
		monitorRepo.insert(salida);
		log.debug("mailEnabled "  + mailEnabled);
		if (notOk && mailEnabled) {
			log.debug("Sending notification mail");
			try {
				sendEmail(salida);
			} catch (Exception e) {
				e.printStackTrace();
				log.error("Notification mail couldn't be sent");
				
			}
		}
		return salida;
	}
	
	public List<MonitorDto> historic(String serverName) {
		return monitorRepo.findByServerNameOrderByTimeDesc(serverName);
	}

	public List<ManagerDto> getManagers() {
		return managerRepo.findAll();
	}
	
	public List<MonitorDto> lastStatus() {
		List<ServerDto> servers = repo.findAll();
		List<MonitorDto> lastStatus = new ArrayList<>();
		MonitorDto monitorDtoAux;
		for (ServerDto it : servers) {
			if (it.getActive()) {
			monitorDtoAux = monitorRepo.findFirst1ByServerNameOrderByTimeDesc(it.getName());
			lastStatus.add(monitorDtoAux);
			}
		}
		return lastStatus;
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
		List<ServerManagerDto> managedServers;
		List<LastReportSentDto> sentReports;
		managedServers = serverManagerRepo.findByServerName(server.getName());
		// If it has servers associated we delete them
		for (ServerManagerDto managedServer: managedServers) {
			serverManagerRepo.delete(managedServer);
		}
		// If it has reports associated we delete them
		sentReports = lastReportSentRepo.findByServerName(server.getName());
		for (LastReportSentDto sentReport: sentReports) {
			lastReportSentRepo.delete(sentReport);
		}
		repo.delete(server);
	}
	
	public void remove(String serverName) {
		List<ServerDto> servers = repo.findByName(serverName);
		servers.forEach(s -> this.remove(s));
	}
	
	public ManagerDto save(ManagerDto manager) {
		Optional<ManagerDto> opt;
		ManagerDto managerAux;
		List<ServerManagerDto> managedServers;
		List<LastReportSentDto> sentReports;
		String oldName;
		if (manager.getId() != null) {
			opt = managerRepo.findById(manager.getId());
			if (opt.isPresent()) { 
				managerAux= opt.get();
				oldName = managerAux.getName();
				// If Name is updated
				if (!oldName.equals(manager.getName())) {
					//update managed Servers
					managedServers = serverManagerRepo.findByManagerName(oldName);
					for (ServerManagerDto managedServer: managedServers) {
						managedServer.setManagerName(manager.getName());
						serverManagerRepo.save(managedServer);
					}
					//update sent reports
					sentReports = lastReportSentRepo.findByManagerName(oldName);
					for (LastReportSentDto sentReport: sentReports) {
						sentReport.setManagerName(manager.getName());
						lastReportSentRepo.save(sentReport);
					}
				
				}

			}
		}
		return managerRepo.save(manager);
	}
	public void remove(ManagerDto manager) {
		List<ServerManagerDto> managedServers;
		List<LastReportSentDto> sentReports;
		managedServers = serverManagerRepo.findByManagerName(manager.getName());
		// If it has servers associated we delete them
		for (ServerManagerDto managedServer: managedServers) {
			serverManagerRepo.delete(managedServer);
		}
		// If it has reports associated we delete them
		sentReports = lastReportSentRepo.findByManagerName(manager.getName());
		for (LastReportSentDto sentReport: sentReports) {
			lastReportSentRepo.delete(sentReport);
		}
		managerRepo.delete(manager);
	}
	 private void sendEmail(List<MonitorDto> monitorResults) throws Exception{
		  	MimeMessage message = null; 
		  	MimeMessageHelper helper = null;
	        String[] to;
	        LastReportSentDto lastSentDto;
	        ManagerDto manager;
	        Hashtable <String, String>  managersData = new Hashtable <String, String>();
	        List <ServerManagerDto> managers = new ArrayList();
	        Calendar currentDate = Calendar.getInstance();
	        Calendar lastReportDate = Calendar.getInstance();
	        
	        Date auxDate = null;
	        String messageBody = "";
	        for (MonitorDto it : monitorResults) {
	        	message = sender.createMimeMessage(); 
	        	helper = new MimeMessageHelper(message);
	        	messageBody = "Monitorizacion de Servidores PVC .\nSe han detectado fallos en la monitorizacion de los servidores.\n El siguiente servidor no está disponible:\n";
	        	managers.clear();
	        	if (!it.getStatus().equals("OK")) {
					managers = this.getManagedServersByServerAndActive(it.getServerName(),Boolean.TRUE);
					if (managers.size()>0) {
						to = new String[managers.size()];
						for (ServerManagerDto serverManager : managers) {
							lastSentDto = lastReportSentRepo.findByManagerNameAndServerName(serverManager.getManagerName(),it.getServerName());
							if (lastSentDto!= null) {
								auxDate = lastSentDto.getDate();
							}
							if (auxDate!=null) {
								lastReportDate.setTime(auxDate);
							}
							//If the previous status is ok , or if was notified the day before and is still failing we send the mail
							if (auxDate == null
									|| (currentDate.getTimeInMillis() - lastReportDate.getTimeInMillis() >= (24*60*60*1000))) {
								manager = managerRepo.findFirst1ByName(serverManager.getManagerName());
								managersData.put(manager.getName(), manager.getMail());
							} 
						}
						to = new String[managersData.size()];
						int index = 0;
						for (String managerAux: managersData.values()){
							to[index] = managerAux;
				            index++;
				        }; 
						if (to.length>0) {
							log.debug("Sending mail for server "+ it.getServerName() + " to " + to.length + " users");
							helper.setTo(to);
							helper.setFrom(mailFrom);
							messageBody = messageBody + "Server: " + it.getServerName() + "\n Estado: " + it.getStatus() + "\n"; 
					        messageBody += "\n\n\nSi no desea seguir recibiendo noticaciones de este servidor acceda a la aplicación y desactive la notificación o elimine este servidor como mantenido por usted.";
					        //log.debug("message: " + messageBody);
					        helper.setText(messageBody);
							//log.debug("subject: " + "Sever monitoring: Server " +  it.getServerName() + " is not available");
							helper.setSubject("Sever monitoring: Server " +  it.getServerName() + " is not available");
				        	//log.debug("send");
					        sender.send(message);
				        	//We sent the last notification date
				        	managersData.forEach((k, v) -> { 
				        		String[] managerAux = {k};  
				        		LastReportSentDto[] lastSentDtoAux = {LastReportSentDto.builder()
										.managerName(managerAux[0])
										.serverName(it.getServerName())
										.date(currentDate.getTime())
										.build()};
								this.save(lastSentDtoAux[0]);
				            }); 
				        }
					}   
				}
			}
	         
	        
	 }
	 
		public List<ServerDto> getAvailableServerByManager(String managerName) {
			List<ServerDto> availables = new ArrayList<>();
			List<ServerDto> servers = repo.findAll();
			List<ServerManagerDto> managedServers = serverManagerRepo.findByManagerName(managerName);
			for (ServerDto serverDtoAux : servers) {
				int found = 0;
				for (ServerManagerDto SerManDto: managedServers) {
					if (serverDtoAux.getName().equals(SerManDto.getServerName())) {
						found = 1;
					}
				}
				if (found<1) {
					availables.add(serverDtoAux);
				}
			}
			return availables; 
		}
		
		public List<ServerManagerDto> getManagedServersByManager(String managerName) {
			return serverManagerRepo.findByManagerName(managerName);
		}
		
		public List<ServerManagerDto> getManagedServersByServer(String serverName) {
			return serverManagerRepo.findByServerName(serverName);
		}

		public List<ServerManagerDto> getManagedServersByServerAndActive(String serverName,Boolean active) {
			return serverManagerRepo.findByServerNameAndActive(serverName,active);
		}

		public void remove(ServerManagerDto managedServer) {
			serverManagerRepo.delete(managedServer);
		}
		
		public List<ServerManagerDto> saveAll(List<ServerManagerDto> serverManagers) {
			return serverManagerRepo.saveAll(serverManagers);
		}

		public ServerManagerDto save(ServerManagerDto serverManager) {
			return serverManagerRepo.save(serverManager);
		}

		public LastReportSentDto save(LastReportSentDto lastReportSent) {
			LastReportSentDto auxDto = lastReportSentRepo.findByManagerNameAndServerName(lastReportSent.getManagerName(), lastReportSent.getServerName());
			//If exits we edit the existing 
			if (auxDto != null) {
				auxDto.setDate(lastReportSent.getDate());
				auxDto = lastReportSentRepo.save(auxDto);
			} else { // if doesn't exists we create a new one
				auxDto = lastReportSentRepo.save(lastReportSent);
				
			}
			return auxDto;
		}

		public void remove(LastReportSentDto lastReportSent) {
			LastReportSentDto auxDto = lastReportSentRepo.findByManagerNameAndServerName(lastReportSent.getManagerName(), lastReportSent.getServerName());
			if (auxDto != null) {
				auxDto.setDate(null);
				lastReportSentRepo.save(auxDto);
			} else {
				lastReportSent.setDate(null);
				lastReportSentRepo.save(lastReportSent);
			}
		}

}
