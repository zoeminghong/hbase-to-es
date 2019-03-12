package com.zerostech.csp.hte.phoenix;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.CompareOperator;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.RowFilter;
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

    public volatile static Map<String, TableSchema> tableSchema = new HashMap<>();
    public volatile static Map<String, String> unMatchSchema = new HashMap<>();

    public static void convert(Map<String, Object> json,Put put, Connection connection, String regx, CellFunction c, int count) {
        NavigableMap<byte[], List<Cell>> familyMap = put.getFamilyCellMap();
        for (Map.Entry<byte[], List<Cell>> entry : familyMap.entrySet()) {
            if (!Bytes.toString(entry.getKey()).equals("L#0")) {
                for (Cell cell : entry.getValue()) {
                    try {
                        String qualifier = Bytes.toHex(CellUtil.cloneQualifier(cell));
                        if (tableSchema.containsKey(qualifier)) {
                            logger.info("Existed Qualifier is {}", qualifier);
                            matchColumnValue(json, qualifier, cell);
                        } else {
                            if (count > 0) {
                                logger.info("Not Match Qualifier is {}", qualifier);
                                unMatchSchema.put(qualifier, qualifier);
                            } else {
                                if (!unMatchSchema.containsKey(qualifier)) {
                                    logger.info("Start Request Table Schema");
                                    synchronized (regx) {
                                        tableSchema = scanFilter(connection, regx, c);
                                        logger.info(tableSchema.toString());
                                        convert(json,put, connection, regx, c, 1);
                                    }
                                }
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        logger.error(ex.getMessage());
                    }

                }
            }
        }
    }

    public static void matchColumnValue(Map<String, Object> json, String qualifier, Cell cell) {
        TableSchema ts = tableSchema.get(qualifier);
        logger.debug("*** value is " + ts.toString());
        Object value;
        switch (ts.getType()) {
            // BIGINT
            case "-5":
                value = PLong.INSTANCE.toObject(CellUtil.cloneValue(cell));
                break;
            // LONG
            case "10":
                value = PLong.INSTANCE.toObject(CellUtil.cloneValue(cell));
                break;
            //DECIMAL
            case "3":
                value = PDecimal.INSTANCE.toObject(CellUtil.cloneValue(cell));
                break;
            // CHAR
            case "1":
                value = PChar.INSTANCE.toObject(CellUtil.cloneValue(cell));
                break;
            // TINYINT
            case "-6":
                value = PTinyint.INSTANCE.toObject(CellUtil.cloneValue(cell));
                break;
            // SMALLINT
            case "5":
                value = PSmallint.INSTANCE.toObject(CellUtil.cloneValue(cell));
                break;
            // INTEGER
            case "4":
                value = PInteger.INSTANCE.toObject(CellUtil.cloneValue(cell));
                break;
            // FLOAT
            case "6":
                value = PFloat.INSTANCE.toObject(CellUtil.cloneValue(cell));
                break;
            // DOUBLE
            case "8":
                value = PDouble.INSTANCE.toObject(CellUtil.cloneValue(cell));
                break;
            // TIMESTAMP
            case "93":
                value = PTimestamp.INSTANCE.toObject(CellUtil.cloneValue(cell));
                break;
            // DATE
            case "91":
                value = PDate.INSTANCE.toObject(CellUtil.cloneValue(cell));
                break;
            // TIME
            case "92":
                value = PTime.INSTANCE.toObject(CellUtil.cloneValue(cell));
                break;
            // BINARY
            case "-2":
                value = PBinary.INSTANCE.toObject(CellUtil.cloneValue(cell));
                break;
            // VARBINARY
            case "-3":
                value = PVarbinary.INSTANCE.toObject(CellUtil.cloneValue(cell));
                break;
            default:
                value = Bytes.toString(CellUtil.cloneValue(cell));
        }
        json.put(ts.getColumnName(), value);
    }

    public static Map<String, TableSchema> scanFilter(Connection connection, String regx, CellFunction c) throws IOException {
        return scanFilter(connection, regx, c, TableName.valueOf("SYSTEM.CATALOG"));
    }

    public static Map<String, TableSchema> scanFilter(Connection connection, String regx, CellFunction c, TableName tableName) throws IOException {
        Table table = null;
        Map<String, TableSchema> tbM = new HashMap<>();
        try {
            table = connection.getTable(tableName);
            Scan scan = new Scan();
            RowFilter filter = new RowFilter(CompareOperator.EQUAL, new RegexStringComparator(regx));
            scan.setFilter(filter);
            ResultScanner scanner;

            scanner = table.getScanner(scan);
            for (Result rs : scanner) {
                TableSchema tb = new TableSchema();
                rs.listCells().forEach(t -> {
                    String q = Bytes.toString(CellUtil.cloneQualifier(t));
                    if (q.equalsIgnoreCase("COLUMN_QUALIFIER")) {
                        tb.setColumnName(c.action(t));
                        tb.setColumnQualifier(Bytes.toHex(CellUtil.cloneValue(t)));
                    }
                    if (q.equalsIgnoreCase("DATA_TYPE")) {
                        tb.setType(PInteger.INSTANCE.toObject(CellUtil.cloneValue(t)) + "");
                    }
                });
                tbM.put(tb.getColumnQualifier(), tb);
            }
        } finally {
            if (table != null) {
                table.close();
            }
        }
        return tbM;
    }

    @FunctionalInterface
    public interface CellFunction {
        String action(Cell c);
    }
}
