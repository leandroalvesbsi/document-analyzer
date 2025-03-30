package com.visiblethread.documentanalyzer.document;

import com.visiblethread.documentanalyzer.user.User;
import com.visiblethread.documentanalyzer.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.contains;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class DocumentServiceTest {

    public static final String USER_EMAIL = "user@test.com";
    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private S3Client s3Client;

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @InjectMocks
    private DocumentService documentService;

    private final String bucketName = "test-bucket";
    private final String promptTemplate = "Provide {0} synonyms in a simple list for {1}, ensuring they are contextually appropriate for general use.";
    private final List<String> excludedWords = Arrays.asList("the", "me", "you", "i", "of", "and", "a", "we");
    private final String documentContent = "This is a test content for unit test";
    private final String documentTitle = "This is a test title";
    private final String shortestWord = "is";
    private final String longestWord = "content";
    private final int wordCount = 7;

    @BeforeEach
    void setUp() {
        documentService = new DocumentService(
                documentRepository,
                userRepository,
                s3Client,
                chatClientBuilder,
                bucketName,
                promptTemplate,
                excludedWords
        );
    }

    // Testes para createDocument
    @Test
    void createDocument_Success() {
        // Arrange
        DocumentRequest request = new DocumentRequest(documentTitle, documentContent);
        User user = new User();
        user.setEmail(USER_EMAIL);
        when(userRepository.findById(USER_EMAIL)).thenReturn(Optional.of(user));
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(null);
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Document result = documentService.createDocument(request, USER_EMAIL);

        // Assert
        assertThat(result.getName(), is(documentTitle));
        assertThat(result.getUser().getEmail(), is(USER_EMAIL));
        assertThat(result.getS3ObjectKey(), is(not(emptyOrNullString())));
        assertThat(result.getStatistics().getWordCount(), is(wordCount));
        assertThat(result.getStatistics().getShortestWord(), is(shortestWord));
        assertThat(result.getStatistics().getLongestWord(), is(longestWord));
        verify(userRepository).findById(USER_EMAIL);
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        verify(documentRepository).save(any(Document.class));
    }

    @Test
    void createDocument_UserNotFound_ThrowsResponseStatusException() {
        // Arrange
        DocumentRequest request = new DocumentRequest(documentTitle, documentTitle);
        when(userRepository.findById(USER_EMAIL)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> documentService.createDocument(request, USER_EMAIL));
        assertThat(exception.getStatusCode(), is(HttpStatus.NOT_FOUND));
        assertThat(exception.getReason(), is("User not found"));
        verify(userRepository).findById(USER_EMAIL);
        verifyNoInteractions(s3Client, documentRepository);
    }

    // Testes para getSynonymsForLongestWord
    @Test
    void getSynonymsForLongestWord_Success() {
        // Arrange
        Long docId = 1L;
        int numberOfSynonyms = 3;

        Document document = new Document();
        DocumentStatistics stats = new DocumentStatistics();
        stats.setLongestWord(longestWord);
        document.setStatistics(stats);
        when(documentRepository.findById(docId)).thenReturn(Optional.of(document));

        ChatClient chatClient = mock(ChatClient.class);

        when(chatClientBuilder.build()).thenReturn(chatClient);

        ChatClient.ChatClientRequestSpec request = mock(ChatClient.ChatClientRequestSpec.class);
        when(chatClient.prompt(anyString())).thenReturn(request);

        ChatClient.CallResponseSpec callResponseSpec = mock(ChatClient.CallResponseSpec.class);
        when(request.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("content synonyms");

        // Act
        List<String> result = documentService.getSynonymsForLongestWord(docId, numberOfSynonyms);

        // Assert
        assertThat(result, hasSize(1));
        assertThat(result.get(0), is("content synonyms"));
        verify(documentRepository).findById(docId);
        verify(chatClient).prompt(contains("Provide " + numberOfSynonyms + " synonyms in a simple list for " + longestWord + ", ensuring they are contextually appropriate for general use."));
    }

    @Test
    void getSynonymsForLongestWord_DocumentNotFound_ThrowsEntityNotFoundException() {
        // Arrange
        Long docId = 1L;
        when(documentRepository.findById(docId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> documentService.getSynonymsForLongestWord(docId, 3));
        assertThat(exception.getMessage(), is("Document with id 1 not found"));
        verify(documentRepository).findById(docId);
        verifyNoInteractions(chatClientBuilder);
    }

    @Test
    void getSynonymsForLongestWord_NoStatistics_ThrowsIllegalStateException() {
        // Arrange
        Long docId = 1L;
        Document document = new Document();
        when(documentRepository.findById(docId)).thenReturn(Optional.of(document));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> documentService.getSynonymsForLongestWord(docId, 3));
        assertThat(exception.getMessage(), is("No statistics available for document with id 1"));
        verify(documentRepository).findById(docId);
        verifyNoInteractions(chatClientBuilder);
    }

    // Testes para getWordFrequency
    @Test
    void getWordFrequency_Success() {
        // Arrange
        Long docId = 1L;
        Document document = new Document();
        DocumentStatistics stats = new DocumentStatistics();
        stats.setWordCount(documentContent.length());
        stats.setShortestWord(shortestWord);
        stats.setLongestWord(longestWord);
        List<WordFrequency> frequencies = Arrays.asList(
            WordFrequency.builder().word(shortestWord).frequency(1).documentStatistics(stats).build(),
            WordFrequency.builder().word(longestWord).frequency(1).documentStatistics(stats).build()
        );
        stats.setWordFrequencies(frequencies);
        document.setStatistics(stats);
        
        when(documentRepository.findById(docId)).thenReturn(Optional.of(document));

        // Act
        DocumentStatisticsResponse result = documentService.getWordFrequency(docId);

        // Assert
        assertThat(result.wordCount(), is(documentContent.length()));
        assertThat(result.shortestWord(), is(shortestWord));
        assertThat(result.longestWord(), is(longestWord));
        assertThat(result.wordFrequency(), hasEntry(shortestWord, 1));
        assertThat(result.wordFrequency(), hasEntry(longestWord, 1));
        verify(documentRepository).findById(docId);
    }

    @Test
    void getWordFrequency_DocumentNotFound_ThrowsEntityNotFoundException() {
        // Arrange
        Long docId = 1L;
        when(documentRepository.findById(docId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> documentService.getWordFrequency(docId));
        assertThat(exception.getMessage(), is("Document with id 1 not found"));
        verify(documentRepository).findById(docId);
    }

    @Test
    void getWordFrequency_NoStatistics_ThrowsIllegalStateException() {
        // Arrange
        Long docId = 1L;
        Document document = new Document();
        when(documentRepository.findById(docId)).thenReturn(Optional.of(document));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> documentService.getWordFrequency(docId));
        assertThat(exception.getMessage(), is("No statistics available for document with id 1"));
        verify(documentRepository).findById(docId);
    }

}