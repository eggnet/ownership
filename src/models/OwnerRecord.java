package models;

public class OwnerRecord {
	private String Id;
	private String Email;
	
	public OwnerRecord() { } 
	public String getId() {
		return Id;
	}
	public void setId(String id) {
		Id = id;
	}
	public String getEmail() {
		return Email;
	}
	public void setEmail(String email) {
		Email = email;
	}
}
