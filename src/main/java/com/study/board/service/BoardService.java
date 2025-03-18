package com.study.board.service;

import com.study.board.entity.Board;
import com.study.board.repository.BoardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Optional;
import java.util.UUID;

@Service
public class BoardService {

    @Autowired
    private BoardRepository boardRepository;

    // 파일 저장 및 경로 설정 수정
    public void write(Board board, MultipartFile file) throws Exception {
        if (file != null && !file.isEmpty()) {
        try {
            // 프로젝트 내 저장 경로
            String projectPath = System.getProperty("user.dir") + "/src/main/resources/static/files";
            File directory = new File(projectPath);
            if (!directory.exists()) {
                directory.mkdirs();  // 폴더가 없으면 생성
            }

            // 파일 이름 생성
            UUID uuid = UUID.randomUUID();
            String fileName = uuid + "_" + file.getOriginalFilename();
            File saveFile = new File(projectPath, fileName);

            // 파일 저장
            file.transferTo(saveFile);

            // DB에 저장될 파일 경로 설정
            board.setFilename(fileName);
            board.setFilepath("/files/" + fileName);  // '/files/파일명' 형식으로 저장

            System.out.println("✅ 파일 저장 완료: " + board.getFilepath()); // 디버깅 로그 추가

            } catch (Exception e) {
                System.err.println("❌ 파일 저장 중 오류 발생: " + e.getMessage());
                throw new RuntimeException("파일 저장 실패", e);
            }
        } else {
            System.out.println("⚠ 파일이 비어 있음");
        }

        boardRepository.save(board);
        System.out.println("✅ 게시글 저장 완료: " + board.getId());
    }


    // 게시글 수정
    @Transactional
    public void updateBoard(Integer id, Board board, MultipartFile file) throws Exception {
        Optional<Board> boardOptional = boardRepository.findById(id);

        if (boardOptional.isPresent()) {
            Board boardTemp = boardOptional.get();
            boardTemp.setTitle(board.getTitle());
            boardTemp.setContent(board.getContent());

            if (file != null && !file.isEmpty()) {
                String projectPath = System.getProperty("user.dir") + "/src/main/resources/static/files";
                UUID uuid = UUID.randomUUID();
                String fileName = uuid + "_" + file.getOriginalFilename();

                File saveFile = new File(projectPath, fileName);
                file.transferTo(saveFile);

                boardTemp.setFilename(fileName);
                boardTemp.setFilepath("/files/" + fileName);
            }

            boardRepository.save(boardTemp);
        }
    }

    // 게시글 목록
    public Page<Board> boardList(Pageable pageable) {
        return boardRepository.findAll(pageable);
    }

    // 검색 기능
    public Page<Board> boardSearchList(String searchKeyword, Pageable pageable) {
        return boardRepository.findByTitleContaining(searchKeyword, pageable);
    }

    // 특정 게시글 조회
    public Board boardView(Integer id) {
        return boardRepository.findById(id).orElse(null);
    }

    public void delete(Integer id) {
        boardRepository.deleteById(id);
    }
    
}
