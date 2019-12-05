package org.restalion.servermonitor.dto;

import java.util.Date;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LastReportSentDto {
	@Id
	private ObjectId id;
	private String serverName;
	private String managerName;
	private Date date;

}
