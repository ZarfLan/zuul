package com.jiuzhe.app.gateway.filter;

import com.jiuzhe.app.gateway.constant.CommonConstant;
import com.jiuzhe.app.gateway.dto.ResponseBase;
import com.jiuzhe.app.gateway.utils.JsonUtil;
import com.jiuzhe.app.gateway.utils.MD5Util;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public class AuthFilter extends ZuulFilter {
	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	private final Log logger = LogFactory.getLog(AuthFilter.class);

	private final String TOKEN = "token";
	private final String SIGN = "sign";

	private List<String> ignoreUris;

	public AuthFilter(List<String> ignoreUris) {
		this.ignoreUris = ignoreUris;
	}

	@Override
	public String filterType() {
		// 可以在请求被路由之前调用
		return "pre";
	}

	@Override
	public int filterOrder() {
		// filter执行顺序，通过数字指定 ,优先级为0，数字越大，优先级越低
		return 0;
	}

	@Override
	public boolean shouldFilter() {
		RequestContext ctx = RequestContext.getCurrentContext();
		HttpServletRequest request = ctx.getRequest();

		String uri = request.getRequestURI();
		if (uri.indexOf("assets") != -1) {
			return false;
		}
		if (ignoreUris.contains(uri)) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public Object run() {
		RequestContext ctx = RequestContext.getCurrentContext();
		HttpServletRequest request = ctx.getRequest();

		String uri = request.getRequestURI();
		String uuid = request.getParameter(TOKEN);
		String sign = request.getParameter(SIGN);

		String token = stringRedisTemplate.opsForValue().get("plantform:atuh:token:usr-id:" + uuid);
		if (null == token || token.isEmpty()) {
			ResponseBase<String> res = new ResponseBase<>();
			res.setStatus(CommonConstant.TOKEN_INVALID);
			res.setMessage(CommonConstant.TOKEN_INVALID_MSG);

			// 用户没有登陆/token过期
			ctx.setSendZuulResponse(false);
			ctx.setResponseStatusCode(200);
			ctx.setResponseBody(JsonUtil.toJson(res));
			ctx.set("isSuccess", false);
			return null;
		}

		// 去掉token前后到双引号
		token = token.replace("\"", "");
		// 生成MD5密文，加密字符串为"uri+?token=***"
		String codingContent = uri + "?token=" + token;
		String md5 = MD5Util.md5(codingContent);
		// logger.info("uri:" + codingContent);
		// logger.info("md5:" + md5);
		// logger.info("sign:" + sign);

		if (null != token && !md5.equals(sign)) {
			ResponseBase<String> res = new ResponseBase<>();
			res.setStatus(CommonConstant.TAKEN_CHANG);
			res.setMessage(CommonConstant.TAKEN_CHANG_MSG);
			ctx.setSendZuulResponse(false);
			ctx.setResponseStatusCode(200);
			ctx.setResponseBody(JsonUtil.toJson(res));
			ctx.set("isSuccess", false);
			return null;
		}

		if (null == token || token.isEmpty() || !md5.equals(sign)) {
			ResponseBase<String> res = new ResponseBase<>();
			res.setStatus(CommonConstant.SIGN_INVALID);
			res.setMessage(CommonConstant.SIGN_INVALID_MSG);

			// 用户没有登陆/token过期
			ctx.setSendZuulResponse(false);
			ctx.setResponseStatusCode(200);
			ctx.setResponseBody(JsonUtil.toJson(res));
			ctx.set("isSuccess", false);
			return null;
		}

		// 对请求进行路由
		ctx.setSendZuulResponse(true);
		ctx.setResponseStatusCode(200);
		ctx.set("isSuccess", true);
		return null;
	}

}
