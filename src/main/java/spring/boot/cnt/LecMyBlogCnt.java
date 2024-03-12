package spring.boot.cnt;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LecMyBlogCnt {

	@GetMapping("/menu")
	public String select(Model model) {
		return "/menu";
	}
}
