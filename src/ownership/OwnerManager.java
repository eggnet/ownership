package ownership;

import java.util.List;
import java.util.Map;
import java.util.Set;

import ownership.Resources.ChangeType;

import models.Commit;
import differ.filediffer;
import differ.filediffer.diffObjectResult;
import db.DbConnection;

public class OwnerManager {
	DbConnection db;
	String startPoint = null;
	String endPoint = null;
	filediffer diff = null;
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
		Map<Commit, Map<String, Resources.ChangeType>> commitsBetween;
		if (endPoint == null)
			commitsBetween = db.getCommitObjectsBeforeChanges(startPoint, true, true);
		else
			commitsBetween = db.getCommitObjectsBeforeAndAfterChanges(startPoint, endPoint, true, true);
		// Get all commits between these.
		for (Commit key : commitsBetween.keySet())
		{
			System.out.println("Commit id: " + key.getCommit_id() + "\t" + key.getCommit_date().toString());
			for (String filePath: commitsBetween.get(key).keySet())
			{
				Set<String> s = commitsBetween.get(key).keySet();
				// need to update our owners table
				diff = new filediffer(db.getLatestRevOfFile(key.getCommit_id(), filePath), db.getRawFile(filePath, key.getCommit_id()));
				if (diff.getNewFileContent() == null)
				{
					db.insertOwnerRecord(key.getCommit_id(), key.getAuthor_email(), filePath, 0, -1, ChangeType.DELETE);
				}
				else if (diff.getOldFileContent() == null)
				{
					db.insertOwnerRecord(key.getCommit_id(), key.getAuthor_email(), filePath, 0, -1, ChangeType.ADD);
				}
				else 
				{
					diff.diffFilesLineMode();
					if(diff.isModified())
					{
						// return the change sets from the two files
						List<diffObjectResult> deleteObjects = diff.getDeleteObjects();
						List<diffObjectResult> insertObjects = diff.getInsertObjects();
						
						// for all the insert objects.
						for (diffObjectResult insert : insertObjects) 
							db.insertOwnerRecord(key.getCommit_id(), key.getAuthor_email(), filePath, insert.start, insert.end, ChangeType.MODIFY);
						
						for (diffObjectResult delete : deleteObjects)
							db.insertOwnerRecord(key.getCommit_id(), key.getAuthor_email(), filePath, delete.start, delete.end, ChangeType.MODIFY);
					}	
				}	
			}
			db.execBatch();	// Make the insertion
//			System.out.println("}");
		}
	}
}
