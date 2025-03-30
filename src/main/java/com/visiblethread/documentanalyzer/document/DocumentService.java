package com.visiblethread.documentanalyzer.document;

import com.visiblethread.documentanalyzer.user.User;
import com.visiblethread.documentanalyzer.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepo;
    private final S3Client s3Client;
    private final String bucketName;
    private final ChatClient.Builder chatClientBuilder;
    private final String promptTemplate;
    private final List<String> excludedWords;

    public DocumentService(
            DocumentRepository documentRepository,
            UserRepository userRepo,
            S3Client s3Client,
            ChatClient.Builder chatClientBuilder,
            @Value("${aws.s3.bucket}") String bucketName,
            @Value("${app.synonym.prompt}") String promptTemplate,
            @Value("${app.document.excluded-words}") List<String> excludedWordsList) {
        this.documentRepository = documentRepository;
        this.userRepo = userRepo;
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.chatClientBuilder = chatClientBuilder;
        this.promptTemplate = promptTemplate;
        this.excludedWords = excludedWordsList;
    }

    public Document createDocument(DocumentRequest request, String userEmail) {

        User user = userRepo.findById(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Document document = new Document();
        document.setName(request.name());
        document.setUser(user);

        String s3Key = sendObjectToS3(request);

        document.setS3ObjectKey(s3Key);

        DocumentStatistics stats = calculateStatistics(request.text());
        document.setStatistics(stats);
        stats.setDocument(document);

        return documentRepository.save(document);

    }

    public List<String> getSynonymsForLongestWord(Long id, int numberOfSynonyms) {

        DocumentStatisticsResponse documentStatisticsResponse = getWordFrequency(id);

        String prompt = MessageFormat.format(promptTemplate, numberOfSynonyms, documentStatisticsResponse.longestWord());

        String response = this.chatClientBuilder.build().prompt(prompt)
                .call()
                .content();

        return Collections.singletonList(response);

    }

    public DocumentStatisticsResponse getWordFrequency(Long id) {

        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Document with id " + id + " not found"));

        DocumentStatistics statistics = document.getStatistics();
        if (statistics == null) {
            throw new IllegalStateException("No statistics available for document with id " + id);
        }

        Map<String, Integer> wordFrequency = statistics.getWordFrequencies().stream().collect(Collectors.toMap(WordFrequency::getWord, WordFrequency::getFrequency));

        return new DocumentStatisticsResponse(statistics.getWordCount(), statistics.getShortestWord(), statistics.getLongestWord(), wordFrequency);

    }


    private String sendObjectToS3(DocumentRequest request) {
        String s3Key = UUID.randomUUID().toString();
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();
        s3Client.putObject(putObjectRequest, RequestBody.fromString(request.text()));
        return s3Key;
    }

    private DocumentStatistics calculateStatistics(String text) {

        List<String> words = Stream.of(text.split("\\s+"))
                .filter(word -> !excludedWords.contains(word) && !word.trim().isEmpty())
                .toList();

        String shortestWord = "";
        String longestWord = "";

        Map<String, Integer> frequencyMap = new HashMap<>();
        for (String word : words) {
            frequencyMap.merge(word.toLowerCase(), 1, Integer::sum);
            if (word.length() > longestWord.length() || longestWord.isEmpty()) {
                longestWord = word;
            }
            if (word.length() < shortestWord.length() || shortestWord.isEmpty()) {
                shortestWord = word;
            }
        }

        DocumentStatistics stats = new DocumentStatistics();
        stats.setWordCount(words.size());
        stats.setShortestWord(shortestWord);
        stats.setLongestWord(longestWord);

        List<WordFrequency> frequencies = frequencyMap.entrySet().stream()
                .map(entry -> {
                    WordFrequency wf = new WordFrequency();
                    wf.setWord(entry.getKey());
                    wf.setFrequency(entry.getValue());
                    wf.setDocumentStatistics(stats);
                    return wf;
                })
                .collect(Collectors.toList());

        stats.setWordFrequencies(frequencies);

        return stats;

    }


}