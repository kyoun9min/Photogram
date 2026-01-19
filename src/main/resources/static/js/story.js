/**
	2. 스토리 페이지
	(1) 스토리 로드하기
	(2) 스토리 스크롤 페이징하기
	(3) 좋아요, 안좋아요
	(4) 댓글쓰기
	(5) 댓글삭제
 */

// (1) 스토리 로드하기
let page = 0;
function storyLoad() {

	$.ajax({
		url: `/api/image?page=${page}`,
		dataType: "json"
	}).done(res=> {
		console.log(res);
		res.data.content.forEach((image)=> {
			let storyItem = getStoryItem(image);
			$("#storyList").append(storyItem);
		})
	}).fail(error=> {
		console.log("오류", error);
	});
}

storyLoad();

function getStoryItem(image) {
	let item = `<div class="story-list__item">
\t<div class="sl__item__header">
\t\t<div>
\t\t\t<img class="profile-image" src="/upload/${image.user.profileImageUrl}"
\t\t\t\tonerror="this.src='/images/person.jpeg'" />
\t\t</div>
\t\t<div>${image.user.username}</div>
\t</div>

\t<div class="sl__item__img">
\t\t<img src="/upload/${image.postImageUrl}" />
\t</div>

\t<div class="sl__item__contents">
\t\t<div class="sl__item__contents__icon">

\t\t\t<button>`;

	if (image.likeState) {
		item += `<i class="fas fa-heart active" id="storyLikeIcon-${image.id}" onclick="toggleLike(${image.id})"></i>`;
	}
	else {
		item += `<i class="far fa-heart" id="storyLikeIcon-${image.id}" onclick="toggleLike(${image.id})"></i>`;
	}

	item += `
\t\t\t</button>
\t\t</div>

\t\t<span class="like"><b id="storyLikeCount-${image.id}">${image.likeCount}</b>likes</span>

\t\t<div class="sl__item__contents__content">
\t\t\t<p>${image.caption}</p>
\t\t</div>

\t\t<div id="storyCommentList-${image.id}">`;

	image.comments.forEach((comment)=>{
		item += `\t\t\t<div class="sl__item__contents__comment" id="storyCommentItem-${comment.id}">
\t\t\t\t<p>
\t\t\t\t\t<b>${comment.user.username} :</b> ${comment.content}
\t\t\t\t</p>

\t\t\t\t<button>
\t\t\t\t\t<i class="fas fa-times"></i>
\t\t\t\t</button>
\t\t\t</div>`;
	});

	item += `

\t\t</div>

\t\t<div class="sl__item__input">
\t\t\t<input type="text" placeholder="댓글 달기..." id="storyCommentInput-${image.id}" />
\t\t\t<button type="button" onClick="addComment(${image.id})">게시</button>
\t\t</div>

\t</div>
</div>`;
	return item;
}

// (2) 스토리 스크롤 페이징하기
$(window).scroll(() => {
	// console.log("윈도우 scrollTop", $(window).scrollTop());
	// console.log("문서의 높이", $(document).height());
	// console.log("윈도우 높이", $(window).height());

	let checkNum = $(window).scrollTop() - ($(document).height() - $(window).height());
	// console.log(checkNum);

	if (-1 < checkNum && checkNum < 1) {
		page++;
		storyLoad();
	}
});


// (3) 좋아요, 안좋아요
function toggleLike(imageId) {
	let likeIcon = $(`#storyLikeIcon-${imageId}`);
	if (likeIcon.hasClass("far")) { // far : 빈 테두리 하트 -> 좋아요 하겠다.

		$.ajax({
			type: "post",
			url: `/api/image/${imageId}/likes`,
			dataType: "json"
		}).done(res=>{

			// let likeCountStr = $(`#storyLikeCount-${imageId}`).text();
			// let likeCount = Number(likeCountStr) + 1;
			// $(`#storyLikeCount-${imageId}`).text(likeCount);

			const $count = $(`#storyLikeCount-${imageId}`);
			$count.text(+$count.text() + 1);

			likeIcon.addClass("fas");
			likeIcon.addClass("active");
			likeIcon.removeClass("far");
		}).fail(error=>{
			console.log("오류", error);
		});

	} else { // fas : 꽉 찬 하트 -> 좋아요 취소하겠다.

		$.ajax({
			type: "delete",
			url: `/api/image/${imageId}/likes`,
			dataType: "json"
		}).done(res=>{

			// let likeCountStr = $(`#storyLikeCount-${imageId}`).text();
			// let likeCount = Number(likeCountStr) - 1;
			// $(`#storyLikeCount-${imageId}`).text(likeCount);

			const $count = $(`#storyLikeCount-${imageId}`);
			$count.text(+$count.text() - 1);

			likeIcon.removeClass("fas");
			likeIcon.removeClass("active");
			likeIcon.addClass("far");
		}).fail(error=>{
			console.log("오류", error);
		});

	}
}

// (4) 댓글쓰기
function addComment(imageId) {

	let commentInput = $(`#storyCommentInput-${imageId}`);
	let commentList = $(`#storyCommentList-${imageId}`);

	let data = {
		imageId: imageId,
		content: commentInput.val()
	}

	if (data.content === "") {
		alert("댓글을 작성해주세요!");
		return;
	}

	$.ajax({
		type: "post",
		url: "/api/comment",
		data: JSON.stringify(data),
		contentType: "application/json; charset=utf-8",
		dataType: "json"
	}).done(res=>{
		// console.log("성공", res);

		let comment = res.data;

		let content = `
			  <div class="sl__item__contents__comment" id="storyCommentItem-${comment.id}"> 
			    <p>
			      <b>${comment.user.username} :</b>
			      ${comment.content}
			    </p>
			    <button><i class="fas fa-times"></i></button>
			  </div>
		`;
		commentList.prepend(content);
	}).fail(error=>{
		console.log("오류", error);
	});


	commentInput.val(""); // 인풋 필드 비우기
}

// (5) 댓글 삭제
function deleteComment() {

}







