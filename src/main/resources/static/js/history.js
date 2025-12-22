const PAGE_INIT_LAST_IDX = 4294967295;  // unsigned int 최대값
let lastIdx = PAGE_INIT_LAST_IDX;
let next = false;
let loading = false;

const $historyList = $('#historyList');
const $loadMoreBtn = $('#loadMoreBtn');

function fmtCreated(created) {
    if (!created) return '방금전';
    return String(created).replace('T', ' ');
}

function showLoadMore(next) {
    $loadMoreBtn.css('display', next ? 'inline-block' : 'none');
}

function escape(s) {
    return String(s ?? '').replaceAll('&','&amp;').replaceAll('<','&lt;').replaceAll('>','&gt;');
}


function renderItem(history) {
    const idx = history.idx;
    return `
    <div class="history-item" data-idx="${idx}">
      <div class="history-top">
        <div class="history-created">${fmtCreated(history.created)}</div>
      </div>

      <audio controls preload="none" src="/api/audio/${idx}"></audio>

      <div class="block-title">Summary</div>
      <div class="block-text">${escape(history.summary)}</div>

      <div class="block-title">Dialogue</div>
      <div class="block-text">${escape(history.dialogue)}</div>
    </div>
  `;
}

function append(historyList) {
    if (!historyList || historyList.length === 0) return;

    for (const history of historyList) {
        $historyList.append(renderItem(history));
    }

    lastIdx = historyList[historyList.length - 1].idx;
}

function prependOne(history) {
    if (!history) return;
    $historyList.find('.muted').remove();
    $historyList.prepend(renderItem(history));
}

function loadHistory() {
    if (loading) return;
    loading = true;

    $.ajax({
        url: '/api/history',
        method: 'GET',
        dataType: 'json',
        data: { lastIdx },
        success: function (res) {
            const list = res.history || [];
            next = !!res.next;

            if (lastIdx === PAGE_INIT_LAST_IDX && list.length === 0) {
                $historyList.html(`<div class="muted" style="padding:10px;">히스토리가 없습니다.</div>`);
            } else {
                append(list);
            }

            showLoadMore(next);
        },
        error: function (xhr) {
            console.error(xhr.responseText);

            let message = '에러발생';
            try {
                message = JSON.parse(xhr.responseText).error || message;
            } catch {}

            $historyList.html(`<div class="muted" style="padding:10px;">${message}</div>`);
            showLoadMore(false);
        },
        complete: function () {
            loading = false;
        }
    });
}

// 페이지 접속 시 1회
loadHistory();

// 더보기
$loadMoreBtn.on('click', function () {
    if (!next) return;
    loadHistory();
});

// record.js에서 업로드 성공 시 새 항목 반영
window.addEventListener('history:prepend', (e) => {

    const history = e.detail.history;
    if (!history) return;

    prependOne(history);
});

