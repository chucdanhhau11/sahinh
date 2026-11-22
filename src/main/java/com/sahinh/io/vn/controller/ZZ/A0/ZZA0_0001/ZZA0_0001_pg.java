package com.sahinh.io.vn.controller.ZZ.A0.ZZA0_0001;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Controller — chỉ mapping URL + trả template. Không nghiệp vụ, không query DB. */
@Controller
public class ZZA0_0001_pg {
    @GetMapping("/")
    public String page0() {
        return "ZZ/A0/ZZA0_0001/ZZA0_0001";
    }
    @GetMapping("/login")
    public String page1() {
        return "ZZ/A0/ZZA0_0001/ZZA0_0001";
    }
    @GetMapping("/ZZ/ZZA0_0001")
    public String page2() {
        return "ZZ/A0/ZZA0_0001/ZZA0_0001";
    }
}
