package ownership;

import db.DbConnection;

public class OwnerManager {
	DbConnection db;
	
	public OwnerManager(DbConnection db) {
		this.db = db;
	}
	/**
	 * Checks the Owners table in our db to know if we need to update it,
	 * then updates up to our given commit.
	 * @author braden
	 */
	public void update() {
		// Check if the owners table is up to date.
		if (this.isUpToDate())
			return;
		// Otherwise update the table.
	}
	
	public boolean isUpToDate() {
		//String endPoint = db.getLastOwnerCommit();
		//String startPoint = db.getLastCommit();
//		if (db.isCommitInOwners(CommitId))
//			return true;
		return false;
	}
	
	public boolean test () {
		return true;
	}
}
