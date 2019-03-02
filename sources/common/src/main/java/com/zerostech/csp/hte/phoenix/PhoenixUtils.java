package com.zerostech.csp.hte.phoenix;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.phoenix.schema.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

/**
 * Created on 2019/3/2.
 *
 * @author 迹_Jason
 */
public class PhoenixUtils {

    private static final Logger logger = LoggerFactory.getLogger(PhoenixUtils.class);


    public static Map<String, TableSchema> getTableSchema(String originColumnRelation) throws IOException {
        if (originColumnRelation.length() < 1) {
            throw new IOException("Table Schema Not Find");
        }
        String[] linesValues = originColumnRelation.split("\\n");
        if (linesValues.length < 1) {
            return new HashMap<>();
        }
        Map<String, TableSchema> tableSchemas = new HashMap<>();
        for (String linesValue : linesValues) {
            String[] column = linesValue.split(",");
            int cl = column.length;
            if (cl > 0) {
                if (cl < 3) {
                    tableSchemas.put(column[1], new TableSchema(column[0], column[1]));
                } else {
                    tableSchemas.put(column[1], new TableSchema(column[0], column[1], column[2].toUpperCase()));
                }
            } else {
                throw new IOException("Table Schema Pattern Is Error");
            }
        }
        return tableSchemas;
    }

    public static Map<String, Object> convert(Put put, Map<String, TableSchema> tableSchema) {
        Map<String, Object> json = new HashMap<>();
        NavigableMap<byte[], List<Cell>> familyMap = put.getFamilyCellMap();
        for (Map.Entry<byte[], List<Cell>> entry : familyMap.entrySet()) {
            if (!Bytes.toString(entry.getKey()).equals("L#0")) {
                for (Cell cell : entry.getValue()) {
                    try {
                        String qualifier = Bytes.toHex(CellUtil.cloneQualifier(cell));
                        if (tableSchema.containsKey(qualifier)) {
                            TableSchema ts = tableSchema.get(qualifier);
                            logger.info("*** value is " + ts.toString());
                            Object value;
                            switch (ts.getType()) {
                                case "BIGINT":
                                    value = PLong.INSTANCE.toObject(CellUtil.cloneValue(cell));
                                    break;
                                case "LONG":
                                    value = PLong.INSTANCE.toObject(CellUtil.cloneValue(cell));
                                    break;
                                case "DECIMAL":
                                    value = PDecimal.INSTANCE.toObject(CellUtil.cloneValue(cell));
                                    break;
                                case "CHAR":
                                    value = PChar.INSTANCE.toObject(CellUtil.cloneValue(cell));
                                    break;
                                case "TINYINT":
                                    value = PTinyint.INSTANCE.toObject(CellUtil.cloneValue(cell));
                                    break;
                                case "SMALLINT":
                                    value = PSmallint.INSTANCE.toObject(CellUtil.cloneValue(cell));
                                    break;
                                case "INTEGER":
                                    value = PInteger.INSTANCE.toObject(CellUtil.cloneValue(cell));
                                    break;
                                case "FLOAT":
                                    value = PFloat.INSTANCE.toObject(CellUtil.cloneValue(cell));
                                    break;
                                case "DOUBLE":
                                    value = PDouble.INSTANCE.toObject(CellUtil.cloneValue(cell));
                                    break;
                                case "TIMESTAMP":
                                    value = PTimestamp.INSTANCE.toObject(CellUtil.cloneValue(cell));
                                    break;
                                case "DATE":
                                    value = PDate.INSTANCE.toObject(CellUtil.cloneValue(cell));
                                    break;
                                case "TIME":
                                    value = PTime.INSTANCE.toObject(CellUtil.cloneValue(cell));
                                    break;
                                case "BINARY":
                                    value = PBinary.INSTANCE.toObject(CellUtil.cloneValue(cell));
                                    break;
                                case "VARBINARY":
                                    value = PVarbinary.INSTANCE.toObject(CellUtil.cloneValue(cell));
                                    break;
                                default:
                                    value = Bytes.toString(CellUtil.cloneValue(cell));
                            }
                            json.put(ts.getColumnName(), value);
                        } else {
                            logger.info("Not Match Qualifier is {}", qualifier);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        logger.error(ex.getMessage());
                    }

                }
            }
        }
        return json;
    }
}
