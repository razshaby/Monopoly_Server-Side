package smartspace.data;

import org.springframework.data.annotation.Id;

//import javax.persistence.Column;
//import javax.persistence.Entity;
//import javax.persistence.EnumType;
//import javax.persistence.Enumerated;
//import javax.persistence.Table;
//import javax.persistence.Transient;

import org.springframework.data.mongodb.core.mapping.Document;

//import javax.persistence.Id;
//
//@Entity
//@Table(name = "USERS")
@Document (collection = "USERS")
public class UserEntity implements SmartspaceEntity<String> {
	
	private String key;
	private String userSmartspace;
	private String userEmail;
	private String username;
	private String avatar;
	private UserRole role;
	private long points;
	// private String userKey;

	public UserEntity() {
	}

	public UserEntity(String userEmail) {
		this.userEmail = userEmail;
	}

	public UserEntity(String userSmartspace, String userEmail, String username, String avatar, UserRole role,
			long points) {
		this.userSmartspace = userSmartspace;
		this.userEmail = userEmail;
		this.username = username;
		this.avatar = avatar;
		this.role = role;
		this.points = points;

	}

	//@Transient
	public String getUserSmartspace() {
		return userSmartspace;
	}

	public void setUserSmartspace(String userSmartspace) {
		this.userSmartspace = userSmartspace;
	}

	//@Transient
	public String getUserEmail() {
		/*
		 * if(getKey()==null) { return userEmail; } else if(userEmail==null) { String[]
		 * tempStringArrayHolder = getKey().split("\\|"); return
		 * tempStringArrayHolder[1]; } else if(getKey().isEmpty() ||
		 * !userEmail.isEmpty()) { return userEmail; } else { throw new
		 * RuntimeException("there is no eMail exsists for the user" + getUsername()); }
		 */
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	//@Enumerated(EnumType.STRING)
	public UserRole getRole() {
		return role;
	}

	public void setRole(UserRole role) {
		this.role = role;
	}

	public long getPoints() {
		return points;
	}

	public void setPoints(long points) {
		this.points = points;
	}

//	@Override
//	@Column(name = "ID")
//	@Id
	@Id
	public String getKey() {
		
		return this.userSmartspace + "|" + this.userEmail;
	}

	@Override
	public void setKey(String key) {
		this.key=key;
		String[] tempStringArrayHolder = key.split("\\|");
		this.userSmartspace = tempStringArrayHolder[0];
		this.userEmail = tempStringArrayHolder[1];

	}

	@Override
	public String toString() {
		return "UserEntity [key=" + key + ", userSmartspace=" + userSmartspace + ", userEmail=" + userEmail
				+ ", username=" + username + ", avatar=" + avatar + ", role=" + role + ", points=" + points + "]";
	}
	

}
