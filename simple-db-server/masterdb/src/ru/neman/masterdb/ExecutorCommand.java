package ru.neman.masterdb;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExecutorCommand {
    // general info
    private final static String ROOT_DB = "data\\";
    
    // client commands
    private final static String CREATE = "CREATE TABLE";
    private final static String SCHEMA = "SCHEMA TABLE";
    private final static String DELETE = "DELETE TABLE";
    private final static String INSERT = "INSERT INTO";
    private final static String SHOW = "SHOW TABLES";
    private final static String SELECT = "SELECT";
    private final static String HELP = "HELP";

    // response
    private final static String HELP_RESPONSE = "CREATE - create table\n" +
            "example: CREATE TABLE Persons (PersonID int, LastName varchar)\n" +
            "INSERT_INTO - insert new! data into table\n" +
            "example: INSERT INTO Persons (val1,val2)\n" +
            "SELECT - select * from table\n" +
            "example: SELECT (col1, col2) FROM table\n" +
            "DELETE - delete table\n" +
            "example: DELETE TABLE Persons\n";
    private final static String TABLE_DELETED = "Table is destroyed";
    private final static String BAD_REQUEST = "Bad Request";
    private final static String SERVER_PROBLEM = "Server problem";
    private final static String TABLE_ALREADY_EXIST = "Table already exists";
    private final static String TABLE_DOESNT_EXIST = "Table doesn't exists";
    private final static String ONLY_INT_OR_VARCHAR = "Only int or varchar";
    private final static String DONE = "done";

    private ExecutorCommand() {
    }

    public static String parseRequest(String upperLine) {
        if (upperLine.startsWith(CREATE)) return createCommand(upperLine);
        if (upperLine.startsWith(SCHEMA)) return schemaCommand(upperLine);
        if (upperLine.startsWith(INSERT)) return insertCommand(upperLine);
        if (upperLine.startsWith(SHOW)) return showTablesCommand(upperLine);
        if (upperLine.startsWith(SELECT)) return selectCommand(upperLine);
        if (upperLine.startsWith(HELP)) return helpCommand(upperLine);
        if (upperLine.startsWith(DELETE)) return deleteCommand(upperLine);
        return BAD_REQUEST;
    }

    private static String showTablesCommand(String line) {
        if (line.equals(SHOW)) {
            Path root = Path.of(ROOT_DB);
            StringBuilder s = new StringBuilder();
            for (File c : root.toFile().listFiles())
                s.append(c.getName() + "\n");
            return s.toString();
        } else return BAD_REQUEST;
    }

    private static String createCommand(String line) {
        Pattern p = Pattern.compile("(^" + CREATE + ") ([a-zA-Z0-9]+) " + "(\\(.*" + "\\))");
        Matcher m = p.matcher(line);
        String tableName = "";
        String schema = "";

        if (m.find()) {
            schema = m.group(2) + m.group(3);
            tableName = m.group(2);
            if (isTableExist(tableName)) return TABLE_ALREADY_EXIST;

            String[] properties = m.group(3).replaceAll("(\\(|\\))", "").split(",");


            Map<String, String> map = new HashMap<>();
            for (String s : properties) {
                String[] keyAndValue = s.trim().split(" ");

                if (keyAndValue.length > 2) return BAD_REQUEST;

                if (keyAndValue[1].equals("INT")) map.put(keyAndValue[0], "Integer");
                else if (keyAndValue[1].equals("VARCHAR")) map.put(keyAndValue[0], "String");
                else return ONLY_INT_OR_VARCHAR;
            }

            try {
                Files.createDirectory(Path.of(ROOT_DB + tableName));
                Path path = Files.createFile(Path.of(ROOT_DB + tableName + "\\db.data"));
                Files.createFile(Path.of(ROOT_DB + tableName + "\\data.csv"));
                PrintWriter pw = new PrintWriter(path.toFile());
                pw.write(schema + "\n");

                for (Map.Entry<String, String> e : map.entrySet())
                    pw.write(e.getKey() + "=" + e.getValue() + "\n");

                pw.flush();
                pw.close();
                return DONE;
            } catch (IOException e) {
                return SERVER_PROBLEM;
            }
        } else {
            return BAD_REQUEST;
        }
    }

    private static String schemaCommand(String line) {
        Pattern p = Pattern.compile("(^" + SCHEMA + ") ([a-zA-Z0-9]+)");
        Matcher m = p.matcher(line);
        if (m.find()) {
            Path path = Path.of(ROOT_DB + m.group(2) + "\\db.data");
            try {
                if (!Files.exists(path)) return TABLE_DOESNT_EXIST;
                return Files.readAllLines(path).get(0);
            } catch (IOException e) {
                return SERVER_PROBLEM;
            }
        } else {
            return TABLE_DOESNT_EXIST;
        }
    }

    private static String deleteCommand(String line) {
        Pattern p = Pattern.compile("(^" + DELETE + ") ([a-zA-Z0-9]+)");
        Matcher m = p.matcher(line);
        if (m.find()) {
            try {
                Path path = Path.of(ROOT_DB + m.group(2));
                if (path.toFile().isDirectory()) {
                    for (File c : path.toFile().listFiles())
                        c.delete();
                }
                Files.deleteIfExists(path);
                return TABLE_DELETED;
            } catch (IOException e) {
                return SERVER_PROBLEM;
            }
        } else {
            return BAD_REQUEST;
        }
    }

    private static String insertCommand(String line) {
        Pattern p = Pattern.compile("(^" + INSERT + ") ([a-zA-Z0-9]+) " + "(\\(.*" + "\\))");
        Matcher m = p.matcher(line);
        String tableName = "";
        if (m.find()) {
            tableName = m.group(2);
            if (!isTableExist(tableName)) return TABLE_DOESNT_EXIST;
            try {
                String[] values = m.group(3).replaceAll("(\\(|\\))", "").split(",");

                Path patchSchema = Path.of(ROOT_DB + m.group(2) + "\\db.data");
                List<String> types = Files.readAllLines(patchSchema);

                if ((types.size() - 1) != values.length) {
                    return BAD_REQUEST;
                }

                Path pathValues = Path.of(ROOT_DB + tableName + "\\data.csv");
                PrintWriter pw = new PrintWriter(new FileWriter(pathValues.toString(), true));

                StringBuilder s = new StringBuilder();
                for (int i = 0; i < values.length; i++) {
                    //types ignore first row - it's schema
                    if (checkTypes(types.get(i + 1).split("=")[1], values[i])) s.append(values[i].trim() + ",");
                    else {
                        System.out.println("не совместимость типов");
                        return BAD_REQUEST;
                    }
                }
                pw.write(s.toString() + "\n");
                pw.flush();
                pw.close();
                return DONE;
            } catch (IOException e) {
                e.printStackTrace();
                return BAD_REQUEST;
            }
        } else return BAD_REQUEST;
    }

    private static boolean checkTypes(String type, String value) {
        if (type.equals("Integer")) {
            try {
                Integer.parseInt(value);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        } else if (type.equals("String")) {
            try {
                Double.parseDouble(value);
                throw new IllegalAccessException();
            } catch (NumberFormatException e) {
                return true;
            } catch (IllegalAccessException e) {
                return false;
            }
        } else return false;
    }

    private static String selectCommand(String line) {
        Pattern p = Pattern.compile("(^" + SELECT + ")" + " (\\*) " + "(FROM)" + " ([a-zA-Z0-9]+)$");
        Matcher m = p.matcher(line);
        if (m.find()) {
            String tableName = m.group(4);
            if (!isTableExist(tableName)) return TABLE_DOESNT_EXIST;
            try {
                List<String> dataInFiles = Files.readAllLines(Path.of(ROOT_DB + tableName + "\\data.csv"));
                List<String> rawTypes = Files.readAllLines(Path.of(ROOT_DB + tableName + "\\db.data"));

                StringBuilder s = new StringBuilder();
                s.append(rawTypes.get(0) + "\n");
                for (int i = 0; i < dataInFiles.size(); i++) {
                    s.append(dataInFiles.get(i) + "\n");
                }
                return s.toString();
            } catch (IOException e) {
                return SERVER_PROBLEM;
            }
        } else return BAD_REQUEST;
    }

    private static String helpCommand(String line) {
        return HELP_RESPONSE;
    }

    private static boolean isTableExist(String table) {
        return Files.exists(Path.of(ROOT_DB + table));
    }
}
