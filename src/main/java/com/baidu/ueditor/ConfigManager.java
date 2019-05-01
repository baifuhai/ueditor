package com.baidu.ueditor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public final class ConfigManager {
	private final String rootPath;
	private final String originalPath;
	private final String contextPath;
	private static final String configFileName = "config.json";
	private String parentPath = null;
	private JSONObject jsonConfig = null;
	private static final String SCRAWL_FILE_NAME = "scrawl";
	private static final String REMOTE_FILE_NAME = "remote";

	private ConfigManager(String rootPath, String contextPath, String uri) throws FileNotFoundException, IOException {
		rootPath = rootPath.replace("\\", "/");
		this.rootPath = rootPath;
		this.contextPath = contextPath;
		if (contextPath.length() > 0) {
			this.originalPath = this.rootPath + uri.substring(contextPath.length());
		} else {
			this.originalPath = this.rootPath + uri;
		}

		//this.initEnv();
		this.initEnv2(uri.substring(contextPath.length()));
	}

	public static ConfigManager getInstance(String rootPath, String contextPath, String uri) {
		try {
			return new ConfigManager(rootPath, contextPath, uri);
		} catch (Exception var4) {
			return null;
		}
	}

	public boolean valid() {
		return this.jsonConfig != null;
	}

	public JSONObject getAllConfig() {
		return this.jsonConfig;
	}

	public Map<String, Object> getConfig(int type) {
		Map<String, Object> conf = new HashMap();
		String savePath = null;
		switch(type) {
			case 1:
				conf.put("isBase64", "false");
				conf.put("maxSize", this.jsonConfig.getLong("imageMaxSize"));
				conf.put("allowFiles", this.getArray("imageAllowFiles"));
				conf.put("fieldName", this.jsonConfig.getString("imageFieldName"));
				savePath = this.jsonConfig.getString("imagePathFormat");
				break;
			case 2:
				conf.put("filename", "scrawl");
				conf.put("maxSize", this.jsonConfig.getLong("scrawlMaxSize"));
				conf.put("fieldName", this.jsonConfig.getString("scrawlFieldName"));
				conf.put("isBase64", "true");
				savePath = this.jsonConfig.getString("scrawlPathFormat");
				break;
			case 3:
				conf.put("maxSize", this.jsonConfig.getLong("videoMaxSize"));
				conf.put("allowFiles", this.getArray("videoAllowFiles"));
				conf.put("fieldName", this.jsonConfig.getString("videoFieldName"));
				savePath = this.jsonConfig.getString("videoPathFormat");
				break;
			case 4:
				conf.put("isBase64", "false");
				conf.put("maxSize", this.jsonConfig.getLong("fileMaxSize"));
				conf.put("allowFiles", this.getArray("fileAllowFiles"));
				conf.put("fieldName", this.jsonConfig.getString("fileFieldName"));
				savePath = this.jsonConfig.getString("filePathFormat");
				break;
			case 5:
				conf.put("filename", "remote");
				conf.put("filter", this.getArray("catcherLocalDomain"));
				conf.put("maxSize", this.jsonConfig.getLong("catcherMaxSize"));
				conf.put("allowFiles", this.getArray("catcherAllowFiles"));
				conf.put("fieldName", this.jsonConfig.getString("catcherFieldName") + "[]");
				savePath = this.jsonConfig.getString("catcherPathFormat");
				break;
			case 6:
				conf.put("allowFiles", this.getArray("fileManagerAllowFiles"));
				conf.put("dir", this.jsonConfig.getString("fileManagerListPath"));
				conf.put("count", this.jsonConfig.getInt("fileManagerListSize"));
				break;
			case 7:
				conf.put("allowFiles", this.getArray("imageManagerAllowFiles"));
				conf.put("dir", this.jsonConfig.getString("imageManagerListPath"));
				conf.put("count", this.jsonConfig.getInt("imageManagerListSize"));
		}

		conf.put("savePath", savePath);
		conf.put("rootPath", this.rootPath);
		return conf;
	}

	private void initEnv() throws FileNotFoundException, IOException {
		File file = new File(this.originalPath);
		if (!file.isAbsolute()) {
			file = new File(file.getAbsolutePath());
		}

		this.parentPath = file.getParent();
		String configContent = this.readFile(this.getConfigPath());

		try {
			JSONObject jsonConfig = new JSONObject(configContent);
			this.jsonConfig = jsonConfig;
		} catch (Exception var4) {
			this.jsonConfig = null;
		}

	}

	private void initEnv2(String s) throws FileNotFoundException, IOException {
		s = s.replaceFirst("[^/\\\\]+$", configFileName).replaceFirst("^[/\\\\]", "");

		InputStream in = this.getClass().getClassLoader().getResourceAsStream(s);
		BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));

		StringBuilder sb = new StringBuilder();
		String line = null;
		while((line = br.readLine()) != null) {
			sb.append(line);
		}
		br.close();

		JSONObject jsonConfig = new JSONObject(filter(sb.toString()));
		this.jsonConfig = jsonConfig;
	}

	private String getConfigPath() {
		return this.parentPath + File.separator + configFileName;
	}

	private String[] getArray(String key) {
		JSONArray jsonArray = this.jsonConfig.getJSONArray(key);
		String[] result = new String[jsonArray.length()];
		int i = 0;

		for(int len = jsonArray.length(); i < len; ++i) {
			result[i] = jsonArray.getString(i);
		}

		return result;
	}

	private String readFile(String path) throws IOException {
		StringBuilder builder = new StringBuilder();

		try {
			InputStreamReader reader = new InputStreamReader(new FileInputStream(path), "UTF-8");
			BufferedReader bfReader = new BufferedReader(reader);
			String tmpContent = null;

			while((tmpContent = bfReader.readLine()) != null) {
				builder.append(tmpContent);
			}

			bfReader.close();
		} catch (UnsupportedEncodingException var6) {
			;
		}

		return this.filter(builder.toString());
	}

	private String filter(String input) {
		return input.replaceAll("/\\*[\\s\\S]*?\\*/", "");
	}
}
