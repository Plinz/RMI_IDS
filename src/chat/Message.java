package chat;

import java.io.Serializable;

public class Message implements Serializable{
	
	private static final long serialVersionUID = -1574243316445337744L;
	private String from;
	private String to;
	private String data;
	private boolean isPrivate;
	
	public Message(String from, String to, String data, boolean isPrivate) {
		this.from = from;
		this.to = to;
		this.data = data;
		this.isPrivate = isPrivate;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public boolean isPrivate() {
		return isPrivate;
	}

	public void setPrivate(boolean isPrivate) {
		this.isPrivate = isPrivate;
	}

	@Override
	public String toString() {
		return "Message [from=" + from + ", to=" + to + ", data=" + data + ", isPrivate=" + isPrivate + "]";
	}
	
	
	
}
