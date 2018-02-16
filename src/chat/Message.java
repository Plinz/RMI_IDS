package chat;

import java.io.Serializable;

public class Message implements Serializable{
	
	private static final long serialVersionUID = -1574243316445337744L;
	private String from;
	private String to;
	private String data;
	private boolean isPrivate;

	/**
	 * Créer un objet Message qui sera envoyé au server. Si le message est privé on définit le boolean
	 * isPrivate à Vrai. Si le message est à destination d'un utilisateur privé, il faut spécifier le destinataire
	 * @param from
	 * @param to
	 * @param data
	 * @param isPrivate
	 */
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

	/**
	 * Donne le destinataire du message lorsque le message est privé
	 * @return Le nom de l'utilisateur destinataire du message
	 */
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

	/**
	 * Définit si le message est privé ou public
	 * @param isPrivate
	 */
	public void setPrivate(boolean isPrivate) {
		this.isPrivate = isPrivate;
	}

	@Override
	public String toString() {
		return "Message [from=" + from + ", to=" + to + ", data=" + data + ", isPrivate=" + isPrivate + "]";
	}
	
	
	
}
