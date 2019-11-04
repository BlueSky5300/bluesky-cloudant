package org.bluesky.apps.cloudantdemo.controller;

import org.bluesky.apps.cloudantdemo.model.Teacher;
import org.bluesky.apps.cloudantdemo.service.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/v1")
public class ApiController {

    private ApiService apiService;

    @Autowired
    public ApiController(ApiService apiService) {
        this.apiService = apiService;
    }

    @PostMapping("/teacher")
    public ResponseEntity<Teacher> create(@RequestBody Teacher teacher) {
        Teacher result = apiService.save(teacher);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @PutMapping("/teacher")
    public ResponseEntity<Teacher> update(@RequestBody Teacher teacher) {
        Teacher result = apiService.update(teacher);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping("/teacher/{uid}/_rev/{_rev}")
    public ResponseEntity<String> delete(@PathVariable("uid") String uid, @PathVariable("_rev") String _rev) {
        apiService.removeByUid(uid, _rev);
        return new ResponseEntity<>("teacher remove successfully.", HttpStatus.OK);
    }

    @DeleteMapping("/teacher/bulk/{uid}/_rev/{_rev}")
    public ResponseEntity<String> deleteUsingBulk(@PathVariable("uid") String uid, @PathVariable("_rev") String _rev) {
        apiService.removeByUidUsingBulk(uid, _rev);
        return new ResponseEntity<>("teacher remove successfully.", HttpStatus.OK);
    }

    @GetMapping("/teacher/all")
    public ResponseEntity<List<Teacher>> getAllTeachers() {
        List<Teacher> teachers = apiService.getAllTeachers();
        return new ResponseEntity<>(teachers, HttpStatus.OK);
    }

    @GetMapping("/teacher/{uid}")
    public ResponseEntity<Teacher> getTeacherByUid(@PathVariable("uid") String uid) {
        Optional<Teacher> optional = apiService.findByUid(uid);
        return optional.map(teacher -> new ResponseEntity<>(teacher, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
    }

}
