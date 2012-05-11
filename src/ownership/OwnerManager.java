package ownership;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
		Map<Commit, Set<String>> commitsBetween;
		if (endPoint == null)
			commitsBetween = db.getCommitObjectsBeforeChanges(startPoint, true);
		else
			commitsBetween = db.getCommitObjectsBeforeAndAfterChanges(startPoint, endPoint, true);
		
		// Get all commits between these.
		for (Commit key : commitsBetween.keySet())
		{
			System.out.println("Commit id: " + key + "\t{");
			for (String filePath: commitsBetween.get(key))
			{
				System.out.println("\tChanged file : " + filePath);
				// need to update our owners table
				diff = new filediffer(db.getLatestRevOfFile(key.getCommit_id(), filePath), db.getRawFile(filePath, key.getCommit_id()));
				diff.diffFilesLineMode();
				if(diff.isModified())
				{
					// return the change sets from the two files
					System.out.println("+-\t" + filePath);
					List<diffObjectResult> deleteObjects = diff.getDeleteObjects();
					List<diffObjectResult> insertObjects = diff.getInsertObjects();
					diff.print();
					
					// for all the insert objects.
					for (diffObjectResult insert : insertObjects)
						db.insertOwnerRecord(key.getCommit_id(), key.getAuthor(), filePath, insert.start, insert.end, "0");
					
					for (diffObjectResult delete : deleteObjects)
						db.insertOwnerRecord(key.getCommit_id(), key.getAuthor(), filePath, delete.start, delete.end, "1");
					
					db.execBatch();	// Make the insertion
				}		
			}
			System.out.println("}");
		}
	}
	
	
	
}
