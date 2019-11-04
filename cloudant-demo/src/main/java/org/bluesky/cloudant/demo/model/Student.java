package org.bluesky.cloudant.demo.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bluesky.cloudant.utils.model.BaseDocument;

@Data
@EqualsAndHashCode(of = {"sn"}, callSuper = false)
public class Student extends BaseDocument {

    public final static String STUDENT_PARTITION_NAME = "student";

    private String sn;
    private String name;
    private int age;
    private String className;

    public Student() {
    }

    public Student(String sn, String name, int age, String className) {
        super(sn, STUDENT_PARTITION_NAME, false, true);
        this.sn = sn;
        this.name = name;
        this.age = age;
        this.className = className;
    }

}
