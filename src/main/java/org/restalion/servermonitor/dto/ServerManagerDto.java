package org.restalion.servermonitor.dto;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ServerManagerDto {
	@Id
	private ObjectId id;
	private String serverName;
	private String managerName;
	private Boolean active;

}
