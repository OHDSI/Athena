package com.odysseusinc.athena.api.v1.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "ApiVocabularyController")
@RestController
@RequestMapping("/api/s2s/vocabularies")
public class ApiVocabularyController extends AbstractVocabularyController {
}
