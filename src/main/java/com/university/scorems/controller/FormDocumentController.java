package com.university.scorems.controller;

import com.university.scorems.service.FormRenderingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
public class FormDocumentController {

    private static final MediaType HTML_UTF8 = new MediaType("text", "html", StandardCharsets.UTF_8);

    private final FormRenderingService formRenderingService;

    @GetMapping("/forms/bm03/{courseId}")
    public ResponseEntity<String> generateBm03(@PathVariable Long courseId,
                                               @RequestParam Long teacherId,
                                               @RequestParam(required = false) Long classId) {
        return ResponseEntity.ok()
                .contentType(HTML_UTF8)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(formRenderingService.renderBm03(teacherId, courseId, classId));
    }
}
