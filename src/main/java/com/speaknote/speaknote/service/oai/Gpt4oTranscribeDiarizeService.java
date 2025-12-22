package com.speaknote.speaknote.service.oai;

import com.speaknote.speaknote.dto.TranscribeDiarizeResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;


/**
 * OpenAI gpt-4o-transcribe-diarize 모델을 이용하여
 * 음성 파일을 텍스트로 변환(STT)하고 화자 분리를 수행하는 서비스
 *
 * <p>주요 역할:</p>
 * <ul>
 *   <li>음성 파일을 OpenAI Audio Transcriptions API로 전송</li>
 *   <li>화자 분리 결과를 diarized_json 형식으로 수신</li>
 *   <li>외부 API 호출 실패 시 예외를 내부에서 처리</li>
 * </ul>
 */
@Slf4j
@Service
public class Gpt4oTranscribeDiarizeService extends AbstractOpenAiService {

    public Gpt4oTranscribeDiarizeService(@Value("${openai.api-key}") String apiKey) {
        super(apiKey);
    }


    /**
     * 음성 파일을 gpt-4o-transcribe-diarize 모델에 전달하여
     * STT 및 화자 분리 결과(diarized_json)를 생성
     *
     * <p>요청 방식:</p>
     * <ul>
     *   <li>multipart/form-data 형식으로 파일 업로드</li>
     *   <li>모델: gpt-4o-transcribe-diarize</li>
     *   <li>response_format: diarized_json</li>
     *   <li>언어: 한국어(ko)</li>
     * </ul>
     *
     * <p>동작 방식:</p>
     * <ul>
     *   <li>WebClient를 이용한 동기(blocking) 호출</li>
     *   <li>최대 300초 동안 응답 대기</li>
     * </ul>
     *
     * <p>실패 처리:</p>
     * <ul>
     *   <li>OpenAI API 오류(WebClientResponseException) 발생 시 success=false 반환</li>
     *   <li>기타 예외 발생 시 success=false 반환</li>
     *   <li>예외는 상위 계층으로 전파하지 않음</li>
     * </ul>
     *
     * @param file 업로드된 음성 파일 (wav)
     * @return STT 및 화자 분리 결과
     */
    public TranscribeDiarizeResult transcribeDiarize(MultipartFile file) {

        try {
            // multipart/form-data
            MultipartBodyBuilder body = new  MultipartBodyBuilder();
            body.part("model", "gpt-4o-transcribe-diarize");
            body.part("response_format", "diarized_json");

            body.part("file", file.getResource())
                    .filename("audio.wav")
                    .contentType(MediaType.parseMediaType("audio/wav"));

            body.part("chunking_strategy", "auto");
            body.part("language", "ko");

            String diarizedJson = webClient.post()
                    .uri("/audio/transcriptions")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(body.build()))
                    .retrieve() // 응답 200 or 4~500 구분해서 WebClientResponseException 던지고 응답 body를 넘김
                    .bodyToMono(String.class)   // 응답 올 바디를 string 형태로 받음
                    .timeout(Duration.ofSeconds(300))
                    .block();   // 동기식으로 처리

            log.info("gpt-4o-transcribe-diarize 모델 api 호출 성공");

            return TranscribeDiarizeResult.builder()
                    .success(true)
                    .diarizedJson(diarizedJson)
                    .build();

        }
        catch (WebClientResponseException e) {
            // api 요청 에러
//            System.out.println("=== OpenAI ERROR BODY ===");
//            System.out.println(e.getResponseBodyAsString());
//            System.out.println("==========================");
            
            log.error("gpt-4o-transcribe-diarize 모델 api 요청 에러 : {}", e.getResponseBodyAsString());

            return TranscribeDiarizeResult.builder()
                    .success(false)
                    .build();

        }
        catch (Exception e) {

            log.error("gpt-4o-transcribe-diarize 모델 api 호출 중 오류 발생", e);

            return TranscribeDiarizeResult.builder()
                    .success(false)
                    .build();

        }


    }

}
