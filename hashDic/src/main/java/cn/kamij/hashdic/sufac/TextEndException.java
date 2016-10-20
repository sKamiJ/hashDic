package cn.kamij.hashdic.sufac;

/**
 * 原文生成器结束异常
 * 
 * @author KamiJ
 *
 */
public class TextEndException extends Exception {
	private static final long serialVersionUID = -6625660519943867665L;

	public TextEndException() {
		super();
	}

	public TextEndException(String textCreatorType) {
		super(textCreatorType + " has ended!");
	}
}
