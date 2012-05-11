package models;

import java.sql.Timestamp;

public class Commit {
	
	private int id;
	private String commit_id;
	private String author;
	private String author_email;
	private String comment;
	private Timestamp commit_date;

	private String branch_id;
	
	public Commit() { }

	public Commit(int id, String commit_id, String author, String author_email,
			String comment, Timestamp commit_date, String branch_id)
	{
		this.id = id;
		this.commit_id = commit_id;
		this.author = author;
		this.author_email = author_email;
		this.comment = comment;
		this.commit_date = commit_date;
		this.branch_id = branch_id;
	}

	public Commit(String commit_id, String author, String author_email,
			String comment, Timestamp commit_date, String branch_id) {
		super();
		this.commit_id = commit_id;
		this.author = author;
		this.author_email = author_email;
		this.comment = comment;
		this.commit_date = commit_date;
		this.branch_id = branch_id;
	}

	public String getBranch_id() {
		return branch_id;
	}
	
	public void setBranch_id(String branchId) {
		branch_id = branchId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCommit_id() {
		return commit_id;
	}

	public void setCommit_id(String commit_id) {
		this.commit_id = commit_id;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getAuthor_email() {
		return author_email;
	}

	public void setAuthor_email(String author_email) {
		this.author_email = author_email;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Timestamp getCommit_date() {
		return commit_date;
	}

	public void setCommit_date(Timestamp commit_date) {
		this.commit_date = commit_date;
	}
}
