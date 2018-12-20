package org.restalion.servermonitor.dto;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ServerDto {
	@Id
	ObjectId id;
	String name;
	String url;
	Boolean active;
}
