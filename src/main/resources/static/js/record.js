// ESM import – WAV 인코딩 가능하도록 MediaRecorder 커스텀된 외부 라이브러리
import { MediaRecorder, register } from 'https://jspm.dev/extendable-media-recorder';
import { connect } from 'https://jspm.dev/extendable-media-recorder-wav-encoder';


$(async function () {

    // WAV 인코더 등록
    await register(await connect());

    let mediaRecorder = null;
    let mediaStream = null;
    let chunks = [];

    const $startBtn = $('#startBtn');
    const $stopBtn  = $('#stopBtn');
    const $status   = $('#status');
    const $result   = $('#result');


    $startBtn.on('click', async function () {
        try {
            mediaStream = await navigator.mediaDevices.getUserMedia({ audio: true });

            // extendable-media-recorder가 제공하는 MediaRecorder 사용
            mediaRecorder = new MediaRecorder(mediaStream, { mimeType: 'audio/wav' });
            chunks = [];

            // ondataavailable -> 녹음 청크가 생성될 때 마다 실행됨 생성된 청크들을 chunks 배열에 저장
            mediaRecorder.ondataavailable = function (e) {
                if (e.data && e.data.size > 0) {
                    chunks.push(e.data);
                }
            };

            mediaRecorder.onstart = function () {
                $status.text('녹음 중...');
                $startBtn.prop('disabled', true);
                $stopBtn.prop('disabled', false);
            };

            mediaRecorder.onstop = function () {

                // 마이크 브라우저 연결 해제
                if (mediaStream) {
                    mediaStream.getTracks().forEach(t => t.stop());
                    mediaStream = null;
                }

                $status.text('녹음 종료, 서버로 전송 중...');

                // 여기서 만들어지는 blob이 진짜 WAV 파일
                const blob = new Blob(chunks, { type: 'audio/wav' });

                const formData = new FormData();
                formData.append('file', blob);


                $.ajax({
                    url: '/api/audio/process',
                    type: 'POST',
                    data: formData,
                    processData: false,      // jquery가 formData를 문자열로 변경하지 않도록 하는 옵션
                    contentType: false,      // formData의 content-Type 헤더와 바운더리를 jquery가 수정하지 않도록 하는 옵션
                    success: function (data) {
                        $status.text('완료!');
                        if (data && data.text) {
                            $result.text(data.text);
                        } else {
                            $result.text(JSON.stringify(data, null, 2));
                        }
                        $startBtn.prop('disabled', false);
                        $stopBtn.prop('disabled', true);
                    },
                    error: function (xhr, status, err) {
                        console.error(err);
                        $status.text('에러 발생');
                        $result.text(err || xhr.responseText || '알 수 없는 오류');
                        $startBtn.prop('disabled', false);
                        $stopBtn.prop('disabled', true);
                    }
                });
            };

            mediaRecorder.start();
        } catch (err) {
            console.error(err);
            $status.text('마이크 권한 오류: ' + err.message);
        }
    });


    $stopBtn.on('click', function () {
        mediaRecorder.stop();
    });

});
