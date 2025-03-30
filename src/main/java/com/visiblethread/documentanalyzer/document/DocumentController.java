package com.visiblethread.documentanalyzer.document;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/document")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/{userEmail}")
    public Document createDocument(@PathVariable String userEmail, @RequestBody DocumentRequest request) {
        return documentService.createDocument(request, userEmail);
    }

    @GetMapping("/{id}/word-frequency")
    public DocumentStatisticsResponse getWordFrequency(@PathVariable Long id) {
        return documentService.getWordFrequency(id);
    }

    @GetMapping("/{id}/synonym")
    public List<String> getSynonymsForLongestWord(@PathVariable Long id, @RequestParam(required = false, defaultValue = "1") int numberOfSynonyms) {
        return documentService.getSynonymsForLongestWord(id, numberOfSynonyms);
    }

}