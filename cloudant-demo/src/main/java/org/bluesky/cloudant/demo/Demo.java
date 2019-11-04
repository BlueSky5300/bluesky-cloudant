package org.bluesky.cloudant.demo;

import com.cloudant.client.api.Database;
import com.cloudant.client.api.query.QueryBuilder;
import com.cloudant.client.api.query.QueryResult;
import org.bluesky.cloudant.demo.model.Student;
import org.bluesky.cloudant.demo.model.Teacher;
import org.bluesky.cloudant.utils.CloudantDBManager;

import java.util.*;

import static com.cloudant.client.api.query.Expression.eq;
import static com.cloudant.client.api.query.Expression.gt;

public class Demo {

    private final static String DATABASE_NAME = "school-info";

    private final static List<Teacher> teachers = new ArrayList<Teacher>() {{
        add(new Teacher("0001", "Mark", 30, "English"));
        add(new Teacher("0002", "John", 32, "Math"));
        add(new Teacher("0003", "Jack", 35, "Physics"));
        add(new Teacher("0004", "Smith", 39, "History"));
    }};

    private final static List<Student> students = new ArrayList<Student>() {{
        add(new Student("J00000001", "Joe", 14, "4-01"));
        add(new Student("J00000002", "Maria", 13, "4-01"));
        add(new Student("J00000003", "Jenny", 12, "4-02"));
        add(new Student("J00000004", "George", 14, "4-02"));
    }};

    private static Database database = null;

    public static void main(String[] args) {
        createDatabase();
        initialize();
        printAllTeachers();
        printAllStudents();
        removeTeacherNormally();
        removeTeacherUsingBulk();
    }

    private static void createDatabase() {
        CloudantDBManager cloudantDBManager;
        try {
            String path = Objects.requireNonNull(Demo.class.getClassLoader()
                    .getResource("cloudant.yml")).getPath();
            cloudantDBManager = new CloudantDBManager(path);
            List<String> databases = cloudantDBManager.getDatabases();
            if (databases.contains(DATABASE_NAME)) {
                cloudantDBManager.deleteDatabase(DATABASE_NAME);
            }
            cloudantDBManager.createPartitionedDatabase(DATABASE_NAME);
            database = cloudantDBManager.getDatabase(DATABASE_NAME, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void initialize() {
        database.bulk(new ArrayList<>(teachers));
        database.bulk(new ArrayList<>(students));
    }

    private static void removeTeacherNormally() {
        Optional<Teacher> optionalTeacher = findTeacherByName("Mark");
        if (optionalTeacher.isPresent()) {
            Teacher teacher = optionalTeacher.get();
            System.out.println(teacher);
            teacher.setAge(31);
            String rev = updateTeacher(teacher);
            System.out.println("teacher '[Mark]' rev -> " + rev);
            teacher.setRev(rev);
            rev = deleteTeacher(teacher);
            System.out.println("teacher '[Mark]' rev -> " + rev);
        } else {
            System.out.println("teacher not found!");
        }
    }

    private static void removeTeacherUsingBulk() {
        Optional<Teacher> optionalTeacher = findTeacherByName("John");
        if (optionalTeacher.isPresent()) {
            Teacher teacher = optionalTeacher.get();
            System.out.println(teacher);
            teacher.setAge(33);
            String rev = updateTeacher(teacher);
            System.out.println("teacher '[John]' rev -> " + rev);
            teacher.setRev(rev);
            bulkRemoveTeacher(teacher);
        } else {
            System.out.println("teacher not found!");
        }
    }

    private static String updateTeacher(Teacher teacher) {
        return database.update(teacher).getRev();
    }

    private static String deleteTeacher(Teacher teacher) {
        return database.remove(teacher).getRev();
    }

    private static void bulkRemoveTeacher(Teacher teacher) {
        teacher.setDeleted(true);
        database.bulk(Collections.singletonList(teacher));
    }

    private static void printAllTeachers() {
        QueryResult<Teacher> result = database.query(Teacher.TEACHER_PARTITION_NAME,
                new QueryBuilder(gt("_id", "0")).build(), Teacher.class);
        System.out.println(result.getDocs());
    }

    private static void printAllStudents() {
        QueryResult<Student> result = database.query(Student.STUDENT_PARTITION_NAME,
                new QueryBuilder(gt("_id", "0")).build(), Student.class);
        System.out.println(result.getDocs());
    }

    private static Optional<Teacher> findTeacherByName(String name) {
        QueryResult<Teacher> result = database.query(Teacher.TEACHER_PARTITION_NAME,
                new QueryBuilder(eq("name", name)).build(), Teacher.class);
        if (result.getDocs().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(result.getDocs().get(0));
    }

}
