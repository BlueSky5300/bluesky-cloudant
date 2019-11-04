package org.bluesky.apps.cloudantdemo.service;

import com.cloudant.client.api.Database;
import com.cloudant.client.api.query.QueryBuilder;
import com.cloudant.client.api.query.QueryResult;
import org.bluesky.apps.cloudantdemo.document.TeacherDocument;
import org.bluesky.apps.cloudantdemo.model.Teacher;
import org.bluesky.cloudant.model.CloudantDBManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.cloudant.client.api.query.Expression.eq;
import static com.cloudant.client.api.query.Expression.gt;

@Service
public class ApiService {

    private Database database;

    @Autowired
    public ApiService(CloudantDBManager cloudantDBManager) {
        List<String> databases = cloudantDBManager.getDatabases();
        String DATABASE_NAME = "school-info";
        if (databases.contains(DATABASE_NAME)) {
            cloudantDBManager.deleteDatabase(DATABASE_NAME);
        }
        cloudantDBManager.createPartitionedDatabase(DATABASE_NAME);
        database = cloudantDBManager.getDatabase(DATABASE_NAME, false);
    }

    public Teacher save(Teacher teacher) {
        TeacherDocument document = Teacher.convert(teacher);
        String _rev = database.save(document).getRev();
        teacher.set_rev(_rev);
        return teacher;
    }

    public Teacher update(Teacher teacher) {
        TeacherDocument document = Teacher.convert(teacher);
        String _rev = database.update(document).getRev();
        teacher.set_rev(_rev);
        return teacher;
    }

    public Optional<Teacher> findByUid(String uid) {
        QueryResult<TeacherDocument> result = database.query(TeacherDocument.TEACHER_PARTITION_NAME,
                new QueryBuilder(eq("uid", uid)).build(), TeacherDocument.class);
        if (result.getDocs().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(TeacherDocument.convert(result.getDocs().get(0)));
    }

    public List<Teacher> getAllTeachers() {
        QueryResult<TeacherDocument> result = database.query(TeacherDocument.TEACHER_PARTITION_NAME,
                new QueryBuilder(gt("_id", "0")).build(), TeacherDocument.class);
        return result.getDocs().stream().map(TeacherDocument::convert).collect(Collectors.toList());
    }

    public void removeByUid(String uid, String _rev) {
        TeacherDocument document = new TeacherDocument(uid, _rev);
        database.remove(document);
    }

    public void removeByUidUsingBulk(String uid, String _rev) {
        TeacherDocument document = new TeacherDocument(uid, _rev);
        document.setDeleted(true);
        database.bulk(Collections.singletonList(document));
    }

}
