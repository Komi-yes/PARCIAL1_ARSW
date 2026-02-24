package com.example.Lab_0_PPCA.Controllers;

import com.example.Lab_0_PPCA.Services.APIService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping( path = "/api" )
public class APIController {
    APIService apiService;

    public APIController(APIService service){
        apiService = service;
    }

    @GetMapping("/info")
    public ResponseEntity<String> getInfo(){
        return(ResponseEntity.ok(apiService.getInfo()));
    }
}
