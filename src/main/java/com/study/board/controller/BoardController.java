package com.study.board.controller;

import com.study.board.entity.Board;
import com.study.board.service.BoardService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class BoardController {

    @Autowired
    private BoardService boardService;


    @Configuration
    public class WebConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/**")
                    .addResourceLocations("classpath:/static/");
            registry.addResourceHandler("/files/**")
                    .addResourceLocations("file:src/main/resources/static/files/");
        }
    }
   
    @GetMapping("/home")
    public String home() {
        return "home";
    }

    @GetMapping("/board/write") //localhost:8090/board/write
    public String boardWriteForm() {
        return "boardwrite";
    }

    @PostMapping("/board/writepro")
    public String boardWritePro(Board board, Model model, MultipartFile file) throws Exception {

        boardService.write(board, file);

        model.addAttribute("message", "글 작성이 완료되었습니다.");
        model.addAttribute("searchUrl", "/board/list");  // searchUrl 값을 넘겨줌

        return "message";  // message.html로 리다이렉트
    }

    @PostMapping("/filetest")
    public String addImage2(@RequestParam("Photo") MultipartFile uploadFile,
                            HttpServletRequest request) {
        String fileName = uploadFile.getOriginalFilename();
        String filePath = request.getSession().getServletContext().getRealPath("/");
        try {
            uploadFile.transferTo(new File(filePath + fileName));
            System.out.println("이미지 파일 저장 완료");
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
        }
        return " 파일 저장 완료";
    }

    @GetMapping("/board/list")
    public String boardList(Model model,
                            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                            String searchKeyword) {

        Page<Board> list = null;

        if (searchKeyword == null) {
            list = boardService.boardList(pageable);
        } else {
            list = boardService.boardSearchList(searchKeyword, pageable);
        }

        int nowPage = list.getPageable().getPageNumber() + 1;
        int startPage = Math.max(nowPage - 4, 1);
        int endPage = Math.min(nowPage + 5, list.getTotalPages());

        model.addAttribute("list", list);
        model.addAttribute("nowPage", nowPage);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "boardlist";
    }

    @GetMapping("/board/view") // localhost:8080/board/view?id=1
    public String boardView(Model model, Integer id) {

        model.addAttribute("board", boardService.boardView(id));
        return "boardview";
    }

    @GetMapping("/board/modify/{id}")
    public String boardModify(@PathVariable("id") Integer id,
                              Model model) {

        model.addAttribute("board", boardService.boardView(id));

        return "boardmodify";
    }

    @PostMapping("/board/update/{id}")
    public String boardUpdate(@PathVariable("id") Integer id, Board board, MultipartFile file) throws Exception {

        Board boardTemp = boardService.boardView(id);
        boardTemp.setTitle(board.getTitle());
        boardTemp.setContent(board.getContent());

        boardService.write(boardTemp, file);

        return "redirect:/board/list";

    }

    @GetMapping("/board/view/{id}")
    public String boardView(@PathVariable("id") Integer id, Model model) {
        Board board = boardService.boardView(id);
        if (board != null) {
            model.addAttribute("board", board);
        } else {
            model.addAttribute("message", "해당 게시글을 찾을 수 없습니다.");
            model.addAttribute("searchUrl", "/board/list");
        }
        return "boardview";
    }


    @PostMapping("/board/delete/{id}")
    public String boardDelete(@PathVariable("id") Integer id) {
        boardService.delete(id);
        return "redirect:/board/list";
    }

    @GetMapping("/files/{encodedFilename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String encodedFilename) throws MalformedURLException {

        String filename = URLDecoder.decode(encodedFilename, StandardCharsets.UTF_8);
        Path file = Paths.get("src/main/resources/static/files").resolve(filename);
        File checkFile = new File(file.toUri());

        if (checkFile.exists() && checkFile.isFile()) {
            Resource resource = new UrlResource(file.toUri());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}