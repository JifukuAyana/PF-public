document.addEventListener('DOMContentLoaded', async () => {
  const response = await fetch('/api/inquiries');
  const inquiries = await response.json();
  const inquiryTable = document.getElementById('inquiryTable');
  const paginationContainer = document.createElement('div');
  paginationContainer.id = 'pagination';
  document.querySelector('.container').appendChild(paginationContainer);

  let currentPage = 1;
  const itemsPerPage = 5;
  const totalPages = Math.ceil(inquiries.length / itemsPerPage);

  function renderTable(page) {
    inquiryTable.innerHTML = ''; // 一旦クリア

    const start = (page - 1) * itemsPerPage;
    const end = start + itemsPerPage;
    const paginatedItems = inquiries.slice(start, end);

    paginatedItems.forEach((inquiry) => {
      const categoryName = inquiry.category && inquiry.category.name ? inquiry.category.name : '未設定';
      const row = document.createElement('tr');
      row.innerHTML = `
        <td>${categoryName}</td>
        <td>${inquiry.subject}</td>
        <td>${inquiry.message.slice(0, 10)}...</td>
        <td>${inquiry.status}</td>
        <td><button class="detail-btn" onclick="showDetail(${inquiry.id})">詳細</button></td>
      `;
      inquiryTable.appendChild(row);
    });

    renderPagination();
  }

  function renderPagination() {
    paginationContainer.innerHTML = '';

    for (let i = 1; i <= totalPages; i++) {
      const pageButton = document.createElement('button');
      pageButton.innerText = i;
      pageButton.classList.add('page-btn');
      if (i === currentPage) {
        pageButton.classList.add('active');
      }
      pageButton.addEventListener('click', () => {
        currentPage = i;
        renderTable(currentPage);
      });
      paginationContainer.appendChild(pageButton);
    }
  }

  // お問い合わせ詳細ポップアップを表示
  window.showDetail = async function (id) {
    try {
      const response = await fetch(`/api/inquiries/${id}`);
      if (!response.ok) {
        throw new Error(`サーバーエラー: ${response.status}`);
      }
      const inquiry = await response.json();

      if (!inquiry || !inquiry.message) {
        throw new Error("問い合わせ情報が見つかりません");
      }

      const categoryName = inquiry.category && inquiry.category.name ? inquiry.category.name : '未設定';

      document.getElementById('detailCategory').innerText = `カテゴリ: ${categoryName}`;
      document.getElementById('detailSubject').innerText = `件名: ${inquiry.subject}`;
      document.getElementById('detailText').innerHTML = `本文: ${inquiry.message.replace(/\n/g, '<br>')}`;
      document.getElementById('statusSelect').value = inquiry.status;

      document.getElementById('statusUpdateBtn').setAttribute('data-id', inquiry.id);
      document.getElementById('detailContainer').classList.remove('hidden');
    } catch (error) {
      alert(`詳細情報の取得に失敗しました: ${error.message}`);
    }
  };

  // ポップアップを閉じる
  window.closeDetail = function () {
    document.getElementById('detailContainer').classList.add('hidden');
  };

  // ステータス更新処理
  document.getElementById('statusUpdateBtn').addEventListener('click', async () => {
    const inquiryId = document.getElementById('statusUpdateBtn').getAttribute('data-id');
    const newStatus = document.getElementById('statusSelect').value;

    const response = await fetch(`/api/inquiries/${inquiryId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ status: newStatus })
    });

    if (response.ok) {
      alert('ステータスが更新されました！');
      location.reload();
    } else {
      alert('ステータス更新に失敗しました');
    }
  });

  renderTable(currentPage);
});
