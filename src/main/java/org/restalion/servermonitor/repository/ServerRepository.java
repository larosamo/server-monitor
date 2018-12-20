package org.restalion.servermonitor.repository;

import java.util.List;

import org.bson.types.ObjectId;
import org.restalion.servermonitor.dto.ServerDto;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ServerRepository extends MongoRepository<ServerDto, ObjectId> {
	public List<ServerDto> findByName(String name);
}
