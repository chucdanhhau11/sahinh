package com.sahinh.io.vn.controller.SH.A0.SHA0_0200;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Controller — chỉ mapping URL + trả template. Không nghiệp vụ, không query DB. */
@Controller
public class SHA0_0200_pg {
    @GetMapping("/SH/SHA0_0200")
    public String page() {
        return "SH/A0/SHA0_0200/SHA0_0200";
    }
}
