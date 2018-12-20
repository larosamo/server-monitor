package org.restalion.servermonitor.dto;

import java.time.LocalDateTime;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MonitorDto {
	@Id
	private ObjectId id;
	private String serverName;
	private String status;
	private LocalDateTime time;

}
