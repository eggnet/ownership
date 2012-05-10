package db;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import ownership.Resources;

public class DbConnection {
	private ScriptRunner sr;
	public static Connection conn = null;
	public ResultSet savedResultSet = null;
	public boolean isPaging = false;
	public static DbConnection ref = null;
	private String branchName = null;
	private String branchID = null;

	private DbConnection() 
	{
		try 
		{
			Class.forName("org.postgresql.Driver").newInstance();
		} 
		catch (InstantiationException e) 
		{
			e.printStackTrace();
		} 
		catch (IllegalAccessException e) 
		{
			e.printStackTrace();
		} 
		catch (ClassNotFoundException e) 
		{
			e.printStackTrace();
		}
	}
	
	public static DbConnection getInstance()
	{
		if (ref == null)
			return (ref = new DbConnection());
		else
			return ref;
	}

	public String getBranchID() {
		return branchID;
	}
	
	public void setBranchID(String branchID) {
		this.branchID = branchID;
	}

	public String getBranchName() {
		return branchName;
	}

	/**
	 * Should be called AFTER @see {@link #connect(String)}, as it also does 
	 * a lookup on the branchID and sets it behind the scenes.
	 * Also does a lookup in the branches table for 
	 * @param branchName
	 */
	public void setBranchName(String branchName) {
		this.branchName = branchName;
		try
		{
			String[] params = {branchName};
			ResultSet rs = ref.execPreparedQuery("SELECT branch_id from branches where branch_name ~ ? LIMIT 1", params);
			rs.next();
			setBranchID(rs.getString(1));
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Executes a string of SQL on the current database
	 * NOTE: this assumes your sql is valid.
	 * @param sql
	 * @return true if successful
	 */
	public boolean exec(String sql)
	{
		try {
			PreparedStatement s = conn.prepareStatement(sql);
			s.execute();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public boolean execPrepared(String sql, String[] params)
	{
		try {
			PreparedStatement s = conn.prepareStatement(sql);
			for (int i = 1;i <= params.length;i++)
			{
				s.setString(i, params[i-1]);
			}
			s.execute();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Executes the given query with escaped values in String[] params in place of
	 * ? characters in sql.
	 * @param sql ex. "SELECT * FROM something where my_column=?"
	 * @param params ex. {"braden's work"}
	 * @return Query ResultSet on success, null otherwise
	 */
	public ResultSet execPreparedQuery(String sql, String[] params)
	{
		try {
			PreparedStatement s = conn.prepareStatement(sql);
			for (int i = 1;i <= params.length;i++)
			{
				s.setString(i, params[i-1]);
			}
			return s.executeQuery();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Connects to the given database.  
	 * @param dbName
	 * @return true if successful
	 */
	public boolean connect(String dbName)
	{
		try {
			conn = DriverManager.getConnection(Resources.dbUrl + dbName.toLowerCase(), Resources.dbUser, Resources.dbPassword);
			sr = new ScriptRunner(conn, false, true);
			sr.setLogWriter(null);
			sr.runScript(new InputStreamReader(this.getClass().getResourceAsStream("createdb.sql")));
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Close all connection
	 */
	public boolean close()
	{
		try {
			conn.close();
			return true;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Returns a HashMap<(filePath), (fileContents)>
	 * @param commitID
	 * @return
	 */
	public Set<String> getCommitChangedFiles(String commitID)
	{
		Set<String> files = new HashSet<String>();
		try {
			String sql = "SELECT file_id FROM changes where commit_id=?;";
			String[] params = {commitID};
			ResultSet rs = execPreparedQuery(sql, params);
			while(rs.next())
			{
				files.add(rs.getString("file_id"));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		return files;
	}

	/**
	 * Returns an ordered map of <CommitId, Set<ChangedFilePaths>> before a given commitID
	 * @param commitID
	 * @return
	 */
	public Map<String, Set<String>> getCommitsBeforeChanges(String commitID)
	{
		try{
			Map<String, Set<String>> changes = new LinkedHashMap<String, Set<String>>();
			String sql = "SELECT commit_id, file_id from changes natural join commits where " +
					"(branch_id=? or branch_id is NULL) and commit_date < " +
					"(select commit_date from commits where commit_id=? and " +
					"(branch_id=? OR branch_id is NULL)) ORDER BY commit_date desc;";
			String[] params = {this.branchID, commitID, this.branchID};
			ResultSet rs = execPreparedQuery(sql, params);
			String currentCommitId;
			Set<String> currentFileset;
			if (!rs.next())
				return changes;
			currentFileset = new HashSet<String>();
			currentCommitId = rs.getString("commit_id");
			currentFileset.add(rs.getString("file_id"));
			while(rs.next())
			{
				if (rs.getString("commit_id").equals(currentCommitId))
				{
					// append to the current commit
					currentFileset.add(rs.getString("file_id"));
				}
				else
				{
					// start a new one
					changes.put(currentCommitId, currentFileset);
					currentFileset = new HashSet<String>();
					currentCommitId = rs.getString("commit_id");
					currentFileset.add(rs.getString("file_id"));
				}
			}
			changes.put(currentCommitId, currentFileset);
			return changes;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Return a list of commits and their changed files between any 2 commits, order matters.
	 * @param beforeCommitID
	 * @param afterCommitID
	 * @return
	 */
	public Map<String, Set<String>> getCommitsBeforeAndAfterChanges(String beforeCommitID, String afterCommitID)
	{
		try{
			Map<String, Set<String>> changes = new LinkedHashMap<String, Set<String>>();
			String sql = "SELECT commit_id, file_id from changes natural join commits where " +
					"(branch_id=? or branch_id is NULL) and commit_date < " +
					"(select commit_date from commits where commit_id=? and " +
					"(branch_id=? OR branch_id is NULL)) and commit_date > " +
					"(select commit_date from commits where commit_id=? and " +
					"(branch_id=? or branch_id is NULL)) ORDER BY commit_date DESC;";
			
			String[] params = {this.branchID, beforeCommitID, this.branchID, afterCommitID, this.branchID};
			ResultSet rs = execPreparedQuery(sql, params);
			String currentCommitId;
			Set<String> currentFileset;
			if (!rs.next())
				return changes;
			currentFileset = new HashSet<String>();
			currentCommitId = rs.getString("commit_id");
			currentFileset.add(rs.getString("file_id"));
			while(rs.next())
			{
				if (rs.getString("commit_id").equals(currentCommitId))
				{
					// append to the current commit
					currentFileset.add(rs.getString("file_id"));
				}
				else
				{
					// start a new one
					changes.put(currentCommitId, currentFileset);
					currentFileset = new HashSet<String>();
					currentCommitId = rs.getString("commit_id");
					currentFileset.add(rs.getString("file_id"));
				}
			}
			changes.put(currentCommitId, currentFileset);
			return changes;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public Set<String> getBinaryFiles()
	{
		HashSet<String> binaryFiles = new HashSet<String>();
		try {
			String[] params = {};
			ResultSet rs = execPreparedQuery("SELECT file_id from changes EXCEPT (SELECT file_id from files);", params);
			while(rs.next())
			{
				binaryFiles.add(rs.getString(1));
			}
			
			return binaryFiles;
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public Set<String> getChangedFilesFromCommit(String commitID)
	{
		HashSet<String> changed = new HashSet<String>();
		try {
			String[] params = {commitID};
			ResultSet rs = execPreparedQuery("SELECT file_id from changes where commit_id=?", params);
			while(rs.next())
			{
				changed.add(rs.getString(1));
			}
			return changed;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public Set<String> getFileStructureFromCommit(String commitID)
	{
		HashSet<String> files = new HashSet<String>();
		try {
			String[] params = {commitID};
			ResultSet rs = execPreparedQuery("SELECT file_id from source_trees where commit_id=?", params);
			while(rs.next())
			{
				files.add(rs.getString(1));
			}
			return files;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public String getRawFile(String fileID, String commitID)
	{
		try{
			String sql = "SELECT raw_file from files where commit_id=? and file_id=?;";
			String[] params = {commitID, fileID};
			ResultSet rs = execPreparedQuery(sql, params);
			if(rs.next())
				return rs.getString(1);
			else
				return "Binary file";
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Checks whether or not a commit is included in the owners table
	 * @param CommitId
	 * @return
	 */
	public boolean isCommitInOwners(String CommitId) 
	{
		try
		{
			String sql = "SELECT commit_id from owners where commit_id=?;";
			String[] params = {CommitId};
			ResultSet rs = execPreparedQuery(sql, params);
			if (rs.next())
				return true;
			else
				return false;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Gets the latest commit in the owners table.
	 * @return CommitID
	 */
	public String getLastOwnerCommit() 
	{
		try 
		{
			String sql = "Select commit_id from owners natural join commits order by commit_date desc;";
			String[] parms = {};
			ResultSet rs = execPreparedQuery(sql, parms);
			if (rs.next())
				return rs.getString(1);
			else
				return null;
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * Gets the latest commit in the commits table
	 * @return CommitID 
	 */
	public String getLastCommit() 
	{
		try 
		{
			String sql = "Select commit_id from commits order by commit_date desc;";
			String[] parms = {};
			ResultSet rs = execPreparedQuery(sql, parms);
			if (rs.next())
				return rs.getString(1);
			else
				return null;
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
