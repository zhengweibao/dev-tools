package me.zhengweibao.utils.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * ObjectMapper单例工厂方法
 *
 * @author zhengweibao
 */
public class ObjectMapperUtil {

	private static final Object initialObjMonitorLock = new Object();

	private static ObjectMapper instance;

	public static ObjectMapper getInstance() {
		if (instance == null) {
			synchronized (initialObjMonitorLock) {
				if (instance == null) {
					Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();

					// 驼峰属性名转下划线属性名
					builder.propertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
					// 不序列化null值
					builder.serializationInclusion(JsonInclude.Include.NON_NULL);
					// 忽略多余的字段
					builder.failOnUnknownProperties(false);

					instance = builder.build();
				}
			}
		}

		return instance;
	}
}
