package com.kuspidsamples.config;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.identity.IdentityColumnSupport;
import org.hibernate.dialect.identity.IdentityColumnSupportImpl;
import org.hibernate.type.StandardBasicTypes;

import java.sql.Types;

/**
 * Custom Hibernate Dialect for SQLite database
 */
public class SQLiteDialect extends Dialect {

    public SQLiteDialect() {
        super();

        // Register column types using the modern API
        getDefaultProperties().setProperty("hibernate.jdbc.batch_size", "0");
    }

    @Override
    protected String columnType(int sqlTypeCode) {
        switch (sqlTypeCode) {
            case Types.BIT:
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
                return "integer";
            case Types.BIGINT:
                return "bigint";
            case Types.FLOAT:
                return "float";
            case Types.REAL:
                return "real";
            case Types.DOUBLE:
                return "double";
            case Types.NUMERIC:
            case Types.DECIMAL:
                return "decimal";
            case Types.CHAR:
                return "char";
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
                return "varchar";
            case Types.DATE:
                return "date";
            case Types.TIME:
                return "time";
            case Types.TIMESTAMP:
                return "timestamp";
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
            case Types.BLOB:
                return "blob";
            case Types.CLOB:
                return "clob";
            case Types.BOOLEAN:
                return "integer";
            default:
                return super.columnType(sqlTypeCode);
        }
    }

    @Override
    public IdentityColumnSupport getIdentityColumnSupport() {
        return new SQLiteIdentityColumnSupport();
    }

    @Override
    public boolean hasAlterTable() {
        return false;
    }

    @Override
    public boolean dropConstraints() {
        return false;
    }

    @Override
    public String getDropForeignKeyString() {
        return "";
    }

    @Override
    public String getAddForeignKeyConstraintString(String constraintName,
                                                   String[] foreignKey,
                                                   String referencedTable,
                                                   String[] primaryKey,
                                                   boolean referencesPrimaryKey) {
        return "";
    }

    @Override
    public String getAddPrimaryKeyConstraintString(String constraintName) {
        return "";
    }

    @Override
    public boolean supportsIfExistsBeforeTableName() {
        return true;
    }

    @Override
    public boolean supportsCascadeDelete() {
        return false;
    }

    /**
     * Identity column support for SQLite
     */
    public static class SQLiteIdentityColumnSupport extends IdentityColumnSupportImpl {

        @Override
        public boolean supportsIdentityColumns() {
            return true;
        }

        @Override
        public String getIdentitySelectString(String table, String column, int type) {
            return "select last_insert_rowid()";
        }

        @Override
        public String getIdentityColumnString(int type) {
            return "integer";
        }
    }
}