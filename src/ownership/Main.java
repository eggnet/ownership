package ownership;

import db.OwnerDb;;

public class Main {
	public static void main(String[] args)
	{
		OwnerDb db = new OwnerDb();
		db.connect(args[0]);
		db.setBranchName(args[1]);
		OwnerManager ownerMgr = new OwnerManager(db);
		ownerMgr.update();
	}
}
