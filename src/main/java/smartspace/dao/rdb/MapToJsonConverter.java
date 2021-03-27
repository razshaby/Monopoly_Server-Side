package smartspace.dao.rdb;

import java.util.Map;

import javax.persistence.AttributeConverter;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MapToJsonConverter implements AttributeConverter<Map<String, Object>, String>{
	private ObjectMapper jackson;
	
	public MapToJsonConverter() {
		this.jackson = new ObjectMapper();
	}
	
	@Override
	public String convertToDatabaseColumn(Map<String, Object> map) {
		try {
			// marshalling
			return this.jackson.writeValueAsString(map);
		}catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Map<String, Object> convertToEntityAttribute(String json) {
		try {
			// unmarshalling
			return this.jackson.readValue(json, Map.class);
		}catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
