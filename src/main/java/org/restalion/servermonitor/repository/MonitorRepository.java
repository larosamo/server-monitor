package org.restalion.servermonitor.repository;

import java.util.List;

import org.bson.types.ObjectId;
import org.restalion.servermonitor.dto.MonitorDto;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MonitorRepository extends MongoRepository<MonitorDto, ObjectId> {
	public List<MonitorDto> findByServerName(String serverName);
}
