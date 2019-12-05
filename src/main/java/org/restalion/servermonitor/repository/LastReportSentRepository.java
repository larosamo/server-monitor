package org.restalion.servermonitor.repository;

import java.util.List;

//import java.util.List;

import org.bson.types.ObjectId;
import org.restalion.servermonitor.dto.LastReportSentDto;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LastReportSentRepository extends MongoRepository<LastReportSentDto, ObjectId> {
	public LastReportSentDto findByManagerNameAndServerName(String managerName, String serverName);
	public List<LastReportSentDto> findByManagerName(String managerName);
	public List<LastReportSentDto> findByServerName(String serverName);
}
