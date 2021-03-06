/*
 * Copyright 2007 The Fornax Project Team, including the original
 * author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sculptor.framework.util.db;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseSequenceFilter;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.FilteredDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.filter.ITableFilter;
import org.dbunit.operation.DatabaseOperation;

/**
 * DBUnit tear down operation that deletes all tables in the database.
 * The difference between this and {@link org.dbunit.operation.DeleteAllOperation}
 * is that this uses {@link org.dbunit.database.DatabaseSequenceFilter}
 * to determine the order of the tables and this affects all tables
 * in the database, not only the ones in the data set.
 *
 * @author Patrik Nordwall
 *
 */
public class OrderedDeleteAllOperation extends DatabaseOperation {

    public void execute(IDatabaseConnection connection, IDataSet dataSet) throws DatabaseUnitException,
            SQLException {

        ITableFilter filter = new DatabaseSequenceFilter(connection);
        IDataSet dataset = new FilteredDataSet(filter, connection.createDataSet());
        String[] tableNames = dataset.getTableNames();
        List<String> reversedTableNames = new ArrayList<String>();
        reversedTableNames.addAll(Arrays.asList(tableNames));
        Collections.reverse(reversedTableNames);

        Statement stmt = null;
        try {
            stmt = connection.getConnection().createStatement();
            for (String table : reversedTableNames) {
                // don't delete sequence table, eclipselink will not add new sequences
                if (!table.equalsIgnoreCase("SEQUENCE")) {
                    stmt.addBatch("DELETE from " + table);
                }
            }
            stmt.executeBatch();
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ignore) {
                }
            }
        }
    }

}