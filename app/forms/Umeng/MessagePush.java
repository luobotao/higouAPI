package forms.Umeng;

import java.util.List;

import play.data.validation.Constraints.Required;
import play.data.validation.ValidationError;

public class MessagePush {
	@Required(message = "通知栏提示文字不能为空")
	public String ticker;//必填 通知栏提示文字
	
	@Required(message = "通知标题不能为空")
	public String title;//必填 通知标题
	
	@Required(message = "通知文字描述不能为空")
	public String text;//必填 通知文字描述
	
	@Required(message = "点击操作不能为空")
	public String after_open;//必填 值可以为: "go_app": 打开应用 "go_url": 跳转到URL "go_activity": 打开特定的activity "go_custom": 用户自定义内容。
	
	@Required(message = "消息类型不能为空")
	public String display_type;//必填 消息类型，值可以为: notification-通知，message-消息

	public List<ValidationError> validate() {
		return null;
	}
}
