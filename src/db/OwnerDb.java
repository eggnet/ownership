package db;
import java.io.InputStreamReader;

public class OwnerDb extends DbConnection {
	public boolean connect(String dbName)
	{
		super.connect(dbName);
		try {
			sr.runScript(new InputStreamReader(this.getClass().getResourceAsStream("createdb.sql")));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	public OwnerDb () {
		super();
	}
}