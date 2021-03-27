package smartspace.dao.rdb;

import org.springframework.data.mongodb.core.mapping.Document;

@Document (collection = "SEED")
public class IdentitySeed {
	private String id;

	public IdentitySeed() {
	}

	//@Id
	//@GeneratedValue//(strategy=GenerationType.AUTO)
	@org.springframework.data.annotation.Id
	public String getId() {
		return id;
		//return System.nanoTime();
	}

	public void setId(String id) {
		this.id = id;
	}

}