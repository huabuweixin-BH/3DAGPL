package com.threedagpl.system.handler;

import com.pgvector.PGvector;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.postgresql.util.PGobject;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * PGvector类型处理器
 * 
 * @author threedagpl
 */
@MappedTypes(PGvector.class)
public class PGvectorTypeHandler extends BaseTypeHandler<PGvector> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, PGvector parameter, JdbcType jdbcType) throws SQLException {
        PGobject pGobject = new PGobject();
        pGobject.setType("vector");
        pGobject.setValue(parameter.toString());
        ps.setObject(i, pGobject);
    }

    @Override
    public PGvector getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String vectorString = rs.getString(columnName);
        if (vectorString == null) {
            return null;
        }
        return new PGvector(vectorString);
    }

    @Override
    public PGvector getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String vectorString = rs.getString(columnIndex);
        if (vectorString == null) {
            return null;
        }
        return new PGvector(vectorString);
    }

    @Override
    public PGvector getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String vectorString = cs.getString(columnIndex);
        if (vectorString == null) {
            return null;
        }
        return new PGvector(vectorString);
    }
}
