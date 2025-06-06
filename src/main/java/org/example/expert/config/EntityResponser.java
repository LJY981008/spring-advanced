package org.example.expert.config;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

public class EntityResponser {

    public static <T> ResponseEntity<T> responser(T entity, HttpStatusCode status){
        return new ResponseEntity<>(entity, status);
    }

    public static ResponseEntity<Void> voidResponser(HttpStatusCode status){
        return ResponseEntity.status(status).build();
    }
}
