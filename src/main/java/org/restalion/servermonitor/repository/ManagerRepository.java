package org.restalion.servermonitor.repository;

import java.util.List;

import org.bson.types.ObjectId;
import org.restalion.servermonitor.dto.ManagerDto;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ManagerRepository extends MongoRepository<ManagerDto, ObjectId> {
	public List<ManagerDto> findByName(String name);
	public ManagerDto findFirst1ByName (String name);
}
