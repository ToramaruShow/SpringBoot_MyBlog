package spring.boot.cnt;

import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import spring.boot.model.Blog;
import spring.boot.model.BlogConfig;
import spring.boot.model.Category;
import spring.boot.model.user.LoginInfoKey;
import spring.boot.service.BlogService;
import spring.boot.service.CategoryService;

@Controller
public class LecBlogCnt {

	@Autowired
	private BlogService blogService;
	@Autowired
	private CategoryService categoryService;

	@Autowired
	private HttpSession session;

	@Value("${blog.title}")
	private String[] title;

	@Value("#{${blog.io.state}}")
	private Map<String, String> ioState;
	
	@GetMapping("/blog")
	public String show(Model model, Pageable pageable) {
		return select(model, pageable);
	}

	@GetMapping("/blog/select") //ログインしている時だけ使えることができる
	public String select(Model model, Pageable pageable) {
		if (!isCheckLogin()) {
			return "redirect:/";
		}
		model.addAttribute("title", title[BlogConfig.STATE_SELECT]);
		Page<Blog> pageList = blogService.findAll(pageable);
		model.addAttribute("blogList", pageList.getContent());
		model.addAttribute("pages", pageList);
		return "/blog/blog_select";
	}

	@GetMapping("/{userId}")
	public String select(Model model, @PathVariable("userId") String userId) {//余裕があればuseridが存在するか確認する
		model.addAttribute("title", title[BlogConfig.STATE_SELECT]);
		if (Objects.nonNull(userId)) {
			setUserIdToTable(userId);//テーブルに名前をセット
		}
		model.addAttribute("blogList", blogService.findAll());
		return "/blog/blog_select";
	}

	@GetMapping("/blog/search")
	public String search(@RequestParam(value = "id", required = false) Integer id, Model model) {
		if (!isCheckLogin()) {
			return "redirect:/";
		}
		model.addAttribute(blogService.getReferenceById(id));
		//タイトル
		model.addAttribute("title", title[BlogConfig.STATE_UPDATE]);
		//カテゴリ
		model.addAttribute("categoryList", categoryService.getCategoryNameList());
		//ボタンセット
		model.addAttribute("btnModeUp", BlogConfig.MODE_UPDATE);
		model.addAttribute("btnModeDel", BlogConfig.MODE_DELETE);
		return "/blog/blog_input";
	}

	@GetMapping("/blog/input")
	public String input(Model model) {
		if (!isCheckLogin()) {
			return "redirect:/";
		}
		model.addAttribute(new Blog());
		//タイトル
		model.addAttribute("title", title[BlogConfig.STATE_INPUT]);
		//カテゴリ
		model.addAttribute("categoryList", categoryService.getCategoryNameList());
		//ボタンセット
		model.addAttribute("btnMode", BlogConfig.MODE_REGIST);
		return "/blog/blog_input";
	}

	@GetMapping("/blog/confirm")
	public String confirm(@Valid Blog blog, BindingResult result,
			@RequestParam(value = "btn", required = false) String btn, Model model) {
		if (!isCheckLogin()) {
			return "redirect:/";
		}
		if (result.hasErrors()) {
			//カテゴリを送る
			model.addAttribute("categoryList", categoryService.getCategoryNameList());
			if (Objects.equals(btn, BlogConfig.MODE_REGIST)) {
				//タイトル (INPUTにして)
				model.addAttribute("title", title[BlogConfig.STATE_INPUT]);
				model.addAttribute("btnMode", BlogConfig.MODE_REGIST); //ここ忘れてる
				return "/blog/blog_input";
			}
			model.addAttribute("title", title[BlogConfig.STATE_UPDATE]);
			return "/blog/blog_input";
		}
		//ボタンの状態から処理したい内容を取得
		model.addAttribute("confirmMsg", ioState.get(btn));
		//カテゴリー名をセット
		//		blog.setCategory(categoryService.getCategoryNameList()[blog.getCategoryId() - 1]);
		Category category = categoryService.getCategoryNameList().stream().filter(
				item -> item.getId() == blog.getCategoryId()).findFirst().orElse(null);
		blog.setCategory(category.getName());
		model.addAttribute("title", title[BlogConfig.STATE_CONFIRM]);
		//ボタンセット
		model.addAttribute("btnBack", BlogConfig.MODE_PAGE_BACK);
		model.addAttribute("btnMode", btn);
		return "/blog/blog_confirm";
	}

	@GetMapping("/blog/result")
	public String handling(@Valid Blog blog, BindingResult result,
			@RequestParam(value = "btn", required = false) String btn,
			RedirectAttributes reAttributes, Model model, @Value("${blog.update.msg}") String updateStr,
			@Value("${blog.update.err}") String updateErr) {
		if (!isCheckLogin()) {
			return "redirect:/";
		}
		if (result.hasErrors() || Objects.equals(btn, BlogConfig.MODE_PAGE_BACK)) {
			model.addAttribute("categoryList", categoryService.getCategoryNameList());
			if (blog.getId() == 0) {
				model.addAttribute("title", title[BlogConfig.STATE_INPUT]);
				model.addAttribute("btnMode", BlogConfig.MODE_REGIST);
				return "/blog/blog_input";
			}
			model.addAttribute("title", title[BlogConfig.STATE_UPDATE]);
			model.addAttribute("btnModeUp", BlogConfig.MODE_UPDATE);
			model.addAttribute("btnModeDel", BlogConfig.MODE_DELETE);
			return "/blog/blog_input";
		}
		String resultMsg = String.format(updateErr, blog.getTitle(), ioState.get(btn));
		//登録
		try {
			//登録
			if (Objects.equals(btn, BlogConfig.MODE_REGIST)) {
				//				blogService.save(blog);
				blogService.regist(blog);
			}
			//更新
			if (Objects.equals(btn, BlogConfig.MODE_UPDATE)) {
				//				blogService.save(blog);
				blogService.update(blog);
			}
			//削除
			if (Objects.equals(btn, BlogConfig.MODE_DELETE)) {
				//				blogService.deleteById(blog.getId());
				blogService.delete(blog);
			}
			resultMsg = String.format(updateStr, blog.getTitle(), ioState.get(btn));
		} catch (Exception e) {
			System.out.println("handling");
			e.printStackTrace();
		}
		//model.addAttribute("resultMsg", resultMsg);
		//リダイレクト先にデータを送る
		var modelMap = new ModelMap();
		modelMap.addAttribute("resultMsg", resultMsg);
		reAttributes.addFlashAttribute("modelMap", modelMap);
		//return select(model);
		return "redirect:/blog/resultShow";
	}

	@GetMapping("/blog/resultShow")
	public String resultShow(@ModelAttribute("modelMap") ModelMap modelMap, Model model, Pageable pageable) {
		model.addAttribute("resultMsg", modelMap.getAttribute("resultMsg"));
		return select(model, pageable);
	}

	private Boolean isCheckLogin() {
		try {
			LoginInfoKey loginInfoKey = (LoginInfoKey) session.getAttribute(BlogConfig.SESSION_LOGIN_INFO);
			if (Objects.nonNull(loginInfoKey)) {
				setUserIdToTable(loginInfoKey.getUserId());
				return true;
			}
		} catch (Exception e) {

		}
		setUserIdToTable(null);//はじかれたらテーブルの名前をNULLに
		//		System.out.println(blogService.getTableName());
		return false;
	}

	private void setUserIdToTable(String userId) {
		blogService.setTableName(userId);
		categoryService.setTableName(userId);
	}

	@GetMapping("/blog/page")
	public String showWithPage(Model model, @PageableDefault(size = 3) Pageable pageable) {
		Page<Blog> pageList = blogService.findAll(pageable);
		model.addAttribute("title", title[BlogConfig.STATE_SELECT]);
		model.addAttribute("blogList", pageList.getContent());
		model.addAttribute("pages", pageList);
		return "/blog/blog_select";
	}
}
