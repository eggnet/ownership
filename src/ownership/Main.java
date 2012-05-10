package ownership;

import db.DbConnection;

public class Main {
	public static void main(String[] args)
	{
		DbConnection db = DbConnection.getInstance();
		db.connect(args[0]);
		db.setBranchName(args[1]);
		OwnerManager ownerMgr = new OwnerManager(db);
		ownerMgr.update();
	}
}
