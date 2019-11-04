package org.bluesky.apps.cloudantdemo.document;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bluesky.apps.cloudantdemo.model.Teacher;
import org.bluesky.cloudant.model.BaseDocument;

@Data
@EqualsAndHashCode(of = {"uid"}, callSuper = false)
public class TeacherDocument extends BaseDocument {

    public final static String TEACHER_PARTITION_NAME = "teacher";

    private String uid;
    private String name;
    private int age;
    private String subject;

    public TeacherDocument() {
    }

    public TeacherDocument(Teacher teacher) {
        this(teacher.getUid(), teacher.getName(), teacher.getAge(), teacher.getSubject());
        this._rev = teacher.get_rev();
    }

    private TeacherDocument(String uid, String name, int age, String subject) {
        super(uid, TEACHER_PARTITION_NAME, false, true);
        this.uid = uid;
        this.name = name;
        this.age = age;
        this.subject = subject;
    }

    public TeacherDocument(String uid, String _rev) {
        super(uid, TEACHER_PARTITION_NAME, false, true);
        this._rev = _rev;
    }

    public static Teacher convert(TeacherDocument document) {
        Teacher teacher = new Teacher();
        teacher.setAge(document.age);
        teacher.setName(document.name);
        teacher.setSubject(document.subject);
        teacher.setUid(document.uid);
        teacher.set_rev(document._rev);
        return teacher;
    }

}
