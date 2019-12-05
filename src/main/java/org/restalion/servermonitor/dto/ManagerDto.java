package org.restalion.servermonitor.dto;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ManagerDto {
	@Id
	private ObjectId id;
	private String name;
	private String mail;
	private Boolean active;

}
