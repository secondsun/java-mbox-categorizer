/*
 * Copyright (C) 2016 Your Organisation.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package net.saga.java.mbox.ajug.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;

/**
 *
 * This class will create and keep up to date the internal derby database.
 *
 * @author summers
 */
public class DerbyBootStrap implements AutoCloseable {

    public static final String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";

    public DerbyBootStrap() {
        try {
            Class.forName(DRIVER).newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void startUp() {
        try (Connection conn = DriverManager.getConnection("jdbc:derby:categorizer;create=true", "app", "app");) {
            if (databaseExists(conn)) {
                checkUpdate(conn);
            } else {
                createDatabase(conn);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DerbyBootStrap.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void close() throws Exception {
        DriverManager.getConnection("jdbc:derby:notifications;shutdown=true");
    }

    private boolean databaseExists(Connection conn) throws SQLException {
        
        try (ResultSet res = conn.prepareCall("SELECT * FROM sys.systables ").executeQuery()) {
            while (res.next()) {
                if (res.getString(2).equalsIgnoreCase("version"))
                    return true;
            }
            return false;
        }
    }

    private void checkUpdate(Connection conn) throws SQLException {
        int currentVersion = queryCurrentVersion(conn);
        update(conn, currentVersion + 1);
    }

    private void createDatabase(Connection conn) throws SQLException {
        try (InputStream stream = getClass().getResourceAsStream("/db/create_version.sql")) {
            executeFile(stream, conn);
            update(conn, 1);

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private int queryCurrentVersion(Connection conn) throws SQLException {
        try (ResultSet res = conn.prepareCall("select max(version) from version").executeQuery()) {
            if (res.next()) {
                return res.getInt(1);
            }
            return 0;
        }
    }

    private void update(Connection conn, int startingVersion) throws SQLException {
        try (InputStream stream = getClass().getResourceAsStream("/db/version" + startingVersion + ".sql")) {
            if (stream != null) {
                executeFile(stream, conn);
                update(conn, startingVersion + 1);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void executeFile(InputStream stream, Connection conn) throws IOException, SQLException {
        List<String> createVersion = IOUtils.readLines(stream, Charset.forName("UTF-8"));
            for (String query : createVersion) {
                conn.prepareCall(query.replace(";", "")).execute();
            }
    }

}
