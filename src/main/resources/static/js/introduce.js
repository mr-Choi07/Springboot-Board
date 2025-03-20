document.addEventListener("DOMContentLoaded", () => {
    const slideWrap = document.querySelector(".slideWrap");
    const imgSlide = document.querySelector(".imgSlide");

    if (slideWrap && imgSlide) {
        // 복제본 추가
        const clone = imgSlide.cloneNode(true);
        slideWrap.appendChild(clone);

        // 원본, 복제본 위치 지정
        imgSlide.offsetWidth + "px";

        // 클래스 할당
        imgSlide.classList.add("original");
        clone.classList.add("clone");
    } else {
        console.error("'.slideWrap' 또는 '.imgSlide' 요소가 존재하지 않습니다.");
    }
});