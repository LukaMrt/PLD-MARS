package com.lukamaret.pld_mars_common;

import com.lukamaret.pld_mars_common.exception.DBException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author WASO Team
 */
public class DBConnection {

    protected Connection connection;

    public DBConnection(String url, String user, String password, String... tables) throws DBException {

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Class.forName("org.apache.derby.jdbc.ClientDriver");

            this.connection = DriverManager.getConnection(url, user, password);

            for (String table : tables) {
                checkDB(table);
            }
        } catch (ClassNotFoundException ex) {
            throw new DBException("DB Driver not found.", ex);
        } catch (SQLException ex) {
            throw new DBException("Could not connect to DB " + url, ex);
        } catch (IOException ex) {
            throw new DBException("Could not get DB scripts", ex);
        }

    }

    protected final void checkDB(String name) throws SQLException, IOException {
        DatabaseMetaData metadata = this.connection.getMetaData();
        ResultSet result = metadata.getTables(null, null, name, null);
        if (!result.next()) {
            Statement statement = this.connection.createStatement();

            String createTableStatement = readSqlResource(name + ".structure");
            String insertDataStatement = readSqlResource(name + ".data");

            statement.executeUpdate(createTableStatement);
            statement.executeUpdate(insertDataStatement);
        }
    }
    
    public void close() {
        try {
            this.connection.close();
        } catch (SQLException ex) {
            // Ignore
        }
    }

    public PreparedStatement buildPrepareStatement(String query) throws DBException {

        try {
            return this.connection.prepareStatement(query);
        } catch (SQLException ex) {
            throw new DBException("DB Query Exception", ex);
        }

    }

    protected static List<Object[]> transformResultSet(ResultSet results) throws SQLException {
        ResultSetMetaData columnMetaData = results.getMetaData();
        int columns = columnMetaData.getColumnCount();

        List<Object[]> resultList = new ArrayList<Object[]>();
        while (results.next()) {
            Object[] row = new Object[columns];
            for (int col = 1; col <= columns; col++) {
                results.getObject(col);
                switch (columnMetaData.getColumnType(col)) {
                    case Types.INTEGER:
                        row[col - 1] = results.wasNull() ? null : results.getInt(col);
                        break;
                    case Types.VARCHAR:
                        row[col - 1] = results.wasNull() ? null : results.getString(col);
                        break;
                    case Types.DOUBLE:
                        row[col - 1] = results.wasNull() ? null : results.getDouble(col);
                        break;
                    case Types.FLOAT, Types.DECIMAL:
                        row[col - 1] = results.wasNull() ? null : results.getFloat(col);
                        break;
                    case Types.BOOLEAN:
                        row[col - 1] = results.wasNull() ? null : results.getBoolean(col);
                        break;
                    case Types.TIMESTAMP:
                        row[col - 1] = results.wasNull() ? null : results.getTimestamp(col).getTime();
                        break;
                    case Types.DATE:
                        row[col - 1] = results.wasNull() ? null : results.getDate(col).getTime();
                        break;
                    case Types.NULL:
                        row[col - 1] = null;
                        break;
                }
            }
            resultList.add(row);
        }

        return resultList;
    }

    public List<Object[]> launchQuery(String query, Object... parameters) throws DBException {

        try {
            PreparedStatement preparedStatement = this.connection.prepareStatement(query);
            int paramIndex = 0;
            //preparedStatement.setInt(1, ...);
            for (Object parameter : parameters) {
                preparedStatement.setObject(++paramIndex, parameter);
            }

            ResultSet results = preparedStatement.executeQuery();
            List<Object[]> resultList = transformResultSet(results);

            preparedStatement.close();

            return resultList;

        } catch (SQLException ex) {
            throw new DBException("DB Query Exception", ex);
        }

    }

    public List<Object[]> launchQueryWithArrayParameters(String query, Object... parameters) throws DBException {

        try {

            StringBuilder queryWithArray = getStringBuilder(query, parameters);

            PreparedStatement preparedStatement = this.connection.prepareStatement(queryWithArray.toString());

            int paramIndex = 0;
            for (Object parameter : parameters) {
                if (parameter instanceof Collection) {
                    Collection collectionParameter = (Collection) parameter;
                    if (collectionParameter.isEmpty()) {
                        preparedStatement.setObject(++paramIndex, null);
                    } else {
                        for (Object innerParameter : (Collection) collectionParameter) {
                            preparedStatement.setObject(++paramIndex, innerParameter);
                        }
                    }
                } else {
                    preparedStatement.setObject(++paramIndex, parameter);
                }
            }

            ResultSet results = preparedStatement.executeQuery();
            List<Object[]> resultList = transformResultSet(results);

            preparedStatement.close();

            return resultList;

        } catch (SQLException ex) {
            throw new DBException("DB Query Exception", ex);
        }

    }

    private static StringBuilder getStringBuilder(String query, Object[] parameters) {
        StringBuilder queryWithArray = new StringBuilder();

        final String arrayMarker = "([?])";
        int currentCharIndex = 0;
        int parameterCharIndex;
        int currentParameterArrayIndex = 0;
        while ((parameterCharIndex = query.indexOf(arrayMarker, currentCharIndex)) >= 0) {

            queryWithArray.append(query.substring(currentCharIndex, parameterCharIndex));
            currentCharIndex = parameterCharIndex + arrayMarker.length();

            queryWithArray.append('(');

            for (; currentParameterArrayIndex < parameters.length && !(parameters[currentParameterArrayIndex] instanceof Collection); currentParameterArrayIndex++) {
            }

            if (currentParameterArrayIndex < parameters.length) {
                Collection collectionParameter = (Collection) parameters[currentParameterArrayIndex];

                if (collectionParameter.isEmpty()) {
                    queryWithArray.append('?'); // for NULL value
                } else {
                    for (int i = 0; i < collectionParameter.size(); i++) {
                        if (i > 0) {
                            queryWithArray.append(',');
                        }
                        queryWithArray.append('?');
                    }
                }
            }

            queryWithArray.append(')');
        }
        queryWithArray.append(query.substring(currentCharIndex));
        return queryWithArray;
    }

    public int launchUpdate(String query, Object... parameters) throws DBException {

        try {
            PreparedStatement preparedStatement = this.connection.prepareStatement(query);
            int paramIndex = 0;
            //preparedStatement.setInt(1, ...);
            for (Object parameter : parameters) {
                preparedStatement.setObject(++paramIndex, parameter);
            }

            int status = preparedStatement.executeUpdate();

            preparedStatement.close();

            return status;

        } catch (SQLException ex) {
            throw new DBException("DB Update Exception", ex);
        }

    }

    protected static String readSqlResource(String name) throws IOException {
        InputStream fileContent = DBConnection.class.getResourceAsStream("/sql/" + name + ".sql");
        BufferedReader input = new BufferedReader(new InputStreamReader(fileContent, JsonServletHelper.ENCODING_UTF8));
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = input.readLine()) != null) {
            content.append(line).append("\n");
        }
        return content.toString();
    }

    public static void printResultRows(PrintStream out, List<Object[]> resultList) {
        int rowIndex = 0;
        for (Object[] row : resultList) {
            StringBuilder rowDescription = new StringBuilder("{");
            boolean first = true;
            for (Object datum : row) {
                if (first) {
                    first = false;
                } else {
                    rowDescription.append(',');
                }
                rowDescription.append(datum.toString());
            }
            rowDescription.append("}");
            out.println("#" + (++rowIndex) + ": " + rowDescription.toString());
        }
    }
}
