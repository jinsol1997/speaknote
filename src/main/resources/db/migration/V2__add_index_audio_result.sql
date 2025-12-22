-- 페이지 진입 시와 history 조회 시 recorder_id를 기준으로 이전 조회 idx보다 작은 idx(이전 데이터)를 조회하므로
-- (recorder_id, idx) 순으로 정렬된 데이터 필요
CREATE INDEX idx_audio_result_recorder_id_idx
    ON audio_result (recorder_id, idx DESC);