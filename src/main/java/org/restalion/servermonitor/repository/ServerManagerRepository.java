package org.restalion.servermonitor.repository;

import java.util.List;

import org.bson.types.ObjectId;
import org.restalion.servermonitor.dto.ServerManagerDto;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ServerManagerRepository extends MongoRepository<ServerManagerDto, ObjectId> {
	public List<ServerManagerDto> findByManagerName(String managerName);
	public List<ServerManagerDto> findByServerName(String serverName);
	public List<ServerManagerDto> findByServerNameAndActive (String serverName,Boolean active);
}
