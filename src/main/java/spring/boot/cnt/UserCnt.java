package spring.boot.cnt;

import java.util.Objects;
import java.util.ResourceBundle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import spring.boot.model.BlogConfig;
import spring.boot.model.mail.Mail;
import spring.boot.model.mail.SendMailWithVelocity;
import spring.boot.model.user.LoginInfo;
import spring.boot.model.user.LoginInfoKey;
import spring.boot.service.LoginInfoService;
import spring.boot.service.repository.LoginInfoRepository;

@Controller
@RequestMapping("/user") //親　GetMappingでその続き (@RM+@GM /user/input)
/***ユーザ管理*/
public class UserCnt {
	@Value("${user.title}")
	private String[] title;
	@Value("${user.check.item}")
	private String[] userCheckItem;

	@Autowired
	private LoginInfoService service;
	@Autowired
	private HttpSession session;
	@Autowired
	private SendMailWithVelocity seVelocity;
	//private SendMail sendMail;
	//	private TestVelocity testVelocity;
	private ResourceBundle resource;
	@Autowired
	private LoginInfoRepository repository;

	//コンストラクタ
	public UserCnt() {
		resource = ResourceBundle.getBundle(BlogConfig.RESOURCE_NAME);
	}

	@GetMapping("")
	public String top() {
		//セッション情報がない場合はログイン画面へ
		if (Objects.isNull(session.getAttribute(BlogConfig.SESSION_LOGIN_INFO))) {
			return "redirect:/";
		}
		return "/user/user_top";
	}

	//ユーザ登録情報　入力の検査
	@GetMapping("/input")
	public String input(Model model) {
		model.addAttribute(new LoginInfo());
		model.addAttribute("title", title[BlogConfig.STATE_INPUT]);
		return "user/user_regist_input";
	}

	@GetMapping(value = "/regist")
	public String regist(@Valid LoginInfo loginInfo, BindingResult result, Model model) {
		//バリテーション　入力検査
		if (result.hasErrors()) {
			model.addAttribute("title", title[BlogConfig.STATE_INPUT]);
			return "user/user_regist_input";
		}
		//ユーザID重複していないか
		if (service.isRegistUserId(loginInfo.getUserId())) {//UserIDがtrueなら
			model.addAttribute("checkMsg", String.format(resource.getString("user.check.item.err"), userCheckItem[0]));//registinput.htmlの{checkMsg}にデータを渡す
			model.addAttribute("title", title[BlogConfig.STATE_INPUT]);
			return "/user/user_regist_input";//trueだと入力画面に戻される
		}
		//メール重複
		if (service.isRegistUserEmail(loginInfo.getEmail())) {
			model.addAttribute("checkMsg", String.format(resource.getString("user.check.item.err"), userCheckItem[1]));
			model.addAttribute("title", title[BlogConfig.STATE_INPUT]);
			return "user/user_regist_input";
		}
		//登録
		String resultMsg = resource.getString("user.regist.err");
		try {
			//			service.save(loginInfo);//ここをコメントにすると登録されない
			service.blogUserRegist(loginInfo);
			//seVelocity.userRegistMail(loginInfo);
			resultMsg = resource.getString("user.regist");
		} catch (Exception e) {
			e.printStackTrace();
		}
		model.addAttribute("title", title[BlogConfig.STATE_RESULT]);
		model.addAttribute("resultMsg", resultMsg);
		return "user/user_result";
	}

	@GetMapping("/cancel")
	public String cancel(Model model) {
		if (Objects.isNull(session.getAttribute(BlogConfig.SESSION_LOGIN_INFO))) {
			return "redirect:/";
		}
		LoginInfoKey sessionUser = (LoginInfoKey) session.getAttribute(BlogConfig.SESSION_LOGIN_INFO);
		String resultMsg = resource.getString("user.cancel.err");
		try {
			seVelocity.userCancelMail(sessionUser);
			service.blogUserCancel(sessionUser);//DBから削除
			resultMsg = resource.getString("user.cancel");
		} catch (Exception e) {
		}
		//セッション破棄
		session.invalidate();
		model.addAttribute("title", title[BlogConfig.STATE_RESULT]);
		model.addAttribute("resultMsg", resultMsg);
		return "/user/user_result";
	}

	@GetMapping("/testMail")
	public void testMail() {
		var mail = new Mail(
				new String[] { "test@127.0.0.1" },
				resource.getString("mail.webmaster"),
				new String[] { resource.getString("mail.webmaster") },
				null,
				resource.getString("mail.regist.subject"),
				resource.getString("mail.regist.text"));
		//sendMail.send(mail);
		//		testVelocity.send(mail);
	}

	@GetMapping("/pass/update")
	public String passup_show(@RequestParam(name = "pass", required = false) String pass,
			@RequestParam(name = "check", required = false) String check,
			@RequestParam(name = "result", required = false) String result,
			Model model) {
		LoginInfoKey sessionUser = (LoginInfoKey) session.getAttribute(BlogConfig.SESSION_LOGIN_INFO);
		String user = sessionUser.getUserId();
		model.addAttribute("err","");
		if (Objects.isNull(session.getAttribute(BlogConfig.SESSION_LOGIN_INFO))) {
			return "redirect:/";
		}
		if (Objects.equals(pass, check)) {
			if (!Objects.isNull(pass) && !Objects.isNull(check) && !Objects.equals(pass, "") && !Objects.equals(check, "")) {
				repository.changePass(pass, user);
				return "/user/user_result";
			}else {
				model.addAttribute("err","入力項目が空欄です");
			}
		}else {
			model.addAttribute("err","入力項目が一致していません");
		}
		return "/user/user_passwd_update";
	}

	@GetMapping("/email/update")
	public String emailup_show(@RequestParam(name = "mail", required = false) String mail,
			@RequestParam(name = "check", required = false) String check,
			@RequestParam(name = "result", required = false) String result,
			Model model) {
		LoginInfoKey sessionUser = (LoginInfoKey) session.getAttribute(BlogConfig.SESSION_LOGIN_INFO);
		String user = sessionUser.getUserId();
		model.addAttribute("err","");
		if (Objects.isNull(session.getAttribute(BlogConfig.SESSION_LOGIN_INFO))) {
			return "redirect:/";
		}
		System.out.println(user+mail);
		if (Objects.equals(mail, check)) {
			if (!Objects.isNull(mail) && !Objects.isNull(check) && !Objects.equals(mail, "") && !Objects.equals(check, "")) {
				repository.changeMail(mail, user);
				return "/user/user_result";
			}else {
				model.addAttribute("err","入力項目が空欄です");
			}
		}else {
			model.addAttribute("err","入力項目が一致していません");
		}
		return "/user/user_email_update";
	}

	@GetMapping("/logininfo/update/result")
	public String loginInfoUpdateResult(Model model) {
		return "/user/user_result";
	}
}
