package org.bluesky.apps.cloudantdemo.model;

import lombok.Data;
import org.bluesky.apps.cloudantdemo.document.TeacherDocument;

@Data
public class Teacher {
    private String uid;
    private String name;
    private int age;
    private String subject;
    private String _rev;

    public static TeacherDocument convert(Teacher teacher) {
        return new TeacherDocument(teacher);
    }

}
