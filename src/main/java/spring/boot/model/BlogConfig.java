package spring.boot.model;

public interface BlogConfig {
	public static final String RESOURCE_NAME="blog";

	public static final int STATE_SELECT=0;
	public static final int STATE_INPUT=1;
	public static final int STATE_UPDATE=2;
	public static final int STATE_CONFIRM=3;
	public static final int STATE_RESULT=4;

	public static final String MODE_PAGE_BACK="pageback";
	public static final String MODE_REGIST="regist";
	public static final String MODE_UPDATE="update";
	public static final String MODE_DELETE="delete";
	public static final String MODE_CLEAR="clear";
	
	public static final String SESSION_LOGIN_INFO="loginInfo";
}
