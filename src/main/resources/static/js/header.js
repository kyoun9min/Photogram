$(document).ready(function() {
    // 1. 검색창에 키보드 입력이 일어날 때마다 실행
    $("#searchInput").on("keyup", function(e) {
        let name = $(this).val(); // 입력한 값
        let $resultBox = $("#searchResult"); // 결과 보여줄 div

        // 입력값이 없으면 결과창 비우고 숨김
        if(name === "") {
            $resultBox.empty().hide();
            return;
        }

        // 2. 백엔드 API 호출 (Querydsl + DTO 기반)
        $.ajax({
            type: "get",
            url: `/api/user/search?name=${encodeURIComponent(name)}`, // 한글 검색 깨짐 방지
            dataType: "json"
        }).done(res => {
            $resultBox.empty().show(); // 일단 비우고 보여줌

            // 검색 결과가 없을 때 처리
            if (res.data.length === 0) {
                $resultBox.append(`<div class="search-item" style="justify-content:center;">검색 결과가 없습니다.</div>`);
                return;
            }

            // 검색 결과가 있을 때 처리 (forEach 돌리기)
            res.data.forEach((user) => {
                // S3 URL이 있으면 쓰고, 없으면 기본 이미지
                let profileImage = user.s3ProfileImageUrl ? user.s3ProfileImageUrl : '/images/person.jpeg';

                let item = `
                    <div class="search-item" onclick="location.href='/user/${user.id}'">
                        <div class="search-item-img">
                            <img src="${profileImage}" onerror="this.src='/images/person.jpeg'">
                        </div>
                        <div class="search-item-text">
                            <p class="name">${user.name}</p>
                            <p class="username">${user.username}</p>
                        </div>
                    </div>`;
                $resultBox.append(item);
            });
        }).fail(error => {
            console.log("검색 에러", error);
        });
    });

    // 3. 검색창 바깥 영역 클릭 시 결과창 닫기 (사용자 편의성)
    $(document).on("click", function(e) {
        if(!$(e.target).closest(".search-box").length) {
            $("#searchResult").hide();
        }
    });
});