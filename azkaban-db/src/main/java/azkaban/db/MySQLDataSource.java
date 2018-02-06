/*
  * Copyright 2017 LinkedIn Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package azkaban.db;
import java.sql.*;
import java.sql.DriverManager;
import azkaban.utils.Props;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.log4j.Logger;
@Singleton
public class MySQLDataSource extends AzkabanDataSource {

  private static final Logger logger = Logger.getLogger(MySQLDataSource.class);
  private final DBMetrics dbMetrics;

  @Inject
  public MySQLDataSource(final Props props, final DBMetrics dbMetrics) {
    super();
    this.dbMetrics = dbMetrics;

    final int port = props.getInt("mysql.port");
    final String host = props.getString("mysql.host");
    final String dbName = props.getString("mysql.database");
    final String user = props.getString("mysql.user");
    final String password = props.getString("mysql.password");
    final int numConnections = props.getInt("mysql.numconnections");


    final String url = "jdbc:oracle:thin:@" + (host + ":" + port + ":" + dbName);
   //final String url = "jdbc:mysql://" + (host + ":" + port + "/" + dbName);
    addConnectionProperty("useUnicode", "yes");
    addConnectionProperty("characterEncoding", "UTF-8");
    //setDriverClassName("com.mysql.jdbc.Driver");
    setDriverClassName("oracle.jdbc.driver.OracleDriver");
    setUsername(user);
    setPassword(password);
    setUrl(url);
    setMaxTotal(numConnections);
    setValidationQuery("/* ping */ select 1");
    setTestOnBorrow(true);
  }
  /**
   * This method overrides {@link BasicDataSource#getConnection()}, in order to have retry logics.
   * We don't make the call synchronized in order to guarantee normal cases performance.
   */
  @Override
  public Connection getConnection() throws SQLException {

    this.dbMetrics.markDBConnection();
    final long startMs = System.currentTimeMillis();
    Connection connection = null;
    int retryAttempt = 1;
    connection = DriverManager.getConnection("jdbc:oracle:thin:@10.200.99.217:1521:orcl","nihar_test","nihar_test");
    return connection;
  }

  private boolean isReadOnly(final Connection conn) throws SQLException {
    final Statement stmt = conn.createStatement();
    final ResultSet rs = stmt.executeQuery("SELECT @@global.read_only");
    if (rs.next()) {
      final int value = rs.getInt(1);
      return value != 0;
    }
    throw new SQLException("can not fetch read only value from DB");
  }

  private void sleep(final long milliseconds) {
    try {
      Thread.sleep(milliseconds);
    } catch (final InterruptedException e) {
      logger.error("Sleep interrupted", e);
    }
  }

  @Override
  public String getDBType() {
    return "mysql";
  }

  @Override
  public boolean allowsOnDuplicateKey() {
    return true;
  }
}

