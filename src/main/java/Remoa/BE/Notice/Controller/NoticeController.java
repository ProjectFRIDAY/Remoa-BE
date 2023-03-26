package Remoa.BE.Notice.Controller;

import Remoa.BE.Notice.Dto.Req.ReqNoticeDto;
import Remoa.BE.Notice.Dto.Res.ResNoticeDto;
import Remoa.BE.Notice.Service.NoticeService;
import Remoa.BE.Notice.domain.Notice;
import Remoa.BE.exception.CustomMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static Remoa.BE.exception.CustomBody.errorResponse;
import static Remoa.BE.exception.CustomBody.successResponse;

@RestController
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;


    @PostMapping("/notice")
    public ResponseEntity<Object> postNotice(@Validated @RequestBody ReqNoticeDto reqNoticeDto){
        noticeService.registerNotice(reqNoticeDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/notice")
    public ResponseEntity<Object> getNotice(@RequestParam(required = false, defaultValue = "1", name = "page") int pageNumber){
        pageNumber -= 1;
        if (pageNumber < 0) {
            return errorResponse(CustomMessage.PAGE_NUM_OVER);
        }
        Page<Notice> notices =noticeService.getNotice(pageNumber);

        if ((notices.getContent().isEmpty()) && (notices.getTotalElements() > 0)) {
            return errorResponse(CustomMessage.PAGE_NUM_OVER);
        }
        Map<String, Object> responseData = new HashMap<>();

        List<ResNoticeDto> result = new ArrayList<>();

        for(Notice notice:notices) {
            ResNoticeDto resNoticeDto = ResNoticeDto.builder()
                    .noticeId(notice.getNoticeId())
                    .title(notice.getTitle())
                    .postingTime(notice.getPostingTime().toLocalDate())
                    .view(10)
                    .build();
            result.add(resNoticeDto);
        }

        responseData.put("notices", result); //조회한 레퍼런스들
        responseData.put("totalPages", notices.getTotalPages()); //전체 페이지의 수
        responseData.put("totalOfAllNotices", notices.getTotalElements()); //모든 레퍼런스의 수
        responseData.put("totalOfPageElements", notices.getNumberOfElements()); //현 페이지의 레퍼런스 수

        return successResponse(CustomMessage.OK, responseData);
    }

}
