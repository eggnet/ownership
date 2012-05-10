package ownership;

import java.util.Map;
import java.util.Set;

import db.DbConnection;

public class OwnerManager {
	DbConnection db;
	String startPoint = null;
	String endPoint = null;
	public OwnerManager(DbConnection db) {
		this.db = db;
	}	
	/**
	 * Checks the Owners table in our db to know if we need to update it,
	 * then updates up to our given commit.
	 * @author braden
	 */
	public void update() {
		endPoint = db.getLastOwnerCommit();
		startPoint = db.getLastCommit();
		Map<String, Set<String>> commitsBetween;
		if (endPoint == null)
			commitsBetween = db.getCommitsBeforeChanges(startPoint);
		else
			commitsBetween = db.getCommitsBeforeAndAfterChanges(startPoint, endPoint);
		// Get all commits between these.
		for (String key : commitsBetween.keySet())
		{
			System.out.println("Commit id: " + key);
		}
	}
}
