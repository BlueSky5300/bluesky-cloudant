package org.bluesky.cloudant.demo.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bluesky.cloudant.utils.model.BaseDocument;

@Data
@EqualsAndHashCode(of = {"uid"}, callSuper = false)
public class Teacher extends BaseDocument {

    public final static String TEACHER_PARTITION_NAME = "teacher";

    private String uid;
    private String name;
    private int age;
    private String subject;

    public Teacher() {
    }

    public Teacher(String uid, String name, int age, String subject) {
        super(uid, TEACHER_PARTITION_NAME, false, true);
        this.uid = uid;
        this.name = name;
        this.age = age;
        this.subject = subject;
    }

}
