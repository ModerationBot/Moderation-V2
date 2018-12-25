package me.just.moderation.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MysqlConnector {
	private static Connection connection;
	private final String host;
	private final String port;
	private final String user;
	private final String password;
	private final String database;

	private final Logger LOG = LoggerFactory.getLogger(getClass().getSimpleName());


	public MysqlConnector(String host, String port, String user, String password, String database) {
		this.host = host;
		this.port = port;
		this.user = user;
		this.password = password;
		this.database = database;
	}


	public static Connection getConn() {
		return connection;
	}

	public MysqlConnector connect() {

		try {
			connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database + "?autoReconnect=true&useLegacyDatetimeCode=false&serverTimezone=UTC", this.user, this.password);
		} catch (final SQLException e) {
			e.printStackTrace();
		}

		return this;
	}


	public MysqlConnector initialize() {

		connect();

		try {
			connection.prepareStatement("SELECT 1 FROM tablename LIMIT 1").executeQuery();
		} catch (final SQLException e) {
			try {
				connection.prepareStatement(
						"CREATE TABLE IF NOT EXISTS `plugins` (\n" +
								"  `` text,\n" +
								"  `` text,\n" +
								"  `` text,\n" +
								"  `` text\n" +
								") ENGINE=InnoDB DEFAULT CHARSET=utf8;"
						).execute();
				LOG.info("MysqlConnector structure created...");
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
		}

		return this;
	}


	public String getString(String table, String key ,String where, String value) {

		try {
			final PreparedStatement ps = connection.prepareStatement(String.format("SELECT * FROM %s WHERE %s = %s", table, where, value));
			final ResultSet rs = ps.executeQuery();
			if (rs.next())
				return rs.getString(key);
		} catch (final SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean getBool(String table, String key ,String where, String value) {
		try {
			final PreparedStatement ps = connection.prepareStatement(String.format("SELECT * FROM %s WHERE %s = %s", table, where, value));
			final ResultSet rs = ps.executeQuery();
			if (rs.next())
				return rs.getBoolean(key);
		} catch (final SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public MysqlConnector setString(String table, String key, String value, String where, String wherevalue) {
		try {
			PreparedStatement ps;
			final PreparedStatement check = connection.prepareStatement(String.format("SELECT * FROM %s WHERE %s = '%s'", table, where, wherevalue));

			if (check.executeQuery().next())
				ps = connection.prepareStatement(String.format("UPDATE %s SET %s = '%s' WHERE %s = '%s'", table, key, value, where, wherevalue));
			else
				ps = connection.prepareStatement(String.format("INSERT INTO %s (%s, %s) VALUES ('%s', '%s')", table, where, key, wherevalue, value));

			ps.execute();

		} catch (final SQLException e) {
			e.printStackTrace();
		}

		return this;
	}

	public MysqlConnector setBool(String table, String key, boolean value, String where, String wherevalue) {
		try {
			PreparedStatement ps;
			final PreparedStatement check = connection.prepareStatement(String.format("SELECT * FROM %s WHERE %s = '%s'", table, where, wherevalue));

			if (check.executeQuery().next())
				ps = connection.prepareStatement(String.format("UPDATE %s SET %s = '%s' WHERE %s = '%s'", table, key, value ? 1 : 0, where, wherevalue));
			else
				ps = connection.prepareStatement(String.format("INSERT INTO %s (%s, %s) VALUES ('%s', '%s')", table, where, key, wherevalue, value ? 1 : 0));

			ps.execute();

		} catch (final SQLException e) {
			e.printStackTrace();
		}

		return this;
	}

	public MysqlConnector dropEntry(String table, String where, String wherevalue) {
		try {
			PreparedStatement ps;
			final PreparedStatement check = connection.prepareStatement(String.format("SELECT * FROM %s WHERE %s = '%s'", table, where, wherevalue));

			if (check.executeQuery().next()) {
				ps = connection.prepareStatement(String.format("DELETE FROM %s WHERE %s = '%s'", table, where, wherevalue));
				ps.execute();
			}

		} catch (final SQLException e) {
			e.printStackTrace();
		}

		return this;
	}


}
