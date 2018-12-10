package com.nowcoder.service;

import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Hello on 2018/8/2.
 */
@Service
public class SensitiveService implements InitializingBean{

	private static final Logger logger = LoggerFactory.getLogger(SensitiveService.class);

	/**
	 * 默认敏感词替换符
	 */
	private static final String DEFAULT_REPLACEMENT = "***";

	private class TrieNode{

		/**
		 * true 关键词的终结 ； false 继续
		 */
		private boolean end = false;

		/**
		 * key下一个字符，value是对应的节点
		 */
		private Map<Character,TrieNode> subNodes = new HashMap<>();

		/**
		 * 向指定位置添加节点树
		 */
		void addSubNode(Character key, TrieNode value){
			subNodes.put(key, value);
		}

		/**
		 * 获取下个节点
		 */
		TrieNode getSubNode(Character key){
			return subNodes.get(key);
		}

		boolean isKeyWordEnd(){
			return end;
		}

		void setKeyWordEnd(boolean end) {
			this.end = end;
		}

		public int getSubNodeCount() {
			return subNodes.size();
		}

	}

	/**
	 * 根节点
	 */
	private TrieNode rootNode = new TrieNode();

	/**
	 * 判断是否是一个符号
	 */
	private boolean isSymbol(char c){
		int ic = (int) c;
		// 0x2E80-0x9FFF 东亚文字范围
		return !CharUtils.isAsciiAlphanumeric(c) && (ic < 0x2E80 || ic > 0x9FFF);
	}


	/**
	 * 过滤敏感词
	 */
	public String filter(String text) {
		if (StringUtils.isBlank(text)) {
			return text;
		}

		String replacement = DEFAULT_REPLACEMENT;
		StringBuilder result = new StringBuilder();

		TrieNode tempNode = rootNode;// 根节点向下试探,直到关键词的终结
		int begin = 0; // 回滚数,上方红色指针
		int position = 0; // 当前比较的位置,下方指针

		// 由position主动与树节点对比,begin是被动参与,当begin走完，判断结束
		while (position < text.length()) {
			char c = text.charAt(position);

			// 空格直接跳过
			if (isSymbol(c)) {
				// 开头就有空格之类的符号,存进来，往后检测
				if(tempNode == rootNode){
					result.append(c);
					++begin;
				}

				++position; // 往后移
				continue;
			}

			tempNode = tempNode.getSubNode(c);// 去树上查

			// 当前位置的匹配结束
			if(tempNode == null){
				// 以begin开始的字符串不存在敏感词
				result.append(text.charAt(begin));
				// 跳到下一个字符开始测试(还原)
				position = begin + 1;
				begin = position;
				// 回到树初始节点
				tempNode = rootNode;
			}else if(tempNode.isKeyWordEnd()){
				// 发现敏感词， 从begin到position的位置用replacement替换掉
				result.append(replacement);
				position = position + 1; // 快速跳过敏感词
				begin = position;
				tempNode = rootNode;
			}else{
				++position;
			}

		}

		// position试探树节点完毕，虽有极大相似，但末尾时小部分不匹配，故begin开始到position的文本依旧正常
		// abcefg abcdefq
		result.append(text.substring(begin));
		return result.toString();
	}

	// abc
	private void addWord(String lineText) {
		TrieNode tempNode = rootNode; // 指针回归根节点

		// 循环每个字节
		for (int i = 0; i < lineText.length(); ++i ) {
			Character c = lineText.charAt(i); // 将敏感词一个个放到节点树下

			// 过滤空格，色 情 = 色情
			if (isSymbol(c)) {
				continue;
			}

			TrieNode node = tempNode.getSubNode(c);

			if (node == null) {// 没初始化
				node = new TrieNode();
				tempNode.addSubNode(c, node);
			}
			// 如果已有该节点就直接进入下一个节点
			tempNode = node;

			if( i == lineText.length() - 1){
				// 关键词结束， 设置结束标志
				tempNode.setKeyWordEnd(true);
			}
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		rootNode = new TrieNode();

		try {
			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("SensitiveWords.txt");
			InputStreamReader read = new InputStreamReader(is);
			BufferedReader bufferedReader = new BufferedReader(read);

			String lineTxt = null;
			while ((lineTxt = bufferedReader.readLine()) != null) {
				addWord(lineTxt.trim());
			}
			read.close();

		} catch (Exception e) {
			logger.error("读取敏感词文件失败" + e.getMessage());
		}

	}

	public static void main(String[] args) {
		SensitiveService s = new SensitiveService();
		s.addWord("色情");
		s.addWord("赌博");
		System.out.println(s.filter(" hi 你好色 情"));
	}
}
