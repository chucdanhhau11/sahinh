package com.sahinh.io.vn.controller.MS.A0.MSA0_0100;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Controller — chỉ mapping URL + trả template. Không nghiệp vụ, không query DB. */
@Controller
public class MSA0_0100_pg {
    @GetMapping("/MS/MSA0_0100")
    public String page() {
        return "MS/A0/MSA0_0100/MSA0_0100";
    }
}
