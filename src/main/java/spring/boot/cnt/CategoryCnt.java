package spring.boot.cnt;

import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import spring.boot.model.BlogConfig;
import spring.boot.model.Category;
import spring.boot.model.user.LoginInfoKey;
import spring.boot.service.CategoryService;

@Controller
@RequestMapping("/blog")
public class CategoryCnt {
	@Autowired
	private CategoryService categoryService;

	@Value("${blog.category.title}")
	private String categorytitle;

	@Value("${blog.title}")
	private String[] title;

	@Value("#{${blog.io.state}}")
	private Map<String, String> ioState;

	@Autowired
	private HttpSession session;

	@GetMapping("/cate") //これがinput&select どちらかといえば処理の多さ的にinputのイメージが高い idについて　inputで指定しないと０になる
	public String select(Model model) {
		//		if (Objects.isNull(session.getAttribute(BlogConfig.SESSION_LOGIN_INFO))) {//アンチゲスト
		//			return "redirect:/";
		//		}
		if (!isCheckLogin()) {
			return "redirect:/";
		}
		model.addAttribute(new Category());
		//タイトル
		model.addAttribute("categorytitle", categorytitle + "・登録");
		model.addAttribute("blogList", categoryService.findAll());//selectのitem:${blogList}にデータを全部渡します
		//カテゴリ...? そんなものはない！
		//ボタンセット
		model.addAttribute("btnMode", BlogConfig.MODE_REGIST);
		model.addAttribute("btnClear", BlogConfig.MODE_CLEAR);
		model.addAttribute("confirm", false);
		return "/blog/category/category_cnt";
	}

	@GetMapping("/cate/search") //divクリックしたときの処理
	public String search(Category category, @RequestParam("id") Integer id, @RequestParam("btn") String btn,
			Model model) {
		if (Objects.isNull(session.getAttribute(BlogConfig.SESSION_LOGIN_INFO))) {//アンチゲスト
			return "redirect:/";
		}
		//divの一覧
		model.addAttribute("blogList", categoryService.findAll());//selectのitem:${blogList}にデータを全部渡します
		if (Objects.equals("clear", btn)) {
			category.setName("");
			category.setDescription("");
		} else {
			model.addAttribute(categoryService.getReferenceById(id));
		}
		//タイトル
		model.addAttribute("categorytitle", title[BlogConfig.STATE_UPDATE].replace("(jpa)", " カテゴリ "));
		//カテゴリ...? そんなものはない！
		//ボタンセット
		model.addAttribute("btnModeUp", BlogConfig.MODE_UPDATE);
		model.addAttribute("btnModeDel", BlogConfig.MODE_DELETE);
		model.addAttribute("btnMode", BlogConfig.MODE_REGIST);
		model.addAttribute("btnClear", BlogConfig.MODE_CLEAR);
		model.addAttribute("confirm", false);
		return "/blog/category/category_cnt";
	}

	@GetMapping("/cate/confirm") //確認処理
	public String confirm(@Valid Category category, BindingResult result, @RequestParam("btn") String btn,
			Model model) {
		if (Objects.isNull(session.getAttribute(BlogConfig.SESSION_LOGIN_INFO))) {//アンチゲスト
			return "redirect:/";
		}
		model.addAttribute("blogList", categoryService.findAll());//selectのitem:${blogList}にデータを全部渡します
		if (result.hasErrors()) {
			model.addAttribute("confirm", false);
			//カテゴリを送る...なんてことはしません
			model.addAttribute("btnModeUp", BlogConfig.MODE_UPDATE);
			model.addAttribute("btnModeDel", BlogConfig.MODE_DELETE);
			if (Objects.equals(btn, BlogConfig.MODE_REGIST)) {
				model.addAttribute("categorytitle",
						title[BlogConfig.STATE_INPUT].replace("(jpa)", " カテゴリ ") + " 入力エラー");
				//タイトル (INPUTにして)
				model.addAttribute("name", title[BlogConfig.STATE_INPUT]);
				model.addAttribute("btnMode", BlogConfig.MODE_REGIST);
				return "/blog/category/category_cnt";
			}
			model.addAttribute("categorytitle", title[BlogConfig.STATE_UPDATE].replace("(jpa)", " カテゴリ ") + " 入力エラー");
			model.addAttribute("name", title[BlogConfig.STATE_UPDATE]);
			return "/blog/category/category_cnt";
		}
		model.addAttribute("confirm", true);//ここで処理実行ボタンを表示させる
		//ボタンの状態から処理したい内容を取得
		model.addAttribute("resultMsg", ioState.get(btn) + "しますか？");
		//カテゴリー名をセット
		model.addAttribute("categorytitle", title[BlogConfig.STATE_CONFIRM].replace("(jpa)", " カテゴリ "));
		//ボタンセット
		model.addAttribute("btnBack", BlogConfig.MODE_PAGE_BACK);
		model.addAttribute("btnMode", btn);
		return "/blog/category/category_cnt";
	}

	@GetMapping("/cate/result") //required = false, name=
	public String handling(@Valid Category category, BindingResult result, @RequestParam("btn") String btn,
			RedirectAttributes reAttributes, Model model, @Value("${blog.update.msg}") String updateStr,
			@Value("${blog.update.err}") String updateErr) {
		if (Objects.isNull(session.getAttribute(BlogConfig.SESSION_LOGIN_INFO))) {//アンチゲスト
			return "redirect:/";
		}
		if (Objects.equals(btn, BlogConfig.MODE_CLEAR)) {//Clear時のエラーを何とかしたい
			model.addAttribute("confirm", false);
			return "/blog/category/category_cnt";
		}
		model.addAttribute("blogList", categoryService.findAll());
		if (result.hasErrors() || Objects.equals(btn, BlogConfig.MODE_PAGE_BACK)) {
			model.addAttribute("btnClear", BlogConfig.MODE_CLEAR);
			model.addAttribute("categoryList", categoryService.getCategoryNameList());
			model.addAttribute("confirm", false);
			if (category.getId() == 0) {
				model.addAttribute("categorytitle", title[BlogConfig.STATE_INPUT].replace("(jpa)", " カテゴリ "));
				model.addAttribute("btnMode", BlogConfig.MODE_REGIST);
				return "/blog/category/category_cnt";
			}
			model.addAttribute("categorytitle", title[BlogConfig.STATE_UPDATE].replace("(jpa)", " カテゴリ "));
			model.addAttribute("btnModeUp", BlogConfig.MODE_UPDATE);
			model.addAttribute("btnModeDel", BlogConfig.MODE_DELETE);
			return "/blog/category/category_cnt";
		}
		String resultMsg = String.format(updateErr, category.getName(), ioState.get(btn));
		//登録
		try {
			//登録からの更新
			if (Objects.equals(btn, BlogConfig.MODE_REGIST)) {
				category.setId(0);
				//				categoryService.save(category);
				categoryService.regist(category);
				//System.out.println("登録用の処理はコメントアウト中 削除もコメント");
			}
			//更新
			if (Objects.equals(btn, BlogConfig.MODE_UPDATE)) {
				//				blogService.save(blog);
				categoryService.update(category);
			}
			//削除
			if (Objects.equals(btn, BlogConfig.MODE_DELETE)) {
				categoryService.deleteById(category.getId());
			}
			resultMsg = String.format(updateStr, category.getName(), ioState.get(btn));
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
		return "redirect:/blog/cate/resultShow";
	}

	@GetMapping("/cate/resultShow") //リダイレクトするとあら不思議　URLの下部にある情報が消えます
	public String resultShow(@ModelAttribute("modelMap") ModelMap modelMap, Model model) {
		model.addAttribute("resultMsg", modelMap.getAttribute("resultMsg"));
		model.addAttribute("id", -2);
		return select(model);
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
		return false;
	}

	private void setUserIdToTable(String userId) {
		categoryService.setTableName(userId);
	}
}